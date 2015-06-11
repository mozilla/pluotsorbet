package com.jcraft;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZOutputStream;
import com.jcraft.jzlib.ZInputStream;

public class TestJZlib implements Testlet {
    public int getExpectedPass() { return 1; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    public void test(TestHarness th) {
        String value = "Hello, world!";

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ZOutputStream zOut = new ZOutputStream(out, JZlib.Z_BEST_COMPRESSION);
            DataOutputStream dataOut = new DataOutputStream(zOut);
            dataOut.writeUTF(value);
            zOut.close();

            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            ZInputStream zIn = new ZInputStream(in);
            DataInputStream dataIn = new DataInputStream(zIn);
            th.check(dataIn.readUTF(), value);
        } catch (IOException e) {
            th.fail("Unexpected exception: " + e);
        }
    }
}
