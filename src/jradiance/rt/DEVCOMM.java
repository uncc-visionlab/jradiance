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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import jradiance.common.COLORS.COLOR;
import jradiance.common.PORTIO;

/**
 *
 * @author arwillis
 */
public class DEVCOMM {
//#ifndef lint
//static const char	RCSid[] = "$Id$";
//#endif
/*
     *  devcomm.c - communication routines for separate drivers.
     *
     *  External symbols declared in driver.h
     */

//#include "copyright.h"
//
//#include <sys/types.h>
//#ifndef _WIN32
//#include <sys/wait.h>
//#endif
//
//#include "paths.h"
//#include "platform.h"
//#include "standard.h"
//#include "driver.h"
//#ifndef DEVPATH
//#define DEVPATH		getenv("PATH")	/* device search path */
//#endif
    InputStream devin;
    OutputStream devout;
    int devchild;

//static struct driver * final_connect(void);
//static void mygets(char	*s, FILE	*fp);
//static void myputs(char	*s, FILE	*fp);
//static void reply_error(char	*routine);
//static void getstate(void);
    public class COMM_DRIVER extends DRIVER {

        @Override
        void close() throws IOException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//static void
//comm_close()			/* done with driver */
//{
            int pid;

//	erract[COMMAND].pf = NULL;		/* reset error vectors */
//	if (erract[WARNING].pf != NULL)
//		erract[WARNING].pf = wputs;
            devout.close();
            devin.close();
            if (devchild < 0) {
                return;
            }
//#ifndef _WIN32
//	while ((pid = wait(0)) != -1 && pid != devchild)
//		;
//#endif
        }

        @Override
        void clear(int xres, int yres) throws IOException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//static void
//comm_clear(				/* clear screen */
//	int	xres,
//	int	yres
//)
//{
            devout.write(COM_CLEAR);
            putw(xres, devout);
            putw(yres, devout);
            devout.flush();
        }

        void putw(int x, OutputStream devout) throws IOException {
            byte[] bytes = ByteBuffer.allocate(2).putShort((short) x).array();
            devout.write(bytes);
        }

        @Override
        void paintr(COLOR col, int xmin, int ymin, int xmax, int ymax) throws IOException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//static void
//comm_paintr(	/* paint a rectangle */
//	COLOR	col,
//	int	xmin,
//	int	ymin,
//	int	xmax,
//	int	ymax
//)
//{
            devout.write(COM_PAINTR);
            //fwrite((char *)col, sizeof(COLOR), 1, devout);
            col.write(devout);
            putw(xmin, devout);
            putw(ymin, devout);
            putw(xmax, devout);
            putw(ymax, devout);
        }

        int getw(InputStream is) throws IOException {
            long w = PORTIO.getint(2, is);
            return (int) w;
        }

        @Override
        int getcur(int[] xp, int[] yp) throws IOException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//static int
//comm_getcur(			/* get and return cursor position */
//	int	*xp,
//	int	*yp
//)
//{
            int c;

            devout.write(COM_GETCUR);
            devout.flush();
            if (devin.read() != COM_GETCUR) {
//		reply_error("getcur");
            }
            c = devin.read();
            xp[0] = getw(devin);
            yp[0] = getw(devin);
            return (c);
        }

        @Override
        void comout(char[] str) throws IOException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//static void
//comm_comout(			/* print string to command line */
//	char	*str
//)
//{
            devout.write(COM_COMOUT);
            myputs(str, devout);
            if (str[strlen(str) - 1] == '\n') {
                devout.flush();
            }
        }

        @Override
        void comin(char[] buf, char[] prompt) throws IOException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//static void
//comm_comin(			/* read string from command line */
//	char	*buf,
//	char	*prompt
//)
//{
            devout.write(COM_COMIN);
            if (prompt == null) {
                devout.write(0);
            } else {
                devout.write(1);
                myputs(prompt, devout);
            }
            devout.flush();
            if (devin.read() != COM_COMIN) {
//		reply_error("comin");
            }
            mygets(buf, devin);
            getstate();
        }

        @Override
        void flush() throws IOException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//static void
//comm_flush(void)				/* flush output to driver */
//{
            devout.write(COM_FLUSH);
            devout.flush();
            if (devin.read() != COM_FLUSH) {
//		reply_error("flush");
            }
            getstate();
        }
    }

//static DRIVER
//final_connect()				/* verify and initialize connection */
//{
//	putw(COM_SENDM, devout);
//	fflush(devout);
//	if (getw(devin) != COM_RECVM)
//		return(NULL);
//						/* get driver parameters */
//	getstate();
//						/* set error vectors */
//	erract[COMMAND].pf = comm_comout;
//	/*					doesn't work with raypcalls.c
//	if (erract[WARNING].pf != NULL)
//		erract[WARNING].pf = comm_comout;
//	*/
//	return(&comm_driver);
//}
//
//
//DRIVER
//slave_init(			/* run rview in slave mode */
//	char	*dname,
//	char	*id
//)
//{
//	devchild = -1;				/* we're the slave here */
//	devout = stdout;			/* use standard input */
//	devin = stdin;				/* and standard output */
//	return(final_connect());		/* verify initialization */
//}
//
//
//DRIVER
//comm_init(			/* set up and execute driver */
//	char[]	dname,
//	char[]	id
//)
//{
//	char[]	dvcname;
//	int[]	p1 = new int[2], p2 = new int[2];
//	char[]	pin = new char[16], pout = new char[16];
//						/* find driver program */
//	if ((dvcname = getpath(dname, DEVPATH, X_OK)) == NULL) {
//		eputs(dname);
//		eputs(": not found\n");
//		return(null);
//	}
////#ifdef RHAS_FORK_EXEC
//						/* open communication pipes */
//	if (pipe(p1) == -1 || pipe(p2) == -1)
//		goto syserr;
//	if ((devchild = fork()) == 0) {	/* fork driver process */
//		close(p1[1]);
//		close(p2[0]);
//		sprintf(pin, "%d", p1[0]);
//		sprintf(pout, "%d", p2[1]);
//		execl(dvcname, dname, pin, pout, id, NULL);
//		perror(dvcname);
//		_exit(127);
//	}
//	if (devchild == -1)
//		goto syserr;
//	close(p1[0]);
//	close(p2[1]);
//	/*
//	 * Close write stream on exec to avoid multiprocessing deadlock.
//	 * No use in read stream without it, so set flag there as well.
//	 */
//	fcntl(p1[1], F_SETFD, FD_CLOEXEC);
//	fcntl(p2[0], F_SETFD, FD_CLOEXEC);
//	if ((devout = fdopen(p1[1], "w")) == NULL)
//		goto syserr;
//	if ((devin = fdopen(p2[0], "r")) == NULL)
//		goto syserr;
//	return(final_connect());		/* verify initialization */
//syserr:
//	perror(dname);
//	return(NULL);
//
////#else	/* ! RHAS_FORK_EXEC */
////
////	eputs(dname);
////	eputs(": no fork/exec\n");
////	return(NULL);
////
////#endif	/* ! RHAS_FORK_EXEC */
//}
        public static int strlen(char[] str) {
            int len = 0;
            while (str[len] != 0) {
                len++;
            }
            return len;
        }    
    public static int strcmp(char[] str1, char[] str2) {
        int str1idx = 0, str2idx = 0;
        int val = 0;
        do {
            val = str1[str1idx++] - str2[str2idx++];
        } while (val == 0 && str1idx < str1.length && str2idx < str2.length 
                && str1[str1idx] != '\0' && str2[str2idx] != '\0');
        return val;
    }
        public static int strncmp(char[] str1, char[] str2, int len) {
            int idx = 0;
            while (str1[idx] == str2[idx]) {
                idx++;
                if (idx == len) 
                    return 0;
            }
            return str1[idx]-str2[idx];
        }   
    static void mygets( /* get string from file (with nul) */
            char[] s,
            InputStream fp) throws IOException {
        int c, sidx = 0;
        while ((c = fp.read()) != -1) {
            if ((s[sidx++] = (char) c) == '\0') {
                return;
            }
        }
        s[sidx] = '\0';
    }

    static void myputs( /* put string to file (with nul) */
            char[] s,
            OutputStream fp) throws IOException {
        int sidx = 0;
        do {
            fp.write(s[sidx]);
        } while (s[sidx++] != 0);
    }

//
//static void
//reply_error(			/* what should we do here? */
//	char	*routine
//)
//{
//	eputs(routine);
//	eputs(": driver reply error\n");
//	quit(1);
//}
//
    static void getstate() /* get driver state variables */ {
//	fread((char *)&comm_driver.pixaspect,
//			sizeof(comm_driver.pixaspect), 1, devin);
//	comm_driver.xsiz = getw(devin);
//	comm_driver.ysiz = getw(devin);
//	comm_driver.inpready = getw(devin);
    }
}
