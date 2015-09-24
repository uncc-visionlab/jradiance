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

import java.io.BufferedInputStream;
import java.io.IOException;
import jradiance.common.CALCOMP;
import jradiance.common.CALCOMP.EPNODE;
import jradiance.common.CALDEFN;
import jradiance.common.CALEXPR;
import jradiance.common.CALFUNC;
import jradiance.common.CALFUNC.LIBF;
import jradiance.common.ERF;
import jradiance.common.GETLIBPATH;
import jradiance.common.GETPATH;
import jradiance.common.MAT4;
import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OBJECT_STRUCTURE;
import jradiance.common.OCTREE;
import jradiance.common.OTYPES;
import jradiance.common.PATHS;
import jradiance.common.SAVESTR;
import jradiance.common.XF;

/**
 *
 * @author arwillis
 */
public class FUNC {
    /*
     * Header file for modifiers using function files.
     *
     * Include after ray.h
     */

    public static final int MAXEXPR = 9;	/* maximum expressions in modifier */

    public static class MFUNC extends OBJECT_STRUCTURE {

        EPNODE[] ep = new EPNODE[MAXEXPR + 1];		/* null-terminated expression list */

        String ctx;			/* context (from file name) */

        XF f, b;			/* forward and backward transforms */


        @Override
        public int octree_function(Object... obj) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }			/* material function */

//extern XF  unitxf;		/* identity transform */
//extern XF  funcxf;		/* current transform */
//
//
//extern MFUNC	*getfunc(OBJREC *m, int ff, unsigned int ef, int dofwd);
//extern void	freefunc(OBJREC *m);
//extern int	setfunc(OBJREC *m, RAY *r);
//extern void	loadfunc(char *fname);
//
//	/* defined in noise3.c */
//extern void	setnoisefuncs(void);
//
//	/* defined in fprism.c */
//extern void	setprismfuncs(void);
//

    /*
     *  func.c - interface to CALCOMP functions.
     */
    public static final String INITFILE = "rayinit.cal";
    static final String CALSUF = ".cal";
    static final int LCALSUF = 4;
    static final String REFVNAME = "`FILE_REFCNT";
    static XF unitxf = new XF();/* identity transform */

    static XF funcxf = new XF();/* current transformation */

    static OBJREC fobj = null;	/* current function object */

    static RAY fray = null;	/* current function ray */

    static String initfile = INITFILE;
    public static boolean debugFunc = true;

    public static MFUNC getfunc( /* get function for this modifier */
            OBJREC m,
            int ff,
            int ef,
            int dofwd) {
//	static char  initfile[] = INITFILE;
//	char  sbuf[MAXSTR];
        String sbuf;
        String[] arg;
        MFUNC f;
        int ne, na;
        int i;
        /* check to see if done already */
        if ((f = (MFUNC) m.os) != null) {
            return (f);
        }
        fobj = null;
        fray = null;
        try {
            if (initfile.charAt(0) != 0) {		/* initialize on first call */
                CALEXPR.esupport |= CALCOMP.E_VARIABLE | CALCOMP.E_FUNCTION
                        | CALCOMP.E_INCHAN | CALCOMP.E_RCONST | CALCOMP.E_REDEFW;
                CALEXPR.esupport &= ~(CALCOMP.E_OUTCHAN);
                CALDEFN.setcontext("");
                if (FUNC.debugFunc) {
                    System.out.println("context = " + CALDEFN.context);
                }
                CALDEFN.scompile("Dx=$1;Dy=$2;Dz=$3;", null, 0);
                CALDEFN.scompile("Nx=$4;Ny=$5;Nz=$6;", null, 0);
                CALDEFN.scompile("Px=$7;Py=$8;Pz=$9;", null, 0);
                CALDEFN.scompile("T=$10;Ts=$25;Rdot=$11;", null, 0);
                CALDEFN.scompile("S=$12;Tx=$13;Ty=$14;Tz=$15;", null, 0);
                CALDEFN.scompile("Ix=$16;Iy=$17;Iz=$18;", null, 0);
                CALDEFN.scompile("Jx=$19;Jy=$20;Jz=$21;", null, 0);
                CALDEFN.scompile("Kx=$22;Ky=$23;Kz=$24;", null, 0);
                CALDEFN.scompile("Lu=$26;Lv=$27;", null, 0);
                CALFUNC.funset("arg", 1, '=', new l_arg());
                CALFUNC.funset("erf", 1, ':', new l_erf());
                CALFUNC.funset("erfc", 1, ':', new l_erfc());
                NOISE3.setnoisefuncs();
                FPRISM.setprismfuncs();
                loadfunc(initfile);
                initfile = null;
            }
            if ((na = m.oargs.nsargs) <= ff) {
//		goto toofew;
                return null;
            }
            arg = m.oargs.sarg;
            if ((f = new MFUNC()) == null) {
//		goto memerr;
                return null;
            }
            i = arg[ff].length();			/* set up context */
            if (i == 1 && arg[ff].charAt(0) == '.') {
                CALDEFN.setcontext(f.ctx = "");	/* "." means no file */
            } else {
                //strcpy(sbuf,arg[ff]);		/* file name is context */
                sbuf = arg[ff];		/* file name is context */
                int sufIdx = sbuf.indexOf(CALSUF);
                if (sufIdx > 0) {
                    sbuf = sbuf.substring(0, sufIdx);    /* remove suffix */
                }
//		if (i > LCALSUF && !strcmp(sbuf+i-LCALSUF, CALSUF))
//			sbuf[i-LCALSUF] = '\0';	/* remove suffix */
                CALDEFN.setcontext(f.ctx = SAVESTR.savestr(sbuf));
                if (!CALDEFN.vardefined(REFVNAME)) {	/* file loaded? */
                    loadfunc(arg[ff]);
                    CALDEFN.varset(REFVNAME, '=', 1.0);
                } else /* reference_count++ */ {
                    CALDEFN.varset(REFVNAME, '=', CALDEFN.varvalue(REFVNAME) + 1.0);
                }
            }
            CALDEFN.curfunc = null;			/* parse expressions */
//	sprintf(sbuf, "%s \"%s\"", ofun[m.otype].funame, m.oname);
            sbuf = String.format("%s \"%s\"", OTYPES.ofun[m.otype].funame, m.oname);
            for (i = 0, ne = 0; ef != 0 && i < na; i++, ef >>= 1) {
                if ((ef & 1) != 0) {			/* flagged as an expression? */
                    if (ne >= MAXEXPR) {
//				objerror(m, INTERNAL, "too many expressions");
                    }
                    CALEXPR.initstr(arg[i], sbuf, 0);
                    f.ep[ne++] = CALEXPR.getE1();
                    if (CALEXPR.nextc != CALEXPR.EOF) {
//				syntax("unexpected character");
                    }
                }
            }
            if (ef != 0) {
//		goto toofew;
                return null;
            }
            if (i <= ff) /* find transform args */ {
                i = ff + 1;
            }
            while (i < na && arg[i].charAt(0) != '-') {
                i++;
            }
            if (i == na) /* no transform */ {
                f.f = f.b = unitxf;
            } else {				/* get transform */
                if ((f.b = new XF()) == null) {
//			goto memerr;
                    return null;
                }
                String[] ns = new String[na - i];
                System.arraycopy(arg, i, ns, 0, na - i);
//		if (XF.invxf(f.b, na-i, arg+i) != na-i) {
////			objerror(m, USER, "bad transform");
//                }
                if (XF.invxf(f.b, ns.length, ns) != na - i) {
//			objerror(m, USER, "bad transform");
                }
                if (f.b.sca < 0.0) {
                    f.b.sca = -f.b.sca;
                }
                if (dofwd != 0) {			/* do both transforms */
                    if ((f.f = new XF()) == null) {
//				goto memerr;
                        return null;
                    }
                    XF.xf(f.f, ns.length, ns);
//			XF.xf(f.f, na-i, arg+i);
                    if (f.f.sca < 0.0) {
                        f.f.sca = -f.f.sca;
                    }
                }
            }
        } catch (IOException e) {
        }
        m.os = f;
        return (f);
//toofew:
//	objerror(m, USER, "too few string arguments");
//memerr:
//	error(SYSTEM, "out of memory in getfunc");
//	return null; /* pro forma return */
    }
//
//
//extern void
//freefunc(			/* free memory associated with modifier */
//	OBJREC  *m
//)
//{
//	register MFUNC  *f;
//	register int  i;
//
//	if ((f = (MFUNC *)m.os) == null)
//		return;
//	for (i = 0; f.ep[i] != null; i++)
//		epfree(f.ep[i]);
//	if (f.ctx[0]) {			/* done with definitions */
//		setcontext(f.ctx);
//		i = varvalue(REFVNAME)-.5;	/* reference_count-- */
//		if (i > 0)
//			varset(REFVNAME, '=', (double)i);
//		else
//			dcleanup(2);		/* remove definitions */
//		freestr(f.ctx);
//	}
//	if (f.b != &unitxf)
//		free((void *)f.b);
//	if (f.f != null && f.f != &unitxf)
//		free((void *)f.f);
//	free((void *)f);
//	m.os = null;
//}
//
    static long lastrno = ~0;

    public static int setfunc( /* set channels for function call */
            OBJREC m,
            RAY r) {
//	static RNUMBER  lastrno = ~0;
        MFUNC f;
        /* get function */
        if ((f = (MFUNC) m.os) == null) {
//		objerror(m, CONSISTENCY, "setfunc called before getfunc");
        }
        /* set evaluator context */
        CALDEFN.setcontext(f.ctx);
        /* check to see if matrix set */
        if (m == fobj && r.rno == lastrno) {
            return (0);
        }
        fobj = m;
        fray = r;
        if (r.rox != null) {
            if (f.b != unitxf) {
                funcxf.sca = r.rox.b.sca * f.b.sca;
                MAT4.multmat4(funcxf.xfm, r.rox.b.xfm, f.b.xfm);
            } else {
                funcxf = r.rox.b;
            }
        } else {
            funcxf = f.b;
        }
        lastrno = r.rno;
        CALDEFN.eclock++;		/* notify expression evaluator */
        return (1);
    }

    static void loadfunc( /* load definition file */
            String fname) throws IOException {
        String ffname;

        if (debugFunc) {
            System.out.println("reading .cal file: " + fname);
        }
        if ((ffname = GETPATH.getpath(fname, GETLIBPATH.getrlibpath(), PATHS.R_OK)) == null) {
//		sprintf(errmsg, "cannot find function file \"%s\"", fname);
//		error(USER, errmsg);
        }
        if (ffname == null) {
            ffname = "/jradiance/rt/" + fname;
            BufferedInputStream bis = new BufferedInputStream(new OCTREE().getClass().getResourceAsStream(ffname));
            if (bis != null) {
                CALDEFN.fcompile(ffname, bis);
            } else {
                CALDEFN.fcompile(null);
            }
        } else {
            CALDEFN.fcompile(ffname);
        }
    }

    public static class l_arg implements LIBF {

        static double l_arg(String nm) /* return nth real argument */ {
            int n;

            if (fobj == null) {
//		syntax("arg(n) used in constant expression");
            }

            n = (int) (CALFUNC.argument(1) + .5);		/* round to integer */

            if (n < 1) {
                return (fobj.oargs.nfargs);
            }

            if (n > fobj.oargs.nfargs) {
//		sprintf(errmsg, "missing real argument %d", n);
//		objerror(fobj, USER, errmsg);
            }
            return (fobj.oargs.farg[n - 1]);
        }

        @Override
        public double fval(String nm) {
            return l_arg(nm);
        }
    }

    public static class l_erf implements LIBF {

        static double l_erf(String nm) /* error function */ {
            return (ERF.erf(CALFUNC.argument(1)));
        }

        @Override
        public double fval(String nm) {
            return l_erf(nm);
        }
    }

    public static class l_erfc implements LIBF {

        static double l_erfc(String nm) /* cumulative error function */ {
            return (ERF.erfc(CALFUNC.argument(1)));
        }

        @Override
        public double fval(String nm) {
            return l_erfc(nm);
        }
    }

    public static double chanvalue( /* return channel n to CALCOMP */
            int n) {
        if (fray == null) {
//		syntax("ray parameter used in constant expression");
        }

        if (--n < 0) {
//		goto badchan;
        }

        if (n <= 2) {			/* ray direction */

            return ((fray.rdir.data[0] * funcxf.xfm.data[0][n]
                    + fray.rdir.data[1] * funcxf.xfm.data[1][n]
                    + fray.rdir.data[2] * funcxf.xfm.data[2][n])
                    / funcxf.sca);
        }

        if (n <= 5) /* surface normal */ {
            return ((fray.ron.data[0] * funcxf.xfm.data[0][n - 3]
                    + fray.ron.data[1] * funcxf.xfm.data[1][n - 3]
                    + fray.ron.data[2] * funcxf.xfm.data[2][n - 3])
                    / funcxf.sca);
        }

        if (n <= 8) /* intersection */ {
            return (fray.rop.data[0] * funcxf.xfm.data[0][n - 6]
                    + fray.rop.data[1] * funcxf.xfm.data[1][n - 6]
                    + fray.rop.data[2] * funcxf.xfm.data[2][n - 6]
                    + funcxf.xfm.data[3][n - 6]);
        }

        if (n == 9) /* total distance */ {
            return (RAYTRACE.raydist(fray, RAY.PRIMARY) * funcxf.sca);
        }

        if (n == 10) /* dot product (range [-1,1]) */ {
            return (fray.rod <= -1.0 ? -1.0
                    : fray.rod >= 1.0 ? 1.0
                    : fray.rod);
        }

        if (n == 11) /* scale */ {
            return (funcxf.sca);
        }

        if (n <= 14) /* origin */ {
            return (funcxf.xfm.data[3][n - 12]);
        }

        if (n <= 17) /* i unit vector */ {
            return (funcxf.xfm.data[0][n - 15] / funcxf.sca);
        }

        if (n <= 20) /* j unit vector */ {
            return (funcxf.xfm.data[1][n - 18] / funcxf.sca);
        }

        if (n <= 23) /* k unit vector */ {
            return (funcxf.xfm.data[2][n - 21] / funcxf.sca);
        }

        if (n == 24) /* single ray (shadow) distance */ {
            return ((fray.rot + RAYTRACE.raydist(fray.parent, RAY.SHADOW)) * funcxf.sca);
        }

        if (n <= 26) /* local (u,v) coordinates */ {
            return (fray.uv[n - 25]);
        }
//badchan:
//	error(USER, "illegal channel number");
        return (0.0);
    }
}
