/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

/**
 *
 * @author arwillis
 */
public class CLIP {
//#ifndef lint
//static const char	RCSid[] = "$Id$";
//#endif
/*
     *  clip.c - routine to clip 3D line segments to a box.
     */
//
//#include "copyright.h"
//
//#include  "fvect.h"
//
//#include  "plocate.h"

    public static int MAXITER = 6;	/* maximum possible number of iterations */


    public static int clip(FVECT ep1, FVECT ep2, FVECT min, FVECT max) /* clip a line segment to a box */ {
        int itlim = MAXITER;
        int loc1, loc2;
        boolean accept;
        FVECT dp;
        double d;
        int i, j;

        /*
         *	The Cohen-Sutherland algorithm is used to determine
         *  what part (if any) of the given line segment is contained
         *  in the box specified by the min and max vectors.
         *	The routine returns non-zero if any segment is left.
         */

        loc1 = PLOCATE.plocate(ep1, min, max);
        loc2 = PLOCATE.plocate(ep2, min, max);

        /* check for trivial accept and reject */
        /* trivial accept is both points inside */
        /* trivial reject is both points to one side */

        while (((accept = ((loc1 | loc2) == 0)) || ((loc1 & loc2) != 0)) == false) {

            if (itlim-- <= 0) /* past theoretical limit? */ {
                return (0);	/* quit fooling around */
            }

            if (loc1 == 0) {		/* make sure first point is outside */
                dp = ep1;
                ep1 = ep2;
                ep2 = dp;
                i = loc1;
                loc1 = loc2;
                loc2 = i;
            }

            for (i = 0; i < 3; i++) {		/* chop segment */

                if ((loc1 & PLOCATE.position(i) & PLOCATE.BELOW) != 0) {
                    d = (min.data[i] - ep1.data[i]) / (ep2.data[i] - ep1.data[i]);
                    ep1.data[i] = min.data[i];
                } else if ((loc1 & PLOCATE.position(i) & PLOCATE.ABOVE) != 0) {
                    d = (max.data[i] - ep1.data[i]) / (ep2.data[i] - ep1.data[i]);
                    ep1.data[i] = max.data[i];
                } else {
                    continue;
                }

                for (j = 0; j < 3; j++) {
                    if (j != i) {
                        ep1.data[j] += (ep2.data[j] - ep1.data[j]) * d;
                    }
                }
                break;
            }
            loc1 = PLOCATE.plocate(ep1, min, max);
        }
        return (accept ? 1 : 0);
    }
}
