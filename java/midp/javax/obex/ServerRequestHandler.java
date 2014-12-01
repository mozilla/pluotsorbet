/*
 *
 *
 * Portions Copyright  2000-2009 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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

/*
 * (c) Copyright 2001, 2002 Motorola, Inc.  ALL RIGHTS RESERVED.
 */
package javax.obex;
import com.sun.jsr082.obex.HeaderSetImpl;

/*
 * This class is defined by the JSR-82 specification
 * <em>Java&trade; APIs for Bluetooth&trade; Wireless Technology,
 * Version 1.1.</em>
 */
// JAVADOC COMMENT ELIDED
public class ServerRequestHandler {

    // JAVADOC COMMENT ELIDED
    private long connId;

    // JAVADOC COMMENT ELIDED
    protected ServerRequestHandler() {
        connId = -1;
    }

    // JAVADOC COMMENT ELIDED
    public final HeaderSet createHeaderSet() {
        return new HeaderSetImpl(HeaderSetImpl.OWNER_SERVER_USER);
    }

    // JAVADOC COMMENT ELIDED
    public void setConnectionID(long id) {
        if (id < -1L || id > 0xFFFFFFFFL) {
            throw new IllegalArgumentException("invalid id");
        }
        connId = id;
    }

    // JAVADOC COMMENT ELIDED
    public long getConnectionID() {
        return connId;
    }

    // JAVADOC COMMENT ELIDED
    public int onConnect(HeaderSet request, HeaderSet reply) {
        return ResponseCodes.OBEX_HTTP_OK;
    }

    // JAVADOC COMMENT ELIDED
    public void onDisconnect(HeaderSet request, HeaderSet reply) {
        // do nothing
    }

    // JAVADOC COMMENT ELIDED
    public int onSetPath(HeaderSet request, HeaderSet reply, boolean backup,
        boolean create) {
        return ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED;
    }

    // JAVADOC COMMENT ELIDED
    public int onDelete(HeaderSet request, HeaderSet reply) {
        return ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED;
    }

    // JAVADOC COMMENT ELIDED
    public int onPut(Operation op) {
        return ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED;
    }

    // JAVADOC COMMENT ELIDED
    public int onGet(Operation op) {
        return ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED;
    }

    // JAVADOC COMMENT ELIDED
    public void onAuthenticationFailure(byte[] userName) {
        // do nothing
    }
}
