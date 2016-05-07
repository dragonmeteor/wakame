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

package yumyai.jogl.ui;

import yondoko.util.VectorUtil;

/**
 * Arcball helper class to interactively rotate objects on-screen.
 * Taken from Wenzel Jacaob's nanogui implementation. (http://github.com/wjakob/nanogui)
 */
public class Arcball {
    private boolean active;
    private javax_.vecmath.Point2i lastPos = new javax_.vecmath.Point2i();
    private javax_.vecmath.Point2i size = new javax_.vecmath.Point2i();
    private javax_.vecmath.Quat4d quat = new javax_.vecmath.Quat4d();
    private javax_.vecmath.Quat4d incr = new javax_.vecmath.Quat4d();
    double speedFactor = 2;

    public Arcball() {
        this(2.0);
    }

    public Arcball(double speedFactor) {
        this.speedFactor = speedFactor;
        this.active = false;
        this.lastPos.set(0,0);
        this.size.set(0,0);
        this.quat.set(0, 0, 0, 1);
        this.incr.set(0, 0, 0, 1);
    }

    public Arcball(javax_.vecmath.Quat4d quat) {
        this(2.0);
        this.quat.set(quat);
    }

    public void setState(javax_.vecmath.Quat4d state) {
        this.active = false;
        this.lastPos.set(0,0);
        this.quat.set(state);
        this.incr.set(0, 0, 0, 1);
    }

    public void setSize(int width, int height) {
        size.x = width;
        size.y = height;
    }

    public int getWidth() {
        return size.x;
    }

    public int getHeight() {
        return size.y;
    }

    public double getSpeedFactor() {
        return speedFactor;
    }

    public void setSpeedFactor(double speedFactor) {
        this.speedFactor = speedFactor;
    }

    public boolean isActive() {
        return active;
    }

    public void button(int x, int y, boolean pressed) {
        active = pressed;
        lastPos.set(x,y);
        if (!active) {
            javax_.vecmath.Quat4d temp = new javax_.vecmath.Quat4d();
            temp.mul(incr, quat);
            quat.set(temp);
            quat.normalize();
        }
        incr.set(0,0,0,1);
    }

    public boolean motion(int x, int y) {
        if (!active)
            return false;

        // Base on the rotation controller from AntTweakBar
        double invMinDim = 1.0 / VectorUtil.minComponent(size);
        double w = (double)size.x;
        double h = (double)size.y;

        double ox = (speedFactor * (2*lastPos.x - w) + w) - w - 1.0;
        double tx = (speedFactor * (2*x         - w) + w) - w - 1.0;
        double oy = (speedFactor * (h - 2*lastPos.y) + h) - h - 1.0;
        double ty = (speedFactor * (h -         2*y) + h) - h - 1.0;

        ox *= invMinDim;
        oy *= invMinDim;
        tx *= invMinDim;
        ty *= invMinDim;

        javax_.vecmath.Vector3d v0 = new javax_.vecmath.Vector3d(ox, oy, 1.0);
        javax_.vecmath.Vector3d v1 = new javax_.vecmath.Vector3d(tx, ty, 1.0);
        if (v0.lengthSquared() > 1e-4 && v1.lengthSquared() > 1e-4) {
            v0.normalize();
            v1.normalize();
            javax_.vecmath.Vector3d axis = new javax_.vecmath.Vector3d();
            axis.cross(v0, v1);
            double sa = axis.length();
            double ca = v0.dot(v1);
            double angle = Math.atan2(sa, ca);
            if (tx*tx + ty*ty > 1) {
                angle *= 1 + 0.2*(Math.sqrt(tx*tx+ty*ty) - 1.0);
            }
            axis.normalize();
            incr.set(new javax_.vecmath.AxisAngle4d(axis, angle));
            if (!VectorUtil.isFinite(incr)) {
                incr.set(0,0,0,1);
            }
        }
        return true;
    }

    public void getMatrix(javax_.vecmath.Matrix4d output) {
        output.setIdentity();
        javax_.vecmath.Quat4d state = new javax_.vecmath.Quat4d();
        state.mul(incr, quat);
        output.setRotation(state);
    }
}
