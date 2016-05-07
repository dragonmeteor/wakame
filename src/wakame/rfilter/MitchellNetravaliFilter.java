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

package wakame.rfilter;

import wakame.WakameObject;
import wakame.util.PropertiesUtil;

import java.util.HashMap;

/**
 * Separable reconstruction filter by Mitchell and Netravali
 *
 * D. Mitchell, A. Netravali, Reconstruction filters for computer graphics,
 * Proceedings of SIGGRAPH 88, Computer Graphics 22(4), pp. 221-228, 1988.
 */
public class MitchellNetravaliFilter extends ReconstructionFilter {
    private double B, C;

    private MitchellNetravaliFilter() {
        // NO-OP
    }

    @Override
    protected void setProperties(HashMap<String, Object> properties) {
        radius = PropertiesUtil.getDouble(properties, "radius", 2.0);
        /**
         * The B parameter.
         */
        B = PropertiesUtil.getDouble(properties, "B", 1.0 / 3.0);
        /**
         * The C parameter.
         */
        C = PropertiesUtil.getDouble(properties, "C", 1.0 / 3.0);
    }

    @Override
    protected void activate() {
        // NO-OP
    }

    @Override
    public double eval(double x) {
        x = Math.abs(2.0 * x / radius);
        double x2 = x*x, x3 = x2*x;

        if (x < 1) {
            return 1.0/6.0 * ((12-9*B-6*C)*x3
                    + (-18+12*B+6*C) * x2 + (6-2*B));
        } else if (x < 2) {
            return 1.0/6.0 * ((-B-6*C)*x3 + (6*B+30*C) * x2
                    + (-12*B-48*C)*x + (8*B + 24*C));
        } else {
            return 0.0;
        }
    }

    public String toString() {
        return String.format("MitchellNetravaliFilter[radius=%f, B=%f, C=%f]", radius, B, C);
    }

    public static class Builder extends WakameObject.Builder {
        @Override
        protected WakameObject createInstance() {
            return new MitchellNetravaliFilter();
        }
    }
}
