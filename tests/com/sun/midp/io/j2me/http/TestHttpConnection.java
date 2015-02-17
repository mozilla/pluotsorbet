package com.sun.midp.io.j2me.http;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import java.io.*;
import javax.microedition.io.*;

public class TestHttpConnection implements Testlet {
    public int getExpectedPass() { return 0; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 2; }
    public void test(TestHarness th) {
        try {
            HttpConnection hc = (HttpConnection)Connector.open("http://localhost:8000/tests/test.html");

            long len = hc.getLength();
            th.todo(len > 0, "length is > 0");

            int responseCode = hc.getResponseCode();
            th.todo(responseCode, 200, "response code is 200");

            String responseMessage = hc.getResponseMessage();
            th.todo(responseMessage, "OK");

            String type = hc.getType();
            th.todo(type, "text/html");

            InputStream is = hc.openInputStream();

            byte buf[] = new byte[1024];
            int i = 0;
            do {
                buf[i++] = (byte)is.read();
            } while (buf[i-1] != -1 && buf[i-1] != '\r' && buf[i-1] != '\n' && i < buf.length);

            String firstLine = new String(buf, 0, i-1);
            th.todo(firstLine, "<!doctype html>");

            is.close();
            hc.close();
        } catch (IOException e) {
            th.todo(false, "Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
