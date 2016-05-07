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

package yondoko.util;

public class VectorMathUtil {
    public static final javax_.vecmath.Matrix4f IDENTITY_MATRIX = new javax_.vecmath.Matrix4f();

    static {
        IDENTITY_MATRIX.setIdentity();
    }

    /**
     * Create a projection matrix as used by OpenGL
     * @param fovy field of view in the Y direction in degrees
     * @param aspect aspect ratio
     * @param near near z cutoff
     * @param far far z cutoff
     * @return
     */
    public static javax_.vecmath.Matrix4f createProjectionMatrix(float fovy, float aspect, float near, float far) {
        javax_.vecmath.Matrix4f M = new javax_.vecmath.Matrix4f();
        makeProjectionMatrix(M, fovy, aspect, near, far);
        return M;
    }

    public static void makeProjectionMatrix(javax_.vecmath.Matrix4f M, float fovy, float aspect, float near, float far) {
        float f = (float)(1.0 / Math.tan(Math.toRadians(fovy / 2.0)));
        M.setZero();
        M.m00 = f / aspect;
        M.m11 = f;
        M.m22 = (far + near)/(near - far);
        M.m23 = (2*far*near) /(near - far);
        M.m32 = -1;
    }

    public static void makeOrthographicMatrix(javax_.vecmath.Matrix4f M, float height, float aspect, float near, float far) {
        M.setZero();
        M.m00 = 2 / height / aspect;
        M.m11 = 2 / height;
        M.m22 = 2 / (near - far);
        M.m23 = (near + far) / (near - far);
        M.m33 = 1;
    }

    public static javax_.vecmath.Matrix4f createLookAtMatrix(float eyeX, float eyeY, float eyeZ,
                                              float atX, float atY, float atZ,
                                              float upX, float upY, float upZ) {
        javax_.vecmath.Matrix4f M = new javax_.vecmath.Matrix4f();
        makeLookAtMatrix(M, eyeX, eyeY, eyeZ, atX, atY, atZ, upX, upY, upZ);
        return M;
    }

    public static void makeLookAtMatrix(javax_.vecmath.Matrix4f M, float eyeX, float eyeY, float eyeZ,
                                        float atX, float atY, float atZ,
                                        float upX, float upY, float upZ) {
        javax_.vecmath.Vector3f z = new javax_.vecmath.Vector3f(eyeX - atX, eyeY - atY, eyeZ - atZ);
        z.normalize();
        javax_.vecmath.Vector3f y = new javax_.vecmath.Vector3f(upX, upY, upZ);
        y.normalize();
        javax_.vecmath.Vector3f x = new javax_.vecmath.Vector3f();
        x.cross(y, z);
        x.normalize();
        y.cross(z, x);
        y.normalize();

        M.m00 = x.x;
        M.m10 = x.y;
        M.m20 = x.z;
        M.m30 = 0;

        M.m01 = y.x;
        M.m11 = y.y;
        M.m21 = y.z;
        M.m31 = 0;

        M.m02 = z.x;
        M.m12 = z.y;
        M.m22 = z.z;
        M.m32 = 0;

        M.m03 = eyeX;
        M.m13 = eyeY;
        M.m23 = eyeZ;
        M.m33 = 1;

        M.invert();
    }

    /**
     * Complete the set {a} to an orthonormal base
     * @param a a vector
     * @param b the receiver of one basis vector
     * @param c the receiver of one basis vector
     */
    public static void coordinateSystem(javax_.vecmath.Vector3d a, javax_.vecmath.Vector3d b, javax_.vecmath.Vector3d c) {
        a.normalize();
        if (Math.abs(a.x) > Math.abs(a.y)) {
            double invLen = 1.0 / Math.sqrt(a.x * a.x + a.z * a.z);
            c.set(a.z * invLen, 0.0f, -a.x * invLen);
        } else {
            double invLen = 1.0f / Math.sqrt(a.y * a.y + a.z * a.z);
            c.set(0.0f, a.z * invLen, -a.y * invLen);
        }
        b.cross(c, a);
    }

    public static void coordinateSystem(javax_.vecmath.Vector3f normal, javax_.vecmath.Vector3f tangent, javax_.vecmath.Vector3f binormal) {
        normal.normalize();

        float nx = Math.abs(normal.x);
        float ny = Math.abs(normal.y);
        float nz = Math.abs(normal.z);

        if (nx > ny && nx > nz) {
            tangent.set(-normal.y, normal.x, 0);
        } else if (ny > nx && ny > nz) {
            tangent.set(0, -normal.z, normal.y);
        } else {
            tangent.set(normal.z, 0, -normal.x);
        }
        tangent.normalize();

        binormal.cross(normal, tangent);
        binormal.normalize();
    }

    /**
     * Rotates the tuple (vector or point) by a quaternion.
     *
     * Just does `quat * tuple * inverse(quat)`.
     *
     * @param quat The quaternion to rotate by.
     * @param tuple The tuple to rotate. The rotation is done in-place; on
     *        output, `tuple` has been rotated by `quat`.
     */
    public static void rotateTuple(javax_.vecmath.Quat4f quat, javax_.vecmath.Tuple3f tuple)
    {
        if (tuple.x == 0.0f && tuple.y == 0.0f && tuple.z == 0.0f)
        {
            return;
        }

		/* Quat4f.mul() implicitly normalizes the result, so remember the length. */
        float length = (float)Math.sqrt(tuple.x * tuple.x + tuple.y * tuple.y + tuple.z * tuple.z);
        tuple.scale(1.0f / length);

		/* quat * tuple * inverse(quat) */
        javax_.vecmath.Quat4f temp = new javax_.vecmath.Quat4f(quat);
        temp.mul(new javax_.vecmath.Quat4f(tuple.x, tuple.y, tuple.z, 0.0f));
        temp.mulInverse(quat);

        tuple.x = temp.x * length;
        tuple.y = temp.y * length;
        tuple.z = temp.z * length;
    }

    public static void set3x3Part(javax_.vecmath.Matrix3f A, javax_.vecmath.Matrix4f B) {
        A.m00 = B.m00;
        A.m01 = B.m01;
        A.m02 = B.m02;
        A.m10 = B.m10;
        A.m11 = B.m11;
        A.m12 = B.m12;
        A.m20 = B.m20;
        A.m21 = B.m21;
        A.m22 = B.m22;
    }

    /**
     * Compute a direction for the given coordinates in spherical coordinates.
     * @param theta the elevation angle, in radian
     * @param phi the azimuthal angle, in radian
     * @param output the receiver of the direction
     */
    public static void sphericalDirection(double theta, double phi, javax_.vecmath.Tuple3d output) {
        double sinTheta = Math.sin(theta);
        double cosTheta = Math.cos(theta);
        double sinPhi = Math.sin(phi);
        double cosPhi = Math.cos(phi);

        output.set(
                sinTheta * cosPhi,
                sinTheta * sinPhi,
                cosTheta
        );
    }
}
