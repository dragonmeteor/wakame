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

package wakame.mesh;

import wakame.WakameObject;
import wakame.bsdf.Bsdf;
import wakame.bsdf.Diffuse;
import wakame.emitter.Emitter;
import wakame.struct.Aabb3d;
import wakame.struct.Ray;
import yondoko.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Triangle mesh
 *
 * This class stores a triangle mesh object and provides numerous functions
 * for querying the individual triangles. Subclasses of Mesh implement
 * the specifics of how to create its contents (e.g. by loading from an
 * external file).
 */
public abstract class Mesh extends WakameObject {
    /**
     * Identifying name.
     */
    protected String name = "";
    /**
     * Vertex positions.
     */
    protected ArrayList<javax_.vecmath.Point3d> positions = new ArrayList<javax_.vecmath.Point3d>();
    /**
     * Vertex normals
     */
    protected ArrayList<javax_.vecmath.Vector3d> normals = new ArrayList<javax_.vecmath.Vector3d>();
    /**
     * Vertex texture coordinates.
     */
    protected ArrayList<javax_.vecmath.Vector2d> texCoords = new ArrayList<javax_.vecmath.Vector2d>();
    /**
     * Faces
     */
    protected ArrayList<javax_.vecmath.Point3i> triangles = new ArrayList<javax_.vecmath.Point3i>();
    /**
     * BSDF
     */
    protected Bsdf bsdf = null;
    /**
     * Associated emitter, if any.
     */
    protected Emitter emitter = null;
    /**
     * Axis-aligned bounding box.
     */
    protected Aabb3d bbox = new Aabb3d();

    /**
     * Return the triangle count.
     * @return the triangle count
     */
    public int getTriangleCount() {
        return triangles.size();
    }

    /**
     * Get the bounding box of this mesh.
     */
    public void getBoundingBox(Aabb3d bbox) {
        bbox.set(this.bbox);
    }

    /**
     * Get the bounding box of the triangle with the givn index.
     * @param index the index of the triangle
     * @param bbox the receiver of the bounding box value
     */
    public void getTriangleBoundingBox(int index, Aabb3d bbox) {
        bbox.reset();
        javax_.vecmath.Point3i tri = triangles.get(index);
        bbox.expandBy(positions.get(tri.x));
        bbox.expandBy(positions.get(tri.y));
        bbox.expandBy(positions.get(tri.z));
    }

    /**
     * Get the centroid of the triangle with the given index.
     * @param index the index of the triangle
     * @param centroid the receiver of the centroid value
     */
    public void getTriangleCentroid(int index, javax_.vecmath.Tuple3d centroid) {
        centroid.set(0, 0, 0);
        javax_.vecmath.Point3i tri = triangles.get(index);
        centroid.add(positions.get(tri.x));
        centroid.add(positions.get(tri.y));
        centroid.add(positions.get(tri.z));
        centroid.scale(1.0 / 3);
    }

    public void addChild(WakameObject obj) {
        if (obj instanceof Bsdf) {
            if (bsdf != null) {
                throw new RuntimeException("Mesh.addChild(): Tried to register multiple Bsdf instances.");
            }
            bsdf = (Bsdf)obj;
        } else if (obj instanceof Emitter) {
            if (emitter != null) {
                throw new RuntimeException("Mesh.addChild(): Tried to register multiple Emitter instances");
            }
        } else {
            throw new RuntimeException("Mesh.addChild(): Child object of type " + obj.getClass().getName()
                    + " is not supported.");
        }
    }

    /**
     * Return the surface area of the triangle with the given index.
     * @param index the index of the triangle
     * @return the surface area of the triangle with the given index
     */
    public double getTriangleSurfaceArea(int index) {
        javax_.vecmath.Point3i tri = triangles.get(index);
        javax_.vecmath.Point3d p0 = positions.get(tri.x);
        javax_.vecmath.Point3d p1 = positions.get(tri.y);
        javax_.vecmath.Point3d p2 = positions.get(tri.z);
        javax_.vecmath.Vector3d v0 = new javax_.vecmath.Vector3d();
        javax_.vecmath.Vector3d v1 = new javax_.vecmath.Vector3d();
        v0.sub(p1, p0);
        v1.sub(p2, p0);
        javax_.vecmath.Vector3d cross = new javax_.vecmath.Vector3d();
        cross.cross(v0, v1);
        return cross.length() * 0.5;
    }

    /**
     * A structure containting the output of the mesh's triangle-ray intersection test.
     */
    public static class TriangleIntersection {
        /**
         * The barycentric coordinate of the intersection point.
         * Let us say the triangle points are p0, p1, and p2.
         * Then, the intersection point is given by bary.x*p0 + bary.y*p1 + bary.z*p2.
         */
        public javax_.vecmath.Vector3d bary = new javax_.vecmath.Vector3d();
        /**
         * The "time" of the intersection (i.e., the position along the ray of the intersection point,
         * measured in the unit of the length of the direction vector of the ray).
         */
        public double t;
    }

    /**
     * Ray-triangle intersection test.
     *
     * Uses the algorithm by Moeller and Trumbore discussed at
     * http://www.acm.org/jgt/papers/MollerTrumbore97/code.html
     *
     * Note that the test only applies to a single triangle in the mesh.
     * An acceleration data structure like BVH is needed to search
     * for intersections against many triangles.
     *
     * @param index the index of the triangle
     * @param ray the ray
     * @param inter the intersection output
     * @return whether the ray intersect the triangle with the given index
     */
    public boolean rayIntersect(int index, Ray ray, TriangleIntersection inter) {
        javax_.vecmath.Point3i tri = triangles.get(index);
        int i0 = tri.x;
        int i1 = tri.y;
        int i2 = tri.z;

        javax_.vecmath.Point3d p0 = positions.get(i0);
        javax_.vecmath.Point3d p1 = positions.get(i1);
        javax_.vecmath.Point3d p2 = positions.get(i2);

        // Find vectors for the two edges sharing p0.
        javax_.vecmath.Vector3d edge1 = new javax_.vecmath.Vector3d(); edge1.sub(p1, p0);
        javax_.vecmath.Vector3d edge2 = new javax_.vecmath.Vector3d(); edge2.sub(p2, p0);

        // Begin calculating the determinant - also used to calculate the y-component of the barycentric coordinate.
        javax_.vecmath.Vector3d pvec = new javax_.vecmath.Vector3d(); pvec.cross(ray.d, edge2);
        double det = edge1.dot(pvec);

        // If the determinant is near zero, ray lies in the plane of the triangle.
        if (det > -1e-8 && det < 1e-8) {
            return false;
        }
        double invDet = 1.0 / det;

        // Calculate the distance from p0 to ray origin.
        javax_.vecmath.Vector3d tvec = new javax_.vecmath.Vector3d(); tvec.sub(ray.o, p0);

        // Calculate the y-component and test bounds.
        inter.bary.y = tvec.dot(pvec) * invDet;
        if (inter.bary.y < 0 | inter.bary.y > 1) {
            return false;
        }

        // Prepare to test the z-component.
        javax_.vecmath.Vector3d qvec = new javax_.vecmath.Vector3d(); qvec.cross(tvec, edge1);

        // Calculate the z-component and test bounds.
        inter.bary.z = ray.d.dot(qvec) * invDet;
        if (inter.bary.z < 0 || inter.bary.y + inter.bary.z > 1) {
            return false;
        }
        inter.bary.x = 1 - inter.bary.y - inter.bary.z;

        // Compute the "time" of the intersection.
        inter.t = edge2.dot(qvec) * invDet;

        return inter.t >= ray.mint && inter.t <= ray.maxt;
    }

    public List<javax_.vecmath.Point3d> getPositions() {
        return positions;
    }

    public List<javax_.vecmath.Vector3d> getNormals() {
        return normals;
    }

    public List<javax_.vecmath.Vector2d> getTexCoords() {
        return texCoords;
    }

    public List<javax_.vecmath.Point3i> getTriangles() {
        return triangles;
    }

    public Emitter getEmitter() {
        return emitter;
    }

    public Bsdf getBsdf() {
        return bsdf;
    }

    @Override
    protected void activate() {
        if (bsdf == null) {
            bsdf = (Bsdf)new Diffuse.Builder().build();
        }
    }

    public String toString() {
        return String.format(
                "Mesh[\n" +
                "  name = \"%s\"\n" +
                "  vertexCount = %d\n" +
                "  triangleCount = %d\n" +
                "  bsdf = %s\n" +
                "  emitter = %s\n" +
                "]",
                name,
                positions.size(),
                triangles.size(),
                StringUtil.indent(StringUtil.safeToString(bsdf)),
                StringUtil.indent(StringUtil.safeToString(emitter))
        );
    }
}
