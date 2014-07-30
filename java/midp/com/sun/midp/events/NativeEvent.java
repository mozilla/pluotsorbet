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

package com.sun.midp.events;

/**
 * This class represents a union of the parameters of all native events.
 * <p>
 * Since KNI does not allow creation of Java objects an empty object
 * must be create at the Java level to be filled in by the native level.
 * Because the Java level may not know what native event is pending (this
 * to save native method calls) it the object sent down must be able to
 * handle the data of any native event.</p>
 */
public class NativeEvent extends Event {
    /** Construct a native event. */
    NativeEvent() {
        /* The native code will fill in the type field. */
        super(0);
    }

    /**
     * Construct a native event.
     *
     * @param type Type ID of the event
     */
    public NativeEvent(int type) {
        super(type);
    }

    /** First int parameter for the event. Event dependent. */
    public int intParam1;

    /** Second int parameter for the event. Event dependent. */
    public int intParam2;

    /** Third int parameter for the event. Event dependent. */
    public int intParam3;

    /** Fourth int parameter for the event. Event dependent. */
    public int intParam4;

    /** Fifth int parameter for the event. Event dependent. */
    public int intParam5;

    /** Sixth int parameter for the event. Event dependent. */
    public int intParam6;

    /** First string parameter for the event. Event dependent. */
    public String stringParam1;

    /** Second string parameter for the event. Event dependent. */
    public String stringParam2;

    /** Third string parameter for the event. Event dependent. */
    public String stringParam3;

    /** Third string parameter for the event. Event dependent. */
    public String stringParam4;

    /** Third string parameter for the event. Event dependent. */
    public String stringParam5;

    /** Third string parameter for the event. Event dependent. */
    public String stringParam6;

    /**
     * Clears the parameters, so the event can be reused.
     */
    public void clear() {
        type = 0;

        intParam1 = 0;
        intParam2 = 0;
        intParam3 = 0;
        intParam4 = 0;
        intParam5 = 0;
        intParam6 = 0;

        stringParam1 = null;
        stringParam2 = null;
        stringParam3 = null;
        stringParam4 = null;
        stringParam5 = null;
        stringParam6 = null;
    }

    /**
     * Print the event.
     *
     * @return string containing a list of the parameter values
     */
    public String toString() {
        return "Native Event: t  = " + type + ", i1 = " + intParam1 +
            ", i2 = " + intParam2 + ", i3 = " + intParam3 +
            ", i4 = " + intParam4 + ", i5 = " + intParam5 +
            ", i6 = " + intParam6 +
            "\n    s1 = " + stringParam1 +
            "\n    s2 = " + stringParam2 +
            "\n    s3 = " + stringParam3 +
            "\n    s4 = " + stringParam4 +
            "\n    s5 = " + stringParam5 +
            "\n    s6 = " + stringParam6;
    }
}
