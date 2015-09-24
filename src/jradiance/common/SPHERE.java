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
package jradiance.common;

/**
 *
 * @author arwillis
 */
public class SPHERE extends OBJECT_STRUCTURE {

//#ifndef lint
//static const char	RCSid[] = "$Id$";
//#endif
/*
     *  sphere.c - routines for creating octrees for spheres.
     *
     *     7/28/85
     */
//#include  "standard.h"
//
//#include  "octree.h"
//
//#include  "object.h"
//
//#include  "otypes.h"
    static double ROOT3 = 1.732050808;

    /*
     *	Regrettably, the algorithm for determining a cube's location
     *  with respect to a sphere is not simple.  First, a quick test is 
     *  made to determine if the sphere and the bounding sphere of the cube 
     *  are disjoint.  This of course means no intersection.  Failing this,
     *  we determine if the cube lies inside the sphere.  The cube is
     *  entirely inside if the bounding sphere on the cube is
     *  contained within our sphere.  This means no intersection.  Otherwise,
     *  if the cube radius is smaller than the sphere's and the cube center is
     *  inside the sphere, we assume intersection.  If these tests fail,
     *  we proceed as follows.
     *	The sphere center is located in relation to the 6 cube faces,
     *  and one of four things is done depending on the number of
     *  planes the center lies between:
     *
     *	0:  The sphere is closest to a cube corner, find the
     *		distance to that corner.
     *
     *	1:  The sphere is closest to a cube edge, find this
     *		distance.
     *
     *	2:  The sphere is closest to a cube face, find the distance.
     *
     *	3:  The sphere has its center inside the cube.
     *
     *	In cases 0-2, if the closest part of the cube is within
     *  the radius distance from the sphere center, we have intersection.
     *  If it is not, the cube must be outside the sphere.
     *	In case 3, there must be intersection, and no further
     *  tests are necessary.
     */
    public int octree_function(Object ... obj)
//    { return OCTREE.O_MISS; } 
//    int o_sphere( /* determine if sphere intersects cube */
//            OBJECT.OBJREC o,
//            OCTREE.CUBE cu)
    {
        OBJECT.OBJREC o = (OBJECT.OBJREC) obj[0];
        OCTREE.CUBE cu = (OCTREE.CUBE) obj[1];
        FVECT v1 = new FVECT();
        double d1, d2;
        double[] fa;
        int i;
//#define  cent		fa
//#define  rad		fa[3]
					/* get arguments */
        if (o.oargs.nfargs != 4) {
            //objerror(o, USER, "bad # arguments");
        }
        fa = o.oargs.farg;
        if (fa[3] < -FVECT.FTINY) {
            //objerror(o, WARNING, "negative radius");
            o.otype = (short) ((o.otype == OTYPES.OBJ_SPHERE)
                    ? OTYPES.OBJ_BUBBLE : OTYPES.OBJ_SPHERE);
            fa[3] = -fa[3];
        } else if (fa[3] <= FVECT.FTINY) {
            //objerror(o, USER, "zero radius");
        }
        d1 = ROOT3 / 2.0 * cu.cusize;	/* bounding radius for cube */

        d2 = cu.cusize * 0.5;		/* get distance between centers */
        for (i = 0; i < 3; i++) {
            v1.data[i] = cu.cuorg.data[i] + d2 - fa[i];
        }
        d2 = FVECT.DOT(v1, v1);

        if (d2 > (fa[3] + d1 + FVECT.FTINY) * (fa[3] + d1 + FVECT.FTINY)) /* quick test */ {
            return (OCTREE.O_MISS);			/* cube outside */
        }

        /* check sphere interior */
        if (d1 < fa[3]) {
            if (d2 < (fa[3] - d1 - FVECT.FTINY) * (fa[3] - d1 - FVECT.FTINY)) {
                return (OCTREE.O_MISS);		/* cube inside sphere */
            }
            if (d2 < (fa[3] + FVECT.FTINY) * (fa[3] + FVECT.FTINY)) {
                return (OCTREE.O_HIT);		/* cube center inside */
            }
        }
        /* find closest distance */
        for (i = 0; i < 3; i++) {
            if (fa[i] < cu.cuorg.data[i]) {
                v1.data[i] = cu.cuorg.data[i] - fa[i];
            } else if (fa[i] > cu.cuorg.data[i] + cu.cusize) {
                v1.data[i] = fa[i] - (cu.cuorg.data[i] + cu.cusize);
            } else {
                v1.data[i] = 0;
            }
        }
        /* final intersection check */
        if (FVECT.DOT(v1, v1) <= (fa[3] + FVECT.FTINY) * (fa[3] + FVECT.FTINY)) {
            return (OCTREE.O_HIT);
        } else {
            return (OCTREE.O_MISS);
        }
    }
    
}
