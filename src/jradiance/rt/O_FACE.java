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

import jradiance.common.FACE;
import jradiance.common.FVECT;
import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OBJECT_STRUCTURE;

/**
 *
 * @author arwillis
 */
public class O_FACE extends OBJECT_STRUCTURE {
    /*
     *  o_face.c - compute ray intersection with faces.
     */

    @Override
    public int octree_function(Object... obj) {
        return o_face((OBJREC) obj[0], (RAY) obj[1]);
    }

    int o_face( /* compute intersection with polygonal face */
            OBJREC o,
            RAY r) {
        double rdot;		/* direction . normal */
        double t;		/* distance to intersection */
        FVECT pisect = new FVECT();		/* intersection point */
        FACE f;	/* face record */

        f = FACE.getface(o);

        /*
         *  First, we find the distance to the plane containing the
         *  face.  If this distance is less than zero or greater
         *  than a previous intersection, we return.  Otherwise,
         *  we determine whether in fact the ray intersects the
         *  face.  The ray intersects the face if the
         *  point of intersection with the plane of the face
         *  is inside the face.
         */
        /* compute dist. to plane */
        rdot = -FVECT.DOT(r.rdir, f.norm);
        if (rdot <= FVECT.FTINY && rdot >= -FVECT.FTINY) /* ray parallels plane */ {
            t = FVECT.FHUGE;
        } else {
            t = (FVECT.DOT(r.rorg, f.norm) - f.offset) / rdot;
        }

        if (t <= FVECT.FTINY || t >= r.rot) /* not good enough */ {
            return (0);
        }
        /* compute intersection */
        FVECT.VSUM(pisect, r.rorg, r.rdir, t);

        if (FACE.inface(pisect, f) == 0) /* ray intersects face? */ {
            return (0);
        }

        r.ro = o;
        r.rot = t;
        FVECT.VCOPY(r.rop, pisect);
        FVECT.VCOPY(r.ron, f.norm);
        r.rod = rdot;
        r.pert.data[0] = r.pert.data[1] = r.pert.data[2] = 0.0;
        r.uv[0] = r.uv[1] = 0.0;
        r.rox = null;
        return (1);				/* hit */
    }
}
