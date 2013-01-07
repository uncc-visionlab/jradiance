/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.rt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import jradiance.common.EXPANDARG;
import jradiance.common.FIXARGV0;
import jradiance.common.IMAGE;
import jradiance.common.OTYPES;
import jradiance.common.PORTIO;
import jradiance.common.VIEW;
import jradiance.rt.INITOTYPES.o_default;
import jradiance.rt.RPAINT.PNODE;
import jradiance.rt.RPAINT.RECT;
import jradiance.util.VERSION;

/**
 *
 * @author arwillis
 */
public class RVMAIN {
/*
     *  rvmain.c - main for rview interactive viewer
     */

    public static String progname;			/* global argv[0] */

    public static VIEW ourview = VIEW.STDVIEW;		/* viewing parameters */

    public static int hresolu, vresolu;			/* image resolution */

    static int psample = 8;			/* pixel sample size */

    static double maxdiff = .15;			/* max. sample difference */

    static int greyscale = 0;			/* map colors to brightness? */
//char[]  dvcname = dev_default;		/* output device name */
//static final String dev_default = "x11";
    static String dvcname = DEVTABLE.dev_default;
    static double exposure = 1.0;			/* exposure for scene */

    static int newparam = 1;			/* parameter setting changed */

    public static DRIVER dev = null;		/* driver functions */

    static String rifname;			/* rad input file name */

    VIEW oldview;				/* previous view parameters */

    public static PNODE ptrunk = new PNODE();				/* the base of our image */

    public static RECT pframe = new RECT();				/* current frame boundaries */

    public static int pdepth;				/* image depth in current frame */

    static String errfile = null;			/* error output file */

    static int nproc = 1;				/* number of processes */
// signal.h NSIG=65
    public static final int NSIG = 65;
    char[] sigerr = new char[NSIG];			/* signal error messages */

//static void onsig(int  signo);
//static void sigdie(int  signo, char  *msg);
//static void printdefaults(void);
    public static InputStream frandomStr = null;
    public static InputStream randomStr = null;

    public static void main(String[] argv) throws IOException {
//#define	 check(ol,al)		if (argv[i][ol] || \
//				badarg(argc-i-1,argv+i+1,al)) \
//				goto badopt
//#define	 bool(olen,var)		switch (argv[i][olen]) { \
//				case '\0': var = !var; break; \
//				case 'y': case 'Y': case 't': case 'T': \
//				case '+': case '1': var = 1; break; \
//				case 'n': case 'N': case 'f': case 'F': \
//				case '-': case '0': var = 0; break; \
//				default: goto badopt; }
        String octnm = null;
        String err;
        int rval, argc;
        int i = 0;
//        RVMAIN rvu = new RVMAIN();
					/* global program name */
        for (i = 0; i < argv.length; i++) {
            if (argv[i].charAt(0) == '-') {
                break;
            }
        }
        String[] argv2 = new String[argv.length - i + 1];
        for (int j = 1; j < argv2.length; j++) {
            argv2[j] = argv[i++];
        }
        argv2[0] = "rvu";
        argv = argv2;
        argc = argv2.length;
        RVMAIN.progname = FIXARGV0.fixargv0(argv[0]);
        /* set our defaults */
        RAYCALLS.shadthresh = .1;
        RAYCALLS.shadcert = .25;
        RAYCALLS.directrelay = 0;
        RAYCALLS.vspretest = 128;
        RAYCALLS.srcsizerat = 0.;
        RAYCALLS.specthresh = .3;
        RAYCALLS.specjitter = 1.;
        RAYCALLS.maxdepth = 6;
        RAYCALLS.minweight = 1e-2;
        RAYCALLS.ambacc = 0.3;
        RAYCALLS.ambres = 32;
        RAYCALLS.ambdiv = 256;
        RAYCALLS.ambssamp = 64;
        /* option city */
        for (i = 1; i < argc; i++) {
            /* expand arguments */
            while ((rval = EXPANDARG.expandarg(argc, argv, i)) > 0) {
                ;
            }
            if (rval < 0) {
//			sprintf(errmsg, "cannot expand '%s'", argv[i]);
//			error(SYSTEM, errmsg);
            }
            if (argv[i] == null || argv[i].charAt(0) != '-') {
                break;			/* break from options */
            }
            if (argv[i].equals("-version")) {
                System.out.println(VERSION.VersionID);
                System.exit(0);
            }
            if (argv[i].equals("-defaults")
                    || argv[i].equals("-help")) {
//			printdefaults();
                System.exit(0);
            }
            if (argv[i].equals("-devices")) {
//			printdevices();
                System.exit(0);
            }
            String[] argv3 = new String[argv.length - i];
            for (int ii = i; ii < argv.length; ii++) {
                argv3[ii - i] = argv[ii];
            }
            rval = RENDEROPTS.getrenderopt(argc - i, argv3);
            if (rval >= 0) {
                i += rval;
                continue;
            }
            argv3 = new String[argv.length - i];
            for (int ii = i; ii < argv.length; ii++) {
                argv3[ii - i] = argv[ii];
            }
            rval = IMAGE.getviewopt(ourview, argc - i, argv3);
            if (rval >= 0) {
                i += rval;
                continue;
            }
            switch (argv[i].charAt(1)) {
                case 'n':				/* # processes */
//			check(2,"i");
                    RVMAIN.nproc = Integer.parseInt(argv[++i]);
                    if (RVMAIN.nproc <= 0) {
//				error(USER, "bad number of processes");
                    }
                    break;
                case 'v':				/* view file */
                    System.out.println("Unsupported command line argument " + argv[i] + " found.");
//			if (argv[i].charAt(2) != 'f')
//				goto badopt;
//			check(3,"s");
//			rval = viewfile(argv[++i], &ourview, NULL);
                    if (rval < 0) {
//				sprintf(errmsg,
//				"cannot open view file \"%s\"",
//						argv[i]);
//				error(SYSTEM, errmsg);
                    } else if (rval == 0) {
//				sprintf(errmsg,
//					"bad view file \"%s\"",
//						argv[i]);
//				error(USER, errmsg);
                    }
                    break;
                case 'b':				/* grayscale */
//			bool(2,greyscale);
                    System.out.println("Unsupported command line argument " + argv[i] + " found.");
                    break;
                case 'p':				/* pixel */
                    switch (argv[i].charAt(2)) {
                        case 's':				/* sample */
//				check(3,"i");
                            RVMAIN.psample = Integer.parseInt(argv[++i]);
                            break;
                        case 't':				/* threshold */
//				check(3,"f");
                            RVMAIN.maxdiff = Float.parseFloat(argv[++i]);
                            break;
                        case 'e':				/* exposure */
//				check(3,"f");
                            RVMAIN.exposure = Float.parseFloat(argv[++i]);
                            if (argv[i].charAt(0) == '+' || argv[i].charAt(0) == '-') {
                                RVMAIN.exposure = Math.pow(2.0, RVMAIN.exposure);
                            }
                            break;
                        default:
//				goto badopt;
                            System.exit(0);

                    }
                    break;
                case 'w':				/* warnings */
//			rval = erract[WARNING].pf != NULL;
//			bool(2,rval);
//			if (rval) erract[WARNING].pf = wputs;
//			else erract[WARNING].pf = NULL;
                    System.out.println("Unsupported command line argument " + argv[i] + " found.");
                    break;
                case 'e':				/* error file */
//			check(2,"s");
                    RVMAIN.errfile = argv[++i];
                    break;
                case 'o':				/* output device */
//			check(2,"s");
                    RVMAIN.dvcname = argv[++i];
                    break;
                case 'R':				/* render input file */
//			check(2,"s");
                    RVMAIN.rifname = argv[++i];
                    break;
                default:
//			goto badopt;
                    System.out.println("Unsupported command line argument " + argv[i] + " found.");
                    System.exit(0);

            }
        }
        err = IMAGE.setview(RVMAIN.ourview);	/* set viewing parameters */
        if (err != null) {
//		error(USER, err);
        }

					/* open error file */
//	if (errfile != null) {
//		if (freopen(errfile, "a", stderr) == null)
//			quit(2);
//		fprintf(stderr, "**************\n*** PID %5d: ",
//				getpid());
//		printargs(argc, argv, stderr);
//		putc('\n', stderr);
//		fflush(stderr);
//	}
//#ifdef	NICE
//	nice(NICE);			/* lower priority */
//#endif
					/* get octree */
        if (i == argc) {
            octnm = null;
        } else if (i == argc - 1) {
            octnm = argv[i];
        } else {
//		goto badopt;
            System.exit(0);
        }
        if (octnm == null) {
//		error(USER, "missing octree argument");
        }
        
        try {
            frandomStr = new FileInputStream("frandom_vals");
            randomStr = new FileInputStream("random_vals");
        } catch (FileNotFoundException e) {
            OutputStream ofrandomStr = new FileOutputStream("frandom_vals");
            OutputStream orandomStr = new FileOutputStream("random_vals");
            RAYCALLS.randsrc = new Random();
            for (i=0; i < 1000000; i++) {
                double d = RAYCALLS.frandom();
                long l = RAYCALLS.random();
                PORTIO.putflt(d, ofrandomStr);
                PORTIO.putint(l, 4, orandomStr);                
            }
            frandomStr = new FileInputStream("frandom_vals");
            randomStr = new FileInputStream("random_vals");            
        }

        
        /* set up output & start process(es) */
//	SET_FILE_BINARY(stdout);
//	OTYPES otypes = new OTYPES();
        //INITOTYPES iotypes = new INITOTYPES();
        OTYPES.ot_initotypes(new o_default());
//        OBJECT objsrc = new OBJECT();
        RAYCALLS.ray_init(octnm);

        RVIEW.rview();			/* run interactive viewer */

//	devclose();			/* close output device */

//	quit(0);
        System.exit(0);
//
//badopt:
//	sprintf(errmsg, "command line error at '%s'", argv[i]);
//	error(USER, errmsg);
//	return 1; /* pro forma return */
//
//#undef	check
//#undef	bool
    }

//void
//wputs(				/* warning output function */
//	char	*s
//)
//{
//	int  lasterrno = errno;
//	eputs(s);
//	errno = lasterrno;
//}
//void
//eputs(				/* put string to stderr */
//	char  *s
//)
//{
//	static int  midline = 0;
//
//	if (!*s)
//		return;
//	if (!midline++) {
//		fputs(progname, stderr);
//		fputs(": ", stderr);
//	}
//	fputs(s, stderr);
//	if (s[strlen(s)-1] == '\n') {
//		fflush(stderr);
//		midline = 0;
//	}
//}
//static int gotsig = 0;
//static void
//onsig(				/* fatal signal */
//	int  signo
//)
//{
//	static int  gotsig = 0;
//
//	if (gotsig++)			/* two signals and we're gone! */
//		_exit(signo);
//#ifndef _WIN32
//	alarm(15);			/* allow 15 seconds to clean up */
//	signal(SIGALRM, SIG_DFL);	/* make certain we do die */
//#endif
//	eputs("signal - ");
//	eputs(sigerr[signo]);
//	eputs("\n");
//	devclose();
//	quit(3);
//}
//static void
//sigdie(			/* set fatal signal */
//	int  signo,
//	char  *msg
//)
//{
//	if (signal(signo, onsig) == SIG_IGN)
//		signal(signo, SIG_IGN);
//	sigerr[signo] = msg;
//}
    static void printdefaults() /* print default values to stdout */ {
//	String.format("-n %-2d\t\t\t\t# number of rendering processes\n", nproc);
//	String.format(greyscale ? "-b+\t\t\t\t# greyscale on\n" :
//			"-b-\t\t\t\t# greyscale off\n");
//	String.format("-vt%c\t\t\t\t# view type %s\n", ourview.type,
//			ourview.type==VT_PER ? "perspective" :
//			ourview.type==VT_PAR ? "parallel" :
//			ourview.type==VT_HEM ? "hemispherical" :
//			ourview.type==VT_ANG ? "angular" :
//			ourview.type==VT_CYL ? "cylindrical" :
//			ourview.type==VT_PLS ? "planisphere" :
//			"unknown");
//	String.format("-vp %f %f %f\t# view point\n",
//			ourview.vp[0], ourview.vp[1], ourview.vp[2]);
//	String.format("-vd %f %f %f\t# view direction\n",
//			ourview.vdir[0], ourview.vdir[1], ourview.vdir[2]);
//	String.format("-vu %f %f %f\t# view up\n",
//			ourview.vup[0], ourview.vup[1], ourview.vup[2]);
//	String.format("-vh %f\t\t\t# view horizontal size\n", ourview.horiz);
//	String.format("-vv %f\t\t\t# view vertical size\n", ourview.vert);
//	String.format("-vo %f\t\t\t# view fore clipping plane\n", ourview.vfore);
//	String.format("-va %f\t\t\t# view aft clipping plane\n", ourview.vaft);
//	String.format("-vs %f\t\t\t# view shift\n", ourview.hoff);
//	String.format("-vl %f\t\t\t# view lift\n", ourview.voff);
//	String.format("-pe %f\t\t\t# pixel exposure\n", exposure);
//	String.format("-ps %-9d\t\t\t# pixel sample\n", psample);
//	String.format("-pt %f\t\t\t# pixel threshold\n", maxdiff);
//	String.format("-o %s\t\t\t\t# output device\n", dvcname);
//	String.format(erract[WARNING].pf != NULL ?
//			"-w+\t\t\t\t# warning messages on\n" :
//			"-w-\t\t\t\t# warning messages off\n");
        RENDEROPTS.print_rdefaults();
    }
}
