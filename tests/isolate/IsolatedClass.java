package tests.isolate;

import com.sun.cldc.isolate.*;

public class IsolatedClass {
    static public String val = "m";
    static public int testRun = 0;

    public static void main(String args[]) {
        val += args[0];
        System.out.println(Isolate.currentIsolate().id() + " " + (testRun++) + " " + args[0] + " " + val);
    }
}
