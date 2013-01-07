/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author arwillis
 */
public class SAVQSTR {
//    #ifndef lint
//static const char RCSid[] = "$Id$";
//#endif
/*
 *  Save unshared strings.
 *
 *  External symbols declared in standard.h
 */

//#include "copyright.h"
//
//#include <stdlib.h>
//
//#include "rtio.h"
//#include "rterror.h"


//#if 1

public static String
savqstr(			/* save a private string */
String  s)
{
//	char[]  cp;
//	char[]  newp;
//        int cpidx=0;
//        cp = s;
//	for (cpidx = 0; cp[cpidx++] != 0; ) {			/* compute strlen()+1 */
//		;
//        }
//        newp = Arrays.copyOf(s, cpidx);
//	newp = (char *)malloc(cp-s);
//	if (newp == NULL) {
//		eputs("out of memory in savqstr");
//		quit(1);
//	}
//	for (cp = newp; (*cp++ = *s++); )		/* inline strcpy() */
//		;
//	return(newp);				/* return new location */
    return s;
}


void
freeqstr(			/* free a private string */
char[] s)
{
//	if (s != NULL)
//		free((void *)s);
}

//#else

/*
 *  Save unshared strings, packing them together into
 *  large blocks to optimize paging in VM environments.
 */

//#ifdef  SMLMEM
//#ifndef  MINBLOCK
//#define  MINBLOCK	(1<<10)		/* minimum allocation block size */
//#endif
//#ifndef  MAXBLOCK
//#define  MAXBLOCK	(1<<14)		/* maximum allocation block size */
//#endif
//#else
//#ifndef  MINBLOCK
static int  MINBLOCK=	(1<<12);		/* minimum allocation block size */
//#endif
//#ifndef  MAXBLOCK
static int  MAXBLOCK=	(1<<16);		/* maximum allocation block size */
//#endif
//#endif

//extern char  *bmalloc();


//public static char[]
//savqstr(			/* save a private string */
//char[]  s) throws IOException
//{
//	char[] curp = null;		/* allocated memory pointer */
//	int nrem = 0;		/* bytes remaining in block */
//	int nextalloc = MINBLOCK;	/* next block size */
//	char[] cp;
//	int n, cpidx=0;
//
//	for (cp = s; cp[cpidx++]!=0; )	{		/* compute strlen()+1 */
//		//;
//        }
//	if ((n = cpidx) > nrem) {		/* do we need more core? */
//		//bfree(curp, nrem);			/* free remnant */
//		while (n > nextalloc)
//			nextalloc <<= 1;
////                curp = new char[nrem=nextalloc];
////		if ((curp = bmalloc(nrem=nextalloc)) == NULL) {
////			eputs("out of memory in savqstr");
////			quit(1);
////		}
//		if ((nextalloc <<= 1) > MAXBLOCK)	/* double block size */
//			nextalloc = MAXBLOCK;
//	}
//        curp = Arrays.copyOf(s, nextalloc);
////	for (cp = curp; *cp++ = *s++; )		/* inline strcpy() */
////		;
////	s = curp;				/* update allocation info. */
////	curp = cp;
////	nrem -= n;
//	return(s);				/* return new location */
//}
//
//
//void
//freeqstr(			/* free a private string (not recommended) */
//char[]  s)
//{
////	bfree(s, strlen(s)+1);
//}

//#endif
}
