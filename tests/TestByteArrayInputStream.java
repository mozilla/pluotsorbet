import java.io.*;

public class TestByteArrayInputStream extends Test {
	public void main() {
		byte[] a = new byte[11];
		for (int i = 0; i < a.length; i++) {
			a[i] = (byte)(i*i);
		}
		for (int i = 0; i < a.length; i++) {
			check(a[i] == i*i);
		}
		ByteArrayInputStream s = new ByteArrayInputStream(a);
		check(s.available() == 11);
		check(s.read() == 0);
		check(s.skip(4) == 4);
		check(s.available() == 6);
		byte[] bb = new byte[5];
		check(s.read(bb, 1, 3) == 3);
		check(bb[0] == 0);
		check(bb[1] == 25);
		check(bb[2] == 36);
		check(bb[3] == 49);
		check(bb[4] == 0);
		check(s.markSupported());
		check(s.available() == 3);
		s.reset();
		check(s.available() == 11);
		for (int i = 0; i < a.length; i++) {
			check(s.read() == i*i);
		}
		check(s.read() == -1);
		check(s.available() == 0);
		s.reset();
		s.read();
		s.mark(5);
		s.reset();
		
		check(s.available() == 10);
		check(s.read() == 1);
		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ByteArrayInputStream s2 = new ByteArrayInputStream(a, 2, 5);
		for (int i = 2; i < 7; i++) {
			check(s2.read() == i*i);
		}
		
		try {
			ByteArrayInputStream s3 = new ByteArrayInputStream(null);
			check(false);
		} catch (NullPointerException e) {
			check(true);
		}
	}

    public static void main(String[] args) {
	(new TestByteArrayInputStream()).main();
    }
}
