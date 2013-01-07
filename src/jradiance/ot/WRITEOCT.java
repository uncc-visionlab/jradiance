/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.ot;

import java.io.IOException;
import java.io.PrintStream;
import jradiance.common.OBJECT;
import jradiance.common.OBJSET;
import jradiance.common.OCTREE;
import jradiance.common.PORTIO;
import jradiance.common.SCENEIO;

/**
 *
 * @author arwillis
 */
public class WRITEOCT {
//    #ifndef lint
//static const char RCSid[] = "$Id$";
//#endif
/*
     *  WRITEOCT.c - routines for writing octree information to stdout.
     *
     *     7/30/85
     */

//#include  "standard.h"
//
//#include  "octree.h"
//#include  "object.h"
//#include  "OCONV.h"
//
//#ifdef putc_unlocked		/* avoid horrendous overhead of flockfile */
//#undef putc
//#define putc    putc_unlocked
//#endif
//static void oputstr(char *s);
//static void putfullnode(OCTREE fn);
//static void oputint(long i, int siz);
//static void oputflt(double f);
//static void puttree(OCTREE ot);
    PrintStream ostream = null;

    public WRITEOCT(PrintStream os) {
        if (os == null) {
            ostream = System.out;
        } else {
            this.ostream = os;
        }
    }

    public void writeoct( /* write octree structures to stdout */
            int store,
            OCTREE.CUBE scene,
            String[] ofn) throws IOException {
//        char[] sbuf = new char[64];
        int i;
        int sizeofINT = (Integer.SIZE / 8);					/* write format number */
        oputint((long) (OCTREE.OCTMAGIC + sizeofINT), 2);

        if ((store & OCTREE.IO_BOUNDS) == 0) {
            return;
        }
        /* write boundaries */
        if (!OCONV.TEST_JAVA) {
            for (i = 0; i < 3; i++) {
                oputstr(String.format("%.12g", scene.cuorg.data[i]));
            }
            oputstr(String.format("%.12g", scene.cusize));
        }
        /* write object file names */
        if ((store & OCTREE.IO_FILES) != 0) {
            for (i = 0; ofn[i] != null; i++) {
                oputstr(ofn[i]);
            }
        }
        oputstr("\0");
        /* write number of objects */
        oputint((long) OBJECT.nobjects, sizeofINT);

        if ((store & OCTREE.IO_TREE) == 0) {
            return;
        }
        /* write the octree */
        puttree(scene.cutree);
        ostream.flush();
        if ((store & OCTREE.IO_FILES) != 0 || (store & OCTREE.IO_SCENE) == 0) {
            return;
        }
        /* write the scene */
        SCENEIO.writescene(0, OBJECT.nobjects, ostream);
        ostream.flush();
    }

    void oputstr( /* write null-terminated string to stdout */
            String s) throws IOException {
        PORTIO.putstr(s, ostream);
        if (ostream.checkError()) {
//		error(SYSTEM, "write error in putstr");
        }
    }

    void putfullnode( /* write out a full node */
            int fn) throws IOException {
        int[] oset = new int[OBJECT.MAXSET + 1];
        int i;
        int sizeofINT = (Integer.SIZE / 8);					/* write format number */

        OBJSET.objset(oset, fn);
        for (i = 0; i <= oset[0]; i++) {
            oputint((long) oset[i], sizeofINT);
        }
    }

    void oputint( /* write a siz-byte integer to stdout */
            long i,
            int siz) throws IOException {
        PORTIO.putint(i, siz, ostream);
        if (ostream.checkError()) {
//		error(SYSTEM, "write error in putint");
        }
    }

    void oputflt( /* put out floating point number */
            double f) throws IOException {
        PORTIO.putflt(f, ostream);
        if (ostream.checkError()) {
//		error(SYSTEM, "write error in putflt");
        }
    }

    void puttree( /* write octree to stdout in pre-order form */
            int ot) throws IOException {
        int i;

        if (OCTREE.istree(ot) != 0) {
            ostream.write(OCTREE.OT_TREE);		/* indicate tree */
            for (i = 0; i < 8; i++) {	/* write tree */
                puttree(OCTREE.octkid(ot, i));
            }
        } else if (OCTREE.isfull(ot) != 0) {
            ostream.write(OCTREE.OT_FULL);		/* indicate fullnode */
            putfullnode(ot);		/* write fullnode */
        } else {
            ostream.write(OCTREE.OT_EMPTY);		/* indicate empty */
        }
    }
}
