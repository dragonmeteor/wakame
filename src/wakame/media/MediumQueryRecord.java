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

import wakame.struct.Color3d;

/**
 * Convenient data structure used to pass/receiver multiple values from the medium interface.
 */
public class MediumQueryRecord {
    /**
     * The sampled distance along the ray.
     */
    public double t;
    /**
     * The importance weight along the sample.
     * When a position inside the medium is sampled, it
     * records the product of sigma_s and the transmittance, divided
     * by the probability per unit length. When medium sampling fails,
     * it returns the transmittance along the ray, divided by the
     * discrete probability of failure.
     */
    public Color3d weight = new Color3d();
}
