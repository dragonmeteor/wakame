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

import javax_.vecmath.Vector3d;

/**
 * Convenience data structure used to pass multiple parameters
 * to the evaluation and sampling routines in the PhaseFunction class.
 */
public class PhaseFunctionQueryRecord {
    /**
     * Incident direction (in the world frame).
     */
    public Vector3d wi = new Vector3d();
    /**
     * Outgoing direction (in the world frame).
     */
    public Vector3d wo = new Vector3d();
    /**
     * Sampling direction
     */
    public PhaseFunctionSampledDirection direction;
}
