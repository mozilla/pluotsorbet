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

package com.sun.midp.appmanager;

/**
 * This class is Selector that exits after launching the selected MIDlet.
 */
public class Selector extends SelectorBase {
    /**
     * Destroys this Selector midlet and exits after scheduling
     * an execution of the next midlet in SVM, do nothing in MVM.
     */
    protected void yieldToNextMidlet() {
        try {
            // Give the create MIDlet notification 1 second to get to AMS.
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            // ignore the exception
        }

        // exit
        destroyApp(false);
        notifyDestroyed();
        
        return;
    }
}

