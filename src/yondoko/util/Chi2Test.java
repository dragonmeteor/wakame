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

package yondoko.util;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.special.Gamma;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Chi^2 test.
 */
public class Chi2Test {
    /**
     * Perform a Chi^2 test based on the given frequency tables.
     * @param nCells the number of table cells
     * @param obsFrequencies observed cell frequencies in each cell
     * @param expFrequencies integrated cell frequencies in each cell (i.e., the noise-free reference)
     * @param sampleCount total observed sample count
     * @param minExpFrequency minimum expected cell frequency.  The chi^2 test does not work reliably
     *                        when the expected frequency in a cell is low (e.g., less than 5) because
     *                        normally assumptions break down in this case.  Therefore, the implementation
     *                        will merge such low-frewquency cells when they fall below the threshold
     *                        specified here.
     * @param significanceLevel the null hypothesis willl be rejected when the associated p-value
     *                          is below the signifiance level specified here
     * @param numTests specifies the total number of tests that will be executed.  If greater than one,
     *                 the Sidak correction will be applied to the significance level.  This is because
     *                 by conducting multiple independent hypothesis tests in sequence, the probabiliy
     *                 of a failture increases accordingly.
     * @return a pair of values containing the test result (success->true and failure->false) and
     *         a descriptive string
     */
    public static Pair<Boolean, String> chi2Test(int nCells, double[] obsFrequencies, double[] expFrequencies,
                                                 int sampleCount, double minExpFrequency,
                                                 double significanceLevel, int numTests) {
        /* Sort all cells by their expected frequencies */
        ArrayList<Pair<Double, Integer>> cells = new ArrayList<>();
        for (int i = 0; i < nCells; i++) {
            Pair<Double, Integer> cell = new ImmutablePair<>(expFrequencies[i], i);
            cells.add(cell);
        }
        Collections.sort(cells, (Pair<Double, Integer> a, Pair<Double,Integer> b) -> {
            return Double.compare(a.getLeft(), b.getLeft());
        });

        /* Compute the Chi^2 statistic and pool cells as necessary */
        double pooledFrequencies = 0;
        double pooledExpFrequencies = 0;
        double chsq = 0;
        int pooledCell = 0;
        int dof = 0;

        StringBuilder builder = new StringBuilder();
        for (Pair<Double, Integer> c : cells) {
            if (expFrequencies[c.getRight()] == 0) {
                if (obsFrequencies[c.getRight()] > sampleCount * 1e-5) {
                    /* Uh oh: samples in a cell that should be completely empty
					   according to the probability density function. Ordinarily,
					   even a single sample requires immediate rejection of the null
					   hypothesis. But due to finite-precision computations and rounding
					   errors, this can occasionally happen without there being an
					   actual bug. Therefore, the criterion here is a bit more lenient. */
                    builder.append("Encountered " + obsFrequencies[c.getRight()] + " samples " +
                            "in a cell with expected frequency 0.  Rejecting the null hypothesis\n");
                    return new ImmutablePair<>(false, builder.toString());
                }
            } else if (expFrequencies[c.getRight()] < minExpFrequency) {
                /* Pool cells with low expected frequencies */
                pooledFrequencies += obsFrequencies[c.getRight()];
                pooledExpFrequencies += expFrequencies[c.getRight()];
                pooledCell++;
            } else if (pooledExpFrequencies > 0 && pooledExpFrequencies < minExpFrequency) {
                /* Keep on pooling cells until a sufficiently high
				   expected frequency is achieved. */
                pooledFrequencies += obsFrequencies[c.getRight()];
                pooledExpFrequencies += expFrequencies[c.getRight()];
                pooledCell++;
            } else {
                double diff = obsFrequencies[c.getRight()] - expFrequencies[c.getRight()];
                chsq += (diff*diff) / expFrequencies[c.getRight()];
                ++dof;
            }
        }

        if (pooledExpFrequencies > 0 || pooledFrequencies > 0) {
            builder.append("Pooled " + pooledCell + " cells to ensure sufficiently high expected cell frequencies (>" +
                    minExpFrequency + ")\n");
            double diff = pooledFrequencies - pooledExpFrequencies;
            chsq += (diff*diff) / pooledExpFrequencies;
            ++dof;
        }

        /* All parameters are assumed to be known, so there is no
		   additional DF reduction due to model parameters */
        dof -= 1;

        if (dof <= 0) {
            builder.append("The number of degrees of freedom (" + dof + ") is too low!\n");
            return new ImmutablePair<>(false, builder.toString());
        }

        builder.append("Chi^2 statistic = " + chsq + " (d.o.f = " + dof + ")\n");

        /* Probability of obtaining a test statistic at least
		   as extreme as the one observed under the assumption
		   that the distributions match */
        double pValue = 1 - chi2Cdf(chsq, dof);

        /* Apply the Sidak correction term, since we'll be conducting multiple independent
		   hypothesis tests. This accounts for the fact that the probability of a failure
		   increases quickly when several hypothesis tests are run in sequence. */
        double alpha = 1.0 - Math.pow(1.0 - significanceLevel, 1.0 / numTests);

        boolean result = false;
        if (pValue < alpha || !Double.isFinite(pValue)) {
            builder.append("***** Rejected ***** the null hypothesis (p-value = " + pValue +
                    ", significance level = " + alpha + ")");
        } else {
            builder.append("***** Accepted ***** the null hypothesis (p-value = " + pValue +
                    ", significance level = " + alpha + ")");
            result = true;
        }
        return new ImmutablePair<>(result, builder.toString());
    }

    public static Pair<Boolean, String> chi2Test(int nCells, double[] obsFrequencies, double[] expFrequencies,
                                                 int sampleCount, double minExpFrequency, double significanceLevel) {
        return chi2Test(nCells, obsFrequencies, expFrequencies, sampleCount, minExpFrequency, significanceLevel, 1);
    }

    /// Regularized lower incomplete gamma function
    private static double rlgamma(double a, double x) {
        final double epsilon = 0.000000000000001;
        final double big = 4503599627370496.0;
        final double bigInv = 2.22044604925031308085e-16;
        if (a < 0 || x < 0)
            throw new RuntimeException("rlgamma: invalid arguments range!");

        if (x == 0)
            return 0.0f;

        double ax = (a * Math.log(x)) - x - Gamma.logGamma(a);
        if (ax < -709.78271289338399)
            return a < x ? 1.0 : 0.0;

        if (x <= 1 || x <= a) {
            double r2 = a;
            double c2 = 1;
            double ans2 = 1;

            do {
                r2 = r2 + 1;
                c2 = c2 * x / r2;
                ans2 += c2;
            } while ((c2 / ans2) > epsilon);

            return Math.exp(ax) * ans2 / a;
        }

        int c = 0;
        double y = 1 - a;
        double z = x + y + 1;
        double p3 = 1;
        double q3 = x;
        double p2 = x + 1;
        double q2 = z * x;
        double ans = p2 / q2;
        double error;

        do {
            c++;
            y += 1;
            z += 2;
            double yc = y * c;
            double p = (p2 * z) - (p3 * yc);
            double q = (q2 * z) - (q3 * yc);

            if (q != 0) {
                double nextans = p / q;
                error = Math.abs((ans - nextans) / nextans);
                ans = nextans;
            } else {
                // zero div, skip
                error = 1;
            }

            // shift
            p3 = p2;
            p2 = p;
            q3 = q2;
            q2 = q;

            // normalize fraction when the numerator becomes large
            if (Math.abs(p) > big) {
                p3 *= bigInv;
                p2 *= bigInv;
                q3 *= bigInv;
                q2 *= bigInv;
            }
        } while (error > epsilon);

        return 1.0 - (Math.exp(ax) * ans);
    }

    private static double chi2Cdf(double x, int dof) {
        if (dof < 1 || x < 0) {
            return 0.0;
        } else if (dof == 2) {
            return 1.0 - Math.exp(-0.5*x);
        } else {
            return rlgamma(0.5 * dof, 0.5 * x);
        }
    }
}
