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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EventListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author arwillis
 */
public class OBJECT {
    /*
     *  object.h - header file for routines using objects and object sets.
     *
     *  Include after "standard.h"
     */

    /*
     *	Object definitions require general specifications
     *	which may include a number of different argument types.
     *	The following structure aids in the storage of such
     *	argument lists.
     */
    public static class FUNARGS {

        public String[] sarg;			/* string arguments */

        public double[] farg;			/* real arguments */

        public short nsargs;			/* # of string arguments */

        public short nfargs;			/* # of real arguments */
//#ifdef  IARGS

        public short niargs;			/* # of integer arguments */

        public long[] iarg;			/* integer arguments */
//#endif


        public FUNARGS() {
            sarg = null;
            farg = null;
            iarg = null;
            nsargs = nfargs = niargs = 0;
        }

        public FUNARGS(String[] oargs_sargs, double[] oargs_fargs,
                int nsargs, int nfargs) {
            this.sarg = oargs_sargs;
            this.farg = oargs_fargs;
            this.nsargs = (short) nsargs;
            this.nfargs = (short) nfargs;
        }
    };
    static int MAXSTR = 128;		/* maximum string length */

    /*
     *	An object is defined as an index into an array of
     *	structures containing the object type and specification
     *	and the modifier index.
     */

    public static class OBJREC {

        public int omod;			/* modifier number */

        public short otype;			/* object type number */

        public String oname;			/* object name */

        public FUNARGS oargs = new FUNARGS();	/* object specification */

        public OBJECT_STRUCTURE os = null;	/* object structure */


        public OBJREC() {
            omod = 0;
            otype = OBJECT.OVOID;
            oname = null;
        }

        public OBJREC(int omod, int otype, String oname,
                String[] oargs_sargs, double[] oargs_fargs,
                int nsargs, int nfargs, int[] os1) {
            this.omod = omod;
            this.otype = (short) otype;
            this.oname = oname;
            this.oargs = new FUNARGS(oargs_sargs, oargs_fargs, nsargs, nfargs);
            if (os1 == null) {
                this.os = null;
            } else {
                if (this.os == null) {
                    return;
                }
                this.os.setOS(os1);
            }
        }
    }
    static final int MAXOBJBLK = 65535;		/* maximum number of object blocks */

    static final int OBJBLKSHFT = 9;
    static final int OBJBLKSIZ = (1 << OBJBLKSHFT);	/* object block size */


    public static OBJREC objptr(int obj) {
        return objblock[obj >> OBJBLKSHFT][obj & (OBJBLKSIZ - 1)];
    }
    public static final int OVOID = (-1);		/* void object */

    public static final String VOIDID = "void";		/* void identifier */

    /*
     *     Object sets begin with the number of objects and proceed with
     *  the objects in ascending order.
     */
    public static final int MAXSET = 511;		/* maximum object set size */

    static ArrayList<ChangeListener> objNotifyList = new ArrayList();

    static void notifyListeners(OBJREC obj) {
        for (ChangeListener listener : objNotifyList) {
            listener.stateChanged(new ChangeEvent(obj));
        }
    }
//#define setfree(os)	free((void *)(os))
//
//extern void  (*addobjnotify[])();        /* people to notify of new objects */
//
//					/* defined in modobject.c */
//extern OBJECT	objndx(OBJREC *op);
//extern OBJECT	lastmod(OBJECT obj, char *mname);
//extern OBJECT	modifier(char *name);
//extern OBJECT	object(char *oname);
//extern void	insertobject(OBJECT obj);
//extern void	clearobjndx(void);
//					/* defined in objset.c */
//extern void	insertelem(OBJECT *os, OBJECT obj);
//extern void	deletelem(OBJECT *os, OBJECT obj);
//extern int	inset(OBJECT *os, OBJECT obj);
//extern int	setequal(OBJECT *os1, OBJECT *os2);
//extern void	setcopy(OBJECT *os1, OBJECT *os2);
//extern OBJECT *	setsave(OBJECT *os);
//extern void	setunion(OBJECT *osr, OBJECT *os1, OBJECT *os2);
//extern void	setintersect(OBJECT *osr, OBJECT *os1, OBJECT *os2);
//extern OCTREE	fullnode(OBJECT *oset);
//extern void	objset(OBJECT *oset, OCTREE ot);
//extern int	dosets(int (*f)());
//extern void	donesets(void);
//
//					/* defined in otypes.c */
//extern int	otype(char *ofname);
//extern void	objerror(OBJREC *o, int etyp, char *msg);
//					/* defined in readfargs.c */
//extern int	readfargs(FUNARGS *fa, FILE *fp);
//extern void	freefargs(FUNARGS *fa);
//					/* defined in readobj.c */
//extern void	readobj(char *inpspec);
//extern void	getobject(char *name, FILE *fp);
//extern OBJECT	newobject(void);
//extern void	freeobjects(int firstobj, int nobjs);
//					/* defined in free_os.c */
//extern int	free_os(OBJREC *op);
//    static String RCSid = "$Id$";
/*
     *  readobj.c - routines for reading in object descriptions.
    
     *  External symbols declared in object.h
     */
    static OBJREC[][] objblock = new OBJREC[MAXOBJBLK][];		/* our objects */

    public static int nobjects = 0;			/* # of objects */


    public static void readobj( /* read in an object file or stream */
            String inpspec) {
        BufferedInputStream infp = null;
        try {
            if (inpspec == null) {
                infp = new BufferedInputStream(System.in);
                inpspec = "standard input";
            } else if (inpspec.charAt(0) == '!') {
//		if ((infp = popen(inpspec+1, "r")) == NULL) {
//			sprintf(errmsg, "cannot execute \"%s\"", inpspec);
//			error(SYSTEM, errmsg);
//		}
            } else {
                infp = new BufferedInputStream(new FileInputStream(inpspec));
            }
            readStream(inpspec, infp);
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
//		sprintf(errmsg, "cannot open scene file \"%s\"", inpspec);
//		error(SYSTEM, errmsg);
            }
            e.printStackTrace();
        }
    }

    public static void readStream(String inpspec, BufferedInputStream infp) throws IOException {
        int lastobj;
        char[] buf = new char[2048];
        int c;
        lastobj = nobjects;
        infp.mark(1);
        while ((c = infp.read()) != -1) {
            if (Character.isWhitespace(c)) {
                continue;
            }
            if (c == '#') {				/* comment */
                do {
                    c = infp.read();
                } while (c != '\n');
            } else if (c == '!') {			/* command */
                infp.reset();
                do {
                    c = infp.read();
                } while (c != '\n');
                FGETLINE.fgetline(buf, buf.length, infp);
                readobj(new String(buf));
            } else {				/* object */
                infp.reset();
                getobject(inpspec, infp);
            }
            infp.mark(1);
        }
        infp.close();
//	if (inpspec.charAt(0) == '!')
//		pclose(infp);
//	else
//		fclose(infp);
//	if (nobjects == lastobj) {
//		sprintf(errmsg, "(%s): empty file", inpspec);
//		error(WARNING, errmsg);
//	}
    }

    public static void getobject( /* read the next object */
            String name,
            InputStream fp) throws IOException {
        int OALIAS = -2;
        int obj;
        char[] sbuf = new char[MAXSTR];
        int rval;
        OBJREC objp = null;

        if ((obj = newobject()) == OVOID) {
            //error(SYSTEM, "out of object space");
        }
        objp = objptr(obj);
        /* get modifier */
//        sbuf[0] = 'E';
//        sbuf[1] = 'O';
//        sbuf[2] = 'F';
//        sbuf[3] = '\0';
        String word = FGETWORD.fgetword(sbuf, MAXSTR, fp);
        if (word.indexOf('\t') > 0) {
//		sprintf(errmsg, "(%s): illegal tab in modifier \"%s\"",
//					name, sbuf);
//		error(USER, errmsg);
        }
        if (word.equals(VOIDID)) {
            objp.omod = OVOID;
        } else if (word.equals(OTYPES.ALIASMOD)) {
            objp.omod = OALIAS;
        } else if ((objp.omod = MODOBJECT.modifier(word)) == OVOID) {
//		sprintf(errmsg, "(%s): undefined modifier \"%s\"", name, sbuf);
//		error(USER, errmsg);
        }
        /* get type */
//        sbuf[0] = 'E';
//        sbuf[1] = 'O';
//        sbuf[2] = 'F';
//        sbuf[3] = '\0';
        word = FGETWORD.fgetword(sbuf, MAXSTR, fp);
        if ((objp.otype = (short) OTYPES.otype(word)) < 0) {
//		sprintf(errmsg, "(%s): unknown type \"%s\"", name, sbuf);
//		error(USER, errmsg);
        }
        /* get identifier */
        sbuf[0] = '\0';
        word = FGETWORD.fgetword(sbuf, MAXSTR, fp);
        if (word.indexOf('\t') != -1) {
//		sprintf(errmsg, "(%s): illegal tab in identifier \"%s\"",
//					name, sbuf);
//		error(USER, errmsg);
        }
        //objp.oname = new String(SAVQSTR.savqstr(word));
        objp.oname = word;
        /* get arguments */
        if (objp.otype == OTYPES.MOD_ALIAS) {
            int alias;
//            sbuf[0] = 'E';
//            sbuf[1] = 'O';
//            sbuf[2] = 'F';
//            sbuf[3] = '\0';
            word = FGETWORD.fgetword(sbuf, MAXSTR, fp);
            if ((alias = MODOBJECT.modifier(word)) == OVOID) {
//			sprintf(errmsg, "(%s): bad reference \"%s\"",
//					name, sbuf);
//			objerror(objp, USER, errmsg);
            }
            if (objp.omod == OALIAS
                    || objp.omod == objptr(alias).omod) {
                objp.omod = alias;
            } else {
                objp.oargs.sarg = null;
//			if (objp->oargs.sarg == NULL)
//				error(SYSTEM, "out of memory in getobject");
                objp.oargs.nsargs = 1;
                objp.oargs.sarg[0] = SAVESTR.savestr(word);
            }
        } else if ((rval = READFARGS.readfargs(objp.oargs, fp)) == 0) {
//		sprintf(errmsg, "(%s): bad arguments", name);
//		objerror(objp, USER, errmsg);
        } else if (rval < 0) {
//		sprintf(errmsg, "(%s): error reading scene", name);
//		error(SYSTEM, errmsg);
        }
        if (objp.omod == OALIAS) {
//		sprintf(errmsg, "(%s): inappropriate use of '%s' modifier",
//				name, ALIASMOD);
//		objerror(objp, USER, errmsg);
        }
        /* initialize */
        objp.os = null;

        MODOBJECT.insertobject(obj);		/* add to global structure */
//#undef OALIAS
    }

    public static int newobject() /* get a new object */ {
        int i;

        if ((nobjects & (OBJBLKSIZ - 1)) == 0) {	/* new block */
            //errno = 0;
            i = nobjects >> OBJBLKSHFT;
            if (i >= MAXOBJBLK) {
                return (OVOID);
            }
            //objblock[i] = (OBJREC *)calloc(OBJBLKSIZ, sizeof(OBJREC));
            objblock[i] = new OBJREC[OBJBLKSIZ];//(OBJREC *)calloc(OBJBLKSIZ, sizeof(OBJREC));
            for (int j = 0; j < OBJBLKSIZ; j++) {
                objblock[i][j] = new OBJREC();
            }
            if (objblock[i] == null) {
                return (OVOID);
            }
        }
        return (nobjects++);
    }
//
//void
//freeobjects(firstobj, nobjs)		/* free a range of objects */
//int  firstobj, nobjs;
//{
//	register int  obj;
//					/* check bounds */
//	if (firstobj < 0)
//		return;
//	if (nobjs <= 0)
//		return;
//	if (firstobj + nobjs > nobjects)
//		return;
//					/* clear objects */
//	for (obj = firstobj+nobjs; obj-- > firstobj; ) {
//		register OBJREC  *o = objptr(obj);
//		free_os(o);		/* free client memory */
//		freeqstr(o->oname);
//		freefargs(&o->oargs);
//		memset((void *)o, '\0', sizeof(OBJREC));
//	}
//	clearobjndx();
//					/* free objects off end */
//	for (obj = nobjects; obj-- > 0; )
//		if (objptr(obj)->oname != NULL)
//			break;
//	++obj;
//	while (nobjects > obj)		/* free empty end blocks */
//		if ((--nobjects & (OBJBLKSIZ-1)) == 0) {
//			int	i = nobjects >> OBJBLKSHFT;
//			free((void *)objblock[i]);
//			objblock[i] = NULL;
//		}
//}
}
