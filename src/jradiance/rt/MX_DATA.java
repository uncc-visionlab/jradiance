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

import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OBJECT_STRUCTURE;

/**
 *
 * @author arwillis
 */
public class MX_DATA {

    /*
     *  mx_data.c - routine for stored mixtures.
     */

    /*
     *  A stored mixture is specified:
     *
     *	modifier mixdata name
     *	6+ foremod backmod func dfname vfname v0 v1 .. xf
     *	0
     *	n A1 A2 ..
     *
     *  A picture mixture is specified as:
     *
     *	modifier mixpict name
     *	7+ foremod backmod func pfname vfname vx vy xf
     *	0
     *	n A1 A2 ..
     *
     *
     *  Vfname is the name of the file where the variable definitions
     *  can be found.  The list of real arguments can be accessed by
     *  definitions in the file.  Dfname is the data file.
     *  (Pfname is a picture file.)
     *  The dimensions of the data files and the number
     *  of variables must match.  The func is a single argument
     *  function in the case of mixdata (three argument in the case
     *  of mixpict), which returns the corrected data value given the
     *  interpolated value from the file.  The xf is a transformation
     *  to get from the original coordinates to the current coordinates.
     */
    public static class MX_DATA1 extends OBJECT_STRUCTURE {

        @Override
        public int octree_function(Object... obj) {
//        throw new UnsupportedOperationException("Not supported yet.");
            return mx_data((OBJREC) obj[0], (RAY) obj[1]);
        }
    }

    static int mx_data( /* interpolate mixture data */
            OBJREC m,
            RAY r) {
//	OBJECT	obj;
//	double  coef;
//	double  pt[MAXDIM];
//	DATARRAY  *dp;
//	OBJECT  mod[2];
//	register MFUNC  *mf;
//	register int  i;
//
//	if (m->oargs.nsargs < 6)
//		objerror(m, USER, "bad # arguments");
//	obj = objndx(m);
//	for (i = 0; i < 2; i++)
//		if (!strcmp(m->oargs.sarg[i], VOIDID))
//			mod[i] = OVOID;
//		else if ((mod[i] = lastmod(obj, m->oargs.sarg[i])) == OVOID) {
//			sprintf(errmsg, "undefined modifier \"%s\"",
//					m->oargs.sarg[i]);
//			objerror(m, USER, errmsg);
//		}
//	dp = getdata(m->oargs.sarg[3]);
//	i = (1 << dp->nd) - 1;
//	mf = getfunc(m, 4, i<<5, 0);
//	setfunc(m, r);
//	errno = 0;
//	for (i = 0; i < dp->nd; i++) {
//		pt[i] = evalue(mf->ep[i]);
//		if (errno == EDOM || errno == ERANGE)
//			goto computerr;
//	}
//	coef = datavalue(dp, pt);
//	errno = 0;
//	coef = funvalue(m->oargs.sarg[2], 1, &coef);
//	if (errno == EDOM || errno == ERANGE)
//		goto computerr;
//	if (raymixture(r, mod[0], mod[1], coef)) {
//		if (m->omod != OVOID)
//			objerror(m, USER, "inappropriate modifier");
//		return(1);
//	}
//	return(0);
//computerr:
//	objerror(m, WARNING, "compute error");
        return (0);
    }

    public static class MX_PDATA extends OBJECT_STRUCTURE {

        @Override
        public int octree_function(Object... obj) {
//            throw new UnsupportedOperationException("Not supported yet.");
            return mx_pdata((OBJREC) obj[0], (RAY) obj[1]);
        }
    }

    static int mx_pdata( /* interpolate mixture picture */
            OBJREC m,
            RAY r) {
//	OBJECT	obj;
//	double	col[3], coef;
//	double  pt[MAXDIM];
//	DATARRAY  *dp;
//	OBJECT  mod[2];
//	register MFUNC  *mf;
//	register int  i;
//
//	if (m->oargs.nsargs < 7)
//		objerror(m, USER, "bad # arguments");
//	obj = objndx(m);
//	for (i = 0; i < 2; i++)
//		if (!strcmp(m->oargs.sarg[i], VOIDID))
//			mod[i] = OVOID;
//		else if ((mod[i] = lastmod(obj, m->oargs.sarg[i])) == OVOID) {
//			sprintf(errmsg, "undefined modifier \"%s\"",
//					m->oargs.sarg[i]);
//			objerror(m, USER, errmsg);
//		}
//	dp = getpict(m->oargs.sarg[3]);
//	mf = getfunc(m, 4, 0x3<<5, 0);
//	setfunc(m, r);
//	errno = 0;
//	pt[1] = evalue(mf->ep[0]);	/* y major ordering */
//	pt[0] = evalue(mf->ep[1]);
//	if (errno == EDOM || errno == ERANGE)
//		goto computerr;
//	for (i = 0; i < 3; i++)		/* get pixel from picture */
//		col[i] = datavalue(dp+i, pt);
//	errno = 0;			/* evaluate function on pixel */
//	coef = funvalue(m->oargs.sarg[2], 3, col);
//	if (errno == EDOM || errno == ERANGE)
//		goto computerr;
//	if (raymixture(r, mod[0], mod[1], coef)) {
//		if (m->omod != OVOID)
//			objerror(m, USER, "inappropriate modifier");
//		return(1);
//	}
        return (0);
//computerr:
//	objerror(m, WARNING, "compute error");
//	return(0);
    }
}
