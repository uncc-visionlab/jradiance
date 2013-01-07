/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

import java.util.HashMap;

/**
 *
 * @author arwillis
 */
public class SAVESTR {
    /*
     *  savestr.c - routines for efficient string storage.
     *
     *	Savestr(s) stores a shared read-only string.
     *  Freestr(s) indicates a client is finished with a string.
     *	All strings must be null-terminated.  There is
     *  no imposed length limit.
     *	Strings stored with savestr(s) can be equated
     *  reliably using their pointer values.
     *	Calls to savestr(s) and freestr(s) should be
     *  balanced (obviously).  The last call to freestr(s)
     *  frees memory associated with the string; it should
     *  never be referenced again.
     *
     *  External symbols declared in standard.h
     */

    static HashMap<String, Integer> stringMap = new HashMap();

    public static String savestr(String str) /* save a string */ {
        String key = str;
        Integer i = stringMap.get(key);
        if (i == null) {
            stringMap.put(key, 1);
        } else {
            stringMap.put(key, i + 1);
        }
        return str;
    }

    public static void freestr(char[] s) /* free a string */ {
        String key = new String(s);
        Integer i = stringMap.get(key);
        if (i == null) {
            stringMap.put(key, 1);
        } else {
            if (i > 1) {
                stringMap.put(key, i - 1);
            } else {
                stringMap.remove(key);
            }
        }
    }

    public static int shash(char[] s) {
        int h = 0, sidx = 0;

        while (s.length > sidx && s[sidx] != 0) {
            h = (h << 1 & 0x7fff) ^ (s[sidx++] & 0xff);
        }
        return (h);
    }
}
