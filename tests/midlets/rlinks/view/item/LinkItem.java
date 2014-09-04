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
import com.nokia.example.rlinks.model.LinkThing;
import com.nokia.example.rlinks.network.ImageLoader;
import com.nokia.example.rlinks.util.TextWrapper;
import com.nokia.example.rlinks.util.TouchChecker;
import com.nokia.mid.ui.LCDUIUtil;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * A custom view item representing a LinkThing.
 */
public class LinkItem
    extends AbstractCustomItem {

    public static final int H_SPACE = VisualStyles.LINK_H_SPACE;
    public static final int V_SPACE = VisualStyles.LINK_V_SPACE;
    public static final int V_SPACE_HALF = V_SPACE / 2;
    public static final int SEPARATOR_H_SPACE =
        VisualStyles.LINK_SEPARATOR_H_SPACE;
    public static final int SEPARATOR_V_SPACE =
        VisualStyles.LINK_SEPARATOR_V_SPACE;
    public static final int THUMBNAIL_HEIGHT = 70;
    public static final int THUMBNAIL_H_SPACE = THUMBNAIL_HEIGHT + 2 * H_SPACE;
    // Determine fonths and their measurements once
    private static final Font FONT_TITLE = VisualStyles.LARGE_FONT;
    private static final Font FONT_SCORE = VisualStyles.LARGE_BOLD_FONT;
    private static final Font FONT_DETAILS = VisualStyles.SMALL_BOLD_FONT;
    private static final int H_FONT_TITLE = FONT_TITLE.getHeight();
    private static final int H_FONT_SCORE = FONT_SCORE.getHeight();
    private static final int H_FONT_DETAILS = FONT_DETAILS.getHeight();
    private int height;
    private final int preferredWidth;
    private Vector titleLines;
    private final String detailsText;
    private Vector detailsLines;
    private final LinkThing link;
    private final LinkSelectionListener listener;
    private final Hashtable imageCache;
    private final boolean showImage;
    private final ImageLoader imageLoader = ImageLoader.getInstance();
    private static Image separatorImage = Image.createImage(1, 1);

    /**
     * Interface used to signal link selections.
     */
    public interface LinkSelectionListener {

        public void linkSelected(LinkThing link);
    }

    /**
     * Load the static resources once and for all.
     */
    static {
        try {
            separatorImage = Image.createImage("/midlets/rlinks/images/separator-curved.png");
        }
        catch (IOException ex) {
            System.err.println("Couldn't not load image: " + ex.getMessage());
        }
    }

    /**
     * Create a Linkitem.
     *
     * @param link Link represented by the Linkitem
     * @param preferredWidth Preferred width
     * @param showSubreddit Whether the subreddit should be displayed
     * @param listener Listener to signal of link selections
     * @param imageCache A cache to store images into
     */
    public LinkItem(LinkThing link, int preferredWidth, boolean showSubreddit,
        LinkSelectionListener listener, Hashtable imageCache, Form form) {
        super(form, preferredWidth, null);
        this.link = link;
        this.preferredWidth = preferredWidth;
        this.listener = listener;
        this.imageCache = imageCache;

        showImage = link.getThumbnail() != null;

        updateTitleLines();

        // E.g. "funny @ imgur.com", only "imgur.com" if a category is selected
        detailsText = (showSubreddit ? link.getSubreddit() + " @ " : "") + link.
            getDomain();
        updateDetailsLines();
        this.height = getPrefContentHeight(preferredWidth);

        if (TouchChecker.DIRECT_TOUCH_SUPPORTED) {
            LCDUIUtil.setObjectTrait(this, "nokia.ui.s40.item.direct_touch",
                new Boolean(true));
        }

        if (showImage) {
            loadImage();
        }
    }

    public LinkThing getLink() {
        return this.link;
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
        return (int) (V_SPACE_HALF + // Reserve space for a thumbnail or the title text, whichever
            // one of the two takes more vertical space.
            Math.max(
            showImage ? THUMBNAIL_HEIGHT : 0,
            titleLines.size() * H_FONT_TITLE + V_SPACE_HALF + detailsLines.size()
            * H_FONT_DETAILS + V_SPACE_HALF) + V_SPACE + H_FONT_SCORE
            + V_SPACE_HALF);
    }
    
    /**
     * Draw the friendly item.
     */
    protected void paint(final Graphics g, final int w, final int h) {        
        int x = H_SPACE;
        int y = V_SPACE_HALF;
        g.setFont(FONT_TITLE);
        g.setColor(VisualStyles.COLOR_FOREGROUND);
        for (int i = 0; i < titleLines.size(); i++) {
            g.drawString((String) titleLines.elementAt(i), x, y, Graphics.TOP
                | Graphics.LEFT);
            y += H_FONT_TITLE;
        }

        // Details text
        y += V_SPACE_HALF;
        g.setFont(FONT_DETAILS);
        for (int i = 0; i < detailsLines.size(); i++) {
            g.drawString((String) detailsLines.elementAt(i), x, y, Graphics.TOP
                | Graphics.LEFT);
            y += H_FONT_DETAILS;
        }

        // Draw left and right part of the separator, then the score in the middle
        final String scoreStr = (link.getScore() > 0 ? "+" : "")
            + link.getScore();

        final int scoreHeight = H_FONT_SCORE + SEPARATOR_V_SPACE;
        y = height - scoreHeight - V_SPACE_HALF;

        g.setColor(VisualStyles.COLOR_FOREGROUND_DIM);

        if (separatorImage != null) {
            g.drawImage(separatorImage, width / 2, y + scoreHeight / 2,
                Graphics.TOP
                | Graphics.HCENTER);
        }

        g.setFont(FONT_SCORE);
        g.drawString(scoreStr, width / 2, y - V_SPACE_HALF, Graphics.TOP
            | Graphics.HCENTER);

        // Draw thumbnail image
        if (showImage && link.getImage() != null) {
            g.drawImage(link.getImage(), width - H_SPACE, V_SPACE_HALF,
                Graphics.TOP | Graphics.RIGHT);
        }
    }

    private void updateTitleLines() {
        // Allocate some space for a possible thumbnail
        final int titleWidth = width - 4 * H_SPACE
            - (showImage ? THUMBNAIL_H_SPACE : 0) - deviceMargin;
        titleLines = TextWrapper.wrapTextToWidth(link.getTitle(), titleWidth,
            FONT_TITLE);
    }

    private void updateDetailsLines() {
        final int detailsWidth = width - 4 * H_SPACE
            - (showImage ? THUMBNAIL_H_SPACE : 0) - deviceMargin;
        detailsLines = TextWrapper.wrapTextToWidth(detailsText, detailsWidth,
            FONT_DETAILS);
    }

    /**
     * Load the image represented by this Link.
     */
    private void loadImage() {
        String url = link.getThumbnail();
        imageLoader.loadImage(url, Image.createImage(70, 70),
            new ImageLoader.Listener() {

                public void imageLoaded(final Image image) {
                    link.setImage(image);
                    repaint();
                }
            }, imageCache);
    }    

    public void pointerReleased(int x, int y) {
        if (!dragging) {
            listener.linkSelected(this.link);
        }
        super.pointerReleased(x, y);
    }

    public void refresh() {
        repaint();
    }

    protected boolean traverse(int dir, int viewportWidth, int viewportHeight,
        int[] visRect_inout) {
        return false;
    }
}
