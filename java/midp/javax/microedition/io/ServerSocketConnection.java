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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * This interface defines the server socket stream connection.
 * <P>
 * A server socket is accessed using a generic connection string 
 * with the host omitted. For example, <code>socket://:79</code> 
 * defines an inbound server socket on port <code>79</code>.
 * The host can be discovered using the <code>getLocalAddress</code>
 * method. 
 * </P> 
 * <P>
 * The <code>acceptAndOpen()</code> method returns a 
 * <code>SocketConnection</code> instance. In addition to the
 * normal <code>StreamConnection</code> behavior, the 
 * <code>SocketConnection</code> supports accessing the IP
 * end point addresses of the live connection and access
 * to socket options that control the buffering and timing delays 
 * associated with specific application usage of the connection.
 * </P>
 * <P>
 * Access to server socket connections may be restricted by the
 * security policy of the device.  <code>Connector.open</code> MUST
 * check access for the initial server socket connection and
 * <code>acceptAndOpen</code> MUST check before returning
 * each new <code>SocketConnection</code>.
 * </P>
 * <P>
 * A server socket can be used to dynamically select an
 * available port by omitting both the host and the port
 * parameters in the connection URL string.
 * For example, <code>socket://</code> 
 * defines an inbound server socket on a port which is allocated 
 * by the system.
 * To discover the assigned port number use the 
 * <code>getLocalPort</code> method.
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
 * <TD>::= "<strong>socket://</strong>" | 
 *          "<strong>socket://</strong>"&lt;hostport&gt; </TD>
 * </TR>
 * <TR>
 * <TD>&lt;hostport&gt; </TD>
 * <TD>::= <I>host</I> ":" <I>port </I> </TD>
 * </TR>
 * <TR>
 * <TD>&lt;host&gt; </TD>
 * <TD>::= omitted for inbound connections, 
 * See <a href="SocketConnection.html">SocketConnection</a>
 * </TD>
 * </TR>
 * <TR>
 * <TD>&lt;port&gt; </TD>
 * <TD>::= <I>numeric port number </I>(omitted for system assigned port) </TD>
 * </TR>
 * </TABLE>
 * <H2>
 * Examples
 * </H2>
 * <P>
 * The following examples show how a <code>ServerSocketConnection</code>
 * would be used to access a sample loopback program.
 * </P>
 * <PRE>
 *   // Create the server listening socket for port 1234 
 *   ServerSocketConnection scn = (ServerSocketConnection)
 *                            Connector.open("socket://:1234");
 *
 *   // Wait for a connection.
 *   SocketConnection sc = (SocketConnection) scn.acceptAndOpen();
 *
 *   // Set application specific hints on the socket.
 *   sc.setSocketOption(DELAY, 0);
 *   sc.setSocketOption(LINGER, 0);
 *   sc.setSocketOption(KEEPALIVE, 0);
 *   sc.setSocketOption(RCVBUF, 128);
 *   sc.setSocketOption(SNDBUF, 128);
 *
 *   // Get the input stream of the connection.
 *   DataInputStream is = sc.openDataInputStream();
 *
 *   // Get the output stream of the connection.
 *   DataOutputStream os = sc.openDataOutputStream();
 *
 *   // Read the input data.
 *   String result = is.readUTF();
 *
 *   // Echo the data back to the sender.
 *   os.writeUTF(result);
 *
 *   // Close everything.
 *   is.close();
 *   os.close();
 *   sc.close();
 *   scn.close();
 *   ..
 * </PRE>
 */
public interface ServerSocketConnection
    extends  StreamConnectionNotifier {

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
     * @exception  IOException  if the connection was closed
     * @see SocketConnection
     */
    public  String getLocalAddress() throws IOException;

    /**
     * Returns the local port to which this socket is bound.
     *
     * @return the local port number to which this socket is connected.
     * @exception  IOException  if the connection was closed
     * @see SocketConnection
     */
    public  int  getLocalPort() throws IOException;


}
