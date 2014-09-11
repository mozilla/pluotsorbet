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

            FileConnection dir = (FileConnection)Connector.open(dirPath + "provaDir");

            th.check(dir.isOpen(), "Directory opened");
            th.check(!dir.exists(), "Directory doesn't exist");
            th.check(!dir.isDirectory(), "Directory isn't (yet) a directory");

            dir.mkdir();

            th.check(dir.isOpen(), "Directory opened");
            th.check(dir.exists(), "Directory exists");
            th.check(dir.isDirectory(), "Directory is a directory");

            Enumeration files = dir.list();
            th.check(!files.hasMoreElements(), "Directory is empty");

            FileConnection file = (FileConnection)Connector.open(dirPath + "provaDir/prova");
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

            file = (FileConnection)Connector.open(dirPath + "provaDir/prova");
            in = file.openInputStream();
            th.check(in.available(), 5);
            in.read();
            th.check(in.available(), 4);
            th.check(in.skip((long) 1), 1);
            th.check(in.available(), 3);
            th.check(in.skip((long) 10), 3);
            th.check(in.available(), 0);
            th.check(in.skip((long) 1), 0);
            in.close();
            file.close();

            files = dir.list();
            th.check(files.hasMoreElements(), "Directory has one file");
            th.check(files.nextElement(), "/provaDir/prova");
            th.check(!files.hasMoreElements(), "Directory has just one file");

            dir.close();
            th.check(!dir.isOpen());

            file = (FileConnection)Connector.open(dirPath + "provaDir/prova");
            file.delete();
            th.check(!file.exists());
            file.close();

            dir = (FileConnection)Connector.open(dirPath + "provaDir");
            dir.delete();
            th.check(!dir.exists());
            dir.close();

            try {
                file = (FileConnection)Connector.open(dirPath + "prov>");
                th.fail("Exception expected");
            } catch (IllegalArgumentException e) {
                th.check(e.getMessage(), "Invalid file name in FileConnection Url: ///prov>");
            }
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
