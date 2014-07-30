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

package com.sun.midp.midlet;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * This is the interface to "tunnel" across Java package namespace,
 * and call the protected methods in the another package namespace.
 * The callee package will implement this interface, and provide
 * a static utility instance to the caller package.
 */
public interface MIDletTunnel {
    /**
     * Returns the MIDletPeer object corresponding to the given
     * midlet instance.
     *
     * @param m MIDlet instance
     *
     * @return associated MIDletPeer instance
     */
    public MIDletPeer getMIDletPeer(MIDlet m);

    /**
     * Calls the startApp method on the midlet instance.
     *
     * @param m MIDlet instance
     *
     * @exception javax.microedition.midlet.MIDletStateChangeException  
     *     is thrown if the <code>MIDlet</code> cannot start now but 
     *     might be able to start at a later time.
     */
    public void callStartApp(MIDlet m) 
        throws MIDletStateChangeException;

    /**
     * Calls the pauseApp method on the midlet instance.
     *
     * @param m MIDlet instance
     */
    public void callPauseApp(MIDlet m);

    /**
     * Calls the destroyApp method on the midlet instance.
     *
     * @param m MIDlet instance
     * @param unconditional the flag to pass to destroy
     *
     * @exception javax.microedition.midlet.MIDletStateChangeException 
     *     is thrown if the <code>MIDlet</code> wishes to continue 
     *     to execute (Not enter the <em>Destroyed</em> state).
     *     This exception is ignored if <code>unconditional</code>
     *     is equal to <code>true</code>.
     */
    public void callDestroyApp(MIDlet m, boolean unconditional) 
        throws MIDletStateChangeException;
}

