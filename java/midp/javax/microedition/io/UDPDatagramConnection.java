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

/**
 * This interface defines a datagram connection which knows
 * it's local end point address.
 * The protocol is transaction oriented, and delivery and duplicate
 * protection are not guaranteed.  Applications requiring ordered
 * reliable delivery of streams of data should use
 * the <code>SocketConnection</code>.
 * <p>
 * A <code>UDPDatagramConnection</code> is returned from
 * <code>Connector.open()</code> in response to a request to
 * open a <code>datagram://</code> URL connection string.
 * If the connection string omits both the <code>host</code>
 * and <code>port</code> fields in the URL string, then the
 * system will allocate an available port. The local
 * address and the local port can be discovered using
 * the accessor methods within this interface.
 * </p>
 * <p>
 * The syntax described here for the datagram URL connection string
 * is also valid for the <code>Datagram.setAddress()</code> method
 * used to assign a destination address to a <code>Datagram</code>
 * to be sent. e.g., <code>datagram://</code><em>host:port</em>
 * </p>
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
 * <TD>&lt;datagram_connection_string&gt; </TD>
 * <TD>::= "<strong>datagram://</strong>" |
 *         "<strong>datagram://</strong>"&lt;hostport&gt; </TD>
 * </TR>
 * <TR>
 * <TD>&lt;hostport&gt; </TD>
 * <TD>::= <I>host</I> ":" <I>port </I> </TD>
 * </TR>
 * <TR>
 * <TD>&lt;host&gt; </TD>
 * <TD>::= <I>host name or IP address </I>
 * (omitted for inbound connections) </TD>
 * </TR>
 * <TR>
 * <TD>&lt;port&gt; </TD>
 * <TD>::= <I>numeric port number </I>(omitted for system assigned port) </TD>
 * </TR>
 * </TABLE>
 */

public interface UDPDatagramConnection extends DatagramConnection {

    /**
     * Gets the local address to which the
     * datagram connection is bound.
     *
     * <P>The host address(IP number) that can be used to connect to this
     * end of the datagram connection from an external system.
     * Since IP addresses may be dynamically assigned, a remote application
     * will need to be robust in the face of IP number reassignment.</P>
     * <P> The local hostname (if available) can be accessed from
     * <code> System.getProperty("microedition.hostname")</code>
     * </P>
     *
     * @return the local address to which the datagram connection is bound.
     * @exception  IOException  if the connection was closed.
     * @see ServerSocketConnection
     */
    public  String getLocalAddress() throws IOException;

    /**
     * Returns the local port to which this datagram connection is bound.
     *
     * @return the local port number to which this datagram connection
     *         is connected.
     * @exception  IOException  if the connection was closed.
     * @see ServerSocketConnection
     */
    public  int  getLocalPort() throws IOException;
}
