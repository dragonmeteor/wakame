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

package wakame.phase;

import wakame.WakameObject;
import wakame.sampler.Sampler;

import javax_.vecmath.Vector3d;

/**
 * Represents a phase function.
 */
public abstract class PhaseFunction extends WakameObject {
    /**
     * Sample the phase function and evaluated its weighted value, and return the
     * value of the phase function at the sampled direction divided by the probability
     * of the sampling the direction.
     * @param pRec the phase function query record
     * @param sampler the sampler
     * @return the value of the phase function divided by the probability of sampling
     */
    public abstract double sample(PhaseFunctionQueryRecord pRec, Sampler sampler);

    /**
     * Evaluate the phase function.
     * @param wi the incident direction
     * @param wo the outgoing direction
     * @return the value of the phase function
     */
    public abstract double eval(Vector3d wi, Vector3d wo);

    /**
     * Evaluating the probability of sampling the direction indicated in the given PhaseFunctionQueryRecord.
     * @param pRec the phase function query record
     * @return the probability of sampling the direction indicated in the record
     */
    public abstract double pdf(PhaseFunctionQueryRecord pRec);
}
