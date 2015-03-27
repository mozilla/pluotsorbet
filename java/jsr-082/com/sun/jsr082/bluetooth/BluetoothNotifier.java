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

import java.io.IOException;
import javax.microedition.io.Connection;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.ServiceRegistrationException;
import javax.bluetooth.DataElement;
import java.util.Enumeration;

/*
 * Base class for all bluetooth notifiers.
 */
public abstract class BluetoothNotifier implements Connection {

    /* Flag to identify if this notifier is closed. */
    protected boolean isClosed = false;

    /* Bluetooth url this notifier created with. */
    protected BluetoothUrl url;

    /* Service record that describes represented service. */
    protected ServiceRecordImpl serviceRec = null;

    /* Keeps open mode. */
    protected int mode;

    /*
     * Class constructor.
     *
     * @param url server connection string this notifier was created with
     * @param mode I/O access mode
     */
    protected BluetoothNotifier(BluetoothUrl url, int mode) {
        // IMPL_NOTE: find proper place; the intent here is to start EmulationPolling
        // and SDPServer prior to create a user's notifier
        SDDB.getInstance();
        this.url = url;
        this.mode = mode;
    }

    /*
     * Retrieves service record for this notifier.
     * It always returns the same object reference.
     *
     * @return service record associated with this notifier
     * @throws IllegalArgumentException if the notifier is closed
     */
    ServiceRecord getServiceRecord() {
        if (isClosed) {
            throw new IllegalArgumentException("Notifier is closed.");
        }
        // IMPL_NOTE: copy should probably be returned instead of a reference,
        // but the current implementation returns reference to make TCK pass
        // return serviceRec.copy();
        return serviceRec;
    }

    /*
     * Stores the service record for this notifier in the local SDDB.
     * If there is no SDDB version of the service record, this method will
     * do nothing.
     *
     * @param record new service record value
     * @throws IllegalArgumentException if new record is invalid
     *
     * @throws ServiceRegistrationException if the record cannot be
     *         updated successfully in the SDDB
     */
    protected void updateServiceRecord(ServiceRecordImpl record)
            throws ServiceRegistrationException {
        ServiceRecordImpl oldRecord = serviceRec;
        serviceRec = record.copy();
        try {
            checkServiceRecord();
        } catch (ServiceRegistrationException e) {
            serviceRec = oldRecord;
            throw new IllegalArgumentException(e.getMessage());
        }
        if (SDDB.getInstance().contains(serviceRec)) {
            SDDB.getInstance().updateServiceRecord(serviceRec);
        }
    }

    /*
     * Ensures that the service record is valid.
     *
     * @throws ServiceRegistrationException in case described in the 
     *                                      JSR specification
     */
    protected abstract void checkServiceRecord()
            throws ServiceRegistrationException;

    /*
     * Compares two DataElements.
     *
     * @param first first DataElement
     * @param second second DataElement
     * @return true if elements are equal, false otherwise
     * @see javax.bluetooth.DataElement
     */
    protected boolean compareDataElements(DataElement first,
            DataElement second) {
        boolean ret = false;
        int valueType = first.getDataType();
        if (ret = (valueType == second.getDataType())) {
            switch (valueType) {
            case DataElement.BOOL:
                ret = first.getBoolean() == second.getBoolean();
                break;
            case DataElement.U_INT_1:
            case DataElement.U_INT_2:
            case DataElement.U_INT_4:
            case DataElement.INT_1:
            case DataElement.INT_2:
            case DataElement.INT_4:
            case DataElement.INT_8:
                ret = first.getLong() == second.getLong();
                break;
            default:
                Object v1 = first.getValue();
                Object v2 = second.getValue();
                if (v1 instanceof Enumeration && v2 instanceof Enumeration) {
                    Enumeration e1 = (Enumeration)v1;
                    Enumeration e2 = (Enumeration)v2;
                    ret = true;
                    while (e1.hasMoreElements() &&
                           e2.hasMoreElements() && ret) {
                        ret &= e1.nextElement().equals(e2.nextElement());
                    }
                    ret = ret &&
                        !(e1.hasMoreElements() ||
                          e2.hasMoreElements());
                } else if (v1 instanceof byte[] && v2 instanceof byte[]) {
                    byte[] a1 = (byte[])v1;
                    byte[] a2 = (byte[])v2;
                    ret = a1.length == a2.length;
                    for (int i = a1.length; --i >= 0 && ret; ) {
                        ret &= (a1[i] == a2[i]);
                    }
                } else {
                    ret = v1.equals(v2);
                }
                break;
            }
        }
        return ret;
    }

    /*
     * Closes the connection. <code>Connection</code> interface implementation.
     *
     * @throws IOException if an I/O error occurs
     */
    public abstract void close() throws IOException;
}
