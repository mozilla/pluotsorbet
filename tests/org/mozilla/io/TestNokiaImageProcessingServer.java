package org.mozilla.io;

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import com.nokia.mid.s40.io.LocalMessageProtocolMessage;
import com.nokia.mid.s40.io.LocalMessageProtocolConnection;
import com.nokia.mid.s40.codec.DataEncoder;
import com.nokia.mid.s40.codec.DataDecoder;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import gnu.testlet.TestUtils;

public class TestNokiaImageProcessingServer implements Testlet {
    public int getExpectedPass() { return 34; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    LocalMessageProtocolConnection client;
    static final String PROTO_NAME = "nokia.image-processing";

    public void testProtocolVersion(TestHarness th) throws IOException {
        DataEncoder dataEncoder = new DataEncoder("Conv-BEB");
        dataEncoder.putStart(14, "event");
        dataEncoder.put(13, "name", "Common");
        dataEncoder.putStart(14, "message");
        dataEncoder.put(13, "name", "ProtocolVersion");
        dataEncoder.put(10, "version", "1.[0-10]");
        dataEncoder.putEnd(14, "message");
        dataEncoder.putEnd(14, "event");
        byte[] sendData = dataEncoder.getData();
        client.send(sendData, 0, sendData.length);

        LocalMessageProtocolMessage msg = client.newMessage(null);
        client.receive(msg);
        byte[] clientData = msg.getData();

        DataDecoder dataDecoder = new DataDecoder("Conv-BEB", clientData, 0, clientData.length);
        dataDecoder.getStart(14);
        String name = dataDecoder.getString(13);
        th.check(name, "Common");
        th.check(dataDecoder.getName(), "message");
        dataDecoder.getStart(14);
        String string2 = dataDecoder.getString(13);
        String string = string2 + ":" + dataDecoder.getString(10);
        dataDecoder.getEnd(14);
        th.check(string.startsWith("ProtocolVersion:"));
        th.check(string.indexOf(58) + 1 != -1);
        th.check(string.substring(string.indexOf(58) + 1).length() > 0);
    }

    public void testScaleImage(TestHarness th, int maxKb, int maxHres, int maxVres) throws IOException {
        // Store an image in the fs
        FileConnection originalImage = (FileConnection)Connector.open("file:////test.jpg", Connector.READ_WRITE);
        if (!originalImage.exists()) {
            originalImage.create();
        }

        OutputStream os = originalImage.openDataOutputStream();
        InputStream is = getClass().getResourceAsStream("test.jpg");
        os.write(TestUtils.read(is));
        os.close();

        long origFileSize = originalImage.fileSize();

        DataEncoder dataEncoder = new DataEncoder("Conv-BEB");
        dataEncoder.putStart(14, "event");
        dataEncoder.put(13, "name", "Scale");
        dataEncoder.put(2, "trans_id", 42);
        dataEncoder.put(11, "filename", "test.jpg");
        dataEncoder.putStart(15, "limits");
        if (maxVres > 0) {
            dataEncoder.put(5, "max_vres", maxVres);
        }
        if (maxKb > 0) {
            dataEncoder.put(5, "max_kb", maxKb);
        }
        if (maxHres > 0) {
            dataEncoder.put(5, "max_hres", maxHres);
        }
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

        if (maxKb > 0) {
            long scaledFileSize = file.fileSize();
            if (origFileSize > maxKb * 1024) {
                th.check(origFileSize > scaledFileSize, "Image is scaled.");
            } else {
                if ((maxHres > 0 && maxHres < 195 /*  height of test.jpg */ ) ||
                    (maxVres > 0 && maxVres < 195 /*  width of test.jpg */ )) {
                    th.check(origFileSize > scaledFileSize, "Image is scaled.");
                } else {
                    th.check(origFileSize == scaledFileSize, "Image is not scaled");
                }
            }
        }

        file.delete();
        file.close();

        th.check(originalImage.exists(), "Original image has been deleted");
        originalImage.delete();
        originalImage.close();
    }

    public void test(TestHarness th) {
       try {
            client = (LocalMessageProtocolConnection)Connector.open("localmsg://" + PROTO_NAME);

            testProtocolVersion(th);
            testScaleImage(th, 0, 100, 100);
            testScaleImage(th, 1, 0, 0);
            testScaleImage(th, 10000, 0, 0);
            testScaleImage(th, 10000, 101, 101);
            testScaleImage(th, 10000, 10000, 10000);

            client.close();
       } catch (IOException ioe) {
            th.fail("Unexpected exception");
            ioe.printStackTrace();
       }
    }
}

