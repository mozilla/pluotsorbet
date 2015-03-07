package gnu.testlet.vm;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class ClassForNameInStaticInitializerTest implements Testlet {
  public int getExpectedPass() { return 2; }
  public int getExpectedFail() { return 0; }
  public int getExpectedKnownFail() { return 0; }

  private static boolean test1 = ClassForNameInStaticInitializerTest.canLoad("java.lang.Object");
  private static boolean test2 = ClassForNameInStaticInitializerTest.canLoad("java.lang.DoesntExist");

  public static boolean canLoad(String className) {
    try {
      Class.forName(className);
      return true;
    } catch (ClassNotFoundException e) {
    }

    return false;
  }

  public void test(TestHarness th) {
    th.check(test1, "java.lang.Object exists");
    th.check(!test2, "java.lang.DoesntExist doesn't exist");
  }
}
