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
public class RTMATH {
    /*
     * Header for Radiance vector and math routines
     */

//    /* regular transformation */
//    public static class XF {
//
//        public MAT4 xfm = new MAT4();				/* transform matrix */
//
//        public double sca = 1.0;				/* scalefactor */
//        
//    }
    /* complemetary tranformation */

    public static class FULLXF {

        public XF f = new XF();					/* forward */

        public XF b = new XF();					/* backward */
        public FULLXF() {
            
        }
    }
    public static double PI = 3.14159265358979323846;

//#ifdef  FASTMATH
//#define  tcos			cos
//#define  tsin			sin
//#define  ttan			tan
//#else
//					/* table-based cosine approximation */
    public static double tsin(double x) {
        return TCOS.tcos((x) - (Math.PI / 2.));
    }

    public static double ttan(double x) {
        return (tsin(x) / TCOS.tcos(x));
    }
//extern double	tcos(double x);
//					/* defined in xf.c */
//extern int	xf(XF *ret, int ac, char *av[]);
//extern int	invxf(XF *ret, int ac, char *av[]);
//extern int	fullxf(FULLXF *fx, int ac, char *av[]);
//					/* defined in zeroes.c */
//extern int	quadratic(double *r, double a, double b, double c);
//					/* defined in dircode.c */
//extern int32	encodedir(FVECT dv);
//extern void	decodedir(FVECT dv, int32 dc);
//extern double	dir2diff(int32 dc1, int32 dc2);
//extern double	fdir2diff(int32 dc1, FVECT v2);
//
}
