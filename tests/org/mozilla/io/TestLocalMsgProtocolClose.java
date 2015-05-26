package org.mozilla.io;

import java.io.*;
import javax.microedition.io.*;
import com.nokia.mid.s40.io.LocalMessageProtocolMessage;
import com.nokia.mid.s40.io.LocalMessageProtocolConnection;
import com.nokia.mid.s40.io.LocalMessageProtocolServerConnection;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestLocalMsgProtocolClose implements Testlet {
    public int getExpectedPass() { return 16; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    LocalMessageProtocolServerConnection server;
    LocalMessageProtocolConnection client;
    static final String PROTO_NAME = "marco";
    TestHarness th;

    public void serverSendData() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        dataOutputStream.writeByte(5);
        dataOutputStream.writeInt(16491);
        byte[] serverData = byteArrayOutputStream.toByteArray();
        ((LocalMessageProtocolConnection)server).send(serverData, 0, serverData.length);
        dataOutputStream.close();
        byteArrayOutputStream.close();
    }

    public void clientReceiveData() throws IOException {
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

    public void clientSendData() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        dataOutputStream.writeByte(9);
        dataOutputStream.writeInt(24891);
        byte[] clientData = byteArrayOutputStream.toByteArray();
        client.send(clientData, 0, clientData.length);
        dataOutputStream.close();
        byteArrayOutputStream.close();
    }

    public void serverReceiveData() throws IOException {
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

    class ServerThread extends Thread {
        public void run() {
            try {
                int step = 0;

                while (true) {
                    LocalMessageProtocolConnection conn = server.acceptAndOpen();

                    try {
                        serverReceiveData();
                        serverSendData();
                    } catch (IOException e) {
                        th.fail("Unexpected exception: " + e);
                        return;
                    }

                    if (++step == 2) {
                        return;
                    }

                    try {
                        serverReceiveData();
                        th.fail("Expected IOException");
                    } catch (IOException e) {
                        conn.close();
                    }
                }
            } catch (Exception e) {
                th.fail("Unexpected exception: " + e);
            }
        }
    }

    public void test(TestHarness th) {
        this.th = th;

        try {
            server = (LocalMessageProtocolServerConnection)Connector.open("localmsg://:" + PROTO_NAME);

            Thread serverThread = new ServerThread();
            serverThread.start();
            client = (LocalMessageProtocolConnection)Connector.open("localmsg://" + PROTO_NAME);
            clientSendData();
            clientReceiveData();
            client.close();
            client = (LocalMessageProtocolConnection)Connector.open("localmsg://" + PROTO_NAME);
            clientSendData();
            clientReceiveData();
            serverThread.join();

            serverThread.start();
            client = (LocalMessageProtocolConnection)Connector.open("localmsg://" + PROTO_NAME);
            clientSendData();
            clientReceiveData();
            client = (LocalMessageProtocolConnection)Connector.open("localmsg://" + PROTO_NAME);
            clientSendData();
            clientReceiveData();
            serverThread.join();
        } catch (IOException ioe) {
            th.fail("Unexpected exception: " + ioe);
            ioe.printStackTrace();
        } catch (InterruptedException ie) {
            th.fail("Unexpected exception: " + ie);
            ie.printStackTrace();
        }
    }
}
