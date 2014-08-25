package com.sun.midp.io.j2me.http;

import java.io.*;
import javax.microedition.io.*;
import com.sun.j2me.security.AccessControlContext;
import com.sun.j2me.security.AccessController;

public class TestHTTPConnection {
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
            HttpConnection hc = (HttpConnection)Connector.open("http://localhost:8000/");

            long len = hc.getLength();
            if (len == 0) {
                System.out.println("FAIL - content-length is 0");
            }

            int responseCode = hc.getResponseCode();
            if (responseCode != 200) {
                System.out.println("FAIL - response code isn't 200");
            }

            String responseMessage = hc.getResponseMessage();
            if (!responseMessage.equals("OK")) {
                System.out.println("FAIL - " + responseMessage + " != OK");
            }

            String type = hc.getType();
            if (!type.equals("text/html")) {
                System.out.println("FAIL - " + type + " != text/html");
            }

            InputStream is = hc.openInputStream();

            byte buf[] = new byte[1024];
            int i = 0;
            do {
                buf[i++] = (byte)is.read();
            } while (buf[i-1] != -1 && buf[i-1] != '\r' && buf[i-1] != '\n' && i < buf.length);

            String firstLine = new String(buf, 0, i-1);
            if (!firstLine.equals("<!doctype html>")) {
                System.out.println("FAIL - " + firstLine + " != <!doctype html>");
            }

            is.close();
            hc.close();
        } catch (Exception e) {
            System.out.println("FAIL - Unexpected exception: " + e);
            e.printStackTrace();
        }

        System.out.println("DONE");
    }
}
