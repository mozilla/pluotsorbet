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

final class SystemServiceRequestorRemote extends SystemServiceRequestor {
    private SystemServiceConnectionLinks sendReceiveLinks = null;
    private SystemServiceRequestProtocolClient requestProtocol = null;

    SystemServiceRequestorRemote(
            SystemServiceConnectionLinks sendReceiveLinks) {

        this.sendReceiveLinks = sendReceiveLinks;
        if (sendReceiveLinks != null) {
            this.requestProtocol = 
                new SystemServiceRequestProtocolClient(sendReceiveLinks);
        }
    }

    public SystemServiceConnection requestService(String serviceID) {
        synchronized (this) {
            return doRequestService(serviceID);
        }
    }

    private SystemServiceConnection doRequestService(String serviceID) {
        if (sendReceiveLinks == null) {
            return null;
        }

        try {
            // send empty message to kick a session 
            Link sendLink = sendReceiveLinks.getSendLink();
            LinkMessage emptyMsg = LinkMessage.newStringMessage("");
            sendLink.send(emptyMsg);

            requestProtocol.requestService(serviceID);
            SystemServiceConnectionLinks connectionLinks = 
                requestProtocol.getSystemServiceConnectionLinks();

            if (connectionLinks != null) {
                return new SystemServiceConnectionImpl(connectionLinks);
            } else {
                return null;
            }
        } catch (ClosedLinkException e) {
            sendReceiveLinks.close();
            sendReceiveLinks = null;

            return null;
        } catch (InterruptedIOException e) {
            sendReceiveLinks.close();
            sendReceiveLinks = null;

            return null;
        } catch (IOException e) {
            sendReceiveLinks.close();
            sendReceiveLinks = null;

            return null;            
        } catch (IllegalStateException e) {
            sendReceiveLinks.close();
            sendReceiveLinks = null;

            return null;            
        }
    }
}
