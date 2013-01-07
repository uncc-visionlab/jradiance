/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import jradiance.common.MESH.MESHPATCH;
import jradiance.common.MESH.MESHPATCH.PJoin1;
import jradiance.common.MESH.MESHPATCH.PJoin2;
import jradiance.common.MESH.MESHPATCH.PTri;

/**
 *
 * @author arwillis
 */
public class READMESH {
    /*
     *  Routines for reading a compiled mesh from a file
     */

    static String meshfn;	/* input file name */

    static InputStream meshfp;	/* mesh file pointer */

    static int objsize;	/* sizeof(OBJECT) from writer */


    static void mesherror( /* mesh read error */
            int etyp,
            char[] msg) {
        char[] msgbuf = new char[128];

//	sprintf(msgbuf, "(%s): %s", meshfn, msg);
//	error(etyp, msgbuf);
    }

    static long mgetint( /* get a siz-byte integer */
            int siz) throws IOException {
        long r;

        r = PORTIO.getint(siz, meshfp);
//	if (feof(meshfp))
//		mesherror(USER, "truncated mesh file");
        return (r);
    }

    static double mgetflt() throws IOException /* get a floating point number */ {
        double r;

        r = PORTIO.getflt(meshfp);
////	if (feof(meshfp))
////		mesherror(USER, "truncated mesh file");
        return (r);
    }

    static int getfullnode() throws IOException /* get a set, return fullnode */ {
        int[] set = new int[OBJECT.MAXSET + 1];
        int i;

        if ((set[0] = (int) mgetint(objsize)) > OBJECT.MAXSET) {
//		mesherror(USER, "bad set in getfullnode");
        }
        for (i = 1; i <= set[0]; i++) {
            set[i] = (int) mgetint(objsize);
        }
        return (OBJSET.fullnode(set));
    }

    static int gettree() throws IOException /* get a pre-ordered octree */ {
        int ot;
        int i;

        switch (meshfp.read()) {
            case OCTREE.OT_EMPTY:
                return (OCTREE.EMPTY);
            case OCTREE.OT_FULL:
                return (getfullnode());
            case OCTREE.OT_TREE:
                if ((ot = OCTREE.octalloc()) == OCTREE.EMPTY) {
//				mesherror(SYSTEM, "out of tree space in readmesh");
                }
                for (i = 0; i < 8; i++) {
                    OCTREE.octblock[OCTREE.octbi(ot)][OCTREE.octti(ot) + i] = gettree();
                }
//				OCTREE.octkid(ot, i) = gettree();
                return (ot);
//		case EOF:
//			mesherror(USER, "truncated mesh octree");
            default:
//			mesherror(USER, "damaged mesh octree");
        }
        return 0;	/* pro forma return */
    }

    static void skiptree() throws IOException /* skip octree on input */ {
        int i;

        switch (meshfp.read()) {
            case OCTREE.OT_EMPTY:
                return;
            case OCTREE.OT_FULL:
                for (i = (int) mgetint(objsize) * objsize; i-- > 0;) {
                    if (meshfp.read() == -1) {
//				mesherror(USER, "truncated mesh octree");
                    }
                }
                return;
            case OCTREE.OT_TREE:
                for (i = 0; i < 8; i++) {
                    skiptree();
                }
                return;
//	case EOF:
//		mesherror(USER, "truncated mesh octree");
            default:
//		mesherror(USER, "damaged mesh octree");
        }
    }

    static void getpatch( /* load a mesh patch */
            MESHPATCH pp) throws IOException {
        int flags;
        int i, j;
        /* vertex flags */
        flags = (int) mgetint(1);
        if ((flags & MESH.MT_V) == 0 || (flags & ~(MESH.MT_V | MESH.MT_N | MESH.MT_UV)) != 0) {
//		mesherror(USER, "bad patch flags");
        }
        /* allocate vertices */
        pp.nverts = (short) mgetint(2);
        if (pp.nverts <= 0 || pp.nverts > 256) {
//		mesherror(USER, "bad number of patch vertices");
        }
//        pp.xyz = new int[pp.nverts][3];
        pp.xyz = new long[pp.nverts][3];
//	if (pp.xyz == null)
//		goto nomem;
        if ((flags & MESH.MT_N) != 0) {
            pp.norm = new int[pp.nverts];
//		if (pp.norm == null)
//			goto nomem;
        } else {
            pp.norm = null;
        }
        if ((flags & MESH.MT_UV) != 0) {
//            pp.uv = new int[pp.nverts][2];
            pp.uv = new long[pp.nverts][2];
//		if (pp.uv == null)
//			goto nomem;
        } else {
            pp.uv = null;
        }
        /* vertex xyz locations */
        for (i = 0; i < pp.nverts; i++) {
            for (j = 0; j < 3; j++) {
                pp.xyz[i][j] = (long) (mgetint(4)&0xffffffffL);
            }
        }
        /* vertex normals */
        if ((flags & MESH.MT_N) != 0) {
            for (i = 0; i < pp.nverts; i++) {
                pp.norm[i] = (int) mgetint(4);
            }
        }
        /* uv coordinates */
        if ((flags & MESH.MT_UV) != 0) {
            for (i = 0; i < pp.nverts; i++) {
                for (j = 0; j < 2; j++) {
                    pp.uv[i][j] = (long) (mgetint(4)&0xffffffffL);
                }
            }
        }
        /* local triangles */
        pp.ntris = (short) mgetint(2);
        if (pp.ntris < 0 || pp.ntris > 512) {
//		mesherror(USER, "bad number of local triangles");
        }
        if (pp.ntris != 0) {
            pp.tri = new PTri[pp.ntris];
            for (int ii = 0; ii < pp.tri.length; ii++) {
                pp.tri[ii] = pp.new PTri();
            }
//		if (pp.tri == null)
//			goto nomem;
            for (i = 0; i < pp.ntris; i++) {
                pp.tri[i].v1 = (byte) mgetint(1);
                pp.tri[i].v2 = (byte) mgetint(1);
                pp.tri[i].v3 = (byte) mgetint(1);
            }
        } else {
            pp.tri = null;
        }
        /* local triangle material(s) */
        if (mgetint(2) > 1) {
            pp.trimat = new short[pp.ntris];
//		if (pp.trimat == null)
//			goto nomem;
            for (i = 0; i < pp.ntris; i++) {
                pp.trimat[i] = (short) mgetint(2);
            }
        } else {
            pp.solemat = (short) mgetint(2);
            pp.trimat = null;
        }
        /* joiner triangles */
        pp.nj1tris = (short) mgetint(2);
        if (pp.nj1tris < 0 || pp.nj1tris > 512) {
//		mesherror(USER, "bad number of joiner triangles");
        }
        if (pp.nj1tris != 0) {
            pp.j1tri = new PJoin1[pp.nj1tris];
            for (int ii = 0; ii < pp.j1tri.length; ii++) {
                pp.j1tri[ii] = pp.new PJoin1();
            }
//		if (pp.j1tri == null)
//			goto nomem;
            for (i = 0; i < pp.nj1tris; i++) {
                pp.j1tri[i].v1j = (int) mgetint(4);
                pp.j1tri[i].v2 = (byte) mgetint(1);
                pp.j1tri[i].v3 = (byte) mgetint(1);
                pp.j1tri[i].mat = (short) mgetint(2);
            }
        } else {
            pp.j1tri = null;
        }
        /* double joiner triangles */
        pp.nj2tris = (short) mgetint(2);
        if (pp.nj2tris < 0 || pp.nj2tris > 256) {
//		mesherror(USER, "bad number of double joiner triangles");
        }
        if (pp.nj2tris != 0) {
            pp.j2tri = new PJoin2[pp.nj2tris];
            for (int ii = 0; ii < pp.j2tri.length; ii++) {
                pp.j2tri[ii] = pp.new PJoin2();
            }
//		if (pp.j2tri == null)
//			goto nomem;
            for (i = 0; i < pp.nj2tris; i++) {
                pp.j2tri[i].v1j = (int) mgetint(4);
                pp.j2tri[i].v2j = (int) mgetint(4);
                pp.j2tri[i].v3 = (byte) mgetint(1);
                pp.j2tri[i].mat = (short) mgetint(2);
            }
        } else {
            pp.j2tri = null;
        }
        return;
//nomem:
//	error(SYSTEM, "out of mesh memory in getpatch");
    }

    static void readmesh( /* read in mesh structures */
            MESH mp,
            String path,
            int flags) throws FileNotFoundException, IOException {
        String err;
        char[] sbuf = new char[64];
        int i;
        /* check what's loaded */
        flags &= (OCTREE.IO_INFO | OCTREE.IO_BOUNDS | OCTREE.IO_TREE | OCTREE.IO_SCENE) & ~mp.ldflags;
        /* open input file */
        if (path == null) {
//		meshfn = "standard input";
//		meshfp = stdin;
            meshfp = System.in;
        } else if ((meshfp = new FileInputStream(meshfn = path)) == null) {
//		sprintf(errmsg, "cannot open mesh file \"%s\"", path);
//		error(SYSTEM, errmsg);
        }
//	SET_FILE_BINARY(meshfp);
					/* read header */
        HEADER.checkheader(meshfp, MESH.MESHFMT.toCharArray(), (flags & OCTREE.IO_INFO) != 0 ? System.out : null);
        /* read format number */
        objsize = (int) (PORTIO.getint(2, meshfp) - MESH.MESHMAGIC);
        int sizeofLONG = Long.SIZE / 8;
        if (objsize <= 0 || objsize > OCTREE.MAXOBJSIZ || objsize > sizeofLONG) {
//		mesherror(USER, "incompatible mesh format");
        }
        /* read boundaries */
        if ((flags & OCTREE.IO_BOUNDS) != 0) {
            for (i = 0; i < 3; i++) {
                mp.mcube.cuorg.data[i] = Float.parseFloat(PORTIO.getstr(sbuf, meshfp));
            }
            mp.mcube.cusize = Float.parseFloat(PORTIO.getstr(sbuf, meshfp));
            for (i = 0; i < 2; i++) {
                mp.uvlim[0][i] = mgetflt();
                mp.uvlim[1][i] = mgetflt();
            }
        } else {
            for (i = 0; i < 4; i++) {
                PORTIO.getstr(sbuf, meshfp);
            }
            for (i = 0; i < 4; i++) {
                mgetflt();
            }
        }
        /* read the octree */
        if ((flags & OCTREE.IO_TREE) != 0) {
            mp.mcube.cutree = gettree();
        } else if ((flags & OCTREE.IO_SCENE) != 0) {
            skiptree();
        }
        /* read materials and patches */
        if ((flags & OCTREE.IO_SCENE) != 0) {
            mp.mat0 = OBJECT.nobjects;
            SCENEIO.readscene(meshfp, objsize);
            mp.nmats = OBJECT.nobjects - mp.mat0;
            mp.npatches = (int) mgetint(4);
            mp.patch = new MESHPATCH[mp.npatches];
            for (int ii = 0; ii < mp.patch.length; ii++) {
                mp.patch[ii] = new MESHPATCH();
            }
            if (mp.patch == null) {
//			mesherror(SYSTEM, "out of patch memory");
            }
            for (i = 0; i < mp.npatches; i++) {
                getpatch(mp.patch[i]);
            }
        }
        /* clean up */
        meshfp.close();
        mp.ldflags |= flags;
        /* verify data */
	if ((err = MESH.checkmesh(mp)) != null) {
//		mesherror(USER, err);
        }
    }
}
