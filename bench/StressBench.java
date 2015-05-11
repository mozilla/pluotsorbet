package benchmark;

import com.sun.cldchi.jvm.JVM;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;

// This needs to be a midlet in order to have access to the J2ME socket API.

public class StressBench extends MIDlet {

  static class BouncyColors extends Canvas {
    int x = 0;
    int y = 0;
    int dx = 3;
    int dy = 5;

    public void paint(Graphics g) {
      int z = 0;
      while (true) {
        if ((x + dx) < 0 || (x + dx) > 200) {
          dx *= -1;
        }
        if ((y + dy) < 0 || (y + dy) > 200) {
          dy *= -1;
        }
        x += dx;
        y += dy;
        g.setColor(255, 255, 255);
        g.fillRect(0, 0, 300, 300);

        g.setColor((z + x) & 0xff, (z + y) & 0xff, (x + y) & 0xff);
        g.fillRect(x, y, 10, 10);
        try {
          Thread.sleep(16);
        } catch (Exception x) {
          // ...
        }
      }
    }
  }

  static class Worker implements Runnable {

    void benchmarkLargeRead() throws IOException {
      SocketConnection client = (SocketConnection)Connector.open("socket://localhost:8000");

      OutputStream os = client.openOutputStream();
      os.write(("GET /bench/benchmark.jar HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Connection: close\r\n" +
                "\r\n").getBytes());
      os.close();

      InputStream is = client.openInputStream();
      byte[] data = new byte[1024];
      int len;
      long start = JVM.monotonicTimeMillis();
      do {
        len = is.read(data);
      } while (len != -1);
      System.out.println("large read time: " + (JVM.monotonicTimeMillis() - start));
      is.close();

      client.close();
    }

    public void run() {
      while (true) {
        try {
          benchmarkLargeRead();
        } catch (IOException e) {
          System.out.println("error benchmarking large read: " + e);
        }
      }
    }
  }

  void runBenchmark() {
    Display display = Display.getDisplay(this);
    BouncyColors bouncyColors = new BouncyColors();
    display.setCurrent(bouncyColors);

    for (int i = 0; i < 16; i++) {
      Thread thread = new Thread(new Worker(), "T" + i);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }

  public static void main(String args[]) {
    System.out.println("Run the StressBench benchmark as a midlet: midletClassName=benchmark.StressBench");
  }

  public void startApp() {
    runBenchmark();
  }

  public void pauseApp() {
  }

  public void destroyApp(boolean unconditional) {
  }
}
