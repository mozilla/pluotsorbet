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

package com.sun.midp.io.pipe;

import javax.microedition.io.StreamConnection;

/**
 * This interface defines pipe connection.
 * <p>
 * A pipe is accessed using a generic connection string with specified MIDlet name or wildcard.
 * For example <code>pipe://*:my_pipe:1.0;</code> defines a target pipe named "my_pipe", version 1.0,
 * opened by any MIDlet.
 * </p>
 * <p>
 * INPORTANT NOTE: only wildcard addressing is supported. Later on is planned to add support
 * for specifying exact MIDlet name.
 * </p>
 */
public interface PipeConnection extends StreamConnection {

    /**
     * Retrieves the pipe version requested in connection string.
     * @return pipe server version that was requested.
     */
    String getRequestedServerVersion();

    /**
     * Retrieves the pipe name requested.
     * @return pipe name that was requested.
     */
    String getServerName();
}
