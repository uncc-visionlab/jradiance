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
public class MAT4 {
    /*
     * Definitions for 4x4 matrix operations
     */

    public double[][] data = new double[4][4];
    public static final MAT4 m4ident = getIdent();

    public static void setident4(MAT4 m4) {
        copymat4(m4, m4ident);
    }

    public static void copymat4(MAT4 dest, MAT4 src) {
        for (int i = 0; i < 4; i++) {
            System.arraycopy(src.data[i], 0, dest.data[i], 0, 4);
        }
    }

    final void setIdent() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == j) {
                    data[i][j] = 1;
                } else {
                    data[i][j] = 0;
                }
            }
        }        
    }
    public static MAT4 getIdent() {
        MAT4 m4i = new MAT4();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == j) {
                    m4i.data[i][j] = 1;
                } else {
                    m4i.data[i][j] = 0;
                }
            }
        }
        return m4i;
    }

    MAT4() {
        setIdent();
    }

    MAT4(double[][] data) {
        for (int i = 0; i < 4; i++) {
            System.arraycopy(data[i], 0, this.data[i], 0, 4);
        }
    }
    //extern int	invmat4(MAT4 inverse, MAT4 mat);
    /*
     *  mat4.c - routines dealing with 4 X 4 homogeneous transformation matrices.
     */
    static MAT4 m4tmp = new MAT4();		/* for efficiency */


    public static void multmat4( /* multiply m4b X m4c and put into m4a */
            MAT4 m4a,
            MAT4 m4b, MAT4 m4c) {
        int i, j;

        for (i = 4; i-- != 0;) {
            for (j = 4; j-- != 0;) {
                m4tmp.data[i][j] = m4b.data[i][0] * m4c.data[0][j]
                        + m4b.data[i][1] * m4c.data[1][j]
                        + m4b.data[i][2] * m4c.data[2][j]
                        + m4b.data[i][3] * m4c.data[3][j];
            }
        }

        MAT4.copymat4(m4a, m4tmp);
    }

    public static void multv3( /* transform vector v3b by m4 and put into v3a */
            FVECT v3a,
            FVECT v3b,
            MAT4 m4) {
        m4tmp.data[0][0] = v3b.data[0] * m4.data[0][0] + v3b.data[1] * m4.data[1][0] + v3b.data[2] * m4.data[2][0];
        m4tmp.data[0][1] = v3b.data[0] * m4.data[0][1] + v3b.data[1] * m4.data[1][1] + v3b.data[2] * m4.data[2][1];
        m4tmp.data[0][2] = v3b.data[0] * m4.data[0][2] + v3b.data[1] * m4.data[1][2] + v3b.data[2] * m4.data[2][2];

        v3a.data[0] = m4tmp.data[0][0];
        v3a.data[1] = m4tmp.data[0][1];
        v3a.data[2] = m4tmp.data[0][2];
    }

    public static void multp3( /* transform p3b by m4 and put into p3a */
            FVECT p3a,
            FVECT p3b,
            MAT4 m4) {
        multv3(p3a, p3b, m4);	/* transform as vector */
        p3a.data[0] += m4.data[3][0];	/* translate */
        p3a.data[1] += m4.data[3][1];
        p3a.data[2] += m4.data[3][2];
    }
}
