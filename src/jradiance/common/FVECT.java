/* 
 * Copyright (C) 2015 Andrew Willis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jradiance.common;

/**
 *
 * @author arwillis
 */
public class FVECT {
    /*
     * Declarations for floating-point vector operations.
     */
//#ifdef  SMLFLT
//#define  RREAL		float
//#define  FTINY		(1e-3)
//#else
//#define  RREAL		double
//#define  FTINY		(1e-6)
//#endif
    public double[] data = new double[3];
    public static double FTINY = 1e-6;
    public static double FHUGE = 1e10;

    public FVECT() {
        data[0] = data[1] = data[2] = 0;
    }
    
    public FVECT(double[] data) {
        this.data[0] = data[0];
        this.data[1] = data[1];
        this.data[2] = data[2];
    }
    public FVECT(double a, double b, double c) {
        data[0] = a;
        data[1] = b;
        data[2] = c;
    }
    public double[] getData() { return data; }
    public static void VCOPY(FVECT v1, FVECT v2) {
        v1.data[0] = v2.data[0];
        v1.data[1] = v2.data[1];
        v1.data[2] = v2.data[2];
    }

    public static double DOT(FVECT v1, FVECT v2) {
        return v1.data[0] * v2.data[0] + v1.data[1] * v2.data[1] + v1.data[2] * v2.data[2];
    }

    public static double VLEN(FVECT v) {
        return Math.sqrt(DOT(v, v));
    }

    public static void VADD(FVECT vr, FVECT v1, FVECT v2) {
        vr.data[0] = v1.data[0] + v2.data[0];
        vr.data[1] = v1.data[1] + v2.data[1];
        vr.data[2] = v1.data[2] + v2.data[2];
    }

    public static void VSUB(FVECT vr, FVECT v1, FVECT v2) {
        vr.data[0] = v1.data[0] - v2.data[0];
        vr.data[1] = v1.data[1] - v2.data[1];
        vr.data[2] = v1.data[2] - v2.data[2];
    }

    public static void VSUM(FVECT vr, FVECT v1, FVECT v2, double f) {
        vr.data[0] = v1.data[0] + f * v2.data[0];
        vr.data[1] = v1.data[1] + f * v2.data[1];
        vr.data[2] = v1.data[2] + f * v2.data[2];
    }

    public static void VCROSS(FVECT vr, FVECT v1, FVECT v2) {
        vr.data[0] = v1.data[1] * v2.data[2] - v1.data[2] * v2.data[1];
        vr.data[1] = v1.data[2] * v2.data[0] - v1.data[0] * v2.data[2];
        vr.data[2] = v1.data[0] * v2.data[1] - v1.data[1] * v2.data[0];
    }
    public FVECT copy() {
        return new FVECT(data[0], data[1], data[2]);
    }
    @Override
    public String toString() {
        String str = String.format("(%1.3f,%1.3f,%1.3f)", data[0], data[1], data[2]);
        return str;
    }
//extern double	fdot(const FVECT v1, const FVECT v2);
//extern double	dist2(const FVECT v1, const FVECT v2);
//extern double	dist2line(const FVECT p, const FVECT ep1, const FVECT ep2);
//extern double	dist2lseg(const FVECT p, const FVECT ep1, const FVECT ep2);
//extern void	fcross(FVECT vres, const FVECT v1, const FVECT v2);
//extern void	fvsum(FVECT vres, const FVECT v0, const FVECT v1, double f);
//extern double	normalize(FVECT v);
//extern int	closestapproach(RREAL t[2],
//			const FVECT rorg0, const FVECT rdir0,
//			const FVECT rorg1, const FVECT rdir1);
//extern void	spinvector(FVECT vres, const FVECT vorig,
//			const FVECT vnorm, double theta);
    /*
     *  fvect.c - routines for floating-point vector calculations
     */

    public static double fdot(FVECT v1, FVECT v2) {	/* return the dot product of two vectors */
        return (DOT(v1, v2));
    }

    public static double dist2(FVECT p1, FVECT p2) {				/* return square of distance between points */
        FVECT delta = new FVECT();

        delta.data[0] = p2.data[0] - p1.data[0];
        delta.data[1] = p2.data[1] - p1.data[1];
        delta.data[2] = p2.data[2] - p1.data[2];

        return (DOT(delta, delta));
    }

    public static double dist2line( /* return square of distance to line */
            FVECT p, /* the point */
            FVECT ep1,
            FVECT ep2 /* points on the line */) {
        double d, d1, d2;

        d = dist2(ep1, ep2);
        d1 = dist2(ep1, p);
        d2 = d + d1 - dist2(ep2, p);

        return (d1 - 0.25 * d2 * d2 / d);
    }

    public static double dist2lseg( /* return square of distance to line segment */
            FVECT p, /* the point */
            FVECT ep1,
            FVECT ep2 /* the end points */) {
        double d, d1, d2;

        d = dist2(ep1, ep2);
        d1 = dist2(ep1, p);
        d2 = dist2(ep2, p);

        if (d2 > d1) {			/* check if past endpoints */
            if (d2 - d1 > d) {
                return (d1);
            }
        } else {
            if (d1 - d2 > d) {
                return (d2);
            }
        }
        d2 = d + d1 - d2;

        return (d1 - 0.25 * d2 * d2 / d);	/* distance to line */
    }

    public static void fcross( /* vres = v1 X v2 */
            FVECT vres,
            FVECT v1,
            FVECT v2) {
        vres.data[0] = v1.data[1] * v2.data[2] - v1.data[2] * v2.data[1];
        vres.data[1] = v1.data[2] * v2.data[0] - v1.data[0] * v2.data[2];
        vres.data[2] = v1.data[0] * v2.data[1] - v1.data[1] * v2.data[0];
    }

    public static void fvsum( /* vres = v0 + f*v1 */
            FVECT vres,
            FVECT v0,
            FVECT v1,
            double f) {
        vres.data[0] = v0.data[0] + f * v1.data[0];
        vres.data[1] = v0.data[1] + f * v1.data[1];
        vres.data[2] = v0.data[2] + f * v1.data[2];
    }

    public static double normalize( /* normalize a vector, return old magnitude */
            FVECT v) {
        double len, d;

        d = DOT(v, v);

        if (d == 0.0) {
            return (0.0);
        }

        if (d <= 1.0 + FTINY && d >= 1.0 - FTINY) {
            len = 0.5 + 0.5 * d;	/* first order approximation */
            d = 2.0 - len;
        } else {
            len = Math.sqrt(d);
            d = 1.0 / len;
        }
        v.data[0] *= d;
        v.data[1] *= d;
        v.data[2] *= d;

        return (len);
    }

    int closestapproach( /* closest approach of two rays */
            double[] t, /* returned distances along each ray */
            FVECT rorg0, /* first origin */
            FVECT rdir0, /* first direction (normalized) */
            FVECT rorg1, /* second origin */
            FVECT rdir1 /* second direction (normalized) */) {
        double dotprod = DOT(rdir0, rdir1);
        double denom = 1. - dotprod * dotprod;
        double o1o2_d1;
        FVECT o0o1 = new FVECT();

        if (denom <= FTINY) {		/* check if lines are parallel */
            t[0] = t[1] = 0.0;
            return (0);
        }
        VSUB(o0o1, rorg0, rorg1);
        o1o2_d1 = DOT(o0o1, rdir1);
        t[0] = (o1o2_d1 * dotprod - DOT(o0o1, rdir0)) / denom;
        t[1] = o1o2_d1 + t[0] * dotprod;
        return (1);
    }

    void spinvector( /* rotate vector around normal */
            FVECT vres, /* returned vector */
            FVECT vorig, /* original vector */
            FVECT vnorm, /* normalized vector for rotation */
            double theta /* right-hand radians */) {
        double sint, cost, normprod;
        FVECT vperp = new FVECT();
        int i;

        if (theta == 0.0) {
            if (vres != vorig) {
                VCOPY(vres, vorig);
            }
            return;
        }
        cost = Math.cos(theta);
        sint = Math.sin(theta);
        normprod = DOT(vorig, vnorm) * (1. - cost);
        fcross(vperp, vnorm, vorig);
        for (i = 0; i < 3; i++) {
            vres.data[i] = vorig.data[i] * cost + vnorm.data[i] * normprod + vperp.data[i] * sint;
        }
    }
}
