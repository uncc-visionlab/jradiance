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

import java.io.IOException;

/**
 *
 * @author arwillis
 */
public class RAYPWIN {
    /*
     *  raypwin.c - interface for parallel rendering using Radiance (Windows ver)
     *
     *  External symbols declared in ray.h
     */

    /*
     * See raypcalls.c for an explanation of these routines.
     */
    /***** XXX CURRENTLY, THIS IS JUST A COLLECTION OF IMPOTENT STUBS XXX *****/
    public static int ray_pnprocs = 0;	/* number of child processes */

    public static int ray_pnidle = 0;		/* number of idle children */

    public static RAY queued_ray;

    public static void ray_pinit( /* initialize ray-tracing processes */
            String otnm,
            int nproc) throws IOException {
        ray_pdone(0);
        RAYCALLS.ray_init(otnm);
        ray_popen(nproc);
    }

    public static int ray_psend( /* add a ray to our send queue */
            RAY r) {
        if (r == null) {
            return (0);
        }
        if (ray_pnidle <= 0) {
            return (0);
        }
        queued_ray = r;
        ray_pnidle = 0;
        return (1);
    }

    public static int ray_pqueue( /* queue a ray for computation */
            RAY r) {
        long rno;

        if (r == null) {
            return (0);
        }
        if (ray_pnidle <= 0) {
            RAY new_ray = r;
            r = queued_ray;
            queued_ray = new_ray;
        }
        rno = r.rno;
        r.rno = RAYTRACE.raynum++;
        RAYCALLS.samplendx++;
        RAY.rayvalue(r);
        r.rno = rno;
        return (1);
    }

    public static int ray_presult( /* check for a completed ray */
            RAY r,
            int poll) {
        if (r == null) {
            return (0);
        }
        if (ray_pnidle <= 0) {
            r = queued_ray;
            r.rno = RAYTRACE.raynum++;
            RAYCALLS.samplendx++;
            RAY.rayvalue(r);
            r.rno = queued_ray.rno;
            ray_pnidle = 1;
            return (1);
        }
        return (0);
    }

    public static void ray_pdone( /* reap children and free data */
            int freall) throws IOException {
        RAYCALLS.ray_done(freall);
        ray_pnprocs = ray_pnidle = 0;
    }

    public static void ray_popen( /* open the specified # processes */
            int nadd) {
        if (ray_pnprocs + nadd > 1) {
//		error(WARNING, "only single process supported");
            nadd = 1 - ray_pnprocs;
        }
        ray_pnprocs += nadd;
        ray_pnidle += nadd;
    }

    public static void ray_pclose( /* close one or more child processes */
            int nsub) {
        if (nsub > ray_pnprocs) {
            nsub = ray_pnprocs;
        }
        ray_pnprocs -= nsub;
        if ((ray_pnidle -= nsub) < 0) {
            ray_pnidle = 0;
        }
    }
}
