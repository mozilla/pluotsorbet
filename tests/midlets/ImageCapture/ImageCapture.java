package tests.imagecapture;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import java.io.IOException;

public class ImageCapture extends MIDlet implements CommandListener {
    private Display display;
    private Command cmdCapture;
    private Command cmdExit;
    private Command cmdBack;
    private Player player;
    private VideoControl videoControl;
    private Form imageForm;
    private Image capturedImage;

    public ImageCapture() {
        display = Display.getDisplay(this);
 
        createCamera();
        createImageForm();       
    }
 
    private void createCamera() {
        Form cameraForm = new Form("Camera");

        cmdCapture = new Command("Capture", Command.OK, 0);
        cmdExit = new Command("Exit", Command.EXIT, 0);
        cameraForm.addCommand(cmdCapture);
        cameraForm.addCommand(cmdExit);
        cameraForm.setCommandListener(this);

        try {
            player = Manager.createPlayer("capture://image");
            player.realize();
            player.prefetch();

            videoControl = (VideoControl)player.getControl("VideoControl");

            Item item = (Item) videoControl.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, null);
            cameraForm.append(item);

            player.start();
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
        }

        display.setCurrent(cameraForm);
    }

    private void createImageForm() {
        imageForm = new Form("Captured image");

        cmdBack = new Command("Back", Command.BACK, 0);
        imageForm.addCommand(cmdBack);
        imageForm.setCommandListener(this);
    }

    private void captureImage() {
        try {
            byte[] imageData;

            imageData = videoControl.getSnapshot("encoding=jpeg");

            capturedImage = Image.createImage(imageData, 0, imageData.length);

            player.close();
            player = null;
            videoControl = null;

            imageForm.append(capturedImage);
            display.setCurrent(imageForm);
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
 
    public void startApp() {
    }
 
    public void pauseApp() {
    }
 
    public void exitMIDlet() {
        notifyDestroyed();
    }

    public void destroyApp(boolean unconditional) {
        if (player != null) {
            player.deallocate();
            player.close();
        }
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == cmdCapture) {
            captureImage();
        } else if (command == cmdBack) {
            createCamera();
        } else if (command == cmdExit) {
            notifyDestroyed();
        }
    }
}
