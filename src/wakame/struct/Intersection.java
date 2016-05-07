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

package wakame.struct;

import wakame.mesh.Mesh;

import javax_.vecmath.Point2d;
import javax_.vecmath.Point3d;
import javax_.vecmath.Vector3d;

/**
 * Intersection data structure
 *
 * This data structure records local information about a ray-triangle intersection.
 * This includes the position, traveled ray distance, uv coordinates, as well
 * as well as two local coordinate frames (one that corresponds to the true
 * geometry, and one that is used for shading computations).
 */
public class Intersection {
    /**
     * Position of the surface interface.
     */
    public Point3d p = new Point3d();
    /**
     * Unoccluded distance along the ray.
     */
    public double t;
    /**
     * UV coordinates, if any.
     */
    public Point2d uv = new Point2d();
    /**
     * Shading frame (based on the shading normal)
     */
    public Frame shFrame = new Frame();
    /**
     * Geometric frame (based on the true geometry)
     */
    public Frame geoFrame = new Frame();
    /**
     * The associated mesh.
     */
    public Mesh mesh = null;

    /**
     * Create an uninitialized intersection record
     */
    public Intersection() {
        // NO-OP
    }

    /**
     * Transform a direction vector into the local shading frame
     * @param v the vector in world coordinates
     * @param output the receiver of the vector in local shading frame
     */
    public void toLocal(Vector3d v, Vector3d output) {
        shFrame.toLocal(v, output);
    }

    /**
     * Transform a direction vector from local to world coordinates
     * @param v the vector in the shading frame coordinates
     * @param output the receiver of the vector in world coordinates
     */
    public void toWorld(Vector3d v, Vector3d output) {
        shFrame.toWorld(v, output);
    }
}
