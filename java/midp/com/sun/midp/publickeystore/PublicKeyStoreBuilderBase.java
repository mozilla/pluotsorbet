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

import java.io.*;
import java.util.*;

/**
 * A read-write serializable {@link PublicKeyStore}. This class is the base
 * for {@link PublicKeyStoreBuilder}, and does not have any methods that
 * depend on any Microediton specific classes so it can be used alone in a
 * tool written with Standard Edition Java.
 */
public class PublicKeyStoreBuilderBase extends PublicKeyStore {
    /** Vecor of keys maintained as a list. */
    private Vector keyList = new Vector();

    /**
     * Constructs an empty read-write keystore.
     */
    public PublicKeyStoreBuilderBase() {
        initPublicKeyStore(keyList);
    };

    /**
     * Constructs a read-write keystore from a serialized keystore created
     * by this class.
     * @param in stream to read a keystore serialized by
     *        {@link #serialize(OutputStream)} from
     * @exception IOException if the key storage was corrupted
     */
    public PublicKeyStoreBuilderBase(InputStream in) throws IOException {
        initPublicKeyStore(in, keyList);
    }

    /**
     * Constructs an read-write keystore.
     *
     * @param sharedKeyList shared key list
     */
    public PublicKeyStoreBuilderBase(Vector sharedKeyList) {
        keyList = sharedKeyList;
        initPublicKeyStore(keyList);
    };

    /**
     * Serializes the keystore to the given stream.
     * @param out stream to serialize the keystore to
     * @exception IOException is thrown, if an I/O error occurs
     */
    public void serialize(OutputStream out) throws IOException {
        OutputStorage storage = new OutputStorage(out);
        Enumeration e;
        PublicKeyInfo keyInfo;
  
        e = keyList.elements();
        while (e.hasMoreElements()) {
            keyInfo = (PublicKeyInfo)e.nextElement();
            putKeyInStorage(storage, keyInfo);
        }
    }

    /**
     * Adds a public key.
     *
     * @param keyInfo the key to add
     */
    public synchronized void addKey(PublicKeyInfo keyInfo) {
        keyList.addElement(keyInfo);
    }

    /**
     * Updates all of an key's information except for the security domain.
     * information in the store.
     *
     * @param number key number of key 0 being the first
     * @param newKeyInfo new key information
     *
     * @exception  ArrayIndexOutOfBoundsException  if an invalid number was
     *             given.
     */
    public synchronized void updateKey(int number,
                                          PublicKeyInfo newKeyInfo) {
        PublicKeyInfo oldKeyInfo;

        oldKeyInfo = getKey(number);

        newKeyInfo.setDomain(oldKeyInfo.getDomain());

        keyList.setElementAt(newKeyInfo, number);
    }

    /**
     * Deletes a public key from this keystore by number.
     *
     * @param number number of the key with 0 being the first.
     *
     * @exception  ArrayIndexOutOfBoundsException  if an invalid number was
     *             given.
     */
    public void deleteKey(int number) {
        keyList.removeElementAt(number);
    }

    /**
     * Serializes every field with a tag.
     * @param storage what to put the key in
     * @param key key information object
     */
    private void putKeyInStorage(OutputStorage storage, PublicKeyInfo key) 
            throws java.io.IOException {
        storage.writeValue(PublicKeyInfo.OWNER_TAG, key.getOwner());
        storage.writeValue(PublicKeyInfo.NOT_BEFORE_TAG, key.getNotBefore());
        storage.writeValue(PublicKeyInfo.NOT_AFTER_TAG, key.getNotAfter());
        storage.writeValue(PublicKeyInfo.MODULUS_TAG, key.getModulus());
        storage.writeValue(PublicKeyInfo.EXPONENT_TAG, key.getExponent());
        storage.writeValue(PublicKeyInfo.DOMAIN_TAG, key.getDomain());
        storage.writeValue(PublicKeyInfo.ENABLED_TAG, key.isEnabled()?"enabled":"disabled");
    }
}
