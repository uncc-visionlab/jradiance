/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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