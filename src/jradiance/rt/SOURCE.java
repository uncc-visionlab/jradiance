/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.rt;

import java.util.Arrays;
import java.util.Comparator;
import jradiance.common.COLORS.COLOR;
import jradiance.common.FVECT;
import jradiance.common.MODOBJECT;
import jradiance.common.OBJECT;
import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OBJECT_STRUCTURE;
import jradiance.common.OCTREE;
import jradiance.common.OTYPES;
import jradiance.rt.RAYTRACE.RAYTRACER;
import jradiance.rt.SRCSUPP.SSOBJ;

/**
 *
 * @author arwillis
 */
public class SOURCE {
    /*
     *  source.h - header file for ray tracing sources.
     *
     *  Include after ray.h
     */

    //#ifndef  AIMREQT
    public static final int AIMREQT = 100;		/* required aim success/failure */
//#endif
//#ifndef  SHADCACHE

    public static final int SHADCACHE = 20;		/* shadow cache resolution */
//#endif

    public static final int SDISTANT = 01;		/* source distant flag */

    public static final int SSKIP = 02;		/* source skip flag */

    public static final int SPROX = 04;		/* source proximity flag */

    public static final int SSPOT = 010;		/* source spotlight flag */

    public static final int SVIRTUAL = 020;		/* source virtual flag */

    public static final int SFLAT = 040;		/* source flat flag */

    public static final int SCIR = 0100;		/* source circular flag */

    public static final int SCYL = 0200;		/* source cylindrical flag */

    public static final int SFOLLOW = 0400;		/* source follow path flag */


    public static class SPOT extends OBJECT_STRUCTURE {

        FVECT aim = new FVECT();		/* aim direction or center */

        float siz;		/* output solid angle or area */

        float flen;		/* focal length (negative if distant source) */


        @Override
        public int octree_function(Object... objs) {
            return OCTREE.O_MISS;
        }
    }			/* spotlight */


    public static class OBSCACHE {

        public class P {

            public class F { /* flat source indexing */


                FVECT u = new FVECT(), v = new FVECT();/* unit vectors */

            }

            public class D {

                FVECT o = new FVECT();/* origin position */

                double e1, e2; /* 1/extent */

                int ax;/* major direction */

            } /* distant source indexing */

            F f = new F();
            D d = new D();
        }
        P p = new P(); /* indexing parameters */

        int[] obs = new int[1];		/* cache obstructors (extends struct) */


        public OBSCACHE() {
        }

        public void allocache(int cachelen) {
            obs = new int[cachelen];
        }
    }  		/* obstructor cache */


    public static class SRCREC {

        FVECT sloc = new FVECT();		/* direction or position of source */

        FVECT[] ss = {new FVECT(), new FVECT(), new FVECT()};		/* source dimension vectors, U, V, and W */

        float srad;		/* maximum source radius */

        float ss2;		/* solid angle or projected area */

        OBJREC so = new OBJREC();		/* source destination object */


        public class SL {

            float prox;		/* proximity */

            SPOT s = new SPOT();		/* spot */

        }
        SL sl = new SL();
        /* localized source information */

        public class SA {

            long success;		/* successes - AIMREQT*failures */


            public class SV {

                short pn;		/* projection number */

                int sn;		/* next source to aim for */

            } 			/* virtual source */

            SV sv = new SV();
        } 			/* source aiming information */

        SA sa = new SA();
        long ntests, nhits;	/* shadow tests and hits */
//#ifdef  SHADCACHE

        OBSCACHE obscache = null;    /* obstructor cache */
//#endif

        int sflags;		/* source flags */


        public FVECT snorm() {
            return ss[SW];		/* normal vector for flat source */
        }
    }		/* light source */

    public static final int MAXSPART = 64;		/* maximum partitions per source */

    public static final int SU = 0;		/* U vector or partition */

    public static final int SV = 1;		/* V vector or partition */

    public static final int SW = 2;		/* W vector or partition */

    public static final int S0 = 3;		/* leaf partition */


    public static class SRCINDEX {

        double dom;				/* solid angle of partition */

        int sn;				/* source number */

        short np;				/* number of partitions */

        short sp;				/* this partition number */

        char[] spt = new char[MAXSPART / 2];		/* source partitioning */

    }  		/* source index structure */

//#define initsrcindex(s)	((s)->sn = (s)->sp = -1, (s)->np = 0)

    public static void initsrcindex(SRCINDEX s) {
        (s).sn = -1;
        (s).sp = -1;
        (s).np = 0;
    }
    //    
//#define clrpart(pt)	memset((char *)(pt), '\0', MAXSPART/2)

    public static void clrpart(char[] pt) {
        Arrays.fill(pt, '\0');
    }
    //#define setpart(pt,i,v)	((pt)[(i)>>2] |= (v)<<(((i)&3)<<1))

    public static void setpart(char[] pt, int i, int v) {
        (pt)[(i) >> 2] |= (v) << (((i) & 3) << 1);
    }
//#define spart(pt,pi)	((pt)[(pi)>>2] >> (((pi)&3)<<1) & 3)

    public static int spart(char[] pt, int pi) {
        return (pt)[(pi) >> 2] >> (((pi) & 3) << 1) & 3;
    }
    /*
     * Special support functions for sources
     */

    /*
     * Virtual source materials must define the following.
     *
     *	vproj(pm, op, sp, i)	Compute i'th virtual projection
     *				of source sp in object op and assign
     *				the 4x4 transformation matrix pm.
     *				Return 1 on success, 0 if no i'th projection.
     *
     *	nproj			The number of projections.  The value of
     *				i passed to vproj runs from 0 to nproj-1.
     */
    public static abstract class VSMATERIAL {

        abstract int vproj();	/* project virtual sources */

        int nproj;		/* number of possible projections */

    }		/* virtual source material functions */


    public static interface SOBJECT {

        void setsrc(SRCREC s, OBJREC o); /* set light source for object */


        void partit(SRCINDEX si, RAY r); /* partition light source object */


        double getpleq(FVECT nvec, OBJREC op); /* plane equation for surface */


        double getdisk(FVECT ocent, OBJREC op); /* maximum disk for surface */
//	void	(*setsrc)();	/* set light source for object */
//	void	(*partit)();	/* partition light source object */
//	double  (*getpleq)();	/* plane equation for surface */
//	double  (*getdisk)();	/* maximum disk for surface */

    }		/* source object functions */


    public static class SRCFUNC {

        VSMATERIAL mf;      /* material functions */

        SOBJECT of;		/* object functions */

    }		/* source functions */

//extern SRCFUNC  sfun[];			/* source dispatch table */
//
//extern SRCREC  *source;			/* our source list */
//extern int  nsources;			/* the number of sources */

//#define  sflatform(sn,dir)	-DOT(source[sn].snorm, dir)
//
    public static double sflatform(int sn, FVECT dir) {
        return -FVECT.DOT(SRCSUPP.source[sn].snorm(), dir);
    }

//#define  getplaneq(c,o)		(*sfun[(o)->otype].of->getpleq)(c,o)
//#define  getmaxdisk(c,o)	(*sfun[(o)->otype].of->getdisk)(c,o)
//#define  setsource(s,o)		(*sfun[(o)->otype].of->setsrc)(s,o)

    /* defined in source.c */
//extern OBJREC   *findmaterial(OBJREC *o);
//extern void	marksources(void);
//extern void	freesources(void);
//extern int	srcray(RAY *sr, RAY *r, SRCINDEX *si);
//extern void	srcvalue(RAY *r);
//extern int	sourcehit(RAY *r);
//typedef void srcdirf_t(COLOR cv, void *np, FVECT ldir, double omega);
    public static interface srcdirf_t {

        public void dirf(COLOR cv, Object np, FVECT ldir, double omega);
    }
//extern void	direct(RAY *r, srcdirf_t *f, void *p);
//extern void	srcscatter(RAY *r);
//extern int	m_light(OBJREC *m, RAY *r);
//					/* defined in srcobstr.c */
//extern void	initobscache(int sn);
//extern int	srcblocker(RAY *r);
//extern int      srcblocked(RAY *r);
//extern void     freeobscache(SRCREC *s);
//extern void	markclip(OBJREC *m);
//					/* defined in srcsamp.c */
//extern double	nextssamp(RAY *r, SRCINDEX *si);
//extern int	skipparts(int ct[3], int sz[3], int pp[2], unsigned char *pt);
//extern void	nopart(SRCINDEX *si, RAY *r);
//extern void	cylpart(SRCINDEX *si, RAY *r);
//extern void	flatpart(SRCINDEX *si, RAY *r);
//extern double	scylform(int sn, FVECT dir);
//					/* defined in srcsupp.c */
//extern void	initstypes(void);
//extern int	newsource(void);
//extern void	setflatss(SRCREC *src);
//extern void	fsetsrc(SRCREC *src, OBJREC *so);
//extern void	ssetsrc(SRCREC *src, OBJREC *so);
//extern void	sphsetsrc(SRCREC *src, OBJREC *so);
//extern void	rsetsrc(SRCREC *src, OBJREC *so);
//extern void	cylsetsrc(SRCREC *src, OBJREC *so);
//extern SPOT	*makespot(OBJREC *m);
//extern int	spotout(RAY *r, SPOT *s);
//extern double	fgetmaxdisk(FVECT ocent, OBJREC *op);
//extern double	rgetmaxdisk(FVECT ocent, OBJREC *op);
//extern double	fgetplaneq(FVECT nvec, OBJREC *op);
//extern double	rgetplaneq(FVECT nvec, OBJREC *op);
//extern int	commonspot(SPOT *sp1, SPOT *sp2, FVECT org);
//extern int	commonbeam(SPOT *sp1, SPOT *sp2, FVECT org);
//extern int	checkspot(SPOT *sp, FVECT nrm);
//extern double	spotdisk(FVECT oc, OBJREC *op, SPOT *sp, FVECT pos);
//extern double	beamdisk(FVECT oc, OBJREC *op, SPOT *sp, FVECT dir);
//extern double	intercircle(FVECT cc, FVECT c1, FVECT c2,
//			double r1s, double r2s);
//					/* defined in virtuals.c */
//extern void	markvirtuals(void);
//extern void	addvirtuals(int sn, int nr);
//extern void	vproject(OBJREC *o, int sn, int n);
//extern OBJREC	*vsmaterial(OBJREC *o);
//extern int	makevsrc(OBJREC *op, int sn, MAT4 pm);
//extern double	getdisk(FVECT oc, OBJREC *op, int sn);
//extern int	vstestvis(int f, OBJREC *o, FVECT oc, double or2, int sn);
//extern void	virtverb(int sn, FILE *fp);
//
//
//#ifdef __cplusplus
//}
//#endif
//#endif /* _RAD_SOURCE_H_ */
//#ifndef lint
//static const char RCSid[] = "$Id$";
//#endif
/*
     *  source.c - routines dealing with illumination sources.
     *
     *  External symbols declared in source.h
     */
//#include  "ray.h"
//#include  "otypes.h"
//#include  "rtotypes.h"
//#include  "source.h"
//#include  "random.h"
//extern double  ssampdist;		/* scatter sampling distance */
//#ifndef MAXSSAMP
    public static final int MAXSSAMP = 16;		/* maximum samples per ray */
//#endif

    /*
     * Structures used by direct()
     */

    public static class CONTRIB {

        int sno;		/* source number */

        FVECT dir = new FVECT();		/* source direction */

        COLOR coef = new COLOR();		/* material coefficient */

        COLOR val = new COLOR();		/* contribution */

    }		/* direct contribution */


    public static class CNTPTR implements Comparable, Comparator {

        int sndx;		/* source index (to CONTRIB array) */

        float brt;		/* brightness (for comparison) */


        @Override
        public int compare( /* decreasing order */
                Object p1,
                Object p2) {
            CNTPTR sc1 = (CNTPTR) p1;
            CNTPTR sc2 = (CNTPTR) p2;

            if (sc1.brt > sc2.brt) {
                return (-1);
            }
            if (sc1.brt < sc2.brt) {
                return (1);
            }
            return (0);
        }

        @Override
        public int compareTo(Object p2) {
            CNTPTR sc2 = (CNTPTR) p2;

            if (brt > sc2.brt) {
                return (-1);
            }
            if (brt < sc2.brt) {
                return (1);
            }
            return (0);
        }
    } 		/* contribution pointer */

    static CONTRIB[] srccnt;		/* source contributions in direct() */

    static CNTPTR[] cntord;			/* source ordering in direct() */

    static int maxcntr = 0;		/* size of contribution arrays */

//static int cntcmp(const void *p1, const void *p2);

    public static OBJREC /* find an object's actual material */ findmaterial(OBJREC o) {
        while (OTYPES.ismaterial(o.otype) == 0) {
            if (o.otype == OTYPES.MOD_ALIAS && o.oargs.nsargs != 0) {
                int aobj;
                OBJREC ao;
                aobj = MODOBJECT.lastmod(MODOBJECT.objndx(o), o.oargs.sarg[0]);
                if (aobj < 0) {
//				objerror(o, USER, "bad reference");
                }
                ao = OBJECT.objptr(aobj);
                if (OTYPES.ismaterial(ao.otype) != 0) {
                    return (ao);
                }
                if (ao.otype == OTYPES.MOD_ALIAS) {
                    o = ao;
                    continue;
                }
            }
            if (o.omod == OBJECT.OVOID) {
                return (null);
            }
            o = OBJECT.objptr(o.omod);
        }
        return (o);		/* mixtures will return NULL */
    }

    public static void marksources() /* find and mark source objects */ {
        int foundsource = 0;
        int i;
        OBJREC o, m;
        int ns;
        /* initialize dispatch table */
        SRCSUPP.initstypes();
        /* find direct sources */
        for (i = 0; i < RAYCALLS.nsceneobjs; i++) {

            o = OBJECT.objptr(i);

            if (OTYPES.issurface(o.otype) == 0 || o.omod == OBJECT.OVOID) {
                continue;
            }
            /* find material */
            m = SOURCE.findmaterial(OBJECT.objptr(o.omod));
            if (m == null) {
                continue;
            }
            if (m.otype == OTYPES.MAT_CLIP) {
//			markclip(m);	/* special case for antimatter */
                continue;
            }
            if (OTYPES.islight(m.otype) == 0) {
                continue;	/* not source modifier */
            }
            if (m.oargs.nfargs != (m.otype == OTYPES.MAT_GLOW ? 4
                    : m.otype == OTYPES.MAT_SPOT ? 7 : 3)) {
//			objerror(m, USER, "bad # arguments");
            }

            if (m.oargs.farg[0] <= FVECT.FTINY && m.oargs.farg[1] <= FVECT.FTINY
                    && m.oargs.farg[2] <= FVECT.FTINY) {
                continue;			/* don't bother */
            }
            if (m.otype == OTYPES.MAT_GLOW
                    && o.otype != OTYPES.OBJ_SOURCE
                    && m.oargs.farg[3] <= FVECT.FTINY) {
                foundsource += (RPICT.ambounce > 0) ? 1 : 0;
                continue;			/* don't track these */
            }
            if (SRCSUPP.sfun[o.otype].of == null) {
//                      || SRCSUPP.sfun[o.otype].of.setsrc == NULL)
//			objerror(o, USER, "illegal material");
            }

            if ((ns = SRCSUPP.newsource()) < 0) {
//			goto memerr;
                System.exit(0);
            }
//		setsource(&source[ns], o);
            SRCSUPP.sfun[o.otype].of.setsrc(SRCSUPP.source[ns], o);
//#define  setsource(s,o)		(*sfun[(o)->otype].of->setsrc)(s,o)

            if (m.otype == OTYPES.MAT_GLOW) {
                SRCSUPP.source[ns].sflags |= SPROX;
                SRCSUPP.source[ns].sl.prox = (float) m.oargs.farg[3];
                if ((SRCSUPP.source[ns].sflags & SDISTANT) != 0) {
                    SRCSUPP.source[ns].sflags |= SSKIP;
                    foundsource += (RPICT.ambounce > 0) ? 1 : 0;
                }
            } else if (m.otype == OTYPES.MAT_SPOT) {
                SRCSUPP.source[ns].sflags |= SSPOT;
                if ((SRCSUPP.source[ns].sl.s = SRCSUPP.makespot(m)) == null) {
//				goto memerr;
                    System.exit(1);
                }
                if ((SRCSUPP.source[ns].sflags & SFLAT) != 0
                        && SRCSUPP.checkspot(SRCSUPP.source[ns].sl.s, SRCSUPP.source[ns].snorm()) == 0) {
//				objerror(o, WARNING,
//					"invalid spotlight direction");
                    SRCSUPP.source[ns].sflags |= SSKIP;
                }
            }
//#if  SHADCACHE
            if (SOURCE.SHADCACHE > 0) {
                SRCOBSTR.initobscache(ns);
            }
//#endif
            foundsource += (SRCSUPP.source[ns].sflags & SSKIP) == 0 ? 1 : 0;
        }
        if (foundsource == 0) {
//		error(WARNING, "no light sources found");
            return;
        }
        VIRTUALS.markvirtuals();			/* find and add virtual sources */
        /* allocate our contribution arrays */
        maxcntr = SRCSUPP.nsources + MAXSPART;	/* start with this many */
        srccnt = new CONTRIB[maxcntr];
        for (i = 0; i < srccnt.length; i++) {
            srccnt[i] = new CONTRIB();
        }
        cntord = new CNTPTR[maxcntr];
        for (i = 0; i < cntord.length; i++) {
            cntord[i] = new CNTPTR();
        }
//	srccnt = (CONTRIB *)malloc(maxcntr*sizeof(CONTRIB));
//	cntord = (CNTPTR *)malloc(maxcntr*sizeof(CNTPTR));
        if ((srccnt == null) | (cntord == null)) {
//		goto memerr;
            System.exit(1);
        }
        return;
//memerr:
//	error(SYSTEM, "out of memory in marksources");
    }

    public static void freesources() /* free all source structures */ {
        if (SRCSUPP.nsources > 0) {
//#if SHADCACHE
            if (SOURCE.SHADCACHE > 0) {
                while (SRCSUPP.nsources-- != 0) {
                    SRCOBSTR.freeobscache(SRCSUPP.source[SRCSUPP.nsources]);
                }
            }
//#endif
//		free((void *)source);                
            SRCSUPP.source = null;
            SRCSUPP.nsources = 0;
        }
        SRCOBSTR.markclip(null);
        if (maxcntr <= 0) {
            return;
        }
//	free((void *)srccnt);
        srccnt = null;
//	free((void *)cntord);
        cntord = null;
        maxcntr = 0;
    }

    static int srcray( /* send a ray to a source, return domega */
            RAY sr, /* returned source ray */
            RAY r, /* ray which hit object */
            SRCINDEX si /* source sample index */) {
        double d;				/* distance to source */
        SRCREC srcp;

        RAYTRACE.rayorigin(sr, RAY.SHADOW, r, null);		/* ignore limits */

        if (r == null) {
            sr.rmax = 0.0;
        }

        while ((d = SRCSAMP.nextssamp(sr, si)) != 0.0) {
            sr.rsrc = si.sn;			/* remember source */
            srcp = SRCSUPP.source[si.sn];
            if ((srcp.sflags & SDISTANT) != 0) {
                if ((srcp.sflags & SSPOT) != 0 && SRCSUPP.spotout(sr, srcp.sl.s) != 0) {
                    continue;
                }
                return (1);		/* sample OK */
            }
            /* local source */
            /* check proximity */
            if ((srcp.sflags & SPROX) != 0 && d > srcp.sl.prox) {
                continue;
            }
            /* check angle */
            if ((srcp.sflags & SSPOT) != 0) {
                if (SRCSUPP.spotout(sr, srcp.sl.s) != 0) {
                    continue;
                }
                /* adjust solid angle */
                si.dom *= d * d;
                d += srcp.sl.s.flen;
                si.dom /= d * d;
            }
            return (1);			/* sample OK */
        }
        return (0);			/* no more samples */
    }

    public static class SRCVALUE implements RAYTRACER {

        @Override
        public void raytrace(RAY r) {
            srcvalue(r);
        }

        void srcvalue( /* punch ray to source and compute value */
                RAY r) {
            SRCREC sp;

            sp = SRCSUPP.source[r.rsrc];
            if ((sp.sflags & SVIRTUAL) != 0) {	/* virtual source */
                /* check intersection */
                if (OTYPES.ofun[sp.so.otype].ofunc.octree_function(sp.so, r) == 0) {
                    return;
                }
                if (RAYTRACE.rayshade(r, r.ro.omod) == 0) {/* compute contribution */
//			goto nomat;

                }
                RAYTRACE.rayparticipate(r);
                return;
            }
            /* compute intersection */
            if (((sp.sflags & SDISTANT) != 0 ? sourcehit(r)
                    : (OTYPES.ofun[sp.so.otype].ofunc.octree_function(sp.so, r))) != 0) {
                if (sp.sa.success >= 0) {
                    sp.sa.success++;
                }
                if (RAYTRACE.rayshade(r, r.ro.omod) == 0) {/* compute contribution */
//			goto nomat;

                }
                RAYTRACE.rayparticipate(r);
                return;
            }
            /* we missed our mark! */
            if (sp.sa.success < 0) {
                return;			/* bitched already */
            }
            sp.sa.success -= AIMREQT;
            if (sp.sa.success >= 0) {
                return;			/* leniency */
            }
//	sprintf(errmsg, "aiming failure for light source \"%s\"",
//			sp.so.oname);
//	error(WARNING, errmsg);		/* issue warning */
            return;
//nomat:
//	objerror(r.ro, USER, "material not found");
        }
    }

    static int transillum( /* check if material is transparent illum */
            int obj) {
        OBJREC m = findmaterial(OBJECT.objptr(obj));

        if (m == null) {
            return (1);
        }
        if (m.otype != OTYPES.MAT_ILLUM) {
            return (0);
        }
        return (m.oargs.nsargs == 0 || DEVCOMM.strcmp(m.oargs.sarg[0].toCharArray(), OBJECT.VOIDID.toCharArray()) == 0) ? 1 : 0;
    }

    static int sourcehit( /* check to see if ray hit distant source */
            RAY r) {
        int glowsrc = -1;
        int transrc = -1;
        int first, last;
        int i;

        if (r.rsrc >= 0) {		/* check only one if aimed */
            first = last = r.rsrc;
        } else {			/* otherwise check all */
            first = 0;
            last = SRCSUPP.nsources - 1;
        }
        for (i = first; i <= last; i++) {
            if ((SRCSUPP.source[i].sflags & (SDISTANT | SVIRTUAL)) != SDISTANT) {
                continue;
            }
            /*
             * Check to see if ray is within
             * solid angle of source.
             */
            if (2. * Math.PI * (1. - FVECT.DOT(SRCSUPP.source[i].sloc, r.rdir)) > SRCSUPP.source[i].ss2) {
                continue;
            }
            /* is it the only possibility? */
            if (first == last) {
                r.ro = SRCSUPP.source[i].so;
                break;
            }
            /*
             * If it's a glow or transparent illum, just remember it.
             */
            if ((SRCSUPP.source[i].sflags & SSKIP) != 0) {
                if (glowsrc < 0) {
                    glowsrc = i;
                }
                continue;
            }
            if (transillum(SRCSUPP.source[i].so.omod) != 0) {
                if (transrc < 0) {
                    transrc = i;
                }
                continue;
            }
            r.ro = SRCSUPP.source[i].so;	/* otherwise, use first hit */
            break;
        }
        /*
         * Do we need fallback?
         */
        if (r.ro == null) {
            if (transrc >= 0 && (r.crtype & (RAY.AMBIENT | RAY.SPECULAR)) != 0) {
                return (0);	/* avoid overcounting */
            }
            if (glowsrc >= 0) {
                r.ro = SRCSUPP.source[glowsrc].so;
            } else {
                return (0);	/* nothing usable */
            }
        }
        /*
         * Make assignments.
         */
        r.robj = MODOBJECT.objndx(r.ro);
        for (i = 0; i < 3; i++) {
            r.ron.data[i] = -r.rdir.data[i];
        }
        r.rod = 1.0;
        r.pert.data[0] = r.pert.data[1] = r.pert.data[2] = 0.0;
        r.uv[0] = r.uv[1] = 0.0;
        r.rox = null;
        return (1);
    }

//static int
//cntcmp(				/* contribution compare (descending) */
//	const void *p1,
//	const void *p2
//)
//{
//	register const CNTPTR  *sc1 = (const CNTPTR *)p1;
//	register const CNTPTR  *sc2 = (const CNTPTR *)p2;
//
//	if (sc1->brt > sc2->brt)
//		return(-1);
//	if (sc1->brt < sc2->brt)
//		return(1);
//	return(0);
//}
    public static void direct( /* add direct component */
            RAY r, /* ray that hit surface */
            srcdirf_t f, /* direct component coefficient function */
            Object p /* data for f */) {
        int sn;
        CONTRIB scp = new CONTRIB();
        SRCINDEX si = new SRCINDEX();
        int nshadcheck, ncnts;
        int nhits;
        double prob, ourthresh, hwt;
        RAY sr = new RAY();
        /* NOTE: srccnt and cntord global so no recursion */
        if (SRCSUPP.nsources <= 0) {
            return;		/* no sources?! */
        }
        /* potential contributions */
        initsrcindex(si);
        for (sn = 0; srcray(sr, r, si) != 0; sn++) {
            if (sn >= maxcntr) {
                maxcntr = sn + MAXSPART;
                CONTRIB[] nsrccnt = new CONTRIB[maxcntr];
                System.arraycopy(srccnt, 0, nsrccnt, 0, srccnt.length);
                for (int i = srccnt.length; i < nsrccnt.length; i++) {
                    nsrccnt[i] = new CONTRIB();
                }
                srccnt = nsrccnt;
//			srccnt = (CONTRIB *)realloc((void *)srccnt,
//					maxcntr*sizeof(CONTRIB));
                CNTPTR[] ncntord = new CNTPTR[maxcntr];
                System.arraycopy(cntord, 0, ncntord, 0, cntord.length);
                for (int i = cntord.length; i < ncntord.length; i++) {
                    ncntord[i] = new CNTPTR();
                }
                cntord = ncntord;
//			cntord = (CNTPTR *)realloc((void *)cntord,
//					maxcntr*sizeof(CNTPTR));
                if ((srccnt == null) | (cntord == null)) {
//				error(SYSTEM, "out of memory in direct");
                }
            }
            cntord[sn].sndx = sn;
            scp = srccnt[sn];
            scp.sno = sr.rsrc;
            /* compute coefficient */
            f.dirf(scp.coef, p, sr.rdir, si.dom);
            cntord[sn].brt = (float) COLOR.intens(scp.coef);
            if (cntord[sn].brt <= 0.0) {
                continue;
            }
//#if SHADCACHE
            if (SOURCE.SHADCACHE > 0) {				/* check shadow cache */
                if (si.np == 1 && SRCOBSTR.srcblocked(sr) != 0) {
                    cntord[sn].brt = 0.0f;
                    continue;
                }
            }
//#endif
            FVECT.VCOPY(scp.dir, sr.rdir);
            COLOR.copycolor(sr.rcoef, scp.coef);
            /* compute potential */
            sr.revf = new SRCVALUE();//srcvalue;
            RAY.rayvalue(sr);
            COLOR.multcolor(sr.rcol, sr.rcoef);
            COLOR.copycolor(scp.val, sr.rcol);
            cntord[sn].brt = (float) COLOR.bright(sr.rcol);
        }
        /* sort contributions */
//	qsort(cntord, sn, sizeof(CNTPTR), cntcmp);
        Arrays.sort(cntord, 0, sn);
        for (int i = 0; i < sn; i++) {
            System.out.print(String.format("contrib[%d]={%3.3f,%d} ", i, cntord[i].brt, cntord[i].sndx));
        }

        {					/* find last */
            int l, m;

            ncnts = l = sn;
            sn = 0;
            while ((m = (sn + ncnts) >> 1) != l) {
                if (cntord[m].brt > 0.0) {
                    sn = m;
                } else {
                    ncnts = m;
                }
                l = m;
            }
        }
        if (ncnts == 0) {
            return;		/* no contributions! */
        }
        /* accumulate tail */
        for (sn = ncnts - 1; sn > 0; sn--) {
            cntord[sn - 1].brt += cntord[sn].brt;
        }
        /* compute number to check */
        nshadcheck = (int) (Math.pow((double) ncnts, RAYCALLS.shadcert) + .5);
        /* modify threshold */
        ourthresh = RAYCALLS.shadthresh / r.rweight;
        /* test for shadows */
        for (nhits = 0, hwt = 0.0, sn = 0; sn < ncnts;
                hwt += (double) SRCSUPP.source[scp.sno].nhits
                        / (double) SRCSUPP.source[scp.sno].ntests, sn++) {
            /* check threshold */
            if ((sn + nshadcheck >= ncnts ? cntord[sn].brt
                    : cntord[sn].brt - cntord[sn + nshadcheck].brt)
                    < ourthresh * COLOR.bright(r.rcol)) {
                break;
            }
            scp = srccnt[cntord[sn].sndx];
            /* test for hit */
            RAYTRACE.rayorigin(sr, RAY.SHADOW, r, null);
            COLOR.copycolor(sr.rcoef, scp.coef);
            FVECT.VCOPY(sr.rdir, scp.dir);
            sr.rsrc = scp.sno;
            /* keep statistics */
            if (SRCSUPP.source[scp.sno].ntests++ > (long) 0x7ffffff0) {
                //if (SRCSUPP.source[scp.sno].ntests++ > (long) 0xfffffff0) {
                System.out.println("UNSIGNED LONG POSSIBLE DEVIATION HERE");
                SRCSUPP.source[scp.sno].ntests >>= 1;
                SRCSUPP.source[scp.sno].nhits >>= 1;
            }
            if (RAYTRACE.localhit(sr, RAYCALLS.thescene) != 0
                    && (sr.ro != SRCSUPP.source[scp.sno].so
                    || (SRCSUPP.source[scp.sno].sflags & SFOLLOW) != 0)) {
                /* follow entire path */
                RAYTRACE.raycont(sr);
//			if (trace != NULL)
//				(*trace)(&sr);	/* trace execution */
                if (COLOR.bright(sr.rcol) <= FVECT.FTINY) {
//#if SHADCACHE
                    if (SOURCE.SHADCACHE > 0) {
//				if ((scp <= srccnt || scp[-1].sno != scp.sno)
//						&& (scp >= srccnt+ncnts-1 ||
//						    scp[1].sno != scp.sno))
                        if ((cntord[sn].sndx <= 0 || srccnt[cntord[sn].sndx - 1].sno != scp.sno)
                                && (cntord[sn].sndx >= ncnts - 1
                                || srccnt[cntord[sn].sndx + 1].sno != scp.sno)) {
                            SRCOBSTR.srcblocker(sr);
                        }
                    }
//#endif
                    continue;	/* missed! */
                }
                RAYTRACE.rayparticipate(sr);
                COLOR.multcolor(sr.rcol, sr.rcoef);
                COLOR.copycolor(scp.val, sr.rcol);
            } else if (//trace != NULL &&
                    (SRCSUPP.source[scp.sno].sflags & (SDISTANT | SVIRTUAL | SFOLLOW))
                    == (SDISTANT | SFOLLOW)
                    && sourcehit(sr) != 0 && RAYTRACE.rayshade(sr, sr.ro.omod) != 0) {
//			(*trace)(&sr);		/* trace execution */
			/* skip call to rayparticipate() & scp.val update */
            }
            /* add contribution if hit */
            COLOR.addcolor(r.rcol, scp.val);
            nhits++;
            SRCSUPP.source[scp.sno].nhits++;
        }
        /* source hit rate */
        if (hwt > FVECT.FTINY) {
            hwt = (double) nhits / hwt;
        } else {
            hwt = 0.5;
        }
//#ifdef DEBUG
//	sprintf(errmsg, "%d tested, %d untested, %f conditional hit rate\n",
//			sn, ncnts-sn, hwt);
//	eputs(errmsg);
//#endif
					/* add in untested sources */
        for (; sn < ncnts; sn++) {
            scp = srccnt[cntord[sn].sndx];
            prob = hwt * (double) SRCSUPP.source[scp.sno].nhits
                    / (double) SRCSUPP.source[scp.sno].ntests;
            if (prob < 1.0) {
                scp.val.scalecolor(prob);
            }
            COLOR.addcolor(r.rcol, scp.val);
        }
    }

    static void srcscatter( /* compute source scattering into ray */
            RAY r) {
        int oldsampndx;
        int nsamps;
        RAY sr = new RAY();
        SRCINDEX si = new SRCINDEX();
        double t, d;
        double re, ge, be;
        COLOR cvext = new COLOR();
        int i, j;

        if (r.slights == null || r.slights[0] == 0
                || r.gecc >= 1. - FVECT.FTINY || r.rot >= FVECT.FHUGE) {
            return;
        }
        if (RAYCALLS.ssampdist <= FVECT.FTINY || (nsamps = (int) (r.rot / RAYCALLS.ssampdist + .5)) < 1) {
            nsamps = 1;
        } //#if MAXSSAMP
        else if (nsamps > MAXSSAMP) {
            nsamps = MAXSSAMP;
        }
//#endif
        oldsampndx = RAYCALLS.samplendx;
        RAYCALLS.samplendx = (int) (RAYCALLS.random() & 0x7fff);		/* randomize */
        for (i = r.slights[0]; i > 0; i--) {	/* for each source */
            for (j = 0; j < nsamps; j++) {	/* for each sample position */
                RAYCALLS.samplendx++;
                t = r.rot * (j + RAYCALLS.frandom()) / nsamps;
                /* extinction */
                re = t * r.cext.colval(COLOR.RED);
                ge = t * r.cext.colval(COLOR.GRN);
                be = t * r.cext.colval(COLOR.BLU);
                cvext.setcolor((float) (re > 92. ? 0. : Math.exp(-re)),
                        (float) (ge > 92. ? 0. : Math.exp(-ge)),
                        (float) (be > 92. ? 0. : Math.exp(-be)));
                if (COLOR.intens(cvext) <= FVECT.FTINY) {
                    break;			/* too far away */
                }
                sr.rorg.data[0] = r.rorg.data[0] + r.rdir.data[0] * t;
                sr.rorg.data[1] = r.rorg.data[1] + r.rdir.data[1] * t;
                sr.rorg.data[2] = r.rorg.data[2] + r.rdir.data[2] * t;
                initsrcindex(si);	/* sample ray to this source */
                si.sn = r.slights[i];
                SSOBJ ssobj = new SSOBJ();
                ssobj.partit(si, sr); //nopart(si, sr);
                if (srcray(sr, null, si) == 0
                        || sr.rsrc != r.slights[i]) {
                    continue;		/* no path */
                }
//#if SHADCACHE
                if (SOURCE.SHADCACHE > 0) {
                    if (SRCOBSTR.srcblocked(sr) != 0) /* check shadow cache */ {
                        continue;
                    }
                }
//#endif
                COLOR.copycolor(sr.cext, r.cext);
                COLOR.copycolor(sr.albedo, r.albedo);
                sr.gecc = r.gecc;
                sr.slights = r.slights;
                RAY.rayvalue(sr);			/* eval. source ray */
                if (COLOR.bright(sr.rcol) <= FVECT.FTINY) {
//#if SHADCACHE
                    if (SOURCE.SHADCACHE > 0) {
                        SRCOBSTR.srcblocker(sr);	/* add blocker to cache */
                    }
//#endif
                    continue;
                }
                if (r.gecc <= FVECT.FTINY) /* compute P(theta) */ {
                    d = 1.;
                } else {
                    d = FVECT.DOT(r.rdir, sr.rdir);
                    d = 1. + r.gecc * r.gecc - 2. * r.gecc * d;
                    d = (1. - r.gecc * r.gecc) / (d * Math.sqrt(d));
                }
                /* other factors */
                d *= si.dom * r.rot / (4. * Math.PI * nsamps);
                COLOR.multcolor(sr.rcol, r.cext);
                COLOR.multcolor(sr.rcol, r.albedo);
                sr.rcol.scalecolor(d);
                COLOR.multcolor(sr.rcol, cvext);
                COLOR.addcolor(r.rcol, sr.rcol);	/* add it in */
            }
        }
        RAYCALLS.samplendx = oldsampndx;
    }

    public static class M_LIGHT extends OBJECT_STRUCTURE {

        /****************************************************************
         * The following macros were separated from the m_light() routine
         * because they are very nasty and difficult to understand.
         */

        /* illumblock *
         *
         * We cannot allow an illum to pass to another illum, because that
         * would almost certainly constitute overcounting.
         * However, we do allow an illum to pass to another illum
         * that is actually going to relay to a virtual light source.
         * We also prevent an illum from passing to a glow; this provides a
         * convenient mechanism for defining detailed light source
         * geometry behind (or inside) an effective radiator.
         */
        static boolean weaksrcmat(int obj) /* identify material */ {
            OBJREC m = findmaterial(OBJECT.objptr(obj));

            if (m == null) {
                return (false);
            }
            return ((m.otype == OTYPES.MAT_ILLUM) | (m.otype == OTYPES.MAT_GLOW));
        }

        boolean illumblock(OBJREC m, RAY r) {
            return ((SRCSUPP.source[r.rsrc].sflags & SVIRTUAL) == 0
                    && r.rod > 0.0
                    && weaksrcmat(SRCSUPP.source[r.rsrc].so.omod));
        }
        /* wrongsource *
         *
         * This source is the wrong source (ie. overcounted) if we are
         * aimed to a different source than the one we hit and the one
         * we hit is not an illum that should be passed.
         */

        boolean wrongsource(OBJREC m, RAY r) {
            return (r.rsrc >= 0 && SRCSUPP.source[r.rsrc].so != r.ro
                    && (m.otype != OTYPES.MAT_ILLUM || illumblock(m, r)));
        }

        /* distglow *
         *
         * A distant glow is an object that sometimes acts as a light source,
         * but is too far away from the test point to be one in this case.
         * (Glows with negative radii should NEVER participate in illumination.)
         */
        boolean distglow(OBJREC m, RAY r, double d) {
            return (m.otype == OTYPES.MAT_GLOW
                    && m.oargs.farg[3] >= -FVECT.FTINY
                    && d > m.oargs.farg[3]);
        }

        /* badcomponent *
         *
         * We must avoid counting light sources in the ambient calculation,
         * since the direct component is handled separately.  Therefore, any
         * ambient ray which hits an active light source must be discarded.
         * The same is true for stray specular samples, since the specular
         * contribution from light sources is calculated separately.
         */
        boolean badcomponent(OBJREC m, RAY r) {
            return ((r.crtype & (RAY.AMBIENT | RAY.SPECULAR)) != 0
                    && !((r.crtype & RAY.SHADOW) != 0 || r.rod < 0.0
                    || /* not 100% correct */ distglow(m, r, r.rot)));
        }

        /* passillum *
         *
         * An illum passes to another material type when we didn't hit it
         * on purpose (as part of a direct calculation), or it is relaying
         * a virtual light source.
         */
        boolean passillum(OBJREC m, RAY r) {
            return (m.otype == OTYPES.MAT_ILLUM
                    && (r.rsrc < 0 || SRCSUPP.source[r.rsrc].so != r.ro
                    || (SRCSUPP.source[r.rsrc].sflags & SVIRTUAL) != 0));
        }

        /* srcignore *
         *
         * The -dv flag is normally on for sources to be visible.
         */
        boolean srcignore(OBJREC m, RAY r) {
            return !(RAYCALLS.directvis != 0 || (r.crtype & RAY.SHADOW) != 0
                    || distglow(m, r, RAYTRACE.raydist(r, RAY.PRIMARY)));
        }

        @Override
        public int octree_function(Object... obj) {
            return m_light((OBJREC) obj[0], (RAY) obj[1]);
        }

        int m_light( /* ray hit a light source */
                OBJREC m,
                RAY r) {
            /* check for over-counting */
            if (badcomponent(m, r)) {
                r.rcoef.setcolor(0.0f, 0.0f, 0.0f);
                return (1);
            }
            if (wrongsource(m, r)) {
                r.rcoef.setcolor(0.0f, 0.0f, 0.0f);
                return (1);
            }
            /* check for passed illum */
            if (passillum(m, r)) {
                if (m.oargs.nsargs != 0 && !m.oargs.sarg[0].equals(OBJECT.VOIDID)) {
                    return (RAYTRACE.rayshade(r, MODOBJECT.lastmod(MODOBJECT.objndx(m), m.oargs.sarg[0])));
                }
                RAYTRACE.raytrans(r);
                return (1);
            }
            /* check for invisibility */
            if (srcignore(m, r)) {
                r.rcoef.setcolor(0.0f, 0.0f, 0.0f);
                return (1);
            }
            /* otherwise treat as source */
            /* check for behind */
            if (r.rod < 0.0) {
                return (1);
            }
            /* check for outside spot */
            if (m.otype == OTYPES.MAT_SPOT && SRCSUPP.spotout(r, SRCSUPP.makespot(m)) != 0) {
                return (1);
            }
            /* get distribution pattern */
            RAYTRACE.raytexture(r, m.omod);
            /* get source color */
            r.rcol.setcolor((float) m.oargs.farg[0],
                    (float) m.oargs.farg[1],
                    (float) m.oargs.farg[2]);
            /* modify value */
            COLOR.multcolor(r.rcol, r.pcol);
            return (1);
        }
    }
}
