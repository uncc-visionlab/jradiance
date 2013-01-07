/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

/**
 *
 * @author arwillis
 */
public class GETLIBPATH {
    /*
     * Return Radiance library search path
     *
     *  External symbols declared in standard.h
     */

    static String libpath = null;

    public static String getrlibpath() {
        if (libpath == null) {
            if ((libpath = System.getenv(PATHS.ULIBVAR)) == null) {
                libpath = PATHS.DEFPATH;
            }
        }
        return (libpath);
    }
}
