package com.sun.midp.io.j2me.http;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import java.io.*;
import javax.microedition.io.*;

public class TestHttpConnection implements Testlet {
    public int getExpectedPass() { return 5; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    public void test(TestHarness th) {
        try {
            HttpConnection hc = (HttpConnection)Connector.open("http://localhost:8000/tests/test.html");

            long len = hc.getLength();
            th.check(len > 0, "length is > 0");

            int responseCode = hc.getResponseCode();
            th.check(responseCode, 200, "response code is 200");

            String responseMessage = hc.getResponseMessage();
            th.check(responseMessage, "OK");

            String type = hc.getType();
            th.check(type, "text/html");

            InputStream is = hc.openInputStream();

            byte buf[] = new byte[1024];
            int i = 0;
            do {
                buf[i++] = (byte)is.read();
            } while (buf[i-1] != -1 && buf[i-1] != '\r' && buf[i-1] != '\n' && i < buf.length);

            String firstLine = new String(buf, 0, i-1);
            th.check(firstLine, "<!doctype html>");

            is.close();
            hc.close();
        } catch (IOException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
