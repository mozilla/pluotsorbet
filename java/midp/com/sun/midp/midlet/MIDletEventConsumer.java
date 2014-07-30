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

/**
 * This is the DisplayEventHandler/MIDletStateHandler contract for
 * MIDlet events.
 *
 * This interface is to be implemnted by an event processing target 
 * for MIDlet events. 
 * EventListener for these events must find appropriate 
 * instance of this I/F implementor and call its methods.
 *
 * TBD: method and parameter lists of the I/F is preliminary 
 * and is a subject for changes.
 *
 * TBD: Implementor of this I/F shall be instance specific, 
 * and thus will be able to obtain all IDs itself. 
 * normally I/F implementor shall be associated with MIDlet,
 * i.e. implemented by MIDletPeer.
 * 
 */
public interface MIDletEventConsumer {
    /**
     * MIDlet State Management (Lifecycle) Events
     *
     * ACTIVATE_MIDLET_EVENT
     * PAUSE_MIDLET_EVENT
     * DESTROY_MIDLET_EVENT
     *
     */
    /**
     * Pauses a MIDlet.
     */
    public void handleMIDletPauseEvent();
    /**
     * Activates a MIDLet.
     */
    public void handleMIDletActivateEvent();
    /**
     * Destroys a MIDlet.
     */
    public void handleMIDletDestroyEvent();
}
