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

package wakame.phase;

import wakame.WakameObject;
import wakame.sampler.Sampler;
import wakame.util.Warp;

import javax_.vecmath.Vector2d;
import javax_.vecmath.Vector3d;
import java.util.HashMap;

/**
 * Isotropic phase function model.
 */
public class Isotropic extends PhaseFunction {
    private Isotropic() {
        // NO-OP
    }

    @Override
    public double sample(PhaseFunctionQueryRecord pRec, Sampler sampler) {
        Vector2d sample = new Vector2d(sampler.next1D(), sampler.next1D());
        if (pRec.direction == PhaseFunctionSampledDirection.Wi) {
            Warp.squareToUniformSphere(sample, pRec.wi);
        } else {
            Warp.squareToUniformSphere(sample, pRec.wo);
        }
        return 1.0;
    }

    @Override
    public double eval(Vector3d wi, Vector3d wo) {
        return 1.0 / (4 * Math.PI);
    }

    @Override
    public double pdf(PhaseFunctionQueryRecord pRec) {
        return 1.0 / (4 * Math.PI);
    }

    @Override
    protected void activate() {
        // NO-OP
    }

    @Override
    protected void setProperties(HashMap<String, Object> properties) {
        // NO-OP
    }

    @Override
    public String toString() {
        return "Isotropic[]";
    }

    public static class Builder extends WakameObject.Builder {
        @Override
        protected WakameObject createInstance() {
            return new Isotropic();
        }
    }
}
