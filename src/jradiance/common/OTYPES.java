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

import jradiance.common.OBJECT.OBJREC;

/**
 *
 * @author arwillis
 */
public class OTYPES {
    /*
     *  otypes.h - defines for object types.
     */

//    public abstract class octree_function {
//        abstract public int function(Object ... objs);        
//    }
    public static class FUN {
        public String funame;			/* function name */
        public int flags;			/* type flags */
//#ifdef FUN_ARGLIST
        //int  (*funp)(FUN_ARGLIST);	/* pointer to function */
//#else
        //int  (*funp)();			/* pointer to function */
        public OBJECT_STRUCTURE ofunc; /* pointer to function octree_function */
        public FUN(String name, int flags, OBJECT_STRUCTURE ofunc) {
            this.funame = name;
            this.flags = flags;
            this.ofunc = ofunc;
        }
        public void setOctreeFunction(OBJECT_STRUCTURE ofunc) {
            this.ofunc = ofunc;
        }
//#endif
    }


    /* object types in decreasing frequency */
    public static final int OBJ_FACE = 0;		/* polygon */

    public static final int OBJ_CONE = 1;		/* cone */

    public static final int OBJ_SPHERE = 2;		/* sphere */

    public static final int TEX_FUNC = 3;		/* surface texture function */

    public static final int OBJ_RING = 4;		/* disk */

    public static final int OBJ_CYLINDER = 5;		/* cylinder */

    public static final int OBJ_INSTANCE = 6;		/* octree instance */

    public static final int OBJ_CUP = 7;		/* inverted cone */

    public static final int OBJ_BUBBLE = 8;		/* inverted sphere */

    public static final int OBJ_TUBE = 9;		/* inverted cylinder */

    public static final int OBJ_MESH = 10;		/* mesh instance */

    public static final int MOD_ALIAS = 11;		/* modifier alias */

    public static final int MAT_PLASTIC = 12;		/* plastic surface */

    public static final int MAT_METAL = 13;		/* metal surface */

    public static final int MAT_GLASS = 14;		/* thin glass surface */

    public static final int MAT_TRANS = 15;		/* translucent material */

    public static final int MAT_DIELECTRIC = 16;		/* dielectric material */

    public static final int MAT_PLASTIC2 = 17;		/* anisotropic plastic */

    public static final int MAT_METAL2 = 18;		/* anisotropic metal */

    public static final int MAT_TRANS2 = 19;		/* anisotropic translucent material */

    public static final int MAT_INTERFACE = 20;		/* dielectric interface */

    public static final int MAT_PFUNC = 21;		/* plastic brdf function */

    public static final int MAT_MFUNC = 22;		/* metal brdf function */

    public static final int PAT_BFUNC = 23;		/* brightness function */

    public static final int PAT_BDATA = 24;		/* brightness data */

    public static final int PAT_BTEXT = 25;		/* monochromatic text */

    public static final int PAT_CPICT = 26;		/* color picture */

    public static final int MAT_GLOW = 27;		/* proximity light source */

    public static final int OBJ_SOURCE = 28;		/* distant source */

    public static final int MAT_LIGHT = 29;		/* primary light source */

    public static final int MAT_ILLUM = 30;		/* secondary light source */

    public static final int MAT_SPOT = 31;		/* spot light source */

    public static final int MAT_MIST = 32;		/* mist medium */

    public static final int MAT_MIRROR = 33;		/* mirror (secondary source) */

    public static final int MAT_TFUNC = 34;		/* trans brdf function */

    public static final int MAT_BRTDF = 35;		/* BRTD function */

    public static final int MAT_BSDF = 36;		/* BSDF data file */

    public static final int MAT_PDATA = 37;		/* plastic brdf data */

    public static final int MAT_MDATA = 38;		/* metal brdf data */

    public static final int MAT_TDATA = 39;		/* trans brdf data */

    public static final int PAT_CFUNC = 40;		/* color function */

    public static final int MAT_CLIP = 41;		/* clipping surface */

    public static final int PAT_CDATA = 42;		/* color data */

    public static final int PAT_CTEXT = 43;		/* colored text */

    public static final int TEX_DATA = 44;		/* surface texture data */

    public static final int MIX_FUNC = 45;		/* mixing function */

    public static final int MIX_DATA = 46;		/* mixing data */

    public static final int MIX_TEXT = 47;		/* mixing text */

    public static final int MIX_PICT = 48;		/* mixing picture */

    public static final int MAT_DIRECT1 = 49;		/* unidirecting material */

    public static final int MAT_DIRECT2 = 50;		/* bidirecting material */
    /* number of object types */

    public static final int NUMOTYPE = 51;
    /* type flags */
    public static final int T_S = 01;		/* surface (object) */

    public static final int T_M = 02;		/* material */

    public static final int T_P = 04;		/* pattern */

    public static final int T_T = 010;		/* texture */

    public static final int T_X = 020;		/* mixture */

    public static final int T_V = 040;		/* volume */

    public static final int T_L = 0100;		/* light source modifier */

    public static final int T_LV = 0200;		/* virtual light source modifier */

    public static final int T_F = 0400;		/* function */

    public static final int T_D = 01000;		/* data */

    public static final int T_I = 02000;		/* picture */

    public static final int T_E = 04000;		/* text */
    /* user-defined types */

    public static final int T_SP1 = 010000;
    public static final int T_SP2 = 020000;
    public static final int T_SP3 = 040000;
//extern FUN  ofun[];			/* our type list */
    public static FUN[] ofun = new FUN[NUMOTYPE];

    public static int issurface(int t) {
        return (ofun[t].flags & T_S);
    }

    public static int isvolume(int t) {
        return (ofun[t].flags & T_V);
    }

    public static int ismodifier(int t) {
        return (((ofun[t].flags & (T_S | T_V)) == 0) ? 1 : 0);
    }

    public static int ismaterial(int t) {
        return (ofun[t].flags & T_M);
    }

    public static int ispattern(int t) {
        return (ofun[t].flags & T_P);
    }

    public static int istexture(int t) {
        return (ofun[t].flags & T_T);
    }

    public static int ismixture(int t) {
        return (ofun[t].flags & T_X);
    }

    public static int islight(int t) {
        return (ofun[t].flags & T_L);
    }

    public static int isvlight(int t) {
        return (ofun[t].flags & T_LV);
    }

    public static int hasdata(int t) {
        return (ofun[t].flags & (T_D | T_I));
    }

    public static int hasfunc(int t) {
        return (ofun[t].flags & (T_F | T_D | T_I));
    }

    public static int hastext(int t) {
        return (ofun[t].flags & T_E);
    }

    public static int isflat(int t) {
        return ((((t) == OBJ_FACE || (t) == OBJ_RING) == true) ? 1 : 0);
    }
    public static final String ALIASKEY = "alias";			/* alias keyword */

    public static final String ALIASMOD = "inherit";		/* inherit target modifier */

    /* type list initialization */

    /*
     * Object type lookup and error reporting
     *
     *  External symbols declared in object.h
     */

    public static int otype( /* get object function number from its name */
            String ofname) {
        int i;

        for (i = 0; i < NUMOTYPE; i++) {
            if (ofun[i].funame.equals(ofname)) {
                return (i);
            }
        }

        return (-1);		/* not found */
    }

    void objerror( /* report error related to object */
            OBJREC o,
            int etyp,
            char[] msg) {
        char[] msgbuf = new char[512];

//	sprintf(msgbuf, "%s for %s \"%s\"",
//			msg, ofun[o->otype].funame,
//			o->oname!=NULL ? o->oname : "(NULL)");
//	error(etyp, msgbuf);
    }

    /*
     * Initialize ofun[] list for octree generator
     */

//extern int  o_sphere(); /* XXX way too much linker magic involved here */ 
//extern int  o_face();
//extern int  o_cone();
//extern int  o_instance();
//extern int  o_mesh();
//FUN  ofun[NUMOTYPE] = INIT_OTYPE;
    public static void ot_initotypes(OBJECT_STRUCTURE od) /* initialize ofun array */ {
//    static {
//        o_default od = new o_default();
        ofun[0] = new FUN("polygon", T_S, od);
        ofun[1] = new FUN("cone", T_S, od);
        ofun[2] = new FUN("sphere", T_S, od);
        ofun[3] = new FUN("texfunc", T_T | T_F, od);
        ofun[4] = new FUN("ring", T_S, od);
        ofun[5] = new FUN("cylinder", T_S, od);
        ofun[6] = new FUN("instance", T_V, od);
        ofun[7] = new FUN("cup", T_S, od);
        ofun[8] = new FUN("bubble", T_S, od);
        ofun[9] = new FUN("tube", T_S, od);
        ofun[10] = new FUN("mesh", T_V, od);
        ofun[11] = new FUN(ALIASKEY, 0, od);
        ofun[12] = new FUN("plastic", T_M, od);
        ofun[13] = new FUN("metal", T_M, od);
        ofun[14] = new FUN("glass", T_M, od);
        ofun[15] = new FUN("trans", T_M, od);
        ofun[16] = new FUN("dielectric", T_M, od);
        ofun[17] = new FUN("plastic2", T_M | T_F, od);
        ofun[18] = new FUN("metal2", T_M | T_F, od);
        ofun[19] = new FUN("trans2", T_M | T_F, od);
        ofun[20] = new FUN("interface", T_M, od);
        ofun[21] = new FUN("plasfunc", T_M | T_F, od);
        ofun[22] = new FUN("metfunc", T_M | T_F, od);
        ofun[23] = new FUN("brightfunc", T_P | T_F, od);
        ofun[24] = new FUN("brightdata", T_P | T_D, od);
        ofun[25] = new FUN("brighttext", T_P | T_E, od);
        ofun[26] = new FUN("colorpict", T_P | T_I, od);
        ofun[27] = new FUN("glow", T_M | T_L, od);
        ofun[28] = new FUN("source", T_S, od);
        ofun[29] = new FUN("light", T_M | T_L, od);
        ofun[30] = new FUN("illum", T_M | T_L, od);
        ofun[31] = new FUN("spotlight", T_M | T_L, od);
        ofun[32] = new FUN("mist", T_M, od);
        ofun[33] = new FUN("mirror", T_M | T_LV, od);
        ofun[34] = new FUN("transfunc", T_M | T_F, od);
        ofun[35] = new FUN("BRTDfunc", T_M | T_F, od);
        ofun[36] = new FUN("BSDF", T_M | T_D, od);
        ofun[37] = new FUN("plasdata", T_M | T_D, od);
        ofun[38] = new FUN("metdata", T_M | T_D, od);
        ofun[39] = new FUN("transdata", T_M | T_D, od);
        ofun[40] = new FUN("colorfunc", T_P | T_F, od);
        ofun[41] = new FUN("antimatter", T_M, od);
        ofun[42] = new FUN("colordata", T_P | T_D, od);
        ofun[43] = new FUN("colortext", T_P | T_E, od);
        ofun[44] = new FUN("texdata", T_T | T_D, od);
        ofun[45] = new FUN("mixfunc", T_X | T_F, od);
        ofun[46] = new FUN("mixdata", T_X | T_D, od);
        ofun[47] = new FUN("mixtext", T_X | T_E, od);
        ofun[48] = new FUN("mixpict", T_X | T_I, od);
        ofun[49] = new FUN("prism1", T_M | T_F | T_LV, od);
        ofun[50] = new FUN("prism2", T_M | T_F | T_LV, od);
    }
    public static void initotypes_octree() {
        SPHERE s = new SPHERE();
        ofun[OBJ_SPHERE].setOctreeFunction(s);
	ofun[OBJ_BUBBLE].setOctreeFunction(s);
        FACE f = new FACE();
	ofun[OBJ_FACE].setOctreeFunction(f);
        CONE c = new CONE();
	ofun[OBJ_CONE].setOctreeFunction(c);
	ofun[OBJ_CUP].setOctreeFunction(c);
	ofun[OBJ_CYLINDER].setOctreeFunction(c);
	ofun[OBJ_TUBE].setOctreeFunction(c);
	ofun[OBJ_RING].setOctreeFunction(c);
        INSTANCE i = new INSTANCE();
	ofun[OBJ_INSTANCE].setOctreeFunction(i);
        MESH m = new MESH();
	ofun[OBJ_MESH].setOctreeFunction(m);
    }

    public static class o_default extends OBJECT_STRUCTURE { /* default action is no intersection */ 
        
        @Override
        public int octree_function(Object ... objs) /* default action is no intersection */ {
            return (OCTREE.O_MISS);
        }
    }
}