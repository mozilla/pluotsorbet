package com.nokia.mid.impl.jms.core;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import gnu.testlet.TestUtils;
import java.io.*;
import javax.microedition.io.file.*;
import javax.microedition.io.*;

public class TestLauncher implements Testlet {
    native boolean checkImageModalDialog();

    public void test(TestHarness th) {
        try {
            FileConnection file = (FileConnection)Connector.open("file:////test.jpg", Connector.READ_WRITE);
            if (!file.exists()) {
                file.create();
            }
            OutputStream os = file.openDataOutputStream();
            InputStream is = getClass().getResourceAsStream("/org/mozilla/io/test.jpg");
            os.write(TestUtils.read(is));
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
