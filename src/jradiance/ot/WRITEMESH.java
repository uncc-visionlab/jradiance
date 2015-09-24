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
package jradiance.ot;

import java.io.IOException;
import java.io.OutputStream;
import jradiance.common.MESH;
import jradiance.common.MESH.MESHPATCH;
import jradiance.common.OBJECT;
import jradiance.common.OBJSET;
import jradiance.common.OCTREE;
import jradiance.common.PORTIO;
import jradiance.common.SCENEIO;

/**
 *
 * @author arwillis
 */
public class WRITEMESH {
    /*
     *  Routines for writing compiled mesh to a file stream
     */

    static void putfullnode( /* write out a full node */
            int fn,
            OutputStream fp) throws IOException {
        int[] oset = new int[OBJECT.MAXSET + 1];
        int i;

        OBJSET.objset(oset, fn);
        int sizeofOBJECT = Integer.SIZE / 8;
        for (i = 0; i <= oset[0]; i++) {
            PORTIO.putint((long) oset[i], sizeofOBJECT, fp);
        }
    }

    static void puttree( /* write octree to fp in pre-order form */
            int ot,
            OutputStream fp) throws IOException {

        if (OCTREE.istree(ot) != 0) {
            int i;
            fp.write(OCTREE.OT_TREE);		/* indicate tree */
            for (i = 0; i < 8; i++) /* write tree */ {
                puttree(OCTREE.octkid(ot, i), fp);
            }
            return;
        }
        if (OCTREE.isfull(ot) != 0) {
            fp.write(OCTREE.OT_FULL);		/* indicate fullnode */
            putfullnode(ot, fp);		/* write fullnode */
            return;
        }
        fp.write(OCTREE.OT_EMPTY);			/* indicate empty */
    }

    static void putpatch( /* write out a mesh patch */
            MESHPATCH pp,
            OutputStream fp) throws IOException {
        int flags = MESH.MT_V;
        int i, j;
        /* vertex flags */
        if (pp.norm != null) {
            flags |= MESH.MT_N;
        }
        if (pp.uv != null) {
            flags |= MESH.MT_UV;
        }
        PORTIO.putint((long) flags, 1, fp);
        /* number of vertices */
        PORTIO.putint((long) pp.nverts, 2, fp);
        /* vertex xyz locations */
        for (i = 0; i < pp.nverts; i++) {
            for (j = 0; j < 3; j++) {
                PORTIO.putint((long) pp.xyz[i][j], 4, fp);
            }
        }
        /* vertex normals */
        if ((flags & MESH.MT_N) != 0) {
            for (i = 0; i < pp.nverts; i++) {
                PORTIO.putint((long) pp.norm[i], 4, fp);
            }
        }
        /* uv coordinates */
        if ((flags & MESH.MT_UV) != 0) {
            for (i = 0; i < pp.nverts; i++) {
                for (j = 0; j < 2; j++) {
                    PORTIO.putint((long) pp.uv[i][j], 4, fp);
                }
            }
        }
        /* local triangles */
        PORTIO.putint((long) pp.ntris, 2, fp);
        for (i = 0; i < pp.ntris; i++) {
            PORTIO.putint((long) pp.tri[i].v1, 1, fp);
            PORTIO.putint((long) pp.tri[i].v2, 1, fp);
            PORTIO.putint((long) pp.tri[i].v3, 1, fp);
        }
        /* local triangle material(s) */
        if (pp.trimat == null) {
            PORTIO.putint(1L, 2, fp);
            PORTIO.putint((long) pp.solemat, 2, fp);
        } else {
            PORTIO.putint((long) pp.ntris, 2, fp);
            for (i = 0; i < pp.ntris; i++) {
                PORTIO.putint((long) pp.trimat[i], 2, fp);
            }
        }
        /* joiner triangles */
        PORTIO.putint((long) pp.nj1tris, 2, fp);
        for (i = 0; i < pp.nj1tris; i++) {
            PORTIO.putint((long) pp.j1tri[i].v1j, 4, fp);
            PORTIO.putint((long) pp.j1tri[i].v2, 1, fp);
            PORTIO.putint((long) pp.j1tri[i].v3, 1, fp);
            PORTIO.putint((long) pp.j1tri[i].mat, 2, fp);
        }
        /* double joiner triangles */
        PORTIO.putint((long) pp.nj2tris, 2, fp);
        for (i = 0; i < pp.nj2tris; i++) {
            PORTIO.putint((long) pp.j2tri[i].v1j, 4, fp);
            PORTIO.putint((long) pp.j2tri[i].v2j, 4, fp);
            PORTIO.putint((long) pp.j2tri[i].v3, 1, fp);
            PORTIO.putint((long) pp.j2tri[i].mat, 2, fp);
        }
    }

    static void writemesh( /* write mesh structures to fp */
            MESH mp,
            OutputStream fp) throws IOException {
        String err;
        char[] sbuf = new char[64];
        int i;
        /* do we have everything? */
        if ((mp.ldflags & (OCTREE.IO_SCENE | OCTREE.IO_TREE | OCTREE.IO_BOUNDS))
                != (OCTREE.IO_SCENE | OCTREE.IO_TREE | OCTREE.IO_BOUNDS)) {
//		error(INTERNAL, "missing data in writemesh");
        }
        /* validate mesh data */
	if ((err = MESH.checkmesh(mp)) != null) {
//		error(USER, err);
        }
					/* write format number */
        int sizeofOBJECT = Integer.SIZE / 8;
        PORTIO.putint((long) (MESH.MESHMAGIC + sizeofOBJECT), 2, fp);
        /* write boundaries */
        for (i = 0; i < 3; i++) {
            String str = String.format("%.12g", mp.mcube.cuorg.data[i]);
            PORTIO.putstr(str, fp);
        }
        String str = String.format("%.12g", mp.mcube.cusize);
        PORTIO.putstr(str, fp);
        for (i = 0; i < 2; i++) {
            PORTIO.putflt(mp.uvlim[0][i], fp);
            PORTIO.putflt(mp.uvlim[1][i], fp);
        }
        /* write the octree */
        puttree(mp.mcube.cutree, fp);
        /* write the materials */
        SCENEIO.writescene(mp.mat0, mp.nmats, fp);
        /* write the patches */
        PORTIO.putint((long) mp.npatches, 4, fp);
        for (i = 0; i < mp.npatches; i++) {
            putpatch(mp.patch[i], fp);
        }
//	if (ferror(fp))
//		error(SYSTEM, "write error in writemesh");
    }
}
