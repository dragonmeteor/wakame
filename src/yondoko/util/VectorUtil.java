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

public class VectorUtil {
    public static int getComponent(javax_.vecmath.Tuple4i v, int i) {
        switch (i) {
            case 0:
                return v.x;
            case 1:
                return v.y;
            case 2:
                return v.z;
            case 3:
                return v.w;
            default:
                throw new RuntimeException("invalid index");
        }
    }

    public static void setComponent(javax_.vecmath.Tuple4i v, int i, int x) {
        switch (i) {
            case 0:
                v.x = x;
                break;
            case 1:
                v.y = x;
                break;
            case 2:
                v.z = x;
                break;
            case 3:
                v.w = x;
                break;
            default:
                throw new RuntimeException("invalid index");
        }
    }

    public static float getComponent(javax_.vecmath.Tuple4f v, int i) {
        switch (i) {
            case 0:
                return v.x;
            case 1:
                return v.y;
            case 2:
                return v.z;
            case 3:
                return v.w;
            default:
                throw new RuntimeException("invalid index");
        }
    }

    public static double getComponent(javax_.vecmath.Tuple4d v, int i) {
        switch (i) {
            case 0:
                return v.x;
            case 1:
                return v.y;
            case 2:
                return v.z;
            case 3:
                return v.w;
            default:
                throw new RuntimeException("invalid index");
        }
    }

    public static void setComponent(javax_.vecmath.Tuple4f v, int i, float x) {
        switch (i) {
            case 0:
                v.x = x;
                break;
            case 1:
                v.y = x;
                break;
            case 2:
                v.z = x;
                break;
            case 3:
                v.w = x;
                break;
            default:
                throw new RuntimeException("invalid index");
        }
    }

    public static int getComponent(javax_.vecmath.Tuple3i v, int i) {
        switch (i) {
            case 0:
                return v.x;
            case 1:
                return v.y;
            case 2:
                return v.z;
            default:
                throw new RuntimeException("invalid index");
        }
    }

    public static void setComponent(javax_.vecmath.Tuple3i v, int i, int x) {
        switch (i) {
            case 0:
                v.x = x;
                break;
            case 1:
                v.y = x;
                break;
            case 2:
                v.z = x;
                break;
            default:
                throw new RuntimeException("invalid index");
        }
    }

    public static float getComponent(javax_.vecmath.Tuple3f v, int i) {
        switch (i) {
            case 0:
                return v.x;
            case 1:
                return v.y;
            case 2:
                return v.z;
            default:
                throw new RuntimeException("invalid index");
        }
    }

    public static void setComponent(javax_.vecmath.Tuple3f v, int i, float x) {
        switch (i) {
            case 0:
                v.x = x;
                break;
            case 1:
                v.y = x;
                break;
            case 2:
                v.z = x;
                break;
            default:
                throw new RuntimeException("invalid index");
        }
    }

    public static double getComponent(javax_.vecmath.Tuple3d v, int i) {
        switch (i) {
            case 0:
                return v.x;
            case 1:
                return v.y;
            case 2:
                return v.z;
            default:
                throw new RuntimeException("invalid index");
        }
    }

    public static void setComponent(javax_.vecmath.Tuple3d v, int i, double x) {
        switch (i) {
            case 0:
                v.x = x;
                break;
            case 1:
                v.y = x;
                break;
            case 2:
                v.z = x;
                break;
            default:
                throw new RuntimeException("invalid index");
        }
    }
    
    public static boolean isNaN(javax_.vecmath.Tuple4f t) {
        return Float.isNaN(t.x) || Float.isNaN(t.y) || Float.isNaN(t.z) || Float.isNaN(t.w);
    }

    public static boolean isNaN(javax_.vecmath.Tuple3f t) {
        return Float.isNaN(t.x) || Float.isNaN(t.y) || Float.isNaN(t.z);
    }

    public static boolean isNaN(javax_.vecmath.Tuple3d t) {
        return Double.isNaN(t.x) || Double.isNaN(t.y) || Double.isNaN(t.z);
    }

    public static boolean isZero(javax_.vecmath.Tuple3d t) {
        return t.x == 0 && t.y == 0 && t.z == 0;
    }

    public static boolean isZero(javax_.vecmath.Tuple3d t, double epsilon) {
        return Math.abs(t.x) < epsilon && Math.abs(t.y) < epsilon && Math.abs(t.z) < epsilon;
    }

    public static double maxComponent(javax_.vecmath.Tuple3d v) {
        return Math.max(v.x, Math.max(v.y, v.z));
    }

    public static double minComponent(javax_.vecmath.Tuple3d v) {
        return Math.max(v.x, Math.max(v.y, v.z));
    }

    public static int minComponent(javax_.vecmath.Tuple2i v) {
        return Math.min(v.x, v.y);
    }

    public static boolean isFinite(javax_.vecmath.Tuple4d t) {
        return Double.isFinite(t.x)
                && Double.isFinite(t.y)
                && Double.isFinite(t.z)
                && Double.isFinite(t.w);
    }
}
