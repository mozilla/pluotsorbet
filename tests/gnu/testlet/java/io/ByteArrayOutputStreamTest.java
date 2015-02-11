package gnu.testlet.java.io;

import java.io.*;
import gnu.testlet.*;

public class ByteArrayOutputStreamTest implements Testlet {
    public int getExpectedPass() { return 30; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    public void test(TestHarness th) {
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		th.check(s.size() == 0);
		th.check(s.toString().equals(""));
		th.check(s.toByteArray().length == 0);
		s.write(48);
		th.check(s.size() == 1);
		th.check(s.toString().equals("0"));
		th.check(s.toByteArray().length == 1);
		th.check(s.toByteArray()[0] == 48);
		s.write("_test_".getBytes(), 1, 4);
		th.check(s.size() == 5);
		th.check(s.toString().equals("0test"));
		th.check(s.toByteArray().length == 5);
		th.check(s.toByteArray()[4] == 116);
		s.reset();
		//12
		th.check(s.size() == 0);
		th.check(s.toString().equals(""));
		th.check(s.toByteArray().length == 0);
		s.write(645);
		th.check(s.size() == 1);
		th.check(s.toByteArray().length == 1);
		th.check(s.toByteArray()[0] == -123);
		s.write(-129);
		th.check(s.size() == 2);
		th.check(s.toByteArray().length == 2);
		th.check(s.toByteArray()[1] == 127);
		try {
			s.flush();
			th.check(true);
			s.close();
			th.check(true);
		} catch (IOException e) {
			e.printStackTrace();
			th.check(false);
		}
		th.check(s.size() == 2);
		th.check(s.toByteArray().length == 2);
		s = new ByteArrayOutputStream(3);
		//26
		th.check(s.size(), 0);
		th.check(s.toString(), "");
		th.check(s.toByteArray().length, 0);
		byte[] bb = "ściółka冷蔵庫".getBytes();
		for (int i = 0; i < bb.length; i++) {
			s.write(bb[i]);
		}
		th.check(s.size(), 19);
		th.check(s.toString(), "ściółka冷蔵庫");
		try {
			new ByteArrayOutputStream(-3);
			th.check(false);
		} catch (IllegalArgumentException e) {
			th.check(true);
		}
	}
}
