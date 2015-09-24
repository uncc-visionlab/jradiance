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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import jradiance.common.OBJECT.OBJREC;
import jradiance.ot.OCONV;

/**
 *
 * @author arwillis
 */
public class SCENEIO {
//  #ifndef lint
//static const char RCSid[] = "$Id$";
//#endif
/*
 *  Portable, binary Radiance i/o routines.
 *
 *  Called from octree and mesh i/o routines.
 */

    static int object0;			/* zeroeth object */

    static short[] otypmap = new short[OTYPES.NUMOTYPE + 32];	/* object type map */

    static int getobj( /* get next object */
            InputStream fp,
            int objsiz) throws IOException {
        char[] sbuf = new char[OBJECT.MAXSTR];
        int obj;
        int i;
        long m;
        OBJREC objp = null;

        i = (int) PORTIO.getint(1, fp);
        if (i == -1) {
            return (OBJECT.OVOID);		/* terminator */
        }
        if ((obj = OBJECT.newobject()) == OBJECT.OVOID) {
//		error(SYSTEM, "out of object space");
        }
        objp = OBJECT.objptr(obj);
        if ((objp.otype = otypmap[i]) < 0) {
//		error(USER, "reference to unknown type");
        }
        if ((m = PORTIO.getint(objsiz, fp)) != OBJECT.OVOID) {
            m += object0;
            if ((int) m != m) {
//			error(INTERNAL, "too many objects in getobj");
            }
        }
        objp.omod = (int) m;
        objp.oname = SAVQSTR.savqstr(PORTIO.getstr(sbuf, fp));
        if ((objp.oargs.nsargs = (short) PORTIO.getint(2, fp)) > 0) {
            objp.oargs.sarg = new String[objp.oargs.nsargs];
//		if (objp.oargs.sarg == NULL)
//			goto memerr;
            for (i = 0; i < objp.oargs.nsargs; i++) {
                objp.oargs.sarg[i] = SAVESTR.savestr(PORTIO.getstr(sbuf, fp));
            }
        } else {
            objp.oargs.sarg = null;
        }
//#ifdef	IARGS
        if (OCONV.IARGS) {
            if ((objp.oargs.niargs = (short) PORTIO.getint(2, fp)) > 0) {
                objp.oargs.iarg = new long[objp.oargs.niargs];
//		if (objp.oargs.iarg == NULL)
//			goto memerr;
                for (i = 0; i < objp.oargs.niargs; i++) {
                    objp.oargs.iarg[i] = PORTIO.getint(4, fp);
                }
            } else {
                objp.oargs.iarg = null;
            }
        }
//#endif
        if ((objp.oargs.nfargs = (short) PORTIO.getint(2, fp)) > 0) {
            objp.oargs.farg = new double[objp.oargs.nfargs];
//		if (objp.oargs.farg == NULL)
//			goto memerr;
            for (i = 0; i < objp.oargs.nfargs; i++) {
                objp.oargs.farg[i] = PORTIO.getflt(fp);
            }
        } else {
            objp.oargs.farg = null;
        }
//	if (feof(fp))
//		error(SYSTEM, "unexpected EOF in getobj");
						/* initialize */
        objp.os = null;
        /* insert */
	MODOBJECT.insertobject(obj);
        return (obj);
//memerr:
//	error(SYSTEM, "out of memory in getobj");
//	return 0; /* pro forma return */
    }

    static void readscene( /* read binary scene description */
            InputStream fp,
            int objsiz) throws IOException {
        char[] sbuf = new char[32];
        int i = 0;
        /* record starting object */
        object0 = OBJECT.nobjects;
        String str;
        do {				/* read type map */
            str = PORTIO.getstr(sbuf, fp);
//	for (i = 0; PORTIO.getstr(sbuf, fp) != null && sbuf[0] != 0; i++)            
            if ((otypmap[i++] = (short) OTYPES.otype(str)) < 0) {
//			sprintf(errmsg, "unknown object type \"%s\"",
//					sbuf);
//			error(WARNING, errmsg);
            }
        } while (str.length() > 0);
        /* read objects */
        int result = OBJECT.OVOID;
        do {
            result = getobj(fp, objsiz);
        } while (result != OBJECT.OVOID);
    }

    static void putobj( /* write out object */
            OBJECT.OBJREC o,
            OutputStream fp) throws IOException {
        int i;

        if (o == null) {		/* terminator */
            PORTIO.putint(-1L, 1, fp);
            return;
        }
        PORTIO.putint((long) o.otype, 1, fp);
        PORTIO.putint((long) o.omod, Integer.SIZE / 8, fp);
        PORTIO.putstr(o.oname, fp);
        PORTIO.putint((long) o.oargs.nsargs, 2, fp);
        for (i = 0; i < o.oargs.nsargs; i++) {
            PORTIO.putstr(o.oargs.sarg[i], fp);
        }
//#ifdef  IARGS
        if (OCONV.IARGS) {
            PORTIO.putint((long) o.oargs.niargs, 2, fp);
            for (i = 0; i < o.oargs.niargs; i++) {
                PORTIO.putint((long) o.oargs.iarg[i], 4, fp);
            }
        }
//#endif
        PORTIO.putint((long) o.oargs.nfargs, 2, fp);
        for (i = 0; i < o.oargs.nfargs; i++) {
            PORTIO.putflt(o.oargs.farg[i], fp);
        }
    }

    public static void writescene( /* write binary scene description */
            int firstobj,
            //        OBJECT objsrc,
            int nobjs,
            OutputStream fp) throws IOException {
        int i;
        /* write out type list */
        for (i = 0; i < OTYPES.NUMOTYPE; i++) {
            PORTIO.putstr(OTYPES.ofun[i].funame, fp);
        }
        PORTIO.putstr("\0", fp);
        /* write objects */
        for (i = firstobj; i < firstobj + nobjs; i++) {
            putobj(OBJECT.objptr(i), fp);
        }
        putobj(null, fp);		/* terminator */
//	if (ferror(fp))
//		error(SYSTEM, "write error in writescene");
    }
}
