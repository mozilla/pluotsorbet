package benchmark;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import com.sun.cldchi.jvm.JVM;

public class DataInputOutputStreamBench {
  void writeUTF(OutputStream out) throws IOException {
      DataOutputStream dos = new DataOutputStream(out);

      for (int i = 0; i < 50000; i++) {
          dos.writeUTF("abcdefghilmnopqrstuvzABCDEFGHILMNOPQRSTUVZ");
      }

      dos.flush();
      dos.close();
  }

  void readUTF(InputStream is) throws IOException {
      DataInputStream dis = new DataInputStream(is);

      for (int i = 0; i < 25000; i++) {
          String str = dis.readUTF();
      }

      dis.close();
  }

  void runBenchmark() {
      try {
          long start;

          ByteArrayOutputStream out = new ByteArrayOutputStream();
          start = JVM.monotonicTimeMillis();
          writeUTF(out);
          System.out.println("DataOutputStream::writeUTF: " + (JVM.monotonicTimeMillis() - start));
          byte[] array = out.toByteArray();

          ByteArrayInputStream in = new ByteArrayInputStream(array);
          start = JVM.monotonicTimeMillis();
          readUTF(in);
          System.out.println("DataInputStream::readUTF: " + (JVM.monotonicTimeMillis() - start));
      } catch (IOException e) {
          System.out.println("Unexpected exception: " + e);
          e.printStackTrace();
      }
  }

  public static void main(String args[]) {
      DataInputOutputStreamBench bench = new DataInputOutputStreamBench();
      bench.runBenchmark();
  }
}
