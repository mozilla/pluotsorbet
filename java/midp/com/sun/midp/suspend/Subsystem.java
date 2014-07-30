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
 * A subsystem that implies special actions on system
 * suspend/resume.
 */
public interface Subsystem {
    /**
     * Being suspended state.
     */
    int SUSPENDING = 0;
    /**
     * Suspended state.
     */
    int SUSPENDED = 1;
    /**
     * Being resumed state.
     */
    int RESUMING = 2;
    /**
     * Active state.
     */
    int ACTIVE = 3;

    /**
     * Perfoms actions required during system resume and moves
     * the subsystem to <code>ACTIVE</code> state.
     * @throws StateTransitionException if the subsystem cannot be
     *         moved to <code>ACTIVE</code> state.
     */
    void resume() throws StateTransitionException;

    /**
     * Perfoms actions required during system suspend and moves
     * the subsystem to <code>SUSPENDED</code> state.
     * @throws StateTransitionException if the subsystem cannot be
     *         moved to <code>SUSPENDED</code> state.
     */
    void suspend() throws StateTransitionException;
}
