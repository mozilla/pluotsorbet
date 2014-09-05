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

package com.sun.midp.io.j2me.sms;

import java.io.*;
import javax.wireless.messaging.*;
import javax.microedition.io.*;
import com.sun.midp.io.j2me.sms.*;

/**
 * Implements an instance of a text message.
 */
public class TextObject extends MessageObject
    implements TextMessage {

    /** Buffer to be used. */
    byte[] buffer;

    /**
     * Constructs a text-specific message.
     * @param  addr the destination address of the message
     */
    public TextObject(String addr) {
	super(MessageConnection.TEXT_MESSAGE, addr);
    }

    /**
     * Returns the message payload data as a <code>String</code>.
     *
     * @return the payload of this message, or <code>null</code>
     * if the payload for the message is not set
     * @see #setPayloadText
     */
    public String getPayloadText() {
	if (buffer == null) {
	    return null;
	}
	return TextEncoder.toString(buffer);
    }

    /**
     * Sets the payload data of this message. The payload data
     * may be <code>null</code>.
     * @param data payload data as a <code>String</code>
     * @see #getPayloadText
     */
    public void setPayloadText(String data) {
	if (data != null) {
	    buffer = TextEncoder.toByteArray(data);
	} else {
	    buffer = null;
	}
	return;
    }

    /**
     * Gets the raw byte array.
     * @return an array of raw UCS-2 payload data
     * @see #getPayloadText
     * @see #setBytes
     */
    public byte[] getBytes() {
	return buffer;
    }

    /**
     * Sets the raw byte array.
     * @param data an array of raw UCS-2 payload data.
     * @see #getBytes
     */
    void setBytes(byte[] data) {
	buffer = data;
    }
}


