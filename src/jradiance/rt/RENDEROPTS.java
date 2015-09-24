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

import jradiance.common.COLORS.COLOR;
import jradiance.common.SAVQSTR;

/**
 *
 * @author arwillis
 */
public class RENDEROPTS {

/*
 *  renderopts.c - process common rendering options
 *
 *  External symbols declared in ray.h
 */


static int
getrenderopt(		/* get next render option */
	int  ac,
	String[]  av
)
{
//#define	 check(ol,al)		if (av[0][ol] || \
//				badarg(ac-1,av+1,al)) \
//				return(-1)
//#define	 bool(olen,var)		switch (av[0][olen]) { \
//				case '\0': var = !var; break; \
//				case 'y': case 'Y': case 't': case 'T': \
//				case '+': case '1': var = 1; break; \
//				case 'n': case 'N': case 'f': case 'F': \
//				case '-': case '0': var = 0; break; \
//				default: return(-1); }
	String[] amblp;		/* pointer to build ambient list */
	int	rval;
					/* is it even an option? */
	if (ac < 1 || av[0] == null || av[0].charAt(0) != '-')
		return(-1);
					/* check if it's one we know */
	switch (av[0].charAt(1)) {
	case 'u':				/* uncorrelated sampling */
//		bool(2,rand_samp);
		return(0);
	case 'b':				/* back face vis. */
		if (av[0].charAt(2) == 'v') {
//			bool(3,backvis);
			return(0);
		}
		break;
	case 'd':				/* direct */
		switch (av[0].charAt(2)) {
		case 't':				/* threshold */
//			check(3,"f");
			RAYCALLS.shadthresh = Float.parseFloat(av[1]);
			return(1);
		case 'c':				/* certainty */
//			check(3,"f");
			RAYCALLS.shadcert = Float.parseFloat(av[1]);
			return(1);
		case 'j':				/* jitter */
//			check(3,"f");
			RAYCALLS.dstrsrc = Float.parseFloat(av[1]);
			return(1);
		case 'r':				/* relays */
//			check(3,"i");
			RAYCALLS.directrelay = Integer.parseInt(av[1]);
			return(1);
		case 'p':				/* pretest */
//			check(3,"i");
			RAYCALLS.vspretest = Integer.parseInt(av[1]);
			return(1);
		case 'v':				/* visibility */
//			bool(3,directvis);
			return(0);
		case 's':				/* size */
//			check(3,"f");
			RAYCALLS.srcsizerat = Float.parseFloat(av[1]);
			return(1);
		}
		break;
	case 's':				/* specular */
		switch (av[0].charAt(2)) {
		case 't':				/* threshold */
//			check(3,"f");
			RAYCALLS.specthresh = Float.parseFloat(av[1]);
			return(1);
		case 's':				/* sampling */
//			check(3,"f");
			RAYCALLS.specjitter = Float.parseFloat(av[1]);
			return(1);
		}
		break;
	case 'l':				/* limit */
		switch (av[0].charAt(2)) {
		case 'r':				/* recursion */
//			check(3,"i");
			RAYCALLS.maxdepth = Integer.parseInt(av[1]);
			return(1);
		case 'w':				/* weight */
//			check(3,"f");
			RAYCALLS.minweight = Float.parseFloat(av[1]);
			return(1);
		}
		break;
	case 'i':				/* irradiance */
//		bool(2,do_irrad);
		return(0);
	case 'a':				/* ambient */
		switch (av[0].charAt(2)) {
		case 'v':				/* value */
//			check(3,"fff");
			RAYCALLS.ambval.setcolor( Float.parseFloat(av[1]),
					Float.parseFloat(av[2]),
					Float.parseFloat(av[3]));
			return(3);
		case 'w':				/* weight */
//			check(3,"i");
			RAYCALLS.ambvwt = Integer.parseInt(av[1]);
			return(1);
		case 'a':				/* accuracy */
//			check(3,"f");
			RAYCALLS.ambacc = Float.parseFloat(av[1]);
			return(1);
		case 'r':				/* resolution */
//			check(3,"i");
			RAYCALLS.ambres = Integer.parseInt(av[1]);
			return(1);
		case 'd':				/* divisions */
//			check(3,"i");
			RAYCALLS.ambdiv = Integer.parseInt(av[1]);
			return(1);
		case 's':				/* super-samp */
//			check(3,"i");
			RAYCALLS.ambssamp = Integer.parseInt(av[1]);
			return(1);
		case 'b':				/* bounces */
//			check(3,"i");
			RAYCALLS.ambounce = Integer.parseInt(av[1]);
			return(1);
		case 'i':				/* include */
		case 'I':
//			check(3,"s");
			if (RAYCALLS.ambincl != 1) {
				RAYCALLS.ambincl = 1;
				amblp = RAYCALLS.amblist;
			}
			if (av[0].charAt(2) == 'I') {	/* file */
//				rval = wordfile(amblp,
//					getpath(av[1],getrlibpath(),R_OK));
//				if (rval < 0) {
//					sprintf(errmsg,
//			"cannot open ambient include file \"%s\"", av[1]);
//					error(SYSTEM, errmsg);
//				}
//				amblp += rval;
			} else {
//				*amblp++ = savqstr(av[1]);
//				*amblp = NULL;
			}
			return(1);
		case 'e':				/* exclude */
		case 'E':
//			check(3,"s");
			if (RAYCALLS.ambincl != 0) {
				RAYCALLS.ambincl = 0;
				amblp = RAYCALLS.amblist;
			}
			if (av[0].charAt(2) == 'E') {	/* file */
//				rval = wordfile(amblp,
//					getpath(av[1],getrlibpath(),R_OK));
//				if (rval < 0) {
//					sprintf(errmsg,
//			"cannot open ambient exclude file \"%s\"", av[1]);
//					error(SYSTEM, errmsg);
//				}
//				amblp += rval;
			} else {
//				*amblp++ = savqstr(av[1]);
//				*amblp = NULL;
			}
			return(1);
		case 'f':				/* file */
//			check(3,"s");
			RAYCALLS.ambfile = SAVQSTR.savqstr(av[1]);
			return(1);
		}
		break;
	case 'm':				/* medium */
		switch (av[0].charAt(2)) {
		case 'e':				/* extinction */
//			check(3,"fff");
			RAYCALLS.cextinction.setcolor( Float.parseFloat(av[1]),
					Float.parseFloat(av[2]),
					Float.parseFloat(av[3]));
			return(3);
		case 'a':				/* albedo */
//			check(3,"fff");
			RAYCALLS.salbedo.setcolor( Float.parseFloat(av[1]),
					Float.parseFloat(av[2]),
					Float.parseFloat(av[3]));
			return(3);
		case 'g':				/* eccentr. */
//			check(3,"f");
			RAYCALLS.seccg = Float.parseFloat(av[1]);
			return(1);
		case 's':				/* sampling */
//			check(3,"f");
			RAYCALLS.ssampdist = Float.parseFloat(av[1]);
			return(1);
		}
		break;
	}
	return(-1);		/* unknown option */

//#undef	check
//#undef	bool
}


public static void
print_rdefaults()		/* print default render values to stdout */
{
	System.out.print(String.format(RAYCALLS.do_irrad!=0 ? "-i+\t\t\t\t# irradiance calculation on\n" :
			"-i-\t\t\t\t# irradiance calculation off\n"));
	System.out.print(String.format(RAYCALLS.rand_samp!=0 ? "-u+\t\t\t\t# uncorrelated Monte Carlo sampling\n" :
			"-u-\t\t\t\t# correlated quasi-Monte Carlo sampling\n"));
	System.out.print(String.format(RAYCALLS.backvis!=0 ? "-bv+\t\t\t\t# back face visibility on\n" :
			"-bv-\t\t\t\t# back face visibility off\n"));
	System.out.print(String.format("-dt %f\t\t\t# direct threshold\n", RAYCALLS.shadthresh));
	System.out.print(String.format("-dc %f\t\t\t# direct certainty\n", RAYCALLS.shadcert));
	System.out.print(String.format("-dj %f\t\t\t# direct jitter\n", RAYCALLS.dstrsrc));
	System.out.print(String.format("-ds %f\t\t\t# direct sampling\n", RAYCALLS.srcsizerat));
	System.out.print(String.format("-dr %-9d\t\t\t# direct relays\n", RAYCALLS.directrelay));
	System.out.print(String.format("-dp %-9d\t\t\t# direct pretest density\n", RAYCALLS.vspretest));
	System.out.print(String.format(RAYCALLS.directvis!=0 ? "-dv+\t\t\t\t# direct visibility on\n" :
			"-dv-\t\t\t\t# direct visibility off\n"));
	System.out.print(String.format("-ss %f\t\t\t# specular sampling\n", RAYCALLS.specjitter));
	System.out.print(String.format("-st %f\t\t\t# specular threshold\n", RAYCALLS.specthresh));
	System.out.print(String.format("-av %f %f %f\t# ambient value\n", RAYCALLS.ambval.colval(COLOR.RED),
			RAYCALLS.ambval.colval(COLOR.GRN), RAYCALLS.ambval.colval(COLOR.BLU)));
	System.out.print(String.format("-aw %-9d\t\t\t# ambient value weight\n", RAYCALLS.ambvwt));
	System.out.print(String.format("-ab %-9d\t\t\t# ambient bounces\n", RAYCALLS.ambounce));
	System.out.print(String.format("-aa %f\t\t\t# ambient accuracy\n", RAYCALLS.ambacc));
	System.out.print(String.format("-ar %-9d\t\t\t# ambient resolution\n", RAYCALLS.ambres));
	System.out.print(String.format("-ad %-9d\t\t\t# ambient divisions\n", RAYCALLS.ambdiv));
	System.out.print(String.format("-as %-9d\t\t\t# ambient super-samples\n", RAYCALLS.ambssamp));
	System.out.print(String.format("-me %.2e %.2e %.2e\t# mist extinction coefficient\n",
			RAYCALLS.cextinction.colval(COLOR.RED),
			RAYCALLS.cextinction.colval(COLOR.GRN),
			RAYCALLS.cextinction.colval(COLOR.BLU)));
	System.out.print(String.format("-ma %f %f %f\t# mist scattering albedo\n", RAYCALLS.salbedo.colval(COLOR.RED),
			RAYCALLS.salbedo.colval(COLOR.GRN), RAYCALLS.salbedo.colval(COLOR.BLU)));
	System.out.print(String.format("-mg %f\t\t\t# mist scattering eccentricity\n", RAYCALLS.seccg));
	System.out.print(String.format("-ms %f\t\t\t# mist sampling distance\n", RAYCALLS.ssampdist));
	System.out.print(String.format("-lr %-9d\t\t\t# limit reflection%s\n", RAYCALLS.maxdepth,
			RAYCALLS.maxdepth<=0 ? " (Russian roulette)" : ""));
	System.out.print(String.format("-lw %.2e\t\t\t# limit weight\n", RAYCALLS.minweight));
}
    
}
