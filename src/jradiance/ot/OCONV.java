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
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import jradiance.common.FIXARGV0;
import jradiance.common.FVECT;
import jradiance.common.HEADER;
import jradiance.common.OBJECT;
import jradiance.common.OBJSET;
import jradiance.common.OCTREE;
import jradiance.common.OCTREE.CUBE;
import jradiance.common.OTYPES;
import jradiance.common.OTYPES.o_default;

/**
 *
 * @author arwillis
 */
public class OCONV {

    /*
     *  OCONV.c - main program for object to octree conversion.
     *
     *     7/29/85
     */
    public static boolean IARGS = false;
    public static double OMARGIN = (10 * FVECT.FTINY);	/* margin around global cube */

    private static int MAXOBJFIL = 127;		/* maximum number of scene files */

    static String progname;			/* argv[0] */

    static int nowarn = 0;			/* supress warnings? */

    static int objlim = 6;			/* # of objects before split */

    public static int resolu = 16384;			/* octree resolution limit */

    static String[] ofname = new String[MAXOBJFIL + 1];		/* object file names */

    static int nfiles = 0;			/* number of object files */

    public static double mincusize;			/* minimum cube size from resolu */

    public static final boolean TEST_JAVA = false;
    //void  (*addobjnotify[])() = {NULL};	/* new object notifier functions */
//static void addobject(CUBE  *cu, OBJECT	obj);
//static void add2full(CUBE  *cu, OBJECT	obj, int  inc);

    public static class myPrintStream extends PrintStream {

        byte[] buf = new byte[16];
        long streampos;
        int bufpos;

        public myPrintStream(OutputStream os) {
            super(os);
            streampos = bufpos = 0;
        }

        @Override
        public void write(int b) {
            try {
                buf[bufpos++] = (byte) b;
                streampos++;
                if (bufpos == buf.length) {
                    bufpos = 0;
                    hexdump(buf, out);
                }
//                out.write(String.format("0x%x ", b).getBytes());
//                out.write(Character.toString((char)b).getBytes());
            } catch (IOException ex) {
                Logger.getLogger(OCONV.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        void hexdump(byte[] b, OutputStream out) throws IOException {
            out.write(String.format("%08x", streampos - 16).getBytes());
            for (int i = 0; i < buf.length; i++) {
                if (i % 8 == 0) {
                    out.write(' ');
                }
                out.write(' ');
                out.write(String.format("%02x", b[i]).getBytes());
            }
            out.write(' ');
            out.write(' ');
            out.write('|');
            for (int i = 0; i < buf.length; i++) {
                if (Character.isISOControl((char) buf[i]) || ((byte) buf[i]) < 0) {
                    out.write('.');
                } else {
                    out.write(buf[i]);
                }
            }
            out.write('|');
            out.write('\n');
            out.flush();
        }

        @Override
        public void write(byte[] b, int off, int len) {
            for (int i = off; i < off + len; i++) {
                write(b[i]);
            }
        }
    }
    static PrintStream ostream = System.out;
//    static myPrintStream ostream = new myPrintStream(System.out);

    public static void main( /* convert object files to an octree */
            String[] argv) {
        int argc = argv.length;
        FVECT bbmin = new FVECT(), bbmax = new FVECT();
        String infile = null;
        int inpfrozen = 0;
        int outflags = OCTREE.IO_ALL;
        int startobj;
        int i = 0;

        try {
            OCTREE.thescene = new CUBE(new FVECT(0.0, 0.0, 0.0), 0.0, OCTREE.EMPTY);		/* our scene */
            while (argv[i].charAt(0) != '-') {
                i++;
            }
            String[] argv2 = new String[argv.length - i + 1];
            for (int j = 1; j < argv2.length; j++) {
                argv2[j] = argv[i++];
            }
            argv2[0] = "oconv";
            argv = argv2;
            argc = argv2.length;
            progname = FIXARGV0.fixargv0(argv[0]);

            OTYPES.ot_initotypes(new o_default());
            OTYPES.initotypes_octree();

            for (i = 1; i < argc && argv[i].charAt(0) == '-'; i++) {
                switch (argv[i].charAt(1)) {
                    case '\0':				/* scene from stdin */
                        //goto breakopt;
                        break;
                    case 'i':				/* input octree */
                        infile = argv[++i];
                        break;
                    case 'b':				/* bounding cube */
                        OCTREE.thescene.cuorg.data[0] = Double.parseDouble(argv[++i]) - OMARGIN;
                        OCTREE.thescene.cuorg.data[1] = Double.parseDouble(argv[++i]) - OMARGIN;
                        OCTREE.thescene.cuorg.data[2] = Double.parseDouble(argv[++i]) - OMARGIN;
                        OCTREE.thescene.cusize = Double.parseDouble(argv[++i]) + 2 * OMARGIN;
                        break;
                    case 'n':				/* set limit */
                        objlim = Integer.parseInt(argv[++i]);
                        break;
                    case 'r':				/* resolution limit */
                        resolu = Integer.parseInt(argv[++i]);
                        break;
                    case 'f':				/* freeze octree */
                        outflags &= ~OCTREE.IO_FILES;
                        break;
                    case 'w':				/* supress warnings */
                        nowarn = 1;
                        break;
                    default:
//                        sprintf(errmsg, "unknown option: '%s'", argv[i]);
//                        error(USER, errmsg);
                        break;
                }
            }
//breakopt:
            //SET_FILE_BINARY(stdout);
            //PrintStream stdout = System.out;
            PrintStream stdout = ostream;
            if (infile != null) {		/* get old octree & objects */
                if (OCTREE.thescene.cusize > FVECT.FTINY) {
                    //error(USER, "only one of '-b' or '-i'");
                }
                //nfiles = readoct(infile, IO_ALL, &thescene, ofname);
                if (nfiles == 0) {
                    inpfrozen++;
                }
            } else {
                HEADER.newheader("RADIANCE", stdout);	/* new binary file header */
            }
            if (!TEST_JAVA) {
                HEADER.printargs(argc, argv, stdout);
            }
            HEADER.fputformat(OCTREE.OCTFMT, stdout);
            stdout.write('\n');
            startobj = OBJECT.nobjects;		/* previous objects already converted */

            for (; i < argc; i++) /* read new scene descriptions */ {
                if (argv[i].equals("-")) {	/* from stdin */
                    OBJECT.readobj(null);
                    outflags &= ~OCTREE.IO_FILES;
                } else {			/* from file */
                    if (nfiles >= MAXOBJFIL) {
//				error(INTERNAL, "too many scene files");
                    }
                    OBJECT.readobj(ofname[nfiles++] = argv[i]);
                }
            }
            ofname[nfiles] = null;

            if ((inpfrozen != 0) && ((outflags & OCTREE.IO_FILES) != 0)) {
//		error(WARNING, "frozen octree");
                outflags &= ~OCTREE.IO_FILES;
            }
            /* find bounding box */
            bbmin.data[0] = bbmin.data[1] = bbmin.data[2] = FVECT.FHUGE;
            bbmax.data[0] = bbmax.data[1] = bbmax.data[2] = -FVECT.FHUGE;
            for (i = startobj; i < OBJECT.nobjects; i++) {
                BBOX.add2bbox(OBJECT.objptr(i), bbmin, bbmax);
            }
            /* set/check cube */
            if (OCTREE.thescene.cusize == 0.0) {
                if (bbmin.data[0] <= bbmax.data[0]) {
                    for (i = 0; i < 3; i++) {
                        bbmin.data[i] -= OMARGIN;
                        bbmax.data[i] += OMARGIN;
                    }
                    for (i = 0; i < 3; i++) {
                        if (bbmax.data[i] - bbmin.data[i] > OCTREE.thescene.cusize) {
                            OCTREE.thescene.cusize = bbmax.data[i] - bbmin.data[i];
                        }
                    }
                    for (i = 0; i < 3; i++) {
                        OCTREE.thescene.cuorg.data[i] =
                                (bbmax.data[i] + bbmin.data[i] - OCTREE.thescene.cusize) * .5;
                    }
                }
            } else {
                for (i = 0; i < 3; i++) {
                    if (bbmin.data[i] < OCTREE.thescene.cuorg.data[i]
                            || bbmax.data[i] > OCTREE.thescene.cuorg.data[i] + OCTREE.thescene.cusize) {
//				error(USER, "boundary does not encompass scene");
                    }
                }
            }

            mincusize = OCTREE.thescene.cusize / resolu - FVECT.FTINY;

            for (i = startobj; i < OBJECT.nobjects; i++) {		/* add new objects */
                addobject(OCTREE.thescene, i);
//                System.out.println("\nadded object "+i);
            }

            OCTREE.thescene.cutree = OCTREE.combine(OCTREE.thescene.cutree);	/* optimize */

            new WRITEOCT(ostream).writeoct(outflags, OCTREE.thescene, ofname);	/* write structures to stdout */

            quit(0);
        } catch (Exception e) {
        }
    }

    static void quit( /* exit program */
            int code) {
        System.exit(code);
    }

    void cputs() /* interactive error */ {
        /* referenced, but not used */
    }

    void wputs( /* warning message */
            char[] s) {
        if (nowarn == 0) {
            eputs(s);
        }
    }
    private static int inln = 0;

    void eputs( /* put string to stderr */
            char[] s) {
        if (inln++ == 0) {
            System.err.print(progname);
            System.err.print(": ");
        }
        System.err.print(s);
        if (s != null && s[s.length - 1] == '\n') {
            inln = 0;
        }
    }
//				/* conflicting def's in param.h */
//
//#define	 tstbit(f,i)		(f[((i)>>3)] & (1<<((i)&7)))

    static int tstbit(char[] f, int i) {
        return f[((i) >> 3)] & (1 << ((i) & 7));
    }
    //#define	 setbit(f,i)		(f[((i)>>3)] |= (1<<((i)&7)))
//#define	 clrbit(f,i)		(f[((i)>>3)] &=~ (1<<((i)&7)))
//#define	 tglbit(f,i)		(f[((i)>>3)] ^= (1<<((i)&7)))
//
//

    public static void addobject( /* add an object to a cube */
            CUBE cu,
            int obj) {
        int inc;
//        System.out.println("obj "+obj+" cu.cusize "+cu.cusize);
        //inc = (*ofun[objptr(obj)->otype].funp)(objptr(obj), cu);        
        inc = OTYPES.ofun[OBJECT.objptr(obj).otype].ofunc.octree_function(OBJECT.objptr(obj), cu);
//        System.out.println("addobject("+cu.cuorg.data[0]+", "+obj+") inc = "+inc+" cusiz = "+cu.cusize);
        if (inc == OCTREE.O_MISS) {
            return;				/* no intersection */
        }

        if (OCTREE.istree(cu.cutree) != 0) {
            CUBE cukid = new CUBE();			/* do children */
            int i, j;
            cukid.cusize = cu.cusize * 0.5;
            for (i = 0; i < 8; i++) {
                cukid.cutree = OCTREE.octkid(cu.cutree, i);
                for (j = 0; j < 3; j++) {
                    cukid.cuorg.data[j] = cu.cuorg.data[j];
                    if (((1 << j) & i) != 0) {
                        cukid.cuorg.data[j] += cukid.cusize;
                    }
                }
                addobject(cukid, obj);
                OCTREE.octblock[OCTREE.octbi(cu.cutree)][OCTREE.octti(cu.cutree) + i] = cukid.cutree;
                //otree.octkid(cu.cutree, i) = cukid.cutree;
            }
            return;
        }
        if (OCTREE.isempty(cu.cutree) != 0) {
            int[] oset = new int[2];		/* singular set */
            oset[0] = 1;
            oset[1] = obj;
            cu.cutree = OBJSET.fullnode(oset);
            return;
        }
        /* add to full node */
        add2full(cu, obj, inc);
    }

    static void add2full( /* add object to full node */
            CUBE cu,
            int obj,
            int inc) {
        int ot;
        int[] oset = new int[OBJECT.MAXSET + 1];
        CUBE cukid = new CUBE();
        char[] inflg = new char[(OBJECT.MAXSET + 7) / 8], volflg = new char[(OBJECT.MAXSET + 7) / 8];
        int i, j;

        OBJSET.objset(oset, cu.cutree);
        cukid.cusize = cu.cusize * 0.5;

        if (inc == OCTREE.O_IN || oset[0] < objlim || cukid.cusize
                < (oset[0] < OBJECT.MAXSET ? mincusize : mincusize / 256.0)) {
            /* add to set */
            if (oset[0] >= OBJECT.MAXSET) {
//			sprintf(errmsg, "set overflow in addobject (%s)",
//					objptr(obj)->oname);
//			error(INTERNAL, errmsg);
            }
            OBJSET.insertelem(oset, obj);
//            System.out.print(obj);
            cu.cutree = OBJSET.fullnode(oset);
            return;
        }
        /* subdivide cube */
        if ((ot = OCTREE.octalloc()) == OCTREE.EMPTY) {
//		error(SYSTEM, "out of octree space");
        }
        /* mark volumes */
        j = (oset[0] + 7) >> 3;
        while (j-- != 0) {
            volflg[j] = inflg[j] = 0;
        }
        for (j = 1; j <= oset[0]; j++) {
            if (OTYPES.isvolume(OBJECT.objptr(oset[j]).otype) != 0) {
                //setbit(volflg,j-1);
                volflg[((j - 1) >> 3)] |= (1 << ((j - 1) & 7));
                if (OTYPES.ofun[OBJECT.objptr(oset[j]).otype].ofunc.octree_function(
                        OBJECT.objptr(oset[j]), cu) == OCTREE.O_IN) {
                    //setbit(inflg,j-1);
                    inflg[((j - 1) >> 3)] |= (1 << ((j - 1) & 7));
                }
            }
        }
        /* assign subcubes */
        for (i = 0; i < 8; i++) {
            cukid.cutree = OCTREE.EMPTY;
            for (j = 0; j < 3; j++) {
                cukid.cuorg.data[j] = cu.cuorg.data[j];
                if (((1 << j) & i) != 0) {
                    cukid.cuorg.data[j] += cukid.cusize;
                }
            }
            /* surfaces first */
            for (j = 1; j <= oset[0]; j++) {
                if (tstbit(volflg, j - 1) == 0) {
                    addobject(cukid, oset[j]);
                }
            }
            /* then this object */
            addobject(cukid, obj);
            /* then partial volumes */
            for (j = 1; j <= oset[0]; j++) {
                if (tstbit(volflg, j - 1) != 0
                        && tstbit(inflg, j - 1) == 0) {
                    addobject(cukid, oset[j]);
                }
            }
            /* full volumes last */
            for (j = 1; j <= oset[0]; j++) {
                if (tstbit(inflg, j - 1) != 0) {
                    addobject(cukid, oset[j]);
                }
            }
            /* returned node */
            OCTREE.octblock[OCTREE.octbi(ot)][OCTREE.octti(ot) + i] = cukid.cutree;
            //octkid(ot, i) = cukid.cutree;
        }
        cu.cutree = ot;
    }
}