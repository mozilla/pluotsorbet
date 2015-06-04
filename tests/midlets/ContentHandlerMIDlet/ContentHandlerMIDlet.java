package tests.midlets;

import javax.microedition.midlet.MIDlet;
import javax.microedition.content.ContentHandlerServer;
import javax.microedition.content.Invocation;
import javax.microedition.content.Registry;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

public class ContentHandlerMIDlet extends MIDlet {
    boolean alreadyStarted = false;

    static native void sendShareMessage();
    static native boolean shouldStop();

    private Display display;
    Object paintedLock = new Object();
    boolean painted = false;
    class TestCanvas extends Canvas {
        protected void paint(Graphics g) {
            g.setColor(0x00FFFFFF);
            g.fillRect(0, 0, getWidth(), getHeight());

            synchronized (paintedLock) {
                painted = true;
                paintedLock.notify();
            }
        }
    }

    public void startApp() {
        if (alreadyStarted) {
            return;
        }
        alreadyStarted = true;

        if (shouldStop()) {
            System.out.println("Test finished");
            return;
        }

        try {
            Display.getDisplay(this).setCurrent(new TestCanvas());

            // Wait MIDlet to become the foreground MIDlet
            synchronized (paintedLock) {
                while (!painted) {
                    paintedLock.wait();
                }
            }

            ContentHandlerServer chServer = Registry.getServer(getClass().getName());

            // Check if the MIDlet has been invoked
            Invocation invoc = chServer.getRequest(false);
            if (invoc == null) {
                System.out.println("Invocation is null");
                return;
            }

            String shareAction = invoc.getAction();
            System.out.println("Invocation action: " + shareAction);

            String[] shareArgs = invoc.getArgs();
            for (int i = 0; i < shareArgs.length; i++) {
                System.out.println("Invocation args[" + i + "]: " + shareArgs[i]);
            }

            FileConnection image = (FileConnection)Connector.open(shareArgs[0].substring(4), Connector.READ_WRITE);
            if (!image.exists()) {
                System.out.println("Image doesn't exist");
            } else {
                System.out.println("Image exists");
                image.delete();
            }

            invoc.setArgs(null);
            chServer.finish(invoc, Invocation.INITIATED);
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
        }

        // Test that the Content Handler code works also if the MIDlet is already running.
        sendShareMessage();
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
};
