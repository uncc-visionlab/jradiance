/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.rt;

import java.util.logging.Level;
import java.util.logging.Logger;
import jradiance.common.FVECT;
import jradiance.common.OBJECT;
import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OBJSET;
import jradiance.common.OTYPES;
import jradiance.rt.SOURCE.OBSCACHE;
import jradiance.rt.SOURCE.SRCREC;

/**
 *
 * @author arwillis
 */
public class SRCOBSTR {

    /*
     * Source occlusion caching routines
     */

    public static double ABS(double x) {
        return ((x) > 0 ? (x) : -(x));
    }
//#if  SHADCACHE			/* preemptive shadow checking */
    public static int[] antimodlist = null;	/* set of clipped materials */


    static int /* cast source ray to first blocker */ castshadow(int sn, FVECT rorg, FVECT rdir) {
        RAY rt = new RAY();

        FVECT.VCOPY(rt.rorg, rorg);
        FVECT.VCOPY(rt.rdir, rdir);
        rt.rmax = 0;
        RAYTRACE.rayorigin(rt, RAY.PRIMARY, null, null);
        /* check for intersection */
        while (RAYTRACE.localhit(rt, RAYCALLS.thescene) != 0) {
            RAY rt1 = rt.copy();	/* pretend we were aimed at source */
            rt1.crtype |= rt1.rtype = RAY.SHADOW;
            rt1.rdir.data[0] = -rt.rdir.data[0];
            rt1.rdir.data[1] = -rt.rdir.data[1];
            rt1.rdir.data[2] = -rt.rdir.data[2];
            rt1.rod = -rt.rod;
            FVECT.VSUB(rt1.rorg, rt.rop, rt.rdir);
            rt1.rot = 1.;
            rt1.rsrc = sn;
            /* record blocker */
            if (srcblocker(rt1) != 0) {
                return (1);
            }
            /* move past failed blocker */
            FVECT.VSUM(rt.rorg, rt.rop, rt.rdir, FVECT.FTINY);
            RAYTRACE.rayclear(rt);		/* & try again... */
        }
        return (0);			/* found no blockers */
    }

    public static void /* initialize occlusion cache */ initobscache(int sn) {
        SRCREC srcp = SRCSUPP.source[sn];
        int cachelen;
        FVECT rorg = new FVECT(), rdir = new FVECT();
        double d;
        int i, j, k;
        int ax = 0, ax1 = 0, ax2 = 0;

        if ((srcp.sflags & (SOURCE.SSKIP | SOURCE.SPROX | SOURCE.SSPOT | SOURCE.SVIRTUAL)) != 0) {
            return;			/* don't cache these */
        }
        if ((srcp.sflags & SOURCE.SDISTANT) != 0) {
            cachelen = 4 * SOURCE.SHADCACHE * SOURCE.SHADCACHE;
        } else if ((srcp.sflags & SOURCE.SFLAT) != 0) {
            cachelen = SOURCE.SHADCACHE * SOURCE.SHADCACHE * 3 + (SOURCE.SHADCACHE & 1) * SOURCE.SHADCACHE * 4;
        } else /* spherical distribution */ {
            cachelen = SOURCE.SHADCACHE * SOURCE.SHADCACHE * 6;
        }
        /* allocate cache */
        srcp.obscache = new OBSCACHE();
        srcp.obscache.allocache(cachelen);
        if (srcp.obscache == null) {
//		error(SYSTEM, "out of memory in initobscache()");
        }
        /* set parameters */
        if ((srcp.sflags & SOURCE.SDISTANT) != 0) {
            double amax = 0;
            for (ax1 = 3; ax1-- != 0;) {
                if (ABS(srcp.sloc.data[ax1]) > amax) {
                    amax = ABS(srcp.sloc.data[ax1]);
                    ax = ax1;
                }
            }
            srcp.obscache.p.d.ax = ax;
            ax1 = (ax + 1) % 3;
            ax2 = (ax + 2) % 3;
            FVECT.VCOPY(srcp.obscache.p.d.o, RAYCALLS.thescene.cuorg);
            if (srcp.sloc.data[ax] > 0) {
                srcp.obscache.p.d.o.data[ax] += RAYCALLS.thescene.cusize;
            }
            if (srcp.sloc.data[ax1] < 0) {
                srcp.obscache.p.d.o.data[ax1] += RAYCALLS.thescene.cusize
                        * srcp.sloc.data[ax1] / amax;
            }
            if (srcp.sloc.data[ax2] < 0) {
                srcp.obscache.p.d.o.data[ax2] += RAYCALLS.thescene.cusize
                        * srcp.sloc.data[ax2] / amax;
            }
            srcp.obscache.p.d.e1 = 1. / (RAYCALLS.thescene.cusize * (1.
                    + Math.abs(srcp.sloc.data[ax1]) / amax));
            srcp.obscache.p.d.e2 = 1. / (RAYCALLS.thescene.cusize * (1.
                    + Math.abs(srcp.sloc.data[ax2]) / amax));
        } else if ((srcp.sflags & SOURCE.SFLAT) != 0) {
            FVECT.VCOPY(srcp.obscache.p.f.u, srcp.ss[SOURCE.SU]);
            FVECT.normalize(srcp.obscache.p.f.u);
            FVECT.fcross(srcp.obscache.p.f.v,
                    srcp.snorm(), srcp.obscache.p.f.u);
        }
        /* clear cache */
        for (i = cachelen; i-- != 0;) {
            srcp.obscache.obs[i] = OBJECT.OVOID;
        }
        /* cast shadow rays */
        if ((srcp.sflags & SOURCE.SDISTANT) != 0) {
            for (k = 3; k-- != 0;) {
                rdir.data[k] = -srcp.sloc.data[k];
            }
            for (i = 2 * SOURCE.SHADCACHE; i-- != 0;) {
                for (j = 2 * SOURCE.SHADCACHE; j-- != 0;) {
                    FVECT.VCOPY(rorg, srcp.obscache.p.d.o);
                    rorg.data[ax1] += (i + .5)
                            / (2 * SOURCE.SHADCACHE * srcp.obscache.p.d.e1);
                    rorg.data[ax2] += (j + .5)
                            / (2 * SOURCE.SHADCACHE * srcp.obscache.p.d.e2);
                    castshadow(sn, rorg, rdir);
                }
            }
        } else if ((srcp.sflags & SOURCE.SFLAT) != 0) {
            d = 0.01 * srcp.srad;
            FVECT.VSUM(rorg, srcp.sloc, srcp.snorm(), d);
            for (i = SOURCE.SHADCACHE; i-- != 0;) {
                for (j = SOURCE.SHADCACHE; j-- != 0;) {
                    d = 2. / SOURCE.SHADCACHE * (i + .5) - 1.;
                    FVECT.VSUM(rdir, srcp.snorm(),
                            srcp.obscache.p.f.u, d);
                    d = 2. / SOURCE.SHADCACHE * (j + .5) - 1.;
                    FVECT.VSUM(rdir, rdir, srcp.obscache.p.f.v, d);
                    FVECT.normalize(rdir);
                    castshadow(sn, rorg, rdir);
                }
            }
            for (k = 2; k-- != 0;) {
                for (i = SOURCE.SHADCACHE; i-- != 0;) {
                    for (j = SOURCE.SHADCACHE >> 1; j-- != 0;) {
                        d = 2. / SOURCE.SHADCACHE * (i + .5) - 1.;
                        if (k != 0) {
                            FVECT.VSUM(rdir, srcp.obscache.p.f.u,
                                    srcp.obscache.p.f.v, d);
                        } else {
                            FVECT.VSUM(rdir, srcp.obscache.p.f.v,
                                    srcp.obscache.p.f.u, d);
                        }
                        d = 1. - 2. / SOURCE.SHADCACHE * (j + .5);
                        FVECT.VSUM(rdir, rdir, srcp.snorm(), d);
                        FVECT.normalize(rdir);
                        castshadow(sn, rorg, rdir);
                        d = 2. * FVECT.DOT(rdir, srcp.snorm());
                        rdir.data[0] = d * srcp.snorm().data[0] - rdir.data[0];
                        rdir.data[1] = d * srcp.snorm().data[1] - rdir.data[1];
                        rdir.data[2] = d * srcp.snorm().data[2] - rdir.data[2];
                        castshadow(sn, rorg, rdir);
                    }
                }
            }
        } else /* spherical distribution */ {
            for (k = 6; k-- != 0;) {
                ax = k % 3;
                ax1 = (k + 1) % 3;
                ax2 = (k + 2) % 3;
                for (i = SOURCE.SHADCACHE; i-- != 0;) {
                    for (j = SOURCE.SHADCACHE; j-- != 0;) {
                        rdir.data[0] = rdir.data[1] = rdir.data[2] = 0.;
                        rdir.data[ax] = k < 3 ? 1. : -1.;
                        rdir.data[ax1] = 2. / SOURCE.SHADCACHE * (i + .5) - 1.;
                        rdir.data[ax2] = 2. / SOURCE.SHADCACHE * (j + .5) - 1.;
                        FVECT.normalize(rdir);
                        d = 1.05 * srcp.srad;
                        FVECT.VSUM(rorg, srcp.sloc, rdir, d);
                        castshadow(sn, rorg, rdir);
                    }
                }
            }
        }
    }
    static long lastrno = ~0;
    static int noobs;
    static int lastobjp;
    static int lastobjidx = OBJECT.OVOID;

    static int /* return occluder cache entry */ srcobstructp(RAY r) {
        SRCREC srcp;
        int ondx;

        noobs = OBJECT.OVOID;
        if (r.rno == lastrno) {
            return lastobjidx;	/* just recall last pointer */
//		return lastobjp;	/* just recall last pointer */
        }
//	DCHECK(r->rsrc < 0, CONSISTENCY,
//			"srcobstructp() called with unaimed ray");
        lastrno = r.rno;
//	lastobjp = noobs;
        lastobjidx = noobs;
        srcp = SRCSUPP.source[r.rsrc];
        if ((srcp.sflags & (SOURCE.SSKIP | SOURCE.SPROX | SOURCE.SSPOT | SOURCE.SVIRTUAL)) != 0) {
            return (lastobjidx);		/* don't cache these */
        }
        if (srcp.obscache == null) {    /* initialize cache */
            initobscache(r.rsrc);
        }
        /* compute cache index */
        if ((srcp.sflags & SOURCE.SDISTANT) != 0) {
            int ax, ax1, ax2;
            double t;
            ax = srcp.obscache.p.d.ax;
            if ((ax1 = ax + 1) >= 3) {
                ax1 -= 3;
            }
            if ((ax2 = ax + 2) >= 3) {
                ax2 -= 3;
            }
            t = (srcp.obscache.p.d.o.data[ax] - r.rorg.data[ax]) / srcp.sloc.data[ax];
            if (t <= FVECT.FTINY) {
                return (lastobjidx); /* could happen if ray is outside */
            }
            ondx = 2 * SOURCE.SHADCACHE * (int) (2 * SOURCE.SHADCACHE * srcp.obscache.p.d.e1
                    * (r.rorg.data[ax1] + t * srcp.sloc.data[ax1]
                    - srcp.obscache.p.d.o.data[ax1]));
            ondx += (int) (2 * SOURCE.SHADCACHE * srcp.obscache.p.d.e2
                    * (r.rorg.data[ax2] + t * srcp.sloc.data[ax2]
                    - srcp.obscache.p.d.o.data[ax2]));
            if ((ondx < 0) | (ondx >= 4 * SOURCE.SHADCACHE * SOURCE.SHADCACHE)) {
                return (lastobjidx); /* could happen if ray is outside */
            }
        } else if ((srcp.sflags & SOURCE.SFLAT) != 0) {
            FVECT sd = new FVECT();
            double sd0m, sd1m;
            sd.data[0] = -FVECT.DOT(r.rdir, srcp.obscache.p.f.u);
            sd.data[1] = -FVECT.DOT(r.rdir, srcp.obscache.p.f.v);
            sd.data[2] = -FVECT.DOT(r.rdir, srcp.snorm());
            if (sd.data[2] < 0) {
                return (lastobjidx); /* shouldn't happen */
            }
            sd0m = ABS(sd.data[0]);
            sd1m = ABS(sd.data[1]);
            if (sd.data[2] >= sd0m && sd.data[2] >= sd1m) {
                ondx = SOURCE.SHADCACHE * (int) (SOURCE.SHADCACHE * (.5 - FVECT.FTINY)
                        * (1. + sd.data[0] / sd.data[2]));
                ondx += (int) (SOURCE.SHADCACHE * (.5 - FVECT.FTINY)
                        * (1. + sd.data[1] / sd.data[2]));
            } else if (sd0m >= sd1m) {
                ondx = SOURCE.SHADCACHE * SOURCE.SHADCACHE;
                if (sd.data[0] < 0) {
                    ondx += ((SOURCE.SHADCACHE + 1) >> 1) * SOURCE.SHADCACHE;
                }
                ondx += SOURCE.SHADCACHE * (int) (SOURCE.SHADCACHE * (.5 - FVECT.FTINY)
                        * (1. - sd.data[2] / sd0m));
                ondx += (int) (SOURCE.SHADCACHE * (.5 - FVECT.FTINY)
                        * (1. + sd.data[1] / sd0m));
            } else /* sd1m > sd0m */ {
                ondx = SOURCE.SHADCACHE * SOURCE.SHADCACHE
                        + ((SOURCE.SHADCACHE + 1) >> 1) * SOURCE.SHADCACHE * 2;
                if (sd.data[1] < 0) {
                    ondx += ((SOURCE.SHADCACHE + 1) >> 1) * SOURCE.SHADCACHE;
                }
                ondx += SOURCE.SHADCACHE * (int) (SOURCE.SHADCACHE * (.5 - FVECT.FTINY)
                        * (1. - sd.data[2] / sd1m));
                ondx += (int) (SOURCE.SHADCACHE * (.5 - FVECT.FTINY)
                        * (1. + sd.data[0] / sd1m));
            }
//		DCHECK((ondx < 0) | (ondx >= SOURCE.SHADCACHE*SOURCE.SHADCACHE*3 +
//				(SOURCE.SHADCACHE&1)*SOURCE.SHADCACHE*4), CONSISTENCY,
//				"flat source cache index out of bounds");
        } else /* spherical distribution */ {
            int ax = 0, ax1, ax2;
            double amax = 0;
            for (ax1 = 3; ax1-- != 0;) {
                if (ABS(r.rdir.data[ax1]) > amax) {
                    amax = ABS(r.rdir.data[ax1]);
                    ax = ax1;
                }
            }
            if ((ax1 = ax + 1) >= 3) {
                ax1 -= 3;
            }
            if ((ax2 = ax + 2) >= 3) {
                ax2 -= 3;
            }
            ondx = 2 * SOURCE.SHADCACHE * SOURCE.SHADCACHE * ax;
            if (r.rdir.data[ax] < 0) {
                ondx += SOURCE.SHADCACHE * SOURCE.SHADCACHE;
            }
            ondx += SOURCE.SHADCACHE * (int) (SOURCE.SHADCACHE * (.5 - FVECT.FTINY)
                    * (1. + r.rdir.data[ax1] / amax));
            ondx += (int) (SOURCE.SHADCACHE * (.5 - FVECT.FTINY)
                    * (1. + r.rdir.data[ax2] / amax));
//		DCHECK((ondx < 0) | (ondx >= SOURCE.SHADCACHE*SOURCE.SHADCACHE*6), CONSISTENCY,
//				"radial source cache index out of bounds");
        }
        lastobjp = srcp.obscache.obs[ondx];				/* return cache pointer */
        lastobjidx = ondx;
        return (lastobjidx);
    }

    public static void /* free obstruction cache */ freeobscache(SRCREC srcp) {
        if (srcp.obscache == null) {
            return;
        }
        srcp.obscache = null;
//        free((void *)srcp->obscache);
//	srcp->obscache = NULL;
    }

    static int /* record a source blocker */ srcblocker(RAY r) {
        OBJREC m;

        if (r.robj == OBJECT.OVOID || OBJECT.objptr(r.robj) != r.ro
                || OTYPES.isvolume(r.ro.otype) != 0) {
            return (0);		/* don't record complex blockers */
        }
        if (r.rsrc < 0 || SRCSUPP.source[r.rsrc].so == r.ro) {
            return (0);		/* just a mistake, that's all */
        }
	if (antimodlist != null && OBJSET.inset(antimodlist, r.ro.omod) != 0) {
		return(0);		/* could be clipped */
        }
        m = SOURCE.findmaterial(r.ro);
        if (m == null) {
            return (0);		/* no material?! */
        }
        if (OTSPECIAL.isopaque(m.otype) == 0) {
            return (0);		/* material not a reliable blocker */
        }
        int index = srcobstructp(r);
        if (index > 0) {
        SRCREC srcp = SRCSUPP.source[r.rsrc];
        srcp.obscache.obs[index] = r.robj;     /* else record obstructor */
        }
//	*srcobstructp(r) = r->robj;     /* else record obstructor */
        return (1);
    }

    static int /* check ray against cached blocker */ srcblocked(RAY r) {
        int cacheidx = srcobstructp(r);
        int obs = OBJECT.OVOID;
        if (cacheidx > 0) {
            obs = SRCSUPP.source[r.rsrc].obscache.obs[cacheidx];
        }
        System.out.print("obs "+obs+" ");
        OBJREC op;

        if (obs == OBJECT.OVOID) {
            return (0);
        }
        op = OBJECT.objptr(obs);		/* check blocker intersection */
        if (OTYPES.ofun[op.otype].ofunc.octree_function(op, r) == 0) {
//	if (!(*ofun[op.otype].funp)(op, r))
            return (0);
        }
        if ((SRCSUPP.source[r.rsrc].sflags & SOURCE.SDISTANT) != 0) {
            return (1);
        }
        op = SRCSUPP.source[r.rsrc].so;	/* check source intersection */
        if (OTYPES.ofun[op.otype].ofunc.octree_function(op, r) == 0) {
//	if (!(*ofun[op.otype].funp)(op, r))
            return (1);
        }
        RAYTRACE.rayclear(r);
        return (0);			/* source in front */
    }

    public static void /* record potentially clipped materials */ markclip(OBJREC m) {
        int[] set2add, oldset;

        if (m == null) {		/* starting over */
            if (antimodlist != null) {
//			free((void *)antimodlist);
            }
            antimodlist = null;
            return;
        }
        M_CLIP.m_clip(m, null);		/* initialize modifier list */
        if ((set2add = m.os.getOS()) == null || set2add[0] == 0) {
            return;
        }

        if (antimodlist == null) {	/* start of list */
            antimodlist = OBJSET.setsave(set2add);
            return;
        }
        /* else add to previous list */
        oldset = antimodlist;
        antimodlist = new int[(oldset[0] + set2add[0] + 1)];
        if (antimodlist == null) {
//		error(SYSTEM, "out of memory in markclip");
        }
        OBJSET.setunion(antimodlist, oldset, set2add);
//	free((void *)oldset);
    }
//#else	/* SHADCACHE */
//
//
//void				/* no-op also avoids linker warning */
//markclip(OBJREC *m)
//{
//	(void)m;
//}
//
//
//#endif  /* SHADCACHE */
}
