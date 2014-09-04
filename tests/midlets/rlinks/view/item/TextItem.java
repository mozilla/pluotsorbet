/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/
package com.nokia.example.rlinks.view.item;

import com.nokia.example.rlinks.VisualStyles;
import com.nokia.example.rlinks.util.TextWrapper;
import java.util.Vector;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;

/**
 * A custom view item for displaying text with a custom font.
 *
 * Displaying text could obviously also be done with standard
 * text fields or by just appending Strings into the Form view,
 * but this approach makes it possible for us to style the item
 * to our liking.
 */
public class TextItem
    extends AbstractCustomItem {

    private Font font = VisualStyles.MEDIUM_FONT;
    private final String text;
    private Vector textLines;
    private int height;

    /**
     * Create a TextItem with the default font.
     * 
     * @param text Item text
     * @param preferredWidth Preferred width 
     */
    public TextItem(String text, int preferredWidth, Form form) {
        this(text, preferredWidth, null, form);
    }

    /**
     * Create a TextItem with the specified font.
     *
     * @param text Item text
     * @param preferredWidth Preferred width
     * @param font Font to use
     */
    public TextItem(String text, int preferredWidth, Font font, Form form) {
        super(form, preferredWidth, null);

        this.text = text;
        if (font != null) {
            this.font = font;
        }
        textLines = getTextLines();
        this.height = getPrefContentHeight(width);
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
        return VisualStyles.COMMENT_V_SPACE
            + textLines.size() * font.getHeight()
            + VisualStyles.COMMENT_V_SPACE;
    }

    /**
     * Draw the item.
     */
    protected void paint(Graphics g, int w, int h) {
        int x = VisualStyles.COMMENT_H_SPACE;
        int y = VisualStyles.COMMENT_V_SPACE;

        g.setColor(VisualStyles.COLOR_FOREGROUND);
        g.setFont(font);
        for (int i = 0, len = textLines.size(); i < len; i++) {
            g.drawString((String) textLines.elementAt(i), x, y, Graphics.TOP
                | Graphics.LEFT);
            y += font.getHeight();
        }
    }

    protected boolean traverse(int dir, int viewportWidth, int viewportHeight,
        int[] visRect_inout) {
        return false;
    }

    private Vector getTextLines() {
        int textWidth = width - VisualStyles.COMMENT_H_SPACE * 2
            - deviceMargin;
        return TextWrapper.wrapTextToWidth(text, textWidth, this.font);
    }
}
