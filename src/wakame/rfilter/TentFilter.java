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

import java.util.HashMap;

/**
 * Tent filter
 */
public class TentFilter extends ReconstructionFilter {
    private TentFilter() {
        radius = 1.0;
    }

    @Override
    protected void activate() {
        // NO-OP
    }

    @Override
    protected void setProperties(HashMap<String, Object> properties) {
        // NO-OP
    }

    @Override
    public double eval(double x) {
        return Math.max(0, 1.0 - Math.abs(x));
    }

    public String toString() {
        return "TentFilter[]";
    }

    public static class Builder extends WakameObject.Builder {
        @Override
        protected WakameObject createInstance() {
            return new TentFilter();
        }
    }
}
