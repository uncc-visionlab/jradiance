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

import jradiance.common.COLORS.COLOR;
import jradiance.common.FVECT;
import jradiance.common.OBJECT;
import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OBJSET;
import jradiance.common.OCTREE;
import jradiance.common.OCTREE.CUBE;
import jradiance.common.OTYPES;

/**
 *
 * @author arwillis
 */
public class RAYTRACE {

    /*
     *  raytrace.c - routines for tracing and shading rays.
     *
     *  External symbols declared in ray.h
     */
    public static final int MAXCSET = ((OBJECT.MAXSET + 1) * 2 - 1);	/* maximum check set size */

    static long raynum = 0;		/* next unique ray number */

    static long nrays = 0;		/* number of calls to localhit */

    static final double[] Lambfa = {Math.PI, Math.PI, Math.PI, 0.0, 0.0};
    static OBJREC Lamb = new OBJREC(OBJECT.OVOID, OTYPES.MAT_PLASTIC, "Lambertian",
            null, Lambfa, 0, 5, null);				/* a Lambertian surface */

    static OBJREC Aftplane = new OBJREC(0, 0, null, null, null, 0, 0, null);			/* aft clipping plane object */

    public static final int RAYHIT = (-1);		/* return value for intercepted ray */


    public static int rayorigin( /* start new ray from old one */
            RAY r,
            int rt,
            RAY ro,
            COLOR rc) {
        double rw, re;
        /* assign coefficient/weight */
        if (rc == null) {
            rw = 1.0;
            r.rcoef.setcolor(1.f, 1.f, 1.f);
        } else {
            rw = COLOR.intens(rc);
            if (rc != r.rcoef) {
                COLOR.copycolor(r.rcoef, rc);
            }
        }
        if ((r.parent = ro) == null) {		/* primary ray */
            r.rlvl = 0;
            r.rweight = (float) rw;
            r.crtype = r.rtype = (short) rt;
            r.rsrc = -1;
            r.clipset = null;
//		r.revf = raytrace;
            r.revf = std_raytrace;
            COLOR.copycolor(r.cext, RAYCALLS.cextinction);
            COLOR.copycolor(r.albedo, RAYCALLS.salbedo);
            r.gecc = (float) RAYCALLS.seccg;
            r.slights = null;
        } else {				/* spawned ray */
            if (ro.rot >= FVECT.FHUGE) {
                //memset(r, 0, sizeof(RAY));
                return (-1);		/* illegal continuation */
            }
            r.rlvl = ro.rlvl;
            if ((rt & RAY.RAYREFL) != 0) {
                r.rlvl++;
                r.rsrc = -1;
                r.clipset = ro.clipset;
                r.rmax = 0.0;
            } else {
                r.rsrc = ro.rsrc;
                r.clipset = ro.newcset;
                r.rmax = ro.rmax <= FVECT.FTINY ? 0.0 : ro.rmax - ro.rot;
            }
            r.revf = ro.revf;
            COLOR.copycolor(r.cext, ro.cext);
            COLOR.copycolor(r.albedo, ro.albedo);
            r.gecc = ro.gecc;
            r.slights = ro.slights;
            r.crtype = (short) (ro.crtype | (r.rtype = (short) rt));
            FVECT.VCOPY(r.rorg, ro.rop);
            r.rweight = (float) (ro.rweight * rw);
            /* estimate extinction */
            re = ro.cext.colval(COLOR.RED) < ro.cext.colval(COLOR.GRN)
                    ? ro.cext.colval(COLOR.RED) : ro.cext.colval(COLOR.GRN);
            if (ro.cext.colval(COLOR.BLU) < re) {
                re = ro.cext.colval(COLOR.BLU);
            }
            re *= ro.rot;
            if (re > 0.1) {
                if (re > 92.) {
                    r.rweight = 0.0f;
                } else {
                    r.rweight *= Math.exp(-re);
                }
            }
        }
        rayclear(r);
        if (r.rweight <= 0.0) /* check for expiration */ {
            return (-1);
        }
        if ((r.crtype & RAY.SHADOW) != 0) /* shadow commitment */ {
            return (0);
        }
        if (RAYCALLS.maxdepth <= 0 && rc != null) {	/* Russian roulette */
            if (RAYCALLS.minweight <= 0.0) {
//			error(USER, "zero ray weight in Russian roulette");
            }
            if (RAYCALLS.maxdepth < 0 && r.rlvl > -RAYCALLS.maxdepth) {
                return (-1);		/* upper reflection limit */
            }
            if (r.rweight >= RAYCALLS.minweight) {
                return (0);
            }
            if (RAYCALLS.frandom() > r.rweight / RAYCALLS.minweight) {
                return (-1);
            }
            rw = RAYCALLS.minweight / r.rweight;	/* promote survivor */
            r.rcoef.scalecolor(rw);
            r.rweight = (float) RAYCALLS.minweight;
            return (0);
        }
        return (r.rlvl <= Math.abs(RAYCALLS.maxdepth) && r.rweight >= RAYCALLS.minweight ? 0 : -1);
    }

    static void rayclear( /* clear a ray for (re)evaluation */
            RAY r) {
        r.rno = raynum++;
        r.newcset = r.clipset;
//	r.hitf = rayhit;
        r.hitf = std_rayhit;
        r.robj = OBJECT.OVOID;
        r.ro = null;
        r.rox = null;
        r.rt = r.rot = FVECT.FHUGE;
        r.pert.data[0] = r.pert.data[1] = r.pert.data[2] = 0.0;
        r.uv[0] = r.uv[1] = 0.0;
        r.pcol.setcolor(1.0f, 1.0f, 1.0f);
        r.rcol.setcolor(0.0f, 0.0f, 0.0f);
    }

    public interface RAYTRACER {

        public void raytrace(RAY r);
    }

    public static class STDRAYTRACE implements RAYTRACER {

        @Override
        public void raytrace( /* trace a ray and compute its value */
                RAY r) {
            if (localhit(r, RAYCALLS.thescene) != 0) {
                raycont(r);		/* hit local surface, evaluate */
            } else if (r.ro == Aftplane) {
                r.ro = null;		/* hit aft clipping plane */
                r.rot = FVECT.FHUGE;
            } else if (SOURCE.sourcehit(r) != 0) {
                rayshade(r, r.ro.omod);	/* distant source */
            }

//	if (trace != null)
//		(*trace)(r);		/* trace execution */

            rayparticipate(r);		/* for participating medium */
        }
    }
    static STDRAYTRACE std_raytrace = new STDRAYTRACE();

    static void raycont( /* check for clipped object and continue */
            RAY r) {
        if ((r.clipset != null && OBJSET.inset(r.clipset, r.ro.omod) != 0)
                || rayshade(r, r.ro.omod) == 0) {
            raytrans(r);
        }
    }

    public static void raytrans( /* transmit ray as is */
            RAY r) {
        RAY tr = new RAY();

        if (rayorigin(tr, RAY.TRANS, r, null) == 0) {
            FVECT.VCOPY(tr.rdir, r.rdir);
            RAY.rayvalue(tr);
            COLOR.copycolor(r.rcol, tr.rcol);
            r.rt = r.rot + tr.rt;
        }
    }

    public static int rayshade( /* shade ray r with material mod */
            RAY r,
            int mod) {
        OBJREC m;

        r.rt = r.rot;			/* set effective ray length */
        for (; mod != OBJECT.OVOID; mod = m.omod) {
            m = OBJECT.objptr(mod);
            /****** unnecessary test since modifier() is always called
            if (!ismodifier(m.otype)) {
            sprintf(errmsg, "illegal modifier \"%s\"", m.oname);
            error(USER, errmsg);
            }
             ******/
            /* hack for irradiance calculation */
            if (RAYCALLS.do_irrad != 0 && (r.crtype & ~(RAY.PRIMARY | RAY.TRANS)) == 0
                    && m.otype != OTYPES.MAT_CLIP
                    && (OTYPES.ofun[m.otype].flags & (OTYPES.T_M | OTYPES.T_X)) != 0) {
                if (OTSPECIAL.irr_ignore(m.otype) != 0) {
                    raytrans(r);
                    return (1);
                }
                if (OTYPES.islight(m.otype) == 0) {
                    m = Lamb;
                }
            }
            if (OTYPES.ofun[m.otype].ofunc.octree_function(m, r) != 0) {
                return (1);     /* materials call raytexture() */
            }
//		if ((*ofun[m.otype].funp)(m, r))
//			return(1);	/* materials call raytexture() */
        }
        return (0);			/* no material! */
    }

    static void rayparticipate( /* compute ray medium participation */
            RAY r) {
        COLOR ce = new COLOR(), ca = new COLOR();
        double re, ge, be;

        if (COLOR.intens(r.cext) <= 1. / FVECT.FHUGE) {
            return;				/* no medium */
        }
        re = r.rot * r.cext.colval(COLOR.RED);
        ge = r.rot * r.cext.colval(COLOR.GRN);
        be = r.rot * r.cext.colval(COLOR.BLU);
        if ((r.crtype & RAY.SHADOW) != 0) {		/* no scattering for sources */
            re *= 1. - r.albedo.colval(COLOR.RED);
            ge *= 1. - r.albedo.colval(COLOR.GRN);
            be *= 1. - r.albedo.colval(COLOR.BLU);
        }
        ce.setcolor(re <= FVECT.FTINY ? 1.f : re > 92. ? 0.f : (float) Math.exp(-re),
                ge <= FVECT.FTINY ? 1.f : ge > 92. ? 0.f : (float) Math.exp(-ge),
                be <= FVECT.FTINY ? 1.f : be > 92. ? 0.f : (float) Math.exp(-be));
        COLOR.multcolor(r.rcol, ce);			/* path extinction */
        if ((r.crtype & RAY.SHADOW) != 0 || COLOR.intens(r.albedo) <= FVECT.FTINY) {
            return;				/* no scattering */
        }
        ca.setcolor(
                (float) (r.albedo.colval(COLOR.RED) * RAYCALLS.ambval.colval(COLOR.RED) * (1. - ce.colval(COLOR.RED))),
                (float) (r.albedo.colval(COLOR.GRN) * RAYCALLS.ambval.colval(COLOR.GRN) * (1. - ce.colval(COLOR.GRN))),
                (float) (r.albedo.colval(COLOR.BLU) * RAYCALLS.ambval.colval(COLOR.BLU) * (1. - ce.colval(COLOR.BLU))));
        COLOR.addcolor(r.rcol, ca);			/* ambient in scattering */
        SOURCE.srcscatter(r);				/* source in scattering */
    }

    public static void raytexture( /* get material modifiers */
            RAY r,
            int mod) {
        OBJREC m;
        /* execute textures and patterns */
        for (; mod != OBJECT.OVOID; mod = m.omod) {
            m = OBJECT.objptr(mod);
            /****** unnecessary test since modifier() is always called
            if (!ismodifier(m.otype)) {
            sprintf(errmsg, "illegal modifier \"%s\"", m.oname);
            error(USER, errmsg);
            }
             ******/
            //if ((*ofun[m.otype].funp)(m, r)) {
            if ((OTYPES.ofun[m.otype].ofunc.octree_function(m, r)) != 0) {
//			sprintf(errmsg, "conflicting material \"%s\"",
//					m.oname);
//			objerror(r.ro, USER, errmsg);
            }
        }
    }

//extern int
//raymixture(		/* mix modifiers */
//	register RAY  *r,
//	OBJECT  fore,
//	OBJECT  back,
//	double  coef
//)
//{
//	RAY  fr, br;
//	int  foremat, backmat;
//	register int  i;
//					/* bound coefficient */
//	if (coef > 1.0)
//		coef = 1.0;
//	else if (coef < 0.0)
//		coef = 0.0;
//					/* compute foreground and background */
//	foremat = backmat = 0;
//					/* foreground */
//	fr = *r;
//	if (coef > FTINY) {
//		fr.rweight *= coef;
//		scalecolor(fr.rcoef, coef);
//		foremat = rayshade(&fr, fore);
//	}
//					/* background */
//	br = *r;
//	if (coef < 1.0-FTINY) {
//		br.rweight *= 1.0-coef;
//		scalecolor(br.rcoef, 1.0-coef);
//		backmat = rayshade(&br, back);
//	}
//					/* check for transparency */
//	if (backmat ^ foremat) {
//		if (backmat && coef > FTINY)
//			raytrans(&fr);
//		else if (foremat && coef < 1.0-FTINY)
//			raytrans(&br);
//	}
//					/* mix perturbations */
//	for (i = 0; i < 3; i++)
//		r.pert[i] = coef*fr.pert[i] + (1.0-coef)*br.pert[i];
//					/* mix pattern colors */
//	scalecolor(fr.pcol, coef);
//	scalecolor(br.pcol, 1.0-coef);
//	copycolor(r.pcol, fr.pcol);
//	addcolor(r.pcol, br.pcol);
//					/* return value tells if material */
//	if (!foremat & !backmat)
//		return(0);
//					/* mix returned ray values */
//	scalecolor(fr.rcol, coef);
//	scalecolor(br.rcol, 1.0-coef);
//	copycolor(r.rcol, fr.rcol);
//	addcolor(r.rcol, br.rcol);
//	r.rt = bright(fr.rcol) > bright(br.rcol) ? fr.rt : br.rt;
//	return(1);
//}
//
//
    public static double raydist( /* compute (cumulative) ray distance */
            RAY r,
            int flags) {
        RAY r1 = r;
        double sum = 0.0;

        while (r1 != null && (r1.crtype & flags) != 0) {
            sum += r1.rot;
            r1 = r1.parent;
        }
        return (sum);
    }

//
//extern void
//raycontrib(		/* compute (cumulative) ray contribution */
//	RREAL  rc[3],
//	const RAY  *r,
//	int  flags
//)
//{
//	double	eext[3];
//	int	i;
//
//	eext[0] = eext[1] = eext[2] = 0.;
//	rc[0] = rc[1] = rc[2] = 1.;
//
//	while (r != NULL && r.crtype&flags) {
//		for (i = 3; i--; ) {
//			rc[i] *= colval(r.rcoef,i);
//			eext[i] += r.rot * colval(r.cext,i);
//		}
//		r = r.parent;
//	}
//	for (i = 3; i--; )
//		rc[i] *= (eext[i] <= FTINY) ? 1. :
//				(eext[i] > 92.) ? 0. : exp(-eext[i]);
//}
    public static double raynormal( /* compute perturbed normal for ray */
            FVECT norm,
            RAY r) {
        double newdot;
        int i;

        /*	The perturbation is added to the surface normal to obtain
         *  the new normal.  If the new normal would affect the surface
         *  orientation wrt. the ray, a correction is made.  The method is
         *  still fraught with problems since reflected rays and similar
         *  directions calculated from the surface normal may spawn rays behind
         *  the surface.  The only solution is to curb textures at high
         *  incidence (namely, keep DOT(rdir,pert) < Rdot).
         */

        for (i = 0; i < 3; i++) {
            norm.data[i] = r.ron.data[i] + r.pert.data[i];
        }

        if (FVECT.normalize(norm) == 0.0) {
//		objerror(r.ro, WARNING, "illegal normal perturbation");
            FVECT.VCOPY(norm, r.ron);
            return (r.rod);
        }
        newdot = -FVECT.DOT(norm, r.rdir);
        if ((newdot > 0.0) != (r.rod > 0.0)) {		/* fix orientation */
            for (i = 0; i < 3; i++) {
                norm.data[i] += 2.0 * newdot * r.rdir.data[i];
            }
            newdot = -newdot;
        }
        return (newdot);
    }

    public static void newrayxf( /* get new tranformation matrix for ray */
            RAY r) {
//	static struct xfn {
//		struct xfn  *next;
//		FULLXF  xf;
//	}  xfseed = { &xfseed }, *xflast = &xfseed;
//	register struct xfn  *xp;
//	register const RAY  *rp;
//
//	/*
//	 * Search for transform in circular list that
//	 * has no associated ray in the tree.
//	 */
//	xp = xflast;
//	for (rp = r.parent; rp != NULL; rp = rp.parent)
//		if (rp.rox == &xp.xf) {		/* xp in use */
//			xp = xp.next;			/* move to next */
//			if (xp == xflast) {		/* need new one */
//				xp = (struct xfn *)malloc(sizeof(struct xfn));
//				if (xp == NULL)
//					error(SYSTEM,
//						"out of memory in newrayxf");
//							/* insert in list */
//				xp.next = xflast.next;
//				xflast.next = xp;
//				break;			/* we're done */
//			}
//			rp = r;			/* start check over */
//		}
//					/* got it */
//	r.rox = &xp.xf;
//	xflast = xp;
    }

    public static void flipsurface( /* reverse surface orientation */
            RAY r) {
        r.rod = -r.rod;
        r.ron.data[0] = -r.ron.data[0];
        r.ron.data[1] = -r.ron.data[1];
        r.ron.data[2] = -r.ron.data[2];
        r.pert.data[0] = -r.pert.data[0];
        r.pert.data[1] = -r.pert.data[1];
        r.pert.data[2] = -r.pert.data[2];
    }

    public interface RAYHIT {

        public void rayhit(int[] oset, RAY r);
    }

    public static class STDRAYHIT implements RAYHIT {

        @Override
        public void rayhit( /* standard ray hit test */
                int[] oset,
                RAY r) {
            OBJREC o;
            int i;

            for (i = oset[0]; i > 0; i--) {
                o = OBJECT.objptr(oset[i]);
                if (OTYPES.ofun[o.otype].ofunc.octree_function(o, r) != 0) {
                    r.robj = oset[i];
                }
//		if ((*ofun[o.otype].funp)(o, r))
//			r.robj = oset[i];
            }
        }
    }
    static STDRAYHIT std_rayhit = new STDRAYHIT();

    static int localhit( /* check for hit in the octree */
            RAY r,
            CUBE scene) {
        int[] cxset = new int[MAXCSET + 1];	/* set of checked objects */
        FVECT curpos = new FVECT();			/* current cube position */
        int sflags;			/* sign flags */
        double t, dt;
        int i;

        nrays++;			/* increment trace counter */
        sflags = 0;
        for (i = 0; i < 3; i++) {
            curpos.data[i] = r.rorg.data[i];
            if (r.rdir.data[i] > 1e-7) {
                sflags |= 1 << i;
            } else if (r.rdir.data[i] < -1e-7) {
                sflags |= 0x10 << i;
            }
        }
        if (sflags == 0) {
//		error(WARNING, "zero ray direction in localhit");
            return (0);
        }
        /* start off assuming nothing hit */
        if (r.rmax > FVECT.FTINY) {		/* except aft plane if one */
            r.ro = Aftplane;
            r.rot = r.rmax;
            FVECT.VSUM(r.rop, r.rorg, r.rdir, r.rot);
        }
        /* find global cube entrance point */
        t = 0.0;
        if (OCTREE.incube(scene, curpos) == 0) {
            /* find distance to entry */
            for (i = 0; i < 3; i++) {
                /* plane in our direction */
                if ((sflags & 1 << i) != 0) {
                    dt = scene.cuorg.data[i];
                } else if ((sflags & 0x10 << i) != 0) {
                    dt = scene.cuorg.data[i] + scene.cusize;
                } else {
                    continue;
                }
                /* distance to the plane */
                dt = (dt - r.rorg.data[i]) / r.rdir.data[i];
                if (dt > t) {
                    t = dt;	/* farthest face is the one */
                }
            }
            t += FVECT.FTINY;		/* fudge to get inside cube */
            if (t >= r.rot) /* clipped already */ {
                return (0);
            }
            /* advance position */
            FVECT.VSUM(curpos, curpos, r.rdir, t);

            if (OCTREE.incube(scene, curpos) == 0) /* non-intersecting ray */ {
                return (0);
            }
        }
        cxset[0] = 0;
        raymove(curpos, cxset, sflags, r, scene);
        return ((r.ro != null) && (r.ro != Aftplane) ? 1 : 0);
    }

    static int raymove( /* check for hit as we move */
            FVECT pos, /* current position, modified herein */
            int[] cxs, /* checked objects, modified by checkhit */
            int dirf, /* direction indicators to speed tests */
            RAY r,
            CUBE cu) {
        int ax = 0;
        double dt, t;

        if (OCTREE.istree(cu.cutree) != 0) {		/* recurse on subcubes */
            CUBE cukid = new CUBE();
            int br, sgn;

            cukid.cusize = cu.cusize * 0.5;	/* find subcube */
            FVECT.VCOPY(cukid.cuorg, cu.cuorg);
            br = 0;
            if (pos.data[0] >= cukid.cuorg.data[0] + cukid.cusize) {
                cukid.cuorg.data[0] += cukid.cusize;
                br |= 1;
            }
            if (pos.data[1] >= cukid.cuorg.data[1] + cukid.cusize) {
                cukid.cuorg.data[1] += cukid.cusize;
                br |= 2;
            }
            if (pos.data[2] >= cukid.cuorg.data[2] + cukid.cusize) {
                cukid.cuorg.data[2] += cukid.cusize;
                br |= 4;
            }
            for (;;) {
                cukid.cutree = OCTREE.octkid(cu.cutree, br);
                if ((ax = raymove(pos, cxs, dirf, r, cukid)) == RAYHIT) {
                    return (RAYHIT);
                }
                sgn = 1 << ax;
                if ((sgn & dirf) != 0) /* positive axis? */ {
                    if ((sgn & br) != 0) {
                        return (ax);	/* overflow */
                    } else {
                        cukid.cuorg.data[ax] += cukid.cusize;
                        br |= sgn;
                    }
                } else if ((sgn & br) != 0) {
                    cukid.cuorg.data[ax] -= cukid.cusize;
                    br &= ~sgn;
                } else {
                    return (ax);	/* underflow */
                }
            }
            /*NOTREACHED*/
        }
        if (OCTREE.isfull(cu.cutree) != 0) {
            if (checkhit(r, cu, cxs) != 0) {
                return (RAYHIT);
            }
        } else if (r.ro == Aftplane && OCTREE.incube(cu, r.rop) != 0) {
            return (RAYHIT);
        }
        /* advance to next cube */
        if ((dirf & 0x11) != 0) {
            dt = ((dirf & 1) != 0) ? cu.cuorg.data[0] + cu.cusize : cu.cuorg.data[0];
            t = (dt - pos.data[0]) / r.rdir.data[0];
            ax = 0;
        } else {
            t = FVECT.FHUGE;
        }
        if ((dirf & 0x22) != 0) {
            dt = ((dirf & 2) != 0) ? cu.cuorg.data[1] + cu.cusize : cu.cuorg.data[1];
            dt = (dt - pos.data[1]) / r.rdir.data[1];
            if (dt < t) {
                t = dt;
                ax = 1;
            }
        }
        if ((dirf & 0x44) != 0) {
            dt = ((dirf & 4) != 0) ? cu.cuorg.data[2] + cu.cusize : cu.cuorg.data[2];
            dt = (dt - pos.data[2]) / r.rdir.data[2];
            if (dt < t) {
                t = dt;
                ax = 2;
            }
        }
        FVECT.VSUM(pos, pos, r.rdir, t);
        return (ax);
    }

    static int checkhit( /* check for hit in full cube */
            RAY r,
            OCTREE.CUBE cu,
            int[] cxs) {
        int[] oset = new int[OBJECT.MAXSET + 1];

        OBJSET.objset(oset, cu.cutree);
        checkset(oset, cxs);			/* avoid double-checking */

        r.hitf.rayhit(oset, r);
//	(*r.hitf)(oset, r);			/* test for hit in set */

        if (r.robj == OBJECT.OVOID) {
            return (0);			/* no scores yet */
        }

        return (OCTREE.incube(cu, r.rop));		/* hit OK if in current cube */
    }

    static void checkset( /* modify checked set and set to check */
            int[] os, /* os' = os - cs */
            int[] cs /* cs' = cs + os */) {
        int[] cset = new int[MAXCSET + OBJECT.MAXSET + 1];
        int i, j;
        int k;
        /* copy os in place, cset <- cs */
        cset[0] = 0;
        k = 0;
        for (i = j = 1; i <= os[0]; i++) {
            while (j <= cs[0] && cs[j] < os[i]) {
                cset[++cset[0]] = cs[j++];
            }
            if (j > cs[0] || os[i] != cs[j]) {	/* object to check */
                os[++k] = os[i];
                cset[++cset[0]] = os[i];
            }
        }
        if ((os[0] = k) == 0) /* new "to check" set size */ {
            return;			/* special case */
        }
        while (j <= cs[0]) /* get the rest of cs */ {
            cset[++cset[0]] = cs[j++];
        }
        if (cset[0] > MAXCSET) /* truncate "checked" set if nec. */ {
            cset[0] = MAXCSET;
        }
        /* setcopy(cs, cset); */	/* copy cset back to cs */
        os = cset;
        int idx = 0;
        for (i = os[0]; i-- >= 0;) {
            cs[idx] = os[idx];
            idx++;
        }
    }
}
