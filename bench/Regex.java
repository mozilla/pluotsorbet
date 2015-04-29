package benchmark;

import com.sun.cldchi.jvm.JVM;
import me.regexp.RE;

public class Regex {

    public static void main(String args[]) {
      long start = JVM.monotonicTimeMillis();
      RE r1 = new RE("\\[([^\\[\\]])*\\]");
      RE r2 = new RE("(\\d{3})(\\d{4})");
      long initTime = JVM.monotonicTimeMillis() - start;
      String number = "5555231119";
      boolean m1 = false;
      boolean m2 = false;
      start = JVM.monotonicTimeMillis();
      for (int i = 0; i < 1000; i++) {
        m1 = r1.match(number);
        m2 = r2.match(number);
      }
      long matchTime = JVM.monotonicTimeMillis() - start;
      System.out.println("Matches: " + m1 + " " + m2);
      System.out.println("REGEX init time: " + initTime);
      System.out.println("REGEX match time: " + matchTime);
    }
}
