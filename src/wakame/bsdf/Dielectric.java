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

package wakame.bsdf;

import wakame.WakameObject;
import wakame.sampler.Sampler;
import wakame.struct.Color3d;
import wakame.struct.Frame;
import wakame.struct.Measure;
import wakame.util.PropertiesUtil;

import javax_.vecmath.Vector3d;
import java.util.HashMap;

/**
 * Dielectric BSDF
 */
public class Dielectric extends Bsdf {
    double intIOR;
    double extIOR;

    private Dielectric() {
        // NO-OP
    }

    @Override
    protected void setProperties(HashMap<String, Object> properties) {
        /**
         * Interior index of refraction (default: BK7 borosilicate optical glass)
         */
        intIOR = PropertiesUtil.getDouble(properties, "intIOR", 1.5046);
        /**
         * External idex of refraction (default: air)
         */
        extIOR = PropertiesUtil.getDouble(properties, "extIOR", 1.000277);
    }

    @Override
    public void sample(BsdfQueryRecord bRec, Sampler sampler) {
        bRec.measure = Measure.Discrete;
        if (bRec.direction == BsdfSampledDirection.Wi) {
            sample(bRec, bRec.wo, bRec.wi, sampler);
        } else {
            sample(bRec, bRec.wi, bRec.wo, sampler);
        }
    }

    private void sample(BsdfQueryRecord bRec, Vector3d wi, Vector3d wo, Sampler sampler) {
        double cosThetaI = Frame.cosTheta(wi);
        double etaI, etaT;
        // Check whether the ray enters or leaving the surface.
        boolean entering = Frame.cosTheta(wi) > 0;
        if (entering) {
            etaI = extIOR;
            etaT = intIOR;
        } else {
            etaI = intIOR;
            etaT = extIOR;
        }

        /* Using Snell's law, calculate the squared sine of the
           angle between the normal and the transmitted ray */
        double invEta = etaI / etaT;
        double invEta2 = invEta * invEta;
        double sinThetaTSqrt = invEta2 * Frame.sinTheta2(wi);

        double Fr;
        double cosThetaT = 0;
        if (sinThetaTSqrt > 1.0) {
            // Total internal reflection.
            Fr = 1;
        } else {
            cosThetaT = Math.sqrt(1 - sinThetaTSqrt);
            cosThetaI = Math.abs(cosThetaI);

            double Rs = (etaI * cosThetaI - etaT * cosThetaT)
                    / (etaI * cosThetaI + etaT * cosThetaT);
            double Rp = (etaT * cosThetaI - etaI * cosThetaT)
                    / (etaT * cosThetaI + etaI * cosThetaT);

            Fr = (Rs * Rs + Rp * Rp) / 2.0f;

            if (entering)
                cosThetaT = -cosThetaT;
        }

        if (sampler.next1D() <= Fr) {
            // Reflection in local coordinates
            wo.set(-wi.x, -wi.y, wi.z);
            bRec.measure = Measure.Discrete;
            // Relative index of refraction: no change
            bRec.eta = 1;

            bRec.value.set(1,1,1);
        } else {
             /* Given cos(theta_t), compute the transmitted direction */
            wo.set(-invEta*wi.x, -invEta*wi.y, cosThetaT);
            wo.normalize();
            bRec.measure = Measure.Discrete;

            /* Also return the relative refractive index change */
            bRec.eta = 1.0f/invEta;

            /* Account for the solid angle change at boundaries with
               different indices of refraction. */
            bRec.value.set(invEta2, invEta2, invEta2);
        }
    }

    @Override
    public void eval(Vector3d wi, Vector3d wo, Measure measure, Color3d value) {
        value.set(0,0,0);
    }

    @Override
    public double pdf(BsdfQueryRecord bRec) {
        // Discrete BRDFs always evaluate to zero in Wakame.
        return 0;
    }

    @Override
    public boolean isDiffuse() {
        return false;
    }

    @Override
    protected void activate() {
        // NO-OP
    }

    public String toString() {
        return "Dielectirc[intIOR=" + intIOR + ", extIOR=" + extIOR + "]";
    }

    public static class Builder extends WakameObject.Builder {
        @Override
        protected WakameObject createInstance() {
            return new Dielectric();
        }
    }
}
