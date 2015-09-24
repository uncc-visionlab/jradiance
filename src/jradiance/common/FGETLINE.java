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

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author arwillis
 */
public class FGETLINE {
//    #ifndef lint
    static String RCSid = "$Id$";
//#endif
/*
     * fgetline.c - read line with escaped newlines.
     *
     *  External symbols declared in rtio.h
     */

//#include "copyright.h"
//
//#include  "rtio.h"
//
//#ifdef getc_unlocked		/* avoid horrendous overhead of flockfile */
//#undef getc
//#define getc    getc_unlocked
//#endif
    public static char[] fgetline( /* read in line with escapes, elide final newline */
            char[] s,
            int n,
            InputStream fp) throws IOException {
        char[] cp = s;
        int cidx = 0;
        int c = -1;

        while (--n > 0 && (c = fp.read()) != -1) {
            fp.mark(1);
            if (c == '\r' && (c = fp.read()) != '\n') {
                fp.reset();
                c = '\n';
            }
            if (c == '\n' && (cidx == 0 || cp[cidx - 1] != '\\')) {
                break;
            }
            cp[cidx] = (char) c;
            cidx++;
        }
        if (cp == s && c == -1) {
            return null;
        }
        cp[cidx] = '\0';
        return (s);
    }
}
