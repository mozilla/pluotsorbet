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

package com.sun.midp.util.isolate;
import java.util.*;
import com.sun.midp.security.Permissions;
import com.sun.midp.security.SecurityToken;

/**
 * Named mutex that provides limited inter-Isolate synchronization.
 *
 * You MUST NOT use it for synchronizing threads within same Isolate, that is, 
 * call lock/unlock methods from different threads of same Isolate. There are 
 * many reasons why it is not allowed, one of them is because mutex won't be 
 * automatically unlocked when the thread that locked it has been destroyed: 
 * automatic unlock happens only after the whole Isolate that locked the mutex 
 * has been destroyed.
 */
public class InterIsolateMutex {
    /** Mutexes indexed by name */
    private static Hashtable mutexes = new Hashtable();

    /** Unique mutex ID associated with mutex name */
    private int mutexID;
    
    /**
     * Gets mutex instance. For any given mutex name, the same istance per 
     * Isolate is returned.
     *
     * @param mutexName mutex name
     * @return mutex instance
     */
    public static synchronized InterIsolateMutex getInstance(
            SecurityToken token, String mutexName) {

        token.checkIfPermissionAllowed(Permissions.MIDP);        

        InterIsolateMutex mutex = (InterIsolateMutex)mutexes.get(mutexName);
        if (mutex == null) {
            int mutexID = getID0(mutexName);
            mutex = new InterIsolateMutex(mutexID);
            mutexes.put(mutexName, mutex);
        }

        return mutex;
    }

    /**
     * Locks mutex.
     */
    public void lock() {
        lock0(mutexID);
    }

    /**
     * Unlocks mutex.
     */
    public void unlock() {
        unlock0(mutexID);
    }

    /**
     * Private constructore to prevent users from creating class instances.
     */
    private InterIsolateMutex() {
    }

    /**
     * Constructor.
     *
     * @param mutexID mutex ID
     */
    private InterIsolateMutex(int mutexID) {
        this.mutexID = mutexID;
    }

    /**
     * Gets mutex ID for specified mutex name.
     */
    private static native int getID0(String mutexName);

    /**
     * Locks mutex.
     */
    private static native void lock0(int mutexID);

    /**
     * Unlocks mutex.
     */
    private static native void unlock0(int mutexID);

    /**
     * Native finalizer for unlocking mutex when Isolate that locked it 
     * goes down.
     */
    private native void finalize();
}
