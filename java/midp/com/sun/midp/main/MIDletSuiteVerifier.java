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

import com.sun.midp.i18n.ResourceConstants;
import com.sun.midp.i18n.Resource;
import com.sun.midp.midletsuite.MIDletSuiteStorage;
import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;
import com.sun.midp.configurator.Constants;

import java.io.IOException;

/**
 * This is a stub class at present.
 * The implementation of "verify once" optimization
 * for SVM will be provided later.
 */

public class MIDletSuiteVerifier {
    /**
     * Verify classes within JAR
     * @param jarPath path to the JAR package within file system
     * @return always false, since verification is not passes but
     *   only scheduled to be done as soon as the current MIDlet
     *   will be terminated
     */
    static boolean verifyJar(String jarPath) {
        return false;
    }

    /**
     * Schedule suite classes verification to be done by a new VM started
     * as soon as the current VM will be terminated.
     *
     * @param suiteId id of the suite whose classes are to be verified
     * @param suiteStorage suite storage instance
     * @return null verify hash value, since no verification is done,
     *   the verification is only scheduled to be done in the future
     * @throws IOException
     */
    public static byte[] verifySuiteClasses(int suiteId,
        MIDletSuiteStorage suiteStorage) throws IOException {

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_AMS,
            "Queue start of the suite verifier MIDlet");
        }

        String jarPath = suiteStorage.getMidletSuiteJarPath(suiteId);
        MIDletSuiteUtils.executeWithArgs(
            MIDletSuite.INTERNAL_SUITE_ID,
            Constants.SUITE_VERIFIER_MIDLET,
            Resource.getString(
                ResourceConstants.CLASS_VERIFIER_APPLICATION),
            String.valueOf(suiteId), jarPath, null
        );

        return null;
    }

    /**
     * Evaluate hash value for the JAR  package
     *
     * @param jarPath JAR package path
     * @return hash value for JAR package
     */
    public native static byte[] getJarHash(String jarPath)
        throws IOException;

    /**
     * Disable or enable class verifier for the current VM
     * @param verifier true to enable, false to disable verifier
     */
    static native void useClassVerifier(boolean verifier);

    /**
     * Compare hash value of the JAR with provided hash value.
     *
     * @param jarPath path to JAR file
     * @param hashValue hash value to compare with
     * @return true if JAR has hash value equal to the provided one,
     *   otherwise false
     * @throws IOException
     */
    public native static boolean checkJarHash(String jarPath,
            byte[] hashValue) throws IOException;
}
