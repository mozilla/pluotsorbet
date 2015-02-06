package javax.microedition.media;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import gnu.testlet.TestUtils;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import java.io.*;

public class TestAudioRecorder implements Testlet {
    public int getExpectedPass() { return 1; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    TestHarness th;

    private static native byte[] convert3gpToAmr(byte[] data);

    public void test(TestHarness th) {
        this.th = th;

        test3gpToAmr();

        // TODO add more test cases to fully test audio recording.
    }

    private static boolean isArrayEqual(byte[] a, byte[] b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            System.out.println("null");
            return false;
        }
        int length = a.length;
        if (length != b.length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    private void test3gpToAmr() {
        try {
            byte[] actual = convert3gpToAmr(TestUtils.read(getClass().getResourceAsStream("/javax/microedition/media/audio.3gp")));
            byte[] expected = TestUtils.read(getClass().getResourceAsStream("/javax/microedition/media/audio.amr"));
            th.check(isArrayEqual(actual, expected));
        } catch (Exception e) {
            e.printStackTrace();
            th.fail("Unexpected exception: " + e);
        }
    }
}

