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

import jradiance.common.FVECT;
import jradiance.common.OBJECT;
import jradiance.common.OBJECT_STRUCTURE;
import jradiance.common.OTYPES;
import jradiance.common.ZEROES;

/**
 *
 * @author arwillis
 */
public class SPHERE extends OBJECT_STRUCTURE {
    /*
     *  sphere.c - compute ray intersection with spheres.
     */

    @Override
    public int octree_function(Object... obj) //public static int
    //o_sphere(			/* compute intersection with sphere */
    //	OBJECT.OBJREC  so,
    //	RAY  r
    //)
    {
        OBJECT.OBJREC so = (OBJECT.OBJREC) obj[0];
        RAY r = (RAY) obj[1];
        double a, b, c;	/* coefficients for quadratic equation */
        double[] root = new double[2];	/* quadratic roots */
        int nroots;
        double t = 0;
        double[] ap;
        int i;

        if (so.oargs.nfargs != 4) {
//		objerror(so, USER, "bad # arguments");
        }
        ap = so.oargs.farg;
        if (ap[3] < -FVECT.FTINY) {
//		objerror(so, WARNING, "negative radius");
            so.otype = (short) (so.otype == OTYPES.OBJ_SPHERE
                    ? OTYPES.OBJ_BUBBLE : OTYPES.OBJ_SPHERE);
            ap[3] = -ap[3];
        } else if (ap[3] <= FVECT.FTINY) {
//		objerror(so, USER, "zero radius");
        }
        /*
         *	We compute the intersection by substituting into
         *  the surface equation for the sphere.  The resulting
         *  quadratic equation in t is then solved for the
         *  smallest positive root, which is our point of
         *  intersection.
         *	Since the ray is normalized, a should always be
         *  one.  We compute it here to prevent instability in the
         *  intersection calculation.
         */
        /* compute quadratic coefficients */
        a = b = c = 0.0;
        for (i = 0; i < 3; i++) {
            a += r.rdir.data[i] * r.rdir.data[i];
            t = r.rorg.data[i] - ap[i];
            b += 2.0 * r.rdir.data[i] * t;
            c += t * t;
        }
        c -= ap[3] * ap[3];

        nroots = ZEROES.quadratic(root, a, b, c);	/* solve quadratic */

        for (i = 0; i < nroots; i++) /* get smallest positive */ {
            if ((t = root[i]) > FVECT.FTINY) {
                break;
            }
        }
        if (i >= nroots) {
            return (0);			/* no positive root */
        }

        if (t >= r.rot) {
            return (0);			/* other is closer */
        }

        r.ro = so;
        r.rot = t;
        /* compute normal */
        a = ap[3];
        if (so.otype == OTYPES.OBJ_BUBBLE) {
            a = -a;			/* reverse */
        }
        for (i = 0; i < 3; i++) {
            r.rop.data[i] = r.rorg.data[i] + r.rdir.data[i] * t;
            r.ron.data[i] = (r.rop.data[i] - ap[i]) / a;
        }
        r.rod = -FVECT.DOT(r.rdir, r.ron);
        r.rox = null;
        r.pert.data[0] = r.pert.data[1] = r.pert.data[2] = 0.0;
        r.uv[0] = r.uv[1] = 0.0;

        return (1);			/* hit */
    }
}
