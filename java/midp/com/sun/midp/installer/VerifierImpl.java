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

import com.sun.midp.security.Permissions;
import com.sun.midp.io.j2me.storage.RandomAccessStream;

/**
 * Verifier to use when the crypto code is absent.
 * Doesn't do the real verification of suite's signature.
 */
public class VerifierImpl implements Verifier {
    /**
     * Constructor.
     *
     * @param installState midlet suite to verify
     */
    public VerifierImpl(InstallState installState) {
    }

    /**
     * Checks to see if the JAD has a signature, but does not verify the
     * signature. This is a place holder the the Secure Installer and
     * just returns false.
     *
     * @return true if the JAD has a signature
     */
    public boolean isJadSigned() {
        return false;
    }

    /**
     * Gets the security domain name for this MIDlet Suite from storage.
     *
     * @param ca CA of an installed suite
     *
     * @return name of the security domain for the MIDlet Suite
     */
    public String getSecurityDomainName(String ca) {
        return Permissions.UNIDENTIFIED_DOMAIN_BINDING;
    }

    /**
     * Verifies a Jar. Post any error back to the server.
     *
     * @param jarStorage System store for applications
     * @param jarFilename name of the jar to read
     *
     * @exception IOException if any error prevents the reading
     *   of the JAR
     * @exception InvalidJadException if the JAR is not valid
     */
    public String[] verifyJar(RandomAccessStream jarStorage,
            String jarFilename) throws IOException, InvalidJadException {
        return null;
    }

    /**
     * Enables or disables certificate revocation checking using OCSP.
     *
     * @param enable true to enable OCSP checking, false - to disable it
     */
    public void enableOCSPCheck(boolean enable) {
        // OCSP is always disabled when Crypto is not included
    }

    /**
     * Returns true if OCSP certificate revocation checking is enabled,
     * false if it is disabled.
     *
     * @return true if OCSP checking is enabled, false otherwise
     */
    public boolean isOCSPCheckEnabled() {
        // OCSP is always disabled when Crypto is not included
        return false;
    }
}
