/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
