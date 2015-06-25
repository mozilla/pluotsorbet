package java.lang.ref;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class TestWeakReference implements Testlet {
  public int getExpectedPass() { return 2; }
  public int getExpectedFail() { return 0; }
  public int getExpectedKnownFail() { return 0; }

  public void test(TestHarness th) {
    Object obj = new Object();
    WeakReference weakRef = new WeakReference(obj);
    th.check(weakRef.get(), obj, "weakly held referent is object");
    weakRef.clear();
    th.check(weakRef.get(), null, "cleared weakly held referent is null");
  }
}
