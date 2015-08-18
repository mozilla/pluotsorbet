package benchmark;
import com.sun.cldchi.jvm.JVM;

class Time {
  public static void main(String[] args) {
    long start = JVM.monotonicTimeMillis();
    String hello = "Hello";
    for (int i = 0; i < 1000000; i++) {
      hello.startsWith(hello, 0);
    }
    System.out.println("Done: " + (JVM.monotonicTimeMillis() - start));
  }
}
