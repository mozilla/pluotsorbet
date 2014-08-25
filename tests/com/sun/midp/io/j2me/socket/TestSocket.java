package com.sun.midp.io.j2me.socket;

import java.io.*;
import javax.microedition.io.*;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestSocket implements Testlet {
    public void test(TestHarness th) {
        try {
            SocketConnection client = (SocketConnection)Connector.open("socket://localhost:8000");
            OutputStream os = client.openOutputStream();
            os.write("GET /index.html HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes());
            os.close();

            InputStream is = client.openInputStream();
            byte buf[] = new byte[1024];
            int i = 0;
            do {
                buf[i++] = (byte)is.read();
            } while (buf[i-1] != -1 && buf[i-1] != '\n' && i < buf.length);

            is.close();

            String received = new String(buf, 0, i);
            th.check(received, "HTTP/1.0 200 OK");

            client.close();
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
