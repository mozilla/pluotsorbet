import java.io.*;
import java.util.Date;
public class TestString extends Test {
	public void main() {
		try {
			String s = new String();
			check(s.equals(""));
			s = new String("test");
			check(s.equals("test"));
			try {
				char[] nil = null;
				fail(new String(nil));
			} catch (NullPointerException e) {
				check(true);
			}
			char[] in = {'a', 'ś', 'c'};
			s = new String(in);
			char[] in2 = {'f', 'a', 'ś', 'c', 'g'};
			String s2 = new String(in2, 1, 3);
			check(s.equals(s2));
			check(s2.length() == 3);
			try {
				fail(new Character(s2.charAt(-1)));
			} catch (IndexOutOfBoundsException e) {
				check(true);
			}
			try {
				fail(new Character(s2.charAt(3)));
			} catch (IndexOutOfBoundsException e) {
				check(true);
			}
			check(s2.charAt(1) == 'ś');
			char[] dst = new char[15];
			try {
				s.getChars(0, 0, null, 0);
				check(false);
			} catch (NullPointerException e) {
				check(true);
			}
			//10
			try {
				s.getChars(-1, 3, dst, 0);
				check(false);
			} catch (IndexOutOfBoundsException e) {
				check(true);
			}
			try {
				s.getChars(4, 3, dst, 0);
				check(false);
			} catch (IndexOutOfBoundsException e) {
				check(true);
			}
			try {
				s.getChars(0, 4, dst, 0);
				check(false);
			} catch (IndexOutOfBoundsException e) {
				check(true);
			}
			try {
				s.getChars(0, 3, dst, -1);
				check(false);
			} catch (IndexOutOfBoundsException e) {
				check(true);
			}
			try {
				s.getChars(0, 3, dst, 13);
				check(false);
			} catch (IndexOutOfBoundsException e) {
				check(true);
			}
			dst[0] = 'Q';
			s.getChars(3, 3, dst, 0);
			check(dst[0] == 'Q');
			s.getChars(1, 3, dst, 5);
			check(dst[5] == 'ś');
			check(dst[6] == 'c');
			try {
				s.getBytes("bdflsjl");
				check(false);
			} catch (UnsupportedEncodingException e) {
				check(true);
			}
			/*
			check(s.getBytes("utf-8")[0] == 97);
			//20
			check(s.getBytes("utf-8")[1] == -59);
			check(s.getBytes("utf-8")[2] == -101);
			check(s.getBytes("utf-8")[3] == 99);
			check(s.getBytes("utf-8").length == 4);
			check(s.getBytes().length == 4);
			check(!s.equals(null));
			check(s.equalsIgnoreCase("AŚC"));
			check(!s.equalsIgnoreCase("fŚC"));
			try {
				s.compareTo(null);
				check(false);
			} catch (NullPointerException e) {
				check(true);
			}
			check(s.compareTo("aśc") == 0);
			check(s.compareTo("a") == 2);
			check(s.compareTo("aścff") == -2);
			check(s.compareTo("aśa") == 2);
			check(s.compareTo("aśb") == 1);
			check(s.compareTo("aśd") == -1);
			check(s.compareTo("aśe") == -2);

			check(s.regionMatches(false, 0, "aśc", 0, 3));
			check(s.regionMatches(true, 0, "aśC", 0, 3));
			check(!s.regionMatches(false, -1, "aśc", 0, 3));
			check(!s.regionMatches(false, 0, "aśc", -1, 3));
			check(!s.regionMatches(false, 1, "aśc", 0, 3));
			check(!s.regionMatches(false, 0, "aśc", 1, 3));
			check(!s.regionMatches(false, 0, "aśC", 1, 3));
			check(!s.regionMatches(true, 0, "aśD", 1, 3));
			check(!s.regionMatches(false, 1, "a", 0, 1));

			try {
				s.startsWith(null);
				check(false);
			} catch (NullPointerException e) {
				check(true);
			}

			check(s.startsWith("aś", 0));
			check(s.startsWith("śc", 1));
			check(!s.startsWith("ab", 0));
			check(s.startsWith("aś"));
			check(s.endsWith("śc"));
			try {
				s.endsWith(null);
				check(false);
			} catch (NullPointerException e) {
				check(true);
			}
			check(s.hashCode() == 104073);
			check("aaabab".indexOf('b') == 3);
			check("aaabab".indexOf('c') == -1);
			check("aaabab".indexOf('a') == 0);
			check("aaabab".indexOf('b', 3) == 3);
			check("aaabab".indexOf('b', 4) == 5);
			check("aaabab".indexOf('b', 6) == -1);
			check("aaabab".indexOf('c', 3) == -1);
			check("aaabab".indexOf('a', 3) == 4);

			try {
				s.indexOf(null);
				check(false);
			} catch (NullPointerException e) {
				check(true);
			}
			check(s.indexOf("śc") == 1);
			check(s.indexOf("c", 1) == 2);
			check(s.indexOf("c", 4) == -1);
			check(s.indexOf("abc", 0) == -1);

			check("unhappy".substring(2).equals("happy"));
			check("Harbison".substring(3).equals("bison"));
			check("emptiness".substring(9).equals(""));
			try {
				s.substring(-1);
				check(false);
			} catch (IndexOutOfBoundsException e) {
				check(true);
			}
			try {
				s.substring(6);
				check(false);
			} catch (IndexOutOfBoundsException e) {
				check(true);
			}
			check("hamburger".substring(4, 8).equals("urge"));
			check("smiles".substring(1, 5).equals("mile"));

			//73
			try {
				s.concat(null);
				check(false);
			} catch (NullPointerException e) {
				check(true);
			}
			check("cares".concat("s").equals("caress"));
			check("to".concat("get").concat("her").equals("together"));

			check("mesquite in your cellar".replace('e', 'o').equals("mosquito in your collar"));
			check("the war of baronets".replace('r', 'y').equals("the way of bayonets"));
			check("sparring with a purple porpoise".replace('p', 't').equals("starring with a turtle tortoise"));
			check("JonL".replace('q', 'x').equals("JonL"));

			check("ABc".toLowerCase().equals("abc"));
			check("ABc".toUpperCase().equals("ABC"));
			byte[] b = {0, 20, 97, 98, 99, 13, 10};
			check(new String(b).trim().equals("abc"));
			check(s.toString() == s);

			check(s.toCharArray().length == 3);
			check(s.toCharArray()[0] == 'a');
			check(s.toCharArray()[1] == 'ś');

			check(String.valueOf((Object)null).equals("null"));
			check(String.valueOf(s.toCharArray()).equals(s));
			try {
				String.valueOf(null);
				check(false);
			} catch (NullPointerException e) {
				check(true);
			}
			check(String.valueOf(in2).equals("faścg"));
			check(String.valueOf(in2, 1, 2).equals("aś"));
			check(String.valueOf(true).equals("true"));
			check(String.valueOf(false).equals("false"));
			check(String.valueOf(' ').equals(" "));
			check(String.valueOf(2000001).equals("2000001"));
			check(String.valueOf(2000001000000L).equals("2000001000000"));
			check(String.valueOf(1.5f).equals("1.5"));
			check(String.valueOf(1.5555).equals("1.5555"));
			*/
		} catch (Exception e) {
			e.printStackTrace();
			check(false);
		}
	}

    public static void main(String[] args) {
	(new TestString()).main();
	System.out.println("DONE!");
    }
}
