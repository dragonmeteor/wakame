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

package wakame.media;

import wakame.WakameObject;
import wakame.phase.Isotropic;
import wakame.phase.PhaseFunction;
import wakame.sampler.Sampler;
import wakame.struct.Aabb3d;
import wakame.struct.Color3d;
import wakame.struct.Ray;
import wakame.struct.Transform;
import wakame.util.PropertiesUtil;
import wakame.volume.GridVolume;
import yondoko.util.FileResolver;

import javax_.vecmath.Point3d;

import java.util.HashMap;

/**
 * Heterogeneous participating medium class. The implementation
 * fetches density values from an external file that is mapped into memory.
 *
 * The density values in the file are interpreted as the extinction
 * coefficient sigma_t. The scattering albedo is assumed to be constant
 * throughout the volume, and is given as a parameter to this class.
 */
public class Heterogeneous extends Medium {
    private Color3d albedo = new Color3d();
    private Transform worldToMedium = new Transform();
    private double densityMultiplier;
    private String fileName;
    private Aabb3d bbox = new Aabb3d();
    private GridVolume data;

    private Heterogeneous() {
        // The bounding box is always assumed to be [0,1]^3.
        bbox.pMin.set(0,0,0);
        bbox.pMax.set(1,1,1);
    }

    @Override
    protected void setProperties(HashMap<String, Object> properties) {
        /**
         * The scattering albedo.
         */
        albedo.set(PropertiesUtil.getColor(properties, "albedo"));
        /**
         * The world-to-medium transform
         */
        worldToMedium.set(PropertiesUtil.getTransform(properties, "toWorld", new Transform()));
        worldToMedium.invert();
        /**
         * Optional multiplicative factor that will be applied to all density values in the file.
         */
        densityMultiplier = PropertiesUtil.getDouble(properties, "densityMultiplier", 1.0);
        /**
         * Get the data.
         */
        fileName = PropertiesUtil.getString(properties, "filename");
        String resolvedFileName = FileResolver.resolve(fileName);
        data = GridVolume.load(resolvedFileName);
        // Set the bounding box of the underlying volume to be the same as the global one.
        data.setBbox(bbox);
    }

    @Override
    public boolean sampleDistance(Ray ray, Sampler sampler, MediumQueryRecord mRec) {
        // Transform the ray into local coordinate system.
        Ray localRay = new Ray();
        worldToMedium.m.transform(ray.o, localRay.o);
        worldToMedium.m.transform(ray.d, localRay.d);
        //System.out.println(ray);

        double[] nearFar = new double[2];
        boolean hit = bbox.rayIntersect(localRay, nearFar);
        double near = nearFar[0];
        double far = nearFar[1];
        if (!hit) {
            mRec.weight.set(1,1,1);
            return false;
        }

        localRay.mint = Math.max(ray.mint, near);
        localRay.maxt = Math.min(ray.maxt, far);

        //System.out.println("localRay = " + localRay);

        mRec.t = localRay.mint;
        Point3d p = new Point3d();
        // Sample distance by Woodcock tracking.
        while (true) {
            if (mRec.t > localRay.maxt) {
                mRec.weight.set(1,1,1);
                break;
            }

            mRec.t += -Math.log(1 - sampler.next1D()) / densityMultiplier;
            localRay.project(mRec.t, p);
            double sigmaT = data.lookupFloat(p) * densityMultiplier;
            if (sampler.next1D() * densityMultiplier < sigmaT) {
                mRec.weight.set(albedo);
                return true;
            }
        }
        return false;
    }

    @Override
    public void evalTransmittance(Ray ray, Sampler sampler, Color3d output) {
        // Transform the ray into local coordinate system.
        Ray localRay = new Ray();
        worldToMedium.m.transform(ray.o, localRay.o);
        worldToMedium.m.transform(ray.d, localRay.d);

        double[] nearFar = new double[2];
        boolean hit = bbox.rayIntersect(localRay, nearFar);
        double near = nearFar[0];
        double far = nearFar[1];
        if (!hit) {
            output.set(1,1,1);
        }

        localRay.mint = Math.max(ray.mint, near);
        localRay.maxt = Math.min(ray.maxt, far);

        double dist = localRay.maxt - localRay.mint;
        if (dist <= 0) {
            output.set(1,1,1);
        } else {
            double integral = data.integrateFloat(localRay);
            double value = Math.exp(-densityMultiplier *  integral);
            output.set(value, value, value);
        }
    }

    @Override
    protected void activate() {
        if (phaseFunction == null) {
            phaseFunction = (PhaseFunction)new Isotropic.Builder().build();
        }
    }

    public static class Builder extends WakameObject.Builder {
        @Override
        protected WakameObject createInstance() {
            return new Heterogeneous();
        }
    }
}
