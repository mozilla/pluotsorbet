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
package com.sun.jsr082.bluetooth.btspp;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import javax.microedition.io.StreamConnection;
import javax.bluetooth.BluetoothConnectionException;
import com.sun.jsr082.bluetooth.BluetoothUrl;
import com.sun.jsr082.bluetooth.BluetoothUtils;
import com.sun.jsr082.bluetooth.BluetoothConnection;

/*
 * Bluetooth Serial Port Profile connection implementation.
 */
public class BTSPPConnectionImpl extends BluetoothConnection
        implements StreamConnection {

    /* Static initializer. */
    static {
        initialize();
    }

    /*
     * Native static class initializer.
     */
    private native static void initialize();

    /*
     * Native finalizer.
     * Releases all native resources used by this connection.
     */
    protected native void finalize();

    /*
     * Stores the address of the remote device connected by this connection.
     * The value is set by the constructor.
     */
    byte[] remoteDeviceAddress;

    /* Lock object for reading from the socket */
    private final Object readerLock = new Object();

    /* Lock object for writing to the socket */
    private final Object writerLock = new Object();

    /*
     * Identidies this connection at native layer, <code>-1<code>
     * if connection is not open.
     */
    private int handle = -1;

    /* Flag to identify if an input stream is opened for this connection. */
    private boolean isOpened = false;

    /* Flag to identify if an output stream is opened for this connection. */
    private boolean osOpened = false;

    /* Open streams counter. */
    private int objects = 1;

    /*
     * Constructs an instance and opens connection.
     *
     * @param url keeps connection details
     * @param mode I/O access mode
     * @exception IOException if connection fails
     */
    protected BTSPPConnectionImpl(BluetoothUrl url, int mode)
            throws IOException {
        this(url, mode, null);
    }

    /*
     * Constructs an instance and
     * sets up corresponding native connection handle to it.
     *
     * @param url keeps connection details
     * @param mode I/O access mode
     * @param notif corresponding <code>BTSPPNotifierImpl</code> instance
     *              temporary storing native peer handle
     * @exception IOException if connection fails
     */
    protected BTSPPConnectionImpl(BluetoothUrl url,
            int mode, BTSPPNotifierImpl notif) throws IOException {
        super(url, mode);

        if (notif == null) {
            remoteDeviceAddress = BluetoothUtils.getAddressBytes(url.address);
            doOpen();
        } else {
            remoteDeviceAddress = new byte[6];
            System.arraycopy(notif.peerAddress, 0,  remoteDeviceAddress, 0, 6);

            setThisConnHandle0(notif);
        }

        setRemoteDevice();
    }

    /*
     * Retrieves native connection handle from temporary storage
     * inside <code>BTSPPNotifierImpl</code> instance
     * and sets it to this <code>BTSPPConnectionImpl</code> instance.
     *
     * Note: the method sets native connection handle directly to
     * <code>handle<code> field of <code>BTSPPConnectionImpl</code> object.
     *
     * @param notif reference to corresponding <code>BTSPPNotifierImpl</code>
     *              instance storing native peer handle
     */
    private native void setThisConnHandle0(BTSPPNotifierImpl notif);

    /*
     * Retrieves remote address for the connection.
     *
     * @return remote address
     */
    public String getRemoteDeviceAddress() {
        return BluetoothUtils.getAddressString(remoteDeviceAddress);
    }

    /*
     * Open and return a data input stream for a connection.
     *
     * @return An input stream
     * @throws IOException if an I/O error occurs
     */
    public final DataInputStream openDataInputStream() throws IOException {
        return new DataInputStream(openInputStream());
    }

    /*
     * Open and return a data output stream for a connection.
     *
     * @return An output stream
     * @throws IOException if an I/O error occurs
     */
    public final DataOutputStream openDataOutputStream() throws IOException {
        return new DataOutputStream(openOutputStream());
    }

    /*
     * Open and return an input stream for a connection.
     *
     * @return An input stream
     * @throws IOException if an I/O error occurs
     */
    public final InputStream openInputStream() throws IOException {
        checkOpen();
        checkReadMode();
        synchronized (this) {
            if (isOpened) {
                throw new IOException("No more input streams");
            }
            isOpened = true;
            objects++;
        }
        return new SPPInputStream();
    }

    /*
     * Open and return an output stream for a connection.
     *
     * @return An output stream
     * @throws IOException if an I/O error occurs
     */
    public final OutputStream openOutputStream() throws IOException {
        checkOpen();
        checkWriteMode();
        synchronized (this) {
            if (osOpened) {
                throw new IOException("No more output streams");
            }
            osOpened = true;
            objects++;
        }
        return new SPPOutputStream();
    }

    /*
     * Input stream implementation for BTSPPConnection
     */
    private final class SPPInputStream extends InputStream {
        /* Indicates whether the stream is closed. */
        private boolean isClosed;

        /*
         * Reads the next byte of data from the input stream.
         *
         * @return the next byte of data, or <code>-1</code> if the end of the
         *         stream is reached.
         * @exception IOException if an I/O error occurs.
         */
        public int read() throws IOException {
            byte[] buf = new byte[1];
            int res = read(buf);
            if (res != -1)
                res = buf[0] & 0xFF;
            return res;
        }

        /*
         * Reads some number of bytes from the input stream and stores them into
         * the buffer array <code>buf</code>.
         *
         * @param      buf   the buffer into which the data is read.
         * @return     the total number of bytes read into the buffer, or
         *             <code>-1</code> is there is no more data because the end
         *             of the stream has been reached.
         * @exception  IOException  if an I/O error occurs.
         * @see        java.io.InputStream#read(byte[], int, int)
         */
        public int read(byte[] buf) throws IOException {
            return read(buf, 0, buf.length);
        }

        /*
         * Reads up to <code>len</code> bytes of data from the input stream into
         * an array of bytes.  An attempt is made to read as many as
         * <code>len</code> bytes, but a smaller number may be read, possibly
         * zero. The number of bytes actually read is returned as an integer.
         * @param      buf     the buffer into which the data is read.
         * @param      off   the start offset in array <code>buf</code>
         *                   at which the data is written.
         * @param      len   the maximum number of bytes to read.
         * @return     the total number of bytes read into the buffer, or
         *             <code>-1</code> if there is no more data because the end
         *             of the stream has been reached.
         * @exception  IOException  if an I/O error occurs.
         * @see        java.io.InputStream#read()
         */
        public int read(byte[] buf, int off, int len) throws IOException {
            if (isClosed) {
                throw new IOException("Stream is closed");
            }

            if ((off < 0) || (len < 0) || (off + len > buf.length)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }

            int res;

            /*
             * Multiple threads blocked on read operation may
             * return results interleaved arbitrarily. From an
             * application perspective, the results would be
             * indeterministic. So "reader locks" are introduced
             * for "read" operation from the same handle.
             */
            synchronized (readerLock) {
                res = receive0(buf, off, len);
            }

            // convert the 'end of data' to the 'end of stream'
            if (res == 0) {
                res = -1;
            }

            return res;
        }

        /*
         * Returns the number of bytes that can be read (or skipped over) from
         * this input stream without blocking by the next caller of a method for
         * this input stream.  The next caller might be the same thread or
         * another thread.
         *
         * @return     the number of bytes that can be read from this input
         *             stream without blocking.
         * @exception  IOException  if an I/O error occurs.
         */
        public int available() throws IOException {
            if (isClosed) {
                throw new IOException("Stream is closed");
            }
            return available0();
        }

        /*
         * Closes this input stream and releases any system resources associated
         * with the stream.
         *
         * @exception  IOException  if an I/O error occurs.
         */
        public void close() throws IOException {
            synchronized (BTSPPConnectionImpl.this) {
                if (isClosed) {
                    return;
                }
                isClosed = true;
                objects--;
                if (objects == 0) {
                    close0();
                }
            }
        }
    }

    /*
     * Output stream implementation for BTSPPConnection
     */
    private final class SPPOutputStream extends OutputStream {
        /* Indicates whether the stream is closed. */
        private boolean isClosed;

        /*
         * Writes the specified byte to this output stream.
         * @param      b   the <code>byte</code>.
         * @exception  IOException  if an I/O error occurs. In particular,
         *             an <code>IOException</code> may be thrown if the
         *             output stream has been closed.
         */
        public void write(int b) throws IOException {
            write(new byte[] { (byte)b });
        }

        /*
         * Writes <code>size</code> bytes from the specified byte array
         * starting at offset <code>offset</code> to this output stream.
         * @param      buf the data.
         * @param      offset the start offset in the data.
         * @param      size the number of bytes to write.
         * @exception  IOException  if an I/O error occurs. In particular,
         *             an <code>IOException</code> is thrown if the output
         *             stream is closed.
         */
        public void write(byte[] buf, int offset, int size)
                throws IOException {
            if (isClosed) {
                throw new IOException("Stream is closed");
            }

            if (size < 0 || offset < 0 || offset + size > buf.length) {
                throw new IndexOutOfBoundsException();
            }

            /*
             * Multiple threads blocked on write operation may return results
             * interleaved arbitrarily. From an application perspective, the
             * results would be indeterministic. So "writer locks" are
             * introduced for "write" operation to the same socket.
             */
            synchronized (writerLock) {
                while (size > 0) {
                    int res = send0(buf, offset, size);

                    if (res <= 0) {
                        throw new IOException("Data send failed");
                    }

                    offset  += res;
                    size -= res;
                }
            }
        }

        /*
         * Closes this output stream and releases any system resources
         * associated with this stream.
         * @exception  IOException  if an I/O error occurs.
         */
        public void close() throws IOException {
            synchronized (BTSPPConnectionImpl.this) {
                if (isClosed) {
                    return;
                }
                isClosed = true;
                objects--;
                if (objects == 0) {
                    close0();
                }
            }
        }
    }

    /*
     * Closes this connection.
     * @throws IOException if I/O error.
     */
    public void close() throws IOException {
        synchronized (this) {
            if (isClosed()) {
                return;
            }
            resetRemoteDevice();
            objects--;
            if (objects == 0) {
                close0();
            }
        }
    }

    /*
     * Closes client connection.
     *
     * Note: the method gets native connection handle directly from
     * <code>handle<code> field of <code>L2CAPConnectionImpl</code> object.
     *
     * @throws IOException if any I/O error occurs
     */
    private native void close0() throws IOException;

    /*
     * Reads data from a packet received via Bluetooth stack.
     *
     * Note: the method gets native connection handle directly from
     * <code>handle<code> field of <code>BTSPPConnectionImpl</code> object.
     *
     * @param buf the buffer to read to
     * @param off the start offset in array <code>buf</code>
     *               at which the data to be written
     * @param size the maximum number of bytes to read,
     *             the rest of the packet is discarded.
     * @return total number of bytes read into the buffer,
     *             <code>0</code> indicates end-of-data,
     *             <code>-1</code> if there is no data available at this moment
     * @throws IOException if an I/O error occurs
     */
    protected native int receive0(byte[] buf, int off, int size)
        throws IOException;

    /*
     * Returns the number of bytes available to be read from the connection
     * without blocking.
     *
     * Note: the method gets native connection handle directly from
     * <code>handle<code> field of <code>BTSPPConnectionImpl</code> object.
     *
     * @return the number of available bytes
     * @throws IOException if any I/O error occurs
     */
    private native int available0() throws IOException;

    /*
     * Sends the specified data via Bluetooth stack.
     *
     * Note: the method gets native connection handle directly from
     * <code>handle<code> field of <code>L2CAPConnectionImpl</code> object.
     *
     * @param buf the data to send
     * @param off the offset into the data buffer
     * @param size the size of data in the buffer
     * @return total number of send bytes,
     *         or <code>-1</code> if nothing is send
     * @throws IOException if an I/O error occurs
     */
    protected native int send0(byte[] buf, int off, int size) throws IOException;


    /* Opens client connection. */
    private void doOpen() throws IOException {
        /*
         * create native connection object
         * Note: the method <code>create0</code> sets resulting native
         * connection handle directly to the field <code>handle<code>.
         */
        create0(url.authenticate, url.encrypt, url.master);

        byte[] address = BluetoothUtils.getAddressBytes(url.address);

        try {
            // establish connection
            connect0(address, url.port);
        } catch (IOException e) {
            throw new BluetoothConnectionException(
                BluetoothConnectionException.FAILED_NOINFO,
                e.getMessage());

        }
    }

    /*
     * Creates a client connection object.
     *
     * Note: the method gets native connection handle directly from
     * <code>handle<code> field of <code>BTSPPConnectionImpl</code> object.
     *
     * @param auth   <code>true</code> if authication is required
     * @param enc    <code>true</code> indicates
     *                what connection must be encrypted
     * @param master <code>true</code> if client requires to be
     *               a connection's master
     * @throws IOException if any I/O error occurs
     */
    private native void create0(boolean auth, boolean enc, boolean master)
        throws IOException;

    /*
     * Starts client connection establishment.
     *
     * Note: the method gets native connection handle directly from
     * <code>handle<code> field of <code>BTSPPConnectionImpl</code> object.
     *
     * @param addr bluetooth address of device to connect to
     * @param cn Channel number (CN) value
     * @throws IOException if any I/O error occurs
     */
    private native void connect0(byte[] addr, int cn) throws IOException;

}
