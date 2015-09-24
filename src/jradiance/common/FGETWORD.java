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
public class FGETWORD {
//#ifndef lint
    static String RCSid = "$Id$";
//#endif
/*
     * Read white space separated words from stream
     *
     *  External symbols declared in rtio.h
     */

    static String fgetword( /* get (quoted) word up to n-1 characters */
            char[] s,
            int n,
            InputStream fp) throws IOException {
        int quote = '\0';
        char[] cp;
        int cidx = 0;
        int c;
        /* sanity checks */
        if ((s == null) | (n <= 0)) {
            return (null);
        }
        /* skip initial white space */
        do {
            c = fp.read();
        } while (Character.isWhitespace(c));
        /* check for quote */
        if ((c == '"') | (c == '\'')) {
            quote = c;
            c = fp.read();
        }
        /* check for end of file */
        if (c == -1) {
            return (null);
        }
        /* get actual word */
        cp = s;
        do {
            if (--n <= 0) /* check length limit */ {
                break;
            }
            cp[cidx++] = (char) c;
            fp.mark(1);
            c = fp.read();
        } while (c != -1 && !(quote != 0 ? c == quote : Character.isWhitespace(c)));
        cp[cidx] = '\0';
        if ((c != -1) & (quote == 0)) /* replace space */ {
            fp.reset();
        }
        String word = new String(s);
        word = word.substring(0,word.indexOf('\0'));
        return word;
    }
}
