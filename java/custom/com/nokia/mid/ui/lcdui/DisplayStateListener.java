package com.nokia.mid.ui.lcdui;

import javax.microedition.lcdui.Display;

public interface DisplayStateListener {
    public void displayActive(Display display);
    public void displayInactive(Display display);
}
