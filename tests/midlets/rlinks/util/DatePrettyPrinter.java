/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks.util;

import java.util.Date;

/**
 * Print dates in a human-readable format, such as "25 minutes ago" or
 * "4 days ago".
 */
public class DatePrettyPrinter {

    /**
     * Pretty-print the given date as a String, compared to the current time.
     *
     * @param date Date to pretty-print
     * @return Pretty-printed date as a String
     */
    public static String prettyPrint(Date date) {
        long now = new Date().getTime() / 1000;
        long then = date.getTime() / 1000;
        long diff = now - then;

        String result = "";
        long units = 0;
        String unit = "";

        if (diff < 60) {
            return "just now";
        } else if (diff < 3600) {
            // x minutes ago
            units = diff / 60;
            unit = (units > 1 ? "" + units : "a") + " minute" + (units > 1 ? "s" : "");
        } else if (diff < 86400) {
            // x hours ago
            units = diff / 3600;
            unit = (units > 1 ? "" + units : "an") + " hour" + (units > 1 ? "s" : "");
        } else if (diff < 2592000) {
            // x months ago
            units = diff/86400;
            unit = (units > 1 ? "" + units : "a") + " day" + (units > 1 ? "s" : "");
        } else if (diff >= 31104000) {
            // x years ago
            units = diff/31104000;
            unit = (units > 1 ? "" + units : "a") + " year" + (units > 1 ? "s" : "");
        }

        // Past or future? e.g. "x days ago" or "in x days"
        if (diff >= 0) {
            result = unit + " ago";
        } else {
            result = "in " + unit;
        }
        return result;
    }

}
