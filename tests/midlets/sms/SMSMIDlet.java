package tests.sms;

import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;
import javax.microedition.midlet.*;

public class SMSMIDlet extends MIDlet {
    public void startApp() {
        try {
            MessageConnection conn = (MessageConnection)Connector.open("sms://:5000");
            TextMessage message = (TextMessage)conn.receive();
            System.out.println("Message: " + message.getPayloadText());
            System.out.println("From: " + message.getAddress());
        } catch (Exception e) {
            System.out.println("Error: " + e);
            e.printStackTrace();
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
};
