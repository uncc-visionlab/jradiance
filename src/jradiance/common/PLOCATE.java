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
