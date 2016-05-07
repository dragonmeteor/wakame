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
 * Box filter -- faster, but prone to aliasing.
 */
public class BoxFilter extends ReconstructionFilter {
    private BoxFilter() {
        radius = 0.5;
    }

    @Override
    public double eval(double x) {
        return 1;
    }

    @Override
    protected void activate() {
        // NO-OP
    }

    @Override
    protected void setProperties(HashMap<String, Object> properties) {
        // NO-OP
    }

    public String toString() {
        return "BoxFilter[]";
    }

    public static class Builder extends WakameObject.Builder {
        @Override
        protected WakameObject createInstance() {
            return new BoxFilter();
        }
    }
}
