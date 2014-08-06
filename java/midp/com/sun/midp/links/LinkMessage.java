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

package com.sun.midp.links;

/**
 * Encapsulates some data to be sent between Isolates through a Link.
 */
public class LinkMessage {

    // IMPL_NOTE: force classloading of byte[] so that
    // KNI_FindClass() will always find it.
    static {
        try {
            Class.forName("[B");
        } catch (ClassNotFoundException ignore) { }
    }

    Object contents;

    // used only for data (byte-array) messages, otherwise zeroes
    int offset; // = 0
    int length; // = 0

    /**
     * Constructs a LinkMessage with the given contents, length, and offset 
     * values. Called only by the static factory methods.
     */
    private LinkMessage(Object newContents, int newOffset, int newLength) {
        contents = newContents;
        offset = newOffset;
        length = newLength;
    }

    /**
     * Constructs an empty LinkMessage. This is used only by Link.receive().
     */
    LinkMessage() {
        this(null, 0, 0);
    }

    /**
     * Queries whether the LinkMessage contains data, that is, a byte array.
     */
    public boolean containsData() {
        return contents instanceof byte[];
    }

    /**
     * Queries whether the LinkMessage contains a Link.
     */
    public boolean containsLink() {
        return contents instanceof Link;
    }

    /**
     * Queries whether the LinkMessage contains a String.
     */
    public boolean containsString() {
        return contents instanceof String;
    }

    /**
     * Returns the contents of the LinkMessage as an Object. The caller must 
     * test the reference returned using <code>instanceof</code> and cast it 
     * appropriately.
     */
    public Object extract() {
        return contents;
    }

    /**
     * Returns the contents of the LinkMessage if it contains a byte array. If
     * the message does not contain a byte array, throws IllegalStateException.
     */
    public byte[] extractData() {
        if (! (contents instanceof byte[])) {
            throw new IllegalStateException();
        }

        byte[] data = (byte[])contents;

        if (offset == 0 && length == data.length) {
            return data;
        }

        // need to copy the subrange

        byte[] newData = new byte[length];
        System.arraycopy(data, offset, newData, 0, length);
        return newData;
    }

    /**
     * Returns the contents of the LinkMessage if it contains is a Link. If 
     * the message does not contain a Link, throws IllegalStateException.
     */
    public Link extractLink() {
        if (contents instanceof Link) {
            return (Link)contents;
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Returns the contents of the LinkMessage if it contains is a String. If 
     * the message does not contain a String, throws IllegalStateException.
     */
    public String extractString() {
        if (contents instanceof String) {
            return (String)contents;
        } else {
            throw new IllegalStateException();
        }
    }

    public static LinkMessage newDataMessage(byte[] data) {
        return new LinkMessage(data, 0, data.length);
    }

    public static LinkMessage newDataMessage(
            byte[] data, int offset, int length) {
        if (offset < 0
                || offset > data.length
                || length < 0
                || offset + length < 0
                || offset + length > data.length) {
            throw new IndexOutOfBoundsException();
        }

        return new LinkMessage(data, offset, length);
    }

    public static LinkMessage newLinkMessage(Link link) {
        return new LinkMessage(link, 0, 0);
    }

    public static LinkMessage newStringMessage(String string) {
        return new LinkMessage(string, 0, 0);
    }

}
