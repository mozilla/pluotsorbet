package benchmark;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import com.sun.cldchi.jvm.JVM;

public class UTF8Bench {
    FileConnection file;
    char[] cbuf, cbufReader;

    void generateData() throws IOException {
        String str = "";
        String part = "abcdefghilmnopqrstuvzABCDEFGHILMNOPQRSTUVZabcdefghilmnopqrstuvzABCDEFGHILMNOPQRSTUVZ";
        for (int i = 0; i < 2000; i++) {
          str += part;
        }

        cbuf = new char[str.length()];
        cbufReader = new char[cbuf.length];
        str.getChars(0, str.length(), cbuf, 0);

        String dirPath = System.getProperty("fileconn.dir.private");
        file = (FileConnection)Connector.open(dirPath + "test");
        if (file.exists()) {
            file.delete();
        }
        file.create();
    }

    void writeUtf8Data(OutputStream os) throws IOException {
      OutputStreamWriter osWriter = new OutputStreamWriter(os, "UTF_8");
      osWriter.write(cbuf, 0, cbuf.length);
    }

    void readUtf8Data(InputStream is) throws IOException {
      InputStreamReader isReader = new InputStreamReader(is, "UTF_8");
      isReader.read(cbufReader, 0, cbufReader.length);
    }

    void runBenchmark() {
      try {
          long start, time;

          generateData();

          OutputStream os = file.openOutputStream();
          start = JVM.monotonicTimeMillis();
          writeUtf8Data(os);
          time = JVM.monotonicTimeMillis() - start;
          System.out.println("writeUtf8Data: " + time);
          os.flush();
          os.close();

          InputStream is = file.openInputStream();
          start = JVM.monotonicTimeMillis();
          readUtf8Data(is);
          time = JVM.monotonicTimeMillis() - start;
          System.out.println("readUtf8Data: " + time);
          is.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public static void main(String args[]) {
      UTF8Bench bench = new UTF8Bench();
      bench.runBenchmark();
    }
}
