/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.ot;

import jradiance.common.FACE;
import jradiance.common.FVECT;
import jradiance.common.MESH;
import jradiance.common.MESH.MESHVERT;
import jradiance.common.OBJECT;
import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OBJSET;
import jradiance.common.OCTREE;
import jradiance.common.OTYPES;
import jradiance.common.SAVESTR;
import jradiance.common.SAVQSTR;
import jradiance.common.TMESH;

/**
 *
 * @author arwillis
 */
public class CVMESH {
    /*
     *  Radiance triangle mesh conversion routines
     */

    /*
     * We need to divide faces into triangles and record auxiliary information
     * if given (surface normal and uv coordinates).  We do this by extending
     * the face structure linked to the OBJREC os member and putting our
     * auxiliary after it -- a bit sly, but it works.
     */

    /* Auxiliary data for triangle */
    public static class TRIDATA {

        int fl;		/* flags of what we're storing */

        int obj;		/* mesh triangle ID */

        FVECT[] vn = {new FVECT(), new FVECT(), new FVECT()};		/* normals */

        double[][] vc = new double[3][2];	/* uv coords. */

    }

    public static class FACE_W_TRIDATA extends FACE {
        public FACE_W_TRIDATA() {
            super();
        }
        TRIDATA tridata;
    public static FACE_W_TRIDATA getface( /* get arguments for a face */
            OBJREC o) {
        double d1;
        boolean smalloff, badvert;
        FVECT v1 = new FVECT(), v2 = new FVECT(), v3 = new FVECT();
        FACE_W_TRIDATA f;
        int i;

        if (o.os != null && o.os instanceof FACE_W_TRIDATA) {
            //if ((f = (FACE *)o->os) != NULL)
            return ((FACE_W_TRIDATA) o.os);			/* already done */
        }
        f = new FACE_W_TRIDATA();
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
        
    }
//#define tdsize(fl)	((fl)&MT_UV ? sizeof(TRIDATA) : \
//				(fl)&MT_N ? sizeof(TRIDATA)-6*sizeof(RREAL) : \
//				sizeof(int)+sizeof(OBJECT))
    public static final double OMARGIN = (10 * FVECT.FTINY);	/* margin around global cube */

    public static MESH ourmesh = null;		/* our global mesh data structure */

    static FVECT[] meshbounds = {new FVECT(), new FVECT()};			/* mesh bounding box */

//static void add2bounds(FVECT vp, RREAL vc[2]);
//static OBJECT cvmeshtri(OBJECT obj);
//static OCTREE cvmeshoct(OCTREE ot);

    public static MESH cvinit( /* initialize empty mesh */
            String nm) {
        /* free old mesh, first */
        if (ourmesh != null) {
//		MESH.freemesh(ourmesh);
            ourmesh = null;
//		freeobjects(0, nobjects);
//		donesets();
        }
        if (nm == null) {
            return (null);
        }
        ourmesh = new MESH();
        if (ourmesh == null) {
//		goto nomem;
        }
        ourmesh.name = SAVESTR.savestr(nm);
        ourmesh.nref = 1;
        ourmesh.ldflags = 0;
        ourmesh.mcube.cutree = OCTREE.EMPTY;
        ourmesh.uvlim[0][0] = ourmesh.uvlim[0][1] = FVECT.FHUGE;
        ourmesh.uvlim[1][0] = ourmesh.uvlim[1][1] = -FVECT.FHUGE;
        meshbounds[0].data[0] = meshbounds[0].data[1] = meshbounds[0].data[2] = FVECT.FHUGE;
        meshbounds[1].data[0] = meshbounds[1].data[1] = meshbounds[1].data[2] = -FVECT.FHUGE;
        return (ourmesh);
//nomem:
//	error(SYSTEM, "out of memory in cvinit");
//	return(NULL);
    }

    int cvpoly( /* convert a polygon to extended triangles */
            int mo,
            int n,
            FVECT[] vp,
            FVECT[] vn,
            double[][] vc) {
        int tcnt = 0;
        int flagsloc;
        double[][] tn = new double[3][], tc = new double[3][];
        int[] ord;
        int i, j;

        if (n < 3) /* degenerate face */ {
            return (0);
        }
        flagsloc = MESH.MT_V;
        if (vn != null) {
            tn[0] = vn[0].data;
            tn[1] = vn[1].data;
            tn[2] = vn[2].data;
            flagsloc |= MESH.MT_N;
        } else {
            tn[0] = tn[1] = tn[2] = null;
        }
        if (vc != null) {
            tc[0] = vc[0];
            tc[1] = vc[1];
            tc[2] = vc[2];
            flagsloc |= MESH.MT_UV;
        } else {
            tc[0] = tc[1] = tc[2] = null;
        }
        if (n == 3) /* output single triangle */ //		return(cvtri(mo, vp[0], vp[1], vp[2],
        //				tn[0], tn[1], tn[2],
        //				tc[0], tc[1], tc[2]));
        {
            return (cvtri(mo, vp[0], vp[1], vp[2],
                    vn[0], vn[1], vn[2],
                    tc[0], tc[1], tc[2]));
        }

        /* decimate polygon (assumes convex) */
        ord = new int[n];
        if (ord == null) {
//		error(SYSTEM, "out of memory in cvpoly");
        }
        for (i = n; i-- != 0;) {
            ord[i] = i;
        }
        while (n >= 3) {
            if ((flagsloc & MESH.MT_N) != 0) {
                for (i = 3; i-- != 0;) {
                    tn[i] = vn[ord[i]].data;
                }
            }
            if ((flagsloc & MESH.MT_UV) != 0) {
                for (i = 3; i-- != 0;) {
                    tc[i] = vc[ord[i]];
                }
            }
//		tcnt += cvtri(mo, vp[ord[0]], vp[ord[1]], vp[ord[2]],
//				tn[0], tn[1], tn[2],
//				tc[0], tc[1], tc[2]);
            tcnt += cvtri(mo, vp[ord[0]], vp[ord[1]], vp[ord[2]],
                    vn[ord[0]], vn[ord[1]], vn[ord[2]],
                    tc[0], tc[1], tc[2]);
            /* remove vertex and rotate */
            n--;
            j = ord[0];
            for (i = 0; i < n - 1; i++) {
                ord[i] = ord[i + 2];
            }
            ord[i] = j;
        }
//	free((void *)ord);
        return (tcnt);
    }

    static void add2bounds( /* add point and uv coordinate to bounds */
            FVECT vp,
            double[] vc) {
        int j;

        for (j = 3; j-- != 0;) {
            if (vp.data[j] < meshbounds[0].data[j]) {
                meshbounds[0].data[j] = vp.data[j];
            }
            if (vp.data[j] > meshbounds[1].data[j]) {
                meshbounds[1].data[j] = vp.data[j];
            }
        }
        if (vc == null) {
            return;
        }
        for (j = 2; j-- != 0;) {
            if (vc[j] < ourmesh.uvlim[0][j]) {
                ourmesh.uvlim[0][j] = vc[j];
            }
            if (vc[j] > ourmesh.uvlim[1][j]) {
                ourmesh.uvlim[1][j] = vc[j];
            }
        }
    }
    static int fobj = OBJECT.OVOID;

    public static int /* create an extended triangle */ cvtri(
            int mo,
            FVECT vp1,
            FVECT vp2,
            FVECT vp3,
            FVECT vn1,
            FVECT vn2,
            FVECT vn3,
            double[] vc1,
            double[] vc2,
            double[] vc3) {
        char[] buf = new char[32];
        int flags;
        TRIDATA ts;
//        FACE f;
        FACE_W_TRIDATA fwt;
        OBJREC fop;
        int j;

        flags = MESH.MT_V;		/* check what we have */
        if (vn1 != null && vn2 != null && vn3 != null) {
            double[] rp;
            switch (TMESH.flat_tri(vp1, vp2, vp3, vn1, vn2, vn3)) {
                case TMESH.ISBENT:
                    flags |= MESH.MT_N;
                /* fall through */
                case TMESH.ISFLAT:
                    break;
                case TMESH.RVBENT:
                    flags |= MESH.MT_N;
                    rp = vn1.data;
                    vn1.data = vn3.data;
                    vn3.data = rp;
                /* fall through */
                case TMESH.RVFLAT:
                    rp = vp1.data;
                    vp1.data = vp3.data;
                    vp3.data = rp;
                    rp = vc1;
                    vc1 = vc3;
                    vc3 = rp;
                    break;
                case TMESH.DEGEN:
//			error(WARNING, "degenerate triangle");
                    return (0);
                default:
//			error(INTERNAL, "bad return from flat_tri()");
            }
        }
        if (vc1 != null && vc2 != null && vc3 != null) {
            flags |= MESH.MT_UV;
        }
        if (fobj == OBJECT.OVOID) {	/* create new triangle object */
            fobj = OBJECT.newobject();
            if (fobj == OBJECT.OVOID) {
//			goto nomem;
            }
            fop = OBJECT.objptr(fobj);
            fop.omod = mo;
            fop.otype = OTYPES.OBJ_FACE;
            String sbuf = String.format("t%d", (long) fobj);
            fop.oname = SAVQSTR.savqstr(sbuf);
            fop.oargs.nfargs = 9;
            fop.oargs.farg = new double[9];//(RREAL *)malloc(9*sizeof(RREAL));
            if (fop.oargs.farg == null) {
//			goto nomem;
            }
        } else {		/* else reuse failed one */
            fop = OBJECT.objptr(fobj);
            if (fop.otype != OTYPES.OBJ_FACE || fop.oargs.nfargs != 9) {
//			error(CONSISTENCY, "code error 1 in cvtri");
            }
        }
        for (j = 3; j-- != 0;) {
            fop.oargs.farg[j] = vp1.data[j];
            fop.oargs.farg[3 + j] = vp2.data[j];
            fop.oargs.farg[6 + j] = vp3.data[j];
        }
        /* create face record */
//        f = FACE.getface(fop);
        fwt = FACE_W_TRIDATA.getface(fop);
        if (fwt.area == 0.) {
//		free_os(fop);
            return (0);
        }
        if (fop.os != fwt) {
//		error(CONSISTENCY, "code error 2 in cvtri");
        }
        /* follow with auxliary data */
        //f = (FACE *)realloc((void *)f, sizeof(FACE)+tdsize(flags));
        if (fwt == null) {
//		goto nomem;
        }
//        fop.os = fwt;
        //ts = (TRIDATA *)(f+1); // TRIDATA follows the FACE in memory???
        ts = new TRIDATA();
        fwt.tridata = ts;
//        fop.os = fwt;
        ts.fl = flags;
        ts.obj = OBJECT.OVOID;
        if ((flags & MESH.MT_N) != 0) {
            for (j = 3; j-- != 0;) {
                ts.vn[0].data[j] = vn1.data[j];
                ts.vn[1].data[j] = vn2.data[j];
                ts.vn[2].data[j] = vn3.data[j];
            }
        }
        if ((flags & MESH.MT_UV) != 0) {
            for (j = 2; j-- != 0;) {
                ts.vc[0][j] = vc1[j];
                ts.vc[1][j] = vc2[j];
                ts.vc[2][j] = vc3[j];
            }
        } else {
            vc1 = vc2 = vc3 = null;
        }
        /* update bounds */
        add2bounds(vp1, vc1);
        add2bounds(vp2, vc2);
        add2bounds(vp3, vc3);
        fobj = OBJECT.OVOID;		/* we used this one */
        return (1);
//nomem:
//	error(SYSTEM, "out of memory in cvtri");
//	return(0);
    }

    static int cvmeshtri( /* add an extended triangle to our mesh */
            int obj) {
        OBJREC o = OBJECT.objptr(obj);
        TRIDATA ts = null;
        MESHVERT[] vert = {new MESHVERT(), new MESHVERT(), new MESHVERT()};
        int i, j;

        if (o.otype != OTYPES.OBJ_FACE) {
//		error(CONSISTENCY, "non-face in mesh");
        }
        if (o.oargs.nfargs != 9) {
//		error(CONSISTENCY, "non-triangle in mesh");
        }
        if (o.os == null) {
//		error(CONSISTENCY, "missing face record in cvmeshtri");
        }
//	ts = (TRIDATA *)((FACE *)o.os + 1); // POINTER MAGIC!!! YIKES
        if (o.os instanceof FACE_W_TRIDATA) {
            ts = ((FACE_W_TRIDATA) o.os).tridata;
        }
        if (ts.obj != OBJECT.OVOID) /* already added? */ {
            return (ts.obj);
        }
        vert[0].fl = vert[1].fl = vert[2].fl = ts.fl;
        for (i = 3; i-- != 0;) {
            for (j = 3; j-- != 0;) {
                vert[i].v.data[j] = o.oargs.farg[3 * i + j];
            }
        }
        if ((ts.fl & MESH.MT_N) != 0) {
            for (i = 3; i-- != 0;) {
                for (j = 3; j-- != 0;) {
                    vert[i].n.data[j] = ts.vn[i].data[j];
                }
            }
        }
        if ((ts.fl & MESH.MT_UV) != 0) {
            for (i = 3; i-- != 0;) {
                for (j = 2; j-- != 0;) {
                    vert[i].uv[j] = ts.vc[i][j];
                }
            }
        }
        ts.obj = MESH.addmeshtri(ourmesh, vert, o.omod);
        if (ts.obj == OBJECT.OVOID) {
//		error(INTERNAL, "addmeshtri failed");
        }
        return (ts.obj);
    }

    public static void cvmeshbounds() /* set mesh boundaries */ {
        int i;

        if (ourmesh == null) {
            return;
        }
        /* fix coordinate bounds */
        for (i = 0; i < 3; i++) {
            if (meshbounds[0].data[i] > meshbounds[1].data[i]) {
//			error(USER, "no polygons in mesh");
            }
            meshbounds[0].data[i] -= OMARGIN;
            meshbounds[1].data[i] += OMARGIN;
            if (meshbounds[1].data[i] - meshbounds[0].data[i] > ourmesh.mcube.cusize) {
                ourmesh.mcube.cusize = meshbounds[1].data[i]
                        - meshbounds[0].data[i];
            }
        }
        for (i = 0; i < 3; i++) {
            ourmesh.mcube.cuorg.data[i] = (meshbounds[1].data[i] + meshbounds[0].data[i]
                    - ourmesh.mcube.cusize) * .5;
        }
        if (ourmesh.uvlim[0][0] > ourmesh.uvlim[1][0]) {
            ourmesh.uvlim[0][0] = ourmesh.uvlim[0][1] = 0.;
            ourmesh.uvlim[1][0] = ourmesh.uvlim[1][1] = 0.;
        } else {
            int sizeofUINT16 = Short.SIZE / 8;
            for (i = 0; i < 2; i++) {
                double marg;		/* expand past endpoints */
                marg = (2. / (1L << (8 * sizeofUINT16)))
                        * (ourmesh.uvlim[1][i]
                        - ourmesh.uvlim[0][i]) + FVECT.FTINY;
                ourmesh.uvlim[0][i] -= marg;
                ourmesh.uvlim[1][i] += marg;
            }
        }
        ourmesh.ldflags |= OCTREE.IO_BOUNDS;
    }

    static int cvmeshoct( /* convert triangles in subtree */
            int ot) {
        int i;

        if (OCTREE.isempty(ot) != 0) {
            return (OCTREE.EMPTY);
        }

        if (OCTREE.isfull(ot) != 0) {
            int[] oset1 = new int[OBJECT.MAXSET + 1];
            int[] oset2 = new int[OBJECT.MAXSET + 1];
            OBJSET.objset(oset1, ot);
            oset2[0] = 0;
            for (i = oset1[0]; i > 0; i--) {
                OBJSET.insertelem(oset2, cvmeshtri(oset1[i]));
            }
            return (OBJSET.fullnode(oset2));
        }

        for (i = 8; i-- != 0;) {
            OCTREE.octblock[OCTREE.octbi(ot)][OCTREE.octti(ot) + i] = cvmeshoct(OCTREE.octkid(ot, i));
        }
        return (ot);
    }

    public static MESH cvmesh() /* convert mesh and octree leaf nodes */ {
        if (ourmesh == null) {
            return (null);
        }
        /* convert triangles in octree nodes */
        ourmesh.mcube.cutree = cvmeshoct(ourmesh.mcube.cutree);
        ourmesh.ldflags |= OCTREE.IO_SCENE | OCTREE.IO_TREE;

        return (ourmesh);
    }
}
