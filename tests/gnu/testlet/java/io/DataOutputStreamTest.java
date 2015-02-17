package gnu.testlet.java.io;

import java.io.*;
import gnu.testlet.*;

public class DataOutputStreamTest implements Testlet {
	public int getExpectedPass() { return 52; }
	public int getExpectedFail() { return 0; }
	public int getExpectedKnownFail() { return 0; }
	public void test(TestHarness th) {
		
		try {
			byte[] input = {-2, 54, -2, -2, 1, 0, -128, 1, 91, -64, 0x0C, 0x7A, -31, 0x47, -82, 0x14, 0x7B, 0x41, 0x09, 
				0x1d, 0x25, 122, 121, 0, 120, -1, -7, 0x51, 0x54, -1, -1, -1, -1, -1, -7, 0x51, 0x54, -14, 0x16, 0, 10, 
				-59, -101, 99, 105, -61, -77, -59, -126, 107, 97};
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream s = new DataOutputStream(out);
			s.write(254);
			byte[] bb = {54, -2};
			s.write(bb);
			s.write(bb, 1, 1);
			s.writeBoolean(true);
			s.writeBoolean(false);
			s.writeByte(-128);
			s.writeChar('ś');
			s.writeDouble(-3.56);
			s.writeFloat(8.569615f);
			s.writeChars("穹x");
			s.writeInt(-437932);
			s.writeLong(-437932l);
			s.writeShort(-3562);
			s.writeUTF("ściółka");
			s.flush();
			bb = out.toByteArray();
			th.check(bb.length, input.length);
			for (int i = 0; i < bb.length; i++) {
				th.check(bb[i], input[i]);
			}
			s.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			th.check(false);
		}
	}
}
