package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public class TextBoxTest extends MIDlet implements CommandListener {
    private Command quitCommand;
    private Display display;
    private TextBox textBox;

    public TextBoxTest() {
        display = Display.getDisplay(this);
    }

    public void startApp() {
        quitCommand = new Command("Quit", Command.SCREEN, 1);
        textBox = new TextBox("Hello World", "Some text", 40, 0);
        textBox.addCommand(quitCommand);
        textBox.setCommandListener(this);
        display.setCurrent(textBox);

        while (!textBox.isShown()) {
            Thread.yield();
        }

        System.out.println("PAINTED");
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    public void commandAction(Command choice, Displayable displayable) {
        if (choice == quitCommand) {
            destroyApp(false);
            notifyDestroyed();
        }
    }
}

