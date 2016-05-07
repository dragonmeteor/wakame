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

package wakame.sampler;

import wakame.block.ImageBlock;

import javax_.vecmath.Tuple2d;
import java.util.ArrayList;

/**
 * A sampler that returns fixed pre-determined list of values.
 */
public class FixValueSampler extends Sampler {
    private ArrayList<Double> values = new ArrayList<Double>();
    int currentIndex = 0;

    public FixValueSampler() {
        // NO-OP
    }

    @Override
    public void generate() {
        // NO-OP
    }

    @Override
    public void advance() {
        // NO-OP
    }

    @Override
    public double next1D() {
        double result = values.get(currentIndex);
        currentIndex++;
        return result;
    }

    @Override
    public void next2D(Tuple2d output) {
        double x = next1D();
        double y = next1D();
        output.set(x, y);
    }

    @Override
    public Object clone() {
        return null;
    }

    @Override
    public void prepare(ImageBlock block) {
        // NO-OP
    }

    @Override
    protected void activate() {
        // NO-OP
    }

    public void add(double mu) {
        values.add(mu);
    }

    public void reset() {
        currentIndex = 0;
        values.clear();
    }
}
