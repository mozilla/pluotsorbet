package com.nokia.mid.impl.jms.core;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import java.io.*;
import javax.microedition.io.file.*;
import javax.microedition.io.*;

public class TestLauncher implements Testlet {
    native boolean checkImageModalDialog();

    byte[] read(InputStream is) throws IOException {
        int l = is.available();
        byte[] buffer = new byte[l+1];
        int length = 0;

        while ((l = is.read(buffer, length, buffer.length - length)) != -1) {
            length += l;
            if (length == buffer.length) {
                byte[] b = new byte[buffer.length + 4096];
                System.arraycopy(buffer, 0, b, 0, length);
                buffer = b;
            }
        }

        return buffer;
    }

    public void test(TestHarness th) {
        try {
            FileConnection file = (FileConnection)Connector.open("file:////test.jpg", Connector.READ_WRITE);
            if (!file.exists()) {
                file.create();
            }
            OutputStream os = file.openDataOutputStream();
            InputStream is = getClass().getResourceAsStream("/org/mozilla/io/test.jpg");
            os.write(read(is));
            os.close();

            boolean expectedFailure = false;
            try {
                Launcher.handleContent("image_not_exists.jpg");
            } catch (Exception e) {
                expectedFailure = true;
            }
            th.check(expectedFailure, true);

            expectedFailure = false;
            try {
                Launcher.handleContent("not_supported_ext.mp3");
            } catch (Exception e) {
                expectedFailure = true;
            }
            th.check(expectedFailure, true);

            Launcher.handleContent("test.jpg");
            th.check(checkImageModalDialog(), true);

            file.delete();
            file.close();
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
