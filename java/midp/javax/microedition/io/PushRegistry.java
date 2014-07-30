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

import java.lang.ClassNotFoundException;
import java.lang.IllegalStateException;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.midp.io.Util;
import com.sun.midp.io.j2me.push.PushRegistryImpl;

/**
 * The <code>PushRegistry</code> maintains a list of inbound
 * connections. An application can register the inbound
 * connections with an entry in the application descriptor file
 * or dynamically by calling the
 * <code>registerConnection</code> method.
 * <P> While an application is running, it is responsible for
 * all I/O operations associated with the inbound connection.
 * When the application is not running, the application
 * management software(AMS) listens for inbound notification
 * requests. When a notification arrives for a registered
 * <code>MIDlet</code>, the AMS will start the <code>MIDlet</code>
 * via the normal invocation of <code>MIDlet.startApp</code>
 * method.</P>
 *
 * <H3> Installation Handling of Declared Connections </H3>
 *
 * <P>
 * To avoid collisions on inbound generic connections, the application
 * descriptor file MUST include information about static connections
 * that are needed by the <code>MIDlet</code> suite.
 *
 * If all the static Push declarations in the application descriptor
 * can not be fulfilled during the installation, the user MUST be
 * notified that there are conflicts and the MIDlet suite MUST NOT be
 * installed. (See <em>Over The Air User Initiated Provisioning
 * Specification</em> section for errors reported in the event
 * of conflicts.)
 *
 * Conditions
 * when the declarations can not be fulfilled include: syntax errors in
 * the Push attributes, declaration for a connection end point (e.g. port
 * number) that is already reserved in the device, declaration for a
 * protocol that is not supported for Push in the device, and declaration
 * referencing a <code>MIDlet</code> class that is not listed in
 * the <code>MIDlet-&lt;n&gt;</code> attributes of the same
 * application descriptor.
 *
 * If the <code>MIDlet</code> suite
 * can function meaningfully even if a Push registration can't be
 * fulfilled, it MUST register the Push connections using the dynamic
 * registration methods in the <code>PushRegistry</code>.
 * </P>
 * <P>
 * A conflict-free installation reserves each requested connection for
 * the exclusive use of the
 * <code>MIDlets</code> in the suite. While the suite is
 * installed, any attempt by other applications to open one of the
 * reserved connections will fail with an
 * <code>IOException</code>. A call from a
 * <code>MIDlet</code> to
 * <code>Connector.open()</code>
 * on a connection reserved for its suite will always
 * succeed, assuming the suite does not already have the connection open.
 * </P>
 *
 * <P>
 * If two <code>MIDlet</code> suites have a static push connection in
 * common, they cannot be installed together and both function
 * correctly. The end user would typically have to uninstall one before
 * being able to successfully install the other.
 * </P>
 * <H3> <A NAME="PushAttr"></A>Push Registration Attribute </H3>
 * <P>
 * Each push registration entry contains the following information :
 * <CODE><BLOCKQUOTE>
 * <strong>MIDlet-Push-</strong>&lt;n&gt;: &lt;ConnectionURL&gt;,
 *      &lt;MIDletClassName&gt;, &lt;AllowedSender&gt;
 * </BLOCKQUOTE>
 * </CODE>  where :
 * <UL>
 * <LI> <code>MIDlet-Push-&lt;n&gt;</code> =
 *    the Push registration attribute name. Multiple push
 *    registrations can be provided in a <code>MIDlet</code>
 *    suite. The numeric value for &lt;n&gt; starts from 1 and
 *    MUST use consecutive ordinal numbers for additional entries.
 *    The first missing entry terminates the list. Any
 *    additional entries are ignored.
 * </LI>
 * <LI><code>ConnectionURL</code> =
 *    the connection string used in <code>Connector.open()</code></LI>
 * <LI><code>MIDletClassName</code> =
 *   the <code>MIDlet</code> that is responsible for the connection.
 *   The named <code>MIDlet</code> MUST be registered in the
 *   descriptor file or the jar file manifest with a
 *   <code>MIDlet-&lt;n&gt;</code> record.
 *   (This information is needed when displaying messages to
 *   the user about the application when push connections are detected,
 *   or when the user grants/revokes privileges for the application.)
 *   If the named <code>MIDlet</code> appears more than once in the
 *   suite, the first matching entry is used.  </LI>
 * <LI>
 * <code>AllowedSender</code> = a designated filter that restricts which
 * senders are valid for launching the requested <code>MIDlet</code>.
 *
 * The syntax and semantics of the <code>AllowedSender</code> field
 * depend on the addressing format used for the protocol.
 *
 * However, every syntax for this field MUST support using the wildcard
 * characters "*" and "?". The semantics of those wildcard are:
 * <UL>
 *   <LI> "*" matches any string, including an empty string </LI>
 *   <LI> "?" matches any single character </LI>
 * </UL>
 * When the value of this field is just the wildcard character "*",
 * connections will be accepted from any originating source.
 *
 * For Push attributes using the <CODE>datagram</CODE> and
 * <CODE>socket</CODE> URLs (if supported by the platform), this field
 * contains a numeric IP address in the same format for IPv4 and IPv6 as
 * used in the respective URLs (IPv6 address including the square
 * brackets as in the URL).
 *
 * It is possible to use the wildcards also in these IP addresses,
 * e.g. "129.70.40.*" would allow subnet resolution.  Note that the port
 * number is not part of the filter for <CODE>datagram</CODE> and
 * <CODE>socket</CODE> connections.
 * </LI>
 * </UL>
 * <P>
 * The MIDP specification defines the syntax for
 * <code>datagram</code> and <code>socket</code> inbound
 * connections. When other specifications
 * define push semantics for additional connection types, they
 * must define the expected syntax for the filter field, as well as
 * the expected format for the connection URL string.
 * </P>
 * <H4> Example Descriptor File Declarative Notation </H4>
 *
 * <P>
 * The following is a sample descriptor file entry that would reserve
 * a stream socket at port 79 and a datagram connection at
 * port 50000. (Port numbers are maintained by IANA
 * and cover well-known, user-registered and dynamic port numbers)
 * [See <a href="http://www.iana.org/numbers.html#P">
 * IANA Port Number Registry</a>]
 * </P>
 *
 * <CODE>
 * <PRE>
 *  MIDlet-Push-1: socket://:79, com.sun.example.SampleChat, *
 *  MIDlet-Push-2: datagram://:50000, com.sun.example.SampleChat, *
 * </PRE>
 * </CODE>
 * <H3> Buffered Messages </H3>
 * <P>
 * The requirements for buffering of messages are specific
 * to each protocol used for Push and are defined separately
 * for each protocol. There is no general requirement related
 * to buffering that would apply to all protocols. If the
 * implementation buffers messages, these messages MUST
 * be provided to the <code>MIDlet</code> when the
 * <code>MIDlet</code> is started and it opens the related
 * <code>Connection</code> that it has registered for Push.
 * </P>
 * <P>
 * When datagram connections are supported with Push, the
 * implementation MUST guarantee that when a <code>MIDlet</code>
 * registered for datagram Push is started in response to an incoming
 * datagram, at least the datagram that caused the startup of the
 * <code>MIDlet</code> is buffered by the implementation and will be
 * available to the <code>MIDlet</code> when the <code>MIDlet</code>
 * opens the <code>UDPDatagramConnection</code> after startup.
 * </P>
 * <P>
 * When socket connections are supported with Push, the
 * implementation MUST guarantee that when a <code>MIDlet</code>
 * registered for socket Push is started in response to
 * an incoming socket connection, this connection can
 * be accepted by the <code>MIDlet</code> by opening the
 * <code>ServerSocketConnection</code> after startup, provided
 * that the connection hasn't timed out meanwhile.
 * </P>
 *
 * <H3> Connection vs Push Registration Support </H3>
 * <P>
 * Not all generic connections will be appropriate for use
 * as push application transport. Even if a protocol is supported
 * on the device as an inbound connection type, it is not required
 * to be enabled as a valid push mechanism. e.g. a platform might
 * support server socket connections in a <code>MIDlet</code>,
 * but might not support inbound socket connections for push
 * launch capability.
 * A <code>ConnectionNotFoundException</code> is thrown from
 * the <code>registerConnection</code> and from the
 * <code>registerAlarm</code> methods, when the platform
 * does not support that optional capability.
 * </P>
 *
 * <H3> AMS Connection Handoff </H3>
 * <P>
 * Responsibility for registered push connections is shared between
 * the AMS and the <code>MIDlet</code> that handles the I/O
 * operations on the inbound connection. To prevent any data
 * from being lost, an application is responsible for
 * all I/O operations on the connection from the time it calls
 * <code>Connector.open()</code>
 * until it calls <code>Connection.close()</code>.
 * </P>
 * <P>
 * The AMS listens for inbound connection notifications. This
 * MAY be handled via a native callback or polling mechanism
 * looking for new inbound data. The AMS is
 * responsible for enforcing the
 * <A HREF="package-summary.html#push">Security of PushRegistry</A>
 * and presenting notifications (if any) to the user before invoking
 * the MIDlet suite.
 *</P>
 * <P>
 * The AMS is responsible for the shutdown of any running
 * applications (if necessary) prior to the invocation of
 * the push <code>MIDlet</code> method.
 * </P>
 * <P>
 * After the AMS has started the push application, the
 * <code>MIDlet</code> is responsible for opening the
 * connections and for all subsequent I/O operations.
 * An application that needs to perform blocking I/O
 * operations SHOULD use a separate thread to allow
 * for interactive user operations.
 * Once the application has been started and the connection
 * has been opened, the AMS is no longer responsible for
 * listening for push notifications for that connection.
 * The application is
 * responsible for reading all inbound data.
 * </P>
 * <P>
 * If an application has finished with all inbound data
 * it MAY <code>close()</code> the connection.
 * If the connection is closed,
 * then neither the AMS nor the application
 * will be listening for push notifications. Inbound data
 * could be lost, if the application closes the connection
 * before all data has been received.
 * </P>
 * <P>
 * When the application is destroyed, the AMS resumes its
 * responsibility to watch for inbound connections.
 * </P>
 * <P>
 * A push application SHOULD behave in a predictable manner
 * when handling asynchronous data via the push mechanism.
 * A well behaved application SHOULD inform the user that
 * data has been processed. (While it is possible to write
 * applications that do not use any user visible interfaces,
 * this could lead to a confused end user experience to
 * launch an application that only performs a background
 * function.)
 * </P>
 * <H3>Dynamic Connections Registered from a Running MIDlet</H3>
 *
 * <P>
 * There are cases when defining a well known port registered
 * with IANA is not necessary.
 * Simple applications may just wish to exchange data using a private
 * protocol between a <code>MIDlet</code> and server application.
 * </P>
 *
 * <P>
 * To accommodate this type of application, a mechanism is provided
 * to dynamically allocate a connection and to register
 * that information, as if it was known, when the application was
 * installed. This information can then be sent to an agent on the network to
 * use as the mechanism to communicate with the registered
 * <code>MIDlet</code>.
 * </P>
 * <P>
 * For instance, if a <a href="UDPDatagramConnection.html">
 * <code>UDPDatagramConnection</code></a>
 * is opened and a port number,
 * was not specified, then the application is
 * requesting a dynamic port
 * to be allocated from the ports that are currently available. By
 * calling <code>PushRegistry.registerConnection()</code> the
 * <code>MIDlet</code> informs the AMS that it is the target for
 * inbound communication, even
 * after the <code>MIDlet</code> has been destroyed (See
 * <code>MIDlet</code> life cycle for
 * definition of "destroyed" state). If the application is deleted from the
 * phone, then its dynamic communication connections are unregistered
 * automatically.
 * </P>
 * <H3>AMS Runtime Handling - Implementation Notes</H3>
 *
 * <P>
 * During installation each <code>MIDlet</code> that is expecting
 * inbound communication
 * on a well known address has the information recorded with the
 * AMS from the push registration attribute in the manifest or
 * application descriptor file. Once the installation has been
 * successfully completed,
 * (e.g. For the OTA recommended practices - when the <em>Installation
 * notification message</em> has been successfully transmitted, the
 * application is officially installed.)
 * the <code>MIDlet</code> MAY then receive inbound communication.
 * e.g. the push notification event.
 * </P>
 *
 * <P>
 * When the AMS is started, it checks the list of registered
 * connections  and  begins listening for inbound communication.
 * When a notification arrives the AMS starts the registered
 * <code>MIDlet</code>.
 * The <code>MIDlet</code> then opens
 * the connection with <code>Connector.open()</code> method to
 * perform whatever I/O operations are needed for the particular
 * connection type. e.g. for a server socket the application
 * uses <code>acceptAndOpen()</code> to get the socket connected
 * and for a datagram connection the application uses
 * <code>receive()</code> to read the delivered message.
 * </P>
 * <P>
 * For message oriented transports the inbound message MAY be
 * read by the AMS and saved for delivery to the <code>MIDlet</code>
 * when it requests to read the data. For stream oriented transports
 * the connection MAY be lost if the connection is not
 * accepted before the server end of the connection request
 * timeouts.
 * </P>
 * <P>
 * When a <code>MIDlet</code> is started in response to a registered
 * push connection notification, it is platform dependent what
 * happens to the current running application. The <code>MIDlet</code>
 * life cycle defines the expected behaviors that an interrupted
 * <code>MIDlet</code> could see from a call to <code>pauseApp()</code>
 * or from <code>destroyApp()</code>.
 * </P>
 * <H2>Sample Usage Scenarios</H2>
 * <P>
 * <strong>Usage scenario 1:</strong>
 * The suite includes a <code>MIDlet</code> with a well
 * known port for communication.
 * During the <code>startApp</code> processing
 * a thread is launched to handle the incoming data.
 * Using a separate thread is the recommended practice
 * for avoiding conflicts between blocking I/O operations
 * and the normal user interaction events. The
 * thread continues to receive messages until the
 * <code>MIDlet</code> is destroyed.
 * </P>
 *
 * <H4>Sample Chat Descriptor File -</H4>
 * <P> In this sample, the descriptor file includes
 * a static push
 * connection registration. It also includes
 * an indication that this <code>MIDlet</code>
 * requires permission to use a datagram connection
 * for inbound push messages.
 * (See <A HREF="package-summary.html#push">
 * Security of Push Functions</A> in the package
 * overview for details about <code>MIDlet</code> permissions.)
 * <strong>Note:</strong> this sample is appropriate for bursts of
 * datagrams.
 * It is written to loop on the connection, processing
 * received messages.
 * </P>
 * <CODE>
 * <PRE>
 *  MIDlet-Name: SunNetwork - Chat Demo
 *  MIDlet-Version: 1.0
 *  MIDlet-Vendor: Sun Microsystems, Inc.
 *  MIDlet-Description: Network demonstration programs for MIDP
 *  MicroEdition-Profile: MIDP-2.0
 *  MicroEdition-Configuration: CLDC-1.0
 *  MIDlet-1: InstantMessage, /icons/Chat.png, example.chat.SampleChat, *
 *  MIDlet-Push-1: datagram://:79,  example.chat.SampleChat, *
 *  MIDlet-Permissions: javax.microedition.io.PushRegistry, \\
 *                      javax.microedition.io.Connector.datagramreceiver
 * </PRE>
 * </CODE>
 *
 * <H4>Sample Chat MIDlet Processing -</H4>
 * <CODE>
 * <PRE>
 * public class SampleChat extends MIDlet {
 *     // Current inbound message connection.
 *     DatagramConnection conn;
 *     // Flag to terminate the message reading thread.
 *     boolean done_reading;
 *
 *     public void startApp() {
 *         // List of active connections.
 *         String connections[];
 *
 *         // Check to see if this session was started due to
 *         // inbound connection notification.
 *         connections = PushRegistry.listConnections(true);
 *
 *         // Start an inbound message thread for available
 *         // inbound messages for the statically configured
 *         // connection in the descriptor file.
 *         for (int i=0; i &lt; connections.length; i++) {
 *           Thread t = new Thread (new MessageHandler(
 *                            connections[i]));
 *           t.start();
 *         }
 *
 *         ...
 *        }
 *     }
 *
 *     // Stop reading inbound messages and release the push
 *     // connection to the AMS listener.
 *     public void destroyApp(boolean conditional) {
 *        done_reading = true;
 *        if (conn != null)
 *            conn.close();
 *        // Optionally, notify network service that we're
 *        // done with the current session.
 *        ...
 *     }
 *
 *     // Optionally, notify network service.
 *     public void pauseApp() {
 *         ...
 *     }
 *
 *  // Inner class to handle inbound messages on a separate thread.
 *  class MessageHandler implements Runnable {
 *      String connUrl ;
 *      MessageHandler(String url) {
 *           connUrl = url ;
 *      }
 *      // Fetch messages in a blocking receive loop.
 *      public void run() {
 *        try {
 *          // Get a connection handle for inbound messages
 *          // and a buffer to hold the inbound message.
 *          DatagramConnection conn = (DatagramConnection)
 *               Connector.open(connUrl);
 *          Datagram data = conn.newDatagram(conn.getMaximumLength());
 *
 *          // Read the inbound messages
 *          while (!done_reading) {
 *             conn.receive(data);
 *            ...
 *          }
 *         } catch (IOException ioe) {
 *         ...
 *      }
 *      ...
 * </PRE>
 * </CODE>
 * <P>
 *  <strong>Usage scenario 2:</strong>
 *  The suite includes a <code>MIDlet</code>
 *  that dynamically allocates port the first time
 *  it is started.
 * </P>
 *
 *
 * <H4>Sample Ping Descriptor File -</H4>
 * <P> In this sample, the descriptor file includes an
 * entry indicating that
 * the application will need permission to use the datagram
 * connection for inbound push messages. The dynamic connection
 * is allocated in the constructor the first time it is run.
 * The open connection is used during this session and
 * can be reopened in a subsequent session in response to
 * a inbound connection notification.
 * </P>
 * <CODE>
 * <PRE>
 *  MIDlet-Name: SunNetwork - Demos
 *  MIDlet-Version: 1.0
 *  MIDlet-Vendor: Sun Microsystems, Inc.
 *  MIDlet-Description: Network demonstration programs for MIDP
 *  MicroEdition-Profile: MIDP-2.0
 *  MicroEdition-Configuration: CLDC-1.0
 *  MIDlet-1: JustCallMe, /icons/Ping.png, example.ping.SamplePingMe, *
 *  MIDlet-Permissions: javax.microedition.io.PushRegistry, \\
 *                      javax.microedition.io.Connector.datagramreceiver
 * </PRE>
 * </CODE>
 *
 * <H4>Sample Ping MIDlet Processing -</H4>
 * <CODE>
 * <PRE>
 * public class SamplePingMe extends MIDlet {
 *    // Name of the current application for push registration.
 *    String myName = "example.chat.SamplePingMe";
 *    // List of registered push connections.
 *    String connections[];
 *    // Inbound datagram connection
 *    UDPDatagramConnection dconn;
 *
 *    public SamplePingMe() {
 *
 *        // Check to see if the ping connection has been registered.
 *        // This is a dynamic connection allocated on first
 *        // time execution of this MIDlet.
 *        connections = PushRegistry.listConnections(false);
 *
 *        if (connections.length == 0) {
 *            // Request a dynamic port for out-of-band notices.
 *            // (Omitting the port number let's the system allocate
 *            //  an available port number.)
 *            try {
 *                dconn = (UDPDatagramConnection)
 *                        Connector.open("datagram://");
 *                String dport = "datagram://:"  + dconn.getLocalPort();
 *
 *                // Register the port so the MIDlet will wake up, if messages
 *                // are posted after the MIDlet exits.
 *                PushRegistry.registerConnection(dport, myName, "*");
 *
 *                // Post my datagram address to the network
 *                  ...
 *            } catch (IOException ioe) {
 *                ...
 *            } catch (ClassNotFoundException cnfe) {
 *                ...
 *            }
 *    }
 *
 *    public void startApp() {
 *       // Open the connection if it's not already open.
 *       if (dconn == null) {
 *           // This is not the first time this is run, because the
 *           // dconn hasn't been opened by the constructor.
 *
 *          // Check if the startup has been due to an incoming
 *          // datagram.
 *          connections = PushRegistry.listConnections(true);
 *
 *          if (connections.length &gt; 0) {
 *             // There is a pending datagram that can be received.
 *             dconn = (UDPDatagramConnection)
 *                 Connector.open(connections[0]);
 *
 *             // Read the datagram
 *             Datagram d = dconn.newDatagram(dconn.getMaximumLength());
 *             dconn.receive(d);
 *          } else {
 *             // There are not any pending datagrams, but open
 *             // the connection for later use.
 *             connections = PushRegistry.listConnections(false);
 *             if (connections.length &gt; 0) {
 *                     dconn = (UDPDatagramConnection)
 *                         Connector.open(connections[0]);
 *             }
 *         }
 *      }
 *
 *    public void destroyApp(boolean unconditional) {
 *        // Close the connection before exiting
 *        if(dconn != null){
 *           dconn.close()
 *           dconn = null
 *        }
 *    }
 *    ...
 * </PRE>
 * </CODE>
 */

public class PushRegistry {

    /** Prevent instantiation of the push registry. */
    private PushRegistry() { };

    /**
     * Register a dynamic connection with the
     * application management software. Once registered,
     * the dynamic connection acts just like a
     * connection preallocated from the descriptor file.
     *
     * <P> The arguments for the dynamic connection registration are the 
     * same as the <A HREF="#PushAttr">Push Registration Attribute</A>
     * used for static registrations. 
     * </P>
     * <P> If the <code>connection</code> or <code>filter</code>
     * arguments are <code>null</code>,
     * then an <code>IllegalArgumentException</code> will be thrown.
     * If the <code>midlet</code> argument is <code>null</code> a 
     * <code>ClassNotFoundException</code> will be thrown. </P>
     *
     * @param connection generic connection <em>protocol</em>, <em>host</em>
     *              and <em>port number</em>
     *              (optional parameters may be included
     *              separated with semi-colons (;))
     * @param midlet  class name of the <code>MIDlet</code> to be launched,
     *              when new external data is available.
     * The named <code>MIDlet</code> MUST be registered in the
     * descriptor file or the jar file manifest with a
     * MIDlet-&lt;n&gt; record. This parameter has the same semantics
     * as the MIDletClassName in the Push registration attribute
     * defined above in the class description.
     * @param filter a connection URL string indicating which senders
     *              are allowed to cause the <code>MIDlet</code> to be launched
     * @exception  IllegalArgumentException if the connection string is not
     *              valid, or if the filter string is not valid
     * @exception ConnectionNotFoundException if the runtime system does not
     *              support push delivery for the requested
     *              connection protocol
     * @exception IOException if the connection is already
     *              registered or if there are insufficient resources
     *              to handle the registration request
     * @exception ClassNotFoundException if the <code>MIDlet</code>
     * class name can not be found in the current <code>MIDlet</code>
     * suite or if this class is not included in any of the
     * MIDlet-&lt;n&gt; records in the descriptor file or the jar file
     * manifest 
     * @exception SecurityException if the <code>MIDlet</code> does not
     *              have permission to register a connection
     * @see #unregisterConnection
     */
    public static void  registerConnection(String connection, String midlet,
					   String filter)
	throws ClassNotFoundException,
	        IOException {

	PushRegistryImpl
           .registerConnection(connection, midlet, filter);
    }

    /**
     * Remove a dynamic connection registration.
     *
     * @param connection generic connection <em>protocol</em>,
     *            <em>host</em> and <em>port number</em>
     * @exception SecurityException if the connection was
     *            registered by another <code>MIDlet</code>
     *            suite
     * @return <code>true</code> if the unregistration was successful,
     *         <code>false</code> if the connection was not registered
     *         or if the connection argument was <code>null</code>
     * @see #registerConnection
     */
    public static boolean unregisterConnection(String connection) {

	return PushRegistryImpl.unregisterConnection(connection);
    }

    /**
     * Return a list of registered connections for the current
     * <code>MIDlet</code> suite.
     *
     * @param available if <code>true</code>, only return the list of
     *     connections with input available, otherwise return the 
     *     complete list of registered connections for the current 
     *     <code>MIDlet</code> suite
     * @return array of registered connection strings, where each connection 
     *      is represented by the generic connection <em>protocol</em>, 
     *       <em>host</em> and <em>port number</em> identification
     */
    public static String[] listConnections(boolean available) {
        return PushRegistryImpl.listConnections(available);
    }

    /**
     * Retrieve the registered <code>MIDlet</code> for a requested connection.
     *
     * @param connection generic connection <em>protocol</em>, <em>host</em>
     *              and <em>port number</em>
     *              (optional parameters may be included
     *              separated with semi-colons (;))
     * @return  class name of the <code>MIDlet</code> to be launched,
     *              when new external data is available, or
     *              <code>null</code> if the connection was not
     *              registered by the current <code>MIDlet</code> suite
     *              or if the connection argument was <code>null</code>
     * @see #registerConnection
     */
    public static String getMIDlet(String connection) {
	// Delegate to implementation class for native lookup
	return 	PushRegistryImpl.getMIDlet(connection);
    }

    /**
     * Retrieve the registered filter for a requested connection.
     *
     * @param connection generic connection <em>protocol</em>, <em>host</em>
     *              and <em>port number</em>
     *              (optional parameters may be included
     *              separated with semi-colons (;))
     * @return a filter string indicating which senders
     *              are allowed to cause the <code>MIDlet</code> to be 
     *              launched or <code>null</code>, if the connection was not
     *              registered by the current <code>MIDlet</code> suite
     *              or if the connection argument was <code>null</code>
     * @see #registerConnection
     */
    public static String getFilter(String connection) {
	// Delegate to implementation class for native lookup
	return 	PushRegistryImpl.getFilter(connection);
    }

    /**
     * Register a time to launch the specified application. The
     * <code>PushRegistry</code> supports one outstanding wake up
     * time per <code>MIDlet</code> in the current suite. An application
     * is expected to use a <code>TimerTask</code> for notification
     * of time based events while the application is running.
     * <P>If a wakeup time is already registered, the previous value will
     * be returned, otherwise a zero is returned the first time the
     * alarm is registered. </P>
     *
     * @param midlet  class name of the <code>MIDlet</code> within the
     *                current running <code>MIDlet</code> suite
     *                to be launched,
     *                when the alarm time has been reached.
     * The named <code>MIDlet</code> MUST be registered in the
     * descriptor file or the jar file manifest with a
     * MIDlet-&lt;n&gt; record. This parameter has the same semantics
     * as the MIDletClassName in the Push registration attribute
     * defined above in the class description.
     * @param time time at which the <code>MIDlet</code> is to be executed
     *        in the format returned by <code>Date.getTime()</code>
     * @return the time at which the most recent execution of this
     *        <code>MIDlet</code> was scheduled to occur,
     *        in the format returned by <code>Date.getTime()</code>
     * @exception ConnectionNotFoundException if the runtime system does not
     *              support alarm based application launch
     * @exception ClassNotFoundException if the <code>MIDlet</code>
     * class name can not be found in the current <code>MIDlet</code>
     * suite or if this class is not included in any of the
     * MIDlet-&lt;n&gt; records in the descriptor file or the jar file
     * manifest or if the <code>midlet</code> argument is
     * <code>null</code>
     * @exception SecurityException if the <code>MIDlet</code> does not
     *              have permission to register an alarm
     * @see Date#getTime()
     * @see Timer
     * @see TimerTask
     */
    public static long registerAlarm(String midlet, long time)
	 throws ClassNotFoundException, ConnectionNotFoundException {
	// Delegate to implementation class for native registration
	return 	PushRegistryImpl.registerAlarm(midlet, time);
    }
}


