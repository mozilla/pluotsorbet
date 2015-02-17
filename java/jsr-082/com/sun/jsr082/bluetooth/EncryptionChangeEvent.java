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

package com.sun.jsr082.bluetooth;

/*
 * Encryption change event.
 */
public class EncryptionChangeEvent extends BluetoothEvent {

    /* ACL connection handle for which encryption change has been performed. */
    private int handle;

    /* Indicates whether the change has occured. */
    private boolean success;

    /* Indicates whether the link encryption is enabled. */
    private boolean enabled;

    /*
     * Class constructor.
     *
     * @param aclHandle ACL connection handle
     * @param succ true if the change occured, false otherwise
     * @param on true if the change occured, false otherwise
     */
    public EncryptionChangeEvent(int aclHandle, boolean succ, boolean on) {
        handle = aclHandle;
        success = succ;
        enabled = on;
    }

    /*
     * Processes this event.
     */
    public void process() {
        BluetoothStack.getInstance().onEncryptionChange(handle, success);
    }

}
