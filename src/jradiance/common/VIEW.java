/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

/**
 *
 * @author arwillis
 */
public class VIEW {
    /*
     *  view.h - header file for image generation.
     *
     *  Include after stdio.h and rtmath.h
     *  Includes resolu.h
     */
    /* view types */

    public static final char VT_PER = 'v';		/* perspective */

    public static final char VT_PAR = 'l';		/* parallel */

    public static final char VT_ANG = 'a';		/* angular fisheye */

    public static final char VT_HEM = 'h';		/* hemispherical fisheye */

    public static final char VT_PLS = 's';		/* planispheric fisheye */

    public static final char VT_CYL = 'c';		/* cylindrical panorama */

    public int type = VT_PER;		/* view type */

    public FVECT vp = new FVECT(0, 0, 0);		/* view origin */

    public FVECT vdir = new FVECT(0, 1, 0);		/* view direction */

    public FVECT vup = new FVECT(0, 0, 1);		/* view up */

    double vdist = 1;		/* view distance */

    public double horiz = 45;		/* horizontal view size */

    public double vert = 45;		/* vertical view size */

    double hoff = 0;		/* horizontal image offset */

    double voff = 0;		/* vertical image offset */

    double vfore = 0;		/* fore clipping plane */

    double vaft = 0;		/* aft clipping plane (<=0 for inf) */

    FVECT hvec = new FVECT(0, 0, 0);		/* computed horizontal image vector */

    FVECT vvec = new FVECT(0, 0, 0);		/* computed vertical image vector */

    double hn2 = 0;		/* DOT(hvec,hvec) */

    double vn2 = 0;		/* DOT(vvec,vvec) */
//}			/* view parameters */

    public static VIEW STDVIEW = IMAGE.stdview;

    public static double viewaspect(VIEW v) {
        return Math.sqrt(v.vn2 / v.hn2);
    }
//#define  viewaspect(v)	sqrt((v)->vn2/(v)->hn2)
//
//#define  STDVIEW	{VT_PER,{0.,0.,0.},{0.,1.,0.},{0.,0.,1.}, \
//				1.,45.,45.,0.,0.,0.,0., \
//				{0.,0.,0.},{0.,0.,0.},0.,0.}
    public static final String VIEWSTR = "VIEW=";
    public static final int VIEWSTRL = 5;
//extern char	*setview(VIEW *v);
//extern void	normaspect(double va, double *ap, int *xp, int *yp);
//extern double	viewray(FVECT orig, FVECT direc, VIEW *v, double x, double y);
//extern void	viewloc(FVECT ip, VIEW *v, FVECT p);
//extern void	pix2loc(RREAL loc[2], RESOLU *rp, int px, int py);
//extern void	loc2pix(int pp[2], RESOLU *rp, double lx, double ly);
//extern int	getviewopt(VIEW *v, int ac, char *av[]);
//extern int	sscanview(VIEW *vp, char *s);
//extern void	fprintview(VIEW *vp, FILE *fp);
//extern char	*viewopt(VIEW *vp);
//extern int	isview(char *s);
//extern int	viewfile(char *fname, VIEW *vp, RESOLU *rp);
}
