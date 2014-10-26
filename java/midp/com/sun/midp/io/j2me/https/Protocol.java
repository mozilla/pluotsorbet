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

package com.sun.midp.io.j2me.https;

import java.util.Vector;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.*;

import javax.microedition.pki.*;

import com.sun.j2me.security.*;

import com.sun.midp.pki.*;

import com.sun.midp.ssl.*;

import com.sun.midp.main.Configuration;

import com.sun.midp.io.*;

import com.sun.midp.io.j2me.http.*;

import com.sun.midp.publickeystore.WebPublicKeyStore;

import com.sun.midp.security.*;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

import com.sun.midp.util.Properties;

/**
 * This class implements the necessary functionality
 * for an HTTPS connection. With support for HTTPS tunneling.
 * <center><img src="doc-files/https.gif" width=735 height=193
 * ALT="https diagram"></center> 
 * <p>
 * Handshake error codes at the beginning of IOException messages:</p>
 * <blockquote><p>
 *   (1) certificate is expired
 * </p><p>
 *   (2) certificate is not yet valid
 * </p><p>
 *   (3)  certificate failed signature verification
 * </p><p>
 *   (4)  certificate was signed using an unsupported algorithm
 * </p><p>
 *   (5)  certificate was issued by an unrecognized certificate authority
 * </p><p>
 *   (6)  certificate does not contain the correct site name
 * </p><p>
 *   (7)  certificate chain exceeds the length allowed
 * </p><p>
 *   (8)  certificate does not contain a signature
 * </p><p>
 *   (9)  version 3 certificate has unrecognized critical extensions
 * </p><p>
 *   (10) version 3 certificate has an inappropriate keyUsage or
 *        extendedKeyUsage extension
 * </p><p>
 *   (11) certificate in the a chain was not issued by the next
 *        authority in the chain
 * </p><p>
 *   (12) trusted certificate authority's public key is expired
 * </p></blockquote>
 */
public class Protocol extends com.sun.midp.io.j2me.http.Protocol
    implements HttpsConnection {

    /** HTTP permission name. */
    private static final String HTTPS_PERMISSION_NAME =
        "javax.microedition.io.Connector.https";

    /** Common name label. */
    private static final String COMMON_NAME_LABEL = "CN=";

    /** Common name label length. */
    private static final int COMMON_NAME_LABEL_LENGTH =
        COMMON_NAME_LABEL.length();

    /**
     * Inner class to request security token from SecurityInitializer.
     * SecurityInitializer should be able to check this inner class name.
     */
    static private class SecurityTrusted
        implements ImplicitlyTrustedClass {}

    /** This class has a different security domain than the MIDlet suite */
    private static SecurityToken classSecurityToken =
        SecurityInitializer.requestToken(new SecurityTrusted());

    /**
     * Parse the common name out of a distinguished name.
     *
     * @param name distinguished name
     *
     * @return common name attribute without the label
     */
    private static String getCommonName(String name) {
        int start;
        int end;

        if (name == null) {
            return null;
        }

        /* The common name starts with "CN=" label */
        start = name.indexOf(COMMON_NAME_LABEL);
        if (start < 0) {
            return null;
        }

        start += COMMON_NAME_LABEL_LENGTH;
        end = name.indexOf(';', start);
        if (end < 0) {
            end = name.length();
        }

        return name.substring(start, end);
    }

    /**
     * Check to see if the site name given by the user matches the site
     * name of subject in the certificate. The method supports the wild card
     * character for the machine name if a domain name is included after it.
     *
     * @param siteName site name the user provided
     * @param certName site name of the subject from a certificate
     *
     * @return true if the common name checks out, else false
     */
    private static boolean checkSiteName(String siteName, String certName) {
        int startOfDomain;
        int domainLength;

        if (certName == null) {
            return false;
        }

        // try the easy way first, ignoring case
        if ((siteName.length() == certName.length()) &&
            siteName.regionMatches(true, 0, certName, 0,
                                   certName.length())) {
            return true;
        }

        if (!certName.startsWith("*.")) {
            // not a wild card, done
            return false;
        }

        startOfDomain = siteName.indexOf('.');
        if (startOfDomain == -1) {
            // no domain name
            return false;
        }

        // skip past the '.'
        startOfDomain++;

        domainLength = siteName.length() - startOfDomain;
        if ((certName.length() - 2) != domainLength) {
            return false;
        }

        // compare the just the domain names, ignoring case
        if (siteName.regionMatches(true, startOfDomain, certName, 2,
                                   domainLength)) {
            return true;
        }

        return false;
    }

    /** collection of "Proxy-" headers as name/value pairs */
    private Properties proxyHeaders = new Properties();

    /** Underlying SSL connection. */
    private SSLStreamConnection sslConnection;

    /**
     * Create a new instance of this class. Override the some of the values
     * in our super class.
     */
    public Protocol() {
        protocol = "https";
        default_port = 443; // 443 is the default port for HTTPS
    }

    /**
     * Sets up the state of the connection, but
     * does not actually connect to the server until there's something
     * to do.
     * <p>
     * Warning: A subclass that implements this method, not call this
     * method and should also implement the disconnect method.
     *
     * @param name             The URL for the connection, without the
     *                         without the protocol part.
     * @param mode             The access mode, ignored
     * @param timeouts         A flag to indicate that the called wants
     *                         timeout exceptions, ignored
     *
     * @return reference to this connection
     *
     * @exception IllegalArgumentException If a parameter is invalid.
     * @exception ConnectionNotFoundException If the connection cannot be
     *             found.
     * @exception IOException  If some other kind of I/O error occurs.
     */
    public Connection openPrim(String name, int mode, boolean timeouts)
        throws IOException, IllegalArgumentException,
        ConnectionNotFoundException {

        checkForPermission(name);

        initStreamConnection(mode);

        url = new HttpUrl(protocol, name);

        if (url.port == -1) {
            url.port = default_port;
        }

        if (url.host == null) {
            throw new IllegalArgumentException("missing host in URL");
        }

        hostAndPort = url.host + ":" + url.port;

        return this;
    }
    
    /**
     * Check for the required permission.
     *
     * @param name name of resource to insert into the permission question
     *
     * @exception IOInterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    private void checkForPermission(String name)
            throws InterruptedIOException {

        name = protocol + ":" + name;

        try {
            AccessController.checkPermission(HTTPS_PERMISSION_NAME, name);
            permissionChecked = true;
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
     * Get the request header value for the named property.
     * @param key property name of specific HTTP 1.1 header field
     * @return value of the named property, if found, null otherwise.
     */
    public String getRequestProperty(String key) {
        /* https handles the proxy fields in a different way */
        if (key.toLowerCase().startsWith("proxy-")) {
            return proxyHeaders.getPropertyIgnoreCase(key);
        }

        return super.getRequestProperty(key);
    }

    /**
     * Add the named field to the list of request fields.
     *
     * @param key key for the request header field.
     * @param value the value for the request header field.
     */
    protected void setRequestField(String key, String value) {
        /* https handles the proxy fields in a different way */
        if (key.toLowerCase().startsWith("proxy-")) {
            proxyHeaders.setPropertyIgnoreCase(key, value);
            return;
        }

        super.setRequestField(key, value);
    }

    /**
     * Connect to the underlying secure socket transport.
     * Perform the SSL handshake and then proceeded to the underlying
     * HTTP protocol connect semantics.
     *
     * @return SSL/TCP stream connection
     * @exception IOException is thrown if the connection cannot be opened
     */
    protected StreamConnection connect() throws IOException {
        StreamConnection sc;
        String httpsTunnel;
        com.sun.midp.io.j2me.socket.Protocol tcpConnection;
        OutputStream tcpOutputStream;
        InputStream tcpInputStream;
        X509Certificate serverCert;

        if (!permissionChecked) {
            throw new SecurityException();
        }

        sc = connectionPool.get(classSecurityToken, protocol,
                                url.host, url.port);

        if (sc != null) {
            return sc;
        }

        // Open socket connection
        tcpConnection =
            new com.sun.midp.io.j2me.socket.Protocol();

        // check to see if a protocol is specified for the tunnel
        httpsTunnel = Configuration.getProperty("com.sun.midp.io.http.proxy");
        if (httpsTunnel != null) {
            // Make the connection to the ssl tunnel
            tcpConnection.openPrim(classSecurityToken, "//" + httpsTunnel);

            // Do not delay request since this delays the response.
            tcpConnection.setSocketOption(SocketConnection.DELAY, 0);

            tcpOutputStream = tcpConnection.openOutputStream();
            tcpInputStream = tcpConnection.openInputStream();
            
            // Do the handshake with the ssl tunnel
            try {
                doTunnelHandshake(tcpOutputStream, tcpInputStream);
            } catch (IOException ioe) {
                String temp = ioe.getMessage();

                tcpConnection.close();
                tcpOutputStream.close();
                tcpInputStream.close();

                if (temp.indexOf(" 500 ") > -1) {
                    throw new ConnectionNotFoundException(temp);
                }

                throw ioe;
            }    
        } else {
            tcpConnection.openPrim(classSecurityToken, "//" + hostAndPort);

            // Do not delay request since this delays the response.
            tcpConnection.setSocketOption(SocketConnection.DELAY, 0);

            tcpOutputStream = tcpConnection.openOutputStream();
            tcpInputStream = tcpConnection.openInputStream();
        }

        tcpConnection.close();

        try {
            // Get the SSLStreamConnection
            sslConnection = new SSLStreamConnection(url.host, url.port,
                                tcpInputStream, tcpOutputStream,
                                WebPublicKeyStore.getTrustedKeyStore());
        } catch (Exception e) {
            try {
                tcpInputStream.close();
            } catch (Throwable t) {
                // Ignore, we are processing an exception
            }

            try {
                tcpOutputStream.close();
            } catch (Throwable t) {
                // Ignore, we are processing an exception
            }

            if (e instanceof IOException) {
                throw (IOException)e;
            } else {
                throw (RuntimeException)e;
            }
        }

        try {
            serverCert = sslConnection.getServerCertificate();

            /*
             * if the subject alternate name is a DNS name or an IP address,
             * then use that instead of the common name for a site name match
             */
            int i;
            Vector v = serverCert.getSubjectAltNames();
            boolean altNamePresent = false;

            for (i = 0; i < v.size(); i++) {
                SubjectAlternativeName altName =
                        (SubjectAlternativeName) v.elementAt(i);

                // For IP address, it needs to be exact match
                if (altName.getSubjectAltNameType() ==
                        X509Certificate.TYPE_IP_ADDRESS) {
                    String ipAddress = (String)altName.getSubjectAltName();
                    altNamePresent = true;
                    if (url.host.equalsIgnoreCase(ipAddress)) {
                        break;
                    }
                } else if (altName.getSubjectAltNameType() ==
                            X509Certificate.TYPE_DNS_NAME) {
                    // compare DNS Name with host in url
                    String dnsName =
                        ((String)altName.getSubjectAltName()).toLowerCase();
                    altNamePresent = true;
                    if (checkSiteName(url.host, dnsName)) {
                        break;
                    }
                }
            }

            if (altNamePresent) {
                if (i == v.size()) {
                    throw new CertificateException(
                        "Subject alternative name did not match site name",
                        serverCert, CertificateException.SITENAME_MISMATCH);
                }
            } else {
                String cname = getCommonName(serverCert.getSubject());
                if (cname == null) {
                    throw new CertificateException(
                        "Common name missing from subject name",
                        serverCert, CertificateException.SITENAME_MISMATCH);
                }
                
                if (!checkSiteName(url.host, cname)) {
                    throw new CertificateException(serverCert,
                        CertificateException.SITENAME_MISMATCH);
                }
            }

            return sslConnection;
        } catch (Exception e) {
            try {
                sslConnection.close();
            } catch (Throwable t) {
                // Ignore, we are processing an exception
            }

            if (e instanceof IOException) {
                throw (IOException)e;
            } else {
                throw (RuntimeException)e;
            }
        }
    }

    /**
     * disconnect the current connection.
     *
     * @param connection connection return from {@link #connect()}
     * @param inputStream input stream opened from <code>connection</code>
     * @param outputStream output stream opened from <code>connection</code>
     * @exception IOException if an I/O error occurs while
     *                  the connection is terminated.
     */
    protected void disconnect(StreamConnection connection,
           InputStream inputStream, OutputStream outputStream) 
	throws IOException {
        try {
            try {
                inputStream.close();
            } finally {
                try {
                    outputStream.close();
                } finally {
                    connection.close();
                }
            }
        } catch (IOException e) {
	    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
	        Logging.report(Logging.WARNING, LogChannels.LC_PROTOCOL,
	    	          "Exception while closing  streams|connection");
	    }

        } catch (NullPointerException e) {

        }
    }

    /**
     * Return the security information associated with this connection.
     * If the connection is still in <CODE>Setup</CODE> state then
     * the connection is initiated to establish the secure connection
     * to the server.  The method returns when the connection is
     * established and the <CODE>Certificate</CODE> supplied by the
     * server has been validated.
     * The <CODE>SecurityInfo</CODE> is only returned if the
     * connection has been successfully made to the server.
     *
     * @return the security information associated with this open connection.
     *
     * @exception CertificateException if the <code>Certificate</code>
     * supplied by the server cannot be validated.
     * The <code>CertificateException</code> will contain
     * the information about the error and indicate the certificate in the
     * validation chain with the error.
     * @exception IOException if an arbitrary connection failure occurs
     */
    public SecurityInfo getSecurityInfo() throws IOException {
        ensureOpen();

        sendRequest();

        if (sslConnection == null) {
            /*
             * This is a persistent connection so the connect method did 
             * not get called, so the stream connection of HTTP class
             * will be a SSL connection. Get the info from that.
             */
            StreamConnection sc =
                ((StreamConnectionElement)getStreamConnection()).
                    getBaseConnection();

            return ((SSLStreamConnection)sc).getSecurityInfo();
        }

        return sslConnection.getSecurityInfo();
    }
}
