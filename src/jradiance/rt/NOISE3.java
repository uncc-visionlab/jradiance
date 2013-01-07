/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.rt;

import jradiance.common.CALFUNC;
import jradiance.common.CALFUNC.LIBF;

/**
 *
 * @author arwillis
 */
public class NOISE3 {
    /*
     *  noise3.c - noise functions for random textures.
     *
     *     Credit for the smooth algorithm goes to Ken Perlin.
     *     (ref. SIGGRAPH Vol 19, No 3, pp 287-96)
     */

    public static final int A = 0;
    public static final int B = 1;
    public static final int C = 2;
    public static final int D = 3;

    static double rand3a(long x, long y, long z) {
        return frand(67 * (x) + 59 * (y) + 71 * (z));
    }

    static double rand3b(long x, long y, long z) {
        return frand(73 * (x) + 79 * (y) + 83 * (z));
    }

    static double rand3c(long x, long y, long z) {
        return frand(89 * (x) + 97 * (y) + 101 * (z));
    }

    static double rand3d(long x, long y, long z) {
        return frand(103 * (x) + 107 * (y) + 109 * (z));
    }
    static String[] noise_name = {"noise3x", "noise3y", "noise3z", "noise3"};
    static String fnoise_name = "fnoise3";
    static String hermite_name = "hermite";
    static long[][] xlim = new long[3][2];
    static double[] xarg = new double[3];
    public static double EPSILON = .001;		/* error allowed in fractal */


    static double frand3(long x, long y, long z) {
        return frand(17 * (x) + 23 * (y) + 29 * (z));
    }

    public static class l_noise3 implements LIBF {

        static double l_noise3( /* compute a noise function */
                String nam) {
            int i;
            double[] x = new double[3];
            /* get point */
            x[0] = CALFUNC.argument(1);
            x[1] = CALFUNC.argument(2);
            x[2] = CALFUNC.argument(3);
            /* make appropriate call */
            if (nam.equals(fnoise_name)) {
                return (fnoise3(x));
            }
            i = 4;
            while (i-- != 0) {
                if (nam.equals(noise_name[i])) {
                    return (noise3(x)[i]);
                }
            }
//	eputs(nam);
//	eputs(": called l_noise3!\n");
//	quit(1);
            System.exit(1);
            return 1; /* pro forma return */
        }

        @Override
        public double fval(String nm) {
            return l_noise3(nm);
        }
    }

    static double hpoly1(double t) {
        return ((2.0 * t - 3.0) * t * t + 1.0);
    }

    static double hpoly2(double t) {
        return (-2.0 * t + 3.0) * t * t;
    }

    static double hpoly3(double t) {
        return ((t - 2.0) * t + 1.0) * t;
    }

    static double hpoly4(double t) {
        return (t - 1.0) * t * t;
    }

    public static class l_hermite implements LIBF {

        static double hermite(double p0, double p1, double r0, double r1, double t) {
            return p0 * hpoly1(t) + p1 * hpoly2(t) + r0 * hpoly3(t) + r1 * hpoly4(t);
        }

        static double l_hermite(String nm) /* library call for hermite interpolation */ {
            double t;

            t = CALFUNC.argument(5);
            return (hermite(CALFUNC.argument(1), CALFUNC.argument(2),
                    CALFUNC.argument(3), CALFUNC.argument(4), t));
        }

        @Override
        public double fval(String nm) {
            return l_hermite(nm);
        }
    }

    static void setnoisefuncs() /* add noise functions to library */ {
        int i;

        CALFUNC.funset(hermite_name, 5, ':', new l_hermite());
        CALFUNC.funset(fnoise_name, 3, ':', new l_noise3());
        i = 4;
        while (i-- != 0) {
            CALFUNC.funset(noise_name[i], 3, ':', new l_noise3());
        }
    }
    static double[] x = {-100000.0, -100000.0, -100000.0};
    static double[] f = new double[4];

    static double[] noise3( /* compute the noise function */
            double[] xnew) {

        if (x[0] == xnew[0] && x[1] == xnew[1] && x[2] == xnew[2]) {
            return (f);
        }
        x[0] = xnew[0];
        x[1] = xnew[1];
        x[2] = xnew[2];
        xlim[0][0] = (long) Math.floor(x[0]);
        xlim[0][1] = xlim[0][0] + 1;
        xlim[1][0] = (long) Math.floor(x[1]);
        xlim[1][1] = xlim[1][0] + 1;
        xlim[2][0] = (long) Math.floor(x[2]);
        xlim[2][1] = xlim[2][0] + 1;
        xarg[0] = x[0] - xlim[0][0];
        xarg[1] = x[1] - xlim[1][0];
        xarg[2] = x[2] - xlim[2][0];
        interpolate(f, 0, 3);
        return (f);
    }

    static void interpolate(
            double[] f,
            int i,
            int n) {
        double[] f0 = new double[4], f1 = new double[4];
        double hp1, hp2;

        if (n == 0) {
            f[A] = rand3a(xlim[0][i & 1], xlim[1][i >> 1 & 1], xlim[2][i >> 2]);
            f[B] = rand3b(xlim[0][i & 1], xlim[1][i >> 1 & 1], xlim[2][i >> 2]);
            f[C] = rand3c(xlim[0][i & 1], xlim[1][i >> 1 & 1], xlim[2][i >> 2]);
            f[D] = rand3d(xlim[0][i & 1], xlim[1][i >> 1 & 1], xlim[2][i >> 2]);
        } else {
            n--;
            interpolate(f0, i, n);
            interpolate(f1, i | 1 << n, n);
            hp1 = hpoly1(xarg[n]);
            hp2 = hpoly2(xarg[n]);
            f[A] = f0[A] * hp1 + f1[A] * hp2;
            f[B] = f0[B] * hp1 + f1[B] * hp2;
            f[C] = f0[C] * hp1 + f1[C] * hp2;
            f[D] = f0[D] * hp1 + f1[D] * hp2
                    + f0[n] * hpoly3(xarg[n]) + f1[n] * hpoly4(xarg[n]);
        }
    }

    static double frand( /* get random number from seed */
            long s) {
        s = s << 13 ^ s;
        return (1.0 - ((s * (s * s * 15731 + 789221) + 1376312589) & 0x7fffffff) / 1073741824.0);
    }

    static double fnoise3( /* compute fractal noise function */
            double[] p) {
        long[] t = new long[3], v = new long[3], beg = new long[3];
        double[] fval = new double[8];
        double fc;
        int branch;
        long s;
        int i, j;
        /* get starting cube */
        s = (long) (1.0 / EPSILON);
        for (i = 0; i < 3; i++) {
            t[i] = (long) (s * p[i]);
            beg[i] = (long) (s * Math.floor(p[i]));
        }
        for (j = 0; j < 8; j++) {
            for (i = 0; i < 3; i++) {
                v[i] = beg[i];
                if ((j & 1 << i) != 0) {
                    v[i] += s;
                }
            }
            fval[j] = frand3(v[0], v[1], v[2]);
        }
        /* compute fractal */
        for (;;) {
            fc = 0.0;
            for (j = 0; j < 8; j++) {
                fc += fval[j];
            }
            fc *= 0.125;
            if ((s >>= 1) == 0) {
                return (fc);		/* close enough */
            }
            branch = 0;
            for (i = 0; i < 3; i++) {	/* do center */
                v[i] = beg[i] + s;
                if (t[i] > v[i]) {
                    branch |= 1 << i;
                }
            }
            fc += s * EPSILON * frand3(v[0], v[1], v[2]);
            fval[~branch & 7] = fc;
            for (i = 0; i < 3; i++) {	/* do faces */
                if ((branch & 1 << i) != 0) {
                    v[i] += s;
                } else {
                    v[i] -= s;
                }
                fc = 0.0;
                for (j = 0; j < 8; j++) {
                    if ((~(j ^ branch) & 1 << i) != 0) {
                        fc += fval[j];
                    }
                }
                fc = 0.25 * fc + s * EPSILON * frand3(v[0], v[1], v[2]);
                fval[~(branch ^ 1 << i) & 7] = fc;
                v[i] = beg[i] + s;
            }
            for (i = 0; i < 3; i++) {	/* do edges */
                if ((j = i + 1) == 3) {
                    j = 0;
                }
                if ((branch & 1 << j) != 0) {
                    v[j] += s;
                } else {
                    v[j] -= s;
                }
                if (++j == 3) {
                    j = 0;
                }
                if ((branch & 1 << j) != 0) {
                    v[j] += s;
                } else {
                    v[j] -= s;
                }
                fc = fval[branch & ~(1 << i)];
                fc += fval[branch | 1 << i];
                fc = 0.5 * fc + s * EPSILON * frand3(v[0], v[1], v[2]);
                fval[branch ^ 1 << i] = fc;
                if ((j = i + 1) == 3) {
                    j = 0;
                }
                v[j] = beg[j] + s;
                if (++j == 3) {
                    j = 0;
                }
                v[j] = beg[j] + s;
            }
            for (i = 0; i < 3; i++) /* new cube */ {
                if ((branch & 1 << i) != 0) {
                    beg[i] += s;
                }
            }
        }
    }
}
