/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OCTREE.CUBE;

/**
 *
 * @author arwillis
 */
public class FACE extends OBJECT_STRUCTURE {
    /*
     *  face.h - header for routines using polygonal faces.
     */

    public static double VERTEX(FACE f, int n, int i) {
        return ((f).va[3 * (n) + i]);
    }
    /* a polygonal face */
    public FVECT norm = new FVECT();		/* the plane's unit normal */

    public double offset;		/* plane equation:  DOT(norm, v) == offset */

    public double area;		/* area of face */

    public double[] va;		/* vertex array (o->oargs.farg) */

    public short nv;		/* # of vertices */

    public short ax;		/* axis closest to normal */
    /*
     *  face.c - routines dealing with polygonal faces.
     */

    /*
     *	A face is given as a list of 3D vertices.  The normal
     *  direction and therefore the surface orientation is determined
     *  by the ordering of the vertices.  Looking in the direction opposite
     *  the normal (at the front of the face), the vertices will be
     *  listed in counter-clockwise order.
     *	There is no checking done to insure that the edges do not cross
     *  one another.  This was considered too expensive and should be unnecessary.
     *  The last vertex is automatically connected to the first.
     */
//#ifdef  SMLFLT
//#define  VERTEPS	1e-3		/* allowed vertex error */
//#else
    public static double VERTEPS = 1e-5;		/* allowed vertex error */
//#endif


    public static FACE getface( /* get arguments for a face */
            OBJREC o) {
        double d1;
        boolean smalloff, badvert;
        FVECT v1 = new FVECT(), v2 = new FVECT(), v3 = new FVECT();
        FACE f;
        int i;

        if (o.os != null && o.os instanceof FACE) {
            //if ((f = (FACE *)o->os) != NULL)
            return ((FACE) o.os);			/* already done */
        }
        f = new FACE();
        //f = (FACE *)malloc(sizeof(FACE));
//	if (f == NULL)
//		error(SYSTEM, "out of memory in makeface");

        if (o.oargs.nfargs < 9 || (o.oargs.nfargs % 3 != 0)) {
            //objerror(o, USER, "bad # arguments");
        }

        o.os = f;			/* save face */

        f.va = o.oargs.farg;
        f.nv = (short) (o.oargs.nfargs / 3);
        /* check for last==first */
        if (FVECT.dist2(new FVECT(f.va), new FVECT(VERTEX(f, f.nv - 1, 0), VERTEX(f, f.nv - 1, 1), VERTEX(f, f.nv - 1, 2))) <= FVECT.FTINY * FVECT.FTINY) {
            f.nv--;
        }				/* compute area and normal */
        f.norm.data[0] = f.norm.data[1] = f.norm.data[2] = 0.0;
        v1.data[0] = VERTEX(f, 1, 0) - VERTEX(f, 0, 0);
        v1.data[1] = VERTEX(f, 1, 1) - VERTEX(f, 0, 1);
        v1.data[2] = VERTEX(f, 1, 2) - VERTEX(f, 0, 2);
        for (i = 2; i < f.nv; i++) {
            v2.data[0] = VERTEX(f, i, 0) - VERTEX(f, 0, 0);
            v2.data[1] = VERTEX(f, i, 1) - VERTEX(f, 0, 1);
            v2.data[2] = VERTEX(f, i, 2) - VERTEX(f, 0, 2);
            FVECT.fcross(v3, v1, v2);
            f.norm.data[0] += v3.data[0];
            f.norm.data[1] += v3.data[1];
            f.norm.data[2] += v3.data[2];
            FVECT.VCOPY(v1, v2);
        }
        f.area = FVECT.normalize(f.norm);
        if (f.area == 0.0) {
//		objerror(o, WARNING, "zero area");	/* used to be fatal */
            f.offset = 0.0;
            f.ax = 0;
            return (f);
        }
        f.area *= 0.5;
        /* compute offset */
        badvert = false;
        f.offset = FVECT.DOT(f.norm, new FVECT(VERTEX(f, 0, 0), VERTEX(f, 0, 1), VERTEX(f, 0, 2)));
        smalloff = (Math.abs(f.offset) <= VERTEPS);
        for (i = 1; i < f.nv; i++) {
            d1 = FVECT.DOT(f.norm, new FVECT(VERTEX(f, i, 0), VERTEX(f, i, 1), VERTEX(f, i, 2)));
            if (smalloff) {
                badvert |= Math.abs(d1 - f.offset / i) > VERTEPS;
            } else {
                badvert |= Math.abs(1.0 - d1 * i / f.offset) > VERTEPS;
            }
            f.offset += d1;
        }
        f.offset /= (double) f.nv;
        if (f.nv > 3 && badvert) {
            //	objerror(o, WARNING, "non-planar vertex");
        }		/* find axis */
        f.ax = (short) (Math.abs(f.norm.data[0]) > Math.abs(f.norm.data[1]) ? 0 : 1);
        if (Math.abs(f.norm.data[2]) > Math.abs(f.norm.data[f.ax])) {
            f.ax = 2;
        }

        return (f);
    }

    void freeface( /* free memory associated with face */
            OBJREC o) {
//	if (o->os == NULL)
//		return;
//	free(o->os);
//	o->os = NULL;
    }

    public static int inface( /* determine if point is in face */
            FVECT p,
            FACE f) {
        int ncross, n;
        double x, y;
        int tst;
        int xi, yi;
        double[] p0, p1;

        if ((xi = f.ax + 1) >= 3) {
            xi -= 3;
        }
        if ((yi = xi + 1) >= 3) {
            yi -= 3;
        }
        x = p.data[xi];
        y = p.data[yi];
        n = f.nv;
        p0 = new double[]{VERTEX(f, n - 1, 0), VERTEX(f, n - 1, 1), VERTEX(f, n - 1, 2)};
        //p0 = f.va + 3*(n-1);		/* connect last to first */
        p1 = new double[]{VERTEX(f, 0, 0), VERTEX(f, 0, 1), VERTEX(f, 0, 2)};
        //p1 = f->va;
        ncross = 0;
        int pidx = 0;
        /* positive x axis cross test */
        while (n-- != 0) {
            if ((p0[yi] > y) ^ (p1[yi] > y)) {
                tst = (p0[xi] > x ? 1 : 0) + (p1[xi] > x ? 1 : 0);
                if (tst == 2) {
                    ncross++;
                } else if (tst != 0) {
                    ncross += ((p1[yi] > p0[yi])
                            ^ ((p0[yi] - y) * (p1[xi] - x)
                            > (p0[xi] - x) * (p1[yi] - y))) ? 1 : 0;
                }
            }
            p0 = p1;
            pidx++;
            if (n > 0) {
                p1 = new double[]{VERTEX(f, pidx, 0), VERTEX(f, pidx, 1), VERTEX(f, pidx, 2)};
            }
            //p1 += 3;
        }
        return (ncross & 01);
    }
    /*
     *  o_face.c - routines for creating octrees for polygonal faces.
     *
     *     8/27/85
     */

    /*
     *	The algorithm for determining a face's intersection
     *  with a cube is relatively straightforward:
     *
     *	1) Check to see if any vertices are inside the cube
     *	   (intersection).
     *
     *	2) Check to see if all vertices are to one side of
     *	   cube (no intersection).
     *
     *	3) Check to see if any portion of any edge is inside
     *	   cube (intersection).
     *
     *	4) Check to see if the cube cuts the plane of the
     *	   face and one of its edges passes through
     *	   the face (intersection).
     *
     *	5) If test 4 fails, we have no intersection.
     */
    @Override
    public int octree_function(Object... objs) {
        return o_face((OBJREC) objs[0], (CUBE) objs[1]);
    }

    public static int o_face( /* determine if face intersects cube */
            OBJREC o,
            CUBE cu) {
        FVECT cumin = new FVECT(), cumax = new FVECT();
        FVECT v1 = new FVECT(), v2 = new FVECT();
        double d1, d2;
        int vloc;
        FACE f;
        int i, j;
        /* get face arguments */
        f = getface(o);
        if (f.area == 0.0) /* empty face */ {
            return (OCTREE.O_MISS);
        }
        /* compute cube boundaries */
        for (j = 0; j < 3; j++) {
            cumax.data[j] = (cumin.data[j] = cu.cuorg.data[j] - FVECT.FTINY)
                    + cu.cusize + 2.0 * FVECT.FTINY;
        }

        vloc = PLOCATE.ABOVE | PLOCATE.BELOW;		/* check vertices */
        for (i = 0; i < f.nv; i++) {
            if ((j = PLOCATE.plocate(new FVECT(VERTEX(f, i, 0), VERTEX(f, i, 1), VERTEX(f, i, 2)), cumin, cumax)) != 0) {
                vloc &= j;
            } else {
//                System.out.println("vertex "+i+" "+new FVECT(VERTEX(f, i, 0), VERTEX(f, i, 1), VERTEX(f, i, 2))+
//                        " found inside cube with center "+cu.cuorg+" and size "+cu.cusize);
                return (OCTREE.O_HIT);	/* vertex inside */
            }
        }
        if (vloc != 0) /* all to one side */ {
            return (OCTREE.O_MISS);
        }

        for (i = 0; i < f.nv; i++) {	/* check edges */
            if ((j = i + 1) >= f.nv) {
                j = 0;			/* wrap around */
            }
            FVECT.VCOPY(v1, new FVECT(VERTEX(f, i, 0), VERTEX(f, i, 1), VERTEX(f, i, 2)));		/* clip modifies */
            FVECT.VCOPY(v2, new FVECT(VERTEX(f, j, 0), VERTEX(f, j, 1), VERTEX(f, j, 2)));		/* the vertices! */
            if (CLIP.clip(v1, v2, cumin, cumax) != 0) {
                return (OCTREE.O_HIT);		/* edge inside */
            }
        }
        /* see if cube cuts plane */
        for (j = 0; j < 3; j++) {
            if (f.norm.data[j] > 0.0) {
                v1.data[j] = cumin.data[j];
                v2.data[j] = cumax.data[j];
            } else {
                v1.data[j] = cumax.data[j];
                v2.data[j] = cumin.data[j];
            }
        }
        if ((d1 = FVECT.DOT(v1, f.norm) - f.offset) > FVECT.FTINY) {
            return (OCTREE.O_MISS);
        }
        if ((d2 = FVECT.DOT(v2, f.norm) - f.offset) < -FVECT.FTINY) {
            return (OCTREE.O_MISS);
        }
        /* intersect face */
        for (j = 0; j < 3; j++) {
            v1.data[j] = (v1.data[j] * d2 - v2.data[j] * d1) / (d2 - d1);
        }
        if (inface(v1, f) != 0) {
            return (OCTREE.O_HIT);
        }
        return (OCTREE.O_MISS);		/* no intersection */
    }
}
