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

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jradiance.common.COLOROPS;
import jradiance.common.COLORS;
import jradiance.common.COLORS.COLOR;
import jradiance.common.COLORS.COLR;
import jradiance.common.FVECT;
import jradiance.common.GETLIBPATH;
import jradiance.common.GETPATH;
import jradiance.common.HEADER;
import jradiance.common.IMAGE;
import jradiance.common.PATHS;
import jradiance.common.RESOLU;
import jradiance.common.SAVESTR;

/**
 *
 * @author arwillis
 */
public class DATA {
    /*
     * Header for data file loading and computation routines.
     */

    public static final int MAXDDIM = 5;		/* maximum data dimensions */

//#define  DATATYPE	float		/* single precision to save space */
    public static final char DATATY = 'f';		/* format for DATATYPE */


    public static class DATARRAY implements Cloneable {

        String name;			/* name of our data */

        short type;			/* DATATY, RED, GRN or BLU */

        short nd;			/* number of dimensions */


        public class DIM {

            float org, siz;		/* coordinate domain */

            int ne;			/* number of elements */

            float[] p;			/* point locations */

        } 			/* dimension specifications */

        DIM[] dim = new DIM[MAXDDIM];

        public class ARR {

            float[] d;			/* float data */

            COLR[] c = null;		/* RGB data */

        }				/* the data */

        ARR arr = new ARR();
        DATARRAY[] next;		/* next array in list */


        public DATARRAY() {
            for (int ii = 0; ii < dim.length; ii++) {
                dim[ii] = new DIM();
            }
        }

        public static DATARRAY copy(DATARRAY pVal) {
            DATARRAY cVal = new DATARRAY();
            try {
                cVal = (DATARRAY) pVal.clone();
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(DATA.class.getName()).log(Level.SEVERE, null, ex);
            }
            return cVal;
        }
    }			/* a data array */

//extern DATARRAY	*getdata(char *dname);
//extern DATARRAY	*getpict(char *pname);
//extern void	freedata(DATARRAY *dta);
//extern double	datavalue(DATARRAY *dp, double *pt);

    /*
     *  data.c - routines dealing with interpolated data.
     */

    /* picture memory usage before warning */
    public static final int PSIZWARN = 10000000;
    public static final int TABSIZ = 97;		/* table size (prime) */


    public static int hash(String s) {
        return (SAVESTR.shash(s.toCharArray()) % TABSIZ);
    }
    static DATARRAY[][] dtab = new DATARRAY[TABSIZ][];		/* data array list */

//static gethfunc headaspect;

//extern DATARRAY *
//getdata(				/* get data array dname */
//	char  *dname
//)
//{
//	char  *dfname;
//	FILE  *fp;
//	int  asize;
//	register int  i, j;
//	register DATARRAY  *dp;
//						/* look for array in list */
//	for (dp = dtab[hash(dname)]; dp != null; dp = dp.next)
//		if (!strcmp(dname, dp.name))
//			return(dp);		/* found! */
	/*
	 *	If we haven't loaded the data already, we will look
	 *  for it in the directories specified by the library path.
	 *
	 *	The file has the following format:
	 *
	 *		N
	 *		beg0	end0	n0
	 *		beg1	end1	n1
	 *		. . .
	 *		begN	endN	nN
	 *		data, later dimensions changing faster
	 *		. . .
	 *
	 *	For irregularly spaced points, the following can be
	 *  substituted for begi endi ni:
	 *
	 *		0 0 ni p0i p1i .. pni
	 */

//	if ((dfname = getpath(dname, getrlibpath(), R_OK)) == null) {
//		sprintf(errmsg, "cannot find data file \"%s\"", dname);
//		error(USER, errmsg);
//	}
//	if ((fp = fopen(dfname, "r")) == null) {
//		sprintf(errmsg, "cannot open data file \"%s\"", dfname);
//		error(SYSTEM, errmsg);
//	}
//							/* get dimensions */
//	if (fgetval(fp, 'i', (char *)&asize) <= 0)
//		goto scanerr;
//	if ((asize <= 0) | (asize > MAXDDIM)) {
//		sprintf(errmsg, "bad number of dimensions for \"%s\"", dname);
//		error(USER, errmsg);
//	}
//	if ((dp = (DATARRAY *)malloc(sizeof(DATARRAY))) == null)
//		goto memerr;
//	dp.name = savestr(dname);
//	dp.type = DATATY;
//	dp.nd = asize;
//	asize = 1;
//	for (i = 0; i < dp.nd; i++) {
//		if (fgetval(fp, DATATY, (char *)&dp.dim[i].org) <= 0)
//			goto scanerr;
//		if (fgetval(fp, DATATY, (char *)&dp.dim[i].siz) <= 0)
//			goto scanerr;
//		if (fgetval(fp, 'i', (char *)&dp.dim[i].ne) <= 0)
//			goto scanerr;
//		if (dp.dim[i].ne < 2)
//			goto scanerr;
//		asize *= dp.dim[i].ne;
//		if ((dp.dim[i].siz -= dp.dim[i].org) == 0) {
//			dp.dim[i].p = (DATATYPE *)
//					malloc(dp.dim[i].ne*sizeof(DATATYPE));
//			if (dp.dim[i].p == null)
//				goto memerr;
//			for (j = 0; j < dp.dim[i].ne; j++)
//				if (fgetval(fp, DATATY,
//						(char *)&dp.dim[i].p[j]) <= 0)
//					goto scanerr;
//			for (j = 1; j < dp.dim[i].ne-1; j++)
//				if ((dp.dim[i].p[j-1] < dp.dim[i].p[j]) !=
//					(dp.dim[i].p[j] < dp.dim[i].p[j+1]))
//					goto scanerr;
//			dp.dim[i].org = dp.dim[i].p[0];
//			dp.dim[i].siz = dp.dim[i].p[dp.dim[i].ne-1]
//						- dp.dim[i].p[0];
//		} else
//			dp.dim[i].p = null;
//	}
//	if ((dp.arr.d = (DATATYPE *)malloc(asize*sizeof(DATATYPE))) == null)
//		goto memerr;
//	
//	for (i = 0; i < asize; i++)
//		if (fgetval(fp, DATATY, (char *)&dp.arr.d[i]) <= 0)
//			goto scanerr;
//	fclose(fp);
//	i = hash(dname);
//	dp.next = dtab[i];
//	return(dtab[i] = dp);
//
//memerr:
//	error(SYSTEM, "out of memory in getdata");
//scanerr:
//	sprintf(errmsg, "%s in data file \"%s\"",
//			feof(fp) ? "unexpected EOF" : "bad format", dfname);
//	error(USER, errmsg);
//	return null; /* pro forma return */
//}
//
//
    public static class HEADASPECT implements HEADER.gethfunc {

        static int headaspect( /* check string for aspect ratio */
                String s,
                Object iap //	void  *iap
                ) {
            char[] fmt = new char[32];

//	if (isaspect(s))
//		*(double*)iap *= aspectval(s);
//	else if (formatval(fmt, s) && !globmatch(PICFMT, fmt))
//		*(double*)iap = 0.0;
            if (COLORS.isaspect(s) != 0) {
                ((ArrayList) iap).add(new Double(COLORS.aspectval(s)));
            } else if (HEADER.formatval(fmt, s.toCharArray()) != 0
                    && HEADER.globmatch(COLORS.PICFMT.toCharArray(), fmt) == 0) {
                ((ArrayList) iap).add(new Double(0.0));
            }
            return (0);
        }

        @Override
        public int hfunc(char[] s, Object obj) throws IOException {
            return headaspect(new String(s), obj);
        }
    }

    static DATARRAY[] getpict( /* get picture pname */
            String pname) throws IOException {
        double inpaspect;
        String pfname;
        InputStream fp;
        COLR[] scanin;
        int sl, ns;
        RESOLU inpres = new RESOLU();
        double[] loc = new double[2];
        int y;
        int x, i;
        DATARRAY[] pp;
        /* look for array in list */
        for (pp = dtab[hash(pname)]; pp != null; pp = pp[0].next) {
            if (pname.equals(pp[0].name)) {
                return (pp);		/* found! */
            }
        }

        if ((pfname = GETPATH.getpath(pname, GETLIBPATH.getrlibpath(), PATHS.R_OK)) == null) {
//		sprintf(errmsg, "cannot find picture file \"%s\"", pname);
//		error(USER, errmsg);
        }
        pp = new DATARRAY[3];
        for (int ii = 0; ii < pp.length; ii++) {
            pp[ii] = new DATARRAY();
        }
//	if ((pp = (DATARRAY *)malloc(3*sizeof(DATARRAY))) == null)
//		goto memerr;

        pp[0].name = SAVESTR.savestr(pname);

        if ((fp = new FileInputStream(pfname)) == null) {
//		sprintf(errmsg, "cannot open picture file \"%s\"", pfname);
//		error(SYSTEM, errmsg);
        }
//	SET_FILE_BINARY(fp);
//						/* get dimensions */
        inpaspect = 1.0;
        ArrayList inpaspectArr = new ArrayList();
        HEADER.getheader(fp, new HEADASPECT(), inpaspectArr);
        if (inpaspectArr.size() > 0) {
            inpaspect = ((Double) inpaspectArr.get(0)).doubleValue();
        }
        if (inpaspect <= FVECT.FTINY || RESOLU.fgetsresolu(inpres, fp) == 0) {
//		goto readerr;
        }
        pp[0].nd = 2;
        pp[0].dim[0].ne = inpres.yr;
        pp[0].dim[1].ne = inpres.xr;
        pp[0].dim[0].org =
                pp[0].dim[1].org = 0.0f;
        if (inpres.xr <= inpres.yr * inpaspect) {
            pp[0].dim[0].siz = (float) (inpaspect
                    * (double) inpres.yr / inpres.xr);
            pp[0].dim[1].siz = 1.0f;
        } else {
            pp[0].dim[0].siz = 1.0f;
            pp[0].dim[1].siz = (float) ((double) inpres.xr / inpres.yr
                    / inpaspect);
        }
        pp[0].dim[0].p = pp[0].dim[1].p = null;
        sl = RESOLU.scanlen(inpres);				/* allocate array */
        ns = RESOLU.numscans(inpres);
        int sizeofCOLR = 4;
        i = ns * sl * sizeofCOLR;
//#if PSIZWARN
        if (i > PSIZWARN) {				/* memory warning */
//		sprintf(errmsg, "picture file \"%s\" using %.1f MB of memory",
//				pname, i*(1.0/(1024*1024)));
//		error(WARNING, errmsg);

        }
//#endif
        if ((pp[0].arr.c = new COLR[ns * sl]) == null) {
//		goto memerr;
        }
        /* load picture */
        if ((scanin = new COLR[sl]) == null) {
//		goto memerr;
        }
        for (int ii = 0; ii < scanin.length; ii++) {
            scanin[ii] = new COLR();
        }
        for (y = 0; y < ns; y++) {
            if (COLORS.freadcolrs(scanin, sl, fp) < 0) {
//			goto readerr;
            }
            for (x = 0; x < sl; x++) {
                IMAGE.pix2loc(loc, inpres, x, y);
                i = (int) (loc[1] * inpres.yr) * inpres.xr
                        + (int) (loc[0] * inpres.xr);
                pp[0].arr.c[i] = COLR.copycolr(scanin[x]);

//			COLR.copycolr(pp[0].arr.c[i], scanin[x].data);
//                System.arraycopy(scanin[x].data, 0, pp[0].arr.c[i].data, 0, scanin[x].data.length);
            }
        }
//	free((void *)scanin);
        fp.close();
        i = hash(pname);
        pp[0].next = dtab[i];		/* link into picture list */
        pp[1] = DATARRAY.copy(pp[0]);
        pp[2] = DATARRAY.copy(pp[0]);
        pp[0].type = COLOR.RED;		/* differentiate RGB records */
        pp[1].type = COLOR.GRN;
        pp[2].type = COLOR.BLU;
        return (dtab[i] = pp);
//
//memerr:
//	error(SYSTEM, "out of memory in getpict");
//readerr:
//	sprintf(errmsg, "bad picture file \"%s\"", pfname);
//	error(USER, errmsg);
//	return null; /* pro forma return */
    }
//
//
//extern void
//freedata(			/* release data array reference */
//	DATARRAY  *dta
//)
//{
//	DATARRAY  head;
//	int  hval, nents;
//	register DATARRAY  *dpl, *dp;
//	register int  i;
//
//	if (dta == null) {			/* free all if null */
//		hval = 0; nents = TABSIZ;
//	} else {
//		hval = hash(dta.name); nents = 1;
//	}
//	while (nents--) {
//		head.next = dtab[hval];
//		dpl = &head;
//		while ((dp = dpl.next) != null)
//			if ((dta == null) | (dta == dp)) {
//				dpl.next = dp.next;
//				if (dp.type == DATATY)
//					free((void *)dp.arr.d);
//				else
//					free((void *)dp.arr.c);
//				for (i = 0; i < dp.nd; i++)
//					if (dp.dim[i].p != null)
//						free((void *)dp.dim[i].p);
//				freestr(dp.name);
//				free((void *)dp);
//			} else
//				dpl = dp;
//		dtab[hval++] = head.next;
//	}
//}
//

    public static DATARRAY[] getpict(String pname, BufferedImage bi) {
        double inpaspect;
        String pfname;
        InputStream fp;
        COLR[] scanin;
        int sl, ns;
        RESOLU inpres = new RESOLU();
        double[] loc = new double[2];
        int y;
        int x, i;
        DATARRAY[] pp;
        pp = new DATARRAY[3];
        for (int ii = 0; ii < pp.length; ii++) {
            pp[ii] = new DATARRAY();
        }
//	if ((pp = (DATARRAY *)malloc(3*sizeof(DATARRAY))) == null)
//		goto memerr;

        pp[0].name = SAVESTR.savestr(pname);

        inpaspect = 1.0;
        ArrayList inpaspectArr = new ArrayList();
        inpres = new RESOLU();
        inpres.rt = 0;
        inpres.rt |= RESOLU.YMAJOR;
        inpres.rt |= RESOLU.YDECR;
        inpres.xr = bi.getWidth();
        inpres.yr = bi.getHeight();
        pp[0].nd = 2;
        pp[0].dim[0].ne = inpres.yr;
        pp[0].dim[1].ne = inpres.xr;
        pp[0].dim[0].org = pp[0].dim[1].org = 0.0f;
        if (inpres.xr <= inpres.yr * inpaspect) {
            pp[0].dim[0].siz = (float) (inpaspect
                    * (double) inpres.yr / inpres.xr);
            pp[0].dim[1].siz = 1.0f;
        } else {
            pp[0].dim[0].siz = 1.0f;
            pp[0].dim[1].siz = (float) ((double) inpres.xr / inpres.yr
                    / inpaspect);
        }
        pp[0].dim[0].p = pp[0].dim[1].p = null;
        sl = RESOLU.scanlen(inpres);				/* allocate array */
        ns = RESOLU.numscans(inpres);
        int sizeofCOLR = 4;
        i = ns * sl * sizeofCOLR;
//#if PSIZWARN
        if (i > PSIZWARN) {				/* memory warning */
//		sprintf(errmsg, "picture file \"%s\" using %.1f MB of memory",
//				pname, i*(1.0/(1024*1024)));
//		error(WARNING, errmsg);

        }
//#endif
        if ((pp[0].arr.c = new COLR[ns * sl]) == null) {
//		goto memerr;
        }
        /* load picture */
        if ((scanin = new COLR[sl]) == null) {
//		goto memerr;
        }
        for (int ii = 0; ii < scanin.length; ii++) {
            scanin[ii] = new COLR();
        }
        int xmax = sl;
        // get RGB vals for the image
        DataBuffer databuf = bi.getData().getDataBuffer();
        ColorModel cm = bi.getColorModel();
        byte[] pixelBytes = null;
        if (cm.getColorSpace().isCS_sRGB() && databuf instanceof DataBufferByte) {
            pixelBytes = ((DataBufferByte)databuf).getData();            
        }
        double gamcor = 2.2;			/* gamma correction value */
//        double gamcor = 1.0;			/* gamma correction value */
        COLOROPS.setcolrgam(gamcor);
        int byteIdx = 0;
        for (y = 0; y < ns; y++) {
            // convert a scanline here by filling the scanin array
            // then calling gambs_colrs which does RGB --> RGBE
            for (int pixIdx = 0; pixIdx < scanin.length; pixIdx++) {
                byteIdx++;
                for (int compIdx = 0; compIdx < 3; compIdx++) {
                    scanin[pixIdx].data[compIdx] = pixelBytes[byteIdx++];
                }
//                byteIdx++;
            }
            COLOROPS.gambs_colrs(scanin, xmax);

            for (x = 0; x < sl; x++) {
                IMAGE.pix2loc(loc, inpres, x, y);
                i = (int) (loc[1] * inpres.yr) * inpres.xr
                        + (int) (loc[0] * inpres.xr);
                pp[0].arr.c[i] = COLR.copycolr(scanin[x]);

//			COLR.copycolr(pp[0].arr.c[i], scanin[x].data);
//                System.arraycopy(scanin[x].data, 0, pp[0].arr.c[i].data, 0, scanin[x].data.length);
            }
        }
//	free((void *)scanin);
        i = hash(pname);
        pp[0].next = dtab[i];		/* link into picture list */
        pp[1] = DATARRAY.copy(pp[0]);
        pp[2] = DATARRAY.copy(pp[0]);
        pp[0].type = COLOR.RED;		/* differentiate RGB records */
        pp[1].type = COLOR.GRN;
        pp[2].type = COLOR.BLU;
        return (dtab[i] = pp);
    }

    static double datavalue( /* interpolate data value at a point */
            DATARRAY dp,
            double[] pt) {
        return datavalue(dp, pt, 0, 0);

    }

    static double datavalue( /* interpolate data value at a point */
            DATARRAY dp,
            double[] pt,
            int dpoffset,
            int cdim) {
        DATARRAY sd = new DATARRAY();
        int asize = 1;
        int lower, upper;
        int i;
        double x, y0, y1;
        /* set up dimensions for recursion */
        if (dp.nd > 1) {
            sd.name = dp.name;
            sd.type = dp.type;
            sd.nd = (short) (dp.nd - 1);
            asize = 1;
            for (i = 0; i < sd.nd; i++) {
                sd.dim[i].org = dp.dim[i + 1].org;
                sd.dim[i].siz = dp.dim[i + 1].siz;
                sd.dim[i].p = dp.dim[i + 1].p;
                asize *= sd.dim[i].ne = dp.dim[i + 1].ne;
            }
        }
        /* get independent variable */
        if (dp.dim[0].p == null) {		/* evenly spaced points */
//		x = (pt[0] - dp.dim[0].org)/dp.dim[0].siz;
            x = (pt[cdim] - dp.dim[0].org) / dp.dim[0].siz;
            x *= (double) (dp.dim[0].ne - 1);
            i = (int) x;
            if (i < 0) {
                i = 0;
            } else if (i > dp.dim[0].ne - 2) {
                i = dp.dim[0].ne - 2;
            }
        } else {				/* unevenly spaced points */
            if (dp.dim[0].siz > 0.0) {
                lower = 0;
                upper = dp.dim[0].ne;
            } else {
                lower = dp.dim[0].ne;
                upper = 0;
            }
            do {
                i = (lower + upper) >> 1;
//			if (pt[0] >= dp.dim[0].p[i])
                if (pt[cdim] >= dp.dim[0].p[i]) {
                    lower = i;
                } else {
                    upper = i;
                }
            } while (i != (lower + upper) >> 1);
            if (i > dp.dim[0].ne - 2) {
                i = dp.dim[0].ne - 2;
            }
//		x = i + (pt[0] - dp.dim[0].p[i]) /
//				(dp.dim[0].p[i+1] - dp.dim[0].p[i]);
            x = i + (pt[cdim] - dp.dim[0].p[i])
                    / (dp.dim[0].p[i + 1] - dp.dim[0].p[i]);
        }
        /* get dependent variable */
        if (dp.nd > 1) {
            if (dp.type == DATATY) {
//			sd.arr.d = dp.arr.d + i*asize;
//			y0 = datavalue(sd, pt+1);
//			sd.arr.d = dp.arr.d + (i+1)*asize;
//			y1 = datavalue(sd, pt+1);
                sd.arr.d = dp.arr.d;// + i*asize;
                y0 = datavalue(sd, pt, dpoffset + i * asize, cdim + 1);
                sd.arr.d = dp.arr.d;// (i+1)*asize;
                y1 = datavalue(sd, pt, dpoffset + (i + 1) * asize, cdim + 1);
            } else {
//			sd.arr.c = dp.arr.c + i*asize;
//			y0 = datavalue(sd, pt+1);
//			sd.arr.c = dp.arr.c + (i+1)*asize;
//			y1 = datavalue(sd, pt+1);
                sd.arr.c = dp.arr.c;// + i*asize;
                y0 = datavalue(sd, pt, dpoffset + i * asize, cdim + 1);
                sd.arr.c = dp.arr.c;// + (i+1)*asize;
                y1 = datavalue(sd, pt, dpoffset + (i + 1) * asize, cdim + 1);
            }
        } else {
            if (dp.type == DATATY) {
                y0 = dp.arr.d[dpoffset + i];
                y1 = dp.arr.d[dpoffset + i + 1];
            } else {
//			y0 = COLOR.colrval(dp.arr.c[dpoffset+i],dp.type);
//			y1 = COLOR.colrval(dp.arr.c[dpoffset+i+1],dp.type);
                //COLR c1 = new COLR();
                //System.arraycopy(dp.arr.c, dpoffset + i, c1.data, 0, 4);
                //COLR c2 = new COLR();
                //System.arraycopy(dp.arr.c, dpoffset + i + 1, c2.data, 0, 4);
                COLR c1 = dp.arr.c[dpoffset + i];
                COLR c2 = dp.arr.c[dpoffset + i + 1];
                y0 = COLOR.colrval(c1, dp.type);
                y1 = COLOR.colrval(c2, dp.type);
            }
        }
        /*
         * Extrapolate as far as one division, then
         * taper off harmonically to zero.
         */
        if (x > i + 2) {
            return ((2 * y1 - y0) / (x - (i - 1)));
        }
        if (x < i - 1) {
            return ((2 * y0 - y1) / (i - x));
        }
        return (y0 * ((i + 1) - x) + y1 * (x - i));
    }
}
