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

package com.sun.midp.l10n;

import com.sun.midp.i18n.ResourceBundle;
import com.sun.midp.i18n.ResourceConstants;

/**
 * The English-US localization of ResourceBundle (the default, in
 * the absence of any locale info specified).
 * This also acts like a template for future localization experts
 * to work on, when creating locale specific strings. Look for
 * the message "L10N: REPLACE WITH LOCALE SPECIFIC VALUES" and
 * replace with locale specific values.
 *
 * LocalizedStringsBase.java is generated from 
 * src/configuration/configurator/share/l10n/en-US.xml
 */
public class LocalizedStrings extends LocalizedStringsBase 
                              implements ResourceBundle
{
    /**
     * Fetch the entire resource content.
     *
     * @return 2 dimension array of keys and US English strings.
     */
    public String getString(int index) {
        return getContent(index);
    }

    /**
     * Overrides ResourceBundle.getLocalizedDateString.
     * Returns a string representing the date in locale specific
     * date format.
     * @param dayOfWeek a String representing the day of the week.
     * @param date      a String representing the date.
     * @param month     a String representing the month.
     * @param year      a String representing the year.
     * @return a formatted date string that is suited for the target
     * language.
     * In English, this will return:
     *     "Dec 05, 2003"
     * (L10N: REPLACE WITH LOCALE SPECIFIC VALUE)
     */
    public String getLocalizedDateString(String dayOfWeek,
                String date,
                String month,
                String year) {
        return month + " " + date + ", " + year;
    }

    /**
     * Overrides ResourceBundle.getLocalizedTimeString.
     * Returns a string representing the time in locale specific
     * time format.
     * @param hour a String representing the hour.
     * @param min  a String representing the minute.
     * @param sec  a String representing the second.
     * @param ampm a String representing am or pm.
     *               Note that ampm can be null.
     * @return a formatted time string that is suited for the target
     * language.
     * In English, this will return;
     *     "10:05:59 PM"
     * (L10N: REPLACE WITH LOCALE SPECIFIC VALUE)
     *
     */
    public String getLocalizedTimeString(String hour, String min,
                String sec, String ampm) {
        return (hour + ":" + min + ((ampm == null) ? "" : (" " + ampm)));
    }

    /**
     * Overrides ResourceBundle.getLocalizedDateTimeString.
     * Returns the localized date time string value.
     * @param dayOfWeek a String representing the day of the week.
     * @param date      a String representing the date.
     * @param month     a String representing the month.
     * @param year      a String representing the year.
     * @param hour a String representing the hour.
     * @param min  a String representing the minute.
     * @param sec  a String representing the second.
     * @param ampm a String representing am or pm.
     *               Note that ampm can be null.
     * @return a formatted date and time string that is suited for the.
     * target language.
     * In English, this will return:
     *     "Fri, 05 Dec 2000 10:05:59 PM"
     * (L10N: REPLACE WITH LOCALE SPECIFIC VALUE)
     */
    public String getLocalizedDateTimeString(String dayOfWeek, String date,
                    String month, String year,
                    String hour, String min,
                    String sec, String ampm) {
	return getLocalizedDateString(dayOfWeek, date, month, year) + " " +
	    getLocalizedTimeString(hour, min, sec, ampm);
    }

    /**
     * Returns the locale specific first day of the week.
     * @return the first day of the week is; e.g., Sunday in US.
     * (L10N: REPLACE WITH LOCALE SPECIFIC VALUE)
     */
    public int getLocalizedFirstDayOfWeek() {
        return java.util.Calendar.SUNDAY;
    }

    /**
     * Returns whether AM_PM field comes after the time field or
     * not in this locale.
     * @return true for US.
     * (L10N: REPLACE WITH LOCALE SPECIFIC VALUE)
     */
    public boolean isLocalizedAMPMafterTime() {
        return true;
    }
}
