package com.sun.midp.io.j2me.socket;

import java.io.*;
import javax.microedition.io.*;
import com.sun.j2me.security.AccessControlContext;
import com.sun.j2me.security.AccessController;

public class TestSocket {
    private static class StubAccessControlContext implements AccessControlContext {
        public void checkPermission(String name) throws SecurityException {}
        public void checkPermission(String name, String resource) throws SecurityException {}
        public void checkPermission(String name, String resource, String extraValue) throws SecurityException {}
    }

    public static void main(String args[]) {
        StubAccessControlContext stubAcc = new StubAccessControlContext();
        AccessController.setAccessControlContext(stubAcc);

        System.out.println("START");

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
            } while (buf[i-1] != -1 && buf[i-1] != '\r' && buf[i-1] != '\n' && i < buf.length);

            is.close();

            String received = new String(buf, 0, i-1);
            if (!received.equals("HTTP/1.0 200 OK")) {
                System.out.println("FAIL - " + received + " != HTTP/1.0 200 OK");
            }

            client.close();
        } catch (Exception e) {
            System.out.println("FAIL - Unexpected exception: " + e);
            e.printStackTrace();
        }

        System.out.println("DONE");
    }
}
