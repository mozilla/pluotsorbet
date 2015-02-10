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

import javax.bluetooth.DeviceClass;

/*
* Inquiry result record.
*/
class InquiryResult {

    /* Bluetooth address of a discovered device. */
    private String address;

    /* Class of a discovered device. */
    private DeviceClass deviceClass;

    /*
     * Class constructor.
     *
     * @param addr Bluetooth address of a discovered device
     * @param cod class of a discovered device
     */
    public InquiryResult(String addr, int cod) {
        address = addr;
        deviceClass = new DeviceClass(cod);
    }

    /*
     * Returns Bluetooth address of the remote device.
     *
     * @return Bluetooth address of the remote device
     */
    public String getAddress() {
        return address;
    }

    /*
     * Returns class of the remote device.
     *
     * @return class of the device
     */
    public DeviceClass getDeviceClass() {
        return deviceClass;
    }

}
