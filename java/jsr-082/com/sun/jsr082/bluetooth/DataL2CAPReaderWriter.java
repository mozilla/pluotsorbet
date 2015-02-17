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
import java.util.Stack;
import javax.bluetooth.DataElement;
import javax.bluetooth.L2CAPConnection;
import javax.bluetooth.UUID;

/*
 * Logical Link Control and Adaptation Protocol connection reader/writer.
 */
public class DataL2CAPReaderWriter extends DataElementSerializer {

    /* The L2CAP connection to read from or write to. */
    private L2CAPConnection con = null;

    /* Actual size of the read buffer. */
    private long readSize = 0;

    /*
     * Constructs reader-and-writer object for the given connection.
     *
     * @param con the L2CAP connection to create reader-writer for.
     */
    public DataL2CAPReaderWriter(L2CAPConnection con) throws IOException {
        this.con = con;
        writeBuffer = new byte[con.getTransmitMTU()];
        readBuffer = new byte[con.getReceiveMTU()];
    }

    /*
     * Writes 1-byte data to the connection.
     *
     * @param data byte value to write.
     */
    public void writeByte(long data) throws IOException {
        if ((writePos < 0) || (writePos >= writeBuffer.length)) {
            throw new IndexOutOfBoundsException();
        }
        writeBuffer[(int)writePos] = (byte)data;
        writePos++;
        if (writePos == writeBuffer.length) {
            con.send(writeBuffer);
            writePos = 0;
        }
    }

    /*
     * Writes given data to the connection.
     *
     * @param data bytes to write.
     */
    public void writeBytes(byte[] data) throws IOException {
        if ((writePos < 0) || (writePos >= writeBuffer.length)) {
            throw new IndexOutOfBoundsException();
        }
        int dataPos = 0;

        while ((data.length - dataPos) >= ((writeBuffer.length - writePos))) {
            int length = writeBuffer.length - ((int) writePos);
            System.arraycopy(data, (int) dataPos, writeBuffer, (int) writePos,
                             (int) length);
            con.send(writeBuffer);
            writePos = 0;
            dataPos += length;
        }
        int length = data.length - dataPos;

        if (length > 0) {
            System.arraycopy(data, (int) dataPos, writeBuffer, (int) writePos,
                             (int) length);
        }
        writePos += length;
    }

    /*
     * Flushes write buffer to the conection.
     */
    public void flush() throws IOException {
        if ((writePos < 0) || (writePos >= writeBuffer.length)) {
            throw new IndexOutOfBoundsException();
        }
        if (writePos == 0) {
            return;
        }
        byte[] tempBuffer = new byte[(int)writePos];
        System.arraycopy(writeBuffer, 0, tempBuffer, 0, (int)writePos);
        con.send(tempBuffer);
        writePos = 0;
    }

    /*
     * Reads 1-byte value from the connection.
     *
     * @return byte recieved.
     */
    public byte readByte() throws IOException {
        if ((readPos == 0) || (readPos == readSize)) {
            if ((readSize = con.receive(readBuffer)) == 0) {
                throw new IOException("Empty packet is received");
            }
            readPos = 0;
        } else if ((readPos < 0) || (readPos > readSize)) {
            throw new IndexOutOfBoundsException();
        }

        if (readSize == -1) {
            throw new IOException("Reached end of stream");
        }

        byte data = readBuffer[(int)readPos];
        readPos++;
        return data;
    }

    /*
     * Reads given number of bytes from the connection.
     *
     * @param size number of bytes to read.
     * @return array of bytes read.
     */
    public byte[] readBytes(int size) throws IOException {
        byte[] data = new byte[size];
        int dataPos = 0;

        if ((readPos == 0) || (readPos == readSize)) {
            readSize = con.receive(readBuffer);
            readPos = 0;
        } else if ((readPos < 0) || (readPos > readSize)) {
            throw new IndexOutOfBoundsException();
        }

        while ((data.length - dataPos) > (readSize - readPos)) {
            int length = (int) (readSize - readPos);
            System.arraycopy(readBuffer, (int)readPos, data, (int)dataPos,
                             length);
            dataPos += length;
            readSize = con.receive(readBuffer);
            readPos = 0;
        }
        int length = data.length - dataPos;
        System.arraycopy(readBuffer, (int)readPos, data, (int)dataPos,
                         length);
        readPos += length;
        return data;
    }
}
