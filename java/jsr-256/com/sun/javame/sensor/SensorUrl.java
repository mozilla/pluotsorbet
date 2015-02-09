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

package com.sun.javame.sensor;

import javax.microedition.sensor.*;
import java.util.Vector;

public class SensorUrl {
    public  static final String SCHEME = "sensor:";

    private static final char SEP = ';';
    private static final char SEP_PUSH = '?';
    private static final char SEP_PUSH2 = '&';
    private static final char EQ = '=';
    private static final String MODEL = "model";
    private static final String LOCATION = "location";
    private static final String CONTEXT_TYPE = "contextType";
    private int numLength;
    private int currPos;
    private String parseUrl;
    private Vector channelList;

    private String model;
    private String quantity;
    private String contextType;
    private String location;
    
    /** Creates a new instance of SensorUrl */
    private SensorUrl() {
    }
    
    public String getModel() { return model; }
    public String getQuantity() { return quantity; }
    public String getContextType() { return contextType; }
    public String getLocation() { return location; }
    
    private void setModel(String model) {
        this.model = model;
    }
    
    private void setQuantity(String quantity) {
        this.quantity = quantity;
    }
    
    private void setContextType(String contextType) {
        this.contextType = contextType;
    }
    
    private void setLocation(String location) {
        this.location = location;
    }
    
    /**
     * Checks symbol is it alphanum.
     *
     * @param symbol the source url
     *
     * @return true when symbol is alphanum
     */
    static boolean isAlphaNum(char symbol) {
        return Character.isDigit(symbol) ||
            Character.isLowerCase(symbol) ||
            Character.isUpperCase(symbol) ||
            isMark(symbol);
    }

    /**
     * Checks symbol is it mark.
     *
     * @param symbol the source url
     *
     * @return true when symbol is
     * "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
     */
    static boolean isMark(char symbol) {
        char[] symbolsMark = {'-', '_', '.', '!', '~', '*',
                '\'', '(', ')'};
        for (int i = 0; i < symbolsMark.length; i++) {
            if (symbol == symbolsMark[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the alphanum word by current position.
     *
     * @return alphanum ID by position
     * @throws IllegaArgumentException when current
     * symbol is not alphanum or position is out of string
     */
    private String getId() throws IllegalArgumentException {
        if (currPos < 0 || currPos > parseUrl.length() - 1) {
            throw new IllegalArgumentException("Wrong position");
        }

        if (!isAlphaNum(parseUrl.charAt(currPos))) {
            throw new IllegalArgumentException("First symbol is not alphanum");
        }

        StringBuffer sb = new StringBuffer();
        int urlLength = parseUrl.length();
        char sym;
        while (currPos < urlLength && isAlphaNum(sym = parseUrl.charAt(currPos))) {
            sb.append(sym);
            currPos++;
        }
        return sb.toString();
    }
    
    /**
     * Gets the alphanum word by current position
     * and compare it with pattern.
     *
     * @param pattern the word value which getting word should be equal
     *
     * @throws IllegaArgumentException when current
     * symbol is not alphanum or position is out of string or 
     * word is not match
     */
    private void idCompare(String pattern)
        throws IllegalArgumentException {
        if (!pattern.equals(getId())) {
            throw new IllegalArgumentException(
                "Current word isn't equal to "+pattern);
        }
    }
    
    /**
     * Gets the alphanum word by current position
     * and find it in the pattern array.
     *
     * @param patternArray the word value array which getting word
     * should be find
     *
     * @return alphanum ID by position
     * @throws IllegaArgumentException when current
     * symbol is not alphanum or position is out of string or 
     * word is not found in the input array
     */
    private String idCompareArr(String[] patterns)
        throws IllegalArgumentException {
        String currWord = getId();
        boolean isFound = false;
        for (int i = 0; i < patterns.length; i++) {
            if (currWord.equals(patterns[i])) {
                isFound = true;
                break;
            }
        }
        if (!isFound) {
            throw new IllegalArgumentException(
                "Current word isn't found in the input array");
        }
        return currWord;
    }

    /**
     * Gets the double value by current position.
     *
     * @return double value by position
     * @throws IllegaArgumentException on wrong digital format
     */
    private double getNumber() throws IllegalArgumentException {
        numLength = 0;
        boolean isFirstPosition = true;
        boolean isPoint = false;
        boolean isDigit = false;
        StringBuffer sb = new StringBuffer();
        char currSym;
        while (currPos < parseUrl.length()) {
            currSym = parseUrl.charAt(currPos++);
            if (isFirstPosition) {
                isFirstPosition = false;
                if (currSym == '-') {
                    sb.append(currSym);
                    continue;
                }
            }
            if (currSym == '.') {
                if (!isDigit || isPoint) {
                    throw new IllegalArgumentException(
                        "Wrong decimal point position");
                }
                isPoint = true;
                sb.append(currSym);
            } else if (Character.isDigit(currSym)) {
                sb.append(currSym);
                isDigit = true;
            } else {
                currPos--;
                break;
            }
        }
        if (!isDigit) {
            throw new IllegalArgumentException(
                "Wrong number format");
        }
        numLength = sb.length();
        return Double.parseDouble(sb.toString());
    }

    /**
     * Checks that current symbol is a separator.
     *
     * @param separator symbol of separator
     *
     * @throws IllegalArgumentException when current symbol is not separator or
     * position is wrong
     */
    private void checkSeparator(char separator)
    throws IllegalArgumentException {
        if (currPos < 0 || currPos > parseUrl.length() - 1) {
            throw new IllegalArgumentException("Wrong position");
        }
        if (separator != parseUrl.charAt(currPos++)) {
            throw new IllegalArgumentException("Current symbol is not separator "+
                "url = \""+parseUrl+"\" pos = "+currPos);
        }
    }
    
    /**
     * Parse sensor URL for Connector.open().
     *
     * @param url the source url
     * @return SensorUrl object
     * @throws IllegaArgumentException when url is not valid
     */
    public static SensorUrl parseUrl(String url) {
        SensorUrl su = new SensorUrl();
        su.parseUrl = new String(url);
        // <sensor_url> ::=  "sensor:"<sensor_id>
        if (!su.parseUrl.startsWith(SCHEME)) {
            throw new IllegalArgumentException("Wrong scheme");
        }
        
        su.currPos = SCHEME.length();
        int urlLength = su.parseUrl.length();
        
        // <sensor_id> ::=  <quantity>[<contextType>][<model>][<location>]
        // <quantity>  ::=  ("temperature"|"acceleration"|...)
        String quantity = su.getId();
        su.setQuantity(quantity);
        boolean isContentType = false;
        boolean isModel = false;
        boolean isLocation = false;
        while (su.currPos < urlLength) { 

            // contextType, model, location start with separator
            su.checkSeparator(SEP);
            // <contextType> ::=  <separator>"contextType="("ambient"|"device"|"user")
            // <model>       ::=  <separator>"model="<model_id>
            // <location>    ::=  <separator>"location="<location_id>
            String[] headers = {CONTEXT_TYPE, MODEL, LOCATION};
            String header = su.idCompareArr(headers);
            su.checkSeparator(EQ);
            if (header.equals(CONTEXT_TYPE)) {
                if (isContentType) {
                    throw new IllegalArgumentException(
                        "contextType defined twice");
                }
                isContentType = true;
                String[] headers1 = {SensorInfo.CONTEXT_TYPE_AMBIENT,
                        SensorInfo.CONTEXT_TYPE_DEVICE,
                        SensorInfo.CONTEXT_TYPE_USER};
                String contextTypeValue = su.idCompareArr(headers1);
                su.setContextType(contextTypeValue);
            } else if (header.equals(MODEL)) {
                // <model> ::=  <separator>"model="<model_id>
                // <model_id> ::=  <alphanum>*
                if (isModel) {
                    throw new IllegalArgumentException("model defined twice");
                }
                isModel = true;
                String modelId = su.getId();
                su.setModel(modelId);
            } else if (header.equals(LOCATION)) {
                // <location> ::=  <separator>"location="<location_id>
                // <location_id> ::=  <alphanum>*
                if (isLocation) {
                    throw new IllegalArgumentException("location defined twice");
                }
                isLocation = true;
                String locationId = su.getId();
                su.setLocation(locationId);
            }
        }
        return su;
    }
    
    /**
     * Parse sensor URL for push registration.
     *
     * @param url the source url
     * @return SensorUrl object
     * @throws IllegaArgumentException when url is not valid
     */
    public static SensorUrl parseUrlPush(String url) {
        // <push_sensor_url> ::= <sensor_url>[<params>]
        // <params> ::= "?"<channel_list>
        int i = url.indexOf(SEP_PUSH);
        if (i == -1) {
            return parseUrl(url);
        }

        SensorUrl su = parseUrl(url.substring(0, i));
        su.parseUrl = new String(url.substring(i + 1));

        su.currPos = 0;
        int savePos;
        int urlLength = su.parseUrl.length();
        if (su.currPos > urlLength - 1) {
            throw new IllegalArgumentException("No channel list");
        }
        // parse channel list
        while (su.currPos < urlLength) {
            // <channel_list> ::= <channel>[<separator2><channel>]*
            // <channel> ::= "channel="<channel_id><separator2><condition_list>
            su.idCompare("channel");
            su.checkSeparator(EQ);
            String channelId = su.getId();
            if (su.channelList == null) {
                su.channelList = new Vector();
            }
            su.checkSeparator(SEP_PUSH2);
            Vector conditionList = new Vector();
            // fill condition list
            su.currPos--;
            while (su.currPos < urlLength && su.parseUrl.charAt(su.currPos) == SEP_PUSH2) {
                su.currPos++;
                // <condition_list> ::= <condition>[<separator2><condition>]*
                // <condition> ::= (<limit>|<range>)
                // <limit> ::=
                // "limit="<double><separator2>"op="("eq"|"lt"|"le"|"ge"|"gt")
                // <double>::= "-"?(<integer>|<decimal>)
                // <integer> ::= <nonzero-digit><digit>*
                // <decimal> ::= <integer>"."<digit>+
                savePos = su.currPos;
                String[] headers = {"limit", "lowerLimit", "channel"};
                String conditionName = su.idCompareArr(headers);
                // next header could be "channel"
                if (conditionName.equals("channel")) {
                    if (conditionList.size() > 0) { // goto parsing new channel
                        su.currPos = savePos;
                        break;
                    } else {
                        throw new IllegalArgumentException("No condition list");
                    }
                }
                su.checkSeparator(EQ);
                if (conditionName.equals("limit")) { // limit condition
                    double limit = su.getNumber();
                    su.checkSeparator(SEP_PUSH2);
                    su.idCompare("op");
                    su.checkSeparator(EQ);
                    String[] opValues = {Condition.OP_EQUALS, Condition.OP_GREATER_THAN,
                        Condition.OP_GREATER_THAN_OR_EQUALS, Condition.OP_LESS_THAN,
                        Condition.OP_LESS_THAN_OR_EQUALS};
                    String opValue = su.idCompareArr(opValues);
                    conditionList.addElement(new LimitCondition(limit, opValue));
                } else if(conditionName.equals("lowerLimit")) { // range condition
                    // <range>::= "lowerLimit="<double><separator2>
                    // "lowerOp="("ge"|"gt")<separator2>
                    // "upperLimit="<double><separator2> "upperOp="("le"|"lt")
                    double lowerLimit = su.getNumber();
                    su.checkSeparator(SEP_PUSH2);
                    su.idCompare("lowerOp");
                    su.checkSeparator(EQ);
                    String[] headers1 = {Condition.OP_GREATER_THAN,
                        Condition.OP_GREATER_THAN_OR_EQUALS};
                    String lowerOpValue = su.idCompareArr(headers1);
                    su.checkSeparator(SEP_PUSH2);
                    su.idCompare("upperLimit");
                    su.checkSeparator(EQ);
                    double upperLimit = su.getNumber();
                    su.checkSeparator(SEP_PUSH2);
                    su.idCompare("upperOp");
                    su.checkSeparator(EQ);
                    String[] headers2 = {Condition.OP_LESS_THAN,
                        Condition.OP_LESS_THAN_OR_EQUALS};
                    String upperOpValue = su.idCompareArr(headers2);
                    conditionList.addElement(new RangeCondition(
                        lowerLimit, lowerOpValue, upperLimit, upperOpValue));
                }

            }
            Object[] arr = {channelId, conditionList};
            su.channelList.addElement(arr);
        }
        return su;
    }

    public static String createUrl(SensorInfo info) {
        StringBuffer b = new StringBuffer(SCHEME);
        b.append(info.getQuantity());
        
        if (info.getContextType() != null) {
            b.append(SEP);
            b.append(CONTEXT_TYPE);
            b.append(EQ);
            b.append(info.getContextType());
        }
        
        if (info.getModel() != null) {
            b.append(SEP);
            b.append(MODEL);
            b.append(EQ);
            b.append(info.getModel());
        }
        
        String loc = null;
        try {
            loc = (String)info.getProperty(SensorInfo.PROP_LOCATION);
        } catch (IllegalArgumentException ex) {
        }
        if (loc != null) {
            b.append(SEP);
            b.append(LOCATION);
            b.append(EQ);
            b.append(loc);
        }
        
        return b.toString();
    }
}
