package gnu.testlet.java.io;

import java.io.*;
import gnu.testlet.*;

public class DataInputStreamTest implements Testlet {
	public void test(TestHarness th) {
		try {
			byte[] input = {-2, 54, -2, 0, 1, 0, -128, 1, 91, -64, 0x0C, 0x7A, -31, 0x47, -82, 0x14, 0x7B, 0x41, 0x09, 
				0x1d, 0x25, 122, 121, 120, -1, -7, 0x51, 0x54, -1, -1, -1, -1, -1, -7, 0x51, 0x54, -14, 0x16, -1, -1, 
				-1, 0, 10, -59, -101, 99, 105, -61, -77, -59, -126, 107, 97};
			DataInputStream s = new DataInputStream(new ByteArrayInputStream(input));
			th.check(s.available() == input.length);
			th.check(s.markSupported());
			s.mark(0);
			th.check(s.read() == 254);
			byte[] bb = new byte[2];
			th.check(s.read(bb) == 2);
			th.check(bb[0] == 54);
			th.check(bb[1] == -2);
			th.check(s.read(bb, 1, 1) == 1);
			th.check(bb[0] == 54);
			th.check(bb[1] == 0);
			th.check(s.available() == input.length - 4);
			//11
			th.check(s.readBoolean());
			th.check(!s.readBoolean());
			th.check(s.readByte() == -128);
			th.check(s.readChar() == 'ś');
			th.check(s.readDouble() == -3.56);
			th.check(s.readFloat() == 8.569615f);
			try {
				s.readFully(null);
				th.check(false);
			} catch (NullPointerException e) {
				th.check(true);
			}
			bb = new byte[5];
			s.readFully(bb, 1, 3);
			th.check(bb[0] == 0);
			th.check(bb[1] == 122);
			th.check(bb[2] == 121);
			th.check(bb[3] == 120);
			th.check(bb[4] == 0);
			th.check(s.readInt() == -437932);
			th.check(s.readLong() == -437932l);
			th.check(s.readShort() == -3562);
			th.check(s.readUnsignedByte() == 255);
			th.check(s.readUnsignedShort() == 65535);
			th.check(s.readUTF(), "ściółka");
			th.check(s.available() == 0);
			//30
			try {
				s.readBoolean();
				th.check(false);
			} catch (EOFException e) {
				th.check(true);
			}
			try {
				s.readByte();
				th.check(false);
			} catch (EOFException e) {
				th.check(true);
			}
			try {
				s.readChar();
				th.check(false);
			} catch (EOFException e) {
				th.check(true);
			}
			try {
				s.readDouble();
				th.check(false);
			} catch (EOFException e) {
				th.check(true);
			}
			try {
				s.readFloat();
				th.check(false);
			} catch (EOFException e) {
				th.check(true);
			}
			try {
				s.readFully(bb);
				th.check(false);
			} catch (EOFException e) {
				th.check(true);
			}
			try {
				s.readInt();
				th.check(false);
			} catch (EOFException e) {
				th.check(true);
			}
			try {
				s.readLong();
				th.check(false);
			} catch (EOFException e) {
				th.check(true);
			}
			try {
				s.readShort();
				th.check(false);
			} catch (EOFException e) {
				th.check(true);
			}
			try {
				s.readUnsignedByte();
				th.check(false);
			} catch (EOFException e) {
				th.check(true);
			}
			try {
				s.readUnsignedShort();
				th.check(false);
			} catch (EOFException e) {
				th.check(true);
			}
			try {
				s.readUTF();
				th.check(false);
			} catch (EOFException e) {
				th.check(true);
			}
			//40
			s.reset();
			th.check(s.available() == input.length);
			s.skip(5);
			th.check(s.available() == input.length - 5);
			s.skipBytes(7);
			th.check(s.available() == input.length - 12);
			s.close();
			//System.out.println(s.readFloat());
			
		} catch (Exception e) {
			e.printStackTrace();
			th.check(false);
		}
	}
}
