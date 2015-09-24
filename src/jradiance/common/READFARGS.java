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

import java.io.IOException;
import java.io.InputStream;
import jradiance.ot.OCONV;

/**
 *
 * @author arwillis
 */
public class READFARGS {
//    #ifndef lint
//static const char	RCSid[] = "$Id$";
//#endif
/*
     * Allocate, read and free object arguments
     */

//#include "copyright.h"
//
//#include "standard.h"
//
//#include "object.h"
//char[] getstr(char[] s, InputStream fp) throws IOException { return FGETWORD.fgetword(s,s.length,fp)!= null; }
//int getint(char[] s) {	return (getstr(s) && isint(s)!=0)\
//#define getflt(s)	(getstr(s) && isflt(s))
    public static int readfargs( /* read function arguments from stream */
            OBJECT.FUNARGS fa,
            InputStream fp) throws IOException {
//#define getstr(s)	(fgetword(s,sizeof(s),fp)!=NULL)
//#define getint(s)	(getstr(s) && isint(s))
//#define getflt(s)	(getstr(s) && isflt(s))
        char[] sbuf = new char[OBJECT.MAXSTR];
        String word;
        int n = -1, i;

        word = FGETWORD.fgetword(sbuf, sbuf.length, fp);
        n = Integer.parseInt(word);
        if (n < 0) {
            return 0;
        }
//
//	if (PORTIO.getint(sbuf)==0 || (n = Integer.parseInt(new String(sbuf))) < 0)
//		return(0);
        if ((fa.nsargs = (short) n) != 0) {
            fa.sarg = new String[n];
            if (fa.sarg == null) {
                return (-1);
            }
            for (i = 0; i < fa.nsargs; i++) {
                if ((word = FGETWORD.fgetword(sbuf, sbuf.length, fp)) == null) {
                    return (0);
                }
                fa.sarg[i] = SAVESTR.savestr(word);
            }
        } else {
            fa.sarg = null;
        }
        word = FGETWORD.fgetword(sbuf, sbuf.length, fp);
        n = Integer.parseInt(word);
        if (n < 0) {
            return (0);
        }
//#ifdef  IARGS
        if (OCONV.IARGS) {
            if ((fa.niargs = (short) n) != 0) {
                fa.iarg = new long[n];
                if (fa.iarg == null) {
                    return (-1);
                }
                for (i = 0; i < n; i++) {
                    word = FGETWORD.fgetword(sbuf, sbuf.length, fp);
//        n = Integer.parseInt(word);
//			if (!getint(sbuf))
//				return(0);
                    fa.iarg[i] = Long.parseLong(word);
                }
            } else {
                fa.iarg = null;
            }
        } else {
//#else
            if (n != 0) {
                return (0);
            }
        }
//#endif
        word = FGETWORD.fgetword(sbuf, sbuf.length, fp);
        n = Integer.parseInt(word);
        if (n < 0) {
            return (0);
        }
        if ((fa.nfargs = (short) n) != 0) {
            fa.farg = new double[n];
            if (fa.farg == null) {
                return (-1);
            }
            for (i = 0; i < n; i++) {
                word = FGETWORD.fgetword(sbuf, sbuf.length, fp);
//			if (!getflt(sbuf))
//				return(0);
                fa.farg[i] = Double.parseDouble(word);
            }
        } else {
            fa.farg = null;
        }
        return 1;
//#undef getflt
//#undef getint
//#undef getstr
    }

    void freefargs( /* free object arguments */
            OBJECT.FUNARGS fa) {
//	register int  i;
//
//	if (fa->nsargs) {
//		for (i = 0; i < fa->nsargs; i++)
//			freestr(fa->sarg[i]);
//		free((void *)fa->sarg);
//		fa->sarg = NULL;
//		fa->nsargs = 0;
//	}
//#ifdef  IARGS
//	if (fa->niargs) {
//		free((void *)fa->iarg);
//		fa->iarg = NULL;
//		fa->niargs = 0;
//	}
//#endif
//	if (fa->nfargs) {
//		free((void *)fa->farg);
//		fa->farg = NULL;
//		fa->nfargs = 0;
//	}
    }
}
