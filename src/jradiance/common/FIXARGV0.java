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

import java.io.File;

/**
 *
 * @author arwillis
 */
public class FIXARGV0 {
//#ifndef lint
//static const char	RCSid[] = "$Id$";
//#endif
/*
     * Fix argv[0] for DOS environments
     *
     *  External symbols declared in paths.h
     */

//#include "copyright.h"
//
//#include <ctype.h>
    public static String fixargv0( /* extract command name from full path */
            String av0) {
        int extIdx = av0.lastIndexOf(".");
        int pathIdx = av0.lastIndexOf(File.separator);
        extIdx = (extIdx == -1) ? (av0.length()) : extIdx;
        pathIdx = (pathIdx == -1) ? 0 : pathIdx;
        String av1 = av0.substring(pathIdx, extIdx).toLowerCase();
        return av1;
    }
}
