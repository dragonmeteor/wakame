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

import org.apache.commons.io.EndianUtils;
import org.apache.commons.io.input.SwappedDataInputStream;
import wakame.struct.Aabb3d;
import wakame.struct.Aabb3f;
import wakame.struct.Ray;
import yondoko.util.VectorUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Represent a volumetric data in the format of 3D grid.
 */
public class GridVolume implements Volume {
    public static int FLOAT_VOLUME = 0;
    public static int VECTOR_VOLUME = 1;
    private int volumeType;
    private ByteBuffer data;
    private int size[];
    private Aabb3f aabb = new Aabb3f();

    /**
     * Create a grid volume with the given volume type and sizes
     * @param volumeType the volume type (whether a float or a vector volume)
     * @param gridSizeX the number of voxels in the x-axis
     * @param gridSizeY the number of voxels in the y-axis
     * @param gridSizeZ the number of voxels in the z-axis
     */
    public GridVolume(int volumeType, int gridSizeX, int gridSizeY, int gridSizeZ) {
        if (volumeType != FLOAT_VOLUME && volumeType != VECTOR_VOLUME) {
            throw new RuntimeException("invalid volume type");
        }
        this.volumeType = volumeType;

        size = new int[3];
        size[0] = gridSizeX;
        size[1] = gridSizeY;
        size[2] = gridSizeZ;

        if (size[0] <= 0 || size[1] <= 0 || size[2] <= 0) {
            throw new RuntimeException("none of the size can be less than or equal to 0");
        }

        allocateData();
    }

    /**
     * Allocate the data for the underlying volume.
     */
    public void allocateData() {
        if (volumeType == FLOAT_VOLUME) {
            data = ByteBuffer.allocate(size[0] * size[1] * size[2] * 4);
        } else {
            data = ByteBuffer.allocate(size[0] * size[1] * size[2] * 3 * 4);
        }
    }

    /**
     * Returns whether the volume supports float lookup.
     * @return whether the volume supports float lookup.
     */
    public boolean supportFloatLookups() {
        return volumeType == FLOAT_VOLUME;
    }

    /**
     * Look up a floating point value at the given position
     * @param p the position to look up floating point value
     * @return the floating point value at the given position
     */
    @Override
    public double lookupFloat(javax_.vecmath.Tuple3d p) {
        return getFloat((float)p.x, (float)p.y, (float)p.z);
    }

    /**
     * Look up a floating point value at the given position
     * @param p the position to look up floating point value
     * @return the floating point value at the given position
     */
    public float lookupFloat(javax_.vecmath.Tuple3f p) {
        return getFloat(p.x, p.y, p.z);
    }

    /**
     * Returns whether the volume supports vector lookup.
     * @return whether the volume supports vector lookup
     */
    public boolean supportVectorLookups() {
        return volumeType == VECTOR_VOLUME;
    }

    /**
     * Look up the vector value at the given position.
     * @param p the position to look up the vector value
     * @param output the receiver of the vector value
     */
    @Override
    public void lookupVector(javax_.vecmath.Tuple3d p, javax_.vecmath.Tuple3d output) {
        javax_.vecmath.Vector3f result = new javax_.vecmath.Vector3f();
        getVector((float) p.x, (float) p.y, (float) p.z, result);
        output.set(result);
    }

    /**
     * Return the axis-aligned bounding box of the volume.
     * @param bbox the receiver of the axis-aligned bounding box.
     */
    @Override
    public void getBbox(Aabb3d bbox) {
        bbox.pMin.set(aabb.pMin.x, aabb.pMin.y, aabb.pMin.z);
        bbox.pMax.set(aabb.pMax.x, aabb.pMax.y, aabb.pMax.z);
    }

    /**
     * Return the axis-aligned bounding box of the volume.
     * @param bbox the receiver of the axis-aligned bounding box.
     */
    public void getBbox(Aabb3f bbox) {
        bbox.set(aabb);
    }

    /**
     * Return the axis-aligned bounding box of the volume.
     * @return the axis-aligned bounding box of the volume.
     */
    public Aabb3f getBBox() {
        return aabb;
    }

    /**
     * Look up the vector value at the given position.
     * @param p the position to look up the vector value
     * @param output the receiver of the vector value
     */
    public void lookupVector(javax_.vecmath.Tuple3f p, javax_.vecmath.Vector3f output) {
        getVector(p.x, p.y, p.z, output);
    }

    /**
     * Set the volume's axis-aligned bounding box to the given values.
     * @param xMin the minimum x-coordinate of the axis-aligned bounding box
     * @param yMin the minimum y-coordinate of the axis-aligned bounding box
     * @param zMin the minimum z-coordinate of the axis-aligned bounding box
     * @param xMax the maximum x-coordinate of the axis-aligned bounding box
     * @param yMax the maximum y-coordinate of the axis-aligned bounding box
     * @param zMax the maximum z-coordinate of the axis-aligned bounding box
     */
    public void setBbox(float xMin, float yMin, float zMin, float xMax, float yMax, float zMax) {
        aabb.pMin.set(xMin, yMin, zMin);
        aabb.pMax.set(xMax, yMax, zMax);
    }

    /**
     * Set the volume's axis-aligned bounding box to the given bounding box
     * @param value an axis-aligned bounding box
     */
    public void setBbox(Aabb3f value) {
        aabb.set(value);
    }

    /**
     * Set the volume's axis-aligned bounding box to the given bounding box
     * @param value an axis-aligned bounding box
     */
    public void setBbox(Aabb3d value) {
        aabb.pMin.set(value.pMin);
        aabb.pMax.set(value.pMax);
    }

    /**
     * Load the grid volume from the given file.
     * @param fileName the file name
     * @return the grid volume loaded from the given file
     */
    public static GridVolume load(String fileName) {
        try {
            File file = new File(fileName);
            FileInputStream fin_ = new FileInputStream(file);
            SwappedDataInputStream fin = new SwappedDataInputStream(fin_);

            byte magic0 = fin.readByte();
            byte magic1 = fin.readByte();
            byte magic2 = fin.readByte();
            if (magic0 != 'V' || magic1 != 'O' || magic2 != 'L') {
                throw new RuntimeException("Unrecognized volume file format: magic is not 'VOL'");
            }

            byte fileFormatVersion = fin.readByte();
            if (fileFormatVersion != 3) {
                throw new RuntimeException("Unrecognized volume file format: version is not 3");
            }

            int encoding = fin.readInt();
            if (encoding != 1) {
                throw new RuntimeException("Encoding oither than 1 is not supported");
            }

            int numCellsX = fin.readInt();
            int numCellsY = fin.readInt();
            int numCellsZ = fin.readInt();

            int fileNumChannels = fin.readInt();
            int volumeType;
            if (fileNumChannels == 1) {
                volumeType = FLOAT_VOLUME;
            } else if (fileNumChannels == 3) {
                volumeType = VECTOR_VOLUME;
            } else {
                throw new RuntimeException("Invalid channel count (" + fileNumChannels + "). Only 1 and 3 are supported.");
            }

            GridVolume result = new GridVolume(volumeType, numCellsX, numCellsY, numCellsZ);
            result.aabb.pMin.x = fin.readFloat();
            result.aabb.pMin.y = fin.readFloat();
            result.aabb.pMin.z = fin.readFloat();
            result.aabb.pMax.x = fin.readFloat();
            result.aabb.pMax.y = fin.readFloat();
            result.aabb.pMax.z = fin.readFloat();

            ByteBuffer buffer;
            FileChannel channel = fin_.getChannel();
            if (volumeType == FLOAT_VOLUME) {
                buffer = ByteBuffer.allocate(result.getCellCount() * 4);
                channel.read(buffer);
            } else {
                buffer = ByteBuffer.allocate(result.getCellCount() * 12);
                channel.read(buffer);
            }
            buffer.rewind();
            fin.close();

            result.data = buffer;

            float max = -1;
            for (int i = 0; i < buffer.capacity() / 4; i++) {
                float y = result.getFloat(i);
                max = Math.max(y, max);
            }
            //System.out.println("max = " + max);

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the number of cells in the volume.
     * @return the number of cells in the volume
     */
    public int getCellCount() {
        return size[0] * size[1] * size[2];
    }

    /**
     * Return the underlying data buffer.
     * @return the underlying data buffer.
     */
    public Buffer getData() {
        return data;
    }

    /**
     * Return the number of voxels in the x-dimension.
     * @return the number of voxels in the x-dimension
     */
    public int getXCellCount() {
        return size[0];
    }

    /**
     * Return the number of voxels in the y-dimension.
     * @return the number of voxels in the y-dimension
     */
    public int getYCellCount() {
        return size[1];
    }

    /**
     * Return the number of voxels in the z-dimension.
     * @return the number of voxels in the z-dimension
     */
    public int getZCellCount() {
        return size[2];
    }

    /**
     * Return the number of voxels in the dimension with the given index.
     * @param d the index of the dimension (0 = x, 1 = y, 2 = z)
     * @return the number of voxels in the dimension with the given index
     */
    public int getCellCount(int d) {
        return size[d];
    }

    /**
     * Return the width of a voxel in the dimension with the given index.
     * @param d the index of the dimension (0 = x, 1 = y, 2 = z)
     * @return the width of a voxel in the dimension with the given index
     */
    public float getCellSize(int d) {
        if (d == 0) {
            return (aabb.pMax.x - aabb.pMin.x) / size[0];
        } else if (d == 1) {
            return (aabb.pMax.y - aabb.pMin.y) / size[1];
        } else if (d == 2) {
            return (aabb.pMax.z - aabb.pMin.z) / size[2];
        } else {
            throw new RuntimeException("invalid dimension d=" + d);
        }
    }

    /**
     * Return the width of a voxel in the x-dimension.
     * @return the width of a voxel in the x-dimension
     */
    public float getXCellSize() {
        return getCellSize(0);
    }

    /**
     * Return the width of a voxel in the y-dimension.
     * @return the width of a voxel in the y-dimension
     */
    public float getYCellSize() {
        return getCellSize(1);
    }

    /**
     * Return the width of a voxel in the z-dimension.
     * @return the width of a voxel in the z-dimension
     */
    public float getZCellSize() {
        return getCellSize(2);
    }

    private float getFloat(int index) {
        int a = (data.get(4 * index + 0) & 0xFF);
        int b = (data.get(4 * index + 1) & 0xFF);
        int c = (data.get(4 * index + 2) & 0xFF);
        int d = (data.get(4 * index + 3) & 0xFF);
        int v = ((d << 24) | (c << 16) | (b << 8) | (a));
        return Float.intBitsToFloat(v);
    }

    private int getCellIndex(int x, int y, int z) {
        int index = z * size[1] * size[0] + y * size[0] + x;
        return index;
    }

    private void cellIndexToXyz(int index, javax_.vecmath.Tuple3i xyz) {
        xyz.x = index % size[0];
        xyz.y = (index / size[0]) % size[1];
        xyz.z = index / (size[0] * size[1]);
    }

    private void setFloat(int index, float v) {
        int iv = Float.floatToIntBits(v);
        int a = (iv & 0xFF);
        int b = ((iv >> 8) & 0xFF);
        int c = ((iv >> 16) & 0xFF);
        int d = ((iv >> 24) & 0xFF);
        data.put(4 * index + 0, (byte) (a & 0xFF));
        data.put(4 * index + 1, (byte) (b & 0xFF));
        data.put(4 * index + 2, (byte) (c & 0xFF));
        data.put(4 * index + 3, (byte) (d & 0xFF));
    }

    private float getFloat(int x, int y, int z) {
        int index = z * size[1] * size[0] + y * size[0] + x;
        return getFloat(index);
    }

    public void setFloat(int x, int y, int z, float value) {
        int index = z * size[1] * size[0] + y * size[0] + x;
        setFloat(index, value);
    }

    public void setVector(int x, int y, int z, javax_.vecmath.Tuple3f value) {
        int index = z * size[1] * size[0] + y * size[0] + x;
        setFloat(3 * index + 0, value.x);
        setFloat(3 * index + 1, value.y);
        setFloat(3 * index + 2, value.z);
    }

    private void getVector(int x, int y, int z, javax_.vecmath.Tuple3f result) {
        int index = z * size[1] * size[0] + y * size[0] + x;
        result.x = getFloat(3 * index + 0);
        result.y = getFloat(3 * index + 1);
        result.z = getFloat(3 * index + 2);
    }

    private float getVectorComponent(int x, int y, int z, int component) {
        int index = 3*(z * size[1] * size[0] + y * size[0] + x) + component;
        return getFloat(index);
    }

    private void setVectorComponent(int x, int y, int z, int component, float value) {
        int index = 3*(z * size[1] * size[0] + y * size[0] + x) + component;
        setFloat(index, value);
    }

    private float getFloat(float x, float y, float z) {
        float xx = (x - aabb.pMin.x) / (aabb.pMax.x - aabb.pMin.x) * (size[0]-1);
        float yy = (y - aabb.pMin.y) / (aabb.pMax.y - aabb.pMin.y) * (size[1]-1);
        float zz = (z - aabb.pMin.z) / (aabb.pMax.z - aabb.pMin.z) * (size[2]-1);

        if (xx < 0 || xx > size[0]-1 || yy < 0 || yy > size[1]-1 || zz < 0 || zz > size[2]-1) {
            return 0;
        }

        int ix = (int) Math.floor(xx);
        int iy = (int) Math.floor(yy);
        int iz = (int) Math.floor(zz);
        float dx = xx - ix;
        float dy = yy - iy;
        float dz = zz - iz;

        if (ix < 0) {
            ix = 0;
            dx = 0;
        } else if (ix == size[0] - 1) {
            ix = size[0] - 2;
            dx = 1;
        }

        if (iy < 0) {
            iy = 0;
            dy = 0;
        } else if (iy == size[1] - 1) {
            iy = size[1] - 2;
            dy = 1;
        }

        if (iz < 0) {
            iz = 0;
            dz = 0;
        } else if (iz == size[2] - 1) {
            iz = size[2] - 2;
            dz = 1;
        }

        //System.out.println("ix = " + ix + ", iy = " + iy + ", iz = " + iz);

        float v000 = getFloat(ix + 0, iy + 0, iz + 0);
        float v001 = getFloat(ix + 0, iy + 0, iz + 1);
        float v010 = getFloat(ix + 0, iy + 1, iz + 0);
        float v011 = getFloat(ix + 0, iy + 1, iz + 1);
        float v100 = getFloat(ix + 1, iy + 0, iz + 0);
        float v101 = getFloat(ix + 1, iy + 0, iz + 1);
        float v110 = getFloat(ix + 1, iy + 1, iz + 0);
        float v111 = getFloat(ix + 1, iy + 1, iz + 1);

        //System.out.printf("v000 = %f, v001 = %f, v010 = %f, v010 = %f, v100 = %f, v101 = %f, v110 = %f, v111 = %f", v000, v001, v010, v011, v100, v101, v110, v111);

        return v000 * (1 - dx) * (1 - dy) * (1 - dz)
                + v001 * (1 - dx) * (1 - dy) * (dz)
                + v010 * (1 - dx) * (dy) * (1 - dz)
                + v011 * (1 - dx) * (dy) * (dz)
                + v100 * (dx) * (1 - dy) * (1 - dz)
                + v101 * (dx) * (1 - dy) * (dz)
                + v110 * (dx) * (dy) * (1 - dz)
                + v111 * (dx) * (dy) * (dz);
    }

    private void getVector(float x, float y, float z, javax_.vecmath.Tuple3f result) {
        javax_.vecmath.Vector3f temp = new javax_.vecmath.Vector3f();
        result.set(0, 0, 0);

        float xx = (x - aabb.pMin.x) / (aabb.pMax.x - aabb.pMin.x) * (size[0]-1);
        float yy = (y - aabb.pMin.y) / (aabb.pMax.y - aabb.pMin.y) * (size[1]-1);
        float zz = (z - aabb.pMin.z) / (aabb.pMax.z - aabb.pMin.z) * (size[2]-1);

        if (xx < 0 || xx > size[0]-1 || yy < 0 || yy > size[1]-1 || zz < 0 || zz > size[2]-1) {
            // NOP
        } else {
            int ix = (int) Math.floor(xx);
            int iy = (int) Math.floor(yy);
            int iz = (int) Math.floor(zz);
            float dx = xx - ix;
            float dy = yy - iy;
            float dz = zz - iz;

            if (ix < 0) {
                xx = 0;
                dx = 0;
            } else if (ix == size[0] - 1) {
                ix = size[0] - 2;
                dx = 1;
            }

            if (iy < 0) {
                ix = 0;
                dy = 0;
            } else if (iy == size[1] - 1) {
                iy = size[1] - 2;
                dy = 1;
            }

            if (iz < 0) {
                iz = 0;
                dz = 0;
            } else if (iz == size[2] - 1) {
                iz = size[2] - 2;
                dz = 1;
            }

            getVector(ix + 0, iy + 0, iz + 0, temp);
            result.scaleAdd((1 - dx) * (1 - dy) * (1 - dz), temp, result);
            getVector(ix + 0, iy + 0, iz + 1, temp);
            result.scaleAdd((1 - dx) * (1 - dy) * (dz), temp, result);
            getVector(ix + 0, iy + 1, iz + 0, temp);
            result.scaleAdd((1 - dx) * (dy) * (1 - dz), temp, result);
            getVector(ix + 0, iy + 1, iz + 1, temp);
            result.scaleAdd((1 - dx) * (dy) * (dz), temp, result);
            getVector(ix + 1, iy + 0, iz + 0, temp);
            result.scaleAdd((dx) * (1 - dy) * (1 - dz), temp, result);
            getVector(ix + 1, iy + 0, iz + 1, temp);
            result.scaleAdd((dx) * (1 - dy) * (dz), temp, result);
            getVector(ix + 1, iy + 1, iz + 0, temp);
            result.scaleAdd((dx) * (dy) * (1 - dz), temp, result);
            getVector(ix + 1, iy + 1, iz + 1, temp);
            result.scaleAdd((dx) * (dy) * (dz), temp, result);

            float l = (float) Math.sqrt(result.x * result.x + result.y * result.y + result.z * result.z);
            result.x /= l;
            result.y /= l;
            result.z /= l;
        }
    }

    /**
     * Save the volume to the given file.
     * @param fileName the file name
     */
    public void save(String fileName) {
        try {
            File file = new File(fileName);
            FileOutputStream fout = new FileOutputStream(file);

            fout.write('V');
            fout.write('O');
            fout.write('L');
            fout.write(3);

            EndianUtils.writeSwappedInteger(fout, 1);
            EndianUtils.writeSwappedInteger(fout, size[0]);
            EndianUtils.writeSwappedInteger(fout, size[1]);
            EndianUtils.writeSwappedInteger(fout, size[2]);

            if (volumeType == FLOAT_VOLUME) {
                EndianUtils.writeSwappedInteger(fout, 1);
            } else {
                EndianUtils.writeSwappedInteger(fout, 3);
            }

            EndianUtils.writeSwappedFloat(fout, aabb.pMin.x);
            EndianUtils.writeSwappedFloat(fout, aabb.pMin.y);
            EndianUtils.writeSwappedFloat(fout, aabb.pMin.z);
            EndianUtils.writeSwappedFloat(fout, aabb.pMax.x);
            EndianUtils.writeSwappedFloat(fout, aabb.pMax.y);
            EndianUtils.writeSwappedFloat(fout, aabb.pMax.z);

            FileChannel channel = fout.getChannel();
            channel.write(data);

            fout.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void gridToWorldCoord(javax_.vecmath.Tuple3f gridPos, javax_.vecmath.Tuple3f worldPos) {
        worldPos.x = aabb.pMin.x + gridPos.x * getXCellSize();
        worldPos.y = aabb.pMin.y + gridPos.y * getYCellSize();
        worldPos.z = aabb.pMin.z + gridPos.z * getZCellSize();
    }

    /**
     * Return the volume type.
     * @return the volume type
     */
    public int getVolumeType() {
        return volumeType;
    }

    /**
     * Integrate the float value along the ray from t=a to t=b.
     * The function is a degree three polynomial in t, so Gaussian quadrature with 2 points suffice.
     * @param ray the ray
     * @param a starting time
     * @param b end time
     * @return the integral
     */
    private double integrateFloatOneCell(Ray ray, double a, double b) {
        javax_.vecmath.Point3d pA = new javax_.vecmath.Point3d();
        javax_.vecmath.Point3d pB = new javax_.vecmath.Point3d();
        double t0 = -1/Math.sqrt(3);
        double t1 =  1/Math.sqrt(3);
        ray.project((b-a)/2*t0 + (b+a)/2, pA);
        ray.project((b-a)/2*t1 + (b+a)/2, pB);
        double vA = getFloat((float)pA.x, (float)pA.y, (float)pA.z);
        double vB = getFloat((float)pB.x, (float)pB.y, (float)pB.z);
        return (b-a)/2 * (vA+vB);
    }

    /**
     * Integrate the floating point values along the ray using the arc length measure.
     * @param ray the to integrate the floating point values on
     * @return the integral
     */
    public double integrateFloat(Ray ray) {
        Aabb3d bbox = new Aabb3d();
        bbox.pMin.set(this.aabb.pMin);
        bbox.pMax.set(this.aabb.pMax);
        double[] nearFar = new double[2];
        boolean hit = bbox.rayIntersect(ray, nearFar);
        if (!hit)
            return 0;
        double near = nearFar[0];
        double far = nearFar[1];

        near = Math.max(near, ray.mint);
        far = Math.min(far, ray.maxt);

        if (near >= far) {
            return 0;
        }

        double t = near;
        double[] nextT = new double[3];
        int[] nextIndex = new int[3];
        double[] dRcp = new double[]{1.0 / ray.d.x, 1.0 / ray.d.y, 1.0 / ray.d.z};
        for (int dim = 0; dim < 3; dim++) {
            double position = VectorUtil.getComponent(ray.o, dim) + t * VectorUtil.getComponent(ray.d, dim);
            double minPos = VectorUtil.getComponent(aabb.pMin, dim);
            double maxPos = VectorUtil.getComponent(aabb.pMax, dim);
            double fracPos = (position - minPos) / (maxPos - minPos) * (size[dim]-1);
            int cell0 = (int)Math.floor(fracPos);
            cell0 = Math.max(0, Math.min(size[dim]-2, cell0));
            int cell1 = cell0+1;
            if (VectorUtil.getComponent(ray.d, dim) >= 0) {
                nextIndex[dim] = cell1;
            } else {
                nextIndex[dim] = cell0;
            }
            double nextPlanePos = minPos + (maxPos - minPos) * nextIndex[dim] * 1.0 / (size[dim]-1);
            nextT[dim] = (nextPlanePos - VectorUtil.getComponent(ray.o, dim)) * dRcp[dim];
            if (Double.isNaN(nextT[dim])) {
                nextT[dim] = Double.MAX_VALUE;
            }
        }

        // Digital differential analyzer
        double result = 0;
        while (true) {
            int dim = getMinIndex(nextT);
            double next = nextT[dim];
            if (nextIndex[dim] < 0 || nextIndex[dim] > size[dim]-1) {
                break;
            }
            if (next >= far) {
                next = far;
            }
            double integral = integrateFloatOneCell(ray, t, next);
            result += integral;
            if (next == far)
                break;
            if (VectorUtil.getComponent(ray.d, dim) > 0) {
                nextIndex[dim] = nextIndex[dim]+1;
            } else {
                nextIndex[dim] = nextIndex[dim]-1;
            }
            double minPos = VectorUtil.getComponent(aabb.pMin, dim);
            double maxPos = VectorUtil.getComponent(aabb.pMax, dim);
            double nextPlanePos = minPos + (maxPos - minPos) * nextIndex[dim] * 1.0 / (size[dim]-1);
            nextT[dim] = (nextPlanePos - VectorUtil.getComponent(ray.o, dim)) * dRcp[dim];
            t = next;
        }

        return result;
    }

    private int getMinIndex(double[] v) {
        if (v[0] < v[1]) {
            if (v[0] < v[2])
                return 0;
            else
                return 2;
        } else {
            if (v[1] < v[2])
                return 1;
            else
                return 2;
        }
    }
}
