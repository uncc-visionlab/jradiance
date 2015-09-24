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

import java.io.IOException;
import jradiance.common.COLORS.COLOR;
import jradiance.common.FVECT;
import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OBJECT_STRUCTURE;
import jradiance.common.OTYPES;
import jradiance.rt.FUNC.MFUNC;

/**
 *
 * @author arwillis
 */
public class ANISO extends OBJECT_STRUCTURE {
    /*
     *  Shading functions for anisotropic materials.
     */
    public static int MAXITER = 10;		/* maximum # specular ray attempts */

    /*
     *	This routine implements the anisotropic Gaussian
     *  model described by Ward in Siggraph `92 article.
     *	We orient the surface towards the incoming ray, so a single
     *  surface can be used to represent an infinitely thin object.
     *
     *  Arguments for MAT_PLASTIC2 and MAT_METAL2 are:
     *  4+ ux	uy	uz	funcfile	[transform...]
     *  0
     *  6  red	grn	blu	specular-frac.	u-facet-slope v-facet-slope
     *
     *  Real arguments for MAT_TRANS2 are:
     *  8  red 	grn	blu	rspec	u-rough	v-rough	trans	tspec
     */

    /* specularity flags */
    public static int SP_REFL = 01;		/* has reflected specular component */

    public static int SP_TRAN = 02;		/* has transmitted specular */

    public static int SP_FLAT = 04;		/* reflecting surface is flat */

    public static int SP_RBLT = 010;		/* reflection below sample threshold */

    public static int SP_TBLT = 020;		/* transmission below threshold */

    public static int SP_BADU = 040;		/* bad u direction calculation */
    

    public static class ANISODAT {

        OBJREC mp;		/* material pointer */

        RAY rp;		/* ray pointer */

        short specfl;		/* specularity flags, defined above */

        COLOR mcolor = new COLOR();		/* color of this material */

        COLOR scolor = new COLOR();		/* color of specular component */

        FVECT vrefl = new FVECT();		/* vector in reflected direction */

        FVECT prdir = new FVECT();		/* vector in transmitted direction */

        FVECT u = new FVECT(), v = new FVECT();		/* u and v vectors orienting anisotropy */

        double u_alpha;	/* u roughness */

        double v_alpha;	/* v roughness */

        double rdiff, rspec;	/* reflected specular, diffuse */

        double trans;		/* transmissivity */

        double tdiff, tspec;	/* transmitted specular, diffuse */

        FVECT pnorm = new FVECT();		/* perturbed surface normal */

        double pdot;		/* perturbed dot product */

    }  		/* anisotropic material data */

//static srcdirf_t diraniso;

    public static class diraniso implements SOURCE.srcdirf_t {
//static void
//diraniso(		/* compute source contribution */
//	COLOR  cval,			/* returned coefficient */
//	ANISODAT  nnp,		/* material data */
//	FVECT  ldir,			/* light source direction */
//	double  omega			/* light source size */
//)
        @Override
        public void dirf( /* compute source contribution */
                COLOR cval, /* returned coefficient */
                Object nnp, /* material data */
                FVECT ldir, /* light source direction */
                double omega /* light source size */) {
            ANISODAT np = (ANISODAT) nnp;
            double ldot;
            double dtmp, dtmp1, dtmp2;
            FVECT h = new FVECT();
            double au2, av2;
            COLOR ctmp = new COLOR();
            
            cval.setcolor(0.0f, 0.0f, 0.0f);
            
            ldot = FVECT.DOT(np.pnorm, ldir);
            
            if (ldot < 0.0 ? np.trans <= FVECT.FTINY : np.trans >= 1.0 - FVECT.FTINY) {
                return;		/* wrong side */
            }
            
            if (ldot > FVECT.FTINY && np.rdiff > FVECT.FTINY) {
                /*
                 *  Compute and add diffuse reflected component to returned
                 *  color.  The diffuse reflected component will always be
                 *  modified by the color of the material.
                 */
                COLOR.copycolor(ctmp, np.mcolor);
                dtmp = ldot * omega * np.rdiff * (1.0 / Math.PI);
                ctmp.scalecolor(dtmp);
                COLOR.addcolor(cval, ctmp);
            }
            if (ldot > FVECT.FTINY && (np.specfl & (SP_REFL | SP_BADU)) == SP_REFL) {
                /*
                 *  Compute specular reflection coefficient using
                 *  anisotropic Gaussian distribution model.
                 */
                /* add source width if flat */
                if ((np.specfl & SP_FLAT) != 0) {
                    au2 = av2 = omega * (0.25 / Math.PI);
                } else {
                    au2 = av2 = 0.0;
                }
                au2 += np.u_alpha * np.u_alpha;
                av2 += np.v_alpha * np.v_alpha;
                /* half vector */
                h.data[0] = ldir.data[0] - np.rp.rdir.data[0];
                h.data[1] = ldir.data[1] - np.rp.rdir.data[1];
                h.data[2] = ldir.data[2] - np.rp.rdir.data[2];
                /* ellipse */
                dtmp1 = FVECT.DOT(np.u, h);
                dtmp1 *= dtmp1 / au2;
                dtmp2 = FVECT.DOT(np.v, h);
                dtmp2 *= dtmp2 / av2;
                /* new W-G-M-D model */
                dtmp = FVECT.DOT(np.pnorm, h);
                dtmp *= dtmp;
                dtmp1 = (dtmp1 + dtmp2) / dtmp;
                dtmp = Math.exp(-dtmp1) * FVECT.DOT(h, h)
                        / (Math.PI * dtmp * dtmp * Math.sqrt(au2 * av2));
                /* worth using? */
                if (dtmp > FVECT.FTINY) {
                    COLOR.copycolor(ctmp, np.scolor);
                    dtmp *= ldot * omega;
                    ctmp.scalecolor(dtmp);
                    COLOR.addcolor(cval, ctmp);
                }
            }
            if (ldot < -FVECT.FTINY && np.tdiff > FVECT.FTINY) {
                /*
                 *  Compute diffuse transmission.
                 */
                COLOR.copycolor(ctmp, np.mcolor);
                dtmp = -ldot * omega * np.tdiff * (1.0 / Math.PI);
                ctmp.scalecolor(dtmp);
                COLOR.addcolor(cval, ctmp);
            }
            if (ldot < -FVECT.FTINY && (np.specfl & (SP_TRAN | SP_BADU)) == SP_TRAN) {
                /*
                 *  Compute specular transmission.  Specular transmission
                 *  is always modified by material color.
                 */
                /* roughness + source */
                au2 = av2 = omega * (1.0 / Math.PI);
                au2 += np.u_alpha * np.u_alpha;
                av2 += np.v_alpha * np.v_alpha;
                /* "half vector" */
                h.data[0] = ldir.data[0] - np.prdir.data[0];
                h.data[1] = ldir.data[1] - np.prdir.data[1];
                h.data[2] = ldir.data[2] - np.prdir.data[2];
                dtmp = FVECT.DOT(h, h);
                if (dtmp > FVECT.FTINY * FVECT.FTINY) {
                    dtmp1 = FVECT.DOT(h, np.pnorm);
                    dtmp = 1.0 - dtmp1 * dtmp1 / dtmp;
                    if (dtmp > FVECT.FTINY * FVECT.FTINY) {
                        dtmp1 = FVECT.DOT(h, np.u);
                        dtmp1 *= dtmp1 / au2;
                        dtmp2 = FVECT.DOT(h, np.v);
                        dtmp2 *= dtmp2 / av2;
                        dtmp = (dtmp1 + dtmp2) / dtmp;
                    }
                } else {
                    dtmp = 0.0;
                }
                /* Gaussian */
                dtmp = Math.exp(-dtmp) * (1.0 / Math.PI) * Math.sqrt(-ldot / (np.pdot * au2 * av2));
                /* worth using? */
                if (dtmp > FVECT.FTINY) {
                    COLOR.copycolor(ctmp, np.mcolor);
                    dtmp *= np.tspec * omega;
                    ctmp.scalecolor(dtmp);
                    COLOR.addcolor(cval, ctmp);
                }
            }
        }
    }
    @Override
    public int octree_function(Object... obj) {
        throw new UnsupportedOperationException("Not supported yet.");
    }   
    int m_aniso( /* shade ray that hit something anisotropic */
            OBJREC m,
            RAY r) throws IOException {
        ANISODAT nd = new ANISODAT();
        COLOR ctmp = new COLOR();
        int i;
        /* easy shadow test */
        if ((r.crtype & RAY.SHADOW) != 0) {
            return (1);
        }
        
        if (m.oargs.nfargs != (m.otype == OTYPES.MAT_TRANS2 ? 8 : 6)) {
//		objerror(m, USER, "bad number of real arguments");
        }
        /* check for back side */
        if (r.rod < 0.0) {
            if (RPICT.backvis == 0 && m.otype != OTYPES.MAT_TRANS2) {
                RAYTRACE.raytrans(r);
                return (1);
            }
            RAYTRACE.raytexture(r, m.omod);
            RAYTRACE.flipsurface(r);			/* reorient if backvis */
        } else {
            RAYTRACE.raytexture(r, m.omod);
        }
        /* get material color */
        nd.mp = m;
        nd.rp = r;
        nd.mcolor.setcolor((float) m.oargs.farg[0],
                (float) m.oargs.farg[1],
                (float) m.oargs.farg[2]);
        /* get roughness */
        nd.specfl = 0;
        nd.u_alpha = m.oargs.farg[4];
        nd.v_alpha = m.oargs.farg[5];
        if (nd.u_alpha <= FVECT.FTINY || nd.v_alpha <= FVECT.FTINY) {
//		objerror(m, USER, "roughness too small");
        }
        nd.pdot = RAYTRACE.raynormal(nd.pnorm, r);	/* perturb normal */
        if (nd.pdot < .001) {
            nd.pdot = .001;			/* non-zero for diraniso() */
        }
        COLOR.multcolor(nd.mcolor, r.pcol);		/* modify material color */
        /* get specular component */
        if ((nd.rspec = m.oargs.farg[3]) > FVECT.FTINY) {
            nd.specfl |= SP_REFL;
            /* compute specular color */
            if (m.otype == OTYPES.MAT_METAL2) {
                COLOR.copycolor(nd.scolor, nd.mcolor);
            } else {
                nd.scolor.setcolor(1.0f, 1.0f, 1.0f);
            }
            nd.scolor.scalecolor(nd.rspec);
            /* check threshold */
            if (RPICT.specthresh >= nd.rspec - FVECT.FTINY) {
                nd.specfl |= SP_RBLT;
            }
            /* compute refl. direction */
            FVECT.VSUM(nd.vrefl, r.rdir, nd.pnorm, 2.0 * nd.pdot);
            if (FVECT.DOT(nd.vrefl, r.ron) <= FVECT.FTINY) /* penetration? */ {
                FVECT.VSUM(nd.vrefl, r.rdir, r.ron, 2.0 * r.rod);
            }
        }
        /* compute transmission */
        if (m.otype == OTYPES.MAT_TRANS2) {
            nd.trans = m.oargs.farg[6] * (1.0 - nd.rspec);
            nd.tspec = nd.trans * m.oargs.farg[7];
            nd.tdiff = nd.trans - nd.tspec;
            if (nd.tspec > FVECT.FTINY) {
                nd.specfl |= SP_TRAN;
                /* check threshold */
                if (RPICT.specthresh >= nd.tspec - FVECT.FTINY) {
                    nd.specfl |= SP_TBLT;
                }
                if (FVECT.DOT(r.pert, r.pert) <= FVECT.FTINY * FVECT.FTINY) {
                    FVECT.VCOPY(nd.prdir, r.rdir);
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

        /* diffuse reflection */
        nd.rdiff = 1.0 - nd.trans - nd.rspec;
        
        if (r.ro != null && OTYPES.isflat(r.ro.otype) != 0) {
            nd.specfl |= SP_FLAT;
        }
        
        getacoords(r, nd);			/* set up coordinates */
        
        if ((nd.specfl & (SP_REFL | SP_TRAN)) != 0 && (nd.specfl & SP_BADU) == 0) {
            agaussamp(r, nd);
        }
        
        if (nd.rdiff > FVECT.FTINY) {		/* ambient from this side */
            COLOR.copycolor(ctmp, nd.mcolor);	/* modified by material color */
            ctmp.scalecolor(nd.rdiff);
            if ((nd.specfl & SP_RBLT) != 0) /* add in specular as well? */ {
                COLOR.addcolor(ctmp, nd.scolor);
            }
            AMBIENT.multambient(ctmp, r, nd.pnorm);
            COLOR.addcolor(r.rcol, ctmp);	/* add to returned color */
        }
        if (nd.tdiff > FVECT.FTINY) {		/* ambient from other side */
            FVECT bnorm = new FVECT();
            
            RAYTRACE.flipsurface(r);
            bnorm.data[0] = -nd.pnorm.data[0];
            bnorm.data[1] = -nd.pnorm.data[1];
            bnorm.data[2] = -nd.pnorm.data[2];
            COLOR.copycolor(ctmp, nd.mcolor);	/* modified by color */
            if ((nd.specfl & SP_TBLT) != 0) {
                ctmp.scalecolor(nd.trans);
            } else {
                ctmp.scalecolor(nd.tdiff);
            }
            AMBIENT.multambient(ctmp, r, bnorm);
            COLOR.addcolor(r.rcol, ctmp);
            RAYTRACE.flipsurface(r);
        }
        /* add direct component */
//	SOURCE.direct(r, diraniso, nd);
        SOURCE.direct(r, new diraniso(), nd);
        
        return (1);
    }
    
    static void getacoords( /* set up coordinate system */
            RAY r,
            ANISODAT np) {
	MFUNC  mf = new MFUNC();
	int  i;

//	mf = getfunc(np->mp, 3, 0x7, 1);
//	setfunc(np->mp, r);
//	errno = 0;
//	for (i = 0; i < 3; i++)
//		np->u[i] = evalue(mf->ep[i]);
//	if (errno == EDOM || errno == ERANGE) {
//		objerror(np->mp, WARNING, "compute error");
//		np->specfl |= SP_BADU;
//		return;
//	}
//	if (mf->f != &unitxf)
//		multv3(np->u, np->u, mf->f->xfm);
//	fcross(np->v, np->pnorm, np->u);
//	if (normalize(np->v) == 0.0) {
//		objerror(np->mp, WARNING, "illegal orientation vector");
//		np->specfl |= SP_BADU;
//		return;
//	}
//	fcross(np->u, np->v, np->pnorm);
    }
    
    static void agaussamp( /* sample anisotropic Gaussian specular */
            RAY r,
            ANISODAT np) {
        RAY sr = new RAY();
        FVECT h = new FVECT();
//	double  rv[2];
//	double  d, sinp, cosp;
//	COLOR	scol;
//	int  maxiter, ntrials, nstarget, nstaken;
//	register int  i;
//					/* compute reflection */
//	if ((np->specfl & (SP_REFL|SP_RBLT)) == SP_REFL &&
//			rayorigin(&sr, SPECULAR, r, np->scolor) == 0) {
//		nstarget = 1;
//		if (specjitter > 1.5) {	/* multiple samples? */
//			nstarget = specjitter*r->rweight + .5;
//			if (sr.rweight <= minweight*nstarget)
//				nstarget = sr.rweight/minweight;
//			if (nstarget > 1) {
//				d = 1./nstarget;
//				scalecolor(sr.rcoef, d);
//				sr.rweight *= d;
//			} else
//				nstarget = 1;
//		}
//		setcolor(scol, 0., 0., 0.);
//		dimlist[ndims++] = (int)(size_t)np->mp;
//		maxiter = MAXITER*nstarget;
//		for (nstaken = ntrials = 0; nstaken < nstarget &&
//						ntrials < maxiter; ntrials++) {
//			if (ntrials)
//				d = frandom();
//			else
//				d = urand(ilhash(dimlist,ndims)+samplendx);
//			multisamp(rv, 2, d);
//			d = 2.0*PI * rv[0];
//			cosp = tcos(d) * np->u_alpha;
//			sinp = tsin(d) * np->v_alpha;
//			d = 1./sqrt(cosp*cosp + sinp*sinp);
//			cosp *= d;
//			sinp *= d;
//			if ((0. <= specjitter) & (specjitter < 1.))
//				rv[1] = 1.0 - specjitter*rv[1];
//			if (rv[1] <= FTINY)
//				d = 1.0;
//			else
//				d = sqrt(-log(rv[1]) /
//					(cosp*cosp/(np->u_alpha*np->u_alpha) +
//					 sinp*sinp/(np->v_alpha*np->v_alpha)));
//			for (i = 0; i < 3; i++)
//				h[i] = np->pnorm[i] +
//					d*(cosp*np->u[i] + sinp*np->v[i]);
//			d = -2.0 * DOT(h, r->rdir) / (1.0 + d*d);
//			VSUM(sr.rdir, r->rdir, h, d);
//						/* sample rejection test */
//			if ((d = DOT(sr.rdir, r->ron)) <= FTINY)
//				continue;
//			checknorm(sr.rdir);
//			if (nstarget > 1) {	/* W-G-M-D adjustment */
//				if (nstaken) rayclear(&sr);
//				rayvalue(&sr);
//				d = 2./(1. + r->rod/d);
//				scalecolor(sr.rcol, d);
//				addcolor(scol, sr.rcol);
//			} else {
//				rayvalue(&sr);
//				multcolor(sr.rcol, sr.rcoef);
//				addcolor(r->rcol, sr.rcol);
//			}
//			++nstaken;
//		}
//		if (nstarget > 1) {		/* final W-G-M-D weighting */
//			multcolor(scol, sr.rcoef);
//			d = (double)nstarget/ntrials;
//			scalecolor(scol, d);
//			addcolor(r->rcol, scol);
//		}
//		ndims--;
//	}
//					/* compute transmission */
//	copycolor(sr.rcoef, np->mcolor);		/* modify by material color */
//	scalecolor(sr.rcoef, np->tspec);
//	if ((np->specfl & (SP_TRAN|SP_TBLT)) == SP_TRAN &&
//			rayorigin(&sr, SPECULAR, r, sr.rcoef) == 0) {
//		nstarget = 1;
//		if (specjitter > 1.5) {	/* multiple samples? */
//			nstarget = specjitter*r->rweight + .5;
//			if (sr.rweight <= minweight*nstarget)
//				nstarget = sr.rweight/minweight;
//			if (nstarget > 1) {
//				d = 1./nstarget;
//				scalecolor(sr.rcoef, d);
//				sr.rweight *= d;
//			} else
//				nstarget = 1;
//		}
//		dimlist[ndims++] = (int)(size_t)np->mp;
//		maxiter = MAXITER*nstarget;
//		for (nstaken = ntrials = 0; nstaken < nstarget &&
//						ntrials < maxiter; ntrials++) {
//			if (ntrials)
//				d = frandom();
//			else
//				d = urand(ilhash(dimlist,ndims)+1823+samplendx);
//			multisamp(rv, 2, d);
//			d = 2.0*PI * rv[0];
//			cosp = tcos(d) * np->u_alpha;
//			sinp = tsin(d) * np->v_alpha;
//			d = 1./sqrt(cosp*cosp + sinp*sinp);
//			cosp *= d;
//			sinp *= d;
//			if ((0. <= specjitter) & (specjitter < 1.))
//				rv[1] = 1.0 - specjitter*rv[1];
//			if (rv[1] <= FTINY)
//				d = 1.0;
//			else
//				d = sqrt(-log(rv[1]) /
//					(cosp*cosp/(np->u_alpha*np->u_alpha) +
//					 sinp*sinp/(np->v_alpha*np->v_alpha)));
//			for (i = 0; i < 3; i++)
//				sr.rdir[i] = np->prdir[i] +
//						d*(cosp*np->u[i] + sinp*np->v[i]);
//			if (DOT(sr.rdir, r->ron) >= -FTINY)
//				continue;
//			normalize(sr.rdir);	/* OK, normalize */
//			if (nstaken)		/* multi-sampling */
//				rayclear(&sr);
//			rayvalue(&sr);
//			multcolor(sr.rcol, sr.rcoef);
//			addcolor(r->rcol, sr.rcol);
//			++nstaken;
//		}
//		ndims--;
//	}
    }
}
