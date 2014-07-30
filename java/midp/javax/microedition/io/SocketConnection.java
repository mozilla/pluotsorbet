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

package javax.microedition.io;

import java.lang.IllegalArgumentException;
import java.io.IOException;
import java.lang.String;

/**
 * This interface defines the socket stream connection.
 * <P>
 * A socket is accessed using a generic connection string with an explicit host 
 * and port number. The host may be specified as a fully qualified host name or 
 * IPv4 number.
 * e.g. <code>socket://host.com:79</code> defines a target socket on the 
 * <code>host.com</code> system at 
 * port <code>79</code>.
 * <P>Note that 
 * RFC1900 recommends the use of names rather than IP numbers for best results
 * in the event of IP number reassignment. </P>
 * <H3> Closing Streams </H3>
 * <P>
 * Every <code>StreamConnection</code> provides a <code>Connection</code>
 * object as well as an <code>InputStream</code> and <code>OutputStream</code>
 * to handle the I/O associated with the connection. Each of these interfaces
 * has its own <code>close()</code> method. For systems that support
 * duplex communication over the socket connection, closing of the 
 * input or output stream SHOULD shutdown just that side of the 
 * connection. e.g. closing the <code>InputStream</code> will permit the
 * <code>OutputStream</code> to continue sending data.
 * </P>
 * <P>
 * Once the input or output stream has been closed, it can only be reopened
 * with a call to <code>Connector.open()</code>. The application will receive
 * an <code>IOException</code> if an attempt is made to reopen the stream.
 * </P>
 * <H2>
 * BNF Format for Connector.open() string
 * </H2>
 * <P>
 * The URI must conform to the BNF syntax specified below.  If the URI
 * does not conform to this syntax, an <code>IllegalArgumentException</code>
 * is thrown.
 * </P>
 * <TABLE BORDER="1">
 * <TR>
 * <TD>&lt;socket_connection_string&gt; </TD>
 * <TD>::= "<strong>socket://</strong>"&lt;hostport&gt; </TD>
 * </TR>
 * <TR>
 * <TD>&lt;hostport&gt; </TD>
 * <TD>::= <I>host</I> ":" <I>port </I> </TD>
 * </TR>
 * <TR>
 * <TD>&lt;host&gt; </TD>
 * <TD>::= <I>host name or IP address </I> (omitted for inbound connections,
 * See <a href="ServerSocketConnection.html">ServerSocketConnection</a>)
 * </TD>
 * </TR>
 * <TR>
 * <TD>&lt;port&gt; </TD>
 * <TD>::= <I>numeric port number </I> </TD>
 * </TR>
 * </TABLE>
 * <H3>
 * Examples
 * </H3>
 * <P>
 * The following examples show how a <code>SocketConnection</code>
 * would be used to access a sample loopback program.
 * </P>
 * <PRE>
 *   SocketConnection sc = (SocketConnection)
 *                         Connector.open("socket://host.com:79");
 *   sc.setSocketOption(SocketConnection.LINGER, 5);
 *
 *   InputStream is  = sc.openInputStream();
 *   OutputStream os = sc.openOutputStream();
 *
 *   os.write("\r\n".getBytes());
 *   int ch = 0;
 *   while(ch != -1) {
 *       ch = is.read();
 *   }
 *
 *   is.close();
 *   os.close();
 *   sc.close();
 * </PRE>
 */
public interface SocketConnection
    extends StreamConnection  {

    /**
     * Socket option for the small buffer <em>writing delay</em> (0).
     * Set to zero to disable Nagle algorithm for 
     * small buffer operations.
     * Set to a non-zero value to enable.
     */
    public final byte DELAY = 0; 
 
    /**
     * Socket option for the <em>linger time</em> to wait in seconds
     * before closing a connection with
     * pending data output (1). Setting the linger time to zero
     * disables the linger wait interval.
     */
    public final byte LINGER = 1;

    /**
     * Socket option for the <em>keep alive</em> feature (2).
     * Setting KEEPALIVE to zero will disable the feature.
     * Setting KEEPALIVE to a non-zero value will enable the feature.
     */
    public final byte KEEPALIVE = 2;


    /**
     * Socket option for the size of the <em>receiving buffer</em> (3).
     */
    public final byte RCVBUF = 3;

    /**
     * Socket option for the size of the <em>sending buffer</em> (4).
     */
    public final byte SNDBUF = 4;

    /**
     * Set a socket option for the connection.
     * <P>
     * Options inform the low level networking code about intended
     * usage patterns that the application will use in dealing with
     * the socket connection.
     * </P>
     * <P>
     * Calling <code>setSocketOption</code> to assign buffer sizes
     * is a hint to the platform of the sizes to set the underlying 
     * network I/O buffers.
     * Calling <code>getSocketOption</code> can  be used to see what 
     * sizes the system is using.
     * The system MAY adjust the buffer sizes to account for 
     * better throughput available from Maximum Transmission Unit
     * (MTU) and Maximum Segment Size (MSS) data available 
     * from current network information.
     * </P>
     *
     * @param option socket option identifier (KEEPALIVE, LINGER,
     * SNDBUF, RCVBUF, or DELAY)
     * @param value numeric value for specified option
     * @exception  IllegalArgumentException if  the value is not
     *              valid (e.g. negative value) or if the option
     *              identifier is not valid
     * @exception  IOException  if the connection was closed
     *
     * @see #getSocketOption
     */
    public void setSocketOption(byte option,  int value)
	throws IllegalArgumentException, IOException;

    /**
     * Get a socket option for the connection.
     *
     * @param option socket option identifier (KEEPALIVE, LINGER,
     * SNDBUF, RCVBUF, or DELAY)
     * @return  numeric value for specified option or -1 if the
     *  value is not available.
     * @exception IllegalArgumentException if the option identifier is
     *  not valid
     * @exception  IOException  if the connection was closed
     * @see #setSocketOption
     */
    public  int getSocketOption(byte option)
	throws IllegalArgumentException, IOException;

    /**
     * Gets the local address to which the socket is bound.
     *
     * <P>The host address(IP number) that can be used to connect to this
     * end of the socket connection from an external system.
     * Since IP addresses may be dynamically assigned, a remote application
     * will need to be robust in the face of IP number reassignment.</P>
     * <P> The local hostname (if available) can be accessed from
     * <code> System.getProperty("microedition.hostname")</code>
     * </P>
     *
     * @return the local address to which the socket is bound.
     * @exception  IOException  if the connection was closed.
     * @see ServerSocketConnection
     */
    public  String getLocalAddress() throws IOException;

    /**
     * Returns the local port to which this socket is bound.
     *
     * @return the local port number to which this socket is connected.
     * @exception  IOException  if the connection was closed.
     * @see ServerSocketConnection
     */
    public  int  getLocalPort() throws IOException;

    /**
     * Gets the remote address to which the socket is bound.
     * The address can be either the remote host name or the IP
     * address(if available).
     *
     * @return the remote address to which the socket is bound.
     * @exception  IOException  if the connection was closed.
     */
    public  String getAddress() throws IOException;

    /**
     * Returns the remote port to which this socket is bound.
     *
     * @return the remote port number to which this socket is connected.
     * @exception  IOException  if the connection was closed.
     */
    public  int  getPort() throws IOException;
}
