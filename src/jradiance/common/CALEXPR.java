/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

import java.io.IOException;
import java.io.InputStream;
import jradiance.common.CALCOMP.EPNODE;
import jradiance.common.CALCOMP.LIBR;
import jradiance.common.CALDEFN.EVARIABLE;
import jradiance.common.CALFUNC.EFUNC;
import jradiance.rt.FUNC;

/**
 *
 * @author arwillis
 */
public class CALEXPR {
    /*
     *  Compute data values using expression parser
     *
     *  7/1/85  Greg Ward
     *
     *  11/11/85  Made channel input conditional with (INCHAN) compiles.
     *
     *  4/2/86  Added conditional compiles for function definitions (FUNCTION).
     *
     *  1/29/87  Made variables conditional (VARIABLE)
     *
     *  5/19/88  Added constant subexpression elimination (RCONST)
     *
     *  2/19/03	Eliminated conditional compiles in favor of esupport extern.
     */

    public static final int EOF = -1;
    public static final int MAXLINE = 256;		/* maximum line length */


    public static EPNODE newnode() {
        return new EPNODE();
    }

    static boolean isdecimal(char c) {
        return (Character.isDigit(c) || (c) == '.');
    }
//
//static double  euminus(EPNODE *), eargument(EPNODE *), enumber(EPNODE *);
//static double  echannel(EPNODE *);
//static double  eadd(EPNODE *), esubtr(EPNODE *),
//               emult(EPNODE *), edivi(EPNODE *),
//               epow(EPNODE *);
//static double  ebotch(EPNODE *);
//
    public static int esupport = /* what to support */
            CALCOMP.E_VARIABLE | CALCOMP.E_FUNCTION;
    static int eofc = 0;				/* optional end-of-file character */

    public static int nextc;				/* lookahead character */

    // added for compilation purposes comes from std C errno.h in C code
    public static int errno = 0;

    public interface EOPER {

        double op(EPNODE ep);
    }
    static final EOPER ebotch = new EBOTCH();
    static final EOPER evariable = new EVARIABLE();
    static final EOPER enumber = new ENUMBER();
    static final EOPER euminus = new EUMINUS();
    static final EOPER echannel = new ECHANNEL();
    static final EOPER efunc = new EFUNC();
    static final EOPER eargument = new EARGUMENT();
    static final EOPER emult = new EMULT();
    static final EOPER eadd = new EMULT();
    static final EOPER esubtr = new ESUBTR();
    static final EOPER edivi = new EDIVI();
    static final EOPER epow = new EPOW();
    static EOPER[] eoper = {
        //double	(*eoper[])(EPNODE *) = {	/* expression operations */
        ebotch,
        evariable,
        enumber,
        euminus,
        echannel,
        efunc,
        eargument,
        ebotch,
        ebotch,
        null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null,
        emult,
        eadd,
        null,
        esubtr,
        null,
        edivi,
        null, null, null, null, null, null, null, null, null, null,
        ebotch,
        null, null,
        ebotch,
        null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null,
        null, null, null, null, null,
        epow,};
    static InputStream infp;		/* input file pointer */

    static char[] linbuf;			/* line buffer */

    static String infile;			/* input file name */

    static int lineno;			/* input line number */

    static int linepos;			/* position in buffer */


    EPNODE eparse( /* parse an expression string */
            String expr) {
        EPNODE ep = null;

        initstr(expr, null, 0);
        CALDEFN.curfunc = null;
        System.out.println("Unsupported!!!!");
//    ep = getE1();
        if (nextc != EOF) {
//	syntax("unexpected character");
        }
        return (ep);
    }

    double eval( /* evaluate an expression string */
            String expr) {
        EPNODE ep;
        double rval;

        ep = eparse(expr);
        rval = CALCOMP.evalue(ep);
        epfree(ep);
        return (rval);
    }

    static int epcmp( /* compare two expressions for equivalence */
            EPNODE ep1,
            EPNODE ep2) {
        double d;

        if (ep1.type != ep2.type) {
            return (1);
        }

        switch (ep1.type) {

            case CALCOMP.VAR:
                return (ep1.v.ln != ep2.v.ln) ? 1 : 0;

            case CALCOMP.NUM:
                if (ep2.v.num == 0) {
                    return (ep1.v.num != 0) ? 1 : 0;
                }
                d = ep1.v.num / ep2.v.num;
                return ((d > 1.000000000001) | (d < 0.999999999999)) ? 1 : 0;

            case CALCOMP.CHAN:
            case CALCOMP.ARG:
                return (ep1.v.chan != ep2.v.chan) ? 1 : 0;

            case '=':
            case ':':
                return (epcmp(ep1.v.kid.sibling, ep2.v.kid.sibling));

            case CALCOMP.CLKT:
            case CALCOMP.SYM:			/* should never get this one */
                return (0);

            default:
                ep1 = ep1.v.kid;
                ep2 = ep2.v.kid;
                while (ep1 != null) {
                    if (ep2 == null) {
                        return (1);
                    }
                    if (epcmp(ep1, ep2) != 0) {
                        return (1);
                    }
                    ep1 = ep1.sibling;
                    ep2 = ep2.sibling;
                }
                return (ep2 != null) ? 1 : 0;
        }
    }

    static void epfree( /* free a parse tree */
            EPNODE epar) {
        EPNODE ep;

        switch (epar.type) {

            case CALCOMP.VAR:
//	    varfree(epar.v.ln);
                break;

            case CALCOMP.SYM:
//	    freestr(epar.v.name);
                break;

            case CALCOMP.NUM:
            case CALCOMP.CHAN:
            case CALCOMP.ARG:
            case CALCOMP.CLKT:
                break;

            default:
                while ((ep = epar.v.kid) != null) {
                    epar.v.kid = ep.sibling;
                    epfree(ep);
                }
                break;

        }

//    efree((char *)epar);
    }

    static class EARGUMENT implements EOPER {

        /* the following used to be a switch */
        static double eargument(
                EPNODE ep) {
            return (CALFUNC.argument(ep.v.chan));
        }

        @Override
        public double op(EPNODE ep) {
            return eargument(ep);
        }
    }

    static class ENUMBER implements EOPER {

        static double enumber(
                EPNODE ep) {
            return (ep.v.num);
        }

        @Override
        public double op(EPNODE ep) {
            return enumber(ep);
        }
    }

    static class EUMINUS implements EOPER {

        static double euminus(
                EPNODE ep) {
            EPNODE ep1 = ep.v.kid;

            return (-CALCOMP.evalue(ep1));
        }

        @Override
        public double op(EPNODE ep) {
            return euminus(ep);
        }
    }

    static class ECHANNEL implements EOPER {

        static double echannel(
                EPNODE ep) {
//            return (CHANVALUE.chanvalue(ep.v.chan));
            return (FUNC.chanvalue(ep.v.chan));
        }

        @Override
        public double op(EPNODE ep) {
//            throw new UnsupportedOperationException("Not supported yet.");
            return echannel(ep);
        }
    }

    static class EADD implements EOPER {

        static double eadd(
                EPNODE ep) {
            EPNODE ep1 = ep.v.kid;

            return (CALCOMP.evalue(ep1) + CALCOMP.evalue(ep1.sibling));
        }

        @Override
        public double op(EPNODE ep) {
            return eadd(ep);
        }
    }

    static class ESUBTR implements EOPER {

        static double esubtr(
                EPNODE ep) {
            EPNODE ep1 = ep.v.kid;

            return (CALCOMP.evalue(ep1) - CALCOMP.evalue(ep1.sibling));
        }

        @Override
        public double op(EPNODE ep) {
            return esubtr(ep);
        }
    }

    static class EMULT implements EOPER {

        static double emult(
                EPNODE ep) {
            EPNODE ep1 = ep.v.kid;

            return (CALCOMP.evalue(ep1) * CALCOMP.evalue(ep1.sibling));
        }

        @Override
        public double op(EPNODE ep) {
            return emult(ep);
        }
    }

    static class EDIVI implements EOPER {

        static double edivi(
                EPNODE ep) {
            EPNODE ep1 = ep.v.kid;
            double d;

            d = CALCOMP.evalue(ep1.sibling);
            if (d == 0.0) {
//	wputs("Division by zero\n");
                errno = CALCOMP.ERANGE;
                return (0.0);
            }
            return (CALCOMP.evalue(ep1) / d);
        }

        @Override
        public double op(EPNODE ep) {
            return edivi(ep);
        }
    }

    static class EPOW implements EOPER {

        static double epow(
                EPNODE ep) {
            EPNODE ep1 = ep.v.kid;
            double d;
            int lasterrno;

            lasterrno = errno;
            errno = 0;
            d = Math.pow(CALCOMP.evalue(ep1), CALCOMP.evalue(ep1.sibling));
//#ifdef  isnan
            if (errno == 0) {
                if (Double.isNaN(d)) {
                    errno = CALCOMP.EDOM;
                } else if (Double.isInfinite(d)) {
                    errno = CALCOMP.ERANGE;
                }
            }
//#endif
            if (errno == CALCOMP.EDOM || errno == CALCOMP.ERANGE) {
//	wputs("Illegal power\n");
                return (0.0);
            }
            errno = lasterrno;
            return (d);
        }

        @Override
        public double op(EPNODE ep) {
            return epow(ep);
        }
    }

    static class EBOTCH implements EOPER {

        static double ebotch(
                EPNODE ep) {
//    eputs("Bad expression!\n");
            System.exit(1);
            return 0.0; /* pro forma return */
        }

        @Override
        public double op(EPNODE ep) {
            return ebotch(ep);
        }
    }

    static EPNODE ekid( /* return pointer to a node's nth kid */
            EPNODE ep,
            int n) {

        for (ep = ep.v.kid; ep != null; ep = ep.sibling) {
            if (--n < 0) {
                break;
            }
        }

        return (ep);
    }

    static int nekids( /* return # of kids for node ep */
            EPNODE ep) {
        int n = 0;

        for (ep = ep.v.kid; ep != null; ep = ep.sibling) {
            n++;
        }

        return (n);
    }
    static char[] inpbuf = new char[MAXLINE];

    static void initfile( /* prepare input file */
            InputStream fp,
            String fn,
            int ln) {

        infp = fp;
        linbuf = inpbuf;
        infile = fn;
        lineno = ln;
        linepos = 0;
        inpbuf[0] = '\0';
        scan();
    }

    public static void initstr( /* prepare input string */
            String s,
            String fn,
            int ln) {
        infp = null;
        infile = fn;
        lineno = ln;
        linbuf = (s+"\0").toCharArray();
        linepos = 0;
        scan();
    }

    void getscanpos( /* return current scan position */
            String[] fnp,
            int[] lnp,
            char[] spp,
            InputStream[] fpp) {
        if (fnp[0] != null) {
            fnp[0] = infile;
        }
        if (lnp != null) {
            lnp[0] = lineno;
        }
        if (spp != null) {
            System.arraycopy(linbuf, linepos, spp, 0, linbuf.length - linepos);
        }
        if (fpp[0] != null) {
            fpp[0] = infp;
        }
    }

    public static char[] fgets(char[] linbuf, int len, InputStream is) throws IOException {
        char c;
        int cidx=0;
        while ((c = (char) is.read()) != '\n' && ((byte)c) != CALEXPR.EOF) {
            linbuf[cidx++] = c;
        }
        linbuf[cidx++]=' ';
        linbuf[cidx]='\0';
        if (((byte)c) == CALEXPR.EOF) 
            return null;
        return linbuf;
    }

    static int scan() /* scan next character, return literal next */ {
        int lnext = 0;
        try {
            do {
                if (linbuf[linepos] == '\0') {
                    if (infp == null || fgets(linbuf, MAXLINE, infp) == null) {
                        nextc = EOF;
                    } else {
                        nextc = linbuf[0];
                        lineno++;
                        linepos = 1;
                    }
                } else {
                    nextc = linbuf[linepos++];
                }
                if (lnext == 0) {
                    lnext = nextc;
                }
                if (nextc == eofc) {
                    nextc = EOF;
                    break;
                }
                if (nextc == '{') {
                    scan();
                    while (nextc != '}') {
                        if (nextc == EOF) {
//		    syntax("'}' expected");
                        } else {
                            scan();
                        }
                    }
                    scan();
                }
            } while (Character.isWhitespace(nextc));
        } catch (IOException e) {
        }
        return (lnext);
    }

    String long2ascii( /* convert long to ascii */
            long l) {
        return Long.toString(l);
//    static char	 buf[16];
//    register char  *cp;
//    int	 neg = 0;
//
//    if (l == 0)
//	return("0");
//    if (l < 0) {
//	l = -l;
//	neg++;
//    }
//    cp = buf + sizeof(buf);
//    *--cp = '\0';
//    while (l) {
//	*--cp = l % 10 + '0';
//	l /= 10;
//    }
//    if (neg)
//	*--cp = '-';
//    return(cp);
    }

//void
//syntax(			/* report syntax error and quit */
//    char  *err
//)
//{
//    register int  i;
//
//    if (infile != null || lineno != 0) {
//	if (infile != null) eputs(infile);
//	if (lineno != 0) {
//	    eputs(infile != null ? ", line " : "line ");
//	    eputs(long2ascii((long)lineno));
//	}
//	eputs(":\n");
//    }
//    eputs(linbuf);
//    if (linbuf[strlen(linbuf)-1] != '\n')
//	eputs("\n");
//    for (i = 0; i < linepos-1; i++)
//	eputs(linbuf[i] == '\t' ? "\t" : " ");
//    eputs("^ ");
//    eputs(err);
//    eputs("\n");
//    quit(1);
//}
//
//
    static void addekid( /* add a child to ep */
            EPNODE ep,
            EPNODE ekid) {
        if (ep.v.kid == null) {
            ep.v.kid = ekid;
        } else {
            for (ep = ep.v.kid; ep.sibling != null; ep = ep.sibling)
	    ;
            ep.sibling = ekid;
        }
        ekid.sibling = null;
    }

//static char[]	 str = new char[CALCOMP.RMAXWORD+1];
    static String getname() /* scan an identifier */ {
        int i, lnext;
        String str = "";
        lnext = nextc;
        for (i = 0; i < CALCOMP.RMAXWORD && CALCOMP.isid((char) lnext); i++, lnext = scan()) {
            str += (char) lnext;
        }
        //str += '\0';
        while (CALCOMP.isid((char) lnext)) /* skip rest of name */ {
            lnext = scan();
        }

        return (str);
    }

    static int getinum() /* scan a positive integer */ {
        int n, lnext;

        n = 0;
        lnext = nextc;
        while (Character.isDigit(lnext)) {
            n = n * 10 + lnext - '0';
            lnext = scan();
        }
        return (n);
    }

    static double getnum() /* scan a positive float */ {
        int i, lnext;
//    char[] str = new char[CALCOMP.RMAXWORD+1];
        String str = "";

        i = 0;
        lnext = nextc;
        while (Character.isDigit(lnext) && i < CALCOMP.RMAXWORD) {
            str += (char) lnext;
            i++;
            lnext = scan();
        }
        if (lnext == '.' && i < CALCOMP.RMAXWORD) {
            str += (char) lnext;
            i++;
            lnext = scan();
            if (i == 1 && !Character.isDigit(lnext)) {
//	    syntax("badly formed number");
            }
            while (Character.isDigit(lnext) && i < CALCOMP.RMAXWORD) {
                str += (char) lnext;
                i++;
                lnext = scan();
            }
        }
        if ((lnext == 'e') | (lnext == 'E') && i < CALCOMP.RMAXWORD) {
            str += (char) lnext;
            i++;
            lnext = scan();
            if ((lnext == '-') | (lnext == '+') && i < CALCOMP.RMAXWORD) {
                str += (char) lnext;
                i++;
                lnext = scan();
            }
            if (!Character.isDigit(lnext)) {
//	    syntax("missing exponent");
            }
            while (Character.isDigit(lnext) && i < CALCOMP.RMAXWORD) {
                str += lnext;
                i++;
                lnext = scan();
            }
        }
        str += '\0';

        return (Double.parseDouble(str));
    }

    public static EPNODE getE1() /* E1 . E1 ADDOP E2 */ /*	 E2 */ {
        EPNODE ep1, ep2;

        ep1 = getE2();
        while (nextc == '+' || nextc == '-') {
            ep2 = newnode();
            ep2.type = nextc;
            scan();
            addekid(ep2, ep1);
            addekid(ep2, getE2());
            if ((esupport & CALCOMP.E_RCONST) != 0
                    && ep1.type == CALCOMP.NUM && ep1.sibling.type == CALCOMP.NUM) {
                ep2 = rconst(ep2);
            }
            ep1 = ep2;
        }
        return (ep1);
    }

    static EPNODE getE2() /* E2 . E2 MULOP E3 */ /*	 E3 */ {
        EPNODE ep1, ep2;

        ep1 = getE3();
        while (nextc == '*' || nextc == '/') {
            ep2 = newnode();
            ep2.type = nextc;
            scan();
            addekid(ep2, ep1);
            addekid(ep2, getE3());
            if ((esupport & CALCOMP.E_RCONST) != 0) {
                EPNODE ep3 = ep1.sibling;
                if (ep1.type == CALCOMP.NUM && ep3.type == CALCOMP.NUM) {
                    ep2 = rconst(ep2);
                } else if (ep3.type == CALCOMP.NUM) {
                    if (ep2.type == '/') {
                        if (ep3.v.num == 0) {
//					syntax("divide by zero constant");
                        }
                        ep2.type = '*';	/* for speed */
                        ep3.v.num = 1. / ep3.v.num;
                    } else if (ep3.v.num == 0) {
                        ep1.sibling = null;	/* (E2 * 0) */
                        epfree(ep2);
                        ep2 = ep3;
                    }
                } else if (ep1.type == CALCOMP.NUM && ep1.v.num == 0) {
                    epfree(ep3);		/* (0 * E3) or (0 / E3) */
                    ep1.sibling = null;
//			efree((char *)ep2);
                    ep2 = ep1;
                }
            }
            ep1 = ep2;
        }
        return (ep1);
    }

    static EPNODE getE3() /* E3 . E4 ^ E3 */ /*	 E4 */ {
        EPNODE ep1, ep2;

        ep1 = getE4();
        if (nextc != '^') {
            return (ep1);
        }
        ep2 = newnode();
        ep2.type = nextc;
        scan();
        addekid(ep2, ep1);
        addekid(ep2, getE3());
        if ((esupport & CALCOMP.E_RCONST) != 0) {
            EPNODE ep3 = ep1.sibling;
            if (ep1.type == CALCOMP.NUM && ep3.type == CALCOMP.NUM) {
                ep2 = rconst(ep2);
            } else if (ep1.type == CALCOMP.NUM && ep1.v.num == 0) {
                epfree(ep3);		/* (0 ^ E3) */
                ep1.sibling = null;
//			efree((char *)ep2);
                ep2 = ep1;
            } else if ((ep3.type == CALCOMP.NUM && ep3.v.num == 0)
                    || (ep1.type == CALCOMP.NUM && ep1.v.num == 1)) {
                epfree(ep2);		/* (E4 ^ 0) or (1 ^ E3) */
                ep2 = newnode();
                ep2.type = CALCOMP.NUM;
                ep2.v.num = 1;
            }
        }
        return (ep2);
    }

    static EPNODE getE4() /* E4 . ADDOP E5 */ /*	 E5 */ {
        EPNODE ep1, ep2;

        if (nextc == '-') {
            scan();
            ep2 = getE5();
            if (ep2.type == CALCOMP.NUM) {
                ep2.v.num = -ep2.v.num;
                return (ep2);
            }
            if (ep2.type == CALCOMP.UMINUS) {	/* don't generate -(-E5) */
                ep1 = ep2.v.kid;
//	    efree((char *)ep2);
                return (ep1);
            }
            ep1 = newnode();
            ep1.type = CALCOMP.UMINUS;
            addekid(ep1, ep2);
            return (ep1);
        }
        if (nextc == '+') {
            scan();
        }
        return (getE5());
    }

    static EPNODE getE5() /* E5 . (E1) */ /*	 VAR */ /*	 NUM */ /*	 $N */ /*	 FUNC(E1,..) */ /*	 ARG */ {
        int i;
        String nam;
        EPNODE ep1, ep2;

        if (nextc == '(') {
            scan();
            ep1 = getE1();
            if (nextc != ')') {
//			syntax("')' expected");
            }
            scan();
            return (ep1);
        }

        if ((esupport & CALCOMP.E_INCHAN) != 0 && nextc == '$') {
            scan();
            ep1 = newnode();
            ep1.type = CALCOMP.CHAN;
            ep1.v.chan = getinum();
            return (ep1);
        }

        if ((esupport & (CALCOMP.E_VARIABLE | CALCOMP.E_FUNCTION)) != 0
                && (Character.isLetter(nextc) || nextc == CALCOMP.CNTXMARK)) {
            nam = getname();
            ep1 = null;
            if ((esupport & (CALCOMP.E_VARIABLE | CALCOMP.E_FUNCTION)) == (CALCOMP.E_VARIABLE | CALCOMP.E_FUNCTION)
                    && CALDEFN.curfunc != null) {
                for (i = 1, ep2 = CALDEFN.curfunc.v.kid.sibling;
                        ep2 != null; i++, ep2 = ep2.sibling) {
                    if (ep2.v.name.equals(nam)) {
                        ep1 = newnode();
                        ep1.type = CALCOMP.ARG;
                        ep1.v.chan = i;
                        break;
                    }
                }
            }
            if (ep1 == null) {
                ep1 = newnode();
                ep1.type = CALCOMP.VAR;
                ep1.v.ln = CALDEFN.varinsert(nam);
            }
            if ((esupport & CALCOMP.E_FUNCTION) != 0 && nextc == '(') {
                ep2 = newnode();
                ep2.type = CALCOMP.FUNC;
                addekid(ep2, ep1);
                ep1 = ep2;
                do {
                    scan();
                    addekid(ep1, getE1());
                } while (nextc == ',');
                if (nextc != ')') {
//				syntax("')' expected");
                }
                scan();
            } else if ((esupport & CALCOMP.E_VARIABLE) == 0) {
//			syntax("'(' expected");
            }
            if ((esupport & CALCOMP.E_RCONST) != 0 && isconstvar(ep1) != 0) {
                ep1 = rconst(ep1);
            }
            return (ep1);
        }

        if (isdecimal((char) nextc)) {
            ep1 = newnode();
            ep1.type = CALCOMP.NUM;
            ep1.v.num = getnum();
            return (ep1);
        }
//	syntax("unexpected character");
        return null; /* pro forma return */
    }

    static EPNODE rconst( /* reduce a constant expression */
            EPNODE epar) {
        EPNODE ep;

        ep = newnode();
        ep.type = CALCOMP.NUM;
        errno = 0;
        ep.v.num = CALCOMP.evalue(epar);
        if (errno == CALCOMP.EDOM || errno == CALCOMP.ERANGE) {
//	syntax("bad constant expression");
        }
        epfree(epar);

        return (ep);
    }

    static int isconstvar( /* is ep linked to a constant expression? */
            EPNODE ep) {
        EPNODE ep1;

        if ((esupport & CALCOMP.E_FUNCTION) != 0 && ep.type == CALCOMP.FUNC) {
            if (isconstfun(ep.v.kid) == 0) {
                return (0);
            }
            for (ep1 = ep.v.kid.sibling; ep1 != null; ep1 = ep1.sibling) {
                if (ep1.type != CALCOMP.NUM && isconstfun(ep1) == 0) {
                    return (0);
                }
            }
            return (1);
        }
        if (ep.type != CALCOMP.VAR) {
            return (0);
        }
        ep1 = ep.v.ln.def;
        if (ep1 == null || ep1.type != ':') {
            return (0);
        }
        if ((esupport & CALCOMP.E_FUNCTION) != 0 && ep1.v.kid.type != CALCOMP.SYM) {
            return (0);
        }
        return (1);
    }

    static int isconstfun( /* is ep linked to a constant function? */
            EPNODE ep) {
        EPNODE dp;
        LIBR lp;

        if (ep.type != CALCOMP.VAR) {
            return (0);
        }
        if ((dp = ep.v.ln.def) != null) {
            if (dp.v.kid.type == CALCOMP.FUNC) {
                return (dp.type == ':') ? 1 : 0;
            } else {
                return (0);		/* don't identify masked library functions */
            }
        }
        if ((lp = ep.v.ln.lib) != null) {
            return (lp.atyp == ':') ? 1 : 0;
        }
        return (0);
    }
}
