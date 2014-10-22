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
    public void test(TestHarness th) {
        try {
            testSSLStreamConnection(th);
        } catch (Exception e) {
            th.todo(false, "Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    void testSSLStreamConnection(TestHarness th) throws IOException {
        StreamConnection t = (StreamConnection)Connector.open("socket://localhost:54443");
        try {
            WebPublicKeyStore cs = WebPublicKeyStore.getTrustedKeyStore();
            SSLStreamConnection s = new SSLStreamConnection("localhost", 54443,
                       t.openInputStream(), t.openOutputStream(), cs);
            OutputStream sout = s.openOutputStream();
            InputStream sin = s.openInputStream();

            String message = "I haven't stopped thinking about recreating that pluot sorbet.";
            sout.write((message + "\n").getBytes());

            byte buf[] = new byte[1024];
            int i = 0;
            do {
                buf[i++] = (byte)sin.read();
            } while (buf[i-1] != -1 && buf[i-1] != '\n' && i < buf.length);

            String received = new String(buf, 0, i-1);
            th.todo(received, message);
            sin.close();
            sout.close();
            s.close();
        } finally {
            t.close();
        }
    }
}
