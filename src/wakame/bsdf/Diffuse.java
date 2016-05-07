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
import wakame.util.Warp;

import javax_.vecmath.Vector2d;
import javax_.vecmath.Vector3d;
import java.util.HashMap;

/**
 * Diffuse / Lambertian BRDF model
 */
public class Diffuse extends Bsdf {
    private Color3d albedo = new Color3d(0.5, 0.5, 0.5);

    private Diffuse() {
        // NO-OP
    }

    @Override
    protected void activate() {
        // NO-OP
    }

    @Override
    protected void setProperties(HashMap<String, Object> properties) {
        /**
         * The albedo.
         */
        albedo.set(PropertiesUtil.getColor(properties, "albedo", new Color3d(0.5, 0.5, 0.5)));
    }

    @Override
    public void sample(BsdfQueryRecord bRec, Sampler sampler) {
        bRec.measure = Measure.SolidAngle;
        Vector2d sample = new Vector2d();
        sampler.next2D(sample);

        if (bRec.direction == BsdfSampledDirection.Wi) {
            if (Frame.cosTheta(bRec.wo) <= 0) {
                bRec.value.set(0, 0, 0);
                return;
            }
            Warp.squareToCosineHemisphere(sample, bRec.wi);
        } else {
            if (Frame.cosTheta(bRec.wi) <= 0) {
                bRec.value.set(0, 0, 0);
                return;
            }
            Warp.squareToCosineHemisphere(sample, bRec.wo);
        }
        bRec.value.set(albedo);
    }

    @Override
    public void eval(Vector3d wi, Vector3d wo, Measure measure, Color3d value) {
        /* This is a smooth BRDF -- return zero if the measure
           is wrong, or when queried for illumination on the backside */
        if (measure != Measure.SolidAngle || Frame.cosTheta(wi) <= 0 || Frame.cosTheta(wo) <= 0) {
            value.set(0,0,0);
        } else {
            value.scale(1 / Math.PI, albedo);
        }
    }

    @Override
    public double pdf(BsdfQueryRecord bRec) {
        /* This is a smooth BRDF -- return zero if the measure
           is wrong, or when queried for illumination on the backside */
        if (bRec.measure != Measure.SolidAngle
                || Frame.cosTheta(bRec.wi) <= 0
                || Frame.cosTheta(bRec.wo) <= 0) {
            return 0;
        } else {
            /* Importance sampling density wrt. solid angles:
            cos(theta) / pi.

           Note that the directions in 'bRec' are in local coordinates,
           so Frame.cosTheta() actually just returns the 'z' component.
           */
            if (bRec.direction == BsdfSampledDirection.Wi) {
                return Frame.cosTheta(bRec.wi) / Math.PI;
            } else {
                return Frame.cosTheta(bRec.wo) / Math.PI;
            }
        }
    }

    public String toString() {
        return String.format(
                "Diffuse[\n" +
                "  albedo = %s\n" +
                "]", albedo.toString());
    }

    @Override
    public boolean isDiffuse() {
        return true;
    }

    public static class Builder extends WakameObject.Builder {

        @Override
        protected WakameObject createInstance() {
            return new Diffuse();
        }
    }
}
