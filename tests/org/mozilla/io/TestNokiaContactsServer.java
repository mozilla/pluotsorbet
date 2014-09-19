package org.mozilla.io;

import java.io.*;
import javax.microedition.io.*;
import com.nokia.mid.s40.io.LocalMessageProtocolMessage;
import com.nokia.mid.s40.io.LocalMessageProtocolConnection;
import com.nokia.mid.s40.codec.DataEncoder;
import com.nokia.mid.s40.codec.DataDecoder;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestNokiaContactsServer implements Testlet {
    LocalMessageProtocolConnection client;
    static final String PROTO_NAME = "nokia.contacts";

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

    public void testNotifySubscribe(TestHarness th) throws IOException {
        DataEncoder dataEncoder = new DataEncoder("Conv-BEB");
        dataEncoder.putStart(14, "event");
        dataEncoder.put(13, "name", "NotifySubscribe");
        dataEncoder.put(7, "trans_id", System.currentTimeMillis());
        dataEncoder.putEnd(14, "event");
        byte[] sendData = dataEncoder.getData();
        client.send(sendData, 0, sendData.length);

        LocalMessageProtocolMessage msg = client.newMessage(null);
        client.receive(msg);
        byte[] clientData = msg.getData();

        DataDecoder dataDecoder = new DataDecoder("Conv-BEB", clientData, 0, clientData.length);
        dataDecoder.getStart(14);
        th.check(dataDecoder.getString(13), "Notify");
        dataDecoder.getInteger(7);
        byte by = (byte)dataDecoder.getInteger(2);
        th.check(by == 1 || by == 2 || by == 3); // We should understand the meaning of this byte
        th.check(dataDecoder.getName(), "Contact");
        dataDecoder.getStart(15);

        th.check(dataDecoder.listHasMoreItems());
        th.check(dataDecoder.getName(), "ContactID"); // mozContact.id
        th.check(dataDecoder.getString(11).length() > 0);

        //th.check(dataDecoder.listHasMoreItems());
        //th.check(dataDecoder.getName(), "FirstName"); // mozContact.givenName [array]
        //th.check(dataDecoder.getString(11), "Test");

        //th.check(dataDecoder.listHasMoreItems());
        //th.check(dataDecoder.getName(), "LastName"); // mozContact.familyName [array]
        //th.check(dataDecoder.getString(11), "Contact");

        //th.check(dataDecoder.listHasMoreItems());
        //th.check(dataDecoder.getName(), "NickName"); // mozContact.nickname [array]
        //th.check(dataDecoder.getString(11), "");

        th.check(dataDecoder.listHasMoreItems());
        th.check(dataDecoder.getName(), "DisplayName"); // mozContact.name [array]
        th.check(dataDecoder.getString(11), "Test Contact 1");

        //th.check(dataDecoder.listHasMoreItems());
        //th.check(dataDecoder.getName(), "Title"); // mozContact.jobTitle [array]
        //th.check(dataDecoder.getString(11), "");

        //th.check(dataDecoder.listHasMoreItems());
        //th.check(dataDecoder.getName(), "Company"); // mozContact.org [array]
        //th.check(dataDecoder.getString(11), "");

        //th.check(dataDecoder.listHasMoreItems());
        //th.check(dataDecoder.getName(), "Notes"); // mozContact.note [array]
        //th.check(dataDecoder.getString(11), "");

        // groupnames (???)

        th.check(dataDecoder.listHasMoreItems());
        th.check(dataDecoder.getName(), "Numbers"); // mozContact.tel [array]
        dataDecoder.getStart(16);
        dataDecoder.getStart(15);
        th.check(dataDecoder.listHasMoreItems());
        th.check(dataDecoder.getName(), "Number");
        th.check(dataDecoder.getString(11), "+16505550100");
        th.check(!dataDecoder.listHasMoreItems());
        dataDecoder.getEnd(15);
        dataDecoder.getStart(15);
        th.check(dataDecoder.listHasMoreItems());
        th.check(dataDecoder.getName(), "Number");
        th.check(dataDecoder.getString(11), "+16505550101");
        th.check(!dataDecoder.listHasMoreItems());
        dataDecoder.getEnd(15);
        try {
            dataDecoder.getStart(15);
        } catch (IOException e) {
            th.check(e.getMessage(), "no start found 15");
        }

        dataDecoder.getEnd(16);

        //th.check(dataDecoder.listHasMoreItems());
        //th.check(dataDecoder.getName(), "DefaultNumber"); // mozContact.tel [array]
        // get default numbers

        //th.check(dataDecoder.listHasMoreItems());
        //th.check(dataDecoder.getName(), "DefaultEmail"); // mozContact.email [array]
        //th.check(dataDecoder.getString(11), "");

        //th.check(dataDecoder.listHasMoreItems());
        //th.check(dataDecoder.getName(), "Emails"); // mozContact.email [array]
        // get emails

        // source (???)

        // pictureurl (???)

        // mozContact.url [array]
        //th.check(dataDecoder.listHasMoreItems());
        //th.check(dataDecoder.getName(), "WebURL"); // mozContact.url [array]
        //th.check(dataDecoder.getString(11), "");

        // ringtoneurl (???)

        // animatedtoneurl (???)

        //th.check(dataDecoder.listHasMoreItems());
        //th.check(dataDecoder.getName(), "PostalAddresses"); // mozContact.adr [array]
        // get postal addresses

        //th.check(dataDecoder.listHasMoreItems());
        //th.check(dataDecoder.getName(), "OtherAddresses"); // mozContact.adr [array]
        // get other addresses

        //th.check(dataDecoder.listHasMoreItems());
        //th.check(dataDecoder.getName(), "Birthday"); // mozContact.bday
        // get birthday

        //th.check(dataDecoder.listHasMoreItems());
        //th.check(dataDecoder.getName(), "Anniversary"); // mozContact.anniversary
        // get anniversary

        // imaddresses (mozContact.impp [array])

        //th.check(dataDecoder.listHasMoreItems());
        //th.check(dataDecoder.getName(), "IMAddresses"); // mozContact.impp [array]
        // get IM addresses

        dataDecoder.getEnd(15);
    }

    public void test(TestHarness th) {
        try {
            client = (LocalMessageProtocolConnection)Connector.open("localmsg://"+PROTO_NAME);

            testProtocolVersion(th);
            testNotifySubscribe(th);
            //testGetFirst(th); testGetNext(th);

            client.close();
        } catch (IOException ioe) {
            th.fail("Unexpected exception");
            ioe.printStackTrace();
        }
    }
}
