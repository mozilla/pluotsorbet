package java.lang;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

class UnwindOne {
    public static String value = "UnwindOne";
    static {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
        }
        TestStaticInitUnwind.called = 11;
    }
}

class UnwindTwo {
    public static String valueSet;

    static {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
        }
        TestStaticInitUnwind.called = 12;
    }
}

class UnwindThree {

    static {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
        }
        TestStaticInitUnwind.called = 13;
    }
}

class UnwindFour {

    static {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
        }
        TestStaticInitUnwind.called = 14;
    }

    // Use non void and arguments to test stack movement handling.
    static int go(int i) {
        return i + 1;
    }
}

public class TestStaticInitUnwind implements Testlet {
    public static int called = 0;
    public int getExpectedPass() { return 9; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    public void test(TestHarness th) {
        th.check(called, 0);
        // There are four possible places a static initialization can occur:
        // 1: GETSTATIC
        String someValue = UnwindOne.value;
        th.check(someValue, "UnwindOne");
        th.check(called, 11);
        // 2: PUTSTATIC
        UnwindTwo.valueSet = "UnwindTwo";
        th.check(UnwindTwo.valueSet, "UnwindTwo");
        th.check(called, 12);
        // 3: NEW
        UnwindThree ut = new UnwindThree();
        th.check(ut instanceof UnwindThree);
        th.check(called, 13);
        // 3: INVOKESTATIC
        int ret = UnwindFour.go(10);
        th.check(ret, 11);
        th.check(called, 14);
    }
}
