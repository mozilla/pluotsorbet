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

/**
 * Connection between service and client allowing to pass messages 
 * between them. Connection stays open until client Isolate exits. 
 * When connection is closed, service will be notified to give it 
 * a chance to perform necessary cleanup.
 */
public interface SystemServiceConnection {
    /**
     * Receives a message. Blocks until there is a message to receive.
     * On client side, it receives message from service. On service side,
     * it receives message from client.
     *
     * @return received message
     */
    public SystemServiceMessage receive() 
        throws SystemServiceConnectionClosedException;

    /**
     * Sends a message. Blocks until message is received. On client side,
     * it sends message to service. On service side, it sends message 
     * to client.
     *
     * @param msg message to send
     */
    public void send(SystemServiceMessage msg) 
        throws SystemServiceConnectionClosedException;

    /**
     * Sets a listener which will be notified when message has arrived.
     *
     * @param listener listener to notify. if null, removes current
     * listener.
     */
    public void setConnectionListener(SystemServiceConnectionListener 
            listener);
}
