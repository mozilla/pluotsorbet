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

import java.io.IOException;
import com.sun.midp.midletsuite.MIDletSuiteLockedException;

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

import com.sun.midp.midlet.MIDletSuite;

import com.sun.midp.midletsuite.MIDletInfo;
import com.sun.midp.midletsuite.MIDletSuiteStorage;

import com.sun.midp.security.Permissions;

/**
 * This class and its subclasses implement actual auto testing
 * functionality. We need subclasses because there is a difference 
 * between how auto testing works in SVM and MVM modes. 
 * This difference is expressed in how abstract methods of 
 * AutoTesterHelperBase are implemented in its subclasses.
 */
abstract class AutoTesterHelperBase {
    /** MIDlet suite storage object. */
    protected MIDletSuiteStorage midletSuiteStorage;

    /** The installer. */
    protected Installer installer;

    /** URL of the test suite. */
    protected String url;

    /** Security domain to assign to unsigned suites. */
    protected String domain;

    /** How many iterations to run the suite */
    protected int loopCount = -1;

    /**
     * Constructor.
     *
     * @param theURL URL of the test suite
     * @param theDomain security domain to assign to unsigned suites
     * @param theLoopCount how many iterations to run the suite
     */
    protected AutoTesterHelperBase(String theURL, String theDomain, 
            int theLoopCount) {

        midletSuiteStorage = MIDletSuiteStorage.getMIDletSuiteStorage();
        installer = new HttpInstaller();
        // suites will be installed as temporary
        installer.info.temporary = true;

        url = theURL;
        domain = theDomain;
        loopCount = theLoopCount; 

        setDomain();
    }

    /**
     * Installs and performs the tests. 
     */    
    abstract void installAndPerformTests() 
        throws Exception;

    /**
     * Returns the class name of the first MIDlet of the newly installed
     * suite.
     *
     * @param suiteId ID of the MIDlet Suite
     *
     * @return an object with the class name and display name of
     * the suite's MIDlet-1 property
     */
    protected MIDletInfo getFirstMIDletOfSuite(int suiteId) {
        MIDletSuite ms = null;
        String name = null;

        try {
            ms = midletSuiteStorage.getMIDletSuite(suiteId, false);
            name = ms.getProperty("MIDlet-1");
        } catch (Exception e) {
            throw new RuntimeException("midlet properties corrupted");
        } finally {
            if (ms != null) {
                ms.close();
            }
        }

        if (name == null) {
            throw new RuntimeException("MIDlet-1 missing");
        }

        // found the entry now parse out the class name, and display name
        return new MIDletInfo(name);
    }

    /**
     * Returns the associated message for the given installer exception.
     *
     * @param suiteId ID of the MIDlet Suite that caused exception
     * @param ex exception instance
     * @return associated message for given installer exception,
     * null if this exception should be ignored
     */
    protected static String getInstallerExceptionMessage(int suiteId, 
            Throwable ex) {

        String message = null;

        if (ex instanceof InvalidJadException) {
            InvalidJadException ije = (InvalidJadException)ex;

            /*
             * The server will signal the end of testing with not found
             * status. However print out the JAD not found error if this
             * is the first download. (suiteId == unused)
             */
            int reason = ije.getReason();
            if ((reason != InvalidJadException.JAD_NOT_FOUND &&
                reason != InvalidJadException.JAD_SERVER_NOT_FOUND) ||
                    suiteId == MIDletSuite.UNUSED_SUITE_ID) {
                message = "** Error installing suite (" + reason + "): " + 
                    messageForInvalidJadException(ije);
            }
        } else if (ex instanceof IOException) {
            message = "** I/O Error installing suite: " + ex.getMessage();
        } else {
            message = "** Error installing suite: " + ex.toString();
        }

        return message;
    }
    
    /**
     * Sets security domain.
     */
    private void setDomain() {
        if (domain != null) {
            String additionalPermissions = null;
            int index = domain.indexOf(":");
            int len = domain.length();

            if (index > 0 && index + 1 < len) {
                additionalPermissions = domain.substring(index + 1, len);
                domain = domain.substring(0, index);
            }

            if (domain.length() > 0) {
                installer.setUnsignedSecurityDomain(domain);
            }

            installer.setExtraPermissions(additionalPermissions);
        }
    }
    
    /**
     * Returns the associated message for the given exception.
     * This function is here instead of in the exception its self because
     * it not need on devices, it needed only on development platforms that
     * have command line interface.
     *
     * @param ije reason reason code for the exception
     *
     * @return associated message for the given reason
     */
    private static String messageForInvalidJadException(
            InvalidJadException ije) {

        switch (ije.getReason()) {
        case InvalidJadException.MISSING_PROVIDER_CERT:
        case InvalidJadException.MISSING_SUITE_NAME:
        case InvalidJadException.MISSING_VENDOR:
        case InvalidJadException.MISSING_VERSION:
        case InvalidJadException.MISSING_JAR_URL:
        case InvalidJadException.MISSING_JAR_SIZE:
        case InvalidJadException.MISSING_CONFIGURATION:
        case InvalidJadException.MISSING_PROFILE:
            return "A required attribute is missing";

        case InvalidJadException.SUITE_NAME_MISMATCH:
        case InvalidJadException.VERSION_MISMATCH:
        case InvalidJadException.VENDOR_MISMATCH:
            return "A required suite ID attribute in the JAR manifest " +
                "do not match the one in the JAD";

        case InvalidJadException.ATTRIBUTE_MISMATCH:
            return "The value for " + ije.getExtraData() + " in the " +
                "trusted JAR manifest did not match the one in the JAD";

        case InvalidJadException.CORRUPT_PROVIDER_CERT:
            return "The content provider certificate cannot be decoded.";

        case InvalidJadException.UNKNOWN_CA:
            return "The content provider certificate issuer " +
                ije.getExtraData() + " is unknown.";

        case InvalidJadException.INVALID_PROVIDER_CERT:
            return "The signature of the content provider certificate " +
                "is invalid.";

        case InvalidJadException.CORRUPT_SIGNATURE:
            return "The JAR signature cannot be decoded.";

        case InvalidJadException.INVALID_SIGNATURE:
            return "The signature of the JAR is invalid.";

        case InvalidJadException.UNSUPPORTED_CERT:
            return "The content provider certificate is not a supported " +
                "version.";

        case InvalidJadException.EXPIRED_PROVIDER_CERT:
            return "The content provider certificate is expired.";

        case InvalidJadException.EXPIRED_CA_KEY:
            return "The public key of " + ije.getExtraData() +
                " has expired.";

        case InvalidJadException.JAR_SIZE_MISMATCH:
            return "The Jar downloaded was not the size in the JAD";

        case InvalidJadException.OLD_VERSION:
            return "The application is an older version of one that is " +
                "already installed";

        case InvalidJadException.NEW_VERSION:
            return "The application is an newer version of one that is " +
                "already installed";

        case InvalidJadException.INVALID_JAD_URL:
            return "The JAD URL is invalid";

        case InvalidJadException.JAD_SERVER_NOT_FOUND:
            return "JAD server not found";

        case InvalidJadException.JAD_NOT_FOUND:
            return "JAD not found";

        case InvalidJadException.INVALID_JAR_URL:
            return "The JAR URL in the JAD is invalid: " +
                ije.getExtraData();

        case InvalidJadException.JAR_SERVER_NOT_FOUND:
            return "JAR server not found: " + ije.getExtraData();

        case InvalidJadException.JAR_NOT_FOUND:
            return "JAR not found: " + ije.getExtraData();

        case InvalidJadException.CORRUPT_JAR:
            return "Corrupt JAR, error while reading: " +
                ije.getExtraData();

        case InvalidJadException.INVALID_JAR_TYPE:
            if (ije.getExtraData() != null) {
                return "JAR did not have the correct media type, it had " +
                    ije.getExtraData();
            }

            return "The server did not have a resource with an " +
                "acceptable media type for the JAR URL. (code 406)";

        case InvalidJadException.INVALID_JAD_TYPE:
            if (ije.getExtraData() != null) {
                String temp = ije.getExtraData();

                if (temp.length() == 0) {
                    return "JAD did not have a media type";
                }

                return "JAD did not have the correct media type, it had " +
                    temp;
            }

            /*
             * Should not happen, the accept field is not send
             * when getting the JAD.
             */
            return "The server did not have a resource with an " +
                "acceptable media type for the JAD URL. (code 406)";

        case InvalidJadException.INVALID_KEY:
            return "The attribute key [" + ije.getExtraData() +
                "] is not in the proper format";

        case InvalidJadException.INVALID_VALUE:
            return "The value for attribute " + ije.getExtraData() +
                " is not in the proper format";

        case InvalidJadException.INSUFFICIENT_STORAGE:
            return "There is insufficient storage to install this suite";

        case InvalidJadException.UNAUTHORIZED:
            return "Authentication required or failed";

        case InvalidJadException.JAD_MOVED:
            return "The JAD to be installed is for an existing suite, " +
                "but not from the same domain as the existing one: " +
                ije.getExtraData();

        case InvalidJadException.CANNOT_AUTH:
            return
                "Cannot authenticate with the server, unsupported scheme";

        case InvalidJadException.DEVICE_INCOMPATIBLE:
            return "Either the configuration or profile is not supported.";

        case InvalidJadException.ALREADY_INSTALLED:
            return
                "The JAD matches a version of a suite already installed.";

        case InvalidJadException.AUTHORIZATION_FAILURE:
            return "The suite is not authorized for " + ije.getExtraData();

        case InvalidJadException.PUSH_DUP_FAILURE:
            return "The suite is in conflict with another application " +
                "listening for network data on " + ije.getExtraData();

        case InvalidJadException.PUSH_FORMAT_FAILURE:
            return "Push attribute in incorrectly formated: " +
                ije.getExtraData();

        case InvalidJadException.PUSH_PROTO_FAILURE:
            return "Connection in push attribute is not supported: " +
                ije.getExtraData();

        case InvalidJadException.PUSH_CLASS_FAILURE:
            return "The class in push attribute not in a MIDlet-<n> " +
                "attribute: " + ije.getExtraData();

        case InvalidJadException.TRUSTED_OVERWRITE_FAILURE:
            return "Cannot update a trusted suite with an untrusted " +
                "version";

        case InvalidJadException.INVALID_CONTENT_HANDLER:
            return "Content handler attribute(s) incorrectly formatted: " +
                ije.getExtraData();

        case InvalidJadException.CONTENT_HANDLER_CONFLICT:
            return "Content handler would conflict with another handler: " +
                ije.getExtraData();

        case InvalidJadException.CA_DISABLED:
            return "The application can't be authorized because " +
                ije.getExtraData() + " is disabled.";

        case InvalidJadException.UNSUPPORTED_CHAR_ENCODING:
            return "Unsupported character encoding: " + ije.getExtraData();

        case InvalidJadException.REVOKED_CERT:
            return "The content provider certificate has been revoked.";

        case InvalidJadException.UNKNOWN_CERT_STATUS:
            return "The content provider certificate status is unknown.";
        }

        return ije.getMessage();
    }
}
