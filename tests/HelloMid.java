

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;


class HelloGraphics extends Canvas implements Runnable {
    int position = 0;
    boolean increment = true;

    protected void paint(Graphics g) {
        g.setColor(0x00FFFFFF);;
        g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
        g.setColor(0x00000000);
        g.drawLine(0 + position, 0, getWidth() - position, getHeight());
        g.drawArc(
            getWidth() / 4, getHeight() / 2,
            getWidth() / 4, getHeight() / 4,
            0,
            (int)((double)position / (double)getWidth() * (double)360));
        if (increment) {
            g.drawString("Hello", getWidth() / 4 * 3, getHeight() / 2, Graphics.HCENTER | Graphics.VCENTER);
        } else {
            g.drawString("World", getWidth() / 4 * 3, getHeight() / 2, Graphics.HCENTER | Graphics.VCENTER);
        }
    }

    public void run() {
        while (true) {
            if (increment) {
                position = position + 5;
                if (position >= (getWidth() - 5))
                    increment = false;
            } else {
                position = position - 5;
                if (position <= 5) {
                    increment = true;
                }
            }
            // Force a screen refresh.
            repaint();
            serviceRepaints();
            try {
                Thread.currentThread().sleep(20);
            } catch(java.lang.InterruptedException e) {
            }
        }
    }
}


public class HelloMid extends MIDlet implements CommandListener {
    private Command quitCommand;
    private Display display;

    public HelloMid() {
        display = Display.getDisplay(this);
        quitCommand = new Command("Quit", Command.EXIT, 2);
    }

    public void startApp() {

        HelloGraphics graf = new HelloGraphics();
        graf.addCommand(quitCommand);
        graf.setCommandListener(this);

        display.setCurrent(graf);

        Thread thread = new Thread(graf);
        thread.start();
    }

    public void pauseApp() {
        System.out.println("App paused");
    }

    public void destroyApp(boolean unconditional) {
        System.out.println("Goodbye, world");
    }

    public void commandAction(Command c, Displayable s) {
        if (c == quitCommand) {
            destroyApp(false);
            notifyDestroyed();
        }
    }
}

