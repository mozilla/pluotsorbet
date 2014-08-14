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

package com.sun.j2me.pim.formats;

import com.sun.j2me.pim.AbstractPIMItem;
import com.sun.j2me.pim.AbstractPIMList;
import com.sun.j2me.pim.EventImpl;
import com.sun.j2me.pim.LineReader;
import com.sun.j2me.pim.PIMFormat;
import com.sun.j2me.pim.PIMHandler;
import com.sun.j2me.pim.ToDoImpl;
import com.sun.j2me.pim.UnsupportedPIMFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.pim.*;
import com.sun.j2me.jsr75.StringUtil;

/**
 * Implementation of PIMEncoding for VCalendar/1.0.
 *
 */
public class VCalendar10Format extends EndMatcher implements PIMFormat {
    
    /** List of the weekday masks from RepeatRule */
    private static final int[] DAYS_OF_WEEK = {
        RepeatRule.SUNDAY, RepeatRule.MONDAY, RepeatRule.TUESDAY,
        RepeatRule.WEDNESDAY, RepeatRule.THURSDAY, RepeatRule.FRIDAY,
        RepeatRule.SATURDAY
    };
    
    /**
     * List of vCalendar weekday codes. This sequence is parallel to
     * DAYS_OF_WEEK.
     */
    private static final String[] DAYS_OF_WEEK_CODES = {
        "SU", "MO", "TU", "WE", "TH", "FR", "SA"
    };
    
    /** List of the week in month masks from RepeatRule */
    private static final int[] WEEKS_OF_MONTH = {
        RepeatRule.FIRST, RepeatRule.SECOND, RepeatRule.THIRD,
        RepeatRule.FOURTH, RepeatRule.FIFTH,
        RepeatRule.LAST, RepeatRule.SECONDLAST, RepeatRule.THIRDLAST,
        RepeatRule.FOURTHLAST, RepeatRule.FIFTHLAST
    };
    
    /**
     * List of vCalendar week of month codes. This sequence is
     * parallel to WEEKS_OF_MONTH.
     */
    private static final String[] WEEKS_OF_MONTH_CODES = {
        "1+", "2+", "3+", "4+", "5+",
        "1-", "2-", "3-", "4-", "5-",
    };
    
    /** List of month in year masks from RepeatRule */
    private static final int[] MONTHS_IN_YEAR = {
        0,
        RepeatRule.JANUARY, RepeatRule.FEBRUARY, RepeatRule.MARCH,
        RepeatRule.APRIL, RepeatRule.MAY, RepeatRule.JUNE,
        RepeatRule.JULY, RepeatRule.AUGUST, RepeatRule.SEPTEMBER,
        RepeatRule.OCTOBER, RepeatRule.NOVEMBER, RepeatRule.DECEMBER
    };

    /**
     * VCalendar 1.0 formatter.
     */
    public VCalendar10Format() {
        super("VCALENDAR");
    }
    
    /**
     * Gets the code name of this encoding (e.g. "VCARD/2.1").
     * @return the encoding name
     */
    public String getName() {
        return "VCALENDAR/1.0";
    }
    
    /**
     * Checks to see if a given PIM list type is supported by this encoding.
     * @param pimListType int representing the PIM list type to check
     * @return true if the type can be read and written by this encoding,
     * false otherwise
     */
    public boolean isTypeSupported(int pimListType) {
        switch (pimListType) {
        case PIM.EVENT_LIST:
        case PIM.TODO_LIST:
            return FormatSupport.isListTypeSupported(pimListType);
        }
        return false;
    }
    
    /**
     * Serializes a PIMItem.
     * @param out Stream to which serialized data is written
     * @param encoding Character encoding to use for serialized data
     * @param pimItem The item to write to the stream
     * @throws IOException if a write error occurs
     */
    public void encode(OutputStream out, String encoding, PIMItem pimItem)
        throws IOException {
            
        Writer w = new OutputStreamWriter(out, encoding);
        w.write("BEGIN:VCALENDAR\r\n");
        w.write("VERSION:1.0\r\n");
        if (pimItem instanceof Event) {
            encode(w, (Event) pimItem);
        } else if (pimItem instanceof ToDo) {
            encode(w, (ToDo) pimItem);
        }
        w.write("END:VCALENDAR\r\n");
        w.flush();
    }
    
    /**
     * Serializes a vEvent.
     * @param w output stream 
     * @param event the event to be encoded
     * @throws IOException if an error occurs while encoding 
     */
    private void encode(Writer w, Event event) throws IOException {
        w.write("BEGIN:VEVENT\r\n");
        // write known fields
        int[] fields = event.getFields();
        for (int i = 0; i < fields.length; i++) {
            int valueCount = event.countValues(fields[i]);
            for (int j = 0; j < valueCount; j++) {
                writeValue(w, event, fields[i], j);
            }
        }
        // write categories
        String categories = StringUtil.join(event.getCategories(), ",");
        if (categories.length() > 0) {
            w.write("CATEGORIES:");
            w.write(categories);
            w.write("\r\n");
        }
        // write repeat rule
        RepeatRule rule = event.getRepeat();
        if (rule != null) {
            String s = encodeRepeatRule(rule, 0);
            if (s != null) {
                w.write("RRULE:");
                w.write(s);
                w.write("\r\n");
            }
            Enumeration exDates = rule.getExceptDates();
            if (exDates.hasMoreElements()) {
               w.write("EXDATE;VALUE=DATE:");
               while (exDates.hasMoreElements()) {
                   long time = ((Date) exDates.nextElement()).getTime();
                   w.write(PIMHandler.getInstance().composeDate1(time));
                   if (exDates.hasMoreElements()) {
                       w.write(",");
                   }
               }
               w.write("\r\n");
            }
        }
        w.write("END:VEVENT\r\n");
    }
    
    /**
     * Serializes a vToDo.
     * @param w output stream target
     * @param todo item to be encoded
     * @throws IOException if an error occurs while encoding
     */
    private void encode(Writer w, ToDo todo) throws IOException {
        w.write("BEGIN:VTODO\r\n");
        // write known fields
        int[] fields = todo.getFields();
        for (int i = 0; i < fields.length; i++) {
            int valueCount = todo.countValues(fields[i]);
            for (int j = 0; j < valueCount; j++) {
                writeValue(w, todo, fields[i], j);
            }
        }
        // write categories
        String categories = StringUtil.join(todo.getCategories(), ",");
        if (categories.length() > 0) {
            w.write("CATEGORIES:");
            w.write(categories);
            w.write("\r\n");
        }
        w.write("END:VTODO\r\n");
    }
    
    /**
     * Serializes one line of a vEvent.
     * @param w output stream target
     * @param event element to be processed
     * @param field component of element to output
     * @param index offset in field to process
     * @throws IOException if an error occurs while writing
     */
    private void writeValue(Writer w, Event event, int field, int index)
        throws IOException {
        
        switch (field) {
            case Event.CLASS: {
                int iValue = event.getInt(field, index);
                String sValue = VEventSupport.getClassType(iValue);
                if (sValue != null) {
                    w.write("CLASS:");
                    w.write(sValue);
                    w.write("\r\n");
                }
                break;
            }
            case Event.ALARM: {
                int iValue = event.getInt(field, index);
                // subtract Event.ALARM from Event.START
                try {
                    long startTime = event.getDate(Event.START, 0);
                    w.write("DALARM:");
                    w.write(PIMHandler.getInstance()
                        .composeDateTime(startTime - iValue * 1000));
                    w.write("\r\n");
                } catch (IOException e) {
                    // don't write a DALARM field
                }
                break;
            }
            case Event.LOCATION:
            case Event.NOTE:
            case Event.SUMMARY:
            case Event.UID: {
                String sValue = event.getString(field, index);
                if (sValue != null) {
                    String property = VEventSupport.getFieldLabel(field);
                    w.write(property);
                    w.write(":");
                    w.write(sValue);
                    w.write("\r\n");
                }
                break;
            }
            case Event.END:
            case Event.REVISION:
            case Event.START: {
                long date = event.getDate(field, index);
                w.write(VEventSupport.getFieldLabel(field));
                w.write(":");
                w.write(PIMHandler.getInstance().composeDateTime(date));
                w.write("\r\n");
                break;
            }
        }
        
    }
    
    /** 
     * Serializes one line of a vToDo.
     * @param w output stream target
     * @param todo element to be processed
     * @param field component of element to output
     * @param index offset in field to process
     * @throws IOException if an error occurs while writing
     *
     */
    private void writeValue(Writer w, ToDo todo, int field, int index)
        throws IOException {
            
        switch (field) {
            case ToDo.CLASS: {
                int iValue = todo.getInt(field, index);
                String sValue = VToDoSupport.getClassType(iValue);
                if (sValue != null) {
                    w.write("CLASS:");
                    w.write(sValue);
                    w.write("\r\n");
                }
                break;
            }
            case ToDo.NOTE:
            case ToDo.SUMMARY:
            case ToDo.UID: {
                String sValue = todo.getString(field, index);
                if (sValue != null) {
                    String property = VToDoSupport.getFieldLabel(field);
                    w.write(property);
                    w.write(":");
                    w.write(sValue);
                    w.write("\r\n");
                }
                break;
            }
            case ToDo.DUE:
            case ToDo.COMPLETION_DATE:
            case ToDo.REVISION: {
                long date = todo.getDate(field, index);
                w.write(VToDoSupport.getFieldLabel(field));
                w.write(":");
                w.write(PIMHandler.getInstance().composeDateTime(date));
                w.write("\r\n");
                break;
            }
            case ToDo.COMPLETED: {
                w.write("STATUS:COMPLETED\r\n");
                break;
            }
            case ToDo.PRIORITY: {
                w.write(VToDoSupport.getFieldLabel(field));
                w.write(":");
                w.write(String.valueOf(todo.getInt(field, index)));
                w.write("\r\n");
                break;
            }
        }
        
    }
    
    /**
     * Returns a VCalendar representation of a repeating rule, or
     * null if the rule cannot be encoded.
     *
     * For more details please see The Electronic Calendaring and Scheduling
     * Exchange Format Version 1.0
     *
     * @param rule data to be encoded
     * @param startFreq start frequency value (0 on start)
     * @return encoded rule
     */
    private String encodeRepeatRule(RepeatRule rule, int startFreq) {
        StringBuffer sb = new StringBuffer();
        int[] fields = rule.getFields();
        FormatSupport.sort(fields);
        if (!FormatSupport.contains(fields, RepeatRule.FREQUENCY)) {
            return null;
        }
        int frequency;
        if (startFreq != 0) {
            frequency = startFreq;
        } else {
            frequency = rule.getInt(RepeatRule.FREQUENCY);
        }
        int interval = 1; // default value according to JSR75 spec
        if (FormatSupport.contains(fields, RepeatRule.INTERVAL) &&
            (startFreq == 0)) {
            interval = rule.getInt(RepeatRule.INTERVAL);
        }
        String encodedCount = " #0"; // forever
        if (FormatSupport.contains(fields, RepeatRule.COUNT) &&
            (startFreq == 0)) {
            encodedCount = " #" + rule.getInt(RepeatRule.COUNT);
        }
        // enddate - ISO 8601 clause 5.4.1
        String encodedEndDate = "";
        if (FormatSupport.contains(fields, RepeatRule.END)) {
            encodedEndDate = " " + PIMHandler.getInstance().composeDateTime(
                rule.getDate(RepeatRule.END));
        }
        switch (frequency) {
            case RepeatRule.DAILY: {
                // D<interval> [<duration>]
                sb.append(FormatSupport.DAILY);
                sb.append(interval);
                sb.append(encodedCount);
                break;
            }
            case RepeatRule.WEEKLY: {
                // W<interval> <weekday> [<duration>]
                sb.append(FormatSupport.WEEKLY);
                sb.append(interval);
                if (FormatSupport.contains(fields, RepeatRule.DAY_IN_WEEK)) {
                    sb.append(encodeRepeatRuleDaysInWeek(rule));
                }
                sb.append(encodedCount);
                break;
            }
            case RepeatRule.MONTHLY: {
                sb.append(FormatSupport.MONTHLY);
                if (FormatSupport.contains(fields, RepeatRule.DAY_IN_MONTH)) {
                    // MD<interval> <daynumber> [<duration>]
                    sb.append(FormatSupport.DAY_IN_MONTH);
                    sb.append(interval);
                    sb.append(" ");
                    sb.append(rule.getInt(RepeatRule.DAY_IN_MONTH));
                    sb.append(encodedCount);
                } else if (FormatSupport.contains(fields, 
						  RepeatRule.WEEK_IN_MONTH)) {
                    // MP<interval> {<1>|<2>}{<+>|<->} [<duration>] [weekly|daily]
                    sb.append(FormatSupport.WEEK_IN_MONTH);
                    sb.append(interval);
                    sb.append(encodeRepeatRuleWeeksInMonth(fields, rule));
                    sb.append(encodedCount);
                    if (FormatSupport.contains(fields, RepeatRule.DAY_IN_WEEK)) {
                        sb.append(" " + encodeRepeatRule(rule, RepeatRule.WEEKLY));
                    }
                }
                break;
            }
            case RepeatRule.YEARLY: {
                sb.append(FormatSupport.YEARLY);
                if (FormatSupport.contains(fields, RepeatRule.DAY_IN_YEAR)) {
                    sb.append(FormatSupport.DAY_IN_YEAR);
                    sb.append(interval);
                    sb.append(" ");
                    sb.append(rule.getInt(RepeatRule.DAY_IN_YEAR));
                    sb.append(encodedCount);
                } else if (FormatSupport.contains(fields,
						  RepeatRule.MONTH_IN_YEAR)) {
                    sb.append(FormatSupport.MONTH_IN_YEAR);
                    sb.append(interval);
                    sb.append(encodeRepeatRuleMonthsInYear(fields, rule));
                    sb.append(encodedCount);
                    if (FormatSupport.contains(fields, RepeatRule.DAY_IN_MONTH) ||
                        FormatSupport.contains(fields, RepeatRule.WEEK_IN_MONTH)) {
                        sb.append(" " + 
                            encodeRepeatRule(rule, RepeatRule.MONTHLY)); 
                    }
                }
                break;
            }
            default: return null;
        }
        if (startFreq == 0) {
            sb.append(encodedEndDate);
        }
        return sb.toString();
    }
    
    /** 
     * Returns a string representation of a weekly rule.
     * @param rule data to be encoded
     * @return encoded rule
     */
    private String encodeRepeatRuleDaysInWeek(RepeatRule rule) {
        StringBuffer sb = new StringBuffer();
        int daysInWeek = rule.getInt(RepeatRule.DAY_IN_WEEK);
        for (int i = 0; i < DAYS_OF_WEEK.length; i++) {
            if ((daysInWeek & DAYS_OF_WEEK[i]) != 0) {
                sb.append(" ");
                sb.append(DAYS_OF_WEEK_CODES[i]);
            }
        }
        return sb.toString();
    }
    
    /**
     * Returns a string representation of a monthly rule with a weekly
     * parameter.
     * @param fields data to be processed
     * @param rule to encode
     * @return encoded rule
     */
    private String encodeRepeatRuleWeeksInMonth(int[] fields, RepeatRule rule) {
        StringBuffer sb = new StringBuffer();
        int weeksInMonth = rule.getInt(RepeatRule.WEEK_IN_MONTH);
        for (int i = 0; i < WEEKS_OF_MONTH.length; i++) {
            if ((weeksInMonth & WEEKS_OF_MONTH[i]) != 0) {
                sb.append(" ");
                sb.append(WEEKS_OF_MONTH_CODES[i]);
            }
        }
        return sb.toString();
    }

    /**
     * Returns a string representation of a yearly rule with a monthly
     * parameter.
     * @param fields data to be processed
     * @param rule to encode
     * @return encoded rule
     */
    private String encodeRepeatRuleMonthsInYear(int[] fields, RepeatRule rule) {
        StringBuffer sb = new StringBuffer();
        int monthsInYear = rule.getInt(RepeatRule.MONTH_IN_YEAR);
        for (int i = 0; i < MONTHS_IN_YEAR.length; i++) {
            if ((monthsInYear & MONTHS_IN_YEAR[i]) != 0) {
                sb.append(" ");
                sb.append(i);
            }
        }
        return sb.toString();
    }
    
    /**
     * Constructs one or more PIMItems from serialized data.
     * @param in Stream containing serialized data
     * @param encoding Character encoding of the stream
     * @param list PIMList to which items should be added, or null if the items
     * should not be part of a list
     * @throws UnsupportedPIMFormatException if the serialized
     * data cannot be interpreted by this encoding.
     * @return a non-empty array of PIMItems containing the objects described
     * in the serialized data, or null if no items are available
     * @throws IOException if a read error occurs
     */
    public PIMItem[] decode(InputStream in, String encoding, PIMList list)
        throws IOException {

        LineReader r = new LineReader(in, encoding, this);
        String line = r.readLine();
        if (line == null) {
            return null;
        }

        if (!line.toUpperCase().equals("BEGIN:VCALENDAR")) {
            throw new UnsupportedPIMFormatException("Not a vCalendar object");
        }
        Vector items = new Vector();
        for (AbstractPIMItem item; (item = decode(r, list)) != null; ) {
            items.addElement(item);
        }
        if (items.size() == 0) {
            return null;
        }
        AbstractPIMItem[] a = new AbstractPIMItem[items.size()];
        items.copyInto(a);
        return a;
    }

    /**
     * Constructs a single PIMItem from serialized data.
     * @param in LineReader containing serialized data
     * @param list PIM list to which the item belongs
     * @throws UnsupportedPIMFormatException if the serialized data cannot be
     * interpreted by this encoding.
     * @return an unserialized Event, or null if no data was available
     */
    private AbstractPIMItem decode(LineReader in, PIMList list)
        throws IOException {

        while (true) {
            String line = in.readLine();
            if (line == null) {
                return null;
            }
            FormatSupport.DataElement element =
                FormatSupport.parseObjectLine(line);

            if (element.propertyName.equals("BEGIN")) {
                if (element.data.toUpperCase().equals("VEVENT")) {
                    if (isTypeSupported(PIM.EVENT_LIST)) {
                        return decodeEvent(in, list);
                    }
                    throw new UnsupportedPIMFormatException(
                        "Events list is not supported");
                } else if (element.data.toUpperCase().equals("VTODO")) {
                    if (isTypeSupported(PIM.TODO_LIST)) {
                        return decodeToDo(in, list);
                    }
                    throw new UnsupportedPIMFormatException(
                        "ToDo list is not supported");
                } else {
                    throw new UnsupportedPIMFormatException(
                        "Bad argument to BEGIN: " + element.data);
                }
            } else if (element.propertyName.equals("END")) {
                if (element.data.toUpperCase().equals("VCALENDAR")) {
                    return null;
                } else {
                    throw new UnsupportedPIMFormatException(
                        "Bad argument to END: " + element.data);
                }
            } else if (element.propertyName.equals("PRODID")) {
                // ignore product ID
            } else if (element.propertyName.equals("VERSION")) {
                // check version, then keep reading
                if (!element.data.equals("1.0")) {
                    throw new 
                        UnsupportedPIMFormatException("vCalendar version '" +
                                                      element.data +
                                                      "' is not supported");
                }
            } else if (element.propertyName.equals("CATEGORIES")) {
                // what should I do with this? this seems to be the wrong place
                // to put the field.
            } else {
                throw new UnsupportedPIMFormatException("Unrecognized item: " +
                                                        line);
            }
        }
    }
    
    /** 
     * Reads and decodes a single vEvent.
     * @param in encoded event reader stream
     * @param list PIM list to which the item belongs
     * @return event reader implementation handle
     * @throws IOException if a reading error occurs
     */
    private EventImpl decodeEvent(LineReader in, PIMList list)
        throws IOException {

        EventImpl event = new EventImpl((AbstractPIMList)list);
        String line;
        while ((line = in.readLine()) != null) {
            FormatSupport.DataElement element =
                FormatSupport.parseObjectLine(line);

            if (element.propertyName.equals("END")) {
                // patch DALARM values
                try {
                    int alarmValues = event.countValues(Event.ALARM);
                    if (alarmValues > 0 && event.countValues(Event.START) > 0) {
                        int startTime = (int)
                            (event.getDate(Event.START, 0) / 1000);
                        for (int i = 0, j = 0; i < alarmValues; i++, j++) {
                            int alarmTime = event.getInt(Event.ALARM, i);
                            if (alarmTime * 1000 < startTime) {
                                event.setInt(Event.ALARM, i, Event.ATTR_NONE,
                                    startTime - alarmTime);
                            } else {
                                event.removeValue(Event.ALARM, i);
                                alarmValues --;
                                i --;
                            }
                        }
                    }
                } catch (UnsupportedFieldException ufe) {
                    // Nothing to do if ALARM is not supported
                }
                return event;
            } else if (element.propertyName.equals("VERSION")) {
                if (!element.data.equals("1.0")) {
                    throw new UnsupportedPIMFormatException("Version "
                        + element.data + " is not supported");
                }
            } else if (element.propertyName.equals("CATEGORIES")) {
                String[] categories = StringUtil.split(element.data, ',', 0);
                for (int j = 0; j < categories.length; j++) {
                    try {
                        event.addToCategory(categories[j]);
                    } catch (PIMException e) {
                        // cannot add item
                    }
                }
            } else if (element.propertyName.equals("RRULE")) {
                RepeatRule rule = new RepeatRule();
                if (!decodeRepeatRule(rule, element.data, true)) {
                    throw new IOException(
                        "Empty or invalid RepeatRule data");
                }
                event.setRepeat(rule);
            } else if (element.propertyName.equals("EXDATE")) {
                RepeatRule rule = event.getRepeat();
                if (rule != null) {
                    decodeExDates(rule, element.data);
                    event.setRepeat(rule);
                }
            } else {
                importData(event,
                    element.propertyName, element.attributes, element.data);
            }
        }
        throw new IOException("Unterminated vEvent");
    }

    /**
     * Decodes except dates.
     *
     * For more details please see The Electronic Calendaring and Scheduling
     * Exchange Format Version 1.0
     *
     * @param rule repeat rule instance
     * @param data string contains encoded dates
     * separated by ","
     *
     */
    private void decodeExDates(RepeatRule rule, String data)
            throws IOException {
        Parser parser = new Parser(data);
        long date;
        while (parser.hasNextDate()) {
            int dateLen = parser.getEndDate().length();
            // end date is either date in yyyyMMdd format or
            // date/time in yyyymmddThhmmss(Z).
            date = (dateLen < 15) ?
                PIMHandler.getInstance().parseDate(parser.getEndDate()) :
                PIMHandler.getInstance().parseDateTime(parser.getEndDate());
            parser.setPos(parser.getPos() + parser.getEndDate().length());
            rule.addExceptDate(date);
            if (!parser.hasMoreChars()) {
                break;
            }
            parser.matchSkip(','); // separator
        }
    }

    /**
     * Decodes repeat rule.
     *
     * For more details please see The Electronic Calendaring and Scheduling
     * Exchange Format Version 1.0
     *
     * @param rule repeat rule instance
     * @param data string contains encoded repeat rule
     * @param isTop true on top recursive level else false
     * @return true if repeat rule was successfully decoded, false otherwise
     */
    private boolean decodeRepeatRule(RepeatRule rule, String data,
                                     boolean isTop) {
        boolean res = true;
        Parser parser = new Parser(data);

        char sym;
        try {
            parser.skipBlank();
            sym = parser.readChar();
            int interval;
            switch (sym) {
                case FormatSupport.DAILY:
                    // D<interval> [<duration>]
                    interval  = parser.readInt();
                    if (isTop) {
                       rule.setInt(RepeatRule.FREQUENCY, RepeatRule.DAILY);
                       rule.setInt(RepeatRule.INTERVAL, interval);
                    }
                    setRepeatRuleCount(parser, rule, isTop);
                    break;
                case FormatSupport.WEEKLY:
                    // W<interval> <weekday> [<duration>]
                    interval  = parser.readInt();
                    if (isTop) {
                        rule.setInt(RepeatRule.FREQUENCY, RepeatRule.WEEKLY);
                        rule.setInt(RepeatRule.INTERVAL, interval);
                    }
                    rule.setInt(RepeatRule.DAY_IN_WEEK, decodeDaysInWeek(parser));
                    setRepeatRuleCount(parser, rule, isTop);
                    break;
                case FormatSupport.MONTHLY:
                    if (isTop) {
                        rule.setInt(RepeatRule.FREQUENCY, RepeatRule.MONTHLY);
                    }
                    sym = parser.readChar();
                    switch (sym) {
                        case FormatSupport.DAY_IN_MONTH:
                            // MD<interval> <daynumber> [<duration>]
                            interval  = parser.readInt();
                            if (isTop) {
                                rule.setInt(RepeatRule.INTERVAL, interval);
                            }
                            parser.skipBlank();
                            rule.setInt(RepeatRule.DAY_IN_MONTH,
                                parser.readInt());
                            setRepeatRuleCount(parser, rule, isTop);
                            break;
                        case FormatSupport.WEEK_IN_MONTH:
                            interval  = parser.readInt();
                            if (isTop) {
                                rule.setInt(RepeatRule.INTERVAL, interval);
                            }
                            parser.skipBlank();
                            rule.setInt(RepeatRule.WEEK_IN_MONTH, 
                                decodeWeeksInMonth(parser, rule));
                            setRepeatRuleCount(parser, rule, isTop);
                            parser.skipBlank();
                            if (parser.hasMoreChars()) { // parse next rule
                                res = decodeRepeatRule(rule,
                                    parser.getRemainder(), false);
                            }
                            break;
                        default:
                            res = false;
                    }
                    break;
                case FormatSupport.YEARLY:
                    if (isTop) {
                        rule.setInt(RepeatRule.FREQUENCY, RepeatRule.YEARLY);
                    }
                    sym = parser.readChar();
                    switch (sym) {
                        case FormatSupport.DAY_IN_YEAR:
                            interval  = parser.readInt();
                            if (isTop) {
                                rule.setInt(RepeatRule.INTERVAL, interval);
                            }
                            parser.skipBlank();
                            rule.setInt(RepeatRule.DAY_IN_YEAR, parser.readInt());
                            setRepeatRuleCount(parser, rule, isTop);
                            break;
                        case FormatSupport.MONTH_IN_YEAR:
                            interval  = parser.readInt();
                            if (isTop) {
                                rule.setInt(RepeatRule.INTERVAL, interval);
                            }
                            rule.setInt(RepeatRule.MONTH_IN_YEAR,
                                decodeMonthsInYear(parser, rule));
                            setRepeatRuleCount(parser, rule, isTop);
                            parser.skipBlank();
                            if (parser.hasMoreChars()) { // parse next rule
                                res = decodeRepeatRule(rule,
                                        parser.getRemainder(), false);
                            }
                            break;
                        default:
                            res = false;
                    }
            }
        } catch (IOException ex) {
            res = false;
        } catch (NumberFormatException ex) {
            res = false;
        } catch (IllegalArgumentException ex) {
            res = false;
        } catch (FieldEmptyException ex) {
            res = false;
        }

        return res;
    }

    /**
     * Decodes days in a week.
     *
     * @param parser input data parser
     * @return day-in-a-week value
     * @throws IOException if a reading error occurs or wrong format
     */
    private int decodeDaysInWeek(Parser parser)
        throws IOException {
        int daysInWeek = 0;
        while (true) { // decode days
            parser.skipBlank();
            if (!parser.isNextMatchStr(DAYS_OF_WEEK_CODES)) {
                break;
            }
            String dayStr = parser.readId();
            int i;
            for (i = 0; i < DAYS_OF_WEEK_CODES.length; i++) {
                if (DAYS_OF_WEEK_CODES[i].equals(dayStr)) {
                    daysInWeek |= DAYS_OF_WEEK[i];
                    break;
                }
            }
            if (i == DAYS_OF_WEEK_CODES.length) {
                throw new IOException("Wrong format"); // not found
            }
        }
        return daysInWeek;
    }

    /**
     * Decodes weeks in a month.
     *
     * @param parser input data parser
     * @param rule RepeatRule for setting fields
     * @return week-in-a-month value
     * @throws IOException if a reading error occurs or wrong format
     */
    private int decodeWeeksInMonth(Parser parser, RepeatRule rule)
        throws IOException {
        int weeksInMonth = 0;
        while (true) { // decode weeks
            parser.skipBlank();
            if (!parser.isNextMatchStr(WEEKS_OF_MONTH_CODES)) {
                break;
            }
            String weekStr = parser.readId();
            int i;
            for (i = 0; i < WEEKS_OF_MONTH_CODES.length; i++) {
                if (WEEKS_OF_MONTH_CODES[i].equals(weekStr)) {
                    weeksInMonth |= WEEKS_OF_MONTH[i];
                    break;
                }
            }
            if (i == WEEKS_OF_MONTH_CODES.length) {
                throw new IOException("Wrong format"); // not found
            }
        }
        return weeksInMonth;
    }

    /**
     * Decodes months in a year.
     *
     * @param parser input data parser
     * @param rule RepeatRule for setting fields
     * @return month-in-a-year value
     * @throws IOException if a reading error occurs or wrong format
     */
    private int decodeMonthsInYear(Parser parser, RepeatRule rule)
        throws IOException {
        int monthsInYear = 0;
        int monthNum;
        while (true) { // decode monthes
            parser.skipBlank();
            if (!parser.isNextInt()) {
                break;
            }
            monthNum = parser.readInt();
            if (monthNum >= MONTHS_IN_YEAR.length) {
                throw new IOException("Wrong month number");
            }
            monthsInYear |= MONTHS_IN_YEAR[monthNum];
        }
        return monthsInYear;
    }

    /**
     * Puts duration (#value) and end date (yyyymmddThhmmss(Z)/yyyyMMdd)
     * to COUNT and END fields of the repeat rule.
     *
     * @param parser input data parser
     * @param rule repeat rule object for setting
     * @param isSetCount set COUNT field else don't set
     * @throws IOException if a reading error occurs or wrong format
     */
    private void setRepeatRuleCount(Parser parser, RepeatRule rule,
            boolean isSetCount) throws IOException {
        // parse duration
        parser.skipBlank();
        if (parser.match('#')) {
            parser.skip();
            int count = parser.readInt();
            if (isSetCount && count > 0) {
                rule.setInt(RepeatRule.COUNT, count);
            }
        }
        // parse end date
        parser.skipBlank();
        if (parser.hasNextDate()) {
            int dateLen = parser.getEndDate().length();
            // end date is either date in yyyyMMdd format or
            // date/time in yyyymmddThhmmss(Z).
            long date = (dateLen < 15) ?
                PIMHandler.getInstance().parseDate(parser.getEndDate()) :
                PIMHandler.getInstance().parseDateTime(parser.getEndDate());
            rule.setDate(RepeatRule.END, date);
            parser.setPos(parser.getPos() + dateLen);
        }
    }

    /**
     * Decodes one line of a vEvent.
     * @param event data to be processed
     * @param propertyName property key name
     * @param attributes fields to be processed
     * @param data string to be processed
     * 
     * @throws IOException if a read error occured
     */
    private void importData(Event event,
        String propertyName, String[] attributes, String data)
        throws IOException {

        int field = VEventSupport.getFieldCode(propertyName);

        if (event instanceof EventImpl) {
            EventImpl eventImpl = (EventImpl)event;
            
            if (!PIMHandler.getInstance().isSupportedField(
                    eventImpl.getPIMListHandle(), field)) {
                // ignore unsupported fields
                return;
            }
        }

        switch (field) {
            case Event.SUMMARY:
            case Event.LOCATION:
            case Event.NOTE:
            case Event.UID: {
                String sdata = FormatSupport.parseString(attributes, data);
                event.addString(field, Event.ATTR_NONE, sdata);
                break;
            }
            case Event.END:
            case Event.REVISION:
            case Event.START: {
                long date = PIMHandler.getInstance().parseDateTime(data);
                event.addDate(field, Event.ATTR_NONE, date);
                break;
            }
            case Event.CLASS: {
                String sdata = FormatSupport.parseString(attributes, data);
                if (attributes != null && attributes.length != 0) {
                    throw new IOException("Invalid parameter for field CLASS");
                }
                int c = VEventSupport.getClassCode(sdata);
                event.addInt(Event.CLASS, Event.ATTR_NONE, c);
                break;
            }
            case Event.ALARM: {
                String[] s = FormatSupport.parseStringArray(attributes, data);
                if (s.length > 0) {
                    long alarmTime =
                        PIMHandler.getInstance().parseDateTime(s[0]);
                    event.addInt(Event.ALARM, Event.ATTR_NONE,
                        (int) (alarmTime / 1000));
                }
                break;
            }
        }
    }
    
    /**
     * Reads and decodes a single vToDo.
     * @param in input stream
     * @param list PIM list to which the item belongs
     * @throws IOException if an error occurs reading
     * @return ToDo implementation handler
     */
    private ToDoImpl decodeToDo(LineReader in, PIMList list)
        throws IOException {
        ToDoImpl todo = new ToDoImpl((AbstractPIMList)list);
        String line;
        while ((line = in.readLine()) != null) {
            FormatSupport.DataElement element =
                FormatSupport.parseObjectLine(line);
            if (element.propertyName.equals("END")) {
                return todo;
            } else if (element.propertyName.equals("VERSION")) {
                if (!element.data.equals("1.0")) {
                    throw new UnsupportedPIMFormatException("Version "
                        + element.data + " is not supported");
                }
            } else if (element.propertyName.equals("CATEGORIES")) {
                String[] categories = StringUtil.split(element.data, ',', 0);
                for (int j = 0; j < categories.length; j++) {
                    try {
                        todo.addToCategory(categories[j]);
                    } catch (PIMException e) {
                        // cannot add item to category
                    }
                }
            } else {
                importData(todo,
                    element.propertyName, element.attributes, element.data);
            }
        }
        throw new IOException("Unterminated vToDo");
    }

    /** 
     * Decodes one line of a vToDo.
     * @param todo element to fill 
     * @param propertyName key to property value
     * @param attributes fields to import
     * @param data string containing input data
     */
    private void importData(ToDo todo,
        String propertyName, String[] attributes, String data) {
        
        int field = VToDoSupport.getFieldCode(propertyName);
        switch (field) {
            case ToDo.SUMMARY:
            case ToDo.NOTE:
            case ToDo.UID: {
                String sdata = FormatSupport.parseString(attributes, data);
                todo.addString(field, ToDo.ATTR_NONE, sdata);
                break;
            }
            case ToDo.COMPLETION_DATE:
                todo.addBoolean(ToDo.COMPLETED, ToDo.ATTR_NONE, true);
                // fall through
            case ToDo.DUE:
            case ToDo.REVISION: {
                long date = PIMHandler.getInstance().parseDateTime(data);
                todo.addDate(field, ToDo.ATTR_NONE, date);
                break;
            }
            case ToDo.CLASS: {
                String sdata = FormatSupport.parseString(attributes, data);
                int c = VToDoSupport.getClassCode(sdata);
                todo.addInt(ToDo.CLASS, ToDo.ATTR_NONE, c);
                break;
            }
            case ToDo.PRIORITY: {
                try {
                    int i = Integer.parseInt(data);
                    todo.addInt(ToDo.PRIORITY, ToDo.ATTR_NONE, i);
                } catch (NumberFormatException e) {
                    // ignore this field
                }
                break;
            }
        }
    }

}

/**
 * A simple parser for encoded RepeatRule data.
 */
class Parser {

    /** Source data buffer. */
    String s;

    /** Current position in source data buffer. */
    private int index;

    /** Saved end date. */
    private String endDate = "";


    /** 
     * Constructor.
     *
     * @param s string for parsing 
     */
    Parser(String s) {
        this.s = s;
        index = 0;
    }
    
    /**
     * Checks that current position is valid.
     * @throws IOException if position is out of buffer
     */
    private void checkBound() throws IOException {
        checkBound(0);
    }
    
    /**
     * Checks that input position is valid.
     * @param off offset from current position
     * @throws IOException if position + offset is out of buffer
     */
    private void checkBound(int off) throws IOException {
        int bound = index + off;
        if (bound < 0 || bound >= s.length()) {
            throw new IOException("Out of bound");
        }
    }

    /**
     * Reads next char from buffer.
     * @return next char from buffer
     * @throws IOException if position is out of buffer
     */
    char readChar() throws IOException {
        checkBound();
        return s.charAt(index++);
    }

    /**
     * Reads next char from buffer without changing the pointer.
     * @return next char from buffer
     * @throws IOException if position is out of buffer
     */
    char nextChar() throws IOException {
        return nextChar(0);
    }

    /**
     * Reads next char by given position 
     * from buffer without changing the pointer.
     * @param off offset from current position
     * @return next char from buffer
     * @throws IOException if position + offset is out of buffer
     */
    char nextChar(int off) throws IOException {
        checkBound(off);
        return s.charAt(index + off);
    }

    /**
     * Checks that next chars contain a date
     * in format yyyymmddThhmmss.
     *
     * For more details please see RFC 2445
     *
     * @return true when next chars contain a date else false
     * If the date exists then it is saved in endDate member
     */
    boolean hasNextDate() { // Date format yyyymmddThhmmss
        endDate = "";
        if (!hasMoreChars()) {
            return false;
        }
        String strDate = getRemainder();
        int dateLength = 8; // yyyymmdd
        if (strDate.length() < dateLength) {
            return false;
        }
        if (strDate.length() > 14 && strDate.charAt(8) == 'T') {
            dateLength = 15; // yyyymmddThhmmss
            // yyyymmddThhmmssZ - absolute time
            if (strDate.length() > 15 && strDate.charAt(15) == 'Z') {
                dateLength = 16;
            }
        }
        strDate = strDate.substring(0,dateLength);
        try {
            int year = Integer.parseInt(strDate.substring(0, 4));
            if (year < 1970) {
                return false;
            }
            int month = Integer.parseInt(strDate.substring(4, 6));
            if ((month < 1) || (month > 12)) {
                return false;
            }
            int day = Integer.parseInt(strDate.substring(6, 8));
            if ((day < 1) || (day > 31)) {
                return false;
            }
            // Don't check time
        } catch (NumberFormatException ex) {
            return false;
        }
        endDate = strDate;
        return true;
    }

    /**
     * Gets a date that saved by hasNextDate method.
     *
     * @return saved date
     */
    String getEndDate() {
        return endDate;
    }

    /**
     * Reads next integer value from buffer. 
     *
     * @return integer value
     * @throws IOException if chars from current position don't contain
     * integer value
     */
    int readInt() throws IOException {
        checkBound();
        StringBuffer sb = new StringBuffer();
        for( ; index < s.length() && Character.isDigit(s.charAt(index)) ; index++) {
            sb.append(s.charAt(index));
        }
        if (sb.length() == 0) {
            throw new IOException("No digital chars");
        }
        return Integer.parseInt(sb.toString());
    }

    /**
     * Checks that the next char is equal to given char.
     * @param sym char for comparing
     * @return true when next char is equal to given char
     */
    boolean match(char sym) {
        if (!hasMoreChars()) {
            return false;
        }
        return sym == s.charAt(index);
    }

    /**
     * Checks that the buffer contains chars from current
     * position.
     * @return true when buffer has unparsed chars
     */
    boolean hasMoreChars() {
        return index < s.length();
    }
    
    /**
     * Skips the current position.
     */
    void skip() {
        index++;
    }

    /**
     * Skips next spaces and tabs.
     */
    void skipBlank() {
        while (hasMoreChars() && (match(' ') || match('\t'))) {
            skip();
        }
    }

    /**
     * Checks that the next symbol is equal to given one.
     * 
     * @throws IOException if next symbol is different from input one
     * @param sym the symbol to be checked
     */
    void matchSkip(char sym) throws IOException {
        if (!match(sym)) {
            throw new IOException("No symbol " + sym);
        }
        index++;
    }

    /**
     * Gets the remainder of parsed buffer.
     * @return part of buffer from current position
     */
   String getRemainder() {
        String retValue = null;
        if (index < s.length()) {
            retValue = s.substring(index);
        }
        return retValue;
    }
    
    /**
     * Gets the current position.
     * @return current position
     */
    int getPos() {
        return index;
    }
    
    /**
     * Sets the given position.
     * @param pos position for setting
     */
    void setPos(int pos) {
        index = pos;
    }

    /**
     * Checks that next ID from buffer could be
     * found in input string array.
     * @param arrStr array for searching
     * @return true when next ID is found in the input array else false
     */
    boolean isNextMatchStr(String[] arrStr) {
        if (!hasMoreChars()) {
            return false;
        }
        int pos = index;
        String nextId;
        try {
            nextId = readId();
        } catch (IOException ex) {
            return false;
        }
        index = pos;
        if ((nextId == null) || (nextId.length() == 0)) {
            return false;
        }
        for (int i = 0; i < arrStr.length; i++) {
            if (arrStr[i].equals(nextId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks that next ID from buffer contains
     * digit symbols only.
     * @return true when next ID contains digit symbols only
     */
    boolean isNextInt() {
        if (!hasMoreChars()) {
            return false;
        }
        int pos = index;
        String nextId;
        try {
            nextId = readId();
        } catch (IOException ex) {
            return false;
        }
        index = pos;
        if ((nextId == null) || (nextId.length() == 0)) {
            return false;
        }
        return Character.isDigit(nextId.charAt(0));
    }


    /**
     * Gets next ID from buffer.
     * @return next ID
     * @throws IOException if position is out of buffer bounds
     */
    String readId() throws IOException {
        checkBound();
        String id = getRemainder();
        int index = id.indexOf(' ');
        if (index > -1) {
            id = id.substring(0, index);
        }
        setPos(getPos() + id.length());
        return id;
    }
}