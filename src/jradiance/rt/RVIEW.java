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
import jradiance.common.COLORS.COLOR;
import jradiance.rt.RPAINT.PNODE;

/**
 *
 * @author arwillis
 */
public class RVIEW {
    /*
     *  rview.c - routines and variables for interactive view generation.
     *
     *  External symbols declared in rpaint.h
     */

//#define	 CTRL(c)	((c)-'@')
    static void quit( /* quit program */
            int code) {
        if (RAYPWIN.ray_pnprocs > 0) /* close children if any */ {
            RAYPWIN.ray_pclose(0);
        } else if (RAYPWIN.ray_pnprocs == 0) {	/* in parent */
//		devclose();

        }
        System.exit(code);
    }

    static void devopen( /* open device driver */
            String dname) {
        String id;
        int i;

        id = RAYCALLS.octname != null ? RAYCALLS.octname : RVMAIN.progname;
        /* check device table */
        for (i = 0; DEVTABLE.devtable[i].name != null; i++) {
            if (DEVCOMM.strcmp(dname.toCharArray(), DEVTABLE.devtable[i].name.toCharArray()) == 0) {
                RVMAIN.dev = DEVTABLE.devtable[i].init(dname, id);
                //if ((dev = (*devtable[i].init)(dname, id)) == NULL) {
                //	sprintf(errmsg, "cannot initialize %s", dname);
                //	error(USER, errmsg);
                //} else
                return;
            }
        }
//						/* not there, try exec */
//	if ((dev = comm_init(dname, id)) == NULL) {
//		sprintf(errmsg, "cannot start device \"%s\"", dname);
//		error(USER, errmsg);
//	}
    }

    void devclose() /* close our device */ {
//	if (dev != NULL)
//		(*dev.close)();
//	dev = NULL;
    }

    void printdevices() /* print list of output devices */ {
//	int  i;
//
//	for (i = 0; devtable[i].name; i++)
//		printf("%-16s # %s\n", devtable[i].name, devtable[i].descrip);
    }

    public static void rview() throws IOException /* do a view */ {
        String buf = null;

        devopen(RVMAIN.dvcname);		/* open device */
        RV3.newimage(null);			/* start image */

        for (;;) {			/* quit in command() */
            while (RVMAIN.hresolu <= 1 << RVMAIN.pdepth && RVMAIN.vresolu <= 1 << RVMAIN.pdepth) {
                command("done: ");
            }
//		errno = 0;
            if (RVMAIN.hresolu <= RVMAIN.psample << RVMAIN.pdepth && RVMAIN.vresolu <= RVMAIN.psample << RVMAIN.pdepth) {
                buf = String.format("%d sampling...\n", 1 << RVMAIN.pdepth);
                RVMAIN.dev.comout(buf.toCharArray());
                rsample();
            } else {
                buf = String.format("%d refining...\n", 1 << RVMAIN.pdepth);
                RVMAIN.dev.comout(buf.toCharArray());
                refine(RVMAIN.ptrunk, RVMAIN.pdepth + 1);
            }
            if (RVMAIN.dev.inpready != 0) /* noticed some input */ {
                command(": ");
            } else /* finished this depth */ {
                RVMAIN.pdepth++;
            }
        }
    }

//int badcom(char[] s) {
//    return DEVCOMM.strncmp(s, s, pd)
//}
    static void command( /* get/execute command */
            String prompt) throws IOException {
//#define	 badcom(s)	strncmp(s, inpbuf, args-inpbuf-1)
        char[] inpbuf = new char[256];
        char[] args;
//again:
//	dev.comin(inpbuf, prompt);		/* get command + arguments */
//	for (args = inpbuf; *args && *args != ' '; args++)
//		;
//	if (args != null) *args++ = '\0';
//	else *++args = '\0';

        if (RV3.waitrays() < 0) /* clear ray queue */ {
            quit(1);
        }

//	switch (inpbuf[0]) {
//	case 'f':				/* new frame (|focus|free) */
//		if (badcom("frame")) {
//			if (badcom("focus")) {
//				if (badcom("free"))
//					goto commerr;
//				free_objmem();
//				break;
//			}
//			getfocus(args);
//			break;
//		}
//		getframe(args);
//		break;
//	case 'v':				/* view */
//		if (badcom("view"))
//			goto commerr;
//		getview(args);
//		break;
//	case 'l':				/* last view */
//		if (badcom("last"))
//			goto commerr;
//		lastview(args);
//		break;
//	case 'V':				/* save view */
//		if (badcom("V"))
//			goto commerr;
//		saveview(args);
//		break;
//	case 'L':				/* load view */
//		if (badcom("L"))
//			goto commerr;
//		loadview(args);
//		break;
//	case 'e':				/* exposure */
//		if (badcom("exposure"))
//			goto commerr;
//		getexposure(args);
//		break;
//	case 's':				/* set a parameter */
//		if (badcom("set")) {
//#ifdef	SIGTSTP
//			if (!badcom("stop"))
//				goto dostop;
//#endif
//			goto commerr;
//		}
//		setparam(args);
//		break;
//	case 'n':				/* new picture */
//		if (badcom("new"))
//			goto commerr;
//		newimage(args);
//		break;
//	case 't':				/* trace a ray */
//		if (badcom("trace"))
//			goto commerr;
//		traceray(args);
//		break;
//	case 'a':				/* aim camera */
//		if (badcom("aim"))
//			goto commerr;
//		getaim(args);
//		break;
//	case 'm':				/* move camera (or memstats) */
//		if (badcom("move"))
//			goto commerr;
//		getmove(args);
//		break;
//	case 'r':				/* rotate/repaint */
//		if (badcom("rotate")) {
//			if (badcom("repaint")) {
//				if (badcom("redraw"))
//					goto commerr;
//				redraw();
//				break;
//			}
//			getrepaint(args);
//			break;
//		}
//		getrotate(args);
//		break;
//	case 'p':				/* pivot view */
//		if (badcom("pivot")) {
//			if (badcom("pause"))
//				goto commerr;
//			goto again;
//		}
//		getpivot(args);
//		break;
//	case CTRL('R'):				/* redraw */
//		redraw();
//		break;
//	case 'w':				/* write */
//		if (badcom("write"))
//			goto commerr;
//		writepict(args);
//		break;
//	case 'q':				/* quit */
//		if (badcom("quit"))
//			goto commerr;
//		quit(0);
//	case CTRL('C'):				/* interrupt */
//		goto again;
//#ifdef	SIGTSTP
//	case CTRL('Z'):;			/* stop */
//dostop:
//		devclose();
//		kill(0, SIGTSTP);
//		/* pc stops here */
//		devopen(dvcname);
//		redraw();
//		break;
//#endif
//	case '\0':				/* continue */
//		break;
//	default:;
//commerr:
//		if (iscntrl(inpbuf[0]))
//			sprintf(errmsg, "^%c: unknown control",
//					inpbuf[0]|0100);
//		else
//			sprintf(errmsg, "%s: unknown command", inpbuf);
//		error(COMMAND, errmsg);
//		break;
//	}
//#undef	badcom
    }

    static void rsample() throws IOException /* sample the image */ {
        int xsiz, ysiz, y;
        PNODE p;
        PNODE[] pl;
        int x;
        /*
         *     We initialize the bottom row in the image at our current
         * resolution.	During sampling, we check super-pixels to the
         * right and above by calling bigdiff().  If there is a significant
         * difference, we subsample the super-pixels.  The testing process
         * includes initialization of the next row.
         */
        xsiz = (int) (((long) (RVMAIN.pframe.r - RVMAIN.pframe.l) << RVMAIN.pdepth) + RVMAIN.hresolu - 1) / RVMAIN.hresolu;
        ysiz = (int) (((long) (RVMAIN.pframe.u - RVMAIN.pframe.d) << RVMAIN.pdepth) + RVMAIN.vresolu - 1) / RVMAIN.vresolu;
//	pl = (PNODE **)malloc(xsiz*sizeof(PNODE *));
        pl = new PNODE[xsiz];
        if (pl == null) {
            return;
        }
        /*
         * Initialize the bottom row.
         */
        pl[0] = RV3.findrect(RVMAIN.pframe.l, RVMAIN.pframe.d, RVMAIN.ptrunk, RVMAIN.pdepth);
        for (x = 1; x < xsiz; x++) {
            pl[x] = RV3.findrect(RVMAIN.pframe.l + ((x * RVMAIN.hresolu) >> RVMAIN.pdepth),
                    RVMAIN.pframe.d, RVMAIN.ptrunk, RVMAIN.pdepth);
        }
        /* sample the image */
        for (y = 0; /* y < ysiz */; y++) {
            for (x = 0; x < xsiz - 1; x++) {
                if (RVMAIN.dev.inpready != 0) {
//				goto escape;
                    pl = null;
                    return;
                }
                /*
                 * Test super-pixel to the right.
                 */
                if (pl[x] != pl[x + 1] && COLOR.bigdiff(pl[x].v,
                        pl[x + 1].v, RVMAIN.maxdiff) != 0) {
                    refine(pl[x], 1);
                    refine(pl[x + 1], 1);
                }
            }
            if (y >= ysiz - 1) {
                break;
            }
            for (x = 0; x < xsiz; x++) {
                if (RVMAIN.dev.inpready != 0) {
//				goto escape;
                    pl = null;
                    return;
                }
                /*
                 * Find super-pixel at this position in next row.
                 */
                p = RV3.findrect(RVMAIN.pframe.l + ((x * RVMAIN.hresolu) >> RVMAIN.pdepth),
                        RVMAIN.pframe.d + (((y + 1) * RVMAIN.vresolu) >> RVMAIN.pdepth),
                        RVMAIN.ptrunk, RVMAIN.pdepth);
                /*
                 * Test super-pixel in next row.
                 */
                if (pl[x] != p && COLOR.bigdiff(pl[x].v, p.v, RVMAIN.maxdiff) != 0) {
                    refine(pl[x], 1);
                    refine(p, 1);
                }
                /*
                 * Copy into super-pixel array.
                 */
                pl[x] = p;
            }
        }
        pl = null;
//escape:
//	free((void *)pl);
    }

    static int refine( /* refine a node */
            PNODE p,
            int pd) throws IOException {
        int growth = 0;
        int mx, my;

        if (RVMAIN.dev.inpready != 0) /* quit for input */ {
            return (0);
        }

        if (pd <= 0) /* depth limit */ {
            return (0);
        }

        mx = (p.xmin + p.xmax) >> 1;
        my = (p.ymin + p.ymax) >> 1;
        growth = 0;

        if (p.kid == null) {			/* subdivide */

            if ((p.kid = RPAINT.newptree()) == null) {
                return (0);
            }

            p.kid[RPAINT.UR].xmin = (short) mx;
            p.kid[RPAINT.UR].ymin = (short) my;
            p.kid[RPAINT.UR].xmax = p.xmax;
            p.kid[RPAINT.UR].ymax = p.ymax;
            p.kid[RPAINT.UL].xmin = p.xmin;
            p.kid[RPAINT.UL].ymin = (short) my;
            p.kid[RPAINT.UL].xmax = (short) mx;
            p.kid[RPAINT.UL].ymax = p.ymax;
            p.kid[RPAINT.DR].xmin = (short) mx;
            p.kid[RPAINT.DR].ymin = p.ymin;
            p.kid[RPAINT.DR].xmax = p.xmax;
            p.kid[RPAINT.DR].ymax = (short) my;
            p.kid[RPAINT.DL].xmin = p.xmin;
            p.kid[RPAINT.DL].ymin = p.ymin;
            p.kid[RPAINT.DL].xmax = (short) mx;
            p.kid[RPAINT.DL].ymax = (short) my;
            /*
             *  The following paint order can leave a black pixel
             *  if redraw() is called in (*dev.paintr)().
             */
            if (p.x >= mx && p.y >= my) {
                RV3.pcopy(p, p.kid[RPAINT.UR]);
                RV3.recolor(p);
            } else if (RV3.paint(p.kid[RPAINT.UR]) < 0) {
                quit(1);
            }
            if (p.x < mx && p.y >= my) {
                RV3.pcopy(p, p.kid[RPAINT.UL]);
                RV3.recolor(p);
            } else if (RV3.paint(p.kid[RPAINT.UL]) < 0) {
                quit(1);
            }
            if (p.x >= mx && p.y < my) {
                RV3.pcopy(p, p.kid[RPAINT.DR]);
                RV3.recolor(p);
            } else if (RV3.paint(p.kid[RPAINT.DR]) < 0) {
                quit(1);
            }
            if (p.x < mx && p.y < my) {
                RV3.pcopy(p, p.kid[RPAINT.DL]);
                RV3.recolor(p);
            } else if (RV3.paint(p.kid[RPAINT.DL]) < 0) {
                quit(1);
            }

            growth++;
        }
        /* do children */
        if (mx > RVMAIN.pframe.l) {
            if (my > RVMAIN.pframe.d) {
                growth += refine(p.kid[RPAINT.DL], pd - 1);
            }
            if (my < RVMAIN.pframe.u) {
                growth += refine(p.kid[RPAINT.UL], pd - 1);
            }
        }
        if (mx < RVMAIN.pframe.r) {
            if (my > RVMAIN.pframe.d) {
                growth += refine(p.kid[RPAINT.DR], pd - 1);
            }
            if (my < RVMAIN.pframe.u) {
                growth += refine(p.kid[RPAINT.UR], pd - 1);
            }
        }
        return (growth);
    }
}
