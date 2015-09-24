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
package jradiance.rt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JFrame;
import jradiance.common.COLORS.COLOR;

/**
 *
 * @author arwillis
 */
public class X11 extends DRIVER {
    /*
     *  x11.c - driver for X-windows version 11
     */

    public static final double GAMMA = 2.2;		/* default exponent correction */

    public static final String COMFN = "8x13";		/* command line font name */

    public static final int COMLH = 3;		/* number of command lines */

    public static final int COMCW = 8;		/* approx. character width (pixels) */

    public static final int COMCH = 14;		/* approx. character height (pixels) */

    public static final int MINWIDTH = (32 * COMCW);	/* minimum graphics window width */

    public static final int MINHEIGHT = (MINWIDTH / 2);	/* minimum graphics window height */

    public static final int BORWIDTH = 5;		/* border width */

    public static final int COMHEIGHT = (COMLH * COMCH);	/* command line height (pixels) */

//#define  ourscreen	DefaultScreen(ourdisplay)
//#define  ourroot	RootWindow(ourdisplay,ourscreen)
//
//#define  levptr(etype)	((etype *)&currentevent)
//static XEvent  currentevent;		/* current event */
    static int ncolors = 0;		/* color table size */

    static int mapped = 0;			/* window is mapped? */

    static long[] pixval = null;	/* allocated pixels */

    static long ourblack = 0, ourwhite = 1;
//static Display  *ourdisplay = NULL;	/* our display */
//
//static XVisualInfo  ourvinfo;		/* our visual information */
//
//static Window  gwind = 0;		/* our graphics window */
//static Cursor  pickcursor = 0;		/* cursor used for picking */
    static int gwidth, gheight;		/* graphics window size */

    static int comheight;			/* desired comline height */
//static TEXTWIND  *comline = NULL;	/* our command line */

    static char[] c_queue = new char[64];		/* input queue */

    static int c_first = 0;		/* first character in queue */

    static int c_last = 0;			/* last character in queue */

//static GC  ourgc = 0;			/* our graphics context for drawing */
//static Colormap ourmap = 0;		/* our color map */
//#define IC_X11		0
//#define IC_IOCTL	1
//#define IC_READ		2
    static int inpcheck;			/* whence to check input */


    class MyCanvas extends JComponent {
//        BufferedImage bi = null;
//    public void makeImage() {
//        bi = new BufferedImage(gwidth, gheight, BufferedImage.TYPE_3BYTE_BGR);
//    }        
//        BufferedImage bi = new BufferedImage(gwidth, gheight, BufferedImage.TYPE_INT_RGB);        

        Rectangle r;
        Color c;

        public void setVals(Rectangle r, Color c) {
            this.r = r;
            this.c = c;
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(c);
            g2.fill(r);
        }

        public void draw(Color c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) this.getGraphics();
//            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//                    RenderingHints.VALUE_ANTIALIAS_ON);
            this.c = c;
            this.r = r;
            g2.setPaint(c);
            g2.fill(r);
//            paintComponent(g2);
//            paintImmediately(r);
//            repaint(r);
//            paint(g2);
            //g2.setPaint(Color.black);
            //g2.drawString("Filled Rectangle2D", x, 250);
//            int rgb = c.getRGB();            
//            byte rv = (byte)((rgb&0xff0000)>>16);
//            byte gv = (byte)((rgb&0xff00)>>8);
//            byte bv = (byte)(rgb&0xff);
//            byte[] pixelByteData = new byte[r.width*r.height*3];
//            for (int pixidx=0; pixidx < r.width*r.height; pixidx+=3) {
//                pixelByteData[pixidx]=rv;
//                pixelByteData[pixidx+1]=gv;
//                pixelByteData[pixidx+2]=bv;                
//            }
//            DataBufferByte dataBuffer = new DataBufferByte(pixelByteData, (r.width * r.height * 3), 0);
//            int[] bandOffsets = {0, 1, 2};
//            SampleModel sampleModel = new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE,
//                    r.width, r.height,
//                    3, r.width * 3, bandOffsets);
//            Point origin = new Point(r.x, r.y);
//            WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, origin);
//            bi.setData(raster);
//            Graphics g = bi.getGraphics();
//            g.drawImage(bi, 0, 0, gwidth, gheight, Color.WHITE, null);
        }
    }
    MyCanvas canvas = null;
    JFrame gwind = null;

    public X11() {
        pixaspect = 1.0;
    }
//static void x11_errout(char  *msg);

//static dr_closef_t x11_close;
//static dr_clearf_t x11_clear;
//static dr_paintrf_t x11_paintr;
//static dr_getcurf_t x11_getcur;
//static dr_comoutf_t x11_comout;
//static dr_cominf_t x11_comin;
//static dr_flushf_t x11_flush;
//
//static dr_cominf_t std_comin;
//static dr_comoutf_t std_comout;
//static struct driver  x11_driver = {
//	x11_close, x11_clear, x11_paintr, x11_getcur,
//	NULL, NULL, x11_flush, 1.0
//};
//
//static dr_getchf_t x11_getc;
//
//static void freepixels(void);
//static int getpixels(void);
//static dr_newcolrf_t xnewcolr;
//static unsigned long true_pixel(COLOR  col);
//static void getevent(void);
//static void getkey(XKeyPressedEvent  *ekey);
//static void fixwindow(XExposeEvent  *eexp);
//static void resizewindow(XConfigureEvent  *ersz);
//extern dr_initf_t x11_init; /* XXX this should be in a seperate header file */
    public void x11_init( /* initialize driver */
            String name,
            String id) {
        /* set gamma */
//	if ((gv = XGetDefault(ourdisplay, "radiance", "gamma")) != NULL
//			|| (gv = getenv("DISPLAY_GAMMA")) != NULL)
//		make_gmap(atof(gv));
//	else
        COLORTAB.make_gmap(GAMMA);

        Toolkit javakit = java.awt.Toolkit.getDefaultToolkit();
        Dimension scrsize = javakit.getScreenSize();
//        if (scrsize.width > 1600) {
//            scrsize.width = 1280;
//            scrsize.height = 1024;
//        }
//        if (scrsize.height > 1200) {
//            scrsize.width = 1280;
//            scrsize.height = 1024;
//        }
        gwind = new JFrame();
//        Dimension framesize = new Dimension((int) (0.7f * scrsize.width), (int) (0.7f * scrsize.height));
        Dimension framesize = new Dimension(scrsize.width - 10, scrsize.height - 102);
        canvas = new MyCanvas();
        gwind.getContentPane().add(canvas);
        //gwind.getContentPane().setLayout(new BorderLayout());
        //this.setPreferredSize(framesize);
        //canvas = new Renderer(this,framesize.width/2,framesize.height,config);
        //canvas = new Renderer(this, (int) (0.65f * framesize.width), (int) (0.7f * framesize.height), bestGC);
        gwind.setSize(framesize);
        gwind.setVisible(true);
//	/* create a cursor */
//	pickcursor = XCreateFontCursor(ourdisplay, XC_diamond_cross);
//	ourgc = XCreateGC(ourdisplay, gwind, 0, NULL);
//	ourxwmhints.flags = InputHint|IconPixmapHint;
//	ourxwmhints.input = True;
//	ourxwmhints.icon_pixmap = XCreateBitmapFromData(ourdisplay,
//			gwind, x11icon_bits, x11icon_width, x11icon_height);
//	XSetWMHints(ourdisplay, gwind, &ourxwmhints);
//	oursizhints.min_width = MINWIDTH;
//	oursizhints.min_height = MINHEIGHT+comheight;
//	oursizhints.flags = PMinSize;
//	XSetNormalHints(ourdisplay, gwind, &oursizhints);
//	XSelectInput(ourdisplay, gwind, ExposureMask);
//	XMapWindow(ourdisplay, gwind);
//	XWindowEvent(ourdisplay, gwind, ExposureMask, levptr(XEvent));
        gwidth = framesize.width;
        gheight = framesize.height;
        xsiz = gwidth < MINWIDTH ? MINWIDTH : gwidth;
        ysiz = gheight < MINHEIGHT ? MINHEIGHT : gheight;
//        canvas.makeImage();
        inpready = 0;
        mapped = 1;
//					/* set i/o vectors */
//	if (comheight) {
//		x11_driver.comin = x11_comin;
//		x11_driver.comout = x11_comout;
//		erract[COMMAND].pf = x11_comout;
//		/*			doesn't work with raypcalls.c
//		if (erract[WARNING].pf != NULL)
//			erract[WARNING].pf = x11_errout;
//		*/
//		inpcheck = IC_X11;
//	} else {
//		x11_driver.comin = std_comin;
//		x11_driver.comout = std_comout;
//		erract[COMMAND].pf = std_comout;
//		inpcheck = IC_IOCTL;
//	}
//	return(&x11_driver);
    }

    @Override
    void close() throws IOException /* close our display */ {
//	erract[COMMAND].pf = NULL;		/* reset error vectors */
//	if (erract[WARNING].pf != NULL)
//		erract[WARNING].pf = wputs;
//	if (ourdisplay == NULL)
//		return;
//	if (comline != NULL) {
//		xt_close(comline);
//		comline = NULL;
//	}
//	freepixels();
//	XFreeGC(ourdisplay, ourgc);
//	XDestroyWindow(ourdisplay, gwind);
//	gwind = 0;
//	ourgc = 0;
//	XFreeCursor(ourdisplay, pickcursor);
//	XCloseDisplay(ourdisplay);
//	ourdisplay = NULL;
    }

    @Override
    void clear( /* clear our display */
            int xres,
            int yres) throws IOException {
        /* check limits */
        if (xres < MINWIDTH) {
            xres = MINWIDTH;
        }
        if (yres < MINHEIGHT) {
            yres = MINHEIGHT;
        }
        /* resize window */
        if (xres != gwidth || yres != gheight) {
            gwind.setSize(xres, yres);
//		XSelectInput(ourdisplay, gwind, 0);
//		XResizeWindow(ourdisplay, gwind, xres, yres+comheight);
            gwidth = xres;
            gheight = yres;
        }
//	XClearWindow(ourdisplay, gwind);
//						/* reinitialize color table */
//	if (ourvinfo.class == PseudoColor || ourvinfo.class == GrayScale) {
//		if (getpixels() == 0)
//			eputs("cannot allocate colors\n");
//		else
//			new_ctab(ncolors);
//	}
//						/* get new command line */
//	if (comline != NULL)
//		xt_close(comline);
//	if (comheight) {
//		comline = xt_open(ourdisplay, gwind, 0, gheight, gwidth,
//				comheight, 0, ourblack, ourwhite, COMFN);
//		if (comline == NULL) {
//			eputs("cannot open command line window\n");
//			quit(1);
//		}
//		XSelectInput(ourdisplay, comline->w, ExposureMask);
//						/* remove earmuffs */
//		XSelectInput(ourdisplay, gwind,
//		StructureNotifyMask|ExposureMask|KeyPressMask|ButtonPressMask);
//	} else					/* remove earmuffs */
//		XSelectInput(ourdisplay, gwind,
//			StructureNotifyMask|ExposureMask|ButtonPressMask);
    }

    @Override
    void paintr( /* fill a rectangle */
            COLOR col,
            int xmin,
            int ymin,
            int xmax,
            int ymax) throws IOException {
        long pixel;

        if (mapped == 0) {
            return;
        }
//	if (ncolors > 0)
//		pixel = pixval[get_pixel(col, xnewcolr)];
//	else
        pixel = true_pixel(col);
        Color c = new Color((int) pixel);
        Rectangle r = new Rectangle(xmin, gheight - ymax, xmax - xmin, ymax - ymin);
        canvas.draw(c, r);
        try {
            //do what you want to do before sleeping
            Thread.sleep(5);//sleep for 1000 ms
            //do what you want to do after sleeptig
        } catch (InterruptedException ie) {
            //If this thread was intrrupted by another thread 
        }
    }

    @Override
    void flush() throws IOException /* flush output */ {
        char[] buf = new char[256];
        int n;
        /* check for input */
//	XNoOp(ourdisplay);
//	n = XPending(ourdisplay);			/* from X server */
//	while (n-- > 0)
//		getevent();
//#ifdef FNDELAY
//	if (inpcheck == IC_IOCTL) {			/* from stdin */
//#ifdef FIONREAD
//		if (ioctl(fileno(stdin), FIONREAD, &n) < 0) {
//#else
//		if (1) {
//#endif
//			if (fcntl(fileno(stdin), F_SETFL, FNDELAY) < 0) {
//				eputs("cannot change input mode\n");
//				quit(1);
//			}
//			inpcheck = IC_READ;
//		} else
//			x11_driver.inpready += n;
//	}
//	if (inpcheck == IC_READ) {
//		n = read(fileno(stdin), buf, sizeof(buf)-1);
//		if (n > 0) {
//			buf[n] = '\0';
//			tocombuf(buf, &x11_driver);
//		}
//	}
//#endif
    }

    @Override
    void comin( /* read in a command line */
            char[] inp,
            char[] prompt) throws IOException {
        if (prompt != null) {
            flush();		/* make sure we get everything */
//		if (fromcombuf(inp, &x11_driver))
//			return;
            System.out.print(prompt);
            System.out.print(inp);
        }
//	xt_cursor(comline, TBLKCURS);
//	editline(inp, x11_getc, x11_comout);
//	xt_cursor(comline, TNOCURS);
    }

    @Override
    void comout( /* output a string to command line */
            char[] outp) throws IOException {
//	if (comline == NULL || outp == NULL || !outp[0])
//		return;
//	xt_puts(outp, comline);
//	if (outp[strlen(outp)-1] == '\n')
//		XFlush(ourdisplay);
    }

    static void x11_errout( /* output an error message */
            char[] msg) {
//	eputs(msg);		/* send to stderr also! */
//	x11_comout(msg);
    }

    static void std_comin( /* read in command line from stdin */
            char[] inp,
            char[] prompt) {
//	if (prompt != NULL) {
//		if (fromcombuf(inp, &x11_driver))
//			return;
//		if (!x11_driver.inpready)
//			std_comout(prompt);
//	}
//#ifdef FNDELAY
//	if (inpcheck == IC_READ) {	/* turn off FNDELAY */
//		if (fcntl(fileno(stdin), F_SETFL, 0) < 0) {
//			eputs("cannot change input mode\n");
//			quit(1);
//		}
//		inpcheck = IC_IOCTL;
//	}
//#endif
//	if (gets(inp) == NULL) {
//		strcpy(inp, "quit");
//		return;
//	}
//	x11_driver.inpready -= strlen(inp) + 1;
//	if (x11_driver.inpready < 0)
//		x11_driver.inpready = 0;
    }

    static void std_comout( /* write out string to stdout */
            char[] outp) {
        System.out.print(outp);
        System.out.flush();
    }

    @Override
    int getcur( /* get cursor position */
            int[] xp,
            int[] yp) throws IOException {
//	while (XGrabPointer(ourdisplay, gwind, True, ButtonPressMask,
//			GrabModeAsync, GrabModeAsync, None, pickcursor,
//			CurrentTime) != GrabSuccess)
//		sleep(2);
//
//	do
//		getevent();
//	while (c_last <= c_first && levptr(XEvent)->type != ButtonPress);
//	*xp = levptr(XButtonPressedEvent)->x;
//	*yp = gheight-1 - levptr(XButtonPressedEvent)->y;
//	XUngrabPointer(ourdisplay, CurrentTime);
//	XFlush(ourdisplay);				/* insure release */
//	if (c_last > c_first)			/* key pressed */
//		return(x11_getc());
//						/* button pressed */
//	if (levptr(XButtonPressedEvent)->button == Button1)
//		return(MB1);
//	if (levptr(XButtonPressedEvent)->button == Button2)
//		return(MB2);
//	if (levptr(XButtonPressedEvent)->button == Button3)
//		return(MB3);
        return (ABORT);
    }

    static void xnewcolr( /* enter a color into hardware table */
            int ndx,
            int r,
            int g,
            int b) {
//	XColor  xcolor;
//
//	xcolor.pixel = pixval[ndx];
//	xcolor.red = r << 8;
//	xcolor.green = g << 8;
//	xcolor.blue = b << 8;
//	xcolor.flags = DoRed|DoGreen|DoBlue;
//
//	XStoreColor(ourdisplay, ourmap, &xcolor);
    }

    static int getpixels() /* get the color map */ {
//	XColor  thiscolor;
//	register int  i, j;
//
//	if (ncolors > 0)
//		return(ncolors);
//	if (ourvinfo.visual == DefaultVisual(ourdisplay,ourscreen)) {
//		ourmap = DefaultColormap(ourdisplay,ourscreen);
//		goto loop;
//	}
//newmap:
//	ourmap = XCreateColormap(ourdisplay,gwind,ourvinfo.visual,AllocNone);
//loop:
//	for (ncolors = ourvinfo.colormap_size;
//			ncolors > ourvinfo.colormap_size/3;
//			ncolors = ncolors*.937) {
//		pixval = (unsigned long *)malloc(ncolors*sizeof(unsigned long));
//		if (pixval == NULL)
//			return(ncolors = 0);
//		if (XAllocColorCells(ourdisplay,ourmap,0,NULL,0,pixval,ncolors))
//			break;
//		free((void *)pixval);
//		pixval = NULL;
//	}
//	if (pixval == NULL) {
//		if (ourmap == DefaultColormap(ourdisplay,ourscreen))
//			goto newmap;		/* try it with our map */
//		else
//			return(ncolors = 0);	/* failed */
//	}
//	if (ourmap != DefaultColormap(ourdisplay,ourscreen))
//		for (i = 0; i < ncolors; i++) {	/* reset black and white */
//			if (pixval[i] != ourblack && pixval[i] != ourwhite)
//				continue;
//			thiscolor.pixel = pixval[i];
//			thiscolor.flags = DoRed|DoGreen|DoBlue;
//			XQueryColor(ourdisplay,
//					DefaultColormap(ourdisplay,ourscreen),
//					&thiscolor);
//			XStoreColor(ourdisplay, ourmap, &thiscolor);
//			for (j = i; j+1 < ncolors; j++)
//				pixval[j] = pixval[j+1];
//			ncolors--;
//			i--;
//		}
//	XSetWindowColormap(ourdisplay, gwind, ourmap);
        return (ncolors);
    }

    static void freepixels() /* free our pixels */ {
//	if (ncolors == 0)
//		return;
//	XFreeColors(ourdisplay,ourmap,pixval,ncolors,0L);
//	free((void *)pixval);
//	pixval = NULL;
//	ncolors = 0;
//	if (ourmap != DefaultColormap(ourdisplay,ourscreen))
//		XFreeColormap(ourdisplay, ourmap);
//	ourmap = 0;
    }

    static long true_pixel( /* return true pixel value for color */
            COLOR col) {
        long rval = 0;
        byte[] rgb = new byte[3];

        COLORTAB.map_color(rgb, col);
//	rval = ourvinfo.red_mask*rgb[COLOR.RED]/255 & ourvinfo.red_mask;
//	rval |= ourvinfo.green_mask*rgb[COLOR.GRN]/255 & ourvinfo.green_mask;
//	rval |= ourvinfo.blue_mask*rgb[COLOR.BLU]/255 & ourvinfo.blue_mask;
        rval = ((rgb[COLOR.RED] << 16) & 0xff0000) | ((rgb[COLOR.GRN] << 8) & 0xff00) | (rgb[COLOR.BLU] & 0xff);
        return (rval);
    }

    static int x11_getc() /* get a command character */ {
//	while (c_last <= c_first) {
//		c_first = c_last = 0;		/* reset */
//		getevent();			/* wait for key */
//	}
//	x11_driver.inpready--;
//	return(c_queue[c_first++]);
        return 0;
    }

    static void getevent() /* get next event */ {
//	XNextEvent(ourdisplay, levptr(XEvent));
//	switch (levptr(XEvent)->type) {
//	case ConfigureNotify:
//		resizewindow(levptr(XConfigureEvent));
//		break;
//	case UnmapNotify:
//		mapped = 0;
//		freepixels();
//		break;
//	case MapNotify:
//		if (ourvinfo.class == PseudoColor ||
//				ourvinfo.class == GrayScale) {
//			if (getpixels() == 0)
//				eputs("cannot allocate colors\n");
//			else
//				new_ctab(ncolors);
//		}
//		mapped = 1;
//		break;
//	case Expose:
//		fixwindow(levptr(XExposeEvent));
//		break;
//	case KeyPress:
//		getkey(levptr(XKeyPressedEvent));
//		break;
//	case ButtonPress:
//		break;
//	}
    }
//
//static void
//getkey(				/* get input key */
//	register XKeyPressedEvent  *ekey
//)
//{
//	register int  n;
//
//	n = XLookupString(ekey, c_queue+c_last, sizeof(c_queue)-c_last,
//				NULL, NULL);
//	c_last += n;
//	x11_driver.inpready += n;
//}
//
//
//static void
//fixwindow(				/* repair damage to window */
//	register XExposeEvent  *eexp
//)
//{
//	char  buf[80];
//
//	if (eexp->window == gwind) {
//		sprintf(buf, "repaint %d %d %d %d\n",
//			eexp->x, gheight - eexp->y - eexp->height,
//			eexp->x + eexp->width, gheight - eexp->y);
//		tocombuf(buf, &x11_driver);
//	} else if (eexp->window == comline->w) {
//		if (eexp->count == 0)
//			xt_redraw(comline);
//	}
//}
//
//
//static void
//resizewindow(			/* resize window */
//	register XConfigureEvent  *ersz
//)
//{
//	if (ersz->width == gwidth && ersz->height-comheight == gheight)
//		return;
//
//	gwidth = ersz->width;
//	gheight = ersz->height-comheight;
//	x11_driver.xsiz = gwidth < MINWIDTH ? MINWIDTH : gwidth;
//	x11_driver.ysiz = gheight < MINHEIGHT ? MINHEIGHT : gheight;
//
//	tocombuf("new\n", &x11_driver);
//}
}
