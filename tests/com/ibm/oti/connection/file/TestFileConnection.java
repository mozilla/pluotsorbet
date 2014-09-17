package com.ibm.oti.connection.file;

import javax.microedition.io.*;
import javax.microedition.io.file.*;
import java.util.Enumeration;
import java.util.Vector;
import java.io.*;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestFileConnection implements Testlet {
    String dirPath;
    FileConnection dir;

    void testListFilter(TestHarness th) throws IOException {
        Vector provaDirFiles = new Vector();
        provaDirFiles.addElement(Connector.open(dirPath + "provaDir/prova1.doc"));
        provaDirFiles.addElement(Connector.open(dirPath + "provaDir/prova2.doc"));
        provaDirFiles.addElement(Connector.open(dirPath + "provaDir/prova3.doc"));
        provaDirFiles.addElement(Connector.open(dirPath + "provaDir/.doc"));
        provaDirFiles.addElement(Connector.open(dirPath + "provaDir/marco_it.res"));
        provaDirFiles.addElement(Connector.open(dirPath + "provaDir/marco_en.res"));
        provaDirFiles.addElement(Connector.open(dirPath + "provaDir/marco_"));

        for (int i = 0; i < provaDirFiles.size(); i++) {
            FileConnection file = (FileConnection)provaDirFiles.elementAt(i);
            file.create();
        }

        Enumeration files = dir.list("*.doc", false);
        th.check(files.hasMoreElements(), "Elements found");
        th.check(files.nextElement(), "provaDir/prova1.doc");
        th.check(files.nextElement(), "provaDir/prova2.doc");
        th.check(files.nextElement(), "provaDir/prova3.doc");
        th.check(!files.hasMoreElements(), "Only 3 elements found");

        files = dir.list("marco_*.res", false);
        th.check(files.hasMoreElements(), "Elements found");
        th.check(files.nextElement(), "provaDir/marco_it.res");
        th.check(files.nextElement(), "provaDir/marco_en.res");
        th.check(!files.hasMoreElements(), "Only 2 elements found");

        files = dir.list("m*.re*", false);
        th.check(files.hasMoreElements(), "Elements found");
        th.check(files.nextElement(), "provaDir/marco_it.res");
        th.check(files.nextElement(), "provaDir/marco_en.res");
        th.check(!files.hasMoreElements(), "Only 2 elements found");

        files = dir.list("*.js", false);
        th.check(!files.hasMoreElements(), "No elements found");

        for (int i = 0; i < provaDirFiles.size(); i++) {
            FileConnection file = (FileConnection)provaDirFiles.elementAt(i);
            file.delete();
            file.close();
        }
    }

    void testLastModified(TestHarness th) throws IOException {
        FileConnection file = (FileConnection)Connector.open(dirPath + "provaDir/stat.txt");

        // FileConnection.lastModified returns microseconds for some reason,
        // so we have to convert this value from millis to micros.
        long lastTime = System.currentTimeMillis() * 1000;
        long modifiedTime;

        try {
            file.lastModified();
            th.fail("nonexistent path: /provaDir/stat.txt");
        } catch(IllegalArgumentException e) {
            th.check(e.getMessage(), "nonexistent path: /provaDir/stat.txt");
        }

        try { Thread.sleep(1); } catch (Exception e) {}
        file.create();
        modifiedTime = file.lastModified();
        th.check(modifiedTime > lastTime, "create updates mtime");
        lastTime = modifiedTime;

        // Merely opening the output stream shouldn't update the mtime, but our
        // implementation eagerly truncates the file, which updates the mtime.
        try { Thread.sleep(1); } catch (Exception e) {}
        OutputStream out = file.openOutputStream();
        modifiedTime = file.lastModified();
        th.todo(modifiedTime, lastTime, "open output stream doesn't update mtime");
        lastTime = modifiedTime;

        try { Thread.sleep(1); } catch (Exception e) {}
        out.write(new byte[]{ 4, 3, 2, 1 });
        out.close();
        modifiedTime = file.lastModified();
        th.check(modifiedTime > lastTime, "write updates mtime");
        lastTime = modifiedTime;

        try { Thread.sleep(1); } catch (Exception e) {}
        file.truncate(4);
        modifiedTime = file.lastModified();
        th.check(modifiedTime, lastTime, "truncate to same size doesn't update mtime");
        lastTime = modifiedTime;

        // FileConnection.truncate behaves differently from ftruncate
        // when the new size is greater than the existing size of the file.
        // In that case, ftruncate increases the size of the file,
        // while FileConnection.truncate returns early without changing it.
        try { Thread.sleep(1); } catch (Exception e) {}
        file.truncate(5);
        modifiedTime = file.lastModified();
        th.check(modifiedTime, lastTime, "truncate to larger size doesn't update mtime");
        lastTime = modifiedTime;

        try { Thread.sleep(1); } catch (Exception e) {}
        file.truncate(3);
        modifiedTime = file.lastModified();
        th.check(modifiedTime > lastTime, "truncate to smaller size updates mtime");
        lastTime = modifiedTime;

        file.delete();
        file.close();
    }

    public void test(TestHarness th) {
        try {
            dirPath = System.getProperty("fileconn.dir.private").substring(2);

            dir = (FileConnection)Connector.open(dirPath + "provaDir");

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
            // Closing the output stream again should succeed
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
            // Closing the input stream again should succeed
            in.close();

            // Test reading
            in = file.openInputStream();
            byte[] data = new byte[5];
            th.check(in.read(data, 0, 5), 5);
            th.check(data[0], 5);
            th.check(data[1], 4);
            th.check(data[2], 3);
            th.check(data[3], 2);
            th.check(data[4], 1);
            in.close();

            // Test reading with offset and length
            in = file.openInputStream();
            byte[] smallBuffer = new byte[3];
            smallBuffer[0] = 42;
            th.check(in.read(smallBuffer, 1, 2), 2);
            th.check(smallBuffer[0], 42);
            th.check(smallBuffer[1], 5);
            th.check(smallBuffer[2], 4);
            in.close();

            // Test reading more than the size of the file
            in = file.openInputStream();
            byte[] bigBuffer = new byte[50];
            th.check(in.read(bigBuffer, 0, 50), 5);
            th.check(bigBuffer[0], 5);
            th.check(bigBuffer[1], 4);
            th.check(bigBuffer[2], 3);
            th.check(bigBuffer[3], 2);
            th.check(bigBuffer[4], 1);
            for (int i = 5; i < bigBuffer.length; i++) {
                th.check(bigBuffer[i], 0);
            }
            in.close();

            // Test with negative offset
            in = file.openInputStream();
            try {
                in.read(data, -1, 0);
                th.fail("Exception expected");
            } catch (IndexOutOfBoundsException e) {
                th.check(true, "Exception expected");
            }

            // Test with negative count
            try {
                in.read(data, 0, -1);
                th.fail("Exception expected");
            } catch (IndexOutOfBoundsException e) {
                th.check(true, "Exception expected");
            }

            // Test with offset > buffer len
            try {
                in.read(data, 7, 1);
                th.fail("Exception expected");
            } catch (IndexOutOfBoundsException e) {
                th.check(true, "Exception expected");
            }

            // Test with (buffer len - offset) < count
            try {
                in.read(data, 4, 3);
                th.fail("Exception expected");
            } catch (IndexOutOfBoundsException e) {
                th.check(true, "Exception expected");
            }

            // Test with buffer len 0
            byte[] empty = new byte[0];
            th.check(in.read(empty, 0, 0), 0);

            // Test with count 0
            th.check(in.read(data, 0, 0), 0);

            in.close();
            file.close();
            th.check(!file.isOpen());

            // Test with closed file
            try {
                in.read(data, 0, 5);
                th.fail("Exception expected");
            } catch (IOException e) {
                th.check(e.getMessage(), "File Connection InputStream closed");
            }

            try {
                in.read();
                th.fail("Exception expected");
            } catch (IOException e) {
                th.check(e.getMessage(), "File Connection InputStream closed");
            }

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
            th.check(files.nextElement(), "provaDir/prova");
            th.check(!files.hasMoreElements(), "Directory has just one file");

            testListFilter(th);
            testLastModified(th);

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
