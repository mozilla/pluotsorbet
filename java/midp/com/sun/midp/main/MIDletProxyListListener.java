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

package com.sun.midp.main;

/**
 * A listener interface for processing MIDlet proxy list changes and additions.
 */
public interface MIDletProxyListListener {
    /** Field ID for the midletState. */
    static final int MIDLET_STATE = 1;

    /** Field ID for the wantsForegroundState. */
    static final int WANTS_FOREGROUND = 2;

    /** Field ID for the alertWaiting. */
    static final int ALERT_WAITING = 3;

    /** Field ID for the PREEMPTING_DISPLAY. */
    static final int PREEMPTING_DISPLAY = 4;

    /** Field ID for the resources suspend notification. */
    static final int RESOURCES_SUSPENDED = 5;

    /**
     * Called when a MIDlet is added to the list.
     *
     * @param midlet The proxy of the MIDlet being added
     */
    void midletAdded(MIDletProxy midlet);

    /**
     * Called when the state of a MIDlet in the list is updated.
     *
     * @param midlet The proxy of the MIDlet that was updated
     * @param fieldId code for which field of the proxy was updated,
     * see constants above
     */
    void midletUpdated(MIDletProxy midlet, int fieldId);

    /**
     * Called when a MIDlet is removed from the list.
     *
     * @param midlet The proxy of the removed MIDlet
     */
    void midletRemoved(MIDletProxy midlet);

    /**
     * Called when error occurred while starting a MIDlet object.
     *
     * @param externalAppId ID assigned by the external application manager
     * @param suiteId Suite ID of the MIDlet
     * @param className Class name of the MIDlet
     * @param errorCode start error code
     * @param errorDetails start error details
     */
    void midletStartError(int externalAppId, int suiteId, String className,
                          int errorCode, String errorDetails);
}
