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
public class MX_FUNC extends OBJECT_STRUCTURE {
    /*
     *  mx_func.c - routine for mixture functions.
     */
    /*
     *	A mixture function is specified:
     *
     *	modifier mixfunc name
     *	4+ foremod backmod varname vfname xf
     *	0
     *	n A1 A2 ..
     *
     *  Vfname is the name of the file where the variable definition
     *  can be found.  The list of real arguments can be accessed by
     *  definitions in the file.  The xf is a transformation
     *  to get from the original coordinates to the current coordinates.
     */

    @Override
    public int octree_function(Object... obj) {
        return mx_func((OBJREC) obj[0], (RAY) obj[1]);
    }

    static int mx_func( /* compute mixture function */
            OBJREC m,
            RAY r) {
        int obj;
        throw new UnsupportedOperationException("Not supported yet.");
//	register int  i;
//	double  coef;
//	OBJECT  mod[2];
//	register MFUNC  *mf;
//
//	if (m->oargs.nsargs < 4)
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
//	mf = getfunc(m, 3, 0x4, 0);
//	setfunc(m, r);
//	errno = 0;
//	coef = evalue(mf->ep[0]);
//	if (errno == EDOM || errno == ERANGE) {
//		objerror(m, WARNING, "compute error");
//		return(0);
//	}
//	if (raymixture(r, mod[0], mod[1], coef)) {
//		if (m->omod != OVOID)
//			objerror(m, USER, "inappropriate modifier");
//		return(1);
//	}
//        return (0);
    }
}
