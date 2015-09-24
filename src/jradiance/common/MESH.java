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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import jradiance.common.LOOKUP.LUENT;
import jradiance.common.LOOKUP.LUTAB;
import jradiance.common.MESH.MESHPATCH.PJoin1;
import jradiance.common.MESH.MESHPATCH.PJoin2;
import jradiance.common.MESH.MESHPATCH.PTri;
import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OCTREE.CUBE;
import jradiance.common.RTMATH.FULLXF;
import jradiance.rt.DEVCOMM;

/**
 *
 * @author arwillis
 */
public class MESH extends OBJECT_STRUCTURE {
    /*
     * Header for compact triangle mesh geometry
     *
     *  Include after standard.h, object.h and octree.h
     */
    /*
     * Vertex space is minimized without compromising accuracy by using a
     * 4-byte unsigned int to indicate position in the enclosing octree cube.
     * The same trick is used for any local (u,v) coordinates, whose limits
     * are recorded separately in the parent MESH structure.  The uvlimit's
     * in the MESH structure are set such that (0,0) is out of range, so
     * we use this to indicate an unspecified local coordinate.
     * A vertex normal, if specified, is stored in a single 4-byte
     * integer using the codec in dircode.c.  The encodedir() function
     * never generates 0, so we can use this for unspecified normals.
     *
     * Vertex ID's are encoded using the bottom 8 bits of a 4-byte integer
     * to index a vertex in a patch indicated by the 22 bits above (8-29).
     * For triangle ID's, the top 22 bits (10-31) indicate the parent patch,
     * and the 10th bit (0x200) indicates whether the triangle joins patches.
     * If not, then the bottom 9 bits index into the local PTri array.
     * If it's a joiner, then the 9th bit indicates whether the triangle joins
     * two patches, in which case the bottom 8 bits index the PJoin2 array.
     * Otherwise, the bottom 8 bits index the PJoin1 array.
     *
     * These shenanigans minimize vertex reference memory requirements
     * in compiled mesh structures, where the octree leaves contain sets 
     * of triangle ID's rather than the more usual objects.  It seems like
     * a lot of effort, but it can reduce mesh storage by a factor of 3
     * or more.  This is important, as the whole point is to model very
     * complicated geometry with this structure, and memory is the main
     * limitation.  (This representation is so efficient, that the octree
     * structure ends up dominating memory for most compiled meshes.)
     */
//
///* A triangle mesh patch */

    public static class MESHPATCH {
        // xyz and uv need to be long as they are unsigned ints in the C
        // version of RADIANCE
//        public int[][] xyz;//[3];	/* up to 256 patch vertices */

        public long[][] xyz;//[3];	/* up to 256 patch vertices */
        public int[] norm;		/* vertex normals */

//        public int[][] uv;//[2];	/* vertex local coordinates */
        public long[][] uv;//[2];	/* vertex local coordinates */

        public class PTri {

            public byte v1, v2, v3;	/* local vertices */

        }
        public PTri[] tri;		/* local triangles */

        public short solemat;	/* sole material */

        public short[] trimat;	/* or local material indices */


        public class PJoin1 {

            public int v1j;		/* non-local vertex */

            public short mat;		/* material index */

            public byte v2, v3;		/* local vertices */

        }
        public PJoin1[] j1tri;		/* joiner triangles */


        public class PJoin2 {

            public int v1j, v2j;	/* non-local vertices */

            public short mat;		/* material index */

            public byte v3;		/* local vertex */

        }
        public PJoin2[] j2tri;		/* double joiner triangles */

        public short nverts;		/* vertex count */

        public short ntris;		/* local triangle count */

        public short nj1tris;	/* joiner triangle count */

        public short nj2tris;	/* double joiner triangle count */

    }
///* A loaded mesh */
//public static class MESH1 {
    public String name;		/* mesh file name */

    public int nref;		/* reference count */

    public int ldflags;	/* what we've loaded */

    public CUBE mcube = new CUBE();		/* bounds and octree */

    public double[][] uvlim = new double[2][2];	/* local coordinate extrema */

    public int mat0;		/* base material index */

    public int nmats;		/* number of materials */

    public MESHPATCH[] patch;		/* allocated mesh patch array */

    public int npatches;	/* number of mesh patches */

    OBJREC[] pseudo;	/* mesh pseudo objects */

    LUTAB lut = new CVLUTAB();		/* vertex lookup table */

    MESH next;		/* next mesh in list */
//}

    /* A mesh instance */

    public static class MESHINST extends OBJECT_STRUCTURE {

        public FULLXF x = new FULLXF();		/* forward and backward transforms */

        public MESH msh;		/* mesh object reference */


        @Override
        public int octree_function(Object... obj) {
            throw new UnsupportedOperationException("Not supported yet.");
            // call o_mesh here?
        }
    }

    /* vertex flags */
    public static final int MT_V = 01;
    public static final int MT_N = 02;
    public static final int MT_UV = 04;
    public static final int MT_ALL = 07;

    /* A mesh vertex */
    public static class MESHVERT {

        public int fl;		/* setting flags */

        public FVECT v = new FVECT();		/* vertex location */

        public FVECT n = new FVECT();		/* vertex normal */

        public double[] uv = new double[2];		/* local coordinates */

    }

    /* mesh format identifier */
    public static final String MESHFMT = "Radiance_tmesh";
    /* magic number for mesh files */
    public static final int MESHMAGIC = (1 * OCTREE.MAXOBJSIZ + 311);	/* increment first value */


    @Override
    public int octree_function(Object... obj) {
        try {
            return o_mesh((OBJREC) obj[0], (CUBE) obj[1]);
        } catch (IOException ex) {
            Logger.getLogger(MESH.class.getName()).log(Level.SEVERE, null, ex);
        }
        return OCTREE.O_MISS;
    }

    int o_mesh( /* determine if mesh intersects */
            OBJREC o,
            CUBE cu) throws IOException {
        MESHINST mip;
        /* get mesh bounds */
        mip = MESH.getmeshinst(o, OCTREE.IO_BOUNDS);
        /* call o_cube to do the work */
        return (INSTANCE.o_cube(mip.msh.mcube, mip.x, cu));
    }

    /*
     * Mesh support routines
     */

    /* An encoded mesh vertex */
    public static class MCVERT implements Cloneable {

        int fl;
//        int[] xyz = new int[3];
        long[] xyz = new long[3];
        int norm;
//        int[] uv = new int[2];
        long[] uv = new long[2];
    }
    public static final int MPATCHBLKSIZ = 128;		/* patch allocation block size */

    public static final int IO_LEGAL = (OCTREE.IO_BOUNDS | OCTREE.IO_TREE | OCTREE.IO_SCENE);
    static MESH[] mlist = {null};		/* list of loaded meshes */


    public static class CVLUTAB extends LUTAB {

        static long cvhash(MCVERT p) /* hash an encoded vertex */ {
            MCVERT cvp = p;
            long hval;

            if ((cvp.fl & MT_V) == 0) {
                return (0);
            }
            hval = cvp.xyz[0] ^ cvp.xyz[1] << 11 ^ cvp.xyz[2] << 22;
            if ((cvp.fl & MT_N) != 0) {
                hval ^= cvp.norm;
            }
            if ((cvp.fl & MT_UV) != 0) {
                hval ^= cvp.uv[0] ^ cvp.uv[1] << 16;
            }
            return (Math.abs(hval));
        }

        static int cvcmp(MCVERT vv1, MCVERT vv2) /* compare encoded vertices */ {
            MCVERT v1 = (MCVERT) vv1, v2 = (MCVERT) vv2;
            if (v1.fl != v2.fl) {
                return (1);
            }
            if (v1.xyz[0] != v2.xyz[0]) {
                return (1);
            }
            if (v1.xyz[1] != v2.xyz[1]) {
                return (1);
            }
            if (v1.xyz[2] != v2.xyz[2]) {
                return (1);
            }
            if ((v1.fl & MT_N) != 0 && v1.norm != v2.norm) {
                return (1);
            }
            if ((v1.fl & MT_UV) != 0) {
                if (v1.uv[0] != v2.uv[0]) {
                    return (1);
                }
                if (v1.uv[1] != v2.uv[1]) {
                    return (1);
                }
            }
            return (0);
        }

        @Override
        long hashVal(char[] s) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        int compare(char[] s1, char[] s2) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        long hashVal(Object o) {
            return cvhash((MCVERT) o);
        }

        @Override
        public int compare(Object o1, Object o2) {
            return cvcmp((MCVERT) o1, (MCVERT) o2);
        }
    }

    static MESH getmesh( /* get new mesh data reference */
            String mname,
            int flags) {
        String pathname = null;
        MESH ms;

        flags &= IO_LEGAL;
        for (ms = mlist[0]; ms != null; ms = ms.next) {
            if (DEVCOMM.strcmp(mname.toCharArray(), ms.name.toCharArray()) == 0) {
                ms.nref++;	/* increase reference count */
                break;
            }
        }
        if (ms == null) {		/* load first time */
            ms = new MESH();
            if (ms == null) {
//			error(SYSTEM, "out of memory in getmesh");
            }
            ms.name = SAVESTR.savestr(new String(mname));
            ms.nref = 1;
            ms.mcube.cutree = OCTREE.EMPTY;
            ms.next = mlist[0];
            mlist[0] = ms;
        }
        if ((pathname = GETPATH.getpath(mname, GETLIBPATH.getrlibpath(), PATHS.R_OK)) == null) {
//		sprintf(errmsg, "cannot find mesh file \"%s\"", mname);
//		error(USER, errmsg);
        }
        flags &= ~ms.ldflags;
        if (flags != 0) {
            try {
                READMESH.readmesh(ms, pathname, flags);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MESH.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MESH.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return (ms);
    }

    public static MESHINST getmeshinst( /* create mesh instance */
            OBJREC o,
            int flags) throws IOException {
        MESHINST ins;

        flags &= IO_LEGAL;
        if ((ins = (MESHINST) o.os) == null) {
            if ((ins = new MESHINST()) == null) {
//			error(SYSTEM, "out of memory in getmeshinst");
            }
            if (o.oargs.nsargs < 1) {
//			objerror(o, USER, "bad # of arguments");
            }
            String[] ns = new String[o.oargs.sarg.length - 1];
            System.arraycopy(o.oargs.sarg, 1, ns, 0, o.oargs.sarg.length - 1);
            if (XF.fullxf(ins.x, ns.length,
                    ns) != o.oargs.nsargs - 1) {
//			objerror(o, USER, "bad transform");
            }
            if (ins.x.f.sca < 0.0) {
                ins.x.f.sca = -ins.x.f.sca;
                ins.x.b.sca = -ins.x.b.sca;
            }
            ins.msh = null;
            o.os = ins;
        }
        if (ins.msh == null) {
            ins.msh = getmesh(o.oargs.sarg[0], flags);
        } else if ((flags &= ~ins.msh.ldflags) != 0) {
            READMESH.readmesh(ins.msh,
                    GETPATH.getpath(o.oargs.sarg[0], GETLIBPATH.getrlibpath(), PATHS.R_OK),
                    flags);
        }
        return (ins);
    }

    public static int getmeshtrivid( /* get triangle vertex ID's */
            int[] tvid,
            int[] mo,
            MESH mp,
            int ti) {
        int pn = ti >> 10;
        MESHPATCH pp;

        if (pn >= mp.npatches) {
            return (0);
        }
        pp = mp.patch[pn];
        ti &= 0x3ff;
        if ((ti & 0x200) == 0) {		/* local triangle */
            PTri tp;
            if (ti >= pp.ntris) {
                return (0);
            }
            tp = pp.tri[ti];
            tvid[0] = tvid[1] = tvid[2] = pn << 8;
            tvid[0] |= (int) (tp.v1&0xff);
            tvid[1] |= (int) (tp.v2&0xff);
            tvid[2] |= (int) (tp.v3&0xff);
            if (pp.trimat != null) {
                mo[0] = pp.trimat[ti];
            } else {
                mo[0] = pp.solemat;
            }
            if (mo[0] != OBJECT.OVOID) {
                mo[0] += mp.mat0;
            }
            return (1);
        }
        ti &= ~0x200;
        if ((ti & 0x100) == 0) {		/* single link vertex */
            PJoin1 tp1;
            if (ti >= pp.nj1tris) {
                return (0);
            }
            tp1 = pp.j1tri[ti];
            tvid[0] = tp1.v1j;
            tvid[1] = tvid[2] = pn << 8;
            tvid[1] |= (int) (tp1.v2&0xff);
            tvid[2] |= (int) (tp1.v3&0xff);
            if ((mo[0] = tp1.mat) != OBJECT.OVOID) {
                mo[0] += mp.mat0;
            }
            return (1);
        }
        ti &= ~0x100;
        {				/* double link vertex */
            PJoin2 tp2;
            if (ti >= pp.nj2tris) {
                return (0);
            }
            tp2 = pp.j2tri[ti];
            tvid[0] = tp2.v1j;
            tvid[1] = tp2.v2j;
            tvid[2] = pn << 8 | (int) (tp2.v3&0xff);
            if ((mo[0] = tp2.mat) != OBJECT.OVOID) {
                mo[0] += mp.mat0;
            }
        }
        return (1);
    }
//    static int callnum = 0;
    public static int getmeshvert( /* get triangle vertex from ID */
            MESHVERT vp,
            MESH mp,
            int vid,
            int what) {
//        System.out.print(String.format("callnum = %d vid = %d\n",callnum++,vid));
        int pn = vid >> 8;        
        MESHPATCH pp;
        double vres;
        int i;

        vp.fl = 0;
        if (pn >= mp.npatches) {
            return (0);
        }
        pp = mp.patch[pn];
        vid &= 0xff;
        if (vid >= pp.nverts) {
            return (0);
        }
        /* get location */
        if ((what & MT_V) != 0) {
            vres = (1. / 4294967296.) * mp.mcube.cusize;
            for (i = 0; i < 3; i++) {
                vp.v.data[i] = mp.mcube.cuorg.data[i]
                        + (pp.xyz[vid][i] + .5) * vres;
            }
            vp.fl |= MT_V;
        }
        /* get normal */
        if ((what & MT_N) != 0 && pp.norm != null && pp.norm[vid] != 0) {
            DIRCODE.decodedir(vp.n, pp.norm[vid]);
            vp.fl |= MT_N;
        }
        /* get (u,v) */
        if ((what & MT_UV) != 0 && pp.uv != null && pp.uv[vid][0] != 0) {
            for (i = 0; i < 2; i++) {
                vp.uv[i] = mp.uvlim[0][i]
                        + (mp.uvlim[1][i] - mp.uvlim[0][i])
                        * (pp.uv[vid][i] + .5) * (1. / 4294967296.);
            }
            vp.fl |= MT_UV;
        }
        return (vp.fl);
    }

    public static OBJREC getmeshpseudo( /* get mesh pseudo object for material */
            MESH mp,
            int mo) {
        if (mo < mp.mat0 || mo >= mp.mat0 + mp.nmats) {
//		error(INTERNAL, "modifier out of range in getmeshpseudo");
        }
        if (mp.pseudo == null) {
            int i;
            mp.pseudo = new OBJREC[mp.nmats];
            for (i = 0; i < mp.pseudo.length; i++) {
                mp.pseudo[i] = new OBJREC();
            }
            if (mp.pseudo == null) {
//			error(SYSTEM, "out of memory in getmeshpseudo");
            }
            for (i = mp.nmats; i-- != 0;) {
                mp.pseudo[i].omod = mp.mat0 + i;
                mp.pseudo[i].otype = OTYPES.OBJ_FACE;
                mp.pseudo[i].oname = "M-Tri";
            }
        }
        return (mp.pseudo[mo - mp.mat0]);
    }

    public static int getmeshtri( /* get triangle vertices */
            MESHVERT[] tv,
            int[] mo,
            MESH mp,
            int ti,
            int wha) {
        int[] tvid = new int[3];

        if (getmeshtrivid(tvid, mo, mp, ti) == 0) {
            return (0);
        }

        getmeshvert(tv[0], mp, tvid[0], wha);
        getmeshvert(tv[1], mp, tvid[1], wha);
        getmeshvert(tv[2], mp, tvid[2], wha);

        return (tv[0].fl & tv[1].fl & tv[2].fl);
    }

    public static class INT_P_MCVERT extends MCVERT {

        int ikey;

        INT_P_MCVERT(MCVERT cv) {
            // probably should clone cv here
            this.fl = cv.fl;
            this.norm = cv.norm;
            this.uv = cv.uv;
            this.xyz = cv.xyz;
        }
    }

    public static int addmeshvert( /* find/add a mesh vertex */
            MESH mp,
            MESHVERT vp) {
        LUENT lvp;
        MCVERT cv = new MCVERT();
        int i;

        if ((vp.fl & MT_V) == 0) {
            return (-1);
        }
        /* encode vertex */
        for (i = 0; i < 3; i++) {
            if (vp.v.data[i] < mp.mcube.cuorg.data[i]) {
                return (-1);
            }
            if (vp.v.data[i] >= mp.mcube.cuorg.data[i] + mp.mcube.cusize) {
                return (-1);
            }
            cv.xyz[i] = (long) (4294967296.
                    * (vp.v.data[i] - mp.mcube.cuorg.data[i])
                    / mp.mcube.cusize);
        }
        if ((vp.fl & MT_N) != 0) /* assumes normalized! */ {
            cv.norm = DIRCODE.encodedir(vp.n);
        }
        if ((vp.fl & MT_UV) != 0) {
            for (i = 0; i < 2; i++) {
                if (vp.uv[i] <= mp.uvlim[0][i]) {
                    return (-1);
                }
                if (vp.uv[i] >= mp.uvlim[1][i]) {
                    return (-1);
                }
                cv.uv[i] = (long) (4294967296.
                        * (vp.uv[i] - mp.uvlim[0][i])
                        / (mp.uvlim[1][i] - mp.uvlim[0][i]));
            }
        }
        if (false) {
            System.out.print(String.format("cv.xyz = (%d,%d,%d) cv.uv = (%d,%d)\n",
                    cv.xyz[0], cv.xyz[1], cv.xyz[2], cv.uv[0], cv.uv[1]));
        }
        cv.fl = vp.fl;
        if (mp.lut.tsiz == 0) {
            mp.lut = new CVLUTAB();
            //mp.lut.hashf = cvhash;
            //mp.lut.keycmp = cvcmp;
            //mp.lut.freek = free;
            if (LOOKUP.lu_init(mp.lut, 50000) == 0) {
//			goto nomem;
            }
        }
        /* find entry */
        lvp = LOOKUP.lu_find(mp.lut, cv);
        if (lvp == null) {
//		goto nomem;
        }
        if (lvp.key == null) {
            lvp.key = new INT_P_MCVERT(cv);
//                        (char *)malloc(sizeof(MCVERT)+sizeof(int32));
//		memcpy((void *)lvp.key, (void *)&cv, sizeof(MCVERT));
        }
        if (lvp.data == null) {	/* new vertex */
            MESHPATCH pp;
            if (mp.npatches <= 0) {
                mp.patch = new MESHPATCH[MPATCHBLKSIZ];
                for (int ii = 0; ii < mp.patch.length; ii++) {
                    mp.patch[ii] = new MESHPATCH();
                }
                if (mp.patch == null) {
//				goto nomem;
                }
                mp.npatches = 1;
            } else if (mp.patch[mp.npatches - 1].nverts >= 256) {
                if (mp.npatches % MPATCHBLKSIZ == 0) {
                    mp.patch = new MESHPATCH[mp.npatches + MPATCHBLKSIZ];
                    for (int ii = 0; ii < mp.patch.length; ii++) {
                        mp.patch[ii] = new MESHPATCH();
                    }
//				memset((void *)(mp.patch + mp.npatches), '\0',
//					MPATCHBLKSIZ*sizeof(MESHPATCH));
                }
                if (mp.npatches++ >= 1L << 22) {
//				error(INTERNAL, "too many mesh patches");
                }
            }
            pp = mp.patch[mp.npatches - 1];
            if (pp.xyz == null) {
//                pp.xyz = new int[256][3];//(uint32 (*)[3])calloc(256, 3*sizeof(int32));
                pp.xyz = new long[256][3];//(uint32 (*)[3])calloc(256, 3*sizeof(int32));                
                if (pp.xyz == null) {
//				goto nomem;
                }
            }
            for (i = 0; i < 3; i++) {
                pp.xyz[pp.nverts][i] = cv.xyz[i];
            }
            if ((cv.fl & MT_N) != 0) {
                if (pp.norm == null) {
                    pp.norm = new int[256];//(int32 *)calloc(256, sizeof(int32));
                    if (pp.norm == null) {
//					goto nomem;
                    }
                }
                pp.norm[pp.nverts] = cv.norm;
            }
            if ((cv.fl & MT_UV) != 0) {
                if (pp.uv == null) {
//                    pp.uv = new int[256][2];//(uint32 (*)[2])calloc(256,2*sizeof(uint32));
                    pp.uv = new long[256][2];//(uint32 (*)[2])calloc(256,2*sizeof(uint32));
                    if (pp.uv == null) {
//					goto nomem;
                    }
                }
                for (i = 0; i < 2; i++) {
                    pp.uv[pp.nverts][i] = cv.uv[i];
                }
            }
            pp.nverts++;
            lvp.data = lvp.key;
            ((INT_P_MCVERT) lvp.data).ikey = (mp.npatches - 1) << 8 | (pp.nverts - 1);
//		lvp.data = lvp.key + sizeof(MCVERT);
//		*(int32 *)lvp.data = (mp.npatches-1) << 8 | (pp.nverts-1);
        }
        return ((INT_P_MCVERT) lvp.data).ikey;
//	return(*(int32 *)lvp.data);
//nomem:
//	error(SYSTEM, "out of memory in addmeshvert");
//	return(-1);
    }
    static int mtri = 0;

    public static int addmeshtri( /* add a new mesh triangle */
            MESH mp,
            MESHVERT[] tv,
            int mo) {
        int[] vid = new int[3];
        int t;
        int[] pn = new int[3];
        int i;
        MESHPATCH pp;
        if ((tv[0].fl & tv[1].fl & tv[2].fl & MT_V) == 0) {
            return (OBJECT.OVOID);
        }
        /* find/allocate patch vertices */
        for (i = 0; i < 3; i++) {
            if ((vid[i] = addmeshvert(mp, tv[i])) < 0) {
                return (OBJECT.OVOID);
            }
            pn[i] = vid[i] >> 8;
        }
        /* normalize material index */
        if (mo != OBJECT.OVOID) {
            if ((mo -= mp.mat0) >= mp.nmats) {
                mp.nmats = mo + 1;
            } else if (mo < 0) {
//			error(INTERNAL, "modifier range error in addmeshtri");
            }
        }
        if (false) {
            System.out.print(String.format("addmeshtri %d obj %d vid=(%d,%d,%d) pn=(%d,%d,%d)\n",
                    mtri++, mo, vid[0], vid[1], vid[2], pn[0], pn[1], pn[2]));
        }
        /* assign triangle */
        if (pn[0] == pn[1] && pn[1] == pn[2]) {	/* local case */
            pp = mp.patch[pn[0]];
            if (pp.tri == null) {
                pp.tri = new PTri[512];//(struct PTri *)malloc(512*sizeof(struct PTri));
                for (int ii = 0; ii < pp.tri.length; ii++) {
                    pp.tri[ii] = pp.new PTri();
                }
                if (pp.tri == null) {
//				goto nomem;
                }
            }
            if (pp.ntris < 512) {
                pp.tri[pp.ntris].v1 = (byte) (vid[0] & 0xff);
                pp.tri[pp.ntris].v2 = (byte) (vid[1] & 0xff);
                pp.tri[pp.ntris].v3 = (byte) (vid[2] & 0xff);
                if (pp.ntris == 0) {
                    pp.solemat = (short) mo;
                } else if (pp.trimat == null && mo != pp.solemat) {
                    pp.trimat = new short[512]; //(int16 *)malloc(512*sizeof(int16));
                    if (pp.trimat == null) {
//					goto nomem;
                    }
                    for (i = pp.ntris; i-- != 0;) {
                        pp.trimat[i] = pp.solemat;
                    }
                }
                if (pp.trimat != null) {
                    pp.trimat[pp.ntris] = (short) mo;
                }
                return (pn[0] << 10 | pp.ntris++);
            }
        }
        if (pn[0] == pn[1]) {
            t = vid[2];
            vid[2] = vid[1];
            vid[1] = vid[0];
            vid[0] = t;
            i = pn[2];
            pn[2] = pn[1];
            pn[1] = pn[0];
            pn[0] = i;
        } else if (pn[0] == pn[2]) {
            t = vid[0];
            vid[0] = vid[1];
            vid[1] = vid[2];
            vid[2] = t;
            i = pn[0];
            pn[0] = pn[1];
            pn[1] = pn[2];
            pn[2] = i;
        }
        if (pn[1] == pn[2]) {			/* single link */
            pp = mp.patch[pn[1]];
            if (pp.j1tri == null) {
                pp.j1tri = new PJoin1[256];//(struct PJoin1 *)malloc(256*sizeof(struct PJoin1));
                for (int ii = 0; ii < pp.j1tri.length; ii++) {
                    pp.j1tri[ii] = pp.new PJoin1();
                }
                if (pp.j1tri == null) {
//				goto nomem;
                }
            }
            if (pp.nj1tris < 256) {
                pp.j1tri[pp.nj1tris].v1j = vid[0];
                pp.j1tri[pp.nj1tris].v2 = (byte) (vid[1] & 0xff);
                pp.j1tri[pp.nj1tris].v3 = (byte) (vid[2] & 0xff);
                pp.j1tri[pp.nj1tris].mat = (short) mo;
                return (pn[1] << 10 | 0x200 | pp.nj1tris++);
            }
        }
        /* double link */
        pp = mp.patch[pn[2]];
        if (pp.j2tri == null) {
            pp.j2tri = new PJoin2[256]; //(struct PJoin2 *)malloc(256*sizeof(struct PJoin2));
            for (int ii = 0; ii < pp.j2tri.length; ii++) {
                pp.j2tri[ii] = pp.new PJoin2();
            }
            if (pp.j2tri == null) {
//			goto nomem;
            }
        }
        if (pp.nj2tris >= 256) {
//		error(INTERNAL, "too many patch triangles in addmeshtri");
        }
        pp.j2tri[pp.nj2tris].v1j = vid[0];
        pp.j2tri[pp.nj2tris].v2j = vid[1];
        pp.j2tri[pp.nj2tris].v3 = (byte) (vid[2] & 0xff);
        pp.j2tri[pp.nj2tris].mat = (short) mo;
        return (pn[2] << 10 | 0x300 | pp.nj2tris++);
//nomem:
//	error(SYSTEM, "out of memory in addmeshtri");
//	return(OVOID);        
    }
    static String embuf;

    public static String checkmesh( /* validate mesh data */
            MESH mp) {
        int nouvbounds = 1;
        int i;
        /* basic checks */
        if (mp == null) {
            return ("null mesh pointer");
        }
        if (mp.ldflags == 0) {
            return ("unassigned mesh");
        }
        if (mp.name == null) {
            return ("missing mesh name");
        }
        if (mp.nref <= 0) {
            return ("unreferenced mesh");
        }
        /* check boundaries */
        if ((mp.ldflags & OCTREE.IO_BOUNDS) != 0) {
            if (mp.mcube.cusize <= FVECT.FTINY) {
                return ("illegal octree bounds in mesh");
            }
            nouvbounds = (mp.uvlim[1][0] - mp.uvlim[0][0] <= FVECT.FTINY
                    || mp.uvlim[1][1] - mp.uvlim[0][1] <= FVECT.FTINY) ? 1 : 0;
        }
        /* check octree */
        if ((mp.ldflags & OCTREE.IO_TREE) != 0) {
            if (OCTREE.isempty(mp.mcube.cutree) != 0) {
//			error(WARNING, "empty mesh octree");
            }
        }
        /* check scene data */
        if ((mp.ldflags & OCTREE.IO_SCENE) != 0) {
            if ((mp.ldflags & OCTREE.IO_BOUNDS) == 0) {
                return ("unbounded scene in mesh");
            }
            if (mp.mat0 < 0 || mp.mat0 + mp.nmats > OBJECT.nobjects) {
                return ("bad mesh modifier range");
            }
            for (i = mp.mat0 + mp.nmats; i-- > mp.mat0;) {
                int otyp = OBJECT.objptr(i).otype;
                if (OTYPES.ismodifier(otyp) == 0) {
                    embuf = String.format("non-modifier in mesh (%s \"%s\")",
                            OTYPES.ofun[otyp].funame, OBJECT.objptr(i).oname);
                    return (embuf);
                }
            }
            if (mp.npatches <= 0) {
//			error(WARNING, "no patches in mesh");
            }
            for (i = 0; i < mp.npatches; i++) {
                MESHPATCH pp = mp.patch[i];
                if (pp.nverts <= 0) {
//				error(WARNING, "no vertices in patch");
                } else {
                    if (pp.xyz == null) {
                        return ("missing patch vertex list");
                    }
                    if (nouvbounds != 0 && pp.uv != null) {
                        return ("unreferenced uv coordinates");
                    }
                }
                if (pp.ntris > 0 && pp.tri == null) {
                    return ("missing patch triangle list");
                }
                if (pp.nj1tris > 0 && pp.j1tri == null) {
                    return ("missing patch joiner triangle list");
                }
                if (pp.nj2tris > 0 && pp.j2tri == null) {
                    return ("missing patch double-joiner list");
                }
            }
        }
        return (null);			/* seems OK */
    }

    static void tallyoctree( /* tally octree size */
            int ot,
            int[] ecp, int[] lcp, int[] ocp) {
        int i;

        if (OCTREE.isempty(ot) != 0) {
            ecp[0]++;
            return;
        }
        if (OCTREE.isfull(ot) != 0) {
            int[] oset = new int[OBJECT.MAXSET + 1];
            lcp[0]++;
            OBJSET.objset(oset, ot);
            ocp[0] += oset[0];
            return;
        }
        for (i = 0; i < 8; i++) {
            tallyoctree(OCTREE.octkid(ot, i), ecp, lcp, ocp);
        }
    }

    public static void printmeshstats( /* print out mesh statistics */
            MESH ms,
            OutputStream fp) throws IOException {
        int[] lfcnt = new int[1], lecnt = new int[1], locnt = new int[1];
        lfcnt[0] = lecnt[0] = locnt[0] = 0;
        int vcnt = 0, ncnt = 0, uvcnt = 0;
        int nscnt = 0, uvscnt = 0;
        int tcnt = 0, t1cnt = 0, t2cnt = 0;
        int i, j;

        tallyoctree(ms.mcube.cutree, lecnt, lfcnt, locnt);
        for (i = 0; i < ms.npatches; i++) {
            MESHPATCH pp = ms.patch[i];
            vcnt += pp.nverts;
            if (pp.norm != null) {
                for (j = pp.nverts; j-- != 0;) {
                    if (pp.norm[j] != 0) {
                        ncnt++;
                    }
                }
                nscnt += pp.nverts;
            }
            if (pp.uv != null) {
                for (j = pp.nverts; j-- != 0;) {
                    if (pp.uv[j][0] != 0) {
                        uvcnt++;
                    }
                }
                uvscnt += pp.nverts;
            }
            tcnt += pp.ntris;
            t1cnt += pp.nj1tris;
            t2cnt += pp.nj2tris;
        }
        String str = new String();
        str += String.format("Mesh statistics:\n");
        str += String.format("\t%d materials\n", (long) ms.nmats);
        str += String.format("\t%d patches (??? MBytes)\n", ms.npatches);//(%.2f MBytes)\n", ms.npatches,
//			(ms.npatches*sizeof(MESHPATCH) +
//			vcnt*3*sizeof(uint32) +
//			nscnt*sizeof(int32) +
//			uvscnt*2*sizeof(uint32) +
//			tcnt*sizeof(struct PTri) +
//			t1cnt*sizeof(struct PJoin1) +
//			t2cnt*sizeof(struct PJoin2))/(1024.*1024.));
        str += String.format("\t%d vertices (%.1f%% w/ normals, %.1f%% w/ uv)\n",
                vcnt, 100. * ncnt / vcnt, 100. * uvcnt / vcnt);
        str += String.format("\t%d triangles (%.1f%% local, %.1f%% joiner)\n",
                tcnt + t1cnt + t2cnt,
                100. * tcnt / (tcnt + t1cnt + t2cnt),
                100. * t1cnt / (tcnt + t1cnt + t2cnt));
        str += String.format(
                "\t%d leaves in octree (%.1f%% empty, %.2f avg. set size)\n",
                lfcnt[0] + lecnt[0], 100. * lecnt[0] / (lfcnt[0] + lecnt[0]),
                (double) locnt[0] / lfcnt[0]);
        fp.write(str.getBytes());
    }

    void freemesh( /* free mesh data */
            MESH ms) {
        MESH mhead = new MESH();
        MESH msp;

        if (ms == null) {
            return;
        }
        if (ms.nref <= 0) {
//		error(CONSISTENCY, "unreferenced mesh in freemesh");
        }
        ms.nref--;
        if (ms.nref != 0) /* still in use */ {
            return;
        }
        /* else remove from list */
        mhead.next = mlist[0];
        for (msp = mhead; msp.next != null; msp = msp.next) {
            if (msp.next == ms) {
                msp.next = ms.next;
                ms.next = null;
                break;
            }
        }
        if (ms.next != null) {	/* can't be in list anymore */
//		error(CONSISTENCY, "unlisted mesh in freemesh");

        }
        mlist[0] = mhead.next;
        /* free mesh data */
//	freestr(ms.name);
//	octfree(ms.mcube.cutree);
//	lu_done(&ms.lut);
//	if (ms.npatches > 0) {
//		MESHPATCH	*pp = ms.patch + ms.npatches;
//		while (pp-- > ms.patch) {
//			if (pp.j2tri != null)
//				free((void *)pp.j2tri);
//			if (pp.j1tri != null)
//				free((void *)pp.j1tri);
//			if (pp.tri != null)
//				free((void *)pp.tri);
//			if (pp.uv != null)
//				free((void *)pp.uv);
//			if (pp.norm != null)
//				free((void *)pp.norm);
//			if (pp.xyz != null)
//				free((void *)pp.xyz);
//		}
//		free((void *)ms.patch);
//	}
//	if (ms.pseudo != null)
//		free((void *)ms.pseudo);
//	free((void *)ms);
    }

    void freemeshinst( /* free mesh instance */
            OBJREC o) {
        if (o.os == null) {
            return;
        }
//	freemesh((*(MESHINST *)o.os).msh);
//	free((void *)o.os);
        o.os = null;
    }
}
