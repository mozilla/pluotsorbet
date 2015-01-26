package org.mozilla.io;

import java.io.*;
import javax.microedition.io.*;
import com.nokia.mid.s40.io.LocalMessageProtocolMessage;
import com.nokia.mid.s40.io.LocalMessageProtocolConnection;
import com.nokia.mid.s40.codec.DataEncoder;
import com.nokia.mid.s40.codec.DataDecoder;
import com.nokia.mid.ui.lcdui.Indicator;
import com.nokia.mid.ui.lcdui.IndicatorManager;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import gnu.testlet.TestUtils;

public class TestNokiaActiveStandbyServer implements Testlet {
    LocalMessageProtocolConnection client;

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

    public void testRegister(TestHarness th) throws IOException {
        DataEncoder dataEncoder = new DataEncoder("Conv-BEB");
        dataEncoder.put(13, "name", "Register");
        dataEncoder.put(10, "client_id", "aClientID");
        dataEncoder.put(11, "personalise_view_text", "Title");
        dataEncoder.put(0, "activate_scroll_events", true);
        dataEncoder.putEnd(14, "event");
        byte[] sendData = dataEncoder.getData();
        client.send(sendData, 0, sendData.length);

        LocalMessageProtocolMessage msg = client.newMessage(null);
        client.receive(msg);
        byte[] clientData = msg.getData();

        DataDecoder dataDecoder = new DataDecoder("Conv-BEB", clientData, 0, clientData.length);
        dataDecoder.getStart(14);
        th.check(dataDecoder.getString(13), "Register");
        th.check(dataDecoder.getString(11), "aClientID");
        th.check(dataDecoder.getString(10), "OK");

        msg = client.newMessage(null);
        client.receive(msg);
        clientData = msg.getData();
        dataDecoder = new DataDecoder("Conv-BEB", clientData, 0, clientData.length);
        dataDecoder.getStart(14);
        th.check(dataDecoder.getString(13), "Activated");
        th.check(dataDecoder.getString(11), "aClientID");
        dataDecoder.getStart(15);
        dataDecoder.getEnd(15);
        dataDecoder.getInteger(2);
        dataDecoder.getInteger(4);
        dataDecoder.getInteger(4);
    }

    public void testUpdate(TestHarness th) throws IOException {
        InputStream is = getClass().getResourceAsStream("/gfx/images/FirefoxLogo.png");
        byte[] icon = TestUtils.read(is);

        DataEncoder dataEncoder = new DataEncoder("Conv-BEB");
        dataEncoder.put(13, "name", "Update");
        dataEncoder.put(10, "client_id", "aClientID");
        dataEncoder.put(11, "personalise_view_text", "Title");
        dataEncoder.put(0, "activate_scroll_events", true);
        dataEncoder.put("content_icon", icon, icon.length);
        dataEncoder.put(10, "mime_type", "image/png");
        dataEncoder.put(11, "context_text", "Notification text");
        dataEncoder.putEnd(14, "event");
        byte[] sendData = dataEncoder.getData();
        client.send(sendData, 0, sendData.length);

        LocalMessageProtocolMessage msg = client.newMessage(null);
        client.receive(msg);
        byte[] clientData = msg.getData();

        DataDecoder dataDecoder = new DataDecoder("Conv-BEB", clientData, 0, clientData.length);
        dataDecoder.getStart(14);
        th.check(dataDecoder.getString(13), "Update");
        th.check(dataDecoder.getString(11), "aClientID");
        th.check(dataDecoder.getString(10), "OK");
    }

    public void test(TestHarness th) {
       try {
            client = (LocalMessageProtocolConnection)Connector.open("localmsg://nokia.active-standby");

            Indicator indicator = new Indicator(0, null);
            IndicatorManager indicatorManager = IndicatorManager.getIndicatorManager();
            indicatorManager.appendIndicator(indicator, false);

            indicator.setActive(true);

            testProtocolVersion(th);
            testRegister(th);
            testUpdate(th);

            indicator.setActive(false);

            client.close();
       } catch (IOException ioe) {
            th.fail("Unexpected exception: " + ioe);
            ioe.printStackTrace();
       }
    }
}

