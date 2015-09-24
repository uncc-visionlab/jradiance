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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Comparator;
import jradiance.common.COLORS;
import jradiance.common.COLORS.COLOR;
import jradiance.common.FVECT;
import jradiance.common.HEADER;
import jradiance.common.OBJECT;
import jradiance.common.OBJSET;
import jradiance.common.OTYPES;
import jradiance.common.PORTIO;
import jradiance.util.VERSION;

/**
 *
 * @author arwillis
 */
public class AMBIENT {
    /* RCSid $Id$ */
    /*
     * Common definitions for interreflection routines.
     *
     * Include after ray.h
     */
//#ifndef _RAD_AMBIENT_H_
//#define _RAD_AMBIENT_H_
//#ifdef __cplusplus
//extern "C" {
//#endif
// from stdio.h
    public static final int BUFSIZ = 8192;
    /*
     * Since we've defined our vectors as float below to save space,
     * watch out for changes in the definitions of VCOPY() and DOT()
     * and don't pass these vectors to fvect routines.
     */

    public static class AMBVAL {

        AMBVAL next;	/* next in list */

        long latick;	/* last accessed tick */

        float[] pos = new float[3];		/* position in space */

        float[] dir = new float[3];		/* normal direction */

        int lvl;		/* recursion level of parent ray */

        float weight;		/* weight of parent ray */

        float rad;		/* validity radius */

        COLOR val = new COLOR();		/* computed ambient value */

        float[] gpos = new float[3];		/* gradient wrt. position */

        float[] gdir = new float[3];		/* gradient wrt. direction */

    }			/* ambient value */


    public static class AMBTREE {

        AMBVAL alist;		/* ambient value list */

        AMBTREE[] kid;	/* 8 child nodes */

    }		/* ambient octree */


    public static class AMBSAMP implements Comparator {

        COLOR v = new COLOR();		/* division sum (partial) */

        float r;		/* 1/distance sum */

        float k;		/* variance for this division */

        int n;		/* number of subsamples */

        short t, p;	/* theta, phi indices */
        public int compare( /* decreasing order */
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

        @Override
        public boolean equals(Object p1) {
            AMBSAMP d1 = (AMBSAMP) p1;
            return d1.k == this.k;
        }
    }		/* ambient sample division */


    public static class AMBHEMI {

        FVECT ux = new FVECT(), uy= new FVECT(), uz= new FVECT();	/* x, y and z axis directions */

        COLOR acoef = new COLOR();		/* division contribution coefficient */

        int ns;		/* number of super-samples */

        int nt, np;		/* number of theta and phi directions */

    }  		/* ambient sample hemisphere */

//extern double  maxarad;		/* maximum ambient radius */
//extern double  minarad;		/* minimum ambient radius */
//#ifndef AVGREFL
    public static final float AVGREFL = 0.5f;	/* assumed average reflectance */
//#endif

    public static final double AMBVALSIZ = 75;	/* number of bytes in portable AMBVAL struct */

    public static final double AMBMAGIC = 557;	/* magic number for ambient value files */

    public static final String AMBFMT = "Radiance_ambval";	/* format id string */

    /* defined in ambient.c */
//extern void	setambres(int ar);
//extern void	setambacc(double newa);
//extern void	setambient(void);
//extern void	multambient(COLORS aval, RAY *r, FVECT nrm);
//extern void	ambdone(void);
//extern void	ambnotify(OBJECT obj);
//extern double	sumambient(COLORS acol, RAY *r, FVECT rn, int al,
//				AMBTREE *at, FVECT c0, double s);
//extern double	makeambient(COLORS acol, RAY *r, FVECT rn, int al);
//extern void	extambient(COLORS cr, AMBVAL *ap, FVECT pv, FVECT nv);
//extern int	ambsync(void);
//					/* defined in AMBCOMP.c */
//extern double	doambient(COLORS acol, RAY *r, double wt,
//					FVECT pg, FVECT dg);
//extern void	inithemi(AMBHEMI *hp, COLORS ac, RAY *r, double wt);
//extern int	divsample(AMBSAMP *dp, AMBHEMI *h, RAY *r);
//extern void	comperrs(AMBSAMP *da, AMBHEMI *hp);
//extern void	posgradient(FVECT gv, AMBSAMP *da, AMBHEMI *hp);
//extern void	dirgradient(FVECT gv, AMBSAMP *da, AMBHEMI *hp);
//					/* defined in ambio.c */
//extern void	putambmagic(FILE *fp);
//extern int	hasambmagic(FILE *fp);
//extern int	writambval(AMBVAL *av, FILE *fp);
//extern int	ambvalOK(AMBVAL *av);
//extern int	readambval(AMBVAL *av, FILE *fp);
//					/* defined in lookamb.c */
//extern void	lookamb(FILE *fp);
//extern void	writamb(FILE *fp);
//#ifdef __cplusplus
//}
//#endif
//#endif /* _RAD_AMBIENT_H_ */
//
//#ifndef lint
//static const char	RCSid[] = "$Id$";
//#endif
/*
     *  ambient.c - routines dealing with ambient (inter-reflected) component.
     *
     *  Declarations of external symbols in ambient.h
     */
//#include "copyright.h"
//
//#include <string.h>
//
//#include  "platform.h"
//#include  "ray.h"
//#include  "otypes.h"
//#include  "resolu.h"
//#include  "ambient.h"
//#include  "random.h"
//
//#ifndef  OCTSCALE
    public static final double OCTSCALE = 1.0;	/* ceil((valid rad.)/(cube size)) */
//#endif

//extern char  *shm_boundary;	/* memory sharing boundary */
//#ifndef  MAXASET
    public static final int MAXASET = 4095;	/* maximum number of elements in ambient set */
//#endif
    static int[] ambset = new int[MAXASET + 1];	/* ambient include/exclude set */

    public static double maxarad;		/* maximum ambient radius */

    public static double minarad;		/* minimum ambient radius */

    static AMBTREE atrunk;		/* our ambient trunk node */

    static FileOutputStream ambfp = null;	/* ambient file pointer */
//static RandomAccessFile  ambfp = null;	/* ambient file pointer */
    static int nunflshed = 0;	/* number of unflushed ambient values */

//#ifndef SORT_THRESH
//#ifdef SMLMEM
//#define SORT_THRESH	((3L<<20)/sizeof(AMBVAL))
//#else
//public static final int SORT_THRESH=	((9L<<20)/sizeof(AMBVAL));
    public static final long SORT_THRESH = ((9L << 20) / 64); //64~=sizeof(AMBVAL)
//#endif
//#endif
//#ifndef SORT_INTVL
    public static final long SORT_INTVL = (SORT_THRESH << 1);
//#endif
//#ifndef MAX_SORT_INTVL
    public static final long MAX_SORT_INTVL = (SORT_INTVL << 6);
//#endif
    static double avsum = 0.;		/* computed ambient value sum (log) */

    static int navsum = 0;	/* number of values in avsum */

    static int nambvals = 0;	/* total number of indirect values */

    static int nambshare = 0;	/* number of values from file */

    static long ambclock = 0;	/* ambient access clock */

    static long lastsort = 0;	/* time of last value sort */

    static long sortintvl = SORT_INTVL;	/* time until next sort */

    static FileInputStream ambinp = null;		/* auxiliary file for input */
//static long  lastpos = -1;		/* last flush position */

    static long MAXACLOCK = (1L << 30);	/* clock turnover value */
    /*
     * Track access times unless we are sharing ambient values
     * through memory on a multiprocessor, when we want to avoid
     * claiming our own memory (copy on write).  Go ahead anyway
     * if more than two thirds of our values are unshared.
     * Compile with -Dtracktime=0 to turn this code off.
     */
//#ifndef tracktime
    static boolean tracktime() {
        return (RAYCALLS.shm_boundary == null || nambvals > 3 * nambshare);
    }
//#endif
    public static final int AMBFLUSH = (int) (BUFSIZ / AMBVALSIZ);
//
    static AMBVAL newambval() {
        return new AMBVAL();
    }
//#define	 newambval()	(AMBVAL *)malloc(sizeof(AMBVAL))
//#define  freeav(av)	free((void *)av);

//static void initambfile(int creat);
//static void avsave(AMBVAL *av);
//static AMBVAL *avstore(AMBVAL  *aval);
//static AMBTREE *newambtree(void);
//static void freeambtree(AMBTREE  *atp);
//
//typedef void unloadtf_t(void *);
//static unloadtf_t avinsert;
//static unloadtf_t av2list;
//static void unloadatree(AMBTREE  *at, unloadtf_t *f);
//
//static int aposcmp(const void *avp1, const void *avp2);
//static int avlmemi(AMBVAL *avaddr);
//static void sortambvals(int always);
//
//#ifdef  F_SETLKW
//static void aflock(int  typ);
//#endif
    public static void setambres( /* set ambient resolution */
            int ar) {
        RAYCALLS.ambres = ar < 0 ? 0 : ar;		/* may be done already */
        /* set min & max radii */
        if (ar <= 0) {
            minarad = 0;
            maxarad = RAYCALLS.thescene.cusize / 2.0;
        } else {
            minarad = RAYCALLS.thescene.cusize / ar;
            maxarad = 64 * minarad;			/* heuristic */
            if (maxarad > RAYCALLS.thescene.cusize / 2.0) {
                maxarad = RAYCALLS.thescene.cusize / 2.0;
            }
        }
        if (minarad <= FVECT.FTINY) {
            minarad = 10 * FVECT.FTINY;
        }
        if (maxarad <= minarad) {
            maxarad = 64 * minarad;
        }
    }

    public static void setambacc( /* set ambient accuracy */
            double newa) {
        double ambdiff;

        if (newa < 0.0) {
            newa = 0.0;
        }
        ambdiff = Math.abs(newa - RAYCALLS.ambacc);
        if (ambdiff >= .01 && (RAYCALLS.ambacc = newa) > FVECT.FTINY && nambvals > 0) {
            sortambvals(1);			/* rebuild tree */
        }
    }
//
//
    public static void setambient() throws IOException /* initialize calculation */ {
        int readonly = 0;
        long flen;
        AMBVAL amb = new AMBVAL();
        /* make sure we're fresh */
        ambdone();
        /* init ambient limits */
        setambres(RAYCALLS.ambres);
        setambacc(RAYCALLS.ambacc);
        if (RAYCALLS.ambfile == null) {
            return;
        }
        if (RAYCALLS.ambacc <= FVECT.FTINY) {
//		sprintf(errmsg, "zero ambient accuracy so \"%s\" not opened",
//				ambfile);
//		error(WARNING, errmsg);
            return;
        }
        /* open ambient file */
//        ambfp = new RandomAccessFile(RAYCALLS.ambfile,"rw");                
//	if ((ambfp = new RandomAccessFile(RAYCALLS.ambfile, "rw")) == null)
//		if ((ambfp = new RandomAccessFile(RAYCALLS.ambfile, "r")) != null)
//                    readonly = 1;
        FileInputStream ambfpin = new FileInputStream(RAYCALLS.ambfile);
        if (ambfp != null) {
            System.out.println("READING AMBIENT FILES NOT SUPPORTED");
            System.exit(1);
            initambfile(0);			/* file exists */
//		lastpos = ambfp.getFilePointer();
            while (readambval(amb, ambfpin) != 0) {
                avinsert(avstore(amb));
            }
            nambshare = nambvals;		/* share loaded values */
            if (readonly != 0) {
//			sprintf(errmsg,
//				"loaded %u values from read-only ambient file",
//					nambvals);
//			error(WARNING, errmsg);
                ambfp.close();		/* close file so no writes */
                ambfp = null;
                return;			/* avoid ambsync() */
            }
            /* align file pointer */
//		lastpos += (long)nambvals*AMBVALSIZ;
//                flen = ambfp.length();
//                ambfp.seek(ambfp.length());
//		if (flen != lastpos) {
//			sprintf(errmsg,
//			"ignoring last %ld values in ambient file (corrupted)",
//					(flen - lastpos)/AMBVALSIZ);
//			error(WARNING, errmsg);
//			ambfp.seek(lastpos);
//#ifndef _WIN32 /* XXX we need a replacement for that one */
//			ftruncate(fileno(ambfp), (off_t)lastpos);
//#endif
//		}
            //} else if ((ambfp =  new RandomAccessFile(RAYCALLS.ambfile, "w")) != null) {
        } else if ((ambfp = new FileOutputStream(RAYCALLS.ambfile)) != null) {
            initambfile(1);			/* else create new file */
            //lastpos = ambfp.getFilePointer();
//                lastpos = -1;
        } else {
//		sprintf(errmsg, "cannot open ambient file \"%s\"", ambfile);
//		error(SYSTEM, errmsg);
        }
//#ifdef  F_SETLKW
//	aflock(F_UNLCK);			/* release file */
//#endif
    }

    static void ambdone() throws IOException /* close ambient file and free memory */ {
        if (ambfp != null) {		/* close ambient file */
            ambsync();
            ambfp.close();
            ambfp = null;
            if (ambinp != null) {
                ambinp.close();
                ambinp = null;
            }
//		lastpos = -1;
        }
        /* free ambient tree */
//	unloadatree(atrunk, free);
					/* reset state variables */
        avsum = 0.;
        navsum = 0;
        nambvals = 0;
        nambshare = 0;
        ambclock = 0;
        lastsort = 0;
        sortintvl = SORT_INTVL;
    }

    public static void ambnotify( /* record new modifier */
            int obj) {
        int hitlimit = 0;
        OBJECT.OBJREC o;
        String[] amblp;

        if (obj == OBJECT.OVOID) {		/* starting over */
            AMBIENT.ambset[0] = 0;
            hitlimit = 0;
            return;
        }
        o = OBJECT.objptr(obj);
        if (hitlimit != 0 || OTYPES.ismodifier(o.otype) == 0) {
            return;
        }
        amblp = RAYCALLS.amblist;
        int amblpidx;
        for (amblpidx = 0; amblp[amblpidx] != null; amblpidx++) {
            if (o.oname.equals(amblp[amblpidx])) {
                if (ambset[0] >= MAXASET) {
//				error(WARNING, "too many modifiers in ambient list");
                    hitlimit++;
                    return;		/* should this be fatal? */
                }
                OBJSET.insertelem(ambset, obj);
                return;
            }
        }
    }
    static int rdepth = 0;			/* ambient recursion */


    public static void multambient( /* compute ambient component & multiply by coef. */
            COLOR aval,
            RAY r,
            FVECT nrm) throws IOException {
        //static int  rdepth = 0;			/* ambient recursion */
        COLOR acol = new COLOR();
        double d, l;

        if (RAYCALLS.ambdiv <= 0) {			/* no ambient calculation */
            dumbamb(aval, r, nrm);
            return;
        }
        /* check number of bounces */
        if (rdepth >= RAYCALLS.ambounce) {
            dumbamb(aval, r, nrm);
            return;
        }
        /* check ambient list */
        if (RAYCALLS.ambincl != -1 && r.ro != null
                && RAYCALLS.ambincl != OBJSET.inset(ambset, r.ro.omod)) {
            dumbamb(aval, r, nrm);
            return;
        }

        if (RAYCALLS.ambacc <= FVECT.FTINY) {			/* no ambient storage */
            COLOR.copycolor(acol, aval);
            rdepth++;
            d = AMBCOMP.doambient(acol, r, r.rweight, null, null);
            rdepth--;
            if (d <= FVECT.FTINY) {
                dumbamb(aval, r, nrm);
                return;
            }
            COLOR.copycolor(aval, acol);
            return;
        }

        if (AMBIENT.tracktime()) /* sort to minimize thrashing */ {
            sortambvals(0);
        }
        /* interpolate ambient value */
        acol.setcolor(0.0f, 0.0f, 0.0f);
        d = sumambient(acol, r, nrm, rdepth,
                atrunk, RAYCALLS.thescene.cuorg, RAYCALLS.thescene.cusize);
        if (d > FVECT.FTINY) {
            d = 1.0 / d;
            acol.scalecolor(d);
            COLOR.multcolor(aval, acol);
            return;
        }
        rdepth++;				/* need to cache new value */
        d = makeambient(acol, r, nrm, rdepth - 1);
        rdepth--;
        if (d > FVECT.FTINY) {
            COLOR.multcolor(aval, acol);		/* got new value */
            return;
        }
        dumbamb(aval, r, nrm);

//dumbamb:					/* return global value */
//	if ((ambvwt <= 0) | (navsum == 0)) {
//		multcolor(aval, ambval);
//		return;
//	}
//	l = bright(ambval);			/* average in computations */
//	if (l > FTINY) {
//		d = (log(l)*(double)ambvwt + avsum) /
//				(double)(ambvwt + navsum);
//		d = exp(d) / l;
//		scalecolor(aval, d);
//		multcolor(aval, ambval);	/* apply color of ambval */
//	} else {
//		d = exp( avsum / (double)navsum );
//		scalecolor(aval, d);		/* neutral color */
//	}
    }

    public static void dumbamb(COLOR aval,
            RAY r,
            FVECT nrm) {
        double d, l;
//dumbamb:					/* return global value */
        if ((RAYCALLS.ambvwt <= 0) | (navsum == 0)) {
            COLOR.multcolor(aval, RAYCALLS.ambval);
            return;
        }
        l = COLOR.bright(RAYCALLS.ambval);			/* average in computations */
        if (l > FVECT.FTINY) {
            d = (Math.log(l) * (double) RAYCALLS.ambvwt + avsum)
                    / (double) (RAYCALLS.ambvwt + navsum);
            d = Math.exp(d) / l;
            aval.scalecolor(d);
            COLOR.multcolor(aval, RAYCALLS.ambval);	/* apply color of ambval */
        } else {
            d = Math.exp(avsum / (double) navsum);
            aval.scalecolor(d);		/* neutral color */
        }

    }

    public static double sumambient( /* get interpolated ambient value */
            COLOR acol,
            RAY r,
            FVECT rn,
            int al,
            AMBTREE at,
            FVECT c0,
            double s) {
        double d, e1, e2, wt, wsum;
        COLOR ct = new COLOR();
        FVECT ck0 = new FVECT();
        int i;
        int j;
        AMBVAL av;

        wsum = 0.0;
        /* do this node */
//	for (av = at->alist; av != NULL; av = av->next) {
//		double	rn_dot = -2.0;
//		if (tracktime)
//			av->latick = ambclock;
//		/*
//		 *  Ambient level test.
//		 */
//		if (av->lvl > al)	/* list sorted, so this works */
//			break;
//		if (av->weight < 0.9*r->rweight)
//			continue;
//		/*
//		 *  Ambient radius test.
//		 */
//		VSUB(ck0, av->pos, r->rop);
//		e1 = DOT(ck0, ck0) / (av->rad * av->rad);
//		if (e1 > ambacc*ambacc*1.21)
//			continue;
//		/*
//		 *  Direction test using closest normal.
//		 */
//		d = DOT(av->dir, r->ron);
//		if (rn != r->ron) {
//			rn_dot = DOT(av->dir, rn);
//			if (rn_dot > 1.0-FTINY)
//				rn_dot = 1.0-FTINY;
//			if (rn_dot >= d-FTINY) {
//				d = rn_dot;
//				rn_dot = -2.0;
//			}
//		}
//		e2 = (1.0 - d) * r->rweight;
//		if (e2 < 0.0)
//			e2 = 0.0;
//		else if (e1 + e2 > ambacc*ambacc*1.21)
//			continue;
//		/*
//		 *  Ray behind test.
//		 */
//		d = 0.0;
//		for (j = 0; j < 3; j++)
//			d += (r->rop[j] - av->pos[j]) *
//					(av->dir[j] + r->ron[j]);
//		if (d*0.5 < -minarad*ambacc-.001)
//			continue;
//		/*
//		 *  Jittering final test reduces image artifacts.
//		 */
//		e1 = sqrt(e1);
//		e2 = sqrt(e2);
//		wt = e1 + e2;
//		if (wt > ambacc*(.9+.2*urand(9015+samplendx)))
//			continue;
//		/*
//		 *  Recompute directional error using perturbed normal
//		 */
//		if (rn_dot > 0.0) {
//			e2 = sqrt((1.0 - rn_dot)*r->rweight);
//			wt = e1 + e2;
//		}
//		if (wt <= 1e-3)
//			wt = 1e3;
//		else
//			wt = 1.0 / wt;
//		wsum += wt;
//		extambient(ct, av, r->rop, rn);
//		scalecolor(ct, wt);
//		addcolor(acol, ct);
//	}
//	if (at->kid == NULL)
//		return(wsum);
//					/* do children */
//	s *= 0.5;
//	for (i = 0; i < 8; i++) {
//		for (j = 0; j < 3; j++) {
//			ck0[j] = c0[j];
//			if (1<<j & i)
//				ck0[j] += s;
//			if (r->rop[j] < ck0[j] - OCTSCALE*s)
//				break;
//			if (r->rop[j] > ck0[j] + (1.0+OCTSCALE)*s)
//				break;
//		}
//		if (j == 3)
//			wsum += sumambient(acol, r, rn, al,
//						at->kid+i, ck0, s);
//	}
        return (wsum);
    }

    public static double makeambient( /* make a new ambient value for storage */
            COLOR acol,
            RAY r,
            FVECT rn,
            int al) throws IOException {
        AMBVAL amb = new AMBVAL();
        FVECT gp = new FVECT(), gd = new FVECT();
        int i;

        amb.weight = 1.0f;			/* compute weight */
        for (i = al; i-- > 0;) {
            amb.weight *= AVGREFL;
        }
        if (r.rweight < 0.1 * amb.weight) /* heuristic override */ {
            amb.weight = 1.25f * r.rweight;
        }
        acol.setcolor((float) AVGREFL, (float) AVGREFL, (float) AVGREFL);
        /* compute ambient */
        amb.rad = (float) AMBCOMP.doambient(acol, r, amb.weight, gp, gd);
        if (amb.rad <= FVECT.FTINY) {
            acol.setcolor(0.0f, 0.0f, 0.0f);
            return (0.0);
        }
        acol.scalecolor(1. / AVGREFL);		/* undo assumed reflectance */
        /* store value */
        FVECT.VCOPY(new FVECT(amb.pos[0], amb.pos[1], amb.pos[2]), r.rop);
        FVECT.VCOPY(new FVECT(amb.dir[0], amb.dir[1], amb.dir[2]), r.ron);
        amb.lvl = al;
        COLOR.copycolor(amb.val, acol);
        FVECT.VCOPY(new FVECT(amb.gpos[0], amb.gpos[1], amb.gpos[2]), gp);
        FVECT.VCOPY(new FVECT(amb.gdir[0], amb.gdir[1], amb.gdir[2]), gd);
        /* insert into tree */
        avsave(amb);				/* and save to file */
        if (rn != r.ron) {
            extambient(acol, amb, r.rop, rn);	/* texture */
        }
        return (amb.rad);
    }

    static void extambient( /* extrapolate value at pv, nv */
            COLOR cr,
            AMBVAL ap,
            FVECT pv,
            FVECT nv) {
        FVECT v1 = new FVECT();
        int i;
        double d;

        d = 1.0;			/* zeroeth order */
        /* gradient due to translation */
        for (i = 0; i < 3; i++) {
            d += ap.gpos[i] * (pv.data[i] - ap.pos[i]);
        }
        /* gradient due to rotation */
        FVECT.VCROSS(v1, new FVECT(ap.dir[0], ap.dir[1], ap.dir[2]), nv);
        d += FVECT.DOT(new FVECT(ap.gdir[0], ap.gdir[1], ap.gdir[2]), v1);
        if (d <= 0.0) {
            cr.setcolor(0.0f, 0.0f, 0.0f);
            return;
        }
        COLOR.copycolor(cr, ap.val);
        cr.scalecolor(d);
    }

//static char[] mybuf = null;
    static void initambfile( /* initialize ambient file */
            int cre8
            ) throws IOException {
//	extern char  *progname, *octname;
//	static char  *mybuf = NULL;

//#ifdef	F_SETLKW
//	aflock(cre8 ? F_WRLCK : F_RDLCK);
//#endif
//	SET_FILE_BINARY(ambfp);
//	if (mybuf == null)
//		mybuf = new char[BUFSIZ+8];
//	setbuf(ambfp, mybuf);

        if (cre8 != 0) {			/* new file */
            ByteArrayOutputStream bstream = new ByteArrayOutputStream();
            HEADER.newheader("RADIANCE", bstream);
            ambfp.write(bstream.toByteArray());
            bstream.reset();
            ambfp.write(String.format("%s -av %g %g %g -aw %d -ab %d -aa %g ",
                    RVMAIN.progname, RAYCALLS.ambval.colval(COLOR.RED),
                    RAYCALLS.ambval.colval(COLOR.GRN), RAYCALLS.ambval.colval(COLOR.BLU),
                    RAYCALLS.ambvwt, RAYCALLS.ambounce, RAYCALLS.ambacc).getBytes());
            ambfp.write(String.format("-ad %d -as %d -ar %d ",
                    RAYCALLS.ambdiv, RAYCALLS.ambssamp, RAYCALLS.ambres).getBytes());
            if (RAYCALLS.octname != null) {
                ambfp.write(RAYCALLS.octname.getBytes());
            }
            ambfp.write('\n');
            ambfp.write(String.format("SOFTWARE= %s\n", VERSION.VersionID).getBytes());
            HEADER.fputnow(bstream);
            HEADER.fputformat(AMBFMT, bstream);
            ambfp.write(bstream.toByteArray());
            bstream.reset();
            ambfp.write('\n');
            putambmagic(bstream);
            ambfp.write(bstream.toByteArray());
            bstream.reset();
        }
//        else if (checkheader(ambfp, AMBFMT, null) < 0 || hasambmagic(ambfp)==0) {
//		error(USER, "bad ambient file");
//        }
    }

    static void avsave( /* insert and save an ambient value */
            AMBVAL av) throws IOException {
        avinsert(avstore(av));
        if (ambfp == null) {
            return;
        }
        if (writambval(av, ambfp) < 0) {
//		goto writerr;
            System.exit(0);
        }
        if (++nunflshed >= AMBIENT.AMBFLUSH) {
//		if (ambsync() == EOF) {
//			goto writerr;
            System.exit(0);
//                }
        }
        return;
//writerr:
//	error(SYSTEM, "error writing to ambient file");
    }

    static AMBVAL avstore( /* allocate memory and store aval */
            AMBVAL aval) {
        AMBVAL av;
        double d;

        if ((av = newambval()) == null) {
//		error(SYSTEM, "out of memory in avstore");
        }
        av = aval;
        av.latick = ambclock;
        av.next = null;
        nambvals++;
        d = COLOR.bright(av.val);
        if (d > FVECT.FTINY) {		/* add to log sum for averaging */
            avsum += Math.log(d);
            navsum++;
        }
        return (av);
    }
//
    public static final int ATALLOCSZ = 512;		/* #/8 trees to allocate at once */
//
//static AMBTREE  *atfreelist = NULL;	/* free ambient tree structures */
    static AMBTREE[] atfreelist = null;	/* free ambient tree structures */


    static AMBTREE[] newambtree() /* allocate 8 ambient tree structs */ {
        AMBTREE atp;
        AMBTREE[] upperlim;

        if (atfreelist == null) {	/* get more nodes */
            atfreelist = new AMBTREE[ATALLOCSZ * 8];
            if (atfreelist == null) {
                return (null);
            }
            /* link new free list */
            //upperlim = atfreelist[8*(ATALLOCSZ-1)];
            int atfreelistidx;
            for (atfreelistidx = 0; atfreelistidx < 8 * (ATALLOCSZ - 1); atfreelistidx += 8) {
                atp = atfreelist[atfreelistidx];
                atp.kid[0] = atfreelist[atfreelistidx + 8];
            }
            atp = atfreelist[atfreelistidx];
            atp.kid = null;
        }
        atp = atfreelist[0];
        upperlim = atfreelist;
        atfreelist = atp.kid;
        // CANNOT BE DONE IN JAVA
        //memset((char *)atp, '\0', 8*sizeof(AMBTREE));
//	return(atp);
        return upperlim;
    }

    static void freeambtree( /* free 8 ambient tree structs */
            AMBTREE atp) {
//	atp.kid = AMBIENT.atfreelist;
//	atfreelist = atp;
    }

    static void avinsert( /* insert ambient value in our tree */
            //	void *av
            Object av // AMBVAL?
            ) {
        AMBTREE at;
        AMBVAL ap;
        AMBVAL avh = new AMBVAL();
        FVECT ck0 = new FVECT();
        double s;
        int branch;
        int i;

        if (((AMBVAL) av).rad <= FVECT.FTINY) {
//		error(CONSISTENCY, "zero ambient radius in avinsert");
        }
        at = atrunk;
        FVECT.VCOPY(ck0, RAYCALLS.thescene.cuorg);
        s = RAYCALLS.thescene.cusize;
        while (s * (OCTSCALE / 2) > ((AMBVAL) av).rad * RAYCALLS.ambacc) {
            if (at.kid == null) {
                if ((at.kid = newambtree()) == null) {
//				error(SYSTEM, "out of memory in avinsert");
                }
            }
            s *= 0.5;
            branch = 0;
            for (i = 0; i < 3; i++) {
                if (((AMBVAL) av).pos[i] > ck0.data[i] + s) {
                    ck0.data[i] += s;
                    branch |= 1 << i;
                }
            }
            at = at.kid[branch];
        }
        avh.next = at.alist;		/* order by increasing level */
        for (ap = avh; ap.next != null; ap = ap.next) {
            if (ap.next.lvl >= ((AMBVAL) av).lvl) {
                break;
            }
        }
        ((AMBVAL) av).next = ap.next;
        ap.next = (AMBVAL) av;
        at.alist = avh.next;
    }

    static void unloadatree( /* unload an ambient value tree */
            AMBTREE at//,
            //unloadtf_t *f
            ) {
        AMBVAL av;
        int i;
        /* transfer values at this node */
        for (av = at.alist; av != null; av = at.alist) {
            at.alist = av.next;
            av = null; // free(av);
//		(*f)(av);
        }
        if (at.kid == null) {
            return;
        }
        for (i = 0; i < 8; i++) {		/* transfer and free children */
            unloadatree(at.kid[i]);
        }
//	freeambtree(at.kid);
        at.kid = null;
    }

    class avl {

        AMBVAL p;
        long t;
    }
    static avl[] avlist1;			/* ambient value list with ticks */

    static AMBVAL[][] avlist2;		/* memory positions for sorting */

    static int i_avlist;		/* index for lists */
//
//static int alatcmp(const void *av1, const void *av2);
//
//static void
//av2list(
//	void *av
//)
//{
//#ifdef DEBUG
//	if (i_avlist >= nambvals)
//		error(CONSISTENCY, "too many ambient values in av2list1");
//#endif
//	avlist1[i_avlist].p = avlist2[i_avlist] = (AMBVAL*)av;
//	avlist1[i_avlist++].t = ((AMBVAL*)av)->latick;
//}
//
//
//static int
//alatcmp(			/* compare ambient values for MRA */
//	const void *av1,
//	const void *av2
//)
//{
//	long  lc = ((struct avl *)av2)->t - ((struct avl *)av1)->t;
//	return(lc<0 ? -1 : lc>0 ? 1 : 0);
//}
//
//
///* GW NOTE 2002/10/3:
// * I used to compare AMBVAL pointers, but found that this was the
// * cause of a serious consistency error with gcc, since the optimizer
// * uses some dangerous trick in pointer subtraction that
// * assumes pointers differ by exact struct size increments.
// */
//static int
//aposcmp(			/* compare ambient value positions */
//	const void	*avp1,
//	const void	*avp2
//)
//{
//	long	diff = *(char * const *)avp1 - *(char * const *)avp2;
//	if (diff < 0)
//		return(-1);
//	return(diff > 0);
//}
//
//#if 1
//static int
//avlmemi(				/* find list position from address */
//	AMBVAL	*avaddr
//)
//{
//	AMBVAL  **avlpp;
//
//	avlpp = (AMBVAL **)bsearch((char *)&avaddr, (char *)avlist2,
//			nambvals, sizeof(AMBVAL *), aposcmp);
//	if (avlpp == NULL)
//		error(CONSISTENCY, "address not found in avlmemi");
//	return(avlpp - avlist2);
//}
//#else
//#define avlmemi(avaddr)	((AMBVAL **)bsearch((char *)&avaddr,(char *)avlist2, \
//				nambvals,sizeof(AMBVAL *),aposcmp) - avlist2)
//#endif
//
//
    static void sortambvals( /* resort ambient values */
            int always) {
        AMBTREE oldatrunk;
        AMBVAL tav, pnext;
        AMBVAL[] tap;
        int i, j;
        /* see if it's time yet */
        if (always == 0 && (ambclock++ < lastsort + sortintvl
                || nambvals < SORT_THRESH)) {
            return;
        }
        /*
         * The idea here is to minimize memory thrashing
         * in VM systems by improving reference locality.
         * We do this by periodically sorting our stored ambient
         * values in memory in order of most recently to least
         * recently accessed.  This ordering was chosen so that new
         * ambient values (which tend to be less important) go into
         * higher memory with the infrequently accessed values.
         *	Since we expect our values to need sorting less
         * frequently as the process continues, we double our
         * waiting interval after each call.
         * 	This routine is also called by setambacc() with
         * the "always" parameter set to 1 so that the ambient
         * tree will be rebuilt with the new accuracy parameter.
         */
        if (tracktime()) {		/* allocate pointer arrays to sort */
            avlist2 = new AMBVAL[nambvals][];//(AMBVAL **)malloc(nambvals*sizeof(AMBVAL *));
            avlist1 = new avl[nambvals];//malloc(nambvals*sizeof(struct avl));
        } else {
            avlist2 = null;
            avlist1 = null;
        }
        if (avlist1 == null) {		/* no time tracking -- rebuild tree? */
            if (avlist2 != null) {
//			free((void *)avlist2);
            }
            if (always != 0) {		/* rebuild without sorting */
                oldatrunk = atrunk;
                atrunk.alist = null;
                atrunk.kid = null;
//			unloadatree(&oldatrunk, avinsert);
            }
        } else {			/* sort memory by last access time */
            /*
             * Sorting memory is tricky because it isn't contiguous.
             * We have to sort an array of pointers by MRA and also
             * by memory position.  We then copy values in "loops"
             * to minimize memory hits.  Nevertheless, we will visit
             * everyone at least twice, and this is an expensive process
             * when we're thrashing, which is when we need to do it.
             */
//#ifdef DEBUG
//		sprintf(errmsg, "sorting %u ambient values at ambclock=%lu...",
//				nambvals, ambclock);
//		eputs(errmsg);
//#endif
            i_avlist = 0;
//		unloadatree(&atrunk, av2list);	/* empty current tree */
//#ifdef DEBUG
//		if (i_avlist < nambvals)
//			error(CONSISTENCY, "missing ambient values in sortambvals");
//#endif
//		qsort((char *)avlist1, nambvals, sizeof(struct avl), alatcmp);
//		qsort((char *)avlist2, nambvals, sizeof(AMBVAL *), aposcmp);
            for (i = 0; i < nambvals; i++) {
                if (avlist1[i].p == null) {
                    continue;
                }
                tap = avlist2[i];
                tav = tap[0];
//			for (j = i; (pnext = avlist1[j].p) != tap[0];
//					j = avlmemi(pnext)) {
//				*(avlist2[j]) = *pnext;
//				avinsert(avlist2[j]);
//				avlist1[j].p = NULL;
//			}
//			*(avlist2[j]) = tav;
//			avinsert(avlist2[j]);
//			avlist1[j].p = NULL;
            }
//		free((void *)avlist1);
//		free((void *)avlist2);
						/* compute new sort interval */
            sortintvl = ambclock - lastsort;
            if (sortintvl >= MAX_SORT_INTVL / 2) {
                sortintvl = MAX_SORT_INTVL;
            } else {
                sortintvl <<= 1;	/* wait twice as long next */
            }
//#ifdef DEBUG
//		eputs("done\n");
//#endif
        }
        if (ambclock >= MAXACLOCK) {
            ambclock = MAXACLOCK / 2;
        }
        lastsort = ambclock;
    }

//#ifdef	F_SETLKW
    static void aflock( /* lock/unlock ambient file */
            int typ) {
//	static struct flock  fls;	/* static so initialized to zeroes */
//
//	if (typ == fls.l_type)		/* already called? */
//		return;
//	fls.l_type = typ;
//	if (fcntl(fileno(ambfp), F_SETLKW, &fls) < 0)
//		error(SYSTEM, "cannot (un)lock ambient file");
    }

    static int ambsync() /* synchronize ambient file */ {
        long flen;
        AMBVAL avs;
        int n = 0;

        if (ambfp == null) /* no ambient file? */ {
            return (0);
        }
        /* gain appropriate access */
//	aflock(nunflshed ? F_WRLCK : F_RDLCK);
				/* see if file has grown */
//	if ((flen = lseek(fileno(ambfp), (off_t)0, SEEK_END)) < 0)
//		goto seekerr;
//	if ((n = flen - lastpos) > 0) {		/* file has grown */
//		if (ambinp == NULL) {		/* use duplicate filedes */
//			ambinp = fdopen(dup(fileno(ambfp)), "r");
//			if (ambinp == NULL)
//				error(SYSTEM, "fdopen failed in ambsync");
//		}
//		if (fseek(ambinp, lastpos, SEEK_SET) < 0)
//			goto seekerr;
//		while (n >= AMBVALSIZ) {	/* load contributed values */
//			if (!readambval(&avs, ambinp)) {
//				sprintf(errmsg,
//			"ambient file \"%s\" corrupted near character %ld",
//						ambfile, flen - n);
//				error(WARNING, errmsg);
//				break;
//			}
//			avinsert(avstore(&avs));
//			n -= AMBVALSIZ;
//		}
//		lastpos = flen - n;
//		/*** seek always as safety measure
//		if (n) ***/			/* alignment */
//			if (lseek(fileno(ambfp), (off_t)lastpos, SEEK_SET) < 0)
//				goto seekerr;
//	}
//	n = ambfp.flush();			/* calls write() at last */
//	if (n != EOF)
//		lastpos += (long)nunflshed*AMBVALSIZ;
//	else if ((lastpos = lseek(fileno(ambfp), (off_t)0, SEEK_CUR)) < 0)
//		goto seekerr;
//		
//	aflock(F_UNLCK);			/* release file */
        nunflshed = 0;
        return (n);
//seekerr:
//	error(SYSTEM, "seek failed in ambsync");
//	return -1; /* pro forma return */
    }

//#else
//
//extern int
//ambsync(void)			/* flush ambient file */
//{
//	if (ambfp == NULL)
//		return(0);
//	nunflshed = 0;
//	return(fflush(ambfp));
//}
//
//#endif
//#ifndef lint
//static const char	RCSid[] = "$Id$";
//#endif
/*
     * Read and write portable ambient values
     *
     *  Declarations of external symbols in ambient.h
     */
//#include "copyright.h"
//
//#include "ray.h"
//
//#include "ambient.h"
//
//
//#define  putvec(v,fp)	putflt((v)[0],fp);putflt((v)[1],fp);putflt((v)[2],fp)
    public static void putvec(float[] v, OutputStream fp) throws IOException {
        PORTIO.putflt((v)[0], fp);
        PORTIO.putflt((v)[1], fp);
        PORTIO.putflt((v)[2], fp);
    }
//
//#define  getvec(v,fp)	(v)[0]=getflt(fp);(v)[1]=getflt(fp);(v)[2]=getflt(fp)
    public static void getvec(float[] v, InputStream fp) throws IOException {
        (v)[0] = (float) PORTIO.getflt(fp);
        (v)[1] = (float) PORTIO.getflt(fp);
        (v)[2] = (float) PORTIO.getflt(fp);
    }
//
//#define  badflt(x)	((x) < -FHUGE || (x) > FHUGE)
    public static boolean badflt(float x) {
        return ((x) < -FVECT.FHUGE || (x) > FVECT.FHUGE);
    }
//
//#define  badvec(v)	(badflt((v)[0]) || badflt((v)[1]) || badflt((v)[2]))
    public static boolean badvec(float[] v) {
        return (badflt((v)[0]) || badflt((v)[1]) || badflt((v)[2]));
    }
//

    static void putambmagic( /* write out ambient value magic number */
            OutputStream fp) throws IOException {
        PORTIO.putint((long) AMBMAGIC, 2, fp);
    }

    boolean hasambmagic( /* read in and check validity of magic # */
            InputStream fp) throws IOException {
        int magic;

        magic = (int) PORTIO.getint(2, fp);
//	if (feof(fp))
//		return(0);
        return (magic == AMBMAGIC);
    }

    public static int writambval( /* write ambient value to stream */
            AMBVAL av,
            OutputStream fp) throws IOException {
        COLORS.COLR col;

        PORTIO.putint((long) av.lvl, 1, fp);
        PORTIO.putflt(av.weight, fp);
        putvec(av.pos, fp);
        putvec(av.dir, fp);
//	setcolr(col, colval(av->val,RED),
//			colval(av->val,GRN), colval(av->val,BLU));
//	fp.write((char *)col, sizeof(col), 1, fp);
        PORTIO.putflt(av.rad, fp);
        putvec(av.gpos, fp);
        putvec(av.gdir, fp);
//	return(ferror(fp) ? -1 : 0);
        return 0;
    }

    static int ambvalOK( /* check consistency of ambient value */
            AMBVAL av) throws IOException {
        double d;

        if (badvec(av.pos)) {
            return (0);
        }
        if (badvec(av.dir)) {
            return (0);
        }
        d = FVECT.DOT(new FVECT(av.dir[0], av.dir[1], av.dir[2]),
                new FVECT(av.dir[0], av.dir[1], av.dir[2]));
        if (d < 0.9999 || d > 1.0001) {
            return (0);
        }
        if (av.lvl < 0 || av.lvl > 100) {
            return (0);
        }
        if (av.weight <= 0. || av.weight > 1.) {
            return (0);
        }
        if (av.rad <= 0. || av.rad >= FVECT.FHUGE) {
            return (0);
        }
        if (av.val.colval(COLOR.RED) < 0.
                || av.val.colval(COLOR.RED) > FVECT.FHUGE
                || av.val.colval(COLOR.GRN) < 0.
                || av.val.colval(COLOR.GRN) > FVECT.FHUGE
                || av.val.colval(COLOR.BLU) < 0.
                || av.val.colval(COLOR.BLU) > FVECT.FHUGE) {
            return (0);
        }
        if (badvec(av.gpos)) {
            return (0);
        }
        if (badvec(av.gdir)) {
            return (0);
        }
        return (1);
    }

    public static int readambval( /* read ambient value from stream */
            AMBVAL av,
            InputStream fp) throws IOException {
        byte[] col = new byte[4];

        av.lvl = (int) PORTIO.getint(1, fp);
//	if (feof(fp))
        if (fp.available() == 0) {
            return (0);
        }
        av.weight = (float) PORTIO.getflt(fp);
        getvec(av.pos, fp);
        getvec(av.dir, fp);
        fp.read(col, 0, col.length);
        COLORS.colr_color(av.val, col);
        av.rad = (float) PORTIO.getflt(fp);
        getvec(av.gpos, fp);
        getvec(av.gdir, fp);
//	return(feof(fp) ? 0 : ambvalOK(av));
        return (fp.available() == 0 ? 0 : ambvalOK(av));
    }
}
