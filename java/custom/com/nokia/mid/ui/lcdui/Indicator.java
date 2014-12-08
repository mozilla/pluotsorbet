package com.nokia.mid.ui.lcdui;

import javax.microedition.lcdui.Image;

public class Indicator {
    Image image;

    public Indicator(int aInt, Image aImage) {
        setIcon(aImage);
    }

    public native void setActive(boolean active);

    public void setIcon(Image image) {
        this.image = image;
    }
}
