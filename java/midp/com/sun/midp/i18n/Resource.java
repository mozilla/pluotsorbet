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

import java.util.Hashtable;
import com.sun.midp.main.Configuration;
import com.sun.midp.l10n.*;
import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * The Resource class retrieves the locale specific values such
 * as label strings and date formats from the locale specific
 * subclasses of com.sun.midp.i18n.ResourceBundle class. 
 * The default locale is assumed to be en_US, meaning,
 * in the absence of any locale information being specified, en_US
 * will be assumed as the default locale.
 *
 * A subclass of ResourceBundle namely, 
 * com.sun.midp.l10n.LocalizedStrings 
 * is the default localization file, for a default locale of en_US.
 * This also acts as a template for future localization and is 
 * easily localizable. The new localized file created should be 
 * accompanied with a locale name following an underscore: for
 * example, a German one would be named "LocalizedStrings_de".
 * In this way, as many related locale-specific classes as needed
 * can be provided.
 * 
 * The location of such locale-specific classes is expected to be
 * "com.sun.midp.l10n".  
 */
abstract public class Resource {
    /** Local handle to the current Resource structure. */
    private static ResourceBundle res = null;

    static {
	res = null;
	String loc = Configuration.getProperty("microedition.locale");
	
	if ((loc == null) || (loc.equals("en-US"))) {
	    // the default case
	    res = (ResourceBundle) new LocalizedStrings();
	} else {
	    String cls = "com.sun.midp.l10n.LocalizedStrings";
            /* 
             * This only checks for the first '-' in the locale, and
             * convert to '_' for Class.forName() to work.
             */
            int hyphen;
        if ((hyphen = loc.indexOf('-')) != -1) {
                StringBuffer tmploc = new StringBuffer(loc);
                tmploc.setCharAt(hyphen, '_');
                loc = tmploc.toString();
        }
	    
            while (true) {
                try {
                    Class c = Class.forName(cls + "_" + loc);
                    if (c != null) {
                        res = (ResourceBundle) c.newInstance();
                    }
                } catch (Throwable t) {}
                if (res == null) {
                    int pos = loc.lastIndexOf('_');
                    if (pos != -1) {
                        loc = loc.substring(0, pos);
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }

	if (res == null) {
	    if (Logging.REPORT_LEVEL <= Logging.ERROR) {
		Logging.report(Logging.ERROR, LogChannels.LC_I18N,
			       "Just can't proceed! Resource is NULL!!");
	    }

        // the default case
	    res = (ResourceBundle) new LocalizedStrings();
            // Porting suggestion:
            // System should quit MIDP runtime since resource is 
            // not available. 
	}
    }

    /**
     * Returns a localized string for the integer key.
     * @param key used to search the value pair.
     * @return the requested localized resource string.
     */
    public static String getString(int key) {
        return res.getString(key);
    }
    
    /**
     * Returns a localized string for the argument string after substituting
     * values for the "%d" tokens in the localized string, where "d" is 1-9
     * and representing a values 0-8 in an array. The tokens can be in any
     * order in the string. If the localized String is not found
     * the key is used as the localized string. If a "%" is not followed by
     * 1-9 then the "%" is dropped but the next char is put directly into the
     * output string, so "%%" will be "%" in the output and not count as part
     * of a token. Another example would be that "%a" would be just be "a".
     * <p>
     * For example, given "%2 had a little %1." and {"lamb", "Mary"} and there
     * is no localized string for the key, the result would be:
     * <p>
     * <blockquote>"Mary had a little lamb."</blockquote>
     *
     * @param key an original string in the source code with optional
     *            substitution tokens
     * @param values values to substitute for the tokens in the resource
     * @return value of named resource with the tokens substituted
     * @exception ArrayIndexOutOfBoundsException if there are not enough values
     *            to substitute
     */
    public static String getString(int key, String[] values) {
        String str = getString(key);
		if (str == null) {
                    return null;
		}
		return getString(str, values);
	}

    /**
     * Returns a localized string for the argument string after substituting
     * values for the "%d" tokens in the localized string, where "d" is 1-9
     * and representing a values 0-8 in an array. The tokens can be in any
     * order in the string. If the localized String is not found
     * the key is used as the localized string. If a "%" is not followed by
     * 1-9 then the "%" is dropped but the next char is put directly into the
     * output string, so "%%" will be "%" in the output and not count as part
     * of a token. Another example would be that "%a" would be just be "a".
     * <p>
     * For example, given "%2 had a little %1." and {"lamb", "Mary"} and there
     * is no localized string for the key, the result would be:
     * <p>
     * <blockquote>"Mary had a little lamb."</blockquote>
     *
     * @param key an original string in the source code with optional
     *            substitution tokens
     * @param values values to substitute for the tokens in the resource
     * @return value of named resource with the tokens substituted
     * @exception ArrayIndexOutOfBoundsException if there are not enough values
     *            to substitute
     */
    public static String getString(String str, String[] values) {
        boolean tokenMarkerFound = false;
        StringBuffer output;
        char currentChar;
        int length;
	
        if (str == null) {
            return null;
        }
	
        length = str.length();
        output = new StringBuffer(length * 2); // try to avoid resizing
	
        for (int i = 0; i < length; i++) {
            currentChar = str.charAt(i);
	    
            if (tokenMarkerFound) {
                if (currentChar < '1' || currentChar > '9') {
                    // covers the "%%" case
                    output.append(currentChar);
                } else {
                    // substitute a value, "1" is index 0 into the value array
                    output.append(values[currentChar - '1']);
                }
		
                tokenMarkerFound = false;
            } else if (currentChar == '%') {
                tokenMarkerFound = true;
            } else {
                output.append(currentChar);
            }
        }
	
        return output.toString();
    }

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
    public static String getDateString(String dayOfWeek, 
				       String date,
				       String month,
				       String year) {
	return res.getLocalizedDateString(dayOfWeek,
					  date,
					  month,
					  year);
    }
    
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
    public static String getTimeString(String hour, String min, 
				       String sec, String ampm) {
	return res.getLocalizedTimeString(hour, min, sec, ampm);
    }
    
    
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
    public static String 
	getDateTimeString(String dayOfWeek, String date, 
			  String month, String year,
			  String hour, String min, 
			  String sec, String ampm) {
	return res.getLocalizedDateTimeString(dayOfWeek, date,
					      month, year,
					      hour, min,
					      sec, ampm);
    }
    
    
    /**
     * Returns what the first day of the week is; e.g., Sunday in US,
     * Monday in France.
     * @return numeric value for first day of week
     */
    public static int getFirstDayOfWeek() {
	return res.getLocalizedFirstDayOfWeek();
    }
    
    /**
     * Returns whether the AM_PM field comes after the time field 
     * or not.  
     * @return true, if AM/PM is after the time field.
     */
    public static boolean isAMPMafterTime() {
	return res.isLocalizedAMPMafterTime();
    }
}
