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

package com.sun.midp.io;

/**
 * Base class for Network Connection protocols.
 * This class allows one to initialize the network, if necessary,
 * before any networking code is called.
 */
public abstract class NetworkConnectionBase extends
         BufferedConnectionAdapter { 

    /**
     * This is so not StreamConnection classes can intialize the
     * network if they are loaded first.
     */
    public static void initializeNativeNetwork() {
        /*
         * This method just has to be a reference to
         * get this class loaded and cause the
         * class initializer to initialize the network.
         */
    }

    /**
     * Initialize any possible native networking code.
     */
    private static native void initializeInternal();

    /**
     * This will make sure the network is initialized once and only once
     * per VM instance.
     */
    static { 
        initializeInternal();
    }

    /**
     * Initializes the connection.
     *
     * @param sizeOfBuffer size of the internal buffer or 0 for the default
     *                     size
     */
    protected NetworkConnectionBase(int sizeOfBuffer) {
        super(sizeOfBuffer);
    }
}


