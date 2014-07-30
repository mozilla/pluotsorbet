/*
 *   
 *
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.midp.chameleon.skins.resources;

import javax.microedition.lcdui.Font;

/**
 * There are three different font faces available in MIDP:
 * Monospace, Proportional, and System. There are eight 
 * different styles available: Plain, Italic, Bold, Bold Italic,
 * Underline, Underline Italic, Underline Bold, and 
 * Underline Bold Italic. There are three different sizes 
 * available: Small, Medium, and Large. That presents
 * a matrix of 72 different font possibilities. This class
 * serves to manage those different possibilities.
 *
 * Identifiers use a FACE_STYLE_SIZE naming scheme, where
 * FACE is one of [MONO|PROP|SYS], STYLE is one of 
 * [P|I|B|BI|U|UI|UB|UBI], and SIZE is one of [S|M|L].
 */
public class FontResources {

    /**
     * IMPL_NOTE: font constants have been moved to 
     * FontResourcesConstants class. However, they are
     * duplicated here because this file is used by
     * skin authors to lookup for possible constants 
     * values. This should be changed in future releases.
     */

    /** Monospaced, plain, small */
    public final static int MONO_P_S    = FontResourcesConstants.MONO_P_S;
    /** Monospaced, italic, small */
    public final static int MONO_I_S    = FontResourcesConstants.MONO_I_S;
    /** Monospaced, bold, small */
    public final static int MONO_B_S    = FontResourcesConstants.MONO_B_S;
    /** Monospaced, bold italic, small */
    public final static int MONO_BI_S   = FontResourcesConstants.MONO_BI_S;
    /** Monospaced, underline, small */
    public final static int MONO_U_S    = FontResourcesConstants.MONO_U_S;
    /** Monospaced, underline italic, small */
    public final static int MONO_UI_S   = FontResourcesConstants.MONO_UI_S;
    /** Monospaced, underline bold, small */
    public final static int MONO_UB_S   = FontResourcesConstants.MONO_UB_S;
    /** Monospaced, underline bold italic, small */
    public final static int MONO_UBI_S  = FontResourcesConstants.MONO_UBI_S;

    /** Monospaced, plain, medium */
    public final static int MONO_P_M    = FontResourcesConstants.MONO_P_M;
    /** Monospaced, italic, medium */
    public final static int MONO_I_M    = FontResourcesConstants.MONO_I_M;
    /** Monospaced, bold, medium */
    public final static int MONO_B_M    = FontResourcesConstants.MONO_B_M;
    /** Monospaced, bold italic, medium */
    public final static int MONO_BI_M   = FontResourcesConstants.MONO_BI_M;
    /** Monospaced, underline, medium */
    public final static int MONO_U_M    = FontResourcesConstants.MONO_U_M;
    /** Monospaced, underline italic, medium */
    public final static int MONO_UI_M   = FontResourcesConstants.MONO_UI_M;
    /** Monospaced, underline bold, medium */
    public final static int MONO_UB_M   = FontResourcesConstants.MONO_UB_M;
    /** Monospaced, underline bold italic, medium */
    public final static int MONO_UBI_M  = FontResourcesConstants.MONO_UBI_M;
    
    /** Monospaced, plain, large */
    public final static int MONO_P_L    = FontResourcesConstants.MONO_P_L;
    /** Monospaced, italic, large */
    public final static int MONO_I_L    = FontResourcesConstants.MONO_I_L;
    /** Monospaced, bold, large */
    public final static int MONO_B_L    = FontResourcesConstants.MONO_B_L;
    /** Monospaced, bold italic, large */
    public final static int MONO_BI_L   = FontResourcesConstants.MONO_BI_L;
    /** Monospaced, underline, large */
    public final static int MONO_U_L    = FontResourcesConstants.MONO_U_L;
    /** Monospaced, underline italic, large */
    public final static int MONO_UI_L   = FontResourcesConstants.MONO_UI_L;
    /** Monospaced, underline bold, large */
    public final static int MONO_UB_L   = FontResourcesConstants.MONO_UB_L;
    /** Monospaced, underline bold italic, large */
    public final static int MONO_UBI_L  = FontResourcesConstants.MONO_UBI_L;
    
    /** Proportional, plain, small */
    public final static int PROP_P_S    = FontResourcesConstants.PROP_P_S;
    /** Proportional, italic, small */
    public final static int PROP_I_S    = FontResourcesConstants.PROP_I_S;
    /** Proportional, bold, small */
    public final static int PROP_B_S    = FontResourcesConstants.PROP_B_S;
    /** Proportional, bold italic, small */
    public final static int PROP_BI_S   = FontResourcesConstants.PROP_BI_S;
    /** Proportional, underline, small */
    public final static int PROP_U_S    = FontResourcesConstants.PROP_U_S;
    /** Proportional, underline italic, small */
    public final static int PROP_UI_S   = FontResourcesConstants.PROP_UI_S;
    /** Proportional, underline bold, small */
    public final static int PROP_UB_S   = FontResourcesConstants.PROP_UB_S;
    /** Proportional, underline bold italic, small */
    public final static int PROP_UBI_S  = FontResourcesConstants.PROP_UBI_S;
    
    /** Proportional, plain, medium */
    public final static int PROP_P_M    = FontResourcesConstants.PROP_P_M;
    /** Proportional, italic, medium */
    public final static int PROP_I_M    = FontResourcesConstants.PROP_I_M;
    /** Proportional, bold, medium */
    public final static int PROP_B_M    = FontResourcesConstants.PROP_B_M;
    /** Proportional, bold italic, medium */
    public final static int PROP_BI_M   = FontResourcesConstants.PROP_BI_M;
    /** Proportional, underline, medium */
    public final static int PROP_U_M    = FontResourcesConstants.PROP_U_M;
    /** Proportional, underline italic, medium */
    public final static int PROP_UI_M   = FontResourcesConstants.PROP_UI_M;
    /** Proportional, underline bold, medium */
    public final static int PROP_UB_M   = FontResourcesConstants.PROP_UB_M;
    /** Proportional, underline bold italic, medium */
    public final static int PROP_UBI_M  = FontResourcesConstants.PROP_UBI_M;
    
    /** Proportional, plain, large */
    public final static int PROP_P_L    = FontResourcesConstants.PROP_P_L;
    /** Proportional, italic, large */
    public final static int PROP_I_L    = FontResourcesConstants.PROP_I_L;
    /** Proportional, bold, large */
    public final static int PROP_B_L    = FontResourcesConstants.PROP_B_L;
    /** Proportional, bold italic, large */
    public final static int PROP_BI_L   = FontResourcesConstants.PROP_BI_L;
    /** Proportional, underline, large */
    public final static int PROP_U_L    = FontResourcesConstants.PROP_U_L;
    /** Proportional, underline italic, large */
    public final static int PROP_UI_L   = FontResourcesConstants.PROP_UI_L;
    /** Proportional, underline bold, large */
    public final static int PROP_UB_L   = FontResourcesConstants.PROP_UB_L;
    /** Proportional, underline bold italic, large */
    public final static int PROP_UBI_L  = FontResourcesConstants.PROP_UBI_L;
    
    /** System, plain, small */
    public final static int SYS_P_S     = FontResourcesConstants.SYS_P_S;
    /** System, italic, small */
    public final static int SYS_I_S     = FontResourcesConstants.SYS_I_S;
    /** System, bold, small */
    public final static int SYS_B_S     = FontResourcesConstants.SYS_B_S;
    /** System, bold italic, small */
    public final static int SYS_BI_S    = FontResourcesConstants.SYS_BI_S;
    /** System, underline, small */
    public final static int SYS_U_S     = FontResourcesConstants.SYS_U_S;
    /** System, underline italic, small */
    public final static int SYS_UI_S    = FontResourcesConstants.SYS_UI_S;
    /** System, underline bold, small */
    public final static int SYS_UB_S    = FontResourcesConstants.SYS_UB_S;
    /** System, underline bold italic, small */
    public final static int SYS_UBI_S   = FontResourcesConstants.SYS_UBI_S;
    
    /** System, plain, medium */
    public final static int SYS_P_M     = FontResourcesConstants.SYS_P_M;
    /** System, italic, medium */
    public final static int SYS_I_M     = FontResourcesConstants.SYS_I_M;
    /** System, bold, medium */
    public final static int SYS_B_M     = FontResourcesConstants.SYS_B_M;
    /** System, bold italic, medium */
    public final static int SYS_BI_M    = FontResourcesConstants.SYS_BI_M;
    /** System, underline, medium */
    public final static int SYS_U_M     = FontResourcesConstants.SYS_U_M;
    /** System, underline italic, medium */
    public final static int SYS_UI_M    = FontResourcesConstants.SYS_UI_M;
    /** System, underline bold, medium */
    public final static int SYS_UB_M    = FontResourcesConstants.SYS_UB_M;
    /** System, underline bold italic, medium */
    public final static int SYS_UBI_M   = FontResourcesConstants.SYS_UBI_M;
    
    /** System, plain, large */
    public final static int SYS_P_L     = FontResourcesConstants.SYS_P_L;
    /** System, italic, large */
    public final static int SYS_I_L     = FontResourcesConstants.SYS_I_L;
    /** System, bold, large */
    public final static int SYS_B_L     = FontResourcesConstants.SYS_B_L;
    /** System, bold italic, large */
    public final static int SYS_BI_L    = FontResourcesConstants.SYS_BI_L;
    /** System, underline, large */
    public final static int SYS_U_L     = FontResourcesConstants.SYS_U_L;
    /** System, underline italic, large */
    public final static int SYS_UI_L    = FontResourcesConstants.SYS_UI_L;
    /** System, underline bold, large */
    public final static int SYS_UB_L    = FontResourcesConstants.SYS_UB_L;
    /** System, underline bold italic, large */
    public final static int SYS_UBI_L   = FontResourcesConstants.SYS_UBI_L;


    /**
     * This is a static convenience method for retrieving a
     * system Font object based on an identifier. The identifier
     * must be one of the values defined in FontResourcesConstants, 
     * ie, MONO_S_P, SYS_L_UBI, etc.
     *
     * @param fontID the integer identifier for the Font to retrieve
     * @return the system Font corresponding to the given integer id,
     *         null if the fontID is not a valid identifier.
     *         Note, this Font may not be exactly what is requested and
     *         has the same caveats as the normal Font constructor in
     *         terms of what gets returned versus the parameters given.
     */
    static Font getFont(int fontID) {
        int face, size, style;
        
        if (fontID >= 700) {
            face = Font.FACE_SYSTEM;
            fontID -= 700;   
        } else if (fontID >= 400) {
            face = Font.FACE_PROPORTIONAL;
            fontID -= 400;
        } else if (fontID >= 100) {
            face = Font.FACE_MONOSPACE;
            fontID -= 100;
        } else {
            return null;
        }
            
        if (fontID >= 200) {
            size = Font.SIZE_LARGE;
            fontID -= 200;
        } else if (fontID >= 100) {
            size = Font.SIZE_MEDIUM;
            fontID -= 100;
        } else {
            size = Font.SIZE_SMALL;
        }
        
        switch (fontID) {
            case 0:
                style = Font.STYLE_PLAIN;
                break;
            case 1:
                style = Font.STYLE_ITALIC;
                break;
            case 2:
                style = Font.STYLE_BOLD;
                break;
            case 3:
                style = Font.STYLE_ITALIC | Font.STYLE_BOLD;
                break;
            case 4:
                style = Font.STYLE_UNDERLINED;
                break;
            case 5:
                style = Font.STYLE_UNDERLINED | Font.STYLE_ITALIC;
                break;
            case 6:
                style = Font.STYLE_UNDERLINED | Font.STYLE_BOLD;
                break;
            case 7:
                style = Font.STYLE_UNDERLINED | Font.STYLE_BOLD |
                    Font.STYLE_ITALIC;
                break;
            default:
                return null;
        }
        
        return Font.getFont(face, style, size);
    }
}

