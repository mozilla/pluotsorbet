/* vim: set filetype=java shiftwidth=4 tabstop=4 autoindent cindent expandtab : */

import com.sun.cldchi.jvm.JVM;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import java.io.*;

public class TestFileSystemPerf {
    public static void main(String args[]) {
        try {
            String dirPath = System.getProperty("fileconn.dir.private");
            long then;

            String str = "I am the very model of a modern major general.";
            byte[] bytes = str.getBytes();

            then = JVM.monotonicTimeMillis();
            FileConnection file = (FileConnection)Connector.open(dirPath + "test.txt");
            System.out.println("Time to open file: " + (JVM.monotonicTimeMillis() - then) + "ms");

            then = JVM.monotonicTimeMillis();
            boolean exists = file.exists();
            System.out.println("Time to check if file exists: " + (JVM.monotonicTimeMillis() - then) + "ms");

            if (exists) {
                then = JVM.monotonicTimeMillis();
                InputStream in = file.openInputStream();
                byte[] input = new byte[1024];
                int numBytes;
                int totalNumBytes = 0;
                while ((numBytes = in.read(input)) != -1) {
                    totalNumBytes += numBytes;
                }
                in.close();
                System.out.println("Time to read " + totalNumBytes + " bytes: " + (JVM.monotonicTimeMillis() - then) + "ms");

                then = JVM.monotonicTimeMillis();
                file.delete();
                System.out.println("Time to delete file: " + (JVM.monotonicTimeMillis() - then) + "ms");
            }

            then = JVM.monotonicTimeMillis();
            file = (FileConnection)Connector.open(dirPath + "test2.txt");
            System.out.println("Time to open test2: " + (JVM.monotonicTimeMillis() - then) + "ms");

            then = JVM.monotonicTimeMillis();
            exists = file.exists();
            System.out.println("Time to check if test2 exists: " + (JVM.monotonicTimeMillis() - then) + "ms");

            if (exists) {
                then = JVM.monotonicTimeMillis();
                InputStream in = file.openInputStream();
                byte[] input = new byte[1024];
                int numBytes;
                int totalNumBytes = 0;
                while ((numBytes = in.read(input)) != -1) {
                    totalNumBytes += numBytes;
                }
                in.close();
                System.out.println("Time to read " + totalNumBytes + " bytes: " + (JVM.monotonicTimeMillis() - then) + "ms");

                then = JVM.monotonicTimeMillis();
                file.delete();
                System.out.println("Time to delete test2: " + (JVM.monotonicTimeMillis() - then) + "ms");
            }

            then = JVM.monotonicTimeMillis();
            file.create();
            System.out.println("Time to create file: " + (JVM.monotonicTimeMillis() - then) + "ms");

            then = JVM.monotonicTimeMillis();
            OutputStream out = file.openOutputStream();
            for (int i = 0; i < 1000; i++) {
                out.write(bytes);
                out.flush();
            }
            System.out.println("Time to write/flush 1,000 times: " + (JVM.monotonicTimeMillis() - then) + "ms");

            then = JVM.monotonicTimeMillis();
            out.close();
            System.out.println("Time to close output stream: " + (JVM.monotonicTimeMillis() - then) + "ms");

            then = JVM.monotonicTimeMillis();
            file.close();
            System.out.println("Time to close file: " + (JVM.monotonicTimeMillis() - then) + "ms");

            then = JVM.monotonicTimeMillis();
            file = (FileConnection)Connector.open(dirPath + "uncached");
            file.create();
            out = file.openOutputStream();
            out.write(bytes);
            out.flush();
            out.close();
            file.delete();
            file.close();
            System.out.println("open/create/write/delete/close uncached: " + (JVM.monotonicTimeMillis() - then) + "ms");

            then = JVM.monotonicTimeMillis();
            file = (FileConnection)Connector.open(dirPath + "test.txt");
            System.out.println("Time to reopen file: " + (JVM.monotonicTimeMillis() - then) + "ms");

            then = JVM.monotonicTimeMillis();
            file.close();
            System.out.println("Time to reclose file: " + (JVM.monotonicTimeMillis() - then) + "ms");

            then = JVM.monotonicTimeMillis();
            file = (FileConnection)Connector.open(dirPath + "test2.txt");
            System.out.println("Time to open another file: " + (JVM.monotonicTimeMillis() - then) + "ms");

            then = JVM.monotonicTimeMillis();
            file.create();
            out = file.openOutputStream();
            for (int i = 0; i < 100000; i++) {
                out.write(bytes);
            }
            System.out.println("Time to write 100,000 times: " + (JVM.monotonicTimeMillis() - then) + "ms");

            then = JVM.monotonicTimeMillis();
            out.flush();
            System.out.println("Time to flush once: " + (JVM.monotonicTimeMillis() - then) + "ms");

            then = JVM.monotonicTimeMillis();
            out.close();
            System.out.println("Time to close output stream: " + (JVM.monotonicTimeMillis() - then) + "ms");

            then = JVM.monotonicTimeMillis();
            file.close();
            System.out.println("Time to close file: " + (JVM.monotonicTimeMillis() - then) + "ms");

            then = JVM.monotonicTimeMillis();
            file = (FileConnection)Connector.open(dirPath + "uncached2");
            file.create();
            out = file.openOutputStream();
            out.write(bytes);
            out.flush();
            out.close();
            file.delete();
            file.close();
            System.out.println("open/create/write/delete/close uncached2: " + (JVM.monotonicTimeMillis() - then) + "ms");

            file = (FileConnection)Connector.open(dirPath + "list-test-dir");
            if (!file.exists()) {
                file.mkdir();
            }
            file.close();

            for (int i = 0; i < 1000; i++) {
                file = (FileConnection)Connector.open(dirPath + "list-test-dir/file" + i);
                if (!file.exists()) {
                    file.create();
                    out = file.openOutputStream();
                    for (int j = 0; j < 1000; j++) {
                        out.write(bytes);
                    }
                    out.flush();
                    out.close();
                }
                file.close();
            }

            file = (FileConnection)Connector.open(dirPath + "list-test-dir");
            then = JVM.monotonicTimeMillis();
            file.list();
            System.out.println("list dir with 1000 files: " + (JVM.monotonicTimeMillis() - then) + "ms");
            file.close();
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
