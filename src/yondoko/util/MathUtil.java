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

public class MathUtil {
    public static int getClosestPowerOfTwo(int x) {
        if (x < 0)
            return 1;
        else {
            int y = 1;
            while (y < x) {
                y = y*2;
            }
            if (Math.abs(y-x) < Math.abs(y/2-x)) {
                return y;
            } else {
                return y/2;
            }
        }
    }

    public static int getCeilingPowerOfTwo(int x) {
        if (x < 0) {
            return 1;
        } else {
            int y = 1;
            while (y < x) {
                y *= 2;
            }
            return y;
        }
    }

    public static boolean isPowerOfTwo(int x) {
        x = Math.abs(x);
        if (x == 0)
            return false;
        else if (x == 1)
            return true;
        else {
            while (x > 1) {
                if ((x & 1) == 1)
                    return false;
                else
                    x = (x >> 1);
            }
            return x == 1;
        }
    }

    /**
     * Clamp x to the given lower and upper bound.
     * @param x the value
     * @param lower the lower bound
     * @param upper the upper bound
     * @return x clamped to the interval [lower, upper].
     */
    public static double clamp(double x, double lower, double upper) {
        return Math.min(upper, Math.max(x, lower));
    }
}
