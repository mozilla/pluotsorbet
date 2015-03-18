package com.sun.midp.l10n;

import com.sun.midp.i18n.ResourceBundle;
import com.sun.midp.i18n.ResourceConstants;

public class LocalizedStrings_it_IT extends LocalizedStringsBase 
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
     * In Italian, this will return:
     *     "05 Dec 2003"
     */
    public String getLocalizedDateString(String dayOfWeek,
                String date,
                String month,
                String year) {
        return date + " " + month + " " + year;
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
     * In Italian, this will return;
     *     "10:05:59"
     *
     */
    public String getLocalizedTimeString(String hour, String min,
                String sec, String ampm) {
        return hour + ":" + min;
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
     * In Italian, this will return:
     *     "Ven, 05 Dec 2000 10:05:59"
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
     */
    public int getLocalizedFirstDayOfWeek() {
        return java.util.Calendar.MONDAY;
    }

    /**
     * Returns whether AM_PM field comes after the time field or
     * not in this locale.
     * @return true for US.
     */
    public boolean isLocalizedAMPMafterTime() {
        return false;
    }
}
