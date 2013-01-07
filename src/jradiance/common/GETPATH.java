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
public class GETPATH {
    /*
     *  getpath.c - function to search for file in a list of directories
     *
     *  External symbols declared in standard.h
     */

//    static char[] pname = new char[PATHS.PATH_MAX];
    static String pname = null;

    public static String getpath /* expand fname, return full path */(
            String fname,
            String searchpath,
            int mode) {
        String uname = null;
        String cp;
        int i;

        if (fname == null) {
            return (null);
        }
        pname = "";
//        pname[0] = '\0';		/* check for full specification */
//        pname = fname;
        if (PATHS.ISABS(fname)) { /* absolute path */
            //strncpy(pname, fname, sizeof(pname) - 1);
            pname = fname;
        } else {
            switch (fname.charAt(0)) {
                case '.':				/* relative to cwd */
//                    strncpy(pname, fname, sizeof(pname) - 1);
                    pname = fname;
                    break;
                case '~':				/* relative to home directory */
//                    fname++;
//                    cp = uname;
//                    for (i = 0; i < sizeof(uname) &&  * fname != '\0' && !ISDIRSEP( * fname); i++) {
//                         * cp++ =  * fname++;
//                    }
//                     * cp = '\0';
                    cp = GETHOMEDIR.gethomedir(uname, pname, -1);
                    if (cp == null) {
                        return null;
                    }
                    //strncat(pname, fname, sizeof(pname) - strlen(pname) - 1);
                    pname += fname;
                    break;
            }
        }
        File f = new File(pname);
        if (pname.length() > 0) /* got it, check access if search requested */ {
//            return (searchpath == null || access(pname, mode) == 0 ? pname : null);
            return (searchpath == null || f.canRead() ? pname : null);
        }

        if (searchpath == null) {			/* don't search */
//            strncpy(pname, fname, sizeof(pname) - 1);
            pname = fname;
            return (pname);
        }
        String filesep = System.getProperty("file.separator");
        String[] paths = searchpath.split(PATHS.PATHSEP);
        /* check search path */
        for (String path : paths) {
//            while ( * searchpath && ( * cp =  * searchpath++) != PATHSEP) {
//                cp++;
//            }
//            if (cp > pname && !ISDIRSEP(cp[-1])) {
//                 * cp++ = DIRSEP;
//            }
            if (path.startsWith(filesep) && !path.endsWith(filesep)) {
                path += filesep;
            }
//            strncpy(cp, fname, sizeof(pname) - strlen(pname) - 1);
            path += fname;
//            if (access(pname, mode) == 0) /* file accessable? */ {
//                return (pname);
//            }
            try {
                f = new File(path);
                if (f.canRead()) {
                    return path;
                }
            } catch (Exception e) {
            }
        }         /* not found */
        return (null);
    }
}
