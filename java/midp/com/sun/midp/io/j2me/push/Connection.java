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

package com.sun.midp.io.j2me.push;

/**
 * Connection string parser.
 */
public final class Connection {
    /**
     * Protocol name.
     *
     * <p>
     * <strong>Invariant</strong>: never <code>null</code> or empty.
     * </p>
     */
    private final String protocol;

    /**
     * Target.
     *
     * <p>
     * <strong>Invariant</strong>: never <code>null</code>.
     * </p>
     */
    private final String target;

    /**
     * Params.
     */
    private final String params;

    /**
     * Constructs a connection.
     *
     * @param protocol protocol
     * @param target target
     * @param params params
     */
    private Connection(
            final String protocol,
            final String target,
            final String params) {
        // assert (protocol != null) && (protocol.length() > 0);
        this.protocol = protocol;
        // assert (target != null);
        this.target = target;
        this.params = params;
    }

    /**
     * Gets a protocol.
     *
     * <p>
     * <strong>Invariant</strong>: never <code>null</code> or empty.
     * </p>
     *
     * @return protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Gets a target.
     *
     * <p>
     * <strong>Invariant</strong>: never <code>null</code>.
     * </p>
     *
     * @return target
     */
    public String getTarget() {
        return target;
    }

    /**
     * Gets params.
     *
     * <p>
     * Params (if any) are prefixed with ';'.
     * </p>
     *
     * @return params
     */
    public String getParams() {
        return params;
    }

    /**
     * Returns original, unmodifed connection string.
     *
     * @return connection string
     */
    public String getConnection() {
        final String s = protocol + ":" + target;
        return (params != null) ? s + params : s;
    }

    /**
     * Parses a connection string.
     *
     * @param connection connection string to parse
     *
     * @return Connection object
     *
     * @throws IllegalArgumentException if connection is invalid
     */
    public static Connection parse(final String connection)
            throws IllegalArgumentException {
        if (connection == null) {
            throw new IllegalArgumentException("connection is null");
        }

        // Parse a connection string partially (fetching scheme and the rest)
        // Actually, I'd rather factor it into a class
        final int colonPos = connection.indexOf(':');
        if (colonPos == -1) {
            throw new IllegalArgumentException("connection is invalid");
        }
        if (colonPos < 1) {
            throw new IllegalArgumentException("invalid protocol");
        }

        final String protocol = connection.substring(0, colonPos);
        final String targetAndParams = connection.substring(colonPos + 1);

        final int paramsPos = targetAndParams.indexOf(';');
        if (paramsPos == -1) {
            return new Connection(protocol, targetAndParams, null);
        } else {
            final String target = targetAndParams.substring(0, paramsPos);
            final String params = targetAndParams.substring(paramsPos);
            return new Connection(protocol, target, params);
        }
    }
}
