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
public class TMPRIVAT {
/*
 * Private header file for tone mapping routines.
 */
//#define	MEM_PTR		void *

//#include	"color.h"
//#include	"tonemap.h"


				/* required constants */
//#define M_LN2		0.69314718055994530942
//#define M_LN10		2.30258509299404568402
//				/* minimum values and defaults */
//#define	MINGAM		0.75
//#define DEFGAM		2.2
//#define	MINLDDYN	2.
//#define DEFLDDYN	32.
//#define	MINLDMAX	1.
//#define	DEFLDMAX	100.
//
//#define BRT2SCALE(l2)	(int)(M_LN2*TM_BRTSCALE*(l2) + .5 - ((l2) < 0))
//
public static final int HISTEP=		16;		/* steps in BRTSCALE for each bin */
//
//#define MINBRT		(-16*TM_BRTSCALE)	/* minimum usable brightness */
//#define MINLUM		(1.125352e-7)		/* tmLuminance(MINBRT) */
//
//#define HISTI(li)	(((li)-MINBRT)/HISTEP)
//#define HISTV(i)	(MINBRT + HISTEP/2 + (i)*HISTEP)
//
//#define LMESLOWER	(5.62e-3)		/* lower mesopic limit */
//#define	LMESUPPER	(5.62)			/* upper mesopic limit */
//#define BMESLOWER	((int)(-5.18*TM_BRTSCALE-.5))
//#define BMESUPPER	((int)(1.73*TM_BRTSCALE+.5))
//
//						/* approximate scotopic lum. */
//#define	SCO_rf		0.062
//#define SCO_gf		0.608
//#define SCO_bf		0.330
//#define scotlum(c)	(SCO_rf*(c)[RED] + SCO_gf*(c)[GRN] + SCO_bf*(c)[BLU])
//#define normscot(c)	( (	(int32)(SCO_rf*256.+.5)*(c)[RED] + \
//				(int32)(SCO_gf*256.+.5)*(c)[GRN] + \
//				(int32)(SCO_bf*256.+.5)*(c)[BLU]	) >> 8 )
//
//extern int	tmNewMap(TMstruct *tms);	/* allocate new tone-mapping */
//
//extern int	tmErrorReturn(const char *, TMstruct *, int);
//
//						/* lookup for mesopic scaling */
//extern uby8	tmMesofact[BMESUPPER-BMESLOWER];
//
//extern void	tmMkMesofact(void);			/* build tmMesofact */
//
//#define	returnErr(code)	return(tmErrorReturn(funcName,tms,code))
public static int returnOK() {
    return(TONEMAP.TM_E_OK);
}
//
//#define	FEQ(a,b)	((a) < (b)+1e-5 && (b) < (a)+1e-5)
//
//#define	PRIMEQ(p1,p2)	(FEQ((p1)[0][0],(p2)[0][0])&&FEQ((p1)[0][1],(p2)[0][1])\
//			&&FEQ((p1)[1][0],(p2)[1][0])&&FEQ((p1)[1][1],(p2)[1][1])\
//			&&FEQ((p1)[2][0],(p2)[2][0])&&FEQ((p1)[2][1],(p2)[2][1])\
//			&&FEQ((p1)[3][0],(p2)[3][0])&&FEQ((p1)[3][1],(p2)[3][1]))
}
