package com.sun.midp.io.j2me.storage;

import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestRandomAccessStream implements Testlet {
    public int getExpectedPass() { return 108; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    public void test(TestHarness th) {
        RandomAccessStream ras = new RandomAccessStream();

        boolean exceptionThrown = false;
        try {
            ras.getSizeOf();
        } catch (IOException e) {
            exceptionThrown = true;
        }
        th.check(exceptionThrown);

        exceptionThrown = false;
        try {
            ras.connect("afile", Connector.READ_WRITE);
            th.check(ras.getSizeOf(), 0);
        } catch(IOException e) {
            exceptionThrown = true;
        }
        th.check(!exceptionThrown);

        try {
            byte bytes[] = new byte[100];
            for (int i = 0; i < 100; i++) {
                bytes[i] = (byte)i;
            }
            int bytesReadWritten = ras.writeBytes(bytes, 0, 100);
            th.check(bytesReadWritten, 100);
            ras.commitWrite();

            th.check(ras.getSizeOf(), 100);

            ras.setPosition(0);
            bytesReadWritten = ras.readBytes(bytes, 0, 100);
            th.check(bytesReadWritten, 100);
            for (int i = 0; i < 100; i++) {
                th.check(bytes[i], i);
            }

            ras.close();

            cleanup();
        } catch (IOException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }

        exceptionThrown = false;
        try {
            ras.connect("afile2", Connector.READ);
        } catch(IOException e) {
            exceptionThrown = true;
        }
        th.check(exceptionThrown);

        exceptionThrown = false;
        try {
            ras.connect("adir/afile", Connector.READ);
        } catch(IOException e) {
            exceptionThrown = true;
        }
        th.check(exceptionThrown);
    }

    void cleanup() throws IOException {
        FileConnection file = (FileConnection)Connector.open("file:////afile");
        file.delete();
        file.close();
    }
}
