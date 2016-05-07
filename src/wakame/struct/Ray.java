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

package wakame.struct;

import wakame.Constants;

import javax_.vecmath.Point3d;
import javax_.vecmath.Tuple3d;
import javax_.vecmath.Vector3d;

/**
 * Simple 3-dimensional ray segment data structure
 *
 * Along with the ray origin and direction, this data structure additionally
 * stores a ray segment [mint, maxt] (whose entries may include positive/negative
 * infinity), as well as the component-wise reciprocals of the ray direction.
 * That is just done for convenience, as these values are frequently required.
 *
 * Important: be careful when changing the ray direction. You must
 * call update() to compute the component-wise reciprocals as well, or Wakame's
 * ray-triangle intersection code will go haywire.
 */
public class Ray {
    /**
     * The origin.
     */
    public Point3d o = new Point3d();
    /**
     * The direction.
     */
    public Vector3d d = new Vector3d();
    /**
     * Minimum position on the ray segment.
     */
    public double mint;
    /**
     * Maximum position on the ray segment.
     */
    public double maxt;

    /**
     * Create a new ray.
     */
    public Ray() {
        this.o.set(0,0,0);
        this.d.set(0,0,1);
        this.mint = Constants.EPSILON;
        this.maxt = Double.POSITIVE_INFINITY;
    }

    /**
     * Create a new ray.
     * @param o
     * @param d
     */
    public Ray(Point3d o, Vector3d d) {
        this(o, d, Constants.EPSILON, Double.POSITIVE_INFINITY);
    }

    /**
     * Construct a new ray.
     * @param o
     * @param d
     * @param mint
     * @param maxt
     */
    public Ray(Point3d o, Vector3d d, double mint, double maxt) {
        this.o.set(o);
        this.d.set(d);
        this.mint = mint;
        this.maxt = maxt;
    }

    /**
     * Copy constructor
     * @param other
     */
    public Ray(Ray other) {
        this.o.set(other.o);
        this.d.set(other.d);
        this.mint = other.mint;
        this.maxt = other.maxt;
    }

    /**
     * Get the position of a point along the ray at "time" t.
     * @param t the time
     * @param p the receiver of the postion.
     */
    public void project(double t, Tuple3d p) {
        p.scaleAdd(t, d, o);
    }

    /**
     * Set the output ray to the ray that goes in the opposition direction of the current instant.
     * @param output the output ray
     */
    public void reverse(Ray output) {
        output.o.set(this.o);
        output.d.negate(this.d);
        output.mint = mint;
        output.maxt = maxt;
    }

    public String toString() {
        return String.format(
                "Ray[\n" +
                "  o = %s,\n" +
                "  d = %s,\n" +
                "  mint = %f,\n" +
                "  maxt = %f\n" +
                "]",
                o.toString(), d.toString(), mint, maxt);
    }
}
