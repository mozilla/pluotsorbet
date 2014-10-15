package com.sun.midp.io.j2me.socket;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import java.io.*;
import javax.microedition.io.*;

public class StressTestSocket implements Testlet {
    TestHarness th;
    static final String URL = "socket://localhost:50003";

    public void test(TestHarness th) {
        this.th = th;

        try {
            testMultipleSendsReceivesOnSameSocket();
            testMultipleSendsReceivesOnMultipleSockets();
        } catch (IOException e) {
            th.todo(false, "Exception unexpected: " + e);
            e.printStackTrace();
        }
    }

    void send(OutputStream os, InputStream is, String string) throws IOException {
        os.write(string.getBytes());

        byte buf[] = new byte[1024];
        int i = 0;
        do {
            buf[i++] = (byte)is.read();
        } while (buf[i-1] != -1 && buf[i-1] != '\n' && i < buf.length);
        
        String received = new String(buf, 0, i);
        th.todo(received, string);
    }

    void testMultipleSendsReceivesOnSameSocket() throws IOException {
        SocketConnection client = (SocketConnection)Connector.open(URL);
        OutputStream os = client.openOutputStream();
        InputStream is = client.openInputStream();

        for (int i = 0; i < 100; i++) {
            send(os, is, "Message n." + i + "\n");
        }

        is.close();
        os.close();
        client.close();
    }

    void testMultipleSendsReceivesOnMultipleSockets() throws IOException {
        for (int i = 0; i < 100; i++) {
            SocketConnection client = (SocketConnection)Connector.open(URL);
            OutputStream os = client.openOutputStream();
            InputStream is = client.openInputStream();
            send(os, is, "Message n." + i + "\n");
            is.close();
            os.close();
            client.close();
        }
    }
}
