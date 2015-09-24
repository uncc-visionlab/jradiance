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

import jradiance.common.MODOBJECT;
import jradiance.common.OBJECT;
import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OBJECT_STRUCTURE;
import jradiance.common.OBJSET;

/**
 *
 * @author arwillis
 */
public class M_CLIP extends OBJECT_STRUCTURE {
//#ifndef lint
//static const char	RCSid[] = "$Id$";
//#endif
/*
     *  m_clip.c - routine for clipped (cut) objects.
     */

//#include "copyright.h"
//
//#include  "ray.h"
//#include  "rtotypes.h"

    /*
     *  Clipping objects permit holes and sections to be taken out
     *  of other objects.  The method is simple:  
     *
     *  The argument is the clipped materials;
     *  the first is used to shade upon exit.
     */
    @Override
    public int octree_function(Object... obj) {
        //throw new UnsupportedOperationException("Not supported yet.");
        return m_clip((OBJREC)obj[0], (RAY)obj[1]);                
    }    

    public static int m_clip( /* clip objects from ray */
            OBJREC m,
            RAY r) {
        int[] cset = new int[OBJECT.MAXSET + 1], modset;
        int obj, mod;
        int entering;
        int i;

        obj = MODOBJECT.objndx(m);
        if ((modset = m.os.getOS()) == null) {
            if (m.oargs.nsargs < 1 || m.oargs.nsargs > OBJECT.MAXSET) {
//			objerror(m, USER, "bad # arguments");
            }
            modset = new int[m.oargs.nsargs + 1];
            if (modset == null) {
//			error(SYSTEM, "out of memory in m_clip");
            }
            modset[0] = 0;
            for (i = 0; i < m.oargs.nsargs; i++) {
                if (m.oargs.sarg[i].equals(OBJECT.VOIDID)) {
                    continue;
                }
                if ((mod = MODOBJECT.lastmod(obj, m.oargs.sarg[i])) == OBJECT.OVOID) {
//				sprintf(errmsg, "unknown modifier \"%s\"",
//						m->oargs.sarg[i]);
//				objerror(m, WARNING, errmsg);
                    continue;
                }
                if (OBJSET.inset(modset, mod) != 0) {
//				objerror(m, WARNING, "duplicate modifier");
                    continue;
                }
                OBJSET.insertelem(modset, mod);
            }
            m.os.setOS(modset);
        }
        if (r == null) {
            return (0);			/* just initializing */
        }
        if (r.clipset != null) {
            OBJSET.setcopy(cset, 0, r.clipset);
        } else {
            cset[0] = 0;
        }

        entering = r.rod > 0.0 ? 1 : 0;		/* entering clipped region? */

        for (i = modset[0]; i > 0; i--) {
            if (entering != 0) {
                if (OBJSET.inset(cset, modset[i]) == 0) {
                    if (cset[0] >= OBJECT.MAXSET) {
//					error(INTERNAL, "set overflow in m_clip");
                    }
                    OBJSET.insertelem(cset, modset[i]);
                }
            } else {
                if (OBJSET.inset(cset, modset[i]) != 0) {
                    OBJSET.deletelem(cset, modset[i]);
                }
            }
        }
        /* compute ray value */
        r.newcset = cset;
        if (m.oargs.sarg[0].equals(OBJECT.VOIDID)) {
            int inside = 0;
            RAY rp;
            /* check for penetration */
            for (rp = r; rp.parent != null; rp = rp.parent) {
                if ((rp.rtype & RAY.RAYREFL) == 0 && rp.parent.ro != null
                        && OBJSET.inset(modset, rp.parent.ro.omod) != 0) {
                    if (rp.parent.rod > 0.0) {
                        inside++;
                    } else {
                        inside--;
                    }
                }
            }
            if (inside > 0) {	/* we just hit the object */
                RAYTRACE.flipsurface(r);
                return (RAYTRACE.rayshade(r, MODOBJECT.lastmod(obj, m.oargs.sarg[0])));
            }
        }
        RAYTRACE.raytrans(r);			/* else transfer ray */
        return (1);
    }
}
