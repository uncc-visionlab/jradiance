/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
