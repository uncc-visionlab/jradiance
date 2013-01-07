/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.ot;

import java.io.FileOutputStream;
import java.io.PrintStream;
import jradiance.common.FACE;
import jradiance.common.FVECT;
import jradiance.common.GETLIBPATH;
import jradiance.common.GETPATH;
import jradiance.common.HEADER;
import jradiance.common.MESH;
import jradiance.common.OBJECT;
import jradiance.common.OBJECT_STRUCTURE;
import jradiance.common.OBJSET;
import jradiance.common.OCTREE;
import jradiance.common.OCTREE.CUBE;
import jradiance.common.OTYPES;
import jradiance.common.PATHS;
import jradiance.ot.OCONV.myPrintStream;

/**
 *
 * @author arwillis
 */
public class OBJ2MESH {
    /*
     *  Main program to compile a Wavefront .OBJ file into a Radiance mesh
     */

    static FACE f = new FACE();                     /* XXX should go to a header file */


    public static class o_default extends OBJECT_STRUCTURE { /* default action is no intersection */


        @Override
        public int octree_function(Object... objs) /* default action is no intersection */ {
            return (OCTREE.O_MISS);
        }
    }
    static String progname;			/* argv[0] */

    static int nowarn = 0;			/* supress warnings? */

    static int objlim = 9;			/* # of objects before split */

    public static int resolu = 16384;			/* octree resolution limit */

    public static double mincusize;			/* minimum cube size from resolu */

    static PrintStream stdout = null;

    public static void main( /* compile a .OBJ file into a mesh */
            String[] argv) {
        int nmatf = 0;
//	char  pathnames[12800];
//	char  *pns = pathnames;
        String[] matinp = new String[128];
        String cp = null;
        int i=0, j;

            while (argv[i].charAt(0) != '-') {
                i++;
            }
            String[] argv2 = new String[argv.length - i + 1];
            for (j = 1; j < argv2.length; j++) {
                argv2[j] = argv[i++];
            }
            argv2[0] = "obj2mesh";
            argv = argv2;
            int argc = argv2.length;     
            
        try {
            progname = argv[0];
//        OTYPES.ot_initotypes(OTYPES.o_default);
            OTYPES.ot_initotypes(new o_default());
            OTYPES.ofun[OTYPES.OBJ_FACE].ofunc = new FACE();//funp = o_face;

            for (i = 1; i < argc && argv[i].charAt(0) == '-'; i++) {
                switch (argv[i].charAt(1)) {
                    case 'n':				/* set limit */
                        objlim = Integer.parseInt(argv[++i]);
                        break;
                    case 'r':				/* resolution limit */
                        resolu = Integer.parseInt(argv[++i]);
                        break;
                    case 'a':				/* material file */
                        matinp[nmatf++] = argv[++i];
                        break;
                    case 'l':				/* library material */
			cp = GETPATH.getpath(argv[++i], GETLIBPATH.getrlibpath(), PATHS.R_OK);
			if (cp == null) {
//				sprintf(errmsg,
//					"cannot find library material: '%s'",
//						argv[i]);
//				error(USER, errmsg);
			}
			matinp[nmatf++] = cp;//strcpy(pns, cp);
//			while (*pns++)
//				;
                        break;
                    case 'w':				/* supress warnings */
                        nowarn = 1;
                        break;
                    default:
//			sprintf(errmsg, "unknown option: '%s'", argv[i]);
//			error(USER, errmsg);
                        break;
                }
            }

//	if (i < argc-2)
//		error(USER, "too many file arguments");
					/* initialize mesh */
            CVMESH.cvinit(i == argc - 2 ? argv[i + 1] : "<stdout>");
            /* load material input */
            for (j = 0; j < nmatf; j++) {
                OBJECT.readobj(matinp[j]);
            }
            /* read .OBJ file into triangles */
            if (i == argc) {
                WFCONV.wfreadobj(null);
            } else {
                WFCONV.wfreadobj(argv[i]);
            }

            CVMESH.cvmeshbounds();			/* set octree boundaries */

            if (i == argc - 2) {		/* open output file */
//		if (freopen(argv[i+1], "w", stdout) == NULL)
//			error(SYSTEM, "cannot open output file");
                stdout = new PrintStream(new FileOutputStream(argv[i + 1]));
//                stdout = new myPrintStream(System.out);
            } else {
                stdout = System.out;
            }

//	SET_FILE_BINARY(stdout);
            HEADER.newheader("RADIANCE", stdout);	/* new binary file header */
            HEADER.printargs(i < argc ? i + 1 : argc, argv, stdout);
            HEADER.fputformat(MESH.MESHFMT, stdout);
            stdout.write('\n');

            mincusize = CVMESH.ourmesh.mcube.cusize / resolu - FVECT.FTINY;

            for (i = 0; i < OBJECT.nobjects; i++) /* add triangles to octree */ {
                if (OBJECT.objptr(i).otype == OTYPES.OBJ_FACE) {
                    addface(CVMESH.ourmesh.mcube, i);
                }
            }

            /* optimize octree */
            CVMESH.ourmesh.mcube.cutree = OCTREE.combine(CVMESH.ourmesh.mcube.cutree);

            if (CVMESH.ourmesh.mcube.cutree == OCTREE.EMPTY) {
//		error(WARNING, "mesh is empty");
            }

            CVMESH.cvmesh();			/* convert mesh and leaf nodes */

            WRITEMESH.writemesh(CVMESH.ourmesh, stdout);	/* write mesh to output */

            /* MESH.printmeshstats(CVMESH.ourmesh, System.err); */
        } catch (Exception e) {
            e.printStackTrace();
        }
        quit(0);
        return; /* pro forma return */
    }

    static void quit( /* exit program */
            int code) {
        System.exit(code);
    }

    public static void addface( /* add a face to a cube */
            CUBE cu,
            int obj) {

        if (f.octree_function(OBJECT.objptr(obj), cu) == OCTREE.O_MISS) {
            return;
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
                addface(cukid, obj);
//			OCTREE.octkid(cu.cutree, i) = cukid.cutree;
                OCTREE.octblock[OCTREE.octbi(cu.cutree)][OCTREE.octti(cu.cutree) + i] = cukid.cutree;
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
        add2full(cu, obj);
    }

    static void add2full( /* add object to full node */
            CUBE cu,
            int obj) {
        int ot;
        int[] oset = new int[OBJECT.MAXSET + 1];
        CUBE cukid = new CUBE();
        int i, j;

        OBJSET.objset(oset, cu.cutree);
        cukid.cusize = cu.cusize * 0.5;

        if (oset[0] < objlim || cukid.cusize
                < (oset[0] < OBJECT.MAXSET ? mincusize : mincusize / 256.0)) {
            /* add to set */
            if (oset[0] >= OBJECT.MAXSET) {
//			sprintf(errmsg, "set overflow in addobject (%s)",
//					OBJECT.objptr(obj).oname);
//			error(INTERNAL, errmsg);
            }
            OBJSET.insertelem(oset, obj);
            cu.cutree = OBJSET.fullnode(oset);
            return;
        }
        /* subdivide cube */
        if ((ot = OCTREE.octalloc()) == OCTREE.EMPTY) {
//		error(SYSTEM, "out of octree space");
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
            for (j = 1; j <= oset[0]; j++) {
                addface(cukid, oset[j]);
            }
            addface(cukid, obj);
            /* returned node */
//		octkid(ot, i) = cukid.cutree;
            OCTREE.octblock[OCTREE.octbi(ot)][OCTREE.octti(ot) + i] = cukid.cutree;
        }
        cu.cutree = ot;
    }
}
