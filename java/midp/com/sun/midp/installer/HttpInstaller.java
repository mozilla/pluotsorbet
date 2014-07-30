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

package com.sun.midp.installer;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.Connection;
import javax.microedition.io.HttpConnection;

import com.sun.midp.io.j2me.storage.RandomAccessStream;
import com.sun.midp.io.Base64;
import com.sun.midp.io.HttpUrl;
import com.sun.midp.io.Util;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * An Installer allowing to install a midlet suite from an http server.
 * If the midlet suite is given by a descriptor file, the jar URL
 * specified in the descriptor must have an "http" or "https" scheme.
 */
public class HttpInstaller extends Installer {
    /** Max number of bytes to download at one time (1K). */
    private static final int MAX_DL_SIZE = 1024;

    /** Tag that signals that the HTTP server supports basic auth. */
    private static final String BASIC_TAG = "basic";

    /** HTTP connection to close when we stop the installation. */
    private HttpConnection httpConnection;

    /** HTTP stream to close when we stop the installation. */
    private InputStream httpInputStream;

    /**
     * Constructor of the HttpInstaller.
     */
    public HttpInstaller() {
        super();
    }

    /**
     * Downloads an application descriptor file from the given URL.
     *
     * @return a byte array representation of the file or null if not found
     *
     * @exception IOException is thrown if any error prevents the download
     *   of the JAD
     */
    protected byte[] downloadJAD() throws IOException {
        String[] encoding = new String[1];
        ByteArrayOutputStream bos = new ByteArrayOutputStream(MAX_DL_SIZE);
        String[] acceptableTypes = {JAD_MT};
        String[] extraFieldKeys = new String[3];
        String[] extraFieldValues = new String[3];
        String locale;
        String prof = System.getProperty(MICROEDITION_PROFILES);
        int space = prof.indexOf(' ');
        if (space != -1) {
            prof = prof.substring(0, space);
        }

        extraFieldKeys[0] = "User-Agent";
        extraFieldValues[0] = "Profile/" + prof
                              + " Configuration/" +
                              System.getProperty(MICROEDITION_CONFIG);

        extraFieldKeys[1] = "Accept-Charset";
        extraFieldValues[1] = "UTF-8, ISO-8859-1";

        /* locale can be null */
        locale = System.getProperty(MICROEDITION_LOCALE);
        if (locale != null) {
            extraFieldKeys[2] = "Accept-Language";
            extraFieldValues[2] = locale;
        }

        state.beginTransferDataStatus = DOWNLOADING_JAD;
        state.transferStatus = DOWNLOADED_1K_OF_JAD;

        /*
         * Do not send the list of acceptable types because some servers
         * will send a 406 if the URL is to a JAR. It is better to
         * reject the resource at the client after check the media-type so
         * if the type is JAR a JAR only install can be performed.
         */
        downloadResource(info.jadUrl, extraFieldKeys,
                         extraFieldValues,
                         acceptableTypes, false, false, bos, encoding,
                         InvalidJadException.INVALID_JAD_URL,
                         InvalidJadException.JAD_SERVER_NOT_FOUND,
                         InvalidJadException.JAD_NOT_FOUND,
                         InvalidJadException.INVALID_JAD_TYPE);

        state.jadEncoding = encoding[0];
        return bos.toByteArray();
    }

    /**
     * Downloads an application archive file from the given URL into the
     * given file. Automatically handle re-tries.
     *
     * @param filename name of the file to write. This file resides
     *          in the storage area of the given application
     *
     * @return size of the JAR
     *
     * @exception IOException is thrown if any error prevents the download
     *   of the JAR
     */
    protected int downloadJAR(String filename)
            throws IOException {
        HttpUrl parsedUrl;
        String url;
        String[] acceptableTypes = {JAR_MT_1, JAR_MT_2};
        String[] extraFieldKeys = new String[3];
        String[] extraFieldValues = new String[3];
        int jarSize;
        String locale;
        String prof;
        int space;
        RandomAccessStream jarOutputStream = null;
        OutputStream outputStream = null;

        parsedUrl = new HttpUrl(info.jarUrl);
        if (parsedUrl.authority == null && info.jadUrl != null) {
            // relative URL, add the JAD URL as the base
            try {
                parsedUrl.addBaseUrl(info.jadUrl);
            } catch (IOException e) {
                postInstallMsgBackToProvider(
                    OtaNotifier.INVALID_JAD_MSG);
                throw new InvalidJadException(
                         InvalidJadException.INVALID_JAR_URL);
            }

            url = parsedUrl.toString();

            // The JAR URL saved to storage MUST be absolute
            info.jarUrl = url;
        } else {
            url = info.jarUrl;
        }

        jarOutputStream = new RandomAccessStream();
        jarOutputStream.connect(filename,
                                RandomAccessStream.READ_WRITE_TRUNCATE);
        outputStream = jarOutputStream.openOutputStream();

        prof = System.getProperty(MICROEDITION_PROFILES);
        space = prof.indexOf(' ');
        if (space != -1) {
            prof = prof.substring(0, space);
        }

        extraFieldKeys[0] = "User-Agent";
        extraFieldValues[0] = "Profile/" + prof
                              + " Configuration/" +
                              System.getProperty(MICROEDITION_CONFIG);

        extraFieldKeys[1] = "Accept-Charset";
        extraFieldValues[1] = "UTF-8, ISO-8859-1";

        /* locale can be null */
        locale = System.getProperty(MICROEDITION_LOCALE);
        if (locale != null) {
            extraFieldKeys[2] = "Accept-Language";
            extraFieldValues[2] = locale;
        }

        try {
            state.beginTransferDataStatus = DOWNLOADING_JAR;
            state.transferStatus = DOWNLOADED_1K_OF_JAR;
            jarSize = downloadResource(url, extraFieldKeys, extraFieldValues,
                         acceptableTypes, true, true, outputStream, null,
                         InvalidJadException.INVALID_JAR_URL,
                         InvalidJadException.JAR_SERVER_NOT_FOUND,
                         InvalidJadException.JAR_NOT_FOUND,
                         InvalidJadException.INVALID_JAR_TYPE);
            return jarSize;
        } catch (InvalidJadException ije) {
            switch (ije.getReason()) {
            case InvalidJadException.INVALID_JAR_URL:
            case InvalidJadException.JAR_SERVER_NOT_FOUND:
            case InvalidJadException.JAR_NOT_FOUND:
            case InvalidJadException.INVALID_JAR_TYPE:
                postInstallMsgBackToProvider(
                    OtaNotifier.INVALID_JAR_MSG);
                break;

            default:
                // for safety/completeness.
                if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                    Logging.report(Logging.ERROR, LogChannels.LC_AMS,
                    "Installer InvalidJadException: " + ije.getMessage());
                }
                break;
            }

            throw ije;
        } finally {
            try {
                jarOutputStream.disconnect();
            } catch (Exception e) {
                 if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                     Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                     "disconnect  threw a  Exception");
                 }
            }
        }
    }

    /**
     * Downloads an resource from the given URL into the output stream.
     *
     * @param url location of the resource to download
     * @param extraFieldKeys keys to the extra fields to put in the request
     * @param extraFieldValues values to the extra fields to put in the request
     * @param acceptableTypes list of acceptable media types for this resource,
     *                        there must be at least one
     * @param sendAcceptableTypes if true the list of acceptable media types
     *       for this resource will be sent in the request
     * @param allowNoMediaType if true it is not consider an error if
     *       the media type is not in the response
     *       for this resource will be sent in the request
     * @param output output stream to write the resource to
     * @param encoding an array to receive the character encoding of resource,
     *                 can be null
     * @param invalidURLCode reason code to use when the URL is invalid
     * @param serverNotFoundCode reason code to use when the server is not
     *     found
     * @param resourceNotFoundCode reason code to use when the resource is not
     *     found on the server
     * @param invalidMediaTypeCode reason code to use when the media type of
     *     the resource is not valid
     *
     * @return size of the resource
     *
     * @exception IOException is thrown if any error prevents the download
     *   of the resource
     */
    private int downloadResource(String url, String[] extraFieldKeys,
            String[] extraFieldValues, String[] acceptableTypes,
            boolean sendAcceptableTypes, boolean allowNoMediaType,
            OutputStream output, String[] encoding, int invalidURLCode,
            int serverNotFoundCode, int resourceNotFoundCode,
            int invalidMediaTypeCode) throws IOException {
        Connection conn = null;
        StringBuffer acceptField;
        int responseCode;
        String retryAfterField;
        int retryInterval;
        String mediaType;
        boolean resourceMoved = false;

        try {
            for (; ; ) {
                try {
                    conn = Connector.open(url, Connector.READ);
                } catch (IllegalArgumentException e) {
                    throw new InvalidJadException(invalidURLCode, url);
                } catch (ConnectionNotFoundException e) {
                    if (url.startsWith("http:") || url.startsWith("https:")) {
                        throw new InvalidJadException(serverNotFoundCode, url);
                    }

                    // protocol not found
                    throw new InvalidJadException(invalidURLCode, url);
                }

                if (!(conn instanceof HttpConnection)) {
                    // only HTTP or HTTPS are supported
                    throw new InvalidJadException(invalidURLCode, url);
                }

                httpConnection = (HttpConnection)conn;

                if (extraFieldKeys != null) {
                    for (int i = 0; i < extraFieldKeys.length &&
                                    extraFieldKeys[i] != null; i++) {
                        httpConnection.setRequestProperty(extraFieldKeys[i],
                                                          extraFieldValues[i]);
                    }
                }

                // 256 is given to avoid resizing without adding lengths
                acceptField = new StringBuffer(256);

                if (sendAcceptableTypes) {
                    // there must be one or more acceptable media types
                    acceptField.append(acceptableTypes[0]);
                    for (int i = 1; i < acceptableTypes.length; i++) {
                        acceptField.append(", ");
                        acceptField.append(acceptableTypes[i]);
                    }
                } else {
                    /* Send at least a wildcard to satisfy WAP gateways. */
                    acceptField.append("*/*");
                }

                httpConnection.setRequestProperty("Accept",
                                                  acceptField.toString());
                httpConnection.setRequestMethod(HttpConnection.GET);

                if (state.username != null &&
                    state.password != null) {
                    httpConnection.setRequestProperty("Authorization",
                        formatAuthCredentials(state.username,
                                              state.password));
                }

                if (state.proxyUsername != null &&
                    state.proxyPassword != null) {
                    httpConnection.setRequestProperty("Proxy-Authorization",
                        formatAuthCredentials(state.proxyUsername,
                                              state.proxyPassword));
                }

                try {
                    responseCode = httpConnection.getResponseCode();
                } catch (IOException ioe) {
                    if (httpConnection.getHost() == null) {
                        throw new InvalidJadException(invalidURLCode, url);
                    }

                    throw new InvalidJadException(serverNotFoundCode, url);
                }

                if (responseCode == HttpConnection.HTTP_MOVED_PERM ||
                        responseCode == HttpConnection.HTTP_MOVED_TEMP ||
                            responseCode == HttpConnection.HTTP_TEMP_REDIRECT ||
                                responseCode == HttpConnection.HTTP_SEE_OTHER) {
                    // prevent multiple redirection attempts
                    if (resourceMoved) {
                        // exception will be thrown bellow
                        break;
                    }

                    /*
                     * Resource was moved, get a new location and retry
                     * the operation.
                     */
                    resourceMoved = true;
                    url = httpConnection.getHeaderField("Location");
                    try {
                        httpConnection.close();
                    } catch (Exception e) {
                        // ignore
                    }

                    // confirm redirection with the user
                    if (state.listener != null &&
                            !state.listener.confirmRedirect(state, url)) {
                        state.stopInstallation = true;
                        postInstallMsgBackToProvider(
                            OtaNotifier.USER_CANCELLED_MSG);
                        throw new IOException("stopped");
                    }

                    continue;
                }

                if (responseCode != HttpConnection.HTTP_UNAVAILABLE) {
                    break;
                }

                retryAfterField = httpConnection.getHeaderField("Retry-After");
                if (retryAfterField == null) {
                    break;
                }

                try {
                    /*
                     * see if the retry interval is in seconds, and
                     * not an absolute date
                     */
                    retryInterval = Integer.parseInt(retryAfterField);
                    if (retryInterval > 0) {
                        if (retryInterval > 60) {
                            // only wait 1 min
                            retryInterval = 60;
                        }

                        Thread.sleep(retryInterval * 1000);
                    }
                } catch (InterruptedException ie) {
                    // ignore thread interrupt
                    break;
                } catch (NumberFormatException ne) {
                    // ignore bad format
                    break;
                }

                httpConnection.close();

                if (state.stopInstallation) {
                    postInstallMsgBackToProvider(
                        OtaNotifier.USER_CANCELLED_MSG);
                    throw new IOException("stopped");
                }
            } // end for

            if (responseCode == HttpConnection.HTTP_NOT_FOUND) {
                throw new InvalidJadException(resourceNotFoundCode);
            }

            if (responseCode == HttpConnection.HTTP_NOT_ACCEPTABLE) {
                throw new InvalidJadException(invalidMediaTypeCode, "");
            }

            if (responseCode == HttpConnection.HTTP_UNAUTHORIZED) {
                // automatically throws the correct exception
                checkIfBasicAuthSupported(
                     httpConnection.getHeaderField("WWW-Authenticate"));

                state.exception =
                    new InvalidJadException(InvalidJadException.UNAUTHORIZED);
                return 0;
            }

            if (responseCode == HttpConnection.HTTP_PROXY_AUTH) {
                // automatically throws the correct exception
                checkIfBasicAuthSupported(
                     httpConnection.getHeaderField("WWW-Authenticate"));

                state.exception =
                    new InvalidJadException(InvalidJadException.PROXY_AUTH);
                return 0;
            }

            if (responseCode != HttpConnection.HTTP_OK) {
                throw new IOException("Failed to download " + url +
                                      " HTTP response code: " + responseCode);
            }

            mediaType = Util.getHttpMediaType(httpConnection.getType());

            if (mediaType != null) {
                boolean goodType = false;

                for (int i = 0; i < acceptableTypes.length; i++) {
                    if (mediaType.equals(acceptableTypes[i])) {
                        goodType = true;
                        break;
                    }
                }

                if (!goodType) {
                    throw new InvalidJadException(invalidMediaTypeCode,
                                                  mediaType);
                }
            } else if (!allowNoMediaType) {
                throw new InvalidJadException(invalidMediaTypeCode, "");
            }

            if (encoding != null) {
                encoding[0] = getCharset(httpConnection.getType());
            }

            httpInputStream = httpConnection.openInputStream();
            return transferData(httpInputStream, output, MAX_DL_SIZE);
        } finally {
            // Close the streams or connections this method opened.
            try {
                httpInputStream.close();
            } catch (Exception e) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                    "stream close  threw an Exception");
                }
            }

            try {
                conn.close();
            } catch (Exception e) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                    "connection close  threw an Exception");
                }
            }
        }
    }

    /**
     * Compares two URLs for equality in sense that they have the same
     * scheme, host and path.
     *
     * @param url1 the first URL for comparision
     * @param url1 the second URL for comparision
     *
     * @return true if the scheme, host and path of the first given url
     *              is identical to the scheme, host and path of the second
     *              given url; false otherwise
     */
    protected boolean isSameUrl(String url1, String url2) {
        HttpUrl newUrl;
        HttpUrl originalUrl;

        try {
            newUrl = new HttpUrl(url1);
            originalUrl = new HttpUrl(url2);

            if (newUrl.scheme.equals(originalUrl.scheme) &&
                newUrl.host.equals(originalUrl.host) &&
                newUrl.path.equals(originalUrl.path)) {
                return true;
            }
        } catch (NullPointerException npe) {
            // no match, fall through
        }

        return false;
    }

    /**
     * Checks to make sure the HTTP server will support Basic authentication.
     *
     * @param wwwAuthField WWW-Authenticate field from the response header
     *
     * @exception InvalidJadException if server does not support Basic
     *                                authentication
     */
    private void checkIfBasicAuthSupported(String wwwAuthField)
            throws InvalidJadException {
        if (wwwAuthField != null) {
            wwwAuthField = wwwAuthField.trim();

            if (wwwAuthField.regionMatches(true, 0, BASIC_TAG, 0,
                                           BASIC_TAG.length())) {
                return;
            }
        }

        throw new InvalidJadException(InvalidJadException.CANNOT_AUTH);
    }

    /**
     * Parses out the charset from the content-type field.
     * The charset parameter is after the ';' in the content-type field.
     *
     * @param contentType value of the content-type field
     *
     * @return charset
     */
    private static String getCharset(String contentType) {
        int start;
        int end;

        if (contentType == null) {
            return null;
        }

        start = contentType.indexOf("charset");
        if (start < 0) {
            return null;
        }

        start = contentType.indexOf('=', start);
        if (start < 0) {
            return null;
        }

        // start past the '='
        start++;

        end = contentType.indexOf(';', start);
        if (end < 0) {
            end = contentType.length();
        }

        return contentType.substring(start, end).trim();
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

        return "Basic " + Base64.encode(data, 0, data.length);
    }

    /**
     * Stops the installation. If installer is not installing then this
     * method has no effect. This will cause the install method to
     * throw an IOException if the install is not writing the suite
     * to storage which is the point of no return.
     *
     * @return true if the install will stop, false if it is too late
     */
    public boolean stopInstalling() {

        boolean res = super.stopInstalling();
        if (!res) {
            return res;
        }

        try {
            httpInputStream.close();
        } catch (Exception e) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                "stream close threw an Exception");
            }
        }

        try {
            httpConnection.close();
        } catch (Exception e) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                "stream close threw an Exception");
            }
        }

        return true;
    }

}
