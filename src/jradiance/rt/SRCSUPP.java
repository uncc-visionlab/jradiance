/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.rt;

import jradiance.common.CONE;
import jradiance.common.FACE;
import jradiance.common.FVECT;
import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OTYPES;
import jradiance.rt.M_DIRECT.DIRECT1_VS;
import jradiance.rt.M_DIRECT.DIRECT2_VS;
import jradiance.rt.M_MIRROR.MIRROR_VS;
import jradiance.rt.SOURCE.SOBJECT;
import jradiance.rt.SOURCE.SPOT;
import jradiance.rt.SOURCE.SRCFUNC;
import jradiance.rt.SOURCE.SRCINDEX;
import jradiance.rt.SOURCE.SRCREC;

/**
 *
 * @author arwillis
 */
public class SRCSUPP {
    /*
     *  Support routines for source objects and materials
     *
     *  External symbols declared in source.h
     */

    public static final int SRCINC = 8;		/* realloc increment for array */

    static SRCREC[] source = null;			/* our list of sources */

    static int nsources = 0;			/* the number of sources */

    static SRCFUNC[] sfun = new SRCFUNC[OTYPES.NUMOTYPE];		/* source dispatch table */


    public static class FSOBJ implements SOBJECT {

        FSOBJ() {
        }

        @Override
        public void setsrc(SRCREC src, OBJREC so) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        void fsetsrc( /* set a face as a source */
//                SRCREC src,
//                OBJREC so) {
            FACE f;
            int i, j;
            double d;

            src.sa.success = 2 * SOURCE.AIMREQT - 1;		/* bitch on second failure */
            src.so = so;
            /* get the face */
            f = FACE.getface(so);
            if (f.area == 0.0) {
//		objerror(so, USER, "zero source area");
            }
            /* find the center */
            for (j = 0; j < 3; j++) {
                src.sloc.data[j] = 0.0;
                for (i = 0; i < f.nv; i++) {
                    src.sloc.data[j] += FACE.VERTEX(f, i, j);
                }
                src.sloc.data[j] /= (double) f.nv;
            }
            if (FACE.inface(src.sloc, f) == 0) {
//		objerror(so, USER, "cannot hit source center");
            }
            src.sflags |= SOURCE.SFLAT;
            FVECT.VCOPY(src.snorm(), f.norm);
            src.ss2 = (float) f.area;
            /* find maximum radius */
            src.srad = 0.f;
            for (i = 0; i < f.nv; i++) {
                d = FVECT.dist2(new FVECT(FACE.VERTEX(f, i, 0), FACE.VERTEX(f, i, 1),
                        FACE.VERTEX(f, i, 2)), src.sloc);
                if (d > src.srad) {
                    src.srad = (float) d;
                }
            }
            src.srad = (float) Math.sqrt(src.srad);
            /* compute size vectors */
            if (f.nv == 4) /* parallelogram case */ {
                for (j = 0; j < 3; j++) {
                    src.ss[SOURCE.SU].data[j] = .5 * (FACE.VERTEX(f, 1, j) - FACE.VERTEX(f, 0, j));
                    src.ss[SOURCE.SV].data[j] = .5 * (FACE.VERTEX(f, 3, j) - FACE.VERTEX(f, 0, j));
                }
            } else {
                SRCSUPP.setflatss(src);
            }
        }

        @Override
        public void partit(SRCINDEX si, RAY r) {
            //            throw new UnsupportedOperationException("Not supported yet.");
            //        }
            //
            //        void flatpart( /* partition a flat source */
            //                SRCINDEX si,
            //                RAY r) {
            //double[]  vp;
            FVECT vp;
            FVECT v = new FVECT();
            double du2, dv2;
            int[] pi = new int[1];

            SOURCE.clrpart(si.spt);
            vp = SRCSUPP.source[si.sn].sloc;
            v.data[0] = r.rorg.data[0] - vp.data[0];
            v.data[1] = r.rorg.data[1] - vp.data[1];
            v.data[2] = r.rorg.data[2] - vp.data[2];
            vp = SRCSUPP.source[si.sn].snorm();
            if (FVECT.DOT(v, vp) <= 0.) {		/* behind source */
                si.np = 0;
                return;
            }
            dv2 = 2. * r.rweight / RAYCALLS.srcsizerat;
            dv2 *= dv2;
            vp = source[si.sn].ss[SOURCE.SU];
            du2 = dv2 * FVECT.DOT(vp, vp);
            vp = source[si.sn].ss[SOURCE.SV];
            dv2 *= FVECT.DOT(vp, vp);
            pi[0] = 0;
            si.np = (short) SRCSAMP.flt_partit(r.rorg, si.spt, pi, SOURCE.MAXSPART,
                    source[si.sn].sloc,
                    source[si.sn].ss[SOURCE.SU], source[si.sn].ss[SOURCE.SV], du2, dv2);
        }

        @Override
        public double getpleq(FVECT nvec, OBJREC op) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        double fgetplaneq( /* get plane equation for face */
//                FVECT nvec,
//                OBJREC op) {
            FACE fo;

            fo = FACE.getface(op);
            FVECT.VCOPY(nvec, fo.norm);
            return (fo.offset);
        }

        @Override
        public double getdisk(FVECT ocent, OBJREC op) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        double fgetmaxdisk( /* get center and squared radius of face */
//                FVECT ocent,
//                OBJREC op) {
            double maxrad2;
            double d;
            int i, j;
            FACE f;

            f = FACE.getface(op);
            if (f.area == 0.) {
                return (0.);
            }
            for (i = 0; i < 3; i++) {
                ocent.data[i] = 0.;
                for (j = 0; j < f.nv; j++) {
                    ocent.data[i] += FACE.VERTEX(f, j, i);
                }
                ocent.data[i] /= (double) f.nv;
            }
            d = FVECT.DOT(ocent, f.norm);
            for (i = 0; i < 3; i++) {
                ocent.data[i] += (f.offset - d) * f.norm.data[i];
            }
            maxrad2 = 0.;
            for (j = 0; j < f.nv; j++) {
                d = FVECT.dist2(new FVECT(FACE.VERTEX(f, j, 0), FACE.VERTEX(f, j, 1),
                        FACE.VERTEX(f, j, 2)), ocent);
                if (d > maxrad2) {
                    maxrad2 = d;
                }
            }
            return (maxrad2);
        }
    }

    public static class SSOBJ implements SOBJECT {

        SSOBJ() {
        }

        @Override
        public void setsrc(SRCREC src, OBJREC so) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        void ssetsrc( /* set a source as a source */
//                SRCREC src,
//                OBJREC so) {
            double theta;

            src.sa.success = 2 * SOURCE.AIMREQT - 1;		/* bitch on second failure */
            src.so = so;
            if (so.oargs.nfargs != 4) {
//		objerror(so, USER, "bad arguments");
            }
            src.sflags |= (SOURCE.SDISTANT | SOURCE.SCIR);
            FVECT.VCOPY(src.sloc, new FVECT(so.oargs.farg[0], so.oargs.farg[1], so.oargs.farg[2]));
            if (FVECT.normalize(src.sloc) == 0.0) {
//		objerror(so, USER, "zero direction");
            }
            theta = Math.PI / 180.0 / 2.0 * so.oargs.farg[3];
            if (theta <= FVECT.FTINY) {
//		objerror(so, USER, "zero size");
            }
            src.ss2 = (float) (2.0 * Math.PI * (1.0 - Math.cos(theta)));
            /* the following is approximate */
            src.srad = (float) Math.sqrt(src.ss2 / Math.PI);
            FVECT.VCOPY(src.snorm(), src.sloc);
            setflatss(src);			/* hey, whatever works */
            src.ss[SOURCE.SW].data[0] = src.ss[SOURCE.SW].data[1] = src.ss[SOURCE.SW].data[2] = 0.0;
        }

        @Override
        public void partit(SRCINDEX si, RAY r) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        void nopart( /* single source partition */
//                SRCINDEX si,
//                RAY r) {
            SOURCE.clrpart(si.spt);
            SOURCE.setpart(si.spt, 0, SOURCE.S0);
            si.np = 1;
        }

        @Override
        public double getpleq(FVECT nvec, OBJREC op) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double getdisk(FVECT ocent, OBJREC op) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static class SPHSOBJ extends SSOBJ {

        SPHSOBJ() {
        }

        @Override
        public void setsrc(SRCREC s, OBJREC o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        void sphsetsrc( /* set a sphere as a source */
                SRCREC src,
                OBJREC so) {
            int i;

            src.sa.success = 2 * SOURCE.AIMREQT - 1;		/* bitch on second failure */
            src.so = so;
            if (so.oargs.nfargs != 4) {
//		objerror(so, USER, "bad # arguments");
            }
            if (so.oargs.farg[3] <= FVECT.FTINY) {
//		objerror(so, USER, "illegal source radius");
            }
            src.sflags |= SOURCE.SCIR;
            FVECT.VCOPY(src.sloc, new FVECT(so.oargs.farg[0], so.oargs.farg[1], so.oargs.farg[2]));
            src.srad = (float) so.oargs.farg[3];
            src.ss2 = (float) (Math.PI * src.srad * src.srad);
            for (i = 0; i < 3; i++) {
                src.ss[SOURCE.SU].data[i] = src.ss[SOURCE.SV].data[i] = src.ss[SOURCE.SW].data[i] = 0.0;
            }
            for (i = 0; i < 3; i++) {
                src.ss[i].data[i] = 0.7236 * so.oargs.farg[3];
            }
        }
    }

    public static class CYLSOBJ implements SOBJECT {

        CYLSOBJ() {
        }

        @Override
        public void setsrc(SRCREC s, OBJREC o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        void cylsetsrc( /* set a cylinder as a source */
                SRCREC src,
                OBJREC so) {
            CONE co;
            int i;

            src.sa.success = 4 * SOURCE.AIMREQT - 1;		/* bitch on fourth failure */
            src.so = so;
            /* get the cylinder */
            co = CONE.getcone(so, 0);
            if (CONE.CO_R0(co, 0) <= FVECT.FTINY) {
//		objerror(so, USER, "illegal source radius");
            }
            if (CONE.CO_R0(co, 0) > .2 * co.al) {		/* heuristic constraint */
//		objerror(so, WARNING, "source aspect too small");

            }
            src.sflags |= SOURCE.SCYL;
            for (i = 0; i < 3; i++) {
                src.sloc.data[i] = .5 * (CONE.CO_P1(co, i) + CONE.CO_P0(co, i));
            }
            src.srad = (float) (.5 * co.al);
            src.ss2 = (float) (2. * CONE.CO_R0(co, 0) * co.al);
            /* set sampling vectors */
            for (i = 0; i < 3; i++) {
                src.ss[SOURCE.SU].data[i] = (.5 * co.al * co.ad.data[i]);
            }
            src.ss[SOURCE.SV].data[0] = src.ss[SOURCE.SV].data[1] = src.ss[SOURCE.SV].data[2] = 0.0;
            for (i = 0; i < 3; i++) {
                if (co.ad.data[i] < 0.6 && co.ad.data[i] > -0.6) {
                    break;
                }
            }
            src.ss[SOURCE.SV].data[i] = 1.0;
            FVECT.fcross(src.ss[SOURCE.SW], src.ss[SOURCE.SV], co.ad);
            FVECT.normalize(src.ss[SOURCE.SW]);
            for (i = 0; i < 3; i++) {
                src.ss[SOURCE.SW].data[i] *= .8559 * CONE.CO_R0(co, 0);
            }
            FVECT.fcross(src.ss[SOURCE.SV], src.ss[SOURCE.SW], co.ad);
        }

        @Override
        public void partit(SRCINDEX si, RAY r) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        void cylpart( /* partition a cylinder */
//                SRCINDEX si,
//                RAY r) {
            double dist2, safedist2, dist2cent, rad2;
            FVECT v = new FVECT();
            SRCREC sp;
            int[] pi = new int[1];
            /* first check point location */
            SOURCE.clrpart(si.spt);
//	sp = source + si.sn;
            sp = source[si.sn];
            rad2 = 1.365 * FVECT.DOT(sp.ss[SOURCE.SV], sp.ss[SOURCE.SV]);
            v.data[0] = r.rorg.data[0] - sp.sloc.data[0];
            v.data[1] = r.rorg.data[1] - sp.sloc.data[1];
            v.data[2] = r.rorg.data[2] - sp.sloc.data[2];
            dist2 = FVECT.DOT(v, sp.ss[SOURCE.SU]);
            safedist2 = FVECT.DOT(sp.ss[SOURCE.SU], sp.ss[SOURCE.SU]);
            dist2 *= dist2 / safedist2;
            dist2cent = FVECT.DOT(v, v);
            dist2 = dist2cent - dist2;
            if (dist2 <= rad2) {		/* point inside extended cylinder */
                si.np = 0;
                return;
            }
            safedist2 *= 4. * r.rweight * r.rweight / (RAYCALLS.srcsizerat * RAYCALLS.srcsizerat);
            if (dist2 <= 4. * rad2
                    || /* point too close to subdivide */ dist2cent >= safedist2) {	/* or too far */
                SOURCE.setpart(si.spt, 0, SOURCE.S0);
                si.np = 1;
                return;
            }
            pi[0] = 0;
            si.np = (short) SRCSAMP.cyl_partit(r.rorg, si.spt, pi, SOURCE.MAXSPART,
                    sp.sloc, sp.ss[SOURCE.SU], safedist2);
        }

        @Override
        public double getpleq(FVECT nvec, OBJREC op) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double getdisk(FVECT ocent, OBJREC op) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static class RSOBJ extends FSOBJ {

        RSOBJ() {
        }

        @Override
        public void setsrc(SRCREC s, OBJREC o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        void rsetsrc( /* set a ring (disk) as a source */
                SRCREC src,
                OBJREC so) {
            CONE co;

            src.sa.success = 2 * SOURCE.AIMREQT - 1;		/* bitch on second failure */
            src.so = so;
            /* get the ring */
            co = CONE.getcone(so, 0);
            if (CONE.CO_R1(co, 0) <= FVECT.FTINY) {
//		objerror(so, USER, "illegal source radius");
            }
            FVECT.VCOPY(src.sloc, new FVECT(CONE.CO_P0(co, 0), CONE.CO_P0(co, 1), CONE.CO_P0(co, 2)));
            if (CONE.CO_R0(co, 0) > 0.0) {
//		objerror(so, USER, "cannot hit source center");
            }
            src.sflags |= (SOURCE.SFLAT | SOURCE.SCIR);
            FVECT.VCOPY(src.snorm(), co.ad);
            src.srad = (float) CONE.CO_R1(co, 0);
            src.ss2 = (float) (Math.PI * src.srad * src.srad);
            setflatss(src);
        }

        @Override
        public double getpleq(FVECT nvec, OBJREC op) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        double rgetplaneq( /* get plane equation for ring */
//                FVECT nvec,
//                OBJREC op) {
            CONE co;

            co = CONE.getcone(op, 0);
            FVECT.VCOPY(nvec, co.ad);
            return (FVECT.DOT(nvec, new FVECT(CONE.CO_P0(co, 0), CONE.CO_P0(co, 1), CONE.CO_P0(co, 2))));
        }

        @Override
        public double getdisk(FVECT ocent, OBJREC op) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        double rgetmaxdisk( /* get center and squared radius of ring */
//                FVECT ocent,
//                OBJREC op) {
            CONE co;

            co = CONE.getcone(op, 0);
            FVECT.VCOPY(ocent, new FVECT(CONE.CO_P0(co, 0), CONE.CO_P0(co, 1), CONE.CO_P0(co, 2)));
            return (CONE.CO_R1(co, 0) * CONE.CO_R1(co, 0));
        }
    }
    //	extern VSMATERIAL  mirror_vs, direct1_vs, direct2_vs;
//	static SOBJECT  fsobj = {fsetsrc, flatpart, fgetplaneq, fgetmaxdisk};
//	static SOBJECT  ssobj = {ssetsrc, nopart};
//	static SOBJECT  sphsobj = {sphsetsrc, nopart};
//	static SOBJECT  cylsobj = {cylsetsrc, cylpart};
//	static SOBJECT  rsobj = {rsetsrc, flatpart, rgetplaneq, rgetmaxdisk};

    public static void initstypes() /* initialize source dispatch table */ {
        sfun[OTYPES.MAT_MIRROR] = new SRCFUNC();
        sfun[OTYPES.MAT_MIRROR].mf = new MIRROR_VS();
        sfun[OTYPES.MAT_DIRECT1] = new SRCFUNC();
        sfun[OTYPES.MAT_DIRECT1].mf = new DIRECT1_VS();
        sfun[OTYPES.MAT_DIRECT2] = new SRCFUNC();
        sfun[OTYPES.MAT_DIRECT2].mf = new DIRECT2_VS();
        sfun[OTYPES.OBJ_FACE] = new SRCFUNC();
        sfun[OTYPES.OBJ_FACE].of = new FSOBJ();
        sfun[OTYPES.OBJ_SOURCE] = new SRCFUNC();
        sfun[OTYPES.OBJ_SOURCE].of = new SSOBJ();
        sfun[OTYPES.OBJ_SPHERE] = new SRCFUNC();
        sfun[OTYPES.OBJ_SPHERE].of = new SPHSOBJ();
        sfun[OTYPES.OBJ_CYLINDER] = new SRCFUNC();
        sfun[OTYPES.OBJ_CYLINDER].of = new CYLSOBJ();
        sfun[OTYPES.OBJ_RING] = new SRCFUNC();
        sfun[OTYPES.OBJ_RING].of = new RSOBJ();
    }

    public static int newsource() /* allocate new source in our array */ {
        if (nsources == 0) {
            source = new SRCREC[SRCINC];
        } else if (nsources % SRCINC == 0) {
            SRCREC[] tmpsource = new SRCREC[nsources + SRCINC];
//		source = (SRCREC *)realloc((void *)source,
//				(unsigned)(nsources+SRCINC)*sizeof(SRCREC));
            System.arraycopy(source, 0, tmpsource, 0, source.length);
            source = tmpsource;
        }
        for (int i = nsources; i < source.length; i++) {
            source[i] = new SRCREC();
        }
        if (source == null) {
            return (-1);
        }
        source[nsources].sflags = 0;
        source[nsources].nhits = 1;
        source[nsources].ntests = 2;	/* initial hit probability = 50% */
//#if SHADCACHE
        if (SOURCE.SHADCACHE > 0) {
            source[nsources].obscache = null;
        }
//#endif
        return (nsources++);
    }

    public static void setflatss( /* set sampling for a flat source */
            SRCREC src) {
        double mult;
        int i;

        src.ss[SOURCE.SV].data[0] = src.ss[SOURCE.SV].data[1] = src.ss[SOURCE.SV].data[2] = 0.0;
        for (i = 0; i < 3; i++) {
            if (src.snorm().data[i] < 0.6 && src.snorm().data[i] > -0.6) {
                break;
            }
        }
        src.ss[SOURCE.SV].data[i] = 1.0;
        FVECT.fcross(src.ss[SOURCE.SU], src.ss[SOURCE.SV], src.snorm());
        mult = .5 * Math.sqrt(src.ss2 / FVECT.DOT(src.ss[SOURCE.SU], src.ss[SOURCE.SU]));
        for (i = 0; i < 3; i++) {
            src.ss[SOURCE.SU].data[i] *= mult;
        }
        FVECT.fcross(src.ss[SOURCE.SV], src.snorm(), src.ss[SOURCE.SU]);
    }

    public static SPOT makespot( /* make a spotlight */
            OBJREC m) {
        SPOT ns;

        if ((ns = (SPOT) m.os) != null) {
            return (ns);
        }
        if ((ns = new SPOT()) == null) {
            return (null);
        }
        if (m.oargs.farg[3] <= FVECT.FTINY) {
//		objerror(m, USER, "zero angle");
        }
        ns.siz = (float) (2.0 * Math.PI * (1.0 - Math.cos(Math.PI / 180.0 / 2.0 * m.oargs.farg[3])));
        FVECT.VCOPY(ns.aim, new FVECT(m.oargs.farg[4], m.oargs.farg[5], m.oargs.farg[6]));
        if ((ns.flen = (float) FVECT.normalize(ns.aim)) == 0.0) {
//		objerror(m, USER, "zero focus vector");
        }
        m.os = ns;
        return (ns);
    }

    static int spotout( /* check if we're outside spot region */
            RAY r,
            SPOT s) {
        double d;
        FVECT vd = new FVECT();

        if (s == null) {
            return (0);
        }
        if (s.flen < -FVECT.FTINY) {		/* distant source */
            vd.data[0] = s.aim.data[0] - r.rorg.data[0];
            vd.data[1] = s.aim.data[1] - r.rorg.data[1];
            vd.data[2] = s.aim.data[2] - r.rorg.data[2];
            d = FVECT.DOT(r.rdir, vd);
            /*			wrong side?
            if (d <= FTINY)
            return(1);	*/
            d = FVECT.DOT(vd, vd) - d * d;
            if (Math.PI * d > s.siz) {
                return (1);	/* out */
            }
            return (0);	/* OK */
        }
        /* local source */
        if (s.siz < 2.0 * Math.PI * (1.0 + FVECT.DOT(s.aim, r.rdir))) {
            return (1);	/* out */
        }
        return (0);	/* OK */
    }

//double
//fgetplaneq(nvec, op)			/* get plane equation for face */
//FVECT  nvec;
//OBJREC  *op;
//{
//	register FACE  *fo;
//
//	fo = getface(op);
//	VCOPY(nvec, fo->norm);
//	return(fo->offset);
//}
//
//
//int
//commonspot(sp1, sp2, org)	/* set sp1 to intersection of sp1 and sp2 */
//register SPOT  *sp1, *sp2;
//FVECT  org;
//{
//	FVECT  cent;
//	double  rad2, cos1, cos2;
//
//	cos1 = 1. - sp1->siz/(2.*PI);
//	cos2 = 1. - sp2->siz/(2.*PI);
//	if (sp2->siz >= 2.*PI-FTINY)		/* BIG, just check overlap */
//		return(DOT(sp1->aim,sp2->aim) >= cos1*cos2 -
//					sqrt((1.-cos1*cos1)*(1.-cos2*cos2)));
//				/* compute and check disks */
//	rad2 = intercircle(cent, sp1->aim, sp2->aim,
//			1./(cos1*cos1) - 1.,  1./(cos2*cos2) - 1.);
//	if (rad2 <= FTINY || normalize(cent) == 0.)
//		return(0);
//	VCOPY(sp1->aim, cent);
//	sp1->siz = 2.*PI*(1. - 1./sqrt(1.+rad2));
//	return(1);
//}
//
//
//int
//commonbeam(sp1, sp2, dir)	/* set sp1 to intersection of sp1 and sp2 */
//register SPOT  *sp1, *sp2;
//FVECT  dir;
//{
//	FVECT  cent, c1, c2;
//	double  rad2, d;
//					/* move centers to common plane */
//	d = DOT(sp1->aim, dir);
//	VSUM(c1, sp1->aim, dir, -d);
//	d = DOT(sp2->aim, dir);
//	VSUM(c2, sp2->aim, dir, -d);
//					/* compute overlap */
//	rad2 = intercircle(cent, c1, c2, sp1->siz/PI, sp2->siz/PI);
//	if (rad2 <= FTINY)
//		return(0);
//	VCOPY(sp1->aim, cent);
//	sp1->siz = PI*rad2;
//	return(1);
//}
    public static int checkspot( /* check spotlight for behind source */
            SPOT sp, /* spotlight */
            FVECT nrm) /* source surface normal */ {
        double d, d1;

        d = FVECT.DOT(sp.aim, nrm);
        if (d > FVECT.FTINY) /* center in front? */ {
            return (1);
        }
        /* else check horizon */
        d1 = 1. - sp.siz / (2. * Math.PI);
        return (1. - FVECT.FTINY - d * d < d1 * d1) ? 1 : 0;
    }
//double
//spotdisk(oc, op, sp, pos)	/* intersect spot with object op */
//FVECT  oc;
//OBJREC  *op;
//register SPOT  *sp;
//FVECT  pos;
//{
//	FVECT  onorm;
//	double  offs, d, dist;
//
//	offs = getplaneq(onorm, op);
//	d = -DOT(onorm, sp->aim);
//	if (d >= -FTINY && d <= FTINY)
//		return(0.);
//	dist = (DOT(pos, onorm) - offs)/d;
//	if (dist < 0.)
//		return(0.);
//	VSUM(oc, pos, sp->aim, dist);
//	return(sp->siz*dist*dist/PI/(d*d));
//}
//
//
//double
//beamdisk(oc, op, sp, dir)	/* intersect beam with object op */
//FVECT  oc;
//OBJREC  *op;
//register SPOT  *sp;
//FVECT  dir;
//{
//	FVECT  onorm;
//	double  offs, d, dist;
//
//	offs = getplaneq(onorm, op);
//	d = -DOT(onorm, dir);
//	if (d >= -FTINY && d <= FTINY)
//		return(0.);
//	dist = (DOT(sp->aim, onorm) - offs)/d;
//	VSUM(oc, sp->aim, dir, dist);
//	return(sp->siz/PI/(d*d));
//}
//
//
//double
//intercircle(cc, c1, c2, r1s, r2s)	/* intersect two circles */
//FVECT  cc;			/* midpoint (return value) */
//FVECT  c1, c2;			/* circle centers */
//double  r1s, r2s;		/* radii squared */
//{
//	double  a2, d2, l;
//	FVECT  disp;
//
//	VSUB(disp, c2, c1);
//	d2 = DOT(disp,disp);
//					/* circle within overlap? */
//	if (r1s < r2s) {
//		if (r2s >= r1s + d2) {
//			VCOPY(cc, c1);
//			return(r1s);
//		}
//	} else {
//		if (r1s >= r2s + d2) {
//			VCOPY(cc, c2);
//			return(r2s);
//		}
//	}
//	a2 = .25*(2.*(r1s+r2s) - d2 - (r2s-r1s)*(r2s-r1s)/d2);
//					/* no overlap? */
//	if (a2 <= 0.)
//		return(0.);
//					/* overlap, compute center */
//	l = sqrt((r1s - a2)/d2);
//	VSUM(cc, c1, disp, l);
//	return(a2);
//}
//    
}
