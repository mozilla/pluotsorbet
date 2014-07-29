package gnu.testlet.vm;

import gnu.testlet.*;

public class ObjectsTest implements Testlet {
	
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
	
	public void test(TestHarness th) {
		B a = new B();
		a.put(5);
		th.check(a.getb() == 5);
		a.puts(6);
		a.b = 7;
		th.check(a.get() == 5);
		th.check(a.gets() == 6);
		th.check(a.getb() == 6);
		th.check(a.b == 7);
	}
}
