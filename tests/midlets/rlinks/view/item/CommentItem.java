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
import com.nokia.example.rlinks.model.CommentThing;
import com.nokia.example.rlinks.util.DatePrettyPrinter;
import com.nokia.example.rlinks.util.TextWrapper;
import com.nokia.example.rlinks.util.TouchChecker;
import com.nokia.mid.ui.LCDUIUtil;
import java.util.Vector;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;

/**
 * A custom view item for representing a CommentThing.
 */
public class CommentItem
    extends AbstractCustomItem {

    protected static final int H_SPACE = VisualStyles.COMMENT_H_SPACE;
    protected static final int V_SPACE = VisualStyles.COMMENT_V_SPACE;
    protected static final Font FONT_AUTHOR = VisualStyles.MEDIUM_BOLD_FONT;
    protected static final Font FONT_META = VisualStyles.SMALL_BOLD_FONT;
    protected static final Font FONT_BODY = VisualStyles.MEDIUM_FONT;
    protected static final int H_FONT_AUTHOR = FONT_AUTHOR.getHeight();
    protected static final int H_FONT_META = FONT_META.getHeight();
    protected static final int H_FONT_BODY = FONT_BODY.getHeight();
    protected int height;
    protected final int preferredWidth;
    protected final int xIndent;
    protected Vector bodyLines;
    protected final CommentThing comment;
    protected CommentSelectionListener listener;
    protected int itemIndex;
    private final String metaText;

    /**
     * Interface used to signal comment selections.
     */
    public interface CommentSelectionListener {

        public void commentSelected(CommentThing comment, int itemIndex);
    }

    /**
     * Create a CommentItem.
     * 
     * @param comment CommentThing represented by this item
     * @param listener Listener to signal of selections
     * @param preferredWidth Preferred width
     * @param form Parent form of this CommentItem
     */
    public CommentItem(CommentThing comment, CommentSelectionListener listener,
        int preferredWidth, Form form) {
        super(form, preferredWidth, null);

        this.comment = comment;
        this.preferredWidth = preferredWidth;
        this.listener = listener;

        int level = comment.getLevel();
        this.xIndent = level == 0 ? 0 : Math.max(0, 12 * level) - (5 * (level
            - 1));
        this.bodyLines = getBodyLines();
        this.height = getPrefContentHeight(preferredWidth);
        this.metaText = comment.getCreated() == null ? "" : DatePrettyPrinter.
            prettyPrint(comment.getCreated()) + comment.getFormattedScore();

        if (TouchChecker.DIRECT_TOUCH_SUPPORTED) {
            LCDUIUtil.setObjectTrait(this, "nokia.ui.s40.item.direct_touch",
                new Boolean(true));
        }
    }

    /**
     * Split the body text into lines.
     *
     * @return Comment body as a Vector of text lines
     */
    protected Vector getBodyLines() {
        return TextWrapper.wrapTextToWidth(comment.getBody(), width - 3
            * H_SPACE - xIndent - deviceMargin, FONT_BODY);
    }

    protected int getMinContentWidth() {
        return width;
    }

    protected int getMinContentHeight() {
        return height;
    }

    protected int getPrefContentWidth(int height) {
        return preferredWidth;
    }

    protected int getPrefContentHeight(int width) {
        return V_SPACE + H_FONT_AUTHOR + H_FONT_META + (int) (V_SPACE * 1.5)
            + bodyLines.size() * H_FONT_BODY + V_SPACE;
    }

    /**
     * Draw the item. Subclasses should override this.
     */
    protected void paint(final Graphics g, final int w, final int h) {
        int x = H_SPACE + xIndent;
        int y = V_SPACE;

        g.setColor(VisualStyles.COLOR_HIGHLIGHTED_FOREGROUND);
        g.drawLine(xIndent, V_SPACE, xIndent, h - V_SPACE);

        g.setColor(VisualStyles.COLOR_FOREGROUND);
        if (comment.getAuthor() != null) {
            g.setFont(FONT_AUTHOR);
            g.drawString(comment.getAuthor(), x, y, Graphics.TOP | Graphics.LEFT);
            y += H_FONT_AUTHOR;
        }

        g.setFont(FONT_META);
        g.drawString(metaText, x, y, Graphics.TOP | Graphics.LEFT);
        y += H_FONT_META + V_SPACE * 1.5;

        g.setColor(VisualStyles.COLOR_FOREGROUND_DIM);
        g.setFont(FONT_BODY);
        for (int i = 0; i < bodyLines.size(); i++) {
            g.drawString((String) bodyLines.elementAt(i), x, y, Graphics.TOP
                | Graphics.LEFT);
            y += H_FONT_BODY;
        }
    }

    public void pointerReleased(int x, int y) {
        if (!dragging && listener != null) {
            listener.commentSelected(comment, itemIndex);
        }
        super.pointerReleased(x, y);
    }

    protected boolean traverse(int dir, int viewportWidth, int viewportHeight,
        int[] visRect_inout) {
        return false;
    }

    public int getItemIndex() {
        return itemIndex;
    }

    public void setItemIndex(int index) {
        this.itemIndex = index;
    }
}
