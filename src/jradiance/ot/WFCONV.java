/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.ot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import jradiance.common.FGETLINE;
import jradiance.common.FVECT;
import jradiance.common.MODOBJECT;
import jradiance.common.OBJECT;

/**
 *
 * @author arwillis
 */
public class WFCONV {
    /*
     *  Load Wavefront .OBJ file and convert to triangles with mesh info.
     *  Code borrowed largely from obj2rad.c
     */

    public static class VNDX {

        int[] data = new int[3];        /* vertex index (point,map,normal) */

    }
    public static final int CHUNKSIZ = 1024;	/* vertex allocation chunk size */

    public static final int MAXARG = 512;	/* maximum # arguments in a statement */

    static FVECT[] vlist;		/* our vertex list */

    static int nvs;		/* number of vertices in our list */

    static FVECT[] vnlist;	/* vertex normal list */

    static int nvns;
    static double[][] vtlist;	/* map vertex list */

    static int nvts1;
    static String inpfile;	/* input file name */

    static int havemats;	/* materials available? */

    static String material = null;	/* current material name */

    static String group = null;	/* current group name */

    static int lineno;		/* current line number */

    static int faceno;		/* current face number */

    static void wfreadobj( /* read in .OBJ file and convert */
            String objfn) throws FileNotFoundException, IOException {
        InputStream fp;
        String[] argv = new String[MAXARG];
        int argc = 0;
        int nstats, nunknown;

        if (objfn == null) {
            inpfile = "<stdin>";
            fp = System.in;
        } else if ((fp = new FileInputStream(inpfile = objfn)) == null) {
//		sprintf(errmsg, "cannot open \"%s\"", inpfile);
//		error(USER, errmsg);
        }
        havemats = (OBJECT.nobjects > 0) ? 1 : 0;
        nstats = nunknown = 0;
        material = null;
        group = null;
        lineno = 0;
        faceno = 0;
        /* scan until EOF */
        while ((argc = getstmt(argv, fp)) != 0) {
            switch (argv[0].charAt(0)) {
                case 'v':		/* vertex */
                    if (argv[0].length() == 1) { 			/* point */
//				if (badarg(argc-1,argv+1,"fff"))
//					syntax("bad vertex");
                        newv(Double.parseDouble(argv[1]), Double.parseDouble(argv[2]),
                                Double.parseDouble(argv[3]));
                        break;
                    } else {
                        switch (argv[0].charAt(1)) {
//                        case '\0':			/* point */
//				if (badarg(argc-1,argv+1,"fff"))
//					syntax("bad vertex");
//                            newv(Double.parseDouble(argv[1]), Double.parseDouble(argv[2]),
//                                    Double.parseDouble(argv[3]));
//                            break;
                            case 'n':			/* normal */
//				if (argv[0][2])
//					goto unknown;
//				if (badarg(argc-1,argv+1,"fff"))
//					syntax("bad normal");
                                if (newvn(Double.parseDouble(argv[1]), Double.parseDouble(argv[2]),
                                        Double.parseDouble(argv[3])) == 0) {
//					syntax("zero normal");
                                }
                                break;
                            case 't':			/* coordinate */
//				if (argv[0][2])
//					goto unknown;
//				if (badarg(argc-1,argv+1,"ff"))
//					goto unknown;
                                newvt(Double.parseDouble(argv[1]), Double.parseDouble(argv[2]));
                                break;
                            default:
//				goto unknown;
                        }
                    }
                    break;
                case 'f':				/* face */
//			if (argv[0][1])
//				goto unknown;
                    faceno++;
                    switch (argc - 1) {
                        case 0:
                        case 1:
                        case 2:
//				syntax("too few vertices");
                            break;
                        case 3:
				if (puttri(argv[1], argv[2], argv[3])==0) {
//					syntax("bad triangle");
                                }
                            break;
                        default:
                            String[] ns = new String[argv.length-1];
                            System.arraycopy(argv, 1, ns, 0, argc-1);
				if (putface(ns.length, ns)==0) {
//					syntax("bad face");
                                }
                            break;
                    }
                    break;
                case 'u':				/* usemtl/usemap */
                    if (!argv[0].equals("usemap")) {
                        break;
                    }
//			if (strcmp(argv[0], "usemtl"))
//				goto unknown;
                    if (argc > 1) {
//				strcpy(material, argv[1]);
                        material = new String(argv[1]);
                    } else {
                        material = null;
                    }
                    break;
                case 'o':		/* object name */
//			if (argv[0].charAt(1))
//				goto unknown;
                    break;
                case 'g':		/* group name */
//			if (argv[0][1])
//				goto unknown;
                    if (argc > 1) {
                        //strcpy(group, argv[1]);
                        group = new String(argv[1]);
                    } else {
                        group = null;
                    }
                    break;
                case '#':		/* comment */
                    break;
                default:
                    ;		/* something we don't deal with */
                    unknown:
                    nunknown++;
                    break;
            }
            nstats++;
        }
        /* clean up */
        freeverts();
        fp.close();
        if (nunknown > 0) {
//		sprintf(errmsg, "%d of %d statements unrecognized",
//				nunknown, nstats);
//		error(WARNING, errmsg);
        }
    }
    static char[] sbuf = new char[MAXARG * 16];

    static int getstmt( /* read the next statement from fp */
            String[] av,
            InputStream fp) throws IOException {
//	static char	sbuf[MAXARG*16];
        char[] cp;
        int i, cpidx = 0;

        do {
//            int sizeofsbuf = sbuf.length * Character.SIZE / 8;
            int sizeofsbuf = MAXARG * 16;
            if (FGETLINE.fgetline(cp = sbuf, sizeofsbuf, fp) == null) {
                return (0);
            }
//            String s = new String(cp);
//            av = s.split("\\s");
            i = 0;
            for (;;) {
                while (Character.isWhitespace(cp[cpidx]) || cp[cpidx] == '\\') {
                    if (cp[cpidx] == '\n') {
                        lineno++;
                    }
                    cp[cpidx++] = '\0';
                }
                if (cp[cpidx] == 0) {
                    break;
                }
                if (i >= MAXARG - 1) {
//				sprintf(errmsg,
//			"%s: too many arguments near line %d (limit %d)\n",
//					inpfile, lineno+1, MAXARG-1);
                    break;
                }
                int idx1 = cpidx;
                while (cp[++cpidx] != 0 && !Character.isWhitespace(cp[cpidx]))
				;
                int idx2 = cpidx;
                av[i++] = new String(cp).substring(idx1, idx2);
            }
            av[i] = null;
            lineno++;
        } while (i == 0);

        return (i);
    }

    static int cvtndx( /* convert vertex string to index */
            VNDX vi,
            String vs) {
        //String str = new String(vs);
        String[] strs = vs.split("/");

        /* get point */
        vi.data[0] = Integer.parseInt(strs[0]);
        if (vi.data[0] > 0) {
            if (vi.data[0]-- > nvs) {
                return (0);
            }
        } else if (vi.data[0] < 0) {
            vi.data[0] += nvs;
            if (vi.data[0] < 0) {
                return (0);
            }
        } else {
            return (0);
        }
        /* get map coord. */
//	while (*vs)
//		if (*vs++ == '/')
//			break;
        vi.data[1] = Integer.parseInt(strs[1]);
        if (vi.data[1] > 0) {
            if (vi.data[1]-- > nvts1) {
                return (0);
            }
        } else if (vi.data[1] < 0) {
            vi.data[1] += nvts1;
            if (vi.data[1] < 0) {
                return (0);
            }
        } else {
            vi.data[1] = -1;
        }
        /* get normal */
//	while (*vs)
//		if (*vs++ == '/')
//			break;
        vi.data[2] = Integer.parseInt(strs[2]);
        if (vi.data[2] > 0) {
            if (vi.data[2]-- > nvns) {
                return (0);
            }
        } else if (vi.data[2] < 0) {
            vi.data[2] += nvns;
            if (vi.data[2] < 0) {
                return (0);
            }
        } else {
            vi.data[2] = -1;
        }
        return (1);
    }

    static int putface( /* put out an N-sided polygon */
            int ac,
            String[] av) {
        char[] cp;
        int i;

        while (ac > 3) {		/* break into triangles */
            if (puttri(av[0], av[1], av[2]) == 0) {
                return (0);
            }
            ac--;			/* remove vertex & rotate */
            cp = av[0].toCharArray();
            for (i = 0; i < ac - 1; i++) {
                av[i] = av[i + 2];
            }
            av[i] = new String(cp);
        }
        return (puttri(av[0], av[1], av[2]));
    }

    static int getmod() /* get current modifier ID */ {
        String mnam;
        int mod;

        if (havemats == 0) {
            return (OBJECT.OVOID);
        }
        if (material.equals(OBJECT.VOIDID)) {
            return (OBJECT.OVOID);
        }
        if (material != null) /* prefer usemtl statements */ {
            mnam = material;
        } else if (group != null) /* else use group name */ {
            mnam = group;
        } else {
            return (OBJECT.OVOID);
        }
        mod = MODOBJECT.modifier(mnam.toString());
        if (mod == OBJECT.OVOID) {
//		sprintf(errmsg, "%s: undefined modifier \"%s\"",
//				inpfile, mnam);
//		error(USER, errmsg);
        }
        return (mod);
    }

    public static int puttri( /* convert a triangle */
            String v1,
            String v2,
            String v3) {
        VNDX v1i = new VNDX(), v2i = new VNDX(), v3i = new VNDX();
        double[] v1c, v2c, v3c;
        FVECT v1n, v2n, v3n;

        if (cvtndx(v1i, v1) == 0 || cvtndx(v2i, v2) == 0 || cvtndx(v3i, v3) == 0) {
//		error(WARNING, "bad vertex reference");
            return (0);
        }
        if (v1i.data[1] >= 0 && v2i.data[1] >= 0 && v3i.data[1] >= 0) {
            v1c = vtlist[v1i.data[1]];
            v2c = vtlist[v2i.data[1]];
            v3c = vtlist[v3i.data[1]];
        } else {
            v1c = v2c = v3c = null;
        }

        if (v1i.data[2] >= 0 && v2i.data[2] >= 0 && v3i.data[2] >= 0) {
            v1n = vnlist[v1i.data[2]];
            v2n = vnlist[v2i.data[2]];
            v3n = vnlist[v3i.data[2]];
        } else {
            v1n = v2n = v3n = null;
        }

        return (CVMESH.cvtri(getmod(), vlist[v1i.data[0]], vlist[v2i.data[0]], vlist[v3i.data[0]],
                v1n, v2n, v3n, v1c, v2c, v3c) >= 0) ? 1 : 0;
    }

    static void freeverts() /* free all vertices */ {
        if (nvs != 0) {
//		free((void *)vlist);
            vlist = null;
            nvs = 0;
        }
        if (nvts1 != 0) {
//		free((void *)vtlist);
            vtlist = null;
            nvts1 = 0;
        }
        if (nvns != 0) {
//		free((void *)vnlist);
            vnlist = null;
            nvns = 0;
        }
    }

    public static int newv( /* create a new vertex */
            double x,
            double y,
            double z) {
        if ((nvs % CHUNKSIZ) == 0) {		/* allocate next block */
            if (nvs == 0) {
                vlist = new FVECT[CHUNKSIZ];
                for (int i = 0; i < vlist.length; i++) {
                    vlist[i] = new FVECT();
                }
            } else {
                FVECT[] nvlist = new FVECT[(nvs + CHUNKSIZ)];
                System.arraycopy(vlist, 0, nvlist, 0, nvs);
                vlist = nvlist;
                for (int i = nvs; i < vlist.length; i++) {
                    vlist[i] = new FVECT();
                }
            }
            if (vlist == null) {
//			error(SYSTEM, "out of memory in newv");
            }
        }
        /* assign new vertex */
        vlist[nvs].data[0] = x;
        vlist[nvs].data[1] = y;
        vlist[nvs].data[2] = z;
        return (++nvs);
    }

    public static int newvn( /* create a new vertex normal */
            double x,
            double y,
            double z) {
        if ((nvns % CHUNKSIZ) == 0) {		/* allocate next block */
            if (nvns == 0) {
                vnlist = new FVECT[CHUNKSIZ];
                for (int i = 0; i < vnlist.length; i++) {
                    vnlist[i] = new FVECT();
                }
            } else {
                FVECT[] nvnlist = new FVECT[(nvns + CHUNKSIZ)];
                System.arraycopy(vnlist, 0, nvnlist, 0, nvns);
                vnlist = nvnlist;
                for (int i = nvns; i < vnlist.length; i++) {
                    vnlist[i] = new FVECT();
                }
            }
            if (vnlist == null) {
//			error(SYSTEM, "out of memory in newvn");
            }
        }
        /* assign new normal */
        vnlist[nvns].data[0] = x;
        vnlist[nvns].data[1] = y;
        vnlist[nvns].data[2] = z;
        if (FVECT.normalize(vnlist[nvns]) == 0.0) {
            return (0);
        }
        return (++nvns);
    }

    public static int newvt( /* create a new texture map vertex */
            double x,
            double y) {
        if ((nvts1 % CHUNKSIZ) == 0) {		/* allocate next block */
            if (nvts1 == 0) {
                vtlist = new double[CHUNKSIZ][2];
            } else {
                double[][] nvtlist = new double[(nvts1 + CHUNKSIZ)][2];
                for (int i = 0; i < nvts1; i++) {
                    System.arraycopy(vtlist[i], 0, nvtlist[i], 0, 2);
                }
                vtlist = nvtlist;
            }
            if (vtlist == null) {
//			error(SYSTEM, "out of memory in newvt");
            }
        }
        /* assign new vertex */
        vtlist[nvts1][0] = x;
        vtlist[nvts1][1] = y;
        return (++nvts1);
    }
//
//static void
//syntax(			/* report syntax error and exit */
//	char	*er
//)
//{
//	sprintf(errmsg, "%s: Wavefront syntax error near line %d: %s\n",
//			inpfile, lineno, er);
//	error(USER, errmsg);
//}
//    
}
