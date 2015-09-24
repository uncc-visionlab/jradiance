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

/**
 *
 * @author arwillis
 */
public class WORDS {
    /*
     * Routines for recognizing and moving about words in strings.
     *
     * External symbols declared in standard.h
     */

    char[] atos(char[] rs, int nb, char[] s) /* get word from string, returning rs */ {
        char[] cp = rs;
        int sidx = 0, cpidx = 0;
        while (Character.isWhitespace(s[sidx])) {
            sidx++;
        }
        while (--nb > 0 && s[sidx] != 0 && !Character.isWhitespace(s[sidx])) {
            cp[cpidx++] = s[sidx++];
        }
        cp[cpidx] = '\0';
        return (rs);
    }

    char[] nextword(char[] cp, int nb, char[] s) /* get (quoted) word, returning new s */ {
        int quote = 0, sidx = 0, cpidx = 0;

        if (s == null) {
            return (null);
        }
        while (Character.isWhitespace(s[sidx])) {
            sidx++;
        }
        switch (s[sidx]) {
            case '\0':
                return (null);
            case '"':
            case '\'':
                quote = s[sidx++];
        }
        while (--nb > 0 && s[sidx] != 0 && (quote != 0 ? s[sidx] != quote : !Character.isWhitespace(s[sidx]))) {
            cp[cpidx++] = s[sidx++];
        }
        cp[cpidx] = '\0';
        if (quote != 0 && s[sidx] == quote) {
            sidx++;
        }
        return (s);
    }

    char[] sskip(char[] s) /* skip word in string, leaving on space */ {
        int sidx = 0;
        while (Character.isWhitespace(s[sidx])) {
            sidx++;
        }
        while (s[sidx] != 0 && !Character.isWhitespace(s[sidx])) {
            sidx++;
        }
        return (s);
    }

    char[] sskip2(char[] s, int n) /* skip word(s) in string, leaving on word */ {
        int sidx = 0;
        while (Character.isWhitespace(s[sidx])) {
            sidx++;
        }
        while (n-- > 0) {
            while (s[sidx] != 0 && !Character.isWhitespace(s[sidx])) {
                sidx++;
            }
            while (Character.isWhitespace(s[sidx])) {
                sidx++;
            }
        }
        return (s);
    }

    public static char[] iskip(char[] s) /* skip integer in string */ {
        int sidx = 0;
        while (Character.isWhitespace(s[sidx])) {
            sidx++;
        }
        if ((s[sidx] == '-') | (s[sidx] == '+')) {
            sidx++;
        }
        if (!Character.isDigit(s[sidx])) {
            return null;
        }
        do {
            sidx++;
        } while (Character.isDigit(s[sidx]));
        return (s);
    }

    char[] fskip(char[] s) /* skip float in string */ {
        char[] cp;
        int sidx = 0;
        while (Character.isWhitespace(s[sidx])) {
            sidx++;
        }
        if ((s[sidx] == '-') | (s[sidx] == '+')) {
            sidx++;
        }
        cp = s;
//	while (isdigit(*cp))
//		cp++;
//	if (*cp == '.') {
//		cp++; s++;
//		while (isdigit(*cp))
//			cp++;
//	}
//	if (cp == s)
//		return(NULL);
//	if ((*cp == 'e') | (*cp == 'E'))
//		return(isspace(*++cp) ? NULL : iskip(cp));
        return (cp);
    }

    public static int isint(char[] s) /* check integer format */ {
        char[] cp;

        cp = iskip(s);
        return ((cp != null && cp[0] == '\0') ? 1 : 0);
    }
//
//int
//isintd(char *s, char *ds)	/* check integer format with delimiter set */
//{
//	char  *cp;
//
//	cp = iskip(s);
//	return(cp != NULL && strchr(ds, *cp) != NULL);
//}
//
//
//int
//isflt(char *s)			/* check float format */
//{
//	char  *cp;
//
//	cp = fskip(s);
//	return(cp != NULL && *cp == '\0');
//}
//
//
//int
//isfltd(char *s, char *ds)	/* check integer format with delimiter set */
//{
//	char  *cp;
//
//	cp = fskip(s);
//	return(cp != NULL && strchr(ds, *cp) != NULL);
//}
//
//
//int
//isname(char *s)			/* check for legal identifier name */
//{
//	while (*s == '_')			/* skip leading underscores */
//		s++;
//	if (!isascii(*s) || !isalpha(*s))	/* start with a letter */
//		return(0);
//	while (isascii(*++s) && isgraph(*s))	/* all visible characters */
//		;
//	return(*s == '\0');			/* ending in nul */
//}
//   
}
