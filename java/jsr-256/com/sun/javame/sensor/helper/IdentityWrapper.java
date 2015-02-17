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

package com.sun.javame.sensor.helper;

/**
 * This class can be used in standard CLDC collections to force them to check
 * identity not equality, like in JSE classes IdentityHashMap and
 * IdentityHashSet.
 */
public class IdentityWrapper {

    /** The wrapped object instance. */
    private final Object wrapped;

    /**
     * Wrapper constructor.
     *
     * @param obj wrapped object
     */
    public IdentityWrapper(Object obj) {
        this.wrapped = obj;
    }

    /**
     * Checks identity of wrapped objects.
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IdentityWrapper other = (IdentityWrapper) obj;
        return wrapped == other.wrapped;
    }

    public int hashCode() {
        return System.identityHashCode(wrapped);
    }

    public String toString() {
        return wrapped.toString();
    }

    /**
     * Get wrapped object.
     *
     * @return wrapped object instance
     */
    public Object getWrapped() {
        return wrapped;
    }
}
