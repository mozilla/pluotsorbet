package benchmark;

import com.sun.cldchi.jvm.JVM;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import javax.microedition.midlet.MIDlet;
import org.mozilla.MemorySampler;

// This needs to be a midlet in order to have access to the J2ME socket API.

public class SocketBench extends MIDlet {
  void benchmarkLargeRead() throws IOException {
    SocketConnection client = (SocketConnection)Connector.open("socket://localhost:8000");

    OutputStream os = client.openOutputStream();
    os.write("GET /bench/benchmark.jar HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes());
    os.close();

    InputStream is = client.openInputStream();
    byte[] data = new byte[1024];
    int len;
    MemorySampler.sampleMemory("Memory before");
    long start = JVM.monotonicTimeMillis();
    do {
      len = is.read(data);
    } while (len != -1);
    System.out.println("large read time: " + (JVM.monotonicTimeMillis() - start));
    MemorySampler.sampleMemory("Memory  after");
    is.close();

    client.close();
  }

  void runBenchmark() {
    try {
      benchmarkLargeRead();
    } catch (IOException e) {
      System.out.println("Exception unexpected: " + e);
      System.out.println("Make sure the test HTTP server is running: python tests/httpServer.py");
    }
  }

  public static void main(String args[]) {
    System.out.println("Run the SocketBench benchmark as a midlet: midletClassName=benchmark.SocketBench");
  }

  public void startApp() {
    runBenchmark();
  }

  public void pauseApp() {
  }

  public void destroyApp(boolean unconditional) {
  }
}
