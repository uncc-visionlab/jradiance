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

import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author arwillis
 */
public class WORDFILE {
//#ifndef lint
//static const char	RCSid[] = "$Id$";
//#endif
/*
 * Load whitespace separated words from a file into an array.
 * Assume the passed pointer array is big enough to hold them all.
 *
 * External symbols declared in standard.h
 */

//#include "copyright.h"
//
//#include <ctype.h>
//#include <string.h>
//#include <stdio.h>
//#include <sys/types.h>
//#include <sys/stat.h>
//#include <fcntl.h>
//
//#include "platform.h"
//#include "standard.h"


//#ifndef MAXFLEN
public static final int MAXFLEN=		65536;	/* file must be smaller than this */
//#endif


int
wordfile(		/* get words from fname, put in words */
String[] words,
String fname) throws IOException
{
//	int	fd;
	byte[]	buf = new byte[MAXFLEN];
	int	n=0;
					/* load file into buffer */
	if (fname == null)
		return(-1);			/* no filename */
        FileInputStream fd = new FileInputStream(fname);
//	if ((fd = open(fname, 0)) < 0)
//		return(-1);			/* open error */
	fd.read( buf, 0, MAXFLEN);
	fd.close();
//	if (n < 0)				/* read error */
//		return(-1);
	if (n == MAXFLEN)		/* file too big, take what we can */
		while (!Character.isWhitespace(buf[--n]))
			if (n <= 0)		/* one long word! */
				return(-1);
	buf[n] = '\0';			/* terminate */
	return(wordstring(words, buf));	/* wordstring does the rest */
}


int
wordstring(			/* allocate and load argument list */
String[] avl,
byte[] str)
{
//	char	*cp, **ap;
        byte[] cp, ap=null;
	
	if (str == null)
		return(-1);
	//cp = bmalloc(strlen(str)+1);
        cp = new byte[str.length+1];
	if (cp == null)			/* ENOMEM */
		return(-1);
//	strcpy(cp, str);
        System.arraycopy(str, 0, cp, 0, str.length+1);
//	ap = avl;		/* parse into words */
        int cpidx=0, apidx=0;
	for ( ; ; ) {
		while (Character.isWhitespace( cp[cpidx]))	/* nullify spaces */
			cp[cpidx++] = '\0';
		if (cp[cpidx]==0)		/* all done? */
			break;
//		ap[apidx++] = cp;		/* add argument to list */
		while (cp[++cpidx] != 0 && !Character.isWhitespace(cp[cpidx])) {
			;
                }
	}
	ap[apidx] = '\0';
//	return(ap - avl);
        return 0;
}
   
}
