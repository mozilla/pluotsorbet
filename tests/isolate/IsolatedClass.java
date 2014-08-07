package tests.isolate;

public class IsolatedClass {
    static public String val = "m";

    public static void main(String args[]) {
        val += args[0];
        System.out.println(args[0] + " " + val);
    }
}
