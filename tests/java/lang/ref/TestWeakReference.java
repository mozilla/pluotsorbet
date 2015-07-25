package java.lang.ref;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class TestWeakReference implements Testlet {
  public int getExpectedPass() { return 8; }
  public int getExpectedFail() { return 0; }
  public int getExpectedKnownFail() { return 0; }

  int objAddr;
  WeakReference gcWeakRef;

  class SetWeakRefThread extends Thread {
    public void run() {
      Object obj = new Object();
      gcWeakRef = new WeakReference(obj);
    }
  }

  public void test(TestHarness th) {
    Object obj = new Object();

    WeakReference weakRef = new WeakReference(obj);
    WeakReference weakRef2 = new WeakReference(obj);
    th.check(weakRef.get(), obj, "weakly held referent is object");
    th.check(weakRef2.get(), obj, "second weakly held referent is object");
    weakRef.clear();
    th.check(weakRef.get() == null, "cleared weakly held referent is null");
    weakRef.clear();
    th.check(weakRef.get() == null, "clearing a cleared WeakReference works");
    th.check(weakRef2.get(), obj, "second weakly held referent is object");

    SetWeakRefThread thread = new SetWeakRefThread();
    thread.start();
    try {
        thread.join();
    } catch (InterruptedException e) {
      th.fail("Unexpected exception: " + e);
    }

    th.check(gcWeakRef.get() != null, "weakly held referent isn't null");
    Runtime.getRuntime().gc();
    Runtime.getRuntime().gc();
    th.check(gcWeakRef.get() == null, "GC cleared weakly held referent is null");
    gcWeakRef.clear();
    th.check(weakRef.get() == null, "clearing a WeakReference cleared by the GC works");
  }
}
