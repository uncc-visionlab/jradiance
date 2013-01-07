/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.rt;

import jradiance.common.MODOBJECT;
import jradiance.common.OBJECT;
import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OBJECT_STRUCTURE;
import jradiance.common.OTYPES;

/**
 *
 * @author arwillis
 */
public class M_ALIAS extends OBJECT_STRUCTURE {
    /*
     * Handler for modifier alias
     */


    /*
     *	If the alias has a single string argument, it's the
     *  name of the target modifier, and we must substitute the
     *  target and its arguments in place of this alias.  The
     *  only difference is that we use our modifier rather than
     *  theirs.
     *	If the alias has no string arguments, then we simply
     *  pass through to our modifier as if we weren't in the
     *  chain at all.
     */
    
    @Override
    public int octree_function(Object... obj) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public static int m_alias( /* transfer shading to alias target */
            OBJREC m,
            RAY r) {
        int aobj;
        OBJREC aop;
        OBJREC arec;
        int rval;
        /* straight replacement? */
        if (m.oargs.nsargs == 0) {
            return (RAYTRACE.rayshade(r, m.omod));
        }
        /* else replace alias */
        if (m.oargs.nsargs != 1) {
//		objerror(m, INTERNAL, "bad # string arguments");
        }
        aop = m;
        aobj = MODOBJECT.objndx(aop);
        do {				/* follow alias trail */
            if (aop.oargs.nsargs == 1) {
                aobj = MODOBJECT.lastmod(aobj, aop.oargs.sarg[0]);
            } else {
                aobj = aop.omod;
            }
            if (aobj < 0) {
//			objerror(aop, USER, "bad reference");
            }
            aop = OBJECT.objptr(aobj);
        } while (aop.otype == OTYPES.MOD_ALIAS);
        /* copy struct */
        arec = aop;
        /* irradiance hack */
        if (RAYCALLS.do_irrad != 0 && (r.crtype & ~(RAY.PRIMARY | RAY.TRANS)) == 0
                && m.otype != OTYPES.MAT_CLIP
                && (OTYPES.ofun[arec.otype].flags & (OTYPES.T_M | OTYPES.T_X)) != 0) {
            if (OTSPECIAL.irr_ignore(arec.otype) != 0) {
                RAYTRACE.raytrans(r);
                return (1);
            }
            if (OTYPES.islight(arec.otype) == 0) {
                return ((OTYPES.ofun[RAYTRACE.Lamb.otype].ofunc.octree_function(RAYTRACE.Lamb, r)));
            }
        }
        /* substitute modifier */
        arec.omod = m.omod;
        /* replacement shader */
//	rval = (*ofun[arec.otype].funp)(&arec, r);
        rval = (OTYPES.ofun[arec.otype].ofunc.octree_function(arec, r));
        /* save allocated struct */
        if (arec.os != aop.os) {
            if (aop.os != null) {	/* should never happen */
//			free_os(aop);
            }
            aop.os = arec.os;
        }
        return (rval);
    }

}
