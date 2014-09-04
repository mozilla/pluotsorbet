/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/
package com.nokia.example.rlinks.view.item;

import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Form;

/**
 * Base class for all CustomItems in RLinks.
 */
public abstract class AbstractCustomItem
    extends CustomItem {

    public static final boolean isFTDevice;
    protected static final int deviceMargin;

    static {
        boolean result = false;
        try {
            Class.forName("com.nokia.mid.ui.IconCommand");           
            result = !System.getProperty("com.nokia.keyboard.type").equalsIgnoreCase("OnekeyBack");
        }
        catch (Exception e) {
        }
        isFTDevice = result;
        deviceMargin = isFTDevice ? 12 : 0;
    }

    protected boolean dragging;
    protected final int width;

    public AbstractCustomItem(Form form, int preferredWidth, String label) {
        super(label);
        this.width = preferredWidth;
    }

    public void pointerDragged(int x, int y) {
        dragging = true;
    }
    
    public void pointerReleased(int x, int y) {
        dragging = false;
    }

}
