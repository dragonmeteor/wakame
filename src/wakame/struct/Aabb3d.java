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

import yondoko.util.VectorUtil;

import javax_.vecmath.Tuple3d;
import javax_.vecmath.Vector3d;

public class Aabb3d {
    public final Vector3d pMin = new Vector3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
    public final Vector3d pMax = new Vector3d(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);

    public void reset() {
        pMin.set(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        pMax.set(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
    }

    public void expandBy(Tuple3d p) {
        pMin.x = Math.min(pMin.x, p.x);
        pMin.y = Math.min(pMin.y, p.y);
        pMin.z = Math.min(pMin.z, p.z);
        pMax.x = Math.max(pMax.x, p.x);
        pMax.y = Math.max(pMax.y, p.y);
        pMax.z = Math.max(pMax.z, p.z);
    }

    public void expandBy(double t) {
        pMin.x -= t;
        pMin.y -= t;
        pMin.z -= t;
        pMax.x += t;
        pMax.y += t;
        pMax.z += t;
    }

    public void expandBy(Aabb3d other) {
        pMin.x = Math.min(pMin.x, other.pMin.x);
        pMin.y = Math.min(pMin.y, other.pMin.y);
        pMin.z = Math.min(pMin.z, other.pMin.z);
        pMax.x = Math.max(pMax.x, other.pMax.x);
        pMax.y = Math.max(pMax.y, other.pMax.y);
        pMax.z = Math.max(pMax.z, other.pMax.z);
    }

    public boolean overlap(Aabb3d other) {
        if (pMin.x > other.pMax.x || pMax.x < other.pMin.x) {
            return false;
        } else if (pMin.y > other.pMax.y || pMax.y < other.pMin.y) {
            return false;
        } else if (pMin.z > other.pMax.z || pMax.z < other.pMin.z) {
            return false;
        } else {
            return true;
        }
    }

    public boolean overlap(Tuple3d p) {
        if (p.x < pMin.x || p.x > pMax.x) {
            return false;
        } else if (p.y < pMin.y || p.y > pMax.y) {
            return false;
        } else if (p.z < pMin.z || p.z > pMax.z) {
            return false;
        } else {
            return true;
        }
    }

    public void set(Aabb3d other) {
        pMin.set(other.pMin);
        pMax.set(other.pMax);
    }

    @Override
    public String toString() {
        return "Aabb[pMin = " + pMin.toString() + ", pMax = " + pMax.toString() + "]";
    }

    public Double getMaxExtent() {
        return (Double) Math.max(pMax.x - pMin.x, Math.max(pMax.y - pMin.y, pMin.z - pMin.z));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Aabb3d) {
            Aabb3d other = (Aabb3d) obj;
            return other.pMin.equals(this.pMin) && other.pMax.equals(this.pMax);
        } else {
            return false;
        }
    }

    public void getCornerPoint(int cornerId, Tuple3d out) {
        out.x = ((cornerId & 1) == 0) ? pMin.x : pMax.x;
        out.y = ((cornerId & 2) == 0) ? pMin.y : pMax.y;
        out.z = ((cornerId & 4) == 0) ? pMin.z : pMax.z;
    }

    public double getExtent(int dim) {
        switch (dim) {
            case 0:
                return pMax.x - pMin.x;
            case 1:
                return pMax.y - pMin.y;
            case 2:
                return pMax.z - pMin.z;
            default:
                throw new RuntimeException("dim must be 0, 1, or 2");
        }
    }

    public double getSurfaceArea() {
        double xx = pMax.x - pMin.x;
        double yy = pMax.y - pMin.y;
        double zz = pMax.z - pMin.z;
        return 2*(xx*yy + yy*zz + zz*xx);
    }

    public double getVolume() {
        double xx = pMax.x - pMin.x;
        double yy = pMax.y - pMin.y;
        double zz = pMax.z - pMin.z;
        return xx*yy*zz;
    }

    public int getLargestAxis() {
        double xx = pMax.x - pMin.x;
        double yy = pMax.y - pMin.y;
        double zz = pMax.z - pMin.z;

        if (xx >= yy && xx >= zz)
            return 0;
        else if (yy >= xx && yy >= zz)
            return 1;
        else
            return 2;
    }

    /**
     * Check if the ray intersect the bounding box.
     * @param ray the ray
     * @return whether the ray intersect the bounding box
     */
    public boolean rayIntersect(Ray ray) {
        double nearT = Double.NEGATIVE_INFINITY;
        double farT = Double.POSITIVE_INFINITY;

        for (int i=0; i<3; i++) {
            double origin, minVal, maxVal, d;

            if (i == 0) {
                origin = ray.o.x;
                minVal = pMin.x;
                maxVal = pMax.x;
                d = ray.d.x;
            } else if (i == 1) {
                origin = ray.o.y;
                minVal = pMin.y;
                maxVal = pMax.y;
                d = ray.d.y;
            } else {
                origin = ray.o.z;
                minVal = pMin.z;
                maxVal = pMax.z;
                d = ray.d.z;
            }

            if (d == 0) {
                if (origin < minVal || origin > maxVal)
                    return false;
            } else {
                double t1, t2;
                double recip = VectorUtil.getComponent(ray.d, i);
                if (recip > 0) {
                    t1 = (minVal - origin) * recip;
                    t2 = (maxVal - origin) * recip;
                } else {
                    t2 = (minVal - origin) * recip;
                    t1 = (maxVal - origin) * recip;
                }

                if (nearT < t1) nearT = t1;
                if (farT > t2) farT = t2;

                if (nearT > farT)
                    return false;
            }
        }

        return ray.mint <= farT && nearT <= ray.maxt;
    }

    /**
     * Check if the ray intersect the bounding box.
     * This version is faster than the one without dRcp.
     * @param ray the ray
     * @param dRcp the component-wise reciprocal of the ray direction
     * @return whether the ray intersect the bounding box
     */
    public boolean rayIntersectFast(Ray ray, double[] dRcp) {
        double nearT = Double.NEGATIVE_INFINITY;
        double farT = Double.POSITIVE_INFINITY;

        for (int i=0; i<3; i++) {
            double origin, minVal, maxVal, d;

            if (i == 0) {
                origin = ray.o.x;
                minVal = pMin.x;
                maxVal = pMax.x;
                d = ray.d.x;
            } else if (i == 1) {
                origin = ray.o.y;
                minVal = pMin.y;
                maxVal = pMax.y;
                d = ray.d.y;
            } else {
                origin = ray.o.z;
                minVal = pMin.z;
                maxVal = pMax.z;
                d = ray.d.z;
            }

            if (d == 0) {
                if (origin < minVal || origin > maxVal)
                    return false;
            } else {
                double t1, t2;
                double recip = dRcp[i];
                if (recip > 0) {
                    t1 = (minVal - origin) * recip;
                    t2 = (maxVal - origin) * recip;
                } else {
                    t2 = (minVal - origin) * recip;
                    t1 = (maxVal - origin) * recip;
                }

                if (nearT < t1) nearT = t1;
                if (farT > t2) farT = t2;

                if (nearT > farT)
                    return false;
            }
        }

        return ray.mint <= farT && nearT <= ray.maxt;
    }

    /**
     * Compute the overlapping region of the bounding box and the unbounded ray.
     * @param ray the ray
     * @param t a 2-element array which receives the distance along the ray where it intersects the AABB.
     *          The intersection interval is [t[0], t[1]].
     * @return whether the unbounded ray intersects the bounding box
     */
    public boolean rayIntersect(Ray ray, double[] t) {
        double nearT = Double.NEGATIVE_INFINITY;
        double farT = Double.POSITIVE_INFINITY;

        for (int i=0; i<3; i++) {
            double origin = VectorUtil.getComponent(ray.o, i);
            double minVal = VectorUtil.getComponent(pMin, i);
            double maxVal = VectorUtil.getComponent(pMax, i);

            if (VectorUtil.getComponent(ray.d, i) == 0) {
                if (origin < minVal || origin > maxVal)
                    return false;
            } else {
                double t1 = (minVal - origin) / VectorUtil.getComponent(ray.d, i);
                double t2 = (maxVal - origin) / VectorUtil.getComponent(ray.d, i);

                if (t1 > t2) {
                    double temp = t1;
                    t1 = t2;
                    t2 = temp;
                }

                nearT = Math.max(t1, nearT);
                farT = Math.min(t2, farT);

                t[0] = nearT;
                t[1] = farT;

                if (!(nearT <= farT))
                    return false;
            }
        }

        return true;
    }
}
