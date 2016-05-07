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
 * Windowed Gaussian filter with configurable extent
 * and standard deviation. Often produces pleasing
 * results, but may introduce too much blurring.
 */
public class GaussianFilter extends ReconstructionFilter {
    private double stddev;

    private GaussianFilter() {
        // NO-OP
    }

    @Override
    protected void setProperties(HashMap<String, Object> properties) {
        radius = PropertiesUtil.getDouble(properties, "radius", 2.0);
        /**
         * The standard deviation of the Gaussian filter.
         */
        stddev = PropertiesUtil.getDouble(properties, "stddev", 0.5);
    }

    @Override
    public double eval(double x) {
        double alpha = -1 / (2.0 * stddev * stddev);
        return Math.max(0, Math.exp(alpha*x*x) - Math.exp(alpha*radius*radius));
    }

    @Override
    protected void activate() {
        // NO-OP
    }

    public String toString() {
        return String.format("GaussianFilter[radius=%f, stddev=%f]", radius, stddev);
    }

    public static class Builder extends WakameObject.Builder {
        @Override
        protected WakameObject createInstance() {
            return new GaussianFilter();
        }
    }
}
