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

package wakame.accel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wakame.Constants;
import wakame.mesh.Mesh;
import wakame.struct.Aabb3d;
import wakame.struct.Intersection;
import wakame.struct.Ray;
import yondoko.util.VectorUtil;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class Bvh {
    /**
     * The logger
     */
    private static Logger logger = LoggerFactory.getLogger(Bvh.class);
    // Build-related parameters.
    /**
     * Switch to a serial build when less than 32 triangles are left.
     */
    private static final int SERIAL_THRESHOLD = 32;
    /**
     * Heuristic cost value for traveral operation.
     */
    private static final double TRAVERSAL_COST = 1;
    /**
     * Hueristic cost value for intersection operations.
     */
    private static final double INTERSECTION_COST = 1;

    // Fields
    /**
     * List of meshes registered with this BVH.
     */
    private ArrayList<Mesh> meshes = new ArrayList<Mesh>();
    /**
     * Index of the first triangle of each mesh.
     */
    private ArrayList<Integer> meshOffset = new ArrayList<Integer>();
    /**
     * BVH nodes.
     */
    private BvhNode[] nodes;
    /**
     * Triangle indices referenced by the BVH nodes.
     */
    private Integer[] indices;
    /**
     * The surface area of the bounding box of triangles to the left of the reference index.
     * Used when computing the SAH cost of a split.
     */
    private Double[] leftAreas;
    /**
     * Bounding box of the entire BVH.
     */
    private Aabb3d bbox = new Aabb3d();
    /**
     * The fork-join pool.
     */
    ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

    /**
     * Create a new and empty BVH.
     */
    public Bvh() {
        meshOffset.add(0);
    }

    /**
     * Register a triangle mesh for inclusion in the BVH.
     *
     * @param mesh
     */
    public void addMesh(Mesh mesh) {
        meshes.add(mesh);
        meshOffset.add(mesh.getTriangleCount() + meshOffset.get(meshOffset.size() - 1));
        Aabb3d meshBbox = new Aabb3d();
        mesh.getBoundingBox(meshBbox);
        bbox.expandBy(meshBbox);
    }

    /**
     * Return the number of registered meshes.
     *
     * @return the number of registered meshes
     */
    public int getMeshCount() {
        return meshes.size();
    }

    /**
     * Return the mesh with the given index.
     *
     * @param index
     * @return the mesh with the given index
     */
    public Mesh getMesh(int index) {
        return meshes.get(index);
    }

    /**
     * Get the bounding box of the whole BVH.
     */
    public void getBoundingBox(Aabb3d bbox) {
        bbox.set(this.bbox);
    }

    /**
     * Get the number of triangles.
     */
    public int getTriangleCount() {
        return meshOffset.get(meshOffset.size() - 1);
    }

    /**
     * Return the index of the mesh containing the triangle with the given index.
     *
     * @param triangleIndex the index of the triangle
     * @return the index of the mesh containing the triangle with the given index
     */
    protected int findMesh(int triangleIndex) {
        int searchResult = Collections.binarySearch(meshOffset, triangleIndex);
        if (searchResult >= 0)
            return searchResult;
        else
            return -(searchResult + 1) - 1;
    }

    /**
     * Get the centroid of the triangle with the given index.
     *
     * @param triangleIndex the index of the triangle
     * @param centroid      the receiver of the centroid value
     */
    protected void getTriangleCentroid(int triangleIndex, javax_.vecmath.Tuple3d centroid) {
        int meshIndex = findMesh(triangleIndex);
        triangleIndex -= meshOffset.get(meshIndex);
        meshes.get(meshIndex).getTriangleCentroid(triangleIndex, centroid);
    }

    /**
     * Get the bounding box of the triangle with the given index.
     *
     * @param triangleIndex the index of the triangle
     * @param bbox          the receiver of the bounding box value
     */
    protected void getTriangleBoundingBox(int triangleIndex, Aabb3d bbox) {
        int meshIndex = findMesh(triangleIndex);
        triangleIndex -= meshOffset.get(meshIndex);
        meshes.get(meshIndex).getTriangleBoundingBox(triangleIndex, bbox);
    }

    /**
     * Build the BVH.
     */
    public void build() {
        int size = getTriangleCount();
        if (size == 0)
            return;
        logger.info("Constructing a SAH BVH from " + meshes.size() + " mesh(es) with "
                + size + " triangles.");

        long start = System.currentTimeMillis();

        nodes = new BvhNode[2*size];
        leftAreas = new Double[size];
        indices = new Integer[size];
        IntStream.range(0, size).parallel().forEach((int i) -> {
            indices[i] = i;
        });

        BvhBuildTask task = new BvhBuildTask(0, 0, size);
        forkJoinPool.invoke(task);

        long end = System.currentTimeMillis();
        long elapsed = end - start;
        logger.info(String.format("BVH building took %d min(s) %d second(s) %d ms",
                elapsed / (60 * 1000), (elapsed / 1000) % 60, elapsed % 1000));

        leftAreas = null;
    }

    protected class BvhNode {
        public Aabb3d bbox = new Aabb3d();
        public int start;
        public int size;
        public int axis;
        public int rightChild;
        public boolean isLeaf = true;
    }

    public BvhNode createLeaf(int start, int size) {
        BvhNode node = new BvhNode();
        node.isLeaf = true;
        node.start = start;
        node.size = size;
        return node;
    }

    public BvhNode createInternalNode(int axis, int rightChild) {
        BvhNode node = new BvhNode();
        node.isLeaf = false;
        node.axis = axis;
        node.rightChild = rightChild;
        return node;
    }

    protected class CentroidComparator implements Comparator<Integer> {
        int axis = 0;

        public CentroidComparator(int axis) {
            this.axis = axis;
        }

        @Override
        public int compare(Integer i1, Integer i2) {
            javax_.vecmath.Vector3d centroid1 = new javax_.vecmath.Vector3d();
            javax_.vecmath.Vector3d centroid2 = new javax_.vecmath.Vector3d();
            getTriangleCentroid(i1, centroid1);
            getTriangleCentroid(i2, centroid2);
            return Double.compare(VectorUtil.getComponent(centroid1, axis),
                    VectorUtil.getComponent(centroid2, axis));
        }
    }

    protected class BvhBuildTask extends RecursiveAction {
        int nodeIndex;
        int start;
        int end;

        public BvhBuildTask(int nodeIndex, int start, int end) {
            this.nodeIndex = nodeIndex;
            this.start = start;
            this.end = end;
        }

        public void compute() {
            int size = end - start;

            if (size < SERIAL_THRESHOLD) {
                executeSerially(nodeIndex, start, end);
                return;
            }

            double bestCost = INTERSECTION_COST * size;
            int bestIndex = -1;
            int bestAxis = -1;
            Aabb3d nodeBbox = new Aabb3d();

            for (int axis = 0; axis < 3; axis++) {
                /* Sort all triangles based on their centroid positions projected on the axis */
                CentroidComparator comparator = new CentroidComparator(axis);
                Arrays.parallelSort(indices, start, end, comparator);

                Aabb3d bbox = new Aabb3d();
                Aabb3d triBbox = new Aabb3d();
                for (int i = start; i < end; i++) {
                    getTriangleBoundingBox(indices[i], triBbox);
                    bbox.expandBy(triBbox);
                    leftAreas[i] = bbox.getSurfaceArea();
                }

                if (axis == 0)
                    nodeBbox.set(bbox);

                // Choose the best splitting plane.
                bbox.reset();
                double triFactor = INTERSECTION_COST / nodeBbox.getSurfaceArea();
                for (int i = size - 1; i >= 1; i--) {
                    int index = start + i;
                    int triIndex = indices[index];
                    getTriangleBoundingBox(triIndex, triBbox);
                    bbox.expandBy(triBbox);
                    double leftArea = leftAreas[index - 1];
                    double rightArea = bbox.getSurfaceArea();
                    int primsLeft = i;
                    int primsRight = size - i;
                    double sahCost = 2.0 * TRAVERSAL_COST
                            + triFactor * (primsLeft * leftArea + primsRight * rightArea);
                    if (sahCost < bestCost) {
                        bestCost = sahCost;
                        bestIndex = i;
                        bestAxis = axis;
                    }
                }
            }

            if (bestIndex == -1) {
                // Splitting does not reduce the cost, make a leaf.
                BvhNode node = createLeaf(start, size);
                node.bbox.set(nodeBbox);
                nodes[nodeIndex] = node;
            } else {
                CentroidComparator comparator = new CentroidComparator(bestAxis);
                Arrays.parallelSort(indices, start, end, comparator);

                int leftCount = bestIndex;
                int nodeIndexLeft = nodeIndex + 1;
                int nodeIndexRight = nodeIndex + 2 * leftCount;

                BvhNode node = createInternalNode(bestIndex, nodeIndexRight);
                node.bbox.set(nodeBbox);
                nodes[nodeIndex] = node;

                BvhBuildTask leftTask = new BvhBuildTask(nodeIndexLeft, start, start+leftCount);
                BvhBuildTask rightTask = new BvhBuildTask(nodeIndexRight, start+leftCount, end);
                invokeAll(leftTask, rightTask);
            }
        }

        public void executeSerially(int nodeIndex, int start, int end) {
            int size = end - start;
            double bestCost = INTERSECTION_COST * size;
            int bestIndex = -1;
            int bestAxis = -1;
            Aabb3d nodeBbox = new Aabb3d();

            for (int axis = 0; axis < 3; axis++) {
                /* Sort all triangles based on their centroid positions projected on the axis */
                CentroidComparator comparator = new CentroidComparator(axis);
                Arrays.sort(indices, start, end, comparator);

                Aabb3d bbox = new Aabb3d();
                Aabb3d triBbox = new Aabb3d();
                for (int i = start; i < end; i++) {
                    getTriangleBoundingBox(indices[i], triBbox);
                    bbox.expandBy(triBbox);
                    leftAreas[i] = bbox.getSurfaceArea();
                }

                if (axis == 0)
                    nodeBbox.set(bbox);

                // Choose the best splitting plane.
                bbox.reset();
                double triFactor = INTERSECTION_COST / nodeBbox.getSurfaceArea();
                for (int i = size - 1; i >= 1; i--) {
                    int index = start + i;
                    int triIndex = indices[index];
                    getTriangleBoundingBox(triIndex, triBbox);
                    bbox.expandBy(triBbox);
                    double leftArea = leftAreas[index - 1];
                    double rightArea = bbox.getSurfaceArea();
                    int primsLeft = i;
                    int primsRight = size - i;
                    double sahCost = 2.0 * TRAVERSAL_COST
                            + triFactor * (primsLeft * leftArea + primsRight * rightArea);
                    if (sahCost < bestCost) {
                        bestCost = sahCost;
                        bestIndex = i;
                        bestAxis = axis;
                    }
                }
            }

            if (bestIndex == -1) {
                // Splitting does not reduce the cost, make a leaf.
                BvhNode node = createLeaf(start, size);
                node.bbox.set(nodeBbox);
                nodes[nodeIndex] = node;
            } else {
                CentroidComparator comparator = new CentroidComparator(bestAxis);
                Arrays.sort(indices, start, end, comparator);

                int leftCount = bestIndex;
                int nodeIndexLeft = nodeIndex + 1;
                int nodeIndexRight = nodeIndex + 2 * leftCount;

                BvhNode node = createInternalNode(bestIndex, nodeIndexRight);
                node.bbox.set(nodeBbox);
                nodes[nodeIndex] = node;

                executeSerially(nodeIndexLeft, start, start + leftCount);
                executeSerially(nodeIndexRight, start + leftCount, end);
            }
        }
    }

    /**
     * Intersect a ray against all triangle meshes registered
     * with the BVH
     *
     * Detailed information about the intersection, if any, will be
     * stored in the provided Intersection data record.
     *
     * The shadowRay parameter specifies whether this detailed
     * information is really needed. When set to true, the
     * function just checks whether or not there is occlusion, but without
     * providing any more detail (i.e. its will not be filled with
     * contents). This is usually much faster.
     *
     * @param _ray the ray
     * @param its the receiver of the intersection information
     * @param shadowRay whether the ray is a shadow ray
     * @return whether the ray hits something
     */
    public boolean rayIntersect(Ray _ray, Intersection its, boolean shadowRay) {
        int nodeIdx = 0;
        int stackIdx = 0;
        int[] stack = new int[64];
        Mesh.TriangleIntersection triIts = new Mesh.TriangleIntersection();


        /* Use an adaptive ray epsilon */
        Ray ray = new Ray(_ray);
        double[] dRcp = new double[] { 1.0 / ray.d.x, 1.0 / ray.d.y, 1.0 / ray.d.z };
        if (ray.mint == Constants.EPSILON) {
            ray.mint = Math.max(ray.mint, ray.mint * Math.max(Math.abs(ray.o.x), Math.max(Math.abs(ray.o.y), Math.abs(ray.o.z))));
        }
        //System.out.println(ray);

        if (nodes == null || ray.maxt < ray.mint) {
            return false;
        }

        boolean foundIntersection = false;
        int f = 0;

        //int nodesVisited = 0;
        while (true) {
            //nodesVisited++;

            BvhNode node = nodes[nodeIdx];
            /*
            System.out.println("stackIdx = " + stackIdx);
            System.out.println("nodeIdx = " + nodeIdx);
            System.out.println("node.bbox = " + node.bbox);
            System.out.println("node is leaf?: " + (node instanceof BvhLeaf));
            */

            if (!node.bbox.rayIntersectFast(ray, dRcp)) {
                if (stackIdx == 0)
                    break;
                stackIdx--;
                nodeIdx = stack[stackIdx];
                continue;
            }

            if (!node.isLeaf) {
                stack[stackIdx] = node.rightChild;
                stackIdx++;
                nodeIdx++;
            } else {
                for (int i = node.start; i < node.start + node.size; i++) {
                    int idx = indices[i];
                    int meshIdx = findMesh(idx);
                    Mesh mesh = meshes.get(meshIdx);
                    int triIdx = idx - meshOffset.get(meshIdx);

                    if (mesh.rayIntersect(triIdx, ray, triIts)) {
                        if (shadowRay)
                            return true;
                        foundIntersection = true;
                        ray.maxt = its.t = triIts.t;
                        its.uv.set(triIts.bary.y, triIts.bary.z);
                        its.mesh = mesh;
                        f = triIdx;
                    }
                }
                if (stackIdx == 0)
                    break;
                stackIdx--;
                nodeIdx = stack[stackIdx];
                continue;
            }
        }
        //System.out.println("nodesVisited = " + nodesVisited);

        if (foundIntersection) {
            /* Find the barycentric coordinates */
            javax_.vecmath.Vector3d bary = new javax_.vecmath.Vector3d(1 - its.uv.x - its.uv.y, its.uv.x, its.uv.y);

            /* References to all relevant mesh buffers */
            Mesh mesh = its.mesh;
            List<javax_.vecmath.Point3d> V = mesh.getPositions();
            List<javax_.vecmath.Vector3d> N = mesh.getNormals();
            List<javax_.vecmath.Vector2d> UV = mesh.getTexCoords();
            List<javax_.vecmath.Point3i> F = mesh.getTriangles();

            /* Vertex indices of the triangle */
            javax_.vecmath.Point3i tri = F.get(f);
            int idx0 = tri.x;
            int idx1 = tri.y;
            int idx2 = tri.z;

            javax_.vecmath.Point3d p0 = V.get(idx0);
            javax_.vecmath.Point3d p1 = V.get(idx1);
            javax_.vecmath.Point3d p2 = V.get(idx2);

            /* Compute the intersection positon accurately using barycentric coordinates */
            its.p.set(0,0,0);
            its.p.scaleAdd(bary.x, p0, its.p);
            its.p.scaleAdd(bary.y, p1, its.p);
            its.p.scaleAdd(bary.z, p2, its.p);

            /* Compute proper texture coordinates if provided by the mesh */
            if (UV.size() > 0) {
                its.uv.set(0,0);
                its.uv.scaleAdd(bary.x, UV.get(idx0), its.uv);
                its.uv.scaleAdd(bary.y, UV.get(idx1), its.uv);
                its.uv.scaleAdd(bary.z, UV.get(idx2), its.uv);
            }

            /* Compute the geometry frame */
            javax_.vecmath.Vector3d v1 = new javax_.vecmath.Vector3d();
            v1.sub(p1, p0);
            javax_.vecmath.Vector3d v2 = new javax_.vecmath.Vector3d();
            v2.sub(p2, p0);
            javax_.vecmath.Vector3d n = new javax_.vecmath.Vector3d();  n.cross(v1, v2);
            its.geoFrame.setFromNormal(n);

            if (N.size() > 0) {
                /* Compute the shading frame. Note that for simplicity,
                the current implementation doesn't attempt to provide
                tangents that are continuous across the surface. That
                means that this code will need to be modified to be able
                use anisotropic BRDFs, which need tangent continuity */
                n.set(0, 0, 0);
                n.scaleAdd(bary.x, N.get(idx0), n);
                n.scaleAdd(bary.y, N.get(idx1), n);
                n.scaleAdd(bary.z, N.get(idx2), n);
                its.shFrame.setFromNormal(n);
            } else {
                its.shFrame.set(its.geoFrame);
            }
        }

        return foundIntersection;
    }
}
