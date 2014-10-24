

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import com.sun.midp.lcdui.GraphicsAccess;


class HelloGraphics extends Canvas implements Runnable {
    int position = 0;
    boolean increment = true;
    Image offscreenImage = null;
    Image offscreenImage2 = null;

    protected void paint(Graphics g) {
        g.setColor(0x00FFFFFF);
        g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
        g.setColor(0, 0, 0);
        g.fillRect(getWidth() / 4, getHeight() / 4 * 3, 250, 25);
        g.drawImage(offscreenImage, getWidth() / 4, getHeight() / 4 * 3, 0);
        g.drawImage(offscreenImage2, getWidth() / 4, getHeight() / 2, 0);
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
        g.setColor(0xFF, 0, 0);
        g.drawRoundRect(0, 0, getWidth() / 4, getHeight() / 4, getWidth() / 8, getHeight() / 8);
        g.setColor(0xFF, 0, 0xFF);
        g.fillRoundRect(10, 10, getWidth() / 8, getHeight() / 10, getWidth() / 16, getHeight() / 20);
    }

    public void run() {
        offscreenImage = Image.createImage(250, 25);
        Graphics g = offscreenImage.getGraphics();
        g.setColor(0, 0xFF, 0x88);
	g.drawString("This is a test", 250/2, 25/2, Graphics.HCENTER | Graphics.VCENTER);
        int[] arrayOfInt = new int[250 * 25];
        offscreenImage.getRGB(arrayOfInt, 0, 25, 0, 0, 25, 25);
        for (int i = 0; i < arrayOfInt.length; i++) {
            if (arrayOfInt[i] != 0)
                arrayOfInt[i] = (0xFF000000 & arrayOfInt[i] | 0x0000FF00);
        }
        offscreenImage2 = Image.createRGBImage(
            arrayOfInt, 250, 25, true);
        
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

