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

package wakame.util;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Discrete probability distribution
 *
 * This data structure can be used to transform uniformly distributed
 * samples to a stored discrete probability distribution.
 */
public class DiscretePdf {
    private ArrayList<Double> cdf;
    private double sum;
    private double normalization;
    private boolean normalized;

    private DiscretePdf() {
        // NO-OP
    }

    /**
     * Return the number of entries.
     * @return the number of entries
     */
    public int size() {
        return cdf.size();
    }

    /**
     * Return the orignal (unnormalized) sum of all PDF entries.
     * @return
     */
    public double getSum() {
        return sum;
    }

    /**
     * Return the normalization factor (i.e., the inverse of getSum()).
     * @return the normalization factor
     */
    public double getNormalization() {
        return normalization;
    }

    /**
     * Transform a uniformly distributed sample to the stored distribution.
     * @param sampleValue a uniformly distributed sample on [0,1]
     * @return the discrete index associated with the sample
     */
    public int sample(double sampleValue) {
        int index = Collections.binarySearch(cdf, sampleValue);
        if (index >= 0) {
            return index;
        } else {
            index = -(index+1);
            return index;
        }
    }

    /**
     * Return the probability of sampling the item with given index.
     * @param index the index of the item sampled
     * @return the probability of sampling the item
     */
    public double pdf(int index) {
        if (index == 0)
            return cdf.get(0);
        else
            return cdf.get(index) - cdf.get(index-1);
    }

    /**
     * Create this class, add the items, and then call build() to
     * get an instance of DiscretePDF.
     */
    public static class Builder {
        private ArrayList<Double> values = new ArrayList<Double>();

        /**
         * Add a weight of an item to the collection of weights.
         * @param value
         */
        public Builder add(double value) {
            values.add(value);
            return this;
        }

        /**
         * Actually create a DiscretePDF instance.
         * @return an instance of DiscretePDF built on the collection of items.
         */
        public DiscretePdf build() {
            DiscretePdf result = new DiscretePdf();
            result.sum = 0;
            for (int i = 0; i < values.size(); i++) {
                result.sum += values.get(i);
                values.set(i, result.sum);
            }
            result.normalization = 1.0 / result.sum;
            for (int i = 0; i < values.size(); i++) {
                values.set(i, values.get(i) * result.normalization);
            }
            values.set(values.size()-1, 1.0);
            result.cdf = values;
            return result;
        }
    }
}
