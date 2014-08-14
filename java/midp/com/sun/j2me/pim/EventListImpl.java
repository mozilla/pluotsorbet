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

package com.sun.j2me.pim;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.pim.Event;
import javax.microedition.pim.EventList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.RepeatRule;
import com.sun.j2me.security.PIMPermission;

/**
 * Class EventListImpl implements methods of PIM interface EventList.
 *
 */
class EventListImpl extends AbstractPIMList implements EventList {
    /**
     * Constructs an Event list.
     * @param name label for the Event List
     * @param mode read or write access
     * @param handle handle of the list
     */
    EventListImpl(String name, int mode, Object handle) {
        super(PIM.EVENT_LIST, name, mode, handle);
    }

    /**
     * Creates a new event instance.
     * @return Event handler
     */
    public Event createEvent() {
        return new EventImpl(this);
    }

    /**
     * Gets supported repeat rule fields.
     * @param frequency repeat rule frequency
     * @return array of repeat rule fields
     */
    public int[] getSupportedRepeatRuleFields(int frequency) {
        switch (frequency) {
            case RepeatRule.DAILY:
                return new int[] {
                    RepeatRule.COUNT,
                    RepeatRule.INTERVAL,
                    RepeatRule.END
                };
            case RepeatRule.WEEKLY:
                return new int[] {
                    RepeatRule.COUNT,
                    RepeatRule.INTERVAL,
                    RepeatRule.END,
                    RepeatRule.DAY_IN_WEEK
                };
            case RepeatRule.MONTHLY:
                return new int[] {
                    RepeatRule.COUNT,
                    RepeatRule.INTERVAL,
                    RepeatRule.END,
                    RepeatRule.DAY_IN_WEEK,
                    RepeatRule.WEEK_IN_MONTH,
                    RepeatRule.DAY_IN_MONTH
                };
            case RepeatRule.YEARLY:
                return new int[] {
                    RepeatRule.COUNT,
                    RepeatRule.INTERVAL,
                    RepeatRule.END,
                    RepeatRule.DAY_IN_WEEK,
                    RepeatRule.WEEK_IN_MONTH,
                    RepeatRule.DAY_IN_MONTH,
                    RepeatRule.MONTH_IN_YEAR,
                    RepeatRule.DAY_IN_YEAR
                };
            default:
                throw new IllegalArgumentException("Unsupported frequency: "
                    + frequency);
        }
    }

    /**
     * Creates new event from item template.
     * @param item template with initial data
     * @return Event implementation handler
     */
    public Event importEvent(Event item) {
        return new EventImpl(this, item);
    }

    /**
     * Gets enumeration of Event items.
     * @param searchType search rule
     * @param startDate beginning of range
     * @param endDate end of range
     * @param initialEventOnly first event only
     * @return Enumeration of matching events
     */
    public Enumeration items(int searchType,
        long startDate,
        long endDate,
        boolean initialEventOnly) throws PIMException {

        switch (searchType) {
            case ENDING:
            case OCCURRING:
            case STARTING:
                break;
            default:
                throw new IllegalArgumentException(
                    "Invalid search type: " + searchType);
        }
        if (startDate > endDate) {
            throw new IllegalArgumentException(
                "Start date must be earlier than end date");
        }
        Vector selectedItems = new Vector();
        Vector itemKeys = new Vector();
        for (Enumeration e = items(); e.hasMoreElements(); ) {
            Event event = (Event) e.nextElement();
            long eventStart = 0l;
            long eventEnd = 0l;
            // a START or END field may have at most one value
            if (event.countValues(Event.START) != 0) {
                eventStart = event.getDate(Event.START, 0);
                if (event.countValues(Event.END) != 0) {
                    eventEnd = event.getDate(Event.END, 0);
                } else {
                    // see specification of Event.END field: if
                    // END is not specified but START is, the event only
                    // occurs at the START time.
                    eventEnd = eventStart;
                }
            } else if (event.countValues(Event.END) != 0) {
                // see specification of Event.START field: if
                // START is not specified but END is, the event only
                // occurs at the END time.
                eventEnd = event.getDate(Event.END, 0);
                eventStart = eventEnd;
            } else {
                // no start or end date
                continue;
            }
            long duration = Math.max(0, eventEnd - eventStart);
            RepeatRule repeatRule = event.getRepeat();
            boolean includeItem = false;
            if (repeatRule != null) {
                // check all occurrences of the event
                long timeSlot = eventEnd - eventStart;
                Enumeration dates =
                    repeatRule.dates(eventStart,
                        Math.max(startDate - duration, 0), endDate);
                while (dates.hasMoreElements()) {
                    Date date = (Date) dates.nextElement();
                    eventStart = date.getTime();
                    eventEnd = eventStart + timeSlot;
                    if (eventStart > endDate) {
                        // no point continuing
                        break;
                    }
                    includeItem =
                        checkRange(searchType,
                            startDate, endDate,
                            eventStart, eventEnd);
                    if (includeItem) {
                        break;
                    }
                    if (initialEventOnly) {
                        break;
                    }
                }
            } else {
                // check the base occurrence
                includeItem =
                    checkRange(searchType,
                        startDate, endDate,
                        eventStart, eventEnd);
            }
            if (includeItem) {
                KeySortUtility.store(itemKeys, selectedItems,
				     eventStart, event);
            }
        }
        return selectedItems.elements();
    }

    /**
     * Verifies search range.
     * @param searchType search mode
     * @param startDate beginning of range
     * @param endDate end of range
     * @param eventStart event start date
     * @param eventEnd event end date
     * @return <code>true</code> if event is within range
     */
    private boolean checkRange(int searchType,
        long startDate, long endDate,
        long eventStart, long eventEnd) {
        switch (searchType) {
            case EventList.STARTING:
                return (eventStart >= startDate && eventStart <= endDate);
            case EventList.ENDING:
                return (eventEnd >= startDate && eventEnd <= endDate);
            case EventList.OCCURRING:
                return (eventStart <= endDate && eventEnd >= startDate);
            default:
                return false;
        }
    }

    /**
     * Removes event.
     * @param item to remove
     * @throws PIMException if item not found
     */
    public void removeEvent(Event item) throws PIMException {
        removeItem(item);
    }

    /**
     * Verifies read permission.
     *
     * @param action description of the operation
     * @throws SecurityException if operation is not permitted
     */
    protected void checkReadPermission(String action)
        throws SecurityException {
        checkReadPermission();
        checkPermission(PIMPermission.EVENT_READ, action);
    }

    /**
     * Verifies write permission.
     *
     * @param action description of the operation
     * @throws SecurityException if operation is not permitted
     */
    protected void checkWritePermission(String action)
        throws SecurityException {
        checkWritePermission();
        checkPermission(PIMPermission.EVENT_WRITE, action);
    }
}
