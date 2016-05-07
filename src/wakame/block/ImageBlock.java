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

package wakame.block;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wakame.Constants;
import wakame.rfilter.ReconstructionFilter;
import wakame.struct.Color3d;
import yondoko.util.VectorUtil;

import javax_.vecmath.Point2d;
import javax_.vecmath.Vector4d;

/**
 * Weighted pixel storage for a rectangular subregion of an image
 *
 * This class implements storage for a rectangular subregion of a
 * larger image that is being rendered. For each pixel, it records color
 * values along with a weight that specifies the accumulated influence of
 * nearby samples on the pixel (according to the used reconstruction filter).
 *
 * When rendering with filters, the samples in a rectangular
 * region will generally also contribute to pixels just outside of
 * this region. For that reason, this class also stores information about
 * a small border region around the rectangle, whose size depends on the
 * properties of the reconstruction filter.
 */
public class ImageBlock {
    /**
     * Whether to throw an exception and terminate execution when
     * there is an invalid pixel value begin put into an image block.
     */
    private boolean failHard = true;
    /**
     * The logger
     */
    private static Logger logger = LoggerFactory.getLogger(ImageBlock.class);

    protected int offsetX = 0;
    protected int offsetY = 0;
    protected int sizeX = 0;
    protected int sizeY = 0;
    protected int borderSize = 0;
    protected double[] filter;
    protected double filterRadius;
    protected double lookupFactor = 0;
    protected Vector4d[][] data;

    /**
     * Create a new image block of the specified maximum size
     * @param sizeX the width of the block
     * @param sizeY the height of the block
     * @param _filter the filter used to reconstruct the image
     */
    public ImageBlock(int sizeX, int sizeY, ReconstructionFilter _filter) {
        offsetX = 0;
        offsetY = 0;
        this.sizeX = sizeX;
        this.sizeY = sizeY;

        if (_filter != null) {
            filterRadius = _filter.getRadius();
            borderSize = (int) Math.ceil(filterRadius - 0.5);
            filter = new double[Constants.FILTER_RESOLUTION + 1];
            for (int i = 0; i < Constants.FILTER_RESOLUTION; i++) {
                double pos = (filterRadius * i) / Constants.FILTER_RESOLUTION;
                filter[i] = _filter.eval(pos);
            }
            filter[Constants.FILTER_RESOLUTION] = 0;
            lookupFactor = Constants.FILTER_RESOLUTION / filterRadius;
        } else {
            throw new RuntimeException("ImageBlock(): Reconstruction filter is specified.");
        }

        /* Allocate space for pixels and border regions */
        data = new Vector4d[this.sizeY + 2*borderSize][this.sizeX + 2*borderSize];
        for (int y = 0; y < data.length; y++) {
            for (int x = 0; x < data[y].length; x++) {
                data[y][x] = new Vector4d(0, 0, 0, 0);
            }
        }
    }

    /**
     * Get the width
     * @return the width
     */
    public int getSizeX() {
        return sizeX;
    }

    /**
     * Get the height.
     * @return the height
     */
    public int getSizeY() {
        return sizeY;
    }

    /**
     * Set the size of the block.
     * @param sizeX the width
     * @param sizeY the height
     */
    public void setSize(int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    /**
     * Configure the offset of the block within the main image
     * @param offsetX the x offset
     * @param offsetY the y offset
     */
    public void setOffset(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    /**
     * Get the x offset
     * @return the x offset
     */
    public int getOffsetX() {
        return offsetX;
    }

    /**
     * Get the y offset
     * @return the y offset
     */
    public int getOffsetY() {
        return offsetY;
    }

    /**
     * Return the border size
     * @return the border size
     */
    public int getBorderSize() {
        return borderSize;
    }

    /**
     * Clear all contents.
     */
    public void clear() {
        for (int y = 0; y < data.length; y++) {
            for (int x = 0; x < data[y].length; x++) {
                data[y][x] = new Vector4d(0, 0, 0, 0);
            }
        }
    }

    /**
     * Record a sample with the given position and radiance value
     *
     * @param x the x-coordinate of the sample position
     * @param y the y-coordinate of the sample position
     * @param value the radiance value
     */
    public void put(double x, double y, Color3d value) {
        if (VectorUtil.isNaN(value)) {
            if (failHard) {
                throw new RuntimeException("ImageBlock.put(): computed an invalid radiance value: " + value.toString());
            } else {
                logger.error("ImageBlock.put(): computed an invalid radiance value: " + value.toString());
                return;
            }
        }

        /* Convert to pixel coordinates within the image block */
        Point2d pos = new Point2d(
                x - 0.5 - (offsetX - borderSize),
                y - 0.5 - (offsetY - borderSize));

        //System.out.println("filterRadius = " + filterRadius);

        /* Compute the rectangle of pixels that will need to be updated */
        int minX = Math.max((int) Math.ceil(pos.x - filterRadius), 0);
        int minY = Math.max((int) Math.ceil(pos.y - filterRadius), 0);
        int maxX = Math.min((int) Math.floor(pos.x + filterRadius), data[0].length-1);
        int maxY = Math.min((int) Math.floor(pos.y + filterRadius), data.length - 1);

        for (int _y=minY; _y<=maxY; ++_y) {
            double weightY = filter[(int) (Math.abs(_y-pos.y) * lookupFactor)];
            for (int _x = minX; _x <= maxX; ++_x) {
                double weightX = filter[(int) (Math.abs(_x-pos.x) * lookupFactor)];
                data[_y][_x].x += value.x * weightX * weightY;
                data[_y][_x].y += value.y * weightX * weightY;
                data[_y][_x].z += value.z * weightX * weightY;
                data[_y][_x].w += weightX * weightY;
            }
        }
    }

    /**
     * Merge another image block into this one
     *
     * During the merge operation, this function locks
     * the destination block using a mutex.
     */
    public void put(ImageBlock b) {
        int offsetX = b.getOffsetX();
        int offsetY = b.getOffsetY();
        int sizeX = b.getSizeX();
        int sizeY = b.getSizeY();

        synchronized (this) {
            for (int y = 0; y < sizeY + 2*b.borderSize; y++) {
                for (int x = 0; x < sizeX + 2*b.borderSize; x++) {
                    int yy = offsetY + borderSize - b.borderSize + y;
                    int xx = offsetX + borderSize - b.borderSize + x;
                    data[yy][xx].add(b.data[y][x]);
                }
            }
        }
    }

    /**
     * Get the underlying data array.
     * @return the underlying data array.
     */
    public Vector4d[][] getData() {
        return data;
    }
}
