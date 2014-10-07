import javax.microedition.io.*;
import javax.microedition.io.file.*;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.io.*;

class DupMore {
    private long longValue = 4;
    
    private long[] longArr = { 1 };
    
    // since this is not static, the 'this' operand causes javac to generate dup_x1
    public long dup2_x1() {
        return longValue++;
    }
    
    void popLong(long a) {}
    
    // the array ref operand makes this a dup_x2
    public long dup2_x2() {
        return longArr[0]++;
    }
    
    private int intArr[] = { 4 };
    
    public int dup_x2() {
        return intArr[0]++;
    }
}

class A {
    private int a;
    protected int b;
    
    void put(int i) {
        this.a = i;
        this.b = i;
    }
    
    int get() {
        return this.a;
    }
    
    int getb() {
        return this.b;
    }
}

class B extends A {
    private int a;
    public int b;
    
    void puts(int i) {
        this.a = i;
        super.b = i;
    }
    
    int gets() {
        return this.a;
    }
}

class JITTest {
    // Funzione interpretata
    public int prova() {
        return 9;
    }

    public int prova2(int val) {
        return val * val;
    }

    public int prova2static(int val) {
        return val * val + 1;
    }

    // Funzione compilata 2
    public int ciao2() {
        return 7;
    }

    public void lanciaEccezione() throws Exception {
        throw new Exception("LANCIAECCEZIONE");
    }

    public void rilanciaEccezione() throws Exception {
        lanciaEccezione();
    }

    public void lanciaECatturaEccezione() {
        try {
            throw new Exception("LANCIAECCEZIONE");
        } catch (Exception e) {
            
        }
    }

    public void catturaEccezione() {
        try {
            lanciaEccezione();
            System.out.println("catturaEccezione: FAIL 1");
        } catch (Exception e) {
            
        }
    }

    public void variTestBasicEccezioni() {
        
    }

    public void provaVoid() {
    }

    public static native int provanative();

    // Funzione compilata
    public int ciao() {
        int c = 0;
        for (int i = 0; i < 100; i++) {
            c = prova2(i) + prova();
        }
        return c;
    }

    public static void testDup() {
        // do it twice so we know the adding was done correctly in the first call
        if (dup2() != 5) {
            System.out.println("testDup: FAIL 1");
            return;
        }
        if (dup2() != 6) {
            System.out.println("testDup: FAIL 2");
            return;
        }
        
        DupMore d = new DupMore();
        if (d.dup2_x1() != 4) {
            System.out.println("testDup: FAIL 3");
            return;
        }
        if (d.dup2_x1() != 5) {
            System.out.println("testDup: FAIL 4");
            return;
        }

        if (d.dup2_x2() != 1) {
            System.out.println("testDup: FAIL 5");
            return;
        }
        if (d.dup2_x2() != 2) {
            System.out.println("testDup: FAIL 6");
            return;
        }

        if (d.dup_x2() != 4) {
            System.out.println("testDup: FAIL 7");
            return;
        }
        if (d.dup_x2() != 5) {
            System.out.println("testDup: FAIL 8");
            return;
        }
    }
    
    private static long longValue = 5;
    
    // this function generates the dup2 instruction
    public static long dup2() {
        return longValue++;
    }

    public static void testArray() {
        byte b1[][] = new byte[3][4];
        b1[1][2] = 5;
        if (b1[1][2] != 5) {
            System.out.println("testArray: FAIL 1");
            return;
        }
        Object o[][] = new Object[5][5];
        o[1][1] = new Integer(5);
        if (!o[1][1].toString().equals("5")) {
            System.out.println("testArray: FAIL 2");
            return;
        }
        Object[] a = new Object[5];
        a[3] = "smt";
        if (!a[3].equals("smt")) {
            System.out.println("testArray: FAIL 3");
            return;
        }
        Object[][] b = new Object[6][];
        b[1] = new String[3];
        if (b[1][0] != null) {
            System.out.println("testArray: FAIL 4");
            return;
        }
        if (b[0] != null) {
            System.out.println("testArray: FAIL 5");
            return;
        }
        String[][] x = new String[2][3];
        for (int i = 0; i < 2; ++i)
            for (int j = 0; j < 3; ++j)
                x[i][j] = "" + i + " " + j;
        if (!x[0][0].equals("0 0")) {
            System.out.println("testArray: FAIL 6");
            return;
        }
        if (!x[0][1].equals("0 1")) {
            System.out.println("testArray: FAIL 7");
            return;
        }
        if (!x[0][2].equals("0 2")) {
            System.out.println("testArray: FAIL 8");
            return;
        }
        if (!x[1][0].equals("1 0")) {
            System.out.println("testArray: FAIL 9");
            return;
        }
        if (!x[1][1].equals("1 1")) {
            System.out.println("testArray: FAIL 10");
            return;
        }
        if (!x[1][2].equals("1 2")) {
            System.out.println("testArray: FAIL 11");
            return;
        }
    }

    public static void testConditions() {
		String a, b, c;
		a = "smt";
		b = a;
		c = "smt2";
		int d = 9;
		int e = -9;
		int f = 0;
		String g = null;
        if (a != b) {
            System.out.println("testConditions: FAIL 1");
            return;
        }
        if (a == c) {
            System.out.println("testConditions: FAIL 2");
            return;
        }
        if (a == null) {
            System.out.println("testConditions: FAIL 3");
            return;
        }
        if (g != null) {
            System.out.println("testConditions: FAIL 4");
            return;
        }
        if (d != 9) {
            System.out.println("testConditions: FAIL 5");
            return;
        }
        if (d <= 7) {
            System.out.println("testConditions: FAIL 6");
            return;
        }
        if (d < 9) {
            System.out.println("testConditions: FAIL 7");
            return;
        }
        if (d < 7) {
            System.out.println("testConditions: FAIL 8");
            return;
        }
        if (7 >= d) {
            System.out.println("testConditions: FAIL 9");
            return;
        }
        if (9 > d) {
            System.out.println("testConditions: FAIL 10");
            return;
        }
        if (7 > d) {
            System.out.println("testConditions: FAIL 11");
            return;
        }
        if (d == 6) {
            System.out.println("testConditions: FAIL 12");
            return;
        }
        if (d < 0) {
            System.out.println("testConditions: FAIL 13");
            return;
        }
        if (e > 0) {
            System.out.println("testConditions: FAIL 14");
            return;
        }
        if (f < 0) {
            System.out.println("testConditions: FAIL 15");
            return;
        }
        if (f > 0) {
            System.out.println("testConditions: FAIL 16");
            return;
        }
        if (f != 0) {
            System.out.println("testConditions: FAIL 17");
            return;
        }
        if (e == 0) {
            System.out.println("testConditions: FAIL 18");
            return;
        }
        if (d == 0) {
            System.out.println("testConditions: FAIL 19");
            return;
        }
	}

    static void testExceptions() {
        boolean caught = false;
        try {
            throw new RuntimeException("Foo");
        } catch (Exception e) {
            if (!e.getMessage().equals("Foo")) {
                System.out.println("testExceptions: FAIL 1");
                return;
            }
            caught = true;
        }
        if (!caught) {
            System.out.println("testExceptions: FAIL 2");
            return;
        }

        int i = 8;
        try {
            i /= 0;
        } catch (Exception e) {
            i++;
        }
        if (i != 9) {
            System.out.println("testExceptions: FAIL 3");
            return;
        }
    }

    public static void testSwitch() {
        int i = 1;
        switch (i) {
            case 1:
                break;
            case 2:
                System.out.println("testSwitch: FAIL 1");
                break;
            case 3:
                System.out.println("testSwitch: FAIL 2");
                break;
            default:
                System.out.println("testSwitch: FAIL 3");
        }

        i = 2;
        switch (i) {
            case 1:
                System.out.println("testSwitch: FAIL 1");
                break;
            case 2:
                break;
            case 3:
                System.out.println("testSwitch: FAIL 2");
                break;
            default:
                System.out.println("testSwitch: FAIL 3");
        }

        i = 3;
        switch (i) {
            case 1:
                System.out.println("testSwitch: FAIL 1");
                break;
            case 2:
                System.out.println("testSwitch: FAIL 2");
                break;
            case 3:
                break;
            default:
                System.out.println("testSwitch: FAIL 3");
        }

        i = 10;
        switch (i) {
            case 1:
                System.out.println("testSwitch: FAIL 1");
                break;
            case 2:
                System.out.println("testSwitch: FAIL 2");
                break;
            case 3:
                System.out.println("testSwitch: FAIL 3");
                break;
            default:
                
        }
    }

    public static void testBytecodes() {
		String s = null;
		if (s != null) {
            System.out.println("testBytecodes: FAIL 1");
            return;
        }
		int i = (new Integer(-1)).intValue();
		if (i != -1) {
            System.out.println("testBytecodes: FAIL 2");
            return;
        }
		i = 0;
		if (i != 0) {
            System.out.println("testBytecodes: FAIL 3");
            return;
        }
		i = 1;
		if (i != 1) {
            System.out.println("testBytecodes: FAIL 4");
            return;
        }
		i = 2;
		if (i != 2) {
            System.out.println("testBytecodes: FAIL 5");
            return;
        }
		i = 3;
		if (i != 3) {
            System.out.println("testBytecodes: FAIL 6");
            return;
        }
		i = 4;
		if (i != 4) {
            System.out.println("testBytecodes: FAIL 7");
            return;
        }
		i = 5;
		if (i != 5) {
            System.out.println("testBytecodes: FAIL 8");
            return;
        }
		long l = 0;
		if (l != 0) {
            System.out.println("testBytecodes: FAIL 9");
            return;
        }
		l = 1;
		if (l != 1) {
            System.out.println("testBytecodes: FAIL 10");
            return;
        }
		byte b = -113;
        if (b != -113) {
            System.out.println("testBytecodes: FAIL 11");
            return;
        }
		b = 113;
		if (b != 113) {
            System.out.println("testBytecodes: FAIL 12");
            return;
        }
		short q = -424;
		if (q != -424) {
            System.out.println("testBytecodes: FAIL 13");
            return;
        }
		q = 1424;
		if (q != 1424) {
            System.out.println("testBytecodes: FAIL 14");
            return;
        }
		s = "test";
		if (!s.equals("test")) {
            System.out.println("testBytecodes: FAIL 15");
            return;
        }
		//16
		l = 542434;
		if (l != 542434) {
            System.out.println("testBytecodes: FAIL 16");
            return;
        }
		l = l + 566;
		if (l != 543000) {
            System.out.println("testBytecodes: FAIL 17");
            return;
        }
		String s2 = s;
		if (!s2.equals(s)) {
            System.out.println("testBytecodes: FAIL 18");
            return;
        }
        if (!s2.equals("test")) {
            System.out.println("testBytecodes: FAIL 19");
            return;
        }
		byte[] bb = new byte[9];
		for (byte j = 0; j < bb.length; j++) {
			bb[j] = j;
		}
		for (byte j = 0; j < bb.length; j++) {
			if (bb[j] != j) {
                System.out.println("testBytecodes: FAIL 20");
                return;
            }
		}
		String[] ss = new String[4];
		ss[0] = "s";
		for (byte j = 1; j < ss.length; j++) {
			ss[j] = ss[j-1] + 's';
		}
		for (byte j = 0; j < ss.length; j++) {
			if (ss[j].length() != j + 1) {
                System.out.println("testBytecodes: FAIL 21");
                return;
            }
			if (ss[j].length() - 1 != ss[j].lastIndexOf('s')) {
                System.out.println("testBytecodes: FAIL 22");
                return;
            }
		}
		//37
		if (i >> 2 != 1) {
            System.out.println("testBytecodes: FAIL 23");
            return;
        }
        if (i << 2 != 20) {
            System.out.println("testBytecodes: FAIL 24");
            return;
        }
		if (i >> 6 != 0) {
            System.out.println("testBytecodes: FAIL 25");
            return;
        }
		//40
		String[] as = new String[]{"abc", "smt", "123"};
		if (!as[1].equals("smt")) {
            System.out.println("testBytecodes: FAIL 26");
            return;
        }
		try {
            String stasd = as[4];
            System.out.println("testBytecodes: FAIL 27");
            return;
		} catch (ArrayIndexOutOfBoundsException e) {

		}
		as = null;
		try {
            String stasd = as[0];
		    System.out.println("testBytecodes: FAIL 28");
            return;
		} catch (NullPointerException e) {

		}
		try {
			as[0] = "test";
            System.out.println("testBytecodes: FAIL 29");
            return;
		} catch (NullPointerException e) {

		}
		as = new String[2];
		as[0] = "test";
		if (!as[0].equals("test")) {
            System.out.println("testBytecodes: FAIL 30");
            return;
        }
		Object x[]=new String[3];
		x[0] = "test";
		try {
			x[1] = new Integer(0);
			System.out.println("testBytecodes: FAIL 31");
            return;
		} catch (ArrayStoreException e) {

		}
		try {
			as[-1] = "test";
			System.out.println("testBytecodes: FAIL 32");
            return;
		} catch (ArrayIndexOutOfBoundsException e) {

		}
		try {
			as = new String[-1];
			System.out.println("testBytecodes: FAIL 33");
            return;
		} catch (NegativeArraySizeException e) {

		}
		if (x.length != 3) {
            System.out.println("testBytecodes: FAIL 34");
            return;
        }
		as = null;
		try {
            int ciao = as.length;
            System.out.println("testBytecodes: FAIL 35");
            return;
		} catch (NullPointerException e) {

		}
	}

    public static void testString() {
		try {
			String s = new String();
            if (!s.equals("")) {
                System.out.println("testString: FAIL 1");
                return;
            }
			s = new String("test");
            if (!s.equals("test")) {
                System.out.println("testString: FAIL 2");
                return;
            }
			try {
				char[] nil = null;
                new String(nil);
                System.out.println("testString: FAIL 3");
                return;
			} catch (NullPointerException e) {

			}
			char[] in = {'a', 'ś', 'c'};
			s = new String(in);
			char[] in2 = {'f', 'a', 'ś', 'c', 'g'};
			String s2 = new String(in2, 1, 3);
            if (!s.equals(s2)) {
                System.out.println("testString: FAIL 4");
                return;
            }
            if (s2.length() != 3) {
                System.out.println("testString: FAIL 5");
                return;
            }
			try {
                new Character(s2.charAt(-1));
                System.out.println("testString: FAIL 6");
                return;
			} catch (IndexOutOfBoundsException e) {

			}
			try {
                new Character(s2.charAt(3));
                System.out.println("testString: FAIL 7");
                return;
			} catch (IndexOutOfBoundsException e) {

			}
			if (s2.charAt(1) != 'ś') {
                System.out.println("testString: FAIL 8");
                return;
            }
			char[] dst = new char[15];
			try {
				s.getChars(0, 0, null, 0);
                System.out.println("testString: FAIL 9");
                return;
			} catch (NullPointerException e) {

			}
			//10
			try {
				s.getChars(-1, 3, dst, 0);
                System.out.println("testString: FAIL 10");
                return;
			} catch (IndexOutOfBoundsException e) {

			}
			try {
				s.getChars(4, 3, dst, 0);
                System.out.println("testString: FAIL 11");
                return;
			} catch (IndexOutOfBoundsException e) {

			}
			try {
				s.getChars(0, 4, dst, 0);
                System.out.println("testString: FAIL 12");
                return;
			} catch (IndexOutOfBoundsException e) {

			}
			try {
				s.getChars(0, 3, dst, -1);
                System.out.println("testString: FAIL 13");
                return;
			} catch (IndexOutOfBoundsException e) {

			}
			try {
				s.getChars(0, 3, dst, 13);
                System.out.println("testString: FAIL 14");
                return;
			} catch (IndexOutOfBoundsException e) {

			}
			dst[0] = 'Q';
			s.getChars(3, 3, dst, 0);
			if (dst[0] != 'Q') {
                System.out.println("testString: FAIL 15");
                return;
            }
			s.getChars(1, 3, dst, 5);
			if (dst[5] != 'ś') {
                System.out.println("testString: FAIL 16");
                return;
            }
			if (dst[6] != 'c') {
                System.out.println("testString: FAIL 17");
                return;
            }
			try {
				s.getBytes("bdflsjl");
                System.out.println("testString: FAIL 18");
                return;
			} catch (UnsupportedEncodingException e) {

			}
			if (s.getBytes("utf-8")[0] != 97) {
                System.out.println("testString: FAIL 19");
                return;
            }
			//20
			if (s.getBytes("utf-8")[1] != -59) {
                System.out.println("testString: FAIL 20");
                return;
            }
			if (s.getBytes("utf-8")[2] != -101) {
                System.out.println("testString: FAIL 21");
                return;
            }
			if (s.getBytes("utf-8")[3] != 99) {
                System.out.println("testString: FAIL 22");
                return;
            }
			if (s.getBytes("utf-8").length != 4) {
                System.out.println("testString: FAIL 23");
                return;
            }
			if (s.getBytes().length != 4) {
                System.out.println("testString: FAIL 24");
                return;
            }
			if (s.equals(null)) {
                System.out.println("testString: FAIL 25");
                return;
            }
			if (!s.equalsIgnoreCase("AŚC")) {
                System.out.println("testString: FAIL 26");
                return;
            }
			if (s.equalsIgnoreCase("fŚC")) {
                System.out.println("testString: FAIL 27");
                return;
            }
			try {
				s.compareTo(null);
				System.out.println("testString: FAIL 28");
                return;
			} catch (NullPointerException e) {

			}
			if (s.compareTo("aśc") != 0) {
                System.out.println("testString: FAIL 29");
                return;
            }
			if (s.compareTo("a") != 2) {
                System.out.println("testString: FAIL 30");
                return;
            }
			if (s.compareTo("aścff") != -2) {
                System.out.println("testString: FAIL 31");
                return;
            }
			if (s.compareTo("aśa") != 2) {
                System.out.println("testString: FAIL 32");
                return;
            }
			if (s.compareTo("aśb") != 1) {
                System.out.println("testString: FAIL 33");
                return;
            }
			if (s.compareTo("aśd") != -1) {
                System.out.println("testString: FAIL 34");
                return;
            }
			if (s.compareTo("aśe") != -2) {
                System.out.println("testString: FAIL 35");
                return;
            }
            
			if (!s.regionMatches(false, 0, "aśc", 0, 3)) {
                System.out.println("testString: FAIL 36");
                return;
            }
			if (!s.regionMatches(true, 0, "aśC", 0, 3)) {
                System.out.println("testString: FAIL 37");
                return;
            }
			if (s.regionMatches(false, -1, "aśc", 0, 3)) {
                System.out.println("testString: FAIL 38");
                return;
            }
			if (s.regionMatches(false, 0, "aśc", -1, 3)) {
                System.out.println("testString: FAIL 39");
                return;
            }
			if (s.regionMatches(false, 1, "aśc", 0, 3)) {
                System.out.println("testString: FAIL 40");
                return;
            }
			if (s.regionMatches(false, 0, "aśc", 1, 3)) {
                System.out.println("testString: FAIL 41");
                return;
            }
			if (s.regionMatches(false, 0, "aśC", 1, 3)) {
                System.out.println("testString: FAIL 42");
                return;
            }
			if (s.regionMatches(true, 0, "aśD", 1, 3)) {
                System.out.println("testString: FAIL 43");
                return;
            }
			if (s.regionMatches(false, 1, "a", 0, 1)) {
                System.out.println("testString: FAIL 44");
                return;
            }
            
			try {
				s.startsWith(null);
				System.out.println("testString: FAIL 45");
                return;
			} catch (NullPointerException e) {

			}
            
			if (!s.startsWith("aś", 0)) {
                System.out.println("testString: FAIL 46");
                return;
            }
			if (!s.startsWith("śc", 1)) {
                System.out.println("testString: FAIL 47");
                return;
            }
			if (s.startsWith("ab", 0)) {
                System.out.println("testString: FAIL 48");
                return;
            }
			if (!s.startsWith("aś")) {
                System.out.println("testString: FAIL 49");
                return;
            }
			if (!s.endsWith("śc")) {
                System.out.println("testString: FAIL 50");
                return;
            }
			try {
				s.endsWith(null);
				System.out.println("testString: FAIL 51");
                return;
			} catch (NullPointerException e) {

			}
			if (s.hashCode() != 104073) {
                System.out.println("testString: FAIL 52");
                return;
            }
			if ("aaabab".indexOf('b') != 3) {
                System.out.println("testString: FAIL 53");
                return;
            }
			if ("aaabab".indexOf('c') != -1) {
                System.out.println("testString: FAIL 54");
                return;
            }
			if ("aaabab".indexOf('a') != 0) {
                System.out.println("testString: FAIL 55");
                return;
            }
			if ("aaabab".indexOf('b', 3) != 3) {
                System.out.println("testString: FAIL 56");
                return;
            }
			if ("aaabab".indexOf('b', 4) != 5) {
                System.out.println("testString: FAIL 57");
                return;
            }
			if ("aaabab".indexOf('b', 6) != -1) {
                System.out.println("testString: FAIL 58");
                return;
            }
			if ("aaabab".indexOf('c', 3) != -1) {
                System.out.println("testString: FAIL 59");
                return;
            }
			if ("aaabab".indexOf('a', 3) != 4) {
                System.out.println("testString: FAIL 60");
                return;
            }
            
			try {
				s.indexOf(null);
				System.out.println("testString: FAIL 61");
                return;
			} catch (NullPointerException e) {

			}
			if (s.indexOf("śc") != 1) {
                System.out.println("testString: FAIL 62");
                return;
            }
			if (s.indexOf("c", 1) != 2) {
                System.out.println("testString: FAIL 63");
                return;
            }
			if (s.indexOf("c", 4) != -1) {
                System.out.println("testString: FAIL 64");
                return;
            }
			if (s.indexOf("abc", 0) != -1) {
                System.out.println("testString: FAIL 65");
                return;
            }
            
			if (!"unhappy".substring(2).equals("happy")) {
                System.out.println("testString: FAIL 66");
                return;
            }
			if (!"Harbison".substring(3).equals("bison")) {
                System.out.println("testString: FAIL 67");
                return;
            }
			if (!"emptiness".substring(9).equals("")) {
                System.out.println("testString: FAIL 68");
                return;
            }
			try {
				s.substring(-1);
				System.out.println("testString: FAIL 69");
                return;
			} catch (IndexOutOfBoundsException e) {

			}
			try {
				s.substring(6);
				System.out.println("testString: FAIL 70");
                return;
			} catch (IndexOutOfBoundsException e) {

			}
			if (!"hamburger".substring(4, 8).equals("urge")) {
                System.out.println("testString: FAIL 71");
                return;
            }
			if (!"smiles".substring(1, 5).equals("mile")) {
                System.out.println("testString: FAIL 72");
                return;
            }
            
			//73
			try {
				s.concat(null);
				System.out.println("testString: FAIL 73");
                return;
			} catch (NullPointerException e) {

			}
			if (!"cares".concat("s").equals("caress")) {
                System.out.println("testString: FAIL 74");
                return;
            }
			if (!"to".concat("get").concat("her").equals("together")) {
                System.out.println("testString: FAIL 75");
                return;
            }
            
			if (!"mesquite in your cellar".replace('e', 'o').equals("mosquito in your collar")) {
                System.out.println("testString: FAIL 76");
                return;
            }
			if (!"the war of baronets".replace('r', 'y').equals("the way of bayonets")) {
                System.out.println("testString: FAIL 77");
                return;
            }
			if (!"sparring with a purple porpoise".replace('p', 't').equals("starring with a turtle tortoise")) {
                System.out.println("testString: FAIL 78");
                return;
            }
			if (!"JonL".replace('q', 'x').equals("JonL")) {
                System.out.println("testString: FAIL 79");
                return;
            }
            
			if (!"ABc".toLowerCase().equals("abc")) {
                System.out.println("testString: FAIL 80");
                return;
            }
			if (!"ABc".toUpperCase().equals("ABC")) {
                System.out.println("testString: FAIL 81");
                return;
            }
			byte[] b = {0, 20, 97, 98, 99, 13, 10};
			if (!new String(b).trim().equals("abc")) {
                System.out.println("testString: FAIL 82");
                return;
            }
			if (s.toString() != s) {
                System.out.println("testString: FAIL 83");
                return;
            }
            
			if (s.toCharArray().length != 3) {
                System.out.println("testString: FAIL 84");
                return;
            }
			if (s.toCharArray()[0] != 'a') {
                System.out.println("testString: FAIL 85");
                return;
            }
			if (s.toCharArray()[1] != 'ś') {
                System.out.println("testString: FAIL 86");
                return;
            }

			if (!String.valueOf((Object)null).equals("null")) {
                System.out.println("testString: FAIL 87");
                return;
            }
			if (!String.valueOf(s.toCharArray()).equals(s)) {
                System.out.println("testString: FAIL 88");
                return;
            }
			try {
				String.valueOf(null);
				System.out.println("testString: FAIL 89");
                return;
			} catch (NullPointerException e) {

			}
            if (!String.valueOf(in2).equals("faścg")) {
                System.out.println("testString: FAIL 90");
                return;
            }
			if (!String.valueOf(in2, 1, 2).equals("aś")) {
                System.out.println("testString: FAIL 91");
                return;
            }
			if (!String.valueOf(true).equals("true")) {
                System.out.println("testString: FAIL 92");
                return;
            }
			if (!String.valueOf(false).equals("false")) {
                System.out.println("testString: FAIL 93");
                return;
            }
			if (!String.valueOf(' ').equals(" ")) {
                System.out.println("testString: FAIL 94");
                return;
            }
			if (!String.valueOf(2000001).equals("2000001")) {
                System.out.println("testString: FAIL 95");
                return;
            }
			if (!String.valueOf(2000001000000L).equals("2000001000000")) {
                System.out.println("testString: FAIL 96");
                return;
            }
			if (!String.valueOf(1.5f).equals("1.5")) {
                System.out.println("testString: FAIL 97 - " + String.valueOf(1.5f));
                return;
            }
			if (!String.valueOf(1.5555).equals("1.5555")) {
                System.out.println("testString: FAIL 98 - " + String.valueOf(1.5555));
                return;
            }
            
			if ("\0".length() != 1) {
                System.out.println("testString: FAIL 99");
                return;
            }
		} catch (Exception e) {
			e.printStackTrace();
            System.out.println("testString: FAIL 100");
            return;
		}
	}

    public static void testObjects() {
        B a = new B();
		a.put(5);
        if (a.getb() != 5) {
            System.out.println("testObjects: FAIL 1");
            return;
        }
		a.puts(6);
		a.b = 7;
        if (a.get() != 5) {
            System.out.println("testObjects: FAIL 2");
            return;
        }
        if (a.gets() != 6) {
            System.out.println("testObjects: FAIL 3");
            return;
        }
        if (a.getb() != 6) {
            System.out.println("testObjects: FAIL 4");
            return;
        }
        if (a.b != 7) {
            System.out.println("testObjects: FAIL 5");
            return;
        }
    }

    public static void basicTestNative() {
        provanative();
    }

    native static int getInt();
    native static int fromJavaString(String string);
    native static int decodeUtf8(byte[] string);
    
    public static void testNative() {
        if (getInt() != 0xFFFFFFFF) {
            System.out.println("testNative: TODO 1");
        }

        String s = "marco";
        if (!s.substring(0, 0).equals("")) {
            System.out.println("testNative: FAIL 1");
            return;
        }
        if (fromJavaString(s.substring(0, 0)) != fromJavaString("")) {
            System.out.println("testNative: FAIL 2");
            return;
        }
        if (fromJavaString(s.substring(0, 1)) != fromJavaString("m")) {
            System.out.println("testNative: FAIL 3");
            return;
        }
        
        if (fromJavaString("\0") != 1) {
            System.out.println("testNative: FAIL 4");
            return;
        }
        if (decodeUtf8("\0".getBytes()) != 1) {
            System.out.println("testNative: FAIL 5");
            return;
        }
        if (fromJavaString("") != 0) {
            System.out.println("testNative: FAIL 6");
            return;
        }
        if (decodeUtf8("".getBytes()) != 0) {
            System.out.println("testNative: FAIL 7");
            return;
        }
    }

    public static void testInteger() {
		if (!Integer.toString(543).equals("543")) {
            System.out.println("testInteger: FAIL 1");
            return;
        }

		if (!Integer.toString(-543).equals("-543")) {
            System.out.println("testInteger: FAIL 2");
            return;
        }
		if (!Integer.toString(71, 36).equals("1z")) {
            System.out.println("testInteger: FAIL 3");
            return;
        }
		if (!Integer.toString(-71, 36).equals("-1z")) {
            System.out.println("testInteger: FAIL 4");
            return;
        }
		if (!Integer.toString(127, 2).equals("1111111")) {
            System.out.println("testInteger: FAIL 5");
            return;
        }
		if (!Integer.toString(127, 664).equals("127")) {
            System.out.println("testInteger: FAIL 6");
            return;
        }
		if (!Integer.toString(127, -44).equals("127")) {
            System.out.println("testInteger: FAIL 7");
            return;
        }
		if (!Integer.toString(127, 0).equals("127")) {
            System.out.println("testInteger: FAIL 8");
            return;
        }
		/*th.check(Integer.toString(127, 1), ("127"));
		th.check(Integer.toString(127, 1), ("127"));
		th.check(Integer.toHexString(-71), ("ffffffb9"));
		th.check(Integer.toOctalString(-71), ("37777777671"));
		th.check(Integer.toBinaryString(-71), ("11111111111111111111111110111001"));
		th.check(Integer.toString(6546456, 0), ("6546456"));
		try {
			Integer.parseInt(null, 10);
			th.check(false);
		} catch	(NumberFormatException e) {
			th.check(true);
		}
		try {
			Integer.parseInt("", 10);
			th.check(false);
		} catch	(NumberFormatException e) {
			th.check(true);
		}
		try {
			Integer.parseInt("1", 0);
			th.check(false);
		} catch	(NumberFormatException e) {
			th.check(true);
		}
		try {
			Integer.parseInt("1", 55);
			th.check(false);
		} catch	(NumberFormatException e) {
			th.check(true);
		}
		try {
			Integer.parseInt("test", 10);
			th.check(false);
		} catch	(NumberFormatException e) {
			th.check(true);
		}
		try {
			Integer.parseInt("11111111111111111", 0);
			th.check(false);
		} catch	(NumberFormatException e) {
			th.check(true);
		}
		th.check(Integer.parseInt("-1z", 36), -71);
		th.check(Integer.parseInt("0434444310", 10), 434444310);
		th.check(Integer.parseInt("0434444310"), 434444310);
		th.check(Integer.valueOf("-1z", 36).intValue(), -71);
		Integer i = new Integer(2147483647);
		Integer j = new Integer(-2147483648);
		th.check(i.byteValue(), -1);
		th.check(j.byteValue(), -0);
		th.check(i.shortValue(), -1);
		th.check(j.shortValue(), 0);
		th.check(i.intValue(), 2147483647);
		th.check(j.intValue(), -2147483648);
		th.check(i.longValue(), 2147483647L);
		th.check(j.longValue(), -2147483648L);
		th.check(i.doubleValue(),2147483647.0);
		th.check(j.doubleValue(), -2147483648.0);
		th.check(i.hashCode(), 2147483647);
		th.check(j.hashCode(), -2147483648);
		th.check(i, new Integer(2147483647));
		th.check(!j.equals("-2147483648"));*/
	}

    public static int testStringBuffer() {
        try {
            new StringBuffer(-1);
            return 100;
        }
        catch (NegativeArraySizeException e) {
        }
        
        StringBuffer str1 = new StringBuffer();
        if (str1.length() != 0) {
            return 110;
        }
        if (str1.capacity() != 16) {
            return 120;
        }
        if (! str1.toString().equals("")) {
            return 130;
        }
        
        StringBuffer str2 = new StringBuffer("testing");
        if (str2.length() != 7) {
            return 140;
        }
        if (! str2.toString().equals("testing")) {
            return 150;
        }
        
        StringBuffer str4 = new StringBuffer("hi there");
        if (str4.length() != 8) {
            return 160;
        }
        if (! str4.toString().equals("hi there")) {
            return 170;
        }
        if (str4.capacity() != 24) {
            return 180;
        }
        
        StringBuffer strbuf = new StringBuffer(0);
        if (! strbuf.append("hiii").toString().equals("hiii")) {
            return 190;
        }
        
        strbuf = new StringBuffer(10);
        if (strbuf.capacity() != 10) {
            return 200;
        }
        str1 = new StringBuffer("03041965");
        if (! str1.toString().equals("03041965")) {
            return 210;
        }
        
        str1 = new StringBuffer();
        if (! str1.toString().equals("")) {
            return 220;
        }
        
        // capacity tests...
        
        str1 = new StringBuffer("");
        str2 = new StringBuffer("pentiumpentiumpentium");
        if (str1.capacity() != 16) {
            return 230;
        }
        if (str2.capacity() != 37) {
            return 240;
        }
        
        str1.ensureCapacity(17);
        if (str1.capacity() != 34) {
            return 250;
        }
        
        // setLength tests...
        
        str1 = new StringBuffer("ba");
        try {
            str1.setLength(-1);
            return 260;
        }
        catch (IndexOutOfBoundsException e) {
        }
        
        str1. setLength(4);
        if (str1.length() != 4) {
            return 270;
        }
        
        if (str1.charAt(0) != 'b') {
            return 280;
        }
        if (str1.charAt(1) != 'a') {
            return 290;
        }
        if (str1.charAt(2) != '\u0000') {
            return 300;
        }
        if (str1.charAt(3) != '\u0000') {
            return 310;
        }
        
        // charAt tests...
        
        str1 = new StringBuffer("abcd");
        if (str1.charAt(0) != 'a') {
            return 320;
        }
        if (str1.charAt(1) != 'b') {
            return 330;
        }
        if (str1.charAt(2) != 'c') {
            return 340;
        }
        if (str1.charAt(3) != 'd') {
            return 350;
        }
        
        try {
            str1.charAt(4);
            return 360;
        }
        catch (IndexOutOfBoundsException e) {
        }
        
        try {
            str1.charAt(-1);
            return 370;
        }
        catch (IndexOutOfBoundsException e) {
        }
        
        // getChars tests...

        str1 = new StringBuffer("abcdefghijklmn");
        try {
            str1.getChars(0, 3, null, 1);
            return 380;
        }
        catch (NullPointerException e) {
            // dst is null
        }
        
        char[] dst = new char[5];
        try {
            str1.getChars(-1, 3, dst, 1);
            return 390;
        }
        catch (IndexOutOfBoundsException e) {
            // Index out of bounds of StringBuffer - srcOffset
        }
        
        try {
            str1.getChars(4, 3, dst, 3);
            return 400;
        }
        catch (IndexOutOfBoundsException e) {

        }
        
        try {
            str1.getChars(1, 15, dst, 1);
            return 410;
        }
        catch (IndexOutOfBoundsException e) {
        }
        
        try {
            str1.getChars(1, 5, dst, -1);
            return 420;
        }
        catch (IndexOutOfBoundsException e) {
        }
        
        try {
            str1.getChars(1, 10, dst, 1);
            return 430;
        }
        catch (IndexOutOfBoundsException e) {
        }
        
        str1.getChars(0, 5, dst, 0);
        if (dst[0] != 'a') {
            return 440;
        }
        if (dst[1] != 'b') {
            return 450;
        }
        if (dst[2] != 'c') {
            return 460;
        }
        if (dst[3] != 'd') {
            return 470;
        }
        if (dst[4] != 'e') {
            return 480;
        }
        
        dst[0] = dst[1] = dst[2] = dst[3] = dst[4] = ' ';
        str1.getChars(0, 1, dst, 0);
        if (dst[0] != 'a') {
            return 490;
        }
        if (dst[1] != ' ') {
            return 500;
        }
        if (dst[2] != ' ') {
            return 510;
        }
        if (dst[3] != ' ') {
            return 520;
        }
        if (dst[4] != ' ') {
            return 530;
        }
        
        // append tests...
        
        str1 = new StringBuffer();
        Object NULL = null;
        
        if (! str1.append(NULL).toString().equals("null")) {
            return 540;
        }
        if (! str1.append(new Integer(100)).toString().equals("null100")) {
            return 550;
        }
        
        str1 = new StringBuffer("hi");
        str1.append(" there");
        str1.append(" buddy");
        if (! str1.toString().equals("hi there buddy")) {
            return 560;
        }
        
        str1 = new StringBuffer();
        str1 = str1.append("sdljfksdjfklsdjflksdjflkjsdlkfjlsdkjflksdjfklsd");
        if (! str1.toString().equals("sdljfksdjfklsdjflksdjflkjsdlkfjlsdkjflksdjfklsd")) {
            return 570;
        }
        
        str1 = new StringBuffer();
        char[] carr = null;
        try {
            str1 = str1.append(carr);
            return 580;
        }
        catch (NullPointerException e) {
        }
        
        char[] carr1 = {'h', 'i', 't', 'h', 'e', 'r'};
        str1 = new StringBuffer("!");
        str1 = str1.append(carr1);
        if (! str1.toString().equals("!hither")) {
            return 590;
        }
        
        str1 = new StringBuffer();
        try {
            str1 = str1.append(carr1, -1, 3);
            return 600;
        }
        catch (IndexOutOfBoundsException e) {
        }
        
        str1 = new StringBuffer("!");
        str1 = str1.append(carr1, 2, 3);
        if (! str1.toString().equals("!the")) {
            return 610;
        }
        
        str1 = new StringBuffer();
        str1 = str1.append(true);
        if (! str1.toString().equals("true")) {
            return 620;
        }
        str1 = str1.append(false);
        if (! str1.toString().equals("truefalse")) {
            return 630;
        }
        str1 = str1.append(20);
        if (! str1.toString().equals("truefalse20")) {
            return 640;
        }
        
        str1 = new StringBuffer();
        str1 = str1.append(2034L);
        if (! str1.toString().equals("2034")) {
            return 650;
        }
        
        // Wait until we fix the floating point formatting stuff...
        str1 = new StringBuffer();
        str1 = str1.append(12.5f);
        if (! str1.toString().equals("12.5")) {
            System.out.println(">>>" + str1.toString() + "<<<");
            return 660;
        }
        
        str1 = new StringBuffer();
        str1 = str1.append(12.35);
        if (! str1.toString().equals("12.35")) {
            System.out.println(">>>" + str1.toString() + "<<<");
            return 670;
        }
        
        
        // insert tests...

        str1 = new StringBuffer("1234567");
        str1 = str1.insert(5, NULL);
        if (! str1.toString().equals("12345null67")) {
            System.out.println(">>>" + str1 + "<<<");
            return 680;
        }
        
        try {
            str1 = str1.insert(-1, new Object());
            return 690;
        }
        catch (IndexOutOfBoundsException e) {
        }
        
        str1 = new StringBuffer("1234567");
        try {
            str1 = str1.insert(8, new Object());
            return 700;
        }
        catch (IndexOutOfBoundsException e) {
        }
        
        str1 = new StringBuffer("1234567");
        str1 = str1.insert(4, "inserted");
        if (! str1.toString().equals("1234inserted567")) {
            return 710;
        }
        
        str1 = new StringBuffer("1234567");
        char cdata[] = null;
        try {
            str1 = str1.insert(4, cdata);
            return 720;
        }
        catch (NullPointerException e) {
        }
        
        cdata = new char[1];
        try {
            str1 = str1.insert(-1, cdata);
            return 730;
        }
        catch (IndexOutOfBoundsException e) {
        }
        
        try {
            str1 = str1.insert(8, cdata);
            return 740;
        }
        catch (IndexOutOfBoundsException e) {
        }
        
        str1 = new StringBuffer("1234567");
        char[] cdata1 = {'h', 'e', 'l', 'l', 'o'};
        str1 = str1.insert(4, cdata1);
        if (! str1.toString().equals("1234hello567")) {
            return 750;
        }
        
        str1 = new StringBuffer("1234567");
        str1 = str1.insert(0, true);
        if (! str1.toString().equals("true1234567")) {
            return 760;
        }
        
        str1 = new StringBuffer("1234567");
        str1 = str1.insert(7, false);
        if (! str1.toString().equals("1234567false")) {
            return 770;
        }
        
        str1 = new StringBuffer("1234567");
        str1 = str1.insert(0, 'c');
        if (! str1.toString().equals("c1234567")) {
            return 780;
        }
        
        str1 = new StringBuffer("1234567");
        str1 = str1.insert(7, 'b');
        if (! str1.toString().equals("1234567b")) {
            return 790;
        }
        
        str1 = new StringBuffer("1234567");
        str1 = str1.insert(7, 999);
        if (! str1.toString().equals("1234567999")) {
            return 800;
        }
        
        str1 = new StringBuffer("1234567");
        str1 = str1.insert(3, (long)1230);
        if (! str1.toString().equals("12312304567")) {
            return 810;
        }
        
        // setCharAt tests...
        
        str1 = new StringBuffer("1234567");
        try {
            str1.setCharAt(-1, 'A');
            return 820;
        }
        catch (IndexOutOfBoundsException e) {
        }
        
        try {
            str1.setCharAt(7, 'A');
            return 830;
        }
        catch (IndexOutOfBoundsException e) {
        }
        
        str1.setCharAt(3, 'A');
        if (! str1.toString().equals("123A567")) {
            return 840;
        }
        
        // deleteCharAt tests...
        
        str1 = new StringBuffer("123456789");
        try {
            str1.deleteCharAt(-1);
            return 920;
        }
        catch (StringIndexOutOfBoundsException e) {
        }
        
        try {
            str1.deleteCharAt(9);
            return 930;
        }
        catch (StringIndexOutOfBoundsException e) {
        }
        
        str1.deleteCharAt(4);
        if (! str1.toString().equals("12346789")) {
            return 940;
        }
        
        if (str1.length() != 8) {
            return 950;
        }
        
        // reverse test...
        
        str1 = new StringBuffer("1234567890");
        str1 = str1.reverse();
        if (! str1.toString().equals("0987654321")) {
            return 1040;
        }
        
        str1 = new StringBuffer("123456789");
        str1 = str1.reverse();
        if (! str1.toString().equals("987654321")) {
            return 1050;
        }
        
        str1 = new StringBuffer("");
        str1 = str1.reverse();
        if (! str1.toString().equals("")) {
            return 1060;
        }
        
        str1 = new StringBuffer("A");
        str1 = str1.reverse();
        if (! str1.toString().equals("A")) {
            return 1070;
        }
        
        return 0;
    }

    static void testFieldNotFoundException() {
        boolean caught = false;
        try {
            boolean missingField = org.mozilla.test.ClassWithMissingField.missingField;
        } catch (Exception e) {
            // Despite the test's name, the VM raises a generic RuntimeException
            // because CLDC doesn't provide a FieldNotFoundException class.
            if (!(e instanceof RuntimeException)) {
                System.out.println("testFieldNotFoundException: FAIL 1");
                return;
            }
            if (!e.getMessage().equals("org/mozilla/test/ClassWithMissingField.missingField.Z not found")) {
                System.out.println("testFieldNotFoundException: FAIL 2");
                return;
            }
            caught = true;
        }
        if (!caught) {
            System.out.println("testFieldNotFoundException: FAIL 3");
            return;
        }
    }

    final static int ASX = 10;
    final static int NPX = 20;
    final static int ISX = 30;
    final static int AOK = 40;
    final static int ERR = 50;
    
    static public void fill(int[] a) {
        for (int i = 0; i < a.length; ++i)
            a[i] = i;
    }

    static public boolean check(int[] expect, int[] result) {
        
        boolean ok = expect.length == result.length;
        
        for (int i = 0; ok && i < expect.length; ++i) {
            if (expect[i] != result[i]) {
                ok = false;
            }
        }
        
        return ok;
        
    }

    static public int copy (Object from, int a, Object to, int b, int c) {
        try {
            System.arraycopy (from, a, to, b, c);
            return AOK;
        }
        catch (ArrayStoreException xa) {
            return ASX;
        }
        catch (IndexOutOfBoundsException xb) {
            return ISX;
        }
        catch (NullPointerException xc) {
            return NPX;
        }
        catch (Throwable xd) {
            return ERR;
        }
    }
    
    public static int testSystem() {
        int[] x, y;
        
        x = new int[5];
        y = new int[5];
        fill(x);
        
        if (copy (x, 0, y, 0, x.length) != AOK) {
            return 160;
        }
        
        int[] one = { 0, 1, 2, 3, 4 };
        if (! check(y, one)) {
            return 170;
        }
        
        if (copy (x, 1, y, 0, x.length - 1) != AOK) {
            return 180;
        }
        
        if (copy (x, 0, y, x.length - 1, 1) != AOK) {
            return 190;
        }
        
        int[] two = { 1, 2, 3, 4, 0 };
        if (! check (y, two)) {
            return 200;
        }
        
        Object[] z = new Object[5];
        if (copy (x, 0, z, 0, x.length) != ASX) {
            return 210;
        }
        
        if (copy (x, 0, y, 0, -23) != ISX) {
            return 220;
        }
        
        if (copy (null, 0, y, 0, -23) != NPX) {
            return 230;
        }
        
        if (copy (x, 0, null, 0, -23) != NPX) {
            return 240;
        }
        
        String q = "metonymy";
        if (copy (q, 0, y, 0, 19) != ASX) {
            return 250;
        }
        
        if (copy (x, 0, q, 0, 19) != ASX) {
            return 260;
        }
        
        double[] v = new double[5];
        if (copy (x, 0, v, 0, 5) != ASX) {
            return 270;
        }
        
        if (copy (x, -1, y, 0, 1) != ISX) {
            return 280;
        }
        
        if (copy (x, 0, z, 0, x.length) != ASX) {
            return 290;
        }
        
        if (copy (x, 0, y, -1, 1) != ISX) {
            return 300;
        }
        
        if (copy (x, 3, y, 0, 5) != ISX) {
            return 310;
        }
        
        Object[] w = new Object[5];
        String[] ss = new String[5];
        for (int i = 0; i < 5; i++) {
            w[i] = i + "";
            ss[i] = (i + 23) + "";
        }
        w[3] = new Integer (23);
        
        if (copy (w, 0, ss, 0, 5) != ASX) {
            return 330;
        }
        
        return 0;
    }

    public static void main(String[] args) {
        testConditions();
        testConditions();
        testArray();
        testArray();

        JITTest test = new JITTest();
        boolean pass = false;
        try {
            test.lanciaEccezione();
        } catch (Exception e) {
            pass = true;
        }
        if (!pass) {
            System.out.println("lanciaEccezione: FAIL 1");
            return;
        }
        pass = false;
        try {
            test.lanciaEccezione();
        } catch (Exception e) {
            pass = true;
        }
        if (!pass) {
            System.out.println("lanciaEccezione: FAIL 2");
            return;
        }
        pass = false;
        try {
            test.rilanciaEccezione();
        } catch (Exception e) {
            pass = true;
        }
        if (!pass) {
            System.out.println("rilanciaEccezione: FAIL 1");
            return;
        }
        pass = false;
        try {
            test.rilanciaEccezione();
        } catch (Exception e) {
            pass = true;
        }
        if (!pass) {
            System.out.println("rilanciaEccezione: FAIL 3");
            return;
        }
        try {
            test.lanciaECatturaEccezione();
        } catch (Exception e) {
            System.out.println("lanciaECatturaEccezione: FAIL 1");
        }
        try {
            test.lanciaECatturaEccezione();
        } catch (Exception e) {
            System.out.println("lanciaECatturaEccezione: FAIL 2");
        }
        try {
            test.catturaEccezione();
        } catch (Exception e) {
            System.out.println("lanciaECatturaEccezione: FAIL 1");
        }
        try {
            test.catturaEccezione();
        } catch (Exception e) {
            System.out.println("lanciaECatturaEccezione: FAIL 2");
        }

        try {
            System.out.println("PROVA1");
            System.out.println("PROVA2");
        } catch (Exception e) {
            System.out.println("testPrintln: FAIL 1");
        }

        testExceptions();
        testExceptions();

        testSwitch();
        testSwitch();

        testDup();
        longValue = 5;
        testDup();

        testObjects();
        testObjects();

        testString();
        testString();

        basicTestNative();
        basicTestNative();

        testNative();
        testNative();

        testBytecodes();
        testBytecodes();

        testInteger();
        testInteger();

        int fail = testStringBuffer();
        if (fail != 0) {
            System.out.println("testStringBuffer: FAIL " + fail);
        }
        fail = testStringBuffer();
        if (fail != 0) {
            System.out.println("testStringBuffer: FAIL " + fail);
        }

        testFieldNotFoundException();
        testFieldNotFoundException();

        fail = testSystem();
        if (fail != 0) {
            System.out.println("testSystem: FAIL " + fail);
        }
        fail = testSystem();
        if (fail != 0) {
            System.out.println("testSystem: FAIL " + fail);
        }

        System.out.println("DONE");

        /*JITTest test = new JITTest();
        for (int i = 0; i < 1000; i++) {
            test.ciao();
        }
        System.out.println("DONE");*/

        //System.out.println("START");
        /*JITTest test = new JITTest();
        int val = 0;
        //String s = "CIAO" + test.prova();
        //val = s.length();
        //for (int i = 0; i < 99999; i++) {
        /*try {
            new Exception("ASDASD");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("CALL COMPILED NOW\n\n\n\n\n");*/
        /*for (int i = 0; i < 2; i++) {
            try {
                val = test.ciao();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        //}
        System.out.println("DONE: " + val);*/
    }
}
