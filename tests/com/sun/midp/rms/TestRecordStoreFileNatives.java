package com.sun.midp.rms;

import com.sun.j2me.security.AccessControlContext;
import com.sun.j2me.security.AccessController;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestRecordStoreFileNatives implements Testlet, SuiteContainer, AccessControlContext {
    public void test(TestHarness th) {
        int suiteId = 0;
        String filenameBase = "testBase";
        String name = "testFile";
        int extension = 1;
        byte[] writeBuffer = {111, 11, 0, -11, -111};
        byte[] readBuffer = new byte[5];

        int expectedSpaceAvailable = 50 * 1024 * 1024;

        // Init stuff we need in order to be able to use the API.
        AccessController.setAccessControlContext(this);
        RmsEnvironment.init(this);

        th.check(RecordStoreFile.spaceAvailableNewRecordStore0(filenameBase, suiteId), expectedSpaceAvailable);

        try {
            // Tests openRecordStoreFile native.
            RecordStoreFile file = new RecordStoreFile(suiteId, name, extension);

            // Tests writeBytes and readBytes natives.
            file.write(writeBuffer);
            file.commitWrite();
            file.read(readBuffer);
            for (int i = 0; i < writeBuffer.length; i++) {
              th.check(writeBuffer[i], readBuffer[i]);
            }
        } catch(java.io.IOException ex) {
            th.fail(ex);
        }
    }

    // SuiteContainer methods
    public int getCallersSuiteId() { return 0; }
    public int getSuiteId(String vendorName, String suiteName) { return 0; }
    public String getSecureFilenameBase(int suiteId) { return "testBase"; }
    public int getStorageAreaId(int suiteId) { return 0; }

    // AccessControlContext methods
    public void checkPermission(String name) throws SecurityException {}
    public void checkPermission(String name, String resource) throws SecurityException {}
    public void checkPermission(String name, String resource, String extraValue) throws SecurityException {}
}
