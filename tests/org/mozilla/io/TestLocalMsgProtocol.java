package org.mozilla.io;

import java.io.*;
import javax.microedition.io.*;
import com.nokia.mid.s40.io.LocalMessageProtocolMessage;
import com.nokia.mid.s40.io.LocalMessageProtocolConnection;
import com.nokia.mid.s40.io.LocalMessageProtocolServerConnection;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestLocalMsgProtocol implements Testlet {
    public int getExpectedPass() { return 16; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
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

    class TestThread extends Thread {
        TestHarness th;
        int sleep1;
        int sleep2;
        int content;
        LocalMessageProtocolServerConnection server;
        LocalMessageProtocolConnection client;

        public TestThread(TestHarness th, int sleepBeforeSend, int sleepBeforeReceive, int content) throws IOException {
            this.th = th;
            this.sleep1 = sleepBeforeSend;
            this.sleep2 = sleepBeforeReceive;
            this.content = content;
            // To prevent the native localmsg connections mess with each other, we initialize
            // client and server in constructor here.
            this.server = (LocalMessageProtocolServerConnection)Connector.open("localmsg://:"+PROTO_NAME);
            this.client = (LocalMessageProtocolConnection)Connector.open("localmsg://"+PROTO_NAME);
        }

        public void testServerSendsClientReceives() throws IOException, InterruptedException {
            Thread.sleep(sleep1);

            // Server sends data
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeByte(5);
            dataOutputStream.writeInt(content);
            byte[] serverData = byteArrayOutputStream.toByteArray();
            ((LocalMessageProtocolConnection)server).send(serverData, 0, serverData.length);
            dataOutputStream.close();
            byteArrayOutputStream.close();

            Thread.sleep(sleep2);

            // Client receives data
            byte[] clientData = new byte[5];
            client.receive((byte[])clientData);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream((byte[])clientData);
            DataInputStream dataInputStream = new DataInputStream((InputStream)byteArrayInputStream);
            byte by = dataInputStream.readByte();
            int n = dataInputStream.readInt();
            th.check(by, 5);
            th.check(n, content);
            dataInputStream.close();
            byteArrayInputStream.close();
        }


        public void run() {
            try {
                testServerSendsClientReceives();
            } catch (IOException ioe) {
                th.fail("Unexpected exception");
                ioe.printStackTrace();
            } catch (InterruptedException ie) {
                th.fail("Unexpected exception");
                ie.printStackTrace();
            }
        }
    }

    public void test(TestHarness th) {
        try {
            server = (LocalMessageProtocolServerConnection)Connector.open("localmsg://:"+PROTO_NAME);

            client = (LocalMessageProtocolConnection)Connector.open("localmsg://"+PROTO_NAME);

            testServerSendsClientReceives(th);
            testClientSendsServerReceives(th);

            testServerSendsClientReceives2(th);
            testClientSendsServerReceives2(th);

            Thread t1 = new TestThread(th, 10,   2000, 12421);
            Thread t2 = new TestThread(th, 500, 500, 32311);
            Thread t3 = new TestThread(th, 1000, 500, 92330);
            t1.start();
            t2.start();
            t3.start();
            t1.join();
            t2.join();
            t3.join();

            client.close();
            server.close();
        } catch (IOException ioe) {
            th.fail("Unexpected exception");
            ioe.printStackTrace();
        } catch (InterruptedException ie) {
            th.fail("Unexpected exception");
            ie.printStackTrace();
        }
    }
}
