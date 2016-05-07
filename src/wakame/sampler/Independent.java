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

import wakame.WakameObject;
import wakame.block.ImageBlock;
import yondoko.random.MersenneTwister;

import javax_.vecmath.Tuple2d;

/**
 * Independent sampling - returns independent uniformly distributed
 * random numbers on [0, 1).
 *
 * This class is essentially just a wrapper around the Mersenne Twister pseudorandom
 * number generator.
 */
public class Independent extends Sampler {
    private MersenneTwister random = new MersenneTwister();

    private Independent() {
        // NO-OP
    }

    public Object clone() {
        Independent output = new Independent();
        output.random = (MersenneTwister)this.random.clone();
        output.sampleCount = this.sampleCount;
        return output;
    }

    @Override
    public void prepare(ImageBlock block) {
        int offsetX = block.getOffsetX();
        int offsetY = block.getOffsetY();
        random.setSeed(offsetY * 1000000 + offsetX);
    }

    public void setSeed(int x) {
        random.setSeed(x);
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
        return random.nextDouble();
    }

    @Override
    public void next2D(Tuple2d output) {
        output.x = random.nextDouble();
        output.y = random.nextDouble();
    }

    @Override
    protected void activate() {
        // NO-OP
    }

    public String toString() {
        return String.format("Independent[sampleCount=%d]", sampleCount);
    }

    public static class Builder extends WakameObject.Builder {

        @Override
        protected WakameObject createInstance() {
            return new Independent();
        }
    }
}
