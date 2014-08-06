package org.mozilla.test;

public class ClassWithMissingMethod {
    // Define the method at buildtime so the test class will compile.
    public static void missingMethod() {}
}
