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

package com.sun.midp.io.j2me.datagram;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.UTFDataFormatException;

import javax.microedition.io.Datagram;

import com.sun.midp.io.Util;
import com.sun.midp.io.HttpUrl;

/**
 * Implements a UDP datagram for the UDP datagram connection.
 */
public class DatagramObject implements Datagram {

    /** Length of the hostname buffer. */
    private static final int MAX_HOST_LENGTH = 256;

    /** Buffer to be used. */
    private byte[] buffer;

    /** Where to start sending or receiving data. */
    private int offset;

    /** Number of bytes to send or receive. */
    private int length;

    /** Datagram address as a URL. */
    String address;

    /** Raw IPv4 address. */
    int ipNumber;

    /** UDP port */
    int port;

    /** Current read/write position in buffer. */
    private int readWritePosition;

    /**
     * Create a Datagram Object.
     *
     * @param  buf             The buffer to be used in the datagram
     * @param  len             The length of the buffer to be allocated
     *                         for the datagram
     */
    public DatagramObject(byte[] buf, int len) {
        setData(buf, 0, len);
    }

    /**
     * Get the address in the datagram.
     *
     * @return the address in string form, or null if no address was set
     *
     * @see #setAddress
     */
    public String getAddress() {
        return address;
    }

    /**
     * Get the buffer.
     *
     * @return the data buffer
     *
     * @see #setData
     */
    public byte[] getData() {
        return buffer;
    }

    /**
     * Get the a number that is either the number of bytes to send or the
     * number of bytes received.
     *
     * @return the length of the data
     *
     * @see #setLength
     */
    public int getLength() {
        return length;
    }

    /**
     * Get the offset.
     *
     * @return the offset into the data buffer
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Set datagram address. Must a "datagram://" URI.
     * <p>
     * @param addr the new target address as a URL
     * @exception IllegalArgumentException if the address is not valid
     *
     * @see #getAddress
     */
    public void setAddress(String addr) {
        HttpUrl url;
        int temp;

	if (addr == null)  {
            throw new IllegalArgumentException("Invalid address");
        }

        url = new HttpUrl(addr);

        if (url.scheme == null || !url.scheme.equals("datagram")) {
            throw new IllegalArgumentException("Invalid scheme");
        }

        /*
         * Since we reused the HttpUrl parser, we must make sure that
         * there was nothing past the authority in the URL.
         */
        if (url.path != null || url.query != null || url.fragment != null) {
            throw new IllegalArgumentException("Malformed address");
        }

        port = url.port;

        if (url.host == null) {
            throw new IllegalArgumentException("Missing host");
        }
            
        if (port == -1) {
            throw new IllegalArgumentException("Missing port");
        }

        temp = Protocol.getIpNumber(url.host);
        if (temp == -1) {
            throw new IllegalArgumentException("Invalid host");
        }

        ipNumber = temp;
        address = addr;
    }

    /**
     * Set datagram address, copying the address from another datagram.
     *
     * @param reference the datagram who's address will be copied as
     * the new target address for this datagram.
     * @exception IllegalArgumentException if the address is not valid
     *
     * @see #getAddress
     */
    public void setAddress(Datagram reference) {
        setAddress(reference.getAddress());
    }

    /**
     * Set the length. Which can represent either the number of bytes to send
     * or the maxium number of bytes to receive.
     *
     * @param len the new length of the data
     * @exception IllegalArgumentException if the length is negative
     * or larger than the buffer
     *
     * @see #getLength
     */
    public void setLength(int len) {
        setData(buffer, offset, len);
    }

    /**
     * Set the buffer, offset and length.
     *
     * @param buf the data buffer
     * @param off the offset into the data buffer
     * @param len the length of the data in the buffer
     * @exception IllegalArgumentException if the length or offset
     *                                     fall outside the buffer
     *
     * @see #getData
     */
    public void setData(byte[] buf, int off, int len) {
	/*
	 * Check that the offset and length are valid.
	 *   - must be positive
	 *   - must not exceed buffer length
	 *   - must have valid buffer
	 */
        if (len < 0 || off < 0 ||
	    (buf == null) ||
	    (off > 0 && off == buf.length) ||
            ((len + off) > buf.length)||
            ((len + off) < 0)) {
            throw new IllegalArgumentException("Illegal length or offset");
        }

        buffer = buf;
        offset = off;
        length = len;
    }

    /**
     * Zero the read/write pointer as well as the offset and
     * length parameters.
     */
    public void reset() {
        readWritePosition = 0;
        offset = 0;
        length = 0;
    }

    /**
     * A more effient <code>skip</code> than the one in
     * <code>GeneralBase</code>.
     * Skips over and discards <code>n</code> bytes of data from this input
     * stream. The <code>skip</code> method may, for a variety of reasons, end
     * up skipping over some smaller number of bytes, possibly <code>0</code>.
     * This may result from any of a number of conditions; reaching end of file
     * before <code>n</code> bytes have been skipped is only one possibility.
     * The actual number of bytes skipped is returned.  If <code>n</code> is
     * negative, no bytes are skipped.
     *
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @exception  IOException  if an I/O error occurs.
     */
    public long skip(long n) throws IOException {
        if (n < 0) {
            return 0;
        }

        if (readWritePosition >= length) {
            return 0;
        }

        int min = Math.min((int)n, length - readWritePosition);
        readWritePosition += min;
        return (min);
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     */
    public int read() {
        if (readWritePosition >= length) {
            return -1;
        }

        return buffer[offset + readWritePosition++] & 0xFF;
    }


//
// DataOutput methods
//

    /**
     * Writes the specified byte to this output stream. The general
     * contract for <code>write</code> is that one byte is written
     * to the output stream. The byte to be written is the eight
     * low-order bits of the argument <code>b</code>. The 24
     * high-order bits of <code>ch</code> are ignored.
     *
     * @param      b   the <code>byte</code>.
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> may be thrown if the
     *             buffer is full.
     */
    public void write(int b) throws IOException {
        if (offset + readWritePosition >= buffer.length) {
            throw new IOException("Buffer full");
        }

        buffer[offset + readWritePosition++] = (byte)b;
        length = readWritePosition;
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array.
     *
     * @param      b     the data.
     * @exception  IOException  if an I/O error occurs.
     */
    public final void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to the underlying output stream.
     * If no exception is thrown, the counter <code>written</code> is
     * incremented by <code>len</code>.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                   ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        for (int i = 0 ; i < len ; i++) {
            write(b[off + i]);
        }
    }

    /**
     * Writes a <code>boolean</code> to the underlying output stream as
     * a 1-byte value. The value <code>true</code> is written out as the
     * value <code>(byte)1</code>; the value <code>false</code> is
     * written out as the value <code>(byte)0</code>. If no exception is
     * thrown, the counter <code>written</code> is incremented by
     * <code>1</code>.
     *
     * @param      v   a <code>boolean</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeBoolean(boolean v) throws IOException {
        write(v ? 1 : 0);
    }

    /**
     * Writes out a <code>byte</code> to the underlying output stream as
     * a 1-byte value. If no exception is thrown, the counter
     * <code>written</code> is incremented by <code>1</code>.
     *
     * @param      v   a <code>byte</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeByte(int v) throws IOException {
        write(v);
    }

    /**
     * Writes a <code>short</code> to the underlying output stream as two
     * bytes, high byte first. If no exception is thrown, the counter
     * <code>written</code> is incremented by <code>2</code>.
     *
     * @param      v   a <code>short</code> to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeShort(int v) throws IOException {
        write((v >>> 8) & 0xFF);
        write((v >>> 0) & 0xFF);
    }

    /**
     * Writes a <code>char</code> to the underlying output stream as a
     * 2-byte value, high byte first. If no exception is thrown, the
     * counter <code>written</code> is incremented by <code>2</code>.
     *
     * @param      v   a <code>char</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeChar(int v) throws IOException {
        write((v >>> 8) & 0xFF);
        write((v >>> 0) & 0xFF);
    }

    /**
     * Writes an <code>int</code> to the underlying output stream as four
     * bytes, high byte first. If no exception is thrown, the counter
     * <code>written</code> is incremented by <code>4</code>.
     *
     * @param      v   an <code>int</code> to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeInt(int v) throws IOException {
        write((v >>> 24) & 0xFF);
        write((v >>> 16) & 0xFF);
        write((v >>>  8) & 0xFF);
        write((v >>>  0) & 0xFF);
    }

    /**
     * Writes a <code>long</code> to the underlying output stream as eight
     * bytes, high byte first. In no exception is thrown, the counter
     * <code>written</code> is incremented by <code>8</code>.
     *
     * @param      v   a <code>long</code> to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeLong(long v) throws IOException {
        write((int)(v >>> 56) & 0xFF);
        write((int)(v >>> 48) & 0xFF);
        write((int)(v >>> 40) & 0xFF);
        write((int)(v >>> 32) & 0xFF);
        write((int)(v >>> 24) & 0xFF);
        write((int)(v >>> 16) & 0xFF);
        write((int)(v >>>  8) & 0xFF);
        write((int)(v >>>  0) & 0xFF);
    }

    /**
     * Writes a string to the underlying output stream as a sequence of
     * characters. Each character is written to the data output stream as
     * if by the <code>writeChar</code> method. If no exception is
     * thrown, the counter <code>written</code> is incremented by twice
     * the length of <code>s</code>.
     *
     * @param      s   a <code>String</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.DataOutputStream#writeChar(int)
     */
    public void writeChars(String s) throws IOException {
        int len = s.length();
        for (int i = 0 ; i < len ; i++) {
            int v = s.charAt(i);
            write((v >>> 8) & 0xFF);
            write((v >>> 0) & 0xFF);
        }
    }

    /**
     * Writes a string to the underlying output stream using UTF-8
     * encoding in a machine-independent manner.
     * <p>
     * First, two bytes are written to the output stream as if by the
     * <code>writeShort</code> method giving the number of bytes to
     * follow. This value is the number of bytes actually written out,
     * not the length of the string. Following the length, each character
     * of the string is output, in sequence, using the UTF-8 encoding
     * for the character. If no exception is thrown, the counter
     * <code>written</code> is incremented by the total number of
     * bytes written to the output stream. This will be at least two
     * plus the length of <code>str</code>, and at most two plus
     * thrice the length of <code>str</code>.
     *
     * @param      str   a string to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeUTF(String str) throws IOException {
        int strlen = str.length();
        int utflen = 0;
        char[] charr = new char[strlen];
        int c, count = 0;

        str.getChars(0, strlen, charr, 0);

        for (int i = 0; i < strlen; i++) {
            c = charr[i];
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }

        if (utflen > 65535)
            throw new UTFDataFormatException();

        byte[] bytearr = new byte[utflen+2];
        bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
        bytearr[count++] = (byte) ((utflen >>> 0) & 0xFF);
        for (int i = 0; i < strlen; i++) {
            c = charr[i];
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytearr[count++] = (byte) c;
            } else if (c > 0x07FF) {
                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            } else {
                bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            }
        }
        write(bytearr);
    }


//
// DataInput methods
//

    /**
     * See the general contract of the <code>readFully</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @param      b   the buffer into which the data is read.
     * @exception  EOFException  if this input stream reaches the end
     *                           before reading all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public void readFully(byte b[]) throws IOException {
        readFully(b, 0, b.length);
    }

    /**
     * See the general contract of the <code>readFully</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset of the data.
     * @param      len   the number of bytes to read.
     * @exception  EOFException  if this input stream reaches the end
     *                           before reading all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public void readFully(byte b[], int off, int len) throws IOException {
        if (len < 0)
            throw new IndexOutOfBoundsException();

        int n = 0;
        while (n < len) {
            int ch = read();
            if (ch < 0) {
                throw new EOFException();
            }
            b[off+(n++)] = (byte)ch;
        }
    }

    /**
     * See the general contract of the <code>skipBytes</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @exception  IOException   if an I/O error occurs.
     */
    public int skipBytes(int n) throws IOException {
        int total = 0;
        int cur = 0;

        while ((total<n) && ((cur = (int) skip(n-total)) > 0)) {
            total += cur;
        }

        return total;
    }

    /**
     * See the general contract of the <code>readBoolean</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @return     the <code>boolean</code> value read.
     * @exception  EOFException  if this input stream has reached the end.
     */
    public boolean readBoolean() throws IOException {
        int ch = read();
        if (ch < 0)
            throw new EOFException();
        return (ch != 0);
    }

    /**
     * See the general contract of the <code>readByte</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @return     the next byte of this input stream as a signed 8-bit
     *             <code>byte</code>.
     * @exception  EOFException  if this input stream has reached the end.
     * @exception  IOException   if an I/O error occurs.
     */
    public byte readByte() throws IOException {
        int ch = read();
        if (ch < 0)
            throw new EOFException();
        return (byte)(ch);
    }

    /**
     * See the general contract of the <code>readUnsignedByte</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @return     the next byte of this input stream, interpreted as an
     *             unsigned 8-bit number.
     * @exception  EOFException  if this input stream has reached the end.
     * @exception  IOException   if an I/O error occurs.
     */
    public int readUnsignedByte() throws IOException {
        int ch = read();
        if (ch < 0)
            throw new EOFException();
        return ch;
    }

    /**
     * See the general contract of the <code>readShort</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     the next two bytes of this input stream, interpreted
     *             as a signed 16-bit number.
     * @exception  EOFException  if this input stream reaches the end
     *                           before reading two bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public short readShort() throws IOException {
        int ch1 = read();
        int ch2 = read();
        if ((ch1 | ch2) < 0)
             throw new EOFException();
        return (short)((ch1 << 8) + (ch2 << 0));
    }

    /**
     * See the general contract of the <code>readUnsignedShort</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @return     the next two bytes of this input stream, interpreted
     *             as an unsigned 16-bit integer.
     * @exception  EOFException  if this input stream reaches the end
     *                           before reading two bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public int readUnsignedShort() throws IOException {
        int ch1 = read();
        int ch2 = read();
        if ((ch1 | ch2) < 0)
             throw new EOFException();
        return (ch1 << 8) + (ch2 << 0);
    }

    /**
     * See the general contract of the <code>readChar</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @return     the next two bytes of this input stream as
     *             a Unicode character.
     * @exception  EOFException  if this input stream reaches the end
     *                           before reading two bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public char readChar() throws IOException {
        int ch1 = read();
        int ch2 = read();
        if ((ch1 | ch2) < 0)
             throw new EOFException();
        return (char)((ch1 << 8) + (ch2 << 0));
    }

    /**
     * See the general contract of the <code>readInt</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @return     the next four bytes of this input stream, interpreted
     *             as an <code>int</code>.
     * @exception  EOFException  if this input stream reaches the end
     *                           before reading four bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public int readInt() throws IOException {
        int ch1 = read();
        int ch2 = read();
        int ch3 = read();
        int ch4 = read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
             throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    /**
     * See the general contract of the <code>readLong</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @return     the next eight bytes of this input stream, interpreted
     *             as a
     *             <code>long</code>.
     * @exception  EOFException  if this input stream reaches the end
     *                           before reading eight bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public long readLong() throws IOException {
        return ((long)(readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
    }

    /**
     * See the general contract of the <code>readUTF</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @return     a Unicode string.
     * @exception  EOFException  if this input stream reaches the end
     *                           before reading all the bytes.
     * @exception  IOException   if an I/O error occurs.
     * @see        java.io.DataInputStream#readUTF(java.io.DataInput)
     */
    public String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }



    /**
     * See the general contract of the <code>writeFloat</code>
     * method of <code>DataInput</code>.
     * <p>
     * need revisit.
     * @param f float to write
     * @exception  IOException   if an I/O error occurs
     * @see        java.io.DataInputStream#writeFloat(java.io.DataInput)
     */ 
    public void writeFloat(float f) throws IOException {
	// Not implemented
    }

    /**
     * See the general contract of the <code>writeDouble</code>
     * method of <code>DataInput</code>.
     * <p>
     * need revisit.
     * @param d float to write
     * @exception  IOException   if an I/O error occurs
     * @see        java.io.DataInputStream#writeDouble(java.io.DataInput)
     */ 
    public void writeDouble(double d) throws IOException {
	// Not implemented
    }

    /**
     * See the general contract of the <code>readFloat</code>
     * method of <code>DataInput</code>.
     * <p>
     * need revisit: Always return 0.0.
     * @return float that was read
     * @exception  IOException   if an I/O error occurs
     * @see        java.io.DataInputStream#readFloat(java.io.DataInput)
     */ 
    public float readFloat() {
	// Not implemented
        return 0.0f;
    }

    /**
     * See the general contract of the <code>readDouble</code>
     * method of <code>DataInput</code>.
     * <p>
     * need revisit: Always return 0.0.
     * @return double read
     * @exception  IOException   if an I/O error occurs
     * @see        java.io.DataInputStream#readDouble(java.io.DataInput)
     */ 
    public double readDouble() {
	// Not implemented
        return 0.0;
    }


}
