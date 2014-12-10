package com.nokia.mid.ui.lcdui;

import javax.microedition.lcdui.Image;

/*
 * This class is undocumented.
 *
 * We're implementing indicators using the Notifications API.
 * When setActive is called with true, we enable showing notifications.
 * When setActive is called with false, we dismiss notifications.
 * This class is associated with the nokia.active-standby localmsg
 * server, that gives us the text associated with the Indicator.
 * We show a notification when the nokia.active-standby receives an
 * "Update" event and the Indicator is active.
 *
 * We're storing an icon in the image property, but we're not really
 * using it (we're using the icon sent to the nokia.active-standby
 * server).
 *
 */

public class Indicator {
    Image image;

    public Indicator(int aInt, Image aImage) {
        if (aInt != 0) {
          System.out.println("Indicator(IL...Image;) unexpected value (" + aInt + ", " + aImage + ")");
        }
        setIcon(aImage);
    }

    public native void setActive(boolean active);

    public void setIcon(Image image) {
        this.image = image;
    }
}
