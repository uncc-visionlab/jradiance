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
import jradiance.common.COLORS.COLR;
import jradiance.common.FVECT;
import jradiance.common.IMAGE;
import jradiance.common.VIEW;
import jradiance.rt.RPAINT.PNODE;
import jradiance.rt.RPAINT.RECT;

/**
 *
 * @author arwillis
 */
public class RV3 {
    /*
     *  rv3.c - miscellaneous routines for rview.
     *
     *  External symbols declared in rpaint.h
     */
//
//#ifndef WFLUSH
    public static final int WFLUSH = 64;		/* flush after this many primary rays */
//#endif
//#ifndef WFLUSH1

    public static final int WFLUSH1 = 512;		/* or this many total rays */
//#endif

//#ifdef  SMLFLT
//#define  sscanvec(s,v)	(sscanf(s,"%f %f %f",v,v+1,v+2)==3)
//#else
//#define  sscanvec(s,v)	(sscanf(s,"%lf %lf %lf",v,v+1,v+2)==3)
//#endif
    static long niflush;		/* flushes since newimage() */


    int getrect( /* get a box */
            char[] s,
            RECT r) {
        int x0 = 0, y0 = 0, x1 = 0, y1 = 0;

        if (s[0] != 0 && DEVCOMM.strncmp(s, "all".toCharArray(), DEVCOMM.strlen(s)) == 0) {
            r.l = r.d = 0;
            r.r = (short) RVMAIN.hresolu;
            r.u = (short) RVMAIN.vresolu;
            return (0);
        }
//	if (sscanf(s, "%d %d %d %d", &x0, &y0, &x1, &y1) != 4) {
//		if (dev.getcur == NULL)
//			return(-1);
//		(*dev.comout)("Pick first corner\n");
//		if ((*dev.getcur)(&x0, &y0) == ABORT)
//			return(-1);
//		(*dev.comout)("Pick second corner\n");
//		if ((*dev.getcur)(&x1, &y1) == ABORT)
//			return(-1);
//	}
        if (x0 < x1) {
            r.l = (short) x0;
            r.r = (short) x1;
        } else {
            r.l = (short) x1;
            r.r = (short) x0;
        }
        if (y0 < y1) {
            r.d = (short) y0;
            r.u = (short) y1;
        } else {
            r.d = (short) y1;
            r.u = (short) y0;
        }
        if (r.l < 0) {
            r.l = 0;
        }
        if (r.d < 0) {
            r.d = 0;
        }
        if (r.r > RVMAIN.hresolu) {
            r.r = (short) RVMAIN.hresolu;
        }
        if (r.u > RVMAIN.vresolu) {
            r.u = (short) RVMAIN.vresolu;
        }
        if (r.l > r.r) {
            r.l = r.r;
        }
        if (r.d > r.u) {
            r.d = r.u;
        }
        return (0);
    }

    int getinterest( /* get area of interest */
            char[] s,
            int direc,
            FVECT vec,
            double[] mp) {
        int x, y;
        RAY thisray;
        int i;

//	if (sscanf(s, "%lf", mp) != 1)
//		*mp = 1.0;
//	else if (*mp < -FTINY)		/* negative zoom is reduction */
//		*mp = -1.0 / *mp;
//	else if (*mp <= FTINY) {	/* too small */
//		error(COMMAND, "illegal magnification");
//		return(-1);
//	}
//	if (!sscanvec(sskip(s), vec)) {
//		if (dev.getcur == NULL)
//			return(-1);
//		(*dev.comout)("Pick view center\n");
//		if ((*dev.getcur)(&x, &y) == ABORT)
//			return(-1);
//		if ((thisray.rmax = viewray(thisray.rorg, thisray.rdir,
//			&ourview, (x+.5)/hresolu, (y+.5)/vresolu)) < -FTINY) {
//			error(COMMAND, "not on image");
//			return(-1);
//		}
//		if (!direc || ourview.type == VT_PAR) {
//			rayorigin(&thisray, PRIMARY, NULL, NULL);
//			if (!localhit(&thisray, &thescene)) {
//				error(COMMAND, "not a local object");
//				return(-1);
//			}
//		}
//		if (direc)
//			if (ourview.type == VT_PAR)
//				for (i = 0; i < 3; i++)
//					vec[i] = thisray.rop[i] - ourview.vp[i];
//			else
//				VCOPY(vec, thisray.rdir);
//		else
//			VCOPY(vec, thisray.rop);
//	} else if (direc) {
//		for (i = 0; i < 3; i++)
//			vec[i] -= ourview.vp[i];
//		if (normalize(vec) == 0.0) {
//			error(COMMAND, "point at view origin");
//			return(-1);
//		}
//	}
        return (0);
    }
    static COLOR gcol = new COLOR();

    static COLOR /* keep consistent with COLOR typedef */ greyof( /* convert color to greyscale */
            COLOR col) {
        double b;

        b = COLOR.bright(col);
        gcol.setcolor((float) b, (float) b, (float) b);
        return (gcol);
    }

    static void recolor( /* recolor the given node */
            PNODE p) throws IOException {
        while (p.kid != null) {		/* need to propogate down */
            int mx = (p.xmin + p.xmax) >> 1;
            int my = (p.ymin + p.ymax) >> 1;
            int ki;
            if (p.x >= mx) {
                ki = (p.y >= my) ? RPAINT.UR : RPAINT.DR;
            } else {
                ki = (p.y >= my) ? RPAINT.UL : RPAINT.DL;
            }
            pcopy(p, p.kid[ki]);
            p = p.kid[ki];
        }

        RVMAIN.dev.paintr(RVMAIN.greyscale != 0 ? greyof(p.v) : p.v,
                p.xmin, p.ymin, p.xmax, p.ymax);
    }
    static RAY thisray = new RAY();

    static int paint( /* compute and paint a rectangle */
            PNODE p) throws IOException {
//	static RAY  thisray;
        double h, v;

        if ((p.xmax <= p.xmin) | (p.ymax <= p.ymin)) {	/* empty */
            p.x = p.xmin;
            p.y = p.ymin;
            p.v.setcolor(0.0f, 0.0f, 0.0f);
            return (0);
        }
        /* jitter ray direction */
        h = p.xmin + (p.xmax - p.xmin) * RAYCALLS.frandom();
        v = p.ymin + (p.ymax - p.ymin) * RAYCALLS.frandom();
        p.x = (short) h;
        p.y = (short) v;
        if ((thisray.rmax = IMAGE.viewray(thisray.rorg, thisray.rdir, RVMAIN.ourview,
                h / RVMAIN.hresolu, v / RVMAIN.vresolu)) < -FVECT.FTINY) {
            thisray.rcol.setcolor(0.0f, 0.0f, 0.0f);
        } else if (RVMAIN.nproc == 1) {		/* immediate mode */
            RAYCALLS.ray_trace(thisray);
        } else {				/* queuing mode */
            int rval;
            RAYTRACE.rayorigin(thisray, RAY.PRIMARY, null, null);
//		thisray.rno = (long)p;
//              thisray.rno = p.hashCode();
            rval = RAYPWIN.ray_pqueue(thisray);
            if (rval == 0) {
                return (0);
            }
            if (rval < 0) {
                return (-1);
            }
            /* get node for returned ray */
//		p = (PNODE *)thisray.rno;
        }

        COLOR.copycolor(p.v, thisray.rcol);
        p.v.scalecolor(RVMAIN.exposure);

        recolor(p);				/* paint it */

//	if (dev.flush != null) {		/* shall we check for input? */
//		static RNUMBER	lastflush = 0;
//		RNUMBER		counter = raynum;
//		int		flushintvl;
//		if (nproc == 1) {
//			counter = nrays;
//			flushintvl = WFLUSH1;
//		} else if (ambounce == 0)
//			flushintvl = nproc*WFLUSH;
//		else if (niflush < WFLUSH)
//			flushintvl = nproc*niflush/(ambounce+1);
//		else
//			flushintvl = nproc*WFLUSH/(ambounce+1);
//		if (lastflush > counter)
//			lastflush = 0;		/* counter wrapped */
//
//		if (counter - lastflush >= flushintvl) {
//			lastflush = counter;
//			(*dev.flush)();
//			niflush++;
//		}
//	}
        return (1);
    }

    static int waitrays() throws IOException /* finish up pending rays */ {
        int nwaited = 0;
        int rval;
        RAY raydone = new RAY();

        if (RVMAIN.nproc <= 1) /* immediate mode? */ {
            return (0);
        }
        while ((rval = RAYPWIN.ray_presult(raydone, 0)) > 0) {
            PNODE p = null;
//		PNODE  p = (PNODE *)raydone.rno;
            COLOR.copycolor(p.v, raydone.rcol);
            p.v.scalecolor(RVMAIN.exposure);
            recolor(p);
            nwaited++;
        }
        if (rval < 0) {
            return (-1);
        }
        return (nwaited);
    }

    public static void newimage( /* start a new image */
            char[] s) throws IOException {
//	extern int	ray_pnprocs;
        int newnp = 0;
        /* # rendering procs arg? */
//	if (s != null)
//		sscanf(s, "%d", &newnp);
						/* free old image */
//	freepkids(&ptrunk);
//						/* compute resolution */
        RVMAIN.hresolu = RVMAIN.dev.xsiz;
        RVMAIN.vresolu = RVMAIN.dev.ysiz;
	IMAGE.normaspect(VIEW.viewaspect(RVMAIN.ourview));
        RVMAIN.ptrunk.xmin = RVMAIN.ptrunk.ymin = RVMAIN.pframe.l = RVMAIN.pframe.d = 0;
        RVMAIN.ptrunk.xmax = RVMAIN.pframe.r = (short) RVMAIN.hresolu;
        RVMAIN.ptrunk.ymax = RVMAIN.pframe.u = (short) RVMAIN.vresolu;
        RVMAIN.pdepth = 0;
        /* clear device */
        RVMAIN.dev.clear(RVMAIN.hresolu, RVMAIN.vresolu);

        if (RVMAIN.newparam != 0) {				/* (re)start rendering procs */
            if (RAYPWIN.ray_pnprocs > 0) {
                RAYPWIN.ray_pclose(0);
            }
            if (newnp > 0) {
                RVMAIN.nproc = newnp;
            }
            if (RVMAIN.nproc > 1) {
                RAYPWIN.ray_popen(RVMAIN.nproc);
            }
            RVMAIN.newparam = 0;
        } else if ((newnp > 0) & (newnp != RVMAIN.nproc)) {
            if (newnp == 1) /* change # rendering procs */ {
                RAYPWIN.ray_pclose(0);
            } else if (newnp < RAYPWIN.ray_pnprocs) {
                RAYPWIN.ray_pclose(RAYPWIN.ray_pnprocs - newnp);
            } else {
                RAYPWIN.ray_popen(newnp - RAYPWIN.ray_pnprocs);
            }
            RVMAIN.nproc = newnp;
        }
        niflush = 0;				/* get first value */
        paint(RVMAIN.ptrunk);
    }

    void redraw() throws IOException /* redraw the image */ {
        RVMAIN.dev.clear(RVMAIN.hresolu, RVMAIN.vresolu);
        RVMAIN.dev.comout("redrawing...\n".toCharArray());
        repaint(0, 0, RVMAIN.hresolu, RVMAIN.vresolu);
        RVMAIN.dev.comout("\n".toCharArray());
    }

    void repaint( /* repaint a region */
            int xmin,
            int ymin,
            int xmax,
            int ymax) {
        RECT reg = new RECT();

        reg.l = (short) xmin;
        reg.r = (short) xmax;
        reg.d = (short) ymin;
        reg.u = (short) ymax;

        paintrect(RVMAIN.ptrunk, reg);
    }

    void paintrect( /* paint picture rectangle */
            PNODE p,
            RECT r) {
        int mx, my;

//	if (p.xmax - p.xmin <= 0 || p.ymax - p.ymin <= 0)
//		return;
//
//	if (p.kid == NULL) {
//		(*dev.paintr)(greyscale?greyof(p.v):p.v,
//			p.xmin, p.ymin, p.xmax, p.ymax);	/* do this */
//		return;
//	}
//	mx = (p.xmin + p.xmax) >> 1;				/* do kids */
//	my = (p.ymin + p.ymax) >> 1;
//	if (mx > r.l) {
//		if (my > r.d)
//			paintrect(p.kid+DL, r);
//		if (my < r.u)
//			paintrect(p.kid+UL, r);
//	}
//	if (mx < r.r) {
//		if (my > r.d)
//			paintrect(p.kid+DR, r);
//		if (my < r.u)
//			paintrect(p.kid+UR, r);
//	}
    }

    static PNODE findrect( /* find a rectangle */
            int x,
            int y,
            PNODE p,
            int pd) {
        int mx, my;

	while (p.kid != null && pd-- != 0) {

		mx = (p.xmin + p.xmax) >> 1;
		my = (p.ymin + p.ymax) >> 1;

		if (x < mx) {
			if (y < my) {
				p = p.kid[RPAINT.DL];
			} else {
				p = p.kid[RPAINT.UL];
			}
		} else {
			if (y < my) {
				p = p.kid[RPAINT.DR];
			} else {
				p = p.kid[RPAINT.UR];
			}
		}
	}
        return (p);
    }

    void compavg( /* recompute averages */
            PNODE p) {
        int i, navg;

//	if (p.kid == NULL)
//		return;
//
//	setcolor(p.v, .0, .0, .0);
//	navg = 0;
//	for (i = 0; i < 4; i++) {
//		if (p.kid[i].xmin >= p.kid[i].xmax) continue;
//		if (p.kid[i].ymin >= p.kid[i].ymax) continue;
//		compavg(p.kid+i);
//		addcolor(p.v, p.kid[i].v);
//		navg++;
//	}
//	if (navg > 1)
//		scalecolor(p.v, 1./navg);
    }

    void scalepict( /* scale picture values */
            PNODE p,
            double sf) {
//	scalecolor(p.v, sf);		/* do this node */
//
//	if (p.kid == NULL)
//		return;
//					/* do children */
//	scalepict(p.kid+DL, sf);
//	scalepict(p.kid+DR, sf);
//	scalepict(p.kid+UL, sf);
//	scalepict(p.kid+UR, sf);
    }

    void getpictcolrs( /* get scanline from picture */
            int yoff,
            COLR scan,
            PNODE p,
            int xsiz,
            int ysiz) {
        int mx;
        int my;

//	if (p.kid == NULL) {			/* do this node */
//		setcolr(scan[0], colval(p.v,RED),
//				colval(p.v,GRN),
//				colval(p.v,BLU));
//		for (mx = 1; mx < xsiz; mx++)
//			copycolr(scan[mx], scan[0]);
//		return;
//	}
						/* do kids */
        mx = xsiz >> 1;
        my = ysiz >> 1;
//	if (yoff < my) {
//		getpictcolrs(yoff, scan, p.kid+DL, mx, my);
//		getpictcolrs(yoff, scan+mx, p.kid+DR, xsiz-mx, my);
//	} else {
//		getpictcolrs(yoff-my, scan, p.kid+UL, mx, ysiz-my);
//		getpictcolrs(yoff-my, scan+mx, p.kid+UR, xsiz-mx, ysiz-my);
//	}
    }

    void freepkids( /* free pnode's children */
            PNODE p) {
//	if (p.kid == NULL)
//		return;
//	freepkids(p.kid+DL);
//	freepkids(p.kid+DR);
//	freepkids(p.kid+UL);
//	freepkids(p.kid+UR);
//	free((void *)p.kid);
//	p.kid = NULL;
    }

    void newview( /* change viewing parameters */
            VIEW vp) {
        char[] err;

//	if ((err = setview(vp)) != NULL) {
//		sprintf(errmsg, "view not set - %s", err);
//		error(COMMAND, errmsg);
//	} else if (memcmp((char *)vp, (char *)&ourview, sizeof(VIEW))) {
//		oldview = ourview;
//		ourview = *vp;
//		newimage(NULL);
//	}
    }

    void moveview( /* move viewpoint */
            double angle,
            double elev,
            double mag,
            FVECT vc) {
        double d;
        FVECT v1 = new FVECT();
        VIEW nv = RVMAIN.ourview;
        int i;

//	spinvector(nv.vdir, ourview.vdir, ourview.vup, angle*(PI/180.));
//	if (elev != 0.0) {
//		fcross(v1, ourview.vup, nv.vdir);
//		normalize(v1);
//		spinvector(nv.vdir, nv.vdir, v1, elev*(PI/180.));
//	}
//	if (nv.type == VT_PAR) {
//		nv.horiz /= mag;
//		nv.vert /= mag;
//		d = 0.0;			/* don't move closer */
//		for (i = 0; i < 3; i++)
//			d += (vc[i] - ourview.vp[i])*ourview.vdir[i];
//	} else {
//		d = sqrt(dist2(ourview.vp, vc)) / mag;
//		if (nv.vfore > FTINY) {
//			nv.vfore += d - d*mag;
//			if (nv.vfore < 0.0) nv.vfore = 0.0;
//		}
//		if (nv.vaft > FTINY) {
//			nv.vaft += d - d*mag;
//			if (nv.vaft <= nv.vfore) nv.vaft = 0.0;
//		}
//		nv.vdist /= mag;
//	}
//	for (i = 0; i < 3; i++)
//		nv.vp[i] = vc[i] - d*nv.vdir[i];
//	newview(&nv);
    }

    static void pcopy( /* copy paint node p1 into p2 */
            PNODE p1,
            PNODE p2) {
        COLOR.copycolor(p2.v, p1.v);
        p2.x = p1.x;
        p2.y = p1.y;
    }

    void zoomview( /* zoom in or out */
            VIEW vp,
            double zf) {
        switch (vp.type) {
            case VIEW.VT_PAR:				/* parallel view */
                vp.horiz /= zf;
                vp.vert /= zf;
                return;
            case VIEW.VT_ANG:				/* angular fisheye */
                vp.horiz /= zf;
                if (vp.horiz > 360.) {
                    vp.horiz = 360.;
                }
                vp.vert /= zf;
                if (vp.vert > 360.) {
                    vp.vert = 360.;
                }
                return;
            case VIEW.VT_PLS:				/* planisphere fisheye */
                vp.horiz = Math.sin((Math.PI / 180. / 2.) * vp.horiz)
                        / (1.0 + Math.cos((Math.PI / 180. / 2.) * vp.horiz)) / zf;
                vp.horiz *= vp.horiz;
                vp.horiz = (2. * 180. / Math.PI) * Math.acos((1. - vp.horiz)
                        / (1. + vp.horiz));
                vp.vert = Math.sin((Math.PI / 180. / 2.) * vp.vert)
                        / (1.0 + Math.cos((Math.PI / 180. / 2.) * vp.vert)) / zf;
                vp.vert *= vp.vert;
                vp.vert = (2. * 180. / Math.PI) * Math.acos((1. - vp.vert)
                        / (1. + vp.vert));
                return;
            case VIEW.VT_CYL:				/* cylindrical panorama */
                vp.horiz /= zf;
                if (vp.horiz > 360.) {
                    vp.horiz = 360.;
                }
                vp.vert = Math.atan(Math.tan(vp.vert * (Math.PI / 180. / 2.)) / zf) / (Math.PI / 180. / 2.);
                return;
            case VIEW.VT_PER:				/* perspective view */
                vp.horiz = Math.atan(Math.tan(vp.horiz * (Math.PI / 180. / 2.)) / zf)
                        / (Math.PI / 180. / 2.);
                vp.vert = Math.atan(Math.tan(vp.vert * (Math.PI / 180. / 2.)) / zf)
                        / (Math.PI / 180. / 2.);
                return;
            case VIEW.VT_HEM:				/* hemispherical fisheye */
                vp.horiz = Math.sin(vp.horiz * (Math.PI / 180. / 2.)) / zf;
                if (vp.horiz >= 1.0 - FVECT.FTINY) {
                    vp.horiz = 180.;
                } else {
                    vp.horiz = Math.asin(vp.horiz) / (Math.PI / 180. / 2.);
                }
                vp.vert = Math.sin(vp.vert * (Math.PI / 180. / 2.)) / zf;
                if (vp.vert >= 1.0 - FVECT.FTINY) {
                    vp.vert = 180.;
                } else {
                    vp.vert = Math.asin(vp.vert) / (Math.PI / 180. / 2.);
                }
                return;
        }
    }
}
