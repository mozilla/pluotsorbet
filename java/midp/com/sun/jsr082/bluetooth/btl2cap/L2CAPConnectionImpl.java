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
package com.sun.jsr082.bluetooth.btl2cap;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Vector;
import javax.bluetooth.L2CAPConnection;
import javax.bluetooth.BluetoothConnectionException;
import com.sun.jsr082.bluetooth.BluetoothConnection;
import com.sun.jsr082.bluetooth.BluetoothUrl;
import com.sun.jsr082.bluetooth.BluetoothUtils;

/*
 * Provides the <code>javax.bluetooth.L2CAPConnection</code>
 * connection implemetation.
 */
public class L2CAPConnectionImpl extends BluetoothConnection
        implements L2CAPConnection {
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
     * Negotiated ReceiveMTU and TransmitMTU.
     * 16 high bits is ReceiveMTU, 16 low bits is TransmitMTU.
     *
     * This packeted value is returned by L2CAPConnectionImpl.connect0 and
     * L2CAPNotifierImpl.accept0 methods and  decoded by doReceiveMTU
     * and doTransmitMTU methods.
     */
    int mtus = (((-1) << 16) & 0xFFFF0000) & ((-1) & 0xFFFF);

    /*
     * Identifies this connection at native layer,
     * <code>-1<code> if connection is not open.
     *
     * Note: in real mode this field is accessed only from native code.
     */
    private int handle = -1;

    /* The receive MTU for the connection. */
    private int receiveMTU  = -1;

    /* The transmit MTU for the connection. */
    private int transmitMTU = -1;

    /*
     * Constructs an instance and opens connection.
     *
     * @param url keeps connection details
     * @param mode I/O access mode
     * @exception IOException if connection fails
     */
    protected L2CAPConnectionImpl(BluetoothUrl url, int mode)
            throws IOException {
        this(url, mode, null);
    }

    /*
     * Constructs an instance and
     * sets up corresponding native connection handle to it.
     *
     * @param url keeps connection details
     * @param mode I/O access mode
     * @param notif corresponding <code>L2CAPNotifierImpl</code> instance
     *              temporary storing native peer handle
     * @exception IOException if connection fails
     */
    protected L2CAPConnectionImpl(BluetoothUrl url,
            int mode, L2CAPNotifierImpl notif) throws IOException {
        super(url, mode);

        if (notif == null) {
            remoteDeviceAddress = BluetoothUtils.getAddressBytes(url.address);
            doOpen();
        } else {
            remoteDeviceAddress = new byte[6];
            System.arraycopy(notif.peerAddress, 0,  remoteDeviceAddress, 0, 6);

            setThisConnHandle0(notif);
            // copy negotiated MTUs returned by L2CAPNotifierImpl.accept0
            mtus = notif.mtus;
        }

        receiveMTU = (mtus >> 16) & 0xFFFF;
        transmitMTU = mtus & 0xFFFF;

        // Check whether transmit MTU was increased during connection
        // establishment phase. If it was, set original MTU value.
        // IMPL_NOTE: pass updated transmit MTU to underlaying Bluetooth stack.
        if (url.transmitMTU != -1 &&
                transmitMTU > url.transmitMTU) {
            transmitMTU = url.transmitMTU;
        }

        setRemoteDevice();
    }

    /*
     * Retrieves native connection handle from temporary storage
     * inside <code>L2CAPNotifierImpl</code> instance
     * and sets it to this <code>L2CAPConnectionImpl</code> instance.
     *
     * Note: the method sets native connection handle directly to
     * <code>handle<code> field of <code>L2CAPConnectionImpl</code> object.
     *
     * @param notif reference to corresponding <code>L2CAPNotifierImpl</code>
     *              instance storing native peer handle
     */
    private native void setThisConnHandle0(L2CAPNotifierImpl notif);

    /*
     * Retrieves address of remote device on the other side of this connection.
     *
     * @return remote device address
     */
    public String getRemoteDeviceAddress() {
        return BluetoothUtils.getAddressString(remoteDeviceAddress);
    }

    /*
     * Returns ReceiveMTU.
     *
     * @return receive MTU
     *
     * @throws IOException if the connection is closed.
     */
    public final int getReceiveMTU() throws IOException {
        if (isClosed()) {
            throw new IOException("Connection is closed");
        }

        return receiveMTU;
    }

    /*
     * Returns TransmitMTU.
     *
     * @return transmit MTU
     *
     * @throws IOException if the connection is closed.
     */
    public final int getTransmitMTU() throws IOException {
        if (isClosed()) {
            throw new IOException("Connection is closed");
        }

        return transmitMTU;
    }

    /*
     * Sends given bytes to this connection.
     *
     * Note: the method is non-blocking.
     *
     * @param data bytes to send.
     *
     * @throws IOException if either connection is closed or I/O error occured
     */
    public void send(byte[] data) throws IOException {
        checkOpen();
        checkWriteMode();
        if (data == null) {
            throw new NullPointerException("The data is null");
        }

        int len = (data.length < transmitMTU) ? data.length : transmitMTU;
        int sentBytes;

        /*
         * Multiple threads blocked on write operation may return results
         * interleaved arbitrarily. From an application perspective, the
         * results would be indeterministic. So "writer locks" are
         * introduced for "write" operation to the same socket.
         */
        synchronized (writerLock) {
            sentBytes = sendData(data, 0, len);
        }

        if (sentBytes != len) {
            throw new IOException("Data sending failed");
        }
    }

    /*
     * Receives data from this connection.
     *
     * Note: The method is blocking.
     *
     * @param buf byte array to place data received to
     * @return amount of bytes received
     * @throws IOException if either connection is closed or I/O error occured
     */
    public int receive(byte[] buf) throws IOException {
        checkOpen();
        checkReadMode();
        if (buf == null) {
            throw new NullPointerException("The buffer is null");
        }
        if (buf.length == 0) {
            return 0;
        }
        int len = (buf.length > receiveMTU) ? receiveMTU : buf.length;

        /*
         * Multiple threads blocked on read operation may
         * return results interleaved arbitrarily. From an
         * application perspective, the results would be
         * indeterministic. So "reader locks" are introduced
         * for "read" operation from the same handle.
         */
        synchronized (readerLock) {
            return receiveData(buf, 0, len);
        }
    }

    /*
     * Checks if there is data to receive without blocking.
     * @return true if any data can be retrieved via
     *        <code>receive()</code> method without blocking.
     * @throws IOException if the connection is closed.
     */
    public boolean ready() throws IOException {
        checkOpen();
        return ready0();
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
        }
        close0();
    }

    /*
     * Receives data from this connection.
     *
     * Note: the method gets native connection handle directly from
     * <code>handle<code> field of <code>L2CAPConnectionImpl</code> object.
     *
     * @param buf the buffer to read to
     * @param off start offset in <code>buf</code> array
     *               at which the data to be written
     * @param size the maximum number of bytes to read,
     *             the rest of the packet is discarded.
     * @return total number of bytes read into the buffer or
     *             <code>0</code> if a zero length packet is received
     * @throws IOException if an I/O error occurs
     */
    protected int receiveData(byte[] buf, int off, int size)
            throws IOException {
        return receive0(buf, off, size);
    }

    /*
     * Receives data from this connection.
     *
     * Note: the method gets native connection handle directly from
     * <code>handle<code> field of <code>L2CAPConnectionImpl</code> object.
     *
     * @param buf the buffer to read to
     * @param off start offset in <code>buf</code> array
     *               at which the data to be written
     * @param size the maximum number of bytes to read,
     *             the rest of the packet is discarded.
     * @return total number of bytes read into the buffer or
     *             <code>0</code> if a zero length packet is received
     * @throws IOException if an I/O error occurs
     */
    protected native int receive0(byte[] buf, int off, int size)
            throws IOException;

    /*
     * Sends the specified data to this connection.
     *
     * Note: the method gets native connection handle directly from
     * <code>handle<code> field of <code>L2CAPConnectionImpl</code> object.
     *
     * @param buf the data to send
     * @param off the offset into the data buffer
     * @param len the length of the data in the buffer
     * @return total number of send bytes,
     *         or <code>-1</code> if nothing is send
     * @throws IOException if an I/O error occurs
     */
    protected int sendData(byte[] buf, int off, int len) throws IOException {
        return send0(buf, off, len);
    }

    /*
     * Sends the specified data to this connection.
     *
     * Note: the method gets native connection handle directly from
     * <code>handle<code> field of <code>L2CAPConnectionImpl</code> object.
     *
     * @param buf the data to send
     * @param off the offset into the data buffer
     * @param len the length of the data in the buffer
     * @return total number of send bytes,
     *         or <code>-1</code> if nothing is send
     * @throws IOException if an I/O error occurs
     */
    protected native int send0(byte[] buf, int off, int len) throws IOException;

    /*
     * Checks if there is data to receive without blocking.
     *
     * Note: the method gets native connection handle directly from
     * <code>handle<code> field of <code>L2CAPConnectionImpl</code> object.
     *
     * @return <code>true</code> if a packet is present,
     *         <code>false</code> otherwise
     * @throws IOException if any I/O error occurs
     */
    private native boolean ready0() throws IOException;

    /*
     * Closes client connection.
     *
     * Note: the method gets native connection handle directly from
     * <code>handle<code> field of <code>L2CAPConnectionImpl</code> object.
     *
     * @throws IOException if any I/O error occurs
     */
    private native void close0() throws IOException;


    /* Opens client connection */
    private void doOpen() throws IOException {
        // create native connection object
        // Note: the method sets resulting native connection handle
        // directly to field <code>handle<code>.
        create0(url.receiveMTU, url.transmitMTU, url.authenticate,
            url.encrypt, url.master);

        byte[] address = BluetoothUtils.getAddressBytes(url.address);

        try {
            // establish connection
            mtus = connect0(address, url.port);
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
     * <code>handle<code> field of <code>L2CAPConnectionImpl</code> object.
     *
     * @param imtu receive MTU or <code>-1</code> if not specified
     * @param omtu transmit MTU or <code>-1</code> if not specified
     * @param auth   <code>true</code> if authication is required
     * @param enc    <code>true</code> indicates
     *                what connection must be encrypted
     * @param master <code>true</code> if client requires to be
     *               a connection's master
     * @throws IOException if any I/O error occurs
     */
    private native void create0(int imtu, int omtu, boolean auth,
        boolean enc, boolean master) throws IOException;

    /*
     * Starts client connection establishment.
     *
     * Note: the method gets native connection handle directly from
     * <code>handle<code> field of <code>L2CAPConnectionImpl</code> object.
     *
     * @param addr bluetooth address of device to connect to
     * @param psm Protocol Service Multiplexor (PSM) value
     * @return Negotiated ReceiveMTU and TransmitMTU.
     *               16 high bits is ReceiveMTU, 16 low bits is TransmitMTU.
     * @throws IOException if any I/O error occurs
     */
    private native int connect0(byte[] addr, int psm) throws IOException;

}
