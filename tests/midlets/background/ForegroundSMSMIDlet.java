package tests.midlets.background;

import javax.microedition.io.Connector;
import com.nokia.mid.s40.io.LocalMessageProtocolMessage;
import com.nokia.mid.s40.io.LocalMessageProtocolConnection;
import com.nokia.mid.s40.codec.DataEncoder;
import javax.microedition.midlet.MIDlet;

public class ForegroundSMSMIDlet extends MIDlet {
    native boolean startedBackgroundAlarm();

    public void receiveSMS() {
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

            // Wait for a new message to arrive
            msg = client.newMessage(null);
            client.receive(msg);

            client.close();
        } catch (Exception e) {
            System.out.println("FAIL - Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    public void startApp() {
        System.out.println("START - Background alarm started: " + startedBackgroundAlarm());

        receiveSMS();

        System.out.println("DONE - Background alarm started: " + startedBackgroundAlarm());
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
};
