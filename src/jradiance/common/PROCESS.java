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
import java.io.OutputStream;

/**
 *
 * @author arwillis
 */
public class PROCESS {
//#ifndef lint
//static const char	RCSid[] = "$Id$";
//#endif
/*
     * Routines to communicate with separate process via dual pipes
     *
     * External symbols declared in standard.h
     */

//#include "copyright.h"
//
//#include "rtprocess.h"

    /*
    
    The functions open_process() and close_process() exist in
    (currently) two versions, which are found in the files:
    
    win_process.c
    unix_process.c
    
     */
    public static int process( /* process data through pd */
            RTPROCESS.SUBPROC pd,
            byte[] recvbuf, byte[] sendbuf,
            int nbr, int nbs) throws IOException {
        if (nbs > RTPROCESS.PIPE_BUF) {
            return (-1);
        }
        if (pd.running == 0) {
            return (-1);
        }
        if (writebuf(pd.w, sendbuf, nbs) < nbs) {
            return (-1);
        }
        return (readbuf(pd.r, recvbuf, nbr));
    }

    public static int readbuf( /* read all of requested buffer */
            InputStream fd,
            byte[] bpos,
            int siz) throws IOException {
        int cc = 0, nrem = siz;
//retry:
        fd.read(bpos, 0, nrem);
        nrem = 0;
//	while (nrem > 0 && (cc = read(fd, bpos, nrem)) > 0) {
//		bpos += cc;
//		nrem -= cc;
//	}
//	if (cc < 0) {
//#ifndef BSD
//		if (errno == EINTR)	/* we were interrupted! */
//			goto retry;
//#endif
//		return(cc);
//	}
        return (siz - nrem);
    }

    public static int writebuf( /* write all of requested buffer */
            OutputStream fd,
            byte[] bpos,
            int siz) throws IOException {
        int cc = 0, nrem = siz;
//retry:
        fd.write(bpos, 0, nrem);
//        while (nrem > 0 && (fd.write(bpos, 0, nrem)) > 0) {
//		bpos += nrem;
        nrem = 0;
//	}
//	if (cc < 0) {
//#ifndef BSD
//		if (errno == EINTR)	/* we were interrupted! */
//			goto retry;
//#endif
//		return(cc);
//	}
        return (siz - nrem);
    }
}
