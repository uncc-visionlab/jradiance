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

import jradiance.common.CALCOMP.EPNODE;
import jradiance.common.CALCOMP.LIBR;
import jradiance.common.CALCOMP.VARDEF;
import jradiance.common.CALEXPR.EOPER;

/**
 *
 * @author arwillis
 */
public class CALFUNC {
    /*
     *  calfunc.c - routines for CALCOMP using functions.
     *
     *      If VARIABLE is not set, only library functions
     *  can be accessed.
     *
     *  2/19/03	Eliminated conditional compiles in favor of esupport extern.
     */

    /* bits in argument flag (better be right!) */
    public static final int AFLAGSIZ = (8 * Long.SIZE / 8);//sizeof(unsigned long))
    public static final int ALISTSIZ = 6;	/* maximum saved argument list */


    public static class ACTIVATION {

        String name;		/* function name */

        ACTIVATION prev;	/* previous activation */

        double[] ap;		/* argument list */
        // was unsigned long!

        long an;		/* computed argument flags */

        EPNODE fun;		/* argument function */

    }		/* an activation record */

    static ACTIVATION curact = null;

//static double  libfunc(char *fname, VARDEF *vp);
    public interface LIBF {

        double fval(String nm);
    }
    public static final int MAXLIB = 64;	/* maximum number of library functions */

    /* functions must be listed alphabetically */
    static LIBR[] library = {
        new LIBR("acos", (short) 1, (short) ':', new l_acos()),
        new LIBR("asin", (short) 1, (short) ':', new l_asin()),
        new LIBR("atan", (short) 1, (short) ':', new l_atan()),
        new LIBR("atan2", (short) 2, (short) ':', new l_atan2()),
        new LIBR("ceil", (short) 1, (short) ':', new l_ceil()),
        new LIBR("cos", (short) 1, (short) ':', new l_cos()),
        new LIBR("exp", (short) 1, (short) ':', new l_exp()),
        new LIBR("floor", (short) 1, (short) ':', new l_floor()),
        new LIBR("if", (short) 3, (short) ':', new l_if()),
        new LIBR("log", (short) 1, (short) ':', new l_log()),
        new LIBR("log10", (short) 1, (short) ':', new l_log10()),
        new LIBR("rand", (short) 1, (short) ':', new l_rand()),
        new LIBR("select", (short) 1, (short) ':', new l_select()),
        new LIBR("sin", (short) 1, (short) ':', new l_sin()),
        new LIBR("sqrt", (short) 1, (short) ':', new l_sqrt()),
        new LIBR("tan", (short) 1, (short) ':', new l_tan()),
    null, null, null, null, null, null, null, null, null, null,
    null, null, null, null, null, null, null, null, null, null,
    null, null, null, null, null, null, null, null, null, null,
    null, null, null, null, null, null, null, null, null, null,
    null, null, null, null, null, null, null, null
    };
    static int libsize = 16;

    static VARDEF resolve(EPNODE ep) {
        return ((ep).type == CALCOMP.VAR ? (ep).v.ln : argf((ep).v.chan));
    }

    public static int fundefined(String fname) /* return # of arguments for function */ {
        LIBR lp;
        VARDEF vp;

        if ((vp = CALDEFN.varlookup(fname)) != null && vp.def != null
                && vp.def.v.kid.type == CALCOMP.FUNC) {
            return (CALEXPR.nekids(vp.def.v.kid) - 1);
        }
        lp = vp != null ? vp.lib : liblookup(fname);
        if (lp == null) {
            return (0);
        }
        return (lp.nargs);
    }

    public static double funvalue( /* return a function value to the user */
            String fname,
            int n,
            double[] a) {
        ACTIVATION act = new ACTIVATION();
        VARDEF vp;
        double rval = 0;
        /* push environment */
        act.name = fname;
        act.prev = curact;
        act.ap = a;
        if (n >= AFLAGSIZ) {
            act.an = ~0;
        } else {
            act.an = (1L << n) - 1;
        }
        act.fun = null;
        curact = act;

        if ((vp = CALDEFN.varlookup(fname)) == null || vp.def == null
                || vp.def.v.kid.type != CALCOMP.FUNC) {
            rval = libfunc(fname, vp);
        } else {
            rval = CALCOMP.evalue(vp.def.v.kid.sibling);
        }

        curact = act.prev;			/* pop environment */
        return (rval);
    }

    public static void funset( /* set a library function */
            String fname,
            int nargs,
            int assign,
            LIBF fptr) {
    int  oldlibsize = libsize;
    char[] cp;
    LIBR lp, lp_prev;
    int lpidx=0;						/* check for context */
//    for (cp = fname; *cp; cp++)
//	;
//    if (cp == fname)
//	return;
    if (fname.length()==0) 
        return;
    if (fname.charAt(fname.length()-1) == CALCOMP.CNTXMARK) {
//	*--cp = '\0';
    }
    if ((lp = liblookup(fname)) == null) {	/* insert */
	if (libsize >= MAXLIB) {
//	    eputs("Too many library functons!\n");
//	    quit(1);
            System.exit(1);
	}
        lp = new LIBR(fname, (short) nargs, (short) assign, fptr);
	for (lpidx=libsize; lpidx > 0; lpidx--) {
	    if (library[lpidx-1].fname.compareTo(fname) > 0) {
                library[lpidx] = library[lpidx-1];
                //library[lpidx-1] = newFunc;
	    } else
		break;
        }
	libsize++;
    }
    if (fptr == null) {				/* delete */
        System.exit(1);
//        throw new UnsupportOperationException("unsupported!");
//	while (lpidx < libsize-1) {
//            lp = library[lpidx];
//            lp_prev = library[lpidx+1];
//	    lp.fname = lp_prev.fname;
//	    lp.nargs = lp_prev.nargs;
//	    lp.atyp = lp_prev.atyp;
//	    lp.f = lp_prev.f;
//	    lpidx++;
//	}
	libsize--;
    } else {					/* or assign */
	lp.fname = fname;		/* string must be static! */
	lp.nargs = (short) nargs;
	lp.atyp = (short) assign;
	lp.f = fptr;
        library[lpidx] = lp;
    }
    if (libsize != oldlibsize)
	CALDEFN.libupdate(fname);			/* relink library */
    }

    static int nargum() /* return number of available arguments */ {
        int n;

        if (curact == null) {
            return (0);
        }
        if (curact.fun == null) {
            for (n = 0; ((1L << n) & curact.an) != 0; n++)
	    ;
            return (n);
        }
        return (CALEXPR.nekids(curact.fun) - 1);
    }

    public static double argument( /* return nth argument for active function */
            int n) {
    ACTIVATION  actp = curact;
    EPNODE  ep = null;
        double aval = 0;

    if (actp == null || --n < 0) {
//	eputs("Bad call to argument!\n");
//	quit(1);
        System.exit(1);
    }
						/* already computed? */
    if (n < AFLAGSIZ && (1L<<n & actp.an)!=0)
	return(actp.ap[n]);

    if (actp.fun == null || (ep = CALEXPR.ekid(actp.fun, n+1)) == null) {
//	eputs(actp.name);
//	eputs(": too few arguments\n");
//	quit(1);
        System.exit(1);
    }
    curact = actp.prev;			/* pop environment */
    aval = CALCOMP.evalue(ep);				/* compute argument */
    curact = actp;				/* push back environment */
    if (n < ALISTSIZ) {				/* save value */
	actp.ap[n] = aval;
	actp.an |= 1L<<n;
    }
        return (aval);
    }

    static VARDEF argf(int n) /* return function def for nth argument */ {
        throw new UnsupportedOperationException("unsupported op");
//    register ACTIVATION  *actp;
//    register EPNODE  *ep;
//
//    for (actp = curact; actp != null; actp = actp.prev) {
//
//	if (n <= 0)
//	    break;
//
//	if (actp.fun == null)
//	    goto badarg;
//
//	if ((ep = ekid(actp.fun, n)) == null) {
//	    eputs(actp.name);
//	    eputs(": too few arguments\n");
//	    quit(1);
//	}
//	if (ep.type == VAR)
//	    return(ep.v.ln);			/* found it */
//
//	if (ep.type != ARG)
//	    goto badarg;
//
//	n = ep.v.chan;				/* try previous context */
//    }
//    eputs("Bad call to argf!\n");
//    quit(1);
//
//badarg:
//    eputs(actp.name);
//    eputs(": argument not a function\n");
//    quit(1);
//        return null; /* pro forma return */
    }

    String argfun(int n) /* return function name for nth argument */ {
        return (argf(n).name);
    }

    static class EFUNC implements EOPER {

        double efunc( /* evaluate a function */
                EPNODE ep) {
            ACTIVATION act = new ACTIVATION();
            double[] alist = new double[ALISTSIZ];
            double rval = 0;
            VARDEF dp;
            /* push environment */
            dp = resolve(ep.v.kid);
            act.name = dp.name;
            act.prev = curact;
            act.ap = alist;
            act.an = 0;
            act.fun = ep;
            curact = act;

            if (dp.def == null || dp.def.v.kid.type != CALCOMP.FUNC) {
                rval = libfunc(act.name, dp);
            } else {
                rval = CALCOMP.evalue(dp.def.v.kid.sibling);
            }

            curact = act.prev;			/* pop environment */
            return (rval);
        }

        @Override
        public double op(EPNODE ep) {
//            throw new UnsupportedOperationException("Not supported yet.");
            return efunc(ep);
        }
    }

    static LIBR liblookup(String fname) /* look up a library function */ {
        int upper, lower;
        int cm, i;

        lower = 0;
        upper = cm = libsize;

        while ((i = (lower + upper) >> 1) != cm) {
            cm = fname.compareTo(library[i].fname);
            if (cm > 0) {
                lower = i;
            } else if (cm < 0) {
                upper = i;
            } else {
                return (library[i]);
            }
            cm = i;
        }
        return (null);
    }


    /*
     *  The following routines are for internal use:
     */
    static double libfunc( /* execute library function */
            String fname,
            VARDEF vp) {
        LIBR lp;
        double d = 0;
        int lasterrno;

        if (vp != null) {
            lp = vp.lib;
        } else {
            lp = liblookup(fname);
        }
        if (lp == null) {
//	eputs(fname);
//	eputs(": undefined function\n");
//	quit(1);
            System.exit(1);
        }
        lasterrno = CALEXPR.errno;
        CALEXPR.errno = 0;
        d = lp.fval(lp.fname);
//#ifdef  isnan
        if (CALEXPR.errno == 0) {
            if (Double.isNaN(d)) {
                CALEXPR.errno = CALCOMP.EDOM;
            } else if (Double.isInfinite(d)) {
                CALEXPR.errno = CALCOMP.ERANGE;
            }
        }
//#endif
        if (CALEXPR.errno == CALCOMP.EDOM || CALEXPR.errno == CALCOMP.ERANGE) {
//	wputs(fname);
            if (CALEXPR.errno == CALCOMP.EDOM) {
//		wputs(": domain error\n");
            } else if (CALEXPR.errno == CALCOMP.ERANGE) {
//		wputs(": range error\n");
            } else {
//		wputs(": error in call\n");
            }
            return (0.0);
        }
//    errno = lasterrno;
        return (d);
    }
    /*
     *  Library functions:
     */

    public static class l_if implements LIBF {

        static double l_if(String nm) /* if(cond, then, else) conditional expression */ /* cond evaluates true if greater than zero */ {
            if (argument(1) > 0.0) {
                return (argument(2));
            } else {
                return (argument(3));
            }
        }

        @Override
        public double fval(String nm) {
            return l_if(nm);
        }
    }

    public static class l_select implements LIBF {

        static double l_select(String nm) /* return argument #(A1+1) */ {
            int n;

            n = (int) (argument(1) + .5);
            if (n == 0) {
                return (nargum() - 1);
            }
            if (n < 1 || n > nargum() - 1) {
                CALEXPR.errno = CALCOMP.EDOM;
                return (0.0);
            }
            return (argument(n + 1));
        }

        @Override
        public double fval(String nm) {
            return l_select(nm);
        }
    }

    public static class l_rand implements LIBF {

        static double l_rand(String nm) /* random function between 0 and 1 */ {
            double x;

            x = argument(1);
            x *= 1.0 / (1.0 + x * x) + 2.71828182845904;
            x += .785398163397447 - Math.floor(x);
            x = 1e5 / x;
            return (x - Math.floor(x));
        }

        @Override
        public double fval(String nm) {
            return l_rand(nm);
        }
    }

    public static class l_floor implements LIBF {

        static double l_floor(String nm) /* return largest integer not greater than arg1 */ {
            return (Math.floor(argument(1)));
        }

        @Override
        public double fval(String nm) {
            return l_floor(nm);
        }
    }

    public static class l_ceil implements LIBF {

        static double l_ceil(String nm) /* return smallest integer not less than arg1 */ {
            return (Math.ceil(argument(1)));
        }

        @Override
        public double fval(String nm) {
            return l_ceil(nm);
        }
    }

    public static class l_sqrt implements LIBF {

        static double l_sqrt(String nm) {
            return (Math.sqrt(argument(1)));
        }

        @Override
        public double fval(String nm) {
            return l_sqrt(nm);
        }
    }

    public static class l_sin implements LIBF {

        static double l_sin(String nm) {
            return (Math.sin(argument(1)));
        }

        @Override
        public double fval(String nm) {
            return l_sin(nm);
        }
    }

    public static class l_cos implements LIBF {

        static double l_cos(String nm) {
            return (Math.cos(argument(1)));
        }

        @Override
        public double fval(String nm) {
            return l_cos(nm);
        }
    }

    public static class l_tan implements LIBF {

        static double l_tan(String nm) {
            return (Math.tan(argument(1)));
        }

        @Override
        public double fval(String nm) {
            return l_tan(nm);
        }
    }

    public static class l_asin implements LIBF {

        static double l_asin(String nm) {
            return (Math.asin(argument(1)));
        }

        @Override
        public double fval(String nm) {
            return l_asin(nm);
        }
    }

    public static class l_acos implements LIBF {

        static double l_acos(String nm) {
            return (Math.acos(argument(1)));
        }

        @Override
        public double fval(String nm) {
            return l_acos(nm);
        }
    }

    public static class l_atan implements LIBF {

        static double l_atan(String nm) {
            return (Math.atan(argument(1)));
        }

        @Override
        public double fval(String nm) {
            return l_atan(nm);
        }
    }

    public static class l_atan2 implements LIBF {

        static double l_atan2(String nm) {
            return (Math.atan2(argument(1), argument(2)));
        }

        @Override
        public double fval(String nm) {
            return l_atan2(nm);
        }
    }

    public static class l_exp implements LIBF {

        static double l_exp(String nm) {
            return (Math.exp(argument(1)));
        }

        @Override
        public double fval(String nm) {
            return l_exp(nm);
        }
    }

    public static class l_log implements LIBF {

        static double l_log(String nm) {
            return (Math.log(argument(1)));
        }

        @Override
        public double fval(String nm) {
            return l_log(nm);
        }
    }

    public static class l_log10 implements LIBF {

        static double l_log10(String nm) {
            return (Math.log10(argument(1)));
        }

        @Override
        public double fval(String nm) {
            return l_log10(nm);
        }
    }
}
