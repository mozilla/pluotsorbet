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

package com.sun.midp.io.j2me;

// Interfaces
import com.sun.j2me.io.ConnectionBaseInterface;
import javax.microedition.io.StreamConnection;
import javax.wireless.messaging.MessageConnection;

// Classes
import com.sun.j2me.app.AppPackage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.Connection;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageListener;

// Exceptions
import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * Base class for SMS/CBS/MMS message connection implementation.
 *
 * <code>Protocol</code> itself is not instantiated. Instead, the application
 * calls <code>Connector.open</code> with an URL string and obtains a
 * {@link javax.wireless.messaging.MessageConnection MessageConnection}
 *  object. It is an instance of <code>MessageConnection</code>
 * that is instantiated. The Generic Connection Framework mechanism
 * in CLDC will return a <code>Protocol</code> object, which is the
 * implementation of <code>MessageConnection</code>. The
 * <code>Protocol</code> object represents a connection to a low-level transport
 * mechanism.
 * <p>
 * Optional packages, such as <code>Protocol</code>, cannot reside in
 * small devices.
 * The Generic Connection Framework allows an application to reach the
 * optional packages and classes indirectly. For example, an application
 * can be written with a string that is used to open a connection. Inside
 * the implementation of <code>Connector</code>, the string is mapped to a
 * particular implementation: <code>Protocol</code>, in this case. This allows
 * the implementation to be optional even though
 * the interface, <code>MessageConnection</code>, is required.
 * <p>
 * Closing the connection frees an instance of <code>MessageConnection</code>.
 * <p>
 * The <code>Protocol</code> class contains methods
 * to open and close the connection to the low-level transport mechanism. The
 * messages passed on the transport mechanism are defined by the
 * {@link MessageObject MessageObject}
 * class.
 * Connections can be made in either client mode or server mode.
 * <ul>
 * <li>Client mode connections are for sending messages only. They are
 * created by passing a string identifying a destination address to the
 * <code>Connector.open()</code> method.</li>
 * <li>Server mode connections are for receiving and sending messages. They
 * are created by passing a string that identifies a port, or equivalent,
 * on the local host to the <code>Connector.open()</code> method.</li>
 * </ul>
 * The class also contains methods to send, receive, and construct
 * <code>Message</code> objects.
 * <p>
 * <p>
 * This class declares that it implements <code>StreamConnection</code>
 * so it can intercept calls to <code>Connector.open*Stream()</code>
 * to throw an <code>IllegalArgumentException</code>.
 * </p>
 *
 */

public abstract class ProtocolBase implements MessageConnection,
                                 ConnectionBaseInterface,
                                 StreamConnection {

    /** Prefic for addressed message connections. */
    protected String ADDRESS_PREFIX;

    /** Handle to the MIDlet suite containing this MIDlet. */
    protected AppPackage appPackage;

    /**
     * Indicates whether the connection is open or closed. If it is closed,
     * subsequent operations should throw an exception.
     */
    protected boolean open = false;

    /** Local handle to connection */
    protected int connHandle = 0;

    /** Connection parameter from the URL. */
    protected String appID = null;

    /** Connector mode. */
    protected int m_mode = 0;

    /** Machine name - the parsed target address from the URL. */
    protected String host = null;

    /** Message listener for async notifications. */
    volatile MessageListener m_listener = null;

    /** Listener thread. */
    Thread m_listenerThread = null;

    /** Used to protect operations with listener thread */
    protected Object listenerLock = new Object();

    /** Used to protect read-modify operation on open field during close() */
    protected Object closeLock = new Object();

    /**
     * Indicates whether a trusted application is allowed to open the
     * message connection. Set to true if the permission check passes.
     * Note: return true to override Security Permissions
     */
    protected boolean openPermission = false;

    /**
     * Indicates whether a trusted application is allowed to read from the
     * message connection. Set to true if the permission check passes.
     * Note: return true to override Security Permissions
     */
    protected boolean readPermission = false;

    /**
     * Indicates whether a trusted application is allowed to write to the
     * message connection. Set to true if the permission check passes.
     * Note: return true to override Security Permissions
     */
    protected boolean writePermission = false;

    /** Creates a message connection protocol handler. */
    public ProtocolBase() {
        appPackage = AppPackage.getInstance();
        // IMPL_NOTE: should be moved to a JSROP initializer
        //    com.sun.jump.driver.wma.Listener.startListener();
    }
    /**
     * Construct a new message object from the given type.
     *
     * @param type <code>MULTIPART_MESSAGE</code> is the only type permitted.
     *
     * @return A new MMS <code>Message</code> object.
     */
    public abstract Message newMessage(String type);

    /**
     * Constructs a new message object from the given type and address.
     *
     * @param type <code>TEXT_MESSAGE</code> or
     *     <code>BINARY_MESSAGE</code>.
     * @param addr the destination address of the message.
     * @return a new <code>Message</code> object.
     */
    public abstract Message newMessage(String type, String addr);

    /**
     * Receives the bytes that have been sent over the connection, constructs a
     * <code>Message</code> object, and returns it.
     * <p>
     * If there are no <code>Message</code>s waiting on the connection, this
     * method will block until a message is received, or the
     * <code>MessageConnection</code> is closed.
     *
     * @return a <code>Message</code> object.
     * @exception java.io.IOException if an error occurs while receiving a
     *     message.
     * @exception java.io.InterruptedIOException if during this method call this
     *     <code>MessageConnection</code> object is closed.
     * @exception java.lang.SecurityException if the application doesn't have
     *      permission to receive messages on the given port.
     */
    public abstract Message receive() throws IOException;

    /**
     * Sends a message over the connection. This method extracts the data
     * payload from the <code>Message</code> object so that it can be sent as a
     * datagram.
     *
     * @param     dmsg a <code>Message</code> object
     * @exception java.io.IOException if the message could not be sent or
     *     because of network failure
     * @exception java.lang.IllegalArgumentException if the message contains
     *     invalid information or is incomplete, or the message's payload
     *     exceeds the maximal length for the given protocol.
     * @exception java.io.InterruptedIOException either if this
     *     <code>Connection</code> object is closed during the execution of this
     *     <code>send</code> method or if a timeout occurs while trying to send
     *     the message.
     * @exception java.lang.NullPointerException if the parameter is
     *     <code>null</code>.
     * @exception java.lang.SecurityException if the application doesn't have
     *      permission for sending the message.
     */
    public abstract void send(Message dmsg) throws IOException;

    /**
     * Ensures that the connection is open.
     * @exception IOException if the connection is closed
     */
    public void ensureOpen() throws IOException {
        if (!open) {
            throw new IOException("Connection closed");
        }
    }

    protected boolean needStopReceiver = false;

    /**
     * Generates InterruptedIOException when connection is closed.
     * @param ex input IOException
     * @param name name of operation: sending or receiving
     * @exception IOException if the connection is not closed
     */
    protected void io2InterruptedIOExc(IOException ex, String name) 
            throws IOException, InterruptedIOException {
        try {
            ensureOpen();
        } catch (IOException ioe) {
            throw new InterruptedIOException("Connection closed " +
                                         "during " + name);
        }
        throw ex;
    }

    /**
     * Registers a <code>MessageListener</code> object.
     * <p>
     * The platform will notify this listener object when a message has been
     * received to this <code>MessageConnection</code>.
     * </p>
     * <p>If the queue of this <code>MessageConnection</code> contains some
     * incoming messages that the application haven't read before the call
     * of this method, the newly registered listener will be notified
     * immediately exactly once for each such message in the queue.
     * </p>
     * <p>There can be at most one listener object registered for
     * a <code>MessageConnection</code> object at any given point in time.
     * Setting a new listener will implicitly de-register the possibly
     * previously set listener.
     * </p>
     * <p>Passing <code>null</code> as the parameter de-registers the currently
     * registered listener, if any.
     * </p>
     * @param listener <code>MessageListener</code> object to be registered.
     *                 If <code>null</code>,
     *                 the possibly currently registered listener will be
     *                 de-registered and will not receive notifications.
     * @exception java.lang.SecurityException if the application does not
     *         have a permission to receive messages using the given port
     *         number
     * @exception java.io.IOException if it is requested to register
     *            a listener on a client connection or if the connection
     *            has been closed
     */
    public void setMessageListener(MessageListener listener)
            throws IOException {

        needStopReceiver = false;

        if (listener != null) {
            /*
             * Make sure the connection is still open.
             */
            ensureOpen();

            /*
             * Check if we have permission to recieve.
             */
            checkReceivePermission();

            /*
             * Don't let the application waste time listening on a client
             * connection, which can not be used for receive operations.
             */
            if (host != null && host.length() > 0) {
                throw new IOException("Cannot listen on client connection");
            }
        }

        synchronized (listenerLock) {
            if ((m_listener != null) && (listener == null)) {
                needStopReceiver = true;
            }
            m_listener = listener;
            /* Start a new receive thread when need */
            if ((listener != null) && (m_listenerThread == null)) {
                startReceiverThread();
            }
        }

        /* Kill listener when need */
        if (needStopReceiver) {
            /* Close thread without deregistering */
            close00(connHandle, 0);
            try {
                m_listenerThread.join();
            } catch (InterruptedException ie) {
            }  /* Ignore interrupted exception */
            m_listenerThread = null;

            /* Unblock the low level */
            unblock00(appPackage.UNUSED_APP_ID);
        }
    }

    /**
     * Gets the connection parameter in string mode.
     * @return string that contains a parameter 
     */
    protected abstract String getAppID();

    /**
     * Sets the connection parameter in string mode.
     * @param newValue new value of connection parameter 
     */
    protected abstract void setAppID(String newValue);

    /**
     * Unblock the receive thread.
     *
     * @param msid The MIDlet suite ID.
     *
     * @return  returns handle to the connection.
     */
    protected abstract int unblock00(int msid) throws IOException;

    /**
     * Close connection.
     *
     * @param connHandle handle returned by open0
     * @param deRegister Deregistration appID when parameter is 1.
     * @return    0 on success, -1 on failure
     */
    protected abstract int close00(int connHandle, int deRegister);

    /**
     * Checks internal setting of receive permission.
     * Called from receive and setMessageListener methods.
     * @exception InterruptedIOException if permission dialog
     * was preempted
     */
    protected abstract void checkReceivePermission() throws InterruptedIOException;

    /** Waits until message available and notify listeners */
    /**
     * Start receiver thread
     */
    private void startReceiverThread() {
        final MessageConnection messageConnection = this;

        if (m_listenerThread == null) {

            m_listenerThread = new Thread() {
               /**
                 * Run the steps that wait for a new message.
                 */
                public void run() {
                    int messageLength = 0;
                    do {
                        /* No message, initially. */
                        messageLength = -1;
                        try {
                            messageLength =
                                waitUntilMessageAvailable00(connHandle);
                            if (needStopReceiver) {
                                break;
                            }
                            /*
                             * If a message is available and there are
                             * listeners, notify all listeners of the
                             * incoming message.
                             */
                            if (messageLength >= 0) {
                                synchronized (listenerLock) {
                                    if (m_listener != null) {
                                        // Invoke registered listener.
                                        m_listener.notifyIncomingMessage(
                                            messageConnection);
                                    }
                                }
                            }
                        } catch (InterruptedIOException iioe) {
                            /*
                             * Terminate, the reader thread has been
                             * interrupted
                             */
                            break;
                        } catch (IOException exception) {
                            break;
                        } catch (IllegalArgumentException iae) {
                            /*
                             * This happens if port has been set to 0;
                             * which indicates that the connection has been
                             * closed. So Terminate.
                             */
                            break;
                         }
                    } while (!needStopReceiver);
                }
            };

            m_listenerThread.start();
        }
    }

    /**
     * Waits until message available
     *
     * @param handle handle to connection
     * @return 0 on success, -1 on failure
     * @exception IOException  if an I/O error occurs
     */
    protected abstract int waitUntilMessageAvailable00(int handle) throws IOException;

    public abstract Connection openPrim(String name, int mode, boolean timeouts) throws IOException;
    public abstract void close() throws IOException;
    public abstract DataInputStream openDataInputStream() throws IOException;
    public abstract DataOutputStream openDataOutputStream() throws IOException;
    public abstract InputStream openInputStream() throws IOException;
    public abstract OutputStream openOutputStream() throws IOException;
}
