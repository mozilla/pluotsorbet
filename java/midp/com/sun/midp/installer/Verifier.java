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
import com.sun.midp.io.j2me.storage.RandomAccessStream;

/*
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;

import java.lang.String;
import java.lang.IllegalArgumentException;

import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.Connection;
import javax.microedition.io.HttpConnection;

import javax.microedition.pki.CertificateException;

import com.sun.midp.crypto.*;

import com.sun.midp.pki.*;

import com.sun.midp.io.*;

import com.sun.midp.io.j2me.storage.*;

import com.sun.midp.publickeystore.*;

import com.sun.midp.midletsuite.*;

import com.sun.midp.security.*;
*/

/**
 * Interface that must be implemented by any class that is used to verify
 * a signature of the midlet suite.
 */
public interface Verifier {
    /**
     * MIDlet property for the application signature
     */
    public static final String SIG_PROP = "MIDlet-Jar-RSA-SHA1";

    /**
     * MIDlet property for the content provider certificates
     */
    public static final String CERT_PROP = "MIDlet-Certificate-";

    /**
     * Checks to see if the JAD has a signature, but does not verify the
     * signature.
     *
     * @return true if the JAD has a signature
     */
    public boolean isJadSigned();

    /**
     * Gets the security domain name for this MIDlet Suite from storage.
     *
     * @param ca CA of an installed suite
     *
     * @return name of the security domain for the MIDlet Suite
     */
    public String getSecurityDomainName(String ca);

    /**
     * Verifies a Jar. On success set the name of the domain owner in the
     * install state. Post any error back to the server.
     *
     * @param jarStorage System store for applications
     * @param jarFilename name of the jar to read.
     *
     * @return authorization path: a list of authority names begining with
     *         the most trusted, or null if jar is not signed
     *
     * @exception IOException if any error prevents the reading
     *   of the JAR
     * @exception InvalidJadException if the JAR is not valid or the
     *   provider certificate is missing
     */
    public String[] verifyJar(RandomAccessStream jarStorage,
        String jarFilename) throws IOException, InvalidJadException;

    /**
     * Enables or disables certificate revocation checking using OCSP.
     *
     * @param enable true to enable OCSP checking, false - to disable it
     */
    public void enableOCSPCheck(boolean enable);

    /**
     * Returns true if OCSP certificate revocation checking is enabled,
     * false if it is disabled.
     *
     * @return true if OCSP checking is enabled, false otherwise
     */
    public boolean isOCSPCheckEnabled();
}
