package org.mozilla.test;

public class ClassWithMissingField {
    // Define the field at buildtime so the test class will compile.
    public static boolean missingField = true;
}
