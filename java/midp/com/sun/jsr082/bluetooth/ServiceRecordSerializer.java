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
import java.util.Enumeration;
import javax.bluetooth.DataElement;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

/*
 * Serializes ServiceRecord objects.
 */
public class ServiceRecordSerializer {

    /* Helper object used for data element serialization. */
    static DataElementSerializer des;

    /* Initializes static fields. */
    static {
        des = new DataElementSerializer();
    }

    /*
     * Serializes given service record - creates an array of bytes representing
     * data elements as described in Bluetooth Specification Version 1.2,
     * vol 3, page 127.
     *
     * @param record the service record to serialize
     * @return an array containing the serialized record
     */
    public static synchronized byte[] serialize(ServiceRecord record) {
        DataElement seq = new DataElement(DataElement.DATSEQ);
        int[] attrIDs = record.getAttributeIDs();
        for (int i = 0; i < attrIDs.length; i++) {
            DataElement attrID = new DataElement(DataElement.U_INT_2,
                    attrIDs[i]);
            DataElement attrValue = record.getAttributeValue(attrIDs[i]);
            if (attrValue != null) {
                seq.addElement(attrID);
                seq.addElement(attrValue);
            }
        }
        try {
            return des.serialize(seq);
        } catch (IOException e) {
            return null;
        }
    }

    /*
     * Restores previously serialized service record.
     *
     * @param notifier notifier object the newly created record
     *         to be associated with
     * @param data serialized service record data
     * @return restored service record
     */
    public static synchronized ServiceRecordImpl restore(
            BluetoothNotifier notifier, byte[] data) {
        DataElement seq;
        try {
            seq = des.restore(data);
        } catch (IOException e) {
            return null;
        }
        Enumeration elements = (Enumeration)seq.getValue();
        int[] attrIDs = new int[seq.getSize() / 2];
        DataElement[] attrValues = new DataElement[attrIDs.length];
        for (int i = 0; i < attrIDs.length; i++) {
            attrIDs[i] = (int)((DataElement)elements.nextElement()).getLong();
            DataElement attrValue = (DataElement)elements.nextElement();
            DataElement newAttrValue;
            int dataType = attrValue.getDataType();
            switch (dataType) {
                case DataElement.BOOL:
                    newAttrValue = new DataElement(attrValue.getBoolean());
                    break;
                case DataElement.NULL:
                    newAttrValue = new DataElement(DataElement.NULL);
                    break;
                case DataElement.U_INT_1:
                case DataElement.U_INT_2:
                case DataElement.U_INT_4:
                case DataElement.INT_1:
                case DataElement.INT_2:
                case DataElement.INT_4:
                case DataElement.INT_8:
                    newAttrValue = new DataElement(dataType,
                            attrValue.getLong());
                    break;
                case DataElement.DATALT:
                case DataElement.DATSEQ:
                    Enumeration e = (Enumeration)attrValue.getValue();
                    newAttrValue = new DataElement(dataType);
                    while (e.hasMoreElements()) {
                        newAttrValue.addElement((DataElement)e.nextElement());
                    }
                    break;
                default:
                    newAttrValue = new DataElement(attrValue.getDataType(),
                            attrValue.getValue());
                    break;
            }
            attrValues[i] = newAttrValue;
        }
        return new ServiceRecordImpl(notifier, attrIDs, attrValues);
    }
    
    public static synchronized ServiceRecordImpl restore(RemoteDevice device, byte[] data ) {
        DataElement seq;
        try {
            seq = des.restore(data);
        } catch (IOException e) {
            return null;
        }
        Enumeration elements = (Enumeration)seq.getValue();
        int[] attrIDs = new int[seq.getSize() / 2];
        DataElement[] attrValues = new DataElement[attrIDs.length];
        for (int i = 0; i < attrIDs.length; i++) {
            attrIDs[i] = (int)((DataElement)elements.nextElement()).getLong();
            DataElement attrValue = (DataElement)elements.nextElement();
            DataElement newAttrValue;
            int dataType = attrValue.getDataType();
            switch (dataType) {
                case DataElement.BOOL:
                    newAttrValue = new DataElement(attrValue.getBoolean());
                    break;
                case DataElement.NULL:
                    newAttrValue = new DataElement(DataElement.NULL);
                    break;
                case DataElement.U_INT_1:
                case DataElement.U_INT_2:
                case DataElement.U_INT_4:
                case DataElement.INT_1:
                case DataElement.INT_2:
                case DataElement.INT_4:
                case DataElement.INT_8:
                    newAttrValue = new DataElement(dataType,
                            attrValue.getLong());
                    break;
                case DataElement.DATALT:
                case DataElement.DATSEQ:
                    Enumeration e = (Enumeration)attrValue.getValue();
                    newAttrValue = new DataElement(dataType);
                    while (e.hasMoreElements()) {
                        newAttrValue.addElement((DataElement)e.nextElement());
                    }
                    break;
                default:
                    newAttrValue = new DataElement(attrValue.getDataType(),
                            attrValue.getValue());
                    break;
            }
            attrValues[i] = newAttrValue;
        }
        return new ServiceRecordImpl(device, attrIDs, attrValues);
    }
    
}
