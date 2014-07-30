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

package com.sun.midp.io.j2me.socket;

import java.io.IOException;
import java.io.InterruptedIOException;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import javax.microedition.io.ConnectionNotFoundException;

import com.sun.j2me.security.AccessController;
import com.sun.j2me.security.InterruptedSecurityException;

import com.sun.midp.io.NetworkConnectionBase;
import com.sun.midp.io.HttpUrl;
import com.sun.midp.io.Util;

import com.sun.midp.main.Configuration;

import com.sun.midp.security.Permissions;
import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.ImplicitlyTrustedClass;
import com.sun.midp.security.SecurityInitializer;
import com.sun.midp.suspend.NetworkSubsystem;
import com.sun.midp.suspend.Subsystem;
import com.sun.midp.suspend.StateTransitionException;

/** Connection to the J2ME socket API. */
public class Protocol extends NetworkConnectionBase
        implements SocketConnection, Subsystem {

    /** TCP client permission name. */
    private static final String CLIENT_PERMISSION_NAME =
        "javax.microedition.io.Connector.socket";

    /** Class registered in SecurityInitializer. */
    private static class SecurityTrusted implements ImplicitlyTrustedClass {}

    /**Security token for provileged access to internal API's. */
    private static SecurityToken classSecurityToken =
        SecurityInitializer.requestToken(new SecurityTrusted());

    /** Size of the read ahead buffer, default is no buffering. */
    private static int bufferSize;

    /**
     * Handle to native socket object. This is set and get only by 
     * native code.
     */
    private int handle = -1;

    /** Lock object for reading from the socket */
    private final Object readerLock = new Object();

    /** Lock object for writing to the socket */
    private final Object writerLock = new Object();

    /**
     * Class initializer
     */
    static {
        /* See if a read ahead / write behind buffer size has been specified */
        bufferSize = Configuration.getNonNegativeIntProperty(
                         "com.sun.midp.io.j2me.socket.buffersize", bufferSize);
    }

    /** Hostname */
    private String host;

    /** TCP port */
    private int port;

    /** Shutdown output flag, true if output has been shutdown. */
    private boolean outputShutdown;

    /** True if the owner of this connection is trusted. */
    private boolean ownerTrusted;

    /** Byte array that represents the IP address */
    byte[] ipBytes = new byte[4];

    /** Creates a buffered TCP client connection. */
    public Protocol() {
        // use the default buffer size
        super(bufferSize);
    }

    /**
     * Open a client or server socket connection.
     * <p>
     * The name string for this protocol should be:
     * "socket://&lt;name or IP number&gt;:&lt;port number&gt;
     * <p>
     * We allow "socket://:nnnn" to mean an inbound server socket connection.
     *
     * @param name       the target for the connection
     * @param mode       I/O access mode
     * @param timeouts   a flag to indicate that the caller wants
     *                   timeout exceptions
     *
     * @return client or server TCP socket connection
     *
     * @exception  IOException  if an I/O error occurs.
     * @exception  ConnectionNotFoundException  if the host cannot be connected
     *              to
     * @exception  IllegalArgumentException  if the name is malformed
     */
    public Connection openPrim(String name, int mode, boolean timeouts)
            throws IOException {
        return open(null, name, mode);
    }

    /**
     * Make sure the calling call has the com.sun.midp permission set to
     * "allowed" and open a connection to a target.
     * Used by internal classes only so they can do work on behalf of suites
     * that do not have the directly use this protocol.
     * 
     * @param token            security token of the calling class
     * @param name             URL for the connection, without the
     *                         without the protocol part
     *
     * @return                 this Connection object
     *
     * @exception IllegalArgumentException If a parameter is invalid.
     * @exception ConnectionNotFoundException If the connection cannot
     *                                        be found.
     * @exception IOException  If some other kind of I/O error occurs.
     */
    public Connection openPrim(SecurityToken token, String name)
            throws IOException {
        return open(token, name, Connector.READ_WRITE);
    }

    /**
     * Open a client or server socket connection.
     * <p>
     * The name string for this protocol should be:
     * "socket://&lt;name or IP number&gt;:&lt;port number&gt;
     * <p>
     * We allow "socket://:nnnn" to mean an inbound server socket connection.
     *
     * @param token      security token of the calling class
     * @param name       the target for the connection
     * @param mode       I/O access mode
     *
     * @return client or server TCP socket connection
     *
     * @exception  IOException  if an I/O error occurs.
     * @exception  ConnectionNotFoundException  if the host cannot be connected
     *              to
     * @exception  IllegalArgumentException  if the name is malformed
     */
    private Connection open(SecurityToken token, String name, int mode)
            throws IOException {
        HttpUrl url;
        ServerSocket serverSocket;

        if (name.charAt(0) != '/' || name.charAt(1) != '/') {
            throw new IllegalArgumentException(
                      "Protocol must start with \"//\"");
        }

        url = new HttpUrl("socket", name); // parse name into host and port

        /*
         * Since we reused the HttpUrl parser, we must make sure that
         * there was nothing past the authority in the URL.
         */
        if (url.path != null || url.query != null || url.fragment != null) {
            throw new IllegalArgumentException("Malformed address");
        }

        NetworkSubsystem.getInstance(classSecurityToken).ensureActive();

        host = url.host;
        port = url.port;
        
        /*
         * If 'host' == null then we are a server endpoint at
         * port 'port'.
         */

        if (host != null) {
            checkForPermission(name, token);
            initStreamConnection(mode);

            // this will call the connect method which uses the host and port
            connect();
            return this;
        }

        // We allow "socket://:nnnn" to mean an inbound TCP server socket.
        try {
            serverSocket = (ServerSocket)Class.forName(
                  "com.sun.midp.io.j2me.serversocket.Socket").newInstance();
        } catch (Exception e) {
            throw new ConnectionNotFoundException("Connection not supported");
        }
            
        serverSocket.open(port, token);
        return (Connection)serverSocket;
    }

    /**
     * Connect to a server.
     * @exception  IOException  if an I/O error occurs.
     * @exception  ConnectionNotFoundException  if the host cannot be connected
     *              to
     * @exception  IllegalStateException  if there is no hostname
     * @exception  IllegalArgumentException  if the name is malformed
     */
    private void connect() throws IOException {

        byte[] szHost;
        byte[] szIpBytes = null;
        int result;
        // Max length of IPv4 address is 4  
        // IMPL NOTE: IPv6 needs to have an address of length =16

        if (handle != -1) {
            // This method should only be called once.
            // IMPL NOTE: should use something other than handle for this check
            throw new RuntimeException("Illegal state for operation");
        }

        /*
         * The host and port were set by overriding the openPrim method of
         * our super class.
         */

        if (port < 0) {
            throw new IllegalArgumentException("Missing port number");
        }

        result = getIpNumber0(host, ipBytes);
        if (result == -1) {
            throw new
                ConnectionNotFoundException("Could not resolve hostname");
        }

        /*
         * JTWI security check, untrusted MIDlets cannot open port 80 or
         * 8080 or 443. This is so they cannot perform HTTP and HTTPS
         * requests on server without using the system code. The
         * system HTTP code will add a "UNTRUSTED/1.0" to the user agent
         * field for untrusted MIDlets.
         */
        if (!ownerTrusted && (port == 80 || port == 8080 || port == 443)) {
            throw new SecurityException(
                "Target port denied to untrusted applications");
        }

        open0(ipBytes, port);

        NetworkSubsystem.getInstance(classSecurityToken).
                registerSubsystem(this);
    }

    /**
     * Create a Java connection object from an open TCP socket.
     * This method is only used by com.sun.midp.io.j2me.serversocket.Socket;
     *
     * @param token     either <code>null</code> for normal processing or 
     *                  a security token with special privileges
     *
     * @exception IOException if an I/O error occurs
     */
    public void open(SecurityToken token) throws IOException {
        
        try {
            // The connection needs to be open to call getAddress.
            connectionOpen = true;
            checkForPermission(getAddress(), token);
        } catch (Exception e) {
            connectionOpen = false;

            try {
                close0();
            } catch (IOException ioe) {
                // ignore
            }

            if (e instanceof IOException) {
                throw (IOException)e;
            }

            throw (RuntimeException)e;
        }
    }

    /**
     * Check for the required permission.
     *
     * @param name name of resource to insert into the permission question
     * @param token either security token with special privileges if pecified 
     *        by caller or <code>null</code>
     *
     * @exception InterruptedIOException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    private void checkForPermission(String name, SecurityToken token)
            throws InterruptedIOException {
            
        if (token != null) {
            token.checkIfPermissionAllowed(CLIENT_PERMISSION_NAME);
        
            /* Any caller with the com.sun.midp permission is trusted. */
            ownerTrusted = true;
            
            return;
        }
        
        name = "TCP" + ":" + name;

        try {
            AccessController.checkPermission(CLIENT_PERMISSION_NAME, name);
        } catch (InterruptedSecurityException ise) {
            throw new InterruptedIOException(
                    "Interrupted while trying to ask the user permission");
        }

        try {
            AccessController.
                checkPermission(AccessController.TRUSTED_APP_PERMISSION_NAME);
            ownerTrusted = true;
        } catch (SecurityException se) {
            ownerTrusted = false;
        } 
    }

    /**
     * Notify blocked Java threads waiting for an input data
     * that all InputStream instances of the connection are closed
     */
    protected void notifyClosedInput() {
        if (iStreams == 0) {
            notifyClosedInput0();
        }
    };

    /**
     * Notify blocked Java threads trying to output data
     * that all OutputStream instances of the connection are closed
     */
    protected void notifyClosedOutput() {
        if (iStreams == 0) {
            notifyClosedOutput0();
        }
    }

    /**
     * Wake up Java threads waiting for input data on the connection
     * to let them know all the input streams have been closed 
     */
    private native void notifyClosedInput0();

    /**
     * Wake up Java threads waiting to output data over the connection
     * to let them know all the output streams have been closed
     */
    private native void notifyClosedOutput0();

    /**
     * Disconnect from the server.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void disconnect() throws IOException {
        /*
         * Only shutdown or close of the sending side of a connection is
         * defined in the TCP spec.
         *
         * The receiver can only abort (reset) the entire connection to stop
         * the a sender from sending. InputStream close already causes
         * an reads to fail so no native action is needed.
         *
         * Shutdown the output gracefully closes the sending side of the 
         * TCP connection by sending all pending data and the FIN flag.
         */

        if (!outputShutdown) {
            shutdownOutput0();
        }

        try {
            close0();
        } catch (IOException ioe) {
            // ignore
        }
        
        NetworkSubsystem.getInstance(classSecurityToken).
                unregisterSubsystem(this);        
    }

    /**
     * Reads up to <code>len</code> bytes of data from the input stream into
     * an array of bytes, blocks until at least one byte is available.
     * Sets the <code>eof</code> field of the connection when the native read
     * returns -1.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset in array <code>b</code>
     *                   at which the data is written.
     * @param      len   the maximum number of bytes to read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     */
    protected int nonBufferedRead(byte b[], int off, int len)
        throws IOException {

        int bytesRead;

        for (;;) {
            try {
                /*
                 * Multiple threads blocked on read operation may
                 * return results interleaved arbitrarily. From an
                 * application perspective, the results would be
                 * indeterministic. So "reader locks" are introduced
                 * for "read" operation from the same handle.
                 */ 
                synchronized (readerLock) {
                    bytesRead = read0(b, off, len);
                }
            } finally {
                if (iStreams == 0) {
                    throw new InterruptedIOException("Stream closed");
                }
            }

            if (bytesRead == -1) { 
                eof = true;
                return -1;
            }

            if (bytesRead != 0) {
                return bytesRead;
            }
        }
    }

    /**
     * Returns the number of bytes that can be read (or skipped over) from
     * this input stream without blocking by the next caller of a method for
     * this input stream.  The next caller might be the same thread or
     * another thread.
     *
     * @return     the number of bytes that can be read from this input stream
     *             without blocking.
     * @exception  IOException  if an I/O error occurs.
     */
    public int available() throws IOException {
        if (count > 0) {
            /*
             * The next read will only return the bytes in the buffer,
             * so only return the number of bytes in the buffer.
             * While available can return a number less than than the next
             * read will get, it should not return more.
             */
            return count;
        }

        // The buffer is empty, so the next read will go directly to native
        return available0();
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this output stream.
     * <p>
     * Polling the will be done by our super class.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @return     number of bytes written
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> is thrown if the output
     *             stream is closed.
     */
    public int writeBytes(byte b[], int off, int len) 
           throws IOException {
        /*
         * Multiple threads blocked on write operation may return results 
         * interleaved arbitrarily. From an application perspective, the 
         * results would be indeterministic. So "writer locks" are 
         * introduced for "write" operation to the same socket. 
         */ 
        synchronized (writerLock) {
            return write0(b, off, len);
        }
    }

    /**
     * Called once by the child output stream. The output side of the socket
     * will be shutdown and then the parent method will be called.
     *
     * @exception IOException if the subclass throws one
     */
    protected void closeOutputStream() throws IOException {
        /*
         * Shutdown the output gracefully closes the sending side of the 
         * TCP connection by sending all pending data and the FIN flag.
         */
        shutdownOutput0();
        outputShutdown = true;
        super.closeOutputStream();
    }

    /**
     * Check a socket option to make sure it's a valid option.
     *
     * @param option socket option identifier (KEEPALIVE, LINGER, 
     * SNDBUF, RCVBUF, or DELAY)
     * @exception  IllegalArgumentException if  the value is not 
     *              valid (e.g. negative value)
     *              
     * @see #getSocketOption
     * @see #setSocketOption
     */
    private void checkOption(byte option) 
        throws IllegalArgumentException {
        if (option == SocketConnection.KEEPALIVE 
            || option == SocketConnection.LINGER 
            || option == SocketConnection.SNDBUF 
            || option == SocketConnection.RCVBUF 
            || option == SocketConnection.DELAY) {
            return;
        }
        throw new IllegalArgumentException("Unsupported Socket Option");
            
    }

    /**
     * Set a socket option for the connection.
     * <P>
     * Options inform the low level networking code about intended 
     * usage patterns that the application will use in dealing with
     * the socket connection. 
     * </P>
     *
     * @param option socket option identifier (KEEPALIVE, LINGER, 
     * SNDBUF, RCVBUF, or DELAY)
     * @param value numeric value for specified option (must be positive)
     * @exception  IllegalArgumentException if  the value is not 
     *              valid (e.g. negative value)
     * @exception  IOException  if the connection was closed
     *              
     * @see #getSocketOption
     */
    public void setSocketOption(byte option,  int value) 
        throws IllegalArgumentException, IOException {
        checkOption(option);
        if (value < 0) {
            throw new IllegalArgumentException("Illegal Socket Option Value");
        }
        ensureOpen();

        setSockOpt0(option, value);
    }
    
    /**
     * Get a socket option for the connection.
     *
     * @param option socket option identifier (KEEPALIVE, LINGER, 
     * SNDBUF, RCVBUF, or DELAY)
     * @return positive numeric value for specified option or -1 if the 
     *  value is not available.
     * @exception IllegalArgumentException if the option identifier is 
     *  not valid
     * @exception  IOException  if the connection was closed
     * @see #setSocketOption
     */
    public  int getSocketOption(byte option) 
        throws IllegalArgumentException, IOException  {
        checkOption(option);
        ensureOpen();
        return getSockOpt0(option);
    }
 
    /**
     * Gets the local address to which the socket is bound.
     *
     * <P>The host address(IP number) that can be used to connect to this
     * end of the socket connection from an external system. 
     * Since IP addresses may be dynamically assigned a remote application
     * will need to be robust in the face of IP number reassignment.</P>
     * <P> The local hostname (if available) can be accessed from 
     * <code>System.getProperty("microedition.hostname")</code>
     * </P>
     *
     * @return the local address to which the socket is bound.
     * @exception  IOException  if the connection was closed
     * @see ServerSocketConnection
     */
    public  String getLocalAddress() throws IOException {
        ensureOpen();
        return getHost0(true);
    }

    /**
     * Returns the local port to which this socket is bound.
     *
     * @return the local port number to which this socket is connected.
     * @exception  IOException  if the connection was closed
     * @see ServerSocketConnection
     */
    public  int  getLocalPort()  throws IOException {
        ensureOpen();
        return getPort0(true); 
    }

    /**
     * Gets the remote address to which the socket is bound.
     * The address can be either the remote host name or the IP
     * address(if available).
     *
     * @return the remote address to which the socket is bound.
     * @exception  IOException  if the connection was closed
     */
    public  String getAddress() throws IOException {
        ensureOpen();
        return getHost0(false);
    }
    /**
     * Returns the remote port to which this socket is bound.
     *
     * @return the remote port number to which this socket is connected.
     * @exception  IOException  if the connection was closed
     */
    public  int  getPort() throws IOException {
        ensureOpen();
        return getPort0(false); 
    }

    /**
     * Perform actions required on system resume. Makes nothing.
     * @throws StateTransitionException
     */
    public void resume() throws StateTransitionException {}

    /**
     * Disconnects connection within system suspend procedure.
     */
    public void suspend() {
        try {
            disconnect();
        } catch (IOException e) {
            // ignoring to proceed system pause
        }
    }

    /**
     * Opens a TCP connection to a server.
     *
     * @param szIpBytes  raw IPv4 address of host
     * @param port       TCP port at host
     *
     * @exception  IOException  if an I/O error occurs.
     */
    private native void open0(byte[] szIpBytes, int port) 
        throws IOException;

    /**
     * Reads from the open socket connection.
     *
     * @param      b      the buffer into which the data is read.
     * @param      off    the start offset in array <code>b</code>
     *                    at which the data is written.
     * @param      len    the maximum number of bytes to read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     */
    private native int read0(byte b[], int off, int len)
        throws IOException;

    /**
     * Writes to the open socket connection.
     *
     * @param      b      the buffer of the data to write
     * @param      off    the start offset in array <code>b</code>
     *                    at which the data is written.
     * @param      len    the number of bytes to write.
     * @return     the total number of bytes written
     * @exception  IOException  if an I/O error occurs.
     */
    private native int write0(byte b[], int off, int len)
        throws IOException;

    /**
     * Gets the number of bytes that can be read without blocking.
     *
     * @return     number of bytes that can be read without blocking
     * @exception  IOException  if an I/O error occurs.
     */
    private native int available0() 
        throws IOException;

    /**
     * Closes the socket connection.
     *
     * @exception  IOException  if an I/O error occurs when closing the
     *                          connection.
     */
    private native void close0() 
        throws IOException;

    /**
     * Native finalizer
     */
    private native void finalize();

    /**
     * Gets a byte array that represents an IPv4 or IPv6 address 
     *
     * @param      sHost  the hostname to lookup
     * @param      ipBytes_out  Output array that receives the
     *             bytes of IP address
     * @return     number of bytes copied to ipBytes_out or -1 for an error
     */
    private native int getIpNumber0(String sHost, byte[] ipBytes_out);
    
    /**
     * Gets the requested IP number.
     *
     * @param      local   <tt>true</tt> to get the local host IP address, or
     *                     <tt>false</tt> to get the remote host IP address
     * @return     the IP address as a dotted-quad <tt>String</tt>
     * @exception  IOException  if an I/O error occurs.
     */
    private native String getHost0(boolean local) 
        throws IOException;

    /**
     * Gets the requested port number.
     *
     * @param      local   <tt>true</tt> to get the local port number, or
     *                     <tt>false</tt> to get the remote port number
     * @return     the port number
     * @exception  IOException  if an I/O error occurs.
     */
    private native int getPort0(boolean local) 
        throws IOException;
    
    /**
     * Gets the requested socket option.
     *
     * @param      option  socket option to retrieve
     * @return     value of the socket option
     * @exception  IOException  if an I/O error occurs.
     */
    private native int getSockOpt0(int option) 
        throws IOException;

    /**
     * Sets the requested socket option.
     *
     * @param      option  socket option to set
     * @param      value   the value to set <tt>option</tt> to
     * @exception  IOException  if an I/O error occurs.
     */
    private native void setSockOpt0(int option, int value) 
        throws IOException;

    /**
     * Shuts down the output side of the connection.  Any error that might
     * result from this operation is ignored.
     */
    private native void shutdownOutput0();
}
