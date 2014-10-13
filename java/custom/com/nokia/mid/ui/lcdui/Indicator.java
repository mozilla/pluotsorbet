package com.nokia.mid.ui.lcdui;

import javax.microedition.lcdui.Image;

public class Indicator {
    public Indicator(int aInt, Image aImage) {
        System.out.println("Indicator(IL...Image;) not implemented (" + aInt + ", " + aImage + ")");
    }

    public void setActive(boolean active) {
        System.out.println("Indicator.setActive(Z)V not implemented (" + active + ")");
    }

    public void setIcon(Image image) {
        System.out.println("Indicator.setIcon(IL...Image;)V not implemented.");
    }
}
