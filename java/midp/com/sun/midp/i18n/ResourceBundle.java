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

package com.sun.midp.i18n;

/**
 * The ResourceBundle interface is just a marker interface holding
 * a particular contract for any localized file to obey.
 */
public interface ResourceBundle {

    /**
     * Fetch the resource content. ......
     * @return  array of key value pairs
     */
    public String getString(int index);

    /**
     * Returns a locale-specific formatted date string.  By default,
     * it will return like "Fri, 05 Dec 2000".
     *
     * @param dayOfWeek day of week
     * @param date date
     * @param month month
     * @param year year
     * @return formatted date string
     */
    public String getLocalizedDateString(String dayOfWeek, 
					 String date,
					 String month,
					 String year);
    
    /**
     * Returns a locale-specific formatted time string.  By default,
     * it will return like "10:05:59 PM".
     *
     * @param hour hour
     * @param min minute
     * @param sec second
     * @param ampm AM or PM
     * @return formatted time string
     */
    public String getLocalizedTimeString(String hour, 
					 String min, 
					 String sec, 
					 String ampm);
    
    
    /**
     * Returns a locale-specific formatted date and time string.  By
     * default, it will like return "Fri, 05 Dec 2000 10:05:59 PM".
     *
     * @param dayOfWeek day of week
     * @param date date
     * @param month month
     * @param year year
     * @param hour hour
     * @param min minute
     * @param sec second
     * @param ampm AM or PM 
     * @return formatted time and date string
     */
    public String 
	getLocalizedDateTimeString(String dayOfWeek, String date, 
				   String month, String year,
				   String hour, String min, 
				   String sec, String ampm);
    
    
    /**
     * Returns what the first day of the week is; e.g., Sunday in US,
     * Monday in France.
     * @return numeric value for first day of week
     */
    public int getLocalizedFirstDayOfWeek();
    
    /**
     * Returns whether the AM_PM field comes after the time field 
     * or not.  
     * @return true, if AM/PM is after the time field.
     */
    public boolean isLocalizedAMPMafterTime();
}
