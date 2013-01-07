/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author arwillis
 */
public class PORTIO {
//#ifndef lint
//static const char	RCSid[] = "$Id$";
//#endif
/*
     * Portable i/o for binary files
     *
     * External symbols declared in rtio.h
     */

//#include "copyright.h"
//
//#include "rtio.h"
//
//#include <math.h>
//
//#ifdef getc_unlocked		/* avoid horrendous overhead of flockfile */
//#undef getc
//#undef putc
//#define getc    getc_unlocked
//#define putc    putc_unlocked
//#endif
    public static void putstr( /* write null-terminated string to fp */
            String s,
            OutputStream fp) throws IOException {
        byte[] b = s.getBytes();
        fp.write(b);
        if (b[b.length - 1] != '\0') {
            fp.write('\0');
        }
    }

    public static void putint( /* write a siz-byte integer to fp */
            long i,
            int siz,
            OutputStream fp) throws IOException {
        while (siz-- != 0) {
            fp.write((int) (i >> (siz << 3) & 0xff));
        }
    }

    public static void putflt( /* put out floating point number */
            double f,
            OutputStream fp) throws IOException {
        long m;
        int e;

        FRexpResult fr = frexp3(f);
        e = fr.exponent;
        m = (long) (fr.mantissa * (0x7fffffff));
        //m = frexp(f, &e) * 0x7fffffff;
        if (e > 127) {			/* overflow */
            m = m > 0 ? (long) 0x7fffffff : -(long) 0x7fffffff;
            e = 127;
        } else if (e < -128) {		/* underflow */
            m = 0;
            e = 0;
        }
        putint(m, 4, fp);
        putint((long) e, 1, fp);
    }

    public static String getstr( /* get null-terminated string */
            char[] s,
            InputStream fp) throws IOException {
        char[] cp;
        int c, cpidx = 0;

        cp = s;
        while ((c = fp.read()) != -1) {
            if ((cp[cpidx++] = (char) c) == '\0') {
                return (new String(cp).substring(0, cpidx - 1));
            }
        }

        return (null);
    }

    public static long getint( /* get a siz-byte integer */
            int siz,
            InputStream fp) throws IOException {
        int c;
        long r;

        if ((c = fp.read()) == -1) {
            return (-1);
        }
        r = ((0x80 & c) != 0) ? -1 << 8 | c : c;		/* sign extend */
        while (--siz > 0) {
            if ((c = fp.read()) == -1) {
                return (-1);
            }
            r <<= 8;
            r |= c;
        }
        return (r);
    }

    public static double getflt( /* get a floating point number */
            InputStream fp) throws IOException {
        long l;
        double d;

        l = getint(4, fp);
        if (l == 0) {
            fp.read();		/* exactly zero -- ignore exponent */
            return (0.0);
        }
        d = (l + (l > 0 ? .5 : -.5)) * (1. / 0x7fffffff);
        return (ldexp(d, (int) getint(1, fp)));
        //return d * Math.pow(2, getint(1, fp));
    }

    public static double ldexp(double d, int e) {
        return d * Math.pow(2, e);
    }

    public static class FRexpResult {

        public int exponent = 0;
        public double mantissa = 0.;
    }

    public static FRexpResult frexp(double value) {
        final FRexpResult result = new FRexpResult();
        long bits = Double.doubleToLongBits(value);
        double realMant = 1.;

        // Test for NaN, infinity, and zero.
        if (Double.isNaN(value)
                || value + value == value
                || Double.isInfinite(value)) {
            result.exponent = 0;
            result.mantissa = value;
        } else {

            boolean neg = (bits < 0);
            int exponent = (int) ((bits >> 52) & 0x7ffL);
            long mantissa = bits & 0xfffffffffffffL;

            if (exponent == 0) {
                exponent++;
            } else {
                mantissa = mantissa | (1L << 52);
            }

            // bias the exponent - actually biased by 1023.
            // we are treating the mantissa as m.0 instead of 0.m
            //  so subtract another 52.
            exponent -= 1075;
            realMant = mantissa;

            // normalize
            while (realMant > 1.0) {
                mantissa >>= 1;
                realMant /= 2.;
                exponent++;
            }

            if (neg) {
                realMant = realMant * -1;
            }

            result.exponent = exponent;
            result.mantissa = realMant;
        }
        return result;
    }

    static final class f64 {

        static final int F64_SIGN_SHIFT = 31;
        static final int F64_SIGN_MASK = 1;
        static final int F64_EXP_SHIFT = 20;
        static final int F64_EXP_MASK = 0x7ff;
        static final int F64_EXP_BIAS = 1023;
        static final int F64_EXP_MAX = 2047;
        static final int F64_MANT_SHIFT = 0;
        static final int F64_MANT_MASK = 0xfffff;

        final int F64_GET_SIGN() {
            return ((high_word >> F64_SIGN_SHIFT)
                    & F64_SIGN_MASK);
        }

        final int F64_GET_EXP() {
            return ((high_word >> F64_EXP_SHIFT)
                    & F64_EXP_MASK);
        }

        final int F64_SET_EXP(int val) {
            return (high_word = (high_word
                    & ~(F64_EXP_MASK << F64_EXP_SHIFT))
                    | (((val) & F64_EXP_MASK) << F64_EXP_SHIFT));
        }

        final int F64_GET_MANT_LOW() {
            return (low_word);
        }

        final int F64_SET_MANT_LOW(int val) {
            return (low_word = (val));
        }

        final int F64_GET_MANT_HIGH() {
            return ((high_word >> F64_MANT_SHIFT)
                    & F64_MANT_MASK);
        }

        final int F64_SET_MANT_HIGH(int val) {
            return (high_word = (high_word
                    & ~(F64_MANT_MASK << F64_MANT_SHIFT))
                    | (((val) & F64_MANT_MASK) << F64_MANT_SHIFT));
        }

        void multiply(double factor) {
            double val = getValue();
            val *= factor;
            setValue(val);
        }
        int low_word;
        int high_word;

        f64(double val) {
            setValue(val);
        }

        void setValue(double val) {
            long ii = Double.doubleToLongBits(val);
            high_word = (int) ii >> 16;
            low_word = (int) ii & 0x0000ffff;
        }

        double getValue() {
            return Double.longBitsToDouble((high_word << 16) | low_word);
        }
    }

    public static FRexpResult frexp3(double v) {
        int i = 0;
        if (v != 0.0) {
            int sign = 1;
            if (v < 0) {
                sign = -1;
                v = -v;
            }
            // slow...
            while (v < 0.5) {
                v = v * 2.0;
                i = i - 1;
            }
            while (v >= 1.0) {
                v = v * 0.5;
                i = i + 1;
            }
            v = v * sign;
        }
        FRexpResult result = new FRexpResult();
        result.exponent = i;
        result.mantissa = v;
        return result;
    }

//     public static double ldexp(double v, int w) {
//         return check(v * Math.pow(2.0, w));
//     }
    private static double check(double v) {
//         if (Double.isNaN(v))
//             throw Exception("math domain error");
//         if (Double.isInfinite(v))
//             throw Exception("math range error");
        return v;
    }

    public static FRexpResult frexp2(
            double value) {
        FRexpResult result = new FRexpResult();
        f64 f64p;
        int exp, exp_bias;
        double factor;

        f64p = new f64(value);
        exp_bias = 0;

        exp = f64p.F64_GET_EXP();
        if (exp == f64.F64_EXP_MAX) {       /* Either infinity or Nan */
            result.exponent = 0;
            result.mantissa = f64p.getValue();
            return result;
//                 return value;
        }
        if (exp == 0) {
            /* Either 0 or denormal */
            if (f64p.F64_GET_MANT_LOW() == 0
                    && f64p.F64_GET_MANT_HIGH() == 0) {
                result.exponent = 0;
                result.mantissa = f64p.getValue();
                return result;
//                         return value;
            }

            /* Multiply by 2^64 */
            factor = 65536.0;        /* 2^16 */
            factor *= factor;       /* 2^32 */
            factor *= factor;       /* 2^64 */
            f64p.multiply(factor);
            exp_bias = 64;
            exp = f64p.F64_GET_EXP();
        }

        exp = exp - f64.F64_EXP_BIAS - exp_bias + 1;
        result.exponent = exp;
        f64p.F64_SET_EXP(f64.F64_EXP_BIAS - 1);
        result.mantissa = value;
        return result;
//         return value;
    }
}
