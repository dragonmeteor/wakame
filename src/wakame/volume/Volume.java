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

package wakame.volume;

import wakame.struct.Aabb3d;

import javax_.vecmath.Tuple3d;

/**
 * Superclass for volumetric data.
 */
public interface Volume {
    /**
     * Whether the volume supports floating point value lookups.
     * @return whether the volume supports floating point value lookup
     */
    public boolean supportFloatLookups();

    /**
     * Look up the floating point value at point p.
     * @param p the position to look up floating point value
     * @return the value of the volume at point p
     */
    public double lookupFloat(Tuple3d p);

    /**
     * Whether the volume supports vector value lookups.
     * @return whether the volume supports vector value lookups
     */
    public boolean supportVectorLookups();

    /**
     * Look up the vector value at point p
     * @param p the position to look up the vector value
     * @param output the receiver of the vector value
     */
    public void lookupVector(Tuple3d p, Tuple3d output);

    /**
     * Return the axis-aligned bounding box of the volume.
     * @param bbox the receiver of the axis-aligned bounding box.
     */
    public void getBbox(Aabb3d bbox);
}
