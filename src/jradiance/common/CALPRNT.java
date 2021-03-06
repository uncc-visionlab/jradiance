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

/**
 *
 * @author arwillis
 */
public class CALPRNT {
//#ifndef lint
//static const char	RCSid[] = "$Id$";
//#endif
///*
// *  calprint.c - routines for printing calcomp expressions.
// */
//
//#include "copyright.h"
//
//#include  <stdio.h>
//
//#include  "rterror.h"
//#include  "calcomp.h"
//
//
//void
//eprint(ep, fp)			/* print a parse tree */
//register EPNODE  *ep;
//FILE  *fp;
//{
//    static EPNODE  *curdef = NULL;
//    register EPNODE  *ep1 = NULL;
//
//    switch (ep->type) {
//
//	case VAR:
//	    fputs(ep->v.ln->name, fp);
//	    break;
//
//	case SYM:
//	    fputs(ep->v.name, fp);
//	    break;
//
//	case FUNC:
//	    eprint(ep->v.kid, fp);
//	    fputc('(', fp);
//	    ep1 = ep->v.kid->sibling;
//	    while (ep1 != NULL) {
//		eprint(ep1, fp);
//		if ((ep1 = ep1->sibling) != NULL)
//		    fputs(", ", fp);
//	    }
//	    fputc(')', fp);
//	    break;
//
//	case ARG:
//	    if (curdef == NULL || curdef->v.kid->type != FUNC ||
//			(ep1 = ekid(curdef->v.kid, ep->v.chan)) == NULL) {
//		eputs("Bad argument!\n");
//		quit(1);
//	    }
//	    eprint(ep1, fp);
//	    break;
//
//	case NUM:
//	    fprintf(fp, "%.9g", ep->v.num);
//	    break;
//
//	case UMINUS:
//	    fputc('-', fp);
//	    eprint(ep->v.kid, fp);
//	    break;
//
//	case CHAN:
//	    fprintf(fp, "$%d", ep->v.chan);
//	    break;
//	
//	case '=':
//	case ':':
//	    ep1 = curdef;
//	    curdef = ep;
//	    eprint(ep->v.kid, fp);
//	    fputc(' ', fp);
//	    fputc(ep->type, fp);
//	    fputc(' ', fp);
//	    eprint(ep->v.kid->sibling, fp);
//	    curdef = ep1;
//	    break;
//	    
//	case '+':
//	case '-':
//	case '*':
//	case '/':
//	case '^':
//	    fputc('(', fp);
//	    eprint(ep->v.kid, fp);
//	    fputc(' ', fp);
//	    fputc(ep->type, fp);
//	    fputc(' ', fp);
//	    eprint(ep->v.kid->sibling, fp);
//	    fputc(')', fp);
//	    break;
//
//	default:
//	    eputs("Bad expression!\n");
//	    quit(1);
//
//    }
//
//}
//
//
//void
//dprint(name, fp)		/* print a definition (all if no name) */
//char  *name;
//FILE  *fp;
//{
//    register EPNODE  *ep;
//    
//    if (name == NULL)
//	for (ep = dfirst(); ep != NULL; ep = dnext()) {
//	    eprint(ep, fp);
//	    fputs(";\n", fp);
//	}
//    else if ((ep = dlookup(name)) != NULL) {
//	eprint(ep, fp);
//	fputs(";\n", fp);
//    } else {
//	wputs(name);
//	wputs(": undefined\n");
//    }
//}
    
}
