package com.sun.midp.io.j2me.socket;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import java.io.*;
import javax.microedition.io.*;

public class TestSocket implements Testlet {
    public void test(TestHarness th) {
        try {
            testBasicSocketConnection(th);
        } catch (IOException e) {
            th.todo(false, "Unexpected exception: " + e);
            e.printStackTrace();
        }

        try {
            testImmediatelyCloseConnection(th);
        } catch (IOException e) {
            th.todo(false, "Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    private void testBasicSocketConnection(TestHarness th) throws IOException {
        SocketConnection client = (SocketConnection)Connector.open("socket://localhost:8000");
        OutputStream os = client.openOutputStream();
        os.write("GET /index.html HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes());
        os.close();

        InputStream is = client.openInputStream();
        byte buf[] = new byte[1024];
        int i = 0;
        do {
            buf[i++] = (byte)is.read();
        } while (buf[i-1] != -1 && buf[i-1] != '\r' && buf[i-1] != '\n' && i < buf.length);

        is.close();

        String received = new String(buf, 0, i-1);
        th.todo(received, "HTTP/1.0 200 OK");

        int keepAlive = client.getSocketOption(SocketConnection.KEEPALIVE);
        th.todo(keepAlive, 1);

        int linger = client.getSocketOption(SocketConnection.LINGER);
        th.todo(linger, 0);

        int sndbuf = client.getSocketOption(SocketConnection.SNDBUF);
        th.todo(sndbuf, 8192);

        int rcvbuf = client.getSocketOption(SocketConnection.RCVBUF);
        th.todo(rcvbuf, 8192);

        int delay = client.getSocketOption(SocketConnection.DELAY);
        th.todo(delay, 1);

        client.setSocketOption(SocketConnection.KEEPALIVE, 0);
        keepAlive = client.getSocketOption(SocketConnection.KEEPALIVE);
        th.todo(keepAlive, 0);

        client.setSocketOption(SocketConnection.LINGER, 1);
        linger = client.getSocketOption(SocketConnection.LINGER);
        th.todo(linger, 1);

        client.setSocketOption(SocketConnection.SNDBUF, 4096);
        sndbuf = client.getSocketOption(SocketConnection.SNDBUF);
        th.todo(sndbuf, 4096);

        client.setSocketOption(SocketConnection.RCVBUF, 16384);
        rcvbuf = client.getSocketOption(SocketConnection.RCVBUF);
        th.todo(rcvbuf, 16384);

        client.setSocketOption(SocketConnection.DELAY, 0);
        delay = client.getSocketOption(SocketConnection.DELAY);
        th.todo(delay, 0);

        client.close();
    }

    private void testImmediatelyCloseConnection(TestHarness th) throws IOException {
        SocketConnection client = (SocketConnection)Connector.open("socket://localhost:8000");
        client.close();
        // I can't find a way to check that the connection is actually closed,
        // but this ensures that the native impl of Protocol.close0 is the code
        // that closes the connection (testBasicSocketConnection also causes
        // that function to be called, but the connection is already closed
        // by then).
        th.todo(true, "socket connection opened and closed");
    }

}
