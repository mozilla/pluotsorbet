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

package com.sun.midp.rms;

import java.io.IOException;
import javax.microedition.rms.*;
import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * This class implements a record offset cache. It enables us to quickly
 * find offsets for records whose IDs we know.
 */
class OffsetCache extends IntToIntMapper {
    /**
     * A special value for LastSeenOffset.
     * There's a database header at offset 0, and 0 means we have not seen any
     * records yet.
     */
    static final int NO_OFFSET = 0;

    /**
     * After this offset there's no cached record IDs.
     * (An OffsetCache may cache offsets only for already seen records.)
     * The value NO_OFFSET is a special value for this variable:
     * NO_OFFSET means we have not seen any records yet.
     */
    public int LastSeenOffset = NO_OFFSET;

    /**
     * Constructs an empty OffsetCache with the specified initial capacity and
     * capacity increment.
     *
     * @param initialCapacity       the initial capacity
     * @param defaultElement        the offset value that gets returned for
     *                              record IDs that are not there
     * @param capacityIncrement     the amount by which the capacity is
     *                              increased when the mapper overflows.
     *                              (0 means "to be doubled")
     * @exception IllegalArgumentException if the specified initial capacity
     *            is negative
     */
    OffsetCache(int initialCapacity, int defaultElement, int capacityIncrement)
    {
        super(initialCapacity, defaultElement, capacityIncrement);
    }
}

/**
 * A class implementing a index of the record store.
 */
class RecordStoreIndex {

    /** The Record Store that this object indexes */
    private AbstractRecordStoreImpl recordStore;

    /** The Record Store database file */
    private AbstractRecordStoreFile dbFile;

    /** specifies record ID to offset mapping */
    private OffsetCache recordIdOffsets;

    /** 
     * Specifies the version of record store for which this index is valid.
     * Index becomes invalid if another MIDlet changes the record store.
     */
    private int indexVersion; 

    /**
     *  This value will be returned by recordIdOffsets.elementAt() when
     *  record id is not found
     */
    private static final int INVALID_OFFSET = -1;

    /**
     *  Capacity increment for the offset cache. Zero indicates that capacity
     *  has to be doubled each time.
     */
    private static final int CACHE_CAPACITY_INCREMENT = 0;

    /** the initial record offset cache capacity */
    private static final int INITIAL_CACHE_CAPACITY = 0x20;

    /**
     * Constructor for creating an index object for the given Record Store.
     *
     * @param rs record store that this object indexes
     * @param suiteId unique ID of the suite that owns the store
     * @param recordStoreName a string to name the record store
     */
    RecordStoreIndex(AbstractRecordStoreImpl rs, int suiteId,
                     String recordStoreName) throws IOException {
        recordStore = rs;
        dbFile = rs.getDbFile();

        indexVersion = 0;
        try {
            indexVersion = rs.getVersion();
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * Closes the index file.
     *
     * @exception IOException if there are any file errors
     */
    void close() throws IOException {
    }

    /**
     * Deletes index filed of named record store. There is no index
     * file in case of linear index implementation, thus makes
     * nothing.
     *
     * Called from RecordStoreImpl where record store files need to
     *     be deleted.
     *
     * @param suiteId not used
     * @param recordStoreName not used
     * @return always <code>true</code>
     */
    static boolean deleteIndex(int suiteId, String recordStoreName) {
        return true;
    }

    /**
     * Returns all of the recordId's currently in the record store index.
     *
     * @return an array of the recordId's currently in the index.
     */
    int[] getRecordIDs() {
        int numRecordIDs = recordStore.getNumRecords();
        int[] recordIDs = new int[numRecordIDs];

        byte[] header = new byte[AbstractRecordStoreImpl.BLOCK_HEADER_SIZE];
        int currentOffset = AbstractRecordStoreImpl.DB_HEADER_SIZE;
        int currentSize = 0;
        int currentRecordId = 0;

        int dbSize = recordStore.getSize();
        int idx = 0;
        while (idx < numRecordIDs) {
            if (currentOffset >= dbSize) {
                // reached end of db file
                break;
            }

            try {
                // seek to the next offset
                currentOffset += currentSize;
                dbFile.seek(currentOffset);

                // read the block header
                if (dbFile.read(header) !=
                    AbstractRecordStoreImpl.BLOCK_HEADER_SIZE) {
                    // error reading header
                    break;
                }
            } catch (java.io.IOException ioe) {
                break;
            }


            currentRecordId = RecordStoreUtil.getInt(header, 0);
            if (currentRecordId > 0) {
                recordIDs[idx++] = currentRecordId;
            }

            currentSize = RecordStoreUtil.
                calculateBlockSize(RecordStoreUtil.getInt(header, 4));
        }

        return recordIDs;
    }

    /**
     *  Finds the record header for the given record and returns the
     *  offset to the header.
     *
     * @param recordId the ID of the record to use in this operation
     * @param header a buffer that receives the header of the block
     *
     * @exception IOException if there is an error accessing the db file
     * @exception InvalidRecordIDException if the recordId is invalid
     *
     * @return the offset in the db file of the block added
     */
    int getRecordHeader(int recordId, byte[] header)
        throws IOException, InvalidRecordIDException {

        if (recordId <= 0) {
            throw new InvalidRecordIDException("error finding record data");
        }

        ensureIndexValidity();

        if (null != recordIdOffsets)
        {
            int offset = recordIdOffsets.elementAt(recordId);
            if (offset != recordIdOffsets.defaultValue) {
                dbFile.seek(offset);

                // read the block header
                if (dbFile.read(header) !=
                    AbstractRecordStoreImpl.BLOCK_HEADER_SIZE) {
                    // did not find the recordId at the returned offset
                    // throw away the index then!
                    // (in principle, this should never happen)
                    recordIdOffsets = null;
                }
                else
                {
                    int foundId = RecordStoreUtil.getInt(header, 0);
                    if (foundId == recordId) {
                        return offset;
                    }
                }
            }
        }
        if (null == recordIdOffsets) {
            recordIdOffsets = new OffsetCache(INITIAL_CACHE_CAPACITY,
                                              INVALID_OFFSET,
                                              CACHE_CAPACITY_INCREMENT);
        }
        return getRecordHeader_NoCache(recordId, header);
    }

    /**
     *  A helper function for getRecordHeader().
     *  Finds the record header for the given record and returns the
     *  offset to the header without using the cache.
     *
     * @param recordId  the ID of the record to use in this operation
     * @param header    a buffer that receives the header of the block
     * @return the offset in the db file of the block
     * @exception IOException
     * @exception InvalidRecordIDException
     */
    protected int getRecordHeader_NoCache(int recordId, byte[] header)
        throws IOException, InvalidRecordIDException {
        final int beginningOffset = AbstractRecordStoreImpl.DB_HEADER_SIZE;
        final int endOffset = recordStore.getSize();
        int resultOffset = INVALID_OFFSET;
        if (null != recordIdOffsets
          &&recordIdOffsets.LastSeenOffset != recordIdOffsets.NO_OFFSET)
        {
            resultOffset =
                getRecordHeader_SearchFromTo(recordId,
                                             header,
                                             recordIdOffsets.LastSeenOffset,
                                             endOffset);
        }
        if (INVALID_OFFSET == resultOffset) {
            resultOffset =
                getRecordHeader_SearchFromTo(recordId,
                                             header,
                                             beginningOffset,
                                             endOffset);
        }
        if (INVALID_OFFSET == resultOffset) {
            throw new InvalidRecordIDException("error finding record");
        }
        return resultOffset;
    }

    /**
     *  A helper function for getRecordHeader().
     *
     * @param recordId    the ID of the record to use in this operation
     * @param header      a buffer that receives the header of the block
     * @param offsetFrom  offset in the file to start search from
     * @param offsetUpto  offset in the file to abandon search at
     * @return            the offset in the db file of the block, or
     *                    -1 (INVALID_OFFSET) in place
     *                    of InvalidRecordIDException
     * @exception IOException
     * @exception InvalidRecordIDException
     */
    protected int getRecordHeader_SearchFromTo(int recordId,
                                               byte[] header,
                                               int offsetFrom,
                                               int offsetUpto)
        throws IOException, InvalidRecordIDException {
        // (we could start from last known index; I tried this, but noticed
        // no improvement)

        // search through the data blocks for a matching record
        int currentId = 0;
        int currentOffset = offsetFrom;
        int currentSize = 0;

        while (currentId != recordId) {
            // seek to the next offset
            currentOffset += currentSize;

            // IMPL_NOTE: is it correct to invoke getSize() once?
            // IMPL_NOTE: So far it is -- the db file is locked...
            if (currentOffset >= offsetUpto) {
                // reached the end and did not find the recordId
                return INVALID_OFFSET;
            }

            dbFile.seek(currentOffset);

            // read the block header
            if (dbFile.read(header) !=
                AbstractRecordStoreImpl.BLOCK_HEADER_SIZE) {
                // did not find the recordId
                return INVALID_OFFSET; // have a chance to retry
            }

            currentId = RecordStoreUtil.getInt(header, 0);
            currentSize = RecordStoreUtil.
                calculateBlockSize(RecordStoreUtil.getInt(header, 4));

            if (null != recordIdOffsets) {
                recordIdOffsets.setElementAt(currentOffset, currentId);
                recordIdOffsets.LastSeenOffset = currentOffset;
                // if we had to repeat search from the beginning, there has been
                // something wrong, and it's ok to forget where we have been.
                // Normally, LastSeenOffset will only grow.
            }
        }

        return currentOffset;
    }

    /**
     * Searches for a free block large enough for the record.
     *
     * @param header a block header with the size set to the record data size
     *
     * @exception IOException if there is an error accessing the db file
     *
     * @return the offset in the db file of the block added
     */
    int getFreeBlock(byte[] header) throws IOException {
        int targetSize = RecordStoreUtil.
            calculateBlockSize(RecordStoreUtil.getInt(header, 4));
        int currentId = 0;
        int currentOffset = AbstractRecordStoreImpl.DB_HEADER_SIZE;
        int currentSize = 0;

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_RMS,
                           "getFreeBlock recordId = " +
                           RecordStoreUtil.getInt(header, 0) +
                           " numBytes = " + RecordStoreUtil.getInt(header, 4) +
                           " targetSize = " + targetSize);
        }

        // search through the data blocks for a free block that is large enough
        while (currentOffset < recordStore.getSize()) {
            // seek to the next offset
            dbFile.seek(currentOffset);

            // read the block header
            if (dbFile.read(header) !=
                AbstractRecordStoreImpl.BLOCK_HEADER_SIZE) {
                // did not find the recordId
                throw new IOException();
            }

            currentId = RecordStoreUtil.getInt(header, 0);
            currentSize = RecordStoreUtil.
                calculateBlockSize(RecordStoreUtil.getInt(header, 4));

            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_RMS,
                               "currentId = " + currentId +
                               " currentSize = " + currentSize);
            }

            // check for a free block big enough to hold the data
            if (currentId < 0 && currentSize >= targetSize) {
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_RMS,
                                   "found free block at offset " +
                                   currentOffset);
                }

                // a free block
                return currentOffset;
            }

            // added the block size to the currentOffset
            currentOffset += currentSize;
        }

        return 0;
    }

    /**
     * Updates the index of the given block and its offset.
     *
     *  Called from RecordStoreImpl.compactRecords()
     *  after the block has been moved.
     *  Called from RecordStoreImpl.writeBlock()
     *  after the block has been written.
     *
     * @param blockOffset the offset in db file to the block to update
     * @param header the header of the block to update
     *
     * @exception java.io.IOException if there is an error
     *                                accessing the db file
     */
    void updateBlock(int blockOffset, byte[] header) throws IOException {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_RMS,
                           "updateBlock recordId = " +
                           RecordStoreUtil.getInt(header, 0) +
                           " numBytes = " + RecordStoreUtil.getInt(header, 4) +
                           " blockOffset = " + blockOffset);
        }

        ensureIndexValidity();

        int recordId = RecordStoreUtil.getInt(header, 0);
        if (null != recordIdOffsets) {
            recordIdOffsets.setElementAt(blockOffset, recordId);
        }
    }

    /**
     * Removes the given block from the index.
     *
     * Called from RecordStoreUtil.compactRecords() when a free block
     * is removed.
     *
     * @param blockOffset the offset in db file to the block to remove
     * @param header the header of the block to remove
     *
     * @exception IOException if there is an error accessing the db file
     */
    void removeBlock(int blockOffset, byte[] header) throws IOException {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_RMS,
                           "removeBlock recordId = " +
                           RecordStoreUtil.getInt(header, 0) +
                           " numBytes = " + RecordStoreUtil.getInt(header, 4) +
                           " blockOffset = " + blockOffset);
        }

        ensureIndexValidity();

        // blocks get moved, LastSeenOffset may point into the middle
        // of a record.
        // In principle, all moved blocks will notify us via UpdateBlock(),
        // so LastSeenOffset=min(LastSeenOffset,blockOffset) should be ok.
        if (null != recordIdOffsets) {
            recordIdOffsets.LastSeenOffset = recordIdOffsets.NO_OFFSET;
        }
    }

    /**
     * The record is deleted from the record store index.
     *
     * Called from  RecordStoreImpl.deleteRecord(int recordId)
     *
     * @param recordId the ID of the record index to delete
     *
     * @exception java.io.IOException if there is an error
     *                                accessing the db index
     */
    void deleteRecordIndex(int recordId) throws IOException {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_RMS,
                           "deleteRecordIndex(" + recordId + ")");
        }

        ensureIndexValidity();

        if (null != recordIdOffsets) {
            recordIdOffsets.removeElementAt(recordId);
        }
    }

    /**
     * Called when record store version has been updated.
     *
     * @param newVersion new record store version
     */
    void recordStoreVersionUpdated(int newVersion) {
        indexVersion = newVersion;
    }

    /**
     * Ensures index validity. Index becomes invalid when another 
     * MIDlet changes the record store.
     */
    void ensureIndexValidity() {
        int storeVersion = indexVersion;

        try {
            storeVersion = recordStore.getVersion();
        } catch (Exception e) {
        }

        if (indexVersion < storeVersion) {
            // out of date, can't use current index anymore           
            invalidateIndex();
            indexVersion = storeVersion;
        }
    }

    /**
     * Invalidates (clears) the index
     */
    private void invalidateIndex() {
        recordIdOffsets = null;
    }
}
