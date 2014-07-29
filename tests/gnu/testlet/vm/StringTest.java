package gnu.testlet.vm;

import gnu.testlet.*;

import java.io.*;
import java.util.Date;

public class StringTest implements Testlet {
	public void test(TestHarness th) {
		try {
			String s = new String();
			th.check(s.equals(""));
			s = new String("test");
			th.check(s.equals("test"));
			try {
				char[] nil = null;
				th.fail(new String(nil));
			} catch (NullPointerException e) {
				th.check(true);
			}
			char[] in = {'a', 'ś', 'c'};
			s = new String(in);
			char[] in2 = {'f', 'a', 'ś', 'c', 'g'};
			String s2 = new String(in2, 1, 3);
			th.check(s.equals(s2));
			th.check(s2.length() == 3);
			try {
			    th.fail(new Character(s2.charAt(-1)));
			} catch (IndexOutOfBoundsException e) {
				th.check(true);
			}
			try {
			    th.fail(new Character(s2.charAt(3)));
			} catch (IndexOutOfBoundsException e) {
				th.check(true);
			}
			th.check(s2.charAt(1) == 'ś');
			char[] dst = new char[15];
			try {
				s.getChars(0, 0, null, 0);
				th.check(false);
			} catch (NullPointerException e) {
				th.check(true);
			}
			//10
			try {
				s.getChars(-1, 3, dst, 0);
				th.check(false);
			} catch (IndexOutOfBoundsException e) {
				th.check(true);
			}
			try {
				s.getChars(4, 3, dst, 0);
				th.check(false);
			} catch (IndexOutOfBoundsException e) {
				th.check(true);
			}
			try {
				s.getChars(0, 4, dst, 0);
				th.check(false);
			} catch (IndexOutOfBoundsException e) {
				th.check(true);
			}
			try {
				s.getChars(0, 3, dst, -1);
				th.check(false);
			} catch (IndexOutOfBoundsException e) {
				th.check(true);
			}
			try {
				s.getChars(0, 3, dst, 13);
				th.check(false);
			} catch (IndexOutOfBoundsException e) {
				th.check(true);
			}
			dst[0] = 'Q';
			s.getChars(3, 3, dst, 0);
			th.check(dst[0] == 'Q');
			s.getChars(1, 3, dst, 5);
			th.check(dst[5] == 'ś');
			th.check(dst[6] == 'c');
			try {
				s.getBytes("bdflsjl");
				th.check(false);
			} catch (UnsupportedEncodingException e) {
				th.check(true);
			}
			th.check(s.getBytes("utf-8")[0] == 97);
			//20
			th.check(s.getBytes("utf-8")[1] == -59);
			th.check(s.getBytes("utf-8")[2] == -101);
			th.check(s.getBytes("utf-8")[3] == 99);
			th.check(s.getBytes("utf-8").length == 4);
			th.check(s.getBytes().length == 4);
			th.check(!s.equals(null));
			th.check(s.equalsIgnoreCase("AŚC"));
			th.check(!s.equalsIgnoreCase("fŚC"));
			try {
				s.compareTo(null);
				th.check(false);
			} catch (NullPointerException e) {
				th.check(true);
			}
			th.check(s.compareTo("aśc") == 0);
			th.check(s.compareTo("a") == 2);
			th.check(s.compareTo("aścff") == -2);
			th.check(s.compareTo("aśa") == 2);
			th.check(s.compareTo("aśb") == 1);
			th.check(s.compareTo("aśd") == -1);
			th.check(s.compareTo("aśe") == -2);

			th.check(s.regionMatches(false, 0, "aśc", 0, 3));
			th.check(s.regionMatches(true, 0, "aśC", 0, 3));
			th.check(!s.regionMatches(false, -1, "aśc", 0, 3));
			th.check(!s.regionMatches(false, 0, "aśc", -1, 3));
			th.check(!s.regionMatches(false, 1, "aśc", 0, 3));
			th.check(!s.regionMatches(false, 0, "aśc", 1, 3));
			th.check(!s.regionMatches(false, 0, "aśC", 1, 3));
			th.check(!s.regionMatches(true, 0, "aśD", 1, 3));
			th.check(!s.regionMatches(false, 1, "a", 0, 1));

			try {
				s.startsWith(null);
				th.check(false);
			} catch (NullPointerException e) {
				th.check(true);
			}

			th.check(s.startsWith("aś", 0));
			th.check(s.startsWith("śc", 1));
			th.check(!s.startsWith("ab", 0));
			th.check(s.startsWith("aś"));
			th.check(s.endsWith("śc"));
			try {
				s.endsWith(null);
				th.check(false);
			} catch (NullPointerException e) {
				th.check(true);
			}
			th.check(s.hashCode() == 104073);
			th.check("aaabab".indexOf('b') == 3);
			th.check("aaabab".indexOf('c') == -1);
			th.check("aaabab".indexOf('a') == 0);
			th.check("aaabab".indexOf('b', 3) == 3);
			th.check("aaabab".indexOf('b', 4) == 5);
			th.check("aaabab".indexOf('b', 6) == -1);
			th.check("aaabab".indexOf('c', 3) == -1);
			th.check("aaabab".indexOf('a', 3) == 4);

			try {
				s.indexOf(null);
				th.check(false);
			} catch (NullPointerException e) {
				th.check(true);
			}
			th.check(s.indexOf("śc") == 1);
			th.check(s.indexOf("c", 1) == 2);
			th.check(s.indexOf("c", 4) == -1);
			th.check(s.indexOf("abc", 0) == -1);

			th.check("unhappy".substring(2).equals("happy"));
			th.check("Harbison".substring(3).equals("bison"));
			th.check("emptiness".substring(9).equals(""));
			try {
				s.substring(-1);
				th.check(false);
			} catch (IndexOutOfBoundsException e) {
				th.check(true);
			}
			try {
				s.substring(6);
				th.check(false);
			} catch (IndexOutOfBoundsException e) {
				th.check(true);
			}
			th.check("hamburger".substring(4, 8).equals("urge"));
			th.check("smiles".substring(1, 5).equals("mile"));

			//73
			try {
				s.concat(null);
				th.check(false);
			} catch (NullPointerException e) {
				th.check(true);
			}
			th.check("cares".concat("s").equals("caress"));
			th.check("to".concat("get").concat("her").equals("together"));

			th.check("mesquite in your cellar".replace('e', 'o').equals("mosquito in your collar"));
			th.check("the war of baronets".replace('r', 'y').equals("the way of bayonets"));
			th.check("sparring with a purple porpoise".replace('p', 't').equals("starring with a turtle tortoise"));
			th.check("JonL".replace('q', 'x').equals("JonL"));

			th.check("ABc".toLowerCase().equals("abc"));
			th.check("ABc".toUpperCase().equals("ABC"));
			byte[] b = {0, 20, 97, 98, 99, 13, 10};
			th.check(new String(b).trim().equals("abc"));
			th.check(s.toString() == s);

			th.check(s.toCharArray().length == 3);
			th.check(s.toCharArray()[0] == 'a');
			th.check(s.toCharArray()[1] == 'ś');

			th.check(String.valueOf((Object)null).equals("null"));
			th.check(String.valueOf(s.toCharArray()).equals(s));
			try {
				String.valueOf(null);
				th.check(false);
			} catch (NullPointerException e) {
				th.check(true);
			}
			th.check(String.valueOf(in2).equals("faścg"));
			th.check(String.valueOf(in2, 1, 2).equals("aś"));
			th.check(String.valueOf(true).equals("true"));
			th.check(String.valueOf(false).equals("false"));
			th.check(String.valueOf(' ').equals(" "));
			th.check(String.valueOf(2000001).equals("2000001"));
			th.check(String.valueOf(2000001000000L).equals("2000001000000"));
			th.check(String.valueOf(1.5f).equals("1.5"));
			th.check(String.valueOf(1.5555).equals("1.5555"));
		} catch (Exception e) {
			e.printStackTrace();
			th.check(false);
		}
	}
}
