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
package com.sun.jsr082.obex;

import javax.obex.HeaderSet;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Calendar;
import java.util.Vector;

public class HeaderSetImpl implements HeaderSet {

    /* Debug information, should be false for RR. */
    private static final boolean DEBUG = false;

    public static final int OWNER_SERVER = 1;
    public static final int OWNER_CLIENT = 2;
    public static final int OWNER_SERVER_USER = 3;
    public static final int OWNER_CLIENT_USER = 4;

    static final int TYPE_UNICODE = 0;
    static final int TYPE_BYTEARRAY = 1;
    static final int TYPE_BYTE = 2;
    static final int TYPE_LONG = 3;
    static final int TYPE_SPECIAL_TIME_4 = 4;
    static final int TYPE_SPECIAL_TIME_ISO = 5;
    static final int TYPE_SPECIAL_TYPE = 6;
    static final int TYPE_UNSUPPORTED = 7;
    static final int TYPE_AUTH_CHALLENGE = 8;
    static final int TYPE_AUTH_RESPONSE = 9;

    /* package protected */ int owner;
    /* package protected */ int packetType;

    private Hashtable headers;
    Vector challenges = new Vector();

    public HeaderSetImpl(int owner) {
        this.owner = owner;
        headers = new Hashtable(5);
    }


    /*
     * Adds headers from h to this HeaderSet.
     */
    void merge(HeaderSetImpl h) {
        int[] idList = h.getHeaderList();

        if (idList == null) {
            return;
        }

        for (int i = 0; i < idList.length; i++) {
            int id = idList[i];
            Object val = h.getHeader(id);
            int type = internalType(id);
            switch (type) {
                case TYPE_UNICODE:
                case TYPE_BYTE:
                case TYPE_LONG:
                case TYPE_SPECIAL_TYPE:
                    setHeader(id, val);
                    break;

                case TYPE_BYTEARRAY:
                    byte[] array = (byte[]) val;
                    byte[] copy = new byte[array.length];
                    System.arraycopy(array, 0, copy, 0, array.length);
                    setHeader(id, (Object) copy);
                    break;

                case TYPE_SPECIAL_TIME_4:
                case TYPE_SPECIAL_TIME_ISO:
                    Calendar cal = (Calendar) val;
                    Calendar calCopy = Calendar.getInstance();
                    calCopy.setTime(cal.getTime());
                    setHeader(id, (Object) calCopy);
                    break;

                default:
                    // no default
                    if (DEBUG) {
                        System.out.println("Debug: unknown header id");
                    }
            }
        }
    }


    /*
     * Makes private copy of the headers.
     */
    HeaderSetImpl(HeaderSetImpl h) {
        this.packetType = h.packetType;
        this.owner = h.owner;
        headers = new Hashtable(5);
        merge(h);
    }

    boolean isSendable() {
        return owner >= OWNER_SERVER_USER;
    }

    // interface functions:
    public void createAuthenticationChallenge(java.lang.String realm,
            boolean userID, boolean access) {

        ObexAuth auth = ObexAuth.createChallenge(realm, userID, access);
        challenges.addElement(auth);
    }

    public Object getHeader(int headerID) {
        Object value = headers.get(new Integer(headerID));

        if (value != null) {
            return value;
        }

        // check validness of headerID
        int type = internalType(headerID);

        if (type >= TYPE_UNSUPPORTED) {
            throw new IllegalArgumentException("bad header id");
        }
        return null;
    }

    public int[] getHeaderList() {
        synchronized (headers) {
            if (headers.isEmpty()) {
                return null;
            }
            Enumeration keys = headers.keys();
            int[] ids = new int[headers.size()];
            int i = 0;

            while (keys.hasMoreElements()) {
                Object obj = keys.nextElement();
                ids[i++] = ((Integer)obj).intValue();
            }
            return ids;
        }
    }

    public int getResponseCode() throws IOException {
        if (owner != OWNER_CLIENT) {
            throw new IOException("invalid use");
        }
        if (packetType == ObexPacketStream.OPCODE_CONTINUE) {
            throw new IOException("operation not finished");
        }
        return packetType;
    }

    public void setHeader(int headerID, Object headerValue) {
        int type = internalType(headerID);
        String errormsg = "incompatible header object";

        if (type >= TYPE_UNSUPPORTED) {
            throw new IllegalArgumentException("bad header id");
        }

        if (headerValue == null) {
            synchronized (headers) {
                headers.remove(new Integer(headerID));
            }
            return;
        }

        boolean fail = false;
        switch (type) {
            case TYPE_UNICODE:
                if (!(headerValue instanceof String)) fail = true;
                break;

            case TYPE_BYTEARRAY:
                if (!(headerValue instanceof byte[])) fail = true;
                break;

            case TYPE_BYTE:
                if (!(headerValue instanceof Byte)) fail = true;
                break;

            case TYPE_LONG:
                if (!(headerValue instanceof Long)) {
                    fail = true;
                    break;
                }
                long value = ((Long)headerValue).longValue();

                if (value < 0L || value > 0xFFFFFFFFL)  {
                    errormsg = "long value out of range 0 .. 0xFFFFFFFF";
                    fail = true;
                }
                break;

            case TYPE_SPECIAL_TIME_4:
                if (!(headerValue instanceof Calendar)) fail = true;
                break;

            case TYPE_SPECIAL_TIME_ISO:
                if (!(headerValue instanceof Calendar)) fail = true;
                break;

            case TYPE_SPECIAL_TYPE:
                if (!(headerValue instanceof String)) fail = true;
                break;
            // no default
        }

        if (fail) {
            throw new IllegalArgumentException(errormsg);
        }
        synchronized (headers) {
            headers.put(new Integer(headerID), headerValue);
        }
    }

    static final int internalType(int headerID) {
        if ((headerID & ~0xFF) != 0) {
            return TYPE_UNSUPPORTED;
        }
        if ((headerID & 0x30) == 0x30) {
            // user defined
            return headerID >> 6;
        }

        switch (headerID) {
            case HeaderSet.TIME_ISO_8601:
                return TYPE_SPECIAL_TIME_ISO;

            case HeaderSet.TIME_4_BYTE:
                return TYPE_SPECIAL_TIME_4;

            case HeaderSet.TYPE:
                return TYPE_SPECIAL_TYPE;

            case HeaderSet.COUNT:
            case HeaderSet.LENGTH:
                return TYPE_LONG;

            case HeaderSet.NAME:
            case HeaderSet.DESCRIPTION:
                return TYPE_UNICODE;

            case HeaderSet.TARGET:
            case HeaderSet.HTTP:
            case HeaderSet.WHO:
            case HeaderSet.OBJECT_CLASS:
            case HeaderSet.APPLICATION_PARAMETER:
                return TYPE_BYTEARRAY;

            case ObexPacketStream.HEADER_AUTH_CHALLENGE:
                return TYPE_AUTH_CHALLENGE;

            case ObexPacketStream.HEADER_AUTH_RESPONSE:
                return TYPE_AUTH_RESPONSE;

            default:
                return TYPE_UNSUPPORTED;
        }
    }
}
