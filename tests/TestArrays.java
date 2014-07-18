public class TestArrays extends Test {
    public void main() {
	byte b[][] = new byte[3][4];
	b[1][2] = 5;
	check(b[1][2] == 5);
	Object o[][] = new Object[5][5];
	o[1][1] = new Integer(5);
	check(o[1][1].toString().equals("5"));
   }
}