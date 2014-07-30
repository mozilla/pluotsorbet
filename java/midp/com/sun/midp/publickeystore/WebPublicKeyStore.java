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

package com.sun.midp.publickeystore;

// Explicit list of declarations to avoid CDC conflict
// with use of java.io.File
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.*;

import javax.microedition.io.*;

import com.sun.j2me.security.AccessController;

import com.sun.midp.io.j2me.storage.*;
import com.sun.midp.security.*;
import com.sun.midp.configurator.Constants;
import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;
import com.sun.midp.pki.*;
import com.sun.midp.security.SecurityToken;

/**
 * A public keystore that can used with SSL.
 * To work with SSL this class implements the SSL
 * {@link CertStore} interface.
 */
public class WebPublicKeyStore extends PublicKeyStore
    implements CertStore {

    /**
     * Inner class to request security token from SecurityInitializer.
     * SecurityInitializer should be able to check this inner class name.
     */
    static private class SecurityTrusted
        implements ImplicitlyTrustedClass {};

    /** This class has a different security domain than the MIDlet suite */
    private static SecurityToken classSecurityToken =
        SecurityInitializer.requestToken(new SecurityTrusted());

    /** keystore this package uses for verifying descriptors */
    private static WebPublicKeyStore trustedKeyStore;

    /** keystore this package uses for verifying descriptors */
    private static Vector sharedKeyList;

    /**
     * Load the certificate authorities for the MIDP from storage
     * into the SSL keystore.
     */
    public static void loadCertificateAuthorities() {
        RandomAccessStream storage;
        InputStream tks;
        WebPublicKeyStore ks;

        if (trustedKeyStore != null) {
            return;
        }

        try {
            storage = new RandomAccessStream(classSecurityToken);
            if (keystoreLocation != null ) {
                storage.connect(keystoreLocation, Connector.READ);
            } else {
                storage.connect(File.getStorageRoot(Constants.INTERNAL_STORAGE_ID) +
                "_main.ks", Connector.READ);
            }
            
            tks = storage.openInputStream();
        } catch (Exception e) {
            if (Logging.TRACE_ENABLED) {
                Logging.trace(e, "Could not open the trusted key store, " +
                              "cannot authenticate HTTPS servers");
            }
            return;
        }

        try {
            sharedKeyList = new Vector();
            ks = new WebPublicKeyStore(tks, sharedKeyList);
        } catch (Exception e) {
            if (Logging.TRACE_ENABLED) {
                Logging.trace(e, "Corrupt key store file, cannot" +
                              "authenticate HTTPS servers");
            }
            return;
        } finally {
            try {
                storage.disconnect();
            } catch (Exception e) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_SECURITY,
                                   "Exception during diconnect");
                }
            }
        }

        WebPublicKeyStore.setTrustedKeyStore(ks);
    }

    /**
     * Disable a certificate authority in the trusted keystore.
     *
     * @param name name of the authority.
     */
    public static void disableCertAuthority(String name) {
        setCertAuthorityEnabledField(name, false);
    }

    /**
     * Enable a certificate authority in the trusted keystore.
     *
     * @param name name of the authority.
     */
    public static void enableCertAuthority(String name) {
        setCertAuthorityEnabledField(name, true);
    }

    /**
     * Disable a certificate authority in the trusted keystore.
     * <p>
     * Method requires com.sun.midp.ams permission.
     *
     * @param name name of the authority.
     * @param enabled value of enable field
     */
    private static void setCertAuthorityEnabledField(String name,
            boolean enabled) {
        Vector keys;
        PublicKeyInfo keyInfo;

        AccessController.checkPermission(Permissions.AMS_PERMISSION_NAME);

        keys = trustedKeyStore.findKeys(name);
        if (keys == null || keys.size() <= 0) {
            return;
        }

        for (int i = 0; i < keys.size(); i++) {
            keyInfo = (PublicKeyInfo)keys.elementAt(i);
            keyInfo.enabled = enabled;
        }

        saveKeyList();
    }

    /** Saves the shared key list to main key store. */
    private static void saveKeyList() {
        PublicKeyStoreBuilderBase keystore;
        RandomAccessStream storage;
        OutputStream outputStream;

        if (trustedKeyStore == null) {
            return;
        }

        keystore = new PublicKeyStoreBuilderBase(sharedKeyList);
        try {
            storage = new RandomAccessStream(classSecurityToken);
            if (keystoreLocation != null ) {
                storage.connect(keystoreLocation, RandomAccessStream.READ_WRITE_TRUNCATE);
            } else {
                storage.connect(File.getStorageRoot(Constants.INTERNAL_STORAGE_ID) +
                "_main.ks", RandomAccessStream.READ_WRITE_TRUNCATE);
            }
            
            outputStream = storage.openOutputStream();
        } catch (Exception e) {
            if (Logging.TRACE_ENABLED) {
                Logging.trace(e, "Could not open the trusted key store, " +
                              "cannot authenticate HTTPS servers");
            }
            return;
        }

        try {
            keystore.serialize(outputStream);
        } catch (Exception e) {
            if (Logging.TRACE_ENABLED) {
                Logging.trace(e, "Corrupt key store file, cannot" +
                              "authenticate HTTPS servers");
            }

            return;
        } finally {
            try {
                storage.disconnect();
            } catch (Exception e) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_SECURITY,
                                   "Exception during diconnect");
                }
            }
        }
    }


    /**
     * Establish the given keystore as the system trusted keystore.
     * This is a one-shot method, it will only set the trusted keystore
     * it there is no keystore set. For security purposes only
     * read-only PublicKeyStores should be set.
     * @param keyStore keystore to be the system trusted keystore
     * @see #getTrustedKeyStore
     */
    private static void setTrustedKeyStore(WebPublicKeyStore keyStore) {
        if (trustedKeyStore != null) {
            return;
        }

        trustedKeyStore = keyStore;
    }

    /**
     * Provides the keystore of resident public keys for
     * security domain owners and other CA's. Loads the public key store if
     * it has not already been loaded.
     *
     * @return keystore of domain owner and CA keys
     * @see #setTrustedKeyStore
     */
    public static WebPublicKeyStore getTrustedKeyStore() {
        if (trustedKeyStore == null) {
            loadCertificateAuthorities();
        }

        return trustedKeyStore;
    }

    /**
     * Constructs an keystore to initialize the class security token.
     */
    public WebPublicKeyStore() {
    }

    /**
     * Constructs an extendable keystore from a serialized keystore created
     * by {@link PublicKeyStoreBuilder}.
     * @param in stream to read a keystore serialized by
     *        {@link PublicKeyStoreBuilder#serialize(OutputStream)} from
     * @exception IOException if the key storage was corrupted
     */
    public WebPublicKeyStore(InputStream in) throws IOException {
        super(in);
    }

    /**
     * Constructs an extendable keystore from a serialized keystore created
     * by {@link PublicKeyStoreBuilder}.
     * @param in stream to read a keystore serialized by
     *        {@link PublicKeyStoreBuilder#serialize(OutputStream)} from
     * @param sharedKeyList shared key list
     * @exception IOException if the key storage was corrupted
     */
    public WebPublicKeyStore(InputStream in, Vector sharedKeyList)
        throws IOException {
        super(in, sharedKeyList);
    }

    /**
     * Returns the certificate(s) corresponding to a
     * subject name string.
     *
     * @param subjectName subject name of the certificate in printable form.
     *
     * @return corresponding certificates or null (if not found)
     */
    public X509Certificate[] getCertificates(String subjectName) {
        Vector keys;
        X509Certificate[] certs;

        keys = findKeys(subjectName);
        if (keys == null) {
            return null;
        }

        certs = new X509Certificate[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            certs[i] = createCertificate((PublicKeyInfo)keys.elementAt(i));
        }

        return certs;
    }

    /**
     * Creates an {@link X509Certificate} using the given public key
     * information.
     * @param keyInfo key information
     * @return X509 certificate
     */
    public static X509Certificate createCertificate(PublicKeyInfo keyInfo) {
        if (keyInfo == null) {
            return null;
        }

        try {
            X509Certificate cert;

            cert = new X509Certificate((byte)0, // fixed at version 1 (raw 0)
                                new byte[0],
                                keyInfo.getOwner(),
                                keyInfo.getOwner(), // issuer same as subject
                                keyInfo.getNotBefore(),
                                keyInfo.getNotAfter(),
                                keyInfo.getModulus(),
                                keyInfo.getExponent(),
                                null, // we don't use finger prints
                                0);
            return cert;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Sets the location of the keystore
     *
     * @param token security token of the caller
     * @param location Name of file containing the keystore (Full path)
     *
     * @exception SecurityException if the caller does not have the
     *   MIDP permission.
     */

    public static void initKeystoreLocation(SecurityToken securityToken,String location) {

        securityToken.checkIfPermissionAllowed(Permissions.MIDP);

        if (keystoreLocation == null) {
            keystoreLocation = location;
        }
    }

    private static String keystoreLocation;

}
