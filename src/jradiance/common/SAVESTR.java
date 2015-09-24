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
