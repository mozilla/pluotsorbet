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

public class TestNokiaImageProcessingServer implements Testlet {
    LocalMessageProtocolConnection client;
    static final String PROTO_NAME = "nokia.phone-status";

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

    public void testScaleImage(TestHarness th) throws IOException {
        // Store an image in the fs
        FileConnection originalImage = (FileConnection)Connector.open(path, Connector.READ_WRITE);
        if (!originalImage.exists()) {
            originalImage.create();
        }
        OutputStream os = originalImage.openDataOutputStream();

        DataEncoder dataEncoder = new DataEncoder("Conv-BEB");
        dataEncoder.putStart(14, "event");
        dataEncoder.put(13, "name", "Scale");
        dataEncoder.put(2, "trans_id", 42);
        dataEncoder.put(11, "filename", ???);
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
        String path = "file:///" + dataDecoder.getString(11);

        FileConnection file = (FileConnection)Connector.open(path);
        if (!file.exists()) {
            System.out.println("FAIL - File doesn't exist");
        }
        file.delete();
        file.close();

        if (!originalImage.exists()) {
            System.out.println("FAIL - Original image has been deleted");
        }
        originalImage.delete();
        originalImage.close();
    }

    public void test(TestHarness th) {
       try {
            client = (LocalMessageProtocolConnection)Connector.open("localmsg://" + PROTO_NAME);

            testProtocolVersion(th);
            //testScaleImage(th);

            client.close();
       } catch (IOException ioe) {
            th.fail("Unexpected exception");
            ioe.printStackTrace();
       }
    }
}

