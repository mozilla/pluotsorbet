package org.mozilla.regression;

import java.io.*;
import gnu.testlet.*;
import org.mozilla.internal.Sys;

public class TestLongArrayDoesntOverrideOtherArray implements Testlet {
  public int getExpectedPass() { return 6; }
  public int getExpectedFail() { return 0; }
  public int getExpectedKnownFail() { return 0; }

  public void test(TestHarness th) {
    byte[] prova = {1, 2, 3};
    th.check(prova[0], 1);
    th.check(prova[1], 2);
    th.check(prova[2], 3);

    long long5pow[] = { 0, 0 };

    th.check(prova[0], 1);
    th.check(prova[1], 2);
    th.check(prova[2], 3);
  }
}
