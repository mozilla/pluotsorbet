package benchmark;

import com.sun.cldchi.jvm.JVM;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

class TestRunnable implements Runnable {
  public TestRunnable(String name, long startTime, long msToRun, ThreadRunner parent) {
    this.name = name;
    this.startTime = startTime;
    this.msToRun = msToRun;
    this.parent = parent;
  }

  public void run() {
    long elapsedTime = 0;
    int numIterations = 0;

    do {
      numIterations++;
      long currentTime = JVM.monotonicTimeMillis();
      elapsedTime = currentTime - startTime;
      // TODO: THIS SLEEP SHOULD NOT BE NECESSARY!!!
      // We should preempt any thread before it
      // runs away with all of our execution time. There
      // seems to be a bug because even when I tested running
      // a few threads for 30s, only 1 thread was given all
      // of that execution time. Importantly, NOT EVEN THE
      // GUI THREAD WAS ABLE TO PREEMPT. That means that the
      // UI was completely frozen until the test finished.
      try {
        Thread.sleep(1);
      } catch(InterruptedException ie) {
      }
    } while (elapsedTime < msToRun);

    parent.notifyFinished(name, numIterations);
  }

  private String name;
  private long msToRun;
  private long startTime;
  private ThreadRunner parent;
}

class ThreadRunner implements Runnable {
  public ThreadRunner(int numOfEachPriority, long startTime, long msToRun) {
    this.numOfEachPriority = numOfEachPriority;
    this.startTime = startTime;
    this.msToRun = msToRun;
  }

  public void run() {
    for (int i = 0; i < numOfEachPriority; i++) {
      Thread t = new Thread(new TestRunnable("lo" + i, startTime, msToRun, this));
      // TODO: Change prio
      t.start();
      t = new Thread(new TestRunnable("med" + i, startTime, msToRun, this));
      // TODO: Change prio
      t.start();
      t = new Thread(new TestRunnable("hi" + i, startTime, msToRun, this));
      // TODO: Change prio
      t.start();
    }

    try {
      synchronized(this) {
        while (numCompleted < (numOfEachPriority * 3)) {
          wait();
        }
      }
      System.out.println("All completed!");
    } catch (InterruptedException ie) {
      System.out.println("Interrupted!");
    }
  }

  public synchronized void notifyFinished(String name, int numIterations) {
    numCompleted++;
    System.out.println("Thread " + name + " completed " + numIterations + " iterations");
  }

  private long msToRun;
  private long startTime;
  private int numOfEachPriority;
  private int numCompleted = 0;
}

public class SchedulerBench extends MIDlet {
  class TestCanvas extends Canvas implements Runnable {
    TestCanvas(Display d, long startTime, long msToRun) {
      display = d;
      this.msToRun = msToRun;
    }

    protected void paint(Graphics screenG) {
      screenG.setColor(0, 0, 0);
      screenG.fillRect(0, 0, getWidth(), getHeight());
      screenG.setColor(0, 255, 0);
      screenG.fillRect(x, y, w, h);
      display.callSerially(this);
      numFrames++;
    }

    public void run() {
      long currentTime = JVM.monotonicTimeMillis();
      long elapsed = currentTime - startTime;
      if (elapsed >= msToRun) {
        System.out.println("In " + elapsed + "ms, " + numFrames + " frames were rendered for an FPS of " + ((numFrames * 1000) / elapsed));
        return;
      }

      x += dx;
      y += dy;

      if (x + w > getWidth() ||
          x < 0) {
        dx = -dx;
        x += (dx << 1);
      }

      if (y + h > getHeight() ||
          y < 0) {
        dy = -dy;
        y += (dy << 1);
      }

      repaint();
    }

    private long numFrames = 0;
    private long msToRun;
    private long startTime = 0;
    private Display display;
    private int x = 0;
    private int dx = 1;
    private int y = 0;
    private int dy = 1;
    private int w = 5;
    private int h = 7;
  }

  public void startApp() {
    /*
     * NB: USE THESE VARIABLES TO TUNE THE TEST
     */
    long msToRun = 30000;
    int numOfEachPriority = 5;
    /*
     * END TUNING SECTION
     */


    long startTime = JVM.monotonicTimeMillis();
    Display d = Display.getDisplay(this);
    TestCanvas c = new TestCanvas(d, startTime, msToRun);
    c.setFullScreenMode(true);
    d.setCurrent(c);

    new Thread(new ThreadRunner(numOfEachPriority, startTime, msToRun)).start();
  }

  public void destroyApp(boolean b) {
  }

  public void pauseApp() {
  }
}

