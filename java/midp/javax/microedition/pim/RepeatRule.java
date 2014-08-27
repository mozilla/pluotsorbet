/*
 * Portions Copyright  2000-2009 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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
 
/*
 * Copyright (C) 2002-2003 PalmSource, Inc.  All Rights Reserved.
 */

package javax.microedition.pim;

import com.sun.j2me.pim.PIMHandler;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class is defined by the JSR-75 specification
 * <em>PDA Optional Packages for the J2ME&trade; Platform</em>
 */
// JAVADOC COMMENT ELIDED
public class RepeatRule {
    /** Fields in the rule. */
    private Hashtable fields = new Hashtable();
    /** Exceptions caused by the rule. */
    private Vector exceptions = new Vector();
    // JAVADOC COMMENT ELIDED
    public RepeatRule() { }
    // JAVADOC COMMENT ELIDED
    public static final int FREQUENCY = 0;
    // JAVADOC COMMENT ELIDED
    public static final int DAY_IN_MONTH = 1;
    // JAVADOC COMMENT ELIDED
    public static final int DAY_IN_WEEK = 2;
    // JAVADOC COMMENT ELIDED
    public static final int DAY_IN_YEAR = 4;
    // JAVADOC COMMENT ELIDED
    public static final int MONTH_IN_YEAR = 8;
    // JAVADOC COMMENT ELIDED
    public static final int WEEK_IN_MONTH = 16;
    // JAVADOC COMMENT ELIDED
    public static final int COUNT = 32;
    // JAVADOC COMMENT ELIDED
    public static final int END = 64;
    // JAVADOC COMMENT ELIDED
    public static final int INTERVAL = 128;
    // JAVADOC COMMENT ELIDED
    public static final int DAILY = 0x10;
    // JAVADOC COMMENT ELIDED
    public static final int WEEKLY = 0x11;
    // JAVADOC COMMENT ELIDED
    public static final int MONTHLY = 0x12;
    // JAVADOC COMMENT ELIDED
    public static final int YEARLY = 0x13;
    // JAVADOC COMMENT ELIDED
    public static final int FIRST = 0x1;
    // JAVADOC COMMENT ELIDED
    public static final int SECOND = 0x2;
    // JAVADOC COMMENT ELIDED
    public static final int THIRD = 0x4;
    // JAVADOC COMMENT ELIDED
    public static final int FOURTH = 0x8;
    // JAVADOC COMMENT ELIDED
    public static final int FIFTH = 0x10;
    // JAVADOC COMMENT ELIDED
    public static final int LAST = 0x20;
    // JAVADOC COMMENT ELIDED
    public static final int SECONDLAST = 0x40;
    // JAVADOC COMMENT ELIDED
    public static final int THIRDLAST = 0x80;
    // JAVADOC COMMENT ELIDED
    public static final int FOURTHLAST = 0x100;
    // JAVADOC COMMENT ELIDED
    public static final int FIFTHLAST = 0x200;
    // JAVADOC COMMENT ELIDED
    public static final int SATURDAY = 0x400;
    // JAVADOC COMMENT ELIDED
    public static final int FRIDAY = 0x800;
    // JAVADOC COMMENT ELIDED
    public static final int THURSDAY = 0x1000;
    // JAVADOC COMMENT ELIDED
    public static final int WEDNESDAY = 0x2000;
    // JAVADOC COMMENT ELIDED
    public static final int TUESDAY = 0x4000;
    // JAVADOC COMMENT ELIDED
    public static final int MONDAY = 0x8000;
    // JAVADOC COMMENT ELIDED
    public static final int SUNDAY = 0x10000;
    // JAVADOC COMMENT ELIDED
    public static final int JANUARY = 0x20000;
    // JAVADOC COMMENT ELIDED
    public static final int FEBRUARY = 0x40000;
    // JAVADOC COMMENT ELIDED
    public static final int MARCH = 0x80000;
    // JAVADOC COMMENT ELIDED
    public static final int APRIL = 0x100000;
    // JAVADOC COMMENT ELIDED
    public static final int MAY = 0x200000;
    // JAVADOC COMMENT ELIDED
    public static final int JUNE = 0x400000;
    // JAVADOC COMMENT ELIDED
    public static final int JULY = 0x800000;
    // JAVADOC COMMENT ELIDED
    public static final int AUGUST = 0x1000000;
    // JAVADOC COMMENT ELIDED
    public static final int SEPTEMBER = 0x2000000;
    // JAVADOC COMMENT ELIDED
    public static final int OCTOBER = 0x4000000;
    // JAVADOC COMMENT ELIDED
    public static final int NOVEMBER = 0x8000000;
    // JAVADOC COMMENT ELIDED
    public static final int DECEMBER = 0x10000000;

    /** Months of the year. */
    private static final int[] MONTHS = {
        JANUARY, FEBRUARY, MARCH, APRIL,
        MAY, JUNE, JULY, AUGUST,
        SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER
    };
    /** DAY_INCREMENT = 86400000l. */
    private static final long DAY_INCREMENT = 86400000l;
    /** DAY_IN_WEEK_MASK = 0x1fc00l. */
    private static final long DAY_IN_WEEK_MASK = 0x1fc00l;
    /** WEEK_IN_MONTH_MASK = 0x3ffl. */
    private static final long WEEK_IN_MONTH_MASK = 0x3ffl;
    /** MONTH_IN_YEAR_MASK = 0x1ffe0000. */
    private static final long MONTH_IN_YEAR_MASK = 0x1ffe0000;
    /** Days of the week. */
    private static final int[] DAYS = {
        SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY
    };
    /**  DAY_LENGTH = 86400000L. */
    private static final long DAY_LENGTH = 86400000L;

    // JAVADOC COMMENT ELIDED
    public Enumeration dates(long startDate, long subsetBeginning,
			     long subsetEnding) {
        if (subsetBeginning > subsetEnding) {
            throw new IllegalArgumentException("Bad range: "
                + subsetBeginning + "("
                + PIMHandler.getInstance().composeDateTime(subsetBeginning)
                + ") to " + subsetEnding + "("
                + PIMHandler.getInstance().composeDateTime(subsetEnding));
        }
        Calendar calendar = Calendar.getInstance();
        Date dateObj = new Date(startDate);
        calendar.setTime(dateObj);
        Vector dates = new Vector();
        long date = startDate;
        Integer frequency = (Integer) getField(FREQUENCY, null);
        int interval = ((Integer) getField(INTERVAL,
					   new Integer(1))).intValue();
        int count = ((Integer) 
		     getField(COUNT, 
			      new Integer(Integer.MAX_VALUE))).intValue();
        long end = ((Long) getField(END, new Long(Long.MAX_VALUE))).longValue();
        Integer dayInWeek = (Integer) getField(DAY_IN_WEEK, null);
        Integer dayInMonth = (Integer) getField(DAY_IN_MONTH, null);
        Integer dayInYear = (Integer) getField(DAY_IN_YEAR, null);
        Integer weekInMonth = (Integer) getField(WEEK_IN_MONTH, null);
        Integer monthInYear = (Integer) getField(MONTH_IN_YEAR, null);
        // set defaults, based on starting date
        if (dayInMonth == null && weekInMonth == null) {
            dayInMonth = new Integer(calendar.get(Calendar.DAY_OF_MONTH));
        }
        if (dayInWeek == null) {
            switch (calendar.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.SUNDAY:
		    dayInWeek = new Integer(SUNDAY);
		    break;
                case Calendar.MONDAY:
		    dayInWeek = new Integer(MONDAY);
		    break;
                case Calendar.TUESDAY:
		    dayInWeek = new Integer(TUESDAY);
		    break;
                case Calendar.WEDNESDAY:
		    dayInWeek = new Integer(WEDNESDAY);
		    break;
                case Calendar.THURSDAY:
		    dayInWeek = new Integer(THURSDAY);
		    break;
                case Calendar.FRIDAY: 
		    dayInWeek = new Integer(FRIDAY);
		    break;
                case Calendar.SATURDAY: 
		    dayInWeek = new Integer(SATURDAY);
		    break;
            }
        }
        long rangeStart = Math.max(subsetBeginning, startDate);
        long rangeEnd = Math.min(subsetEnding, end);
        for (int i = 0; date <= subsetEnding && date <= end && i < count; i++) {
            if (frequency == null) {
                // no repetitions
                storeDate(dates, startDate, rangeStart, rangeEnd);
                break;
            }
            switch (frequency.intValue()) {
                case DAILY:
                    storeDate(dates, date, rangeStart, rangeEnd);
                    date += DAY_INCREMENT * interval;
                    dateObj.setTime(date);
                    calendar.setTime(dateObj);
                    break;
                case WEEKLY:
                    if (dayInWeek == null) {
                        storeDate(dates, date, rangeStart, rangeEnd);
                    } else {
                        // shift date to the beginning of the week
                        date -= DAY_INCREMENT * 
                            (calendar.get(Calendar.DAY_OF_WEEK) -
                            Calendar.SUNDAY);
                        dateObj.setTime(date);
                        calendar.setTime(dateObj);
                        storeDays(dates, date, rangeStart, rangeEnd,
                            dayInWeek.intValue());
                    }
                    // increment the week
                    date += DAY_INCREMENT * 7 * interval;
                    dateObj.setTime(date);
                    calendar.setTime(dateObj);
                    break;
                case MONTHLY: {
                    storeDaysByMonth(dates, date, rangeStart, rangeEnd,
                        dayInWeek, dayInMonth, weekInMonth);
                    // increment the month
                    for (int j = 0; j < interval; j++) {
                        int currentMonth = calendar.get(Calendar.MONTH);
                        if (currentMonth == Calendar.DECEMBER) {
                            int currentYear = calendar.get(Calendar.YEAR);
                            calendar.set(Calendar.YEAR, currentYear + 1);
                            calendar.set(Calendar.MONTH, Calendar.JANUARY);
                        } else {
                            calendar.set(Calendar.MONTH, currentMonth + 1);
                        }
                    }
                    dateObj = calendar.getTime();
                    date = dateObj.getTime();
                    break;
                }
                case YEARLY: {
                    if (monthInYear == null && dayInYear == null) {
                        storeDate(dates, date, rangeStart, rangeEnd);
                    } else {
                        // shift to January
                        calendar.set(Calendar.MONTH, Calendar.JANUARY);
                        if (monthInYear != null) {
                            dateObj = calendar.getTime();
                            date = dateObj.getTime();
                            int months = monthInYear.intValue();
                            for (int m = 0; m < MONTHS.length; m++) {
                                if ((months & MONTHS[m]) != 0) {
                                    calendar.set(Calendar.MONTH, m);
                                    storeDaysByMonth(dates,
                                        calendar.getTime().getTime(),
                                        rangeStart, rangeEnd,
                                        dayInWeek, dayInMonth, weekInMonth);
                                }
                            }
                        } else {
                            // dayInYear is non-null
                            // shift to the first of January
                            calendar.set(Calendar.DAY_OF_MONTH, 1);
                            dateObj = calendar.getTime();
                            date = dateObj.getTime();
                            storeDate(dates,
                                date + (dayInYear.intValue() - 1)
                                * DAY_INCREMENT,
                                rangeStart,
                                rangeEnd);
                        }
                    }
                    // increment the year
                    calendar.set(Calendar.YEAR, 
                        calendar.get(Calendar.YEAR) + interval);
                    dateObj = calendar.getTime();
                    date = dateObj.getTime();
                    break;
                }
                default:
                    throw new IllegalArgumentException(
                        "Unrecognized value for frequency: " + frequency);
            } // end switch
        } // end for
        return dates.elements();
    }

    /**
     * Checks if the given date is within the interval of startDate and endDate
     * with a granularity of one day.
     *
     * @param date the date to check
     * @param startDate start of the interval
     * @param endDate end of the interval
     * @return <code>true</code> if the date is within the interval,
     *         <code>false</code> otherwise
     */
    private static boolean betweenDatesByDaily(long date, long startDate,
        long endDate) {
        // Adjust startDate and endDate to keep YYYYMMDD part only
        startDate = startDate - (startDate % DAY_INCREMENT);
        endDate = endDate - (endDate % DAY_INCREMENT) + (DAY_INCREMENT - 1);

        return  (date >= startDate && date <= endDate);
    }

    /**
     * Compares two dates with a granularity of one day.
     *
     * @param date1 first date to compare
     * @param date2 second date to compare
     * @return <code>true</code> if the dates are equal,
     *         <code>false</code> otherwise
     */
    private static boolean equalDays(long date1, long date2) {
        return
            date1 - (date1 % DAY_INCREMENT) == date2 - (date2 % DAY_INCREMENT);
    }    

    /**
     * Checks if the given date is contained in the given dates vector, with a
     * granularity of one day.
     *
     * @param dates the vector to search in
     * @param date the date to look for
     * @return <code>true</code> if the date is contained in the vector,
     *         <code>false</code> otherwise
     */
    private static boolean containsDateByDaily(Vector dates, long date) {
        Enumeration e = dates.elements();

        while (e.hasMoreElements()) {
            Date date1 = (Date)e.nextElement();
            if (equalDays(date1.getTime(), date)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Stores a date.
     * @param dates array to extend
     * @param date to be stored
     * @param rangeStart beginning of range
     * @param rangeEnd end of range
     */
    private void storeDate(Vector dates, long date, long rangeStart, 
        long rangeEnd) {
        // Check if this date is between rangeStart and rangeEnd
        if (betweenDatesByDaily(date, rangeStart, rangeEnd)) {
            // Check if this date is an exceptional date
            if (!containsDateByDaily(exceptions, date)) {
                dates.addElement(new Date(date));
            }
        }
    }

    /** 
     * Store days.
     * @param dates array to extend
     * @param date to be stored
     * @param rangeStart beginning of range
     * @param rangeEnd end of range
     * @param days filter by specific days
     */    
    private void storeDays(Vector dates, long date,
        long rangeStart, long rangeEnd,
        int days) {
        // shift date back to Sunday, if it is not already Sunday
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(date));
        int dayShift = cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
        date -= dayShift * DAY_INCREMENT;
        long dateNextWeek = date + DAY_INCREMENT * 7;
        for (int i = 0; i < DAYS.length; i++) {
            if ((days & DAYS[i]) != 0) {
                long targetDate = (dayShift > i) ? dateNextWeek : date;
                storeDate(dates,
                    targetDate + DAY_INCREMENT * i,
                    rangeStart, rangeEnd);
            }
        }
    }
    
    /** 
     * Store days by month.
     * @param dates array to be extended
     * @param date date to be added.
     * @param rangeStart beginning of range
     * @param rangeEnd end of range
     * @param dayInWeek filter for day in the week
     * @param dayInMonth filter for day in the month
     * @param weekInMonth filter for week in the month
     */
    private void storeDaysByMonth(Vector dates, long date,
        long rangeStart, long rangeEnd,
        Integer dayInWeek, Integer dayInMonth, Integer weekInMonth) {
        // move date to the first of the month
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(date));
        date -= DAY_INCREMENT *
            (calendar.get(Calendar.DAY_OF_MONTH) - 1);
        if (dayInMonth != null) {
            storeDate(dates,
            date + DAY_INCREMENT * (dayInMonth.intValue() - 1),
            rangeStart, rangeEnd);
        } else if (weekInMonth != null) {
            // get a limited range, containing only this month.
            long monthRangeStart = Math.max(rangeStart, date);
            // find the end of the month, assuming no month is longer
            // than 31 days
            long monthEnd = date + DAY_INCREMENT * 31;
            calendar.setTime(new Date(monthEnd));
            while (calendar.get(Calendar.DAY_OF_MONTH) > 1) {
                monthEnd -= DAY_INCREMENT;
                calendar.setTime(new Date(monthEnd));
            }
            monthEnd -= DAY_INCREMENT;
            long monthRangeEnd = Math.min(rangeEnd, monthEnd);
            int weeks = weekInMonth.intValue();
            if ((weeks & FIRST) != 0) {
                storeDays(dates, date,
                monthRangeStart, monthRangeEnd,
                dayInWeek.intValue());
            }
            if ((weeks & SECOND) != 0) {
                storeDays(dates, date + DAY_INCREMENT * 7,
                monthRangeStart, monthRangeEnd,
                dayInWeek.intValue());
            }
            if ((weeks & THIRD) != 0) {
                storeDays(dates, date +  DAY_INCREMENT * 14,
                monthRangeStart, monthRangeEnd,
                dayInWeek.intValue());
            }
            if ((weeks & FOURTH) != 0) {
                storeDays(dates, date +  DAY_INCREMENT * 21,
                monthRangeStart, monthRangeEnd,
                dayInWeek.intValue());
            }
            if ((weeks & FIFTH) != 0) {
                storeDays(dates, date +  DAY_INCREMENT * 28,
                monthRangeStart, monthRangeEnd,
                dayInWeek.intValue());
            }
            if ((weeks & LAST) != 0) {
                storeDays(dates, monthEnd - DAY_INCREMENT * 6,
                monthRangeStart, monthRangeEnd,
                dayInWeek.intValue());
            }
            if ((weeks & LAST) != 0) {
                storeDays(dates, monthEnd - DAY_INCREMENT * 6,
                monthRangeStart, monthRangeEnd,
                dayInWeek.intValue());
            }
            if ((weeks & SECONDLAST) != 0) {
                storeDays(dates, monthEnd - DAY_INCREMENT * 13,
                monthRangeStart, monthRangeEnd,
                dayInWeek.intValue());
            }
            if ((weeks & THIRDLAST) != 0) {
                storeDays(dates, monthEnd - DAY_INCREMENT * 20,
                monthRangeStart, monthRangeEnd,
                dayInWeek.intValue());
            }
            if ((weeks & FOURTHLAST) != 0) {
                storeDays(dates, monthEnd - DAY_INCREMENT * 27,
                monthRangeStart, monthRangeEnd,
                dayInWeek.intValue());
            }
            if ((weeks & FIFTHLAST) != 0) {
                storeDays(dates, monthEnd - DAY_INCREMENT * 34,
                monthRangeStart, monthRangeEnd,
                dayInWeek.intValue());
            }
        }
    }

    // JAVADOC COMMENT ELIDED
    public void addExceptDate(long date) {
        exceptions.addElement(new Date(date));
    }

    // JAVADOC COMMENT ELIDED
    public void removeExceptDate(long date) {
        exceptions.removeElement(new Date(date));
    }

    // JAVADOC COMMENT ELIDED
    public Enumeration getExceptDates() {
        Vector results = new Vector();
        for (Enumeration e = exceptions.elements();
	    e.hasMoreElements(); ) {
            Date date = (Date) e.nextElement();
            results.addElement(new Date(date.getTime()));
        }
        return results.elements();
    }

    // JAVADOC COMMENT ELIDED
    public int getInt(int field) {
        validateDataType(field, PIMItem.INT);
        return ((Integer) getField(field, NO_DEFAULT)).intValue();
    }
    /** NO_DEFAULT = "". */
    private static final Object NO_DEFAULT = "";
    
    /**
     * Gets the requested field contents.
     * @param field identifier for the requested field
     * @param defaultValue value to return if field is not found
     * @return requetsed field contents
     */
    private Object getField(int field, Object defaultValue) {
        Integer fieldKey = new Integer(field);
        Object fieldValue = fields.get(fieldKey);
        if (fieldValue == null) {
            if (defaultValue == NO_DEFAULT) {
                throw new FieldEmptyException();
            } else {
                return defaultValue;
            }
        }
        return fieldValue;
    }

    // JAVADOC COMMENT ELIDED
    public void setInt(int field, int value) {
        validateDataType(field, PIMItem.INT);
        boolean isValid;
        switch (field) {
            case COUNT:
                isValid = (value >= 1);
                break;
            case DAY_IN_MONTH:
                isValid = (value >= 1 && value <= 31);
                break;
            case DAY_IN_WEEK:
                isValid = (value & ~DAY_IN_WEEK_MASK) == 0;
                break;
            case FREQUENCY:
                switch (value) {
                    case DAILY:
                    case WEEKLY:
                    case MONTHLY:
                    case YEARLY:
                        isValid = true;
                        break;
                    default:
                        isValid = false;
                }
                break;
            case INTERVAL:
                isValid = (value >= 1);
                break;
            case MONTH_IN_YEAR:
                isValid = (value & ~MONTH_IN_YEAR_MASK) == 0;
                break;
            case WEEK_IN_MONTH:
                isValid = (value & ~WEEK_IN_MONTH_MASK) == 0;
                break;
            case DAY_IN_YEAR:
                isValid = (value >= 1 && value <= 366);
                break;
            default:
                isValid = false;
        }
        if (!isValid) {
            throw new IllegalArgumentException("Field value is invalid");
        }
        Integer fieldKey = new Integer(field);
        fields.put(fieldKey, new Integer(value));
    }

    // JAVADOC COMMENT ELIDED
    public long getDate(int field) {
        validateDataType(field, PIMItem.DATE);
        return ((Long) getField(field, NO_DEFAULT)).longValue();
    }

    // JAVADOC COMMENT ELIDED
    public void setDate(int field, long value) {
        validateDataType(field, PIMItem.DATE);
        Integer fieldKey = new Integer(field);
        fields.put(fieldKey, new Long(value));
    }

    // JAVADOC COMMENT ELIDED
    public int[] getFields() {
        int[] result = new int[fields.size()];
        int i = 0;
        for (Enumeration e = fields.keys(); e.hasMoreElements(); ) {
            Integer fieldKey = (Integer) e.nextElement();
            result[i++] = fieldKey.intValue();
        }
        return result;
    }

    // JAVADOC COMMENT ELIDED
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof RepeatRule)) {
            return false;
        }
        RepeatRule rule = (RepeatRule) obj;
        Calendar cal = Calendar.getInstance();
        int[] ruleFields = rule.getFields();
        for (int i = 0; i < ruleFields.length; i++) {
            int field = ruleFields[i];
            Object value = fields.get(new Integer(field));
            if (value == null) {
                // field in other rule is defined, but in this rule is not
                return false;
            }
            switch (getDataType(field)) {
                case PIMItem.INT: {
                    int iValue = ((Integer) value).intValue();
                    if (rule.getInt(field) != iValue) {
                        return false;
                    }
                    break;
                }
                case PIMItem.DATE: {
                    // dates match if they are on the same day
                    long thisDate = ((Long) value).longValue();
                    long ruleDate = rule.getDate(field);
                    if (thisDate == ruleDate) {
                        return true;
                    }
                    if (Math.abs(thisDate - ruleDate) >= DAY_LENGTH) {
                        return false;
                    }
                    cal.setTime(new Date(thisDate));
                    int day = cal.get(Calendar.DATE);
                    cal.setTime(new Date(ruleDate));
                    if (day != cal.get(Calendar.DATE)) {
                        return false;
                    }
                    break;
                }
                default:
                    return false; // unreachable
            }
            
        }
        // see if this rule defines any fields that the other rule does not
        for (Enumeration e = fields.keys(); e.hasMoreElements(); ) {
            Integer fieldKey = (Integer) e.nextElement();
            int field = fieldKey.intValue();
            boolean match = false;
            for (int i = 0; i < ruleFields.length && !match; i++) {
                if (ruleFields[i] == field) {
                    match = true;
                }
            }
            if (!match) {
                return false;
            }
        }
        // check exception dates
        // normalize the list of exception dates to represent only the date
        // and not the time of day
        int[] exceptionDates = new int[exceptions.size()];
        for (int i = 0; i < exceptionDates.length; i++) {
            Date date = (Date) exceptions.elementAt(i);
            cal.setTime(date);
            exceptionDates[i] = cal.get(Calendar.DAY_OF_MONTH)
                + 100 * cal.get(Calendar.MONTH)
                + 10000 * cal.get(Calendar.YEAR);
        }
        boolean[] matchedExceptionDates = new boolean[exceptionDates.length];
        for (Enumeration e = rule.getExceptDates(); e.hasMoreElements(); ) {
            Date date = (Date) e.nextElement();
            cal.setTime(date);
            int day = cal.get(Calendar.DAY_OF_MONTH)
                + 100 * cal.get(Calendar.MONTH)
                + 10000 * cal.get(Calendar.YEAR);
            boolean match = false;
            for (int i = 0; i < exceptionDates.length && !match; i++) {
                if (exceptionDates[i] == day) {
                    match = true;
                    matchedExceptionDates[i] = true;
                }
            }
            if (!match) {
                return false;
            }
        }
        // are there unmatched exception dates?
        for (int i = 0; i < matchedExceptionDates.length; i++) {
            if (!matchedExceptionDates[i]) {
                // make sure this isn't a duplicate of another date
                boolean duplicate = false;
                for (int j = 0; j < i && !duplicate; j++) {
                    duplicate = exceptionDates[j] == exceptionDates[i];
                }
                if (!duplicate) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks that data type is valid.
     * @param field identifier of requested field
     * @param dataType type of data to be checked
     * @throws IllegalArgumentException if type is not appropriate
     */
    private void validateDataType(int field, int dataType) {
        int correctDataType = getDataType(field);
        if (dataType != correctDataType) {
            throw new IllegalArgumentException("Invalid field type");
        }
    }
    
    /**
     * Gets the data type for the requested field.
     * @param field identifier of requested field
     * @return data type of requested field
     */
    private int getDataType(int field) {
        switch (field) {
            case COUNT:
            case DAY_IN_MONTH:
            case DAY_IN_WEEK:
            case DAY_IN_YEAR:
            case FREQUENCY:
            case INTERVAL:
            case MONTH_IN_YEAR:
            case WEEK_IN_MONTH:
                return PIMItem.INT;
            case END:
                return PIMItem.DATE;
            default:
                throw new IllegalArgumentException("Unrecognized field: "
						   + field);
        }
    }
}
