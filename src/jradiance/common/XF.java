/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

import jradiance.common.RTMATH.FULLXF;

/**
 *
 * @author arwillis
 */
public class XF {
    /*
     *  xf.c - routines to convert transform arguments into 4X4 matrix.
     *
     *  External symbols declared in rtmath.h
     */
    /* regular transformation */
//    public static class XF {

        public MAT4 xfm = new MAT4();				/* transform matrix */

        public double sca = 1.0;				/* scalefactor */
        
//    }
    
    static double d2r(double a) {
        return ((Math.PI / 180.) * (a));
    }

//#define  checkarg(a,l)	if (av[i][a] || badarg(ac-i-1,av+i+1,l)) goto done
    public static int xf( /* get transform specification */
            XF ret,
            int ac,
            String[] av) {
        MAT4 xfmat = new MAT4(), m4 = new MAT4();
        double xfsca, dtmp;
        int i, icnt;

        MAT4.setident4(ret.xfm);
        ret.sca = 1.0;

        icnt = 1;
        MAT4.setident4(xfmat);
        xfsca = 1.0;

        for (i = 0; i < ac && av[i].charAt(0) == '-'; i++) {

            MAT4.setident4(m4);

            switch (av[i].charAt(1)) {

                case 't':			/* translate */
//			checkarg(2,"fff");
                    m4.data[3][0] = Float.parseFloat(av[++i]);
                    m4.data[3][1] = Float.parseFloat(av[++i]);
                    m4.data[3][2] = Float.parseFloat(av[++i]);
                    break;

                case 'r':			/* rotate */
                    switch (av[i].charAt(2)) {
                        case 'x':
//				checkarg(3,"f");
                            dtmp = d2r(Float.parseFloat(av[++i]));
                            m4.data[1][1] = m4.data[2][2] = Math.cos(dtmp);
                            m4.data[2][1] = -(m4.data[1][2] = Math.sin(dtmp));
                            break;
                        case 'y':
//				checkarg(3,"f");
                            dtmp = d2r(Float.parseFloat(av[++i]));
                            m4.data[0][0] = m4.data[2][2] = Math.cos(dtmp);
                            m4.data[0][2] = -(m4.data[2][0] = Math.sin(dtmp));
                            break;
                        case 'z':
//				checkarg(3,"f");
                            dtmp = d2r(Float.parseFloat(av[++i]));
                            m4.data[0][0] = m4.data[1][1] = Math.cos(dtmp);
                            m4.data[1][0] = -(m4.data[0][1] = Math.sin(dtmp));
                            break;
                        default:
//				goto done;
                    }
                    break;

                case 's':			/* scale */
//			checkarg(2,"f");
                    dtmp = Float.parseFloat(av[i + 1]);
//			if (dtmp == 0.0) goto done;
                    i++;
                    xfsca *=
                            m4.data[0][0] =
                            m4.data[1][1] =
                            m4.data[2][2] = dtmp;
                    break;

                case 'm':			/* mirror */
                    switch (av[i].charAt(2)) {
                        case 'x':
//				checkarg(3,"");
                            xfsca *=
                                    m4.data[0][0] = -1.0;
                            break;
                        case 'y':
//				checkarg(3,"");
                            xfsca *=
                                    m4.data[1][1] = -1.0;
                            break;
                        case 'z':
//				checkarg(3,"");
                            xfsca *=
                                    m4.data[2][2] = -1.0;
                            break;
                        default:
//				goto done;
                    }
                    break;

                case 'i':			/* iterate */
//			checkarg(2,"i");
                    while (icnt-- > 0) {
                        MAT4.multmat4(ret.xfm, ret.xfm, xfmat);
                        ret.sca *= xfsca;
                    }
                    icnt = Integer.parseInt(av[++i]);
                    MAT4.setident4(xfmat);
                    xfsca = 1.0;
                    continue;

                default:
//			goto done;

            }
            MAT4.multmat4(xfmat, xfmat, m4);
        }
        done:
        while (icnt-- > 0) {
            MAT4.multmat4(ret.xfm, ret.xfm, xfmat);
            ret.sca *= xfsca;
        }
        return (i);
    }

    public static int invxf( /* invert transform specification */
            XF ret,
            int ac,
            String[] av) {
        MAT4 xfmat = new MAT4(), m4 = new MAT4();
        double xfsca, dtmp;
        int i, icnt;

        MAT4.setident4(ret.xfm);
        ret.sca = 1.0;

        icnt = 1;
        MAT4.setident4(xfmat);
        xfsca = 1.0;

        for (i = 0; i < ac && av[i].charAt(0) == '-'; i++) {

            MAT4.setident4(m4);

            switch (av[i].charAt(1)) {

                case 't':			/* translate */
//			checkarg(2,"fff");
                    m4.data[3][0] = -Float.parseFloat(av[++i]);
                    m4.data[3][1] = -Float.parseFloat(av[++i]);
                    m4.data[3][2] = -Float.parseFloat(av[++i]);
                    break;

                case 'r':			/* rotate */
                    switch (av[i].charAt(2)) {
                        case 'x':
//				checkarg(3,"f");
                            dtmp = -d2r(Float.parseFloat(av[++i]));
                            m4.data[1][1] = m4.data[2][2] = Math.cos(dtmp);
                            m4.data[2][1] = -(m4.data[1][2] = Math.sin(dtmp));
                            break;
                        case 'y':
//				checkarg(3,"f");
                            dtmp = -d2r(Float.parseFloat(av[++i]));
                            m4.data[0][0] = m4.data[2][2] = Math.cos(dtmp);
                            m4.data[0][2] = -(m4.data[2][0] = Math.sin(dtmp));
                            break;
                        case 'z':
//				checkarg(3,"f");
                            dtmp = -d2r(Float.parseFloat(av[++i]));
                            m4.data[0][0] = m4.data[1][1] = Math.cos(dtmp);
                            m4.data[1][0] = -(m4.data[0][1] = Math.sin(dtmp));
                            break;
                        default:
//				goto done;
                    }
                    break;

                case 's':			/* scale */
//			checkarg(2,"f");
                    dtmp = Float.parseFloat(av[i + 1]);
//			if (dtmp == 0.0) goto done;
                    i++;
                    xfsca *=
                            m4.data[0][0] =
                            m4.data[1][1] =
                            m4.data[2][2] = 1.0 / dtmp;
                    break;

                case 'm':			/* mirror */
                    switch (av[i].charAt(2)) {
                        case 'x':
//				checkarg(3,"");
                            xfsca *=
                                    m4.data[0][0] = -1.0;
                            break;
                        case 'y':
//				checkarg(3,"");
                            xfsca *=
                                    m4.data[1][1] = -1.0;
                            break;
                        case 'z':
//				checkarg(3,"");
                            xfsca *=
                                    m4.data[2][2] = -1.0;
                            break;
                        default:
//				goto done;
                    }
                    break;

                case 'i':			/* iterate */
//			checkarg(2,"i");
                    while (icnt-- > 0) {
                        MAT4.multmat4(ret.xfm, xfmat, ret.xfm);
                        ret.sca *= xfsca;
                    }
                    icnt = Integer.parseInt(av[++i]);
                    MAT4.setident4(xfmat);
                    xfsca = 1.0;
                    break;

                default:
//			goto done;

            }
            MAT4.multmat4(xfmat, m4, xfmat);	/* left multiply */
        }
        done:
        while (icnt-- > 0) {
            MAT4.multmat4(ret.xfm, xfmat, ret.xfm);
            ret.sca *= xfsca;
        }
        return (i);
    }

    public static int fullxf( /* compute both forward and inverse */
            FULLXF fx,
            int ac,
            String[] av) {
        xf(fx.f, ac, av);
        return (invxf(fx.b, ac, av));
    }
}
