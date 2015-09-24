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
import java.io.OutputStream;
import jradiance.rt.RPICT;
import jradiance.rt.RPMAIN;
import jradiance.rt.RVMAIN;

/**
 *
 * @author arwillis
 */
public class IMAGE {
    /*
     *  image.c - routines for image generation.
     *
     *  External symbols declared in view.h
     */

    boolean FEQ(double x, double y) {
        return (Math.abs((x) - (y)) <= FVECT.FTINY);
    }

    boolean VEQ(FVECT v, FVECT w) {
        return (FEQ((v).data[0], (w).data[0]) && FEQ((v).data[1], (w).data[1])
                && FEQ((v).data[2], (w).data[2]));
    }
    public static VIEW stdview = new VIEW();		/* default view parameters */

//static gethfunc gethview;
    static String ill_horiz = "illegal horizontal view size";
    static String ill_vert = "illegal vertical view size";

    public static String setview( /* set hvec and vvec, return message on error */
            VIEW v) {

        if (v.vaft < -FVECT.FTINY || (v.vaft > FVECT.FTINY && v.vaft <= v.vfore)) {
            return ("illegal fore/aft clipping plane");
        }

        if (v.vdist <= FVECT.FTINY) {
            return ("illegal view distance");
        }
        v.vdist *= FVECT.normalize(v.vdir);		/* normalize direction */
        if (v.vdist == 0.0) {
            return ("zero view direction");
        }

        if (FVECT.normalize(v.vup) == 0.0) /* normalize view up */ {
            return ("zero view up vector");
        }

        FVECT.fcross(v.hvec, v.vdir, v.vup);	/* compute horiz dir */

        if (FVECT.normalize(v.hvec) == 0.0) {
            return ("view up parallel to view direction");
        }

        FVECT.fcross(v.vvec, v.hvec, v.vdir);	/* compute vert dir */

        if (v.horiz <= FVECT.FTINY) {
            return (ill_horiz);
        }
        if (v.vert <= FVECT.FTINY) {
            return (ill_vert);
        }

        switch (v.type) {
            case VIEW.VT_PAR:				/* parallel view */
                v.hn2 = v.horiz;
                v.vn2 = v.vert;
                break;
            case VIEW.VT_PER:				/* perspective view */
                if (v.horiz >= 180.0 - FVECT.FTINY) {
                    return (ill_horiz);
                }
                if (v.vert >= 180.0 - FVECT.FTINY) {
                    return (ill_vert);
                }
                v.hn2 = 2.0 * Math.tan(v.horiz * (Math.PI / 180.0 / 2.0));
                v.vn2 = 2.0 * Math.tan(v.vert * (Math.PI / 180.0 / 2.0));
                break;
            case VIEW.VT_CYL:				/* cylindrical panorama */
                if (v.horiz > 360.0 + FVECT.FTINY) {
                    return (ill_horiz);
                }
                if (v.vert >= 180.0 - FVECT.FTINY) {
                    return (ill_vert);
                }
                v.hn2 = v.horiz * (Math.PI / 180.0);
                v.vn2 = 2.0 * Math.tan(v.vert * (Math.PI / 180.0 / 2.0));
                break;
            case VIEW.VT_ANG:				/* angular fisheye */
                if (v.horiz > 360.0 + FVECT.FTINY) {
                    return (ill_horiz);
                }
                if (v.vert > 360.0 + FVECT.FTINY) {
                    return (ill_vert);
                }
                v.hn2 = v.horiz * (Math.PI / 180.0);
                v.vn2 = v.vert * (Math.PI / 180.0);
                break;
            case VIEW.VT_HEM:				/* hemispherical fisheye */
                if (v.horiz > 180.0 + FVECT.FTINY) {
                    return (ill_horiz);
                }
                if (v.vert > 180.0 + FVECT.FTINY) {
                    return (ill_vert);
                }
                v.hn2 = 2.0 * Math.sin(v.horiz * (Math.PI / 180.0 / 2.0));
                v.vn2 = 2.0 * Math.sin(v.vert * (Math.PI / 180.0 / 2.0));
                break;
            case VIEW.VT_PLS:				/* planispheric fisheye */
                if (v.horiz >= 360.0 - FVECT.FTINY) {
                    return (ill_horiz);
                }
                if (v.vert >= 360.0 - FVECT.FTINY) {
                    return (ill_vert);
                }
                v.hn2 = 2. * Math.sin(v.horiz * (Math.PI / 180.0 / 2.0))
                        / (1.0 + Math.cos(v.horiz * (Math.PI / 180.0 / 2.0)));
                v.vn2 = 2. * Math.sin(v.vert * (Math.PI / 180.0 / 2.0))
                        / (1.0 + Math.cos(v.vert * (Math.PI / 180.0 / 2.0)));
                break;
            default:
                return ("unknown view type");
        }
        if (v.type != VIEW.VT_ANG && v.type != VIEW.VT_PLS) {
            if (v.type != VIEW.VT_CYL) {
                v.hvec.data[0] *= v.hn2;
                v.hvec.data[1] *= v.hn2;
                v.hvec.data[2] *= v.hn2;
            }
            v.vvec.data[0] *= v.vn2;
            v.vvec.data[1] *= v.vn2;
            v.vvec.data[2] *= v.vn2;
        }
        v.hn2 *= v.hn2;
        v.vn2 *= v.vn2;

        return (null);
    }

    public static void normaspect( /* fix pixel aspect or resolution */
            double va /* view aspect ratio */) {
        double ap = RVMAIN.dev.pixaspect;			/* pixel aspect in (or out if 0) */
        int xp = RVMAIN.hresolu;
        int yp = RVMAIN.vresolu;		/* x and y resolution in (or out if *ap!=0) */
        if (ap <= FVECT.FTINY) {
            ap = va * xp / yp;		/* compute pixel aspect */
        } else if (va * xp > ap * yp) {
            xp = (int) (yp / va * ap + .5);	/* reduce x resolution */
        } else {
            yp = (int) (xp * va / ap + .5);	/* reduce y resolution */
        }
        RVMAIN.hresolu = xp;
        RVMAIN.vresolu = yp;
        RVMAIN.dev.pixaspect = ap;
        RPICT.pa = ap;
        RPICT.hres = xp;
        RPICT.vres = yp;
    }

    public static double viewray( /* compute ray origin and direction */
            FVECT orig,
            FVECT direc,
            VIEW v,
            double x,
            double y) {
        double d, z;

        x += v.hoff - 0.5;
        y += v.voff - 0.5;

        switch (v.type) {
            case VIEW.VT_PAR:			/* parallel view */
                orig.data[0] = v.vp.data[0] + v.vfore * v.vdir.data[0]
                        + x * v.hvec.data[0] + y * v.vvec.data[0];
                orig.data[1] = v.vp.data[1] + v.vfore * v.vdir.data[1]
                        + x * v.hvec.data[1] + y * v.vvec.data[1];
                orig.data[2] = v.vp.data[2] + v.vfore * v.vdir.data[2]
                        + x * v.hvec.data[2] + y * v.vvec.data[2];
                FVECT.VCOPY(direc, v.vdir);
                return (v.vaft > FVECT.FTINY ? v.vaft - v.vfore : 0.0);
            case VIEW.VT_PER:			/* perspective view */
                direc.data[0] = v.vdir.data[0] + x * v.hvec.data[0] + y * v.vvec.data[0];
                direc.data[1] = v.vdir.data[1] + x * v.hvec.data[1] + y * v.vvec.data[1];
                direc.data[2] = v.vdir.data[2] + x * v.hvec.data[2] + y * v.vvec.data[2];
                orig.data[0] = v.vp.data[0] + v.vfore * direc.data[0];
                orig.data[1] = v.vp.data[1] + v.vfore * direc.data[1];
                orig.data[2] = v.vp.data[2] + v.vfore * direc.data[2];
                d = FVECT.normalize(direc);
                return (v.vaft > FVECT.FTINY ? (v.vaft - v.vfore) * d : 0.0);
            case VIEW.VT_HEM:			/* hemispherical fisheye */
                z = 1.0 - x * x * v.hn2 - y * y * v.vn2;
                if (z < 0.0) {
                    return (-1.0);
                }
                z = Math.sqrt(z);
                direc.data[0] = z * v.vdir.data[0] + x * v.hvec.data[0] + y * v.vvec.data[0];
                direc.data[1] = z * v.vdir.data[1] + x * v.hvec.data[1] + y * v.vvec.data[1];
                direc.data[2] = z * v.vdir.data[2] + x * v.hvec.data[2] + y * v.vvec.data[2];
                orig.data[0] = v.vp.data[0] + v.vfore * direc.data[0];
                orig.data[1] = v.vp.data[1] + v.vfore * direc.data[1];
                orig.data[2] = v.vp.data[2] + v.vfore * direc.data[2];
                return (v.vaft > FVECT.FTINY ? v.vaft - v.vfore : 0.0);
            case VIEW.VT_CYL:			/* cylindrical panorama */
                d = x * v.horiz * (Math.PI / 180.0);
                z = Math.cos(d);
                x = Math.sin(d);
                direc.data[0] = z * v.vdir.data[0] + x * v.hvec.data[0] + y * v.vvec.data[0];
                direc.data[1] = z * v.vdir.data[1] + x * v.hvec.data[1] + y * v.vvec.data[1];
                direc.data[2] = z * v.vdir.data[2] + x * v.hvec.data[2] + y * v.vvec.data[2];
                orig.data[0] = v.vp.data[0] + v.vfore * direc.data[0];
                orig.data[1] = v.vp.data[1] + v.vfore * direc.data[1];
                orig.data[2] = v.vp.data[2] + v.vfore * direc.data[2];
                d = FVECT.normalize(direc);
                return (v.vaft > FVECT.FTINY ? (v.vaft - v.vfore) * d : 0.0);
            case VIEW.VT_ANG:			/* angular fisheye */
                x *= (1.0 / 180.0) * v.horiz;
                y *= (1.0 / 180.0) * v.vert;
                d = x * x + y * y;
                if (d > 1.0) {
                    return (-1.0);
                }
                d = Math.sqrt(d);
                z = Math.cos(Math.PI * d);
                d = d <= FVECT.FTINY ? Math.PI : Math.sqrt(1.0 - z * z) / d;
                x *= d;
                y *= d;
                direc.data[0] = z * v.vdir.data[0] + x * v.hvec.data[0] + y * v.vvec.data[0];
                direc.data[1] = z * v.vdir.data[1] + x * v.hvec.data[1] + y * v.vvec.data[1];
                direc.data[2] = z * v.vdir.data[2] + x * v.hvec.data[2] + y * v.vvec.data[2];
                orig.data[0] = v.vp.data[0] + v.vfore * direc.data[0];
                orig.data[1] = v.vp.data[1] + v.vfore * direc.data[1];
                orig.data[2] = v.vp.data[2] + v.vfore * direc.data[2];
                return (v.vaft > FVECT.FTINY ? v.vaft - v.vfore : 0.0);
            case VIEW.VT_PLS:			/* planispheric fisheye */
                x *= Math.sqrt(v.hn2);
                y *= Math.sqrt(v.vn2);
                d = x * x + y * y;
                z = (1. - d) / (1. + d);
                d = d <= FVECT.FTINY * FVECT.FTINY ? Math.PI : Math.sqrt((1.0 - z * z) / d);
                x *= d;
                y *= d;
                direc.data[0] = z * v.vdir.data[0] + x * v.hvec.data[0] + y * v.vvec.data[0];
                direc.data[1] = z * v.vdir.data[1] + x * v.hvec.data[1] + y * v.vvec.data[1];
                direc.data[2] = z * v.vdir.data[2] + x * v.hvec.data[2] + y * v.vvec.data[2];
                orig.data[0] = v.vp.data[0] + v.vfore * direc.data[0];
                orig.data[1] = v.vp.data[1] + v.vfore * direc.data[1];
                orig.data[2] = v.vp.data[2] + v.vfore * direc.data[2];
                return (v.vaft > FVECT.FTINY ? v.vaft - v.vfore : 0.0);
        }
        return (-1.0);
    }

//
//void
//viewloc(			/* find image location for point */
//FVECT  ip,
//VIEW  *v,
//FVECT  p
//)
//{
//	double  d, d2;
//	FVECT  disp;
//
//	VSUB(disp, p, v->vp);
//
//	switch (v->type) {
//	case VT_PAR:			/* parallel view */
//		ip[2] = DOT(disp,v->vdir) - v->vfore;
//		break;
//	case VT_PER:			/* perspective view */
//		d = DOT(disp,v->vdir);
//		ip[2] = VLEN(disp);
//		if (d < 0.0) {		/* fold pyramid */
//			ip[2] = -ip[2];
//			d = -d;
//		}
//		if (d > FTINY) {
//			d = 1.0/d;
//			disp[0] *= d;
//			disp[1] *= d;
//			disp[2] *= d;
//		}
//		ip[2] *= (1.0 - v->vfore*d);
//		break;
//	case VT_HEM:			/* hemispherical fisheye */
//		d = normalize(disp);
//		if (DOT(disp,v->vdir) < 0.0)
//			ip[2] = -d;
//		else
//			ip[2] = d;
//		ip[2] -= v->vfore;
//		break;
//	case VT_CYL:			/* cylindrical panorama */
//		d = DOT(disp,v->hvec);
//		d2 = DOT(disp,v->vdir);
//		ip[0] = 180.0/PI * atan2(d,d2) / v->horiz + 0.5 - v->hoff;
//		d = 1.0/sqrt(d*d + d2*d2);
//		ip[1] = DOT(disp,v->vvec)*d/v->vn2 + 0.5 - v->voff;
//		ip[2] = VLEN(disp);
//		ip[2] *= (1.0 - v->vfore*d);
//		return;
//	case VT_ANG:			/* angular fisheye */
//		ip[0] = 0.5 - v->hoff;
//		ip[1] = 0.5 - v->voff;
//		ip[2] = normalize(disp) - v->vfore;
//		d = DOT(disp,v->vdir);
//		if (d >= 1.0-FTINY)
//			return;
//		if (d <= -(1.0-FTINY)) {
//			ip[0] += 180.0/v->horiz;
//			return;
//		}
//		d = (180.0/PI)*acos(d) / sqrt(1.0 - d*d);
//		ip[0] += DOT(disp,v->hvec)*d/v->horiz;
//		ip[1] += DOT(disp,v->vvec)*d/v->vert;
//		return;
//	case VT_PLS:			/* planispheric fisheye */
//		ip[0] = 0.5 - v->hoff;
//		ip[1] = 0.5 - v->voff;
//		ip[2] = normalize(disp) - v->vfore;
//		d = DOT(disp,v->vdir);
//		if (d >= 1.0-FTINY)
//			return;
//		if (d <= -(1.0-FTINY))
//			return;		/* really an error */
//		d = sqrt(1.0 - d*d) / (1.0 + d);
//		ip[0] += DOT(disp,v->hvec)*d/sqrt(v->hn2);
//		ip[1] += DOT(disp,v->vvec)*d/sqrt(v->vn2);
//		return;
//	}
//	ip[0] = DOT(disp,v->hvec)/v->hn2 + 0.5 - v->hoff;
//	ip[1] = DOT(disp,v->vvec)/v->vn2 + 0.5 - v->voff;
//}
//
//
    public static void pix2loc( /* compute image location from pixel pos. */
            double[] loc,
            RESOLU rp,
            int px,
            int py) {
        int x, y;

        if ((rp.rt & RESOLU.YMAJOR) != 0) {
            x = px;
            y = py;
        } else {
            x = py;
            y = px;
        }
        if ((rp.rt & RESOLU.XDECR) != 0) {
            x = rp.xr - 1 - x;
        }
        if ((rp.rt & RESOLU.YDECR) != 0) {
            y = rp.yr - 1 - y;
        }
        loc[0] = (x + .5) / rp.xr;
        loc[1] = (y + .5) / rp.yr;
    }

//
//void
//loc2pix(			/* compute pixel pos. from image location */
//int  pp[2],
//RESOLU  *rp,
//double  lx,
//double  ly
//)
//{
//	int  x, y;
//
//	x = (lx >= 0.0) ? (int)(lx * rp->xr) : -(int)(-lx * rp->xr);
//	y = (ly >= 0.0) ? (int)(ly * rp->yr) : -(int)(-ly * rp->yr);
//	if (rp->rt & XDECR)
//		x = rp->xr-1 - x;
//	if (rp->rt & YDECR)
//		y = rp->yr-1 - y;
//	if (rp->rt & YMAJOR) {
//		pp[0] = x;
//		pp[1] = y;
//	} else {
//		pp[0] = y;
//		pp[1] = x;
//	}
//}
    public static int getviewopt( /* process view argument */
            VIEW v,
            int ac,
            String[] av) {
//#define check(c,l)	if ((av[0][c]&&av[0][c]!=' ') || \
//			badarg(ac-1,av+1,l)) return(-1)

        if (ac <= 0 || av[0].charAt(0) != '-' || av[0].charAt(1) != 'v') {
            return (-1);
        }
        switch (av[0].charAt(2)) {
            case 't':			/* type */
                if (av[0].charAt(3) == 0 || av[0].charAt(3) == ' ') {
                    return (-1);
                }
//		check(4,"");
                v.type = av[0].charAt(3);
                return (0);
            case 'p':			/* point */
//		check(3,"fff");
                v.vp.data[0] = Float.parseFloat(av[1]);
                v.vp.data[1] = Float.parseFloat(av[2]);
                v.vp.data[2] = Float.parseFloat(av[3]);
                return (3);
            case 'd':			/* direction */
//		check(3,"fff");
                v.vdir.data[0] = Float.parseFloat(av[1]);
                v.vdir.data[1] = Float.parseFloat(av[2]);
                v.vdir.data[2] = Float.parseFloat(av[3]);
                v.vdist = 1.;
                return (3);
            case 'u':			/* up */
//		check(3,"fff");
                v.vup.data[0] = Float.parseFloat(av[1]);
                v.vup.data[1] = Float.parseFloat(av[2]);
                v.vup.data[2] = Float.parseFloat(av[3]);
                return (3);
            case 'h':			/* horizontal size */
//		check(3,"f");
                v.horiz = Float.parseFloat(av[1]);
                return (1);
            case 'v':			/* vertical size */
//		check(3,"f");
                v.vert = Float.parseFloat(av[1]);
                return (1);
            case 'o':			/* fore clipping plane */
//		check(3,"f");
                v.vfore = Float.parseFloat(av[1]);
                return (1);
            case 'a':			/* aft clipping plane */
//		check(3,"f");
                v.vaft = Float.parseFloat(av[1]);
                return (1);
            case 's':			/* shift */
//		check(3,"f");
                v.hoff = Float.parseFloat(av[1]);
                return (1);
            case 'l':			/* lift */
//		check(3,"f");
                v.voff = Float.parseFloat(av[1]);
                return (1);
            default:
                return (-1);
        }
//#undef check
    }

public static int
sscanview(				/* get view parameters from string */
VIEW  vp,
String s
)
{
        char[]  schars = s.toCharArray();
	int  ac;
//	char[]  av = new char[4];
        String[] av = {"","","",""};
	int  na;
	int  nvopts = 0;
        int sidx=0;
	while (Character.isWhitespace(schars[sidx]))
		if (schars[sidx++]==0)
			return(0);
	while (schars[sidx]!=0) {
		ac = 0;
		do {
			if (ac != 0|| schars[sidx] == '-') {
				//av[ac++] = schars;
                            while (schars[sidx]!=0) {
                            av[ac] += schars[sidx];
                            }
                            ac++;
                        }
			while (schars[sidx]!=0 && !Character.isWhitespace(schars[sidx])) {
				sidx++;
                        }
			while (Character.isWhitespace(schars[sidx])) {
				sidx++;
                        }
		} while (schars[sidx]!=0 && ac < 4);
		if ((na = getviewopt(vp, ac, av)) >= 0) {
			if (na+1 < ac) {
				s = av[na+1];
                        }
			nvopts++;
		} else if (ac > 1) {
			s = av[1];
                }
	}
	return(nvopts);
}


public static void
fprintview(				/* write out view parameters */
VIEW  vp,
OutputStream fp
) throws IOException
{
	fp.write(String.format( " -vt%c", vp.type).getBytes());
	fp.write(String.format( " -vp %.6g %.6g %.6g", vp.vp.data[0], vp.vp.data[1], vp.vp.data[2]).getBytes());
	fp.write(String.format( " -vd %.6g %.6g %.6g", vp.vdir.data[0]*vp.vdist,
						vp.vdir.data[1]*vp.vdist,
						vp.vdir.data[2]*vp.vdist).getBytes());
	fp.write(String.format( " -vu %.6g %.6g %.6g", vp.vup.data[0], vp.vup.data[1], vp.vup.data[2]).getBytes());
	fp.write(String.format( " -vh %.6g -vv %.6g", vp.horiz, vp.vert).getBytes());
	fp.write(String.format( " -vo %.6g -va %.6g", vp.vfore, vp.vaft).getBytes());
	fp.write(String.format( " -vs %.6g -vl %.6g", vp.hoff, vp.voff).getBytes());
}


//char *
//viewopt(				/* translate to minimal view string */
//VIEW  *vp
//)
//{
//	static char  vwstr[128];
//	char  *cp = vwstr;
//
//	*cp = '\0';
//	if (vp->type != stdview.type) {
//		sprintf(cp, " -vt%c", vp->type);
//		cp += strlen(cp);
//	}
//	if (!VEQ(vp->vp,stdview.vp)) {
//		sprintf(cp, " -vp %.6g %.6g %.6g",
//				vp->vp[0], vp->vp[1], vp->vp[2]);
//		cp += strlen(cp);
//	}
//	if (!FEQ(vp->vdist,stdview.vdist) || !VEQ(vp->vdir,stdview.vdir)) {
//		sprintf(cp, " -vd %.6g %.6g %.6g",
//				vp->vdir[0]*vp->vdist,
//				vp->vdir[1]*vp->vdist,
//				vp->vdir[2]*vp->vdist);
//		cp += strlen(cp);
//	}
//	if (!VEQ(vp->vup,stdview.vup)) {
//		sprintf(cp, " -vu %.6g %.6g %.6g",
//				vp->vup[0], vp->vup[1], vp->vup[2]);
//		cp += strlen(cp);
//	}
//	if (!FEQ(vp->horiz,stdview.horiz)) {
//		sprintf(cp, " -vh %.6g", vp->horiz);
//		cp += strlen(cp);
//	}
//	if (!FEQ(vp->vert,stdview.vert)) {
//		sprintf(cp, " -vv %.6g", vp->vert);
//		cp += strlen(cp);
//	}
//	if (!FEQ(vp->vfore,stdview.vfore)) {
//		sprintf(cp, " -vo %.6g", vp->vfore);
//		cp += strlen(cp);
//	}
//	if (!FEQ(vp->vaft,stdview.vaft)) {
//		sprintf(cp, " -va %.6g", vp->vaft);
//		cp += strlen(cp);
//	}
//	if (!FEQ(vp->hoff,stdview.hoff)) {
//		sprintf(cp, " -vs %.6g", vp->hoff);
//		cp += strlen(cp);
//	}
//	if (!FEQ(vp->voff,stdview.voff)) {
//		sprintf(cp, " -vl %.6g", vp->voff);
//		cp += strlen(cp);
//	}
//	return(vwstr);
//}
//
	static String[] altname={null,VIEW.VIEWSTR,"rpict","rview","rvu","rpiece","pinterp",null};

public static int
isview(					/* is this a view string? */
char[]  s
)
{
//	extern char  *progname;
//	char[]  cp;
	char[][] an;
					/* add program name to list */
	if (altname[0] == null) {
                int cpidx = RPMAIN.progname.lastIndexOf(System.getProperty("file.separator"));
                altname[0] = RPMAIN.progname.substring(cpidx);
//		for (cp = progname; *cp; cp++)
//			;
//		while (cp > progname && !ISDIRSEP(cp[-1]))
//			cp--;
//		altname[0] = cp;
	}
					/* skip leading path */
        String cp = new String(s);
        int cpidx = cp.lastIndexOf(System.getProperty("file.separator"));
//	cp = s;
//	while (*cp && *cp != ' ')
//		cp++;
//	while (cp > s && !PATHS.ISDIRSEP(cp[-1]))
//		cp--;
        for (int altidx=0; altidx < altname.length; altidx++) {
            if (cp.equals(altname[altidx])) {
                return(1);
            }
        }
//	for (an = altname; *an != NULL; an++)
//		if (!strncmp(*an, cp, strlen(*an)))
//			return(1);
	return(0);
}


//struct myview {
//	VIEW	*hv;
//	int	ok;
//};
//
//
//static int
//gethview(				/* get view from header */
//	char  *s,
//	void  *v
//)
//{
//	if (isview(s) && sscanview(((struct myview*)v)->hv, s) > 0)
//		((struct myview*)v)->ok++;
//	return(0);
//}
//
//
//int
//viewfile(				/* get view from file */
//char  *fname,
//VIEW  *vp,
//RESOLU  *rp
//)
//{
//	struct myview	mvs;
//	FILE  *fp;
//
//	if (fname == NULL || !strcmp(fname, "-"))
//		fp = stdin;
//	else if ((fp = fopen(fname, "r")) == NULL)
//		return(-1);
//
//	mvs.hv = vp;
//	mvs.ok = 0;
//
//	getheader(fp, gethview, &mvs);
//
//	if (rp != NULL && !fgetsresolu(rp, fp))
//		mvs.ok = 0;
//
//	fclose(fp);
//
//	return(mvs.ok);
//}
//    
}
