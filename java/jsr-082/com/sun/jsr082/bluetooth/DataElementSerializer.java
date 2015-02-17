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
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

/*
 * Serializes and restores DataElement objects.
 */
public class DataElementSerializer {
    /* NULL data header. */
    private static final byte NULL_DATA = 0x00;

    /* Boolean data header. */
    private static final byte BOOLEAN_DATA = 0x28;

    /* 1-byte signed integer header. */
    private static final byte INT1_SIGNED = 0x10;

    /* 2-byte signed integer header. */
    private static final byte INT2_SIGNED = 0x11;

    /* 4-byte signed integer header. */
    private static final byte INT4_SIGNED = 0x12;

    /* 8-byte signed integer header. */
    private static final byte INT8_SIGNED = 0x13;

    /* 16-byte signed integer header. */
    private static final byte INT16_SIGNED = 0x14;

    /* 1-byte unsigned integer header. */
    private static final byte INT1_UNSIGNED = 0x08;

    /* 2-byte unsigned integer header. */
    private static final byte INT2_UNSIGNED = 0x09;

    /* 4-byte unsigned integer header. */
    private static final byte INT4_UNSIGNED = 0x0a;

    /* 8-byte unsigned integer header. */
    private static final byte INT8_UNSIGNED = 0x0b;

    /* 16-byte unsigned integer header. */
    private static final byte INT16_UNSIGNED = 0x0c;

    /* 16-bit UUID header. */
    private static final byte UUID_2 = 0x19;

    /* 32-bit UUID header. */
    private static final byte UUID_4 = 0x1a;

    /* 128-bit UUID header. */
    private static final byte UUID_16 = 0x1c;

    /* Mask to get type tag from header. */
    private static final byte TYPE_MASK = ((byte)0xf8);

    /* Mask to get size of data size field from header. */
    private static final byte SIZE_MASK = 0x07;

    /* Tag for string type. */
    private static final byte STRING_TYPE = 0x20;

    /* Tag for URL type. */
    private static final byte URL_TYPE = 0x40;

    /* Tag for sequence type. */
    private static final byte SEQUENCE_TYPE = 0x30;

    /* Tag for an alternative type. */
    private static final byte ALTERNATIVE_TYPE = 0x38;

    /* Tag that identifies that size of data size field is 2 bytes. */
    private static final byte SHORT_SIZE = 0x05;

    /* Tag that identifies that size of data size field is 4 bytes. */
    private static final byte NORMAL_SIZE = 0x06;

    /* Tag that identifies that size of data size field is 8 bytes. */
    private static final byte LONG_SIZE = 0x07;

    /* Destination buffer which collects binary data of a data element. */
    protected byte[] writeBuffer = null;

    /* Source buffer which contains binary data of a data element. */
    protected byte[] readBuffer = null;

    /* Current position at the destination buffer. */
    protected long writePos = 0;

    /* Current position at the source buffer. */
    protected long readPos = 0;

    /* Allows to store and retrieve positions at the source buffer. */
    private Stack readPosStack = new Stack();

    /*
     * Constructs the serializer object.
     */
    public DataElementSerializer() {
    }

    /*
     * Serializes given DataElement object, i.e. creates an array of bytes
     * representing DataElement as described in Bluetooth Specification
     * Version 1.2, vol 3, page 127.
     *
     * @param data the data element to serialize
     * @return an array containing the serialized data element
     * @throws IOException if an I/O error occurs
     */
    public synchronized byte[] serialize(DataElement data) throws IOException {
        writeBuffer = new byte[(int)getDataSize(data)];
        writePos = 0;
        writeDataElement(data);
        byte[] result = writeBuffer;
        writeBuffer = null;
        return result;
    }

    /*
     * Constructs DataElement from byte array containing the element in
     * serialized form.
     *
     * @param data byte array containing the element in serialized form
     * @return DataElement constructed from the binary data
     * @throws IOException if an I/O error occurs
     */
    public synchronized DataElement restore(byte[] data) throws IOException {
        readBuffer = data;
        readPos = 0;
        DataElement result = readDataElement();
        readBuffer = null;
        return result;
    }

    /*
     * Returns the size of DataElement with service information
     * to get the total size required to work with this DataElement.
     *
     * @param data the data element to get packet size for
     * @return number of bytes needed to store given data element
     */
    public long getDataSize(DataElement data) {
        int type = data.getDataType();
        long size = getPureDataSize(data);
        if ((type == DataElement.NULL) || (type == DataElement.BOOL)
                || (type == DataElement.INT_1) || (type == DataElement.U_INT_1)
                || (type == DataElement.INT_2) || (type == DataElement.U_INT_2)
                || (type == DataElement.INT_4) || (type == DataElement.U_INT_4)
                || (type == DataElement.INT_8) || (type == DataElement.U_INT_8)
                || (type == DataElement.INT_16)
                || (type == DataElement.U_INT_16)
                || (type == DataElement.UUID)) {
            return size + 1;
        } else if ((type == DataElement.DATSEQ)
                || (type == DataElement.DATALT) || (type == DataElement.STRING)
                || (type == DataElement.URL)) {
            if (size <= 0xffL) {
                return size + 2;
            } else if (size <= 0xffffL) {
                return size + 3;
            } else if (size <= 0xffffffffL) {
                return size + 5;
            } else {
                throw new RuntimeException("Data size is too large.");
            }
        } else {
            throw new RuntimeException("Unexpected data type.");
        }
    }

    /*
     * Returns the size of DataElement without service information.
     *
     * @param data the data element to get pure data size for
     * @return pure data size in bytes
     */
    public long getPureDataSize(DataElement data) {
        switch (data.getDataType()) {
            case DataElement.NULL:
                return 0;
            case DataElement.BOOL:
            case DataElement.INT_1:
            case DataElement.U_INT_1:
                return 1;
            case DataElement.INT_2:
            case DataElement.U_INT_2:
                return 2;
            case DataElement.INT_4:
            case DataElement.U_INT_4:
                return 4;
            case DataElement.INT_8:
            case DataElement.U_INT_8:
                return 8;
            case DataElement.INT_16:
            case DataElement.U_INT_16:
                return 16;
            case DataElement.DATSEQ:
            case DataElement.DATALT:
                long size = 0;
                Enumeration elements = (Enumeration)data.getValue();
                while (elements.hasMoreElements()) {
                    size += getDataSize((DataElement)elements.nextElement());
                }
                return size;
            case DataElement.STRING:
            case DataElement.URL:
                return ((String)data.getValue()).length();
            case DataElement.UUID:
                return 16;
            default:
                throw new RuntimeException("Unknown data type.");
        }
    }

    /*
     * Writes given data element into the write buffer.
     *
     * @param data the data element to write
     * @throws IOException if an I/O error occurs
     */
    public void writeDataElement(DataElement data) throws IOException {
        long size = getPureDataSize(data);
        int type = data.getDataType();
        byte typeBits = 0x00;
        if ((type == DataElement.NULL) || (type == DataElement.BOOL)
                || (type == DataElement.INT_1) || (type == DataElement.U_INT_1)
                || (type == DataElement.INT_2) || (type == DataElement.U_INT_2)
                || (type == DataElement.INT_4) || (type == DataElement.U_INT_4)
                || (type == DataElement.INT_8) || (type == DataElement.U_INT_8)
                || (type == DataElement.INT_16)
                || (type == DataElement.U_INT_16)) {
            switch (type) {
                case DataElement.NULL:
                    writeByte(NULL_DATA);
                    break;
                case DataElement.BOOL:
                    writeByte(BOOLEAN_DATA);
                    writeBoolean(data.getBoolean());
                    break;
                case DataElement.INT_1:
                    writeByte(INT1_SIGNED);
                    writeByte((byte)data.getLong());
                    break;
                case DataElement.U_INT_1:
                    writeByte(INT1_UNSIGNED);
                    writeByte((byte)data.getLong());
                    break;
                case DataElement.INT_2:
                    writeByte(INT2_SIGNED);
                    writeShort((short)data.getLong());
                    break;
                case DataElement.U_INT_2:
                    writeByte(INT2_UNSIGNED);
                    writeShort((short)data.getLong());
                    break;
                case DataElement.INT_4:
                    writeByte(INT4_SIGNED);
                    writeInteger((int)data.getLong());
                    break;
                case DataElement.U_INT_4:
                    writeByte(INT4_UNSIGNED);
                    writeInteger((int)data.getLong());
                    break;
                case DataElement.INT_8:
                    writeByte(INT8_SIGNED);
                    writeLong(data.getLong());
                    break;
                case DataElement.U_INT_8:
                    writeByte(INT8_UNSIGNED);
                    writeBytes((byte[])data.getValue());
                    break;
                case DataElement.INT_16:
                    writeByte(INT16_SIGNED);
                    writeBytes((byte[])data.getValue());
                    break;
                case DataElement.U_INT_16:
                    writeByte(INT16_UNSIGNED);
                    writeBytes((byte[])data.getValue());
                    break;
            }
        } else if ((type == DataElement.DATSEQ)
                || (type == DataElement.DATALT) || (type == DataElement.STRING)
                || (type == DataElement.URL)) {
            switch (type) {
                case DataElement.DATSEQ:
                    typeBits = (TYPE_MASK & SEQUENCE_TYPE);
                    break;
                case DataElement.DATALT:
                    typeBits = (TYPE_MASK & ALTERNATIVE_TYPE);
                    break;
                case DataElement.STRING:
                    typeBits = (TYPE_MASK & STRING_TYPE);
                    break;
                case DataElement.URL:
                    typeBits = (TYPE_MASK & URL_TYPE);
                    break;
            }
            if (size <= 0xff) {
                writeByte(typeBits | (SIZE_MASK & SHORT_SIZE));
                writeByte((byte)size);
            } else if (size <= 0xffff) {
                writeByte(typeBits | (SIZE_MASK & NORMAL_SIZE));
                writeShort((short)size);
            } else {
                writeByte(typeBits | (SIZE_MASK & LONG_SIZE));
                writeInteger((int)size);
            }
            if ((type == DataElement.DATSEQ) || (type == DataElement.DATALT)) {
                Enumeration elements = (Enumeration) data.getValue();
                while (elements.hasMoreElements()) {
                    writeDataElement((DataElement)elements.nextElement());
                }
            } else {
                writeBytes(((String)data.getValue()).getBytes());
            }
        } else if (type == DataElement.UUID) {
            writeByte(UUID_16);
            String uuid = ((UUID)data.getValue()).toString();
            while (uuid.length() < 32) {
                uuid = '0' + uuid;
            }
            for (int i = 0; i < 16; i++) {
                writeByte(Integer.parseInt(
                        uuid.substring(i * 2, i * 2 + 2), 16));
            }
        } else {
            throw new RuntimeException("Unknown data type.");
        }
    }

    /*
     * Creates a data element from the binary data in the read buffer.
     *
     * @return <code>DataElement</code> read
     * @throws IOException if an I/O error occurs
     */
    public DataElement readDataElement() throws IOException {
        byte header = readByte();
        if ((header == NULL_DATA) || (header == BOOLEAN_DATA)
                || (header == INT1_SIGNED) || (header == INT1_UNSIGNED)
                || (header == INT2_SIGNED) || (header == INT2_UNSIGNED)
                || (header == INT4_SIGNED) || (header == INT4_UNSIGNED)
                || (header == INT8_SIGNED) || (header == INT8_UNSIGNED)
                || (header == INT16_SIGNED) || (header == INT16_UNSIGNED)) {
            switch (header) {
            case NULL_DATA:
                return new DataElement(DataElement.NULL);
            case BOOLEAN_DATA:
                return new DataElement(readBoolean());
            case INT1_SIGNED:
                return new DataElement(DataElement.INT_1, readByte());
            case INT1_UNSIGNED:
                return new DataElement(DataElement.U_INT_1,
                        (readByte() & 0xffL));
            case INT2_SIGNED:
                return new DataElement(DataElement.INT_2, readShort());
            case INT2_UNSIGNED:
                return new DataElement(DataElement.U_INT_2,
                        (readShort() & 0xffffL));
            case INT4_SIGNED:
                return new DataElement(DataElement.INT_4, readInteger());
            case INT4_UNSIGNED:
                return new DataElement(DataElement.U_INT_4,
                        (readInteger() & 0xffffffffL));
            case INT8_SIGNED:
                return new DataElement(DataElement.INT_8, readLong());
            case INT8_UNSIGNED:
                return new DataElement(DataElement.U_INT_8, readBytes(8));
            case INT16_SIGNED:
                return new DataElement(DataElement.INT_16, readBytes(16));
            case INT16_UNSIGNED:
                return new DataElement(DataElement.U_INT_16, readBytes(16));
            }
        } else if (((header & TYPE_MASK) == STRING_TYPE)
                || ((header & TYPE_MASK) == URL_TYPE)
                || ((header & TYPE_MASK) == SEQUENCE_TYPE)
                || ((header & TYPE_MASK) == ALTERNATIVE_TYPE)) {
            long size = 0;
            if ((header & SIZE_MASK) == SHORT_SIZE) {
                size = readByte() & 0xffL;
            } else if ((header & SIZE_MASK) == NORMAL_SIZE) {
                size = readShort() & 0xffffL;
            } else if ((header & SIZE_MASK) == LONG_SIZE) {
                size = readInteger() & 0xffffffffL;
            } else {
                System.err.println("Unknown size mask.");
            }
            if ((header & TYPE_MASK) == STRING_TYPE) {
                return new DataElement(DataElement.STRING,
                        new String(readBytes((int)size)));
            } else if ((header & TYPE_MASK) == URL_TYPE) {
                return new DataElement(DataElement.URL,
                        new String(readBytes((int)size)));
            } else {
                DataElement data = null;
                DataElement dataElement = null;
                long dataPos = 0;
                if ((header & TYPE_MASK) == SEQUENCE_TYPE) {
                    data = new DataElement(DataElement.DATSEQ);
                } else {
                    data = new DataElement(DataElement.DATALT);
                }
                while (dataPos < size) {
                    pushReadPos();
                    dataElement = readDataElement();
                    dataPos += readPos - popReadPos();
                    data.addElement(dataElement);
                }
                return data;
            }
        } else if (header == UUID_2) {
            return new DataElement(DataElement.UUID, readUUID(2));
        } else if (header == UUID_4) {
            return new DataElement(DataElement.UUID, readUUID(4));
        } else if (header == UUID_16) {
            return new DataElement(DataElement.UUID, readUUID(16));
        } else {
            throw new RuntimeException("Unknown data type.");
        }
        return null;
    }

    /*
     * Writes boolean data to the buffer.
     * Writes only value given itself. Note that boolean data header
     * should be written before.
     *
     * @param data boolean value to write.
     * @throws IOException if an I/O error occurs
     */
    public void writeBoolean(boolean data) throws IOException {
        writeByte(data ? 1 : 0);
    }

    /*
     * Writes 1-byte data to the buffer.
     *
     * @param data byte value to write.
     * @throws IOException if an I/O error occurs
     */
    public void writeByte(long data) throws IOException {
        if (writePos < 0 || writePos >= writeBuffer.length) {
            throw new IndexOutOfBoundsException();
        }
        writeBuffer[(int)writePos++] = (byte)data;
    }

    /*
     * Writes 2-byte data to the buffer.
     *
     * @param data 2-byte value to write.
     * @throws IOException if an I/O error occurs
     */
    public void writeShort(short data) throws IOException {
        writeByte((byte)((data >>> 8) & 0xff));
        writeByte((byte)((data >>> 0) & 0xff));
    }

    /*
     * Writes 4-byte data to the connection.
     *
     * @param data 4-byte value to write.
     * @throws IOException if an I/O error occurs
     */
    public void writeInteger(int data) throws IOException {
        writeShort((short)((data >>> 16) & 0xffff));
        writeShort((short)((data >>> 0) & 0xffff));
    }

    /*
     * Writes 8-byte data to the connection.
     *
     * @param data 8-byte value to write.
     * @throws IOException if an I/O error occurs
     */
    public void writeLong(long data) throws IOException {
        writeInteger((int)((data >>> 32) & 0xffffffff));
        writeInteger((int)((data >>> 0) & 0xffffffff));
    }

    /*
     * Writes given data to the connection.
     *
     * @param data bytes to write.
     * @throws IOException if an I/O error occurs
     */
    public void writeBytes(byte[] data) throws IOException {
        if (writePos < 0 || writePos + data.length > writeBuffer.length) {
            throw new IndexOutOfBoundsException();
        }
        System.arraycopy(data, 0, writeBuffer, (int)writePos, data.length);
        writePos += data.length;
    }

    /*
     * Reads boolean value from the connection.
     *
     * @return boolean value recieved.
     * @throws IOException if an I/O error occurs
     */
    public boolean readBoolean() throws IOException {
        return (readByte() != 0);
    }

    /*
     * Reads 1-byte value from the connection.
     *
     * @return byte recieved.
     * @throws IOException if an I/O error occurs
     */
    public byte readByte() throws IOException {
        if (readPos < 0 || readPos >= readBuffer.length) {
            throw new IndexOutOfBoundsException();
        }
        return readBuffer[(int)readPos++];
    }

    /*
     * Reads 2-byte value from the connection.
     *
     * @return short which is the 2 bytes read.
     * @throws IOException if an I/O error occurs
     */
    public short readShort() throws IOException {
        int data1 = ((int)readByte()) & 0xff;
        int data2 = ((int)readByte()) & 0xff;
        return (short)((data1 << 8) + (data2 << 0));
    }

    /*
     * Reads 4-byte value from the connection.
     *
     * @return int which is the 4 bytes read.
     * @throws IOException if an I/O error occurs
     */
    public int readInteger() throws IOException {
        int data1 = ((int)readShort()) & 0xffff;
        int data2 = ((int)readShort()) & 0xffff;
        return ((data1 << 16) + (data2 << 0));
    }

    /*
     * Reads 8-byte value from the connection.
     *
     * @return long which is the 8 bytes read.
     * @throws IOException if an I/O error occurs
     */
    public long readLong() throws IOException {
        long data1 = ((long)readInteger()) & 0xffffffffL;
        long data2 = ((long)readInteger()) & 0xffffffffL;
        return ((data1 << 32) + (data2 << 0));
    }

    /*
     * Reads given number of bytes from the connection.
     *
     * @param size number of bytes to read.
     * @return array of bytes read.
     * @throws IOException if an I/O error occurs
     */
    public byte[] readBytes(int size) throws IOException {
        byte[] data = new byte[size];
        int dataPos = 0;
        if (readPos < 0 || readPos + data.length > readBuffer.length) {
            throw new IndexOutOfBoundsException();
        }
        System.arraycopy(readBuffer, (int)readPos, data, 0, data.length);
        readPos += data.length;
        return data;
    }

    /*
     * Reads UUID of a given size.
     *
     * @param len number of bytes to read
     * @return UUID created from <code>len</code> bytes
     * @throws IOException if an I/O error occurs
     */
    public UUID readUUID(int len) throws IOException {
        String uuid = "";
        for (int i = 0; i < len; i++) {
            String digit = Integer.toHexString(readByte() & 0xff);
            if (digit.length() == 1) digit = '0' + digit;
            uuid += digit;
        }
        return new UUID(uuid, len < 16);
    }

    /* Saves the current read position. */
    private void pushReadPos() {
        readPosStack.push(new Long(readPos));
    }

    /* Extracts saved read position. */
    private long popReadPos() {
        return ((Long)readPosStack.pop()).longValue();
    }

}
