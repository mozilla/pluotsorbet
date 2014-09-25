package com.sun.cldc.io;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

import java.io.*;

public class TestResourceInputStream implements Testlet {
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

    public void test(TestHarness th) {
        try {
            InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("utf8.txt"));
            th.check(readLine(reader), "ξεσκεπάζω τὴν ψυχοφθόρα βδελυγμία");
            th.check(readLine(reader) == null);
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

}
