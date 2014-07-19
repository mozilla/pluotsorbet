import java.io.*;
public class TestByteArrayOutputStream extends Test {
	public void main() {
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		check(s.size() == 0);
		check(s.toString().equals(""));
		check(s.toByteArray().length == 0);
		s.write(48);
		check(s.size() == 1);
		check(s.toString().equals("0"));
		check(s.toByteArray().length == 1);
		check(s.toByteArray()[0] == 48);
		s.write("_test_".getBytes(), 1, 4);
		check(s.size() == 5);
		check(s.toString().equals("0test"));
		check(s.toByteArray().length == 5);
		check(s.toByteArray()[4] == 116);
		s.reset();
		//12
		check(s.size() == 0);
		check(s.toString().equals(""));
		check(s.toByteArray().length == 0);
		s.write(645);
		check(s.size() == 1);
		check(s.toByteArray().length == 1);
		check(s.toByteArray()[0] == -123);
		s.write(-129);
		check(s.size() == 2);
		check(s.toByteArray().length == 2);
		check(s.toByteArray()[1] == 127);
		try {
			s.flush();
			check(true);
			s.close();
			check(true);
		} catch (IOException e) {
			e.printStackTrace();
			check(false);
		}
		check(s.size() == 2);
		check(s.toByteArray().length == 2);
		s = new ByteArrayOutputStream(3);
		//26
		compare(s.size(), 0);
		compare(s.toString(), "");
		compare(s.toByteArray().length, 0);
		byte[] bb = "ściółka冷蔵庫".getBytes();
		for (int i = 0; i < bb.length; i++) {
			s.write(bb[i]);
		}
		compare(s.size(), 19);
		compare(s.toString(), "ściółka冷蔵庫");
		try {
			new ByteArrayOutputStream(-3);
			check(false);
		} catch (IllegalArgumentException e) {
			check(true);
		}
	}

    public static void main(String[] args) {
	(new TestByteArrayOutputStream()).run();
    }
}
