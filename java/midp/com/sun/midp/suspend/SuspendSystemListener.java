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

package com.sun.midp.suspend;

/**
 * A listener interested in java stack suspend/resume events.
 * The SuspendSystem notifies all registered listeners on java stack
 * suspend/resume completion.
 */
public interface SuspendSystemListener {
    /**
     * Called to notify the listener that java side of MIDP system
     * has been suspended. Note this method may be called from the
     * thread that processes system suspending routine, so it must
     * be non-blocking.
     */
    public void midpSuspended();

    /**
     * Called to notify the listener that java side of MIDP system
     * has been resumed. Note this method may be called from the thread
     * that processes system resume routine, so blocking in this method
     * may prevent other listeners form receving notification.
     */
    public void midpResumed();
}
