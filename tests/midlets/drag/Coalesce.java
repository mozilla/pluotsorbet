/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

package tests.drag;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

import com.nokia.mid.ui.gestures.*;

/**
 * Waiter
 */
class Waiter implements Runnable {
    public Waiter(String msg, int[] expectedCoords, int[] expectedGestureCoords) {
        this.msg = msg;
        this.expectedCoords = expectedCoords;
        this.expectedGestureCoords = expectedGestureCoords;
    }

    void pointerDragged(int x, int y) {
        synchronized (lock) {
            if (currentCoord >= expectedCoords.length) {
                System.out.println("FAIL - unexpected drag event in " + msg);
                isTimeout = false;
                isSuccess = false;
                lock.notify();
                return;
            }

            int expectedX = expectedCoords[currentCoord++];
            int expectedY = expectedCoords[currentCoord++];

            if (expectedX != x || expectedY != y) {
                System.out.println("FAIL - Coordinate mismatch in " + msg);
                System.out.println("  expectedX: " + expectedX + ". Actual: " + x);
                System.out.println("  expectedY: " + expectedY + ". Actual: " + y);
                isTimeout = false;
                isSuccess = false;
                lock.notify();
                return;
            }

            if (currentCoord == expectedCoords.length &&
                currentGestureCoord == expectedGestureCoords.length) {
              isSuccess = true;
              isTimeout = false;
            }
        }
    }

    void gestureDrag(int distX, int distY) {
        synchronized (lock) {
            if (currentGestureCoord >= expectedGestureCoords.length) {
                System.out.println("FAIL - unexpected drag gesture in " + msg);
                isTimeout = false;
                isSuccess = false;
                lock.notify();
                return;
            }

            int expectedX = expectedGestureCoords[currentGestureCoord++];
            int expectedY = expectedGestureCoords[currentGestureCoord++];

            if (expectedX != distX || expectedY != distY) {
                System.out.println("FAIL - GestureCoordinate mismatch in " + msg);
                System.out.println("  expectedX: " + expectedX + ". Actual: " + distX);
                System.out.println("  expectedY: " + expectedY + ". Actual: " + distY);
                isTimeout = false;
                isSuccess = false;
                lock.notify();
                return;
            }

            if (currentCoord == expectedCoords.length &&
                currentGestureCoord == expectedGestureCoords.length) {
              isSuccess = true;
              isTimeout = false;
            }
        }
    }

    public void run() {
        synchronized (lock) {
            try {
                lock.wait(300);
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
    private boolean isTimeout = true;
    private boolean isSuccess = false;
    private String msg;
    private int currentCoord = 0;
    private int[] expectedCoords;
    private int currentGestureCoord = 0;
    private int[] expectedGestureCoords;
}

// XXX: Maybe we should also test flick gestures?
class CoalesceCanvas extends Canvas implements GestureListener {
    public CoalesceCanvas() {
      // Set this as container (gesture source) and listener
      GestureRegistrationManager.setListener(this, this);
      // Register for drag events in the whole canvas area
      gestureZone = new GestureInteractiveZone(GestureInteractiveZone.GESTURE_DRAG);
      GestureRegistrationManager.register(this, gestureZone);
    }

    public void paint(Graphics g) {
    }

    protected void pointerDragged(int x, int y) {
        this.w.pointerDragged(x, y);
    }

    public void gestureAction(Object container, GestureInteractiveZone gestureInteractiveZone, GestureEvent gestureEvent) {
        int eventType = gestureEvent.getType();
        switch (eventType) {
            case GestureInteractiveZone.GESTURE_DRAG:
                this.w.gestureDrag(gestureEvent.getDragDistanceX(),
                                   gestureEvent.getDragDistanceY());
            break;

            case GestureInteractiveZone.GESTURE_RECOGNITION_START:
                // Do we need to do anything here?
            break;

            case
            GestureInteractiveZone.GESTURE_RECOGNITION_END:
                // Do we need to do anything here?
            break; 
        }
    }

    protected native void singleDragTest0();
    protected Thread singleDragTest() {
        int[] coords = {20, 25};
        int[] gestureCoords = {};
        this.w = new Waiter("singleDragTest", coords, gestureCoords);
        Thread th = new Thread(this.w);
        th.start();
        singleDragTest0();
        return th;
    }

    protected native void simpleMultiDragTest0();
    protected Thread simpleMultiDragTest() {
        int[] coords = {13, 27};
        int[] gestureCoords = {};
        this.w = new Waiter("simpleMultiDragTest", coords, gestureCoords);
        Thread th = new Thread(this.w);
        th.start();
        simpleMultiDragTest0();
        return th;
    }

    protected native void nonCoalesceTest0();
    protected Thread nonCoalesceTest() {
        int[] coords = {1, 1, 3, 7, 9, 15};
        int[] gestureCoords = {};
        this.w = new Waiter("nonCoalesceTest", coords, gestureCoords);
        Thread th = new Thread(this.w);
        th.start();
        nonCoalesceTest0();
        return th;
    }

    protected native void singleDragGestureTest0();
    protected Thread singleDragGestureTest() {
        int[] coords = {};
        int[] gestureCoords = {20, 25};
        this.w = new Waiter("singleDragGestureTest", coords, gestureCoords);
        Thread th = new Thread(this.w);
        th.start();
        singleDragGestureTest0();
        return th;
    }

    protected native void simpleMultiDragGestureTest0();
    protected Thread simpleMultiDragGestureTest() {
        int[] coords = {};
        int[] gestureCoords = {3, 1, 23, 25};
        this.w = new Waiter("simpleMultiDragGestureTest", coords, gestureCoords);
        Thread th = new Thread(this.w);
        th.start();
        simpleMultiDragGestureTest0();
        return th;
    }

    protected native void nonCoalesceGestureTest0();
    protected Thread nonCoalesceGestureTest() {
        int[] coords = {};
        int[] gestureCoords = {};
        this.w = new Waiter("nonCoalesceGestureTest", coords, gestureCoords);
        Thread th = new Thread(this.w);
        th.start();
        nonCoalesceGestureTest0();
        return th;
    }

    protected native void comprehensiveTest0();
    protected Thread comprehensiveTest() {
        int[] coords = {3, 1, 6, 7};
        int[] gestureCoords = {2, 0, -3, 7};
        this.w = new Waiter("comprehensiveTest", coords, gestureCoords);
        Thread th = new Thread(this.w);
        th.start();
        comprehensiveTest0();
        return th;
    }

    private Waiter w;
    private GestureInteractiveZone gestureZone;
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

        th = t.nonCoalesceTest();
        try {
            th.join();
        } catch (InterruptedException ie) {
            System.out.println("FAIL - Interrupted");
        }

        th = t.singleDragGestureTest();
        try {
            th.join();
        } catch (InterruptedException ie) {
            System.out.println("FAIL - Interrupted");
        }

        th = t.simpleMultiDragGestureTest();
        try {
            th.join();
        } catch (InterruptedException ie) {
            System.out.println("FAIL - Interrupted");
        }

        th = t.comprehensiveTest();
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
