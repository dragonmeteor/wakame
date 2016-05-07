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

import yondoko.util.MathUtil;
import yondoko.util.VectorMathUtil;

import javax_.vecmath.Vector3d;

/**
 * Stores a three-dimensional orthonormal coordinate frame
 *
 * This class is mostly used to quickly convert between different
 * cartesian coordinate systems and to efficiently compute certain
 * quantities (e.g. cosTheta, tanTheta, ..).
 */
public class Frame {
    /**
     * The first tangent.
     */
    public Vector3d s = new Vector3d();
    /**
     * The second tangent.
     */
    public Vector3d t = new Vector3d();
    /**
     * The normal.
     */
    public Vector3d n = new Vector3d();

    /**
     * Construct the xyz-frame.
     */
    public Frame() {
        s.set(1,0,0);
        t.set(0,1,0);
        n.set(0,0,1);
    }

    /**
     * Construct a frame from the given basis vectors.
     * @param s the first tangent
     * @param t the second tangent
     * @param n the normal
     */
    public Frame(Vector3d s, Vector3d t, Vector3d n) {
        this.s.set(s);
        this.t.set(t);
        this.n.set(n);
    }

    /**
     * Construct a frame from the given normal.
     * @param n the normal
     */
    public Frame(Vector3d n) {
        this.n.set(n);
        VectorMathUtil.coordinateSystem(this.n, s, t);
    }

    /**
     * Copy from another Frame.
     * @param other the other frame
     */
    public void set(Frame other) {
        this.s.set(other.s);
        this.t.set(other.t);
        this.n.set(other.n);
    }

    /**
     * Change the state of this instance to that it becomes a frame with the given normal vector.
     * @param n the normal vector
     */
    public void setFromNormal(Vector3d n) {
        this.n.set(n);
        VectorMathUtil.coordinateSystem(this.n, s, t);
    }

    /**
     * Convert from world coordinates to local coordinates.
     * @param v a vector in world coordinate
     * @param output the receiver of the local coordinates
     */
    public void toLocal(Vector3d v, Vector3d output) {
        double xx = v.dot(s);
        double yy = v.dot(t);
        double zz = v.dot(n);
        output.set(xx, yy, zz);
    }

    /**
     * Convert from local coordinates to world coordinates.
     * @param v a vector in local coordinates
     * @param output the receiver of the vector in world coordinates
     */
    public void toWorld(Vector3d v, Vector3d output) {
        output.set(0,0,0);
        output.scaleAdd(v.x, s, output);
        output.scaleAdd(v.y, t, output);
        output.scaleAdd(v.z, n, output);
    }

    /**
     * Assuming that the given direction is in the local coordinate
     * system, return the cosine of the angle between the normal and v
     * @param v a vector in local coordinates
     * @return the cosine of the angle between the normal and v
     */
    public static double cosTheta(Vector3d v) {
        return v.z;
    }

    /**
     * Assuming that the given direction is in the local coordinate
     * system, return the sine of the angle between the normal and v
     */
    public static double sinTheta(Vector3d v) {
        double temp = sinTheta2(v);
        if (temp <= 0.0)
            return 0.0;
        return Math.sqrt(temp);
    }

    /**
     * Assuming that the given direction is in the local coordinate
     * system, return the tangent of the angle between the normal and v
     */
    public static double tanTheta(Vector3d v) {
        double temp = 1 - v.z*v.z;
        if (temp <= 0.0)
            return 0.0;
        return Math.sqrt(temp) / v.z;
    }

    /**
     * Assuming that the given direction is in the local coordinate
     * system, return the squared sine of the angle between the normal and v
     */
    public static double sinTheta2(Vector3d v) {
        return 1.0 - v.z * v.z;
    }

    /**
     *  Assuming that the given direction is in the local coordinate
     * system, return the sine of the phi parameter in spherical coordinates
     */
    public static double sinPhi(Vector3d v) {
        double sinTheta = Frame.sinTheta(v);
        if (sinTheta == 0.0)
            return 1.0f;
        return MathUtil.clamp(v.y / sinTheta, -1.0, 1.0);
    }

    /**
     * Assuming that the given direction is in the local coordinate
     * system, return the cosine of the phi parameter in spherical coordinates
     */
    public static double cosPhi(Vector3d v) {
        double sinTheta = Frame.sinTheta(v);
        if (sinTheta == 0.0)
            return 1.0;
        return MathUtil.clamp(v.x / sinTheta, -1.0, 1.0);
    }

    /**
     * Assuming that the given direction is in the local coordinate
     * system, return the squared sine of the phi parameter in  spherical
     * coordinates
     */
    public static double sinPhi2(Vector3d v) {
        return MathUtil.clamp(v.y * v.y / sinTheta2(v), 0.0, 1.0);
    }

    /**
     * Assuming that the given direction is in the local coordinate
     * system, return the squared cosine of the phi parameter in  spherical
     * coordinates
     */
    public static double cosPhi2(Vector3d v) {
        return MathUtil.clamp(v.x * v.x / sinTheta2(v), 0.0, 1.0);
    }

    /**
     * Equality testing
     * @param obj the object to test equality with
     * @return whether the given obj is equal to this
     */
    public boolean equals(Object obj) {
        if (obj instanceof Frame) {
            Frame other = (Frame)obj;
            return s.equals(other.s) && t.equals(other.t) && n.equals(other.n);
        } else {
            return false;
        }
    }
}
