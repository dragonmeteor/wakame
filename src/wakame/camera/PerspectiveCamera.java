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
import wakame.rfilter.GaussianFilter;
import wakame.rfilter.ReconstructionFilter;
import wakame.struct.Color3d;
import wakame.struct.Ray;
import wakame.struct.Transform;
import wakame.util.PropertiesUtil;
import yondoko.util.StringUtil;

import javax_.vecmath.Matrix4d;
import javax_.vecmath.Point3d;
import javax_.vecmath.Vector2d;
import javax_.vecmath.Vector3d;
import java.util.HashMap;

/**
 * Perspective camera with depth of field
 * <p>
 * This class implements a simple perspective camera model. It uses an
 * infinitesimally small aperture, creating an infinite depth of field.
 */
public class PerspectiveCamera extends Camera {
    /**
     * The component-wise reciprocal of the output image size.
     */
    private Vector2d invOutputSize = new Vector2d();
    /**
     * The sample-to-camera transformation.
     */
    private Transform sampleToCamera = new Transform();
    /**
     * The camera-to-world transformation.
     */
    private Transform cameraToWorld = new Transform();
    /**
     * The horizontal field of view in degrees.
     */
    private double fovX;
    /**
     * The near clipping plane location in world-space unit.
     */
    private double nearClip;
    /**
     * The far clipping plane location in world-space unit.
     */
    private double farClip;

    private PerspectiveCamera() {
        // NO-OP
    }

    @Override
    protected void setProperties(HashMap<String, Object> properties) {
        // Width and height of the output image in pixels.  Default is 720p.
        outputSizeX = PropertiesUtil.getInteger(properties, "width", 1280);
        outputSizeY = PropertiesUtil.getInteger(properties, "height", 720);
        invOutputSize.x = 1.0 / outputSizeX;
        invOutputSize.y = 1.0 / outputSizeY;

        // Specifies an optional camera-to-world transformation.  Default is the identity transformation.
        cameraToWorld.set(PropertiesUtil.getTransform(properties, "toWorld", new Transform()));

        // Horizontal field of view in degrees
        fovX = PropertiesUtil.getDouble(properties, "fov", 30);

        // Near and far clipping planes in world-space units.
        nearClip = PropertiesUtil.getDouble(properties, "nearClip", 1e-4);
        farClip = PropertiesUtil.getDouble(properties, "farClip", 1e4);

        rfilter = null;
    }

    @Override
    public void sampleRay(Vector2d samplePosition, Vector2d apertureSample, Ray ray, Color3d importanceWeight) {
        /* Compute the corresponding position on the
           near plane (in local camera space) */
        Point3d nearP = new Point3d(samplePosition.x * invOutputSize.x,
                samplePosition.y * invOutputSize.y,
                0.0);
        sampleToCamera.m.transform(nearP);

        /* Turn into a normalized ray direction, and
           adjust the ray interval accordingly */
        ray.d.set(nearP);
        ray.d.normalize();
        double invZ = 1.0 / ray.d.z;

        ray.o.set(0, 0, 0);
        cameraToWorld.m.transform(ray.o);
        cameraToWorld.m.transform(ray.d);
        ray.mint = nearClip * invZ;
        ray.maxt = farClip * invZ;

        importanceWeight.set(1, 1, 1);
    }

    @Override
    protected void activate() {
        double aspect = outputSizeX * 1.0 / outputSizeY;

        /* Project vectors in camera space onto a plane at z=1:
         *
         *  xProj = cot * x / z
         *  yProj = cot * y / z
         *  zProj = (far * (z - near)) / (z * (far-near))
         *  The cotangent factor ensures that the field of view is
         *  mapped to the interval [-1, 1].
         */
        double recip = 1.0 / (farClip - nearClip);
        double cot = 1.0 / Math.tan(fovX / 2.0 * Math.PI / 180);
        Matrix4d perspective = new Matrix4d(new double[]{
                cot, 0, 0, 0,
                0, cot, 0, 0,
                0, 0, farClip * recip, -nearClip * farClip * recip,
                0, 0, 1, 0
        });

        Matrix4d M0 = new Matrix4d();
        M0.setIdentity();
        M0.m00 = 0.5;
        M0.m11 = -0.5 * aspect;
        M0.m22 = 1.0;
        Matrix4d M1 = new Matrix4d();
        M1.setIdentity();
        M1.setTranslation(new Vector3d(1.0, -1.0 / aspect, 0));
        M0.mul(M1);
        M0.mul(perspective);
        M0.invert();
        sampleToCamera = new Transform(M0);

        /* If no reconstruction filter was assigned, instantiate a Gaussian filter */
        if (rfilter == null) {
            rfilter = (ReconstructionFilter)new GaussianFilter.Builder().build();
        }
    }

    @Override
    public void addChild(WakameObject obj) {
        if (obj instanceof ReconstructionFilter) {
            if (rfilter != null) {
                throw new RuntimeException("PerspectiveCamera.addChild(): " +
                        "Tried to register multiple reconstruction filters!");
            }
            rfilter = (ReconstructionFilter) obj;
        } else {
            throw new RuntimeException("PerspectiveCamera.addChild(): " +
                    "A children of type " + obj.getClass().getName() + " is not supported");
        }
    }

    public String toString() {
        return String.format(
                "PerspectiveCamera[\n" +
                "  cameraToWorld = \n" +
                "    %s\n" +
                "  outputSize = %d x %d\n" +
                "  fovX = %f\n" +
                "  clip = [%f, %f]\n" +
                "  rfilter = %s\n" +
                "]",
                StringUtil.indent(StringUtil.safeToString(cameraToWorld), 2),
                outputSizeX, outputSizeY,
                fovX,
                nearClip, farClip,
                StringUtil.indent(StringUtil.safeToString(rfilter))
        );
    }

    public static class Builder extends WakameObject.Builder {
        @Override
        protected WakameObject createInstance() {
            return new PerspectiveCamera();
        }
    }
}
