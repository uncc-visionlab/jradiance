/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
