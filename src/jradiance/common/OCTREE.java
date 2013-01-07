/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

/**
 *
 * @author arwillis
 */
public class OCTREE {

    /* RCSid $Id$ */
    /*
     *  octree.h - header file for routines using octrees.
     */
//#ifndef _RAD_OCTREE_H_
//#define _RAD_OCTREE_H_
//#ifdef __cplusplus
//extern "C" {
//#endif

    /*
     *	An octree is expressed as an integer which is either
     *	an index to eight other nodes, the empty tree, or an index
     *	to a set of objects.  If the octree has a value:
     *
     *		> -1:	it is an index to eight other nodes.
     *
     *		-1:	it is empty
     *
     *		< -1:	it is an index to a set of objects
     */
//#ifndef  OCTREE
//#define  OCTREE		int

    public static int isempty(int ot) {
        return ((ot) == EMPTY) ? 1 : 0;
    }

    public static int isfull(int ot) {
        return ((ot) < EMPTY) ? 1 : 0;
    }

    public static int istree(int ot) {
        return ((ot) > EMPTY) ? 1 : 0;
    }

    public static int oseti(int ot) {
        return (-(ot) - 2);	/* object set index */
    }
    private static final int OCTBLKSIZ = 04000;		/* octree block size */


    public static int octbi(int ot) {
        return ((ot) >> 11);	/* octree block index */
    }

    public static int octti(int ot) {
        return (((ot) & 03777) << 3); /* octree index in block */
    }

    public static int octkid(int ot, int br) {
        return (octblock[octbi(ot)][octti(ot) + br]);
    }
//#endif
    public static final int EMPTY = (-1);
//#ifndef  MAXOBLK
//#ifdef  SMLMEM
//#define  MAXOBLK	4095		/* maximum octree block */
//#else
    private static final int MAXOBLK = 32767;		/* maximum octree block */
//#endif
//#endif

    public static int[][] octblock = new int[MAXOBLK][];	/* octree blocks */

    public static int mincusize = -1;
    /*
     *	The cube structure is used to hold an octree and its cubic
     *	boundaries.
     */
    public static class CUBE {

        public FVECT cuorg = new FVECT();			/* the cube origin */

        public double cusize;			/* the cube size */

        public int cutree;			/* the octree for this cube */
        public CUBE() {
            cuorg = new FVECT();
            cusize = -1;
            cutree = OCTREE.EMPTY;
        }
        public CUBE(FVECT fvec, double size, int tree) {
            this.cuorg = fvec; 
            this.cusize = size;
            this.cutree = tree;
        }
    }
    public static CUBE thescene;			/* the main scene */

    /* flags for reading and writing octrees */
    public static int IO_CHECK = 0;		/* verify file type */

    public static int IO_INFO = 01;		/* information header */

    public static int IO_SCENE = 02;		/* objects */

    public static int IO_TREE = 04;		/* octree */

    public static int IO_FILES = 010;		/* object file names */

    public static int IO_BOUNDS = 020;		/* octree boundary */

    public static int IO_ALL = (~0);		/* everything */
    /* octree format identifier */

    public static String OCTFMT = "Radiance_octree";
    /* magic number for octree files */
    static int MAXOBJSIZ = 8;		/* maximum sizeof(OBJECT) */

    public static int OCTMAGIC = (4 * MAXOBJSIZ + 251);	/* increment first value */
    /* octree node types */

    public static final int OT_EMPTY = 0;
    public static final int OT_FULL = 1;
    public static final int OT_TREE = 2;
    /* return values for surface functions */
    public static int O_MISS = 0;		/* no intersection */

    public static int O_HIT = 1;		/* intersection */

    public static int O_IN = 2;		/* cube contained entirely */

//OCTREE	octalloc(void);
//void	octfree(OCTREE ot);
//void	octdone(void);
//OCTREE	combine(OCTREE ot);
//void	culocate(CUBE *cu, FVECT pt);
//int	incube(CUBE *cu, FVECT pt);
//
//int	readoct(char *fname, int load, CUBE *scene, char *ofn[]);
//
//void	readscene(FILE *fp, int objsiz);
//void	writescene(int firstobj, int nobjs, FILE *fp);
//#ifdef __cplusplus
//}
//#endif
//#endif /* _RAD_OCTREE_H_ */
//#ifndef lint
    static String RCSid = "$Id$";
//#endif
/*
     *  octree.c - routines dealing with octrees and cubes.
     */
//#include "copyright.h"
//#include  "standard.h"
//#include  "octree.h"
//OCTREE  *octblock[MAXOBLK];		/* our octree */
//static OCTREE  ofreelist = new OCTREE(EMPTY);	/* freed octree nodes */
//static OCTREE  treetop = new OCTREE(0);		/* next free node */
    static int ofreelist = EMPTY;	/* freed octree nodes */

    static int treetop = 0;		/* next free node */


    public static int octalloc() {			/* allocate an octree */

        int freet = 0;

        if ((freet = ofreelist) != EMPTY) {
            ofreelist = octkid(freet, 0);
            return (freet);
        }
        freet = treetop;
        if (octti(freet) == 0) {
            //errno = 0;
            if (octbi(freet) >= MAXOBLK) {
                return (EMPTY);
            }
            octblock[octbi(freet)] = new int[OCTBLKSIZ * 8];
            //if ((octblock[octbi(freet)]  = (OCTREE *)malloc(
            //		(unsigned)OCTBLKSIZ*8*sizeof(OCTREE))) == NULL)
            //	return(EMPTY);
        }
        treetop++;
        return (freet);
    }

    void octfree(OCTREE ot) {			/* free an octree */
//	int  i;
//
//	if (istree(ot) == 0)
//		return;
//	for (i = 0; i < 8; i++)
//		octfree(octkid(ot, i));
//	octkid(ot, 0) = ofreelist;
//	ofreelist = ot;
    }

    void octdone() /* free EVERYTHING */ {
//	int	i;
//
//	for (i = 0; i < MAXOBLK; i++) {
//		if (octblock[i] == NULL)
//			break;
//		free((void *)octblock[i]);
//		octblock[i] = NULL;
//	}
//	ofreelist = EMPTY;
//	treetop = 0;
    }

    public static int combine(int ot) {			/* recursively combine nodes */
        int i;
        int ores;

        if (istree(ot) == 0) {	/* not a tree */
            return (ot);
        }
        ores = octblock[octbi(ot)][octti(ot)] = combine(octblock[octbi(ot)][octti(ot)]);
        for (i = 1; i < 8; i++) {
            if ((octblock[octbi(ot)][octti(ot) + i] = combine(octkid(ot, i))) != ores) {
                ores = ot;
            }
        }
        if (istree(ores) == 0) {	/* all were identical leaves */
            octblock[octbi(ot)][octti(ot)] = ofreelist;
            ofreelist = ot;
        }
        return (ores);
    }

    CUBE culocate(CUBE cu, FVECT pt) /* locate point within cube */ {
        int i;
        int branch;

        while (istree(cu.cutree) == 1) {
            cu.cusize *= 0.5;
            branch = 0;
            for (i = 0; i < 3; i++) {
                if (cu.cuorg.data[i] + cu.cusize <= pt.data[i]) {
                    cu.cuorg.data[i] += cu.cusize;
                    branch |= 1 << i;
                }
            }
            cu.cutree = octkid(cu.cutree, branch);
        }
        return cu;
    }

    public static int incube(CUBE cu, FVECT pt) /* determine if a point is inside a cube */ {
        if (cu.cuorg.data[0] > pt.data[0] || pt.data[0] >= cu.cuorg.data[0] + cu.cusize) {
            return (0);
        }
        if (cu.cuorg.data[1] > pt.data[1] || pt.data[1] >= cu.cuorg.data[1] + cu.cusize) {
            return (0);
        }
        if (cu.cuorg.data[2] > pt.data[2] || pt.data[2] >= cu.cuorg.data[2] + cu.cusize) {
            return (0);
        }
        return (1);
    }
}