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

import com.sun.midp.midlet.MIDletPeer;

/** 
 * This is an implementation of the MIDletTunnel interface, to allow
 * com.sun.midp.midlet.MIDletState instance to call protected and package
 * private methods of javax.microedition.midlet.MIDlet.
 */
public class MIDletTunnelImpl {
    /**
     * Returns the MIDletPeer object corresponding to the given
     * midlet instance.
     *
     * @param m MIDlet instance
     * @return associated MIDletState instance
     */
    public static MIDletPeer getMIDletPeer(MIDlet m) {
        return m.getMIDletPeer();
    }
  
    /**
     * Calls the startApp method on the midlet instance.
     *
     * @param m MIDlet instance
     *
     * @exception javax.microedition.midlet.MIDletStateChangeException  
     *     is thrown if the <code>MIDlet</code> cannot start now but 
     *     might be able to start at a later time.
     */
    public static void callStartApp(MIDlet m) 
        throws MIDletStateChangeException {
        m.startApp();
    }

    /**
     * Calls the pauseApp method on the midlet instance.
     *
     * @param m MIDlet instance
     */
    public static void callPauseApp(MIDlet m) {
        m.pauseApp();
    }

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
    public static void callDestroyApp(MIDlet m, boolean unconditional) 
        throws MIDletStateChangeException {
        m.destroyApp(unconditional);
    }
} 
