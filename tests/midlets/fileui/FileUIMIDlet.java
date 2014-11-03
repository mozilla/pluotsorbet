package tests.fileui;

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import com.nokia.mid.s40.io.LocalMessageProtocolMessage;
import com.nokia.mid.s40.io.LocalMessageProtocolConnection;
import com.nokia.mid.s40.codec.DataEncoder;
import com.nokia.mid.s40.codec.DataDecoder;
import javax.microedition.midlet.*;

public class FileUIMIDlet extends MIDlet {
    public void startApp() {
        System.out.println("START");

        try {
            LocalMessageProtocolConnection client = (LocalMessageProtocolConnection)Connector.open("localmsg://nokia.file-ui");

            // Send protocol version message
            DataEncoder dataEncoder = new DataEncoder("Conv-BEB");
            dataEncoder.putStart(14, "event");
            dataEncoder.put(13, "name", "Common");
            dataEncoder.putStart(14, "message");
            dataEncoder.put(13, "name", "ProtocolVersion");
            dataEncoder.put(10, "version", "1.0");
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

            // Open native file dialog
            dataEncoder = new DataEncoder("Conv-BEB");
            dataEncoder.putStart(14, "event");
            dataEncoder.put(13, "name", "FileSelect");
            dataEncoder.put(5, "trans_id", (short)(int)(System.currentTimeMillis() % 255L));
            dataEncoder.putEnd(14, "event");
            dataEncoder.put(10, "memory", "Internal");
            dataEncoder.put(10, "media_type", "Picture");
            dataEncoder.put(0, "multiple_selection", false);
            dataEncoder.put(10, "starting_url", null);
            dataEncoder.putEnd(14, "event");
            sendData = dataEncoder.getData();

            client.send(sendData, 0, sendData.length);

            // Receive OK message
            msg = client.newMessage(null);
            client.receive(msg);
            client.close();
            clientData = msg.getData();

            dataDecoder = new DataDecoder("Conv-BEB", clientData, 0, clientData.length);
            dataDecoder.getStart(14);
            if (!dataDecoder.getString(13).toLowerCase().equals("fileselect")) {
                System.out.println("FAIL - Expected 'fileselect'");
            }
            dataDecoder.getInteger(5);
            if (!dataDecoder.getString(10).toLowerCase().equals("ok")) {
                System.out.println("FAIL - Expected 'ok'");
            }
            dataDecoder.getStart(16);
            dataDecoder.getStart(14);
            dataDecoder.getString(10);
            dataDecoder.getString(11);
            String path = "file:///" + dataDecoder.getString(11);
            dataDecoder.getBoolean();
            dataDecoder.getInteger(7);
            dataDecoder.getEnd(14);
            dataDecoder.getEnd(16);

            FileConnection file = (FileConnection)Connector.open(path);
            if (!file.exists()) {
                System.out.println("FAIL - File doesn't exist");
            }
            file.delete();
            file.close();
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
