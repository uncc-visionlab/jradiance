/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
