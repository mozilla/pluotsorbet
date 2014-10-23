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
    private static final String SOCKET_URL = "socket://localhost:54443";
    private static final String HOST = "localhost";
    private static final int PORT = 54443;
    private static final WebPublicKeyStore KEY_STORE = WebPublicKeyStore.getTrustedKeyStore();
    private TestHarness th;

    public void test(TestHarness th) {
        this.th = th;

        try {
            testBasicSSLStreamConnection();
            testMultipleSendsReceivesOnSameSocket();
            testMultipleSendsReceivesOnMultipleSockets();
        } catch (Exception e) {
            th.todo(false, "Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    void send(OutputStream os, InputStream is, String string) throws IOException {
        os.write((string + "\n").getBytes());

        byte buf[] = new byte[1024];
        int i = 0;
        do {
            buf[i++] = (byte)is.read();
        } while (buf[i-1] != -1 && buf[i-1] != '\n' && i < buf.length);

        String received = new String(buf, 0, i-1);
        th.todo(received, string);
    }

    void testBasicSSLStreamConnection() throws IOException {
        StreamConnection t = (StreamConnection)Connector.open(SOCKET_URL);
        try {
            SSLStreamConnection s =
                new SSLStreamConnection(HOST, PORT, t.openInputStream(), t.openOutputStream(), KEY_STORE);
            OutputStream os = s.openOutputStream();
            InputStream is = s.openInputStream();

            send(os, is, "I haven't stopped thinking about recreating that pluot sorbet.");

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
                send(os, is, "Message n." + i);
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

                send(os, is, "Message n." + i);

                os.close();
                is.close();
                s.close();
            } finally {
                t.close();
            }
        }
    }
}
