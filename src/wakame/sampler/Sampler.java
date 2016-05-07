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
import wakame.util.PropertiesUtil;

import javax_.vecmath.Tuple2d;
import java.util.HashMap;

/**
 * A sample generator.
 *
 * A sample generator is responsible for generating the random number stream
 * that will be passed an Integrator implementation as it computes the
 * radiance incident along a specified ray.
 *
 * The most simple conceivable sample generator is just a wrapper around the
 * Mersenne-Twister random number generator and is implemented in
 * the Independent class (it is named this way because it generates
 * statistically independent random numbers).
 *
 * Fancier samplers might use stratification or low-discrepancy sequences
 * (e.g. Halton, Hammersley, or Sobol point sets) for improved convergence.
 * Another use of this class is in producing intentionally correlated
 * random numbers, e.g. as part of a Metropolis-Hastings integration scheme.
 *
 * The general interface between a sampler and a rendering algorithm is as
 * follows: Before beginning to render a pixel, the rendering algorithm calls
 * generate(). The first pixel sample can now be computed, after which
 * advance() needs to be invoked. This repeats until all pixel samples have
 * been exhausted.  While computing a pixel sample, the rendering
 * algorithm requests (pseudo-) random numbers using the next1D() and
 * next2D() functions.
 *
 * Conceptually, the right way of thinking of this goes as follows:
 * For each sample in a pixel, a sample generator produces a (hypothetical)
 * point in an infinite dimensional random number hypercube. A rendering
 * algorithm can then request subsequent 1D or 2D components of this point
 * using the next1D() and next2D() functions. Fancy implementations
 * of this class make certain guarantees about the stratification of the
 * first n components with respect to the other points that are sampled
 * within a pixel.
 */
public abstract class Sampler extends WakameObject {
    /**
     * Prepare to generate new samples.
     *
     * This function is called initially and every time the integrator starts
     * rendering a new pixel.
     */
    public abstract void generate();

    /**
     * Advance to the next sample.
     */
    public abstract void advance();

    /**
     * Retrieve the next component value from the current sample.
     * @return a random floating point number int [0,1).
     */
    public abstract double next1D();

    /**
     * Retrieve the next two component values from the current sample.
     * @param output the container of the two random output numbers, each a floating point number in [0,1).
     */
    public abstract void next2D(Tuple2d output);

    /**
     * The number of samples per pixel.
     */
    protected int sampleCount;

    /**
     * Return the number of samples per pixel.
     * @return the number of samples per pixel.
     */
    public int getSampleCount() {
        return sampleCount;
    }

    /**
     * Set the sample count.
     * @param properties
     */
    protected void setProperties(HashMap<String, Object> properties) {
        this.sampleCount = PropertiesUtil.getInteger(properties, "sampleCount", 1);
    }

    /**
     * Clone the sampler.
     * @return the clone of the sampler
     */
    public abstract Object clone();

    /**
     * Prepare to render a new image block
     *
     * This function is called when the sampler begins rendering
     * a new image block. This can be used to deterministically
     * initialize the sampler so that repeated program runs
     * always create the same image.
     *
     * @param block the image block
     */
    public abstract void prepare(ImageBlock block);
}
