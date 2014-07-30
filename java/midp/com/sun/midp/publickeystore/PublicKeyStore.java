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
 * A read-only public keystore for use with MIDP.
 */
public class PublicKeyStore {

    /** Holds the all the keys as {@link PublicKeyInfo} objects. */
    private Vector keyList = null;

    /**
     * Constructor for subclasses.
     */
    protected PublicKeyStore() {
    }

    /**
     * Constructs a read-only keystore from a serialized keystore created
     * by {@link PublicKeyStoreBuilder}.
     * @param in stream to read a keystore serialized by
     *        {@link PublicKeyStoreBuilder#serialize(OutputStream)} from
     * @exception IOException if the key storage was corrupted
     */
    public PublicKeyStore(InputStream in) throws IOException {
        initPublicKeyStore(in, new Vector());
    }

    /**
     * Constructs a read-only keystore from a serialized keystore created
     * by {@link PublicKeyStoreBuilder}.
     * @param in stream to read a keystore serialized by
     *        {@link PublicKeyStoreBuilder#serialize(OutputStream)} from
     * @param sharedKeyList key list of a subclass
     * @exception IOException if the key storage was corrupted
     */
    public PublicKeyStore(InputStream in, Vector sharedKeyList)
            throws IOException {
        initPublicKeyStore(in, sharedKeyList);
    }

    /**
     * Lets this class work with a writeable key list of a subclass.
     * This is needed because we cannot make the key list in this
     * class protected for security reasons. This method will only
     * work if the PublicKeyStore has not been initialized.
     * @param sharedKeyList key list of a subclass
     */
    protected void initPublicKeyStore(Vector sharedKeyList) {
        if (keyList != null) {
            return;
        }

        keyList = sharedKeyList;
    }

    /**
     * Lets this class work with a writeable key list of a subclass and
     * initialized that key list from a serialized key list.
     * This is needed because we cannot make the key list in this
     * class protected for security reasons. This method will only
     * work if the PublicKeyStore has not been initialized. 
     * @param sharedKeyList key list of a subclass
     * @param in stream to read the serialized keystore
     * @exception IOException if the key storage was corrupted
     */
    protected void initPublicKeyStore(InputStream in, Vector sharedKeyList)
            throws IOException {
        InputStorage storage = new InputStorage(in);
        PublicKeyInfo keyInfo;

        if (keyList != null) {
            return;
        }

        keyList = sharedKeyList;
        for (;;) {
            keyInfo = PublicKeyInfo.getKeyFromStorage(storage);
            if (keyInfo == null)
                return;
            
            keyList.addElement(keyInfo);
        }
    }

    /**
     * Gets a by number from the keystore. 0 is the first key.
     *
     * @param number number of key
     *
     * @return public key information of the key
     *
     * @exception  ArrayIndexOutOfBoundsException  if an invalid number was
     *             given.
     */
    public synchronized PublicKeyInfo getKey(int number) {
        return (PublicKeyInfo)keyList.elementAt(number);
    }

    /**
     * Finds a CAs Public keys based on the distinguished name.
     *
     * @param owner distinguished name of keys' owner
     * @return public key information of the keys
     */
    public synchronized Vector findKeys(String owner) {
        PublicKeyInfo keyInfo;
        Vector keys = null;
  
        for (int i = 0; i < keyList.size(); i++) {
            keyInfo = (PublicKeyInfo)keyList.elementAt(i);
	        if (keyInfo.getOwner().compareTo(owner) == 0) {
                if (keys == null) {
                    keys = new Vector();
                }

                keys.addElement(keyInfo);
            }                               
        }

        return keys;
    }

    /**
     * Returns all CAs Public keys.
     *
     * @return public key information of the keys
     */
    public synchronized Vector getKeys() {
        Object keyInfo;
        Vector keys;

        keys = new Vector(keyList.size());

        for (int i = 0; i < keyList.size(); i++) {
            keyInfo = keyList.elementAt(i);
            keys.addElement(keyInfo);
        }

        return keys;
    }

    /**
     * Gets the number of keys in the store.
     * @return number of keys in the keystore
     */
    public synchronized int numberOfKeys() {
        return keyList.size();
    }
}
