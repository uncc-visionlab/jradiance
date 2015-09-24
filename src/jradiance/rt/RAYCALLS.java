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
import java.util.Arrays;
import java.util.Random;
import jradiance.common.COLORS;
import jradiance.common.COLORS.COLOR;
import jradiance.common.OBJECT;
import jradiance.common.OCTREE;
import jradiance.common.OCTREE.CUBE;
import jradiance.common.OTYPES;
import jradiance.common.PORTIO;
import jradiance.common.READOCT;
import jradiance.common.URAND;
import jradiance.rt.RAY.RAYPARAMS;

/**
 *
 * @author arwillis
 */
public class RAYCALLS {

    /*
     *  raycalls.c - interface for running Radiance rendering as a library
     *
     *  External symbols declared in ray.h
     */

    /*
     *  These routines are designed to aid the programmer who wishes
     *  to call Radiance as a library.  Unfortunately, the system was
     *  not originally intended to be run this way, and there are some
     *  awkward limitations to contend with.  The most irritating
     *  perhaps is that the global variables and functions do not have
     *  a prefix, and the symbols are a bit generic.  This results in a
     *  serious invasion of the calling application's name-space, and
     *  you may need to rename either some Radiance routines or some
     *  of your routines to avoid conflicts.  Another limitation is
     *  that the global variables are not gathered together into any
     *  sort of context, so it is impossible to simultaneously run
     *  this library on multiple scenes or in multiple threads.
     *  You get one scene and one thread, and if you want more, you
     *  will have to go with the process model defined in raypcalls.c.
     *  Finally, unrecoverable errors result in a call to the application-
     *  defined function quit().  The usual thing to do is to call exit().
     *  You might want to do something else instead, like
     *  call setjmp()/longjmp() to bring you back to the calling
     *  function for recovery.  You may also wish to define your own
     *  wputs(s) and eputs(s) functions to output warning and error
     *  messages, respectively.
     *
     *  With those caveats, we have attempted to make the interface
     *  as simple as we can.  Global variables and their defaults
     *  are defined below, and including "ray.h" declares these
     *  along with all the routines you are likely to need.  First,
     *  assign the global variable progname to your argv[0], then
     *  change the rendering parameters as you like.  If you have a set
     *  of option arguments you are working from, the getrenderopt(ac,av)
     *  call should be very useful.  Before tracing any rays, you
     *  must read in the octree with a call to ray_init(oct).
     *  Passing NULL for the file name causes ray_init() to read
     *  the octree from the standard input -- rarely a good idea.
     *  However, one may read an octree from a program (such as
     *  oconv) by preceding a shell command by a '!' character.
     *
     *  To trace a ray, define a RAY object myRay and assign:
     *
     *	myRay.rorg = ( ray origin point )
     *	myRay.rdir = ( normalized ray direction )
     *	myRay.rmax = ( maximum length, or zero for no limit )
     *
     *  If you are rendering from a VIEW structure, this can be
     *  accomplished with a single call for the ray at (x,y):
     *
     *	myRay.rmax = viewray(myRay.rorg, myRay.rdir, &myView, x, y);
     *
     *  Then, trace the primary ray with:
     *
     *	ray_trace(&myRay);
     *
     *  The resulting contents of myRay should provide you with
     *  more than enough information about what the ray hit,
     *  the computed value, etc.  For further clues of how to
     *  compute irradiance, how to get callbacks on the evaluated
     *  ray tree, etc., see the contents of rtrace.c.  See
     *  also the rpmain.c, rtmain.c, and rvmain.c modules
     *  to learn more how rendering options are processed.
     *
     *  When you are done, you may call ray_done(1) to clean
     *  up memory used by Radiance.  It doesn't free everything,
     *  but it makes a valiant effort.  If you call ray_done(0),
     *  it leaves data that is likely to be reused, including
     *  loaded data files and fonts.  The library may be
     *  restarted at any point by calling ray_init() on a new
     *  octree.
     *
     *  The call ray_save(rp) fills a parameter structure
     *  with the current global parameter settings, which may be
     *  restored at any time with a call to ray_restore(rp).
     *  This buffer contains no linked information, and thus
     *  may be passed between processes using write() and
     *  read() calls, so long as byte order is maintained.
     *  Calling ray_restore(NULL) restores the original
     *  default parameters, which is also retrievable with
     *  the call ray_defaults(rp).  (These  should be the
     *  same as the defaults for rtrace.)
     */

    String progname = "unknown_app";	/* caller sets to argv[0] */

    public static String octname;			/* octree name we are given */

    public static char[] shm_boundary = null;		/* boundary of shared memory */

    public static CUBE thescene = new CUBE();			/* our scene */

    public static int nsceneobjs;			/* number of objects in our scene */

    public static int[] dimlist = new int[RAY.MAXDIM];		/* sampling dimensions */

    public static int ndims = 0;			/* number of sampling dimensions */

    public static int samplendx = 0;			/* index for this sample */

    public static Random randsrc = null;
//void	(*trace)() = NULL;		/* trace call */

    void trace() {
    }
//void	(*addobjnotify[8])() = {ambnotify, NULL};
//void addobjnotify
    public static int do_irrad = 0;			/* compute irradiance? */

    public static int rand_samp = 0;			/* pure Monte Carlo sampling? */

    public static double dstrsrc = 0.0;			/* square source distribution */

    public static double shadthresh = .03;		/* shadow threshold */

    public static double shadcert = .75;			/* shadow certainty */

    public static int directrelay = 2;		/* number of source relays */

    public static int vspretest = 512;		/* virtual source pretest density */

    public static int directvis = 1;			/* sources visible? */

    public static double srcsizerat = .2;		/* maximum ratio source size/dist. */

    public static COLOR cextinction = COLOR.BLKCOLOR();		/* global extinction coefficient */

    public static COLOR salbedo = COLOR.BLKCOLOR();		/* global scattering albedo */

    public static double seccg = 0.;			/* global scattering eccentricity */

    public static double ssampdist = 0.;			/* scatter sampling distance */

    public static double specthresh = .15;		/* specular sampling threshold */

    public static double specjitter = 1.;		/* specular sampling jitter */

    public static int backvis = 1;			/* back face visibility */

    public static int maxdepth = 8;			/* maximum recursion depth */

    public static double minweight = 5e-4;		/* minimum ray weight */

    public static String ambfile = null;		/* ambient file name */

    public static final COLORS.COLOR ambval = COLORS.COLOR.BLKCOLOR();		/* ambient value */

    public static int ambvwt = 0;			/* initial weight for ambient value */

    public static double ambacc = 0.1;			/* ambient accuracy */

    public static int ambres = 256;			/* ambient resolution */

    public static int ambdiv = 1024;			/* ambient divisions */

    public static int ambssamp = 512;			/* ambient super-samples */

    public static int ambounce = 0;			/* ambient bounces */

    public static String[] amblist = new String[RAY.AMBLLEN + 1];		/* ambient include/exclude list */

    public static int ambincl = -1;			/* include == 1, exclude == 0 */


    public static double frandom() {
        return randsrc.nextDouble();
//        try {
//        return PORTIO.getflt(RVMAIN.frandomStr);
//        } catch (IOException e) {
//            System.exit(1);
//        }
//        return -1;
    }

    public static void srandom(long s) {
        randsrc.setSeed(s);
    }

    public static long random() {
//        return randsrc.nextLong();
        try {
        return PORTIO.getint(4, RVMAIN.randomStr);
        } catch (IOException e) {
            System.exit(1);
        }
        return -1;
    }

    static void ray_init( /* initialize ray-tracing calculation */
            String otnm) throws IOException {
        if (OBJECT.nobjects > 0) {		/* free old scene data */
            RAYCALLS.ray_done(0);
        }
        /* initialize object types */
        if (OTYPES.ofun[OTYPES.OBJ_SPHERE].ofunc instanceof INITOTYPES.o_default) {
            INITOTYPES.initotypes_raytrace();
        }
        /* initialize urand */
        if (rand_samp != 0) {
            randsrc = new Random(System.currentTimeMillis());
            //srandom((long)time(0));
            URAND.initurand(0);
        } else {
            //srandom(0L);
            randsrc = new Random(0);
            URAND.initurand(2048);
        }
        /* read scene octree */
        READOCT.readoct(octname = otnm, ~(OCTREE.IO_FILES | OCTREE.IO_INFO), thescene, null);
        nsceneobjs = OBJECT.nobjects;
        /* find and mark sources */
        SOURCE.marksources();
        /* initialize ambient calculation */
        AMBIENT.setambient();
        /* ready to go... */
    }

    static void ray_trace( /* trace a primary ray */
            RAY r) {
        RAYTRACE.rayorigin(r, RAY.PRIMARY, null, null);
        samplendx++;
        RAY.rayvalue(r);		/* assumes origin and direction are set */
        System.out.println("ray "+samplendx+" rno "+r.rno+" rdir = "+r.rdir+" rcol = "+r.rcol);
    }

    public static void ray_done( /* free ray-tracing data */
            int freall) throws IOException {
//	retainfonts = 1;
        AMBIENT.ambdone();
        AMBIENT.ambnotify(OBJECT.OVOID);
        SOURCE.freesources();
//	freeobjects(0, objsrc.nobjects);
//	donesets();
//	octdone();
        thescene.cutree = OCTREE.EMPTY;
        octname = null;
//	retainfonts = 0;
//	if (freall) {
//		freefont(NULL);
//		freedata(NULL);
//		SDfreeCache(NULL);
//		initurand(0);
//	}
        if (OBJECT.nobjects > 0) {
//		sprintf(errmsg, "%ld objects left after call to ray_done()",
//				(long)nobjects);
//		error(WARNING, errmsg);
        }
    }

//
    void ray_save( /* save current parameter settings */
            RAYPARAMS rp) {
//	int	i, ndx;
//
//	if (rp == NULL)
//		return;
//	rp->do_irrad = do_irrad;
//	rp->dstrsrc = dstrsrc;
//	rp->shadthresh = shadthresh;
//	rp->shadcert = shadcert;
//	rp->directrelay = directrelay;
//	rp->vspretest = vspretest;
//	rp->directvis = directvis;
//	rp->srcsizerat = srcsizerat;
//	copycolor(rp->cextinction, cextinction);
//	copycolor(rp->salbedo, salbedo);
//	rp->seccg = seccg;
//	rp->ssampdist = ssampdist;
//	rp->specthresh = specthresh;
//	rp->specjitter = specjitter;
//	rp->backvis = backvis;
//	rp->maxdepth = maxdepth;
//	rp->minweight = minweight;
//	copycolor(rp->ambval, ambval);
//	memset(rp->ambfile, '\0', sizeof(rp->ambfile));
//	if (ambfile != NULL)
//		strncpy(rp->ambfile, ambfile, sizeof(rp->ambfile)-1);
//	rp->ambvwt = ambvwt;
//	rp->ambacc = ambacc;
//	rp->ambres = ambres;
//	rp->ambdiv = ambdiv;
//	rp->ambssamp = ambssamp;
//	rp->ambounce = ambounce;
//	rp->ambincl = ambincl;
//	memset(rp->amblval, '\0', sizeof(rp->amblval));
//	ndx = 0;
//	for (i = 0; i < AMBLLEN && amblist[i] != NULL; i++) {
//		int	len = strlen(amblist[i]);
//		if (ndx+len >= sizeof(rp->amblval))
//			break;
//		strcpy(rp->amblval+ndx, amblist[i]);
//		ndx += len+1;
//	}
//	while (i <= AMBLLEN)
//		rp->amblndx[i++] = -1;
    }

    void ray_restore( /* restore parameter settings */
            RAYPARAMS rp) {
//	register int	i;
//
//	if (rp == NULL) {		/* restore defaults */
//		RAYPARAMS	dflt;
//		ray_defaults(&dflt);
//		ray_restore(&dflt);
//		return;
//	}
//					/* restore saved settings */
//	do_irrad = rp->do_irrad;
//	dstrsrc = rp->dstrsrc;
//	shadthresh = rp->shadthresh;
//	shadcert = rp->shadcert;
//	directrelay = rp->directrelay;
//	vspretest = rp->vspretest;
//	directvis = rp->directvis;
//	srcsizerat = rp->srcsizerat;
//	copycolor(cextinction, rp->cextinction);
//	copycolor(salbedo, rp->salbedo);
//	seccg = rp->seccg;
//	ssampdist = rp->ssampdist;
//	specthresh = rp->specthresh;
//	specjitter = rp->specjitter;
//	backvis = rp->backvis;
//	maxdepth = rp->maxdepth;
//	minweight = rp->minweight;
//	copycolor(ambval, rp->ambval);
//	ambvwt = rp->ambvwt;
//	ambdiv = rp->ambdiv;
//	ambssamp = rp->ambssamp;
//	ambounce = rp->ambounce;
//	for (i = 0; rp->amblndx[i] >= 0; i++)
//		amblist[i] = rp->amblval + rp->amblndx[i];
//	while (i <= AMBLLEN)
//		amblist[i++] = NULL;
//	ambincl = rp->ambincl;
//					/* update ambient calculation */
//	ambnotify(OVOID);
//	if (thescene.cutree != EMPTY) {
//		int	newamb = (ambfile == NULL) ?  rp->ambfile[0] :
//					strcmp(ambfile, rp->ambfile) ;
//
//		if (amblist[0] != NULL)
//			for (i = 0; i < nobjects; i++)
//				ambnotify(i);
//
//		ambfile = (rp->ambfile[0]) ? rp->ambfile : (char *)NULL;
//		if (newamb) {
//			ambres = rp->ambres;
//			ambacc = rp->ambacc;
//			setambient();
//		} else {
//			setambres(rp->ambres);
//			setambacc(rp->ambacc);
//		}
//	} else {
//		ambfile = (rp->ambfile[0]) ? rp->ambfile : (char *)NULL;
//		ambres = rp->ambres;
//		ambacc = rp->ambacc;
//	}
    }

    void ray_defaults( /* get default parameter values */
            RAYPARAMS rp) {
        int i;

        if (rp == null) {
            return;
        }

        rp.do_irrad = 0;
        rp.dstrsrc = 0.0;
        rp.shadthresh = .03;
        rp.shadcert = .75;
        rp.directrelay = 2;
        rp.vspretest = 512;
        rp.directvis = 1;
        rp.srcsizerat = .2;
        rp.cextinction.setcolor(0.f, 0.f, 0.f);
        rp.salbedo.setcolor(0.f, 0.f, 0.f);
        rp.seccg = 0.;
        rp.ssampdist = 0.;
        rp.specthresh = .15;
        rp.specjitter = 1.;
        rp.backvis = 1;
        rp.maxdepth = 8;
        rp.minweight = 2e-3;
        rp.ambval.setcolor(0f, 0f, 0f);
        Arrays.fill(rp.ambfile, (char) 0);
        //memset(rp.ambfile, '\0', sizeof(rp.ambfile));
        rp.ambvwt = 0;
        rp.ambres = 256;
        rp.ambacc = 0.15;
        rp.ambdiv = 1024;
        rp.ambssamp = 512;
        rp.ambounce = 0;
        rp.ambincl = -1;
        Arrays.fill(rp.amblval, (char) 0);
        //memset(rp.amblval, '\0', sizeof(rp.amblval));
        for (i = RAY.AMBLLEN + 1; i-- != 0;) {
            rp.amblndx[i] = -1;
        }
    }
}
