/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.rt;

import jradiance.common.COLORS.COLOR;
import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OBJECT_STRUCTURE;
import jradiance.rt.FUNC.MFUNC;

/**
 *
 * @author arwillis
 */
public class P_FUNC {
    /*
     *  p_func.c - routine for procedural patterns.
     */
    /*
     *	A procedural pattern can either be a brightness or a
     *  color function.  A brightness function is given as:
     *
     *	modifier brightfunc name
     *	2+ bvarname filename xf
     *	0
     *	n A1 A2 ..
     *
     *  A color function is given as:
     *
     *	modifier colorfunc name
     *	4+ rvarname gvarname bvarname filename xf
     *	0
     *	n A1 A2 ..
     *
     *  Filename is the name of the file where the variable definitions
     *  can be found.  The list of real arguments can be accessed by
     *  definitions in the file.  The xf is a transformation
     *  to get from the original coordinates to the current coordinates.
     */

    public static class P_BFUNC extends OBJECT_STRUCTURE {

        @Override
        public int octree_function(Object... obj) {
            return p_bfunc((OBJREC) obj[0], (RAY) obj[1]);
        }

        int p_bfunc( /* compute brightness pattern */
                OBJREC m,
                RAY r) {
            double bval = 0;
            MFUNC mf;

            if (m.oargs.nsargs < 2) {
//		objerror(m, USER, "bad # arguments");
            }
            mf = FUNC.getfunc(m, 1, 0x1, 0);
            FUNC.setfunc(m, r);
//	errno = 0;
//	bval = evalue(mf->ep[0]);
//	if (errno == EDOM || errno == ERANGE) {
//		objerror(m, WARNING, "compute error");
//		return(0);
//	}
            r.pcol.scalecolor(bval);
            return (0);
        }
    }

    public static class P_CFUNC extends OBJECT_STRUCTURE {

        @Override
        public int octree_function(Object... obj) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        int p_cfunc( /* compute color pattern */
                OBJREC m,
                RAY r) {
            COLOR cval = new COLOR();
            MFUNC mf;

            if (m.oargs.nsargs < 4) {
//		objerror(m, USER, "bad # arguments");
            }
            mf = FUNC.getfunc(m, 3, 0x7, 0);
            FUNC.setfunc(m, r);
//	errno = 0;
//	cval.setcolor( evalue(mf.ep[0]),
//			evalue(mf.ep[1]),
//			evalue(mf.ep[2]));
//	if (errno == EDOM || errno == ERANGE) {
////		objerror(m, WARNING, "compute error");
//		return(0);
//	}
            COLOR.multcolor(r.pcol, cval);
            return (0);
        }
    }
}
