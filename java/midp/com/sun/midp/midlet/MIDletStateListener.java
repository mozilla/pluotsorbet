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

package com.sun.midp.midlet;

import javax.microedition.midlet.MIDlet;

/**
 * The interface decouples the MIDlet state handler for the VM.
 */
public interface MIDletStateListener {
    /**
     * Called before a MIDlet is created.
     *
     * @param suite reference to the loaded suite
     * @param className class name of the MIDlet to be created
     */
    void midletPreStart(MIDletSuite suite, String className);

    /**
     * Called after the MIDlet's peer is successfully created and before
     * the MIDlet is constructed.
     *
     * @param suite reference to the loaded suite
     * @param className Class name of the MIDlet
     * @param externalAppId ID of given by an external application manager
     */
    void midletCreated(MIDletSuite suite, String className, int externalAppId);

    /**
     * Called before a MIDlet is activated.
     *
     * @param suite reference to the loaded suite
     * @param className class name of the MIDlet
     */
    void preActivated(MIDletSuite suite, String className);

    /**
     * Called after a MIDlet is successfully activated. This is after
     * the startApp method is called.
     *
     * @param suite reference to the loaded suite
     * @param midlet reference to the MIDlet
     */
    void midletActivated(MIDletSuite suite, MIDlet midlet);

    /**
     * Called after a MIDlet is successfully paused.
     *
     * @param suite reference to the loaded suite
     * @param className class name of the MIDlet
     */
    void midletPaused(MIDletSuite suite, String className);

    /**
     * Called after a MIDlet pauses itself. In this case pauseApp has
     * not been called.
     *
     * @param suite reference to the loaded suite
     * @param className class name of the MIDlet
     */
    void midletPausedItself(MIDletSuite suite, String className);

    /**
     * Called when a MIDlet calls MIDlet resume request.
     *
     * @param suite reference to the loaded suite
     * @param className class name of the MIDlet
     */
    void resumeRequest(MIDletSuite suite, String className);

    /**
     * Called after a MIDlet is successfully destroyed.
     *
     * @param suite reference to the loaded suite
     * @param className class name of the MIDlet
     * @param midlet reference to the MIDlet, null if the MIDlet's constructor
     *               was not successful
     */
    void midletDestroyed(MIDletSuite suite, String className, MIDlet midlet);
}
