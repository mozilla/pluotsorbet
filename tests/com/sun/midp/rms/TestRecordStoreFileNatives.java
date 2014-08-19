package com.sun.midp.rms;

import com.sun.j2me.security.AccessControlContext;
import com.sun.j2me.security.AccessController;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import javax.microedition.rms.RecordStoreException;

public class TestRecordStoreFileNatives implements Testlet, SuiteContainer, AccessControlContext {
    // SuiteContainer stubs
    public int getCallersSuiteId() { return 0; }
    public int getSuiteId(String vendorName, String suiteName) { return 0; }
    public String getSecureFilenameBase(int suiteId) { return "testBase"; }
    public int getStorageAreaId(int suiteId) { return 0; }

    // AccessControlContext stubs
    public void checkPermission(String name) throws SecurityException {}
    public void checkPermission(String name, String resource) throws SecurityException {}
    public void checkPermission(String name, String resource, String extraValue) throws SecurityException {}

    public void test(TestHarness th) {
        int suiteId = 0;
        String filenameBase = "testBase";
        String name = "testFile";
        int extension = 1;
        byte[] writeBuffer = {111, 11, 0, -11, -111};
        byte[] readBuffer;

        int expectedSpaceAvailable = 50 * 1024 * 1024;

        // Init stuff that uses our SuiteContainer and AccessControlContext
        // stubs to give the midlet access to RecordStoreFile.
        AccessController.setAccessControlContext(this);
        RmsEnvironment.init(this);

        th.check(RecordStoreFile.spaceAvailableNewRecordStore0(filenameBase, suiteId), expectedSpaceAvailable);

        try {
            // Test openRecordStoreFile native, which gets called automatically
            // when you instantiate a RecordStoreFile.
            RecordStoreFile file = new RecordStoreFile(suiteId, name, extension);

            // Test writeBytes, setPosition, and readBytes natives.
            file.write(writeBuffer);
            readBuffer = new byte[5];
            try {
                file.read(readBuffer);
                th.fail("attempt to read past end of file succeeded");
            } catch(java.io.IOException ex) {
                th.check(ex, "java.io.IOException: handle invalid or segment indices out of bounds");
            }
            file.seek(0);
            file.read(readBuffer);
            for (int i = 0; i < writeBuffer.length; i++) {
              th.check(readBuffer[i], writeBuffer[i]);
            }

            // Test closing, reopening, and rereading file.
            // There's no way to reopen an existing RecordStoreFile, so we have
            // to re-instantiate it.
            file.close();
            file = new RecordStoreFile(suiteId, name, extension);
            readBuffer = new byte[5];
            file.read(readBuffer);
            for (int i = 0; i < writeBuffer.length; i++) {
              th.check(readBuffer[i], writeBuffer[i]);
            }

            // Test truncating the file and then rereading it without closing it
            // first.
            file.truncate(0);
            file.seek(0);
            readBuffer = new byte[1];
            try {
                file.read(readBuffer);
                th.fail("attempt to read byte from truncated file succeeded");
            } catch(java.io.IOException ex) {
                th.check(ex, "java.io.IOException: handle invalid or segment indices out of bounds");
            }

            // Now close the file, reopen it, and ensure it's still truncated.
            file.close();
            file = new RecordStoreFile(suiteId, name, extension);
            readBuffer = new byte[1];
            try {
                file.read(readBuffer);
                th.fail("attempt to read byte from truncated file succeeded");
            } catch(java.io.IOException ex) {
                th.check(ex, "java.io.IOException: handle invalid or segment indices out of bounds");
            }

            // Clean up.
            file.close();
            try {
                RecordStoreUtil.deleteFile(filenameBase, name, extension);
            } catch(RecordStoreException ex) {
                th.fail("record store file could not be deleted: " + ex);
            }
        } catch(java.io.IOException ex) {
            // We catch all expected exceptions, so any unexpected ones
            // trigger a test failure.
            th.fail(ex);
        }
    }
}
