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

import javax.obex.Authenticator;
import javax.obex.SessionNotifier;
import javax.obex.ServerRequestHandler;
import javax.microedition.io.Connection;
import java.io.IOException;

public class SessionNotifierImpl implements SessionNotifier {

    private ObexTransportNotifier notifier;

    public SessionNotifierImpl(ObexTransportNotifier notifier)
            throws IOException {
        this.notifier = notifier;
    }

    public Connection acceptAndOpen(ServerRequestHandler handler)
        throws IOException {
        return acceptAndOpen(handler, null);
    }

    public Connection acceptAndOpen(ServerRequestHandler handler,
        Authenticator auth) throws IOException {
        if (notifier == null) {
            throw new IOException("session closed");
        }
        if (handler == null) {
            throw new NullPointerException("null handler");
        }
        ObexTransport transport = notifier.acceptAndOpen();
        return new ServerConnectionImpl(transport, handler, auth);
    }

    public void close() throws IOException {
        if (notifier != null) notifier.close();
        notifier = null;
    }

    public Connection getTransport() {
        return notifier.getUnderlyingConnection();
    }
}
