/*
 *
 *
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 *
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.midp.appmanager;

import com.sun.midp.installer.GraphicalInstaller;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The class is designed to present MIDlet or MIDlet suite item in Form container.
 * MIDletCustomItem consists of an icon and name associated with corresponding
 * MIDlet or MIDlet suite. Whether the name is too long and cannot fit the width
 * of a Form, the truncated name is shown with special truncation marker at the
 * end. When an item with truncated name is focused, full text of the name is
 * auto-scrolled to user. Also, the painting of the MIDlet/MIDlet suite item is
 * dependent on state of the application. It can indicate running, locked, disabled
 * state and availability of alert warnings related to the MIDlet application.
 */
class MIDletCustomItem extends CustomItem {
    /**
     * The font used to paint midlet names in the AppSelector.
     * Inner class cannot have static variables thus it has to be here.
     */
    private static final Font ICON_FONT = Font.getFont(
        Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);

    /**
     * The font used to paint midlet names in the AppSelector.
     * Inner class cannot have static variables thus it has to be here.
     */
    private static final Font ICON_FONT_UL = Font.getFont(
        Font.FACE_SYSTEM, Font.STYLE_BOLD | Font.STYLE_UNDERLINED,
        Font.SIZE_SMALL);

    /**
     * The image used to draw background for the midlet representation.
     * IMPL NOTE: it is assumed that background image is larger or equal
     * than all other images that are painted over it
     */
    private static final Image ICON_BG =
        GraphicalInstaller.getImageFromInternalStorage("_ch_hilight_bg");

    /** The pad between custom item's icon and background icon image */
    private static final int ICON_PAD = 1;

    /** Cached background image width */
    private static final int bgIconW = ICON_BG.getWidth();

    /** Cached background image height */
    private static final int bgIconH = ICON_BG.getHeight();

    /**
     * The icon used to display that user attention is requested
     * and that midlet needs to brought into foreground.
     */
    private static final Image FG_REQUESTED =
        GraphicalInstaller.getImageFromInternalStorage("_ch_fg_requested");

    /** The image used to draw disable midlet representation */
    private static final Image DISABLED_IMAGE =
        GraphicalInstaller.getImageFromInternalStorage("_ch_disabled");

    /**
     * The color used to draw midlet name
     * for the hilighted non-running running midlet representation.
     */
    private static final int ICON_HL_TEXT = 0x000B2876;

    /**
     * The color used to draw the shadow of the midlet name
     * for the non hilighted non-running midlet representation.
     */
    private static final int ICON_TEXT = 0x003177E2;

    /**
     * The color used to draw the midlet name
     * for the non hilighted running midlet representation.
     */
    private static final int ICON_RUNNING_TEXT = 0xbb0000;

    /**
     * The color used to draw the midlet name
     * for the hilighted running midlet representation.
     */
    private static final int ICON_RUNNING_HL_TEXT = 0xff0000;

    /** The pad between custom item's icon and text */
    private static final int ITEM_PAD = 2;

    /** Cached truncation mark */
    private static final char truncationMark =
        Resource.getString(ResourceConstants.TRUNCATION_MARK).charAt(0);


    /** Current locale */
    private String locale;

    /** Layout direction. True if direction is right-to-left */
    private boolean rtlDirection;

    /** Orientation of text, can be Graphics.RIGHT or Graphics.Left */
    private int textOrientation;


    /** A Timer which will handle firing repaints of the ScrollPainter */
    protected Timer textScrollTimer;

    /** Text auto-scrolling parameters */
    private static int SCROLL_RATE = 250;
    private static int SCROLL_DELAY = 500;
    private static int SCROLL_SPEED = 10;

    /** Default constructor */
    protected MIDletCustomItem() {
        super(null);
    }

    /** Constructs a MIDlet representation item */
    MIDletCustomItem(String displayName, Image midletIcon) {
        super(null);
        init(displayName, midletIcon);
    }

    /** Init item instance */
    protected void init(String displayName, Image midletIcon) {
        icon = (midletIcon != null) ? midletIcon : getDefaultIcon();
        text = displayName.toCharArray();
        textLen = displayName.length();
        truncWidth = ICON_FONT.charWidth(truncationMark);
        truncated = false;
        textScrollTimer = null;
        xScrollOffset = 0;
    }

    /**
     * Gets the minimum width of a MIDlet representation in the owner screen.
     * @return the minimum width of a MIDlet representation in the owner screen.
     */
    protected int getMinContentWidth() {
        return owner.getWidth();
    }

    /**
     * Gets the minimum height of a midlet representation in the owner screen.
     * @return the minimum height of a MIDlet representation the owner screen.
     */
    protected int getMinContentHeight() {
        return ICON_BG.getHeight() > ICON_FONT.getHeight() ?
            ICON_BG.getHeight() : ICON_FONT.getHeight();
    }

    /**
     * Gets the preferred width of a midlet representation in
     * the owner screen based on the passed in height.
     *
     * @param height the amount of height available for this Item
     * @return the minimum width of a MIDlet representation in the owner screen.
     */
    protected int getPrefContentWidth(int height) {
        return owner.getWidth();
    }

    /**
     * Gets the preferred height of a midlet representation in
     * the App Selector Screen based on the passed in width.
     * @param width the amount of width available for this Item
     * @return the minimum height of a midlet representation
     *         in the App Selector Screen.
     */
    protected int getPrefContentHeight(int width) {
        return ICON_BG.getHeight() > ICON_FONT.getHeight() ?
            ICON_BG.getHeight() : ICON_FONT.getHeight();
    }

    /**
     * On size change event we define the item's text
     * according to item's new width
     * @param w The current width of this Item
     * @param h The current height of this Item
     */
    protected void sizeChanged(int w, int h) {
        stopScroll();
        width = w;
        height = h;
        int widthForText = w - ITEM_PAD - ICON_BG.getWidth();
        int displayNameWidth = ICON_FONT.charsWidth(text, 0, textLen);
        scrollWidth = displayNameWidth - widthForText + w/5;
        truncated = displayNameWidth > widthForText;
    }

    /**
     * Gets default MIDlet icon
     * @return Image with default icon
     */
    private static Image getDefaultIcon() {
        return GraphicalInstaller.
            getImageFromInternalStorage("_ch_single");
    }
    
    /**
     * Gets running state of the MIDlet or MIDlte suite.
     * @return true is MIDlet is running or associated MIDlet suite has running
     *   MIDlets, false otherwise
     */
    boolean isRunning() {
        return false;
    }

    /**
     * Gets locked state of the MIDlet or MIDlet suite.
     * Locked states limits operations available for this item.
     * @return true if MIDlet or MIDlet suite is locked, false otherwise
     */
    boolean isLocked() {
        return false;
    }

    /**
     * True if  alert is waiting for the foreground in at
     * least of one of the MIDlets from this suite.
     * @return true if there is a waiting alert
     */
    boolean isAnyAlertWaiting() {
        return false;
    }

    /**
     * True if this MIDlet or MIDlet suite item is enabled
     * @return true if the item is enabled
     */
    boolean isEnabled() {
        return true;
    }

    /**
     * Paints the content of a midlet representation in
     * the App Selector Screen.
     * Note that icon representing that foreground was requested
     * is painted on to of the existing ickon.
     * @param g The graphics context where painting should be done
     * @param w The width available to this Item
     * @param h The height available to this Item
     */
    protected void paint(Graphics g, int w, int h) {
        int cX = g.getClipX();
        int cY = g.getClipY();
        int cW = g.getClipWidth();
        int cH = g.getClipHeight();

        // TODO: Don't check locale on each repaint, listen for
        //   locale change events and update item state accordingly
        locale = System.getProperty("microedition.locale");
        if (locale != null && locale.equals("he-IL")) {
            rtlDirection = true;
            textOrientation = Graphics.RIGHT;
        } else {
            rtlDirection = false;
            textOrientation = Graphics.LEFT;
        }

        if ((cW + cX) > bgIconW) {
            if (text != null && h >= ICON_FONT.getHeight()) {

                int color;
                if (isRunning()) {
                    color = hasFocus ?
                        ICON_RUNNING_HL_TEXT : ICON_RUNNING_TEXT;
                } else {
                    color = hasFocus ? ICON_HL_TEXT : ICON_TEXT;
                }

                g.setColor(color);
                g.setFont(isLocked() ? ICON_FONT_UL : ICON_FONT);

                boolean truncate = (xScrollOffset == 0) && truncated;

                if (rtlDirection) {
                    g.clipRect(truncate ? truncWidth + ITEM_PAD : ITEM_PAD, 0,
                        truncate ? w - truncWidth - bgIconW - 2 * ITEM_PAD :
                                w - bgIconW - 2 * ITEM_PAD, h);
                    g.drawChars(text, 0, textLen,
                        w - (bgIconW + ITEM_PAD + xScrollOffset),
                        (h - ICON_FONT.getHeight())/2, textOrientation | Graphics.TOP);
                    g.setClip(cX, cY, cW, cH);

                    if (truncate) {
                        g.drawChar(truncationMark, truncWidth,
                            (h - ICON_FONT.getHeight())/2, Graphics.RIGHT | Graphics.TOP);
                    }
                } else {
                    g.clipRect(bgIconW + ITEM_PAD, 0,
                    truncate ? w - truncWidth - bgIconW - 2 * ITEM_PAD :
                               w - bgIconW - 2 * ITEM_PAD, h);
                    g.drawChars(text, 0, textLen,
                        bgIconW + ITEM_PAD + xScrollOffset, (h - ICON_FONT.getHeight())/2,
                            Graphics.LEFT | Graphics.TOP);
                    g.setClip(cX, cY, cW, cH);

                    if (truncate) {
                        g.drawChar(truncationMark, w - truncWidth,
                            (h - ICON_FONT.getHeight())/2, Graphics.LEFT | Graphics.TOP);
                    }
                }

            }
        }

        int anchorY = (h - bgIconH)/2;
        final int ICON_PAD2 = ICON_PAD * 2;

        if (cX < bgIconW) {
            if (rtlDirection) {
                if (hasFocus) {
                    g.drawImage(ICON_BG, w - bgIconW, anchorY,
                        Graphics.TOP | Graphics.LEFT);
                }

                if (icon != null) {
                    g.clipRect(
                        w - bgIconW + ICON_PAD, anchorY + ICON_PAD,
                        bgIconW - ICON_PAD2, bgIconH - ICON_PAD2);
                    g.drawImage(icon,
                        w - (bgIconW - icon.getWidth())/2,
                        anchorY + (bgIconH - icon.getHeight())/2,
                        Graphics.TOP | Graphics.RIGHT);
                    g.setClip(cX, cY, cW, cH);
                }
                // Draw special icon if user attention is requested and
                // that midlet needs to be brought into foreground by the user
                if (isAnyAlertWaiting()) {
                    g.drawImage(FG_REQUESTED,
                        w - (bgIconW - FG_REQUESTED.getWidth()), 0,
                        Graphics.TOP | Graphics.LEFT);
                }

                if (!isEnabled()) {
                    // indicate that this suite is disabled
                    g.drawImage(DISABLED_IMAGE,
                        w - (bgIconW - DISABLED_IMAGE.getWidth())/2,
                        anchorY + (bgIconH - DISABLED_IMAGE.getHeight())/2,
                        Graphics.TOP | Graphics.LEFT);
                }
            } else {
                if (hasFocus) {
                    g.drawImage(ICON_BG, 0, anchorY,
                        Graphics.TOP | Graphics.LEFT);
                }

                if (icon != null) {
                    g.clipRect(
                        ICON_PAD, anchorY + ICON_PAD,
                        bgIconW - ICON_PAD2, bgIconH - ICON_PAD2);
                    g.drawImage(icon,
                        (bgIconW - icon.getWidth())/2,
                        anchorY + (bgIconH - icon.getHeight())/2,
                        Graphics.TOP | Graphics.LEFT);
                    g.setClip(cX, cY, cW, cH);
                }

                // Draw special icon if user attention is requested and
                // that midlet needs to be brought into foreground by the user
                if (isAnyAlertWaiting()) {
                    g.drawImage(FG_REQUESTED,
                        bgIconW - FG_REQUESTED.getWidth(), 0,
                        Graphics.TOP | Graphics.LEFT);
                }

                if (!isEnabled()) {
                    // indicate that this suite is disabled
                    g.drawImage(DISABLED_IMAGE,
                        (bgIconW - DISABLED_IMAGE.getWidth())/2,
                        anchorY + (bgIconH - DISABLED_IMAGE.getHeight())/2,
                        Graphics.TOP | Graphics.LEFT);
                }
            }
        }

    }

    /**
    * Start the scrolling of the text
    */
    protected void startScroll() {
        if (!hasFocus || !truncated) {
            return;
        }
        stopScroll();
        if (textScrollTimer == null) {
            textScrollTimer = new Timer();
        }
        textScrollPainter = new TextScrollPainter();
        textScrollTimer.schedule(textScrollPainter, SCROLL_DELAY, SCROLL_RATE);
    }

    /**
    * Stop the scrolling of the text
    */
    protected void stopScroll() {
        if (textScrollPainter == null) {
            return;
        }
        xScrollOffset = 0;
        textScrollPainter.cancel();
        textScrollPainter = null;
        if (rtlDirection) {
                repaint(0, 0, width, height);
            } else {
                repaint(bgIconW, 0, width, height);
            }
    }

    /**
    * Called repeatedly to animate a side-scroll effect for text
    */
    protected void repaintScrollText() {
        if (-xScrollOffset < scrollWidth) {
                xScrollOffset -= SCROLL_SPEED;
            if (rtlDirection) {
                repaint(0, 0, width, height);
            } else {
                repaint(bgIconW, 0, width, height);
            }

        } else {
            // already scrolled to the end of text
            stopScroll();
        }
    }

    /**
     * Handles traversal.
     * @param dir The direction of traversal (Canvas.UP, Canvas.DOWN,
     *            Canvas.LEFT, Canvas.RIGHT)
     * @param viewportWidth The width of the viewport in the AppSelector
     * @param viewportHeight The height of the viewport in the AppSelector
     * @param visRect_inout The return array that tells AppSelector
     *        which portion of the MidletCustomItem has to be made visible
     * @return true if traversal was handled in this method
     *         (this MidletCustomItem just got focus or there was an
     *         internal traversal), otherwise false - to transfer focus
     *         to the next item
     */
    protected boolean traverse(int dir,
                               int viewportWidth, int viewportHeight,
                               int visRect_inout[]) {
        // Entirely visible and hasFocus
        if (!hasFocus) {
            hasFocus = true;
        }

        visRect_inout[0] = 0;
        visRect_inout[1] = 0;
        visRect_inout[2] = width;
        visRect_inout[3] = height;

        startScroll();

        return false;
    }

    /**
     * Handles traversal out. This method is called when this
     * MidletCustomItem looses focus.
     */
    protected void traverseOut() {
        hasFocus = false;
        stopScroll();
    }

    /**
     * Repaints MidletCustomItem. Called when internal state changes.
     */
    public void update() {
        repaint();
    }

    /**
     * Sets the owner of this MIDletCustomItem
     * @param form in which this MIDletCustomItem is shown
     */
    void setOwner(Form form) {
        owner = form;
    }

    /**
     * Called by owner when destroyApp happens to clean up data.
     * Timer that shedules scrolling text repainting should be
     * canceled when AMS MIDlet is about to be destroyed to avoid
     * generation of repaint events.
     */
    public void cleanUp() {
        if (textScrollTimer != null) {
            textScrollTimer.cancel();
        }
    }

    /**
     * Sets default <code>Command</code> for this <code>Item</code>.
     *
     * @param c the command to be used as this <code>Item's</code> default
     * <code>Command</code>, or <code>null</code> if there is to
     * be no default command
     */
    public void setDefaultCommand(Command c) {
        defaultCommand = c;
        super.setDefaultCommand(c);
    }

    /** A TimerTask which will repaint scrolling text  on a repeated basis */
    protected TextScrollPainter textScrollPainter;

    /** Width of the scroll area for text */
    protected int scrollWidth;

    /** If text is truncated */
    boolean truncated;

    /**
     * Pixel offset to the start of the text field  (for example, if
     * xScrollOffset is -60 it means means that the text in this
     * text field is scrolled 60 pixels left of the left edge of the
     * text field)
     */
    protected int xScrollOffset;

    /** Helper class used to repaint scrolling text if needed */
    private class TextScrollPainter extends TimerTask {
        /** Repaint the item text */
        public final void run() {
            repaintScrollText();
        }
    }

    /** True if this MidletCustomItem has focus, and false - otherwise */
    boolean hasFocus;

    /**
     * The owner of this MIDletCustomItem
     * IMPL_NOTE: This field has the same name with package private
     *   Item.owner, however the field values are independent.
     */
    Form owner;

    /** The width of this MidletCustomItem */
    int width;
    /** The height of this MIDletSuiteInfo */
    int height;

    /** Cashed width of the truncation mark */
    int truncWidth;

    /** The text of this MIDletCustomItem*/
    char[] text;

    /** Length of the text */
    int textLen;

    /** The icon to be used to draw this midlet representation */
    Image icon;

    /** Current default command */
    Command defaultCommand;
}
