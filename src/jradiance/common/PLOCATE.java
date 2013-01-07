/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

/**
 *
 * @author arwillis
 */
public class PLOCATE {
    /*
     *  plocate.h - header for 3D vector location.
     *
     *  Include after fvect.h
     */

    public static double EPSILON = FVECT.FTINY;		/* acceptable location error */

    public static int XPOS = 03;		/* x position mask */

    public static int YPOS = 014;		/* y position mask */

    public static int ZPOS = 060;		/* z position mask */


    public static int position(int i) {
        return (3 << ((i) << 1));
    }	/* macro version */

    public static int BELOW = 025;		/* below bits */

    public static int ABOVE = 052;		/* above bits */

    /*
     *  plocate.c - routine to locate 3D vector w.r.t. box.
     */

    public static int plocate( /* return location of p w.r.t. min & max */
            FVECT p,
            FVECT min, FVECT max) {
        int loc = 0;

        if (p.data[0] < min.data[0] - EPSILON) {
            loc |= XPOS & BELOW;
        } else if (p.data[0] > max.data[0] + EPSILON) {
            loc |= XPOS & ABOVE;
        }
        if (p.data[1] < min.data[1] - EPSILON) {
            loc |= YPOS & BELOW;
        } else if (p.data[1] > max.data[1] + EPSILON) {
            loc |= YPOS & ABOVE;
        }
        if (p.data[2] < min.data[2] - EPSILON) {
            loc |= ZPOS & BELOW;
        } else if (p.data[2] > max.data[2] + EPSILON) {
            loc |= ZPOS & ABOVE;
        }

        return (loc);
    }
}
