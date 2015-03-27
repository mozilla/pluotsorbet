package javax.microedition.io;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import java.io.InputStream;
import java.io.IOException;

public class TestHttpsConnection implements Testlet {
    public int getExpectedPass() { return 5; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    TestHarness th;

    public void test(TestHarness th) {
        this.th = th;

        try {
            testBasicHttpsConnection();
        } catch (IOException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    public void testBasicHttpsConnection() throws IOException {
        HttpsConnection hc = null;
        InputStream is = null;

        try {
            hc = (HttpsConnection)Connector.open("https://localhost:4443/test.html");

            long len = hc.getLength();
            th.check(len > 0, "length is > 0");

            int responseCode = hc.getResponseCode();
            th.check(responseCode, HttpConnection.HTTP_OK, "response code is HTTP_OK");

            String responseMessage = hc.getResponseMessage();
            th.check(responseMessage, "OK");

            String type = hc.getType();
            th.check(type, "text/html");

            is = hc.openDataInputStream();

            byte buf[] = new byte[1024];
            int i = 0;
            do {
                buf[i++] = (byte)is.read();
            } while (buf[i-1] != -1 && buf[i-1] != '\r' && buf[i-1] != '\n' && i < buf.length);

            String firstLine = new String(buf, 0, i-1);
            th.check(firstLine, "<!doctype html>");
        } finally {
            if (is != null) {
                is.close();
            }
            if (hc != null) {
                hc.close();
            }
        }
    }
}
