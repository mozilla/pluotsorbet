package java.lang;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class TestStaticInit implements Testlet {
    static class TestClass {
        static int value = 0;
    }

    static class TestClassB extends TestClass {
    }

    public void test(TestHarness th) {
        TestClass.value = 1;
        TestClassB newInstance = new TestClassB();
        th.check(TestClass.value, 1);
        th.check(TestClassB.value, 1);
    }
}
