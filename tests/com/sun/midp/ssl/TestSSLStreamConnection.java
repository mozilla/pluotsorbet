/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

package com.sun.midp.ssl;

import com.sun.midp.publickeystore.WebPublicKeyStore;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

public class TestSSLStreamConnection implements Testlet {
    public int getExpectedPass() { return 0; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 1; }
    static final String SOCKET_URL = "socket://localhost:54443";
    static final String HOST = "localhost";
    static final int PORT = 54443;
    static final String MESSAGE = "I haven't stopped thinking about recreating that pluot sorbet.";
    WebPublicKeyStore KEY_STORE;
    TestHarness th;

    public void test(TestHarness th) {
        KEY_STORE = WebPublicKeyStore.getTrustedKeyStore();
        this.th = th;

        try {
            testBasicSSLStreamConnection();
            testMultipleSendsReceivesOnSameSocket();
            testMultipleSendsReceivesOnMultipleSockets();
            testSendOnClosedOutputStream();
            testReceiveOnClosedInputStream();
        } catch (IOException e) {
            th.todo(false, "Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    void send(OutputStream os, String message) throws IOException {
        os.write((message + "\n").getBytes());
    }

    String receive(InputStream is) throws IOException {
        byte buf[] = new byte[1024];
        int i = 0;
        do {
            buf[i++] = (byte)is.read();
        } while (buf[i-1] != -1 && buf[i-1] != '\n' && i < buf.length);
        String message = new String(buf, 0, i-1);
        return message;
    }

    void testBasicSSLStreamConnection() throws IOException {
        StreamConnection t = (StreamConnection)Connector.open(SOCKET_URL);
        try {
            SSLStreamConnection s =
                new SSLStreamConnection(HOST, PORT, t.openInputStream(), t.openOutputStream(), KEY_STORE);
            OutputStream os = s.openOutputStream();
            InputStream is = s.openInputStream();

            send(os, MESSAGE);
            th.todo(receive(is), MESSAGE);

            os.close();
            is.close();
            s.close();
        } finally {
            t.close();
        }
    }

    void testMultipleSendsReceivesOnSameSocket() throws IOException {
        StreamConnection t = (StreamConnection)Connector.open(SOCKET_URL);
        try {
            SSLStreamConnection s =
                new SSLStreamConnection(HOST, PORT, t.openInputStream(), t.openOutputStream(), KEY_STORE);
            OutputStream os = s.openOutputStream();
            InputStream is = s.openInputStream();

            for (int i = 0; i < 100; i++) {
                String message = "Message n." + i;
                send(os, message);
                th.todo(receive(is), message);
            }

            os.close();
            is.close();
            s.close();
        } finally {
            t.close();
        }
    }

    void testMultipleSendsReceivesOnMultipleSockets() throws IOException {
        for (int i = 0; i < 100; i++) {
            StreamConnection t = (StreamConnection)Connector.open(SOCKET_URL);
            try {
                SSLStreamConnection s =
                    new SSLStreamConnection(HOST, PORT, t.openInputStream(), t.openOutputStream(), KEY_STORE);
                OutputStream os = s.openOutputStream();
                InputStream is = s.openInputStream();

                String message = "Message n." + i;
                send(os, message);
                th.todo(receive(is), message);

                os.close();
                is.close();
                s.close();
            } finally {
                t.close();
            }
        }
    }

    void testSendOnClosedOutputStream() throws IOException {
        StreamConnection t = (StreamConnection)Connector.open(SOCKET_URL);
        try {
            SSLStreamConnection s =
                new SSLStreamConnection(HOST, PORT, t.openInputStream(), t.openOutputStream(), KEY_STORE);
            OutputStream os = s.openOutputStream();
            InputStream is = s.openInputStream();

            os.close();
            try {
                send(os, MESSAGE);
                th.fail("send on closed output stream");
            } catch(Exception e) {
                th.todo(e, "java.io.InterruptedIOException: Stream closed");
            }

            is.close();
            s.close();
        } finally {
            t.close();
        }
    }

    void testReceiveOnClosedInputStream() throws IOException {
        StreamConnection t = (StreamConnection)Connector.open(SOCKET_URL);
        try {
            SSLStreamConnection s =
                new SSLStreamConnection(HOST, PORT, t.openInputStream(), t.openOutputStream(), KEY_STORE);
            OutputStream os = s.openOutputStream();
            InputStream is = s.openInputStream();

            send(os, MESSAGE);
            is.close();
            try {
                receive(is);
                th.fail("receive on closed input stream");
            } catch(Exception e) {
                th.todo(e, "java.io.InterruptedIOException: Stream closed");
            }

            os.close();
            s.close();
        } finally {
            t.close();
        }
    }
}
