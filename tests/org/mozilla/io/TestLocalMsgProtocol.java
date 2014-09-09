package org.mozilla.io;

import java.io.*;
import javax.microedition.io.*;
import com.nokia.mid.s40.io.LocalMessageProtocolMessage;
import com.nokia.mid.s40.io.LocalMessageProtocolConnection;
import com.nokia.mid.s40.io.LocalMessageProtocolServerConnection;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestLocalMsgProtocol implements Testlet {
    LocalMessageProtocolServerConnection server;
    LocalMessageProtocolConnection client;
    static final String PROTO_NAME = "marco";

    public void testServerSendsClientReceives(TestHarness th) throws IOException {
        // Server sends data
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        dataOutputStream.writeByte(5);
        dataOutputStream.writeInt(16491);
        byte[] serverData = byteArrayOutputStream.toByteArray();
        ((LocalMessageProtocolConnection)server).send(serverData, 0, serverData.length);
        dataOutputStream.close();
        byteArrayOutputStream.close();

        // Client receives data
        byte[] clientData = new byte[5];
        client.receive((byte[])clientData);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream((byte[])clientData);
        DataInputStream dataInputStream = new DataInputStream((InputStream)byteArrayInputStream);
        byte by = dataInputStream.readByte();
        int n = dataInputStream.readInt();
        th.check(by, 5);
        th.check(n, 16491);
        dataInputStream.close();
        byteArrayInputStream.close();
    }

    public void testClientSendsServerReceives(TestHarness th) throws IOException {
        // Client sends data
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        dataOutputStream.writeByte(9);
        dataOutputStream.writeInt(24891);
        byte[] clientData = byteArrayOutputStream.toByteArray();
        client.send(clientData, 0, clientData.length);
        dataOutputStream.close();
        byteArrayOutputStream.close();

        // Server receives data
        byte[] serverData = new byte[5];
        ((LocalMessageProtocolConnection)server).receive((byte[])serverData);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream((byte[])serverData);
        DataInputStream dataInputStream = new DataInputStream((InputStream)byteArrayInputStream);
        byte by = dataInputStream.readByte();
        int n = dataInputStream.readInt();
        th.check(by, 9);
        th.check(n, 24891);
        dataInputStream.close();
        byteArrayInputStream.close();
    }

    public void testServerSendsClientReceives2(TestHarness th) throws IOException {
        // Server sends data
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        dataOutputStream.writeByte(5);
        dataOutputStream.writeInt(16491);
        byte[] serverData = byteArrayOutputStream.toByteArray();
        ((LocalMessageProtocolConnection)server).send(serverData, 0, serverData.length);
        dataOutputStream.close();
        byteArrayOutputStream.close();

        // Client receives data
        LocalMessageProtocolMessage msg = client.newMessage(null);
        client.receive(msg);
        th.check(msg.getLength(), 5);
        byte[] clientData = msg.getData();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream((byte[])clientData);
        DataInputStream dataInputStream = new DataInputStream((InputStream)byteArrayInputStream);
        byte by = dataInputStream.readByte();
        int n = dataInputStream.readInt();
        th.check(by, 5);
        th.check(n, 16491);
        dataInputStream.close();
        byteArrayInputStream.close();
    }

    public void testClientSendsServerReceives2(TestHarness th) throws IOException {
        // Client sends data
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        dataOutputStream.writeByte(9);
        dataOutputStream.writeInt(24891);
        byte[] clientData = byteArrayOutputStream.toByteArray();
        client.send(clientData, 0, clientData.length);
        dataOutputStream.close();
        byteArrayOutputStream.close();

        // Server receives data
        LocalMessageProtocolMessage msg = ((LocalMessageProtocolConnection)server).newMessage(null);
        ((LocalMessageProtocolConnection)server).receive(msg);
        th.check(msg.getLength(), 5);
        byte[] serverData = msg.getData();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream((byte[])serverData);
        DataInputStream dataInputStream = new DataInputStream((InputStream)byteArrayInputStream);
        byte by = dataInputStream.readByte();
        int n = dataInputStream.readInt();
        th.check(by, 9);
        th.check(n, 24891);
        dataInputStream.close();
        byteArrayInputStream.close();
    }

    public void test(TestHarness th) {
        try {
            server = (LocalMessageProtocolServerConnection)Connector.open("localmsg://:"+PROTO_NAME);

            client = (LocalMessageProtocolConnection)Connector.open("localmsg://"+PROTO_NAME);

            testServerSendsClientReceives(th);
            testClientSendsServerReceives(th);

            testServerSendsClientReceives2(th);
            testClientSendsServerReceives2(th);

            client.close();
            server.close();
        } catch (IOException ioe) {
            th.fail("Unexpected exception");
            ioe.printStackTrace();
        }
    }
}
