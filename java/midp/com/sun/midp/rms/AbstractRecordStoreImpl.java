/*
 *
 *
 * Portions Copyright  2000-2009 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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
 *
 * Copyright 2000 Motorola, Inc. All Rights Reserved.
 * This notice does not imply publication.
 */

package com.sun.midp.rms;

import java.io.IOException;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotOpenException;

/**
 * An interface for a record store implementation.
 */

interface AbstractRecordStoreImpl {
    /*
     * The layout of the database file is as follows:
     *
     * Bytes - Usage
     * 00-07 - Signature = 'midp-rms'
     * 08-11 - Authmode and Writable state info
     * 12-15 - Next record ID to use (big endian)
     * 16-19 - Number of live records in the database (big endian)
     * 20-23 - Database "version" - a monotonically increasing revision
     *         number (big endian)
     * 24-31 - Last modified (64-bit long, big endian, milliseconds since
     *         jan 1970)
     * 32-35 - Size of Data storage (big endian)
     * 36-39 - Size of Free storage (big endian)
     * 40-xx - Record storage
     */

    /** RS_SIGNATURE offset */
    static final int RS0_SIGNATURE = 0;

    /** RS_AUTHMODE offset */
    static final int RS1_AUTHMODE = 8;

    /** RS_NEXT_ID offset */
    static final int RS2_NEXT_ID = 12;

    /** RS_NUM_LIVE offset */
    static final int RS3_NUM_LIVE = 16;

    /** RS_VERSION offset */
    static final int RS4_VERSION = 20;

    /** RS_LAST_MODIFIED offset */
    static final int RS5_LAST_MODIFIED = 24;

    /** RS_START_OF_DATA offset */
    static final int RS6_DATA_SIZE = 32;

    /** RS_START_OF_DATA offset */
    static final int RS7_FREE_SIZE = 36;

    /** Size of the db header */
    static final int DB_HEADER_SIZE = 40;

    /** pre initialized RecordStore header structure */
    static final byte[] DB_SIGNATURE = {
        (byte)'m', (byte)'i', (byte)'d', (byte)'p',
        (byte)'-', (byte)'r', (byte)'m', (byte)'s'
    };

    /** used to compact the records of the record store */
    static final int COMPACT_BUFFER_SIZE = 1024;

    /**
     *  Each block starts with an 8 byte header
     *    First 4 bytes is the recordId or -1 if it is a free block
     *    Second 4 bytes is the size of the data in the block
     *    The data immediately follows the header
     *    If the length of the data is not a multiple of 8 the data is padded
     *
     *  Things that need to be done by the header code
     *
     */
    /** Size of the block header */
    static final int BLOCK_HEADER_SIZE = 8;

    /**
     * Internal indicator for AUTHMODE_ANY with read only access
     * AUTHMODE_ANY_RO has a value of 2.
     */
    final static int AUTHMODE_ANY_RO = 2;

    /**
     * Changes the access mode for this RecordStore. The authorization
     * mode choices are:
     *
     * <ul>
     * <li><code>AUTHMODE_PRIVATE</code> - Only allows the MIDlet
     *          suite that created the RecordStore to access it. This
     *          case behaves identically to
     *          <code>openRecordStore(recordStoreName,
     *          createIfNecessary)</code>.</li>
     * <li><code>AUTHMODE_ANY</code> - Allows any MIDlet to access the
     *          RecordStore. Note that this makes your recordStore
     *          accessible by any other MIDlet on the device. This
     *          could have privacy and security issues depending on
     *          the data being shared. Please use carefully.</li>
     * </ul>
     *
     * <p>The owning MIDlet suite may always access the RecordStore and
     * always has access to write and update the store. Only the
     * owning MIDlet suite can change the mode of a RecordStore.</p>
     *
     * @param authmode the mode under which to check or create access.
     *          Must be one of AUTHMODE_PRIVATE or AUTHMODE_ANY.
     * @param writable true if the RecordStore is to be writable by
     *          other MIDlet suites that are granted access
     *
     * @exception RecordStoreException if a record store-related
     *          exception occurred
     * @exception SecurityException if this MIDlet Suite is not
     *          allowed to change the mode of the RecordStore
     * @exception IllegalArgumentException if authmode is invalid
     */
    void setMode(int authmode, boolean writable)
        throws RecordStoreException;

    /**
     * This method is called when the MIDlet requests to have the
     * record store closed. Note that the record store will not
     * actually be closed until closeRecordStore() is called as many
     * times as openRecordStore() was called. In other words, the
     * MIDlet needs to make a balanced number of close calls as open
     * calls before the record store is closed.
     *
     * <p>When the record store is closed, all listeners are removed
     * and all RecordEnumerations associated with it become invalid.
     * If the MIDlet attempts to perform
     * operations on the RecordStore object after it has been closed,
     * the methods will throw a RecordStoreNotOpenException.
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     * @exception RecordStoreException if a different record
     *          store-related exception occurred
     */
    void closeRecordStore()
        throws RecordStoreNotOpenException, RecordStoreException;

    /**
     * Get the authorization mode for this record store.
     *
     * @return authorization mode
     */
    int getAuthMode();

    /**
     * Each time a record store is modified (by
     * <code>addRecord</code>, <code>setRecord</code>, or
     * <code>deleteRecord</code> methods) its <em>version</em> is
     * incremented. This can be used by MIDlets to quickly tell if
     * anything has been modified.
     *
     * The initial version number is implementation dependent.
     * The increment is a positive integer greater than 0.
     * The version number increases only when the RecordStore is updated.
     *
     * The increment value need not be constant and may vary with each
     * update.
     *
     * @return the current record store version
     */
    int getVersion() throws RecordStoreNotOpenException;

    /**
     * Returns the number of records currently in the record store.
     *
     * @return the number of records currently in the record store
     */
    int getNumRecords();

    /**
     * Returns the amount of space, in bytes, that the record store
     * occupies. The size returned includes any overhead associated
     * with the implementation, such as the data structures
     * used to hold the state of the record store, etc.
     *
     * @return the size of the record store in bytes
     */
    int getSize();

    /**
     * Returns the amount of additional room (in bytes) available for
     * this record store to grow. Note that this is not necessarily
     * the amount of extra MIDlet-level data which can be stored,
     * as implementations may store additional data structures with
     * each record to support integration with native applications,
     * synchronization, etc.
     *
     * @return the amount of additional room (in bytes) available for
     *          this record store to grow
     */
    int getSizeAvailable();

    /**
     * Returns the last time the record store was modified, in the
     * format used by System.currentTimeMillis().
     *
     * @return the last time the record store was modified, in the
     *          format used by System.currentTimeMillis()
     */
    long getLastModified();

    /**
     * Returns the recordId of the next record to be added to the
     * record store. This can be useful for setting up pseudo-relational
     * relationships. That is, if you have two or more
     * record stores whose records need to refer to one another, you can
     * predetermine the recordIds of the records that will be created
     * in one record store, before populating the fields and allocating
     * the record in another record store. Note that the recordId returned
     * is only valid while the record store remains open and until a call
     * to <code>addRecord()</code>.
     *
     * @return the recordId of the next record to be added to the
     *          record store
     */
    int getNextRecordID();

    /**
     * Adds a new record to the record store. The recordId for this
     * new record is returned. This is a blocking atomic operation.
     * The record is written to persistent storage before the
     * method returns.
     *
     * @param data the data to be stored in this record. If the record
     *          is to have zero-length data (no data), this parameter may be
     *          null.
     * @param offset the index into the data buffer of the first
     *          relevant byte for this record
     * @param numBytes the number of bytes of the data buffer to use
     *          for this record (may be zero)
     *
     * @return the recordId for the new record
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     * @exception RecordStoreException if a different record
     *          store-related exception occurred
     * @exception RecordStoreFullException if the operation cannot be
     *          completed because the record store has no more room
     * @exception SecurityException if the MIDlet has read-only access
     *          to the RecordStore
     */
    int addRecord(byte[] data, int offset, int numBytes)
        throws RecordStoreNotOpenException, RecordStoreException,
               RecordStoreFullException;

    /**
     * The record is deleted from the record store. The recordId for
     * this record is NOT reused.
     *
     * @param recordId the ID of the record to delete
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     * @exception InvalidRecordIDException if the recordId is invalid
     * @exception RecordStoreException if a general record store
     *          exception occurs
     * @exception SecurityException if the MIDlet has read-only access
     *          to the RecordStore
     */
    void deleteRecord(int recordId)
        throws RecordStoreNotOpenException, InvalidRecordIDException,
               RecordStoreException;

    /**
     * Returns the size (in bytes) of the MIDlet data available
     * in the given record.
     *
     * @param recordId the ID of the record to use in this operation
     *
     * @return the size (in bytes) of the MIDlet data available
     *          in the given record
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     * @exception InvalidRecordIDException if the recordId is invalid
     * @exception RecordStoreException if a general record store
     *          exception occurs
     */
    int getRecordSize(int recordId)
        throws RecordStoreNotOpenException, InvalidRecordIDException,
               RecordStoreException;

    /**
     * Returns the data stored in the given record.
     *
     * @param recordId the ID of the record to use in this operation
     * @param buffer the byte array in which to copy the data
     * @param offset the index into the buffer in which to start copying
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     * @exception InvalidRecordIDException if the recordId is invalid
     * @exception RecordStoreException if a general record store
     *          exception occurs
     * @exception ArrayIndexOutOfBoundsException if the record is
     *          larger than the buffer supplied
     *
     * @return the number of bytes copied into the buffer, starting at
     *          index <code>offset</code>
     * @see #setRecord
     */
    int getRecord(int recordId, byte[] buffer, int offset)
        throws RecordStoreNotOpenException, InvalidRecordIDException,
               RecordStoreException;

    /**
     * Returns a copy of the data stored in the given record.
     *
     * @param recordId the ID of the record to use in this operation
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     * @exception InvalidRecordIDException if the recordId is invalid
     * @exception RecordStoreException if a general record store
     *          exception occurs
     *
     * @return the data stored in the given record. Note that if the
     *          record has no data, this method will return null.
     * @see #setRecord
     */
    byte[] getRecord(int recordId)
        throws RecordStoreNotOpenException, InvalidRecordIDException,
               RecordStoreException;

    /**
     * Sets the data in the given record to that passed in. After
     * this method returns, a call to <code>getRecord(int recordId)</code>
     * will return an array of numBytes size containing the data
     * supplied here.
     *
     * @param recordId the ID of the record to use in this operation
     * @param newData the new data to store in the record
     * @param offset the index into the data buffer of the first
     *          relevant byte for this record
     * @param numBytes the number of bytes of the data buffer to use
     *          for this record
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     * @exception InvalidRecordIDException if the recordId is invalid
     * @exception RecordStoreException if a general record store
     *          exception occurs
     * @exception RecordStoreFullException if the operation cannot be
     *          completed because the record store has no more room
     * @exception SecurityException if the MIDlet has read-only access
     *          to the RecordStore
     * @see #getRecord
     */
    void setRecord(int recordId, byte[] newData,
                          int offset, int numBytes)
        throws RecordStoreNotOpenException, InvalidRecordIDException,
               RecordStoreException, RecordStoreFullException;

    /**
     * Returns all of the recordId's currently in the record store.
     *
     * @return an array of the recordId's currently in the record store
     *         or null if the record store is closed.
     */
    int[] getRecordIDs();

    /**
     * Returns data base file associated with this record store
     *
     * @return data base file
     */
    AbstractRecordStoreFile getDbFile();

    /**
     * Creates data base index file associated with this record store
     *
     * @return data base index file
     * @exception IOException if failed to create a file
     */
    AbstractRecordStoreFile createIndexFile(int suiteId, String name)
        throws IOException;
}
