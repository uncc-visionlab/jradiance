/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import jradiance.common.CALCOMP.EPNODE;
import jradiance.common.CALCOMP.VARDEF;
import jradiance.common.CALEXPR.EOPER;

/**
 *
 * @author arwillis
 */
public class CALDEFN {
    /*
     *  Store variable definitions.
     *
     *  7/1/85  Greg Ward
     *
     *  11/11/85  Added conditional compiles (OUTCHAN) for control output.
     *
     *  4/2/86  Added conditional compiles for function definitions (FUNCTION).
     *
     *  1/15/88  Added clock for caching of variable values.
     *
     *  11/16/88  Added VARDEF structure for hard linking.
     *
     *  5/31/90  Added conditional compile (REDEFW) for redefinition warning.
     *
     *  4/23/91  Added ':' assignment for constant expressions
     *
     *  8/7/91  Added optional context path to append to variable names
     *
     *  5/17/2001  Fixed clock counter wrapping behavior
     *
     *  2/19/03	Eliminated conditional compiles in favor of esupport extern.
     */

    public static final int NHASH = 521;		/* hash size (a prime!) */


    public static int hash(String s) {
        return (SAVESTR.shash(s.toCharArray()) % NHASH);
    }
    static final long MAXCLOCK = (1L << 31);	/* clock wrap value */

// CANNOT DO UNSIGNED LONG 
    public static long eclock = 0;		/* value storage timer */

    public static final int MAXCNTX = 1023;		/* maximum context length */

    public static String context = "";	/* current context path */

    static VARDEF[] hashtbl = new VARDEF[NHASH];		/* definition list */

    static int htndx;			/* index for */

    static VARDEF htpos;			/* ...dfirst() and */

    static EPNODE ochpos;			/* ...dnext */

    static EPNODE outchan;
    public static EPNODE curfunc = null;

    static String dname(EPNODE ep) {
        return ((ep).v.kid.type == CALCOMP.SYM ? (ep).v.kid.v.name : (ep).v.kid.v.kid.v.name);
    }

    public static void fcompile( /* get definitions from a file */
            String fname) throws IOException {
        InputStream fp;        
        if (fname == null) {
            fp = System.in;
        } else if ((fp = new FileInputStream(fname)) == null) {
//	eputs(fname);
//	eputs(": cannot open\n");
//	quit(1);
            System.exit(1);
        }
        fcompile(fname, fp);
    }    
    public static void fcompile( /* get definitions from a file */
            String fname, InputStream fp) throws IOException {
        CALEXPR.initfile(fp, fname, 0);
        while (CALEXPR.nextc != CALEXPR.EOF) {
            getstatement();
        }
        if (fname != null) {
            fp.close();
        }
    }

    public static void scompile( /* get definitions from a string */
            String str,
            String fn,
            int ln) {
        CALEXPR.initstr(str, fn, ln);
        while (CALEXPR.nextc != CALEXPR.EOF) {
            getstatement();
        }
    }

    public static double varvalue( /* return a variable's value */
            String vname) {
        return (dvalue(vname, dlookup(vname)));
    }

    static class EVARIABLE implements EOPER {

        double evariable( /* evaluate a variable */
                EPNODE ep) {
            VARDEF dp = ep.v.ln;

            return (dvalue(dp.name, dp.def));
        }

        @Override
        public double op(EPNODE ep) {
            return evariable(ep);
        }
    }

    public static void varset( /* set a variable's value */
            String vname,
            int assign,
            double val) {
        String qname;
        EPNODE ep1, ep2;
        /* get qualified name */
        qname = qualname(vname, 0);
        /* check for quick set */
        if ((ep1 = dlookup(qname)) != null && ep1.v.kid.type == CALCOMP.SYM) {
            ep2 = ep1.v.kid.sibling;
            if (ep2.type == CALCOMP.NUM) {
                ep2.v.num = val;
                ep1.type = assign;
                return;
            }
        }
        /* hand build definition */
        ep1 = CALEXPR.newnode();
        ep1.type = assign;
        ep2 = CALEXPR.newnode();
        ep2.type = CALCOMP.SYM;
        ep2.v.name = SAVESTR.savestr(vname);
        CALEXPR.addekid(ep1, ep2);
        ep2 = CALEXPR.newnode();
        ep2.type = CALCOMP.NUM;
        ep2.v.num = val;
        CALEXPR.addekid(ep1, ep2);
        dremove(qname);
        dpush(qname, ep1);
    }

    static void dclear( /* delete variable definitions of name */
            String name) {
        EPNODE ep;

        while ((ep = dpop(name)) != null) {
            if (ep.type == ':') {
                dpush(name, ep);		/* don't clear constants */
                return;
            }
            CALEXPR.epfree(ep);
        }
    }

    static void dremove( /* delete all definitions of name */
            String name) {
        EPNODE ep;

        while ((ep = dpop(name)) != null) {
            CALEXPR.epfree(ep);
        }
    }

    public static boolean vardefined( /* return non-zero if variable defined */
            String name) {
        EPNODE dp;

        return ((dp = dlookup(name)) != null && dp.v.kid.type == CALCOMP.SYM);
    }

    public static String setcontext( /* set a new context path */
            String ctx) {
        String cpp;
        int ctxidx = 0;
        if (ctx == null || ctx.length() == 0) {
            return (context);		/* just asking */
        }
        while (ctx.charAt(ctxidx) == CALCOMP.CNTXMARK) {
            ctxidx++;				/* skip past marks */
        }
        if (ctx.charAt(ctxidx) == 0) {
            context = "";		/* empty means clear context */
            return (context);
        }
        cpp = context;			/* start context with mark */
        int cppidx = 0;
        //*cpp++ = CALCOMP.CNTXMARK;
        cpp += CALCOMP.CNTXMARK;
        cppidx++;
        do {				/* carefully copy new context */
            if (cppidx >= MAXCNTX) {
                break;			/* just copy what we can */
            }
            if (CALCOMP.isid(ctx.charAt(ctxidx))) {
                cpp += ctx.charAt(ctxidx);
            } else {
                cpp += '_';
            }
            cppidx++;
            ctxidx++;
        } while (ctx.charAt(ctxidx) != 0);
        while (cpp.charAt(cppidx) == CALCOMP.CNTXMARK) /* cannot end in context mark */ {
            cppidx--;
        }
        cpp = cpp.substring(0, cppidx);
        return (context);
    }
//char *
//pushcontext(		/* push on another context */
//	char  *ctx
//)
//{
//    char  oldcontext[MAXCNTX+1];
//    register int  n;
//
//    strcpy(oldcontext, context);	/* save old context */
//    setcontext(ctx);			/* set new context */
//    n = strlen(context);		/* tack on old */
//    if (n+strlen(oldcontext) > MAXCNTX) {
//	strncpy(context+n, oldcontext, MAXCNTX-n);
//	context[MAXCNTX] = '\0';
//    } else
//	strcpy(context+n, oldcontext);
//    return(context);
//}
//
//
//char *
//popcontext(void)			/* pop off top context */
//{
//    register char  *cp1, *cp2;
//
//    if (!context[0])			/* nothing left to pop */
//	return(context);
//    cp2 = context;			/* find mark */
//    while (*++cp2 && *cp2 != CNTXMARK)
//	;
//    cp1 = context;			/* copy tail to front */
//    while ( (*cp1++ = *cp2++) )
//	;
//    return(context);
//}
//
//
    static String nambuf;

    static String qualname( /* get qualified name */
            String nam,
            int lvl) {
//    static char	 nambuf[RMAXWORD+1];
//    register char  *cp = nambuf, *cpp;
        String cpp;
        int namidx = 0, cppidx = 0;
        /* check for explicit local */
        if (nam.length() > 0 && nam.charAt(namidx) == CALCOMP.CNTXMARK) {
            if (lvl > 0) /* only action is to refuse search */ {
                return (null);
            } else {
                namidx++;
            }
        } else if (nam.equals(nambuf)) /* check for repeat call */ {
            return (lvl > 0 ? null : nam);
        }
        /* copy name to static buffer */
//    while (nam.charAt(namidx)!=0) {
//	if (cp >= nambuf+RMAXWORD)
//		goto toolong;
//	*cp++ = *nam++;
//    }
        nambuf = nam.substring(namidx);
        /* check for explicit global */
        if (nambuf.length() > 0 && nambuf.charAt(nambuf.length() - 1) == CALCOMP.CNTXMARK) {
//    if (cp > nambuf && cp[-1] == CNTXMARK) {
            if (lvl > 0) {
                return (null);
            }
            nambuf = nambuf.substring(0, nambuf.length() - 1);
//	*--cp = '\0';
            return (nambuf);		/* already qualified */
        }
        cpp = context;		/* else skip the requested levels */

        while (lvl-- > 0) {
            if (cpp.charAt(cppidx) == 0) {
                return (null);	/* return null if past global level */
            }
            while (cpp.charAt(++cppidx) != 0 && cpp.charAt(cppidx) != CALCOMP.CNTXMARK)
	    ;
        }
        nambuf += cpp.substring(cppidx);
//    while (cpp.charAt(cppidx)!=0) {		/* copy context to static buffer */
//	if (cp >= nambuf+RMAXWORD)
//	    goto toolong;
//	*cp++ = *cpp++;
//    }
//toolong:
//    *cp = '\0';
        return (nambuf);		/* return qualified name */
    }

//int
//incontext(			/* is qualified name in current context? */
//	register char  *qn
//)
//{
//    if (!context[0])			/* global context accepts all */
//	return(1);
//    while (*qn && *qn != CNTXMARK)	/* find context mark */
//	qn++;
//    return(!strcmp(qn, context));
//}
//
//
//void
//chanout(			/* set output channels */
//	void  (*cs)(int n, double v)
//)
//{
//    register EPNODE  *ep;
//
//    for (ep = outchan; ep != null; ep = ep.sibling)
//	(*cs)(ep.v.kid.v.chan, evalue(ep.v.kid.sibling));
//
//}
//
//
//void
//dcleanup(		/* clear definitions (0.vars,1.output,2.consts) */
//	int  lvl
//)
//{
//    register int  i;
//    register VARDEF  *vp;
//    register EPNODE  *ep;
//				/* if context is global, clear all */
//    for (i = 0; i < NHASH; i++)
//	for (vp = hashtbl[i]; vp != null; vp = vp.next)
//	    if (incontext(vp.name)) {
//		if (lvl >= 2)
//		    dremove(vp.name);
//		else
//		    dclear(vp.name);
//	    }
//    if (lvl >= 1) {
//	for (ep = outchan; ep != null; ep = ep.sibling)
//	    epfree(ep);
//	outchan = null;
//    }
//}
    static EPNODE dlookup( /* look up a definition */
            String name) {
        VARDEF vp;

        if ((vp = varlookup(name)) == null) {
            return (null);
        }
        return (vp.def);
    }

    static VARDEF varlookup( /* look up a variable */
            String name) {
        int lvl = 0;
        String qname;
        VARDEF vp;
        /* find most qualified match */
        while ((qname = qualname(name, lvl++)) != null) {
//            System.out.print("looking up " + qname + " at level " + (lvl - 1));
            for (vp = hashtbl[hash(qname)]; vp != null; vp = vp.next) {
                if (vp.name.equals(qname)) {
//                    System.out.print(" found it!\n");
                    return (vp);
                }
            }
        }
//        System.out.print(" it was not found.\n");
        return (null);
    }

    static VARDEF varinsert( /* get a link to a variable */
            String name) {
        VARDEF vp;
        int hv;

        if ((vp = varlookup(name)) != null) {
            vp.nlinks++;
            return (vp);
        }
        vp = new VARDEF();
        vp.lib = CALFUNC.liblookup(name);
        if (vp.lib == null) /* if name not in library */ {
            name = qualname(name, 0);	/* use fully qualified version */
        }
        hv = hash(name);
        vp.name = SAVESTR.savestr(name);
        vp.nlinks = 1;
        vp.def = null;
        vp.next = hashtbl[hv];
        hashtbl[hv] = vp;
        return (vp);
    }

    static void libupdate( /* update library links */
            String fn) {
        int i;
        VARDEF vp;
        /* if fn is null then relink all */
        for (i = 0; i < NHASH; i++) {
            for (vp = hashtbl[i]; vp != null; vp = vp.next) {
                if (vp.lib != null || fn == null || fn.compareTo(vp.name) == 0) {
                    vp.lib = CALFUNC.liblookup(vp.name);
                }
            }
        }
    }

    static void varfree( /* release link to variable */
            VARDEF ln) {
        VARDEF vp;
        int hv;

        if (--ln.nlinks > 0) {
            return;				/* still active */
        }

        hv = hash(ln.name);
        vp = hashtbl[hv];
        if (vp == ln) {
            hashtbl[hv] = vp.next;
        } else {
            while (vp.next != ln) /* must be in list */ {
                vp = vp.next;
            }
            vp.next = ln.next;
        }
//    freestr(ln.name);
//    CALEXPR.efree((char *)ln);
    }

//EPNODE *
//dfirst(void)			/* return pointer to first definition */
//{
//    htndx = 0;
//    htpos = null;
//    ochpos = outchan;
//    return(dnext());
//}
//
//
//EPNODE *
//dnext(void)				/* return pointer to next definition */
//{
//    register EPNODE  *ep;
//    register char  *nm;
//
//    while (htndx < NHASH) {
//	if (htpos == null)
//		htpos = hashtbl[htndx++];
//	while (htpos != null) {
//	    ep = htpos.def;
//	    nm = htpos.name;
//	    htpos = htpos.next;
//	    if (ep != null && incontext(nm))
//		return(ep);
//	}
//    }
//    if ((ep = ochpos) != null)
//	ochpos = ep.sibling;
//    return(ep);
//}
//
    static EPNODE dpop( /* pop a definition */
            String name) {
        VARDEF vp;
        EPNODE dp;

        if ((vp = varlookup(name)) == null || vp.def == null) {
            return (null);
        }
        dp = vp.def;
        vp.def = dp.sibling;
        varfree(vp);
        return (dp);
    }

    static void dpush( /* push on a definition */
            String nm,
            EPNODE ep) {
        VARDEF vp;
//        System.out.println("inserted variable " + nm);
        vp = varinsert(nm);
        ep.sibling = vp.def;
        vp.def = ep;
    }

    static void addchan( /* add an output channel assignment */
            EPNODE sp) {
        int ch = sp.v.kid.v.chan;
        EPNODE ep, epl;

        for (epl = null, ep = outchan; ep != null; epl = ep, ep = ep.sibling) {
            if (ep.v.kid.v.chan >= ch) {
                if (epl != null) {
                    epl.sibling = sp;
                } else {
                    outchan = sp;
                }
                if (ep.v.kid.v.chan > ch) {
                    sp.sibling = ep;
                } else {
                    sp.sibling = ep.sibling;
                    CALEXPR.epfree(ep);
                }
                return;
            }
        }
        if (epl != null) {
            epl.sibling = sp;
        } else {
            outchan = sp;
        }
        sp.sibling = null;

    }

    static void getstatement() /* get next statement */ {
        EPNODE ep;
        String qname;
        VARDEF vdef;

        if (CALEXPR.nextc == ';') {		/* empty statement */
            CALEXPR.scan();
            return;
        }
        if ((CALEXPR.esupport & CALCOMP.E_OUTCHAN) != 0
                && CALEXPR.nextc == '$') {		/* channel assignment */
            ep = getchan();
            addchan(ep);
        } else {				/* ordinary definition */
            ep = getdefn();
            qname = qualname(dname(ep), 0);
            if ((CALEXPR.esupport & CALCOMP.E_REDEFW) != 0 && (vdef = varlookup(qname)) != null) {
                if (vdef.def != null && CALEXPR.epcmp(ep, vdef.def) != 0) {
//		wputs(qname);
                    if (vdef.def.type == ':') {
//		    wputs(": redefined constant expression\n");
                    } else {
//		    wputs(": redefined\n");
                    }
                } else if (ep.v.kid.type == CALCOMP.FUNC && vdef.lib != null) {
//		wputs(qname);
//		wputs(": definition hides library function\n");
                }
            }
            if (ep.type == ':') {
                dremove(qname);
            } else {
                dclear(qname);
            }
            dpush(qname, ep);
        }
        if (CALEXPR.nextc != CALEXPR.EOF) {
            if (CALEXPR.nextc != ';') {
//	    syntax("';' expected");
            }
            CALEXPR.scan();
        }
    }

    static EPNODE getdefn() /* A . SYM = E1 */ /*	SYM : E1 */ /*	FUNC(SYM,..) = E1 */ /*	FUNC(SYM,..) : E1 */ {
        EPNODE ep1, ep2;

        if (!Character.isLetter(CALEXPR.nextc) && CALEXPR.nextc != CALCOMP.CNTXMARK) {
//	syntax("illegal variable name");
        }

        ep1 = CALEXPR.newnode();
        ep1.type = CALCOMP.SYM;
        ep1.v.name = SAVESTR.savestr(CALEXPR.getname());

        if ((CALEXPR.esupport & CALCOMP.E_FUNCTION) != 0 && CALEXPR.nextc == '(') {
            ep2 = CALEXPR.newnode();
            ep2.type = CALCOMP.FUNC;
            CALEXPR.addekid(ep2, ep1);
            ep1 = ep2;
            do {
                CALEXPR.scan();
                if (!Character.isLetter(CALEXPR.nextc)) {
//		syntax("illegal parameter name");
                }
                ep2 = CALEXPR.newnode();
                ep2.type = CALCOMP.SYM;
                ep2.v.name = SAVESTR.savestr(CALEXPR.getname());
                CALEXPR.addekid(ep1, ep2);
            } while (CALEXPR.nextc == ',');
            if (CALEXPR.nextc != ')') {
//	    syntax("')' expected");
            }
            CALEXPR.scan();
            curfunc = ep1;
        }

        if (CALEXPR.nextc != '=' && CALEXPR.nextc != ':') {
//	syntax("'=' or ':' expected");
        }

        ep2 = CALEXPR.newnode();
        ep2.type = CALEXPR.nextc;
        CALEXPR.scan();
        CALEXPR.addekid(ep2, ep1);
        CALEXPR.addekid(ep2, CALEXPR.getE1());

        if (ep1.type == CALCOMP.SYM && ep1.sibling.type != CALCOMP.NUM) {
            ep1 = CALEXPR.newnode();
            ep1.type = CALCOMP.CLKT;
            ep1.v.tick = 0;
            CALEXPR.addekid(ep2, ep1);
            ep1 = CALEXPR.newnode();
            ep1.type = CALCOMP.NUM;
            CALEXPR.addekid(ep2, ep1);
        }
        curfunc = null;

        return (ep2);
    }

    static EPNODE getchan() /* A . $N = E1 */ {
        EPNODE ep1, ep2;

        if (CALEXPR.nextc != '$') {
//	syntax("missing '$'");
        }
        CALEXPR.scan();

        ep1 = CALEXPR.newnode();
        ep1.type = CALCOMP.CHAN;
        ep1.v.chan = CALEXPR.getinum();

        if (CALEXPR.nextc != '=') {
//	syntax("'=' expected");
        }
        CALEXPR.scan();

        ep2 = CALEXPR.newnode();
        ep2.type = '=';
        CALEXPR.addekid(ep2, ep1);
        CALEXPR.addekid(ep2, CALEXPR.getE1());

        return (ep2);
    }

    /*
     *  The following routines are for internal use only:
     */
    static double /* evaluate a variable */ dvalue(String name, EPNODE d) {
        EPNODE ep1, ep2;

        if (d == null || d.v.kid.type != CALCOMP.SYM) {
//	eputs(name);
//	eputs(": undefined variable\n");
//	quit(1);
            System.exit(1);
        }
        ep1 = d.v.kid.sibling;			/* get expression */
        if (ep1.type == CALCOMP.NUM) {
            return (ep1.v.num);			/* return if number */
        }
        ep2 = ep1.sibling;				/* check time */
        if (eclock >= MAXCLOCK) {
            eclock = 1;				/* wrap clock counter */
        }
        if (ep2.v.tick < MAXCLOCK
                && (ep2.v.tick == 0) | (ep2.v.tick != eclock)) {
            ep2.v.tick = d.type == ':' ? MAXCLOCK : eclock;
            ep2 = ep2.sibling;
            ep2.v.num = CALCOMP.evalue(ep1);		/* needs new value */
        } else {
            ep2 = ep2.sibling;			/* else reuse old value */
        }

        return (ep2.v.num);
    }
}
