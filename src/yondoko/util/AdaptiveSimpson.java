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

package yondoko.util;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Adaptive Simpson integration over an 1D interval.
 */
public class AdaptiveSimpson {
    private Function<Double, Double> f;
    private double x0;
    private double x1;
    private double eps = 1e-6;
    private int depth = 6;

    private AdaptiveSimpson(Function<Double, Double> f, double x0, double x1, double eps, int depth) {
        this.f = f;
        this.x0 = x0;
        this.x1 = x1;
        this.eps = eps;
        this.depth = depth;
    }

    private AdaptiveSimpson(Function<Double, Double> f, double x0, double x1) {
        this(f, x0, x1, 1e-6, 6);
    }

    private double integrate() {
        double a = x0;
        double b = 0.5 * (x0+x1);
        double c = x1;
        double fa = f.apply(a);
        double fb = f.apply(b);
        double fc = f.apply(c);
        double I = (c-a) * (1.0/6.0) * (fa+4.0*fb+fc);
        return integrate(a, b, c, fa, fb, fc, I, eps, depth);
    }

    private double integrate(double a, double b, double c, double fa, double fb, double fc, double I,
                            double eps, int depth) {
        /* Evaluate the function at two intermediate points */
        double d = 0.5 * (a+b);
        double e = 0.5 * (b+c);
        double fd = f.apply(d);
        double fe = f.apply(e);

        /* Simpson integration over each subinterval */
        double h = c-a;
        double I0 = (1.0/12.0) * h * (fa + 4.0*fd + fb);
        double I1 = (1.0/12.0) * h * (fb + 4.0*fe + fc);
        double Ip = I0+I1;

        /* Stopping criterion from J.N. Lyness (1969)
			  "Notes on the adaptive Simpson quadrature routine" */
        if (depth <= 0 || Math.abs(Ip-I) < 15*eps) {
            // Richardson extrapolation
            return Ip + (1.0/15.0) * (Ip-I);
        }

        return integrate(a, d, b, fa, fd, fb, I0, 0.5*eps, depth-1) +
                integrate(b, e, c, fb, fe, fc, I1, 0.5*eps, depth-1);
    }

    public static double integrate1D(Function<Double, Double> f, double x0, double x1, double eps, int depth) {
        AdaptiveSimpson as = new AdaptiveSimpson(f, x0, x1, eps, depth);
        return as.integrate();
    }

    public static double integrate1D(Function<Double, Double> f, double x0, double x1) {
        return integrate1D(f, x0, x1, 1e-6, 6);
    }

    public static double integrate2D(BiFunction<Double, Double, Double> f,
                                     double x0, double y0, double x1, double y1,
                                     double eps, int depth) {
        Function<Double, Double> integrand = (Double y) -> integrate1D((x) -> f.apply(x,y), x0, x1, eps, depth);
        double value = integrate1D(integrand, y0, y1, eps, depth);
        return value;
    }

    public static double integrate2D(BiFunction<Double, Double, Double> f,
                                     double x0, double y0, double x1, double y1) {
        return integrate2D(f, x0, y0, x1, y1, 1e-6, 6);
    }
}
