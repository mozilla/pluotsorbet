package gnu.testlet.java.io;

import java.io.*;
import gnu.testlet.*;

public class ByteArrayInputStreamTest implements Testlet {
    public void test(TestHarness th) {
		byte[] a = new byte[11];
		for (int i = 0; i < a.length; i++) {
			a[i] = (byte)(i*i);
		}
		for (int i = 0; i < a.length; i++) {
			th.check(a[i] == i*i);
		}
		ByteArrayInputStream s = new ByteArrayInputStream(a);
		th.check(s.available() == 11);
		th.check(s.read() == 0);
		th.check(s.skip(4) == 4);
		th.check(s.available() == 6);
		byte[] bb = new byte[5];
		th.check(s.read(bb, 1, 3) == 3);
		th.check(bb[0] == 0);
		th.check(bb[1] == 25);
		th.check(bb[2] == 36);
		th.check(bb[3] == 49);
		th.check(bb[4] == 0);
		th.check(s.markSupported());
		th.check(s.available() == 3);
		s.reset();
		th.check(s.available() == 11);
		for (int i = 0; i < a.length; i++) {
			th.check(s.read() == i*i);
		}
		th.check(s.read() == -1);
		th.check(s.available() == 0);
		s.reset();
		s.read();
		s.mark(5);
		s.reset();
		
		th.check(s.available() == 10);
		th.check(s.read() == 1);
		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ByteArrayInputStream s2 = new ByteArrayInputStream(a, 2, 5);
		for (int i = 2; i < 7; i++) {
			th.check(s2.read() == i*i);
		}
		
		try {
			ByteArrayInputStream s3 = new ByteArrayInputStream(null);
			th.check(false);
		} catch (NullPointerException e) {
			th.check(true);
		}
	}
}
