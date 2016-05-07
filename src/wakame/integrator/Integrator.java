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

package wakame.integrator;

import wakame.Scene;
import wakame.WakameObject;
import wakame.sampler.Sampler;
import wakame.struct.Color3d;
import wakame.struct.Ray;

/**
 * Abstract integrator (i.e. a rendering technique).
 *
 * In Wakame, the different rendering techniques are collectively referred to as
 * integrators, since they perform integration over a high-dimensional
 * space. Each integrator represents a specific approach for solving
 * the light transport equation---usually favored in certain scenarios, but
 * at the same time affected by its own set of intrinsic limitations.
 */
public abstract class Integrator extends WakameObject {
    // Perform an (optional) preprocess step.
    public void preprocess(Scene scene) {
        // NO-OP
    }

    /**
     * Sample the incident radiance along the ray.
     * @param scene the scene
     * @param sampler the sample generator
     * @param ray the ray
     * @param output the receiver of the radiance value
     */
    public abstract void Li(Scene scene, Sampler sampler, Ray ray, Color3d output);
}
