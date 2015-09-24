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
public class EXPANDARG {
//#ifndef lint
//static const char	RCSid[] = "$Id$";
//#endif
/*
 * Get additional command arguments from file or environment.
 *
 *  External symbols declared in rtio.h
 */

//#include "copyright.h"
//
//#include <errno.h>
//#include <string.h>
//
//#include "rtio.h"
//#include "rtmisc.h"


public static final int MAXARGEXP=	4096;		/* maximum argument expansion */

			/* set the following to suit, -1 to disable */
static final int	envexpchr = '$';		/* environment expansion character */
static final int	filexpchr = '@';		/* file expansion character */


public static int
expandarg(		/* expand list at argument n */
int	acp,
String[] avp,
int	n)
{
	int	ace=0;
	String[]	ave = new String[MAXARGEXP];
        String[] newav;
					/* check argument */
	if (n >= acp)
		return(0);
//	errno = 0;	
	if (avp[n].charAt(0) == filexpchr) {		/* file name */
//		ace = wordfile(ave, (*avp)[n]+1);
//		if (ace < 0)
//			return(-1);	/* no such file */
	} else if (avp[n].charAt(0) == envexpchr) {		/* env. variable */
//		ace = wordstring(ave, getenv((*avp)[n]+1));
//		if (ace < 0)
//			return(-1);	/* no such variable */
	} else						/* regular argument */
		return(0);
					/* allocate new pointer list */
//	newav = (char **)bmalloc((*acp+ace)*sizeof(char *));
        newav = new String[acp+ace];
	if (newav == null)
		return(-1);
					/* copy preceeding arguments */
//	memcpy((void *)newav, (void *)*avp, n*sizeof(char *));
//					/* copy expanded argument */
//	memcpy((void *)(newav+n), (void *)ave, ace*sizeof(char *));
//					/* copy trailing arguments + NULL */
//	memcpy((void *)(newav+n+ace), (void *)(*avp+n+1), (*acp-n)*sizeof(char *));
//					/* free old list */
//	bfree((char *)*avp, (*acp+1)*sizeof(char *));
					/* assign new list */
//	*acp += ace-1;
//	*avp = newav;
	return(1);			/* return success */
}
    
}
