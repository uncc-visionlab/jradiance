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
package jradiance.common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Scanner;
import jradiance.rt.DEVCOMM;

/**
 *
 * @author arwillis
 */
public class HEADER {

    static int MAXLINE = 2048;
    static String HDRSTR = "#?";		/* information header magic number */

    static String FMTSTR = "FORMAT=";	/* format identifier */

    static String TMSTR = "CAPDATE=";	/* capture date identifier */

    static String GMTSTR = "GMT=";		/* GMT identifier */


    public static interface gethfunc {

        public int hfunc(char[] s, Object obj) throws IOException;
    }

    static class mycheck implements gethfunc {
//static int
//mycheck(			/* check a header line for format info. */
//	char[]  s,
//	check  cp
//) throws IOException

        @Override
        public int hfunc(char[] s, Object obj) throws IOException {
            HEADER.check cp = (HEADER.check) obj;
            if (HEADER.formatval((cp).fs, s) == 0
                    && (cp).fp != null) {
                fputs(s, (cp).fp);
            }
            return (0);
        }
    }

    public static void newheader( /* identifying line of information header */
            String s,
            OutputStream fp) throws IOException {
        fp.write(HDRSTR.getBytes());
        fp.write(s.getBytes());
        fp.write((int) '\n');
    }

    int headidval( /* get header id (return true if is id) */
            char[] r,
            char[] s) {
        char[] cp = HDRSTR.toCharArray();
        int cpidx = 0, sidx = 0, ridx = 0;
        while (cp[cpidx] != 0) {
            if (cp[cpidx++] != s[sidx++]) {
                return (0);
            }
        }
        if (r == null) {
            return (1);
        }
        while (s[sidx] != 0 && !Character.isWhitespace(s[sidx])) {
            r[ridx++] = s[sidx++];
        }
        r[ridx] = '\0';
        return (1);
    }

    int dateval( /* convert capture date line to UTC */
            Date tloc,
            char[] s) {
//	struct tm	tms;
        char[] cp = TMSTR.toCharArray();
        int cpidx = 0, sidx = 0;
        while (cp[cpidx] != 0) {
            if (cp[cpidx++] != s[sidx++]) {
                return (0);
            }
        }
        while (Character.isWhitespace(s[sidx])) {
            sidx++;
        }
        if (s[sidx] == 0) {
            return (0);
        }

        tloc = new Date();
        Scanner scanner = new Scanner(new String(s));
        Scanner dayScanner = new Scanner(scanner.next());
        Scanner timeScanner = new Scanner(scanner.next());

        dayScanner.useDelimiter(":");
        int day = dayScanner.nextInt();
        int month = dayScanner.nextInt();
        int year = dayScanner.nextInt();

        timeScanner.useDelimiter(":");
        int hour = timeScanner.nextInt();
        int min = timeScanner.nextInt();
        int sec = timeScanner.nextInt();
        Calendar cal = new GregorianCalendar();
        cal.set(year, month, day, hour, min, sec);
        tloc = cal.getTime();
//	if (sscanf(s, "%d:%d:%d %d:%d:%d",
//			&tms.tm_year, &tms.tm_mon, &tms.tm_mday,
//			&tms.tm_hour, &tms.tm_min, &tms.tm_sec) != 6)
//		return(0);
        if (tloc == null) {
            return (1);
        }
//	tms.tm_mon--;
//	tms.tm_year -= 1900;
//	tms.tm_isdst = -1;	/* ask mktime() to figure out DST */
//	*tloc = mktime(&tms);        
        return (1);
    }

    int gmtval( /* convert GMT date line to UTC */
            Date tloc,
            char[] s) {
//	struct tm	tms;
        String cp = GMTSTR;
        return dateval(tloc, s);
//	while (*cp) if (*cp++ != *s++) return(0);
//	while (isspace(*s)) s++;
//	if (!*s) return(0);
//	if (sscanf(s, "%d:%d:%d %d:%d:%d",
//			&tms.tm_year, &tms.tm_mon, &tms.tm_mday,
//			&tms.tm_hour, &tms.tm_min, &tms.tm_sec) != 6)
//		return(0);
//	if (tloc == NULL)
//		return(1);
//	tms.tm_mon--;
//	tms.tm_year -= 1900;
//	*tloc = timegm(&tms);
    }

    public static void fputdate( /* write out the given time value (local & GMT) */
            Date tv,
            OutputStream fp) throws IOException {

        SimpleDateFormat datef = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        String strDate = datef.format(tv);
        fp.write((TMSTR + " " + strDate + "\n").getBytes());
    }

    public static void fputnow( /* write out the current time */
            OutputStream fp) throws IOException {
        Date date = new Date();
        fputdate(date, fp);
    }

    public static void printargs( /* print arguments to a file */
            int ac,
            String[] av,
            OutputStream fp) throws IOException {
        int i = 0;
        while (ac-- > 0) {
            FPUTWORD.fputword(av[i++], fp);
            fp.write((ac != 0) ? ' ' : '\n');
        }
    }

    public static int formatval( /* get format value (return true if format) */
            char[] r,
            char[] s) {
        char[] cp = FMTSTR.toCharArray();
        int cpidx = 0, sidx = 0, ridx = 0;
        while (cpidx < cp.length) {
            if (cp[cpidx++] != s[sidx++]) {
                return (0);
            }
        }
        while (Character.isWhitespace(s[sidx])) {
            sidx++;
        }
        if (s[sidx] == 0) {
            return (0);
        }
        if (r == null) {
            return (1);
        }
        do {
            r[ridx++] = s[sidx++];
        } while (s[sidx] != '\n' && !Character.isWhitespace(s[sidx]));
        r[ridx] = '\0';
        return (1);
    }

    public static void fputformat( /* put out a format value */
            String s,
            OutputStream fp) throws IOException {
        fp.write(FMTSTR.getBytes());
        fp.write(s.getBytes());
        fp.write('\n');
    }

    public static int fputs(char[] str, OutputStream fp) throws IOException {
        int idx = 0;
        while (str[idx] != '\0') {
            fp.write(str[idx++]);
        }
        return idx;
    }

    static char[] fgets(char[] buf, int max, InputStream fp) throws IOException {
        int idx = 0;
        do {
            buf[idx++] = (char) fp.read();
        } while (idx < max && buf[idx - 1] != '\n' && fp.available() > 0);
        if (fp.available() == 0) {
            return null;
        }
        return buf;
    }

    public static int getheader( /* get header from file */
            InputStream fp,
            gethfunc f,
            Object p) throws IOException {
        char[] buf = new char[MAXLINE];

        for (;;) {
            buf[MAXLINE - 2] = '\n';
            if (fgets(buf, MAXLINE, fp) == null) {
                return (-1);
            }
            if (buf[0] == '\n') {
                return (0);
            }
//		if (buf[MAXLINE-2] != '\n') {
//			ungetc(buf[MAXLINE-2], fp);	/* prevent false end */
//			buf[MAXLINE-2] = '\0';
//		}
            if (f != null && f.hfunc(buf, p) < 0) //		if (mycheck(buf, p) < 0)
            {
                return (-1);
            }
        }
    }

    public static class check {

        OutputStream fp;
//	InputStream	ifp;
        char[] fs = new char[64];
    }

    public static int globmatch( /* check for match of s against pattern p */
            char[] p,
            char[] s) {
        int setmatch;
        int pidx = 0, sidx = 0;
        do {
            switch (p[pidx]) {
                case '?':			/* match any character */
                    if (s[sidx++] == 0) {
                        return (0);
                    }
                    break;
                case '*':			/* match any string */
                    while (p[pidx + 1] == '*') {
                        pidx++;
                    }
                    char[] pshift;
                    do {
                        if ((p[pidx + 1] == '?' || p[pidx + 1] == s[sidx])) {
                            pshift = new char[p.length - 1];
                            System.arraycopy(p, 1, pshift, 0, p.length);
                            if (globmatch(pshift, s) != 0) {
                                return (1);
                            }
                        }
                    } while (s[sidx++] != 0);
                    return (0);
                case '[':			/* character set */
                    setmatch = s[sidx] == p[++pidx] ? 1 : 0;
                    if (p[pidx] == 0) {
                        return (0);
                    }
                    while (p[++pidx] != ']') {
                        if (p[pidx] == 0) {
                            return (0);
                        }
                        if (p[pidx] == '-') {
                            setmatch += p[pidx - 1] <= s[sidx] && s[sidx] <= p[1] ? 1 : 0;
                            if (p[++pidx] == 0) {
                                break;
                            }
                        } else {
                            setmatch += p[pidx] == s[sidx] ? 1 : 0;
                        }
                    }
                    if (setmatch == 0) {
                        return (0);
                    }
                    sidx++;
                    break;
                case '\\':			/* literal next */
                    pidx++;
                /* fall through */
                default:			/* normal character */
                    if (p[pidx] != s[sidx]) {
                        return (0);
                    }
                    sidx++;
                    break;
            }
        } while (++pidx < p.length);
        return (1);
    }


    /*
     * Checkheader(fin,fmt,fout) returns a value of 1 if the input format
     * matches the specification in fmt, 0 if no input format was found,
     * and -1 if the input format does not match or there is an
     * error reading the header.  If fmt is empty, then -1 is returned
     * if any input format is found (or there is an error), and 0 otherwise.
     * If fmt contains any '*' or '?' characters, then checkheader
     * does wildcard expansion and copies a matching result into fmt.
     * Be sure that fmt is big enough to hold the match in such cases,
     * and that it is not a static, read-only string!
     * The input header (minus any format lines) is copied to fout
     * if fout is not NULL.
     */
    public static int checkheader(
            InputStream fin,
            char[] fmt,
            OutputStream fout) throws IOException {
        check cdat = new check();
        char[] cp;

        cdat.fp = fout;
        cdat.fs[0] = '\0';
//	if (getheader(fin, mycheck, &cdat) < 0)
        if (getheader(fin, new HEADER.mycheck(), cdat) < 0) {
            return (-1);
        }
        if (cdat.fs[0] == 0) {
            return (0);
        }
        int cpidx = 0;
        cp = fmt;
        for (cpidx = 0; cpidx < cp.length; cpidx++) {		/* check for globbing */
            if ((cp[cpidx] == '?') | (cp[cpidx] == '*')) {
                if (globmatch(fmt, cdat.fs) != 0) {
                    System.arraycopy(cdat.fs, 0, fmt, 0, cdat.fs.length);
                    //strcpy(fmt, cdat.fs);
                    return (1);
                } else {
                    return (-1);
                }
            }
        }
        return (DEVCOMM.strcmp(fmt, cdat.fs) != 0 ? -1 : 1);	/* literal match */
    }
}
