package benchmark;

import com.sun.cldchi.jvm.JVM;
import com.sun.midp.crypto.SecureRandom;
import java.io.OutputStream;
import java.util.Random;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import org.mozilla.MemorySampler;

public class FileConnectionBench {
  void runBenchmark() {
    try {
      long start, time;

      start = JVM.monotonicTimeMillis();

      String privateDir = System.getProperty("fileconn.dir.private");
      Random random = new Random();
      String filename = new javax.bluetooth.UUID(java.lang.Math.abs(random.nextInt())).toString();
      FileConnection file = (FileConnection)Connector.open(privateDir + filename);
      System.out.println("Writing to file " + privateDir + filename);
      file.create();

      SecureRandom rnd = SecureRandom.getInstance(SecureRandom.ALG_SECURE_RANDOM);
      OutputStream out = file.openOutputStream();
      byte[] b = new byte[1024];
      for (int i = 0; i < 1000; i++) {
        rnd.nextBytes(b, 0, 1024);
        out.write(b);
      }
      out.close();

      time = JVM.monotonicTimeMillis() - start;
      System.out.println("FileConnection.write time: " + time);
    } catch (Exception e) {
      System.out.println("Unexpected exception: " + e);
      e.printStackTrace();
    }
  }

  public static void main(String args[]) {
    FileConnectionBench bench = new FileConnectionBench();
    MemorySampler.sampleMemory();
    bench.runBenchmark();
    MemorySampler.sampleMemory();
  }
}
