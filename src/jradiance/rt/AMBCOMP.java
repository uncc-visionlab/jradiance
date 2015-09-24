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
package jradiance.rt;

import java.util.Arrays;
import jradiance.common.COLORS.COLOR;
import jradiance.common.FVECT;
import jradiance.common.MULTISAMP;
import jradiance.common.RTMATH;
import jradiance.common.TCOS;
import jradiance.common.URAND;
import jradiance.rt.AMBIENT.AMBHEMI;
import jradiance.rt.AMBIENT.AMBSAMP;

/**
 *
 * @author arwillis
 */
public class AMBCOMP {

    /*
     * Routines to compute "ambient" values using Monte Carlo
     *
     *  Declarations of external symbols in ambient.h
     */
    static void inithemi( /* initialize sampling hemisphere */
            AMBHEMI hp,
            COLOR ac,
            RAY r,
            double wt) {
        double d;
        int i;
        /* set number of divisions */
        if (RAYCALLS.ambacc <= FVECT.FTINY
                && wt > (d = 0.8 * COLOR.intens(ac) * r.rweight / (RAYCALLS.ambdiv * RAYCALLS.minweight))) {
            wt = d;			/* avoid ray termination */
        }
        hp.nt = (int) (Math.sqrt(RAYCALLS.ambdiv * wt / Math.PI) + 0.5);
        i = RAYCALLS.ambacc > FVECT.FTINY ? 3 : 1;	/* minimum number of samples */
        if (hp.nt < i) {
            hp.nt = i;
        }
        hp.np = (int) (Math.PI * hp.nt + 0.5);
        /* set number of super-samples */
        hp.ns = (int) (RAYCALLS.ambssamp * wt + 0.5);
        /* assign coefficient */
        COLOR.copycolor(hp.acoef, ac);
        d = 1.0 / (hp.nt * hp.np);
        hp.acoef.scalecolor(d);
        /* make axes */
        FVECT.VCOPY(hp.uz, r.ron);
        hp.uy.data[0] = hp.uy.data[1] = hp.uy.data[2] = 0.0;
        for (i = 0; i < 3; i++) {
            if (hp.uz.data[i] < 0.6 && hp.uz.data[i] > -0.6) {
                break;
            }
        }
        if (i >= 3) {
//		error(CONSISTENCY, "bad ray direction in inithemi");
        }
        hp.uy.data[i] = 1.0;
        FVECT.fcross(hp.ux, hp.uy, hp.uz);
        FVECT.normalize(hp.ux);
        FVECT.fcross(hp.uy, hp.uz, hp.ux);
    }

    static int divsample( /* sample a division */
            AMBSAMP dp,
            AMBHEMI h,
            RAY r) {
        RAY ar = new RAY();
        int[] hlist = new int[3];
        double[] spt = new double[2];
        double xd, yd, zd;
        double b2;
        double phi;
        int i;
        /* ambient coefficient for weight */
        if (RAYCALLS.ambacc > FVECT.FTINY) {
            ar.rcoef.setcolor(AMBIENT.AVGREFL, AMBIENT.AVGREFL, AMBIENT.AVGREFL);
        } else {
            COLOR.copycolor(ar.rcoef, h.acoef);
        }
        if (RAYTRACE.rayorigin(ar, RAY.AMBIENT, r, ar.rcoef) < 0) {
            return (-1);
        }
        if (RAYCALLS.ambacc > FVECT.FTINY) {
            COLOR.multcolor(ar.rcoef, h.acoef);
            ar.rcoef.scalecolor(1. / AMBIENT.AVGREFL);
        }
        hlist[0] = (int) r.rno;
        hlist[1] = dp.t;
        hlist[2] = dp.p;
        MULTISAMP.multisamp(spt, 2, URAND.urand(URAND.ilhash(hlist, 3) + dp.n));
        zd = Math.sqrt((dp.t + spt[0]) / h.nt);
        phi = 2.0 * Math.PI * (dp.p + spt[1]) / h.np;
        xd = TCOS.tcos(phi) * zd;
        yd = RTMATH.tsin(phi) * zd;
        zd = Math.sqrt(1.0 - zd * zd);
        for (i = 0; i < 3; i++) {
            ar.rdir.data[i] = xd * h.ux.data[i]
                    + yd * h.uy.data[i]
                    + zd * h.uz.data[i];
        }
//	checknorm(ar.rdir);
        RAYCALLS.dimlist[RAYCALLS.ndims++] = dp.t * h.np + dp.p + 90171;
        RAY.rayvalue(ar);
        RAYCALLS.ndims--;
        COLOR.multcolor(ar.rcol, ar.rcoef);	/* apply coefficient */
        COLOR.addcolor(dp.v, ar.rcol);
        /* use rt to improve gradient calc */
        if (ar.rt > FVECT.FTINY && ar.rt < FVECT.FHUGE) {
            dp.r += 1.0 / ar.rt;
        }
        /* (re)initialize error */
        if (dp.n++ != 0) {
            b2 = COLOR.bright(dp.v) / dp.n - COLOR.bright(ar.rcol);
            b2 = b2 * b2 + dp.k * ((dp.n - 1) * (dp.n - 1));
            dp.k = (float) b2 / (dp.n * dp.n);
        } else {
            dp.k = 0.0f;
        }
        return (0);
    }

    static int ambcmp( /* decreasing order */
            Object p1,
            Object p2) {
        AMBSAMP d1 = (AMBSAMP) p1;
        AMBSAMP d2 = (AMBSAMP) p2;

        if (d1.k < d2.k) {
            return (1);
        }
        if (d1.k > d2.k) {
            return (-1);
        }
        return (0);
    }

//
//static int
//ambnorm(				/* standard order */
//	const void *p1,
//	const void *p2
//)
//{
//	const AMBSAMP	*d1 = (const AMBSAMP *)p1;
//	const AMBSAMP	*d2 = (const AMBSAMP *)p2;
//	register int	c;
//
//	if ( (c = d1->t - d2->t) )
//		return(c);
//	return(d1->p - d2->p);
//}
//
//
    public static double doambient( /* compute ambient component */
            COLOR acol,
            RAY r,
            double wt,
            FVECT pg,
            FVECT dg) {
        double b, d = 0;
        AMBHEMI hemi = new AMBHEMI();
        AMBSAMP[] div;
        AMBSAMP dnew = new AMBSAMP();
        AMBSAMP[] dp;
        int dpidx = 0;
        double arad;
        int divcnt;
        int i, j;
        /* initialize hemisphere */
        inithemi(hemi, acol, r, wt);
        divcnt = hemi.nt * hemi.np;
        /* initialize */
        if (pg != null) {
            pg.data[0] = pg.data[1] = pg.data[2] = 0.0;
        }
        if (dg != null) {
            dg.data[0] = dg.data[1] = dg.data[2] = 0.0;
        }
        acol.setcolor(0.0f, 0.0f, 0.0f);
        if (divcnt == 0) {
            return (0.0);
        }
        /* allocate super-samples */
        if (hemi.ns > 0 || pg != null || dg != null) {
            div = new AMBSAMP[divcnt];
            if (div == null) {
//			error(SYSTEM, "out of memory in doambient");
            }
        } else {
            div = null;
        }				/* sample the divisions */
        arad = 0.0;
        if ((dp = div) == null) {
            dp[dpidx] = dnew;
        }
        divcnt = 0;
        for (i = 0; i < hemi.nt; i++) {
            for (j = 0; j < hemi.np; j++) {
                dp[dpidx].t = (short) i;
                dp[dpidx].p = (short) j;
                dp[dpidx].v.setcolor(0.0f, 0.0f, 0.0f);
                dp[dpidx].r = 0.0f;
                dp[dpidx].n = 0;
                if (divsample(dp[dpidx], hemi, r) < 0) {
                    if (div != null) {
                        dpidx++;
                    }
                    continue;
                }
                arad += dp[dpidx].r;
                divcnt++;
                if (div != null) {
                    dpidx++;
                } else {
                    COLOR.addcolor(acol, dp[dpidx].v);
                }
            }
        }
        if (divcnt == 0) {
            if (div != null) {
//			free((void *)div);
            }
            return (0.0);		/* no samples taken */
        }
        if (divcnt < hemi.nt * hemi.np) {
            pg = dg = null;		/* incomplete sampling */
            hemi.ns = 0;
        } else if (arad > FVECT.FTINY && divcnt / arad < AMBIENT.minarad) {
            hemi.ns = 0;		/* close enough */
        } else if (hemi.ns > 0) {	/* else perform super-sampling? */
            comperrs(div, hemi);			/* compute errors */
            Arrays.sort(div); // assumes div has length divcnt
            //qsort(div, divcnt, sizeof(AMBSAMP), ambcmp);	/* sort divs */
						/* super-sample */
            for (i = hemi.ns; i > 0; i--) {
                dnew = div[0];
                if (divsample(dnew, hemi, r) < 0) {
                    dpidx++;
                    continue;
                }
                dp = div;		/* reinsert */
                dpidx = 0;
                j = divcnt < i ? divcnt : i;
                while (--j > 0 && dnew.k < dp[dpidx + 1].k) {
//				*dp = *(dp+1);
                    dp[dpidx] = dp[dpidx + 1];
                    dpidx++;
//				dp++;
                }
//			*dp = dnew;
                dp[dpidx] = dnew;
            }
            if (pg != null || dg != null) {	/* restore order */
                //qsort(div, divcnt, sizeof(AMBSAMP), ambnorm);
                Arrays.sort(div); // assumes div has length divcnt
            }
        }
        /* compute returned values */
        if (div != null) {
            arad = 0.0;		/* note: divcnt may be < nt*np */
            for (i = hemi.nt * hemi.np, dp = div, dpidx = 0; i-- > 0; dpidx++) {
                arad += dp[dpidx].r;
                if (dp[dpidx].n > 1) {
                    b = 1.0 / dp[dpidx].n;
                    dp[dpidx].v.scalecolor(b);
                    dp[dpidx].r *= b;
                    dp[dpidx].n = 1;
                }
                COLOR.addcolor(acol, dp[dpidx].v);
            }
            b = COLOR.bright(acol);
            if (b > FVECT.FTINY) {
                b = 1.0 / b;	/* compute & normalize gradient(s) */
                if (pg != null) {
                    posgradient(pg, div, hemi);
                    for (i = 0; i < 3; i++) {
                        pg.data[i] *= b;
                    }
                }
                if (dg != null) {
                    dirgradient(dg, div, hemi);
                    for (i = 0; i < 3; i++) {
                        dg.data[i] *= b;
                    }
                }
            }
//		free((void *)div);
        }
        if (arad <= FVECT.FTINY) {
            arad = AMBIENT.maxarad;
        } else {
            arad = (divcnt + hemi.ns) / arad;
        }
        if (pg != null) {		/* reduce radius if gradient large */
            d = FVECT.DOT(pg, pg);
            if (d * arad * arad > 1.0) {
                arad = 1.0 / Math.sqrt(d);
            }
        }
        if (arad < AMBIENT.minarad) {
            arad = AMBIENT.minarad;
            if (pg != null && d * arad * arad > 1.0) {	/* cap gradient */
                d = 1.0 / arad / Math.sqrt(d);
                for (i = 0; i < 3; i++) {
                    pg.data[i] *= d;
                }
            }
        }
        if ((arad /= Math.sqrt(wt)) > AMBIENT.maxarad) {
            arad = AMBIENT.maxarad;
        }
        return (arad);
    }

    static void comperrs( /* compute initial error estimates */
            AMBSAMP[] da, /* assumes standard ordering */
            AMBHEMI hp) {
        double b, b2;
        int i, j;
        AMBSAMP[] dp;
        /* sum differences from neighbors */
        dp = da;
        int dpidx = 0;
        for (i = 0; i < hp.nt; i++) {
            for (j = 0; j < hp.np; j++) {
//#ifdef  DEBUG
//			if (dp->t != i || dp->p != j)
//				error(CONSISTENCY,
//					"division order in comperrs");
//#endif
                // POINTER MAGIC ALL OVER THE PLACE HERE
                b = COLOR.bright(dp[dpidx + 0].v);
                if (i > 0) {		/* from above */
                    b2 = COLOR.bright(dp[dpidx - hp.np].v) - b;
                    b2 *= b2 * 0.25;
                    dp[dpidx + 0].k += b2;
                    dp[dpidx - hp.np].k += b2;
                }
                if (j > 0) {		/* from behind */
                    b2 = COLOR.bright(dp[dpidx - 1].v) - b;
                    b2 *= b2 * 0.25;
                    dp[dpidx + 0].k += b2;
                    dp[dpidx - 1].k += b2;
                } else {		/* around */
                    b2 = COLOR.bright(dp[dpidx + hp.np - 1].v) - b;
                    b2 *= b2 * 0.25;
                    dp[dpidx + 0].k += b2;
                    dp[dpidx + hp.np - 1].k += b2;
                }
                dpidx++;
            }
        }
        /* divide by number of neighbors */
        dp = da;
        dpidx = 0;
        for (j = 0; j < hp.np; j++) /* top row */ {
            (dp[dpidx++]).k *= 1.0 / 3.0;
        }
        if (hp.nt < 2) {
            return;
        }
        for (i = 1; i < hp.nt - 1; i++) /* central region */ {
            for (j = 0; j < hp.np; j++) {
                (dp[dpidx++]).k *= 0.25;
            }
        }
        for (j = 0; j < hp.np; j++) /* bottom row */ {
            (dp[dpidx++]).k *= 1.0 / 3.0;
        }
    }

    static void posgradient( /* compute position gradient */
            FVECT gv,
            AMBSAMP[] da, /* assumes standard ordering */
            AMBHEMI hp) {
        int i, j;
        double nextsine, lastsine, b, d;
        double mag0, mag1;
        double phi, cosp, sinp, xd, yd;
        AMBSAMP[] dp;
        int dpidx = 0;
        xd = yd = 0.0;
        for (j = 0; j < hp.np; j++) {
            dp = da;
            dpidx = j;
            mag0 = mag1 = 0.0;
            lastsine = 0.0;
            for (i = 0; i < hp.nt; i++) {
//#ifdef  DEBUG
//			if (dp->t != i || dp->p != j)
//				error(CONSISTENCY,
//					"division order in posgradient");
//#endif
                b = COLOR.bright(dp[dpidx].v);
                if (i > 0) {
                    d = dp[dpidx - hp.np].r;
                    if (dp[dpidx + 0].r > d) {
                        d = dp[dpidx + 0].r;
                    }
                    /* sin(t)*cos(t)^2 */
                    d *= lastsine * (1.0 - (double) i / hp.nt);
                    mag0 += d * (b - COLOR.bright(dp[dpidx - hp.np].v));
                }
                nextsine = Math.sqrt((double) (i + 1) / hp.nt);
                if (j > 0) {
                    d = dp[dpidx - 1].r;
                    if (dp[dpidx + 0].r > d) {
                        d = dp[dpidx + 0].r;
                    }
                    mag1 += d * (nextsine - lastsine)
                            * (b - COLOR.bright(dp[dpidx - 1].v));
                } else {
                    d = dp[dpidx + hp.np - 1].r;
                    if (dp[dpidx + 0].r > d) {
                        d = dp[dpidx + 0].r;
                    }
                    mag1 += d * (nextsine - lastsine)
                            * (b - COLOR.bright(dp[dpidx + hp.np - 1].v));
                }
                //dp += hp.np;
                dpidx += hp.np;
                lastsine = nextsine;
            }
            mag0 *= 2.0 * Math.PI / hp.np;
            phi = 2.0 * Math.PI * (double) j / hp.np;
            cosp = TCOS.tcos(phi);
            sinp = RTMATH.tsin(phi);
            xd += mag0 * cosp - mag1 * sinp;
            yd += mag0 * sinp + mag1 * cosp;
        }
        for (i = 0; i < 3; i++) {
            gv.data[i] = (xd * hp.ux.data[i] + yd * hp.uy.data[i]) * (hp.nt * hp.np) / Math.PI;
        }
    }

    static void dirgradient( /* compute direction gradient */
            FVECT gv,
            AMBSAMP[] da, /* assumes standard ordering */
            AMBHEMI hp) {
        int i, j;
        double mag;
        double phi, xd, yd;
        AMBSAMP[] dp;
        int dpidx = 0;

        xd = yd = 0.0;
        for (j = 0; j < hp.np; j++) {
            dp = da;
            dpidx = j;
            mag = 0.0;
            for (i = 0; i < hp.nt; i++) {
//#ifdef  DEBUG
//			if (dp->t != i || dp->p != j)
//				error(CONSISTENCY,
//					"division order in dirgradient");
//#endif
							/* tan(t) */
                mag += COLOR.bright(dp[dpidx].v) / Math.sqrt(hp.nt / (i + .5) - 1.0);
                //dp += hp->np;
                dpidx += hp.np;
            }
            phi = 2.0 * Math.PI * (j + .5) / hp.np + Math.PI / 2.0;
            xd += mag * TCOS.tcos(phi);
            yd += mag * RTMATH.tsin(phi);
        }
        for (i = 0; i < 3; i++) {
            gv.data[i] = xd * hp.ux.data[i] + yd * hp.uy.data[i];
        }
    }
}
