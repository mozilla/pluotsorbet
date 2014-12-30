package javax.microedition.media;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import java.io.*;

public class TestAudioRecorder implements Testlet {
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
            byte[] actual = convert3gpToAmr(read("/javax/microedition/media/audio.3gp"));
            byte[] expected = read("/javax/microedition/media/audio.amr");
            th.check(isArrayEqual(actual, expected));
        } catch (Exception e) {
            e.printStackTrace();
            th.fail("Unexpected exception: " + e);
        }
    }

    private byte[] read(String path) throws IOException {
        InputStream is = getClass().getResourceAsStream(path);
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

        if (length < buffer.length) {
            byte[] b = new byte[length];
            System.arraycopy(buffer, 0, b, 0, length);
            buffer = b;
        }

        return buffer;
    }
}

