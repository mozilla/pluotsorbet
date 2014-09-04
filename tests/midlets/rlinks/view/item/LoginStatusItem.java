/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/
package com.nokia.example.rlinks.view.item;

import com.nokia.example.rlinks.SessionManager;
import com.nokia.example.rlinks.VisualStyles;
import java.io.IOException;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * A custom view item for displaying login status.
 */
public class LoginStatusItem
    extends AbstractCustomItem {

    private static final int H_SPACE = VisualStyles.LINK_H_SPACE;
    private static final int V_SPACE = VisualStyles.LINK_V_SPACE;
    private static final Font FONT_TEXT = VisualStyles.SMALL_BOLD_FONT;
    private final SessionManager session = SessionManager.getInstance();
    private final int height;
    private final SelectionListener listener;
    private static Image separatorImage = Image.createImage(1, 1);

    public interface SelectionListener {

        public void itemSelected();
    }

    static {
        try {
            separatorImage = Image.createImage("/midlets/rlinks/images/separator.png");
        }
        catch (IOException ex) {
            System.err.println("Can not load image " + ex);
        }
    }

    public LoginStatusItem(int preferredWidth, SelectionListener listener,
        Form form) {
        super(form, preferredWidth, null);

        this.height = getPrefContentHeight(width);
        this.listener = listener;
    }

    protected int getMinContentWidth() {
        return width;
    }

    protected int getMinContentHeight() {
        return height;
    }

    protected int getPrefContentWidth(int height) {
        return width;
    }

    protected int getPrefContentHeight(int width) {
        return V_SPACE + FONT_TEXT.getHeight() + V_SPACE * 2;
    }
    
    public void pointerReleased(int x, int y) {
        if (!dragging && listener != null) {
            listener.itemSelected();
        }
        super.pointerReleased(x, y);
    }

    /**
     * Draw the item.
     */
    protected void paint(final Graphics g, final int w, final int h) {
        int y = 0;

        String loginStatus = "";
        if (session.isLoggedIn()) {
            loginStatus = "Logged in as " + session.getUsername();
        }
        else {
            loginStatus = "Not logged in";
        }

        g.setColor(VisualStyles.COLOR_FOREGROUND);
        g.setFont(FONT_TEXT);
        g.drawString(loginStatus, w - H_SPACE * 2, y, Graphics.TOP
            | Graphics.RIGHT);

        g.setColor(VisualStyles.COLOR_FOREGROUND_DIM);
        if (separatorImage != null) {
            y = height - separatorImage.getHeight() - V_SPACE;
            g.drawImage(separatorImage, 0, y, Graphics.TOP | Graphics.LEFT);
        }
    }

    protected boolean traverse(int dir, int viewportWidth, int viewportHeight,
        int[] visRect_inout) {
        return false;
    }
}
