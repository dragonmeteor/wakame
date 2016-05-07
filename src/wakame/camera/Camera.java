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

package wakame.camera;

import wakame.WakameObject;
import wakame.rfilter.ReconstructionFilter;
import wakame.struct.Color3d;
import wakame.struct.Ray;

import javax_.vecmath.Tuple2i;
import javax_.vecmath.Vector2d;

/**
 * Generic camera interface
 *
 * This class provides an abstract interface to cameras in Nori and
 * exposes the ability to sample their response function. By default, only
 * a perspective camera implementation exists, but you may choose to
 * implement other types (e.g. an environment camera, or a physically-based
 * camera model that simulates the behavior actual lenses)
 */
public abstract class Camera extends WakameObject {
    /**
     * The width of the output image in pixels.
     */
    protected int outputSizeX;
    /**
     * The height of the output image in pixels.
     */
    protected int outputSizeY;
    /**
     * The reconstruction filter.
     */
    protected ReconstructionFilter rfilter = null;


    /**
     * Importance sample a ray according to the camera's response function.
     * @param samplePosition the desired sample position in the film, expressed in fractional pixel coordinates
     * @param apertureSample the uniformly distributed 2D vector that is used to sample a position on the aperture
     *                       of the sensor if necessary
     * @param ray the receiver of the sample ray
     * @param importanceWeight the receiver of the importance weight of the sample ray.
     *                         This accounts for the difference in the camera response function
     *                         and the sampling density.
     */
    public abstract void sampleRay(Vector2d samplePosition, Vector2d apertureSample,
                                   Ray ray, Color3d importanceWeight);

    /**
     * Get the size of the output image, in pixels.
     * @param output the receiver of the output image size
     */
    public void getOutputSize(Tuple2i output) {
        output.set(outputSizeX, outputSizeY);
    }

    /**
     * Get the image width.
     * @return the image width
     */
    public int getOutputSizeX() {
        return outputSizeX;
    }

    /**
     * Get the image height.
     * @return the image height
     */
    public int getOutputSizeY() {
        return outputSizeY;
    }

    /**
     * Get the reconstruction filter.
     * @return the reconstruction filter in image space
     */
    public ReconstructionFilter getReconstructionFilter() {
        return rfilter;
    }
}
