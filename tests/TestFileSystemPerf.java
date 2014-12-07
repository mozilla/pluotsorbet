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

            then = System.currentTimeMillis();
            FileConnection file = (FileConnection)Connector.open(dirPath + "test.txt");
            System.out.println("Time to open file: " + (System.currentTimeMillis() - then) + "ms");

            then = System.currentTimeMillis();
            boolean exists = file.exists();
            System.out.println("Time to check if file exists: " + (System.currentTimeMillis() - then) + "ms");

            if (exists) {
                then = System.currentTimeMillis();
                InputStream in = file.openInputStream();
                byte[] input = new byte[1024];
                int numBytes;
                int totalNumBytes = 0;
                while ((numBytes = in.read(input)) != -1) {
                    totalNumBytes += numBytes;
                }
                in.close();
                System.out.println("Time to read " + totalNumBytes + " bytes: " + (System.currentTimeMillis() - then) + "ms");

                then = System.currentTimeMillis();
                file.delete();
                System.out.println("Time to delete file: " + (System.currentTimeMillis() - then) + "ms");
            }

            then = System.currentTimeMillis();
            file = (FileConnection)Connector.open(dirPath + "test2.txt");
            System.out.println("Time to open test2: " + (System.currentTimeMillis() - then) + "ms");

            then = System.currentTimeMillis();
            exists = file.exists();
            System.out.println("Time to check if test2 exists: " + (System.currentTimeMillis() - then) + "ms");

            if (exists) {
                then = System.currentTimeMillis();
                InputStream in = file.openInputStream();
                byte[] input = new byte[1024];
                int numBytes;
                int totalNumBytes = 0;
                while ((numBytes = in.read(input)) != -1) {
                    totalNumBytes += numBytes;
                }
                in.close();
                System.out.println("Time to read " + totalNumBytes + " bytes: " + (System.currentTimeMillis() - then) + "ms");

                then = System.currentTimeMillis();
                file.delete();
                System.out.println("Time to delete test2: " + (System.currentTimeMillis() - then) + "ms");
            }

            then = System.currentTimeMillis();
            file.create();
            System.out.println("Time to create file: " + (System.currentTimeMillis() - then) + "ms");

            then = System.currentTimeMillis();
            OutputStream out = file.openOutputStream();
            for (int i = 0; i < 1000; i++) {
                out.write(bytes);
                out.flush();
            }
            System.out.println("Time to write/flush 1,000 times: " + (System.currentTimeMillis() - then) + "ms");

            then = System.currentTimeMillis();
            out.close();
            System.out.println("Time to close output stream: " + (System.currentTimeMillis() - then) + "ms");

            then = System.currentTimeMillis();
            file.close();
            System.out.println("Time to close file: " + (System.currentTimeMillis() - then) + "ms");

            then = System.currentTimeMillis();
            file = (FileConnection)Connector.open(dirPath + "uncached");
            file.create();
            out = file.openOutputStream();
            out.write(bytes);
            out.flush();
            out.close();
            file.delete();
            file.close();
            System.out.println("open/create/write/delete/close uncached: " + (System.currentTimeMillis() - then) + "ms");

            then = System.currentTimeMillis();
            file = (FileConnection)Connector.open(dirPath + "test.txt");
            System.out.println("Time to reopen file: " + (System.currentTimeMillis() - then) + "ms");

            then = System.currentTimeMillis();
            file.close();
            System.out.println("Time to reclose file: " + (System.currentTimeMillis() - then) + "ms");

            then = System.currentTimeMillis();
            file = (FileConnection)Connector.open(dirPath + "test2.txt");
            System.out.println("Time to open another file: " + (System.currentTimeMillis() - then) + "ms");

            then = System.currentTimeMillis();
            file.create();
            out = file.openOutputStream();
            for (int i = 0; i < 100000; i++) {
                out.write(bytes);
            }
            System.out.println("Time to write 100,000 times: " + (System.currentTimeMillis() - then) + "ms");

            then = System.currentTimeMillis();
            out.flush();
            System.out.println("Time to flush once: " + (System.currentTimeMillis() - then) + "ms");

            then = System.currentTimeMillis();
            out.close();
            System.out.println("Time to close output stream: " + (System.currentTimeMillis() - then) + "ms");

            then = System.currentTimeMillis();
            file.close();
            System.out.println("Time to close file: " + (System.currentTimeMillis() - then) + "ms");

            then = System.currentTimeMillis();
            file = (FileConnection)Connector.open(dirPath + "uncached2");
            file.create();
            out = file.openOutputStream();
            out.write(bytes);
            out.flush();
            out.close();
            file.delete();
            file.close();
            System.out.println("open/create/write/delete/close uncached2: " + (System.currentTimeMillis() - then) + "ms");
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
