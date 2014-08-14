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

package com.sun.j2me.security;

import com.sun.j2me.i18n.Resource;
import com.sun.j2me.i18n.ResourceConstants;

/**
 * PIM access permissions.
 */
public class PIMPermission extends Permission {
    
    static public final PIMPermission CONTACT_READ = new PIMPermission(
        "javax.microedition.pim.ContactList.read",
        Resource.getString(ResourceConstants.ABSTRACTIONS_PIM_CONTACTS));

    static public final PIMPermission CONTACT_WRITE = new PIMPermission(
        "javax.microedition.pim.ContactList.write",
        Resource.getString(ResourceConstants.ABSTRACTIONS_PIM_CONTACTS));

    static public final PIMPermission EVENT_READ = new PIMPermission(
        "javax.microedition.pim.EventList.read",
        Resource.getString(ResourceConstants.ABSTRACTIONS_PIM_EVENTS));

    static public final PIMPermission EVENT_WRITE = new PIMPermission(
        "javax.microedition.pim.EventList.write",
        Resource.getString(ResourceConstants.ABSTRACTIONS_PIM_EVENTS));

    static public final PIMPermission TODO_READ = new PIMPermission(
        "javax.microedition.pim.ToDoList.read",
        Resource.getString(ResourceConstants.ABSTRACTIONS_PIM_TODO));

    static public final PIMPermission TODO_WRITE = new PIMPermission(
        "javax.microedition.pim.ToDoList.write",
        Resource.getString(ResourceConstants.ABSTRACTIONS_PIM_TODO));
    
    public PIMPermission(String name, String resource) {
        super(name, resource);
    }
}
