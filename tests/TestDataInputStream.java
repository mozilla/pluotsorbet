import java.io.*;
public class TestDataInputStream extends Test {
	public void main() {
		try {
			byte[] input = {-2, 54, -2, 0, 1, 0, -128, 1, 91, -64, 0x0C, 0x7A, -31, 0x47, -82, 0x14, 0x7B, 0x41, 0x09, 
				0x1d, 0x25, 122, 121, 120, -1, -7, 0x51, 0x54, -1, -1, -1, -1, -1, -7, 0x51, 0x54, -14, 0x16, -1, -1, 
				-1, 0, 10, -59, -101, 99, 105, -61, -77, -59, -126, 107, 97};
			DataInputStream s = new DataInputStream(new ByteArrayInputStream(input));
			check(s.available() == input.length);
			check(s.markSupported());
			s.mark(0);
			check(s.read() == 254);
			byte[] bb = new byte[2];
			check(s.read(bb) == 2);
			check(bb[0] == 54);
			check(bb[1] == -2);
			check(s.read(bb, 1, 1) == 1);
			check(bb[0] == 54);
			check(bb[1] == 0);
			check(s.available() == input.length - 4);
			//11
			check(s.readBoolean());
			check(!s.readBoolean());
			check(s.readByte() == -128);
			check(s.readChar() == 'ś');
			check(s.readDouble() == -3.56);
			check(s.readFloat() == 8.569615f);
			try {
				s.readFully(null);
				check(false);
			} catch (NullPointerException e) {
				check(true);
			}
			bb = new byte[5];
			s.readFully(bb, 1, 3);
			check(bb[0] == 0);
			check(bb[1] == 122);
			check(bb[2] == 121);
			check(bb[3] == 120);
			check(bb[4] == 0);
			check(s.readInt() == -437932);
			check(s.readLong() == -437932l);
			check(s.readShort() == -3562);
			check(s.readUnsignedByte() == 255);
			check(s.readUnsignedShort() == 65535);
			String x = s.readUTF();
			compare(x, "ściółka");
			System.out.println(x.length());
			System.out.println("ściółka".length());
			System.out.println(x.getBytes().length);
			System.out.println("ściółka".getBytes().length);
			check(s.available() == 0);
			//30
			try {
				s.readBoolean();
				check(false);
			} catch (EOFException e) {
				check(true);
			}
			try {
				s.readByte();
				check(false);
			} catch (EOFException e) {
				check(true);
			}
			try {
				s.readChar();
				check(false);
			} catch (EOFException e) {
				check(true);
			}
			try {
				s.readDouble();
				check(false);
			} catch (EOFException e) {
				check(true);
			}
			try {
				s.readFloat();
				check(false);
			} catch (EOFException e) {
				check(true);
			}
			try {
				s.readFully(bb);
				check(false);
			} catch (EOFException e) {
				check(true);
			}
			try {
				s.readInt();
				check(false);
			} catch (EOFException e) {
				check(true);
			}
			try {
				s.readLong();
				check(false);
			} catch (EOFException e) {
				check(true);
			}
			try {
				s.readShort();
				check(false);
			} catch (EOFException e) {
				check(true);
			}
			try {
				s.readUnsignedByte();
				check(false);
			} catch (EOFException e) {
				check(true);
			}
			try {
				s.readUnsignedShort();
				check(false);
			} catch (EOFException e) {
				check(true);
			}
			try {
				s.readUTF();
				check(false);
			} catch (EOFException e) {
				check(true);
			}
			//40
			s.reset();
			check(s.available() == input.length);
			s.skip(5);
			check(s.available() == input.length - 5);
			s.skipBytes(7);
			check(s.available() == input.length - 12);
			s.close();
			//System.out.println(s.readFloat());
			
		} catch (Exception e) {
			e.printStackTrace();
			check(false);
		}
	}

    public static void main(String[] args) {
	(new TestDataInputStream()).main();
    }
}
