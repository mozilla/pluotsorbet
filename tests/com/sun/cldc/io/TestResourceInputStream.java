package com.sun.cldc.io;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

import java.io.*;

public class TestResourceInputStream implements Testlet {
    public int getExpectedPass() { return 4; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    TestHarness th;

    private String readLine(InputStreamReader reader) throws IOException {
        // Test whether the end of file has been reached. If so, return null.
        int readChar = reader.read();
        if (readChar == -1) {
            return null;
        }
        StringBuffer string = new StringBuffer("");
        // Read until end of file or new line
        while (readChar != -1 && readChar != '\n') {
            // Append the read character to the string. Some operating systems
            // such as Microsoft Windows prepend newline character ('\n') with
            // carriage return ('\r'). This is part of the newline character
            // and therefore an exception that should not be appended to the
            // string.
            if (readChar != '\r') {
                string.append((char)readChar);
            }
            // Read the next character
            readChar = reader.read();
        }
        return string.toString();
    }

    public void readWithStreamReader(ResourceInputStream stream) throws IOException {
        InputStreamReader reader = new InputStreamReader(stream);
        th.check(readLine(reader), "ξεσκεπάζω τὴν ψυχοφθόρα βδελυγμία");
        th.check(readLine(reader) == null);
    }

    public void test(TestHarness th) {
        this.th = th;

        try {
            ResourceInputStream stream = (ResourceInputStream)getClass().getResourceAsStream("utf8.txt");
            stream.mark(0);
            readWithStreamReader(stream);
            stream.reset();
            readWithStreamReader(stream);
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

}
