/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import jradiance.common.PORTIO.FRexpResult;

/**
 *
 * @author arwillis
 */
public class COLORS {
    /* RCSid $Id$ */
    /*
     *  color.h - header for routines using pixel color values.
     *
     *  Two color representations are used, one for calculation and
     *  another for storage.  Calculation is done with three floats
     *  for speed.  Stored color values use 4 bytes which contain
     *  three single byte mantissas and a common exponent.
     */
//#ifndef _RAD_COLOR_H_
//#define _RAD_COLOR_H_
//
//#include <stdlib.h>
//
//#ifdef __cplusplus
//extern "C" {
//#endif

    public static final int CIEX = 0;	/* or, if input is XYZ... */

    public static final int CIEY = 1;
    public static final int CIEZ = 2;
    public static final int COLXS = 128;	/* excess used for exponent */

    public static final int WHT = 3;	/* used for RGBPRIMS type */

//#undef uby8
//#define uby8  unsigned char	/* 8-bit unsigned integer */

//typedef uby8  COLR[4];		/* red, green, blue (or X,Y,Z), exponent */
    public static class COLR implements Serializable {

        public byte[] data = new byte[4];

        public static COLR copycolr(COLR src) {
            COLR cVal = new COLR();
            System.arraycopy(src.data, 0, cVal.data, 0, cVal.data.length);
            return cVal;
        }

        private void writeObject(ObjectOutputStream os) throws IOException {
            for (int i = 0; i < data.length; i++) {
                os.write(data[i]);
            }
        }
    }
//typedef float COLORV;
//typedef COLORV  COLORS[3];	/* red, green, blue (or X,Y,Z) */

    public static class COLOR {

        public static final int RED = 0;
        public static final int GRN = 1;
        public static final int BLU = 2;
        public static final int EXP = 3;	/* exponent same for either format */

        public float[] data = new float[3];

        public COLOR() {
        }

        public COLOR(float r, float g, float b) {
            data[0] = r;
            data[1] = g;
            data[2] = b;
        }

        public static COLOR BLKCOLOR() {
            return new COLOR(0, 0, 0);
        }

        public double colval(int idx) {
            return data[idx];
        }

        public void setcolor(float r, float g, float b) {
            (data)[RED] = (r);
            (data)[GRN] = (g);
            (data)[BLU] = (b);
        }

        public static void copycolor(COLOR c1, COLOR c2) {
            (c1.data)[0] = (c2.data)[0];
            (c1.data)[1] = (c2.data)[1];
            (c1.data)[2] = (c2.data)[2];
        }

        public void scalecolor(double sf) {
            data[0] *= sf;
            data[1] *= sf;
            data[2] *= sf;
        }

        public static void addcolor(COLOR c1, COLOR c2) {
            (c1.data)[0] += (c2.data)[0];
            (c1.data)[1] += (c2.data)[1];
            (c1.data)[2] += (c2.data)[2];
        }

        public static void multcolor(COLOR c1, COLOR c2) {
            (c1.data)[0] *= (c2.data)[0];
            (c1.data)[1] *= (c2.data)[1];
            (c1.data)[2] *= (c2.data)[2];
        }

        public static double bright(COLOR col) {
            return (CIE_rf * (col.data)[RED] + CIE_gf * (col.data)[GRN] + CIE_bf * (col.data)[BLU]);
        }

        public static double luminance(COLOR col) {
            return (WHTEFFICACY * bright(col));
        }

        /***** ...end of stuff specific to RGB colors *****/
        public static double intens(COLOR col) {
            return ((col.data)[0] > (col.data)[1]
                    ? (col.data)[0] > (col.data)[2] ? (col.data)[0] : (col.data)[2]
                    : (col.data)[1] > (col.data)[2] ? (col.data)[1] : (col.data)[2]);
        }

        public static double colrval(COLR c, int p) {
            short cdata = (short) (c.data[p] & 0xff);
            short edata = (short) (c.data[EXP] & 0xff);
            return ((edata) != 0 ? PORTIO.ldexp((cdata) + .5, (int) (edata) - (COLXS + 8)) : 0.);
//            return ((c.data)[EXP] != 0 ? PORTIO.ldexp((c.data)[p] + .5, (int) (c.data)[EXP] - (COLXS + 8)) : 0.);
        }

        public static int bigdiff( /* c1 delta c2 > md? */
                COLOR c1, COLOR c2,
                double md) {
            int i;

            for (i = 0; i < 3; i++) {
                if (c1.colval(i) - c2.colval(i) > md * c2.colval(i)
                        || c2.colval(i) - c1.colval(i) > md * c1.colval(i)) {
                    return (1);
                }
            }
            return (0);
        }

        public void write(OutputStream os) throws IOException {
            for (int i = 0; i < data.length; i++) {
                PORTIO.putflt(data[i], os);
            }
        }

        public COLOR copy() {
            return new COLOR(data[0], data[1], data[2]);
        }

        @Override
        public String toString() {
            String str = String.format("(%1.3f,%1.3f,%1.3f)", data[0], data[1], data[2]);
            return str;
        }
    }
//typedef float  RGBPRIMS[4][2];	/* (x,y) chromaticities for RGBW */
    float[][] RGBPRIMS = new float[4][2];
//typedef float  (*RGBPRIMP)[2];	/* pointer to RGBPRIMS array */
// POINTER MAGIC HERE
//typedef float  COLORMAT[3][3];	/* color coordinate conversion matrix */
    float[][] COLORMAT = new float[3][3];
//#define  copycolr(c1,c2)	(c1[0]=c2[0],c1[1]=c2[1], \
//				c1[2]=c2[2],c1[3]=c2[3])
//
//double  colval(double[] col,pri)	((col)[pri])
//
//#define  setcolor(col,r,g,b)	((col)[RED]=(r),(col)[GRN]=(g),(col)[BLU]=(b))
//
//#define  copycolor(c1,c2)	((c1)[0]=(c2)[0],(c1)[1]=(c2)[1],(c1)[2]=(c2)[2])
//
//#define  scalecolor(col,sf)	((col)[0]*=(sf),(col)[1]*=(sf),(col)[2]*=(sf))
//
//#define  addcolor(c1,c2)	((c1)[0]+=(c2)[0],(c1)[1]+=(c2)[1],(c1)[2]+=(c2)[2])
//
//#define  multcolor(c1,c2)	((c1)[0]*=(c2)[0],(c1)[1]*=(c2)[1],(c1)[2]*=(c2)[2])
//#ifdef  NTSC
//#define  CIE_x_r		0.670		/* standard NTSC primaries */
//#define  CIE_y_r		0.330
//#define  CIE_x_g		0.210
//#define  CIE_y_g		0.710
//#define  CIE_x_b		0.140
//#define  CIE_y_b		0.080
//#define  CIE_x_w		0.3333		/* use true white */
//#define  CIE_y_w		0.3333
//#else
    public static final double CIE_x_r = 0.640;		/* nominal CRT primaries */

    public static final double CIE_y_r = 0.330;
    public static final double CIE_x_g = 0.290;
    public static final double CIE_y_g = 0.600;
    public static final double CIE_x_b = 0.150;
    public static final double CIE_y_b = 0.060;
    public static final double CIE_x_w = 0.3333;		/* use true white */

    public static final double CIE_y_w = 0.3333;
//#endif
//#define  STDPRIMS	{{CIE_x_r,CIE_y_r},{CIE_x_g,CIE_y_g}, \
//				{CIE_x_b,CIE_y_b},{CIE_x_w,CIE_y_w}}
//
    public static final double CIE_D = (CIE_x_r * (CIE_y_g - CIE_y_b)
            + CIE_x_g * (CIE_y_b - CIE_y_r)
            + CIE_x_b * (CIE_y_r - CIE_y_g));
    public static final double CIE_C_rD = ((1. / CIE_y_w)
            * (CIE_x_w * (CIE_y_g - CIE_y_b)
            - CIE_y_w * (CIE_x_g - CIE_x_b)
            + CIE_x_g * CIE_y_b - CIE_x_b * CIE_y_g));
    public static final double CIE_C_gD = ((1. / CIE_y_w)
            * (CIE_x_w * (CIE_y_b - CIE_y_r)
            - CIE_y_w * (CIE_x_b - CIE_x_r)
            - CIE_x_r * CIE_y_b + CIE_x_b * CIE_y_r));
    public static final double CIE_C_bD = ((1. / CIE_y_w)
            * (CIE_x_w * (CIE_y_r - CIE_y_g)
            - CIE_y_w * (CIE_x_r - CIE_x_g)
            + CIE_x_r * CIE_y_g - CIE_x_g * CIE_y_r));
    public static final double CIE_rf = (CIE_y_r * CIE_C_rD / CIE_D);
    public static final double CIE_gf = (CIE_y_g * CIE_C_gD / CIE_D);
    public static final double CIE_bf = (CIE_y_b * CIE_C_bD / CIE_D);

    /* As of 9-94, CIE_rf=.265074126, CIE_gf=.670114631 and CIE_bf=.064811243 */
    /***** The following definitions are valid for RGB colors only... *****/
//#define  bright(col)	(CIE_rf*(col)[RED]+CIE_gf*(col)[GRN]+CIE_bf*(col)[BLU])
    public static long normbright(COLR c) {
        return (((long) (CIE_rf * 256. + .5) * (c).data[COLOR.RED]
                + (long) (CIE_gf * 256. + .5) * (c).data[COLOR.GRN]
                + (long) (CIE_bf * 256. + .5) * (c).data[COLOR.BLU]) >> 8);
    }

    /* luminous efficacies over visible spectrum */
    public static final double MAXEFFICACY = 683.;		/* defined maximum at 550 nm */

    public static final double WHTEFFICACY = 179.;		/* uniform white light */

    public static final double D65EFFICACY = 203.;		/* standard illuminant D65 */

    public static final double INCEFFICACY = 160.;		/* illuminant A (incand.) */

    public static final double SUNEFFICACY = 208.;		/* illuminant B (solar dir.) */

    public static final double SKYEFFICACY = D65EFFICACY;	/* skylight (should be 110) */

    public static final double DAYEFFICACY = D65EFFICACY;	/* combined sky and solar */

//#define  WHTCOLOR		{1.0,1.0,1.0}
//#define  BLKCOLOR		{0.0,0.0,0.0}
//#define  WHTCOLR		{128,128,128,COLXS+1}
//#define  BLKCOLR		{0,0,0,0}
//
				/* picture format identifier */
    public static final String COLRFMT = "32-bit_rle_rgbe";
    public static final String CIEFMT = "32-bit_rle_xyze";
    public static final String PICFMT = "32-bit_rle_???e";	/* matches either */

    public static final int LPICFMT = 15;			/* max format id len */

    /* macros for exposures */
    public static final String EXPOSSTR = "EXPOSURE=";
    public static final int LEXPOSSTR = 9;
//#define  isexpos(hl)		(!strncmp(hl,EXPOSSTR,LEXPOSSTR))
//#define  exposval(hl)		atof((hl)+LEXPOSSTR)
//#define  fputexpos(ex,fp)	fprintf(fp,"%s%e\n",EXPOSSTR,ex)

    /* macros for pixel aspect ratios */
    public static final String ASPECTSTR = "PIXASPECT=";
    public static final int LASPECTSTR = 10;

    public static int isaspect(String hl) {
        return (hl.substring(0, LASPECTSTR).equals(ASPECTSTR)) ? 1 : 0;
    }

    public static double aspectval(String hl) {
        return Double.parseDouble(hl.substring(LASPECTSTR));
    }
    
    public static void fputaspect(double pa, OutputStream fp) throws IOException {
        fp.write(String.format("%s%f\n",ASPECTSTR,pa).getBytes());
    }

    /* macros for primary specifications */
    public static final String PRIMARYSTR = "PRIMARIES=";
    public static final int LPRIMARYSTR = 10;
//#define  isprims(hl)		(!strncmp(hl,PRIMARYSTR,LPRIMARYSTR))
//#define  primsval(p,hl)		sscanf((hl)+LPRIMARYSTR, \
//					"%f %f %f %f %f %f %f %f", \
//					&(p)[RED][CIEX],&(p)[RED][CIEY], \
//					&(p)[GRN][CIEX],&(p)[GRN][CIEY], \
//					&(p)[BLU][CIEX],&(p)[BLU][CIEY], \
//					&(p)[WHT][CIEX],&(p)[WHT][CIEY])
//#define  fputprims(p,fp)	fprintf(fp, \
//				"%s %.4f %.4f %.4f %.4f %.4f %.4f %.4f %.4f\n",\
//					PRIMARYSTR, \
//					(p)[RED][CIEX],(p)[RED][CIEY], \
//					(p)[GRN][CIEX],(p)[GRN][CIEY], \
//					(p)[BLU][CIEX],(p)[BLU][CIEY], \
//					(p)[WHT][CIEX],(p)[WHT][CIEY])

    /* macros for color correction */
    public static final String COLCORSTR = "COLORCORR=";
    public static final int LCOLCORSTR = 10;
//#define  iscolcor(hl)		(!strncmp(hl,COLCORSTR,LCOLCORSTR))
//#define  colcorval(cc,hl)	sscanf((hl)+LCOLCORSTR,"%f %f %f", \
//					&(cc)[RED],&(cc)[GRN],&(cc)[BLU])
//#define  fputcolcor(cc,fp)	fprintf(fp,"%s %f %f %f\n",COLCORSTR, \
//					(cc)[RED],(cc)[GRN],(cc)[BLU])

    /*
     * Conversions to and from XYZ space generally don't apply WHTEFFICACY.
     * If you need Y to be luminance (cd/m^2), this must be applied when
     * converting from radiance (watts/sr/m^2).
     */
//extern RGBPRIMS  stdprims;	/* standard primary chromaticities */
//extern COLORMAT  rgb2xyzmat;	/* RGB to XYZ conversion matrix */
//extern COLORMAT  xyz2rgbmat;	/* XYZ to RGB conversion matrix */
//extern COLORS  cblack, cwhite;	/* black (0,0,0) and white (1,1,1) */
    public static final int CGAMUT_LOWER = 01;
    public static final int CGAMUT_UPPER = 02;
    public static final int CGAMUT = (CGAMUT_LOWER | CGAMUT_UPPER);
//#define  rgb_cie(xyz,rgb)	colortrans(xyz,rgb2xyzmat,rgb)
//
//#define  cpcolormat(md,ms)	memcpy((void *)md,(void *)ms,sizeof(COLORMAT))

    /* defined in color.c */
//extern char	*tempbuffer(unsigned int len);
//extern int	fwritecolrs(COLR *scanline, int len, FILE *fp);
//extern int	freadcolrs(COLR *scanline, int len, FILE *fp);
//extern int	fwritescan(COLORS *scanline, int len, FILE *fp);
//extern int	freadscan(COLORS *scanline, int len, FILE *fp);
//extern void	setcolr(COLR clr, double r, double g, double b);
//extern void	colr_color(COLORS col, COLR clr);
//extern int	bigdiff(COLORS c1, COLORS c2, double md);
//					/* defined in spec_rgb.c */
//extern void	spec_rgb(COLORS col, int s, int e);
//extern void	spec_cie(COLORS col, int s, int e);
//extern void	cie_rgb(COLORS rgb, COLORS xyz);
//extern int	clipgamut(COLORS col, double brt, int gamut,
//				COLORS lower, COLORS upper);
//extern void	colortrans(COLORS c2, COLORMAT mat, COLORS c1);
//extern void	multcolormat(COLORMAT m3, COLORMAT m2,
//					COLORMAT m1);
//extern int	colorprimsOK(RGBPRIMS pr);
//extern int	compxyz2rgbmat(COLORMAT mat, RGBPRIMS pr);
//extern int	comprgb2xyzmat(COLORMAT mat, RGBPRIMS pr);
//extern int	comprgb2rgbmat(COLORMAT mat, RGBPRIMS pr1, RGBPRIMS pr2);
//extern int	compxyzWBmat(COLORMAT mat, float wht1[2],
//				float wht2[2]);
//extern int	compxyz2rgbWBmat(COLORMAT mat, RGBPRIMS pr);
//extern int	comprgb2xyzWBmat(COLORMAT mat, RGBPRIMS pr);
//extern int	comprgb2rgbWBmat(COLORMAT mat, RGBPRIMS pr1, RGBPRIMS pr2);
//					/* defined in colrops.c */
//extern int	setcolrcor(double (*f)(double, double), double a2);
//extern int	setcolrinv(double (*f)(double, double), double a2);
//extern int	setcolrgam(double g);
//extern int	colrs_gambs(COLR *scan, int len);
//extern int	gambs_colrs(COLR *scan, int len);
//extern void	shiftcolrs(COLR *scan, int len, int adjust);
//extern void	normcolrs(COLR *scan, int len, int adjust);
//#ifdef __cplusplus
//}
//#endif
//#endif /* _RAD_COLOR_H_ */

    /*
     *  color.c - routines for color calculations.
     *
     *  Externals declared in color.h
     */
    public static final int MINELEN = 8;	/* minimum scanline length for encoding */

    public static final int MAXELEN = 0x7fff;	/* maximum scanline length for encoding */

    public static final int MINRUN = 4;	/* minimum run length */

    static COLR[] tempbuf = null;
    static int tempbuflen = 0;

    static COLR[] tempbuffer( /* get a temporary buffer */
            int len) {
//	char[]  tempbuf = null;
//	int  tempbuflen = 0;

        if (len > tempbuflen) {
            if (tempbuflen > 0) {
                COLR[] ntempbuf = new COLR[len];
                System.arraycopy(tempbuf, 0, ntempbuf, 0, tempbuf.length);
//                        (char *)realloc((void *)tempbuf, len);
                for (int i = tempbuf.length; i < ntempbuf.length; i++) {
                    ntempbuf[i] = new COLR();
                }
                tempbuf = ntempbuf;
            } else {
                tempbuf = new COLR[len];
                for (int i = 0; i < tempbuf.length; i++) {
                    tempbuf[i] = new COLR();
                }
                //tempbuf = (char *)malloc(len);
            }
            tempbuflen = tempbuf == null ? 0 : len;
        }
        return (tempbuf);
    }

    public static int fwritecolrs( /* write out a colr scanline */
            COLR[] scanline,
            int len,
            OutputStream fp) throws IOException {
        int i, j, beg, cnt = 1;
        int c2;
        if ((len < MINELEN) | (len > MAXELEN)) {
            /* OOBs, write out flat */
            for (i = 0; i < scanline.length; i++) {
                scanline[i].writeObject(new ObjectOutputStream(fp));
            }
//		return(fwrite((char *)scanline,sizeof(COLR),len,fp) - len);
        }
        /* put magic header */
        fp.write(2);
        fp.write(2);
        fp.write(len >> 8);
        fp.write(len & 255);
        /* put components seperately */
        for (i = 0; i < 4; i++) {
            for (j = 0; j < len; j += cnt) {	/* find next run */
                for (beg = j; beg < len; beg += cnt) {
                    for (cnt = 1; cnt < 127 && beg + cnt < len
                            && scanline[beg + cnt].data[i] == scanline[beg].data[i]; cnt++) {
                        ;
                    }
                    if (cnt >= MINRUN) {
                        break;			/* long enough */
                    }
                }
                if (beg - j > 1 && beg - j < MINRUN) {
                    c2 = j + 1;
                    while (scanline[c2++].data[i] == scanline[j].data[i]) {
                        if (c2 == beg) {	/* short run */
                            fp.write(128 + beg - j);
                            fp.write(scanline[j].data[i]);
                            j = beg;
                            break;
                        }
                    }
                }
                while (j < beg) {		/* write out non-run */
                    if ((c2 = beg - j) > 128) {
                        c2 = 128;
                    }
                    fp.write(c2);
                    while (c2-- != 0) {
                        fp.write(COLOROPS.byte2uint(scanline[j++].data[i]));
                    }
                }
                if (cnt >= MINRUN) {		/* write out run */
                    fp.write(128 + cnt);
                    fp.write(scanline[beg].data[i]);
                } else {
                    cnt = 0;
                }
            }
        }
//	return(ferror(fp) ? -1 : 0);
        return 0;
    }

    static int oldreadcolrs( /* read in an old colr scanline */
            COLR[] scanline,
            int len,
            InputStream fp, int offset) throws IOException {
        int rshift;
        int i;
        int scanlineidx = offset;
        len -= offset;
        rshift = 0;
        while (len > 0) {
            scanline[scanlineidx].data[COLOR.RED] = (byte) fp.read();
            scanline[scanlineidx].data[COLOR.GRN] = (byte) fp.read();
            scanline[scanlineidx].data[COLOR.BLU] = (byte) fp.read();
            scanline[scanlineidx].data[COLOR.EXP] = (byte) fp.read();
//		if (feof(fp) || ferror(fp))
//			return(-1);
            if (scanline[scanlineidx].data[COLOR.RED] == 1
                    && scanline[scanlineidx].data[COLOR.GRN] == 1
                    && scanline[scanlineidx].data[COLOR.BLU] == 1) {
                for (i = scanline[scanlineidx].data[COLOR.EXP] << rshift; i > 0; i--) {
//				copycolr(scanline[0], scanline[-1]);
                    System.arraycopy(scanline[scanlineidx - 1].data, 0, scanline[scanlineidx].data,
                            0, scanline[scanlineidx].data.length);
                    scanlineidx++;
                    len--;
                }
                rshift += 8;
            } else {
                scanlineidx++;
                len--;
                rshift = 0;
            }
        }
        return (0);
    }

    public static int freadcolrs( /* read in an encoded colr scanline */
            COLR[] scanline,
            int len,
            InputStream fp) throws IOException {
        int i, j;

        int code, val;
        /* determine scanline type */
        if ((len < MINELEN) | (len > MAXELEN)) {
            return (oldreadcolrs(scanline, len, fp, 0));
        }
        fp.mark(1);
        if ((i = fp.read()) == CALEXPR.EOF) {
            return (-1);
        }
        if (i != 2) {
//		ungetc(i, fp);
            if (!fp.markSupported()) {
                throw new UnsupportedOperationException("Cannot mark() input stream.");
            }
            fp.reset();
            return (oldreadcolrs(scanline, len, fp, 0));
        }
        scanline[0].data[COLOR.GRN] = (byte) fp.read();
        scanline[0].data[COLOR.BLU] = (byte) fp.read();
        if ((i = fp.read()) == CALEXPR.EOF) {
            return (-1);
        }
        if (scanline[0].data[COLOR.GRN] != 2 || (scanline[0].data[COLOR.BLU] & 128) != 0) {
            scanline[0].data[COLOR.RED] = 2;
            scanline[0].data[COLOR.EXP] = (byte) i;
            return (oldreadcolrs(scanline, len, fp, 1));
        }
        if ((scanline[0].data[COLOR.BLU] << 8 | i) != len) {
            return (-1);		/* length mismatch! */
        }
        /* read each component */
        for (i = 0; i < 4; i++) {
            for (j = 0; j < len;) {
                if ((code = fp.read()) == CALEXPR.EOF) {
                    return (-1);
                }
                if (code > 128) {	/* run */
                    code &= 127;
                    if ((val = fp.read()) == CALEXPR.EOF) {
                        return -1;
                    }
                    if (j + code > len) {
                        return -1;	/* overrun */
                    }
                    while (code-- != 0) {
                        scanline[j++].data[i] = (byte) val;
                    }
                } else {		/* non-run */
                    if (j + code > len) {
                        return -1;	/* overrun */
                    }
                    while (code-- != 0) {
                        if ((val = fp.read()) == CALEXPR.EOF) {
                            return -1;
                        }
                        scanline[j++].data[i] = (byte) val;
                    }
                }
            }
        }
        return (0);
    }

    public static int fwritescan( /* write out a scanline */
            COLOR[] scanline,
            int len,
            OutputStream fp) throws IOException {
        COLR[] clrscan;
        int n, scanidx = 0;
        COLR[] sp;
        /* get scanline buffer */
        if ((sp = tempbuffer(len)) == null) {
            return (-1);
        }
        clrscan = sp;
        /* convert scanline */
        n = len;
        while (n-- > 0) {
            setcolr(sp[scanidx], scanline[scanidx].data[COLOR.RED],
                    scanline[scanidx].data[COLOR.GRN],
                    scanline[scanidx].data[COLOR.BLU]);
            scanidx++;
        }
        return (fwritecolrs(clrscan, len, fp));
//    return 0;
    }

    public static int freadscan( /* read in a scanline */
            COLOR[] scanline,
            int len,
            InputStream fp) {
//	register COLR  *clrscan;
//
//	if ((clrscan = (COLR *)tempbuffer(len*sizeof(COLR))) == NULL)
//		return(-1);
//	if (freadcolrs(clrscan, len, fp) < 0)
//		return(-1);
//					/* convert scanline */
//	colr_color(scanline[0], clrscan[0]);
//	while (--len > 0) {
//		scanline++; clrscan++;
//		if (clrscan[0][RED] == clrscan[-1][RED] &&
//			    clrscan[0][GRN] == clrscan[-1][GRN] &&
//			    clrscan[0][BLU] == clrscan[-1][BLU] &&
//			    clrscan[0][EXP] == clrscan[-1][EXP])
//			copycolor(scanline[0], scanline[-1]);
//		else
//			colr_color(scanline[0], clrscan[0]);
//	}
        return (0);
    }

    static void setcolr( /* assign a short color value */
            COLR clr,
            double r, double g, double b) {
        double d;
        int e;

        d = r > g ? r : g;
        if (b > d) {
            d = b;
        }

        if (d <= 1e-32) {
            clr.data[COLOR.RED] = clr.data[COLOR.GRN] = clr.data[COLOR.BLU] = 0;
            clr.data[COLOR.EXP] = 0;
            return;
        }

        FRexpResult fr = PORTIO.frexp3(d);
        e = fr.exponent;
        d = (long) (fr.mantissa * 255.9999 / d);
//	d = frexp(d, &e) * 255.9999 / d;

        if (r > 0.0) {
            clr.data[COLOR.RED] = (byte) (r * d);
        } else {
            clr.data[COLOR.RED] = 0;
        }
        if (g > 0.0) {
            clr.data[COLOR.GRN] = (byte) (g * d);
        } else {
            clr.data[COLOR.GRN] = 0;
        }
        if (b > 0.0) {
            clr.data[COLOR.BLU] = (byte) (b * d);
        } else {
            clr.data[COLOR.BLU] = 0;
        }

        clr.data[COLOR.EXP] = (byte) (e + COLXS);
    }

    public static void colr_color( /* convert short to float color */
            COLOR col,
            byte[] clr) {
        double f;

        if (clr[COLOR.EXP] == 0) {
            col.data[COLOR.RED] = col.data[COLOR.GRN] = col.data[COLOR.BLU] = 0.0f;
        } else {
            f = PORTIO.ldexp(1.0, (int) clr[COLOR.EXP] - (COLXS + 8));
            col.data[COLOR.RED] = (float) ((clr[COLOR.RED] + 0.5f) * f);
            col.data[COLOR.GRN] = (float) ((clr[COLOR.GRN] + 0.5f) * f);
            col.data[COLOR.BLU] = (float) ((clr[COLOR.BLU] + 0.5f) * f);
        }
    }
}
