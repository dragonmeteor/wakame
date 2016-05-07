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

import javax_.vecmath.Matrix4d;

/**
 * Represents a 3D affine transformation, its inverse, and inverse transfose.
 */
public class Transform {
    /**
     * The matrix of the transformation.
     */
    public Matrix4d m = new Matrix4d();
    /**
     * The inverse of the transformation.
     */
    public Matrix4d mi = new Matrix4d();
    /**
     * The inverse transpose of the transformation.
     */
    public Matrix4d mit = new Matrix4d();

    /**
     * Creates the identity transformation.
     */
    public Transform() {
        m.setIdentity();
        mi.setIdentity();
        mit.setIdentity();
    }

    /**
     * Create a transformation from a given matrix.
     * @param m the matrix of the transformation.
     */
    public Transform(Matrix4d m) {
        this.m.set(m);
        mi.invert(m);
        mit.set(mi);
        mit.transpose();
    }

    /**
     * Copy from another Transform.
     * @param other the transform to copy from
     */
    public void set(Transform other) {
        this.m.set(other.m);
        this.mi.set(other.mi);
        this.mit.set(other.mit);
    }

    /**
     * Set the value of this transform to the inverse of the given transfrom T.
     * @param T
     */
    public void invert(Transform T) {
        m.set(T.mi);
        mi.set(T.m);
        mit.set(mi);
        mit.transpose();
    }

    /**
     * Invert the transformation in place.
     */
    public void invert() {
        Matrix4d temp = new Matrix4d();
        temp.set(this.m);
        this.m.set(this.mi);
        this.mi.set(temp);
        this.mit.set(this.mi);
        this.mit.transpose();
    }

    public String toString() {
        return m.toString();
    }
}
