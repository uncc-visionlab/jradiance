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

import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author arwillis
 */
public class RTPROCESS {
/* RCSid $Id$ */
/*
 *   rtprocess.h 
 *   Routines to communicate with separate process via dual pipes
 *
 *   WARNING: On Windows, there's a system header named <process.h>.
 */
//#ifndef _RAD_PROCESS_H_
//#define _RAD_PROCESS_H_
//
//#include  <errno.h>
//#ifdef _WIN32
//  #include <windows.h> /* DWORD etc. */
//  #include <stdio.h>
//  typedef DWORD RT_PID;
//  #include <process.h> /* getpid() and others */
//  #define nice(inc) win_nice(inc)
//
//  #ifdef __cplusplus
//  extern "C" {
//  #endif
//  extern FILE *win_popen(char *command, char *type);
//  extern int win_pclose(FILE *p);
//  int win_kill(RT_PID pid, int sig /* ignored */);
//  #define kill(pid,sig) win_kill(pid,sig)
//  #ifdef __cplusplus
//  }
//  #endif
//
//  #define popen(cmd,mode) win_popen(cmd,mode)
//  #define pclose(p) win_pclose(p)
//#else
//  #include <stdio.h>
//  #include <sys/param.h>
//  #include <sys/types.h>
//  typedef pid_t RT_PID;
//#endif

//#include "paths.h"
//
//#ifdef __cplusplus
//extern "C" {
//#endif

/* On Windows, a process ID is a DWORD. That might actually be the
   same thing as an int, but it's better not to assume anything.

   This means that we shouldn't rely on PIDs and file descriptors
   being the same type, so we have to describe processes with a struct,
   instead of the original int[3]. For that purpose, we typedef a
   platform independent RT_PID.
*/

public static final int PIPE_BUF = 4096; // from limits.h
//#ifndef PIPE_BUF
//  #ifdef PIPSIZ
//    #define PIPE_BUF	PIPSIZ
//  #else
//    #ifdef PIPE_MAX
//      #define PIPE_BUF	PIPE_MAX
//    #else
//      #define PIPE_BUF	512		/* hyperconservative */
//    #endif
//  #endif
//#endif

public class SUBPROC {
//	public int r; /* read handle */
//	public int w; /* write handle */
	public InputStream r; /* read handle */
	public OutputStream w; /* write handle */
	public int running; /* doing something */
//	RT_PID pid; /* process ID */
        public Process pid;
}

//#define SP_INACTIVE {-1,-1,0,0} /* for static initializations */

//extern int open_process(SUBPROC *pd, char *av[]);
//extern int close_process(SUBPROC *pd);
//extern int process(SUBPROC *pd, char *recvbuf, char *sendbuf, int nbr, int nbs);
//extern int readbuf(int fd, char *bpos, int siz);
//extern int writebuf(int fd, char *bpos, int siz);

//#ifdef _WIN32
///* any non-negative increment will send the process to IDLE_PRIORITY_CLASS. */
//extern int win_nice(int inc);
//#endif
//
//
//#ifdef __cplusplus
//}
//#endif
//#endif /* _RAD_PROCESS_H_ */

    
}
