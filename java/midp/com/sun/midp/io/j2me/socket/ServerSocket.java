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

package com.sun.midp.io.j2me.socket;

import java.io.IOException;
import java.io.InterruptedIOException;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import javax.microedition.io.ConnectionNotFoundException;

import com.sun.midp.security.SecurityToken;

/** Enables the server socket package to be optional. */
public interface ServerSocket {
    /**
     * Opens a port to listen on.
     *
     * @param port       TCP to listen on
     *
     * @exception IOException  if some other kind of I/O error occurs
     * @exception SecurityException if the current MIDlet suite does not have
     *            permission to open a server socket
     */
    void open(int port) throws IOException;
    /**
     * Opens a port to listen on. For privileged use only.
     *
     * @param port TCP to listen on; if less than or equal to zero, a
     *             port will be assigned automatically
     * @param token the security token
     * 
     * @exception IOException  if some other kind of I/O error occurs
     * @exception SecurityException if the token is invalid
     */
    public void open(int port, SecurityToken token) throws IOException;

}
