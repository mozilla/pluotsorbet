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

package javax.microedition.midlet;

import java.lang.String;

/**
 * Signals that a requested <code>MIDlet</code> state change failed. This
 * exception is thrown by the <code>MIDlet</code> in response to
 * state change calls
 * into the application via the <code>MIDlet</code> interface
 *
 * @see MIDlet
 * @since MIDP 1.0
 */

public class MIDletStateChangeException extends Exception {

    /**
     * Constructs an exception with no specified detail message.
     */

    public MIDletStateChangeException() {}

    /**
     * Constructs an exception with the specified detail message.
     *
     * @param s the detail message
     */

    public MIDletStateChangeException(String s) {
	super(s);
    }

}
