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

package com.sun.midp.lcdui;

import javax.microedition.lcdui.Displayable;

/**
 * This class provides methods to abstract the central foreground control
 * code from the the LCDUI library.
 */
public interface ForegroundController {
    /**
     * Called to register a newly created Display. Must method must
     * be called before the other methods can be called.
     *
     * @param displayId ID of the Display
     * @param ownerClassName Class name of the that owns the display
     *
     * @return a place holder displayable to used when "getCurrent()==null",
     *         if null is returned an empty form is used
     */
    /*
     * Implementation note: If an general customization interface is
     * created for LCDUI to replace the build time constants based system, the
     * displayable creation function of the method should be moved to it.
     */
    Displayable registerDisplay(int displayId, String ownerClassName);

    /**
     * Called to request the foreground.
     *
     * @param displayId ID of the Display
     * @param isAlert true if the current displayable is an Alert
     */
    void requestForeground(int displayId, boolean isAlert);

    /**
     * Called to request the background.
     *
     * @param displayId ID of the Display
     */
    void requestBackground(int displayId);

    /**
     * Called to start preempting. The given display will preempt all other
     * displays for this isolate.
     *
     * @param displayId ID of the Display
     */
    void startPreempting(int displayId);

    /**
     * Called to stop preempting.
     *
     * @param displayId ID of the Display
     */
    void stopPreempting(int displayId);
}
