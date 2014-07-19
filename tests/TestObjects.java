public class TestObjects extends Test {
	
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
	
	public void main() {
		B a = new B();
		a.put(5);
		check(a.getb() == 5);
		a.puts(6);
		a.b = 7;
		check(a.get() == 5);
		check(a.gets() == 6);
		check(a.getb() == 6);
		check(a.b == 7);
	}

    public static void main(String[] args) {
	(new TestObjects()).main();
    }
}
