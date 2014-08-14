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

package com.sun.j2me.i18n;

/**
 * Intermediate class for logging facilities
 */
public class Resource extends com.sun.midp.i18n.Resource {
    static final int[] months = { ResourceConstants.LCDUI_DF_JAN_SHORT, 
                                  ResourceConstants.LCDUI_DF_FEB_SHORT,
                                  ResourceConstants.LCDUI_DF_MAR_SHORT, 
                                  ResourceConstants.LCDUI_DF_APR_SHORT,
                                  ResourceConstants.LCDUI_DF_MAY_SHORT, 
                                  ResourceConstants.LCDUI_DF_JUN_SHORT,
                                  ResourceConstants.LCDUI_DF_JUL_SHORT,
                                  ResourceConstants.LCDUI_DF_AUG_SHORT,
                                  ResourceConstants.LCDUI_DF_SEP_SHORT, 
                                  ResourceConstants.LCDUI_DF_OCT_SHORT,
                                  ResourceConstants.LCDUI_DF_NOV_SHORT,
                                  ResourceConstants.LCDUI_DF_DEC_SHORT };

    static final int[] days = { ResourceConstants.LCDUI_DF_SUN, 
                                ResourceConstants.LCDUI_DF_MON,
                                ResourceConstants.LCDUI_DF_TUE,
                                ResourceConstants.LCDUI_DF_WED, 
                                ResourceConstants.LCDUI_DF_THU, 
                                ResourceConstants.LCDUI_DF_FRI, 
                                ResourceConstants.LCDUI_DF_SAT };
        
    
    public static String getMonthName(int key) {
        return getString(months[key]);
    }
    
    public static String getShortDayName(int key) {
        return getString(days[key]);
    }
}
