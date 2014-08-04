

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;


class HelloGraphics extends Canvas {
    protected void paint(Graphics g) {
        g.setColor(0x00FFFFFF);;
        g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
        g.setColor(0x00000000);
        for (int i = 0; i < getWidth() / 5; i++) {
            g.drawLine(0 + (i * 5), 0, getWidth() - (i * 5), getHeight());
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

