import com.sun.cldc.i18n.Helper;

public class TestString extends Test {
	public void main() {
	    String s = "ściółka";
	    byte[] b = s.getBytes();
	    // char[] chars = s.toCharArray();
	    // byte[] b = Helper.charToByteArray(chars, 0, chars.length);
	    s = "";
	    for (int n = 0; n < b.length; ++n)
		s += (int)b[n] + ",";
	    compare(s, "-59,-101,99,105,-61,-77,-59,-126,107,97,");
	}

    public static void main(String[] args) {
	(new TestString()).main();
    }
}
