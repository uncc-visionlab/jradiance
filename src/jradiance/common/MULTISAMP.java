/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

import jradiance.rt.RAYCALLS;

/**
 *
 * @author arwillis
 */
public class MULTISAMP {
//#ifndef lint
//static const char	RCSid[] = "$Id$";
//#endif
/*
     * Binary space partitioning curve for multidimensional sampling.
     *
     *	Written by Christophe Schlick
     */
//
//#include "copyright.h"
//
//#include <stdlib.h>
//
//#include "random.h"
//
    public static void multisamp( /* convert 1-dimensional sample to N dimensions */
            double t[], /* returned N-dimensional vector */
            int n, /* number of dimensions */
            double r) /* 1-dimensional sample [0,1) */ {
        int j;
        int i, k;
        int[] ti = new int[8];
        double s;

        i = n;
        while (i-- > 0) {
            ti[i] = 0;
        }
        j = 8;
        while (j-- != 0) {
            s = r * (1 << n);
            k = (int) s;
            r = s - k;
            i = n;
            while (i-- > 0) {
                ti[i] += ti[i] + ((k >> i) & 1);
            }
        }
        i = n;
        while (i-- > 0) {
            t[i] = 1. / 256. * (ti[i] + RAYCALLS.frandom());
        }
    }
}
