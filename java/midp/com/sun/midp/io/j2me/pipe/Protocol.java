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

package com.sun.midp.io.j2me.pipe;

import java.io.IOException;
import javax.microedition.io.Connection;

import com.sun.cldc.io.ConnectionBaseInterface;
import com.sun.cldc.isolate.Isolate;
import com.sun.midp.io.j2me.pipe.serviceProtocol.PipeServiceProtocol;
import com.sun.midp.security.ImplicitlyTrustedClass;
import com.sun.midp.security.SecurityInitializer;
import com.sun.midp.security.SecurityToken;

public class Protocol implements ConnectionBaseInterface {
    private static class SecurityTrusted implements ImplicitlyTrustedClass {};
    private static SecurityToken token;
    private static Isolate currentIsolate;

    private static final boolean DEBUG = false;

    public Connection openPrim(String name, int mode, boolean timeouts) throws IOException {
        if (name.charAt(0) != '/' || name.charAt(1) != '/')
            throw new IllegalArgumentException(
                      "Protocol must start with \"//\"");

        // server. format is: "pipe://:server-name:server-version;"
        // client. format is: "pipe://(suite-id|*):server-name:server-version;"
        // suite-id is midlet suite's "vendor:name:version" triplet
        int colon2 = name.lastIndexOf(':');
        int colon1 = name.lastIndexOf(':', colon2-1);
        int semicolon = name.lastIndexOf(';');
        if (colon1 < 0 || semicolon < name.length() - 1)
            throw new IllegalArgumentException("Malformed server protocol name");
        String serverName = name.substring(colon1, colon2);
        String version = name.substring(colon2 + 1, semicolon);
        
        if (token == null) {
            token = SecurityInitializer.requestToken(new SecurityTrusted());
        }

        if (currentIsolate == null)
            throw new IllegalStateException();

        PipeServiceProtocol.setCurrentIsolate(currentIsolate);

        // check if we deal with server or client connection
        if (colon1 == 2) {
            // check if this is AMS isolate and opens connection for push purposes
            //       or this is user isolate and it needs to checkout connection from AMS
            // TODO. no push for now

            PipeServerConnectionImpl connection =
                    new PipeServerConnectionImpl(serverName, version, token);
            connection.establish(mode);
            return connection;
        } else {
            Object suiteId = null;
            if (name.charAt(2) == '*') {
                if (colon1 != 3)
                    throw new IllegalArgumentException("Malformed protocol name");
            } else {
                // TODO parse suite identity into suiteId object
                // suiteId might change its type
            }
            
            PipeClientConnectionImpl connection = 
                    new PipeClientConnectionImpl(suiteId, serverName, version, token);
            connection.establish(mode);
            
            return connection;
        }
    }

    /**
     * Registers pipe service with System Service API. To be used only in context of service task
     * (e.g. AMS Isolate).
     *
     * @param token priviledged instance of SecurityToken
     */
    public static void registerService(SecurityToken token) {
        PipeServiceProtocol.registerService(token);
    }

    /**
     * Initializes pipe service in context of user MIDlet (e.g. Isolate user MIDlet
     * is about to start in).
     */
    public static void initUserContext() {
        currentIsolate = Isolate.currentIsolate();
    }
}
