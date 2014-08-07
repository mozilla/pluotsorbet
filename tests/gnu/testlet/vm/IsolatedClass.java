package gnu.testlet.vm;

import gnu.testlet.*;

public class IsolatedClass {
    public static int val = 1;

    public static void main(String args[]) {
        val += Integer.parseInt(args[0]);
    }
}
