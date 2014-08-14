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

package com.sun.j2me.pim;

import com.sun.j2me.pim.formats.VCalendar10Format;
import java.util.Date;
import java.util.Enumeration;
import javax.microedition.pim.Event;
import javax.microedition.pim.FieldEmptyException;
import javax.microedition.pim.PIM;
import javax.microedition.pim.RepeatRule;

/**
 * Implementation of a PIM Event.
 *
 */
public class EventImpl extends AbstractPIMItem implements Event {
    /** Rule for repeating events. */
    private RepeatRule repeatRule = null;

    /**
     * Constructs an event implementation handler.
     * @param list handler for event list
     */
    public EventImpl(AbstractPIMList list) {
        super(list, PIM.EVENT_LIST);
        if (list != null && !(list instanceof EventListImpl)) {
            throw new RuntimeException("Wrong list passed");
        }
    }

    /**
     * Constructs an event list from a base entry.
     * @param list event list handler
     * @param base event template
     */
    EventImpl(AbstractPIMList list, Event base) {
        super(list, base);
        if (!(list instanceof EventListImpl)) {
            throw new RuntimeException("Wrong list passed");
        }
        this.repeatRule = base.getRepeat();
    }

    /**
     * Gets the repeat rule, if any.
     * @return repeat rule, which could be <code>null</code>
     * @see #setRepeat
     */
    public RepeatRule getRepeat() {
        if (repeatRule == null) {
            return null;
        }
        // clone the repeat rule
        int[] fields = repeatRule.getFields();
        RepeatRule newRule = new RepeatRule();
        for (int i = 0; i < fields.length; i++) {
            int field = fields[i];
            switch (field) {
                case RepeatRule.COUNT:
                case RepeatRule.DAY_IN_MONTH:
                case RepeatRule.DAY_IN_WEEK:
                case RepeatRule.DAY_IN_YEAR:
                case RepeatRule.FREQUENCY:
                case RepeatRule.INTERVAL:
                case RepeatRule.MONTH_IN_YEAR:
                case RepeatRule.WEEK_IN_MONTH:
                    newRule.setInt(field, repeatRule.getInt(field));
                    break;
                case RepeatRule.END:
                    newRule.setDate(field, repeatRule.getDate(field));
                    break;
            }
        }
        Enumeration dates = repeatRule.getExceptDates();
        while (dates.hasMoreElements()) {
            Date date = (Date) dates.nextElement();
            newRule.addExceptDate(date.getTime());
        }
        return newRule;
    }

    /**
     * Updates the repeat rule.
     * @param value the new repeat rule
     * @see #getRepeat
     */
    public void setRepeat(RepeatRule value) {
        this.repeatRule = value;
        setModified(true);
    }

    /**
     * Gets the encoding format handler.
     * @return the PIM format handler
     */
    PIMFormat getEncodingFormat() {
        return new VCalendar10Format();
    }

    /**
     * Ensures the PIM field identifier is supported.
     * @param field the property identifier
     * @return <code>true</code> if the field is supported
     */
    static boolean isValidPIMField(int field) {
        switch (field) {
            case Event.ALARM:
            case Event.CLASS:
            case Event.END:
            case Event.LOCATION:
            case Event.NOTE:
            case Event.REVISION:
            case Event.START:
            case Event.SUMMARY:
            case Event.UID:
                return true;
            default:
                return false;
        }
    }

    /**
     * Gets the revision field identifier.
     * @return revision field identifier
     */
    protected int getRevisionField() {
        return REVISION;
    }

    /**
     * Gets the UID field identifier.
     * @return UID field identifier
     */
    protected int getUIDField() {
        return UID;
    }

    /**
     * Sets default values for this item.
     */
    protected void setDefaultValues() {
        super.setDefaultValues();
        if (repeatRule != null) {
            try {
                repeatRule.getInt(RepeatRule.FREQUENCY);
            } catch (FieldEmptyException e) {
                repeatRule.setInt(RepeatRule.FREQUENCY, RepeatRule.DAILY);
            }
            try {
                repeatRule.getInt(RepeatRule.INTERVAL);
            } catch (FieldEmptyException e) {
                repeatRule.setInt(RepeatRule.INTERVAL, 1);
            }
        }
    }

    /**
     * Converts the Event record to a printable format.
     * @return formatted Event record
     */
    protected String toDisplayableString() {
        StringBuffer sb = new StringBuffer("Event[");
        String data = formatData();
        sb.append(data);
        RepeatRule rule = getRepeat();
        if (rule != null) {
            if (data.length() > 0) {
                sb.append(", ");
            }
            sb.append("Rule=[");
            int[] fields = rule.getFields();
            for (int i = 0; i < fields.length; i++) {
                if (i != 0) {
                    sb.append(",");
                }
                int field = fields[i];
                switch (field) {
                    case RepeatRule.FREQUENCY:
                        sb.append("Frequency=");
                        switch (rule.getInt(field)) {
                            case RepeatRule.DAILY:
                                sb.append("Daily");
                                break;
                            case RepeatRule.WEEKLY:
                                sb.append("Weekly");
                                break;
                            case RepeatRule.MONTHLY:
                                sb.append("Monthly");
                                break;
                            case RepeatRule.YEARLY:
                                sb.append("Yearly");
                                break;
                            default:
                                sb.append("<Unknown: " +
                                    rule.getInt(field) + ">");
                        }
                        break;
                    case RepeatRule.END:
                        sb.append("End=" +
                            PIMHandler.getInstance()
                                .composeDateTime(rule.getDate(field)));
                        break;
                    case RepeatRule.COUNT:
                        sb.append("Count=" + rule.getInt(field));
                        break;
                    case RepeatRule.INTERVAL:
                        sb.append("Interval=" + rule.getInt(field));
                        break;
                    case RepeatRule.DAY_IN_WEEK:
                        sb.append("DayInWeek=0x" + Integer
                            .toHexString(rule.getInt(field)));
                        break;
                    case RepeatRule.DAY_IN_MONTH:
                        sb.append("DayInMonth=" + rule.getInt(field));
                        break;
                    case RepeatRule.DAY_IN_YEAR:
                        sb.append("DayInYear=" + rule.getInt(field));
                        break;
                    case RepeatRule.WEEK_IN_MONTH:
                        sb.append("WeekInMonth=0x" + Integer
                            .toHexString(rule.getInt(field)));
                        break;
                    case RepeatRule.MONTH_IN_YEAR:
                        sb.append("MonthInYear=0x" + Integer
                            .toHexString(rule.getInt(field)));
                        break;
                    default:
                        sb.append("<Unknown: " + field + "="
                            + rule.getInt(field));

                }
            }
            sb.append("]");
        }
        sb.append("]");
        return sb.toString();
    }

}
