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

package com.sun.midp.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.SecurityInfo;
import javax.microedition.io.StreamConnection;

import javax.microedition.pki.Certificate;

import com.sun.midp.pki.*;

/**
 * The SSLStreamConnection class implements the StreamConnection
 * interface. Data exchanged through a SSLStreamConnection is 
 * automatically protected by SSL. Currently, only SSL version 3.0
 * is supported and the list of cipher suites proposed 
 * by the client is hardcoded to {SSL_RSA_WITH_RC4_128_MD5,
 * SSL_RSA_EXPORT_WITH_RC4_40_MD5}. This version of the implementation 
 * does not support client authentication at the SSL layer -- a feature
 * that is rarely used.
 * 
 * Typical usage of this class by an application would be along the
 * following lines: <BR />
 * 
 * <PRE>
 *      // create a TCP connection
 *      StreamConnection t = Connector.open("socket://www.server.com:443");
 *
 *      // Create an SSL connection
 *      SSLStreamConnection s = new SSLStreamConnection("www.server.com", 443,
 *                 t.openInputStream(), t.openOutputStream());
 *      t.close();
 * 
 *      // obtain the associated input/output streams
 *      OutputStream sout = s.openOutputStream();
 *      InputStream sin = s.openInputStream();
 *      ...
 *      // send SSL-protected data by writing to sout and
 *      // receive SSL-protected by reading from sin
 *      ...
 *      sin.close();
 *      sout.close();
 *      s.close();   // close the SSL connection when done
 *
 * </PRE>
 * 
 */ 

public class SSLStreamConnection implements StreamConnection {
    /** Indicates that a is ready to be opened. */
    static final int READY = 0;
    /** Indicates that a stream is opened. */
    static final int OPEN = 1;
    /** Indicates that a stream is closed. */
    static final int CLOSED = 2;

    /** Current record being processed. */
    private Record rec = null;
    /** Input stream for buffered records. */
    private In uin = null;
    /** Output stream for buffered records. */
    private Out uout = null;
    /** Raw encrypted input stream. */
    private InputStream sin = null;
    /** Raw encrypted output stream. */
    private OutputStream sout = null;
    /** Current host name. */
    private String host = null;
    /** Current port number. */
    private int port = 0;
    /** Flag indicating the underlying TCP connection is open. */
    private boolean copen = false;
    /** Server certificate from a successful handshake. */
    private X509Certificate serverCert;
    /** Cipher suite from a successful handshake. */
    private String cipherSuite;

    /*
     * The following are visible within the package so In and Out can 
     * manipulate them directly
     */ 
    /** State of the input stream given out by getInputStream. */
    int inputStreamState;
    /** State of the output stream given out by getOutputStream. */
    int outputStreamState;
    
    /**
     * Establish and SSL session over a reliable stream.
     * This connection will forward the input and output stream close methods
     * to the given connection. If the caller wants to have the given
     * connection closed with this connection, the caller can close given
     * connection after constructing this connection, but leaving the closing
     * of the streams to this connection.
     * 
     * @param host hostname of the SSL server
     * @param port port number of the SSL server
     * @param in   InputStream associated with the StreamConnection
     * @param out  OutputStream associated with the StreamConnection
     * @param cs trusted certificate store to be used for this connection
     *
     * @exception IOException if there is a problem initializing the SSL
     * data structures or the SSL handshake fails
     */ 
    public SSLStreamConnection(String host, int port,
                               InputStream in, OutputStream out,
                               CertStore cs)
            throws IOException {

        if (cs == null) {
            throw new IllegalArgumentException(
                "SSLStreamConnection: no trusted certificate store given");
        }

        if ((in == null) || (out == null)) {
            throw new IllegalArgumentException(
                "SSLStreamConnection: stream missing");
        }

        this.host = host;
        this.port = port;
        this.sin = in;
        this.sout = out;

        this.rec = new Record(sin, sout);

        uin = new In(rec, this);
        uout = new Out(rec, this);
        
        try {
            Handshake hndshk = new Handshake(host, port, rec, cs);

            hndshk.doHandShake(Record.CLIENT);
            serverCert = hndshk.sCert;
            cipherSuite = hndshk.negSuiteName;
        } catch (IOException e) {
            cleanupIfNeeded();
            throw e;
        }

        copen = true;
    }
    
    /**
     * Returns the InputStream associated with this SSLStreamConnection.
     *
     * @return InputStream object from which SSL protected bytes can
     * be read
     * @exception IOException if the connection is not open or the stream was 
     * already open
     */ 
    synchronized public InputStream openInputStream() throws IOException {
        if (!copen) {
            throw new IOException("Connection closed");
        }

        if (inputStreamState != READY) {
            throw new IOException("Input stream already opened");
        }

        inputStreamState = OPEN;
        return (uin);
    }
    
    /**
     * Returns the OutputStream associated with this SSLStreamConnection.
     * 
     * @return OutputStream object such that bytes written to this stream
     * are sent over an SSL secured channel
     * @exception IOException if the connection is not open or the stream was 
     * already open
     */
    synchronized public OutputStream openOutputStream() throws IOException {
        if (!copen) {
            throw new IOException("Connection closed");
        }

        if (outputStreamState != READY) {
            throw new IOException("Output stream already opened");
        }

        outputStreamState = OPEN;
        return (uout);
    }
    
    /**
     * Returns the DataInputStream associated with this SSLStreamConnection.
     * @exception IOException if the connection is not open or the stream was 
     * already open
     * @return a DataInputStream object
     */ 
    public DataInputStream openDataInputStream() throws IOException {
	return (new DataInputStream(openInputStream()));
    }
     
    /** 
     * Returns the DataOutputStream associated with this SSLStreamConnection.
     * @exception IOException if the connection is not open or the stream was 
     * already open
     * @return a DataOutputStream object
     */
    public DataOutputStream openDataOutputStream() throws IOException {
          return (new DataOutputStream(openOutputStream()));
    }
            
    /**
     * Closes the SSL connection. The underlying TCP socket, over which
     * SSL is layered, is also closed unless the latter was opened by
     * an external application and its input/output streams were passed 
     * as argument to the SSLStreamConnection constructor.
     *
     * @exception IOException if the SSL connection could not be
     *                        terminated cleanly
     */ 
    synchronized public void close() throws IOException {
        if (copen) {
            copen = false;
            cleanupIfNeeded();
        }
    }

    /**
     * Returns the security information associated with this connection.
     *
     * @return the security information associated with this open connection
     *
     * @exception IOException if the connection is closed
     */
    public SecurityInfo getSecurityInfo() throws IOException {
        if (!copen) {
            throw new IOException("Connection closed");
        }

        return new SSLSecurityInfo(this);
    }

    /**
     * Returns the server certificate associated with this connection.
     *
     * @return the server certificate associated with this connection
     */
    public X509Certificate getServerCertificate() {
        return serverCert;
    }

    /**
     * Returns the cipher suite in use for the connection.
     * The value returned is one of the CipherSuite definitions
     * in Appendix C of RFC 2246.
     * The cipher suite string should be used to represent the
     * actual parameters used to establish the connection regardless
     * of whether the secure connection uses SSL V3 or TLS 1.0 or WTLS.
     *
     * @return a String containing the cipher suite in use
     */
    String getCipherSuite() {
        return cipherSuite;
    }

    /**
     * Closes the SSL connection. The underlying TCP socket, over which
     * SSL is layered, is also closed unless the latter was opened by
     * an external application and its input/output streams were passed 
     * as argument to the SSLStreamConnection constructor.
     * 
     * @exception IOException if the SSL connection could not be
     *                        terminated cleanly
     */ 
    void cleanupIfNeeded() throws IOException {
        if (copen || inputStreamState == OPEN || outputStreamState == OPEN ||
               rec == null) {
            // we do not need to cleanup
            return;
        }

        rec.shutdownConnection();
        rec = null;
    }
}


/**
 * This class implements methods
 * to access information about a SSL secure network connection.
 */
class SSLSecurityInfo implements SecurityInfo {

    /** Parent connection. */
    private SSLStreamConnection parent;

    /**
     * Creates a <code>SecurityInfo</code> object to pass back to
     * an application.
     *
     * @param parentObj parent object
     */
    SSLSecurityInfo(SSLStreamConnection parentObj) {
        parent = parentObj;
    }

    /**
     * Gets the <CODE>Certificate</CODE> used to establish the
     * secure connection with the server.
     *
     * @return the <CODE>Certificate</CODE> used to establish the
     * secure connection with the server
     */
    public Certificate getServerCertificate() {
        return parent.getServerCertificate();
    }

    /**
     * Returns the security status of the connection.
     *
     * @return <CODE>true</CODE> if the connection has been made directly to
     * the server specified in <code>Connector.open</code> and a handshake
     * with that server has established a secure connection.
     * <CODE>false</CODE> is returned otherwise
     */
    public boolean isSecure() {
        return true;
    }

    /**
     * Returns the protocol version.
     * If appropriate, it should contain the major and minor versions
     * for the protocol separated with a "." (Unicode x2E).
     * For example,
     *     for SSL V3 it MUST return "3.0";
     *     for TLS 1.0 it MUST return "3.1".
     *
     * @return a String containing the version of the protocol
     */
    public String getProtocolVersion() {
        return "3.0";
    }

    /**
     * Returns the secure protocol name.
     *
     * @return a <code>String</code> containing the secure protocol identifier;
     * if TLS (RFC 2246) is used for the connection the return value is "TLS".
     * If SSL V3 (The SSL Protocol Version 3.0) is used for the connection
     * the return value is "SSL").
     * If WTLS (WAP 199) is used for the connection the return value is "WTLS".
     */
    public String getProtocolName() {
        return "SSL";
    }

    /**
     * Returns the cipher suite in use for the connection.
     * The value returned is one of the CipherSuite definitions
     * in Appendix C of RFC 2246.
     * The cipher suite string should be used to represent the
     * actual parameters used to establish the connection regardless
     * of whether the secure connection uses SSL V3 or TLS 1.0 or WTLS.
     *
     * @return a String containing the cipher suite in use
     */
    public String getCipherSuite() {
        return parent.getCipherSuite();
    }
}
