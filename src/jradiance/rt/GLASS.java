/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.rt;

import jradiance.common.COLORS.COLOR;
import jradiance.common.FVECT;
import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OBJECT_STRUCTURE;
import jradiance.common.OTYPES;

/**
 *
 * @author arwillis
 */
public class GLASS extends OBJECT_STRUCTURE {
    /*
     *  glass.c - simpler shading function for thin glass surfaces.
     */

    /*
     *  This definition of glass provides for a quick calculation
     *  using a single surface where two closely spaced parallel
     *  dielectric surfaces would otherwise be used.  The chief
     *  advantage to using this material is speed, since internal
     *  reflections are avoided.
     *
     *  The specification for glass is as follows:
     *
     *	modifier glass id
     *	0
     *	0
     *	3+ red grn blu [refractive_index]
     *
     *  The color is used for the transmission at normal incidence.
     *  To compute transmissivity (tn) from transmittance (Tn) use:
     *
     *	tn = (sqrt(.8402528435+.0072522239*Tn*Tn)-.9166530661)/.0036261119/Tn
     *
     *  The transmissivity of standard 88% transmittance glass is 0.96.
     *  A refractive index other than the default can be used by giving
     *  it as the fourth real argument.  The above formula no longer applies.
     *
     *  If we appear to hit the back side of the surface, then we
     *  turn the normal around.
     */
    public static final double RINDEX = 1.52;		/* refractive index of glass */


    @Override
    public int octree_function(Object... obj) {
        return m_glass((OBJREC) obj[0], (RAY) obj[1]);
    }

    int m_glass( /* color a ray which hit a thin glass surface */
            OBJREC m,
            RAY r) {
        COLOR mcolor = new COLOR();
        double pdot;
        FVECT pnorm = new FVECT();
        double rindex = 0, cos2;
        COLOR trans = new COLOR(), refl = new COLOR();
        int hastexture, hastrans;
        double d, r1e, r1m;
        double transtest, transdist;
        double mirtest, mirdist;
        RAY p = new RAY();
        int i;
        /* check arguments */
        if (m.oargs.nfargs == 3) {
            rindex = RINDEX;		/* default value of n */
        } else if (m.oargs.nfargs == 4) {
            rindex = m.oargs.farg[3];	/* use their value */
        } else {
//		objerror(m, USER, "bad arguments");
        }
        /* check transmission */
        mcolor.setcolor((float) m.oargs.farg[0], (float) m.oargs.farg[1], (float) m.oargs.farg[2]);
        if ((hastrans = (COLOR.intens(mcolor) > 1e-15) ? 1 : 0) != 0) {
            for (i = 0; i < 3; i++) {
                if (mcolor.colval(i) < 1e-15) {
                    mcolor.data[i] = 1e-15f;
                }
            }
        } else if ((r.crtype & RAY.SHADOW) != 0) {
            return (1);
        }
        /* get modifiers */
        RAYTRACE.raytexture(r, m.omod);
        if (r.rod < 0.0) /* reorient if necessary */ {
            RAYTRACE.flipsurface(r);
        }
        mirtest = transtest = 0;
        mirdist = transdist = r.rot;
        /* perturb normal */
        if ((hastexture = (FVECT.DOT(r.pert, r.pert) > FVECT.FTINY * FVECT.FTINY) ? 1 : 0) != 0) {
            pdot = RAYTRACE.raynormal(pnorm, r);
        } else {
            FVECT.VCOPY(pnorm, r.ron);
            pdot = r.rod;
        }
        /* angular transmission */
        cos2 = Math.sqrt((1.0 - 1.0 / (rindex * rindex))
                + pdot * pdot / (rindex * rindex));
        if (hastrans != 0) {
            mcolor.setcolor((float) Math.pow(mcolor.colval(COLOR.RED), 1.0 / cos2),
                    (float) Math.pow(mcolor.colval(COLOR.GRN), 1.0 / cos2),
                    (float) Math.pow(mcolor.colval(COLOR.BLU), 1.0 / cos2));
        }

        /* compute reflection */
        r1e = (pdot - rindex * cos2) / (pdot + rindex * cos2);
        r1e *= r1e;
        r1m = (1.0 / pdot - rindex / cos2) / (1.0 / pdot + rindex / cos2);
        r1m *= r1m;
        /* compute transmission */
        if (hastrans != 0) {
            for (i = 0; i < 3; i++) {
                d = mcolor.colval(i);
                trans.data[i] = (float) (.5 * (1.0 - r1e) * (1.0 - r1e) * d
                        / (1.0 - r1e * r1e * d * d));
                trans.data[i] += (float) (.5 * (1.0 - r1m) * (1.0 - r1m) * d
                        / (1.0 - r1m * r1m * d * d));
            }
            COLOR.multcolor(trans, r.pcol);	/* modify by pattern */
            /* transmitted ray */
            if (RAYTRACE.rayorigin(p, RAY.TRANS, r, trans) == 0) {
                if ((r.crtype & RAY.SHADOW) == 0 && hastexture != 0) {
                    FVECT.VSUM(p.rdir, r.rdir, r.pert, 2. * (1. - rindex));
                    if (FVECT.normalize(p.rdir) == 0.0) {
//					objerror(m, WARNING, "bad perturbation");
                        FVECT.VCOPY(p.rdir, r.rdir);
                    }
                } else {
                    FVECT.VCOPY(p.rdir, r.rdir);
                    transtest = 2;
                }
                RAY.rayvalue(p);
                COLOR.multcolor(p.rcol, p.rcoef);
                COLOR.addcolor(r.rcol, p.rcol);
                transtest *= COLOR.bright(p.rcol);
                transdist = r.rot + p.rt;
            }
        }
        if ((r.crtype & RAY.SHADOW) != 0) {		/* skip reflected ray */
            r.rt = transdist;
            return (1);
        }
        /* compute reflectance */
        for (i = 0; i < 3; i++) {
            d = mcolor.colval(i);
            d *= d;
            refl.data[i] = (float) (.5 * r1e * (1.0 + (1.0 - 2.0 * r1e) * d) / (1.0 - r1e * r1e * d));
            refl.data[i] += (float) (.5 * r1m * (1.0 + (1.0 - 2.0 * r1m) * d) / (1.0 - r1m * r1m * d));
        }
        /* reflected ray */
        if (RAYTRACE.rayorigin(p, RAY.REFLECTED, r, refl) == 0) {
            FVECT.VSUM(p.rdir, r.rdir, pnorm, 2. * pdot);
//		checknorm(p.rdir);
            RAY.rayvalue(p);
            COLOR.multcolor(p.rcol, p.rcoef);
            COLOR.addcolor(r.rcol, p.rcol);
            if (hastexture == 0 && r.ro != null && OTYPES.isflat(r.ro.otype) != 0) {
                mirtest = 2.0 * COLOR.bright(p.rcol);
                mirdist = r.rot + p.rt;
            }
        }
        /* check distance */
        d = COLOR.bright(r.rcol);
        if (transtest > d) {
            r.rt = transdist;
        } else if (mirtest > d) {
            r.rt = mirdist;
        }
        return (1);
    }
}
