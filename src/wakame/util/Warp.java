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

package wakame.util;

import wakame.sampler.Sampler;

import javax_.vecmath.Tuple2d;
import javax_.vecmath.Tuple3d;
import javax_.vecmath.Vector3d;

/**
 * A collection of useful warping functions for importance sampling
 */
public class Warp {
    /**
     * Uniformly sample a vector on the unit hemisphere with respect to solid angles (naive implementation)
     *
     * @param sampler   the sampler
     * @param northPole the position of the north pole of the hemisphere
     * @param output    the receiver of the sampled unit vector
     */
    public static void sampleUniformHemisphere(Sampler sampler, Vector3d northPole, Tuple3d output) {
        // Naive implementation using rejection sampling
        Vector3d v = new Vector3d();
        do {
            v.x = 1 - 2 * sampler.next1D();
            v.y = 1 - 2 * sampler.next1D();
            v.z = 1 - 2 * sampler.next1D();
        } while (v.lengthSquared() > 1);

        if (v.dot(northPole) < 0)
            v.negate();
        v.normalize();
        output.set(v);
    }

    /**
     * Dummy warping function: takes uniformly distributed points in a square and just returns them
     *
     * @param sample a 2D point in [0,1]^2
     * @param output the same point as the given sample
     */
    public static void squareToUniformSquare(Tuple2d sample, Tuple2d output) {
        output.set(sample);
    }

    /**
     * Probability density of squareToUniformSquare()
     *
     * @param p the sampled point
     * @return the probability density of squareToUniformSquare() to sample p
     */
    public static double squareToUniformSquarePdf(Tuple2d p) {
        if (p.x < 0 || p.y < 0 || p.x > 1 || p.y > 1)
            return 0;
        else
            return 1;
    }

    /**
     * Uniformly sample a vector on a 2D disk with radius 1, centered around the origin
     *
     * @param sample a 2D point in [0,1]^2
     * @param output the receiver of the sampled vector
     */
    public static void squareToUniformDisk(Tuple2d sample, Tuple2d output) {
        // TODO: Fill in this method.
    }

    /**
     * Probability density of squareToUniformDisk()
     *
     * @param p the sampled point
     * @return the probability density of squareToUniformDisk() to sample p
     */
    public static double squareToUniformDiskPdf(Tuple2d p) {
        // TODO: Fill in this method.
        return 0;
    }

    /**
     * Uniformly sample a vector on the unit sphere with respect to solid angles
     *
     * @param sample a point uniformly sampled from [0,1]^2
     * @param output the receiver of the sampled vector
     */
    public static void squareToUniformSphere(Tuple2d sample, Tuple3d output) {
        // TODO: Fill in this method.
    }

    /**
     * Probability density of squareToUniformSphere()
     *
     * @param v the sampled vector
     * @return the probability density of squareToUniformSphere() to sample v
     */
    public static double squareToUniformSpherePdf(Tuple3d v) {
        // TODO: Fill in this method.
        return 0;
    }

    /**
     * Uniformly sample a vector on a spherical cap around (0, 0, 1)
     * <p>
     * A spherical cap is the subset of a unit sphere whose directions
     * make an angle of less than 'theta' with the north pole. This function
     * expects the cosine of 'theta' as a parameter.
     *
     * @param sample      a point uniformly sampled from [0,1]^2
     * @param cosThetaMax the maximum value of the cosine of the longitudinal angle between the sample vector and the vector
     *                    (0, 0, 1)
     * @param output      the receiver of the sampled vector
     */
    public static void squareToUniformSphereCap(Tuple2d sample, double cosThetaMax, Tuple3d output) {
        // TODO: Fill in this method.
    }

    /**
     * Probability density of squareToUniformSphereCap()
     *
     * @param v           the sampled vector
     * @param cosThetaMax the cosine value of the maximum longitudinal angle between the sample vector
     *                    and the vector (0, 0, 1)
     * @return the probability density of squareToUniformSphereCap() to sample v
     */
    public static double squareToUniformSphereCapPdf(Tuple3d v, double cosThetaMax) {
        // TODO: Fill in this method.
        return 0;
    }

    /**
     * Uniformly sample a vector on the unit hemisphere around the pole (0,0,1) with respect to solid angles
     * (fast implementation)
     *
     * @param sample a point uniformly sampled from [0,1]^2
     * @param output the receiver of the sampled vector
     */
    public static void squareToUniformHemisphere(Tuple2d sample, Tuple3d output) {
        // TODO: Fill in this method.
    }

    /**
     * Probability density of squareToUniformHemisphere()
     *
     * @param v the sampled vector
     * @return the probability density of squareToUniformSphere() to sample v
     */
    public static double squareToUniformHemispherePdf(Tuple3d v) {
        // TODO: Fill in this method.
        return 0;
    }

    /**
     * Uniformly sample a vector on the unit hemisphere around the pole (0,0,1) with respect to projected solid angles
     *
     * @param sample a point uniformly sampled from in [0,1]^2
     * @param output the receiver of the sampled vector
     */
    public static void squareToCosineHemisphere(Tuple2d sample, Tuple3d output) {
        // TODO: Fill in this method.
    }

    /**
     * Probability density of squareToCosineHemisphere()
     *
     * @param v the sampled vector
     * @return the probability density of squareToCosineHemisphere() to sample v
     */
    public static double squareToCosineHemispherePdf(Tuple3d v) {
        // TODO: Fill in this method.
        return 0;
    }

    /**
     * Warp a uniformly distributed square sample to a Beckmann distribution * cosine for the given 'alpha' parameter
     *
     * @param sample a point uniformly sampled from [0,1]^2
     * @param alpha  the alpha parameter of the Beckmann distribution
     * @param output the receiver of the sampled vector
     */
    public static void squareToBeckmann(Tuple2d sample, double alpha, Tuple3d output) {
        // TODO: Fill in this method.
    }

    /**
     * Probability density of squareToBeckmann()
     *
     * @param v     the sampled vector
     * @param alpha the alpha parameter of the Beckmann distribution
     * @return the probability density of squareToBeckmann() to sample m
     */
    public static double squareToBeckmannPdf(Tuple3d v, double alpha) {
        // TODO: Fill in this method.
        return 0;
    }
}
