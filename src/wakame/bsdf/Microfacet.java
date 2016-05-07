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
import yondoko.util.VectorUtil;

import javax_.vecmath.Tuple2d;
import javax_.vecmath.Vector2d;
import javax_.vecmath.Vector3d;
import java.util.HashMap;

/**
 * Microfacet BSDF.  See Walter et al. [2006].
 */
public class Microfacet extends Bsdf {
    private double alpha;
    private double intIOR;
    private double extIOR;
    private Color3d kd = new Color3d();
    private double ks;

    private Microfacet() {
        // NO-OP
    }

    @Override
    protected void setProperties(HashMap<String, Object> properties) {
        /**
         * RMS surface roughness.
         */
        alpha = PropertiesUtil.getDouble(properties, "alpha", 0.1);
        /**
         * Interior index of refraction (default: BK7 borosilicate optical glass)
         */
        intIOR = PropertiesUtil.getDouble(properties, "intIOR", 1.5046);
        /**
         * Exterior index of refraction (default: air)
         */
        extIOR = PropertiesUtil.getDouble(properties, "extIOR", 1.000277);
        /**
         * Albedo of the diffuse base material (a.k.a "kd")
         */
        kd.set(PropertiesUtil.getColor(properties, "kd", new Color3d(0.5,0.5,0.5)));
        /**
         * The coefficient of the specular component.
         * Set to 1-maxComponent(kd) by default to ensure energy conservation.
         *
         * While that is not a particularly realistic model of what
         * happens in reality, this will greatly simplify the
         * implementation.
         */
        ks = 1 - VectorUtil.maxComponent(kd);
    }

    @Override
    public void sample(BsdfQueryRecord bRec, Sampler sampler) {
        // TODO: Fill in this method.
    }

    @Override
    public void eval(Vector3d wi, Vector3d wo, Measure measure, Color3d value) {
        // TODO: Fill in this method.
    }

    @Override
    public double pdf(BsdfQueryRecord bRec) {
        // TODO: Fill in this method.
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
        return String.format(
                "Microfacet[\n" +
                "  alpha = %f\n" +
                "  intIOR = %f\n" +
                "  extIOR = %f\n" +
                "  kd = %s\n" +
                "]",
                alpha, intIOR, extIOR, kd.toString()
        );
    }

    public static class Builder extends WakameObject.Builder {
        @Override
        protected WakameObject createInstance() {
            return new Microfacet();
        }
    }
}
