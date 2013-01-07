/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
