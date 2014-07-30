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

import java.io.*;

import javax.microedition.rms.*;

import com.sun.midp.i18n.Resource;

import com.sun.midp.i18n.ResourceConstants;

import com.sun.midp.main.MIDletSuiteUtils;

import com.sun.midp.midlet.MIDletStateHandler;

import com.sun.midp.midletsuite.MIDletInfo;
import com.sun.midp.midletsuite.MIDletSuiteStorage;
import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.configurator.Constants;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

import com.sun.midp.midletsuite.MIDletSuiteLockedException;

/**
 * Implements auto testing for SVM.
 */
class AutoTesterHelper extends AutoTesterHelperBase {
    /** Settings database name. */
    private static final String AUTOTEST_STORE = "autotest";

    /** Record ID of URL. */
    private static final int URL_RECORD_ID = 1;

    /** Record ID of the security domain for unsigned suites. */
    private static final int DOMAIN_RECORD_ID = 2;

    /** Record ID of suite ID. */
    private static final int SUITE_ID_RECORD_ID = 3;

    /** Record ID of loopCount */
    private static final int LOOP_COUNT_RECORD_ID = 4;

    /** ID of the test suite being run */
    private int suiteId = MIDletSuite.UNUSED_SUITE_ID;   

    /** Class name of the MIDlet that uses this AutoTesterHelper instance */
    private String midletClassName;

    /**
     * Constructor for creating class instance in case of new session.
     *
     * @param theMIDletClassName class name of the MIDlet that uses 
     * this AutoTesterHelper instance
     * @param theURL URL of the test suite
     * @param theDomain security domain to assign to unsigned suites
     * @param theLoopCount how many iterations to run the suite
     */
    AutoTesterHelper(String theMIDletClassName, String theURL, 
            String theDomain, int theLoopCount) {

        super(theURL, theDomain, theLoopCount);

        suiteId = MIDletSuite.UNUSED_SUITE_ID;
        midletClassName = theMIDletClassName;
    }   

    /**
     * Constructor for creating class instance in case of restored session.
     *
     * @param theMIDletClassName class name of the MIDlet that uses 
     * this AutoTesterHelper instance
     * @param theURL URL of the test suite
     * @param theDomain security domain to assign to unsigned suites
     * @param theLoopCount how many iterations to run the suite
     * @param theSuiteId ID of the tests suite
     */
    AutoTesterHelper(String theMIDletClassName, String theURL, 
            String theDomain, int theLoopCount, int theSuiteId) {

        super(theURL, theDomain, theLoopCount);

        suiteId = theSuiteId;
        midletClassName = theMIDletClassName;
    }

    /**
     * Installs and performs the tests.
     */
    void installAndPerformTests() 
        throws Exception {

        if (url == null) {
            return;
        }

        MIDletInfo midletInfo;
        boolean restartScheduled = false;

        try {
            if (loopCount != 0) {
                // force an overwrite and remove the RMS data
                suiteId = installer.installJad(url, 
                        Constants.INTERNAL_STORAGE_ID, true, true, null);

                midletInfo = getFirstMIDletOfSuite(suiteId);
                MIDletSuiteUtils.execute(suiteId, midletInfo.classname, 
                        midletInfo.name);

                // We want auto tester MIDlet to run after the test is run.
                MIDletSuiteUtils.setLastSuiteToRun(
                        MIDletStateHandler.getMidletStateHandler().
                        getMIDletSuite().getID(),
                        midletClassName, null, null);

                if (loopCount > 0) {
                    loopCount -= 1;
                }
            
                saveSession();
                restartScheduled = true;
            }
        } finally {
            if (!restartScheduled) {
                // we are done: cleanup
                if (midletSuiteStorage != null && 
                        suiteId != MIDletSuite.UNUSED_SUITE_ID) {
                    try {
                        midletSuiteStorage.remove(suiteId);
                    } catch (Throwable ex) {
                        if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                            Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                                        "Throwable in remove");
                        }
                    }
                }

                endSession();
            }
        }
    }    
    
    /**
     * Gets ID of current test suite.
     *
     * @return ID of current test suite 
     */
    int getTestSuiteId() {
        return suiteId;
    } 

    /**
     * Restores the data from the last session.
     *
     * @param theMIDletClassName class name of the MIDlet that uses 
     * this AutoTesterHelper instance. This name is not expected to
     * change within single session and therefore not saved
     * @return AutoTesterHelper instance if there was data saved from 
     * the last session, null otherwise
     */
    static AutoTesterHelper restoreSession(String theMIDletClassName)
        throws Exception {

        RecordStore settings = null;
        ByteArrayInputStream bas;
        DataInputStream dis;
        byte[] data;

        try {
            String url = null;
            String domain = null;
            int suiteId = MIDletSuite.UNUSED_SUITE_ID;
            int loopCount = -1;

            settings = RecordStore.openRecordStore(AUTOTEST_STORE, false);

            data = settings.getRecord(URL_RECORD_ID);
            if (data == null) {
                return null;
            }

            bas = new ByteArrayInputStream(data);
            dis = new DataInputStream(bas);
            url = dis.readUTF();

            data = settings.getRecord(DOMAIN_RECORD_ID);
            if (data != null && data.length > 0) {
                bas = new ByteArrayInputStream(data);
                dis = new DataInputStream(bas);
                domain = dis.readUTF();
            }

            data = settings.getRecord(SUITE_ID_RECORD_ID);
            if (data != null && data.length > 0) {
                bas = new ByteArrayInputStream(data);
                dis = new DataInputStream(bas);
                suiteId = dis.readInt();
            }

            data = settings.getRecord(LOOP_COUNT_RECORD_ID);
            if (data != null && data.length > 0) {
                bas = new ByteArrayInputStream(data);
                dis = new DataInputStream(bas);
                loopCount = dis.readInt();
            }

            return new AutoTesterHelper(theMIDletClassName, 
                    url, domain, loopCount, suiteId);

        } catch (RecordStoreNotFoundException rsnfe) {
            // This normal when no initial args are given, ignore
        } finally {
            if (settings != null) {
                try {
                    settings.closeRecordStore();
                } catch (Exception ex) {
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                        "closeRecordStore threw an Exception");
                    }
                }
            }
        }

        return null;
    }
    
    /**
     * Save session data for next time.
     *
     * @exception if an exception occurs
     */
    private void saveSession() 
        throws Exception {

        RecordStore settings = null;
        boolean newStore = false;
        ByteArrayOutputStream bas;
        DataOutputStream dos;
        byte[] data;

        if (url == null) {
            return;
        }

        try {
            settings = RecordStore.openRecordStore(AUTOTEST_STORE, true);

            if (settings.getNextRecordID() == URL_RECORD_ID) {
                newStore = true;
            }

            bas = new ByteArrayOutputStream();
            dos = new DataOutputStream(bas);
            dos.writeUTF(url);
            data = bas.toByteArray();

            if (newStore) {
                settings.addRecord(data, 0, data.length);
            } else {
                settings.setRecord(URL_RECORD_ID, data, 0, data.length);
            }

            bas.reset();
            dos.writeUTF(domain);
            data = bas.toByteArray();

            if (newStore) {
                settings.addRecord(data, 0, data.length);
            } else {
                settings.setRecord(DOMAIN_RECORD_ID, data, 0, data.length);
            }

            bas.reset();
            dos.writeInt(suiteId);
            data = bas.toByteArray();

            if (newStore) {
                settings.addRecord(data, 0, data.length);
            } else {
                settings.setRecord(SUITE_ID_RECORD_ID, data, 0, data.length);
            }

            bas.reset();
            dos.writeInt(loopCount);
            data = bas.toByteArray();

            if (newStore) {
                settings.addRecord(data, 0, data.length);
            } else {
                settings.setRecord(LOOP_COUNT_RECORD_ID, data, 0, data.length);
            }
        } finally {
            if (settings != null) {
                try {
                    settings.closeRecordStore();
                } catch (Exception ex) {
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                        "closeRecordStore threw an exception");
                    }
                }
            }
        }
    }

    /** End the testing session. */
    private void endSession() {
        try {
            RecordStore.deleteRecordStore(AUTOTEST_STORE);
        } catch (Throwable ex) {
            // ignore
        }
    }
}
