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

import jradiance.common.FVECT;
import jradiance.common.MULTISAMP;
import jradiance.common.URAND;
import jradiance.rt.SOURCE.SRCINDEX;
import jradiance.rt.SOURCE.SRCREC;
import jradiance.rt.SRCSUPP.SSOBJ;

/**
 *
 * @author arwillis
 */
public class SRCSAMP {
//#ifndef lint
//static const char	RCSid[] = "$Id$";
//#endif
///*
// * Source sampling routines
// *
// *  External symbols declared in source.h
// */
//
//#include "copyright.h"
//
//#include  "ray.h"
//
//#include  "source.h"
//
//#include  "random.h"
//
//
//static int  cyl_partit(), flt_partit();
//

static double
nextssamp(		/* compute sample for source, rtn. distance */
RAY  r,		/* origin is read, direction is set */
SRCINDEX  si)		/* source index (modified to current) */
{
	int[]  cent = new int[3], size = new int[3], parr = new int[2];
	SRCREC  srcp;
	FVECT  vpos = new FVECT();
	double  d;
	int  i;
//nextsample:
	while (++si.sp >= si.np) {	/* get next sample */
		if (++si.sn >= SRCSUPP.nsources)
			return(0.0);	/* no more */
		if ((SRCSUPP.source[si.sn].sflags & SOURCE.SSKIP) != 0)
			si.np = 0;
		else if (RAYCALLS.srcsizerat <= FVECT.FTINY) {
                    SSOBJ ssobj = new SSOBJ();
                    ssobj.partit(si, r);
//			nopart(si, r);
                }
		else {
			for (i = si.sn; (SRCSUPP.source[i].sflags & SOURCE.SVIRTUAL) != 0;
					i = SRCSUPP.source[i].sa.sv.sn)
				;		/* partition source */
                        SRCSUPP.sfun[SRCSUPP.source[i].so.otype].of.partit(si, r);
//			(*sfun[source[i].so->otype].of->partit)(si, r);
		}
		si.sp = -1;
	}
					/* get partition */
	cent[0] = cent[1] = cent[2] = 0;
	size[0] = size[1] = size[2] = SOURCE.MAXSPART;
	parr[0] = 0; parr[1] = si.sp;
	if (skipparts(cent, size, parr, si.spt)==0) {
//		error(CONSISTENCY, "bad source partition in nextssamp");
        }
					/* compute sample */
	srcp = SRCSUPP.source[si.sn];
	if (RAYCALLS.dstrsrc > FVECT.FTINY) {			/* jitter sample */
		RAYCALLS.dimlist[RAYCALLS.ndims] = si.sn + 8831;
		RAYCALLS.dimlist[RAYCALLS.ndims+1] = si.sp + 3109;
		d = URAND.urand(URAND.ilhash(RAYCALLS.dimlist,RAYCALLS.ndims+2)+RAYCALLS.samplendx);
		if ((srcp.sflags & SOURCE.SFLAT)!=0) {
			MULTISAMP.multisamp(vpos.data, 2, d);
			vpos.data[SOURCE.SW] = 0.5;
		} else
			MULTISAMP.multisamp(vpos.data, 3, d);
		for (i = 0; i < 3; i++)
			vpos.data[i] = RAYCALLS.dstrsrc * (1. - 2.*vpos.data[i]) *
					(double)size[i]*(1.0/SOURCE.MAXSPART);
	} else
		vpos.data[0] = vpos.data[1] = vpos.data[2] = 0.0;

	FVECT.VSUM(vpos, vpos, new FVECT(cent[0],cent[1],cent[2]), 1.0/SOURCE.MAXSPART);
					/* avoid circular aiming failures */
	if ((srcp.sflags & SOURCE.SCIR)!=0 && (si.np > 1 || RAYCALLS.dstrsrc > 0.7)) {
		FVECT	trim = new FVECT();
		if ((srcp.sflags & (SOURCE.SFLAT|SOURCE.SDISTANT))!=0) {
			d = 1.12837917;		/* correct setflatss() */
			trim.data[SOURCE.SU] = d*Math.sqrt(1.0 - 0.5*vpos.data[SOURCE.SV]*vpos.data[SOURCE.SV]);
			trim.data[SOURCE.SV] = d*Math.sqrt(1.0 - 0.5*vpos.data[SOURCE.SU]*vpos.data[SOURCE.SU]);
			trim.data[SOURCE.SW] = 0.0;
		} else {
			trim.data[SOURCE.SW] = trim.data[SOURCE.SU] = vpos.data[SOURCE.SU]*vpos.data[SOURCE.SU];
			d = vpos.data[SOURCE.SV]*vpos.data[SOURCE.SV];
			if (d > trim.data[SOURCE.SW]) trim.data[SOURCE.SW] = d;
			trim.data[SOURCE.SU] += d;
			d = vpos.data[SOURCE.SW]*vpos.data[SOURCE.SW];
			if (d > trim.data[SOURCE.SW]) trim.data[SOURCE.SW] = d;
			trim.data[SOURCE.SU] += d;
			if (trim.data[SOURCE.SU] > FVECT.FTINY*FVECT.FTINY) {
				d = 1.0/0.7236;	/* correct sphsetsrc() */
				trim.data[SOURCE.SW] = trim.data[SOURCE.SV] = trim.data[SOURCE.SU] =
						d*Math.sqrt(trim.data[SOURCE.SW]/trim.data[SOURCE.SU]);
			} else
				trim.data[SOURCE.SW] = trim.data[SOURCE.SV] = trim.data[SOURCE.SU] = 0.0;
		}
		for (i = 0; i < 3; i++)
			vpos.data[i] *= trim.data[i];
	}
					/* compute direction */
	for (i = 0; i < 3; i++)
		r.rdir.data[i] = srcp.sloc.data[i] +
				vpos.data[SOURCE.SU]*srcp.ss[SOURCE.SU].data[i] +
				vpos.data[SOURCE.SV]*srcp.ss[SOURCE.SV].data[i] +
				vpos.data[SOURCE.SW]*srcp.ss[SOURCE.SW].data[i];

	if ((srcp.sflags & SOURCE.SDISTANT)==0)
		FVECT.VSUB(r.rdir, r.rdir, r.rorg);
					/* compute distance */
	if ((d = FVECT.normalize(r.rdir)) == 0.0) {
//		goto nextsample;		/* at source! */
        }
					/* compute sample size */
	if ((srcp.sflags & SOURCE.SFLAT)!=0) {
		si.dom = SOURCE.sflatform(si.sn, r.rdir);
		si.dom *= size[SOURCE.SU]*size[SOURCE.SV]*(1.0/SOURCE.MAXSPART/SOURCE.MAXSPART);
	} else if ((srcp.sflags & SOURCE.SCYL)!=0) {
		si.dom = scylform(si.sn, r.rdir);
		si.dom *= size[SOURCE.SU]*(1.0/SOURCE.MAXSPART);
	} else {
		si.dom = size[SOURCE.SU]*size[SOURCE.SV]*(double)size[SOURCE.SW] *
				(1.0/SOURCE.MAXSPART/SOURCE.MAXSPART/SOURCE.MAXSPART) ;
	}
	if ((srcp.sflags & SOURCE.SDISTANT)!=0) {
		si.dom *= srcp.ss2;
		return(FVECT.FHUGE);
	}
	if (si.dom <= 1e-4) {
//		goto nextsample;		/* behind source? */
        }
	si.dom *= srcp.ss2/(d*d);
	return(d);		/* sample OK, return distance */
}


static int
skipparts(		/* skip to requested partition */
int[]  ct, int[] sz,		/* center and size of partition (returned) */
int[]  pp,		/* current index, number to skip (modified) */
char[]  pt)		/* partition array */
{
	int  p;
					/* check this partition */
	p = SOURCE.spart(pt, pp[0]);
	pp[0]++;
	if (p == SOURCE.S0) {			/* leaf partition */
		if (pp[1]!=0) {
			pp[1]--;
			return(0);	/* not there yet */
		} else
			return(1);	/* we've arrived */
	}
				/* else check lower */
	sz[p] >>= 1;
	ct[p] -= sz[p];
	if (skipparts(ct, sz, pp, pt)!=0)
		return(1);	/* return hit */
				/* else check upper */
	ct[p] += sz[p] << 1;
	if (skipparts(ct, sz, pp, pt)!=0)
		return(1);	/* return hit */
				/* else return to starting position */
	ct[p] -= sz[p];
	sz[p] <<= 1;
	return(0);		/* return miss */
}


static int
cyl_partit(	/* slice a cylinder */
FVECT  ro,
char[]  pt,
int[]  pi,
int  mp,
FVECT  cent, FVECT axis,
double  d2)
{
	FVECT  newct = new FVECT(), newax = new FVECT();
	int  npl, npu;

	if (mp < 2 || FVECT.dist2(ro, cent) >= d2) {	/* hit limit? */
		SOURCE.setpart(pt, pi[0], SOURCE.S0);
		pi[0]++;
		return(1);
	}
					/* subdivide */
	SOURCE.setpart(pt, pi[0], SOURCE.SU);
	pi[0]++;
	newax.data[0] = .5*axis.data[0];
	newax.data[1] = .5*axis.data[1];
	newax.data[2] = .5*axis.data[2];
	d2 *= 0.25;
					/* lower half */
	newct.data[0] = cent.data[0] - newax.data[0];
	newct.data[1] = cent.data[1] - newax.data[1];
	newct.data[2] = cent.data[2] - newax.data[2];
	npl = cyl_partit(ro, pt, pi, mp/2, newct, newax, d2);
					/* upper half */
	newct.data[0] = cent.data[0] + newax.data[0];
	newct.data[1] = cent.data[1] + newax.data[1];
	newct.data[2] = cent.data[2] + newax.data[2];
	npu = cyl_partit(ro, pt, pi, mp/2, newct, newax, d2);
					/* return total */
	return(npl + npu);
}

//
//void
//flatpart(si, r)				/* partition a flat source */
//register SRCINDEX  *si;
//register RAY  *r;
//{
//	register RREAL  *vp;
//	FVECT  v;
//	double  du2, dv2;
//	int  pi;
//
//	clrpart(si->spt);
//	vp = source[si->sn].sloc;
//	v[0] = r->rorg[0] - vp[0];
//	v[1] = r->rorg[1] - vp[1];
//	v[2] = r->rorg[2] - vp[2];
//	vp = source[si->sn].snorm;
//	if (DOT(v,vp) <= 0.) {		/* behind source */
//		si->np = 0;
//		return;
//	}
//	dv2 = 2.*r->rweight/srcsizerat;
//	dv2 *= dv2;
//	vp = source[si->sn].ss[SU];
//	du2 = dv2 * DOT(vp,vp);
//	vp = source[si->sn].ss[SV];
//	dv2 *= DOT(vp,vp);
//	pi = 0;
//	si->np = flt_partit(r->rorg, si->spt, &pi, MAXSPART,
//		source[si->sn].sloc,
//		source[si->sn].ss[SU], source[si->sn].ss[SV], du2, dv2);
//}
//

public static int
flt_partit(	/* partition flatty */
FVECT  ro,
char[]  pt,
int[]  pi,
int  mp,
FVECT  cent, FVECT u, FVECT v,
double  du2, double dv2)
{
	double  d2;
	FVECT  newct = new FVECT(), newax = new FVECT();
	int  npl, npu;
	if (mp < 2 || ((d2 = FVECT.dist2(ro, cent)) >= du2
			&& d2 >= dv2)) {	/* hit limit? */
		SOURCE.setpart(pt, pi[0], SOURCE.S0);
		pi[0]++;
		return(1);
	}
	if (du2 > dv2) {			/* subdivide in U */
		SOURCE.setpart(pt, pi[0], SOURCE.SU);
		pi[0]++;
		newax.data[0] = .5*u.data[0];
		newax.data[1] = .5*u.data[1];
		newax.data[2] = .5*u.data[2];
		u = newax;
		du2 *= 0.25;
	} else {				/* subdivide in V */
		SOURCE.setpart(pt, pi[0], SOURCE.SV);
		pi[0]++;
		newax.data[0] = .5*v.data[0];
		newax.data[1] = .5*v.data[1];
		newax.data[2] = .5*v.data[2];
		v = newax;
		dv2 *= 0.25;
	}
					/* lower half */
	newct.data[0] = cent.data[0] - newax.data[0];
	newct.data[1] = cent.data[1] - newax.data[1];
	newct.data[2] = cent.data[2] - newax.data[2];
	npl = flt_partit(ro, pt, pi, mp/2, newct, u, v, du2, dv2);
					/* upper half */
	newct.data[0] = cent.data[0] + newax.data[0];
	newct.data[1] = cent.data[1] + newax.data[1];
	newct.data[2] = cent.data[2] + newax.data[2];
	npu = flt_partit(ro, pt, pi, mp/2, newct, u, v, du2, dv2);
				/* return total */
	return(npl + npu);
}


static double
scylform(		/* compute cosine for cylinder's projection */
int  sn,
FVECT  dir)		/* assume normalized */
{
	FVECT  dv;
	double  d;

	dv = SRCSUPP.source[sn].ss[SOURCE.SU];
	d = FVECT.DOT(dir, dv);
	d *= d / FVECT.DOT(dv,dv);
	return(Math.sqrt(1. - d));
}
    
}
