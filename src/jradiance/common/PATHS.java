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
public class PATHS {
    public static final int PATH_MAX = 4096;        /* our own maximum */
//  #define RMAX_PATH_MAX 4096 /* our own maximum */
//  #ifndef PATH_MAX
//    #define PATH_MAX 512
//  #elif PATH_MAX > RMAX_PATH_MAX /* the OS is exaggerating */
//    #undef PATH_MAX
//    #define PATH_MAX RMAX_PATH_MAX
//  #endif

    /* posix */

	/* this is defined as _PATH_DEVNULL in /usr/include/paths.h on Linux */
//	#define NULL_DEVICE	"/dev/null"
//    #define DIRSEP		'/'
    public static final int R_OK = 4;
public static boolean ISABS(String s) {
    return ((s)!=null && (ISDIRSEP(s.charAt(0))));
}
public static final String PATHSEP=		":";
public static final char CURDIR=		'.';
//    #define DEFAULT_TEMPDIRS {"/var/tmp", "/usr/tmp", "/tmp", ".", NULL}
public static final String TEMPLATE=	"/tmp/rtXXXXXX";
public static final String TEMPLATE2=	"/tmp/rt";
//    #define TEMPLEN		17
public static final String ULIBVAR=		"RAYPATH";
public static final String DEFPATH=		":/usr/local/lib/ray";
//    #define	 fixargv0(a0)	(a0)


public static boolean ISDIRSEP(char c) {
    return ((c)==System.getProperty("file.separator").charAt(0));
}
//  #define CASEDIRSEP	case DIRSEP

}
