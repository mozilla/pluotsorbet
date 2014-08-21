/*
 * Copyright © 2013 Nokia Corporation. All rights reserved. Nokia and Nokia
 * Connecting People are registered trademarks of Nokia Corporation. Oracle and
 * Java are trademarks or registered trademarks of Oracle and/or its affiliates.
 * Other product and company names mentioned herein may be trademarks or trade
 * names of their respective owners. See LICENSE.TXT for license information.
 */

package com.nokia.example;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/*
 * Main class CanvasItemsExample
 *
 * This MIDlet show examples of usage of CanvasGraphicsItem and TextEditor.
 *
 * Application starts with a login screen, where user name and password need to
 * be entered. TextEditor and CanvasGraphicsItem are used to implement labeled
 * text box. Once both the user name and password are entered, the "Login"
 * button is enabled, and the application can be switched to the second screen
 * with blog post title and content text boxes.
 *
 * The labeled text box is implemented in class CanvasTextBox. It supports
 * single line and multi line configuration, and changes its appearance
 * depending on its state (disabled, focused, non-focused), for multiline
 * configuration implementation of simple scrollbar is demonstrated in S60.
 *
 * Buttons used in the application UI are implemented in class Button.
 * CanvasGraphicsItem is used as a base class. Painting on CanvasGraphicsItem is
 * demonstrated.
 */
public class BlogWriter extends MIDlet {

    private LoginScreen loginScreen = null;
    private Display display;

    public BlogWriter() {
        this.display = Display.getDisplay(this);
        // Crate login screen
        this.loginScreen = new LoginScreen(this.display);
        this.loginScreen.setParent(this);
        // Activate login screen
        this.display.setCurrent(this.loginScreen);
    }

    /**
     * To determine whether a Series 40 device is a Full Touch device.
     */
    public static boolean isFullTouch() {
        return System.getProperty("com.nokia.keyboard.type").equals("None") && !isS60Platform();
    }
    
    public static boolean isAshaPlatform() {
        return System.getProperty("com.nokia.keyboard.type").equals("OnekeyBack") && !isS60Platform();
    }
    
    public static boolean isS60Platform()
    {
    	return (System.getProperty("microedition.platform").toLowerCase().indexOf("sw_platform=s60") > -1);
    }

    protected void destroyApp(boolean unconditional)
            throws MIDletStateChangeException {
        this.display.setCurrent(null);
        this.loginScreen.removeItems();
        this.loginScreen = null;
        this.display = null;
    }

    protected void pauseApp() {
    }

    protected void startApp() throws MIDletStateChangeException {
    }
}
