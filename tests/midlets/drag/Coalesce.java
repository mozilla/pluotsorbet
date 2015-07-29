/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

package tests.drag;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

/**
 * Waiter
 */
class Waiter implements Runnable {
    public Waiter(String msg, int expectedX, int expectedY) {
        this.msg = msg;
        this.expectedX = expectedX;
        this.expectedY = expectedY;
    }

    void pointerDragged(int x, int y) {
        synchronized (lock) {
            if (!expectingDrag) {
                System.out.println("FAIL - unexpected drag event in " + msg);
                isTimeout = false;
                isSuccess = false;
                lock.notify();
                return;
            }
            expectingDrag = false;

            if (expectedX != x || expectedY != y) {
                System.out.println("FAIL - Coordinate mismatch in " + msg);
                isTimeout = false;
                isSuccess = false;
                lock.notify();
                return;
            }

            isSuccess = true;
            isTimeout = false;
        }
    }

    public void run() {
        synchronized (lock) {
            expectingDrag = true;
            try {
                lock.wait(1000);
            } catch (InterruptedException ie) {
                System.out.println("FAIL - Interrupted");
                return;
            }

            if (isTimeout) {
                System.out.println("FAIL - Timeout in " + msg);
                return;
            }

            if (isSuccess) {
                System.out.println("SUCCESS - " + msg);
                return;
            }
        }
    }

    private Object lock = new Object();
    private boolean expectingDrag = false;
    private boolean isTimeout = true;
    private boolean isSuccess = false;
    private String msg;
    private int expectedX;
    private int expectedY;
}

class CoalesceCanvas extends Canvas {
    public void paint(Graphics g) {
    }

    protected void pointerDragged(int x, int y) {
        this.w.pointerDragged(x, y);
    }

    protected native void singleDragTest0();
    protected Thread singleDragTest() {
        this.w = new Waiter("singleDragTest", 20, 25);
        Thread th = new Thread(this.w);
        th.start();
        singleDragTest0();
        return th;
    }

    protected native void simpleMultiDragTest0();
    protected Thread simpleMultiDragTest() {
        this.w = new Waiter("simpleMultiDragTest", 13, 27);
        Thread th = new Thread(this.w);
        th.start();
        simpleMultiDragTest0();
        return th;
    }

    private Waiter w;
}

class CoalesceRunner implements Runnable {
    CoalesceRunner(CoalesceCanvas t) {
        this.t = t;
    }

    public void run() {
        Thread th = t.singleDragTest();
        try {
            th.join();
        } catch (InterruptedException ie) {
            System.out.println("FAIL - Interrupted");
        }

        th = t.simpleMultiDragTest();
        try {
            th.join();
        } catch (InterruptedException ie) {
            System.out.println("FAIL - Interrupted");
        }

        System.out.println("DONE");
    }

    private CoalesceCanvas t;
}

public class Coalesce extends MIDlet {
    public void startApp() {
        CoalesceCanvas t = new CoalesceCanvas();
        t.setFullScreenMode(true);

        Display d = Display.getDisplay(this);
        d.setCurrent(t);

        runner = new CoalesceRunner(t);
        Thread th = new Thread(runner);
        th.start();
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean b) {
    }

    private CoalesceRunner runner;
}
