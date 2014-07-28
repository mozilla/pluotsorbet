package gnu.testlet.vm;

import gnu.testlet.*;

public class BytecodesTest implements Testlet {
    public void test(TestHarness th) {
		String s = null;
		th.check(s == null);
		int i = (new Integer(-1)).intValue();
		th.check(i == -1);
		i = 0;
		th.check(i == 0);
		i = 1;
		th.check(i == 1);
		i = 2;
		th.check(i == 2);
		i = 3;
		th.check(i == 3);
		i = 4;
		th.check(i == 4);
		i = 5;
		th.check(i == 5);
		long l = 0;
		th.check(l == 0);
		l = 1;
		th.check(l == 1);
		byte b = -113;
		th.check(b == -113);
		b = 113;
		th.check(b == 113);
		short q = -424;
		th.check(q == -424);
		q = 1424;
		th.check(q == 1424);
		s = "test";
		th.check(s.equals("test"));
		//16
		l = 542434;
		th.check(l == 542434);
		l = l + 566;
		th.check(l == 543000);
		String s2 = s;
		th.check(s2.equals(s));
		th.check(s2.equals("test"));
		byte[] bb = new byte[9];
		for (byte j = 0; j < bb.length; j++) {
			bb[j] = j;
		}
		for (byte j = 0; j < bb.length; j++) {
			th.check(bb[j] == j);
		}
		String[] ss = new String[4];
		ss[0] = "s";
		for (byte j = 1; j < ss.length; j++) {
			ss[j] = ss[j-1] + 's';
		}
		for (byte j = 0; j < ss.length; j++) {
			th.check(ss[j].length() == j + 1);
			th.check(ss[j].length() - 1 == ss[j].lastIndexOf('s'));
		}
		//37
		th.check(i >> 2 == 1);
		th.check(i << 2 == 20);
		th.check(i >> 6 == 0);
		//40
		String[] as = new String[]{"abc", "smt", "123"};
		th.check(as[1].equals("smt"));
		try {
		    th.fail(as[4]);
		} catch (ArrayIndexOutOfBoundsException e) {
			th.check(true);
		}
		as = null;
		try {
		    th.fail(as[0]);
		} catch (NullPointerException e) {
			th.check(true);
		}
		try {
			as[0] = "test";
			th.check(false);
		} catch (NullPointerException e) {
			th.check(true);
		}
		as = new String[2];
		as[0] = "test";
		th.check(as[0].equals("test"));
		Object x[]=new String[3];
		x[0] = "test";
		try {
			x[1] = new Integer(0);
			th.check(false);
		} catch (ArrayStoreException e) {
			th.check(true);
		}
		try {
			as[-1] = "test";
			th.check(false);
		} catch (ArrayIndexOutOfBoundsException e) {
			th.check(true);
		}
		try {
			as = new String[-1];
			th.check(false);
		} catch (NegativeArraySizeException e) {
			th.check(true);
		}
		th.check(x.length == 3);
		as = null;
		try {
			
		    th.fail("" + as.length);
		} catch (NullPointerException e) {
			th.check(true);
		}
	}
}
