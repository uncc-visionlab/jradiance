/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.rt;

import jradiance.common.COLORS.COLOR;
import jradiance.common.FVECT;
import jradiance.common.MULTISAMP;
import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OBJECT_STRUCTURE;
import jradiance.common.OTYPES;
import jradiance.common.RTMATH;
import jradiance.common.TCOS;
import jradiance.common.URAND;

/**
 *
 * @author arwillis
 */
public class NORMAL extends OBJECT_STRUCTURE {

    /*
     *  normal.c - shading function for normal materials.
     *
     *     8/19/85
     *     12/19/85 - added stuff for metals.
     *     6/26/87 - improved specular model.
     *     9/28/87 - added model for translucent materials.
     *     Later changes described in delta comments.
     */
    public static final int MAXITER = 10;		/* maximum # specular ray attempts */
    /* estimate of Fresnel function */


    static double FRESNE(double ci) {
        return (Math.exp(-5.85 * (ci)) - 0.00287989916);
    }
    public static final double FRESTHRESH = 0.017999;	/* minimum specularity for approx. */


    /*
     *	This routine implements the isotropic Gaussian
     *  model described by Ward in Siggraph `92 article.
     *	We orient the surface towards the incoming ray, so a single
     *  surface can be used to represent an infinitely thin object.
     *
     *  Arguments for MAT_PLASTIC and MAT_METAL are:
     *	red	grn	blu	specular-frac.	facet-slope
     *
     *  Arguments for MAT_TRANS are:
     *	red 	grn	blu	rspec	rough	trans	tspec
     */

    /* specularity flags */
    public static final int SP_REFL = 01;		/* has reflected specular component */

    public static final int SP_TRAN = 02;		/* has transmitted specular */

    public static final int SP_PURE = 04;		/* purely specular (zero roughness) */

    public static final int SP_FLAT = 010;		/* flat reflecting surface */

    public static final int SP_RBLT = 020;		/* reflection below sample threshold */

    public static final int SP_TBLT = 040;		/* transmission below threshold */


    public static class NORMDAT {

        OBJREC mp;		/* material pointer */

        RAY rp;		/* ray pointer */

        short specfl;		/* specularity flags, defined above */

        COLOR mcolor = new COLOR();		/* color of this material */

        COLOR scolor = new COLOR();		/* color of specular component */

        FVECT vrefl = new FVECT();		/* vector in direction of reflected ray */

        FVECT prdir = new FVECT();		/* vector in transmitted direction */

        double alpha2;		/* roughness squared */

        double rdiff, rspec;	/* reflected specular, diffuse */

        double trans;		/* transmissivity */

        double tdiff, tspec;	/* transmitted specular, diffuse */

        FVECT pnorm = new FVECT();		/* perturbed surface normal */

        double pdot;		/* perturbed dot product */

    }		/* normal material data */


    @Override
    public int octree_function(Object... obj) {
        return m_normal((OBJREC) obj[0], (RAY) obj[1]);
    }

    int m_normal( /* color a ray that hit something normal */
            OBJREC m,
            RAY r) {
        NORMDAT nd = new NORMDAT();
        double fest;
        double transtest, transdist;
        double mirtest, mirdist;
        int hastexture;
        double d;
        COLOR ctmp = new COLOR();
        int i;
        /* easy shadow test */
        if ((r.crtype & RAY.SHADOW) != 0 && m.otype != OTYPES.MAT_TRANS) {
            return (1);
        }

        if (m.oargs.nfargs != (m.otype == OTYPES.MAT_TRANS ? 7 : 5)) {
//		objerror(m, USER, "bad number of arguments");
        }
        /* check for back side */
        if (r.rod < 0.0) {
            if (RAYCALLS.backvis == 0 && m.otype != OTYPES.MAT_TRANS) {
                RAYTRACE.raytrans(r);
                return (1);
            }
            RAYTRACE.raytexture(r, m.omod);
            RAYTRACE.flipsurface(r);			/* reorient if backvis */
        } else {
            RAYTRACE.raytexture(r, m.omod);
        }
        nd.mp = m;
        nd.rp = r;
        /* get material color */
        nd.mcolor.setcolor((float) m.oargs.farg[0],
                (float) m.oargs.farg[1],
                (float) m.oargs.farg[2]);
        /* get roughness */
        nd.specfl = 0;
        nd.alpha2 = m.oargs.farg[4];
        if ((nd.alpha2 *= nd.alpha2) <= FVECT.FTINY) {
            nd.specfl |= SP_PURE;
        }

        if ((hastexture = (FVECT.DOT(r.pert, r.pert) > FVECT.FTINY * FVECT.FTINY) ? 1 : 0) != 0) {
            nd.pdot = RAYTRACE.raynormal(nd.pnorm, r);	/* perturb normal */
        } else {
            FVECT.VCOPY(nd.pnorm, r.ron);
            nd.pdot = r.rod;
        }
        if (r.ro != null && OTYPES.isflat(r.ro.otype) != 0) {
            nd.specfl |= SP_FLAT;
        }
        if (nd.pdot < .001) {
            nd.pdot = .001;			/* non-zero for dirnorm() */
        }
        COLOR.multcolor(nd.mcolor, r.pcol);		/* modify material color */
        mirtest = transtest = 0;
        mirdist = transdist = r.rot;
        nd.rspec = m.oargs.farg[3];
        /* compute Fresnel approx. */
        if ((nd.specfl & SP_PURE) != 0 && nd.rspec >= FRESTHRESH) {
            fest = FRESNE(r.rod);
            nd.rspec += fest * (1. - nd.rspec);
        } else {
            fest = 0.;
        }
        /* compute transmission */
        if (m.otype == OTYPES.MAT_TRANS) {
            nd.trans = m.oargs.farg[5] * (1.0 - nd.rspec);
            nd.tspec = nd.trans * m.oargs.farg[6];
            nd.tdiff = nd.trans - nd.tspec;
            if (nd.tspec > FVECT.FTINY) {
                nd.specfl |= SP_TRAN;
                /* check threshold */
                if ((nd.specfl & SP_PURE) == 0
                        && RAYCALLS.specthresh >= nd.tspec - FVECT.FTINY) {
                    nd.specfl |= SP_TBLT;
                }
                if (hastexture == 0 || (r.crtype & RAY.SHADOW) != 0) {
                    FVECT.VCOPY(nd.prdir, r.rdir);
                    transtest = 2;
                } else {
                    for (i = 0; i < 3; i++) /* perturb */ {
                        nd.prdir.data[i] = r.rdir.data[i] - r.pert.data[i];
                    }
                    if (FVECT.DOT(nd.prdir, r.ron) < -FVECT.FTINY) {
                        FVECT.normalize(nd.prdir);	/* OK */
                    } else {
                        FVECT.VCOPY(nd.prdir, r.rdir);
                    }
                }
            }
        } else {
            nd.tdiff = nd.tspec = nd.trans = 0.0;
        }
        /* transmitted ray */
        if ((nd.specfl & (SP_TRAN | SP_PURE | SP_TBLT)) == (SP_TRAN | SP_PURE)) {
            RAY lr = new RAY();
            COLOR.copycolor(lr.rcoef, nd.mcolor);	/* modified by color */
            lr.rcoef.scalecolor(nd.tspec);
            if (RAYTRACE.rayorigin(lr, RAY.TRANS, r, lr.rcoef) == 0) {
                FVECT.VCOPY(lr.rdir, nd.prdir);
                RAY.rayvalue(lr);
                COLOR.multcolor(lr.rcol, lr.rcoef);
                COLOR.addcolor(r.rcol, lr.rcol);
                transtest *= COLOR.bright(lr.rcol);
                transdist = r.rot + lr.rt;
            }
        } else {
            transtest = 0;
        }

        if ((r.crtype & RAY.SHADOW) != 0) {		/* the rest is shadow */
            r.rt = transdist;
            return (1);
        }
        /* get specular reflection */
        if (nd.rspec > FVECT.FTINY) {
            nd.specfl |= SP_REFL;
            /* compute specular color */
            if (m.otype != OTYPES.MAT_METAL) {
                nd.scolor.setcolor((float) nd.rspec, (float) nd.rspec, (float) nd.rspec);
            } else if (fest > FVECT.FTINY) {
                d = nd.rspec * (1. - fest);
                for (i = 0; i < 3; i++) {
                    nd.scolor.data[i] = (float) (fest + nd.mcolor.data[i] * d);
                }
            } else {
                COLOR.copycolor(nd.scolor, nd.mcolor);
                nd.scolor.scalecolor(nd.rspec);
            }
            /* check threshold */
            if ((nd.specfl & SP_PURE) == 0 && RAYCALLS.specthresh >= nd.rspec - FVECT.FTINY) {
                nd.specfl |= SP_RBLT;
            }
            /* compute reflected ray */
            FVECT.VSUM(nd.vrefl, r.rdir, nd.pnorm, 2. * nd.pdot);
            /* penetration? */
            if (hastexture != 0 && FVECT.DOT(nd.vrefl, r.ron) <= FVECT.FTINY) {
                FVECT.VSUM(nd.vrefl, r.rdir, r.ron, 2. * r.rod);
            }
//            checknorm(nd.vrefl);
        }
        /* reflected ray */
        if ((nd.specfl & (SP_REFL | SP_PURE | SP_RBLT)) == (SP_REFL | SP_PURE)) {
            RAY lr = new RAY();
            if (RAYTRACE.rayorigin(lr, RAY.REFLECTED, r, nd.scolor) == 0) {
                FVECT.VCOPY(lr.rdir, nd.vrefl);
                RAY.rayvalue(lr);
                COLOR.multcolor(lr.rcol, lr.rcoef);
                COLOR.addcolor(r.rcol, lr.rcol);
                if (hastexture == 0 && (nd.specfl & SP_FLAT) != 0) {
                    mirtest = 2. * COLOR.bright(lr.rcol);
                    mirdist = r.rot + lr.rt;
                }
            }
        }
        /* diffuse reflection */
        nd.rdiff = 1.0 - nd.trans - nd.rspec;

        if ((nd.specfl & SP_PURE) != 0 && nd.rdiff <= FVECT.FTINY && nd.tdiff <= FVECT.FTINY) {
            return (1);			/* 100% pure specular */
        }

        if ((nd.specfl & SP_PURE) == 0) {
            gaussamp(r, nd);		/* checks *BLT flags */
        }
        try {
            if (nd.rdiff > FVECT.FTINY) {		/* ambient from this side */
                COLOR.copycolor(ctmp, nd.mcolor);	/* modified by material color */
                ctmp.scalecolor(nd.rdiff);
                if ((nd.specfl & SP_RBLT) != 0) /* add in specular as well? */ {
                    COLOR.addcolor(ctmp, nd.scolor);
                }
                AMBIENT.multambient(ctmp, r, hastexture != 0 ? nd.pnorm : r.ron);
                COLOR.addcolor(r.rcol, ctmp);	/* add to returned color */
            }
            if (nd.tdiff > FVECT.FTINY) {		/* ambient from other side */
                COLOR.copycolor(ctmp, nd.mcolor);	/* modified by color */
                if ((nd.specfl & SP_TBLT) != 0) {
                    ctmp.scalecolor(nd.trans);
                } else {
                    ctmp.scalecolor(nd.tdiff);
                }
                RAYTRACE.flipsurface(r);
                if (hastexture != 0) {
                    FVECT bnorm = new FVECT();
                    bnorm.data[0] = -nd.pnorm.data[0];
                    bnorm.data[1] = -nd.pnorm.data[1];
                    bnorm.data[2] = -nd.pnorm.data[2];
                    AMBIENT.multambient(ctmp, r, bnorm);
                } else {
                    AMBIENT.multambient(ctmp, r, r.ron);
                }
                COLOR.addcolor(r.rcol, ctmp);
                RAYTRACE.flipsurface(r);
            }
        } catch (Exception e) {
        }
        /* add direct component */
        SOURCE.direct(r, new dirnorm(), nd);
        /* check distance */
        d = COLOR.bright(r.rcol);
        if (transtest > d) {
            r.rt = transdist;
        } else if (mirtest > d) {
            r.rt = mirdist;
        }

        return (1);
    }

    public static class dirnorm implements SOURCE.srcdirf_t {

        @Override
        public void dirf( /* compute source contribution */
                COLOR cval, /* returned coefficient */
                Object nnp, /* material data */
                FVECT ldir, /* light source direction */
                double omega /* light source size */) {
            dirnorm(cval, nnp, ldir, omega);
        }

        void dirnorm( /* compute source contribution */
                COLOR cval, /* returned coefficient */
                Object nnp, /* material data */
                FVECT ldir, /* light source direction */
                double omega /* light source size */) {
            NORMDAT np = (NORMDAT) nnp;
            double ldot;
            double lrdiff, ltdiff;
            double dtmp, d2, d3, d4;
            FVECT vtmp = new FVECT();
            COLOR ctmp = new COLOR();

            cval.setcolor(0.0f, 0.0f, 0.0f);

            ldot = FVECT.DOT(np.pnorm, ldir);

            if (ldot < 0.0 ? np.trans <= FVECT.FTINY : np.trans >= 1.0 - FVECT.FTINY) {
                return;		/* wrong side */
            }

            /* Fresnel estimate */
            lrdiff = np.rdiff;
            ltdiff = np.tdiff;
            if ((np.specfl & SP_PURE) != 0 && np.rspec >= FRESTHRESH
                    && (lrdiff > FVECT.FTINY) | (ltdiff > FVECT.FTINY)) {
                dtmp = 1. - FRESNE(Math.abs(ldot));
                lrdiff *= dtmp;
                ltdiff *= dtmp;
            }

            if (ldot > FVECT.FTINY && lrdiff > FVECT.FTINY) {
                /*
                 *  Compute and add diffuse reflected component to returned
                 *  color.  The diffuse reflected component will always be
                 *  modified by the color of the material.
                 */
                COLOR.copycolor(ctmp, np.mcolor);
                dtmp = ldot * omega * lrdiff * (1.0 / Math.PI);
                ctmp.scalecolor(dtmp);
                COLOR.addcolor(cval, ctmp);
            }
            if (ldot > FVECT.FTINY && (np.specfl & (SP_REFL | SP_PURE)) == SP_REFL) {
                /*
                 *  Compute specular reflection coefficient using
                 *  Gaussian distribution model.
                 */
                /* roughness */
                dtmp = np.alpha2;
                /* + source if flat */
                if ((np.specfl & SP_FLAT) != 0) {
                    dtmp += omega * (0.25 / Math.PI);
                }
                /* half vector */
                vtmp.data[0] = ldir.data[0] - np.rp.rdir.data[0];
                vtmp.data[1] = ldir.data[1] - np.rp.rdir.data[1];
                vtmp.data[2] = ldir.data[2] - np.rp.rdir.data[2];
                d2 = FVECT.DOT(vtmp, np.pnorm);
                d2 *= d2;
                d3 = FVECT.DOT(vtmp, vtmp);
                d4 = (d3 - d2) / d2;
                /* new W-G-M-D model */
                dtmp = Math.exp(-d4 / dtmp) * d3 / (Math.PI * d2 * d2 * dtmp);
                /* worth using? */
                if (dtmp > FVECT.FTINY) {
                    COLOR.copycolor(ctmp, np.scolor);
                    dtmp *= ldot * omega;
                    ctmp.scalecolor(dtmp);
                    COLOR.addcolor(cval, ctmp);
                }
            }
            if (ldot < -FVECT.FTINY && ltdiff > FVECT.FTINY) {
                /*
                 *  Compute diffuse transmission.
                 */
                COLOR.copycolor(ctmp, np.mcolor);
                dtmp = -ldot * omega * ltdiff * (1.0 / Math.PI);
                ctmp.scalecolor(dtmp);
                COLOR.addcolor(cval, ctmp);
            }
            if (ldot < -FVECT.FTINY && (np.specfl & (SP_TRAN | SP_PURE)) == SP_TRAN) {
                /*
                 *  Compute specular transmission.  Specular transmission
                 *  is always modified by material color.
                 */
                /* roughness + source */
                dtmp = np.alpha2 + omega * (1.0 / Math.PI);
                /* Gaussian */
                dtmp = Math.exp((2. * FVECT.DOT(np.prdir, ldir) - 2.) / dtmp) / (Math.PI * dtmp);
                /* worth using? */
                if (dtmp > FVECT.FTINY) {
                    COLOR.copycolor(ctmp, np.mcolor);
                    dtmp *= np.tspec * omega * Math.sqrt(-ldot / np.pdot);
                    ctmp.scalecolor(dtmp);
                    COLOR.addcolor(cval, ctmp);
                }
            }
        }
    }

    static void gaussamp( /* sample Gaussian specular */
            RAY r,
            NORMDAT np) {
        RAY sr = new RAY();
        FVECT u = new FVECT(), v = new FVECT(), h = new FVECT();
        double[] rv = new double[2];
        double d, sinp, cosp;
        COLOR scol = new COLOR();
        int maxiter, ntrials, nstarget, nstaken;
        int i;
        /* quick test */
        if ((np.specfl & (SP_REFL | SP_RBLT)) != SP_REFL
                && (np.specfl & (SP_TRAN | SP_TBLT)) != SP_TRAN) {
            return;
        }
        /* set up sample coordinates */
        v.data[0] = v.data[1] = v.data[2] = 0.0;
        for (i = 0; i < 3; i++) {
            if (np.pnorm.data[i] < 0.6 && np.pnorm.data[i] > -0.6) {
                break;
            }
        }
        v.data[i] = 1.0;
        FVECT.fcross(u, v, np.pnorm);
        FVECT.normalize(u);
        FVECT.fcross(v, np.pnorm, u);
        /* compute reflection */
        if ((np.specfl & (SP_REFL | SP_RBLT)) == SP_REFL
                && RAYTRACE.rayorigin(sr, RAY.SPECULAR, r, np.scolor) == 0) {
            nstarget = 1;
            if (RAYCALLS.specjitter > 1.5) {	/* multiple samples? */
                nstarget = (int) (RAYCALLS.specjitter * r.rweight + .5);
                if (sr.rweight <= RAYCALLS.minweight * nstarget) {
                    nstarget = (int) (sr.rweight / RAYCALLS.minweight);
                }
                if (nstarget > 1) {
                    d = 1. / nstarget;
                    sr.rcoef.scalecolor(d);
                    sr.rweight *= d;
                } else {
                    nstarget = 1;
                }
            }
            scol.setcolor(0.f, 0.f, 0.f);
            // size_t cast on np.mp??? this is broken code
            RAYCALLS.dimlist[RAYCALLS.ndims++] = np.mp.hashCode();
            maxiter = MAXITER * nstarget;
            for (nstaken = ntrials = 0; nstaken < nstarget
                    && ntrials < maxiter; ntrials++) {
                if (ntrials != 0) {
                    d = RAYCALLS.frandom();
                } else {
                    d = URAND.urand(URAND.ilhash(RAYCALLS.dimlist, RAYCALLS.ndims) + RAYCALLS.samplendx);
                }
                MULTISAMP.multisamp(rv, 2, d);
                d = 2.0 * Math.PI * rv[0];
                cosp = TCOS.tcos(d);
                sinp = RTMATH.tsin(d);
                if ((0. <= RAYCALLS.specjitter) & (RAYCALLS.specjitter < 1.)) {
                    rv[1] = 1.0 - RAYCALLS.specjitter * rv[1];
                }
                if (rv[1] <= FVECT.FTINY) {
                    d = 1.0;
                } else {
                    d = Math.sqrt(np.alpha2 * -Math.log(rv[1]));
                }
                for (i = 0; i < 3; i++) {
                    h.data[i] = np.pnorm.data[i] + d * (cosp * u.data[i] + sinp * v.data[i]);
                }
                d = -2.0 * FVECT.DOT(h, r.rdir) / (1.0 + d * d);
                FVECT.VSUM(sr.rdir, r.rdir, h, d);
                /* sample rejection test */
                if ((d = FVECT.DOT(sr.rdir, r.ron)) <= FVECT.FTINY) {
                    continue;
                }
//			checknorm(sr.rdir);
                if (nstarget > 1) {	/* W-G-M-D adjustment */
                    if (nstaken != 0) {
                        RAYTRACE.rayclear(sr);
                    }
                    RAY.rayvalue(sr);
                    d = 2. / (1. + r.rod / d);
                    sr.rcol.scalecolor(d);
                    COLOR.addcolor(scol, sr.rcol);
                } else {
                    RAY.rayvalue(sr);
                    COLOR.multcolor(sr.rcol, sr.rcoef);
                    COLOR.addcolor(r.rcol, sr.rcol);
                }
                ++nstaken;
            }
            if (nstarget > 1) {		/* final W-G-M-D weighting */
                COLOR.multcolor(scol, sr.rcoef);
                d = (double) nstarget / ntrials;
                scol.scalecolor(d);
                COLOR.addcolor(r.rcol, scol);
            }
            RAYCALLS.ndims--;
        }
        /* compute transmission */
        COLOR.copycolor(sr.rcoef, np.mcolor);	/* modified by color */
        sr.rcoef.scalecolor(np.tspec);
        if ((np.specfl & (SP_TRAN | SP_TBLT)) == SP_TRAN
                && RAYTRACE.rayorigin(sr, RAY.SPECULAR, r, sr.rcoef) == 0) {
            nstarget = 1;
            if (RAYCALLS.specjitter > 1.5) {	/* multiple samples? */
                nstarget = (int) (RAYCALLS.specjitter * r.rweight + .5);
                if (sr.rweight <= RAYCALLS.minweight * nstarget) {
                    nstarget = (int) (sr.rweight / RAYCALLS.minweight);
                }
                if (nstarget > 1) {
                    d = 1. / nstarget;
                    sr.rcoef.scalecolor(d);
                    sr.rweight *= d;
                } else {
                    nstarget = 1;
                }
            }
            // cast size_t ?
//		RAYCALLS.dimlist[RAYCALLS.ndims++] = (int)(size_t)np.mp;
            RAYCALLS.dimlist[RAYCALLS.ndims++] = (int) np.mp.hashCode();
            maxiter = MAXITER * nstarget;
            for (nstaken = ntrials = 0; nstaken < nstarget
                    && ntrials < maxiter; ntrials++) {
                if (ntrials != 0) {
                    d = RAYCALLS.frandom();
                } else {
                    d = URAND.urand(URAND.ilhash(RAYCALLS.dimlist, RAYCALLS.ndims) + RAYCALLS.samplendx);
                }
                MULTISAMP.multisamp(rv, 2, d);
                d = 2.0 * Math.PI * rv[0];
                cosp = TCOS.tcos(d);
                sinp = RTMATH.tsin(d);
                if ((0. <= RAYCALLS.specjitter) & (RAYCALLS.specjitter < 1.)) {
                    rv[1] = 1.0 - RAYCALLS.specjitter * rv[1];
                }
                if (rv[1] <= FVECT.FTINY) {
                    d = 1.0;
                } else {
                    d = Math.sqrt(np.alpha2 * -Math.log(rv[1]));
                }
                for (i = 0; i < 3; i++) {
                    sr.rdir.data[i] = np.prdir.data[i] + d * (cosp * u.data[i] + sinp * v.data[i]);
                }
                /* sample rejection test */
                if (FVECT.DOT(sr.rdir, r.ron) >= -FVECT.FTINY) {
                    continue;
                }
                FVECT.normalize(sr.rdir);	/* OK, normalize */
                if (nstaken != 0) /* multi-sampling */ {
                    RAYTRACE.rayclear(sr);
                }
                RAY.rayvalue(sr);
                COLOR.multcolor(sr.rcol, sr.rcoef);
                COLOR.addcolor(r.rcol, sr.rcol);
                ++nstaken;
            }
            RAYCALLS.ndims--;
        }
    }
}
