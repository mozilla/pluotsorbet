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

final class SystemServiceConnectionImpl 
    implements SystemServiceConnection {
    
    private SystemServiceConnectionLinks connectionLinks = null;
    private SystemServiceConnectionListener listener = null;

    class ListenerThread extends Thread {
        public void run() {
            try {
                while (true) {
                    SystemServiceMessage msg = receive();
                    listener.onMessage(msg);
                }
            } catch ( SystemServiceConnectionClosedException e) {
                listener.onConnectionClosed();
            }
        }
    }

    SystemServiceConnectionImpl(SystemServiceConnectionLinks connectionLinks) {
        this.connectionLinks = connectionLinks;
    }

    public SystemServiceMessage receive() 
        throws SystemServiceConnectionClosedException {

        try { 
            Link receiveLink = connectionLinks.getReceiveLink();
            LinkMessage msg = receiveLink.receive(); 
            if (msg.containsData()) {
                return new SystemServiceReadMessage(msg.extractData());
            } else {
                return new SystemServiceLinkReadMessage(msg.extractLink());
            }
        } catch (ClosedLinkException e) {
            throw new SystemServiceConnectionClosedException();
        } catch (InterruptedIOException e) {
            connectionLinks.close();
            throw new SystemServiceConnectionClosedException();
        } catch (IOException e) {
            connectionLinks.close();
            throw new SystemServiceConnectionClosedException();
        }
    }

    public void send(SystemServiceMessage msg) 
        throws SystemServiceConnectionClosedException {

        try {
            LinkMessage linkMsg;
            if (msg instanceof SystemServiceWriteMessage) {
                SystemServiceWriteMessage writeMsg = (SystemServiceWriteMessage)msg;
                byte[] data = writeMsg.getData();
                linkMsg = LinkMessage.newDataMessage(data);
            } else {
                SystemServiceLinkWriteMessage writeMsg = (SystemServiceLinkWriteMessage)msg;
                Link link = writeMsg.getLinkInternal();
                linkMsg = LinkMessage.newLinkMessage(link);
            }
            Link sendLink = connectionLinks.getSendLink();
            sendLink.send(linkMsg);
        } catch (ClosedLinkException e) {
            throw new SystemServiceConnectionClosedException();
        } catch (InterruptedIOException e) {
            connectionLinks.close();
            throw new SystemServiceConnectionClosedException();
        } catch (IOException e) {
            connectionLinks.close();
            throw new SystemServiceConnectionClosedException();
        }        
    }

    public void setConnectionListener(SystemServiceConnectionListener l) {
        listener = l;
        new ListenerThread().start();
    }
}
