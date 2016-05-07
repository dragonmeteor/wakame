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

package wakame.bsdf;

import wakame.WakameObject;
import wakame.sampler.Sampler;
import wakame.struct.Color3d;
import wakame.struct.Measure;

import javax_.vecmath.Vector3d;

/**
 * Superclass of all bidirectional scattering distribution functions
 */
public abstract class Bsdf extends WakameObject{
    /**
     * Sample the BSDF.
     *
     * The value field of the sample record should contain the "importance weight"
     * (i.e. the value of the BSDF * cos(theta_o) divided by the probability density
     * of the sample with respect to solid angles).
     *
     * @param bRec the sample record
     * @param sampler the sampler to use
     */
    public abstract void sample(BsdfQueryRecord bRec, Sampler sampler);

    /**
     * Evaluate the BSDF for a pair of directions and measure
     * @param wi the incoming direction in local frame
     * @param wo the outgoing direction in local frame
     * @param measure the measure
     * @param value the receiver of the BSDF value
     */
    public abstract void eval(Vector3d wi, Vector3d wo, Measure measure, Color3d value);

    /**
     * Compute the probability of sampling the direction specified in
     * the BsdfQueryRecord.
     *
     * @param bRec a record with detailed information on the BSDF query
     * @return the probability/density value expressed with respect to the measured specified in the record
     */
    public abstract double pdf(BsdfQueryRecord bRec);

    /**
     * Return whether or not this BRDF is diffuse. This
     * is primarily used by photon mapping to decide whether
     * or not to store photons on a surface.
     *
     * @return whether the BSDF is a diffuse BSDF.
     */
    public abstract boolean isDiffuse();
}
