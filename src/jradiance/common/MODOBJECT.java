/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

import java.util.ArrayList;
import java.util.HashMap;
import jradiance.common.OBJECT.OBJREC;

/**
 *
 * @author arwillis
 */
public class MODOBJECT {
//    #ifndef lint
//static const char RCSid[] = "$Id$";
//#endif
/*
 *  Routines for tracking object modifiers
 *
 *  External symbols declared in object.h
 */


static HashMap<String, Integer> str2objidx = new HashMap();
//public static class ohtab {
//	int  hsiz;			/* current table size */
//	int[]  htab;			/* table, if allocated */
//        ohtab(int hsize, int[] htab) {
//            this.hsiz = hsize;
//            this.htab = htab;
//        }
//} 
//static ohtab modtab,objtab;
//static {
//modtab = new ohtab(100, null);
//objtab = new ohtab(1000, null);	/* modifiers and objects */
//}
//static int  otndx(char *, struct ohtab *);


public static int
objndx(			/* get object number from pointer */
OBJREC  op)
{
	int  i, j;
//  POINTER MAGIC HERE ** THIS CAN'T BE DONE IN JAVA
//        System.out.println("MODOBJECT::objndx() FUNCTION NOT SUPPORTED");
	for (i = OBJECT.nobjects>>OBJECT.OBJBLKSHFT; i >= 0; i--) {
//		j = op - objsrc.objblock[i];
//		if ((j >= 0) && (j < OBJECT.OBJBLKSIZ))
//			return((i<<OBJECT.OBJBLKSHFT) + j);
            for (j=0; j < OBJECT.OBJBLKSHFT; j++) {
                if (op == OBJECT.objblock[i][j]) {
			return((i<<OBJECT.OBJBLKSHFT) + j);
                    
                }
            }
	}
	return OBJECT.OVOID;
}


public static int
lastmod(		/* find modifier definition before obj */
int  obj,
String mname)
{
	OBJECT.OBJREC  op;
	int  i;

	i = modifier(mname);		/* try hash table first */
	if ((obj == OBJECT.OVOID) | (i < obj))
		return(i);
	for (i = obj; i-- > 0; ) {	/* need to search */
		op = OBJECT.objptr(i);
		if (OTYPES.ismodifier(op.otype)!=0 && !op.oname.equals(mname))
			return(i);
	}
	return OBJECT.OVOID;
}


public static int
modifier(			/* get a modifier number from its name */
String mname)
{
//	int  ndx=0;
        return str2objidx.get(mname);
//	ndx = otndx(mname, modtab);
//	return modtab.htab[ndx];
}


//#ifdef  GETOBJ
//OBJECT
//object(oname)			/* get an object number from its name */
//char  *oname;
//{
//	register int  ndx;
//
//	ndx = otndx(oname, &objtab);
//	return(objtab.htab[ndx]);
//}
//#endif


public static void
insertobject(		/* insert new object into our list */
int  objIdx)
{
	int  i=0;
        OBJECT.OBJREC obj = OBJECT.objptr(objIdx);
	if (OTYPES.ismodifier(obj.otype) != 0) {
//		i = otndx(OBJECT.objptr(obj).oname, modtab);
//		modtab.htab[i] = obj;
            //ArrayList<OBJECT.OBJREC> objList = modtab2.get(obj.oname);
//            if (objList == null) {
//                objList = new ArrayList<OBJECT.OBJREC>();
//                modtab2.put(obj.oname, objList);
//            }
//            objList.add(obj);
            str2objidx.put(obj.oname,objIdx);
	}
//#ifdef  GETOBJ
//	else {
//		i = otndx(objptr(obj)->oname, &objtab);
//		objtab.htab[i] = obj;
//	}
//#endif
//	for (i = 0; addobjnotify[i] != null; i++)
//		(*addobjnotify[i])(obj);
        OBJECT.notifyListeners(obj);
}


void
clearobjndx()			/* clear object hash tables */
{
//	if (modtab.htab != NULL) {
//		free((void *)modtab.htab);
//		modtab.htab = NULL;
//		modtab.hsiz = 100;
//	}
//	if (objtab.htab != NULL) {
//		free((void *)objtab.htab);
//		objtab.htab = NULL;
//		objtab.hsiz = 100;
//	}
}


static int
nexthsiz(		/* return next hash table size */
int  oldsiz)
{
	int  hsiztab[] = {
		251, 509, 1021, 2039, 4093, 8191, 16381, 0
	};
        int hspidx=0;
	int[] hsp = hsiztab;

	for (hspidx = 0; hsp[hspidx] != 0; hspidx++)
		if (hsp[hspidx] > oldsiz)
			return(hsp[hspidx]);
	return(oldsiz*2 + 1);		/* not always prime */
}


//static int
//otndx(		/* get object table index for name */
//char[] name,
//ohtab[] tab)
//{
//	OBJECT  *oldhtab;
//	int  hval, i;
//	register int  ndx;
//
//	if (tab->htab == NULL) {		/* new table */
//		tab->hsiz = nexthsiz(tab->hsiz);
//		tab->htab = (OBJECT *)malloc(tab->hsiz*sizeof(OBJECT));
//		if (tab->htab == NULL)
//			error(SYSTEM, "out of memory in otndx");
//		ndx = tab->hsiz;
//		while (ndx--)			/* empty it */
//			tab->htab[ndx] = OVOID;
//	}
//					/* look up object */
//	hval = shash(name);
//tryagain:
//	for (i = 0; i < tab->hsiz; i++) {
//		ndx = (hval + (unsigned long)i*i) % tab->hsiz;
//		if (tab->htab[ndx] == OVOID ||
//				!strcmp(objptr(tab->htab[ndx])->oname, name))
//			return(ndx);
//	}
//					/* table is full, reallocate */
//	oldhtab = tab->htab;
//	ndx = tab->hsiz;
//	tab->htab = NULL;
//	while (ndx--)
//		if (oldhtab[ndx] != OVOID) {
//			i = otndx(objptr(oldhtab[ndx])->oname, tab);
//			tab->htab[i] = oldhtab[ndx];
//		}
//	free((void *)oldhtab);
//	goto tryagain;			/* should happen only once! */
//        return 0;
//}
}
