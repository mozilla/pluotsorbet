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
package javax.microedition.location;

/**
 * This class is defined by the JSR-179 specification
 * <em>Location API for J2ME for J2ME&trade;.</em>
 */
// JAVADOC COMMENT ELIDED
public class AddressInfo {
    // JAVADOC COMMENT ELIDED
    public static final int EXTENSION = 1;

    // JAVADOC COMMENT ELIDED
    public static final int STREET = 2;

    // JAVADOC COMMENT ELIDED
    public static final int POSTAL_CODE = 3;

    // JAVADOC COMMENT ELIDED
    public static final int CITY = 4;

    // JAVADOC COMMENT ELIDED
    public static final int COUNTY = 5;

    // JAVADOC COMMENT ELIDED
    public static final int STATE = 6;

    // JAVADOC COMMENT ELIDED
    public static final int COUNTRY = 7;

    // JAVADOC COMMENT ELIDED
    public static final int COUNTRY_CODE = 8;

    // JAVADOC COMMENT ELIDED
    public static final int DISTRICT = 9;

    // JAVADOC COMMENT ELIDED
    public static final int BUILDING_NAME = 10;

    // JAVADOC COMMENT ELIDED
    public static final int BUILDING_FLOOR = 11;

    // JAVADOC COMMENT ELIDED
    public static final int BUILDING_ROOM = 12;

    // JAVADOC COMMENT ELIDED
    public static final int BUILDING_ZONE = 13;

    // JAVADOC COMMENT ELIDED
    public static final int CROSSING1 = 14;

    // JAVADOC COMMENT ELIDED
    public static final int CROSSING2 = 15;

    // JAVADOC COMMENT ELIDED
    public static final int URL = 16;

    // JAVADOC COMMENT ELIDED
    public static final int PHONE_NUMBER = 17;

    // JAVADOC COMMENT ELIDED
    final static int DATA_SIZE = 17;

    // JAVADOC COMMENT ELIDED    
    private String[] data = new String[DATA_SIZE];

    // JAVADOC COMMENT ELIDED
    public AddressInfo() {
    }

    // JAVADOC COMMENT ELIDED
    AddressInfo(String[] data) {
        this.data = data;
    }

    // JAVADOC COMMENT ELIDED
    String[] getData() {
        return data;
    }

    // JAVADOC COMMENT ELIDED
    public String getField(int field) {
        checkField(field);
        return data[field - 1];
    }

    // JAVADOC COMMENT ELIDED
    private void checkField(int field) {
        if (field < 1 || field > data.length) {
            throw new 
		IllegalArgumentException("Unsuported field attribute value: "
					 + field);
        }
    }

    // JAVADOC COMMENT ELIDED
    public void setField(int field, String value) {
        checkField(field);
        data[field - 1] = value;
    }
}
