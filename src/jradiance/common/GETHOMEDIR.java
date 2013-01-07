/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

/**
 *
 * @author arwillis
 */
public class GETHOMEDIR {

    public static String gethomedir(String uname, String path, int plen) {
        return System.getProperty("user.home");
//	struct passwd *pwent;
//	uid_t uid;
//	char *cp;
//
//	if (uname == NULL || *uname == '\0') {	/* ours */
//		if ((cp = getenv("HOME")) != NULL) {
//			strncpy(path, cp, plen);
//			path[plen-1] = '\0';
//			return path;
//		}
//		uid = getuid();
//		if ((pwent = getpwuid(uid)) == NULL)
//			return(NULL); /* we don't exist ?!? */
//		strncpy(path, pwent->pw_dir, plen);
//		path[plen-1] = '\0';
//		return path;
//	}
//	/* someone else */
//        System.
//	if ((pwent = getpwnam(uname)) == NULL)
//		return(NULL); /* no such user */
//
//	strncpy(path, pwent->pw_dir, plen);
//	path[plen-1] = '\0';
//	return path;
    }
}
