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

import jradiance.common.COLORS.COLOR;
import jradiance.common.COLORS.COLR;

/**
 *
 * @author arwillis
 */
public class COLOROPS {
    /*
     * Integer operations on COLR scanlines
     */

    public static int MAXGSHIFT = 31;		/* maximum shift for gamma table */

    static byte[] g_mant = null, g_nexp = null;
    static byte[][] g_bval = new byte[256][];

    public interface GAMMAFUNC {

        double f(double a, double b);
    }

    public static class POW_GAMMAFUNC implements GAMMAFUNC {

        @Override
        public double f(double a, double b) {
            return Math.pow(a, b);
        }
    }

    static int setcolrcor( /* set brightness correction */
            GAMMAFUNC f,
            double a2) {
        double mult;
        int i, j;
        /* allocate tables */
        if (g_bval[0] == null && (g_bval =
                new byte[MAXGSHIFT + 1][256]) == null) {
            return (-1);
        }
        /* compute colr -> gamb mapping */
        mult = 1.0 / 256.0;
        for (i = 0; i <= MAXGSHIFT; i++) {
            for (j = 0; j < 256; j++) {
                g_bval[i][j] = (byte) (256.0 * f.f((j + .5) * mult, a2));
            }
            mult *= 0.5;
        }
        return (0);
    }

    static int setcolrinv( /* set inverse brightness correction */
            GAMMAFUNC f,
            double a2) {
        double mult;
        int i, j, g_mantVal;
        /* allocate tables */
        if (g_mant == null && (g_mant = new byte[256]) == null) {
            return (-1);
        }
        if (g_nexp == null && (g_nexp = new byte[256]) == null) {
            return (-1);
        }
        /* compute gamb -> colr mapping */
        i = 0;
        mult = 256.0;
        for (j = 256; j-- != 0;) {
            while ((g_mantVal = (int) (mult * f.f((j + .5) / 256.0, a2))) < 128) {
                i++;
                mult *= 2.0;
            }
            g_mant[j] = (byte) g_mantVal;
            g_nexp[j] = (byte) i;
        }
        return (0);
    }

    public static int setcolrgam( /* set gamma conversion */
            double g) {
        POW_GAMMAFUNC pow = new POW_GAMMAFUNC();
        if (setcolrcor(pow, 1.0 / g) < 0) {
            return (-1);
        }
        return (setcolrinv(pow, g));
    }

    public static int colrs_gambs( /* convert scanline of colrs to gamma bytes */
            COLR[] scan,
            int len) {
        int i, expo;
        int scanidx = 0;
        if (g_bval == null) {
            return (-1);
        }
        while (len-- > 0) {
            expo = scan[scanidx].data[COLOR.EXP] - COLORS.COLXS;
            if (expo < -MAXGSHIFT) {
                if (expo < -MAXGSHIFT - 8) {
                    scan[scanidx].data[COLOR.RED] =
                            scan[scanidx].data[COLOR.GRN] =
                            scan[scanidx].data[COLOR.BLU] = 0;
                } else {
                    i = (-MAXGSHIFT - 1) - expo;
                    scan[scanidx].data[COLOR.RED] =
                            g_bval[MAXGSHIFT][((scan[scanidx].data[COLOR.RED] >> i) + 1) >> 1];
                    scan[scanidx].data[COLOR.GRN] =
                            g_bval[MAXGSHIFT][((scan[scanidx].data[COLOR.GRN] >> i) + 1) >> 1];
                    scan[scanidx].data[COLOR.BLU] =
                            g_bval[MAXGSHIFT][((scan[scanidx].data[COLOR.BLU] >> i) + 1) >> 1];
                }
            } else if (expo > 0) {
                if (expo > 8) {
                    scan[scanidx].data[COLOR.RED] =
                            scan[scanidx].data[COLOR.GRN] =
                            scan[scanidx].data[COLOR.BLU] = (byte) 255;
                } else {
                    i = (scan[scanidx].data[COLOR.RED] << 1 | 1) << (expo - 1);
                    scan[scanidx].data[COLOR.RED] = (byte) (i > 255 ? 255 : g_bval[0][i]);
                    i = (scan[scanidx].data[COLOR.GRN] << 1 | 1) << (expo - 1);
                    scan[scanidx].data[COLOR.GRN] = (byte) (i > 255 ? 255 : g_bval[0][i]);
                    i = (scan[scanidx].data[COLOR.BLU] << 1 | 1) << (expo - 1);
                    scan[scanidx].data[COLOR.BLU] = (byte) (i > 255 ? 255 : g_bval[0][i]);
                }
            } else {
                scan[scanidx].data[COLOR.RED] = g_bval[-expo][scan[scanidx].data[COLOR.RED]];
                scan[scanidx].data[COLOR.GRN] = g_bval[-expo][scan[scanidx].data[COLOR.GRN]];
                scan[scanidx].data[COLOR.BLU] = g_bval[-expo][scan[scanidx].data[COLOR.BLU]];
            }
            scan[scanidx].data[COLOR.EXP] = (byte) (COLORS.COLXS);
            scanidx++;
        }
        return (0);
    }
    
    public static int byte2uint(byte b) {
        return (int) (b&0x0ff); 
    }
    
    public static int gambs_colrs( /* convert gamma bytes to colr scanline */
            COLR[] scan,
            int len) {
        int nexpo;
        int scanidx = 0;
        if ((g_mant == null) | (g_nexp == null)) {
            return (-1);
        }
        while (len-- > 0) {
            nexpo = byte2uint(g_nexp[byte2uint(scan[scanidx].data[COLOR.RED])]);
            if (g_nexp[byte2uint(scan[scanidx].data[COLOR.GRN])] < nexpo) {
                nexpo = byte2uint(g_nexp[byte2uint(scan[scanidx].data[COLOR.GRN])]);
            }
            if (g_nexp[byte2uint(scan[scanidx].data[COLOR.BLU])] < nexpo) {
                nexpo = byte2uint(g_nexp[byte2uint(scan[scanidx].data[COLOR.BLU])]);
            }
            if (nexpo < g_nexp[byte2uint(scan[scanidx].data[COLOR.RED])]) {
                scan[scanidx].data[COLOR.RED] = (byte) (byte2uint(g_mant[byte2uint(scan[scanidx].data[COLOR.RED])])
                        >> (byte2uint(g_nexp[byte2uint(scan[scanidx].data[COLOR.RED])]) - nexpo));
            } else {
                scan[scanidx].data[COLOR.RED] = g_mant[byte2uint(scan[scanidx].data[COLOR.RED])];
            }
            if (nexpo < byte2uint(g_nexp[byte2uint(scan[scanidx].data[COLOR.GRN])])) {
                scan[scanidx].data[COLOR.GRN] = (byte) (byte2uint(g_mant[byte2uint(scan[scanidx].data[COLOR.GRN])])
                        >> (byte2uint(g_nexp[byte2uint(scan[scanidx].data[COLOR.GRN])]) - nexpo));
            } else {
                scan[scanidx].data[COLOR.GRN] = g_mant[byte2uint(scan[scanidx].data[COLOR.GRN])];
            }
            if (nexpo < byte2uint(g_nexp[byte2uint(scan[scanidx].data[COLOR.BLU])])) {
                scan[scanidx].data[COLOR.BLU] = (byte) (byte2uint(g_mant[byte2uint(scan[scanidx].data[COLOR.BLU])])
                        >> (byte2uint(g_nexp[byte2uint(scan[scanidx].data[COLOR.BLU])]) - nexpo));
            } else {
                scan[scanidx].data[COLOR.BLU] = g_mant[byte2uint(scan[scanidx].data[COLOR.BLU])];
            }
            scan[scanidx].data[COLOR.EXP] = (byte) (COLORS.COLXS - nexpo);
            scanidx++;
        }
        return (0);
    }

    public static void shiftcolrs( /* shift a scanline of colors by 2^adjust */
            COLR[] scan,
            int len,
            int adjust) {
        int minexp;
        int scanidx = 0;

        if (adjust == 0) {
            return;
        }
        minexp = adjust < 0 ? -adjust : 0;
        while (len-- > 0) {
            if (byte2uint(scan[scanidx].data[COLOR.EXP]) <= minexp) {
                scan[scanidx].data[COLOR.RED] = scan[scanidx].data[COLOR.GRN] = scan[scanidx].data[COLOR.BLU] =
                        scan[scanidx].data[COLOR.EXP] = 0;
            } else {
                scan[scanidx].data[COLOR.EXP] = (byte) (byte2uint(scan[scanidx].data[COLOR.EXP])+adjust);
            }
            scanidx++;
        }
    }

    void normcolrs( /* normalize a scanline of colrs */
            COLR[] scan,
            int len,
            int adjust) {
        int c;
        int shift;
        int scanidx = 0;

        while (len-- > 0) {
            shift = byte2uint(scan[scanidx].data[COLOR.EXP]) + adjust - COLORS.COLXS;
            if (shift > 0) {
                if (shift > 8) {
                    scan[scanidx].data[COLOR.RED] =
                            scan[scanidx].data[COLOR.GRN] =
                            scan[scanidx].data[COLOR.BLU] = (byte) 255;
                } else {
                    shift--;
                    c = (scan[scanidx].data[COLOR.RED] << 1 | 1) << shift;
                    scan[scanidx].data[COLOR.RED] = (byte) (c > 255 ? 255 : c);
                    c = (scan[scanidx].data[COLOR.GRN] << 1 | 1) << shift;
                    scan[scanidx].data[COLOR.GRN] = (byte) (c > 255 ? 255 : c);
                    c = (scan[scanidx].data[COLOR.BLU] << 1 | 1) << shift;
                    scan[scanidx].data[COLOR.BLU] = (byte) (c > 255 ? 255 : c);
                }
            } else if (shift < 0) {
                if (shift < -8) {
                    scan[scanidx].data[COLOR.RED] =
                            scan[scanidx].data[COLOR.GRN] =
                            scan[scanidx].data[COLOR.BLU] = 0;
                } else {
                    shift = -1 - shift;
                    scan[scanidx].data[COLOR.RED] = (byte) (((byte2uint(scan[scanidx].data[COLOR.RED]) >> shift) + 1) >> 1);
                    scan[scanidx].data[COLOR.GRN] = (byte) (((byte2uint(scan[scanidx].data[COLOR.GRN]) >> shift) + 1) >> 1);
                    scan[scanidx].data[COLOR.BLU] = (byte) (((byte2uint(scan[scanidx].data[COLOR.BLU]) >> shift) + 1) >> 1);
                }
            }
            scan[scanidx].data[COLOR.EXP] = (byte) (COLORS.COLXS - adjust);
            scanidx++;
        }
    }
}
