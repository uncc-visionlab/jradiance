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
