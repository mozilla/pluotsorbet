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
import java.util.Date;
import javax.wireless.messaging.*;
import javax.microedition.io.*;
import com.sun.midp.io.j2me.sms.*;

/**
 * Implements a SMS message for the SMS message connection.
 * <p>
 * This class contains methods for manipulating message objects and their
 * contents. Messages can be composed of data and an address.
 * <code>MessageObject</code> contains methods that can get and set the data and
 * the address parts of a message separately. The data part can be either text
 * or binary format. The address part has the format:
 * <p>
 * <code>sms://[<em>phone_number</em>:][<em>port_number</em>]</code>
 * <p>
 * and represents the address of a port that can accept or receive SMS messages.
 * <p>
 * Port numbers are used to designate a specific application or communication
 * channel for messages. When the port number is omitted from the address, then
 * the message is targeted at the end user and their normal mailbox handling
 * application. In this case, the JSR120 <code>MessageConnection</code> cannot
 * be used to receive an inbound message to the user mailbox.
 * <p>
 * A well-written application would always check the number of segments that 
 * would be used before sending a message, since the user is
 * paying for each SMS message transferred and not just the fixed rate per 
 * high-level message sent.</p>
 * <h2>Instantiating and Freeing MessageObjects</h2>
 * <p>
 * <code>MessageObject</code>s are instantiated when they are received from the
 * {@link javax.wireless.messaging.MessageConnection MessageConnection}
 * or by using the
 * {@link MessageConnection#newMessage(String type)
 *  MessageConnection.newMessage}
 * message factory. Instances are freed when they are garbage collected or
 * when they go out of scope.
 */
public class MessageObject  implements Message {

    /** High level message type. */
    String messtype;

    /** High level message address. */
    String messaddr;

    /** Timestamp when the message was sent. */
    long sentAt;

    /**
     * Creates a <code>Message</code> object without a buffer.
     * @param type text or binary message type.
     * @param  addr the destination address of the message.
     *
     */
    public MessageObject(String type, String addr) {
	messtype = type;
	messaddr = addr;


    }

    /**
     * Gets the address from the message object as a <code>String</code>. If no
     * address is found in the message, this method returns
     * <code>null</code>. If the method
     * is applied to an inbound message, the source address is returned.
     * If it is applied to an outbound message, the destination addess
     * is returned.
     * <p>
     * The following code sample retrieves the address from a received
     *  message.
     * <pre>
     *    ...
     *    Message msg = conn.receive();
     *    String addr = msg.getAddress();
     *    ...
     * </pre>
     * @return the address in string form, or <code>null</code> if no
     *         address was set
     *
     * @see #setAddress
     */
    public String getAddress() {
	return messaddr;
    }

    /**
     * Sets the address part of the message object. The address is a
     * <code>String</code> and should be in the format:
     * <p>
     * <code>sms://[<em>phone_number</em>:][<em>port</em>]</code>
     * <p>
     * The following code sample assigns an SMS URL address to the
     * <code>Message</code> object.</p>
     * <pre>
     *    ...
     *    String addr = "sms://+358401234567";
     *    Message msg = newMessage(TEXT_MESSAGE);
     *    msg.setAddress(addr);
     *    ...
     * </pre>
     * <p>
     * @param addr the address of the target device
     *
     * @see #getAddress
     */
    public void setAddress(String addr) {

	messaddr = addr;
    }

    /**
     * Returns the timestamp indicating when this message has been
     * sent.
     *
     * @return the date indicating the timestamp in the message or
     *         <code>null</code> if the timestamp is not set
     * @see #setTimeStamp
     */
    public java.util.Date getTimestamp() {
        if (sentAt == 0) {
            return null;
        }
	return new Date(sentAt); 
    }

    /**
     * Sets the timestamp for inbound SMS messages.
     *
     * @param timestamp  the date indicating the timestamp in the message 
     * @see #getTimestamp
     */
    public void setTimeStamp(long timestamp) {
	sentAt = timestamp;
    }

}


