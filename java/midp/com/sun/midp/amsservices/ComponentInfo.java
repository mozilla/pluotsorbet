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

package com.sun.midp.amsservices;

/** Simple attribute storage describing a component */
public interface ComponentInfo {
    /** Component ID that is never used. */
    public static final int UNUSED_COMPONENT_ID = 0;

    /**
     *
     * @return ID of the component
     */
    public int getComponentId();

    /**
     *
     * @return ID of the component
     */
    public int getSuiteId();

    /**
     * Returns the display name of the component.
     *
     * @return user-friendly name of the component
     */
    public String getDisplayName();

    /**
     * Returns the version of the component.
     *
     * @return version of the component
     */
    public String getVersion();

    /**
     * Returns true if this component is trusted, false otherwise.
     *
     * @return true if this component is trusted, false otherwise
     */
    public boolean isTrusted();
    
    /**
     * Returns a string representation of the ComponentInfo object.
     * For debug only.
     */
    public String toString();
}
