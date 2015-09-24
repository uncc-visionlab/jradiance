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

import jradiance.common.OBJECT_STRUCTURE;
import jradiance.common.OTYPES;
import jradiance.rt.MX_DATA.MX_DATA1;
import jradiance.rt.MX_DATA.MX_PDATA;
import jradiance.rt.M_BRDF.BRDF;
import jradiance.rt.M_BRDF.BRDF2;
import jradiance.rt.P_DATA.P_BDATA;
import jradiance.rt.P_DATA.P_CDATA;
import jradiance.rt.P_DATA.P_PDATA;
import jradiance.rt.P_FUNC.P_BFUNC;
import jradiance.rt.P_FUNC.P_CFUNC;
import jradiance.rt.SOURCE.M_LIGHT;

/**
 *
 * @author arwillis
 */
public class INITOTYPES extends OTYPES {

    /*
     * Initialize ofun[] list for renderers
     */
//FUN  ofun[NUMOTYPE] = INIT_OTYPE;
    public static void initotypes_raytrace() /* initialize ofun array */ {
        SPHERE s = new SPHERE();
        OTYPES.ofun[OBJ_SPHERE].setOctreeFunction(s);
        ofun[OBJ_BUBBLE].setOctreeFunction(s);
        O_FACE f = new O_FACE();
        ofun[OBJ_FACE].setOctreeFunction(f);
        O_CONE c = new O_CONE();
        ofun[OBJ_CONE].setOctreeFunction(c);
        ofun[OBJ_CUP].setOctreeFunction(c);
        ofun[OBJ_CYLINDER].setOctreeFunction(c);
        ofun[OBJ_TUBE].setOctreeFunction(c);
        ofun[OBJ_RING].setOctreeFunction(c);
        O_INSTANCE ins = new O_INSTANCE();
        ofun[OBJ_INSTANCE].setOctreeFunction(ins);
        O_MESH m = new O_MESH();
        ofun[OBJ_MESH].setOctreeFunction(m);
        M_ALIAS m_alias = new M_ALIAS();
        ofun[MOD_ALIAS].setOctreeFunction(m_alias); // m_alias
        M_LIGHT m_light = new M_LIGHT();
        ofun[MAT_LIGHT].setOctreeFunction(m_light);
        ofun[MAT_ILLUM].setOctreeFunction(m_light);
        ofun[MAT_GLOW].setOctreeFunction(m_light);
        ofun[MAT_SPOT].setOctreeFunction(m_light); // source
        ofun[MAT_LIGHT].flags |= OTSPECIAL.T_OPAQUE;
        ofun[MAT_SPOT].flags |= OTSPECIAL.T_OPAQUE;
        NORMAL m_normal = new NORMAL();
        ofun[MAT_PLASTIC].setOctreeFunction(m_normal);
        ofun[MAT_METAL].setOctreeFunction(m_normal);
        ofun[MAT_TRANS].setOctreeFunction(m_normal); // normal
        ofun[MAT_PLASTIC].flags |= OTSPECIAL.T_OPAQUE;
        ofun[MAT_METAL].flags |= OTSPECIAL.T_OPAQUE;
        ofun[MAT_TRANS].flags |= OTSPECIAL.T_IRR_IGN;
        ANISO m_aniso = new ANISO();
        ofun[MAT_PLASTIC2].setOctreeFunction(m_aniso);
        ofun[MAT_METAL2].setOctreeFunction(m_aniso);
        ofun[MAT_TRANS2].setOctreeFunction(m_aniso); // aniso
        ofun[MAT_PLASTIC2].flags |= OTSPECIAL.T_OPAQUE;
        ofun[MAT_METAL2].flags |= OTSPECIAL.T_OPAQUE;
        ofun[MAT_TRANS2].flags |= OTSPECIAL.T_IRR_IGN;
        DIELECTRIC m_dielectric = new DIELECTRIC();
        ofun[MAT_DIELECTRIC].setOctreeFunction(m_dielectric);
        ofun[MAT_INTERFACE].setOctreeFunction(m_dielectric); // dielectric
        ofun[MAT_DIELECTRIC].flags |= OTSPECIAL.T_IRR_IGN;
        ofun[MAT_INTERFACE].flags |= OTSPECIAL.T_IRR_IGN;
        M_MIST m_mist = new M_MIST();
        ofun[MAT_MIST].setOctreeFunction(m_mist); // m_mist
        ofun[MAT_MIST].flags |= OTSPECIAL.T_IRR_IGN;
        GLASS m_glass = new GLASS();
        ofun[MAT_GLASS].setOctreeFunction(m_glass); // glass
        ofun[MAT_GLASS].flags |= OTSPECIAL.T_IRR_IGN;
        M_MIRROR m_mirror = new M_MIRROR();
        ofun[MAT_MIRROR].setOctreeFunction(m_mirror); // m_mirror
        M_DIRECT m_direct = new M_DIRECT();
        ofun[MAT_DIRECT1].setOctreeFunction(m_direct);
        ofun[MAT_DIRECT2].setOctreeFunction(m_direct); // m_direct
        M_CLIP m_clip = new M_CLIP();
        ofun[MAT_CLIP].setOctreeFunction(m_clip); // m_clip
        BRDF m_brdf = new BRDF();
        ofun[MAT_BRTDF].setOctreeFunction(m_brdf); // m_brdf
        M_BSDF m_bsdf = new M_BSDF();
        ofun[MAT_BSDF].setOctreeFunction(m_bsdf); // m_bsdf
        BRDF2 m_brdf2 = new BRDF2();
        ofun[MAT_PFUNC].setOctreeFunction(m_brdf2);
        ofun[MAT_MFUNC].setOctreeFunction(m_brdf2);
        ofun[MAT_PDATA].setOctreeFunction(m_brdf2);
        ofun[MAT_MDATA].setOctreeFunction(m_brdf2);
        ofun[MAT_TFUNC].setOctreeFunction(m_brdf2);
        ofun[MAT_TDATA].setOctreeFunction(m_brdf2); // m_brdf
        ofun[MAT_PFUNC].flags |= OTSPECIAL.T_OPAQUE;
        ofun[MAT_MFUNC].flags |= OTSPECIAL.T_OPAQUE;
        ofun[MAT_PDATA].flags |= OTSPECIAL.T_OPAQUE;
        ofun[MAT_MDATA].flags |= OTSPECIAL.T_OPAQUE;
        T_FUNC t_func = new T_FUNC();
        ofun[TEX_FUNC].setOctreeFunction(t_func);
        T_DATA t_data = new T_DATA();
        ofun[TEX_DATA].setOctreeFunction(t_data);
        P_CFUNC p_cfunc = new P_CFUNC();
        ofun[PAT_CFUNC].setOctreeFunction(p_cfunc);
        P_BFUNC p_bfunc = new P_BFUNC();
        ofun[PAT_BFUNC].setOctreeFunction(p_bfunc);
        P_PDATA p_pdata = new P_PDATA();
        ofun[PAT_CPICT].setOctreeFunction(p_pdata);
        P_CDATA p_cdata = new P_CDATA();
        ofun[PAT_CDATA].setOctreeFunction(p_cdata);
        P_BDATA p_bdata = new P_BDATA();
        ofun[PAT_BDATA].setOctreeFunction(p_bdata);
        TEXT do_text = new TEXT();
        ofun[PAT_CTEXT].setOctreeFunction(do_text);
        ofun[PAT_BTEXT].setOctreeFunction(do_text);
        ofun[MIX_TEXT].setOctreeFunction(do_text); // text
        MX_FUNC mx_func = new MX_FUNC();
        ofun[MIX_FUNC].setOctreeFunction(mx_func);
        MX_DATA1 mx_data = new MX_DATA1();
        ofun[MIX_DATA].setOctreeFunction(mx_data);
        MX_PDATA mx_pdata = new MX_PDATA();
        ofun[MIX_PICT].setOctreeFunction(mx_pdata);
    }

    public static class o_default extends OBJECT_STRUCTURE {

        @Override
        public int octree_function(Object... objs) /* default action is no intersection */ {
//	objerror(o, CONSISTENCY, "unexpected object call");
            System.out.println("unexpected object call");
            /* unused call to load freeobjmem.o */
//	free_objs(0, 0);
            return (0);
        }
    }
//int
//o_default(OBJECT.OBJREC o, RAY r)			/* default action is error */
//{
//	objerror(o, CONSISTENCY, "unexpected object call");
				/* unused call to load freeobjmem.o */
//	free_objs(0, 0);
//	return(0);
//}
}
