package com.sun.cldc.io;

import java.io.IOException;
import com.sun.cldchi.jvm.JVM;

public class ResourceInputStreamBench {
    void runBenchmark() {
        try {
            ResourceInputStream stream = (ResourceInputStream)getClass().getResourceAsStream("/javax/microedition/media/hello.wav");

            long start = JVM.monotonicTimeMillis();
            for (int i = 0; i < 250000; i++) {
              stream.available();
            }
            System.out.println("available: " + (JVM.monotonicTimeMillis() - start));

            start = JVM.monotonicTimeMillis();
            for (int i = 0; i < 250000; i++) {
              stream.read();
            }
            System.out.println("read single: " + (JVM.monotonicTimeMillis() - start));
        } catch (IOException e) {
            System.out.println("Unexpected exception: " + e);
        }
    }

    public static void main(String args[]) {
      ResourceInputStreamBench bench = new ResourceInputStreamBench();
      bench.runBenchmark();
    }
}
