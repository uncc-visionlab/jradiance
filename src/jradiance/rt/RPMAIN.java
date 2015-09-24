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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import jradiance.common.CALEXPR;
import jradiance.common.EXPANDARG;
import jradiance.common.FIXARGV0;
import jradiance.common.HEADER;
import jradiance.common.IMAGE;
import jradiance.common.OBJECT;
import jradiance.common.OCTREE;
import jradiance.common.OTYPES;
import jradiance.common.READOCT;
import jradiance.common.URAND;
import jradiance.rt.INITOTYPES.o_default;
import jradiance.util.VERSION;

/**
 *
 * @author arwillis
 */
public class RPMAIN {
/*
 *  rpmain.c - main for rpict batch rendering program
 */

//#include "copyright.h"
//
//#include  <time.h>
//#include  <signal.h>
//
//#include  "platform.h"
//#include  "rtprocess.h" /* getpid() */
//#include  "ray.h"
//#include  "source.h"
//#include  "ambient.h"
//#include  "random.h"
//#include  "paths.h"
//#include  "view.h"

					/* persistent processes define */
//#ifdef  F_SETLKW
//#define  PERSIST	1		/* normal persist */
//#define  PARALLEL	2		/* parallel persist */
//#define  PCHILD		3		/* child of normal persist */
//#endif

public static String progname;			/* argv[0] */
static String octname;				/* octree name */
//char  *sigerr[NSIG];			/* signal error messages */
//char  *shm_boundary = null;		/* boundary of shared memory */
static String errfile = null;			/* error output file */
static OutputStream stdout = System.out;
static InputStream stdin = System.in;
//extern time_t  time();
//extern time_t  tstart;			/* start time */
static long tstart;
//
//extern int  ralrm;			/* seconds between reports */
//
//extern VIEW  ourview;			/* viewing parameters */
//
//extern int  hresolu;			/* horizontal resolution */
//extern int  vresolu;			/* vertical resolution */
//extern double  pixaspect;		/* pixel aspect ratio */
//
//extern int  psample;			/* pixel sample size */
//extern double  maxdiff;			/* max. sample difference */
//extern double  dstrpix;			/* square pixel distribution */
//
//extern double  mblur;			/* motion blur parameter */
//
//extern double  dblur;			/* depth-of-field blur parameter */

//static void onsig(int signo);
//static void sigdie(int  signo, char  *msg);
//static void printdefaults(void);


public static void main( String[] argv) throws IOException
{
//#define	 check(ol,al)		if (argv[i][ol] || \
//				badarg(argc-i-1,argv+i+1,al)) \
//				goto badopt
//#define	 bool(olen,var)		switch (argv[i][olen]) { \
//				case '\0': var = !var; break; \
//				case 'y': case 'Y': case 't': case 'T': \
//				case '+': case '1': var = 1; break; \
//				case 'n': case 'N': case 'f': case 'F': \
//				case '-': case '0': var = 0; break; \
//				default: goto badopt; }
	String err;
	String recover = null;
	String outfile = null;
	String zfile = null;
	int  loadflags = ~OCTREE.IO_FILES;
	int  seqstart = 0;
	int  persist = 0;
	int  duped1;
	int  rval;
	int  i,argc;        
					/* record start time */
//	tstart = time((time_t *)null);
        tstart = System.currentTimeMillis();
					/* global program name */
        for (i = 0; i < argv.length; i++) {
            if (argv[i].charAt(0) == '-') {
                break;
            }
        }
        String[] argv2 = new String[argv.length - i + 1];
        for (int j = 1; j < argv2.length; j++) {
            argv2[j] = argv[i++];
        }
        argv2[0] = "rpict";
        argv = argv2;
        argc = argv2.length;
        progname = FIXARGV0.fixargv0(argv[0]);
//	progname = argv[0] = fixargv0(argv[0]);
					/* option city */
	for (i = 1; i < argc; i++) {
						/* expand arguments */
		while ((rval = EXPANDARG.expandarg(argc, argv, i)) > 0)
			;
		if (rval < 0) {
//			sprintf(errmsg, "cannot expand '%s'", argv[i]);
//			error(SYSTEM, errmsg);
		}
		if (argv[i] == null || argv[i].charAt(0) != '-')
			break;			/* break from options */
		if (argv[i].equals("-version")) {
			System.out.println(VERSION.VersionID);
			System.exit(0);
		}
		if (argv[i].equals("-defaults") ||
				argv[i].equals("-help")) {
//			printdefaults();
			System.exit(0);
		}
            String[] argv3 = new String[argv.length - i];
            for (int ii = i; ii < argv.length; ii++) {
                argv3[ii - i] = argv[ii];
            }
		rval = RENDEROPTS.getrenderopt(argc-i, argv3);
		if (rval >= 0) {
			i += rval;
			continue;
		}
            argv3 = new String[argv.length - i];
            for (int ii = i; ii < argv.length; ii++) {
                argv3[ii - i] = argv[ii];
            }
		rval = IMAGE.getviewopt(RPICT.ourview, argc-i, argv3);
		if (rval >= 0) {
			i += rval;
			continue;
		}
						/* rpict options */
		switch (argv[i].charAt(1)) {
		case 'v':				/* view file */
//			if (argv[i].charAt(2) != 'f')
//				goto badopt;
//			check(3,"s");
//			rval = viewfile(argv[++i], &ourview, null);
			if (rval < 0) {
//				sprintf(errmsg,
//				"cannot open view file \"%s\"",
//						argv[i]);
//				error(SYSTEM, errmsg);
			} else if (rval == 0) {
//				sprintf(errmsg,
//					"bad view file \"%s\"",
//						argv[i]);
//				error(USER, errmsg);
			}
			break;
		case 'p':				/* pixel */
			switch (argv[i].charAt(2)) {
			case 's':				/* sample */
//				check(3,"i");
				RPICT.psample = Integer.parseInt(argv[++i]);
				break;
			case 't':				/* threshold */
//				check(3,"f");
				RPICT.maxdiff = Float.parseFloat(argv[++i]);
				break;
			case 'j':				/* jitter */
//				check(3,"f");
				RPICT.dstrpix = Float.parseFloat(argv[++i]);
				break;
			case 'a':				/* aspect */
//				check(3,"f");
				RPICT.pixaspect = Float.parseFloat(argv[++i]);
				break;
			case 'm':				/* motion */
//				check(3,"f");
				RPICT.mblur = Float.parseFloat(argv[++i]);
				break;
			case 'd':				/* aperture */
//				check(3,"f");
				RPICT.dblur = Float.parseFloat(argv[++i]);
				break;
			default:
//				goto badopt;
			}
			break;
		case 'x':				/* x resolution */
//			check(2,"i");
			RPICT.hresolu = Integer.parseInt(argv[++i]);
			break;
		case 'y':				/* y resolution */
//			check(2,"i");
			RPICT.vresolu = Integer.parseInt(argv[++i]);
			break;
		case 'S':				/* slave index */
//			check(2,"i");
			seqstart = Integer.parseInt(argv[++i]);
			break;
		case 'o':				/* output file */
//			check(2,"s");
			outfile = argv[++i];
			break;
		case 'z':				/* z file */
//			check(2,"s");
			zfile = argv[++i];
			break;
		case 'r':				/* recover file */
			if (argv[i].charAt(2) == 'o') {		/* +output */
//				check(3,"s");
				outfile = argv[i+1];
			} else {
//				check(2,"s");
                        }
			recover = argv[++i];
			break;
		case 't':				/* timer */
//			check(2,"i");
			RPICT.ralrm = Integer.parseInt(argv[++i]);
			break;
//#ifdef  PERSIST
//		case 'P':				/* persist file */
//			if (argv[i][2] == 'P') {
//				check(3,"s");
//				persist = PARALLEL;
//			} else {
//				check(2,"s");
//				persist = PERSIST;
//			}
//			persistfile(argv[++i]);
//			break;
//#endif
		case 'w':				/* warnings */
//			rval = erract[WARNING].pf != null;
//			bool(2,rval);
//			if (rval) erract[WARNING].pf = wputs;
//			else erract[WARNING].pf = null;
                    System.out.println("Unsupported command line argument " + argv[i] + " found.");
                        
			break;
		case 'e':				/* error file */
//			check(2,"s");
			errfile = argv[++i];
			break;
		default:
//			goto badopt;
		}
	}
	err = IMAGE.setview(RPICT.ourview);	/* set viewing parameters */
	if (err != null) {
//		error(USER, err);
        }
					/* initialize object types */
        OTYPES.ot_initotypes(new o_default());
					/* initialize urand */
        /* initialize urand */
        if (RPICT.rand_samp != 0) {
            RAYCALLS.randsrc = new Random(System.currentTimeMillis());
            //srandom((long)time(0));
            URAND.initurand(0);
        } else {
            //srandom(0L);
            RAYCALLS.randsrc = new Random(0);
            URAND.initurand(2048);
        }
					/* set up signal handling */
//	sigdie(SIGINT, "Interrupt");
//#ifdef SIGHUP
//	sigdie(SIGHUP, "Hangup");
//#endif
//	sigdie(SIGTERM, "Terminate");
//#ifdef SIGPIPE
//	sigdie(SIGPIPE, "Broken pipe");
//#endif
//#ifdef SIGALRM
//	sigdie(SIGALRM, "Alarm clock");
//#endif
//#ifdef	SIGXCPU
//	sigdie(SIGXCPU, "CPU limit exceeded");
//	sigdie(SIGXFSZ, "File size exceeded");
//#endif
					/* open error file */
//	if (errfile != null) {
//		if (freopen(errfile, "a", stderr) == null)
//			quit(2);
//		fprintf(stderr, "**************\n*** PID %5d: ",
//				getpid());
//		printargs(argc, argv, stderr);
//		putc('\n', stderr);
//		fflush(stderr);
//	}
//#ifdef	NICE
//	nice(NICE);			/* lower priority */
//#endif
					/* get octree */
	if (i == argc)
		octname = null;
	else if (i == argc-1)
		octname = argv[i];
        else {
//		goto badopt;
        }
	if (seqstart > 0 && octname == null) {
//		error(USER, "missing octree argument");
        }
					/* set up output */
//#ifdef  PERSIST
//	if (persist) {
//		if (recover != null)
//			error(USER, "persist option used with recover file");
//		if (seqstart <= 0)
//			error(USER, "persist option only for sequences");
//		if (outfile == null)
//		duped1 = dup(fileno(stdout));	/* don't lose our output */
//		openheader();
//	} else
//#endif
	if (outfile != null) {
		DUPHEAD.openheader();
        }
//#ifdef	_WIN32
//	SET_FILE_BINARY(stdout);
//	if (octname == null)
//		SET_FILE_BINARY(stdin);
//#endif
	READOCT.readoct(octname, loadflags, RPICT.thescene, null);
	RPICT.nsceneobjs = OBJECT.nobjects;

	if ((loadflags & OCTREE.IO_INFO) != 0) {	/* print header */
		HEADER.printargs(i, argv, stdout);
		System.out.print(String.format("SOFTWARE= %s\n", VERSION.VersionID));
	}

	SOURCE.marksources();			/* find and mark sources */

	AMBIENT.setambient();			/* initialize ambient calculation */

//#ifdef  PERSIST
//	if (persist) {
//		fflush(stdout);
//		if (outfile == null) {		/* reconnect stdout */
//			dup2(duped1, fileno(stdout));
//			close(duped1);
//		}
//		if (persist == PARALLEL) {	/* multiprocessing */
//			preload_objs();		/* preload scene */
//			shm_boundary = (char *)malloc(16);
//			strcpy(shm_boundary, "SHM_BOUNDARY");
//			while ((rval=fork()) == 0) {	/* keep on forkin' */
//				pflock(1);
//				pfhold();
//				tstart = time((time_t *)null);
//				ambsync();		/* load new values */
//			}
//			if (rval < 0)
//				error(SYSTEM, "cannot fork child for persist function");
//			pfdetach();		/* parent will run then exit */
//		}
//	}
//runagain:
//	if (persist) {
//		if (outfile == null)			/* if out to stdout */
//			dupheader();			/* send header */
//		else					/* if out to file */
//			duped1 = dup(fileno(stdout));	/* hang onto pipe */
//	}
//#endif
					/* batch render picture(s) */
	RPICT.rpict(seqstart, outfile, zfile, recover);
					/* flush ambient file */
	AMBIENT.ambsync();
//#ifdef  PERSIST
//	if (persist == PERSIST) {	/* first run-through */
//		if ((rval=fork()) == 0) {	/* child loops until killed */
//			pflock(1);
//			persist = PCHILD;
//		} else {			/* original process exits */
//			if (rval < 0)
//				error(SYSTEM, "cannot fork child for persist function");
//			pfdetach();		/* parent exits */
//		}
//	}
//	if (persist == PCHILD) {	/* wait for a signal then go again */
//		if (outfile != null)
//			close(duped1);		/* release output handle */
//		pfhold();
//		tstart = time((time_t *)null);	/* reinitialize */
//		raynum = nrays = 0;
//		goto runagain;
//	}
//#endif
	System.exit(0);

//badopt:
//	sprintf(errmsg, "command line error at '%s'", argv[i]);
//	error(USER, errmsg);
//	return 1; /* pro forma return */
//
//#undef	check
//#undef	bool
}


void
wputs(				/* warning output function */
	String s
)
{
	int  lasterrno = CALEXPR.errno;
	eputs(s);
	CALEXPR.errno = lasterrno;
}

static int midline = 0;
void
eputs(				/* put string to stderr */
	String s
)
{
//	static int  midline = 0;

	if (s==null)
		return;
	if (midline++ == 0) {
		System.err.print(progname);
		System.err.print(": ");
	}
	System.err.print(s);
	if (s.endsWith("\n")) {
		System.err.flush();
		midline = 0;
	}
}


static void
onsig(				/* fatal signal */
	int  signo
)
{
//	static int  gotsig = 0;
//
//	if (gotsig++)			/* two signals and we're gone! */
//		_exit(signo);
//
//#ifdef SIGALRM /* XXX how critical is this? */
//	alarm(15);			/* allow 15 seconds to clean up */
//	signal(SIGALRM, SIG_DFL);	/* make certain we do die */
//#endif
//	eputs("signal - ");
//	eputs(sigerr[signo]);
//	eputs("\n");
//	quit(3);
}


static void
sigdie(			/* set fatal signal */
	int  signo,
	String msg
)
{
//	if (signal(signo, onsig) == SIG_IGN)
//		signal(signo, SIG_IGN);
//	sigerr[signo] = msg;
}


static void
printdefaults()			/* print default values to stdout */
{
//	printf("-vt%c\t\t\t\t# view type %s\n", ourview.type,
//			ourview.type==VT_PER ? "perspective" :
//			ourview.type==VT_PAR ? "parallel" :
//			ourview.type==VT_HEM ? "hemispherical" :
//			ourview.type==VT_ANG ? "angular" :
//			ourview.type==VT_CYL ? "cylindrical" :
//			ourview.type==VT_PLS ? "planisphere" :
//			"unknown");
//	printf("-vp %f %f %f\t# view point\n",
//			ourview.vp[0], ourview.vp[1], ourview.vp[2]);
//	printf("-vd %f %f %f\t# view direction\n",
//			ourview.vdir[0], ourview.vdir[1], ourview.vdir[2]);
//	printf("-vu %f %f %f\t# view up\n",
//			ourview.vup[0], ourview.vup[1], ourview.vup[2]);
//	printf("-vh %f\t\t\t# view horizontal size\n", ourview.horiz);
//	printf("-vv %f\t\t\t# view vertical size\n", ourview.vert);
//	printf("-vo %f\t\t\t# view fore clipping plane\n", ourview.vfore);
//	printf("-va %f\t\t\t# view aft clipping plane\n", ourview.vaft);
//	printf("-vs %f\t\t\t# view shift\n", ourview.hoff);
//	printf("-vl %f\t\t\t# view lift\n", ourview.voff);
//	printf("-x  %-9d\t\t\t# x resolution\n", hresolu);
//	printf("-y  %-9d\t\t\t# y resolution\n", vresolu);
//	printf("-pa %f\t\t\t# pixel aspect ratio\n", pixaspect);
//	printf("-pj %f\t\t\t# pixel jitter\n", dstrpix);
//	printf("-pm %f\t\t\t# pixel motion\n", mblur);
//	printf("-pd %f\t\t\t# pixel depth-of-field\n", dblur);
//	printf("-ps %-9d\t\t\t# pixel sample\n", psample);
//	printf("-pt %f\t\t\t# pixel threshold\n", maxdiff);
//	printf("-t  %-9d\t\t\t# time between reports\n", ralrm);
//	printf(erract[WARNING].pf != null ?
//			"-w+\t\t\t\t# warning messages on\n" :
//			"-w-\t\t\t\t# warning messages off\n");
//	print_rdefaults();
}
    
}
