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

package wakame.media;

import wakame.WakameObject;
import wakame.phase.PhaseFunction;
import wakame.sampler.Sampler;
import wakame.struct.Color3d;
import wakame.struct.Ray;

/**
 * Generic participating medium interface.
 *
 * This is the base class of all participating media implementations in Wakame
 * (e.g. homogeneous/heterogeneous). It only contains two method, since that's
 * all that is required to interface with path tracers: sampling a distance
 * and evaluating the transmittance along a ray segment.
 */
public abstract class Medium extends WakameObject {
    protected PhaseFunction phaseFunction;

    /**
     * Importance sample the distance to the next medium interaction along the specified ray.
     * @param ray the ray
     * @param sampler the sampler
     * @param mRec the record to receiver the sampled distance and its weight
     * @return true if medium sampling succeeded, and false otherwise
     */
    public abstract boolean sampleDistance(Ray ray, Sampler sampler, MediumQueryRecord mRec);

    /**
     * Evaluate the transmittance along the ray segment [mint, maxt].
     *
     * The transmittance is defined as $$ \exp(-\int_{mint}^{maxt} \sigma_t(t) dt) $$.
     *
     * The transmittance evaluation may either be deterministic, in which
     * case the sampler parameter is ignored. Or it can also be
     * random, but under the assumption that an unbiased transmittance
     * estimate is returned.
     * @param ray the ray
     * @param sampler the sampler
     * @param output the receiver of the transmittance value
     */
    public abstract void evalTransmittance(Ray ray, Sampler sampler, Color3d output);

    /**
     * Get the phase function of the medium.
     * @return the phase function of the medium
     */
    public PhaseFunction getPhaseFunction() {
        return phaseFunction;
    }

    @Override
    public void addChild(WakameObject obj) {
        if (obj instanceof PhaseFunction) {
            phaseFunction = (PhaseFunction)obj;
        } else {
            throw new RuntimeException("Medium.addChild(): Child of type " + obj.getClass().getName()
                + " is not supported");
        }
    }
}
