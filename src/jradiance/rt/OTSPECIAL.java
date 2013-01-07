/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.rt;

import jradiance.common.OTYPES;

/**
 *
 * @author arwillis
 */
public class OTSPECIAL {
/* RCSid $Id$ */
/*
 * Special type flags for objects used in rendering.
 * Depends on definitions in otypes.h
 */
//#ifndef _RAD_OTSPECIAL_H_
//#define _RAD_OTSPECIAL_H_
//#ifdef __cplusplus
//extern "C" {
//#endif

		/* flag for materials to ignore during irradiance comp. */
public static int  T_IRR_IGN=	OTYPES.T_SP1;

		/* flag for completely opaque materials */
public static int  T_OPAQUE=       OTYPES.T_SP2;

public static int  irr_ignore(int t)	{ return (OTYPES.ofun[t].flags & T_IRR_IGN); }

public static int  isopaque(int t)  {  return (OTYPES.ofun[t].flags & T_OPAQUE); }


//#ifdef __cplusplus
//}
//#endif
//#endif /* _RAD_OTSPECIAL_H_ */
}
