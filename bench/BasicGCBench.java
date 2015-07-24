package benchmark;

import java.util.Vector;
import com.sun.cldchi.jvm.JVM;

public class BasicGCBench {
	  public static void main(String args[]) {
        long start = JVM.monotonicTimeMillis();
		    for (int i = 0; i < 100000; i++) {
			      long[] array = new long[1000];
		    }
        long time = JVM.monotonicTimeMillis() - start;
        System.out.println("allocate 1000 long arrays: " + time);


        start = JVM.monotonicTimeMillis();
		    for (int i = 0; i < 100000; i++) {
			      Vector v = new Vector(100);
		    }
        time = JVM.monotonicTimeMillis() - start;
        System.out.println("allocate 1000 Vector objects: " + time);
	  }
}
