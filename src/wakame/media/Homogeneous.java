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
import yondoko.util.VectorUtil;

import java.util.HashMap;

/**
 * Box-shaped homogeneous participating medium
 */
public class Homogeneous extends Medium {
    private Color3d sigmaS = new Color3d();
    private Color3d sigmaT = new Color3d();
    private Transform worldToMedium = new Transform();
    private Aabb3d bbox = new Aabb3d();

    private Homogeneous() {
        // The bounding box is always assumed to be [0,1]^3.
        bbox.pMin.set(0,0,0);
        bbox.pMax.set(1, 1, 1);
    }

    @Override
    protected void setProperties(HashMap<String, Object> properties) {
        /**
         * The scattering coefficient.
         */
        sigmaS.set(PropertiesUtil.getColor(properties, "sigmaS"));
        /**
         * The extinction coefficient.
         */
        // Get the absorption coefficient first.
        sigmaT.set(PropertiesUtil.getColor(properties, "sigmaA"));
        // sigmaT = sigmaS + sigmaA;
        sigmaT.add(sigmaS);
        /**
         * The world to medium transform.
         */
        worldToMedium.set(PropertiesUtil.getTransform(properties, "toWorld", new Transform()));
        worldToMedium.invert();
    }


    @Override
    public boolean sampleDistance(Ray ray, Sampler sampler, MediumQueryRecord mRec) {
        // The maximum possible distance should be stored in the ray (ray.maxT)
        // Attenuation = tau(s) = exp(-sigma_t*s), where s is arc length
        // We want to importance sample s
        // If we normalize tau (assuming distances can go from 0 to infinity) and find the cdf
        // cdf(tau(s)) = 1 - exp(-sigma_t*s)
        // So to importance sample s, choose random number r between 0 and 1, solve cdf(tau(s)) = r
        Ray localRay = new Ray();
        worldToMedium.m.transform(ray.o, localRay.o);
        worldToMedium.m.transform(ray.d, localRay.d);

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

        // Use min to avoid infinite variance.
        double sigma = VectorUtil.minComponent(sigmaT);
        mRec.t = -Math.log(1 - sampler.next1D()) / sigma;
        mRec.t += localRay.mint;

        if (mRec.t < localRay.maxt) {
            // Sampled a point inside the medium, weight is (sigma_s * transmittance) / pdf(t)
            double s = mRec.t - localRay.mint;
            double pdf = Math.exp(-s * sigma) * sigma;
            Color3d tau = new Color3d();
            tau.x = Math.exp(-s * sigmaT.x);
            tau.y = Math.exp(-s * sigmaT.y);
            tau.z = Math.exp(-s * sigmaT.z);
            mRec.weight.set(sigmaS);
            mRec.weight.mul(tau);
            mRec.weight.scale(1 / pdf);
            return true;
        } else {
            // Sampled a point past the end of the medium, weight is transmittance / prob of failure
            // cdf(tau(maxT)) = 1 - exp(-sigmaT*(maxT - minT))
            // so probability of sampling point past maxT is 1-cdf(tau(maxT-minT)) = exp(-simgaT*(maxT-minT))
            double s = localRay.maxt - localRay.mint;
            double pdf = Math.exp(-sigma*s);
            Color3d tau = new Color3d();
            tau.x = Math.exp(-s * sigmaT.x);
            tau.y = Math.exp(-s * sigmaT.y);
            tau.z = Math.exp(-s * sigmaT.z);
            mRec.weight.set(tau);
            mRec.weight.scale(1 / pdf);
            return false;
        }
    }

    @Override
    public void evalTransmittance(Ray ray, Sampler sampler, Color3d output) {
        // The transmittance along the ray is exp(- int_mint^maxt sigmaT(t) dt)
        // In homogeneous media, this is exp( -sigmaT * int_mint^maxT dt )
        Ray localRay = new Ray();
        worldToMedium.m.transform(ray.o, localRay.o);
        worldToMedium.m.transform(ray.d, localRay.d);

        double[] nearFar = new double[2];
        boolean hit = bbox.rayIntersect(localRay, nearFar);
        double near = nearFar[0];
        double far = nearFar[1];
        if (!hit) {
            output.set(1,1,1);
            return;
        }

        localRay.mint = Math.max(ray.mint, near);
        localRay.maxt = Math.min(ray.maxt, far);
        double dist = localRay.maxt - localRay.mint;
        if (dist < 0) {
            output.set(1, 1, 1);
        } else {
            output.x = Math.exp(-sigmaT.x * dist);
            output.y = Math.exp(-sigmaT.y * dist);
            output.z = Math.exp(-sigmaT.z * dist);
        }
    }

    @Override
    protected void activate() {
        if (phaseFunction == null) {
            phaseFunction = (PhaseFunction)new Isotropic.Builder().build();
        }
    }

    public String toString() {
        return String.format(
                "HomogeneousMedium[\n" +
                "  sigmaS = %f\n",
                "  sigmaT = %f\n",
                "  phase = %s\n",
                "]",
                sigmaS, sigmaT, phaseFunction.toString());
    }

    public static class Builder extends WakameObject.Builder {
        @Override
        protected WakameObject createInstance() {
            return new Homogeneous();
        }
    }
}
