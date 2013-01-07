/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import jradiance.common.OCTREE.CUBE;

/**
 *
 * @author arwillis
 */
public class READOCT {
    /*
     *  readoct.c - routines to read octree information.
     */

    static String infn;			/* input file specification */

    static InputStream infp;			/* input file stream */

    static int objsize;			/* size of stored OBJECT's */

    static int objorig;			/* zeroeth object */

    static int fnobjects;		/* number of objects in this file */


    public static int readoct( /* read in octree file or stream */
            String inpspec,
            int load,
            CUBE scene,
            String[] ofn) throws FileNotFoundException, IOException {
        char[] sbuf = new char[512];
        int nf;
        int i;
        long m;

        if (inpspec == null) {
            infn = "standard input";
            infp = System.in;
        } else if (inpspec.charAt(0) == '!') {
            infn = inpspec;
//		if ((infp = popen(inpspec+1, "r")) == NULL) {
//			sprintf(errmsg, "cannot execute \"%s\"", inpspec);
//			error(SYSTEM, errmsg);
//		}
        } else {
            infn = inpspec;
            if ((infp = new FileInputStream(inpspec)) == null) {
//			sprintf(errmsg, "cannot open octree file \"%s\"",
//					inpspec);
//			error(SYSTEM, errmsg);
            }
        }
//	SET_FILE_BINARY(infp);
					/* get header */
        if (HEADER.checkheader(infp, OCTREE.OCTFMT.toCharArray(), (load & OCTREE.IO_INFO) != 0 ? System.out : null) < 0) {
//		octerror(USER, "not an octree");
        }
        int sizeofLONG = Long.SIZE / 8;				/* check format */
        if ((objsize = (int) ogetint(2) - OCTREE.OCTMAGIC) <= 0
                || objsize > OCTREE.MAXOBJSIZ || objsize > sizeofLONG) {
//		octerror(USER, "incompatible octree format");
        }
//					/* get boundaries */
        if ((load & OCTREE.IO_BOUNDS) != 0) {
            for (i = 0; i < 3; i++) {
                scene.cuorg.data[i] = Float.parseFloat(ogetstr(sbuf));
            }
            scene.cusize = Float.parseFloat(ogetstr(sbuf));
        } else {
            for (i = 0; i < 4; i++) {
                ogetstr(sbuf);
            }
        }
        objorig = OBJECT.nobjects;		/* set object offset */
        nf = 0;				/* get object files */
//	while (ogetstr(sbuf).charAt(0) != 0) {
        while (ogetstr(sbuf).length() > 0) {
            if ((load & OCTREE.IO_SCENE) != 0) {
                OBJECT.readobj(new String(sbuf));
            }
            if ((load & OCTREE.IO_FILES) != 0) {
                ofn[nf] = SAVQSTR.savqstr(new String(sbuf));
            }
            nf++;
        }
        if ((load & OCTREE.IO_FILES) != 0) {
            ofn[nf] = null;
        }
        /* get number of objects */
        m = ogetint(objsize);
        fnobjects = (int) m;
        if (fnobjects != m) {
//		octerror(USER, "too many objects");
        }
        if ((load & OCTREE.IO_TREE) != 0) {		/* get the octree */
            scene.cutree = gettree();
        } else if ((load & OCTREE.IO_SCENE) != 0 && nf == 0) {
            skiptree();
        }
        if ((load & OCTREE.IO_SCENE) != 0) {		/* get the scene */
            if (nf == 0) {
                /* load binary scene data */
                SCENEIO.readscene(infp, objsize);

            } else {			/* consistency checks */
                /* check object count */
                if (OBJECT.nobjects != objorig + fnobjects) {
//			octerror(USER, "bad object count; octree stale?");
                }
                /* check for non-surfaces */
                if (nonsurfintree(scene.cutree) != 0) {
//			octerror(USER, "modifier in tree; octree stale?");
                }
            }
        }
        /* close the input */
        if (infn.charAt(0) == '!') {
            infp.close();
        } else {
            infp.close();
        }
        return (nf);
    }

    static String ogetstr(char[] s) throws IOException /* get null-terminated string */ {

        return PORTIO.getstr(s, infp);
//	if (PORTIO.getstr(s, infp) == null) {
//		octerror(USER, "truncated octree");
//        }
//	return(s);
    }

    static int getfullnode() throws IOException /* get a set, return fullnode */ {
        int[] set = new int[OBJECT.MAXSET + 1];
        int i;
        long m;

        if ((set[0] = (int) ogetint(objsize)) > OBJECT.MAXSET) {
//		octerror(USER, "bad set in getfullnode");
        }
        for (i = 1; i <= set[0]; i++) {
            m = ogetint(objsize) + objorig;
            if ((set[i] = (int) m) != m) {
//			octerror(USER, "too many objects");
            }
        }
        return (OBJSET.fullnode(set));
    }

    static long ogetint(int siz) throws IOException /* get a siz-byte integer */ {
        long r;

        r = PORTIO.getint(siz, infp);
        if ((infp.available() == 0)) {
//		octerror(USER, "truncated octree");
        }
        return (r);
    }

    static double ogetflt() throws IOException /* get a floating point number */ {
        double r;

        r = PORTIO.getflt(infp);
        if ((infp.available()) == 0) {
//		octerror(USER, "truncated octree");
        }
        return (r);
    }

    static int gettree() throws IOException /* get a pre-ordered octree */ {
        int ot;
        int i;

        switch (infp.read()) {
            case OCTREE.OT_EMPTY:
                return (OCTREE.EMPTY);
            case OCTREE.OT_FULL:
                return (getfullnode());
            case OCTREE.OT_TREE:
                if ((ot = OCTREE.octalloc()) == OCTREE.EMPTY) {
//			octerror(SYSTEM, "out of tree space in gettree");
                }
                for (i = 0; i < 8; i++) {
//			OCTREE.octkid(ot, i) = gettree();
                    OCTREE.octblock[OCTREE.octbi(ot)][OCTREE.octti(ot) + i] = gettree();
                }
                return (ot);
//	case EOF:
//		octerror(USER, "truncated octree");
            default:
//		octerror(USER, "damaged octree");
        }
        return OCTREE.EMPTY; /* pro forma return */
    }

    static int nonsurfintree(int ot) /* check tree for modifiers */ {
        int[] set = new int[OBJECT.MAXSET + 1];
        int i;

        if (OCTREE.isempty(ot) != 0) {
            return (0);
        }
        if (OCTREE.istree(ot) != 0) {
            for (i = 0; i < 8; i++) {
                if (nonsurfintree(OCTREE.octkid(ot, i)) != 0) {
                    return (1);
                }
            }
            return (0);
        }
        OBJSET.objset(set, ot);
        for (i = set[0]; i > 0; i--) {
            if (OTYPES.ismodifier(OBJECT.objptr(set[i]).otype) != 0) {
                return (1);
            }
        }
        return (0);
    }

    static void skiptree() throws IOException /* skip octree on input */ {
        int i;

        switch (infp.read()) {
            case OCTREE.OT_EMPTY:
                return;
            case OCTREE.OT_FULL:
                for (i = (int) ogetint(objsize) * objsize; i-- > 0;) {
                    infp.read();
                    if (infp.available() == 0) {
//				octerror(USER, "truncated octree");
                    }
                }
                return;
            case OCTREE.OT_TREE:
                for (i = 0; i < 8; i++) {
                    skiptree();
                }
                return;
//	case EOF:
//		octerror(USER, "truncated octree");
            default:
//		octerror(USER, "damaged octree");
        }
    }

//
    static void octerror(int etyp, char[] msg) /* octree error */ {
//	char  msgbuf[128];
//
//	sprintf(msgbuf, "(%s): %s", infn, msg);
//	error(etyp, msgbuf);
    }
}
