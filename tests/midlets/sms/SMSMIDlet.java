package tests.sms;

import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;
import java.io.*;
import javax.microedition.io.*;
import com.nokia.mid.s40.io.LocalMessageProtocolMessage;
import com.nokia.mid.s40.io.LocalMessageProtocolConnection;
import com.nokia.mid.s40.codec.DataEncoder;
import com.nokia.mid.s40.codec.DataDecoder;
import javax.microedition.midlet.*;

class J2MEAPIClass implements Runnable {
    public void run() {
        try {
            MessageConnection conn = (MessageConnection)Connector.open("sms://:5000");
            TextMessage message = (TextMessage)conn.receive();

            if (!message.getPayloadText().equals("Prova SMS")) {
                System.out.println("FAIL - Wrong SMS text: " + message.getPayloadText());
            }

            if (!message.getAddress().equals("sms://unknown:5000")) {
                System.out.println("FAIL - Wrong SMS address: " + message.getAddress());
            }
        } catch (Exception e) {
            System.out.println("FAIL - Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}

class NokiaAPIClass implements Runnable {
    public void run() {
        try {
            System.out.println("START");

            LocalMessageProtocolConnection client = (LocalMessageProtocolConnection)Connector.open("localmsg://nokia.messaging");

            // Send protocol version message
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

            // Receive protocol version message
            LocalMessageProtocolMessage msg = client.newMessage(null);
            client.receive(msg);
            byte[] clientData = msg.getData();

            DataDecoder dataDecoder = new DataDecoder("Conv-BEB", clientData, 0, clientData.length);
            dataDecoder.getStart(14);
            String name = dataDecoder.getString(13);
            if (!name.equals("Common")) {
                System.out.println("FAIL - Expected 'Common', got " + name);
            }
            String struct_name = dataDecoder.getName();
            if (!struct_name.equals("message")) {
                System.out.println("FAIL - Expected 'message', got " + struct_name);
            }
            dataDecoder.getStart(14);
            String string2 = dataDecoder.getString(13);
            String string = string2 + ":" + dataDecoder.getString(10);
            dataDecoder.getEnd(14);
            if (!string.startsWith("ProtocolVersion:")) {
                System.out.println("FAIL - Expected 'ProtocolVersion:'");
            }
            if (string.indexOf(58) + 1 == -1) {
                System.out.println("FAIL");
            }
            if (string.substring(string.indexOf(58) + 1).length() <= 0) {
                System.out.println("FAIL - Version string length should be > 0");
            }

            // Subscribe for SMS messages
            dataEncoder = new DataEncoder("Conv-BEB");
            dataEncoder.putStart(14, "event");
            dataEncoder.put(13, "name", "SubscribeMessages");
            dataEncoder.put(5, "trans_id", (long)(short)(System.currentTimeMillis() % 255));
            dataEncoder.putEnd(14, "event");
            sendData = dataEncoder.getData();

            client.send(sendData, 0, sendData.length);

            // Receive subscription OK message
            msg = client.newMessage(null);
            client.receive(msg);
            clientData = msg.getData();

            dataDecoder = new DataDecoder("Conv-BEB", clientData, 0, clientData.length);
            dataDecoder.getStart(14);
            if (!dataDecoder.getString(13).toLowerCase().equals("subscribemessages")) {
                System.out.println("FAIL - Expected 'subscribemessages'");
            }
            dataDecoder.getInteger(5);
            if (!dataDecoder.getString(10).toLowerCase().equals("ok")) {
                System.out.println("FAIL - Expected 'ok'");
            }

            // Wait for a new message to arrive
            msg = client.newMessage(null);
            client.receive(msg);
            clientData = msg.getData();
            dataDecoder = new DataDecoder("Conv-BEB", clientData, 0, clientData.length);
            dataDecoder.getStart(14);
            if (!dataDecoder.getString(13).toLowerCase().equals("messagenotify")) {
                System.out.println("FAIL - Expected 'messagenotify'");
            }
            dataDecoder.getInteger(5);
            if (!dataDecoder.getString(10).equals("SMS")) {
                System.out.println("FAIL - Expected 'SMS'");
            }
            long message_id = dataDecoder.getInteger(7);

            // Ask for message details
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

            // Receive message details
            msg = client.newMessage(null);
            client.receive(msg);
            clientData = msg.getData();
            dataDecoder = new DataDecoder("Conv-BEB", clientData, 0, clientData.length);
            dataDecoder.getStart(14);
            if (!dataDecoder.getString(13).toLowerCase().equals("getmessageentity")) {
                System.out.println("FAIL - Expected 'getmessageentity'");
            }
            dataDecoder.getInteger(5);
            if (!dataDecoder.getString(10).toLowerCase().equals("ok")) {
                System.out.println("FAIL - Expected 'ok'");
            }
            long message_id_2 = dataDecoder.getInteger(7);
            if (message_id != message_id_2) {
                System.out.println("FAIL - Message ID should be the same");
            }
            dataDecoder.getStart(15);
            String sms_text = dataDecoder.getString(11);
            if (!sms_text.equals("Prova SMS")) {
                System.out.println("FAIL - Wrong SMS text: " + sms_text);
            }
            dataDecoder.getString(10);

            // Delete message
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

            client.close();
        } catch (Exception e) {
            System.out.println("FAIL - Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}

public class SMSMIDlet extends MIDlet {
    public void startApp() {
        Thread first = new Thread(new J2MEAPIClass());
        first.start();
        Thread second = new Thread(new NokiaAPIClass());
        second.start();

        try {
            first.join();
            second.join();
        } catch (Exception e) {
            System.out.println("FAIL - Unexpected exception: " + e);
            e.printStackTrace();
        }

        System.out.println("DONE");
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
};
