package org.mozilla.io;

import java.io.*;
import javax.microedition.io.*;
import com.nokia.mid.s40.io.LocalMessageProtocolMessage;
import com.nokia.mid.s40.io.LocalMessageProtocolConnection;
import com.nokia.mid.s40.codec.DataEncoder;
import com.nokia.mid.s40.codec.DataDecoder;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestNokiaProductInfoServer implements Testlet {
    LocalMessageProtocolConnection client;
    static final String PROTO_NAME = "nokia.product-info";

    public void testProtocolVersion(TestHarness th) throws IOException {
        DataEncoder dataEncoder = new DataEncoder("Conv-BEB");
        dataEncoder.putStart(14, "event");
        dataEncoder.put(13, "name", "Common");
        dataEncoder.putStart(14, "message");
        dataEncoder.put(13, "name", "ProtocolVersion");
        dataEncoder.put(10, "version", "2.[0-10]");
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

    public void testReadProductInfo(TestHarness th) throws IOException {
        DataEncoder dataEncoder = new DataEncoder("Conv-BEB");
        dataEncoder.putStart(14, "event");
        dataEncoder.put(13, "name", "ReadProductInfo");
        dataEncoder.putEnd(14, "event");
        byte[] sendData = dataEncoder.getData();
        client.send(sendData, 0, sendData.length);

        LocalMessageProtocolMessage msg = client.newMessage(null);
        client.receive(msg);
        byte[] clientData = msg.getData();

        DataDecoder dataDecoder = new DataDecoder("Conv-BEB", clientData, 0, clientData.length);
        dataDecoder.getStart(14);
        th.check(dataDecoder.getString(13), "ReadProductInfo");
        th.check(dataDecoder.getString(10), "OK");
        th.check(dataDecoder.getString(10), "");
        th.check(dataDecoder.getString(10), "");
        th.check(dataDecoder.getString(10), "");
        th.check(dataDecoder.getString(10), "");
        th.check(dataDecoder.getString(10), "");
        dataDecoder.getEnd(14);
    }

    public void test(TestHarness th) {
       try {
            client = (LocalMessageProtocolConnection)Connector.open("localmsg://" + PROTO_NAME);

            testProtocolVersion(th);
            testReadProductInfo(th);

            client.close();
       } catch (IOException ioe) {
            th.fail("Unexpected exception");
            ioe.printStackTrace();
       }
    }
}

