/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.rt;

import jradiance.common.COLORS.COLOR;
import jradiance.common.FVECT;
import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OBJECT_STRUCTURE;
import jradiance.rt.SOURCE.SRCREC;

/**
 *
 * @author arwillis
 */
public class M_MIST extends OBJECT_STRUCTURE {
    /*
     * Mist volumetric material.
     */

    /*
     *  A mist volume is used to specify a region in the scene where a certain
     *  light source (or sources) is going to contribute to scattering.  The
     *  material can add to the existing global medium, and override any ray
     *  settings for scattering albedo and eccentricity.  Overlapping mist
     *  regions should agree w.r.t. albedo and eccentricity, and
     *  should have disjoint source lists.
     *
     *  A pattern, if used, should compute the line integral of extinction,
     *  and will modify the first three arguments directly.  This will tend
     *  to invalidate results when there are other objects intersected within
     *  the mist region.
     *
     *  The string arguments for MAT_MIST are the identifiers for the important
     *  light sources, which will be looked up in the source array.  The last
     *  source found matching a name is the one used.  A relayed light source
     *  may be indicated by the relay surface name, followed by a '>' character,
     *  followed by the relayed source name (which may be another relay).
     *
     *  Up to five real arguments may be given for MAT_MIST:
     *
     *	[ext_r  ext_g  ext_b  [albedo_r albedo_g albedo_b [gecc]]]
     *
     *  The primaries indicate medium extinction per unit length (absorption
     *  plus scattering), which is added to the global extinction coefficient, set
     *  by the -me option.  The albedo is the ratio of scattering to extinction,
     *  and is set globally by the -ma option (salbedo) and overridden here.
     *  The Heyney-Greenstein eccentricity parameter (-mg seccg) indicates how much
     *  scattering favors the forward direction.  A value of 0 means isotropic
     *  scattering.  A value approaching 1 indicates strong forward scattering.
     */
    public static final int MAXSLIST = 32;	/* maximum sources to check */

    public static final char RELAYDELIM = '>';		/* relay delimiter character */


    static int inslist( /* return index of source n if it's in list sl */
            int[] sl,
            int n) {
        int i;

        for (i = sl[0]; i > 0; i--) {
            if (sl[i] == n) {
                return (i);
            }
        }
        return (0);
    }

    static int srcmatch( /* check for an id match on a light source */
            SRCREC sp,
            String id) {
        int cpidx = 0;
        /* check for relay sources */
        while ((cpidx = id.indexOf(RELAYDELIM)) != -1) {
            if ((sp.sflags & SOURCE.SVIRTUAL) == 0 || sp.so == null) {
                return (0);
            }
            if (id.startsWith(sp.so.oname) || sp.so.oname.charAt(cpidx) != 0) {
                return (0);
            }
            cpidx = cpidx + 1;				/* relay to next */
            sp = SRCSUPP.source[sp.sa.sv.sn];
        }
        if ((sp.sflags & SOURCE.SVIRTUAL) != 0 || sp.so == null) {
            return (0);
        }
        return (id.equals(sp.so.oname) ? 0 : 1);
    }
    static int[] slspare = new int[MAXSLIST + 1];	/* in case of emergence */


    static void add2slist( /* add source list to ray's */
            RAY r,
            int[] sl) {
        int i;

        if (sl == null || sl[0] == 0) /* nothing to add */ {
            return;
        }
        if (r.slights == null) {
            (r.slights = slspare)[0] = 0;	/* just once per ray path */
        }
        for (i = sl[0]; i > 0; i--) {
            if (inslist(r.slights, sl[i]) == 0) {
                if (r.slights[0] >= MAXSLIST) {
//				error(INTERNAL,
//					"scattering source list overflow");
                }
                r.slights[++r.slights[0]] = sl[i];
            }
        }
    }

    @Override
    public int octree_function(Object... obj) {
        return m_mist((OBJREC) obj[0], (RAY) obj[1]);
    }

    int m_mist( /* process a ray entering or leaving some mist */
            OBJREC m,
            RAY r) {
        RAY p = new RAY();
        int[] myslist = null;
        int[] newslist = new int[MAXSLIST + 1];
        COLOR mext = new COLOR();
        double re, ge, be;
        int i, j;
        /* check arguments */
        if (m.oargs.nfargs > 7) {
//		objerror(m, USER, "bad arguments");
        }
        /* get source indices */
        if (m.oargs.nsargs > 0 && (myslist = m.os.getOS()) == null) {
            if (m.oargs.nsargs > MAXSLIST) {
//			objerror(m, INTERNAL, "too many sources in list");
            }
            myslist = new int[m.oargs.nsargs + 1];
//		if (myslist == null)
//			goto memerr;
            myslist[0] = 0;			/* size is first in list */
            for (j = 0; j < m.oargs.nsargs; j++) {
                i = SRCSUPP.nsources;		/* look up each source id */
                while (i-- != 0) {
                    if (srcmatch(SRCSUPP.source[i], m.oargs.sarg[j]) != 0) {
                        break;
                    }
                }
                if (i < 0) {
//				sprintf(errmsg, "unknown source \"%s\"",
//						m.oargs.sarg[j]);
//				objerror(m, WARNING, errmsg);
                } else if (inslist(myslist, i) != 0) {
//				sprintf(errmsg, "duplicate source \"%s\"",
//						m.oargs.sarg[j]);
//				objerror(m, WARNING, errmsg);
                } else {
                    myslist[++myslist[0]] = i;
                }
            }
            m.os.setOS(myslist);// = (char *)myslist;
        }
        if (m.oargs.nfargs > 2) {		/* compute extinction */
            mext.setcolor((float) m.oargs.farg[0], (float) m.oargs.farg[1],
                    (float) m.oargs.farg[2]);
            RAYTRACE.raytexture(r, m.omod);			/* get modifiers */
            COLOR.multcolor(mext, r.pcol);
        } else {
            mext.setcolor(0.f, 0.f, 0.f);
        }
        /* start transmitted ray */
        if (RAYTRACE.rayorigin(p, RAY.TRANS, r, null) < 0) {
            return (1);
        }
        FVECT.VCOPY(p.rdir, r.rdir);
        p.slights = newslist;
        if (r.slights != null) /* copy old list if one */ {
            for (j = r.slights[0]; j >= 0; j--) {
                p.slights[j] = r.slights[j];
            }
        } else {
            p.slights[0] = 0;
        }
        if (r.rod > 0.) {			/* entering ray */
            COLOR.addcolor(p.cext, mext);
            if (m.oargs.nfargs > 5) {
                p.albedo.setcolor((float) m.oargs.farg[3],
                        (float) m.oargs.farg[4], (float) m.oargs.farg[5]);
            }
            if (m.oargs.nfargs > 6) {
                p.gecc = (float) m.oargs.farg[6];
            }
            add2slist(p, myslist);			/* add to list */
        } else {				/* leaving ray */
            if (myslist != null) {			/* delete from list */
                for (j = myslist[0]; j > 0; j--) {
                    if ((i = inslist(p.slights, myslist[j])) != 0) {
                        p.slights[i] = -1;
                    }
                }
                for (i = 0, j = 1; j <= p.slights[0]; j++) {
                    if (p.slights[j] != -1) {
                        p.slights[++i] = p.slights[j];
                    }
                }
                if (p.slights[0] - i < myslist[0]) {	/* fix old */
                    COLOR.addcolor(r.cext, mext);
                    if (m.oargs.nfargs > 5) {
                        r.albedo.setcolor((float) m.oargs.farg[3],
                                (float) m.oargs.farg[4], (float) m.oargs.farg[5]);
                    }
                    if (m.oargs.nfargs > 6) {
                        r.gecc = (float) m.oargs.farg[6];
                    }
                    add2slist(r, myslist);
                }
                p.slights[0] = i;
            }
            if ((re = r.cext.colval(COLOR.RED) - mext.colval(COLOR.RED))
                    < RAYCALLS.cextinction.colval(COLOR.RED)) {
                re = RAYCALLS.cextinction.colval(COLOR.RED);
            }
            if ((ge = r.cext.colval(COLOR.GRN) - mext.colval(COLOR.GRN))
                    < RAYCALLS.cextinction.colval(COLOR.GRN)) {
                ge = RAYCALLS.cextinction.colval(COLOR.GRN);
            }
            if ((be = r.cext.colval(COLOR.BLU) - mext.colval(COLOR.BLU))
                    < RAYCALLS.cextinction.colval(COLOR.BLU)) {
                be = RAYCALLS.cextinction.colval(COLOR.BLU);
            }
            p.cext.setcolor((float) re, (float) ge, (float) be);
            if (m.oargs.nfargs > 5) {
                COLOR.copycolor(p.albedo, RAYCALLS.salbedo);
            }
            if (m.oargs.nfargs > 6) {
                p.gecc = (float) RAYCALLS.seccg;
            }
        }
        RAY.rayvalue(p);				/* calls rayparticipate() */
        COLOR.copycolor(r.rcol, p.rcol);		/* return value */
        r.rt = r.rot + p.rt;
        return (1);
//memerr:
//	error(SYSTEM, "out of memory in m_mist");
//	return 0; /* pro forma return */
    }
}
