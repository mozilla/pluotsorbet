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

package javax.microedition.lcdui;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * A <code>DateField</code> is an editable component for presenting
 * date and time (calendar)
 * information that may be placed into a <code>Form</code>. Value for
 * this field can be
 * initially set or left unset. If value is not set then the UI for the field
 * shows this clearly. The field value for &quot;not initialized
 * state&quot; is not valid
 * value and <code>getDate()</code> for this state returns <code>null</code>.
 * <p>
 * Instance of a <code>DateField</code> can be configured to accept
 * date or time information
 * or both of them. This input mode configuration is done by
 * <code>DATE</code>, <code>TIME</code> or
 * <code>DATE_TIME</code> static fields of this
 * class. <code>DATE</code> input mode allows to set only
 * date information and <code>TIME</code> only time information
 * (hours, minutes). <code>DATE_TIME</code>
 * allows to set both clock time and date values.
 * <p>
 * In <code>TIME</code> input mode the date components of
 * <code>Date</code> object
 * must be set to the &quot;zero epoch&quot; value of January 1, 1970.
 * <p>
 * Calendar calculations in this field are based on default locale and defined
 * time zone. Because of the calculations and different input modes date object
 * may not contain same millisecond value when set to this field and get back
 * from this field.
 * @since MIDP 1.0
 */
public class DateField extends Item {

    /**
     * Input mode for date information (day, month, year). With this mode this
     * <code>DateField</code> presents and allows only to modify date
     * value. The time
     * information of date object is ignored.
     *
     * <P>Value <code>1</code> is assigned to <code>DATE</code>.</P>
     */
    public static final int DATE = 1;

    /**
     * Input mode for time information (hours and minutes). With this mode this
     * <code>DateField</code> presents and allows only to modify
     * time. The date components
     * should be set to the &quot;zero epoch&quot; value of January 1, 1970 and
     * should not be accessed.
     *
     * <P>Value <code>2</code> is assigned to <code>TIME</code>.</P>
     */
    public static final int TIME = 2;

    /**
     * Input mode for date (day, month, year) and time (minutes, hours)
     * information. With this mode this <code>DateField</code>
     * presents and allows to modify
     * both time and date information.
     *
     * <P>Value <code>3</code> is assigned to <code>DATE_TIME</code>.</P>
     */
    public static final int DATE_TIME = 3;

    /**
     * Creates a <code>DateField</code> object with the specified
     * label and mode. This call
     * is identical to <code>DateField(label, mode, null)</code>.
     *
     * @param label item label
     * @param mode the input mode, one of <code>DATE</code>, <code>TIME</code>
     * or <code>DATE_TIME</code>
     * @throws IllegalArgumentException if the input <code>mode's</code>
     * value is invalid
     */
    public DateField(String label, int mode) {
        this(label, mode, null);
    }

    /**
     * Creates a date field in which calendar calculations are based
     * on specific
     * <code>TimeZone</code> object and the default calendaring system for the
     * current locale.
     * The value of the <code>DateField</code> is initially in the
     * &quot;uninitialized&quot; state.
     * If <code>timeZone</code> is <code>null</code>, the system's
     * default time zone is used.
     *
     * @param label item label
     * @param mode the input mode, one of <code>DATE</code>, <code>TIME</code>
     * or <code>DATE_TIME</code>
     * @param timeZone a specific time zone, or <code>null</code> for the
     * default time zone
     * @throws IllegalArgumentException if the input <code>mode's</code> value
     * is invalid
     */
    public DateField(String label, int mode, java.util.TimeZone timeZone) {
        super(label);

        synchronized (Display.LCDUILock) {
            if ((mode != DATE) && (mode != TIME) && (mode != DATE_TIME)) {
                throw new IllegalArgumentException("Invalid input mode");
            }

            this.mode = mode;

            if (timeZone == null) {
                timeZone = TimeZone.getDefault();
            }

            this.currentDate = Calendar.getInstance(timeZone);

            itemLF = dateFieldLF = LFFactory.getFactory().getDateFieldLF(this);

        } // synchronized
    }

    /**
     * Returns date value of this field. Returned value is
     * <code>null</code> if field
     * value is
     * not initialized. The date object is constructed according the rules of
     * locale specific calendaring system and defined time zone.
     *
     * In <code>TIME</code> mode field the date components are set to
     * the &quot;zero
     * epoch&quot; value of January 1, 1970. If a date object that presents 
     * time beyond one day from this &quot;zero epoch&quot; then this field
     * is in &quot;not
     * initialized&quot; state and this method returns <code>null</code>.
     *
     * In <code>DATE</code> mode field the time component of the calendar is 
     * set to zero when constructing the date object.
     *
     * @return date object representing time or date depending on input mode
     * @see #setDate
     */
    public java.util.Date getDate() {
        synchronized (Display.LCDUILock) {
            // NOTE: 
            // defensive copy of the Date object is necessary 
            // because CLDC's Calendar returns a reference to an internal, 
            // shared Date object.  See bugID: 4479408.

// 	       original:
//             return (initialized ? 
//                 new java.util.Date(currentDate.getTime().getTime()) : null);

	    if (initialized) {
		java.util.Date retDate = dateFieldLF.lGetDate();
		if (retDate == null) {
		    return new java.util.Date(currentDate.getTime().getTime());
		} else {
		    return retDate;
		}
	    } 

	    return null;

        } // synchronized
    }

    /**
     * Sets a new value for this field. <code>null</code> can be
     * passed to set the field
     * state to &quot;not initialized&quot; state. The input mode of
     * this field defines
     * what components of passed <code>Date</code> object is used.<p>
     *
     * In <code>TIME</code> input mode the date components must be set
     * to the &quot;zero
     * epoch&quot; value of January 1, 1970. If a date object that presents 
     * time
     * beyond one day then this field is in &quot;not initialized&quot; state.
     * In <code>TIME</code> input mode the date component of
     * <code>Date</code> object is ignored and time
     * component is used to precision of minutes.<p>
     *
     * In <code>DATE</code> input mode the time component of
     * <code>Date</code> object is ignored.<p>
     *
     * In <code>DATE_TIME</code> input mode the date and time
     * component of <code>Date</code> are used but
     * only to precision of minutes.
     *
     * @param date new value for this field
     * @see #getDate
     */
    public void setDate(java.util.Date date) {
        synchronized (Display.LCDUILock) {
            setDateImpl(date);
            dateFieldLF.lSetDate(date);
        } // synchronized
    }

    /**
     * Gets input mode for this date field. Valid input modes are
     * <code>DATE</code>, <code>TIME</code> and <code>DATE_TIME</code>.
     *
     * @return input mode of this field
     * @see #setInputMode
     */
    public int getInputMode() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return mode;
    }

    /**
     * Set input mode for this date field. Valid input modes are
     * <code>DATE</code>, <code>TIME</code> and <code>DATE_TIME</code>.
     *
     * @param mode the input mode, must be one of <code>DATE</code>,
     * <code>TIME</code> or <code>DATE_TIME</code>
     * @throws IllegalArgumentException if an invalid value is specified
     * @see #getInputMode
     */
    public void setInputMode(int mode) {

        if ((mode != DATE) && (mode != TIME) && (mode != DATE_TIME)) {
            throw new IllegalArgumentException("Invalid input mode");
        }

        synchronized (Display.LCDUILock) {
            if (this.mode != mode) {
                this.mode = mode;

                // While the input mode is changed
                // some irrelevant values for new mode could be lost. 
                // Currently that is allowed by the spec.

                // So for TIME mode we make sure that time is set
                // on a zero epoch date
                // and for DATE mode we zero out hours and minutes
                if (mode == TIME) {
                    currentDate.set(Calendar.YEAR, 1970);
                    currentDate.set(Calendar.MONTH, Calendar.JANUARY);
                    currentDate.set(Calendar.DATE, 1);
                } else if (mode == DATE) {
                    currentDate.set(Calendar.HOUR, 0);
		    currentDate.set(Calendar.HOUR_OF_DAY, 0);
                    currentDate.set(Calendar.MINUTE, 0);
                }
                dateFieldLF.lSetInputMode(mode);
            }
        } // synchronized
    }

    // package private
    /**
     * Sets the date.
     * @param date the date value to set to.
     */
    void setDateImpl(java.util.Date date) {

        if (date == null) {
            initialized = false;
        } else {
            currentDate.setTime(date);
            
            if (mode == TIME) {

                if (currentDate.getTime().getTime() >= 24*60*60*1000) {
                    initialized = false;
                } else {
                    currentDate.set(Calendar.YEAR, 1970);
                    currentDate.set(Calendar.MONTH, Calendar.JANUARY);
                    currentDate.set(Calendar.DATE, 1);
                    initialized = true;
                }
            } else {
                // Currently spec does not prohibit from losing
                // irrelevant for that mode information
                // so we always zero out hours and minutes
                
                // NOTE: the specification doesn't prohibit 
                // the loss of information irrelevant to 
                // the current input mode, so we always zero out the
                // hours and minutes.
                if (mode == DATE) {
                    currentDate.set(Calendar.HOUR, 0);
		            currentDate.set(Calendar.HOUR_OF_DAY, 0);
                    currentDate.set(Calendar.MINUTE, 0);
                }
                initialized = true;
            }

            // always ignore seconds and milliseconds
            currentDate.set(Calendar.SECOND, 0);
            currentDate.set(Calendar.MILLISECOND, 0);
        }
    }

    /**
     * Return whether the Item takes user input focus.
     *
     * @return Always return <code>true</code>
     */
    boolean acceptFocus() {
	return true;
    }

    /**
     * The look&feel associated with this DateField. 
     * Set in the constructor.
     */
    DateFieldLF dateFieldLF; // = null
    
    /**
     * A flag indicating the initialization state of this DateField
     */
    boolean initialized; // = false;

    /**
     * The mode of this DateField
     */
    int mode;

    /**
     * The last saved date.
     * This is used for making the last saved date bold.
     */
    Calendar currentDate;

}
