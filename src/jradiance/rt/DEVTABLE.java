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

import jradiance.rt.DRIVER.DEVICE;

/**
 *
 * @author arwillis
 */
public class DEVTABLE {
/*
 *  devtable.c - device table for rview.
 */
//extern dr_initf_t x11_init;
public static final String  dev_default = "x11";
//#else
//char  dev_default[] = "qt";
//#endif

//#ifdef HAS_QT
//extern dr_initf_t qt_init;
//#endif
public static class X11DEV extends DEVICE {
        public X11DEV(String name, String descrip){ 
            this.descrip = descrip;
            this.name = name;
        }
        @Override
        public DRIVER init(String name, String id) {
            X11 x11driver = new X11();
            x11driver.x11_init(name, id);
            return x11driver;
        }
    }

public static DEVICE[] devtable = {			/* supported devices */
//	{"slave", "Slave driver", slave_init},
//#ifdef HAS_X11
    new X11DEV("x11", "X11 color or greyscale display")
//	{"x11d", "X11 display using stdin/stdout", x11_init},
//#endif
//#ifdef HAS_QT
//	{"qt", "QT display", qt_init},
//#endif
//	{0}					/* terminator */
};
    
}
