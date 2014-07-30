/*
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

/**
 * There are three different font faces available in MIDP:
 * Monospace, Proportional, and System. There are eight 
 * different styles available: Plain, Italic, Bold, Bold Italic,
 * Underline, Underline Italic, Underline Bold, and 
 * Underline Bold Italic. There are three different sizes 
 * available: Small, Medium, and Large. That presents
 * a matrix of 72 different font possibilities. This class
 * defines a numeric identifer to each one for easy representation
 * and loading.
 *
 * Identifiers use a FACE_STYLE_SIZE naming scheme, where
 * FACE is one of [MONO|PROP|SYS], STYLE is one of 
 * [P|I|B|BI|U|UI|UB|UBI], and SIZE is one of [S|M|L].
 */
public class FontResourcesConstants {
    
    /** Monospaced, plain, small */
    public static final int MONO_P_S    = 100;
    /** Monospaced, italic, small */
    public static final int MONO_I_S    = 101;
    /** Monospaced, bold, small */
    public static final int MONO_B_S    = 102;
    /** Monospaced, bold italic, small */
    public static final int MONO_BI_S   = 103;
    /** Monospaced, underline, small */
    public static final int MONO_U_S    = 104;
    /** Monospaced, underline italic, small */
    public static final int MONO_UI_S   = 105;
    /** Monospaced, underline bold, small */
    public static final int MONO_UB_S   = 106;
    /** Monospaced, underline bold italic, small */
    public static final int MONO_UBI_S  = 107;

    /** Monospaced, plain, medium */
    public static final int MONO_P_M    = 200;
    /** Monospaced, italic, medium */
    public static final int MONO_I_M    = 201;
    /** Monospaced, bold, medium */
    public static final int MONO_B_M    = 202;
    /** Monospaced, bold italic, medium */
    public static final int MONO_BI_M   = 203;
    /** Monospaced, underline, medium */
    public static final int MONO_U_M    = 204;
    /** Monospaced, underline italic, medium */
    public static final int MONO_UI_M   = 205;
    /** Monospaced, underline bold, medium */
    public static final int MONO_UB_M   = 206;
    /** Monospaced, underline bold italic, medium */
    public static final int MONO_UBI_M  = 207;
    
    /** Monospaced, plain, large */
    public static final int MONO_P_L    = 300;
    /** Monospaced, italic, large */
    public static final int MONO_I_L    = 301;
    /** Monospaced, bold, large */
    public static final int MONO_B_L    = 302;
    /** Monospaced, bold italic, large */
    public static final int MONO_BI_L   = 303;
    /** Monospaced, underline, large */
    public static final int MONO_U_L    = 304;
    /** Monospaced, underline italic, large */
    public static final int MONO_UI_L   = 305;
    /** Monospaced, underline bold, large */
    public static final int MONO_UB_L   = 306;
    /** Monospaced, underline bold italic, large */
    public static final int MONO_UBI_L  = 307;
    
    /** Proportional, plain, small */
    public static final int PROP_P_S    = 400;
    /** Proportional, italic, small */
    public static final int PROP_I_S    = 401;
    /** Proportional, bold, small */
    public static final int PROP_B_S    = 402;
    /** Proportional, bold italic, small */
    public static final int PROP_BI_S   = 403;
    /** Proportional, underline, small */
    public static final int PROP_U_S    = 404;
    /** Proportional, underline italic, small */
    public static final int PROP_UI_S   = 405;
    /** Proportional, underline bold, small */
    public static final int PROP_UB_S   = 406;
    /** Proportional, underline bold italic, small */
    public static final int PROP_UBI_S  = 407;
    
    /** Proportional, plain, medium */
    public static final int PROP_P_M    = 500;
    /** Proportional, italic, medium */
    public static final int PROP_I_M    = 501;
    /** Proportional, bold, medium */
    public static final int PROP_B_M    = 502;
    /** Proportional, bold italic, medium */
    public static final int PROP_BI_M   = 503;
    /** Proportional, underline, medium */
    public static final int PROP_U_M    = 504;
    /** Proportional, underline italic, medium */
    public static final int PROP_UI_M   = 505;
    /** Proportional, underline bold, medium */
    public static final int PROP_UB_M   = 506;
    /** Proportional, underline bold italic, medium */
    public static final int PROP_UBI_M  = 507;
    
    /** Proportional, plain, large */
    public static final int PROP_P_L    = 600;
    /** Proportional, italic, large */
    public static final int PROP_I_L    = 601;
    /** Proportional, bold, large */
    public static final int PROP_B_L    = 602;
    /** Proportional, bold italic, large */
    public static final int PROP_BI_L   = 603;
    /** Proportional, underline, large */
    public static final int PROP_U_L    = 604;
    /** Proportional, underline italic, large */
    public static final int PROP_UI_L   = 605;
    /** Proportional, underline bold, large */
    public static final int PROP_UB_L   = 606;
    /** Proportional, underline bold italic, large */
    public static final int PROP_UBI_L  = 607;
    
    /** System, plain, small */
    public static final int SYS_P_S     = 700;
    /** System, italic, small */
    public static final int SYS_I_S     = 701;
    /** System, bold, small */
    public static final int SYS_B_S     = 702;
    /** System, bold italic, small */
    public static final int SYS_BI_S    = 703;
    /** System, underline, small */
    public static final int SYS_U_S     = 704;
    /** System, underline italic, small */
    public static final int SYS_UI_S    = 705;
    /** System, underline bold, small */
    public static final int SYS_UB_S    = 706;
    /** System, underline bold italic, small */
    public static final int SYS_UBI_S   = 707;
    
    /** System, plain, medium */
    public static final int SYS_P_M     = 800;
    /** System, italic, medium */
    public static final int SYS_I_M     = 801;
    /** System, bold, medium */
    public static final int SYS_B_M     = 802;
    /** System, bold italic, medium */
    public static final int SYS_BI_M    = 803;
    /** System, underline, medium */
    public static final int SYS_U_M     = 804;
    /** System, underline italic, medium */
    public static final int SYS_UI_M    = 805;
    /** System, underline bold, medium */
    public static final int SYS_UB_M    = 806;
    /** System, underline bold italic, medium */
    public static final int SYS_UBI_M   = 807;
    
    /** System, plain, large */
    public static final int SYS_P_L     = 900;
    /** System, italic, large */
    public static final int SYS_I_L     = 901;
    /** System, bold, large */
    public static final int SYS_B_L     = 902;
    /** System, bold italic, large */
    public static final int SYS_BI_L    = 903;
    /** System, underline, large */
    public static final int SYS_U_L     = 904;
    /** System, underline italic, large */
    public static final int SYS_UI_L    = 905;
    /** System, underline bold, large */
    public static final int SYS_UB_L    = 906;
    /** System, underline bold italic, large */
    public static final int SYS_UBI_L   = 907;
}

