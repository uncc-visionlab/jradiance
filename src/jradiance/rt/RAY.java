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

import java.util.logging.Level;
import java.util.logging.Logger;
import jradiance.common.COLORS;
import jradiance.common.COLORS.COLOR;
import jradiance.common.FVECT;
import jradiance.common.OBJECT;
import jradiance.common.RTMATH.FULLXF;
import jradiance.rt.RAYTRACE.RAYHIT;
import jradiance.rt.RAYTRACE.RAYTRACER;

/**
 *
 * @author arwillis
 */
public class RAY {
    /*
     *  RAY.h - header file for routines using rays.
     */
//#define RNUMBER		unsigned long	/* RAY counter (>= sizeof pointer) */

    public static final int MAXDIM = 32;	/* maximum number of dimensions */

    /* RAY type flags */
    public static final int PRIMARY = 01;		/* original RAY */

    public static final int SHADOW = 02;		/* RAY to light source */

    public static final int REFLECTED = 04;		/* reflected RAY */

    public static final int REFRACTED = 010;		/* refracted (bent) RAY */

    public static final int TRANS = 020;		/* transmitted/transferred RAY */

    public static final int AMBIENT = 040;		/* RAY scattered for interreflection */

    public static final int SPECULAR = 0100;		/* RAY scattered for specular */

    /* reflected RAY types */
    public static final int RAYREFL = (SHADOW | REFLECTED | AMBIENT | SPECULAR);

    /* Arrange so double's come first for optimal alignment */
    /* Pointers and long's come second for 64-bit mode */
    /* Int's next (unknown length), then floats, followed by short's & char's */
    FVECT rorg = new FVECT();		/* origin of RAY */

    FVECT rdir = new FVECT();		/* normalized direction of RAY */

    double rmax;		/* maximum distance (aft clipping plane) */

    double rot;		/* distance to object */

    FVECT rop = new FVECT();		/* intersection point */

    FVECT ron = new FVECT();		/* intersection surface normal */

    double rod;		/* -DOT(rdir, ron) */

    double[] uv = new double[2];		/* local coordinates */

    FVECT pert = new FVECT();		/* surface normal perturbation */

    double rt;		/* returned effective RAY length */

    RAY parent;	/* RAY this originated from */

    int[] clipset;	/* set of objects currently clipped */

    int[] newcset;	/* next clipset, used for transmission */

    //void	(*revf)(struct RAY *);	/* RAY evaluation function */
    RAYTRACER revf;
    //void	(*hitf)(OBJECT *, struct RAY *);	/* custom hit test */
    RAYHIT hitf;
    OBJECT.OBJREC ro;		/* intersected object (one with material) */

    FULLXF rox = null;		/* object transformation */

    int[] slights;	/* list of lights to test for scattering */

    long rno;		// no unsigned long in java /* unique RAY number */
    int rlvl;		/* number of reflections for this RAY */

    int rsrc;		/* source we're aiming for */

    float rweight;	/* cumulative weight (for termination) */

    COLOR rcoef = new COLOR();		/* contribution coefficient w.r.t. parent */

    COLOR pcol = new COLOR();		/* pattern color */

    COLOR rcol = new COLOR();		/* returned radiance value */

    COLOR cext = new COLOR();		/* medium extinction coefficient */

    COLOR albedo = new COLOR();		/* medium scattering albedo */

    float gecc;		/* scattering eccentricity coefficient */

    int robj;		/* intersected object number */

    short rtype;		/* RAY type */

    short crtype;		/* cumulative RAY type */


    public static void rayvalue(RAY r) {
        r.revf.raytrace(r);
    }

    public RAY copy() {
        RAY newr = new RAY();
        newr.rorg = rorg.copy();
        newr.rdir = rdir.copy();
        newr.rmax = rmax;
        newr.rot = rot;
        newr.rop = rop.copy();
        newr.ron = ron.copy();
        newr.rod = rod;
        if (uv != null) {
            newr.uv = new double[uv.length];
            System.arraycopy(uv, 0, newr.uv, 0, uv.length);
        } else {
            newr.uv = null;
        }
        newr.pert = pert.copy();
        newr.rt = rt;
        if (clipset != null) {
            newr.clipset = new int[clipset.length];
            System.arraycopy(clipset, 0, newr.clipset, 0, clipset.length);
        } else {
            newr.clipset = null;
        }
        if (newcset != null) {
            newr.newcset = new int[newcset.length];
            System.arraycopy(newcset, 0, newr.newcset, 0, newcset.length);
        } else {
            newr.newcset = null;
        }
        // shallow copies
        newr.parent = parent;
        newr.revf = revf;
        newr.hitf = hitf;
        newr.ro = ro;
        newr.rox = rox;
        // shallow copies end
        if (slights != null) {
            newr.slights = new int[slights.length];
            System.arraycopy(slights, 0, newr.slights, 0, slights.length);
        } else {
            newr.slights = null;
        }
        newr.rno = rno;
        newr.rlvl = rlvl;
        newr.rsrc = rsrc;
        newr.rweight = rweight;
        newr.rcoef = rcoef.copy();
        newr.pcol = pcol.copy();
        newr.rcol = rcol.copy();
        newr.cext = cext.copy();
        newr.albedo = albedo.copy();
        newr.gecc = gecc;
        newr.robj = robj;
        newr.rtype = rtype;
        newr.crtype = crtype;
        return newr;
    }
//extern char  VersionID[];	/* Radiance version ID string */
//
//extern CUBE	thescene;	/* our scene */
//extern OBJECT	nsceneobjs;	/* number of objects in our scene */
//
//extern RNUMBER	raynum;		/* next RAY ID */
//extern RNUMBER	nrays;		/* total rays traced so far */
//
//extern OBJREC  Lamb;		/* a Lambertian surface */
//extern OBJREC  Aftplane;	/* aft clipping object */
//
//extern void	(*trace)();	/* global trace reporting callback */
//
//extern int	dimlist[];	/* dimension list for distribution */
//extern int	ndims;		/* number of dimensions so far */
//extern int	samplendx;	/* index for this sample */
//
//extern int	ray_savesiz;	/* size of parameter save buffer */
//
//extern int	do_irrad;	/* compute irradiance? */
//
//extern int	rand_samp;	/* pure Monte Carlo sampling? */
//
//extern double	dstrsrc;	/* square source distribution */
//extern double	shadthresh;	/* shadow threshold */
//extern double	shadcert;	/* shadow testing certainty */
//extern int	directrelay;	/* number of source relays */
//extern int	vspretest;	/* virtual source pretest density */
//extern int	directvis;	/* light sources visible to eye? */
//extern double	srcsizerat;	/* maximum source size/dist. ratio */
//
//extern double	specthresh;	/* specular sampling threshold */
//extern double	specjitter;	/* specular sampling jitter */
//
//extern COLORS	cextinction;	/* global extinction coefficient */
//extern COLORS	salbedo;	/* global scattering albedo */
//extern double	seccg;		/* global scattering eccentricity */
//extern double	ssampdist;	/* scatter sampling distance */
//
//extern int	backvis;	/* back face visibility */
//
//extern int	maxdepth;	/* maximum recursion depth */
//extern double	minweight;	/* minimum RAY weight */
//
//extern char	*ambfile;	/* ambient file name */
//extern COLORS	ambval;		/* ambient value */
//extern int	ambvwt;		/* initial weight for ambient value */
//extern double	ambacc;		/* ambient accuracy */
//extern int	ambres;		/* ambient resolution */
//extern int	ambdiv;		/* ambient divisions */
//extern int	ambssamp;	/* ambient super-samples */
//extern int	ambounce;	/* ambient bounces */
//extern char	*amblist[];	/* ambient include/exclude list */
//extern int	ambincl;	/* include == 1, exclude == 0 */
//
//extern int	ray_pnprocs;	/* number of child processes */
//extern int	ray_pnidle;	/* number of idle processes */
//
    public static final int AMBLLEN = 512;	/* max. ambient list length */

    public static final int AMBWORD = 12;	/* average word length */


    public class RAYPARAMS {		/* rendering parameter holder */


        int do_irrad;
        int rand_samp;
        double dstrsrc;
        double shadthresh;
        double shadcert;
        int directrelay;
        int vspretest;
        int directvis;
        double srcsizerat;
        COLORS.COLOR cextinction;
        COLORS.COLOR salbedo;
        double seccg;
        double ssampdist;
        double specthresh;
        double specjitter;
        int backvis;
        int maxdepth;
        double minweight;
        char[] ambfile = new char[512];
        COLORS.COLOR ambval;
        int ambvwt;
        double ambacc;
        int ambres;
        int ambdiv;
        int ambssamp;
        int ambounce;
        int ambincl;
        short[] amblndx = new short[AMBLLEN + 1];
        char[] amblval = new char[AMBLLEN * AMBWORD];
    }
//#define rpambmod(p,i)	( (i)>=AMBLLEN||(p)->amblndx[i]<0 ? \
//			  (char *)NULL : (p)->amblval+(p)->amblndx[i] )
//
//					/* defined in duphead.c */
//extern void	headclean(void);
//extern void	openheader(void);
//extern void	dupheader(void);
//					/* defined in persist.c */
//extern void persistfile(char *pfn);
//extern void	pfdetach(void);
//extern void	pfclean(void);
//extern void	pflock(int lf);
//extern void	pfhold(void);
//extern void	io_process(void);
//					/* defined in freeobjmem.c */
//extern int	free_objs(OBJECT on, OBJECT no);
//extern void	free_objmem(void);
//					/* defined in preload.c */
//extern int	load_os(OBJREC *op);
//extern void	preload_objs(void);
//					/* defined in raycalls.c */
//extern void	ray_init(char *otnm);
//extern void	ray_trace(RAY *r);
//extern void	ray_done(int freall);
//extern void	ray_save(RAYPARAMS *rp);
//extern void	ray_restore(RAYPARAMS *rp);
//extern void	ray_defaults(RAYPARAMS *rp);
//					/* defined in raypcalls.c */
//extern void	ray_pinit(char *otnm, int nproc);
//extern int	ray_psend(RAY *r);
//extern int	ray_pqueue(RAY *r);
//extern int	ray_presult(RAY *r, int poll);
//extern void	ray_pdone(int freall);
//extern void	ray_popen(int nadd);
//extern void	ray_pclose(int nsub);
//					/* defined in ray_fifo.c */
//extern int	(*ray_fifo_out)(RAY *r);
//extern int	ray_fifo_in(RAY *r);
//extern int	ray_fifo_flush(void);
//					/* defined in raytrace.c */
//extern int	rayorigin(RAY *r, int rt, const RAY *ro, const COLORS rc);
//extern void	rayclear(RAY *r);
//extern void	raytrace(RAY *r);
//extern void	rayhit(OBJECT *oset, RAY *r);
//extern void	raycont(RAY *r);
//extern void	raytrans(RAY *r);
//extern int	rayshade(RAY *r, int mod);
//extern void	rayparticipate(RAY *r);
//extern void	raytexture(RAY *r, OBJECT mod);
//extern int	raymixture(RAY *r, OBJECT fore, OBJECT back, double coef);
//extern void	raycontrib(RREAL rc[3], const RAY *r, int flags);
//extern double	raydist(const RAY *r, int flags);
//extern double	raynormal(FVECT norm, RAY *r);
//extern void	newrayxf(RAY *r);
//extern void	flipsurface(RAY *r);
//extern int	localhit(RAY *r, CUBE *scene);
//					/* defined in renderopts.c */
//extern int	getrenderopt(int ac, char *av[]);
//extern void	print_rdefaults(void);
//					/* defined in srcdraw.c */
//extern void	drawsources(COLORS *pic[], float *zbf[],
//			int x0, int xsiz, int y0, int ysiz);
//extern void init_drawsources(int rad);
//					/* defined in rt/initotypes.c */
//extern void initotypes(void);
//					/* module main procedures */
//extern void	rtrace(char *fname, int nproc);
//extern char	*formstr(int  f);
//extern void	rview(void);
//extern void	rpict(int seq, char *pout, char *zout, char *prvr);
}
