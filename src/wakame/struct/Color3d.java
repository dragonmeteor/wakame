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

import javax_.vecmath.Tuple3d;
import javax_.vecmath.Tuple3f;
import java.io.Serializable;
import java.awt.Color;


public class Color3d extends Tuple3d implements Serializable {
    public Color3d(float var1, float var2, float var3) {
        super(var1, var2, var3);
    }

    public Color3d(double var1, double var2, double var3) {
        super(var1, var2, var3);
    }

    public Color3d(double[] var1) {
        super(var1);
    }

    public Color3d(Tuple3f var1) {
        super(var1);
    }

    public Color3d(Tuple3d var1) {
        super(var1);
    }

    public Color3d(Color var1) {
        super((float) var1.getRed() / 255.0, (float) var1.getGreen() / 255.0F, (float) var1.getBlue() / 255.0F);
    }

    public double getLuminance() {
        return this.x * 0.212671 + this.y * 0.715160 + this.z * 0.072169;
    }

    public Color3d() {
    }

    public final void set(Color var1) {
        this.x = var1.getRed() / 255.0;
        this.y = var1.getGreen() / 255.0;
        this.z = var1.getBlue() / 255.0;
    }

    public final Color get() {
        int var1 = (int) Math.round(this.x * 255.0);
        int var2 = (int) Math.round(this.y * 255.0);
        int var3 = (int) Math.round(this.z * 255.0);
        return new Color(var1, var2, var3);
    }

    public void mul(Tuple3d v) {
        this.x *= v.x;
        this.y *= v.y;
        this.z *= v.z;
    }

    public void div(Tuple3d v) {
        this.x /= v.x;
        this.y /= v.y;
        this.z /= v.z;
    }
}
