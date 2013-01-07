/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

/**
 *
 * @author arwillis
 */
public class FALSECOLOR {
    /*
     * Header file for false color tone-mapping.
     *
     * Include after "color.h" and "tonemap.h"
     */
    /* false color mapping data structure */

    public static class FCstruct {

        short mbrmin, mbrmax;	/* mapped min. & max. brightnesses */

        byte[] lumap;		/* false color luminance map */

        byte[] scale = new byte[3];	/* false color ordinal scale */

    }
//extern uby8	fcDefaultScale[256][3];		/* default false color scale */
//extern FCstruct *
//fcInit(uby8 fcscale[256][3]);
/*
    Allocate and initialize false color mapping data structure.
    
    fcscale	-	false color ordinal scale.
    
    returns	-	new false color mapping pointer, or NULL if no memory.
     */
//extern int
//fcFixedLinear(FCstruct *fcs, double Lwmax);
/*
    Assign fixed linear false color map.
    
    fcs	-	false color structure pointer.
    Lwmax	-	maximum luminance for scaling
    
    returns	-	0 on success, TM_E_* on failure.
     */
//extern int
//fcFixedLog(FCstruct *fcs, double Lwmin, double Lwmax);
/*
    Assign fixed logarithmic false color map.
    
    fcs	-	false color structure pointer.
    Lwmin	-	minimum luminance for scaling
    Lwmax	-	maximum luminance for scaling
    
    returns	-	0 on success, TM_E_* on failure.
     */
//extern int
//fcLinearMapping(FCstruct *fcs, TMstruct *tms, double pctile);
/*
    Compute linear false color map.
    
    fcs	-	false color structure pointer.
    tms	-	tone mapping structure pointer, with histogram.
    pctile	-	percentile to ignore on top
    
    returns	-	0 on success, TM_E_* on failure.
     */
//extern int
//fcLogMapping(FCstruct *fcs, TMstruct *tms, double pctile);
/*
    Compute logarithmic false color map.
    
    fcs	-	false color structure pointer.
    tms	-	tone mapping structure pointer, with histogram.
    pctile	-	percentile to ignore on top and bottom
    
    returns	-	0 on success, TM_E_* on failure.
     */
//extern int
//fcMapPixels(FCstruct *fcs, uby8 *ps, TMbright *ls, int len);
/*
    Apply false color mapping to pixel values.
    
    fcs	-	false color structure pointer.
    ps	-	returned RGB pixel values.
    ls	-	encoded luminance values.
    len	-	number of pixels.
    
    returns	-	0 on success, TM_E_* on failure.
     */
//extern int
//fcIsLogMap(FCstruct *fcs);
/*
    Determine if false color mapping is logarithmic.
    
    fcs     -       false color structure pointer.
    
    returns	-	1 if map follows logarithmic mapping, -1 on error.
     */
//extern FCstruct *
//fcDup(FCstruct *fcs);
/*
    Duplicate a false color structure.
    
    fcs     -       false color structure pointer.
    
    returns	-	duplicate structure, or NULL if no memory.
     */
//extern void
//fcDone(FCstruct *fcs);
/*
    Free data associated with the given false color mapping structure.
    
    fcs	-	false color mapping structure to free.
     */

    /*
     * False color mapping functions.
     * See falsecolor.h for detailed function descriptions.
     *
     * Externals declared in falsecolor.h
     */
//#include "copyright.h"
//
//#include	<stdio.h>
//#include	<math.h>
//#include	<string.h>
//#include	"tmprivat.h"
//#include	"falsecolor.h"

    /* Initialize new false color mapping */
FCstruct 
fcInit(byte[][] fcscale)
{
	FCstruct	fcs =  new FCstruct();
//	
//	if (fcs == NULL)
//		return(NULL);
	fcs.mbrmin = 10; fcs.mbrmax = -10;
	fcs.lumap = null;
//	if ((fcs->scale = fcscale) == NULL)
//		fcs->scale = fcDefaultScale;
	return(fcs);
}

/* Assign fixed linear false color map */
int
fcFixedLinear(FCstruct fcs, double Lwmax)
{
//	double	mult;
//	int	i;
//
//	if ((fcs == NULL) | (Lwmax <= MINLUM))
//		return(TM_E_ILLEGAL);
//	if (fcs->lumap != NULL)
//		free((void *)fcs->lumap);
//	fcs->mbrmin = tmCvLuminance(Lwmax/256.);
//	fcs->mbrmax = tmCvLuminance(Lwmax);
//	fcs->lumap = (uby8 *)malloc(sizeof(uby8)*(fcs->mbrmax - fcs->mbrmin + 1));
//	if (fcs->lumap == NULL)
//		return(TM_E_NOMEM);
//	mult = 255.999/tmLuminance(fcs->mbrmax);
//	for (i = fcs->mbrmin; i <= fcs->mbrmax; i++)
//		fcs->lumap[i - fcs->mbrmin] = (int)(mult * tmLuminance(i));
	return TMPRIVAT.returnOK();
}

/* Assign fixed logarithmic false color map */
int
fcFixedLog(FCstruct fcs, double Lwmin, double Lwmax)
{
//	int	i;
//
//	if ((fcs == NULL) | (Lwmin <= MINLUM) | (Lwmax <= Lwmin))
//		return(TM_E_ILLEGAL);
//	if (fcs->lumap != NULL)
//		free((void *)fcs->lumap);
//	fcs->mbrmin = tmCvLuminance(Lwmin);
//	fcs->mbrmax = tmCvLuminance(Lwmax);
//	if (fcs->mbrmin >= fcs->mbrmax) {
//		fcs->lumap = NULL;
//		return(TM_E_ILLEGAL);
//	}
//	fcs->lumap = (uby8 *)malloc(sizeof(uby8)*(fcs->mbrmax - fcs->mbrmin + 1));
//	if (fcs->lumap == NULL)
//		return(TM_E_NOMEM);
//	for (i = fcs->mbrmax - fcs->mbrmin; i >= 0; i--)
//		fcs->lumap[i] = 256L * i / (fcs->mbrmax - fcs->mbrmin + 1);
	return TMPRIVAT.returnOK();
}

/* Compute linear false color map */
int
fcLinearMapping(FCstruct fcs, short[] tms, double pctile)
{
	int		i=0, histlen;
	int		histot, cnt;
	int		brt0=0;
//
//	if ((fcs == NULL) | (tms == NULL) || (tms->histo == NULL) |
//			(0 > pctile) | (pctile >= 50))
//		return(TM_E_ILLEGAL);
//	i = HISTI(tms->hbrmin);
//	brt0 = HISTV(i);
//	histlen = HISTI(tms->hbrmax) + 1 - i;
//	histot = 0;
//	for (i = histlen; i--; )
//		histot += tms->histo[i];
//	cnt = histot * pctile / 100;
//	for (i = histlen; i--; )
//		if ((cnt -= tms->histo[i]) < 0)
//			break;
//	if (i <= 0)
//		return(TM_E_TMFAIL);
	return(fcFixedLinear(fcs, TONEMAP.tmLuminance(brt0 + i*TMPRIVAT.HISTEP)));
}

/* Compute logarithmic false color map */
int
fcLogMapping(FCstruct fcs, short[] tms, double pctile)
{
	int		i, histlen;
	int		histot, cnt;
	int		brt0, wbrmin=0, wbrmax=0;
//
//	if ((fcs == NULL) | (tms == NULL) || (tms->histo == NULL) |
//			(.0 > pctile) | (pctile >= 50.))
//		return(TM_E_ILLEGAL);
//	i = HISTI(tms->hbrmin);
//	brt0 = HISTV(i);
//	histlen = HISTI(tms->hbrmax) + 1 - i;
//	histot = 0;
//	for (i = histlen; i--; )
//		histot += tms->histo[i];
//	cnt = histot * pctile * .01;
//	for (i = 0; i < histlen; i++)
//		if ((cnt -= tms->histo[i]) < 0)
//			break;
//	if (i >= histlen)
//		return(TM_E_TMFAIL);
//	wbrmin = brt0 + i*HISTEP;
//	cnt = histot * pctile * .01;
//	for (i = histlen; i--; )
//		if ((cnt -= tms->histo[i]) < 0)
//			break;
//	wbrmax = brt0 + i*HISTEP;
//	if (wbrmax <= wbrmin)
//		return(TM_E_TMFAIL);
	return(fcFixedLog(fcs, TONEMAP.tmLuminance(wbrmin), TONEMAP.tmLuminance(wbrmax)));
}
 
/* Apply false color mapping to pixel values */
int
fcMapPixels(FCstruct fcs, byte[] ps, short[] ls, int len)
{
//	int	li;
//
//	if (fcs == NULL || (fcs->lumap == NULL) | (fcs->scale == NULL))
//		return(TM_E_ILLEGAL);
//	if ((ps == NULL) | (ls == NULL) | (len < 0))
//		return(TM_E_ILLEGAL);
//	while (len--) {
//		if ((li = *ls++) < fcs->mbrmin)
//			li = 0;
//		else if (li > fcs->mbrmax)
//			li = 255;
//		else
//			li = fcs->lumap[li - fcs->mbrmin];
//		*ps++ = fcs->scale[li][RED];
//		*ps++ = fcs->scale[li][GRN];
//		*ps++ = fcs->scale[li][BLU];
//	}
	return TMPRIVAT.returnOK();
}

/* Determine if false color mapping is logarithmic */
int
fcIsLogMap(FCstruct fcs)
{
	int	miderr=0;
//
//	if (fcs == NULL || fcs->lumap == NULL)
//		return(-1);
//	
//	miderr = (fcs->mbrmax - fcs->mbrmin)/2;
//	miderr = fcs->lumap[miderr] -
//			256L * miderr / (fcs->mbrmax - fcs->mbrmin + 1);
//
	return((-1 <= miderr) && (miderr <= 1) ? 1 : 0);
}

/* Duplicate a false color structure */
FCstruct 
fcDup(FCstruct fcs)
{
	FCstruct	fcnew=null;
//
//	if (fcs == NULL)
//		return(NULL);
//	fcnew = fcInit(fcs->scale);
//	if (fcnew == NULL)
//		return(NULL);
//	if (fcs->lumap != NULL) {
//		fcnew->lumap = (uby8 *)malloc(sizeof(uby8)*(fcs->mbrmax -
//							fcs->mbrmin + 1));
//		if (fcnew->lumap == NULL)
//			return(fcnew);
//		fcnew->mbrmin = fcs->mbrmin; fcnew->mbrmax = fcs->mbrmax;
//		memcpy((void *)fcnew->lumap, (void *)fcs->lumap,
//				sizeof(uby8)*(fcs->mbrmax - fcs->mbrmin + 1));
//	}
	return(fcnew);
}

/* Free data associated with the given false color mapping structure */
void
fcDone(FCstruct fcs)
{
	if (fcs == null)
		return;
	if (fcs.lumap != null) {
//		free((void *)fcs->lumap);
            fcs.lumap = null;
        }
        fcs = null;
//	free((void *)fcs);
}
    byte[][] fcDefaultScale = { /* default false color scale */
        {(byte) 111, (byte) 8, (byte) 132},
        {(byte) 108, (byte) 7, (byte) 133},
        {(byte) 105, (byte) 7, (byte) 134},
        {(byte) 102, (byte) 6, (byte) 136},
        {(byte) 98, (byte) 6, (byte) 137},
        {(byte) 93, (byte) 5, (byte) 139},
        {(byte) 89, (byte) 4, (byte) 141},
        {(byte) 84, (byte) 3, (byte) 143},
        {(byte) 79, (byte) 2, (byte) 145},
        {(byte) 74, (byte) 1, (byte) 148},
        {(byte) 68, (byte) 0, (byte) 150},
        {(byte) 63, (byte) 0, (byte) 153},
        {(byte) 57, (byte) 0, (byte) 155},
        {(byte) 52, (byte) 0, (byte) 157},
        {(byte) 46, (byte) 0, (byte) 160},
        {(byte) 41, (byte) 0, (byte) 162},
        {(byte) 36, (byte) 0, (byte) 164},
        {(byte) 31, (byte) 0, (byte) 166},
        {(byte) 26, (byte) 0, (byte) 168},
        {(byte) 22, (byte) 0, (byte) 170},
        {(byte) 18, (byte) 0, (byte) 172},
        {(byte) 14, (byte) 2, (byte) 174},
        {(byte) 11, (byte) 4, (byte) 175},
        {(byte) 8, (byte) 7, (byte) 176},
        {(byte) 7, (byte) 9, (byte) 177},
        {(byte) 6, (byte) 11, (byte) 177},
        {(byte) 5, (byte) 13, (byte) 178},
        {(byte) 4, (byte) 16, (byte) 178},
        {(byte) 3, (byte) 18, (byte) 179},
        {(byte) 2, (byte) 21, (byte) 180},
        {(byte) 1, (byte) 24, (byte) 180},
        {(byte) 1, (byte) 28, (byte) 181},
        {(byte) 0, (byte) 31, (byte) 181},
        {(byte) 0, (byte) 35, (byte) 182},
        {(byte) 0, (byte) 38, (byte) 182},
        {(byte) 0, (byte) 42, (byte) 183},
        {(byte) 0, (byte) 46, (byte) 184},
        {(byte) 0, (byte) 50, (byte) 184},
        {(byte) 0, (byte) 54, (byte) 184},
        {(byte) 0, (byte) 58, (byte) 185},
        {(byte) 0, (byte) 63, (byte) 185},
        {(byte) 0, (byte) 71, (byte) 186},
        {(byte) 0, (byte) 76, (byte) 186},
        {(byte) 0, (byte) 80, (byte) 187},
        {(byte) 0, (byte) 84, (byte) 187},
        {(byte) 0, (byte) 89, (byte) 187},
        {(byte) 0, (byte) 93, (byte) 187},
        {(byte) 1, (byte) 97, (byte) 187},
        {(byte) 1, (byte) 102, (byte) 187},
        {(byte) 1, (byte) 106, (byte) 187},
        {(byte) 2, (byte) 110, (byte) 187},
        {(byte) 2, (byte) 114, (byte) 187},
        {(byte) 3, (byte) 118, (byte) 186},
        {(byte) 3, (byte) 122, (byte) 186},
        {(byte) 4, (byte) 126, (byte) 186},
        {(byte) 4, (byte) 130, (byte) 185},
        {(byte) 4, (byte) 133, (byte) 185},
        {(byte) 5, (byte) 137, (byte) 184},
        {(byte) 5, (byte) 140, (byte) 183},
        {(byte) 6, (byte) 143, (byte) 182},
        {(byte) 6, (byte) 146, (byte) 181},
        {(byte) 6, (byte) 149, (byte) 180},
        {(byte) 7, (byte) 151, (byte) 179},
        {(byte) 7, (byte) 154, (byte) 178},
        {(byte) 7, (byte) 156, (byte) 177},
        {(byte) 8, (byte) 158, (byte) 175},
        {(byte) 8, (byte) 161, (byte) 172},
        {(byte) 9, (byte) 163, (byte) 169},
        {(byte) 9, (byte) 165, (byte) 165},
        {(byte) 9, (byte) 167, (byte) 161},
        {(byte) 9, (byte) 169, (byte) 157},
        {(byte) 10, (byte) 170, (byte) 153},
        {(byte) 10, (byte) 172, (byte) 148},
        {(byte) 10, (byte) 173, (byte) 143},
        {(byte) 11, (byte) 174, (byte) 138},
        {(byte) 11, (byte) 174, (byte) 133},
        {(byte) 11, (byte) 175, (byte) 127},
        {(byte) 12, (byte) 175, (byte) 122},
        {(byte) 12, (byte) 176, (byte) 117},
        {(byte) 13, (byte) 176, (byte) 111},
        {(byte) 14, (byte) 176, (byte) 106},
        {(byte) 14, (byte) 176, (byte) 101},
        {(byte) 15, (byte) 175, (byte) 95},
        {(byte) 16, (byte) 175, (byte) 90},
        {(byte) 17, (byte) 175, (byte) 86},
        {(byte) 18, (byte) 174, (byte) 81},
        {(byte) 20, (byte) 174, (byte) 77},
        {(byte) 21, (byte) 173, (byte) 73},
        {(byte) 22, (byte) 172, (byte) 69},
        {(byte) 24, (byte) 172, (byte) 66},
        {(byte) 26, (byte) 171, (byte) 63},
        {(byte) 28, (byte) 170, (byte) 60},
        {(byte) 30, (byte) 169, (byte) 58},
        {(byte) 32, (byte) 168, (byte) 57},
        {(byte) 34, (byte) 167, (byte) 56},
        {(byte) 37, (byte) 166, (byte) 55},
        {(byte) 40, (byte) 165, (byte) 54},
        {(byte) 42, (byte) 164, (byte) 54},
        {(byte) 45, (byte) 163, (byte) 54},
        {(byte) 48, (byte) 162, (byte) 55},
        {(byte) 52, (byte) 160, (byte) 55},
        {(byte) 55, (byte) 158, (byte) 56},
        {(byte) 58, (byte) 157, (byte) 57},
        {(byte) 62, (byte) 155, (byte) 57},
        {(byte) 66, (byte) 153, (byte) 59},
        {(byte) 69, (byte) 152, (byte) 60},
        {(byte) 73, (byte) 150, (byte) 61},
        {(byte) 77, (byte) 148, (byte) 63},
        {(byte) 81, (byte) 146, (byte) 64},
        {(byte) 84, (byte) 144, (byte) 66},
        {(byte) 88, (byte) 142, (byte) 67},
        {(byte) 92, (byte) 139, (byte) 69},
        {(byte) 96, (byte) 137, (byte) 70},
        {(byte) 99, (byte) 135, (byte) 72},
        {(byte) 103, (byte) 133, (byte) 73},
        {(byte) 107, (byte) 131, (byte) 75},
        {(byte) 110, (byte) 128, (byte) 76},
        {(byte) 113, (byte) 126, (byte) 77},
        {(byte) 117, (byte) 124, (byte) 78},
        {(byte) 120, (byte) 121, (byte) 79},
        {(byte) 123, (byte) 119, (byte) 80},
        {(byte) 126, (byte) 117, (byte) 80},
        {(byte) 128, (byte) 114, (byte) 81},
        {(byte) 131, (byte) 112, (byte) 81},
        {(byte) 133, (byte) 110, (byte) 81},
        {(byte) 135, (byte) 108, (byte) 80},
        {(byte) 136, (byte) 106, (byte) 80},
        {(byte) 137, (byte) 105, (byte) 80},
        {(byte) 138, (byte) 104, (byte) 79},
        {(byte) 139, (byte) 102, (byte) 79},
        {(byte) 140, (byte) 101, (byte) 79},
        {(byte) 141, (byte) 100, (byte) 78},
        {(byte) 142, (byte) 98, (byte) 78},
        {(byte) 143, (byte) 96, (byte) 77},
        {(byte) 144, (byte) 95, (byte) 76},
        {(byte) 144, (byte) 93, (byte) 76},
        {(byte) 145, (byte) 92, (byte) 75},
        {(byte) 146, (byte) 90, (byte) 74},
        {(byte) 146, (byte) 89, (byte) 73},
        {(byte) 147, (byte) 87, (byte) 73},
        {(byte) 148, (byte) 85, (byte) 72},
        {(byte) 148, (byte) 84, (byte) 71},
        {(byte) 149, (byte) 82, (byte) 70},
        {(byte) 149, (byte) 80, (byte) 69},
        {(byte) 150, (byte) 79, (byte) 68},
        {(byte) 150, (byte) 77, (byte) 67},
        {(byte) 151, (byte) 75, (byte) 66},
        {(byte) 151, (byte) 73, (byte) 65},
        {(byte) 151, (byte) 72, (byte) 64},
        {(byte) 152, (byte) 70, (byte) 63},
        {(byte) 152, (byte) 68, (byte) 62},
        {(byte) 153, (byte) 66, (byte) 61},
        {(byte) 153, (byte) 65, (byte) 60},
        {(byte) 153, (byte) 63, (byte) 59},
        {(byte) 154, (byte) 61, (byte) 58},
        {(byte) 154, (byte) 60, (byte) 57},
        {(byte) 154, (byte) 58, (byte) 56},
        {(byte) 154, (byte) 56, (byte) 55},
        {(byte) 155, (byte) 55, (byte) 54},
        {(byte) 155, (byte) 53, (byte) 53},
        {(byte) 155, (byte) 51, (byte) 51},
        {(byte) 156, (byte) 50, (byte) 50},
        {(byte) 156, (byte) 48, (byte) 49},
        {(byte) 156, (byte) 46, (byte) 48},
        {(byte) 157, (byte) 45, (byte) 47},
        {(byte) 157, (byte) 43, (byte) 46},
        {(byte) 157, (byte) 42, (byte) 45},
        {(byte) 158, (byte) 40, (byte) 44},
        {(byte) 158, (byte) 39, (byte) 43},
        {(byte) 158, (byte) 37, (byte) 42},
        {(byte) 159, (byte) 36, (byte) 41},
        {(byte) 159, (byte) 34, (byte) 40},
        {(byte) 159, (byte) 33, (byte) 39},
        {(byte) 160, (byte) 32, (byte) 38},
        {(byte) 160, (byte) 31, (byte) 37},
        {(byte) 161, (byte) 29, (byte) 37},
        {(byte) 161, (byte) 28, (byte) 36},
        {(byte) 162, (byte) 27, (byte) 35},
        {(byte) 162, (byte) 26, (byte) 34},
        {(byte) 163, (byte) 25, (byte) 33},
        {(byte) 163, (byte) 24, (byte) 33},
        {(byte) 164, (byte) 23, (byte) 32},
        {(byte) 165, (byte) 22, (byte) 31},
        {(byte) 165, (byte) 21, (byte) 31},
        {(byte) 168, (byte) 18, (byte) 29},
        {(byte) 170, (byte) 16, (byte) 28},
        {(byte) 172, (byte) 13, (byte) 26},
        {(byte) 175, (byte) 11, (byte) 25},
        {(byte) 177, (byte) 9, (byte) 24},
        {(byte) 180, (byte) 7, (byte) 23},
        {(byte) 183, (byte) 5, (byte) 22},
        {(byte) 185, (byte) 3, (byte) 21},
        {(byte) 188, (byte) 2, (byte) 21},
        {(byte) 191, (byte) 1, (byte) 20},
        {(byte) 194, (byte) 0, (byte) 19},
        {(byte) 197, (byte) 0, (byte) 19},
        {(byte) 199, (byte) 0, (byte) 18},
        {(byte) 202, (byte) 0, (byte) 17},
        {(byte) 205, (byte) 0, (byte) 17},
        {(byte) 207, (byte) 0, (byte) 16},
        {(byte) 210, (byte) 2, (byte) 16},
        {(byte) 213, (byte) 3, (byte) 15},
        {(byte) 215, (byte) 6, (byte) 14},
        {(byte) 217, (byte) 8, (byte) 13},
        {(byte) 219, (byte) 11, (byte) 13},
        {(byte) 220, (byte) 13, (byte) 12},
        {(byte) 222, (byte) 17, (byte) 11},
        {(byte) 224, (byte) 20, (byte) 11},
        {(byte) 226, (byte) 24, (byte) 10},
        {(byte) 227, (byte) 28, (byte) 9},
        {(byte) 229, (byte) 32, (byte) 8},
        {(byte) 231, (byte) 37, (byte) 7},
        {(byte) 232, (byte) 42, (byte) 6},
        {(byte) 234, (byte) 47, (byte) 5},
        {(byte) 236, (byte) 52, (byte) 5},
        {(byte) 237, (byte) 57, (byte) 4},
        {(byte) 239, (byte) 63, (byte) 3},
        {(byte) 240, (byte) 68, (byte) 2},
        {(byte) 242, (byte) 74, (byte) 2},
        {(byte) 243, (byte) 79, (byte) 1},
        {(byte) 245, (byte) 85, (byte) 0},
        {(byte) 246, (byte) 91, (byte) 0},
        {(byte) 247, (byte) 96, (byte) 0},
        {(byte) 248, (byte) 102, (byte) 0},
        {(byte) 250, (byte) 108, (byte) 0},
        {(byte) 251, (byte) 113, (byte) 0},
        {(byte) 252, (byte) 118, (byte) 0},
        {(byte) 253, (byte) 123, (byte) 0},
        {(byte) 254, (byte) 128, (byte) 0},
        {(byte) 254, (byte) 133, (byte) 0},
        {(byte) 255, (byte) 138, (byte) 0},
        {(byte) 255, (byte) 143, (byte) 1},
        {(byte) 255, (byte) 148, (byte) 2},
        {(byte) 255, (byte) 154, (byte) 3},
        {(byte) 255, (byte) 159, (byte) 4},
        {(byte) 255, (byte) 165, (byte) 6},
        {(byte) 255, (byte) 170, (byte) 7},
        {(byte) 255, (byte) 176, (byte) 9},
        {(byte) 255, (byte) 181, (byte) 11},
        {(byte) 255, (byte) 187, (byte) 13},
        {(byte) 255, (byte) 192, (byte) 15},
        {(byte) 255, (byte) 198, (byte) 17},
        {(byte) 255, (byte) 203, (byte) 20},
        {(byte) 255, (byte) 208, (byte) 22},
        {(byte) 255, (byte) 213, (byte) 24},
        {(byte) 255, (byte) 218, (byte) 26},
        {(byte) 255, (byte) 223, (byte) 28},
        {(byte) 255, (byte) 227, (byte) 30},
        {(byte) 255, (byte) 232, (byte) 32},
        {(byte) 255, (byte) 236, (byte) 34},
        {(byte) 254, (byte) 240, (byte) 35},
        {(byte) 254, (byte) 243, (byte) 37},
        {(byte) 254, (byte) 246, (byte) 38},
        {(byte) 254, (byte) 249, (byte) 39},
        {(byte) 254, (byte) 252, (byte) 40},};
}
