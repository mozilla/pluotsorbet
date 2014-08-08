/*
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
package com.sun.midp.io.j2me.pipe;

import com.sun.midp.io.j2me.pipe.serviceProtocol.PipeServiceProtocol;
import com.sun.midp.io.pipe.PipeServerConnection;
import java.io.IOException;
import javax.microedition.io.StreamConnection;
import com.sun.midp.security.SecurityToken;

/**
 * Implementation of PipeServerConnection interface. Uses Links as bearer. Uses
 * com.sun.midp.io.j2me.pipe.serviceProtocol.* for organazing messaging over
 * bearer.
 */
class PipeServerConnectionImpl implements PipeServerConnection {
    private String name;
    private String version;
    private SecurityToken token;
    private PipeServiceProtocol pipe;
    private boolean requestedToClose = false;
    private int mode;

    PipeServerConnectionImpl(String serverName, String serverVersion, SecurityToken token) {
        name = serverName;
        version = serverVersion;
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public StreamConnection acceptAndOpen() throws IOException {
        if (requestedToClose)
            throw new IOException("Connection closed");
        
        PipeServiceProtocol ptp = pipe.acceptByServer();
        PipeClientConnectionImpl conn = new PipeClientConnectionImpl(token, ptp);
        conn.establishTransfer(mode);
        
        return conn;
    }

    public void close() throws IOException {
        pipe.closeServer();
        
        requestedToClose = true;
    }

    /**
     * Establishes connection with Pipe system service. Registers this
     * pipe server there so client connection can be bound when opened
     * @param mode
     */
    void establish(int mode) throws IOException {
        pipe = PipeServiceProtocol.getService(token);
        
        pipe.bindServer(name, version);

        this.mode = mode;
    }

}
