package com.ibm.oti.connection.file;

import javax.microedition.io.*;
import javax.microedition.io.file.*;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.io.*;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestFileConnectionPerf implements Testlet {
    public void test(TestHarness th) {
        try {
            String dirPath = System.getProperty("fileconn.dir.private");

            String str = "I am the very model of a modern major general.";
            byte[] bytes = str.getBytes();

            FileConnection file = (FileConnection)Connector.open(dirPath + "test.txt");
            file.create();

            long then = System.currentTimeMillis();

            OutputStream out = file.openOutputStream();
            for (int i = 0; i < 1000; i++) {
                out.write(bytes);
                out.flush();
            }

            System.out.println("Time to write/flush output: " + (System.currentTimeMillis() - then) + "ms");

            out.close();
            System.out.println("Time to close output: " + (System.currentTimeMillis() - then) + "ms");
            file.delete();
            System.out.println("Time to delete file: " + (System.currentTimeMillis() - then) + "ms");
            file.close();
            System.out.println("Time to close file: " + (System.currentTimeMillis() - then) + "ms");
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
