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

import org.apache.commons.math3.distribution.TDistribution;
import wakame.Scene;
import wakame.WakameObject;
import wakame.bsdf.Bsdf;
import wakame.bsdf.BsdfQueryRecord;
import wakame.bsdf.BsdfSampledDirection;
import wakame.camera.Camera;
import wakame.integrator.Integrator;
import wakame.sampler.Independent;
import wakame.sampler.Sampler;
import wakame.struct.Color3d;
import wakame.struct.Ray;
import wakame.util.PropertiesUtil;
import yondoko.util.VectorMathUtil;

import javax_.vecmath.Vector2d;
import javax_.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Student's t-test for the equality of means
 *
 * This test analyzes whether the expected value of a random variable matches a
 * certain known value. When there is significant statistical "evidence"
 * against this hypothesis, the test fails.
 *
 * This is useful in checking whether a Monte Carlo method method converges
 * against the right value. Because statistical tests are able to handle the
 * inherent noise of these methods, they can be used to construct statistical
 * test suites not unlike the traditional unit tests used in software engineering.
 *
 * This implementation can be used to test two things:
 *
 * 1. that the illumination scattered by a BRDF model under uniform illumination
 *    into a certain direction matches a given value (modulo noise).
 *
 * 2. that the average radiance received by a camera within some scene
 *    matches a given value (modulo noise).
 */
public class StudentsTTest extends WakameObject {
    /**
     * The null hypothesis will be rejected when the associated
     * p-value is below the significance level specified here.
     */
    private double significanceLevel;
    /**
     * Number of BSDF samples that should be generated (default: 100K).
     */
    private int sampleCount;
    /**
     * The list of incident angles that will be tested for each BSDF.
     */
    ArrayList<Double> angles = new ArrayList<Double>();
    /**
     * Reference values for each angles or scenes.
     */
    ArrayList<Double> references = new ArrayList<Double>();
    /**
     * BSDFs to be tested.
     */
    ArrayList<Bsdf> bsdfs = new ArrayList<Bsdf>();
    /**
     * Scenes to be tested.
     */
    ArrayList<Scene> scenes = new ArrayList<Scene>();

    private StudentsTTest() {
        // NO-OP
    }

    @Override
    protected void setProperties(HashMap<String, Object> properties) {
        significanceLevel = PropertiesUtil.getDouble(properties, "significanceLevel", 0.01);

        String angleString = PropertiesUtil.getString(properties, "angles", "");
        angleString = angleString.trim();
        if (angleString.length() > 0) {
            String[] comps = angleString.split("[\\s,]+");
            for (int i = 0; i < comps.length; i++) {

                angles.add(Double.valueOf(comps[i]));
            }
        }

        String referenceString = PropertiesUtil.getString(properties, "references", "");
        referenceString = referenceString.trim();
        if (referenceString.length() > 0) {
            String[] comps = referenceString.split("[\\s,]+");
            for (int i = 0; i < comps.length; i++) {
                references.add(Double.valueOf(comps[i]));
            }
        }

        sampleCount = PropertiesUtil.getInteger(properties, "sampleCount", 100000);
    }

    @Override
    protected void addChild(WakameObject obj) {
        if (obj instanceof Bsdf) {
            bsdfs.add((Bsdf) obj);
        } else if (obj instanceof Scene) {
            scenes.add((Scene)obj);
        } else {
            throw new RuntimeException("StudentsTTest.addChild(): Child of type " + obj.getClass().getName()
                    + " is not supported.");
        }
    }

    @Override
    protected void activate() {
        if (!bsdfs.isEmpty()) {
            testBsdfs();
        } else {
            testScenes();
        }
    }

    private void testBsdfs() {
        int total = 0;
        int passed = 0;

        if (references.size() != bsdfs.size() * angles.size()) {
            throw new RuntimeException("#references != #bsdfs * #angles");
        }
        if (!scenes.isEmpty()) {
            throw new RuntimeException("Cannot test BSDFs and scenes at the same time!");
        }

        // Test each registered BSDF
        int refIndex = 0;
        Sampler sampler = (Sampler)new Independent.Builder().build();
        for(Bsdf bsdf : bsdfs) {
            for (int i = 0; i < angles.size(); i++) {
                double angle = angles.get(i);
                double reference = references.get(refIndex);
                refIndex++;

                System.out.println("------------------------------------------------------");
                System.out.println(String.format("Testing (angle=%f): %s", angle, bsdf.toString()));
                total++;

                Vector3d wo = new Vector3d();
                Color3d value = new Color3d();
                VectorMathUtil.sphericalDirection(angle * Math.PI / 180, 0, wo);
                BsdfQueryRecord bRec = new BsdfQueryRecord();
                bRec.wo.set(wo);
                bRec.direction = BsdfSampledDirection.Wi;

                System.out.println(String.format("Drawing %d samples ...", sampleCount));
                double mean = 0;
                double variance = 0;
                for (int k = 0; k < sampleCount; k++) {
                    bsdf.sample(bRec, sampler);
                    bsdf.eval(bRec.wi, bRec.wo, bRec.measure, value);
                    double result = value.getLuminance();

                    /* Numerically robust online variance estimation using an
                    algorithm proposed by Donald Knuth (TAOCP vol.2, 3rd ed., p.232) */
                    double delta = result - mean;
                    mean += delta / (double) (k+1);
                    variance += delta * (result - mean);
                }
                variance /= (sampleCount-1);
                System.out.println("reference = " + reference);
                System.out.println("sample mean = " + mean);
                System.out.println("sample variance = " + variance);
                System.out.println("sample stddev = " + Math.sqrt(variance));
                System.out.println("variance of mean = " + variance / sampleCount);
                System.out.println("stddev of mean = " + Math.sqrt(variance / sampleCount));

                double stddev = Math.sqrt(Math.max(variance / sampleCount, 1e-6));
                double t = Math.abs(reference - mean) / stddev;
                TDistribution tDist = new TDistribution(sampleCount-1);
                double pValue = 2*(tDist.cumulativeProbability(-t));

                /* Apply the Sidak correction term, since we'll be conducting multiple independent
                hypothesis tests. This accounts for the fact that the probability of a failure
                increases quickly when several hypothesis tests are run in sequence. */
                double alpha = 1.0 - Math.pow(1.0 - significanceLevel, 1.0 / references.size());

                if (variance == 0) {
                    if (Math.abs(reference - mean) < 1e-12) {
                        passed++;
                        System.out.println("Accepted the null hypothesis because variance = 0 and error < 1e-12.");
                    } else {
                        System.out.println("Rejected the null hypothesis because variance = 0 and error > 1e-12.");
                    }
                } else {
                    if (pValue < alpha) {
                        System.out.println("***** Rejected ***** the null hypothesis (p-value = "
                                + pValue + ", significance level = " + alpha + ")");
                    } else {
                        passed++;
                        System.out.println("Accepted the null hypothesis (p-value = " +
                                pValue + ", significance level = " + alpha + ")");
                    }
                }
            }
        }
        System.out.println("\n");
        System.out.println("Passed " + passed + "/" + total + " tests");
    }

    private void testScenes() {
        int total = 0;
        int passed = 0;

        if (references.size() != scenes.size()) {
            throw new RuntimeException("Specified a different number of scenes and reference values!");
        }

        Independent sampler = (Independent)new Independent.Builder().build();

        int refIndex = 0;
        for (Scene scene : scenes) {
            Integrator integrator = scene.getIntegrator();
            Camera camera = scene.getCamera();
            double reference = references.get(refIndex);
            refIndex++;

            System.out.println("------------------------------------------------------");
            System.out.println(String.format("Testing scene: %s", scene.toString()));
            total++;

            System.out.println(String.format("Generating %d paths ...", sampleCount));

            Ray ray = new Ray();
            Vector2d mu1 = new Vector2d();
            Vector2d mu2 = new Vector2d();
            Color3d value = new Color3d();
            Color3d Li = new Color3d();
            double mean = 0;
            double variance = 0;
            for (int k = 0; k < sampleCount; k++) {
                /* Sample a ray from the camera */
                sampler.next2D(mu1);
                sampler.next2D(mu2);
                camera.sampleRay(mu1, mu2, ray, value);

                /* Compute the incident radiance */
                integrator.Li(scene, sampler, ray, Li);
                value.mul(Li);
                double result = value.getLuminance();

                /* Numerically robust online variance estimation using an
                algorithm proposed by Donald Knuth (TAOCP vol.2, 3rd ed., p.232) */
                double delta = result - mean;
                mean += delta / (double) (k+1);
                variance += delta * (result - mean);
            }
            variance /= (sampleCount-1);
            System.out.println("reference = " + reference);
            System.out.println("sample mean = " + mean);
            System.out.println("sample variance = " + variance);
            System.out.println("sample stddev = " + Math.sqrt(variance));
            System.out.println("variance of mean = " + variance / sampleCount);
            System.out.println("stddev of mean = " + Math.sqrt(variance / sampleCount));

            if (variance == 0) {
                if (Math.abs(reference - mean) < 1e-12) {
                    passed++;
                    System.out.println("Accepted the null hypothesis because variance = 0 and error < 1e-12.");
                } else {
                    System.out.println("Rejected the null hypothesis because variance = 0 and error > 1e-12.");
                }
            } else {
                double stddev = Math.sqrt(Math.max(variance / sampleCount, 1e-6));
                double t = Math.abs(reference - mean) / stddev;
                TDistribution tDist = new TDistribution(sampleCount-1);
                double pValue = 2*(tDist.cumulativeProbability(-t));;

                /* Apply the Sidak correction term, since we'll be conducting multiple independent
                hypothesis tests. This accounts for the fact that the probability of a failure
                increases quickly when several hypothesis tests are run in sequence. */
                double alpha = 1.0 - Math.pow(1.0 - significanceLevel, 1.0 / references.size());

                if (pValue < alpha) {
                    System.out.println("***** Rejected ***** the null hypothesis (p-value = "
                            + pValue + ", significance level = " + alpha + ")");
                } else {
                    passed++;
                    System.out.println("Accepted the null hypothesis (p-value = " +
                            pValue + ", significance level = " + alpha + ")");
                }
            }
        }

        System.out.println("\n");
        System.out.println("Passed " + passed + "/" + total + " tests");
    }

    public static class Builder extends WakameObject.Builder {
        @Override
        protected WakameObject createInstance() {
            return new StudentsTTest();
        }
    }
}
