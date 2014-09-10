package org.mozilla.io;

import java.io.*;
import javax.microedition.io.*;
import com.nokia.mid.s40.io.LocalMessageProtocolMessage;
import com.nokia.mid.s40.io.LocalMessageProtocolConnection;
import com.nokia.mid.s40.codec.DataEncoder;
import com.nokia.mid.s40.codec.DataDecoder;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestNokiaServer implements Testlet {
    LocalMessageProtocolConnection client;
    static final String PROTO_NAME = "nokia.messaging";

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

        // Client receives data
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
        System.out.println(string.substring(string.indexOf(58) + 1));
        th.check(string.substring(string.indexOf(58) + 1).length() > 0);
    }

    public void testSubscribeMessages(TestHarness th) throws IOException {
        DataEncoder dataEncoder = new DataEncoder("Conv-BEB");
        dataEncoder.putStart(14, "event");
        dataEncoder.put(13, "name", "SubscribeMessages");
        dataEncoder.put(5, "trans_id", (long)(short)(System.currentTimeMillis() % 255));
        dataEncoder.putEnd(14, "event");
        byte[] sendData = dataEncoder.getData();
        
        client.send(sendData, 0, sendData.length);
        
        // Client receives data
        LocalMessageProtocolMessage msg = client.newMessage(null);
        client.receive(msg);
        byte[] clientData = msg.getData();
        
        DataDecoder dataDecoder = new DataDecoder("Conv-BEB", clientData, 0, clientData.length);
        dataDecoder.getStart(14);
        th.check(dataDecoder.getString(13).toLowerCase(), "subscribemessages");
        dataDecoder.getInteger(5);
        th.check(dataDecoder.getString(10).toLowerCase(), "ok");

        // TO REMOVE
        msg = client.newMessage(null);
        client.receive(msg);
        clientData = msg.getData();
        dataDecoder = new DataDecoder("Conv-BEB", clientData, 0, clientData.length);
        dataDecoder.getStart(14);
        th.check(dataDecoder.getString(13).toLowerCase(), "messagenotify");
        long message_id = dataDecoder.getInteger(7);
        th.check(dataDecoder.getString(10), "SMS");

        dataEncoder = new DataEncoder("Conv-BEB");
        dataEncoder.putStart(14, "event");
        dataEncoder.put(13, "name", "GetMessageEntity");
        dataEncoder.put(5, "trans_id", (long)(short)(System.currentTimeMillis() % 255));
        dataEncoder.put(7, "message_id", message_id);
        dataEncoder.putStart(16, "entries");
        dataEncoder.put(10, "entity_element", "body_text");
        dataEncoder.put(10, "entity_element", "address");
        dataEncoder.putEnd(16, "entries");
        dataEncoder.putEnd(14, "event");
        sendData = dataEncoder.getData();
        client.send(sendData, 0, sendData.length);

        msg = client.newMessage(null);
        client.receive(msg);
        clientData = msg.getData();
        dataDecoder = new DataDecoder("Conv-BEB", clientData, 0, clientData.length);
        dataDecoder.getStart(14);
        th.check(dataDecoder.getString(13).toLowerCase(), "getmessageentity");
        dataDecoder.getInteger(5);
        th.check(dataDecoder.getString(10).toLowerCase(), "ok");
        long message_id_2 = dataDecoder.getInteger(7);
        dataDecoder.getStart(15);
        String sms_text = dataDecoder.getString(11);
        dataDecoder.getString(10);
        
        dataEncoder = new DataEncoder("Conv-BEB");
        dataEncoder.putStart(14, "event");
        dataEncoder.put(13, "name", "DeleteMessages");
        dataEncoder.put(5, "trans_id", (long)(short)(System.currentTimeMillis() % 255));
        dataEncoder.putStart(16, "entries");
        dataEncoder.put(7, "message_id", message_id_2);
        dataEncoder.putEnd(16, "entries");
        dataEncoder.putEnd(14, "event");
        sendData = dataEncoder.getData();
        client.send(sendData, 0, sendData.length);
    }

    public void test(TestHarness th) {
        try {
            client = (LocalMessageProtocolConnection)Connector.open("localmsg://"+PROTO_NAME);

            testProtocolVersion(th);
            testSubscribeMessages(th);

            client.close();
        } catch (IOException ioe) {
            th.fail("Unexpected exception");
            ioe.printStackTrace();
        }
    }
}
