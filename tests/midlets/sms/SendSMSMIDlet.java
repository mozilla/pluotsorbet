package tests.sms;

import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;
import javax.microedition.midlet.*;

public class SendSMSMIDlet extends MIDlet {
    public void startApp() {
        try {
            MessageConnection conn = (MessageConnection)Connector.open("sms://3393333333");

            TextMessage msg = (TextMessage)conn.newMessage(MessageConnection.TEXT_MESSAGE);
            msg.setPayloadText("SMS nuntius");

            conn.send(msg);
            conn.close();
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
