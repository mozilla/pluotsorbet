/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/
package com.nokia.example.rlinks.view.item;

import com.nokia.example.rlinks.model.CommentThing;
import com.nokia.example.rlinks.VisualStyles;
import com.nokia.example.rlinks.util.TextWrapper;
import java.util.Vector;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;

/**
 * A custom view item for retrieving more comments.
 */
public class MoreCommentsItem
    extends CommentItem {

    private final int V_SPACE = (int) (CommentItem.V_SPACE * 1.5);

    /**
     * Create a MoreCommentsItem.
     * 
     * @param comment The CommentThing whose items we're dealing with
     * @param listener Listener to signal of eslections
     * @param preferredWidth Preferred width
     * @param form Parent form of this CommentItem
     */
    public MoreCommentsItem(CommentThing comment,
        CommentSelectionListener listener, int preferredWidth, Form form) {
        super(comment, listener, preferredWidth, form);
        this.height = getPrefContentHeight(preferredWidth);
    }

    protected Vector getBodyLines() {
        final String text = "Load more replies";
        return TextWrapper.wrapTextToWidth(text, width - 3 * H_SPACE - xIndent
            - (isFTDevice ? 8 : 0), FONT_AUTHOR);
    }

    /**
     * Indicate that stuff is now being loaded.
     */
    public void setLoading() {
        bodyLines.removeAllElements();
        bodyLines.addElement("Loading...");
        invalidate();
    }

    protected int getPrefContentHeight(int width) {
        return V_SPACE + bodyLines.size() * FONT_AUTHOR.getHeight() + V_SPACE;
    }

    /**
     * @see CommentItem#drawContent(javax.microedition.lcdui.Graphics, int, int) 
     */
    public void paint(final Graphics g, final int w, final int h) {
        int x = H_SPACE + xIndent;
        int y = V_SPACE;

        g.setColor(VisualStyles.COLOR_HIGHLIGHTED_FOREGROUND);
        g.drawLine(xIndent, 0, xIndent, h);

        g.setColor(VisualStyles.COLOR_FOREGROUND);
        g.setFont(FONT_AUTHOR);

        final int fontHeight = FONT_AUTHOR.getHeight();
        for (int i = 0; i < bodyLines.size(); i++) {
            g.drawString((String) bodyLines.elementAt(i), x, y, Graphics.TOP
                | Graphics.LEFT);
            y += fontHeight;
        }
    }
}
