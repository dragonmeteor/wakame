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

import wakame.struct.Color3d;
import wakame.struct.Measure;

import javax_.vecmath.Vector3d;

/**
 * Convenience data structure used to pass multiple
 * parameters to the evaluation and sampling routines in BSDF.
 */
public class BsdfQueryRecord {
    /**
     * Incident direction (in the local frame).
     */
    public Vector3d wi = new Vector3d();
    /**
     * Outgoing direction (in the local frame).
     */
    public Vector3d wo = new Vector3d();
    /**
     * Relative refractive index in the sampled direction.
     */
    public double eta = 1;
    /**
     * Measure associated with the sample
     */
    public Measure measure = Measure.UnknownMeasure;
    /**
     * Sampling direction.
     */
    public BsdfSampledDirection direction = BsdfSampledDirection.Wi;
    /**
     * BSDF sample value
     */
    public Color3d value = new Color3d();

    public static BsdfQueryRecord createWiSampleRecord(Vector3d wo, Measure measure) {
        BsdfQueryRecord result = new BsdfQueryRecord();
        result.direction = BsdfSampledDirection.Wi;
        result.wo.set(wo);
        result.measure = measure;
        return result;
    }

    public static BsdfQueryRecord createWoSampleRecord(Vector3d wi, Measure measure) {
        BsdfQueryRecord result = new BsdfQueryRecord();
        result.direction = BsdfSampledDirection.Wo;
        result.wi.set(wi);
        result.measure = measure;
        return result;
    }

    public static BsdfQueryRecord createWiSampleRecord(Vector3d wo) {
        return createWiSampleRecord(wo, Measure.UnknownMeasure);
    }

    public static BsdfQueryRecord createWoSampleRecord(Vector3d wi) {
        return createWoSampleRecord(wi, Measure.UnknownMeasure);
    }
}
