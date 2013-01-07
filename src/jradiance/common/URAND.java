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
public class URAND {
//#ifndef lint
//static const char	RCSid[] = "$Id$";
//#endif
/*
     * Anticorrelated random function due to Christophe Schlick
     */

//#include "copyright.h"
//
//#include  <stdlib.h>
//
//#include  "standard.h"
//#include  "random.h"
//
//#undef initurand
    public static final int sizeofSHORT = Short.SIZE / 8;
    public static final int MAXORDER = (8 * sizeofSHORT);
    static short empty_tab = 0;
//short  *urperm = &empty_tab;	/* urand() permutation */
    static short[] urperm = {0};
    static int urmask = 0;			/* bits used in permutation */


    public static double urand(int i) {
        return (urmask != 0 ? (urperm[(i) & urmask] + RAYCALLS.frandom()) / (urmask + 1)
                : RAYCALLS.frandom());
    }

    public static int initurand( /* initialize urand() for size entries */
            int size) {
        int order, n;
        int i, offset;

        if ((urperm != null) && (urperm[0] != empty_tab)) {
//		free((void *)urperm);
            urperm = null;
        }
        if (--size <= 0) {
            empty_tab = 0;
            urperm = new short[]{0};
            urmask = 0;
            return (0);
        }
        for (i = 1; (size >>= 1) != 0; i++) {
            if (i == MAXORDER) {
                break;
            }
        }
        order = i;
        urmask = (1 << order) - 1;

//	urperm = (unsigned short *)malloc((urmask+1)*sizeof(unsigned short));
        urperm = new short[((urmask + 1) * sizeofSHORT)];
        if (urperm == null) {
//		eputs("out of memory in initurand\n");
//		quit(1);
            System.exit(1);
        }
        urperm[0] = 0;
        for (n = 1, offset = 1; n <= order; n++, offset <<= 1) {
            for (i = offset; i-- != 0;) {
                urperm[i + offset] = urperm[i] <<= 1;
                if ((RAYCALLS.randsrc.nextInt() & 0x4000) != 0) {
                    urperm[i]++;
                } else {
                    urperm[i + offset]++;
                }
            }
        }
        return (urmask + 1);
    }
    static int[] tab = {103699, 96289, 73771, 65203, 81119, 87037, 92051, 98899};

    public static int ilhash( /* hash a set of integer values */
            int[] d,
            int n) {
        int hval, didx = 0;

        hval = 0;
        while (n-- > 0) {
            hval ^= d[didx++] * tab[n & 7];
        }
        return (hval & 0x7fffffff);
    }
}
