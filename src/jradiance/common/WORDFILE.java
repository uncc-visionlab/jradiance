/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
