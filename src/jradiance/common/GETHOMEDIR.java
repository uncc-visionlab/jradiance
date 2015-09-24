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
