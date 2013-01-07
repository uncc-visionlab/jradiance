/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

/**
 *
 * @author arwillis
 */
public class CONE extends OBJECT_STRUCTURE {
    /* RCSid $Id$ */
    /*
     *  cone.h - header file for cones (cones, cylinders, rings, cups, tubes).
     *
     *	Storage of arguments in the cone structure is a little strange.
     *  To save space, we use an index into the real arguments of the
     *  object structure through ca.  The indices are for the axis
     *  endpoints and radii:  p0, p1, r0 and r1.
     *
     *     2/12/86
     */
//#ifndef _RAD_CONE_H_
//#define _RAD_CONE_H_
//#ifdef __cplusplus
//extern "C" {
//#endif
    //class CONE extends OBJECT_STRUCTURE {

        public FVECT ad;		/* axis direction vector */

        public double al;		/* axis length */

        public double sl;		/* side length */

        public double[] ca;		/* cone arguments (o->oargs.farg) */

        public MAT4 tm;	/* pointer to transformation matrix */

        public char p0, p1;		/* indices for endpoints */

        public char r0, r1;		/* indices for radii */

    //};

    public static double CO_R0(CONE co, int off) {
        return ((co).ca[(int) ((co).r0) + off]);
    }

    public static double CO_R1(CONE co, int off) {
        return ((co).ca[(int) ((co).r1) + off]);
    }

    public static double CO_P0(CONE co, int off) {
        return ((co).ca[(co).p0 + off]);
    }

    public static double CO_P1(CONE co, int off) {
        return ((co).ca[(co).p1 + off]);
    }

//extern CONE  *getcone(OBJREC *o, int getxf);
//extern void  freecone(OBJREC *o);
//extern void  conexform(CONE *co);
//#ifdef __cplusplus
//}
//#endif
//#endif /* _RAD_CONE_H_ */
//#ifndef lint
//static const char	RCSid[] = "$Id$";
//#endif
/*
     *  cone.c - routines for making cones
     */
//#include "copyright.h"
//
//#include  "standard.h"
//
//#include  "object.h"
//
//#include  "otypes.h"
//
//#include  "cone.h"

    /*
     *     In general, a cone may be any one of a cone, a cylinder, a ring,
     *  a cup (inverted cone), or a tube (inverted cylinder).
     *     Most cones are specified with a starting point and radius and
     *  an ending point and radius.  In the cases of a cylinder or tube,
     *  only one radius is needed.  In the case of a ring, a normal direction
     *  is specified instead of a second endpoint.
     *
     *	mtype (cone|cup) name
     *	0
     *	0
     *	8 P0x P0y P0z P1x P1y P1z R0 R1
     *
     *	mtype (cylinder|tube) name
     *	0
     *	0
     *	7 P0x P0y P0z P1x P1y P1z R
     *
     *	mtype ring name
     *	0
     *	0
     *	8 Px Py Pz Nx Ny Nz R0 R1
     */
    public static CONE getcone( /* get cone structure */
            OBJECT.OBJREC o,
            int getxf) {
        int sgn0, sgn1;
        CONE co = null;
        if (o.os == null) {
            //if ((co = (CONE *)o->os) == NULL) {

            co = new CONE();
//		if (co == null)
//			error(SYSTEM, "out of memory in makecone");

            co.ca = o.oargs.farg;
            /* get radii */
            if ((o.otype == OTYPES.OBJ_CYLINDER) || (o.otype == OTYPES.OBJ_TUBE)) {
                if (o.oargs.nfargs != 7) {
                    return null;//break argcerr;
                }
                if (co.ca[6] < -FVECT.FTINY) {
                    //objerror(o, WARNING, "negative radius");
                    o.otype = (short) ((o.otype == OTYPES.OBJ_CYLINDER)
                            ? OTYPES.OBJ_TUBE : OTYPES.OBJ_CYLINDER);
                    co.ca[6] = -co.ca[6];
                } else if (co.ca[6] <= FVECT.FTINY) {
                    return null;//break raderr;
                }
                co.p0 = 0;
                co.p1 = 3;
                co.r0 = co.r1 = 6;
            } else {
                if (o.oargs.nfargs != 8) {
                    return null;//break argcerr;
                }
                if (co.ca[6] < -FVECT.FTINY) {
                    sgn0 = -1;
                } else if (co.ca[6] > FVECT.FTINY) {
                    sgn0 = 1;
                } else {
                    sgn0 = 0;
                }
                if (co.ca[7] < -FVECT.FTINY) {
                    sgn1 = -1;
                } else if (co.ca[7] > FVECT.FTINY) {
                    sgn1 = 1;
                } else {
                    sgn1 = 0;
                }
                if (sgn0 + sgn1 == 0) {
                    return null;//break raderr;
                }
                if ((sgn0 < 0) | (sgn1 < 0)) {
//				objerror(o, o->otype==OBJ_RING?USER:WARNING,
//					"negative radii");
                    o.otype = (short) ((o.otype == OTYPES.OBJ_CONE)
                            ? OTYPES.OBJ_CUP : OTYPES.OBJ_CONE);
                }
                co.ca[6] = co.ca[6] * sgn0;
                co.ca[7] = co.ca[7] * sgn1;
                if (co.ca[7] - co.ca[6] > FVECT.FTINY) {
                    if (o.otype == OTYPES.OBJ_RING) {
                        co.p0 = co.p1 = 0;
                    } else {
                        co.p0 = 0;
                        co.p1 = 3;
                    }
                    co.r0 = 6;
                    co.r1 = 7;
                } else if (co.ca[6] - co.ca[7] > FVECT.FTINY) {
                    if (o.otype == OTYPES.OBJ_RING) {
                        co.p0 = co.p1 = 0;
                    } else {
                        co.p0 = 3;
                        co.p1 = 0;
                    }
                    co.r0 = 7;
                    co.r1 = 6;
                } else {
                    if (o.otype == OTYPES.OBJ_RING) {
                        return null;//break raderr;
                    }
                    o.otype = (short) ((o.otype == OTYPES.OBJ_CONE)
                            ? OTYPES.OBJ_CYLINDER : OTYPES.OBJ_TUBE);
                    o.oargs.nfargs = 7;
                    co.p0 = 0;
                    co.p1 = 3;
                    co.r0 = co.r1 = 6;
                }
            }
            /* get axis orientation */
            if (o.otype == OTYPES.OBJ_RING) {
                FVECT.VCOPY(co.ad, new FVECT(o.oargs.farg[3], o.oargs.farg[4], o.oargs.farg[5]));
            } else {
                co.ad.data[0] = CO_P1(co, 0) - CO_P0(co, 0);
                co.ad.data[1] = CO_P1(co, 1) - CO_P0(co, 1);
                co.ad.data[2] = CO_P1(co, 2) - CO_P0(co, 2);
            }
            co.al = FVECT.normalize(co.ad);
            if (co.al == 0.0) //			objerror(o, USER, "zero orientation");
            /* compute axis and side lengths */ {
                if (o.otype == OTYPES.OBJ_RING) {
                    co.al = 0.0;
                    co.sl = CO_R1(co, 0) - CO_R0(co, 0);
                } else if ((o.otype == OTYPES.OBJ_CONE) || (o.otype == OTYPES.OBJ_CUP)) {
                    co.sl = co.ca[7] - co.ca[6];
                    co.sl = Math.sqrt(co.sl * co.sl + co.al * co.al);
                } else { /* OBJ_CYLINDER or OBJ_TUBE */
                    co.sl = co.al;
                }
            }
            co.tm = null;
            o.os = co;
        }
//	if (getxf && co->tm == NULL)
//		conexform(co);
        return (co);
//argcerr:
//	objerror(o, USER, "bad # arguments");
//raderr:
//	objerror(o, USER, "illegal radii");
//	return null; /* pro forma return */
    }

    void freecone( /* free memory associated with cone */
            OBJECT.OBJREC o) {
//	register CONE  *co = (CONE *)o->os;
//
//	if (co == NULL)
//		return;
//	if (co->tm != NULL)
//		free((void *)co->tm);
//	free((void *)co);
//	o->os = NULL;
    }

    void conexform( /* get cone transformation matrix */
            CONE co) {
//	MAT4  m4;
//	double  d;
//	int  i;
//
//	co->tm = (RREAL (*)[4])malloc(sizeof(MAT4));
//	if (co->tm == NULL)
//		error(SYSTEM, "out of memory in conexform");
//
//				/* translate to origin */
//	setident4(co->tm);
//	if (co->r0 == co->r1)
//		d = 0.0;
//	else
//		d = CO_R0(co) / (CO_R1(co) - CO_R0(co));
//	for (i = 0; i < 3; i++)
//		co->tm[3][i] = d*(CO_P1(co)[i] - CO_P0(co)[i])
//				- CO_P0(co)[i];
//	
//				/* rotate to positive z-axis */
//	setident4(m4);
//	d = co->ad[1]*co->ad[1] + co->ad[2]*co->ad[2];
//	if (d <= FTINY*FTINY) {
//		m4[0][0] = 0.0;
//		m4[0][2] = co->ad[0];
//		m4[2][0] = -co->ad[0];
//		m4[2][2] = 0.0;
//	} else {
//		d = sqrt(d);
//		m4[0][0] = d;
//		m4[1][0] = -co->ad[0]*co->ad[1]/d;
//		m4[2][0] = -co->ad[0]*co->ad[2]/d;
//		m4[1][1] = co->ad[2]/d;
//		m4[2][1] = -co->ad[1]/d;
//		m4[0][2] = co->ad[0];
//		m4[1][2] = co->ad[1];
//		m4[2][2] = co->ad[2];
//	}
//	multmat4(co->tm, co->tm, m4);
//
//				/* scale z-axis */
//	if ((co->p0 != co->p1) & (co->r0 != co->r1)) {
//		setident4(m4);
//		m4[2][2] = (CO_R1(co) - CO_R0(co)) / co->al;
//		multmat4(co->tm, co->tm, m4);
//	}
    }
//#ifndef lint
//static String	RCSid = "$Id$";
//#endif

    /*
     *  o_cone.c - routines for intersecting cubes with cones.
     *
     *     2/3/86
     */
//#include  "standard.h"
//#include  "octree.h"
//#include  "object.h"
//#include  "cone.h"
//#include  "plocate.h"
    public static double ROOT3 = 1.732050808;

    /*
     *     The algorithm used to detect cube intersection with cones is
     *  recursive.  First, we approximate the cube to be a sphere.  Then
     *  we test for cone intersection with the sphere by testing the
     *  segment of the cone which is nearest the sphere's center.
     *     If the cone has points within the cube's bounding sphere,
     *  we must check for intersection with the cube.  This is done with
     *  the 3D line clipper.  The same cone segment is used in this test.
     *  If the clip fails, we still cannot be sure there is no intersection,
     *  so we subdivide the cube and recurse.
     *     If none of the sub-cubes intersect, then our cube does not intersect.
     */
//extern double  mincusize;		/* minimum cube size */
//static int findcseg(FVECT ep0, FVECT ep1, CONE *co, FVECT p);
    public int octree_function(Object ... objs) 
            //{ return OCTREE.O_MISS; }
//    int o_cone( /* determine if cone intersects cube */
//            OBJECT.OBJREC o,
//            OCTREE.CUBE cu) 
    {
        OBJECT.OBJREC o = (OBJECT.OBJREC) objs[0];
        OCTREE.CUBE cu = (OCTREE.CUBE) objs[1];
        CONE co;
        FVECT ep0 = new FVECT(), ep1 = new FVECT();
//#ifdef STRICT
        FVECT cumin = new FVECT(), cumax = new FVECT();
        OCTREE.CUBE cukid = new OCTREE.CUBE();
        int j;
//#endif
        double r;
        FVECT p = new FVECT();
        int i;
        /* get cone arguments */
        co = getcone(o, 0);
        /* get cube center */
        r = cu.cusize * 0.5;
        for (i = 0; i < 3; i++) {
            p.data[i] = cu.cuorg.data[i] + r;
        }
        r *= ROOT3;			/* bounding radius for cube */

        if (findcseg(ep0, ep1, co, p) != 0) {
            /* check min. distance to cone */
            if (FVECT.dist2lseg(p, ep0, ep1) > (r + FVECT.FTINY) * (r + FVECT.FTINY)) {
                return (OCTREE.O_MISS);
            }
//#ifdef  STRICT
					/* get cube boundaries */
            for (i = 0; i < 3; i++) {
                cumax.data[i] = (cumin.data[i] = cu.cuorg.data[i]) + cu.cusize;
            }
            /* closest segment intersects? */
            if (CLIP.clip(ep0, ep1, cumin, cumax) != 0) {
                return (OCTREE.O_HIT);
            }
        }
        /* check sub-cubes */
        cukid.cusize = cu.cusize * 0.5;
        if (cukid.cusize < OCTREE.mincusize) {
            return (OCTREE.O_HIT);		/* cube too small */
        }
        cukid.cutree = OCTREE.EMPTY;

        for (j = 0; j < 8; j++) {
            for (i = 0; i < 3; i++) {
                cukid.cuorg.data[i] = cu.cuorg.data[i];
                if ((1 << i & j) != 0) {
                    cukid.cuorg.data[i] += cukid.cusize;
                }
            }
//            if (o_cone(o, cukid) != 0) {
            if (octree_function(o, cukid) != 0) {
                return (OCTREE.O_HIT);	/* sub-cube intersects */
            }
        }
        return (OCTREE.O_MISS);			/* no intersection */
//#else
//	}
//	return(O_HIT);			/* assume intersection */
//#endif
    }

    static int findcseg( /* find line segment from cone closest to p */
            FVECT ep0,
            FVECT ep1,
            CONE co,
            FVECT p) {
        double d;
        FVECT v = new FVECT();
        int i;
        /* find direction from axis to point */
        FVECT.VSUB(v, p, new FVECT(CO_P0(co, 0), CO_P0(co, 1), CO_P0(co, 2)));
        d = FVECT.DOT(v, co.ad);
        for (i = 0; i < 3; i++) {
            v.data[i] -= d * co.ad.data[i];
        }
        if (FVECT.normalize(v) == 0.0) {
            return (0);
        }
        /* find endpoints of segment */
        for (i = 0; i < 3; i++) {
            ep0.data[i] = CO_R0(co, 0) * v.data[i] + CO_P0(co, i);
            ep1.data[i] = CO_R1(co, 0) * v.data[i] + CO_P1(co, i);
        }
        return (1);			/* return distance from axis */
    }
}
