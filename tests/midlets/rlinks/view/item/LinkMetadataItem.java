/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/
package com.nokia.example.rlinks.view.item;

import com.nokia.example.rlinks.model.LinkThing;
import com.nokia.example.rlinks.VisualStyles;
import com.nokia.example.rlinks.util.DatePrettyPrinter;
import java.io.IOException;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * A custom view item for showing Link related metadata.
 */
public class LinkMetadataItem
    extends AbstractCustomItem {

    private static final int H_SPACE = VisualStyles.LINK_H_SPACE;
    private static final int V_SPACE = VisualStyles.LINK_V_SPACE / 2;
    private static final int SEPARATOR_V_SPACE =
        VisualStyles.LINK_SEPARATOR_V_SPACE;
    protected static final Font FONT_TEXT = VisualStyles.MEDIUM_FONT;
    protected static final Font FONT_SEPARATOR = VisualStyles.MEDIUM_BOLD_FONT;
    protected final int height;
    private final LinkThing link;
    private static Image separatorImage = Image.createImage(1, 1);

    static {
        try {
            separatorImage = Image.createImage("/midlets/rlinks/images/separator-curved-wide.png");
        }
        catch (IOException ex) {
            System.err.println("Can not load image " + ex);
        }
    }

    /**
     * Create a LinkMetadataItem.
     *
     * @param link LinkThing related to this item
     * @param preferredWidth Preferred width
     */
    public LinkMetadataItem(LinkThing link, int preferredWidth, Form form) {
        super(form, preferredWidth, null);

        this.link = link;
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
        return V_SPACE + FONT_TEXT.getHeight() + V_SPACE * 2 + FONT_SEPARATOR.
            getHeight() + V_SPACE * 2;
    }

    /**
     * Draw the item.
     */
    protected void paint(final Graphics g, final int w, final int h) {
        final String dateStr = DatePrettyPrinter.prettyPrint(link.getCreated());
        final String numCommentsStr = "" + link.getNumComments() + " comments";
        final int ncHeight = FONT_SEPARATOR.getHeight() + SEPARATOR_V_SPACE;

        int x = H_SPACE;
        int y = V_SPACE;

        g.setColor(VisualStyles.COLOR_FOREGROUND);
        g.setFont(FONT_TEXT);
        g.drawString("by " + link.getAuthor(), x, y, Graphics.TOP
            | Graphics.LEFT);
        g.drawString(dateStr, w - H_SPACE * 2, y, Graphics.TOP
            | Graphics.RIGHT);

        // Draw left and right part of the separator, then the score in the middle
        y = height - ncHeight - V_SPACE / 2;

        g.setColor(VisualStyles.COLOR_FOREGROUND_DIM);
        if (separatorImage != null) {
            g.drawImage(separatorImage, w / 2, y + ncHeight / 2, Graphics.TOP
                | Graphics.HCENTER);
        }
        g.setFont(FONT_SEPARATOR);
        g.drawString(numCommentsStr, w / 2, y - 4, Graphics.TOP
            | Graphics.HCENTER);
    }

    protected boolean traverse(int dir, int viewportWidth, int viewportHeight,
        int[] visRect_inout) {
        return false;
    }

    public void refresh() {
        this.invalidate();
    }
}
