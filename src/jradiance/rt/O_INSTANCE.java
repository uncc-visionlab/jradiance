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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jradiance.common.INSTANCE;
import jradiance.common.MAT4;
import jradiance.common.OBJECT;
import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OBJECT_STRUCTURE;
import jradiance.common.OCTREE;

/**
 *
 * @author arwillis
 */
public class O_INSTANCE extends OBJECT_STRUCTURE {

    /*
     *  o_instance.c - routines for computing ray intersections with octrees.
     */

    @Override
    public int octree_function(Object... obj) {
        try {
            return o_instance((OBJREC) obj[0], (RAY) obj[1]);
        } catch (IOException ex) {
            Logger.getLogger(O_INSTANCE.class.getName()).log(Level.SEVERE, null, ex);
        }
        return OCTREE.O_MISS;
    }
    int
    o_instance(		/* compute ray intersection with octree */
    	OBJREC  o,
    	RAY  r
    ) throws IOException
    {
        RAY rcont;
        double d;
        INSTANCE ins;
        int i;
        /* get the octree */
        ins = INSTANCE.getinstance(o, OCTREE.IO_ALL);
        /* copy and transform ray */
        rcont = r;
        MAT4.multp3(rcont.rorg, r.rorg, ins.x.b.xfm);
        MAT4.multv3(rcont.rdir, r.rdir, ins.x.b.xfm);
        for (i = 0; i < 3; i++) {
            rcont.rdir.data[i] /= ins.x.b.sca;
        }
        rcont.rmax *= ins.x.b.sca;
        /* clear and trace it */
        RAYTRACE.rayclear(rcont);
        if (RAYTRACE.localhit(rcont, ins.obj.scube) == 0) {
            return (0);			/* missed */
        }
        if (rcont.rot * ins.x.f.sca >= r.rot) {
            return (0);			/* not close enough */
        }

        if (o.omod != OBJECT.OVOID) {		/* if we have modifier, use it */
            r.ro = o;
            r.rox = null;
        } else {			/* else use theirs */
            r.ro = rcont.ro;
            if (rcont.rox != null) {
                RAYTRACE.newrayxf(r);		/* allocate transformation */
                /* NOTE: r->rox may equal rcont.rox! */
                MAT4.multmat4(r.rox.f.xfm, rcont.rox.f.xfm, ins.x.f.xfm);
                r.rox.f.sca = rcont.rox.f.sca * ins.x.f.sca;
                MAT4.multmat4(r.rox.b.xfm, ins.x.b.xfm, rcont.rox.b.xfm);
                r.rox.b.sca = ins.x.b.sca * rcont.rox.b.sca;
            } else {
                r.rox = ins.x;
            }
        }
        /* transform it back */
        r.rot = rcont.rot * ins.x.f.sca;
        MAT4.multp3(r.rop, rcont.rop, ins.x.f.xfm);
        MAT4.multv3(r.ron, rcont.ron, ins.x.f.xfm);
        MAT4.multv3(r.pert, rcont.pert, ins.x.f.xfm);
        d = 1. / ins.x.f.sca;
        for (i = 0; i < 3; i++) {
            r.ron.data[i] *= d;
            r.pert.data[i] *= d;
        }
        r.rod = rcont.rod;
        r.uv[0] = rcont.uv[0];
        r.uv[1] = rcont.uv[1];
        /* return hit */
        return (1);
    }
}
