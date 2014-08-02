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

package com.sun.j2me.content;

import javax.microedition.io.Connector;
import javax.microedition.io.Connection;
import javax.microedition.io.ContentConnection;
import javax.microedition.io.HttpConnection;

import java.io.IOException;

/**
 * The helper class extending GCF Connector functionality with demands
 * of the JSR 211 specification:
 * <ul>
 *  <li> the connection may deliver the content from a cache.
 *  <li> the connection should use user credentials.
 * </ul>
 */
class ContentReader {

    private String url;
    private String username;
    private String password;

    ContentReader(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * Creates and opens a Connection to the content addressed by
     * the <code>url</code>. This method is
     * similar to <code>Connector.open(url, READ, timeouts)</code>
     * but may deliver the content from a cache.
     * Regardless of whether or not the content is cached, the
     * application must have permission to access
     * the content via the <code>url</code>.
     *
     * @param timeouts         a flag to indicate that the caller
     *                         wants timeout exceptions
     * @return                 a Connection object
     *
     * @exception ConnectionNotFoundException is thrown if:
     *   <ul>
     *      <li>the target URL can not be found, or</li>
     *      <li>the requested protocol type is not supported</li>
     *   </ul>
     * @exception NullPointerException if the URL is null
     * @exception IllegalArgumentException if an <code>url</code> parameter is invalid.
     * @exception java.io.IOException  if some other kind of I/O error occurs
     * @exception SecurityException is thrown if access to the
     *   protocol handler is prohibited
     */
    Connection open(boolean timeouts) throws IOException, SecurityException {
        return openPrim(timeouts);
    }

    /**
     * Finds the type of the content in this Invocation.
     * <p>
     * The calling thread blocks while the type is being determined.
     * If a network access is needed there may be an associated delay.
     *
     * @return the content type.
     *          May be <code>null</code> if the type can not be determined.
     *
     * @exception IOException if access to the content fails
     * @exception IllegalArgumentException if the content is accessed via
     *  the URL and the URL is invalid
     * @exception SecurityException is thrown if access to the content
     *  is required and is not permitted
     */
    String findType() throws IOException, SecurityException {
        String type = null;
        Connection conn = openPrim(true);
        if (conn instanceof ContentConnection) {
        	if( conn instanceof HttpConnection ){
        		HttpConnection hc = (HttpConnection)conn;
	            hc.setRequestMethod(HttpConnection.HEAD);
	
	            // actual connection performed, some delay...
	            if (hc.getResponseCode() != HttpConnection.HTTP_OK)
	            	return null;
        	}
        	
            type = ((ContentConnection)conn).getType();
            conn.close();

            if (type != null) {
                // Check for and remove any parameters (rfc2616)
                int ndx = type.indexOf(';');
                if (ndx >= 0) {
                    type = type.substring(0, ndx);
                }
                type = type.trim();
                if (type.length() == 0) {
                    type = null;
                }
            }
        }

        return type;
    }

    /**
     * The method currently supports only HTTP protocol and basic authentication.
     *
     * @param timeouts a flag to indicate that the caller
     *                         wants timeout exceptions.
     * @param headsOnly open connection for content type discover.
     *
     * @return a Connection object
     *
     * @exception IOException if access to the content fails
     * @exception IllegalArgumentException if the content is accessed via
     *  the URL and the URL is invalid
     * @exception SecurityException is thrown if access to the content
     *  is required and is not permitted
     */
    private Connection openPrim(boolean timeouts)
            				throws IOException, SecurityException {
    	Connection conn = Connector.open(url, Connector.READ, timeouts);
        if (conn instanceof HttpConnection && 
        		(username != null || password != null)) {
            HttpConnection httpc = (HttpConnection)conn;
            httpc.setRequestMethod(HttpConnection.HEAD);
            // actual connection performed, some delay...
            int rc = httpc.getResponseCode();

            // try to set authorization
            if (rc == HttpConnection.HTTP_UNAUTHORIZED ||
                    	rc == HttpConnection.HTTP_PROXY_AUTH) {
                String authType = httpc.getHeaderField("WWW-Authenticate");
                if (authType == null || !authType.trim().equalsIgnoreCase("basic")) {
                    throw new IOException("not supported authorization");
                }

                conn.close();
                // reopen connection with authorization property set
                conn = Connector.open(url, Connector.READ, timeouts);
                httpc = (HttpConnection)conn;
                httpc.setRequestProperty(
                        rc == HttpConnection.HTTP_UNAUTHORIZED?
                        "Authorization": "Proxy-Authorization",
                        formatAuthCredentials(username, password));
                return conn;
            }
            conn.close();
            conn = Connector.open(url, Connector.READ, timeouts);
        }
        return conn;
    }


    /**
     * Formats the username and password for HTTP basic authentication
     * according RFC 2617.
     *
     * @param username for HTTP authentication
     * @param password for HTTP authentication
     *
     * @return properly formated basic authentication credential
     */
    private static String formatAuthCredentials(String username,
                                                String password) {
        byte[] data = new byte[username.length() + password.length() + 1];
        int j = 0;

        for (int i = 0; i < username.length(); i++, j++) {
            data[j] = (byte)username.charAt(i);
        }

        data[j] = (byte)':';
        j++;

        for (int i = 0; i < password.length(); i++, j++) {
            data[j] = (byte)password.charAt(i);
        }

        return "Basic " + encode(data, 0, data.length);
    }

    /**
     * Converts a byte array into a Base64 encoded string.
     * @param data bytes to encode
     * @param offset which byte to start at
     * @param length how many bytes to encode; padding will be added if needed
     * @return base64 encoding of data; 4 chars for every 3 bytes
     */
    private static String encode(byte[] data, int offset, int length) {
        int i;
        int encodedLen;
        char[] encoded;

        // 4 chars for 3 bytes, run input up to a multiple of 3
        encodedLen = (length + 2) / 3 * 4;
        encoded = new char [encodedLen];

        for (i = 0, encodedLen = 0; encodedLen < encoded.length;
             i += 3, encodedLen += 4) {
            encodeQuantum(data, offset + i, length - i, encoded, encodedLen);
        }

        return new String(encoded);
    }

    /**
     * Encodes 1, 2, or 3 bytes of data as 4 Base64 chars.
     *
     * @param in buffer of bytes to encode
     * @param inOffset where the first byte to encode is
     * @param len how many bytes to encode
     * @param out buffer to put the output in
     * @param outOffset where in the output buffer to put the chars
     */
    private static void encodeQuantum(byte in[], int inOffset, int len,
                                      char out[], int outOffset) {
	byte a = 0, b = 0, c = 0;

        a = in[inOffset];
        out[outOffset] = ALPHABET[(a >>> 2) & 0x3F];

        if (len > 2) {
            b = in[inOffset + 1];
            c = in[inOffset + 2];
            out[outOffset + 1] = ALPHABET[((a << 4) & 0x30) +
                                         ((b >>> 4) & 0xf)];
	    out[outOffset + 2] = ALPHABET[((b << 2) & 0x3c) +
                                          ((c >>> 6) & 0x3)];
	    out[outOffset + 3] = ALPHABET[c & 0x3F];
        } else if (len > 1) {
            b = in[inOffset + 1];
            out[outOffset + 1] = ALPHABET[((a << 4) & 0x30) +
                                         ((b >>> 4) & 0xf)];
	    out[outOffset + 2] =  ALPHABET[((b << 2) & 0x3c) +
                                          ((c >>> 6) & 0x3)];
	    out[outOffset + 3] = '=';
        } else {
            out[outOffset + 1] = ALPHABET[((a << 4) & 0x30) +
                                         ((b >>> 4) & 0xf)];
	    out[outOffset + 2] = '=';
	    out[outOffset + 3] = '=';
        }
    }

    /**
     * This character array provides the alphabet map from RFC1521.
     */
    private final static char ALPHABET[] = {
	//       0    1    2    3    4    5    6    7
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',  // 0
		'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',  // 1
		'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',  // 2
		'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',  // 3
		'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',  // 4
		'o', 'p', 'q', 'r', 's', 't', 'u', 'v',  // 5
		'w', 'x', 'y', 'z', '0', '1', '2', '3',  // 6
		'4', '5', '6', '7', '8', '9', '+', '/'  // 7
	};

}
