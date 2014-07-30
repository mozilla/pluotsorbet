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

/* This is an interface that allows Java code to get at the private    */
/* data fields of a Command.  It's only temporary, because (I believe) */
/* the access problems were fixed in a later version of the MIDP spec. */
package com.sun.midp.lcdui;

import javax.microedition.lcdui.Command;

/**
 * Special class to handle access to Command objects.
 */
public interface CommandAccess {
    /**
     * Get the label of the given Command.
     *
     * @param c The Command to retrieve the label of
     * @return String The label of the Command
     */
    String getLabel(Command c);
    /**
     * Get the type of the given Command.
     *
     * @param c The Command to retrieve the type of
     * @return int The type of the Command
     */
    int    getType(Command c);
    /**
     * Get the priority of the given Command.
     *
     * @param c The Command to retrieve the priority of
     * @return int The priority of the Command
     */
    int    getPriority(Command c);
}
