/*
 * This file is part of Wakame, a Java reimplementation of Nori, an educational ray tracer by Wenzel Jakob.
 *
 * Copyright (c) 2015 by Pramook Khungurn
 *
 * Wakame is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License Version 3
 * as published by the Free Software Foundation.
 *
 * Wakame is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package wakame.test;

import org.apache.commons.lang3.tuple.Pair;
import wakame.WakameObject;
import wakame.bsdf.Bsdf;
import wakame.bsdf.BsdfQueryRecord;
import wakame.bsdf.BsdfSampledDirection;
import wakame.phase.PhaseFunction;
import wakame.phase.PhaseFunctionQueryRecord;
import wakame.phase.PhaseFunctionSampledDirection;
import wakame.sampler.Independent;
import wakame.sampler.Sampler;
import wakame.struct.Measure;
import wakame.util.PropertiesUtil;
import wakame.util.Warp;
import yondoko.util.AdaptiveSimpson;
import yondoko.util.VectorUtil;

import javax_.vecmath.Point2d;
import javax_.vecmath.Vector3d;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;

/**
 * Statistical test for validating that an importance sampling routine
 * (e.g. from a BSDF) produces a distribution that agrees with what the
 * implementation claims via its associated density function.
 */
public class Chi2Test extends WakameObject {
    private double significanceLevel;
    private int thetaResolution;
    private int minExpFrequency;
    private int sampleCount;
    private int testCount;
    private int phiResolution;
    private ArrayList<Bsdf> bsdfs = new ArrayList<Bsdf>();
    private ArrayList<PhaseFunction> phaseFunctions = new ArrayList<PhaseFunction>();

    private Chi2Test() {
        // NO-OP
    }

    @Override
    protected void setProperties(HashMap<String, Object> properties) {
        /* The null hypothesis will be rejected when the associated
           p-value is below the significance level specified here. */
        significanceLevel = PropertiesUtil.getDouble(properties, "significanceLevel", 0.01);

        /* Number of cells along the latitudinal axis. The azimuthal
           resolution is twice this value. */
        thetaResolution = PropertiesUtil.getInteger(properties, "resolution", 10);

        /* Minimum expected bin frequency. The chi^2 test does not
           work reliably when the expected frequency in a cell is
           low (e.g. less than 5), because normality assumptions
           break down in this case. Therefore, the implementation
           will merge such low-frequency cells when they fall below
           the threshold specified here. */
        minExpFrequency = PropertiesUtil.getInteger(properties, "minExpFrequency", 5);

        /* Number of samples that should be taken (-1: automatic) */
        sampleCount = PropertiesUtil.getInteger(properties, "sampleCount", -1);

        /* Each provided BSDF will be tested for a few different
           incident directions. The value specified here determines
           how many tests will be executed per BSDF */
        testCount = PropertiesUtil.getInteger(properties, "testCount", 5);

        phiResolution = 2 * thetaResolution;

        if (sampleCount < 0) // ~5K samples per bin
            sampleCount = thetaResolution * phiResolution * 5000;
    }

    @Override
    public void addChild(WakameObject obj) {
        if (obj instanceof Bsdf) {
            bsdfs.add((Bsdf)obj);
        } else if (obj instanceof PhaseFunction) {
            phaseFunctions.add((PhaseFunction)obj);
        } else {
            throw new RuntimeException("Chi2Test.addChild(): A child of type "
                + obj.getClass().getName() + " is not supported.");
        }
    }

    @Override
    protected void activate() {
        int passed = 0;
        int total = 0;
        int res = thetaResolution * phiResolution;
        Sampler random = (Sampler)new Independent.Builder().build();

        double[] obsFrequencies = new double[res];
        double[] expFrequencies = new double[res];

        int totalCount = (bsdfs.size() + phaseFunctions.size()) * testCount;

        for (Bsdf bsdf : bsdfs) {
            for (int l = 0; l < testCount; l++) {
                for (int i = 0; i < res; i++) {
                    obsFrequencies[i] = 0;
                    expFrequencies[i] = 0;
                }

                System.out.println("------------------------------------------------------");
                System.out.println("Testing: " + bsdf.toString());
                ++total;

                double cosTheta = random.next1D();
                double sinTheta = Math.sqrt(Math.max(0, 1 - cosTheta * cosTheta));
                double phi = 2*Math.PI*random.next1D();
                double sinPhi = Math.sin(phi);
                double cosPhi = Math.cos(phi);
                Vector3d wo = new Vector3d(sinTheta * cosPhi, sinTheta * sinPhi, cosTheta);

                System.out.println("wo = " + wo);

                System.out.print("Accumulating " + sampleCount + " samples into a " + thetaResolution
                        + "x" + phiResolution + " contingency table .. ");

                /* Generate many samples from the BSDF and create
                   a histogram / contingency table */
                BsdfQueryRecord bRec = BsdfQueryRecord.createWiSampleRecord(wo);
                int count = 0;
                for (int i = 0; i < sampleCount; i++) {
                    bsdf.sample(bRec, random);

                    if (VectorUtil.isZero(bRec.value))
                        continue;
                    count++;

                    int cosThetaBin = Math.min(
                            Math.max(0, (int)Math.floor((bRec.wi.z * 0.5 + 0.5) * thetaResolution)),
                            thetaResolution -1);

                    double scaledPhi = Math.atan2(bRec.wi.y, bRec.wi.x) / (2*Math.PI);
                    if (scaledPhi < 0)
                        scaledPhi += 1;

                    int phiBin = Math.min(
                            Math.max(0, (int)Math.floor(scaledPhi * phiResolution)),
                            phiResolution-1);
                    obsFrequencies[cosThetaBin * phiResolution + phiBin] += 1;
                }
                System.out.println("done.");
                System.out.println("count = " + count);

                /* Numerically integrate the probability density
                   function over rectangles in spherical coordinates. */
                System.out.print("Integrating expected frequencies .. ");
                double sum = 0;
                BiFunction<Double, Double, Double> integrand = (Double cosTheta_, Double phi_) -> {
                    double sinTheta_ = Math.sqrt(1 - cosTheta_ * cosTheta_);
                    double sinPhi_ = Math.sin(phi_);
                    double cosPhi_ = Math.cos(phi_);

                    Vector3d wi_ = new Vector3d(sinTheta_ * cosPhi_, sinTheta_ * sinPhi_, cosTheta_);

                    BsdfQueryRecord bRec_ = new BsdfQueryRecord();
                    bRec_.wi.set(wi_);
                    bRec_.wo.set(wo);
                    bRec_.measure = Measure.SolidAngle;
                    bRec_.direction = BsdfSampledDirection.Wi;

                    return bsdf.pdf(bRec_);
                };
                int ptr = 0;
                for (int i = 0; i < thetaResolution; i++) {
                    double cosThetaStart = -1.0 + i * 2.0 / thetaResolution;
                    double cosThetaEnd = -1.0 + (i+1) * 2.0 / thetaResolution;
                    for (int j = 0; j < phiResolution; j++) {
                        double phiStart = j * 2 * Math.PI / phiResolution;
                        double phiEnd = (j+1) * 2 * Math.PI / phiResolution;
                        double integral = AdaptiveSimpson.integrate2D(integrand,
                                cosThetaStart, phiStart, cosThetaEnd, phiEnd,
                                1e-10, 12);
                        expFrequencies[ptr] = integral * sampleCount;
                        sum += integral * sampleCount;
                        ptr++;
                    }
                }
                System.out.println("done.");
                System.out.println("integral = " + sum);

                /* Write the test input data to disk for debugging */
                chi2Dump(thetaResolution, phiResolution, obsFrequencies, expFrequencies,
                        String.format("chitest_%d.m", total));

                /* Perform the chi-squared test. */
                Pair<Boolean, String> testResult = yondoko.util.Chi2Test.chi2Test(thetaResolution * phiResolution,
                        obsFrequencies, expFrequencies, sampleCount, minExpFrequency, significanceLevel,
                        totalCount);

                if (testResult.getLeft()) {
                    passed++;
                }

                System.out.println(testResult.getRight());
            }
        }

        for (PhaseFunction phaseFunction : phaseFunctions) {
            for (int l = 0; l < testCount; l++) {
                for (int i = 0; i < res; i++) {
                    obsFrequencies[i] = 0;
                    expFrequencies[i] = 0;
                }

                System.out.println("------------------------------------------------------");
                System.out.println("Testing: " + phaseFunction.toString());
                ++total;

                Vector3d wo = new Vector3d();
                Warp.squareToUniformSphere(new Point2d(random.next1D(), random.next1D()), wo);

                System.out.println("wo = " + wo);

                System.out.print("Accumulating " + sampleCount + " samples into a " + thetaResolution
                        + "x" + phiResolution + " contingency table .. ");

                double factorTheta = thetaResolution / Math.PI;
                double factorPhi = phiResolution / (2*Math.PI);

                /* Generate many samples from the BSDF and create
                   a histogram / contingency table */
                PhaseFunctionQueryRecord pRec = new PhaseFunctionQueryRecord();
                pRec.wo.set(wo);
                pRec.direction = PhaseFunctionSampledDirection.Wi;
                int count = 0;
                for (int i = 0; i < sampleCount; i++) {
                    double value = phaseFunction.sample(pRec, random);

                    if (value == 0)
                        continue;
                    count++;

                    double theta = Math.acos(pRec.wi.z);
                    double phi = Math.atan2(pRec.wi.y, pRec.wi.x);
                    if (phi < 0) phi += 2*Math.PI;

                    int thetaBin = Math.min(
                            Math.max(0, (int)Math.floor(theta * factorTheta)),
                            thetaResolution -1);
                    int phiBin = Math.min(
                            Math.max(0, (int)Math.floor(phi * factorPhi)),
                            phiResolution-1);
                    obsFrequencies[thetaBin * phiResolution + phiBin] += 1;
                }
                System.out.println("done.");
                System.out.println("count = " + count);

                /* Numerically integrate the probability density
                   function over rectangles in spherical coordinates. */
                System.out.print("Integrating expected frequencies .. ");
                double sum = 0;
                BiFunction<Double, Double, Double> integrand = (Double theta_, Double phi_) -> {
                    double cosTheta_ = Math.cos(theta_);
                    double sinTheta_ = Math.sqrt(1 - cosTheta_ * cosTheta_);
                    double sinPhi_ = Math.sin(phi_);
                    double cosPhi_ = Math.cos(phi_);

                    Vector3d wi_ = new Vector3d(sinTheta_ * cosPhi_, sinTheta_ * sinPhi_, cosTheta_);
                    PhaseFunctionQueryRecord pRec_ = new PhaseFunctionQueryRecord();
                    pRec_.wo.set(wo);
                    pRec_.wi.set(wi_);

                    return phaseFunction.pdf(pRec_) * sinTheta_;
                };
                int ptr = 0;
                for (int i = 0; i < thetaResolution; i++) {
                    double thetaStart = i / factorTheta;
                    double thetaEnd = (i+1) / factorTheta;
                    for (int j = 0; j < phiResolution; j++) {
                        double phiStart = j / factorPhi;
                        double phiEnd = (j+1) / factorPhi;
                        double integral = AdaptiveSimpson.integrate2D(integrand,
                                thetaStart, phiStart, thetaEnd, phiEnd,
                                1e-10, 12);
                        expFrequencies[ptr] = integral * sampleCount;
                        sum += integral * sampleCount;
                        ptr++;
                    }
                }
                System.out.println("done.");
                System.out.println("integral = " + sum);

                /* Write the test input data to disk for debugging */
                chi2Dump(thetaResolution, phiResolution, obsFrequencies, expFrequencies,
                        String.format("chitest_%d.m", total));

                /* Perform the chi-squared test. */
                Pair<Boolean, String> testResult = yondoko.util.Chi2Test.chi2Test(thetaResolution * phiResolution,
                        obsFrequencies, expFrequencies, sampleCount, minExpFrequency, significanceLevel,
                        totalCount);

                if (testResult.getLeft()) {
                    passed++;
                }

                System.out.println(testResult.getRight());
            }
        }

        System.out.println();
        System.out.println("Passed " + passed + "/" + total + " tests.");
    }

    /**
     * Write 2D Chi^2 frequency tables to disk in a format that is nicely plottable by Octave and MATLAB
     * @param res1
     * @param res2
     * @param obsFrequencies
     * @param expFrequencies
     * @param fileName
     */
    private void chi2Dump(int res1, int res2, double[] obsFrequencies, double[] expFrequencies, String fileName) {
        try {
            FileWriter fileWriter = new FileWriter(fileName);
            BufferedWriter fout = new BufferedWriter(fileWriter);

            fout.write("obsFrequencies = [");
            for (int i = 0; i < res1; i++) {
                for (int j = 0; j < res2; j++) {
                    fout.write(Double.toString(obsFrequencies[i * res2 + j]));
                    if (j+1 < res2)
                        fout.write(", ");
                }
                if (i+1 < res1)
                    fout.write("; ");
            }
            fout.write(" ];\n");

            fout.write("expFrequencies = [");
            for (int i = 0; i < res1; i++) {
                for (int j = 0; j < res2; j++) {
                    fout.write(Double.toString(expFrequencies[i * res2 + j]));
                    if (j+1 < res2)
                        fout.write(", ");
                }
                if (i+1 < res1)
                    fout.write("; ");
            }
            fout.write(" ];\n");

            fout.write("colormap(jet);\n");
            fout.write("clf; subplot(2,1,1);\n");
            fout.write("imagesc(obsFrequencies);");
            fout.write("title('Observed frequencies');");
            fout.write("axis equal;");
            fout.write("subplot(2,1,2);");
            fout.write("imagesc(expFrequencies);");
            fout.write("axis equal;");
            fout.write("title('Expected frequencies');");

            fout.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static class Builder extends WakameObject.Builder {
        @Override
        protected WakameObject createInstance() {
            return new Chi2Test();
        }
    }
}
