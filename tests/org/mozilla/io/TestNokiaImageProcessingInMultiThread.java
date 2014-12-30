package org.mozilla.io;

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import com.nokia.mid.s40.io.LocalMessageProtocolMessage;
import com.nokia.mid.s40.io.LocalMessageProtocolConnection;
import com.nokia.mid.s40.codec.DataEncoder;
import com.nokia.mid.s40.codec.DataDecoder;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class TestNokiaImageProcessingInMultiThread implements Testlet {
    LocalMessageProtocolConnection client;
    static final String PROTO_NAME = "nokia.image-processing";

    class TestThread extends Thread {
        TestHarness th = null;
        String name = null;

        public TestThread(String name, TestHarness th) {
            this.name = name;
            this.th = th;
        }

        byte[] read(InputStream is) throws IOException {
            int l = is.available();
            byte[] buffer = new byte[l+1];
            int length = 0;

            while ((l = is.read(buffer, length, buffer.length - length)) != -1) {
                length += l;
                if (length == buffer.length) {
                    byte[] b = new byte[buffer.length + 4096];
                    System.arraycopy(buffer, 0, b, 0, length);
                    buffer = b;
                }
            }

            return buffer;
        }

        public void run() {
            try {
                LocalMessageProtocolConnection client = (LocalMessageProtocolConnection)Connector.open("localmsg://" + PROTO_NAME);
                FileConnection originalImage = (FileConnection)Connector.open("file:////" + this.name, Connector.READ_WRITE);
                if (!originalImage.exists()) {
                    originalImage.create();
                }
                OutputStream os = originalImage.openDataOutputStream();
                InputStream is = TestNokiaImageProcessingInMultiThread.class.getResourceAsStream("test.jpg");
                os.write(read(is));
                os.close();

                DataEncoder dataEncoder = new DataEncoder("Conv-BEB");
                dataEncoder.putStart(14, "event");
                dataEncoder.put(13, "name", "Scale");
                dataEncoder.put(2, "trans_id", 42);
                dataEncoder.put(11, "filename", this.name);
                dataEncoder.putStart(15, "limits");
                dataEncoder.put(5, "max_hres", 100);
                dataEncoder.put(5, "max_vres", 100);
                dataEncoder.putEnd(15, "limits");
                dataEncoder.put(10, "aspect", "FullImage");
                dataEncoder.put(2, "quality", 80);
                dataEncoder.putEnd(14, "event");
                byte[] sendData = dataEncoder.getData();
                client.send(sendData, 0, sendData.length);

                LocalMessageProtocolMessage msg = client.newMessage(null);
                client.receive(msg);
                byte[] clientData = msg.getData();

                DataDecoder dataDecoder = new DataDecoder("Conv-BEB", clientData, 0, clientData.length);
                dataDecoder.getStart(14);
                th.check(dataDecoder.getString(13), "Scale");
                th.check(dataDecoder.getInteger(2), 42);
                th.check(dataDecoder.getString(10), "Complete");
                String path = "file:////" + dataDecoder.getString(11);

                FileConnection file = (FileConnection)Connector.open(path);
                th.check(file.exists(), "File exists");
                file.delete();
                th.check(!file.exists(), path + " has been deleted.");
                file.close();

                originalImage.delete();
                th.check(!originalImage.exists(), "Original image has been deleted");
                originalImage.close();

                client.close();
            } catch (IOException ioe) {
                th.fail("Unexpected exception");
                ioe.printStackTrace();
            }
        }
    }

    public void test(TestHarness th) {
        try {
            Thread t1 = new TestThread("test1.jpg", th);
            Thread t2 = new TestThread("test2.jpg", th);
            t1.start();
            t2.start();
            t1.join();
            t2.join();
            System.out.println("Done");
        } catch (InterruptedException ioe) {
            th.fail("Unexpected thread exception");
            ioe.printStackTrace();
        }
    }
}

