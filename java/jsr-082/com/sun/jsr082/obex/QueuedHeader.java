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

import java.util.Calendar;
import java.util.Vector;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class QueuedHeader {

    /* Debug information, should be false for RR. */
    private static final boolean DEBUG = false;

    private ObexPacketStream stream;
    private int type;
    private Object value;
    byte[] buffer;
    int packetLength;

    QueuedHeader(ObexPacketStream stream) {
        this.stream = stream;
    }

    /*
     *
     * @param type
     * @param value
     * @throws IOException
     */
    void sendOrQueue(int type, Object value) throws IOException {
        if (stream.moreHeaders && send(type, value)) {
            stream.challengesToSend = true;
            return;
        }
        queue(type, value);
    }

    /*
     * Adds the header to queue in ObexPacketStream.
     * @param type header's type defined in <code>HeaderSetImpl<code> class.
     * @param value header's value.
     * @throws IOException thrown if the header is too large.
     */
    void queue(int type, Object value) throws IOException {
        int maxSendLength = stream.maxSendLength;
        boolean fail = false;

        switch (HeaderSetImpl.internalType(type)) {
            // ids which require special handling
            case HeaderSetImpl.TYPE_SPECIAL_TIME_ISO:
            case HeaderSetImpl.TYPE_SPECIAL_TIME_4:
            case HeaderSetImpl.TYPE_LONG:
            case HeaderSetImpl.TYPE_BYTE:
                break;

            case HeaderSetImpl.TYPE_SPECIAL_TYPE:
                // type8 + len16 + head + len16 + bytes[length] + zero8
                if (((String)value).length() + 7 > maxSendLength) {
                    fail = true;
                }
                break;

            case HeaderSetImpl.TYPE_UNICODE:
                // type8 + len16 + head + len16 + bytes[length] + zero16
                if ((((String)value).length() << 1) + 8 > maxSendLength) {
                    fail = true;
                }
                break;

            case HeaderSetImpl.TYPE_BYTEARRAY:
                // type8 + len16 + head + len16 + bytes[length]
                if (((byte[])value).length + 6 > maxSendLength) {
                    fail = true;
                }
                break;

            case HeaderSetImpl.TYPE_AUTH_CHALLENGE:
                Vector challenges = (Vector)value;
                if (challenges.isEmpty())
		    return;
                int len = 0;
                for (int i = 0; i < challenges.size(); i++) {
                    len += ((ObexAuth) challenges.elementAt(i))
                        .prepareChallenge();
                }
                if (len + 3 > maxSendLength) {
                    fail = true;
                }
                break;
        }

        if (fail) {
            stream.headerTooLarge();
            return;
        }
        this.type = type;
        this.value = value;
        newHeader();
    }

    boolean trySendAgain() throws IOException {
        return send(type, value);
    }

    boolean send(int type, Object value) throws IOException {
        buffer = stream.buffer;
        packetLength = stream.packetLength;
        int maxSendLength = stream.maxSendLength;
        if (DEBUG) {
            System.out.println("Sending header 0x" + Integer.toHexString(type));
        }

        try {
            buffer[packetLength++] = (byte)type;
            switch (HeaderSetImpl.internalType(type)) {
                // ids which require special handling
                case HeaderSetImpl.TYPE_SPECIAL_TIME_ISO:
                    encodeTime8601((Calendar)value);
                    break;

                case HeaderSetImpl.TYPE_SPECIAL_TIME_4:
                    encodeInt(((Calendar)value).getTime().getTime() / 1000L);
                    break;

                case HeaderSetImpl.TYPE_SPECIAL_TYPE:
                    byte[] str = ((String)value).getBytes("ISO-8859-1");

                    // head + len16 + bytes[length] + zero8
                    encodeLength16(str.length + 4);
                    System.arraycopy(str, 0, buffer, packetLength,
                            str.length);
                    packetLength += str.length;
                    // null terminator required by protocol spec
                    buffer[packetLength++] = 0;
                    break;

                case HeaderSetImpl.TYPE_AUTH_CHALLENGE:
                    packetLength--;
                    Vector challenges = (Vector) value;

                    stream.authFailed = false;

                    for (int i = 0; i < challenges.size(); i++) {
                        ObexAuth auth = (ObexAuth) challenges.elementAt(i);
                        packetLength += auth.addChallenge(buffer, packetLength);
                    }

                    // need to be shure - challenges are added
                    if (packetLength > maxSendLength) {
                        break;
                    }

                    // if we still there, then authChallenges
		    // successfully added
                    for (int i = 0; i < challenges.size(); i++) {
                        stream.authFailed = true;
                        stream.authChallenges.addElement(
                                challenges.elementAt(i));
                    }
                    break;

                    // normal ids

                    // 4 byte integer values, range (0xC0 - 0xFF)
                    // system and user defined
                case HeaderSetImpl.TYPE_LONG:
                    encodeInt(((Long)value).longValue());
                    break;

                    // unicode encoded string values, range (0x00 - 0x3F)
                    // system and user defined
                case HeaderSetImpl.TYPE_UNICODE:
                    str = ((String)value).getBytes("UTF-16BE");
                    // str = ((String)value).getBytes("ISO-8859-1");

                    // head + len16 + bytes[length] + zero16
                    encodeLength16(str.length + 5);
                    System.arraycopy(str, 0, buffer, packetLength,
                            str.length);
                    packetLength += str.length;
                    // end of unicode string
                    buffer[packetLength++] = 0;
                    buffer[packetLength++] = 0;
                    break;

                    // byte[] array values, range (0x40 - 0x7F)
                    // system and user defined
                case HeaderSetImpl.TYPE_BYTEARRAY:
                    byte[] array = (byte[]) value;

                    // head + len16 + bytes[length]
                    encodeLength16(array.length + 3);
                    System.arraycopy(array, 0, buffer, packetLength,
                            array.length);
                    packetLength += array.length;
                    stream.containsTargetHeader |=
                        (type == HeaderSetImpl.TARGET);
                    break;

                    // byte values, range (0x80 - 0xBF)
                    // user defined
                case HeaderSetImpl.TYPE_BYTE:
                    buffer[packetLength++] =
                        (byte)((Byte)value).byteValue();
                    break;

                    // case HeaderSetImpl.TYPE_UNSUPPORTED:
                default:
                    if (DEBUG) {
                        System.out.println("wrong or unsupported header");
                    }
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.toString());
        } catch (ArrayIndexOutOfBoundsException e) {
            stream.moreHeaders = false;
            return false;
        }
        if (packetLength > maxSendLength) {
            stream.moreHeaders = false;
            return false;
        }

        // finally closing body headers if opened
        if (stream.dataOpened) {
            int dataOffset = stream.dataOffset;
            int len = stream.packetLength - dataOffset;
            buffer[dataOffset+1] = (byte)(len >> 8);
            buffer[dataOffset+2] = (byte)len;
            stream.dataOpened = false;
            stream.dataClosed = true;
        }
        stream.packetLength = packetLength;

        // gc header value
        this.value = null;
        if (DEBUG) {
            System.out.println("  -> sent!");
        }
        return true;
    }

    private void newHeader() {
        stream.queuedHeaders.addElement(this);
        if (stream.emptyHeadersPool.empty()) {
            stream.newHeader = new QueuedHeader(stream);
        } else {
            stream.newHeader = (QueuedHeader) stream.emptyHeadersPool.pop();
        }
    }

    boolean sendAllQueued() throws IOException {
        while (stream.queuedHeaders.size() > 0 && stream.moreHeaders) {
            QueuedHeader header = (QueuedHeader)
                stream.queuedHeaders.firstElement();
            boolean res = header.trySendAgain();
            if (!res) {
                return false;
            }
            stream.queuedHeaders.removeElementAt(0);
        }
        return true;
    }

    private final void encodeInt(long val) {
        buffer[packetLength++] = (byte)(val >> 24);
        buffer[packetLength++] = (byte)(val >> 16);
        buffer[packetLength++] = (byte)(val >> 8);
        buffer[packetLength++] = (byte)val;
    }

    private final void encodeLength16(int value) {
        buffer[packetLength++] = (byte)(value >> 8);
        buffer[packetLength++] = (byte)value;
    }

    private final void encodeTime8601(Calendar cal) {
        encodeLength16(19);

        // copy calendar as it can be with wrong timezone.
        Calendar cal2 = Calendar.getInstance(ObexPacketStream.utcTimeZone);
        cal2.setTime(cal.getTime());
        int year = cal2.get(Calendar.YEAR);
        int month = cal2.get(Calendar.MONTH) + 1; // Calendar.JANUARY = 0
        int date = cal2.get(Calendar.DATE);
        int hour = cal2.get(Calendar.HOUR_OF_DAY);
        int minute = cal2.get(Calendar.MINUTE);
        int second = cal2.get(Calendar.SECOND);
        int zero = 0x30; // zero ('0') code in latin1

        if (year < 0 || year > 9999) {
            if (DEBUG) {
                System.out.println("wrong date header");
            }
        }
        buffer[packetLength++] = (byte)(year / 1000 + zero);
        year = year % 1000;
        buffer[packetLength++] = (byte)(year / 100 + zero);
        year = year % 100;
        buffer[packetLength++] = (byte)(year / 10 + zero);
        year = year % 10;
        buffer[packetLength++] = (byte)(year + zero);
        buffer[packetLength++] = (byte)(month / 10 + zero);
        buffer[packetLength++] = (byte)(month % 10 + zero);
        buffer[packetLength++] = (byte)(date / 10 + zero);
        buffer[packetLength++] = (byte)(date % 10 + zero);
        buffer[packetLength++] = 0x54; // 'T' code in latin1
        buffer[packetLength++] = (byte)(hour / 10 + zero);
        buffer[packetLength++] = (byte)(hour % 10 + zero);
        buffer[packetLength++] = (byte)(minute / 10 + zero);
        buffer[packetLength++] = (byte)(minute % 10 + zero);
        buffer[packetLength++] = (byte)(second / 10 + zero);
        buffer[packetLength++] = (byte)(second % 10 + zero);
        buffer[packetLength++] = (byte)0x5A; // 'Z' code in latin1
    }
}


