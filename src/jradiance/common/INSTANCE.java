/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jradiance.common.OBJECT.OBJREC;
import jradiance.common.OCTREE.CUBE;
import jradiance.common.RTMATH.FULLXF;

/**
 *
 * @author arwillis
 */
public class INSTANCE extends OBJECT_STRUCTURE {

    /*
     *  instance.h - header file for routines using octree objects.
     *
     *  Include after object.h and octree.h
     */
    public class SCENE {

        public String name;			/* octree name */

        public int nref;			/* number of references */

        public int ldflags;			/* what was loaded */

        public OCTREE.CUBE scube;			/* scene cube */

        public int firstobj, nobjs;	/* first object and count */

        public SCENE next;		/* next in list */

    }			/* loaded octree */

//public class INSTANCE {
    public FULLXF x;			/* forward and backward transforms */

    public SCENE obj = new SCENE();			/* loaded object */
//}  			/* instance of octree */
//
//
//extern SCENE  *getscene(char *sname, int flags);
//extern INSTANCE  *getinstance(OBJREC *o, int flags);
//extern void  freescene(SCENE *sc);
//extern void  freeinstance(OBJREC *o);
//
//
//}
/*
     *  instance.c - routines for octree objects.
     */

    public static final int IO_ILLEGAL = (OCTREE.IO_FILES | OCTREE.IO_INFO);
    static SCENE slist = null;		/* list of loaded octrees */


    static SCENE getscene( /* get new octree reference */
            String sname,
            int flags) {
//	char  *pathname;
//	register SCENE  *sc;
//
//	flags &= ~IO_ILLEGAL;		/* not allowed */
//	for (sc = slist; sc != null; sc = sc.next)
//		if (!strcmp(sname, sc.name))
//			break;
//	if (sc == null) {
//		sc = (SCENE *)malloc(sizeof(SCENE));
//		if (sc == null)
//			error(SYSTEM, "out of memory in getscene");
//		sc.name = savestr(sname);
//		sc.nref = 0;
//		sc.ldflags = 0;
//		sc.scube.cutree = EMPTY;
//		sc.scube.cuorg[0] = sc.scube.cuorg[1] =
//				sc.scube.cuorg[2] = 0.;
//		sc.scube.cusize = 0.;
//		sc.firstobj = sc.nobjs = 0;
//		sc.next = slist;
//		slist = sc;
//	}
//	if ((pathname = getpath(sname, getrlibpath(), R_OK)) == null) {
//		sprintf(errmsg, "cannot find octree file \"%s\"", sname);
//		error(USER, errmsg);
//	}
//	flags &= ~sc.ldflags;		/* skip what's already loaded */
//	if (flags & IO_SCENE)
//		sc.firstobj = nobjects;
//	if (flags)
//		readoct(pathname, flags, &sc.scube, null);
//	if (flags & IO_SCENE)
//		sc.nobjs = nobjects - sc.firstobj;
//	sc.ldflags |= flags;
//	sc.nref++;			/* increase reference count */
//	return(sc);
        return null;
    }

    public static INSTANCE getinstance( /* get instance structure */
            OBJREC o,
            int flags) throws IOException {
        INSTANCE ins;

        flags &= ~IO_ILLEGAL;		/* not allowed */
        if ((ins = (INSTANCE) o.os) == null) {
            if ((ins = new INSTANCE()) == null) {
//			error(SYSTEM, "out of memory in getinstance");
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
            ins.obj = null;
            o.os = ins;
        }
        if (ins.obj == null) {
            ins.obj = getscene(o.oargs.sarg[0], flags);
        } else if ((flags &= ~ins.obj.ldflags) != 0) {
            if ((flags & OCTREE.IO_SCENE) != 0) {
                ins.obj.firstobj = OBJECT.nobjects;
            }
            if (flags != 0) {
			READOCT.readoct(GETPATH.getpath(o.oargs.sarg[0], GETLIBPATH.getrlibpath(), PATHS.R_OK),
					flags, ins.obj.scube, null);
            }
            if ((flags & OCTREE.IO_SCENE) != 0) {
                ins.obj.nobjs = OBJECT.nobjects - ins.obj.firstobj;
            }
            ins.obj.ldflags |= flags;
        }
        return (ins);
    }
//
//
//void
//freescene(sc)		/* release a scene reference */
//SCENE *sc;
//{
//	SCENE  shead;
//	register SCENE  *scp;
//
//	if (sc == null)
//		return;
//	if (sc.nref <= 0)
//		error(CONSISTENCY, "unreferenced scene in freescene");
//	sc.nref--;
//	if (sc.nref)			/* still in use? */
//		return;
//	shead.next = slist;		/* else remove from our list */
//	for (scp = &shead; scp.next != null; scp = scp.next)
//		if (scp.next == sc) {
//			scp.next = sc.next;
//			sc.next = null;
//			break;
//		}
//	if (sc.next != null)		/* can't be in list anymore */
//		error(CONSISTENCY, "unlisted scene in freescene");
//	slist = shead.next;
//	freestr(sc.name);		/* free memory */
//	octfree(sc.scube.cutree);
//	freeobjects(sc.firstobj, sc.nobjs);
//	free((void *)sc);
//}
//
//
//void
//freeinstance(o)		/* free memory associated with instance */
//OBJREC  *o;
//{
//	if (o.os == null)
//		return;
//	freescene((*(INSTANCE *)o.os).obj);
//	free((void *)o.os);
//	o.os = null;
//}

    /*
     *  o_instance.c - routines for creating octrees for other octrees
     */

    /*
     *	To determine if two cubes intersect:
     *
     *	1) Check to see if any vertices of first cube are inside the
     *	   second (intersection).
     *
     *	2) Check to see if all vertices of first are to one side of
     *	   second (no intersection).
     *
     *	3) Perform 1 and 2 with roles reversed.
     *
     *	4) Check to see if any portion of any edge of second is inside
     *	   first (intersection).
     *
     *	5) If test 4 fails, we have no intersection.
     *
     *	Note that if we were testing two boxes, we would need
     *  to check that neither had any edges inside the other to be sure.
     *	Since an octree is a volume rather than a surface, we will
     *  return a value of 2 if the cube is entirely within the octree.
     */
//
//static int o_cube(CUBE  *cu1, FULLXF  *fxf, CUBE  *cu);
//
    @Override
    public int octree_function(Object... obj) {
        try {
            return o_instance((OBJREC) obj[0], (CUBE) obj[1]);
        } catch (IOException ex) {
            Logger.getLogger(INSTANCE.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    int o_instance( /* determine if instance intersects */
            OBJREC o,
            CUBE cu) throws IOException {
        INSTANCE ins;
        /* get octree bounds */
        ins = getinstance(o, OCTREE.IO_BOUNDS);
        /* call o_cube to do the work */
        return (o_cube(ins.obj.scube, ins.x, cu));
    }
    static int[] vstart = {0, 3, 5, 6};

    static int o_cube( /* determine if cubes intersect */
            CUBE cu1,
            FULLXF fxf,
            CUBE cu) {
        FVECT cumin = new FVECT(), cumax = new FVECT();
        FVECT[] vert = new FVECT[8];
        for (int i = 0; i < vert.length; i++) {
            vert[i] = new FVECT();
        }
        FVECT v1 = new FVECT(), v2 = new FVECT();
        int vloc, vout;
        int i, j;
        /* check if cube vertex in octree */
        for (j = 0; j < 3; j++) {
            cumax.data[j] = (cumin.data[j] = cu1.cuorg.data[j]) + cu1.cusize;
        }
        vloc = PLOCATE.ABOVE | PLOCATE.BELOW;
        vout = 0;
        for (i = 0; i < 8; i++) {
            for (j = 0; j < 3; j++) {
                v1.data[j] = cu.cuorg.data[j];
                if ((i & 1 << j) != 0) {
                    v1.data[j] += cu.cusize;
                }
            }
            MAT4.multp3(v2, v1, fxf.b.xfm);
            if ((j = PLOCATE.plocate(v2, cumin, cumax)) != 0) {
                vout++;
            }
            vloc &= j;
        }
        if (vout == 0) /* all inside */ {
            return (OCTREE.O_IN);
        }
        if (vout < 8) /* some inside */ {
            return (OCTREE.O_HIT);
        }
        if (vloc != 0) /* all to one side */ {
            return (OCTREE.O_MISS);
        }
        /* octree vertices in cube? */
        for (j = 0; j < 3; j++) {
            cumax.data[j] = (cumin.data[j] = cu.cuorg.data[j]) + cu.cusize;
        }
        vloc = PLOCATE.ABOVE | PLOCATE.BELOW;
        for (i = 0; i < 8; i++) {
            for (j = 0; j < 3; j++) {
                v1.data[j] = cu1.cuorg.data[j];
                if ((i & 1 << j) != 0) {
                    v1.data[j] += cu1.cusize;
                }
            }
            MAT4.multp3(vert[i], v1, fxf.f.xfm);
            if ((j = PLOCATE.plocate(vert[i], cumin, cumax)) != 0) {
                vloc &= j;
            } else {
                return (OCTREE.O_HIT);	/* vertex inside */
            }
        }
        if (vloc != 0) /* all to one side */ {
            return (OCTREE.O_MISS);
        }
        /* check edges */
        for (i = 0; i < 4; i++) {
            for (j = 0; j < 3; j++) {
                /* clip modifies vertices! */
                FVECT.VCOPY(v1, vert[vstart[i]]);
                FVECT.VCOPY(v2, vert[vstart[i] ^ 1 << j]);
                if (CLIP.clip(v1, v2, cumin, cumax) != 0) {
                    return (OCTREE.O_HIT);		/* edge inside */
                }
            }
        }

        return (OCTREE.O_MISS);			/* no intersection */
    }
// MOVED TO MESH.JAVA in COMMON
//    public static class MESH extends OBJECT_STRUCTURE {
//
//        @Override
//        public int octree_function(Object... obj) {
//            return o_mesh((OBJREC) obj[1], (CUBE) obj[1]);
//        }
//
//        int o_mesh( /* determine if mesh intersects */
//                OBJREC o,
//                CUBE cu) {
//	MESHINST	*mip;
//					/* get mesh bounds */
//	mip = getmeshinst(o, IO_BOUNDS);
//					/* call o_cube to do the work */
//	return(o_cube(&mip.msh.mcube, &mip.x, cu));
//            return 0;
//        }
//    }
}
