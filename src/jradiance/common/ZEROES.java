/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

/**
 *
 * @author arwillis
 */
public class ZEROES {
//#ifndef lint
//static const char	RCSid[] = "$Id$";
//#endif
/*
     *  zeroes.c - compute roots for various equations.
     *
     *  External symbols declared in standard.h
     */

    public static int quadratic( /* find real roots of quadratic equation */
            double[] r, /* roots in ascending order */
            double a, double b, double c) {
        double disc;
        int first;

        if (a < -FVECT.FTINY) {
            first = 1;
        } else if (a > FVECT.FTINY) {
            first = 0;
        } else if (Math.abs(b) > FVECT.FTINY) {	/* solve linearly */
            r[0] = -c / b;
            return (1);
        } else {
            return (0);		/* equation is c == 0 ! */
        }

        b *= 0.5;			/* simplifies formula */

        disc = b * b - a * c;		/* discriminant */

        if (disc < -FVECT.FTINY * FVECT.FTINY) /* no real roots */ {
            return (0);
        }

        if (disc <= FVECT.FTINY * FVECT.FTINY) {	/* double root */
            r[0] = -b / a;
            return (1);
        }

        disc = Math.sqrt(disc);

        r[first] = (-b - disc) / a;
        r[1 - first] = (-b + disc) / a;

        return (2);
    }
}
