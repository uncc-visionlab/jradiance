/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

/**
 *
 * @author arwillis
 */
public class DIRCODE {
    /*
     * Compute a 4-byte direction code (externals defined in rtmath.h).
     *
     * Mean accuracy is 0.0022 degrees, with a maximum error of 0.0058 degrees.
     */

    public static final double DCSCALE = 11585.2;		/* (1<<13)*sqrt(2) */

    public static final int FXNEG = 01;
    public static final int FYNEG = 02;
    public static final int FZNEG = 04;
    public static final int F1X = 010;
    public static final int F2Z = 020;
    public static final int F1SFT = 5;
    public static final int F2SFT = 18;
    public static final int FMASK = 0x1fff;

    static int encodedir( /* encode a normalized direction vector */
            FVECT dv) {
        int dc = 0;
        int[] cd = new int[3];
        int cm;
        int i;

        for (i = 0; i < 3; i++) {
            if (dv.data[i] < 0.) {
                cd[i] = (int) (dv.data[i] * -DCSCALE);
                dc |= FXNEG << i;
            } else {
                cd[i] = (int) (dv.data[i] * DCSCALE);
            }
        }
        if ((cd[0] | cd[1] | cd[2]) == 0) {
            return (0);		/* zero normal */
        }
        if (cd[0] <= cd[1]) {
            dc |= F1X | cd[0] << F1SFT;
            cm = cd[1];
        } else {
            dc |= cd[1] << F1SFT;
            cm = cd[0];
        }
        if (cd[2] <= cm) {
            dc |= F2Z | cd[2] << F2SFT;
        } else {
            dc |= cm << F2SFT;
        }
        if (dc == 0) /* don't generate 0 code normally */ {
            dc = F1X;
        }
        return (dc);
    }

    static void decodedir( /* decode a normalized direction vector */
            FVECT dv, /* returned */
            int dc) {
        double d1, d2, der;

        if (dc == 0) {		/* special code for zero normal */
            dv.data[0] = dv.data[1] = dv.data[2] = 0.;
            return;
        }
        d1 = ((dc >> F1SFT & FMASK) + .5) * (1. / DCSCALE);
        d2 = ((dc >> F2SFT & FMASK) + .5) * (1. / DCSCALE);
        der = Math.sqrt(1. - d1 * d1 - d2 * d2);
        if ((dc & F1X) != 0) {
            dv.data[0] = d1;
            if ((dc & F2Z) != 0) {
                dv.data[1] = der;
                dv.data[2] = d2;
            } else {
                dv.data[1] = d2;
                dv.data[2] = der;
            }
        } else {
            dv.data[1] = d1;
            if ((dc & F2Z) != 0) {
                dv.data[0] = der;
                dv.data[2] = d2;
            } else {
                dv.data[0] = d2;
                dv.data[2] = der;
            }
        }
        if ((dc & FXNEG) != 0) {
            dv.data[0] = -dv.data[0];
        }
        if ((dc & FYNEG) != 0) {
            dv.data[1] = -dv.data[1];
        }
        if ((dc & FZNEG) != 0) {
            dv.data[2] = -dv.data[2];
        }
    }

    static double dir2diff( /* approx. radians^2 between directions */
            int dc1, int dc2) {
        FVECT v1 = new FVECT(), v2 = new FVECT();
        decodedir(v1, dc1);
        decodedir(v2, dc2);
        return (2. - 2. * FVECT.DOT(v1, v2));
    }

    static double fdir2diff( /* approx. radians^2 between directions */
            int dc1,
            FVECT v2) {
        FVECT v1 = new FVECT();
        decodedir(v1, dc1);
        return (2. - 2. * FVECT.DOT(v1, v2));
    }
}
