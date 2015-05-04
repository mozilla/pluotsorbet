package benchmark;

import com.sun.cldchi.jvm.JVM;
import java.io.InputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpsConnection;
import javax.microedition.midlet.MIDlet;

// This needs to be a midlet in order to have access to the J2ME socket API.

public class SSLSocketBench extends MIDlet {
  void benchmarkLargeRead() throws IOException {
      HttpsConnection hc = (HttpsConnection)Connector.open("https://localhost:4443/tests.jar");

      InputStream is = hc.openDataInputStream();

      byte[] data = new byte[1024];
      int len;
      long start = JVM.monotonicTimeMillis();
      do {
        len = is.read(data);
      } while (len != -1);
      System.out.println("large read time: " + (JVM.monotonicTimeMillis() - start));

      is.close();
      hc.close();
  }

  void runBenchmark() {
    try {
      benchmarkLargeRead();
    } catch (IOException e) {
      System.out.println("Exception unexpected: " + e);
      System.out.println("Make sure the test ssl server is running: cd tests && python sslEchoServer.py");
    }
  }

  public static void main(String args[]) {
    System.out.println("Run the SSLSocketBench benchmark as a midlet: midletClassName=benchmark.SSLSocketBench");
  }

  public void startApp() {
    runBenchmark();
  }

  public void pauseApp() {
  }

  public void destroyApp(boolean unconditional) {
  }
}
