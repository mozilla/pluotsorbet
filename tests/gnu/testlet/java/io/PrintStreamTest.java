package gnu.testlet.java.io;

import java.io.*;
import gnu.testlet.*;

public class PrintStreamTest implements Testlet {
	public void test(TestHarness th) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintStream s = new PrintStream(out);
		s.print(true);
		s.print(false);
		s.print('ś');
		char[] cc = {'ć', 'x', '冷'};
		s.print(cc);
		s.print(-3.78);
		s.print(-843094830);
		s.print(843094830l);
		s.print(new Integer(7));
		s.print("ściółka");
		s.println();
		s.println(true);
		s.println('y');
		s.println(cc);
		s.println(7.5);
		s.println(-999);
		s.println(-894384038080312l);
		s.println(new Integer(7));
		s.println("test");
		byte[] bb = {120, 65, 48};
		try {
			s.write(bb);
			s.write(bb, 1, 1);
			s.write(99);
		} catch(IOException e) {
			th.check(false);
		}
		th.check(!s.checkError());
		s.flush();
		s.close();
		String test = "truefalseśćx冷-3.78-8430948308430948307ściółka\ntrue\ny\nćx冷\n7.5\n-999\n-894384038080312\n7\ntest\nxA0Ac";
		th.check(test, out.toString());
	}
}
