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

package com.sun.midp.jsr075;

/*
 * Interface provides an interface to clean up file connection data upon suite
 * removal.
 *
 * CLDC Note:
 *     The interface has no method to delete data but just check whether the
 * data exists due to private data removal is performed via MIDP listener
 * mechanism.
 *     That listener is registered by JSR-75 code in the way like this:
 * midp_suite_add_listener(jsr75_remove_listener,
 *                         SUITESTORE_LISTENER_TYPE_REMOVE,
 *                         SUITESTORE_OPERATION_END);
 */
public interface FileConnectionCleanup {

    /**
     * The function checks whether suite has private data.
     *
     * @param suiteId MIDlet suite ID
     *
     * @return true if the suite's private directory exists, false otherwise.
     */
    public boolean suiteHasPrivateData(int suiteId);

}
