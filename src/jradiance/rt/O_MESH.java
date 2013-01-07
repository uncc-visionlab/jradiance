/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.rt;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jradiance.common.FVECT;
import jradiance.common.MAT4;
import jradiance.common.MESH;
import jradiance.common.MESH.MESHINST;
import jradiance.common.MESH.MESHVERT;
import jradiance.common.MODOBJECT;
import jradiance.common.OBJECT;
import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OBJECT_STRUCTURE;
import jradiance.common.OCTREE;
import jradiance.common.TMESH;
import jradiance.rt.O_MESH.EdgeCache.EdgeSide; // ??? weird
import jradiance.rt.RAYTRACE.RAYHIT;

/**
 *
 * @author arwillis
 */
public class O_MESH extends OBJECT_STRUCTURE {
    /*
     *  Routines for computing ray intersections with meshes.
     *
     *  Intersection with a triangle mesh is based on Segura and Feito's
     *  WSCG 2001 paper, "Algorithms to Test Ray-Triangle Intersection,
     *  Comparative Study."  This method avoids additional storage
     *  requirements, floating divides, and allows some savings by
     *  caching ray-edge comparisons that are otherwise repeated locally
     *  in typical mesh geometries.  (This is our own optimization.)
     *
     *  The code herein is quite similar to that in o_instance.c, the
     *  chief differences being the custom triangle intersection routines
     *  and the fact that an "OBJECT" in the mesh octree is not an index
     *  into the Radiance OBJREC list, but a mesh triangle index.  We still
     *  utilize the standard octree traversal code by setting the hitf
     *  function pointer in the RAY struct to our custom mesh_hit() call.
     */

//
//
    public static final int EDGE_CACHE_SIZ = 251;	/* length of mesh edge cache */
//
//#define  curmi			(edge_cache.mi)
//#define  curmsh			(curmi.msh)
//
//
/* Cache of signed volumes for this ray and this mesh */


    public static class EdgeCache {

        OBJREC o;	/* mesh object */

        MESHINST mi;	/* current mesh instance */


        public class EdgeSide {

            int v1i, v2i;	/* vertex indices (lowest first) */

            short signum;		/* signed volume */

        }
        EdgeSide[] cache = new EdgeSide[EDGE_CACHE_SIZ];
    }
    static EdgeCache edge_cache = new EdgeCache();

    static void prep_edge_cache( /* get instance and clear edge cache */
            OBJREC o) throws IOException {
        /* get mesh instance */
        edge_cache.mi = MESH.getmeshinst(edge_cache.o = o, OCTREE.IO_ALL);
        for (int ii=0; ii < edge_cache.cache.length; ii++) {
            edge_cache.cache[ii] = edge_cache.new EdgeSide();
        }
        /* clear edge cache */
        //memset((void *)edge_cache.cache, '\0', sizeof(edge_cache.cache));
    }

    static int volume_sign( /* get signed volume for ray and edge */
            RAY r,
            int v1, int v2) {
        int reversed = 0;
        EdgeSide ecp;

        if (v1 > v2) {
            int t = v2;
            v2 = v1;
            v1 = t;
            reversed = 1;
        }
        ecp = edge_cache.cache[((v2 << 11 ^ v1) & 0x7fffffff) % EDGE_CACHE_SIZ];        
        if ((ecp.v1i != v1) | (ecp.v2i != v2)) {
            MESHVERT tv1 = new MESHVERT(), tv2 = new MESHVERT();	/* compute signed volume */
            FVECT v2d = new FVECT();
            double vol;
            if (MESH.getmeshvert(tv1, edge_cache.mi.msh, v1, MESH.MT_V) == 0
                    | MESH.getmeshvert(tv2, edge_cache.mi.msh, v2, MESH.MT_V) == 0) {
//			objerror(edge_cache.o, INTERNAL,
//				"missing mesh vertex in volume_sign");
            }
//            System.out.print(String.format("tv1.v = (%1.3f,%1.3f,%1.3f) ",tv1.v.data[0],tv1.v.data[1],tv1.v.data[2]));
//            System.out.print(String.format("tv2.v = (%1.3f,%1.3f,%1.3f)\n",tv2.v.data[0],tv2.v.data[1],tv2.v.data[2]));
            FVECT.VSUB(v2d, tv2.v, r.rorg);
            vol = (tv1.v.data[0] - r.rorg.data[0])
                    * (v2d.data[1] * r.rdir.data[2] - v2d.data[2] * r.rdir.data[1]);
            vol += (tv1.v.data[1] - r.rorg.data[1])
                    * (v2d.data[2] * r.rdir.data[0] - v2d.data[0] * r.rdir.data[2]);
            vol += (tv1.v.data[2] - r.rorg.data[2])
                    * (v2d.data[0] * r.rdir.data[1] - v2d.data[1] * r.rdir.data[0]);
            /* don't generate 0 */
            ecp.signum = (short) (vol > .0 ? 1 : -1);
            ecp.v1i = v1;
            ecp.v2i = v2;
        }
        return (reversed != 0 ? -ecp.signum : ecp.signum);
    }

    public static class MESH_RAYHIT implements RAYHIT {

        @Override
        public void rayhit( /* standard ray hit test */
                int[] oset,
                RAY r) {
            mesh_hit(oset, r);
        }

        static void mesh_hit( /* intersect ray with mesh triangle(s) */
                int[] oset,
                RAY r) {
            int[] tvi = new int[3];
            int sv1, sv2, sv3;
            MESHVERT[] tv = {new MESHVERT(), new MESHVERT(), new MESHVERT()};
            int[] tmod = new int[1];
            FVECT va = new FVECT(), vb = new FVECT(), nrm = new FVECT();
            double d;
            int i;
            /* check each triangle */
            for (i = oset[0]; i > 0; i--) {
                if (MESH.getmeshtrivid(tvi, tmod, edge_cache.mi.msh, oset[i]) == 0) {
//			objerror(edge_cache.o, INTERNAL,
//				"missing triangle vertices in mesh_hit");
                }
//                System.out.print(String.format("trivid = (%d,%d,%d)\n", tvi[0],tvi[1],tvi[2]));
                sv1 = volume_sign(r, tvi[0], tvi[1]);
                sv2 = volume_sign(r, tvi[1], tvi[2]);
                sv3 = volume_sign(r, tvi[2], tvi[0]);
                /* compare volume signs */
                if ((sv1 != sv2) | (sv2 != sv3)) {
                    continue;
                }
                /* compute intersection */
                MESH.getmeshvert(tv[0], edge_cache.mi.msh, tvi[0], MESH.MT_V);
                MESH.getmeshvert(tv[1], edge_cache.mi.msh, tvi[1], MESH.MT_V);
                MESH.getmeshvert(tv[2], edge_cache.mi.msh, tvi[2], MESH.MT_V);
                FVECT.VSUB(va, tv[0].v, tv[2].v);
                FVECT.VSUB(vb, tv[1].v, tv[0].v);
                FVECT.VCROSS(nrm, va, vb);
                d = FVECT.DOT(r.rdir, nrm);
                if (d == 0.0) {
                    continue;		/* ray is tangent */
                }
                FVECT.VSUB(va, tv[0].v, r.rorg);
                d = FVECT.DOT(va, nrm) / d;
                if (d <= FVECT.FTINY || d >= r.rot) {
                    continue;		/* not good enough */
                }
                r.robj = oset[i];		/* else record hit */
                r.ro = edge_cache.o;
                r.rot = d;
                FVECT.VSUM(r.rop, r.rorg, r.rdir, d);
//                System.out.print(String.format(" hit object %d at r.rop = (%1.3f,%1.3f,%1.3f) ",
//                        r.robj,r.rop.data[0],r.rop.data[1],r.rop.data[2]));
                FVECT.VCOPY(r.ron, nrm);
                /* normalize(r.ron) called & r.rod set in o_mesh() */
            }
        }
    }

    @Override
    public int octree_function(Object... obj) {
        try {
            return o_mesh((OBJREC) obj[0], (RAY) obj[1]);
        } catch (IOException ex) {
            Logger.getLogger(O_MESH.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    int o_mesh( /* compute ray intersection with a mesh */
            OBJREC o,
            RAY r) throws IOException {
        RAY rcont;
        int flags;
        MESHVERT[] tv = {new MESHVERT(), new MESHVERT(), new MESHVERT()};
        int[] tmod = new int[1];
        double[] wt = new double[3];
        int i;
        /* get the mesh instance */
        prep_edge_cache(o);
        /* copy and transform ray */
        rcont = r.copy();
        MAT4.multp3(rcont.rorg, r.rorg, edge_cache.mi.x.b.xfm);
        MAT4.multv3(rcont.rdir, r.rdir, edge_cache.mi.x.b.xfm);
        for (i = 0; i < 3; i++) {
            rcont.rdir.data[i] /= edge_cache.mi.x.b.sca;
        }
        rcont.rmax *= edge_cache.mi.x.b.sca;
        /* clear and trace ray */
        RAYTRACE.rayclear(rcont);
        rcont.hitf = new MESH_RAYHIT(); //mesh_hit;
        if (RAYTRACE.localhit(rcont, edge_cache.mi.msh.mcube) == 0) {
            return (0);			/* missed */
        }
        if (rcont.rot * edge_cache.mi.x.f.sca >= r.rot) {
            return (0);			/* not close enough */
        }
        /* transform ray back */
        r.rot = rcont.rot * edge_cache.mi.x.f.sca;
        MAT4.multp3(r.rop, rcont.rop, edge_cache.mi.x.f.xfm);
        MAT4.multv3(r.ron, rcont.ron, edge_cache.mi.x.f.xfm);
        FVECT.normalize(r.ron);
        r.rod = -FVECT.DOT(r.rdir, r.ron);
        /* get triangle */
        flags = MESH.getmeshtri(tv, tmod, edge_cache.mi.msh, rcont.robj, MESH.MT_ALL);
        if ((flags & MESH.MT_V) == 0) {
//		objerror(o, INTERNAL, "missing mesh vertices in o_mesh");
        }
        r.robj = MODOBJECT.objndx(o);		/* set object and material */
        if (o.omod == OBJECT.OVOID && tmod[0] != OBJECT.OVOID) {
            r.ro = MESH.getmeshpseudo(edge_cache.mi.msh, tmod[0]);
            r.rox = edge_cache.mi.x;
        } else {
            r.ro = o;
        }
        /* compute barycentric weights */
        if ((flags & (MESH.MT_N | MESH.MT_UV)) != 0) {
            if (TMESH.get_baryc(wt, rcont.rop, tv[0].v, tv[1].v, tv[2].v) < 0) {
//			objerror(o, WARNING, "bad triangle in o_mesh");
                flags &= ~(MESH.MT_N | MESH.MT_UV);
            }
        }
        if ((flags & MESH.MT_N) != 0) {		/* interpolate normal */
            for (i = 0; i < 3; i++) {
                rcont.pert.data[i] = wt[0] * tv[0].n.data[i]
                        + wt[1] * tv[1].n.data[i]
                        + wt[2] * tv[2].n.data[i];
            }
            MAT4.multv3(r.pert, rcont.pert, edge_cache.mi.x.f.xfm);
            if (FVECT.normalize(r.pert) != 0.0) {
                FVECT.VSUB(r.pert, r.pert, r.ron);
            }
        } else {
            r.pert.data[0] = r.pert.data[1] = r.pert.data[2] = .0;
        }

        if ((flags & MESH.MT_UV) != 0) /* interpolate uv coordinates */ {
            for (i = 0; i < 2; i++) {
                r.uv[i] = wt[0] * tv[0].uv[i]
                        + wt[1] * tv[1].uv[i]
                        + wt[2] * tv[2].uv[i];
            }
        } else {
            r.uv[0] = r.uv[1] = .0;
        }

        /* return hit */
        return (1);
    }
}
