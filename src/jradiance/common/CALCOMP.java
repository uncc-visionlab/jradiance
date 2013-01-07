/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

import jradiance.common.CALFUNC.LIBF;

/**
 *
 * @author arwillis
 */
public class CALCOMP {
    /*
     *  CALCOMP.h - header file for expression parser.
     */

    public static final int VAR = 1;
    public static final int NUM = 2;
    public static final int UMINUS = 3;
    public static final int CHAN = 4;
    public static final int FUNC = 5;
    public static final int ARG = 6;
    public static final int CLKT = 7;
    public static final int SYM = 8;
    /* also: '+', '-', '*', '/', '^', '=', ':' */

    public static class LIBR implements CALFUNC.LIBF {

        String fname;		/* function name */

        short nargs;		/* # of required arguments */

        short atyp;		/* assignment type (':' or '=') */
        //double  (*f)(char *);	/* pointer to function */
        //abstract double f(String name); /* pointer to function */
        LIBF f=null;/* pointer to function */
        LIBR(String fname, short nargs, short atyp, LIBF f) {
            this.fname = fname;
            this.nargs = nargs;
            this.atyp = atyp;
            this.f = f;
        }

        @Override
        public double fval(String nm) {
            if (f==null)
            throw new UnsupportedOperationException("Not supported yet.");
            else 
                return f.fval(nm);
        }
    }  		/* a library function */


    public static class VARDEF {

        public String name;		/* variable name */

        public int nlinks;		/* number of references */

        public EPNODE def;	/* definition */

        LIBR lib;			/* library definition */

        public VARDEF next;	/* next in hash list */

    }

    public static class EPNODE {

        public class value {

            EPNODE kid;	/* first child */

            double num;		/* number */

            String name;		/* symbol name */

            int chan;		/* channel number */

            long tick;	/* timestamp */

            VARDEF ln = null; /* link */

        }
        value v = new value();/* value */

        EPNODE sibling;	/* next child this level */

        int type;			/* node type */

    }  	/* an expression node */

    public static int RMAXWORD = 127;		/* maximum word/id length */

    public static char CNTXMARK = '`';		/* context mark */


    static boolean isid(char c) {
        return (Character.isLetterOrDigit(c) || (c) == '_' || (c) == '.' || (c) == CNTXMARK);
    }

    public static double evalue(EPNODE ep) {
        return CALEXPR.eoper[ep.type].op(ep);
    }
    /* flags to set in esupport */
    public static final int E_VARIABLE = 001;
    public static final int E_FUNCTION = 002;
    public static final int E_INCHAN = 004;
    public static final int E_OUTCHAN = 010;
    public static final int E_RCONST = 020;
    public static final int E_REDEFW = 040;
    public static final int EDOM = 33;
    public static final int ERANGE = 34;
//extern double  (*eoper[])(EPNODE *);
//extern unsigned long  eclock;
//extern unsigned int  esupport;
//extern EPNODE	*curfunc;
//extern int  nextc;
//extern int  eofc;
//
//					/* defined in biggerlib.c */
//extern void biggerlib(void);
//
//					/* defined in caldefn.c */
//extern void	fcompile(char *fname);
//extern void	scompile(char *str, char *fname, int ln);
//extern double	varvalue(char *vname);
//extern double	evariable(EPNODE *ep);
//extern void	varset(char *vname, int assign, double val);
//extern void	dclear(char *name);
//extern void	dremove(char *name);
//extern int	vardefined(char *name);
//extern char	*setcontext(char *ctx);
//extern char	*pushcontext(char *ctx);
//extern char	*popcontext(void);
//extern char	*qualname(char *nam, int lvl);
//extern int	incontext(char *qn);
//extern void	chanout(void (*cs)(int n, double v));
//extern void	dcleanup(int lvl);
//extern EPNODE	*dlookup(char *name);
//extern VARDEF	*varlookup(char *name);
//extern VARDEF	*varinsert(char *name);
//extern void	varfree(VARDEF *ln);
//extern EPNODE	*dfirst(void);
//extern EPNODE	*dnext(void);
//extern EPNODE	*dpop(char *name);
//extern void	dpush(char *nm, EPNODE *ep);
//extern void	addchan(EPNODE *sp);
//extern void	getstatement(void);
//extern EPNODE	*getdefn(void);
//extern EPNODE	*getchan(void);
//					/* defined in calexpr.c */
//extern EPNODE	*eparse(char *expr);
//extern double	eval(char *expr);
//extern int	epcmp(EPNODE *ep1, EPNODE *ep2);
//extern void	epfree(EPNODE *epar);
//extern EPNODE	*ekid(EPNODE *ep, int n);
//extern int	nekids(EPNODE *ep);
//extern void	initfile(FILE *fp, char *fn, int ln);
//extern void	initstr(char *s, char *fn, int ln);
//extern void	getscanpos(char **fnp, int *lnp, char **spp, FILE **fpp);
//extern int	scan(void);
//extern char	*long2ascii(long l);
//extern void	syntax(char *err);
//extern void	addekid(EPNODE *ep, EPNODE *ekid);
//extern char	*getname(void);
//extern int	getinum(void);
//extern double	getnum(void);
//extern EPNODE	*getE1(void);
//extern EPNODE	*getE2(void);
//extern EPNODE	*getE3(void);
//extern EPNODE	*getE4(void);
//extern EPNODE	*getE5(void);
//extern EPNODE	*rconst(EPNODE *epar);
//extern int	isconstvar(EPNODE *ep);
//extern int	isconstfun(EPNODE *ep);
//					/* defined in calfunc.c */
//extern int	fundefined(char *fname);
//extern double	funvalue(char *fname, int n, double *a);
//extern void	funset(char *fname, int nargs, int assign,
//				double (*fptr)(char *));
//extern int	nargum(void);
//extern double	argument(int n);
//extern VARDEF	*argf(int n);
//extern char	*argfun(int n);
//extern double	efunc(EPNODE *ep);
//extern LIBR	*liblookup(char *fname);
//extern void	libupdate(char *fn);
//					/* defined in calprnt.c */
//extern void	eprint(EPNODE *ep, FILE *fp);
//extern void	dprint(char *name, FILE *fp);
//					/* defined by client */
//extern double	chanvalue(int n);
//
//    
}
