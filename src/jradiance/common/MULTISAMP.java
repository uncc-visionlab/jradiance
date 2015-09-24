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
