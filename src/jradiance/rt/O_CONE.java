/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.rt;

import jradiance.common.CONE;
import jradiance.common.FVECT;
import jradiance.common.MAT4;
import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OBJECT_STRUCTURE;
import jradiance.common.OTYPES;
import jradiance.common.ZEROES;

/**
 *
 * @author arwillis
 */
public class O_CONE extends OBJECT_STRUCTURE {
    /*
     *  o_cone.c - routine to determine ray intersection with cones.
     */

    @Override
    public int octree_function(Object... obj) {
        return o_cone((OBJREC) obj[0], (RAY) obj[1]);
    }

    int o_cone( /* intersect ray with cone */
            OBJREC o,
            RAY r) {
        FVECT rox = new FVECT(), rdx = new FVECT();
        double a, b, c;
        double[] root = new double[2];
        int nroots, rn;
        CONE co;
        int i;

        /* get cone structure */
        co = CONE.getcone(o, 1);

        /*
         *     To intersect a ray with a cone, we transform the
         *  ray into the cone's normalized space.  This greatly
         *  simplifies the computation.
         *     For a cone or cup, normalization results in the
         *  equation:
         *
         *		x*x + y*y - z*z == 0
         *
         *     For a cylinder or tube, the normalized equation is:
         *
         *		x*x + y*y - r*r == 0
         *
         *     A normalized ring obeys the following set of equations:
         *
         *		z == 0			&&
         *		x*x + y*y >= r0*r0	&&
         *		x*x + y*y <= r1*r1
         */

        /* transform ray */
        MAT4.multp3(rox, r.rorg, co.tm);
        MAT4.multv3(rdx, r.rdir, co.tm);
        /* compute intersection */

        if (o.otype == OTYPES.OBJ_CONE || o.otype == OTYPES.OBJ_CUP) {

            a = rdx.data[0] * rdx.data[0] + rdx.data[1] * rdx.data[1] - rdx.data[2] * rdx.data[2];
            b = 2.0 * (rdx.data[0] * rox.data[0] + rdx.data[1] * rox.data[1] - rdx.data[2] * rox.data[2]);
            c = rox.data[0] * rox.data[0] + rox.data[1] * rox.data[1] - rox.data[2] * rox.data[2];

        } else if (o.otype == OTYPES.OBJ_CYLINDER || o.otype == OTYPES.OBJ_TUBE) {

            a = rdx.data[0] * rdx.data[0] + rdx.data[1] * rdx.data[1];
            b = 2.0 * (rdx.data[0] * rox.data[0] + rdx.data[1] * rox.data[1]);
            c = rox.data[0] * rox.data[0] + rox.data[1] * rox.data[1] - CONE.CO_R0(co, 0) * CONE.CO_R0(co, 0);

        } else { /* OBJ_RING */

            if (rdx.data[2] <= FVECT.FTINY && rdx.data[2] >= -FVECT.FTINY) {
                return (0);			/* parallel */
            }
            root[0] = -rox.data[2] / rdx.data[2];
            if (root[0] <= FVECT.FTINY || root[0] >= r.rot) {
                return (0);			/* distance check */
            }
            b = root[0] * rdx.data[0] + rox.data[0];
            c = root[0] * rdx.data[1] + rox.data[1];
            a = b * b + c * c;
            if (a < CONE.CO_R0(co, 0) * CONE.CO_R0(co, 0) || a > CONE.CO_R1(co, 0) * CONE.CO_R1(co, 0)) {
                return (0);			/* outside radii */
            }
            r.ro = o;
            r.rot = root[0];
            FVECT.VSUM(r.rop, r.rorg, r.rdir, r.rot);
            FVECT.VCOPY(r.ron, co.ad);
            r.rod = -rdx.data[2];
            r.rox = null;
            return (1);				/* good */
        }
        /* roots for cone, cup, cyl., tube */
        nroots = ZEROES.quadratic(root, a, b, c);

        for (rn = 0; rn < nroots; rn++) {	/* check real roots */
            if (root[rn] <= FVECT.FTINY) {
                continue;		/* too small */
            }
            if (root[rn] >= r.rot) {
                break;			/* too big */
            }
            /* check endpoints */
            FVECT.VSUM(rox, r.rorg, r.rdir, root[rn]);
            FVECT.VSUB(rdx, rox, new FVECT(CONE.CO_P0(co, 0), CONE.CO_P0(co, 1), CONE.CO_P0(co, 2)));
            b = FVECT.DOT(rdx, co.ad);
            if (b < 0.0) {
                continue;		/* before p0 */
            }
            if (b > co.al) {
                continue;		/* after p1 */
            }
            r.ro = o;
            r.rot = root[rn];
            FVECT.VCOPY(r.rop, rox);
            /* get normal */
            if (o.otype == OTYPES.OBJ_CYLINDER) {
                a = CONE.CO_R0(co, 0);
            } else if (o.otype == OTYPES.OBJ_TUBE) {
                a = -CONE.CO_R0(co, 0);
            } else { /* OBJ_CONE || OBJ_CUP */
                c = CONE.CO_R1(co, 0) - CONE.CO_R0(co, 1);
                a = CONE.CO_R0(co, 1) + b * c / co.al;
                if (o.otype == OTYPES.OBJ_CUP) {
                    c = -c;
                    a = -a;
                }
            }
            for (i = 0; i < 3; i++) {
                r.ron.data[i] = (rdx.data[i] - b * co.ad.data[i]) / a;
            }
            if (o.otype == OTYPES.OBJ_CONE || o.otype == OTYPES.OBJ_CUP) {
                for (i = 0; i < 3; i++) {
                    r.ron.data[i] = (co.al * r.ron.data[i] - c * co.ad.data[i])
                            / co.sl;
                }
            }
            a = FVECT.DOT(r.ron, r.ron);
            if (a > 1. + FVECT.FTINY || a < 1. - FVECT.FTINY) {
                c = 1. / (.5 + .5 * a);     /* avoid numerical error */
                r.ron.data[0] *= c;
                r.ron.data[1] *= c;
                r.ron.data[2] *= c;
            }
            r.rod = -FVECT.DOT(r.rdir, r.ron);
            r.pert.data[0] = r.pert.data[1] = r.pert.data[2] = 0.0;
            r.uv[0] = r.uv[1] = 0.0;
            r.rox = null;
            return (1);			/* good */
        }
        return (0);
    }
}
