/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

/**
 *
 * @author arwillis
 */
public class TMESH {
    /*
     * Header file for triangle mesh routines using barycentric coordinates
     */

    public static final String TCALNAME = "tmesh.cal";	/* the name of our auxiliary file */


    public static class BARYCCM {

        int ax;		/* major axis */

        double[][] tm = new double[2][3];	/* transformation */

    }
    public static final double COSTOL = 0.999995;	/* cosine of tolerance for smoothing */

    /* flat_tri() return values */
    public static final int ISBENT = 0;		/* is not flat */

    public static final int ISFLAT = 1;		/* is flat */

    public static final int RVBENT = 2;		/* reversed and not flat */

    public static final int RVFLAT = 3;		/* reversed and flat */

    public static final int DEGEN = -1;		/* degenerate (zero area) */

    /*
     * Compute and print barycentric coordinates for triangle meshes
     */
    static double ABS(double x) {
        return ((x) >= 0 ? (x) : -(x));
    }

    public static int flat_tri( /* determine if triangle is flat */
            FVECT v1, FVECT v2, FVECT v3, FVECT n1, FVECT n2, FVECT n3) {
        double d1, d2, d3;
        FVECT vt1 = new FVECT(), vt2 = new FVECT(), vn = new FVECT();
        /* compute default normal */
        FVECT.VSUB(vt1, v2, v1);
        FVECT.VSUB(vt2, v3, v2);
        FVECT.VCROSS(vn, vt1, vt2);
        if (FVECT.normalize(vn) == 0.0) {
            return (DEGEN);
        }
        /* compare to supplied normals */
        d1 = FVECT.DOT(vn, n1);
        d2 = FVECT.DOT(vn, n2);
        d3 = FVECT.DOT(vn, n3);
        if (d1 < 0 && d2 < 0 && d3 < 0) {
            if (d1 > -COSTOL || d2 > -COSTOL || d3 > -COSTOL) {
                return (RVBENT);
            }
            return (RVFLAT);
        }
        if (d1 < COSTOL || d2 < COSTOL || d3 < COSTOL) {
            return (ISBENT);
        }
        return (ISFLAT);
    }

    static int comp_baryc( /* compute barycentric vectors */
            BARYCCM bcm,
            FVECT v1, FVECT v2, FVECT v3) {
        FVECT vt;
        FVECT va = new FVECT(), vab = new FVECT(), vcb = new FVECT();
        double d;
        int ax0, ax1;
        int i;
        /* compute major axis */
        FVECT.VSUB(vab, v1, v2);
        FVECT.VSUB(vcb, v3, v2);
        FVECT.VCROSS(va, vab, vcb);
        bcm.ax = ABS(va.data[0]) > ABS(va.data[1]) ? 0 : 1;
        bcm.ax = ABS(va.data[bcm.ax]) > ABS(va.data[2]) ? bcm.ax : 2;
        if ((ax0 = bcm.ax + 1) >= 3) {
            ax0 -= 3;
        }
        if ((ax1 = ax0 + 1) >= 3) {
            ax1 -= 3;
        }
        for (i = 0; i < 2; i++) {
            vab.data[0] = v1.data[ax0] - v2.data[ax0];
            vcb.data[0] = v3.data[ax0] - v2.data[ax0];
            vab.data[1] = v1.data[ax1] - v2.data[ax1];
            vcb.data[1] = v3.data[ax1] - v2.data[ax1];
            d = vcb.data[0] * vcb.data[0] + vcb.data[1] * vcb.data[1];
            if (d <= FVECT.FTINY * FVECT.FTINY) {
                return (-1);
            }
            d = (vcb.data[0] * vab.data[0] + vcb.data[1] * vab.data[1]) / d;
            va.data[0] = vab.data[0] - vcb.data[0] * d;
            va.data[1] = vab.data[1] - vcb.data[1] * d;
            d = va.data[0] * va.data[0] + va.data[1] * va.data[1];
            if (d <= FVECT.FTINY * FVECT.FTINY) {
                return (-1);
            }
            d = 1.0 / d;
            bcm.tm[i][0] = va.data[0] *= d;
            bcm.tm[i][1] = va.data[1] *= d;
            bcm.tm[i][2] = -(v2.data[ax0] * va.data[0] + v2.data[ax1] * va.data[1]);
            /* rotate vertices */
            vt = v1;
            v1 = v2;
            v2 = v3;
            v3 = vt;
        }
        return (0);
    }

    static void eval_baryc( /* evaluate barycentric weights at p */
            double[] wt,
            FVECT p,
            BARYCCM bcm) {
        double u, v;
        int i;

        if ((i = bcm.ax + 1) >= 3) {
            i -= 3;
        }
        u = p.data[i];
        if (++i >= 3) {
            i -= 3;
        }
        v = p.data[i];
        wt[0] = u * bcm.tm[0][0] + v * bcm.tm[0][1] + bcm.tm[0][2];
        wt[1] = u * bcm.tm[1][0] + v * bcm.tm[1][1] + bcm.tm[1][2];
        wt[2] = 1. - wt[1] - wt[0];
    }

    public static int get_baryc( /* compute barycentric weights at p */
            double[] wt,
            FVECT p,
            FVECT v1, FVECT v2, FVECT v3) {
        BARYCCM bcm = new BARYCCM();

        if (comp_baryc(bcm, v1, v2, v3) < 0) {
            return (-1);
        }
        eval_baryc(wt, p, bcm);
        return (0);
    }
//
//
//#if 0
//int
//get_baryc(wt, p, v1, v2, v3)	/* compute barycentric weights at p */
//RREAL	wt[3];
//FVECT	p;
//FVECT	v1, v2, v3;
//{
//	FVECT	ac, bc, pc, cros;
//	double	normf;
//				/* area formula w/o 2-D optimization */
//	VSUB(ac, v1, v3);
//	VSUB(bc, v2, v3);
//	VSUB(pc, p, v3);
//	VCROSS(cros, ac, bc);
//	normf = DOT(cros,cros)
//	if (normf <= 0.0)
//		return(-1);
//	normf = 1./sqrt(normf);
//	VCROSS(cros, bc, pc);
//	wt[0] = VLEN(cros) * normf;
//	VCROSS(cros, ac, pc);
//	wt[1] = VLEN(cros) * normf;
//	wt[2] = 1. - wt[1] - wt[0];
//	return(0);
//}
//#endif
//
//
//void
//put_baryc(bcm, com, n)		/* put barycentric coord. vectors */
//register BARYCCM	*bcm;
//register RREAL		com[][3];
//int			n;
//{
//	double	a, b;
//	register int	i;
//
//	printf("%d\t%d\n", 1+3*n, bcm.ax);
//	for (i = 0; i < n; i++) {
//		a = com[i][0] - com[i][2];
//		b = com[i][1] - com[i][2];
//		printf("%14.8f %14.8f %14.8f\n",
//			bcm.tm[0][0]*a + bcm.tm[1][0]*b,
//			bcm.tm[0][1]*a + bcm.tm[1][1]*b,
//			bcm.tm[0][2]*a + bcm.tm[1][2]*b + com[i][2]);
//	}
//}
//    
}
