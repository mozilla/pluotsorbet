package benchmark;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;

public class IOStressBench extends MIDlet {
    static class BouncyColors extends Canvas {
        int x = 0;
        int y = 0;
        int dx = 3;
        int dy = 5;

        public void paint(Graphics g) {
            int z = 0;

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
        }
    }

    static class Worker implements Runnable {
        public void run() {
            while (true) {
                FileConnectionBench bench = new FileConnectionBench();
                bench.runBenchmark();
            }
        }
    }

    public static void main(String args[]) {
      System.out.println("Run the StressBench benchmark as a midlet: midletClassName=benchmark.IOStressBench");
    }

    public void startApp() {
        Display display = Display.getDisplay(this);
        BouncyColors bouncyColors = new BouncyColors();
        display.setCurrent(bouncyColors);

        for (int i = 0; i < 16; i++) {
            Thread thread = new Thread(new Worker(), "T" + i);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
          }

        Thread current = Thread.currentThread();
        current.setPriority(Thread.MAX_PRIORITY);

        while (true) {
            bouncyColors.repaint();

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {}
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
