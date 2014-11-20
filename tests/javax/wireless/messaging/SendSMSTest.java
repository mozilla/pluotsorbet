package javax.wireless.messaging;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

public class SendSMSTest implements Testlet {
    native String getNumber();
    native String getBody();

    public void test(TestHarness th) {
        try {
            MessageConnection conn = (MessageConnection)Connector.open("sms://3393333333");

            TextMessage msg = (TextMessage)conn.newMessage(MessageConnection.TEXT_MESSAGE);
            msg.setPayloadText("SMS nuntius");

            th.check(conn.numberOfSegments(msg), 1);

            conn.send(msg);

            th.check(getNumber(), "3393333333");
            th.todo(getBody(), "SMS nuntius");

            conn.close();
        } catch (Exception e) {
            th.fail("FAIL - Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
};
