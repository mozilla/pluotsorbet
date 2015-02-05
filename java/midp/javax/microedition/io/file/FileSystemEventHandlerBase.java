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

package javax.microedition.io.file;

import com.sun.midp.events.*;
import com.sun.midp.security.*;

import java.util.Enumeration;
import java.util.Vector;

import com.sun.cdc.io.j2me.file.Protocol;

/**
 * File system event handler.
 */
abstract class FileSystemEventHandlerBase implements EventListener {

    /**
     * Inner class to request security token from SecurityInitializer.
     * SecurityInitializer should be able to check this inner class name.
     */
    static private class SecurityTrusted
        implements ImplicitlyTrustedClass {};

    /** Security token to allow access to implementation APIs */
    private static SecurityToken classSecurityToken =
        SecurityInitializer.requestToken(new SecurityTrusted());

    /**
     * The EventListener to receive the MM events.
     */
    private static EventListener listener;


    /**
     * Default constructor.
     * Registers current isolate as interested in receiving mount/unmount
     * events.
     */
    public FileSystemEventHandlerBase() {
        registerListener();
    }

    /**
     * Performs clean up upon object destruction.
     * Removes current isolate from the list of isolates interested in
     * receiving mount/unmount events.
     */
    private native void finalize();

    /**
     * Registers current isolate as interested in receiving mount/unmount
     * events.
     */
    private native void registerListener();

    /**
     * Register the event listener in the event queue.
     *
     * Security note: access specifier for this method is 'package private'
     * so only classes from this package can access it.
     *
     * @param l <code>EventListener</code> for the MM events.
     */
    static void setListener(EventListener l) {
        // Listener can be set only once.
        if (listener != null) {
            return;
        }
        listener = l;

        EventQueue evtq = EventQueue.getEventQueue(classSecurityToken);
        evtq.registerEventListener(EventTypes.FC_DISKS_CHANGED_EVENT, listener);
    }

    /**
     * Preprocess an event that is being posted to the event queue.
     * This method will get called in the thread that posted the event.
     *
     * @param event event being posted
     *
     * @param waitingEvent previous event of this type waiting in the
     *     queue to be processed
     *
     * @return <code>true</code> to allow the post to continue,
     *         <code>false</code> to not post the event to the queue
     */
    public boolean preprocess(Event event, Event waitingEvent) {
        if (event.getType() != EventTypes.FC_DISKS_CHANGED_EVENT) {
            return false;
        }

        if (waitingEvent != null) {
            return false;
        }

        return true;
    }

    /**
     * Process an event.
     * This method will get called in the event queue processing thread.
     *
     * @param event event to process
     */
    public void process(Event event) {
        if (event.getType() != EventTypes.FC_DISKS_CHANGED_EVENT) {
            return;
        }

        // Get up-to-date roots list from OS
        Vector newRoots = Protocol.listRoots();

        // Get cached roots list from FileSystemRegistry
        Vector oldRoots = new Vector(newRoots.size() + 1);
        for (Enumeration e = FileSystemRegistry.listCachedRoots();
                e.hasMoreElements();) {
            oldRoots.addElement(e.nextElement());
        }

        // Find all removed roots and notify FileSystemRegistry about them
        for (int i = 0; i < oldRoots.size(); i++) {
            String root = (String)oldRoots.elementAt(i);
            if (!newRoots.contains(root)) {
                FileSystemRegistry.removeRoot(root);
            } else {
                newRoots.removeElement(root);
            }
        }

        // Notify FileSystemRegistry about all added roots if any
        for (int i = 0; i < newRoots.size(); i++) {
            String root = (String)newRoots.elementAt(i);
            FileSystemRegistry.addRoot(root);
        }
    }
}
