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
package jradiance.ot;

import java.io.IOException;
import jradiance.common.CONE;
import jradiance.common.FACE;
import jradiance.common.FVECT;
import jradiance.common.INSTANCE;
import jradiance.common.MAT4;
import jradiance.common.MESH;
import jradiance.common.MESH.MESHINST;
import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OCTREE;
import jradiance.common.OTYPES;

/**
 *
 * @author arwillis
 */
public class BBOX {
    /*
     *  bbox.c - routines for bounding box computation.
     */

//static void point2bbox(FVECT  p, FVECT  bbmin, FVECT  bbmax);
//static void circle2bbox(FVECT  cent, FVECT  norm, double  rad, FVECT  bbmin, FVECT  bbmax);
    public static void add2bbox( /* expand bounding box to fit object */
            OBJREC o,
            FVECT bbmin,
            FVECT bbmax) throws IOException {
        CONE co;
        FACE fo;
        INSTANCE io;
        MESHINST mi;
        FVECT v = new FVECT();
        int i, j;

        switch (o.otype) {
            case OTYPES.OBJ_SPHERE:
            case OTYPES.OBJ_BUBBLE:
                if (o.oargs.nfargs != 4) {
//			objerror(o, USER, "bad arguments");
                }
                for (i = 0; i < 3; i++) {
                    FVECT.VCOPY(v, new FVECT(o.oargs.farg));
                    v.data[i] -= o.oargs.farg[3];
                    point2bbox(v, bbmin, bbmax);
                    v.data[i] += 2.0 * o.oargs.farg[3];
                    point2bbox(v, bbmin, bbmax);
                }
                break;
            case OTYPES.OBJ_FACE:
                fo = FACE.getface(o);
                j = fo.nv;
                while (j-- != 0) {
                    point2bbox(new FVECT(FACE.VERTEX(fo, j, 0), FACE.VERTEX(fo, j, 1), FACE.VERTEX(fo, j, 2)), bbmin, bbmax);
                }
                break;
            case OTYPES.OBJ_CONE:
            case OTYPES.OBJ_CUP:
            case OTYPES.OBJ_CYLINDER:
            case OTYPES.OBJ_TUBE:
            case OTYPES.OBJ_RING:
                co = CONE.getcone(o, 0);
                if (o.otype != OTYPES.OBJ_RING) {
                    circle2bbox(new FVECT(CONE.CO_P0(co, 0), CONE.CO_P0(co, 1), CONE.CO_P0(co, 2)),
                            co.ad, CONE.CO_R0(co, 0), bbmin, bbmax);
                }
                circle2bbox(new FVECT(CONE.CO_P1(co, 0), CONE.CO_P1(co, 1), CONE.CO_P1(co, 2)),
                        co.ad, CONE.CO_R1(co, 0), bbmin, bbmax);
                break;
            case OTYPES.OBJ_INSTANCE:
                io = INSTANCE.getinstance(o, OCTREE.IO_BOUNDS);
                for (j = 0; j < 8; j++) {
                    for (i = 0; i < 3; i++) {
                        v.data[i] = io.obj.scube.cuorg.data[i];
                        if ((j & 1 << i) != 0) {
                            v.data[i] += io.obj.scube.cusize;
                        }
                    }
                    MAT4.multp3(v, v, io.x.f.xfm);
                    point2bbox(v, bbmin, bbmax);
                }
                break;
            case OTYPES.OBJ_MESH:
                mi = MESH.getmeshinst(o, OCTREE.IO_BOUNDS);
                for (j = 0; j < 8; j++) {
                    for (i = 0; i < 3; i++) {
                        v.data[i] = mi.msh.mcube.cuorg.data[i];
                        if ((j & 1 << i) != 0) {
                            v.data[i] += mi.msh.mcube.cusize;
                        }
                    }
                    MAT4.multp3(v, v, mi.x.f.xfm);
                    point2bbox(v, bbmin, bbmax);
                }
                break;
        }
    }

    static void point2bbox( /* expand bounding box to fit point */
            FVECT p,
            FVECT bbmin,
            FVECT bbmax) {
        int i;

        for (i = 0; i < 3; i++) {
            if (p.data[i] < bbmin.data[i]) {
                bbmin.data[i] = p.data[i];
            }
            if (p.data[i] > bbmax.data[i]) {
                bbmax.data[i] = p.data[i];
            }
        }
    }

    static void circle2bbox( /* expand bbox to fit circle */
            FVECT cent,
            FVECT norm,
            double rad,
            FVECT bbmin,
            FVECT bbmax) {
        double d, r;
        int i;

        for (i = 0; i < 3; i++) {
            r = Math.sqrt(1. - norm.data[i] * norm.data[i]);
            d = cent.data[i] + r * rad;
            if (d > bbmax.data[i]) {
                bbmax.data[i] = d;
            }
            d = cent.data[i] - r * rad;
            if (d < bbmin.data[i]) {
                bbmin.data[i] = d;
            }
        }
    }
}
