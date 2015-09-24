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
public class TCOS {
    /*
     * Table-based cosine approximation.
     *
     * Use doubles in table even though we're not nearly that accurate just
     * to avoid conversion and guarantee that tsin(x)^2 + tcos(x)^2 == 1.
     *
     * No interpolation in this version.
     *
     * External symbols declared in rtmath.h
     */

    public static final int NCOSENTRY = 256;
    static double[] costab = new double[NCOSENTRY + 1];

    public static double tcos(double x) /* approximate cosine */ {
//	static double	costab[NCOSENTRY+1];
        int i;

        if (costab[0] < 0.5) /* initialize table */ {
            for (i = 0; i <= NCOSENTRY; i++) {
                costab[i] = Math.cos((Math.PI / 2. / NCOSENTRY) * i);
            }
        }
        /* normalize angle */
        if (x < 0.) {
            x = -x;
        }
        i = (int) ((NCOSENTRY * 2. / Math.PI) * x + 0.5);
        while (i >= 4 * NCOSENTRY) {
            i -= 4 * NCOSENTRY;
        }
        switch (i / NCOSENTRY) {
            case 0:
                return (costab[i]);
            case 1:
                return (-costab[(2 * NCOSENTRY) - i]);
            case 2:
                return (-costab[i - (2 * NCOSENTRY)]);
            case 3:
                return (costab[(4 * NCOSENTRY) - i]);
        }
        return (0.);		/* should never be reached */
    }
}
