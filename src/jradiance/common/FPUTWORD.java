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
import java.io.OutputStream;

/**
 *
 * @author arwillis
 */
public class FPUTWORD {

    static void fputword( /* put (quoted) word to file stream */
            String s,
            OutputStream fp) throws IOException {
        int hasspace = 0;
        int quote = 0;
        int cidx;
        char[] c = s.toCharArray();
        /* check if quoting needed */
        for (cidx = 0; cidx < c.length; cidx++) {
            if (Character.isWhitespace(c[cidx])) {
                hasspace++;
            } else if (c[cidx] == '"') {
                quote = '\'';
            } else if (c[cidx] == '\'') {
                quote = '"';
            }
        }

        if ((hasspace != 0) || (quote != 0)) {	/* output with quotes */
            if (quote == 0) {
                quote = '"';
            }
            fp.write(quote);
            fp.write(s.getBytes());
            fp.write(quote);
        } else /* output sans quotes */ {
            fp.write(s.getBytes());
        }
    }
}
