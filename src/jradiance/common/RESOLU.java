/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author arwillis
 */
public class RESOLU {
    /*
     * Definitions for resolution line in image file.
     *
     * Include after <stdio.h>
     *
     * True image orientation is defined by an xy coordinate system
     * whose origin is at the lower left corner of the image, with
     * x increasing to the right and y increasing in the upward direction.
     * This true orientation is independent of how the pixels are actually
     * ordered in the file, which is indicated by the resolution line.
     * This line is of the form "{+-}{XY} xyres {+-}{YX} yxres\n".
     * A typical line for a 1024x600 image might be "-Y 600 +X 1024\n",
     * indicating that the scanlines are in English text order (PIXSTANDARD).
     */

    /* flags for scanline ordering */
    public static final int XDECR = 1;
    public static final int YDECR = 2;
    public static final int YMAJOR = 4;

    /* standard scanline ordering */
    public static final int PIXSTANDARD = (YMAJOR | YDECR);
    public static final String PIXSTDFMT = "-Y %d +X %d\n";

    /* structure for image dimensions */
//public static class RESOLU {
    public int rt;		/* orientation (from flags above) */

    public int xr, yr;		/* x and y resolution */
//}

    /* macros to get scanline length and number */

    public static int scanlen(RESOLU rs) {
        return ((rs).rt & YMAJOR) != 0 ? (rs).xr : (rs).yr;
    }

    public static int numscans(RESOLU rs) {
        return ((rs).rt & YMAJOR) != 0 ? (rs).yr : (rs).xr;
    }
    /* resolution string buffer and its size */
    public static final int RESOLU_BUFLEN = 32;

    /* macros for reading/writing resolution struct */
//#define  fputsresolu(rs,fp)	fputs(resolu2str(resolu_buf,rs),fp)
    public static int fgetsresolu(RESOLU rs, InputStream fp) throws IOException {
        return str2resolu(rs, CALEXPR.fgets(resolu_buf, RESOLU_BUFLEN, fp));
    }

    /* reading/writing of standard ordering */
    public static void fprtresolu(int sl,int ns, OutputStream fp) throws IOException {
        fp.write(String.format(PIXSTDFMT,ns,sl).getBytes());
    }
//#define  fscnresolu(sl,ns,fp)	(fscanf(fp,PIXSTDFMT,ns,sl)==2)

    /* identify header lines */
//#define  isheadid(s)	headidval(null,s)
//#define  isformat(s)	formatval(null,s)
//#define  isdate(s)	dateval(null,s)
//#define  isgmt(s)	gmtval(null,s)
//
//#define  LATLONSTR	"LATLONG="
//#define  LLATLONSTR	8
//#define  islatlon(hl)		(!strncmp(hl,LATLONSTR,LLATLONSTR))
//#define  latlonval(ll,hl)	sscanf((hl)+LLATLONSTR, "%f %f", \
//						&(ll)[0],&(ll)[1])
//#define  fputlatlon(lat,lon,fp)	fprintf(fp,"%s %.6f %.6f\n",LATLONSTR,lat,lon)
//
//					/* defined in resolu.c */
//extern void	fputresolu(int ord, int sl, int ns, FILE *fp);
//extern int	fgetresolu(int *sl, int *ns, FILE *fp);
//extern char *	resolu2str(char *buf, RESOLU *rp);
//extern int	str2resolu(RESOLU *rp, char *buf);
//					/* defined in header.c */
//extern void	newheader(char *t, FILE *fp);
//extern int	headidval(char *r, char *s);
//extern int	dateval(time_t *t, char *s);
//extern int	gmtval(time_t *t, char *s);
//extern void	fputdate(time_t t, FILE *fp);
//extern void	fputnow(FILE *fp);
//extern void	printargs(int ac, char **av, FILE *fp);
//extern int	formatval(char *r, char *s);
//extern void	fputformat(char *s, FILE *fp);
//typedef int gethfunc(char *s, void *p); /* callback to process header lines */
//extern int	getheader(FILE *fp, gethfunc *f, void *p);
//extern int	globmatch(char *pat, char *str);
//extern int	checkheader(FILE *fin, char *fmt, FILE *fout);

    /*
     * Read and write image resolutions.
     *
     * Externals declared in resolu.h
     */
    static char[] resolu_buf = new char[RESOLU_BUFLEN];	/* resolution line buffer */

//void
//fputresolu(ord, sl, ns, fp)		/* put out picture dimensions */
//int  ord;			/* scanline ordering */
//int  sl, ns;			/* scanline length and number */
//FILE  *fp;
//{
//	RESOLU  rs;
//
//	if ((rs.rt = ord) & YMAJOR) {
//		rs.xr = sl;
//		rs.yr = ns;
//	} else {
//		rs.xr = ns;
//		rs.yr = sl;
//	}
//	fputsresolu(&rs, fp);
//}
public static int
fgetresolu(			/* get picture dimensions */
int[]  sl, int[] ns,			/* scanline length and number */
InputStream fp) throws IOException
{
	RESOLU  rs = new RESOLU();

	if (fgetsresolu(rs, fp)==0)
		return(-1);
	if ((rs.rt & YMAJOR)!=0) {
		sl[0] = rs.xr;
		ns[0] = rs.yr;
	} else {
		sl[0] = rs.yr;
		ns[0] = rs.xr;
	}
	return(rs.rt);
}
//char *
//resolu2str(buf, rp)		/* convert resolution struct to line */
//char  *buf;
//register RESOLU  *rp;
//{
//	if (rp->rt&YMAJOR)
//		sprintf(buf, "%cY %d %cX %d\n",
//				rp->rt&YDECR ? '-' : '+', rp->yr,
//				rp->rt&XDECR ? '-' : '+', rp->xr);
//	else
//		sprintf(buf, "%cX %d %cY %d\n",
//				rp->rt&XDECR ? '-' : '+', rp->xr,
//				rp->rt&YDECR ? '-' : '+', rp->yr);
//	return(buf);
//}
//
//

    static int str2resolu( /* convert resolution line to struct */
            RESOLU rp,
            char[] buf) {
        String xndx, yndx;
        char[] cp;
        char prev = 0, xprev = 0, yprev = 0;
        if (buf == null) {
            return (0);
        }
        xndx = yndx = null;
        int cpidx = 0, xidx = 0, yidx = 0;
        for (cp = buf; cp[cpidx] != 0; cpidx++) {
            if (cp[cpidx] == 'X') {
                xidx = cpidx;
                xndx = new String(cp).substring(cpidx);
                xndx = xndx.replaceAll(" ", "");
                xndx = xndx.replaceAll("\0", "");
                if (yndx!= null) {
                    yndx = new String(cp).substring(yidx,cpidx-1);
                yndx = yndx.replaceAll(" ", "");
                yndx = yndx.replaceAll("\0", "");
                }
                xprev = prev;
            } else if (cp[cpidx] == 'Y') {
                yidx = cpidx;
                yndx = new String(cp).substring(cpidx);
                yndx = yndx.replaceAll(" ", "");
                yndx = yndx.replaceAll("\0", "");
                if (xndx!= null) {
                    xndx = new String(cp).substring(xidx,cpidx-1);
                xndx = xndx.replaceAll(" ", "");
                xndx = xndx.replaceAll("\0", "");
                }
                yprev = prev;
            }
            prev = cp[cpidx];
        }
        if (xndx == null || yndx == null) {
            return (0);
        }
        rp.rt = 0;
        if (xidx > yidx) {
            rp.rt |= YMAJOR;
        }
        if (xprev == '-') {
            rp.rt |= XDECR;
        }
        if (yprev == '-') {
            rp.rt |= YDECR;
        }
        if ((rp.xr = Integer.parseInt(xndx.substring(1))) <= 0) {
            return (0);
        }
        if ((rp.yr = Integer.parseInt(yndx.substring(1))) <= 0) {
            return (0);
        }
        return (1);
    }
}
