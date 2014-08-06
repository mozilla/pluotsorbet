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

package com.sun.midp.services;

import com.sun.midp.links.*;
import java.io.*;

final class SystemServiceRequestProtocolClient {
    final static String START_SESSION_STR = 
        "Starting service request";
    final static String END_SESSION_STR = 
        "Finishing service request";
    final static String LINKS_RECEIVED_ACK_STR = 
        "Links received";

    private final static int INVALID_STATE = -1;
    private final static int BEGIN_SESSION_STATE = 1;
    private final static int SEND_SERVICE_ID_STATE = 2;
    private final static int WAIT_FOR_STATUS_STATE = 3;
    private final static int WAIT_FOR_SERVICE_TO_CLIENT_LINK_STATE = 4;
    private final static int WAIT_FOR_CLIENT_TO_SERVICE_LINK_STATE = 5;
    private final static int SEND_LINKS_RECEIVED_ACK_STATE = 6;
    private final static int END_SESSION_STATE = 7;
    private final static int END_STATE = 8;

    private LinkMessage startSessionMsg = null;
    private LinkMessage endSessionMsg = null;
    private LinkMessage linksReceivedMsg = null;

    private SystemServiceConnectionLinks sendReceiveLinks = null;
    private SystemServiceConnectionLinks connectionLinks = null;

    private int state = INVALID_STATE;

    SystemServiceRequestProtocolClient(SystemServiceConnectionLinks 
            sendReceiveLinks) {

        Link sendLink = sendReceiveLinks.getSendLink();
        Link receiveLink = sendReceiveLinks.getReceiveLink();

        /**
         * Arguments sanity checks
         */
        if (sendLink == null || receiveLink == null) {
            throw new NullPointerException();
        }

        if (!sendLink.isOpen() || !receiveLink.isOpen()) {
            throw new IllegalStateException();
        }

        this.sendReceiveLinks = sendReceiveLinks;

        this.startSessionMsg = LinkMessage.newStringMessage(START_SESSION_STR);
        this.endSessionMsg = LinkMessage.newStringMessage(END_SESSION_STR);
        this.linksReceivedMsg = LinkMessage.newStringMessage(
                LINKS_RECEIVED_ACK_STR);
    }

    void requestService(String serviceID) 
        throws ClosedLinkException, 
               InterruptedIOException, 
               IOException {

        connectionLinks = null;
        state = BEGIN_SESSION_STATE;

        try {
            doRequestService(serviceID);
        } finally {
            state = INVALID_STATE; 
        }
    }

    SystemServiceConnectionLinks getSystemServiceConnectionLinks() {
        return connectionLinks;
    }


    private void doRequestService(String serviceID) 
        throws ClosedLinkException, 
               InterruptedIOException, 
               IOException {

        Link sendLink = sendReceiveLinks.getSendLink();
        Link receiveLink = sendReceiveLinks.getReceiveLink();

        Link c2sLink = null;
        Link s2cLink = null;

        if (state == INVALID_STATE) {
            throw new IllegalStateException();
        }

        while (state != END_STATE) {
            switch (state) {
                case BEGIN_SESSION_STATE: {
                    // start session
                    sendLink.send(startSessionMsg);

                    // advance to next state
                    state = SEND_SERVICE_ID_STATE;
                    break;
                }

                case SEND_SERVICE_ID_STATE: {
                    // send service ID
                    LinkMessage msg = LinkMessage.newStringMessage(serviceID);
                    sendLink.send(msg);
                       
                    // advance to next state
                    state = WAIT_FOR_STATUS_STATE;
                    break;
                }

                case WAIT_FOR_STATUS_STATE: {
                    // wait for status reply
                    LinkMessage msg = receiveLink.receive();

                    // extract status 
                    byte[] data = msg.extractData();
                    ByteArrayInputStream bis = new ByteArrayInputStream(data);
                    DataInputStream is = new DataInputStream(bis);
                    int status = is.readInt();

                    // advance to next state
                    if (status == SystemServiceRequestProtocolAMS.SERVICE_REQUEST_STATUS_OK) {
                        state = WAIT_FOR_SERVICE_TO_CLIENT_LINK_STATE;
                    } else {
                        state = END_STATE;
                    }
                    
                    break;
                }

                case WAIT_FOR_SERVICE_TO_CLIENT_LINK_STATE: {
                    // wait for link
                    LinkMessage msg = receiveLink.receive();
                    s2cLink = msg.extractLink();

                    // check link state
                    if (!s2cLink.isOpen()) {
                        throw new IllegalStateException();
                    }

                    // advance to next state
                    state = WAIT_FOR_CLIENT_TO_SERVICE_LINK_STATE;
                    break;
                }

                case WAIT_FOR_CLIENT_TO_SERVICE_LINK_STATE: {
                    // wait for link
                    LinkMessage msg = receiveLink.receive();
                    c2sLink = msg.extractLink();

                    // check link state
                    if (!c2sLink.isOpen()) {
                        throw new IllegalStateException();
                    }

                    // advance to next state
                    state = SEND_LINKS_RECEIVED_ACK_STATE; 
                    break;
                }

                case SEND_LINKS_RECEIVED_ACK_STATE: {
                    // acknowledge links reception
                    sendLink.send(linksReceivedMsg);

                    // advance to next state
                    state = END_SESSION_STATE;
                    break;
                }

                case END_SESSION_STATE: {
                    // close session
                    sendLink.send(endSessionMsg);

                    if (c2sLink != null && s2cLink != null) {
                        connectionLinks = new SystemServiceConnectionLinks(
                                c2sLink, s2cLink);
                    }

                    // advance to next state
                    state = END_STATE; 
                    break;
                }
            }
        }

        state = INVALID_STATE;
    }
}
