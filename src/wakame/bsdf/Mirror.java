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

import javax_.vecmath.Vector3d;
import java.util.HashMap;

/**
 * Ideal mirror BRDF
 */
public class Mirror extends Bsdf {
    private Mirror() {
        // NO-OP
    }

    @Override
    public void sample(BsdfQueryRecord bRec, Sampler sampler) {
        if (bRec.direction == BsdfSampledDirection.Wi) {
            if (Frame.cosTheta(bRec.wo) < 0) {
                bRec.value.set(0, 0, 0);
            } else {
                bRec.value.set(1,1,1);
                // Relative index of refraction: no change.
                bRec.eta = 1.0;
                // Reflection in local coordinate.
                bRec.wi.set(-bRec.wo.x, -bRec.wo.y, bRec.wo.z);
            }
            bRec.measure = Measure.Discrete;
        } else {
            if (Frame.cosTheta(bRec.wi) < 0) {
                bRec.value.set(0, 0, 0);
                bRec.measure = Measure.Discrete;
            } else {
                bRec.value.set(1,1,1);
                // Relative index of refraction: no change.
                bRec.eta = 1.0;
                // Reflection in local coordinate.
                bRec.wo.set(-bRec.wi.x, -bRec.wi.y, bRec.wi.z);
            }
            bRec.measure = Measure.Discrete;
        }
    }

    @Override
    public void eval(Vector3d wi, Vector3d wo, Measure measure, Color3d value) {
        // Discrete BRDFs always evaluate to zero in Wakame.
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

    @Override
    protected void setProperties(HashMap<String, Object> properties) {
        // NO-OP
    }

    public String toString() {
        return "Mirror[]";
    }

    public static class Builder extends WakameObject.Builder {
        @Override
        protected WakameObject createInstance() {
            return new Mirror();
        }
    }
}
