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

package com.sun.midp.main;

import java.io.IOException;

import javax.microedition.io.Connection;
import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import com.sun.midp.io.Util;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.midlet.PlatformRequest;

import com.sun.midp.security.SecurityToken;

/**
 * Implements platform request functionality for CLDC platform.
 */
class CldcPlatformRequest implements PlatformRequest {

    /** Class name of the installer use for plaformRequest. */
    static final String INSTALLER_CLASS =
        "com.sun.midp.installer.GraphicalInstaller";

    /** Media-Type for valid application descriptor files. */
    static final String JAD_MT = "text/vnd.sun.j2me.app-descriptor";

    /** Media-Type for valid Jar file. */
    static final String JAR_MT_1 = "application/java";

    /** Media-Type for valid Jar file. */
    static final String JAR_MT_2 = "application/java-archive";

    /** This class has a different security domain than the application. */
    private SecurityToken securityToken;

    /**
     * Initializes the security token for this object, so it can
     * perform actions that a normal MIDlet Suite cannot.
     *
     * @param token security token for this object
     */
    CldcPlatformRequest(SecurityToken token) {
        securityToken = token;
    }

    /**
     * Requests that the device handle (e.g. display or install)
     * the indicated URL.
     *
     * <p>If the platform has the appropriate capabilities and
     * resources available, it SHOULD bring the appropriate
     * application to the foreground and let the user interact with
     * the content, while keeping the MIDlet suite running in the
     * background. If the platform does not have appropriate
     * capabilities or resources available, it MAY wait to handle the
     * URL request until after the MIDlet suite exits. In this case,
     * when the requesting MIDlet suite exits, the platform MUST then
     * bring the appropriate application to the foreground to let the
     * user interact with the content.</p>
     *
     * <p>This is a non-blocking method. In addition, this method does
     * NOT queue multiple requests. On platforms where the MIDlet
     * suite must exit before the request is handled, the platform
     * MUST handle only the last request made. On platforms where the
     * MIDlet suite and the request can be handled concurrently, each
     * request that the MIDlet suite makes MUST be passed to the
     * platform software for handling in a timely fashion.</p>
     *
     * <p>If the URL specified refers to a MIDlet suite (either an
     * Application Descriptor or a JAR file), the request is
     * interpreted as a request to install the named package. In this
     * case, the platform's normal MIDlet suite installation process
     * SHOULD be used, and the user MUST be allowed to control the
     * process (including cancelling the download and/or
     * installation). If the MIDlet suite being installed is an
     * <em>update</em> of the currently running MIDlet suite, the
     * platform MUST first stop the currently running MIDlet suite
     * before performing the update. On some platforms, the currently
     * running MIDlet suite MAY need to be stopped before any
     * installations can occur.</p>
     *
     * <p>If the URL specified is of the form
     * <code>tel:&lt;number&gt;</code>, as specified in <a
     * href="http://rfc.net/rfc2806.html">RFC2806</a>, then the
     * platform MUST interpret this as a request to initiate a voice
     * call. The request MUST be passed to the &quot;phone&quot;
     * application to handle if one is present in the platform.</p>
     *
     * <p>Devices MAY choose to support additional URL schemes beyond
     * the requirements listed above.</p>
     *
     * <p>Many of the ways this method will be used could have a
     * financial impact to the user (e.g. transferring data through a
     * wireless network, or initiating a voice call). Therefore the
     * platform MUST ask the user to explicitly acknowledge each
     * request before the action is taken. Implementation freedoms are
     * possible so that a pleasant user experience is retained. For
     * example, some platforms may put up a dialog for each request
     * asking the user for permission, while other platforms may
     * launch the appropriate application and populate the URL or
     * phone number fields, but not take the action until the user
     * explicitly clicks the load or dial buttons.</p>
     *
     * @return true if the MIDlet suite MUST first exit before the
     * content can be fetched.
     *
     * @param URL The URL for the platform to load.
     *
     * @exception ConnectionNotFoundException if
     * the platform cannot handle the URL requested.
     *
     */
    public boolean dispatch(String URL)
            throws ConnectionNotFoundException {
        if ("".equals(URL)) {
            if (Configuration.getIntProperty(
                    "useJavaInstallerForPlaformRequest", 0) != 0) {
                /*
                 * This is request to try to cancel the last request.
                 *
                 * If the next MIDlet to run is the installer then it can be
                 * cancelled.
                 */
                if (INSTALLER_CLASS.equals(
                    MIDletSuiteUtils.getNextMIDletToRun())) {
                    /*
                     * Try to cancel the installer midlet. Note this call only
                     * works now because suite are not run concurrently and
                     * must be queued to be run after this MIDlet is
                     * destroyed.
                     * This cancel code can be remove when the installer is
                     * runs concurrently with this suite.
                     */
                    MIDletSuiteUtils.execute(securityToken,
                        MIDletSuite.UNUSED_SUITE_ID, null, null);
                    return false;
                }
            }

            /*
             * Give the platform a chance to cancel the request.
             * Note: if the application was launched already this will
             * not have any effect.
             */
            dispatchPlatformRequest("");
            return false;
        }

        /*
         * Remove this "if", when not using the Installer MIDlet,
         * or the native installer will not be launched.
         */
        if (Configuration.getIntProperty(
                "useJavaInstallerForPlaformRequest", 0) != 0) {
            if (isMidletSuiteUrl(URL)) {
                return dispatchMidletSuiteUrl(URL);
            }
        }

        return dispatchPlatformRequest(URL);
    }

    /**
     * Find out if the given URL is a JAD or JAR HTTP URL by performing a
     * HTTP head request and checking the MIME type.
     *
     * @param url The URL for to check
     *
     * @return true if the URL points to a MIDlet suite
     */

    private boolean isMidletSuiteUrl(String url) {
        Connection conn = null;
        HttpConnection httpConnection = null;
        String profile;
        int space;
        String configuration;
        String locale;
        int responseCode;
        String mediaType;

        try {
            conn = Connector.open(url, Connector.READ);
        } catch (IllegalArgumentException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

        try {
            if (!(conn instanceof HttpConnection)) {
                // only HTTP or HTTPS are supported
                return false;
            }

            httpConnection = (HttpConnection)conn;

            httpConnection.setRequestMethod(HttpConnection.HEAD);

            httpConnection.setRequestProperty("Accept", "*/*");

            profile = System.getProperty("microedition.profiles");
            space = profile.indexOf(' ');
            if (space != -1) {
                profile = profile.substring(0, space);
            }

            configuration = System.getProperty("microedition.configuration");
            httpConnection.setRequestProperty("User-Agent",
                "Profile/" + profile + " Configuration/" + configuration);

            httpConnection.setRequestProperty("Accept-Charset",
                                              "UTF-8, ISO-8859-1");

            /* locale can be null */
            locale = System.getProperty("microedition.locale");
            if (locale != null) {
                httpConnection.setRequestProperty("Accept-Language", locale);
            }

            responseCode = httpConnection.getResponseCode();

            if (responseCode != HttpConnection.HTTP_OK) {
                return false;
            }

            mediaType = Util.getHttpMediaType(httpConnection.getType());
            if (mediaType == null) {
                return false;
            }

            if (mediaType.equals(JAD_MT) || mediaType.equals(JAR_MT_1) ||
                    mediaType.equals(JAR_MT_2)) {
                return true;
            }

            return false;
        } catch (IOException ioe) {
            return false;
        } finally {
            try {
                conn.close();
            } catch (Exception e) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                                  "Exception while closing  connection");
                }
            }
        }
    }

    /**
     * Dispatches the a JAD or JAD HTTP URL to the Graphical Installer.
     *
     * @param url The URL to dispatch
     *
     * @return true if the MIDlet suite MUST first exit before the
     * content can be fetched.
     */
    private boolean dispatchMidletSuiteUrl(String url) {
        return MIDletSuiteUtils.executeWithArgs(securityToken,
            MIDletSuite.INTERNAL_SUITE_ID, INSTALLER_CLASS,
                "MIDlet Suite Installer", "PR", url, null);
    }

    /**
     * Passes the URL to the native handler.
     *
     * @param url The URL for the platform to load.
     *
     * @return true if the MIDlet suite MUST first exit before the
     * content can be fetched.
     *
     * @exception ConnectionNotFoundException if
     * the platform cannot handle the URL requested.
     */
    public native final boolean dispatchPlatformRequest(String url) throws
        ConnectionNotFoundException;
}
