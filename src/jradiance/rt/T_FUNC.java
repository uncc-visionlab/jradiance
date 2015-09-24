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

import jradiance.common.OBJECT_STRUCTURE;

/**
 *
 * @author arwillis
 */
public class T_FUNC extends OBJECT_STRUCTURE {
//#ifndef lint
//static const char	RCSid[] = "$Id$";
//#endif
///*
// *  t_func.c - routine for procedural textures.
// */
//
//#include "copyright.h"
//
//#include  "ray.h"
//#include  "func.h"
//#include  "rtotypes.h"
//
///*
// *	A procedural texture perturbs the surface normal
// *  at the point of intersection with an object.  It has
// *  the form:
// *
// *	modifier texfunc name
// *	4+ xvarname yvarname zvarname filename xf
// *	0
// *	n A1 A2 ..
// *
// *  Filename is the name of the file where the variable definitions
// *  can be found.  The list of real arguments can be accessed by
// *  definitions in the file.  The xf is a transformation to get
// *  from the original coordinates to the current coordinates.
// */
//
    @Override
    public int octree_function(Object... obj) {
        throw new UnsupportedOperationException("Not supported yet.");
    }    

    //
//extern int
//t_func(			/* compute texture for ray */
//	register OBJREC  *m,
//	register RAY  *r
//)
//{
//	FVECT  disp;
//	double  d;
//	register MFUNC  *mf;
//	register int  i;
//
//	if (m->oargs.nsargs < 4)
//		objerror(m, USER, "bad # arguments");
//	mf = getfunc(m, 3, 0x7, 1);
//	setfunc(m, r);
//	errno = 0;
//	for (i = 0; i < 3; i++) {
//		disp[i] = evalue(mf->ep[i]);
//		if (errno == EDOM || errno == ERANGE) {
//			objerror(m, WARNING, "compute error");
//			return(0);
//		}
//	}
//	if (mf->f != &unitxf)
//		multv3(disp, disp, mf->f->xfm);
//	if (r->rox != NULL) {
//		multv3(disp, disp, r->rox->f.xfm);
//		d = 1.0 / (mf->f->sca * r->rox->f.sca);
//	} else
//		d = 1.0 / mf->f->sca;
//	VSUM(r->pert, r->pert, disp, d);
//	return(0);
//}
    
}
