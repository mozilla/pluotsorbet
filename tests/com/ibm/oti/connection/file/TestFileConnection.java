package com.ibm.oti.connection.file;

import javax.microedition.io.*;
import javax.microedition.io.file.*;
import java.util.Enumeration;
import java.io.*;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestFileConnection implements Testlet {

    public void test(TestHarness th) {
      try {
        String dirPath = System.getProperty("fileconn.dir.private").substring(2);

        FileConnection dir = (FileConnection)Connector.open(dirPath);

        th.check(dir.isOpen(), "Directory opened");
        th.check(dir.exists(), "Directory exists");
        th.check(dir.isDirectory(), "Directory is a directory");

        Enumeration files = dir.list();
        th.check(!files.hasMoreElements(), "Directory is empty");

        FileConnection file = (FileConnection)Connector.open(dirPath + "prova");
        th.check(file.isOpen(), "File opened");
        th.check(!file.exists(), "File doesn't exist");
        th.check(!file.isDirectory(), "File isn't a directory");

        file.create();

        th.check(file.exists(), "File created");
        th.check(!file.isDirectory(), "Check is directory");
        th.check(file.fileSize(), 0, "Check file size");

        OutputStream out = file.openOutputStream();
        out.write(new byte[]{ 5, 4, 3, 2, 1 });
        out.close();

        th.check(file.fileSize(), 5);

        InputStream in = file.openInputStream();
        th.check(in.read(), 5);
        th.check(in.read(), 4);
        th.check(in.read(), 3);
        th.check(in.read(), 2);
        th.check(in.read(), 1);
        th.check(in.read(), -1);
        in.close();

        file.close();
        th.check(!file.isOpen());

        files = dir.list();
        th.check(files.hasMoreElements(), "Directory has one file");
        th.check(files.nextElement(), "/prova");
        th.check(!files.hasMoreElements(), "Directory has just one file");

        dir.close();
        th.check(!dir.isOpen());
      } catch (Exception e) {
        th.fail("Unexpected exception");
        e.printStackTrace();
      }

        /*int suiteId = 0;
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
        } catch(java.io.IOException ex) {
            // We catch all expected exceptions, so any unexpected ones
            // trigger a test failure.
            th.fail(ex);
        }*/
    }
}
