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

import jradiance.common.MODOBJECT;
import jradiance.common.OBJECT;
import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OTYPES;

/**
 *
 * @author arwillis
 */
public class VIRTUALS {
//#ifndef lint
//static const char	RCSid[] = "$Id$";
//#endif
///*
// * Routines for simulating virtual light sources
// *	Thus far, we only support planar mirrors.
// *
// *  External symbols declared in source.h
// */
//
//#include "copyright.h"
//
//#include  "ray.h"
//
//#include  "otypes.h"
//
//#include  "source.h"
//
//#include  "random.h"

public static final int  MINSAMPLES=	16;		/* minimum number of pretest samples */
public static final int  STESTMAX=	32;		/* maximum seeks per sample */


static int  vobject;		/* virtual source objects */
static int  nvobjects = 0;		/* number of virtual source objects */


public static void
markvirtuals()			/* find and mark virtual sources */
{
	OBJREC  o;
	int  i;
					/* check number of direct relays */
	if (RAYCALLS.directrelay <= 0)
		return;
					/* find virtual source objects */
	for (i = 0; i < RAYCALLS.nsceneobjs; i++) {
		o = OBJECT.objptr(i);
		if (OTYPES.issurface(o.otype)==0 || o.omod == OBJECT.OVOID)
			continue;
		if (OTYPES.isvlight(VIRTUALS.vsmaterial(o).otype)==0)
			continue;
//		if (sfun[o->otype].of == NULL ||
//				sfun[o->otype].of->getpleq == NULL) {
//			objerror(o,WARNING,"secondary sources not supported");
//			continue;
//		}
//		if (nvobjects == 0)
//			vobject = (OBJECT *)malloc(sizeof(OBJECT));
//		else
//			vobject = (OBJECT *)realloc((void *)vobject,
//				(unsigned)(nvobjects+1)*sizeof(OBJECT));
//		if (vobject == NULL)
//			error(SYSTEM, "out of memory in addvirtuals");
//		vobject[nvobjects++] = i;
	}
	if (nvobjects == 0)
		return;
//#ifdef DEBUG
//	fprintf(stderr, "found %d virtual source objects\n", nvobjects);
//#endif
					/* append virtual sources */
//	for (i = nsources; i-- > 0; )
//		addvirtuals(i, directrelay);
					/* done with our object list */
//	free((void *)vobject);
	nvobjects = 0;
}


//extern void
//addvirtuals(		/* add virtuals associated with source */
//	int  sn,
//	int  nr
//)
//{
//	register int  i;
//				/* check relay limit first */
//	if (nr <= 0)
//		return;
//	if (source[sn].sflags & SSKIP)
//		return;
//				/* check each virtual object for projection */
//	for (i = 0; i < nvobjects; i++)
//					/* vproject() calls us recursively */
//		vproject(objptr(vobject[i]), sn, nr-1);
//}
//
//
//extern void
//vproject(		/* create projected source(s) if they exist */
//	OBJREC  *o,
//	int  sn,
//	int  n
//)
//{
//	register int  i;
//	register VSMATERIAL  *vsmat;
//	MAT4  proj;
//	int  ns;
//
//	if (o == source[sn].so)	/* objects cannot project themselves */
//		return;
//				/* get virtual source material */
//	vsmat = sfun[vsmaterial(o)->otype].mf;
//				/* project virtual sources */
//	for (i = 0; i < vsmat->nproj; i++)
//		if ((*vsmat->vproj)(proj, o, &source[sn], i))
//			if ((ns = makevsrc(o, sn, proj)) >= 0) {
//				source[ns].sa.sv.pn = i;
//#ifdef DEBUG
//				virtverb(ns, stderr);
//#endif
//				addvirtuals(ns, n);
//			}
//}
//
//
public static OBJREC 
vsmaterial(			/* get virtual source material pointer */
	OBJREC  o
)
{
	int  i;
	OBJREC  m;

	i = o.omod;
	m = SOURCE.findmaterial(OBJECT.objptr(i));
	if (m == null)
		return(OBJECT.objptr(i));
	if (m.otype != OTYPES.MAT_ILLUM || m.oargs.nsargs < 1 ||
			m.oargs.sarg[0].equals(OBJECT.VOIDID) ||
			(i = MODOBJECT.lastmod(MODOBJECT.objndx(m), m.oargs.sarg[0])) == OBJECT.OVOID)
		return(m);		/* direct modifier */
	return(OBJECT.objptr(i));		/* illum alternate */
}


//extern int
//makevsrc(		/* make virtual source if reasonable */
//	OBJREC  *op,
//	register int  sn,
//	MAT4  pm
//)
//{
//	FVECT  nsloc, nsnorm, ocent, v;
//	double  maxrad2, d;
//	int  nsflags;
//	SPOT  theirspot, ourspot;
//	register int  i;
//
//	nsflags = source[sn].sflags | (SVIRTUAL|SSPOT|SFOLLOW);
//					/* get object center and max. radius */
//	maxrad2 = getdisk(ocent, op, sn);
//	if (maxrad2 <= FTINY)			/* too small? */
//		return(-1);
//					/* get location and spot */
//	if (source[sn].sflags & SDISTANT) {		/* distant source */
//		if (source[sn].sflags & SPROX)
//			return(-1);		/* should never get here! */
//		multv3(nsloc, source[sn].sloc, pm);
//		normalize(nsloc);
//		VCOPY(ourspot.aim, ocent);
//		ourspot.siz = PI*maxrad2;
//		ourspot.flen = -1.;
//		if (source[sn].sflags & SSPOT) {
//			multp3(theirspot.aim, source[sn].sl.s->aim, pm);
//						/* adjust for source size */
//			d = sqrt(dist2(ourspot.aim, theirspot.aim));
//			d = sqrt(source[sn].sl.s->siz/PI) + d*source[sn].srad;
//			theirspot.siz = PI*d*d;
//			ourspot.flen = theirspot.flen = source[sn].sl.s->flen;
//			d = ourspot.siz;
//			if (!commonbeam(&ourspot, &theirspot, nsloc))
//				return(-1);	/* no overlap */
//			if (ourspot.siz < d-FTINY) {	/* it shrunk */
//				d = beamdisk(v, op, &ourspot, nsloc);
//				if (d <= FTINY)
//					return(-1);
//				if (d < maxrad2) {
//					maxrad2 = d;
//					VCOPY(ocent, v);
//				}
//			}
//		}
//	} else {				/* local source */
//		multp3(nsloc, source[sn].sloc, pm);
//		for (i = 0; i < 3; i++)
//			ourspot.aim[i] = ocent[i] - nsloc[i];
//		if ((d = normalize(ourspot.aim)) == 0.)
//			return(-1);		/* at source!! */
//		if (source[sn].sflags & SPROX && d > source[sn].sl.prox)
//			return(-1);		/* too far away */
//		ourspot.flen = 0.;
//						/* adjust for source size */
//		d = (sqrt(maxrad2) + source[sn].srad) / d;
//		if (d < 1.-FTINY)
//			ourspot.siz = 2.*PI*(1. - sqrt(1.-d*d));
//		else
//			nsflags &= ~SSPOT;
//		if (source[sn].sflags & SSPOT) {
//			theirspot = *(source[sn].sl.s);
//			multv3(theirspot.aim, source[sn].sl.s->aim, pm);
//			normalize(theirspot.aim);
//			if (nsflags & SSPOT) {
//				ourspot.flen = theirspot.flen;
//				d = ourspot.siz;
//				if (!commonspot(&ourspot, &theirspot, nsloc))
//					return(-1);	/* no overlap */
//			} else {
//				nsflags |= SSPOT;
//				ourspot = theirspot;
//				d = 2.*ourspot.siz;
//			}
//			if (ourspot.siz < d-FTINY) {	/* it shrunk */
//				d = spotdisk(v, op, &ourspot, nsloc);
//				if (d <= FTINY)
//					return(-1);
//				if (d < maxrad2) {
//					maxrad2 = d;
//					VCOPY(ocent, v);
//				}
//			}
//		}
//		if (source[sn].sflags & SFLAT) {	/* behind source? */
//			multv3(nsnorm, source[sn].snorm, pm);
//			normalize(nsnorm);
//			if (nsflags & SSPOT && !checkspot(&ourspot, nsnorm))
//				return(-1);
//		}
//	}
//					/* pretest visibility */
//	nsflags = vstestvis(nsflags, op, ocent, maxrad2, sn);
//	if (nsflags & SSKIP)
//		return(-1);	/* obstructed */
//					/* it all checks out, so make it */
//	if ((i = newsource()) < 0)
//		goto memerr;
//	source[i].sflags = nsflags;
//	VCOPY(source[i].sloc, nsloc);
//	multv3(source[i].ss[SU], source[sn].ss[SU], pm);
//	multv3(source[i].ss[SV], source[sn].ss[SV], pm);
//	if (nsflags & SFLAT)
//		VCOPY(source[i].snorm, nsnorm);
//	else
//		multv3(source[i].ss[SW], source[sn].ss[SW], pm);
//	source[i].srad = source[sn].srad;
//	source[i].ss2 = source[sn].ss2;
//	if (nsflags & SSPOT) {
//		if ((source[i].sl.s = (SPOT *)malloc(sizeof(SPOT))) == NULL)
//			goto memerr;
//		*(source[i].sl.s) = ourspot;
//	}
//	if (nsflags & SPROX)
//		source[i].sl.prox = source[sn].sl.prox;
//	source[i].sa.sv.sn = sn;
//	source[i].so = op;
//	return(i);
//memerr:
//	error(SYSTEM, "out of memory in makevsrc");
//	return -1; /* pro forma return */
//}
//
//
//extern double
//getdisk(		/* get visible object disk */
//	FVECT  oc,
//	OBJREC  *op,
//	register int  sn
//)
//{
//	double  rad2, roffs, offs, d, rd, rdoto;
//	FVECT  rnrm, nrm;
//				/* first, use object getdisk function */
//	rad2 = getmaxdisk(oc, op);
//	if (!(source[sn].sflags & SVIRTUAL))
//		return(rad2);		/* all done for normal source */
//				/* check for correct side of relay surface */
//	roffs = getplaneq(rnrm, source[sn].so);
//	rd = DOT(rnrm, source[sn].sloc);	/* source projection */
//	if (!(source[sn].sflags & SDISTANT))
//		rd -= roffs;
//	d = DOT(rnrm, oc) - roffs;	/* disk distance to relay plane */
//	if ((d > 0.) ^ (rd > 0.))
//		return(rad2);		/* OK if opposite sides */
//	if (d*d >= rad2)
//		return(0.);		/* no relay is possible */
//				/* we need a closer look */
//	offs = getplaneq(nrm, op);
//	rdoto = DOT(rnrm, nrm);
//	if (d*d >= rad2*(1.-rdoto*rdoto))
//		return(0.);		/* disk entirely on projection side */
//				/* should shrink disk but I'm lazy */
//	return(rad2);
//}
//
//
//extern int
//vstestvis(		/* pretest source visibility */
//	int  f,			/* virtual source flags */
//	OBJREC  *o,		/* relay object */
//	FVECT  oc,		/* relay object center */
//	double  or2,		/* relay object radius squared */
//	register int  sn	/* target source number */
//)
//{
//	RAY  sr;
//	FVECT  onorm;
//	FVECT  offsdir;
//	SRCINDEX  si;
//	double  or, d, d1;
//	int  stestlim, ssn;
//	int  nhit, nok;
//	register int  i, n;
//				/* return if pretesting disabled */
//	if (vspretest <= 0)
//		return(f);
//				/* get surface normal */
//	getplaneq(onorm, o);
//				/* set number of rays to sample */
//	if (source[sn].sflags & SDISTANT) {
//					/* 32. == heuristic constant */
//		n = 32.*or2/(thescene.cusize*thescene.cusize)*vspretest + .5;
//	} else {
//		for (i = 0; i < 3; i++)
//			offsdir[i] = source[sn].sloc[i] - oc[i];
//		d = DOT(offsdir,offsdir);
//		if (d <= FTINY)
//			n = 2.*PI * vspretest + .5;
//		else
//			n = 2.*PI * (1.-sqrt(1./(1.+or2/d)))*vspretest + .5;
//	}
//	if (n < MINSAMPLES) n = MINSAMPLES;
//#ifdef DEBUG
//	fprintf(stderr, "pretesting source %d in object %s with %d rays\n",
//			sn, o->oname, n);
//#endif
//				/* sample */
//	or = sqrt(or2);
//	stestlim = n*STESTMAX;
//	ssn = 0;
//	nhit = nok = 0;
//	initsrcindex(&si);
//	while (n-- > 0) {
//					/* get sample point */
//		do {
//			if (ssn >= stestlim) {
//#ifdef DEBUG
//				fprintf(stderr, "\ttoo hard to hit\n");
//#endif
//				return(f);	/* too small a target! */
//			}
//			multisamp(offsdir, 3, urand(sn*931+5827+ssn));
//			for (i = 0; i < 3; i++)
//				offsdir[i] = or*(1. - 2.*offsdir[i]);
//			ssn++;
//			d = 1. - DOT(offsdir, onorm);
//			for (i = 0; i < 3; i++) {
//				sr.rorg[i] = oc[i] + offsdir[i] + d*onorm[i];
//				sr.rdir[i] = -onorm[i];
//			}
//			rayorigin(&sr, PRIMARY, NULL, NULL);
//		} while (!(*ofun[o->otype].funp)(o, &sr));
//					/* check against source */
//		VCOPY(sr.rorg, sr.rop);	/* starting from intersection */
//		samplendx++;
//		if (si.sp >= si.np-1 ||
//				!srcray(&sr, NULL, &si) || sr.rsrc != sn) {
//			si.sn = sn-1;		/* reset index to our source */
//			si.np = 0;
//			if (!srcray(&sr, NULL, &si) || sr.rsrc != sn)
//				continue;	/* can't get there from here */
//		}
//		sr.revf = srcvalue;
//		rayvalue(&sr);			/* check sample validity */
//		if ((d = bright(sr.rcol)) <= FTINY)
//			continue;
//		nok++;			/* got sample; check obstructions */
//		rayclear(&sr);
//		sr.revf = raytrace;
//		rayvalue(&sr);
//		if ((d1 = bright(sr.rcol)) > FTINY) {
//			if (d - d1 > FTINY) {
//#ifdef DEBUG
//				fprintf(stderr, "\tpartially shadowed\n");
//#endif
//				return(f);	/* intervening transmitter */
//			}
//			nhit++;
//		}
//		if (nhit > 0 && nhit < nok) {
//#ifdef DEBUG
//			fprintf(stderr, "\tpartially occluded\n");
//#endif
//			return(f);		/* need to shadow test */
//		}
//	}
//	if (nhit == 0) {
//#ifdef DEBUG
//		fprintf(stderr, "\t0%% hit rate\n");
//#endif
//		return(f | SSKIP);	/* 0% hit rate:  totally occluded */
//	}
//#ifdef DEBUG
//	fprintf(stderr, "\t100%% hit rate\n");
//#endif
//	return(f & ~SFOLLOW);		/* 100% hit rate:  no occlusion */
//}
	

//#ifdef DEBUG
//extern void
//virtverb(	/* print verbose description of virtual source */
//	register int  sn,
//	FILE  *fp
//)
//{
//	fprintf(fp, "%s virtual source %d in %s %s\n",
//			source[sn].sflags & SDISTANT ? "distant" : "local",
//			sn, ofun[source[sn].so->otype].funame,
//			source[sn].so->oname);
//	fprintf(fp, "\tat (%f,%f,%f)\n",
//		source[sn].sloc[0], source[sn].sloc[1], source[sn].sloc[2]);
//	fprintf(fp, "\tlinked to source %d (%s)\n",
//		source[sn].sa.sv.sn, source[source[sn].sa.sv.sn].so->oname);
//	if (source[sn].sflags & SFOLLOW)
//		fprintf(fp, "\talways followed\n");
//	else
//		fprintf(fp, "\tnever followed\n");
//	if (!(source[sn].sflags & SSPOT))
//		return;
//	fprintf(fp, "\twith spot aim (%f,%f,%f) and size %f\n",
//			source[sn].sl.s->aim[0], source[sn].sl.s->aim[1],
//			source[sn].sl.s->aim[2], source[sn].sl.s->siz);
//}
//#endif
  
}
