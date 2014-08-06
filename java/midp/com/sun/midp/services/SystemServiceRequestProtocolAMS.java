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

final class SystemServiceRequestProtocolAMS {
    final static int SERVICE_REQUEST_STATUS_OK = 0;
    final static int SERVICE_REQUEST_STATUS_ERROR = 1;

    private final static int INVALID_STATE = -1;
    private final static int WAIT_FOR_BEGIN_SESSION_STATE = 1;
    private final static int WAIT_FOR_SERVICE_ID_STATE = 2;
    private final static int SEND_SERVICE_REQUEST_STATUS_STATE = 3;
    private final static int SEND_SERVICE_TO_CLIENT_LINK_STATE = 4;
    private final static int SEND_CLIENT_TO_SERVICE_LINK_STATE = 5;
    private final static int WAIT_FOR_LINKS_RECEIVED_ACK_STATE = 6;
    private final static int WAIT_FOR_END_SESSION_STATE = 7;
    private final static int END_SESSION_STATE = 8;
    private final static int END_STATE = 9;

    private SystemServiceRequestListener requestListener;

    private int state = INVALID_STATE; 

    SystemServiceRequestProtocolAMS(
            SystemServiceRequestListener requestListener) {

        /**
         * Argument sanity check
         */
        if (requestListener == null) {
            throw new NullPointerException();
        }

        this.requestListener = requestListener;
    }

    SystemServiceConnectionLinks handleServiceRequest(
            SystemServiceConnectionLinks sendReceiveLinks)
        throws ClosedLinkException, 
               InterruptedIOException, 
               IOException {

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

        SystemServiceConnectionLinks connectionLinks = null;

        state = WAIT_FOR_BEGIN_SESSION_STATE;
        try {
            connectionLinks = doHandleServiceRequest(sendLink, receiveLink);
        } finally {
            state = INVALID_STATE;
        }

        return connectionLinks;
    }

    private SystemServiceConnectionLinks doHandleServiceRequest(
            Link sendLink, Link receiveLink) 
        throws ClosedLinkException, 
               InterruptedIOException, 
               IOException {

        String serviceID = "";
        SystemServiceConnectionLinks connectionLinks = null;

        if (state == INVALID_STATE) {
            throw new IllegalStateException();
        }

        while (state != END_STATE) {
            switch (state) {
                case WAIT_FOR_BEGIN_SESSION_STATE: {
                    // wait for session begin request 
                    LinkMessage msg = receiveLink.receive();
                    String str = msg.extractString();

                    // check request validity
                    if (!str.equals(
                      SystemServiceRequestProtocolClient.START_SESSION_STR)) {
                        throw new IllegalStateException();
                    }

                    // advance to next state
                    state = WAIT_FOR_SERVICE_ID_STATE;
                    break;
                }

                case WAIT_FOR_SERVICE_ID_STATE: {
                    // wait for service id
                    LinkMessage msg = receiveLink.receive();
                    serviceID = msg.extractString();

                    // advance to next state
                    state = SEND_SERVICE_REQUEST_STATUS_STATE; 
                    break;
                }

                case SEND_SERVICE_REQUEST_STATUS_STATE: {
                    // try to obtain connection to service
                    int status = SERVICE_REQUEST_STATUS_OK;
                    try {
                        connectionLinks = null;
                        connectionLinks = 
                            requestListener.onServiceRequest(serviceID);
                    } finally {
                        if (connectionLinks == null) {
                            status = SERVICE_REQUEST_STATUS_ERROR;
                        }

                        // send status
                        ByteArrayOutputStream bos = 
                            new ByteArrayOutputStream();
                        DataOutputStream os = new DataOutputStream(bos);
                        os.writeInt(status);
                        byte[] data = bos.toByteArray();

                        LinkMessage msg = LinkMessage.newDataMessage(data);
                        sendLink.send(msg);

                        // advance to next state
                        if (status == SERVICE_REQUEST_STATUS_OK) {
                            state = SEND_SERVICE_TO_CLIENT_LINK_STATE;
                        } else {
                            state = END_STATE;
                        }
                    }

                    break;
                }

                case SEND_SERVICE_TO_CLIENT_LINK_STATE: {
                    // send service to client link
                    Link link = connectionLinks.getSendLink();
                    LinkMessage msg = LinkMessage.newLinkMessage(link);
                    sendLink.send(msg);

                    // advance to next state
                    state = SEND_CLIENT_TO_SERVICE_LINK_STATE; 
                    break;
                }

                case SEND_CLIENT_TO_SERVICE_LINK_STATE: {
                    // send client to service link
                    Link link = connectionLinks.getReceiveLink();
                    LinkMessage msg = LinkMessage.newLinkMessage(link);
                    sendLink.send(msg);

                    // advance to next state
                    state = WAIT_FOR_LINKS_RECEIVED_ACK_STATE;
                    break;
                }

                case WAIT_FOR_LINKS_RECEIVED_ACK_STATE: {
                    // wait for links recieved ack
                    LinkMessage msg = receiveLink.receive();
                    String str = msg.extractString();

                    // check request validity
                    if (!str.equals(SystemServiceRequestProtocolClient.
                                LINKS_RECEIVED_ACK_STR)) {
                        throw new IllegalStateException();
                    }

                    // notify listener about connection passed to client
                    if (connectionLinks != null) {
                        requestListener.onLinksPassedToClient(connectionLinks);
                    }
                                                           
                    // advance to next state
                    state = WAIT_FOR_END_SESSION_STATE;
                    break;
                }

                case WAIT_FOR_END_SESSION_STATE: {
                    // wait for session end request
                    LinkMessage msg = receiveLink.receive();
                    String str = msg.extractString();

                    // check request validity
                    if (!str.equals(
                      SystemServiceRequestProtocolClient.END_SESSION_STR)) {
                        throw new IllegalStateException();
                    }

                    // advance to next state
                    state = END_SESSION_STATE;
                    break;
                }

                case END_SESSION_STATE: {
                    // advance to next state
                    state = END_STATE;
                    break;
                }
            }
        }

        state = INVALID_STATE;

        return connectionLinks;
    }
}

