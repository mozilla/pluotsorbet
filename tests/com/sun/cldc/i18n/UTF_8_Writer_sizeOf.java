package com.sun.cldc.i18n;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import com.sun.cldc.i18n.j2me.UTF_8_Writer;
import com.sun.cldc.i18n.Helper;
import com.sun.cldc.i18n.StreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class UTF_8_Writer_sizeOf implements Testlet {
    public int getExpectedPass() { return 1; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    public void test (TestHarness th) {
        try {
            String str = "abcdefghilmnopqrstuvzABCDEFGHILMNOPQRSTUVZabcdefghilmnopqrstuvzABCDEFGHILMNOPQRSTUVZ";
            char[] cbuf = new char[str.length()];
            str.getChars(0, str.length(), cbuf, 0);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StreamWriter sw = new UTF_8_Writer();

            sw.open(baos, "UTF_8");

            th.check(sw.sizeOf(cbuf, 0, cbuf.length), 84);

            sw.close();
        } catch (IOException e) {
            th.fail("Unexpected exception: " + e);
        }
    }
}
