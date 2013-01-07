/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common.px;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import jradiance.common.COLOROPS;
import jradiance.common.COLORS;
import jradiance.common.COLORS.COLOR;
import jradiance.common.COLORS.COLR;
import jradiance.common.HEADER;
import jradiance.common.RESOLU;
import jradiance.ot.OCONV;

/**
 *
 * @author arwillis
 */
public class RA_PPM {
    /*
     *  program to convert between RADIANCE and Poskanzer Pixmaps
     */

    static double fltv(int i) {
        return (((i) + .5) / (maxval + 1.));
    }
    static int bradj = 0;				/* brightness adjustment */

    static double gamcor = 2.2;			/* gamma correction value */

    static int maxval = 255;			/* maximum primary value */

    static String progname;
    static int xmax, ymax;
    static OutputStream stdout = System.out;
    static final int EOF = -1;
    static InputStream stdin = System.in;
    static PrintStream stderr = System.err;

    public interface COLRSCANF_T {

        int colrscanf_t(COLR[] scan, int len, InputStream fp) throws IOException;
    }

    public interface COLORSCANF_T {

        int colorscanf_t(COLOR[] scan, int len, InputStream fp) throws IOException;
    }

//typedef int colrscanf_t(COLR *scan, int len, FILE *fp);
//typedef int colorscanf_t(COLOR *scan, int len, FILE *fp);
//static colrscanf_t agryscan, bgryscan, aclrscan, bclrscan;
//static void ppm2ra(colrscanf_t *getscan);
//static void ra2ppm(int  binary, int  grey);
//
//static colorscanf_t agryscan2, bgryscan2, aclrscan2, bclrscan2;
//static void ppm2ra2(colorscanf_t *getscan);
//static void ra2ppm2(int  binary, int  grey);
//
//static int normval(unsigned int  v);
//static unsigned int scanint(FILE  *fp);
//static unsigned int intv(double	v);
//static unsigned int getby2(FILE	*fp);
//static void putby2(unsigned int	w, FILE	*fp);
//static void quiterr(char  *err);
//
    public static void main(
            String[] argv) throws IOException {
        int argc = argv.length;
        char[] inpbuf = new char[2];
        int gryflag = 0;
        int binflag = 1;
        boolean reverse = false;
        int ptype;
        int i;

        progname = argv[0];

        for (i = 1; i < argc; i++) {
            if (argv[i].charAt(0) == '-') {
                switch (argv[i].charAt(1)) {
                    case 's':
                        maxval = Integer.parseInt(argv[++i]) & 0xffff;
                        break;
                    case 'b':
                        gryflag = 1;
                        break;
                    case 'g':
                        gamcor = Double.parseDouble(argv[++i]);
                        break;
                    case 'e':
//				if (argv[i+1][0] != '+' && argv[i+1][0] != '-')
//					goto userr;
                        bradj = Integer.parseInt(argv[++i]);
                        break;
                    case 'a':
                        binflag = 0;
                        break;
                    case 'r':
                        reverse = !reverse;
                        break;
                    default:
//				goto userr;
                }
            } else {
                break;
            }
        }

//	if (i < argc-2)
//		goto userr;
        if (i <= argc - 1 && (stdin = new FileInputStream(argv[i])) == null) {
//		fprintf(stderr, "%s: can't open input \"%s\"\n",
//				progname, argv[i]);
//		exit(1);
        }
        if (i == argc - 2 && (stdout = new FileOutputStream(argv[i + 1])) == null) {
//		fprintf(stderr, "%s: can't open output \"%s\"\n",
//				progname, argv[i+1]);
//		exit(1);
        }
//        stdout = new OCONV.myPrintStream(stdout);
        if (maxval < 256) {
            COLOROPS.setcolrgam(gamcor);
        }
        if (reverse) {
            /* get header */
            inpbuf[0] = (char) stdin.read();
            inpbuf[1] = (char) stdin.read();
            if (inpbuf[0] != 'P') {
                quiterr("input not a Poskanzer Pixmap");
            }
            ptype = inpbuf[1];
//#ifdef _WIN32
//		if (ptype > 4)
//			SET_FILE_BINARY(stdin);
//		SET_FILE_BINARY(stdout);
//#endif
            xmax = scanint(stdin);
            ymax = scanint(stdin);
            maxval = scanint(stdin);
            /* put header */
            HEADER.newheader("RADIANCE", stdout);
            HEADER.printargs(i, argv, stdout);
//            stdout.write("ra_ppm -r\n".getBytes());
            HEADER.fputformat(COLORS.COLRFMT, stdout);
            stdout.write('\n');
            RESOLU.fprtresolu(xmax, ymax, stdout);
            /* convert file */
            if (maxval >= 256) {
                switch (ptype) {
                    case '2':
                        ppm2ra2(new AGRYSCAN2());
                        break;
                    case '5':
                        ppm2ra2(new BGRYSCAN2());
                        break;
                    case '3':
                        ppm2ra2(new ACLRSCAN2());
                        break;
                    case '6':
                        ppm2ra2(new BCLRSCAN2());
                        break;
                    default:
//				quiterr("unsupported Pixmap type");
                }
            } else {
                switch (ptype) {
                    case '2':
                        ppm2ra(new AGRYSCAN());
                        break;
                    case '5':
                        ppm2ra(new BGRYSCAN());
                        break;
                    case '3':
                        ppm2ra(new ACLRSCAN());
                        break;
                    case '6':
                        ppm2ra(new BCLRSCAN());
                        break;
                    default:
//				quiterr("unsupported Pixmap type");
                }
            }
        } else {
//#ifdef _WIN32
//		SET_FILE_BINARY(stdin);
//		if (binflag)
//			SET_FILE_BINARY(stdout);
//#endif
					/* get header info. */
            int[] xmaxA = new int[1];
            int[] ymaxA = new int[1];
            if (HEADER.checkheader(stdin, COLORS.COLRFMT.toCharArray(), null) < 0
                    || RESOLU.fgetresolu(xmaxA, ymaxA, stdin) < 0) {
//			quiterr("bad picture format");
            }
            xmax = xmaxA[0];
            ymax = ymaxA[0];
            /* write PPM header */
            stdout.write(String.format("P%1d\n%d %d\n%d\n", (gryflag != 0 ? 2 : 3) + (binflag != 0 ? 3 : 0),
                    xmax, ymax, maxval).getBytes());
            /* convert file */
            if (maxval >= 256) {
                ra2ppm2(binflag, gryflag);
            } else {
                ra2ppm(binflag, gryflag);
            }
        }
        System.exit(0);
//userr:
//	fprintf(stderr,
//		"Usage: %s [-r][-a][-b][-s maxv][-g gamma][-e +/-stops] [input [output]]\n",
//			progname);
//	exit(1);
    }

    static void quiterr( /* print message and exit */
            String err) {
        if (err != null) {
            stderr.print(String.format("%s: %s\n", progname, err));
            System.exit(1);
        }
        System.exit(0);
    }

    static void ppm2ra( /* convert 1-byte Pixmap to Radiance picture */
            COLRSCANF_T getscan) throws IOException {
        COLR[] scanout;
        int y;
        /* allocate scanline */
        scanout = new COLR[xmax];
        for (int i = 0; i < scanout.length; i++) {
            scanout[i] = new COLR();
        }
        if (scanout == null) {
            quiterr("out of memory in ppm2ra");
        }
        /* convert image */
        for (y = ymax - 1; y >= 0; y--) {
            if (getscan.colrscanf_t(scanout, xmax, stdin) < 0) {
                quiterr("error reading Pixmap");
            }
            COLOROPS.gambs_colrs(scanout, xmax);
            if (bradj != 0) {
                COLOROPS.shiftcolrs(scanout, xmax, bradj);
            }
            if (COLORS.fwritecolrs(scanout, xmax, stdout) < 0) {
                quiterr("error writing Radiance picture");
            }
        }
        /* free scanline */
//	free((void *)scanout);
        scanout = null;
    }

    static void ra2ppm( /* convert Radiance picture to 1-byte/sample Pixmap */
            int binary,
            int grey) throws IOException {
        COLR[] scanin;
        int x;
        int y;
        /* allocate scanline */
        scanin = new COLR[xmax];
        for (int i = 0; i < scanin.length; i++) {
            scanin[i] = new COLR();
        }
        if (scanin == null) {
            quiterr("out of memory in ra2ppm");
        }
        /* convert image */
        for (y = ymax - 1; y >= 0; y--) {
            if (COLORS.freadcolrs(scanin, xmax, stdin) < 0) {
                quiterr("error reading Radiance picture");
            }
            if (bradj != 0) {
                COLOROPS.shiftcolrs(scanin, xmax, bradj);
            }
            for (x = grey != 0 ? xmax : 0; x-- != 0;) {
                scanin[x].data[COLOR.GRN] = (byte) COLORS.normbright(scanin[x]);
            }
            COLOROPS.colrs_gambs(scanin, xmax);
            if (grey != 0) {
                if (binary != 0) {
                    for (x = 0; x < xmax; x++) {
                        stdout.write(scanin[x].data[COLOR.GRN]);
                    }
                } else {
                    for (x = 0; x < xmax; x++) {
                        stdout.write(String.format("%d\n", scanin[x].data[COLOR.GRN]).getBytes());
                    }
                }
            } else if (binary != 0) {
                for (x = 0; x < xmax; x++) {
                    stdout.write(scanin[x].data[COLOR.RED]);
                    stdout.write(scanin[x].data[COLOR.GRN]);
                    stdout.write(scanin[x].data[COLOR.BLU]);
                }
            } else {
                for (x = 0; x < xmax; x++) {
                    stdout.write(String.format("%d %d %d\n", scanin[x].data[COLOR.RED],
                            scanin[x].data[COLOR.GRN],
                            scanin[x].data[COLOR.BLU]).getBytes());
                }
            }
//		if (ferror(stdout))
//			quiterr("error writing Pixmap");
        }
        /* free scanline */
//	free((void *)scanin);
        scanin = null;
    }

    static void ppm2ra2( /* convert 2-byte Pixmap to Radiance picture */
            COLORSCANF_T getscan) throws IOException {
        COLOR[] scanout;
        double mult = 0;
        int y;
        int x;
        /* allocate scanline */
        scanout = new COLOR[xmax];
        for (int i = 0; i < scanout.length; i++) {
            scanout[i] = new COLOR();
        }
        if (scanout == null) {
            quiterr("out of memory in ppm2ra2");
        }
        if (bradj != 0) {
            mult = Math.pow(2., (double) bradj);
        }
        /* convert image */
        for (y = ymax - 1; y >= 0; y--) {
            if (getscan.colorscanf_t(scanout, xmax, stdin) < 0) {
                quiterr("error reading Pixmap");
            }
            for (x = ((gamcor > 1.01) || (gamcor < 0.99)) ? xmax : 0; x-- != 0;) {
                scanout[x].data[COLOR.RED] = (float) Math.pow(scanout[x].colval(COLOR.RED), gamcor);
                scanout[x].data[COLOR.GRN] = (float) Math.pow(scanout[x].colval(COLOR.GRN), gamcor);
                scanout[x].data[COLOR.BLU] = (float) Math.pow(scanout[x].colval(COLOR.BLU), gamcor);
            }
            for (x = bradj != 0 ? xmax : 0; x-- != 0;) {
                scanout[x].scalecolor(mult);
            }
            if (COLORS.fwritescan(scanout, xmax, stdout) < 0) {
                quiterr("error writing Radiance picture");
            }
        }
        /* free scanline */
//	free((void *)scanout);
        scanout = null;
    }

    static void ra2ppm2( /* convert Radiance picture to Pixmap (2-byte) */
            int binary,
            int grey) throws IOException {
        COLOR[] scanin;
        double mult = 0, d;
        int x;
        int y;
        /* allocate scanline */
        scanin = new COLOR[xmax];
        for (int i = 0; i < scanin.length; i++) {
            scanin[i] = new COLOR();
        }
        if (scanin == null) {
//		quiterr("out of memory in ra2ppm2");
        }
        if (bradj != 0) {
            mult = Math.pow(2., (double) bradj);
        }
        /* convert image */
        for (y = ymax - 1; y >= 0; y--) {
            if (COLORS.freadscan(scanin, xmax, stdin) < 0) {
//			quiterr("error reading Radiance picture");
            }
            for (x = bradj != 0 ? xmax : 0; x-- != 0;) {
                scanin[x].scalecolor(mult);
            }
            for (x = grey != 0 ? xmax : 0; x-- != 0;) {
                scanin[x].data[COLOR.GRN] = (float) COLOR.bright(scanin[x]);
            }
            d = 1. / gamcor;
            for (x = ((d > 1.01) || (d < 0.99)) ? xmax : 0; x-- != 0;) {
                scanin[x].data[COLOR.GRN] = (float) Math.pow(scanin[x].colval(COLOR.GRN), d);
                if (grey == 0) {
                    scanin[x].data[COLOR.RED] = (float) Math.pow(scanin[x].colval(COLOR.RED), d);
                    scanin[x].data[COLOR.BLU] = (float) Math.pow(scanin[x].colval(COLOR.BLU), d);
                }
            }
            if (grey != 0) {
                if (binary != 0) {
                    for (x = 0; x < xmax; x++) {
                        putby2(intv(scanin[x].colval(COLOR.GRN)),
                                stdout);
                    }
                } else {
                    for (x = 0; x < xmax; x++) {
                        stdout.write(String.format("%d\n",
                                intv(scanin[x].colval(COLOR.GRN))).getBytes());
                    }
                }
            } else if (binary != 0) {
                for (x = 0; x < xmax; x++) {
                    putby2(intv(scanin[x].colval(COLOR.RED)),
                            stdout);
                    putby2(intv(scanin[x].colval(COLOR.GRN)),
                            stdout);
                    putby2(intv(scanin[x].colval(COLOR.BLU)),
                            stdout);
                }
            } else {
                for (x = 0; x < xmax; x++) {
                    stdout.write(String.format("%d %d %d\n",
                            intv(scanin[x].colval(COLOR.RED)),
                            intv(scanin[x].colval(COLOR.GRN)),
                            intv(scanin[x].colval(COLOR.BLU))).getBytes());
                }
            }
//		if (ferror(stdout))
//			quiterr("error writing Pixmap");
        }
        /* free scanline */
//	free((void *)scanin);
        scanin = null;
    }

    public static class AGRYSCAN implements COLRSCANF_T {

        @Override
        public int colrscanf_t(COLR[] scan, int len, InputStream fp) throws IOException {
            return agryscan(scan, len, fp);
        }

        static int agryscan( /* get an ASCII greyscale scanline */
                COLR[] scan,
                int len,
                InputStream fp) throws IOException {
            int scanidx = 0;
            while (len-- > 0) {
                scan[scanidx].data[COLOR.RED] =
                        scan[scanidx].data[COLOR.GRN] =
                        scan[scanidx].data[COLOR.BLU] = (byte) normval(scanint(fp));
                scanidx++;
            }
            return (0);
        }
    }

    public static class BGRYSCAN implements COLRSCANF_T {

        @Override
        public int colrscanf_t(COLR[] scan, int len, InputStream fp) throws IOException {
            return bgryscan(scan, len, fp);
        }

        static int bgryscan( /* get a binary greyscale scanline */
                COLR[] scan,
                int len,
                InputStream fp) throws IOException {
            int c;
            int scanidx = 0;
            while (len-- > 0) {
                if ((c = fp.read()) == EOF) {
                    return (-1);
                }
                if (maxval != 255) {
                    c = normval(c);
                }
                scan[scanidx].data[COLOR.RED] =
                        scan[scanidx].data[COLOR.GRN] =
                        scan[scanidx].data[COLOR.BLU] = (byte) c;
                scanidx++;
            }
            return (0);
        }
    }

    public static class ACLRSCAN implements COLRSCANF_T {

        @Override
        public int colrscanf_t(COLR[] scan, int len, InputStream fp) throws IOException {
            return aclrscan(scan, len, fp);
        }

        static int aclrscan( /* get an ASCII color scanline */
                COLR[] scan,
                int len,
                InputStream fp) throws IOException {
            int scanidx = 0;
            while (len-- > 0) {
                scan[scanidx].data[COLOR.RED] = (byte) normval(scanint(fp));
                scan[scanidx].data[COLOR.GRN] = (byte) normval(scanint(fp));
                scan[scanidx].data[COLOR.BLU] = (byte) normval(scanint(fp));
                scanidx++;
            }
            return (0);
        }
    }

    public static class BCLRSCAN implements COLRSCANF_T {

        @Override
        public int colrscanf_t(COLR[] scan, int len, InputStream fp) throws IOException {
            return bclrscan(scan, len, fp);
        }

        static int bclrscan( /* get a binary color scanline */
                COLR[] scan,
                int len,
                InputStream fp) throws IOException {
            int r, g, b;
            int scanidx = 0;
            while (len-- > 0) {
                r = fp.read();
                g = fp.read();
                if ((b = fp.read()) == EOF) {
                    return (-1);
                }
                if (maxval == 255) {
                    scan[scanidx].data[COLOR.RED] = (byte) r;
                    scan[scanidx].data[COLOR.GRN] = (byte) g;
                    scan[scanidx].data[COLOR.BLU] = (byte) b;
                } else {
                    scan[scanidx].data[COLOR.RED] = (byte) normval(r);
                    scan[scanidx].data[COLOR.GRN] = (byte) normval(g);
                    scan[scanidx].data[COLOR.BLU] = (byte) normval(b);
                }
                scanidx++;
            }
            return (0);
        }
    }

    public static class AGRYSCAN2 implements COLORSCANF_T {

        @Override
        public int colorscanf_t(COLOR[] scan, int len, InputStream fp) throws IOException {
            return agryscan2(scan, len, fp);
        }

        static int agryscan2( /* get an ASCII greyscale scanline */
                COLOR[] scan,
                int len,
                InputStream fp) throws IOException {
            int scanidx = 0;
            while (len-- > 0) {
                scan[scanidx].data[COLOR.RED] =
                        scan[scanidx].data[COLOR.GRN] =
                        scan[scanidx].data[COLOR.BLU] = (float) fltv(scanint(fp));
                scanidx++;
            }
            return (0);
        }
    }

    public static class BGRYSCAN2 implements COLORSCANF_T {

        @Override
        public int colorscanf_t(COLOR[] scan, int len, InputStream fp) throws IOException {
            return bgryscan2(scan, len, fp);
        }

        static int bgryscan2( /* get a binary greyscale scanline */
                COLOR[] scan,
                int len,
                InputStream fp) throws IOException {
            int c;
            int scanidx = 0;
            while (len-- > 0) {
                if ((c = getby2(fp)) == EOF) {
                    return (-1);
                }
                scan[scanidx].data[COLOR.RED] =
                        scan[scanidx].data[COLOR.GRN] =
                        scan[scanidx].data[COLOR.BLU] = (float) fltv(c);
                scanidx++;
            }
            return (0);
        }
    }

    public static class ACLRSCAN2 implements COLORSCANF_T {

        @Override
        public int colorscanf_t(COLOR[] scan, int len, InputStream fp) throws IOException {
            return aclrscan2(scan, len, fp);
        }

        static int aclrscan2( /* get an ASCII color scanline */
                COLOR[] scan,
                int len,
                InputStream fp) throws IOException {
            int scanidx = 0;
            while (len-- > 0) {
                scan[scanidx].data[COLOR.RED] = (float) fltv(scanint(fp));
                scan[scanidx].data[COLOR.GRN] = (float) fltv(scanint(fp));
                scan[scanidx].data[COLOR.BLU] = (float) fltv(scanint(fp));
                scanidx++;
            }
            return (0);
        }
    }

    public static class BCLRSCAN2 implements COLORSCANF_T {

        @Override
        public int colorscanf_t(COLOR[] scan, int len, InputStream fp) throws IOException {
            return bclrscan2(scan, len, fp);
        }

        static int bclrscan2( /* get a binary color scanline */
                COLOR[] scan,
                int len,
                InputStream fp) throws IOException {
            int r, g, b;
            int scanidx = 0;
            while (len-- > 0) {
                r = getby2(fp);
                g = getby2(fp);
                if ((b = getby2(fp)) == EOF) {
                    return (-1);
                }
                scan[scanidx].data[COLOR.RED] = (float) fltv(r);
                scan[scanidx].data[COLOR.GRN] = (float) fltv(g);
                scan[scanidx].data[COLOR.BLU] = (float) fltv(b);
                scanidx++;
            }
            return (0);
        }
    }

    static int scanint( /* scan the next positive integer value */
            InputStream fp) throws IOException {
        int c;
        int i;
//tryagain:
        while (Character.isWhitespace(c = fp.read()))
		;
        if (c == EOF) {
//		quiterr("unexpected end of file");
        }
        if (c == '#') {		/* comment */
            while ((c = fp.read()) != EOF && c != '\n')
			;
//		goto tryagain;
            return scanint(fp);
        }
        /* should be integer */
        i = 0;
        do {
            if (!Character.isDigit(c)) {
                quiterr("error reading integer");
            }
            i = 10 * i + c - '0';
            c = fp.read();
        } while (c != EOF && !Character.isWhitespace(c));
        return (i);
    }

    static int normval( /* normalize a value to [0,255] */
            int v) {
        if (v >= maxval) {
            return (255);
        }
        if (maxval == 255) {
            return (v);
        }
        return ((int) (v * 255L / maxval));
    }

    static int getby2( /* return 2-byte quantity from fp */
            InputStream fp) throws IOException {
        int lowerb, upperb;

        upperb = fp.read();
        lowerb = fp.read();
        if (lowerb == EOF) {
            return (EOF);
        }
        return (upperb << 8 | lowerb);
    }

    static void putby2( /* put 2-byte quantity to fp */
            int w,
            OutputStream fp) throws IOException {
        fp.write(w >> 8 & 0xff);
        fp.write(w & 0xff);
//	if (ferror(fp)) {
//		fprintf(stderr, "%s: write error on PPM output\n", progname);
//		exit(1);
//	}
    }

// unsigned return value
    static int intv( /* return integer quantity for v */
            double v) {
        if (v >= 0.99999) {
            return (maxval);
        }
        if (v <= 0.) {
            return (0);
        }
        return ((int) (v * (maxval + 1)));
    }
}
