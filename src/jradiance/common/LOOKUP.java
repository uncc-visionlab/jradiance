/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

import java.util.Comparator;
import jradiance.rt.DEVCOMM;

/**
 *
 * @author arwillis
 */
public class LOOKUP {
    /*
     * Header file for general associative table lookup routines
     */
//
//typedef void lut_free_t(void *p);
//typedef unsigned long lut_hashf_t(const char *);
//typedef int lut_keycmpf_t(const char *, const char *);

    public static class LUENT {

        Object key;			/* key name */

        long hval;		/* key hash value (for efficiency) */

        Object data;			/* pointer to client data */

    }

    public static abstract class LUTAB implements Comparator {

        abstract long hashVal(char[] s);

        abstract int compare(char[] s1, char[] s2);
        abstract long hashVal(Object o);
//	lut_hashf_t *hashf;		/* key hash function */
//	lut_keycmpf_t *keycmp;		/* key comparison function */
//	lut_free_t *freek;		/* free a key */
//	lut_free_t *freed;		/* free the data */
        int tsiz;			/* current table size */

        LUENT[] tabl;			/* table, if allocated */

        int ndel;			/* number of deleted entries */

    }
    /*
     * The lu_init routine is called to initialize a table.  The number of
     * elements passed is not a limiting factor, as a table can grow to
     * any size permitted by memory.  However, access will be more efficient
     * if this number strikes a reasonable balance between default memory use
     * and the expected (minimum) table size.  The value returned is the
     * actual allocated table size (or zero if there was insufficient memory).
     *
     * The hashf, keycmp, freek and freed member functions must be assigned
     * separately.  If the hash value is sufficient to guarantee equality
     * between keys, then the keycmp pointer may be null.  Otherwise, it
     * should return 0 if the two passed keys match.  If it is not necessary
     * (or possible) to free the key and/or data values, then the freek and/or
     * freed member functions may be null.
     *
     * It isn't fully necessary to call lu_init to initialize the LUTAB structure.
     * If tsiz is 0, then the first call to lu_find will allocate a minimal table.
     * The LU_SINIT macro provides a convenient static declaration for character
     * string keys.
     *
     * The lu_find routine returns the entry corresponding to the given
     * key.  If the entry does not exist, the corresponding key field will
     * be null.  If the entry has been previously deleted but not yet freed,
     * then only the data field will be null.  It is the caller's
     * responsibility to (allocate and) assign the key and data fields when
     * creating a new entry.  The only case where lu_find returns null is when
     * the system has run out of memory.
     *
     * The lu_delete routine frees an entry's data (if any) by calling
     * the freed member function, but does not free the key field.  This
     * will be freed later during (or instead of) table reallocation.
     * It is therefore an error to reuse or do anything with the key
     * field after calling lu_delete.
     *
     * The lu_doall routine loops through every filled table entry, calling
     * the given function once on each entry.  If a null pointer is passed
     * for this function, then lu_doall simply returns the total number of
     * active entries.  Otherwise, it returns the sum of all the function
     * evaluations.
     *
     * The lu_done routine calls the given free function once for each
     * assigned table entry (i.e. each entry with an assigned key value).
     * The user must define these routines to free the key and the data
     * in the LU_TAB structure.  The final action of lu_done is to free the
     * allocated table itself.
     */
//typedef int lut_doallf_t(const LUENT *e, void *p);
//
//extern int	lu_init(LUTAB *tbl, int nel);
//extern lut_hashf_t	lu_shash;
//extern LUENT	*lu_find(LUTAB *tbl, const char *key);
//extern void	lu_delete(LUTAB *tbl, const char *key);
//extern int	lu_doall(const LUTAB *tbl, lut_doallf_t *f, void *p);
//extern void	lu_done(LUTAB *tbl);
//
//#define LU_SINIT(fk,fd) {lu_shash,strcmp,fk,fd,0,null,0}

    /*
     * Table lookup routines
     */
    static int hsiztab[] = {
        31, 61, 127, 251, 509, 1021, 2039, 4093, 8191, 16381,
        32749, 65521, 131071, 262139, 524287, 1048573, 2097143,
        4194301, 8388593, 0
    };

    static int lu_init( /* initialize tbl for at least nel elements */
            LUTAB tbl,
            int nel) {
        int hspidx = 0;
        int[] hsp;

        nel += nel >> 1;			/* 66% occupancy */
        hsp = hsiztab;
        for (hspidx = 0; hsp[hspidx] != 0; hspidx++) {
            if (hsp[hspidx] > nel) {
                break;
            }
        }
        if ((tbl.tsiz = hsp[hspidx]) == 0) {
            tbl.tsiz = nel * 2 + 1;		/* not always prime */
        }
        tbl.tabl = new LUENT[tbl.tsiz];
        for (hspidx = 0; hspidx < tbl.tabl.length; hspidx++) {
            tbl.tabl[hspidx] = new LUENT();
        }
        if (tbl.tabl == null) {
            tbl.tsiz = 0;
        }
        tbl.ndel = 0;
        return (tbl.tsiz);
    }

    public static class SLUTAB extends LUTAB {

        static char[] shuffle = {
            0, 157, 58, 215, 116, 17, 174, 75, 232, 133, 34,
            191, 92, 249, 150, 51, 208, 109, 10, 167, 68, 225,
            126, 27, 184, 85, 242, 143, 44, 201, 102, 3, 160,
            61, 218, 119, 20, 177, 78, 235, 136, 37, 194, 95,
            252, 153, 54, 211, 112, 13, 170, 71, 228, 129, 30,
            187, 88, 245, 146, 47, 204, 105, 6, 163, 64, 221,
            122, 23, 180, 81, 238, 139, 40, 197, 98, 255, 156,
            57, 214, 115, 16, 173, 74, 231, 132, 33, 190, 91,
            248, 149, 50, 207, 108, 9, 166, 67, 224, 125, 26,
            183, 84, 241, 142, 43, 200, 101, 2, 159, 60, 217,
            118, 19, 176, 77, 234, 135, 36, 193, 94, 251, 152,
            53, 210, 111, 12, 169, 70, 227, 128, 29, 186, 87,
            244, 145, 46, 203, 104, 5, 162, 63, 220, 121, 22,
            179, 80, 237, 138, 39, 196, 97, 254, 155, 56, 213,
            114, 15, 172, 73, 230, 131, 32, 189, 90, 247, 148,
            49, 206, 107, 8, 165, 66, 223, 124, 25, 182, 83,
            240, 141, 42, 199, 100, 1, 158, 59, 216, 117, 18,
            175, 76, 233, 134, 35, 192, 93, 250, 151, 52, 209,
            110, 11, 168, 69, 226, 127, 28, 185, 86, 243, 144,
            45, 202, 103, 4, 161, 62, 219, 120, 21, 178, 79,
            236, 137, 38, 195, 96, 253, 154, 55, 212, 113, 14,
            171, 72, 229, 130, 31, 188, 89, 246, 147, 48, 205,
            106, 7, 164, 65, 222, 123, 24, 181, 82, 239, 140,
            41, 198, 99
        };

        long lu_shash( /* hash a nul-terminated string */
                char[] s) {
            int i = 0;
            long h = 0;
            char[] t = s;
            int tidx = 0;
            while (t[tidx] != 0) {
                h ^= (long) shuffle[t[tidx++]] << ((i += 11) & 0xf);
            }

            return (h);
        }

        @Override
        public long hashVal(char[] s) {
            return lu_shash(s);
        }

        @Override
        public int compare(char[] s1, char[] s2) {
            return DEVCOMM.strcmp(s1, s2);
        }

        public int compareTo(Object o) {
            if (!(o instanceof String)) {
                //throw new Exception("Cannot compare this object!");
                return -1;
            }
            String str = (String) o;
            return str.compareTo(str);
        }

        @Override
        public int compare(Object o1, Object o2) {
//            return -1;
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        long hashVal(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    static LUENT lu_find( /* find a table entry */
            LUTAB tbl,
            Object keyStr) {
        long hval;
        int i, n;
        int ndx;
        LUENT le;
        LUENT[] learr;
//        char[] key = keyStr.toCharArray();
        /* look up object */
        if (tbl.tsiz == 0 && lu_init(tbl, 1) == 0) {
            return (null);
        }
//        hval = tbl.hashVal(key);
        hval = tbl.hashVal(keyStr);
//tryagain:
        ndx = (int) (hval % tbl.tsiz);
        for (i = 0, n = 1; i < tbl.tsiz; i++, n += 2) {
            le = tbl.tabl[ndx];
            if (le.key == null) {
                le.hval = hval;
                return (le);
            }
            if (le.hval == hval
                    && (tbl.compare(le.key, keyStr)) == 0) {
                return (le);
            }
            if ((ndx += n) >= tbl.tsiz) /* this happens rarely */ {
                ndx = ndx % tbl.tsiz;
            }
        }
        /* table is full, reallocate */
        learr = tbl.tabl;
        ndx = tbl.tsiz;
        i = tbl.ndel;
        if (lu_init(tbl, ndx - i + 1) == 0) {	/* no more memory! */
            tbl.tabl = learr;
            tbl.tsiz = ndx;
            tbl.ndel = i;
            return (null);
        }
        /*
         * The following code may fail if the user has reclaimed many
         * deleted entries and the system runs out of memory in a
         * recursive call to lu_find().
         */
        return null;
//	while (ndx-- != 0)
//		if (learr[ndx].key != null) {
//			if (learrndx].data != null)
//				*lu_find(tbl,learr[ndx].key) = le[ndx];
//			else if (tbl.freek != null)
//				(*tbl.freek)(learr[ndx].key);
//		}
//	free((void *)learr);
//	goto tryagain;			/* should happen only once! */
    }

    static void lu_delete( /* delete a table entry */
            LUTAB tbl,
            String key) {
        LUENT le;

        if ((le = lu_find(tbl, key)) == null) {
            return;
        }
        if (le.key == null || le.data == null) {
            return;
        }
//	if (tbl.freed != null)
//		(*tbl.freed)(le.data);
        le.data = null;
        tbl.ndel++;
    }

    int lu_doall( /* loop through all valid table entries */
            LUTAB tbl /* int	(*f)(const LUENT *) */ //	lut_doallf_t *f,
            //	void *p
            ) {
        int rval = 0;
        LUENT tp;

//	for (tp = tbl.tabl + tbl.tsiz; tp-- > tbl.tabl; )
//		if (tp.data != null) {
//			if (f != null) {
//				int	r = (*f)(tp, p);
//				if (r < 0)
//					return(-1);
//				rval += r;
//			} else
//				rval++;
//		}
        return (rval);
    }

    static void lu_done( /* free table and contents */
            LUTAB tbl) {
        LUENT tp;
        int tpidx = tbl.tabl.length - 1;

        if (tbl.tsiz == 0) {
            return;
        }
        for (tp = tbl.tabl[tpidx]; tpidx-- > 0;) {
            if (tp.key != null) {
//			if (tbl.freek != null)
//				(*tbl.freek)(tp.key);
//			if (tp.data != null && tbl.freed != null)
//				(*tbl.freed)(tp.data);
                tp.data = null;
                tp = null;
            }
        }
        //free((void *)tbl.tabl);
        tbl.tabl = null;
        tbl.tsiz = 0;
        tbl.ndel = 0;
    }
}
