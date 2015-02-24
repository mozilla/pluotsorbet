package benchmark;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import com.sun.cldchi.jvm.JVM;

public class DataInputOutputStreamFileBench {
  void writeUTF(OutputStream out) throws IOException {
      DataOutputStream dos = new DataOutputStream(out);

      for (int i = 0; i < 25000; i++) {
          dos.writeUTF("abcdefghilmnopqrstuvzABCDEFGHILMNOPQRSTUVZabcdefghilmnopqrstuvzABCDEFGHILMNOPQRSTUVZ");
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

          String dirPath = System.getProperty("fileconn.dir.private");
          FileConnection file = (FileConnection)Connector.open(dirPath + "test");
          if (file.exists()) {
              file.delete();
          }
          file.create();

          OutputStream fileOut = file.openOutputStream();
          start = JVM.monotonicTimeMillis();
          writeUTF(fileOut);
          System.out.println("DataOutputStream::writeUTF in file: " + (JVM.monotonicTimeMillis() - start));

          InputStream fileIn = file.openInputStream();
          start = JVM.monotonicTimeMillis();
          readUTF(fileIn);
          System.out.println("DataInputStream::readUTF from file: " + (JVM.monotonicTimeMillis() - start));

          file.close();
      } catch (IOException e) {
          System.out.println("Unexpected exception: " + e);
          e.printStackTrace();
      }
  }

  public static void main(String args[]) {
      DataInputOutputStreamFileBench bench = new DataInputOutputStreamFileBench();
      bench.runBenchmark();
  }
}
