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
package jradiance.rt;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import jradiance.common.CALEXPR;
import jradiance.common.PATHS;

/**
 *
 * @author arwillis
 */
public class DUPHEAD {
    /*
     * Duplicate header on stdout.
     *
     * Externals declared in ray.h
     */

//#include "copyright.h"
//
//#include  "platform.h"
//#include  "standard.h"
//#include  "paths.h"
//
    int headismine = 1;		/* true if header file belongs to me */

static String  headfname = null;	/* temp file name */
static RandomAccessFile  headfp = null;	/* temp file pointer */

    static void headclean() /* remove header temp file (if one) */ {
//	if (headfname == null)
//		return;
//	if (headfp != null)
//		fclose(headfp);
//	if (headismine)
//		unlink(headfname);
    }

    static String template = PATHS.TEMPLATE2;
    static void openheader() throws IOException /* save standard output to header file */ {

	headfname = RPICT.mktemp(template);
	if ((RPMAIN.stdout = new FileOutputStream(headfname)) == null) {
//		sprintf(errmsg, "cannot open header file \"%s\"", headfname);
//		error(SYSTEM, errmsg);
	}
    }

    static void dupheader() throws IOException /* repeat header on standard output */ {
	int  c;

	if (headfp == null) {
		if ((headfp = new RandomAccessFile(headfname, "w")) == null) {
//			error(SYSTEM, "error reopening header file");
                }
//		SET_FILE_BINARY(headfp);
	} else {
            //if (headfp.seek(0L) < 0) {
            headfp.seek(0L);
//		error(SYSTEM, "seek error on header file");
        }
	while ((c = headfp.read()) != CALEXPR.EOF)
		RPMAIN.stdout.write(c);
    }
}
