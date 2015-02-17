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
package javax.microedition.rms;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

import java.util.Random;

public class TestRecordStore implements Testlet {
    public int getExpectedPass() { return 15; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 1; }
    TestHarness th;

    final String RECORD_STORE_NAME = "testrms";
    byte[] smallData;
    byte[] largeData;
    int recordId1, recordId2;

    /**
     * Add a small record then a large record.
     */
    private void setupRSTest() throws RecordStoreException {
        smallData = new byte[5]; // 5 Bytes
        // Contents are 0,1,...,n
        for (int i = 0; i < smallData.length; i++) {
            smallData[i] = (byte)i;
        }

        Random random = new Random();
        largeData = new byte[10*1024]; // 10K Bytes
        // Contents are 0,1,...,n
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte)(random.nextInt(8) & 0xff);
        }

        // Remove old record store
        try {
            RecordStore.deleteRecordStore(RECORD_STORE_NAME);
        } catch (RecordStoreNotFoundException rnfe) {}

        RecordStore store = RecordStore.openRecordStore(RECORD_STORE_NAME, true);
        recordId1 = store.addRecord(smallData, 0, smallData.length);
        recordId2 = store.addRecord(largeData, 0, largeData.length);
        store.closeRecordStore();
    }

    private void testSequentialRMS() throws RecordStoreException {
        RecordStore store1 = null;
        RecordStore store2 = null;
        boolean exceptionThrown = false;

        store1 = RecordStore.openForLockTesting(RECORD_STORE_NAME);

        try {
            try {
                store2 = RecordStore.openForLockTesting(RECORD_STORE_NAME);
            } catch (RecordStoreException re) {
                exceptionThrown = true;
            }

            if (store2 != null) {
                store2.closeRecordStore();
            }
        } finally {
            store1.closeRecordStore();
        }

        th.todo(exceptionThrown);
    }

    private void testEnumeration() throws RecordStoreException {
        RecordStore store = RecordStore.openRecordStore(RECORD_STORE_NAME, false);
        th.check(store.getNumRecords(), 2);

        try {
            RecordEnumeration recordenumeration = store.enumerateRecords(null, null,
                true);
            th.check(recordenumeration.hasNextElement());
            th.check(recordenumeration.nextRecordId(), recordId1);
            th.check(recordenumeration.hasNextElement());
            th.check(recordenumeration.nextRecordId(), recordId2);

            byte record[];
            int i;

            recordenumeration.reset();

            record = recordenumeration.nextRecord();
            for (i = 0; i < record.length; i++)
                if (record[i] != smallData[i])
                    break;
            th.check(record.length, smallData.length);
            th.check(i, record.length);

            record = recordenumeration.nextRecord();
            for (i = 0; i < record.length; i++)
                if (record[i] != largeData[i])
                    break;
            th.check(record.length, largeData.length);
            th.check(i, record.length);

            recordenumeration.destroy();
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }

        store.closeRecordStore();
    }

    private void testGetRecords() throws RecordStoreException {
        RecordStore store = RecordStore.openRecordStore(RECORD_STORE_NAME, false);

        try {
            byte record[];
            int i;

            record = store.getRecord(recordId1);
            for (i = 0; i < record.length; i++)
                if (record[i] != smallData[i])
                    break;
            th.check(record.length, smallData.length);
            th.check(i, record.length);

            record = store.getRecord(recordId2);
            for (i = 0; i < record.length; i++)
                if (record[i] != largeData[i])
                    break;
            th.check(record.length, largeData.length);
            th.check(i, record.length);
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }

        store.closeRecordStore();
    }

    private void testCompactRecords() throws RecordStoreException {
        RecordStore store = RecordStore.openRecordStore(RECORD_STORE_NAME, false);

        // Delete the small record
        store.deleteRecord(recordId1);

        // Close the store to exercise compacting code
        store.closeRecordStore();

        // Reopen and check for the large record
        store = RecordStore.openRecordStore(RECORD_STORE_NAME, false);

        byte record[] = store.getRecord(recordId2);
        int i;

        for (i = 0; i < record.length; i++)
            if (record[i] != largeData[i])
                break;

        th.check(record.length, largeData.length);
        th.check(i, record.length);

        store.closeRecordStore();
    }

    /**
     * Creates a pseudo random record which is actually a fragment of
     * largeData array.
     * @param size of record required
     * @return byte array of size requested
     */
    private byte[] getRandomRecord(int size) {
        byte[] record = new byte[size];
        byte ind = (byte) (new Random().nextInt(7) & 0x7f);

        record[0] = ind;
        for (int i = 1; i < size; i++) {
            record[i] = largeData[(ind + i) % largeData.length];
        }

        return record;
    }

    /**
     * Checks if record given could be created by getRandomRecord().
     * @param record record to be checked
     * @return false if record with content given cannot be created by
     *          getRandomRecord()
     */
    private boolean isValidRandomRecord(byte[] record) {
        if (record.length < 2) {
            return false;
        }

        byte ind = record [0];
        if (ind < 0) {
            return false;
        }

        for (int i = 1; i < record.length; i++) {
            if (record[i] != largeData[(ind + i) % largeData.length]) {
                return false;
            }
        }

        return true;

    }

    private void testSizeLimit() throws RecordStoreException {
        final int RECORD_SIZE = 64;
        final int DOUBLE_SIZE = RECORD_SIZE * 2;
        int id1 = -1;
        int id2 = -1;

        RecordStore store = RecordStore.openRecordStore(RECORD_STORE_NAME, true);

        try {
            id1 = store.addRecord(getRandomRecord(RECORD_SIZE), 0, RECORD_SIZE);
            while (true) {
                id2 = store.addRecord(getRandomRecord(RECORD_SIZE),
                        0, RECORD_SIZE);
            }
        } catch (RecordStoreFullException e) {
            // Ok, the record store is full, now deleting two short records
            th.check(id1 != id2);
            store.deleteRecord(id1);
            store.deleteRecord(id2);

            // Now there should be enough space for one double-size record
            try {
                store.addRecord(getRandomRecord(DOUBLE_SIZE), 0, DOUBLE_SIZE);

                //Checking data is not damaged by preceding operations
                RecordEnumeration recordEnum = store.enumerateRecords(null, null, false);
                boolean damaged = false;

                while (recordEnum.hasNextElement()) {
                    int id = recordEnum.nextRecordId();
                    byte[] record = store.getRecord(id);
                    if (id == id1 || id == id2 || !isValidRandomRecord(record)) {
                        damaged = true;
                        break;
                    }
                }

                th.check(!damaged);
            } catch (Throwable t) {
                th.fail("Failed adding a record to freed space: " + t);
            }
        }

        store.closeRecordStore();
    }

    private void cleanup() throws RecordStoreException {
        RecordStore.deleteRecordStore(RECORD_STORE_NAME);
    }

    public void test(TestHarness th) {
        this.th = th;

        try {
            setupRSTest();
            testSequentialRMS();
            testEnumeration();
            testGetRecords();
            testCompactRecords();
            //cleanup();
            //testSizeLimit();
        } catch (Throwable t) {
            th.fail("Unexpected exception: " + t);
            t.printStackTrace();
        } finally {
            try {
                cleanup();
            } catch (Exception e) {
                th.fail("Unexpected exception while cleaning up: " + e);
            }
        }
    }
}
