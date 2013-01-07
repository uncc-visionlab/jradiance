/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

/**
 *
 * @author arwillis
 */
public class OBJSET {
//    #ifndef lint
//static const char	RCSid[] = "$Id$";
//#endif
/*
     *  objset.c - routines for maintaining object sets.
     *
     *  External symbols declared in object.h
     */
//
//#include "copyright.h"
//
//#include  "standard.h"
//
//#include  "octree.h"
//
//#include  "object.h"
//
//#ifndef  OSTSIZ
//#ifdef  SMLMEM
//#define  OSTSIZ		32749		/* object table size (a prime!) */
//#else

    static int OSTSIZ = 262139;		/* object table size (a prime!) */
//#endif
//#endif

    static int[][] ostable = new int[OSTSIZ][];	/* the object set table */


    public static void insertelem( /* insert obj into os, no questions */
            int[] os,
            int obj) {
        int i;

        for (i = os[0]++; i > 0; i--) {
            if (os[i] > obj) {
                os[i + 1] = os[i];
            } else {
                break;
            }
        }
        os[i + 1] = obj;
    }

    public static void deletelem( /* delete obj from os, no questions */
            int[] os,
            int obj) {
        int i;
        int osidx = 0;
        i = os[0]--;
        osidx++;
        while (i > 0 && os[osidx] < obj) {
            i--;
            osidx++;
        }
        while (--i > 0) {
            os[osidx] = os[osidx + 1];
            osidx++;
        }
    }

    public static int inset( /* determine if object is in set */
            int[] os,
            int obj) {
        int upper, lower;
        int cm, i;
        int osidx = 0;
        if ((i = os[0]) <= 12) {	/* linear search algorithm */
            cm = obj;
            while (i-- > 0) {
                if (os[++osidx] == cm) {
                    return (1);
                }
            }
            return (0);
        }
        lower = 1;
        upper = cm = i + 1;
        /* binary search algorithm */
        while ((i = (lower + upper) >> 1) != cm) {
            cm = obj - os[osidx + i];
            if (cm > 0) {
                lower = i;
            } else if (cm < 0) {
                upper = i;
            } else {
                return (1);
            }
            cm = i;
        }
        return (0);
    }

    public static int setequal( /* determine if two sets are equal */
            int[] os1, int osidx, int[] os2) {
        int i, os1idx = osidx, os2idx = 0;
        int nelem = os1[osidx];
        for (i = nelem; i-- >= 0;) { // are we comparing the last elements?
            if (os1[os1idx++] != os2[os2idx++]) {
                return (0);
            }
        }
        return (1);
    }

    public static void setcopy( /* copy object set os2 into os1 */
            int[] os1, int os1idx, int[] os2) {
        int i, os2idx=0;
        int nelem = os2[0];
        for (i = nelem; i-- >= 0;) {
            os1[os1idx++] = os2[os2idx++];
        }
    }

    public static int[] setsave( /* allocate space and save set */
            int[] os) {
        int[] osnew;
        int[] oset;
        int i, osidx = 0;

        if ((osnew = oset = new int[os[1] + 1]) == null) {
//		error(SYSTEM, "out of memory in setsave\n");
        }
        int nelem = os[0];
        for (i = nelem; i-- >= 0;) {	/* inline setcopy */
            oset[osidx] = os[osidx];
            osidx++;
        }
        return (osnew);
    }

    public static void setunion( /* osr = os1 Union os2 */
            int[] osr, int[] os1, int[] os2) {
        int i1, i2;

        osr[0] = 0;
        for (i1 = i2 = 1; i1 <= os1[0] || i2 <= os2[0];) {
            while (i1 <= os1[0] && (i2 > os2[0] || os1[i1] <= os2[i2])) {
                osr[++osr[0]] = os1[i1];
                if (i2 <= os2[0] && os2[i2] == os1[i1]) {
                    i2++;
                }
                i1++;
            }
            while (i2 <= os2[0] && (i1 > os1[0] || os2[i2] < os1[i1])) {
                osr[++osr[0]] = os2[i2++];
            }
        }
    }

    public void setintersect( /* osr = os1 Intersect os2 */
            int[] osr, int[] os1, int[] os2) {
        int i1, i2;

        osr[0] = 0;
        if (os1[0] <= 0) {
            return;
        }
        for (i1 = i2 = 1; i2 <= os2[0];) {
            while (os1[i1] < os2[i2]) {
                if (++i1 > os1[0]) {
                    return;
                }
            }
            while (os2[i2] < os1[i1]) {
                if (++i2 > os2[0]) {
                    return;
                }
            }
            if (os1[i1] == os2[i2]) {
                osr[++osr[0]] = os2[i2++];
            }
        }
    }

    public static int fullnode( /* return octree for object set */
            int[] oset) {
        int osentry, ntries;
        long hval;
        int ot;
        int i, osidx = 0;
        int[] os;
        /* hash on set */
        hval = 0;
        os = oset;
        i = os[osidx++];
        while (i-- > 0) {
            hval += os[osidx++];
        }
        ntries = 0;
        tryagain:
        osentry = (int) (hval + (long) ntries * ntries) % OSTSIZ;
        osidx=0;
        os = ostable[osentry];
        if (os == null) {
            os = ostable[osentry] = new int[oset[0] + 2];//(OBJECT *)malloc(
//				(unsigned)(oset[0]+2)*sizeof(OBJECT));
            if (os == null) {
//			goto memerr;
                return 0;
            }
            ot = OCTREE.oseti(osentry);
        } else {
            /* look for set */
            int ostableidx=0;
            //for (i = 0; os[0] > 0; i++, os = ostable[ostableidx]) {
            for (i = 0; os[osidx] > 0; i++, osidx += os[osidx] + 1) {
                if (setequal(os, osidx, oset) != 0) {
                    break;
                }
            }
            ot = OCTREE.oseti(i * OSTSIZ + osentry);
            if (os[osidx] > 0) /* found it */ {
                return (ot);
            }
            if (OCTREE.isfull(ot) == 0) {		/* entry overflow */
                if (++ntries < OSTSIZ) {
                    System.out.println("JAVA PORT BROKEN HERE");
//				break tryagain;
                } else {
//				error(INTERNAL, "hash table overflow in fullnode");
                }
            }
            /* remember position */
//		i = os[osidx] - ostable[osentry];
            i = osidx;
            int[] newostable = new int[i + oset[0] + 2];
            System.arraycopy(ostable[osentry], 0, newostable, 0, ostable[osentry].length);
            os = ostable[osentry] = newostable;
//            os = ostable[osentry] = new int[i + oset[0] + 2]; //(OBJECT *)realloc(
//				(void *)ostable[osentry],
//				(unsigned)(i+oset[0]+2)*sizeof(OBJECT));
            if (os == null) {
                //goto memerr;
                return 0;
            }
            osidx = i;			/* last entry */
        }
        setcopy(os, osidx, oset);		/* add new set */
//        System.out.print(String.format("%d",os[osidx]));
        osidx += os[osidx] + 1;
        os[osidx] = 0;			/* terminator */
        return (ot);
//memerr:
//	error(SYSTEM, "out of memory in fullnode");
//	return 0; /* pro forma return */
    }

    public static void objset( /* get object set for full node */
            int[] oset,
            int ot) {
        int[] os;
        int i, osidx = 0, osetidx = 0;

        if (OCTREE.isfull(ot) == 0) {
//		goto noderr;
//error(CONSISTENCY, "bad node in objset");
        }
        ot = OCTREE.oseti(ot);
        if ((os = ostable[ot % OSTSIZ]) == null) {
//		goto noderr;
//error(CONSISTENCY, "bad node in objset");
        }

        for (i = ot / OSTSIZ; i-- != 0; osidx += os[osidx] + 1) {
            if (os[osidx] <= 0) {
//			goto noderr;
//                        error(CONSISTENCY, "bad node in objset");
            }
        }
        for (i = os[osidx]; i-- >= 0;) /* copy set here */ {
            oset[osetidx++] = os[osidx++];
        }
        return;
//noderr:
//	error(CONSISTENCY, "bad node in objset");
    }

    void donesets() /* free ALL SETS in our table */ {
//	register int  n;
//
//	for (n = 0; n < OSTSIZ; n++) 
//		if (ostable[n] != NULL) {
//			free((void *)ostable[n]);
//			ostable[n] = NULL;
//		}
    }
}
