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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import jradiance.common.CALEXPR;
import jradiance.common.COLORS;
import jradiance.common.COLORS.COLOR;
import jradiance.common.HEADER;
import jradiance.common.IMAGE;
import jradiance.common.OCTREE.CUBE;
import jradiance.common.RESOLU;
import jradiance.common.VIEW;
import jradiance.common.WORDS;

/**
 *
 * @author arwillis
 */
public class RPICT {
//#ifndef lint
//static const char RCSid[] = "$Id$";
//#endif
/*
 *  rpict.c - routines and variables for picture generation.
 */

//#include "copyright.h"
//
//#include  <sys/types.h>
//
//#include  "platform.h"
//#ifdef NON_POSIX
// #ifdef MINGW
//  #include  <sys/time.h>
// #endif
//#else
// #ifdef BSD
//  #include  <sys/time.h>
//  #include  <sys/resource.h>
// #else
//  #include  <sys/times.h>
//  #include  <unistd.h>
// #endif
//#endif

//#include  <time.h>
//#include  <signal.h>
//
//#include  "ray.h"
//#include  "paths.h"
//#include  "ambient.h"
//#include  "view.h"
//#include  "random.h"
//#include  "paths.h"
//#include  "rtmisc.h" /* myhostname() */


static String	 RFTEMPLATE=	"rfXXXXXX";
static String	 RFTEMPLATE2=	"rf_";

//#ifndef SIGCONT
//#ifdef SIGIO     /* XXX can we live without this? */
//#define SIGCONT		SIGIO
//#endif
//#endif

public static CUBE  thescene;				/* our scene */
public static int	nsceneobjs;			/* number of objects in our scene */

//public static int[]  dimlist = new int[RAY.MAXDIM];			/* sampling dimensions */
//public static int  ndims = 0;				/* number of sampling dimensions */
int  samplendx;				/* sample index number */

//void  (*addobjnotify[])() = {ambnotify, null};

static VIEW  ourview = VIEW.STDVIEW;		/* view parameters */
static int  hresolu = 512;			/* horizontal resolution */
static int  vresolu = 512;			/* vertical resolution */
static double	pixaspect = 1.0;		/* pixel aspect ratio */
public static double pa;

static int  psample = 4;			/* pixel sample size */
static double	maxdiff = .05;			/* max. difference for interpolation */
static double	dstrpix = 0.67;			/* square pixel distribution */

static double  mblur = 0.;			/* motion blur parameter */

static double  dblur = 0.;			/* depth-of-field blur parameter */

//void  (*trace)() = null;		/* trace call */

public static int  do_irrad = 0;			/* compute irradiance? */

static int  rand_samp = 0;			/* pure Monte Carlo sampling? */

double	dstrsrc = 0.0;			/* square source distribution */
double	shadthresh = .05;		/* shadow threshold */
double	shadcert = .5;			/* shadow certainty */
public static int  directrelay = 1;			/* number of source relays */
int  vspretest = 512;			/* virtual source pretest density */
int  directvis = 1;			/* sources visible? */
double	srcsizerat = .25;		/* maximum ratio source size/dist. */

COLOR  cextinction = COLOR.BLKCOLOR();		/* global extinction coefficient */
COLOR  salbedo = COLOR.BLKCOLOR();		/* global scattering albedo */
double  seccg = 0.;			/* global scattering eccentricity */
double  ssampdist = 0.;			/* scatter sampling distance */

public static double	specthresh = .15;		/* specular sampling threshold */
double	specjitter = 1.;		/* specular sampling jitter */

public static int  backvis = 1;			/* back face visibility */

int  maxdepth = 7;			/* maximum recursion depth */
double	minweight = 1e-3;		/* minimum ray weight */

String ambfile = null;			/* ambient file name */
COLORS.COLOR  ambval = COLORS.COLOR.BLKCOLOR();		/* ambient value */
int  ambvwt = 0;			/* initial weight for ambient value */
double	ambacc = 0.2;			/* ambient accuracy */
int  ambres = 64;			/* ambient resolution */
int  ambdiv = 512;			/* ambient divisions */
int  ambssamp = 128;			/* ambient super-samples */
public static int  ambounce = 0;			/* ambient bounces */
//char  *amblist[AMBLLEN];		/* ambient include/exclude list */
int  ambincl = -1;			/* include == 1, exclude == 0 */

static int  ralrm = 0;				/* seconds between reports */

static double	pctdone = 0.0;			/* percentage done */
//time_t  tlastrept = 0L;			/* time at last report */
//time_t  tstart;				/* starting time */
//
public static final int MAXDIV=		16;		/* maximum sample size */
//
//#define	 pixjitter()	(.5+dstrpix*(.5-frandom()))
//
public static int  hres, vres;			/* resolution for this frame */
//
static VIEW	lastview;		/* the previous view input */
//
//static void report(int);
//static int nextview(FILE *fp);
//static void render(char *zfile, char *oldfile);
//static void fillscanline(COLOR *scanline, float *zline, char *sd, int xres,
//		int y, int xstep);
//static void fillscanbar(COLOR *scanbar[], float *zbar[], int xres,
//		int y, int ysize);
//static int fillsample(COLOR *colline, float *zline, int x, int y,
//		int xlen, int ylen, int b);
//static double pixvalue(COLOR  col, int  x, int  y);
//static int salvage(char  *oldfile);
//static int pixnumber(int  x, int  y, int  xres, int  yres);
//
//
//
//#ifdef RHAS_STAT
//#include  <sys/types.h>
//#include  <sys/stat.h>
//int
//file_exists(fname)				/* ordinary file exists? */
//char  *fname;
//{
//	struct stat  sbuf;
//	if (stat(fname, &sbuf) < 0) return(0);
//	return((sbuf.st_mode & S_IFREG) != 0);
//}
//#else
//#define  file_exists(f)	(access(f,F_OK)==0)
//#endif
//
//
//void
//quit(code)			/* quit program */
//int  code;
//{
//	if (code)			/* report status */
//		report(0);
//#ifndef NON_POSIX
//	headclean();			/* delete header file */
//	pfclean();			/* clean up persist files */
//#endif
//	exit(code);
//}
//
//
//#ifndef NON_POSIX
//static void
//report(int dummy)		/* report progress */
//{
//	double  u, s;
//#ifdef BSD
//	struct rusage  rubuf;
//#else
//	struct tms  tbuf;
//	double  period;
//#endif
//
//	tlastrept = time((time_t *)null);
//#ifdef BSD
//	getrusage(RUSAGE_SELF, &rubuf);
//	u = rubuf.ru_utime.tv_sec + rubuf.ru_utime.tv_usec/1e6;
//	s = rubuf.ru_stime.tv_sec + rubuf.ru_stime.tv_usec/1e6;
//	getrusage(RUSAGE_CHILDREN, &rubuf);
//	u += rubuf.ru_utime.tv_sec + rubuf.ru_utime.tv_usec/1e6;
//	s += rubuf.ru_stime.tv_sec + rubuf.ru_stime.tv_usec/1e6;
//#else
//	times(&tbuf);
//#ifdef _SC_CLK_TCK
//	period = 1.0 / sysconf(_SC_CLK_TCK);
//#else
//	period = 1.0 / 60.0;
//#endif
//	u = ( tbuf.tms_utime + tbuf.tms_cutime ) * period;
//	s = ( tbuf.tms_stime + tbuf.tms_cstime ) * period;
//#endif
//
//	sprintf(errmsg,
//		"%lu rays, %4.2f%% after %.3fu %.3fs %.3fr hours on %s\n",
//			nrays, pctdone, u/3600., s/3600.,
//			(tlastrept-tstart)/3600., myhostname());
//	eputs(errmsg);
//#ifdef SIGCONT
//	signal(SIGCONT, report);
//#endif
//}
//#else
//static void
//report(int dummy)		/* report progress */
//{
//	tlastrept = time((time_t *)null);
//	sprintf(errmsg, "%lu rays, %4.2f%% after %5.4f hours\n",
//			nrays, pctdone, (tlastrept-tstart)/3600.0);
//	eputs(errmsg);
//}
//#endif
//
//
static void
rpict(			/* generate image(s) */
	int  seq,
	String pout,
	String zout,
	String prvr
) throws IOException
/*
 * If seq is greater than zero, then we will render a sequence of
 * images based on view parameter strings read from the standard input.
 * If pout is null, then all images will be sent to the standard ouput.
 * If seq is greater than zero and prvr is an integer, then it is the
 * frame number at which rendering should begin.  Preceeding view parameter
 * strings will be skipped in the input.
 * If pout and prvr are the same, prvr is renamed to avoid overwriting.
 * Note that pout and zout should contain %d format specifications for
 * sequenced file naming.
 */
{
	String  fbuf = "", fbuf2 = "";
	int  npicts;
	String  cp;
	RESOLU	rs = new RESOLU();
//	double	pa;
					/* check sampling */
	if (psample < 1)
		psample = 1;
	else if (psample > MAXDIV) {
//		sprintf(errmsg, "pixel sampling reduced from %d to %d",
//				psample, MAXDIV);
//		error(WARNING, errmsg);
		psample = MAXDIV;
	}
					/* get starting frame */
	if (seq <= 0)
		seq = 0;
	else if (prvr != null && WORDS.isint(prvr.toCharArray())!=0) {
		int  rn;		/* skip to specified view */
		if ((rn = Integer.parseInt(prvr)) < seq) {
//			error(USER, "recover frame less than start frame");
                }
		if (pout == null) {
//			error(USER, "missing output file specification");
                }
		for ( ; seq < rn; seq++) {
			if (nextview(RPMAIN.stdin) == CALEXPR.EOF) {
//				error(USER, "unexpected EOF on view input");
                        }
                }
		IMAGE.setview(ourview);
		prvr = fbuf;			/* mark for renaming */
	}
	if ((pout != null) & (prvr != null)) {
		//sprintf(fbuf, pout, seq);
                fbuf = String.format(pout, seq);
		if (prvr.equals(fbuf)) {	/* rename */
			fbuf2 = fbuf;
                        int cpidx = fbuf2.lastIndexOf(System.getProperty("file.separator"));
//			for (cp = fbuf2; *cp; cp++)
//				;
//			while (cp > fbuf2 && !ISDIRSEP(cp[-1]))
//				cp--;
//			strcpy(cp, RFTEMPLATE);
                        cp = fbuf2.substring(0,cpidx);
                        cp += RFTEMPLATE2;
			prvr = mktemp(fbuf2);
//			if (rename(fbuf, prvr) < 0) {
//				if (errno == ENOENT) {	/* ghost file */
//					sprintf(errmsg,
//						"new output file \"%s\"",
//						fbuf);
//					error(WARNING, errmsg);
//					prvr = null;
//				} else {		/* serious error */
//					sprintf(errmsg,
//					"cannot rename \"%s\" to \"%s\"",
//						fbuf, prvr);
//					error(SYSTEM, errmsg);
//				}
//			}
		}
	}
	npicts = 0;			/* render sequence */
	do {
		if (seq != 0 && nextview(RPMAIN.stdin) == CALEXPR.EOF)
			break;
		pctdone = 0.0;
		if (pout != null) {
//			sprintf(fbuf, pout, seq);
                    fbuf = String.format(pout, seq);
                    if (new File(fbuf).exists()) {
				if (prvr != null || !fbuf.equals(pout)) {
//					sprintf(errmsg,
//						"output file \"%s\" exists",
//						fbuf);
//					error(USER, errmsg);
				}
				IMAGE.setview(RPICT.ourview);
				continue;		/* don't clobber */
			}
			if ((RPMAIN.stdout = new FileOutputStream(fbuf)) == null) {
//				sprintf(errmsg,
//					"cannot open output file \"%s\"", fbuf);
//				error(SYSTEM, errmsg);
			}
//			SET_FILE_BINARY(stdout);
			DUPHEAD.dupheader();
		}
		hres = hresolu; vres = vresolu; pa = pixaspect;
		if (prvr != null) {
//			if (viewfile(prvr, ourview, &rs) <= 0) {
//				sprintf(errmsg,
//			"cannot recover view parameters from \"%s\"", prvr);
//				error(WARNING, errmsg);
//			} else {
				pa = 0.0;
				hres = RESOLU.scanlen(rs);
				vres = RESOLU.numscans(rs);
//			}
		}
		if ((cp = IMAGE.setview(ourview)) != null) {
//			error(USER, cp);
                }
//		IMAGE.normaspect(VIEW.viewaspect(RPICT.ourview), &pa, &hres, &vres);
		IMAGE.normaspect(VIEW.viewaspect(RPICT.ourview));
		if (seq != 0) {
			if (ralrm > 0) {
//				fprintf(stderr, "FRAME %d:", seq);
//				fprintview(&ourview, stderr);
//				putc('\n', stderr);
//				fflush(stderr);
			}
			System.out.print(String.format("FRAME=%d\n", seq));
		}
		RPMAIN.stdout.write(VIEW.VIEWSTR.getBytes());
		IMAGE.fprintview(ourview, RPMAIN.stdout);
		RPMAIN.stdout.write('\n');
		if (pa < .99 || pa > 1.01) {
			COLORS.fputaspect(pa, RPMAIN.stdout);
                }
		HEADER.fputnow(RPMAIN.stdout);
		HEADER.fputformat(COLORS.COLRFMT, RPMAIN.stdout);
		RPMAIN.stdout.write('\n');
		if (zout != null) {
			cp = String.format(fbuf, zout, seq);
                }
                else {
			cp = null;
                }
		render(cp, prvr);
		prvr = null;
		npicts++;
	} while (seq++ != 0);
					/* check that we did something */
	if (npicts == 0) {
//		error(WARNING, "no output produced");
        }
}


static int
nextview(				/* get next view from fp */
	InputStream fp
) throws IOException
{
        int sizeoflinebuf = 256;
	char[]  linebuf = new char[sizeoflinebuf];
	lastview = ourview;
	while (CALEXPR.fgets(linebuf, 256, fp) != null)
		if (IMAGE.isview(linebuf)!=0 && IMAGE.sscanview(ourview, new String(linebuf)) > 0)
			return(0);
	return(CALEXPR.EOF);
}	


static void
render(				/* render the scene */
	String zfile,
	String oldfile
)
{
//	COLOR  *scanbar[MAXDIV+1];	/* scanline arrays of pixel values */
//	float  *zbar[MAXDIV+1];		/* z values */
//	char  *sampdens;		/* previous sample density */
//	int  ypos;			/* current scanline */
//	int  ystep;			/* current y step size */
//	int  hstep;			/* h step size */
//	int  zfd;
//	COLOR  *colptr;
//	float  *zptr;
//	register int  i;
//					/* check for empty image */
//	if (hres <= 0 || vres <= 0) {
//		error(WARNING, "empty output picture");
//		fprtresolu(0, 0, stdout);
//		return;
//	}
//					/* allocate scanlines */
//	for (i = 0; i <= psample; i++) {
//		scanbar[i] = (COLOR *)malloc(hres*sizeof(COLOR));
//		if (scanbar[i] == null)
//			goto memerr;
//	}
//	hstep = (psample*140+49)/99;		/* quincunx sampling */
//	ystep = (psample*99+70)/140;
//	if (hstep > 2) {
//		i = hres/hstep + 2;
//		if ((sampdens = (char *)malloc(i)) == null)
//			goto memerr;
//		while (i--)
//			sampdens[i] = hstep;
//	} else
//		sampdens = null;
//					/* open z-file */
//	if (zfile != null) {
//		if ((zfd = open(zfile, O_WRONLY|O_CREAT, 0666)) == -1) {
//			sprintf(errmsg, "cannot open z-file \"%s\"", zfile);
//			error(SYSTEM, errmsg);
//		}
//		SET_FD_BINARY(zfd);
//		for (i = 0; i <= psample; i++) {
//			zbar[i] = (float *)malloc(hres*sizeof(float));
//			if (zbar[i] == null)
//				goto memerr;
//		}
//	} else {
//		zfd = -1;
//		for (i = 0; i <= psample; i++)
//			zbar[i] = null;
//	}
//					/* write out boundaries */
//	fprtresolu(hres, vres, stdout);
//					/* recover file and compute first */
//	i = salvage(oldfile);
//	if (i >= vres)
//		goto alldone;
//	if (zfd != -1 && i > 0 &&
//			lseek(zfd, (off_t)i*hres*sizeof(float), SEEK_SET) < 0)
//		error(SYSTEM, "z-file seek error in render");
//	pctdone = 100.0*i/vres;
//	if (ralrm > 0)			/* report init stats */
//		report(0);
//#ifdef SIGCONT
//	else
//	signal(SIGCONT, report);
//#endif
//	ypos = vres-1 - i;			/* initialize sampling */
//	if (directvis)
//		init_drawsources(psample);
//	fillscanline(scanbar[0], zbar[0], sampdens, hres, ypos, hstep);
//						/* compute scanlines */
//	for (ypos -= ystep; ypos > -ystep; ypos -= ystep) {
//							/* bottom adjust? */
//		if (ypos < 0) {
//			ystep += ypos;
//			ypos = 0;
//		}
//		colptr = scanbar[ystep];		/* move base to top */
//		scanbar[ystep] = scanbar[0];
//		scanbar[0] = colptr;
//		zptr = zbar[ystep];
//		zbar[ystep] = zbar[0];
//		zbar[0] = zptr;
//							/* fill base line */
//		fillscanline(scanbar[0], zbar[0], sampdens,
//				hres, ypos, hstep);
//							/* fill bar */
//		fillscanbar(scanbar, zbar, hres, ypos, ystep);
//		if (directvis)				/* add bitty sources */
//			drawsources(scanbar, zbar, 0, hres, ypos, ystep);
//							/* write it out */
//#ifdef SIGCONT
//		signal(SIGCONT, SIG_IGN);	/* don't interrupt writes */
//#endif
//		for (i = ystep; i > 0; i--) {
//			if (zfd != -1 && write(zfd, (char *)zbar[i],
//					hres*sizeof(float))
//					< hres*sizeof(float))
//				goto writerr;
//			if (fwritescan(scanbar[i], hres, stdout) < 0)
//				goto writerr;
//		}
//		if (fflush(stdout) == EOF)
//			goto writerr;
//							/* record progress */
//		pctdone = 100.0*(vres-1-ypos)/vres;
//		if (ralrm > 0 && time((time_t *)null) >= tlastrept+ralrm)
//			report(0);
//#ifdef SIGCONT
//		else
//			signal(SIGCONT, report);
//#endif
//	}
//						/* clean up */
//#ifdef SIGCONT
//	signal(SIGCONT, SIG_IGN);
//#endif
//	if (zfd != -1 && write(zfd, (char *)zbar[0], hres*sizeof(float))
//				< hres*sizeof(float))
//		goto writerr;
//	fwritescan(scanbar[0], hres, stdout);
//	if (fflush(stdout) == EOF)
//		goto writerr;
//alldone:
//	if (zfd != -1) {
//		if (close(zfd) == -1)
//			goto writerr;
//		for (i = 0; i <= psample; i++)
//			free((void *)zbar[i]);
//	}
//	for (i = 0; i <= psample; i++)
//		free((void *)scanbar[i]);
//	if (sampdens != null)
//		free(sampdens);
//	pctdone = 100.0;
//	if (ralrm > 0)
//		report(0);
//#ifdef SIGCONT
//	signal(SIGCONT, SIG_DFL);
//#endif
//	return;
//writerr:
//	error(SYSTEM, "write error in render");
//memerr:
//	error(SYSTEM, "out of memory in render");
}


//static void
//fillscanline(	/* fill scan at y */
//	register COLOR	*scanline,
//	register float	*zline,
//	register char  *sd,
//	int  xres,
//	int  y,
//	int  xstep
//)
//{
//	static int  nc = 0;		/* number of calls */
//	int  bl = xstep, b = xstep;
//	double	z;
//	register int  i;
//
//	z = pixvalue(scanline[0], 0, y);
//	if (zline) zline[0] = z;
//				/* zig-zag start for quincunx pattern */
//	for (i = ++nc & 1 ? xstep : xstep/2; i < xres-1+xstep; i += xstep) {
//		if (i >= xres) {
//			xstep += xres-1-i;
//			i = xres-1;
//		}
//		z = pixvalue(scanline[i], i, y);
//		if (zline) zline[i] = z;
//		if (sd) b = sd[0] > sd[1] ? sd[0] : sd[1];
//		if (i <= xstep)
//			b = fillsample(scanline, zline, 0, y, i, 0, b/2);
//		else
//			b = fillsample(scanline+i-xstep,
//					zline ? zline+i-xstep : (float *)null,
//					i-xstep, y, xstep, 0, b/2);
//		if (sd) *sd++ = nc & 1 ? bl : b;
//		bl = b;
//	}
//	if (sd && nc & 1) *sd = bl;
//}
//
//
//static void
//fillscanbar(	/* fill interior */
//	register COLOR	*scanbar[],
//	register float	*zbar[],
//	int  xres,
//	int  y,
//	int  ysize
//)
//{
//	COLOR  vline[MAXDIV+1];
//	float  zline[MAXDIV+1];
//	int  b = ysize;
//	register int  i, j;
//
//	for (i = 0; i < xres; i++) {
//		copycolor(vline[0], scanbar[0][i]);
//		copycolor(vline[ysize], scanbar[ysize][i]);
//		if (zbar[0]) {
//			zline[0] = zbar[0][i];
//			zline[ysize] = zbar[ysize][i];
//		}
//		b = fillsample(vline, zbar[0] ? zline : (float *)null,
//				i, y, 0, ysize, b/2);
//
//		for (j = 1; j < ysize; j++)
//			copycolor(scanbar[j][i], vline[j]);
//		if (zbar[0])
//			for (j = 1; j < ysize; j++)
//				zbar[j][i] = zline[j];
//	}
//}
//
//
//static int
//fillsample( /* fill interior points */
//	register COLOR	*colline,
//	register float	*zline,
//	int  x,
//	int  y,
//	int  xlen,
//	int  ylen,
//	int  b
//)
//{
//	double	ratio;
//	double	z;
//	COLOR  ctmp;
//	int  ncut;
//	register int  len;
//
//	if (xlen > 0)			/* x or y length is zero */
//		len = xlen;
//	else
//		len = ylen;
//
//	if (len <= 1)			/* limit recursion */
//		return(0);
//
//	if (b > 0 ||
//	(zline && 2.*fabs(zline[0]-zline[len]) > maxdiff*(zline[0]+zline[len]))
//			|| bigdiff(colline[0], colline[len], maxdiff)) {
//
//		z = pixvalue(colline[len>>1], x + (xlen>>1), y + (ylen>>1));
//		if (zline) zline[len>>1] = z;
//		ncut = 1;
//	} else {					/* interpolate */
//		copycolor(colline[len>>1], colline[len]);
//		ratio = (double)(len>>1) / len;
//		scalecolor(colline[len>>1], ratio);
//		if (zline) zline[len>>1] = zline[len] * ratio;
//		ratio = 1.0 - ratio;
//		copycolor(ctmp, colline[0]);
//		scalecolor(ctmp, ratio);
//		addcolor(colline[len>>1], ctmp);
//		if (zline) zline[len>>1] += zline[0] * ratio;
//		ncut = 0;
//	}
//							/* recurse */
//	ncut += fillsample(colline, zline, x, y, xlen>>1, ylen>>1, (b-1)/2);
//
//	ncut += fillsample(colline+(len>>1),
//			zline ? zline+(len>>1) : (float *)null,
//			x+(xlen>>1), y+(ylen>>1),
//			xlen-(xlen>>1), ylen-(ylen>>1), b/2);
//
//	return(ncut);
//}
//
//
//static double
//pixvalue(		/* compute pixel value */
//	COLOR  col,			/* returned color */
//	int  x,			/* pixel position */
//	int  y
//)
//{
//	RAY  thisray;
//	FVECT	lorg, ldir;
//	double	hpos, vpos, vdist, lmax;
//	register int	i;
//						/* compute view ray */
//	hpos = (x+pixjitter())/hres;
//	vpos = (y+pixjitter())/vres;
//	if ((thisray.rmax = viewray(thisray.rorg, thisray.rdir,
//					&ourview, hpos, vpos)) < -FTINY) {
//		setcolor(col, 0.0, 0.0, 0.0);
//		return(0.0);
//	}
//	vdist = ourview.vdist;
//						/* set pixel index */
//	samplendx = pixnumber(x,y,hres,vres);
//						/* optional motion blur */
//	if (lastview.type && mblur > FTINY && (lmax = viewray(lorg, ldir,
//					&lastview, hpos, vpos)) >= -FTINY) {
//		register double  d = mblur*(.5-urand(281+samplendx));
//
//		thisray.rmax = (1.-d)*thisray.rmax + d*lmax;
//		for (i = 3; i--; ) {
//			thisray.rorg[i] = (1.-d)*thisray.rorg[i] + d*lorg[i];
//			thisray.rdir[i] = (1.-d)*thisray.rdir[i] + d*ldir[i];
//		}
//		if (normalize(thisray.rdir) == 0.0)
//			return(0.0);
//		vdist = (1.-d)*vdist + d*lastview.vdist;
//	}
//						/* optional depth-of-field */
//	if (dblur > FTINY && vdist > FTINY) {
//		double  vc, dfh, dfv;
//						/* square/circle conv. */
//		dfh = vc = 1. - 2.*frandom();
//		dfv = 1. - 2.*frandom();
//		dfh *= .5*dblur*sqrt(1. - .5*dfv*dfv);
//		dfv *= .5*dblur*sqrt(1. - .5*vc*vc);
//		if (ourview.type == VT_PER || ourview.type == VT_PAR) {
//			dfh /= sqrt(ourview.hn2);
//			dfv /= sqrt(ourview.vn2);
//			for (i = 3; i--; ) {
//				vc = thisray.rorg[i] + vdist*thisray.rdir[i];
//				thisray.rorg[i] += dfh*ourview.hvec[i] +
//							dfv*ourview.vvec[i] ;
//				thisray.rdir[i] = vc - thisray.rorg[i];
//			}
//		} else {			/* non-standard view case */
//			double	dfd = PI/4.*dblur*(.5 - frandom());
//			if (ourview.type != VT_ANG && ourview.type != VT_PLS) {
//				if (ourview.type != VT_CYL)
//					dfh /= sqrt(ourview.hn2);
//				dfv /= sqrt(ourview.vn2);
//			}
//			for (i = 3; i--; ) {
//				vc = thisray.rorg[i] + vdist*thisray.rdir[i];
//				thisray.rorg[i] += dfh*ourview.hvec[i] +
//							dfv*ourview.vvec[i] +
//							dfd*ourview.vdir[i] ;
//				thisray.rdir[i] = vc - thisray.rorg[i];
//			}
//		}
//		if (normalize(thisray.rdir) == 0.0)
//			return(0.0);
//	}
//
//	rayorigin(&thisray, PRIMARY, null, null);
//
//	rayvalue(&thisray);			/* trace ray */
//
//	copycolor(col, thisray.rcol);		/* return color */
//
//	return(thisray.rt);			/* return distance */
//}
//
//
//static int
//salvage(		/* salvage scanlines from killed program */
//	char  *oldfile
//)
//{
//	COLR  *scanline;
//	FILE  *fp;
//	int  x, y;
//
//	if (oldfile == null)
//		goto gotzip;
//
//	if ((fp = fopen(oldfile, "r")) == null) {
//		sprintf(errmsg, "cannot open recover file \"%s\"", oldfile);
//		error(WARNING, errmsg);
//		goto gotzip;
//	}
//	SET_FILE_BINARY(fp);
//				/* discard header */
//	getheader(fp, null, null);
//				/* get picture size */
//	if (!fscnresolu(&x, &y, fp)) {
//		sprintf(errmsg, "bad recover file \"%s\" - not removed",
//				oldfile);
//		error(WARNING, errmsg);
//		fclose(fp);
//		goto gotzip;
//	}
//
//	if (x != hres || y != vres) {
//		sprintf(errmsg, "resolution mismatch in recover file \"%s\"",
//				oldfile);
//		error(USER, errmsg);
//	}
//
//	scanline = (COLR *)malloc(hres*sizeof(COLR));
//	if (scanline == null)
//		error(SYSTEM, "out of memory in salvage");
//	for (y = 0; y < vres; y++) {
//		if (freadcolrs(scanline, hres, fp) < 0)
//			break;
//		if (fwritecolrs(scanline, hres, stdout) < 0)
//			goto writerr;
//	}
//	if (fflush(stdout) == EOF)
//		goto writerr;
//	free((void *)scanline);
//	fclose(fp);
//	unlink(oldfile);
//	return(y);
//gotzip:
//	if (fflush(stdout) == EOF)
//		error(SYSTEM, "error writing picture header");
//	return(0);
//writerr:
//	sprintf(errmsg, "write error during recovery of \"%s\"", oldfile);
//	error(SYSTEM, errmsg);
//	return -1; /* pro forma return */
//}
//
//static int
//pixnumber(		/* compute pixel index (brushed) */
//	register int  x,
//	register int  y,
//	int  xres,
//	int  yres
//)
//{
//	x -= y;
//	while (x < 0)
//		x += xres;
//	return((((x>>2)*yres + y) << 2) + (x & 3));
//}
//
    public static String mktemp(String format) throws IOException {
        String prefix = format;
        String suffix = null;
        File tmpFile = File.createTempFile(prefix, suffix);
        String tmpFilePath = tmpFile.getAbsolutePath();
        return tmpFilePath;
//	    System.out.println(tmpFilePath);        
    }
}
