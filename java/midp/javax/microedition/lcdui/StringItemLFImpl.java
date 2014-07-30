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

package javax.microedition.lcdui;

import com.sun.midp.lcdui.Text;
import com.sun.midp.configurator.Constants;
import com.sun.midp.chameleon.CGraphicsUtil;
import com.sun.midp.chameleon.skins.ScreenSkin;
import com.sun.midp.chameleon.skins.StringItemSkin;
import com.sun.midp.chameleon.skins.resources.StringItemResources;

/**
* This is the look &amps; feel implementation for StringItem.
*/
class StringItemLFImpl extends ItemLFImpl implements StringItemLF {
        
    /**
     * Creates a look&amps;feel for a StringItem
     * @param strItem The StringItem associated with this look&amps;feel
     */
    StringItemLFImpl(StringItem strItem) {
        
        super(strItem);
        
        this.strItem = strItem;

        StringItemResources.load();
        
        checkTraverse();

        // when no commands are added actual appearance
        // is PLAIN; actual appearance will be the same
        // as appearance set in StringItem if a command is added
        // this StringItem
        this.appearanceMode = Item.PLAIN;
    }


    // *****************************************************
    //  Public methods (StringItemLF interface impl)
    // *****************************************************
    
    /**
     * Get the preferred width of this Item
     *
     * @param h the tentative content height in pixels, or -1 if a
     * tentative height has not been computed
     * @return the preferred width
     */
    public int lGetPreferredWidth(int h) {

        // IMPL NOTE: we ignore the 'h' value and just return
        // a basic width based on our contents.

        // In BUTTON mode internal layout and sizing is done in
        // ItemLFImpl
        if (appearanceMode == Item.BUTTON) {
            return super.lGetPreferredWidth(h);
        }
        
        // In PLAIN and HYPERLINK modes label and content string are
        // almost concatenated together and wrapped together
        // (if both are not empty there is a horizontal padding between them)

        int size[] = contentBounds;
        Text.getTwoStringsSize(size, strItem.label, strItem.str,
            ScreenSkin.FONT_LABEL,
            strItem.font, lGetAvailableWidth(),
            getHorizontalPad());
        return size[WIDTH];
    }
    
    /**
     * Get the preferred height of this Item
     *
     * @param w the tentative content width in pixels, or -1 if a
     * tentative width has not been computed
     * @return the preferred height
     */
    public int lGetPreferredHeight(int w) {

        // In BUTTON and HIPERLINK  mode internal layout and sizing is done in
        // ItemLFImpl
        if (appearanceMode == Item.BUTTON || appearanceMode == Item.HYPERLINK) {
            return super.lGetPreferredHeight(w);
        }

        // In PLAIN and HYPERLINK modes label and content string are
        // almost concatenated together and wrapped together
        // (almost because there is a horizontal padding between them)
        int size[] = contentBounds;

        Text.getTwoStringsSize(size, strItem.label, strItem.str,
                ScreenSkin.FONT_LABEL,
                strItem.font,
                w == -1 ? lGetAvailableWidth() : w,
                getHorizontalPad());
        
        return size[HEIGHT];
    }

    /**
     * Get the minimum width of this Item. 
     * Calculate the minimum width as the width of double "W". If the calculated 
     * width is greater than available width just return available width. 
     *
     * @return the minimum width
     */
    public int lGetMinimumWidth() {
	int minWidth = strItem.font.charWidth('W') * 2;
	int availableWidth = lGetAvailableWidth();

	return (minWidth > availableWidth ? availableWidth : minWidth);
    }


    /**
     * Get the minimum height of this Item. 
     * Calculate the minimum height as the height of the font.
     *
     * @return the minimum height
     */
    public int lGetMinimumHeight() {
	return strItem.font.getHeight();
    }

    /**
     * Notifies L&amps;F of a command addition in the corresponding StringItem.
     * @param cmd the newly added command
     * @param i the index of the added command in the StringItem's
     *        commands[] array
     */
    public void lAddCommand(Command cmd, int i) {
        super.lAddCommand(cmd, i);

        // restore the value of the original appearanceMode
        if ((strItem.numCommands >= 1) && (appearanceMode == Item.PLAIN)) {
            appearanceMode = strItem.appearanceMode == Item.BUTTON ?
                             Item.BUTTON : Item.HYPERLINK;
            lRequestInvalidate(true, true);
        }

        // checkTraverse(); right now traversability is not command dependent
        // lRequestInvalidate(true, true);
    }

    /**
     * Notifies L&amps;F of a command removal in the corresponding StringItem.
     * @param cmd the newly removed command
     * @param i the index of the removed command in the StringItem's
     *        commands[] array
     */
    public void lRemoveCommand(Command cmd, int i) {
        super.lRemoveCommand(cmd, i);

        // default to PLAIN appearance if there are no commands left
        if (strItem.numCommands < 1) {
            appearanceMode = Item.PLAIN;
            lRequestInvalidate(true, true);
        }

        // checkTraverse();  right now traversability is not command dependent
        // lRequestInvalidate(true, true);
    }

    /**
     * Notifies L&amps;F of a string change in the corresponding StringItem.
     * @param str - the new string set in the StringItem
     */
    public void lSetText(String str) {
        checkTraverse();
        lRequestInvalidate(true, true);
    }

    /**
     * Notifies L&amps;F of a font change in the corresponding StringItem.
     * @param font - the new font set in the StringItem
     */
    public void lSetFont(Font font) {
        lRequestInvalidate(true, true);
    }

    /**
     * Gets default font to render text in StringItem if it was not
     * set by the application.
     * @return - the font to render text if it was not set by the app
     */
    public Font getDefaultFont() {
        return getTextFont(appearanceMode);
    }

    // *****************************************************
    //  Package private methods
    // *****************************************************
    /**
     * Returns the font to render text in StringItem.
     * @param appearance The appearance mode of the StringItem
     * @return the font to render text in StringItem.
     */
    static Font getTextFont(int appearance) {
        switch (appearance) {
        case Item.PLAIN:
            return StringItemSkin.FONT;
        case Item.HYPERLINK:
            return StringItemSkin.FONT_LINK;
        default: // BUTTON
            return StringItemSkin.FONT_BUTTON;
        }
    }

    /**
     * Returns the foreground color to render text in StringItem.
     * @param appearance The appearance mode of the StringItem
     * @return the foreground color per appearance mode
     */
    static int getForeground(int appearance) {
        switch (appearance) {
        case Item.PLAIN:
            return ScreenSkin.COLOR_FG;
        case Item.HYPERLINK:
            return StringItemSkin.COLOR_FG_LINK;
        default: // BUTTON
            return StringItemSkin.COLOR_FG_BUTTON;
        }
    }

    /**
     * Returns the foreground color to render text in StringItem.
     * @param appearance The appearance mode of the StringItem
     * @return the foreground color per appearance mode
     */
    static int getForegroundHilight(int appearance) {
        switch (appearance) {
        case Item.HYPERLINK:
            return StringItemSkin.COLOR_FG_LINK_FOCUS;
        case Item.PLAIN:
        default: // BUTTON
            return ScreenSkin.COLOR_FG;
        }
    }

    /**
     * Sets the content size in the passed in array.
     * Content is calculated based on the availableWidth.
     * size[WIDTH] and size[HEIGHT] should be set by this method.
     * @param size The array that holds Item content size and location 
     *             in Item internal bounds coordinate system.
     * @param w The width available for this Item
     */
    void lGetContentSize(int size[], int w) {

        if (appearanceMode == Item.HYPERLINK) {
            Text.getSizeForWidth(size, w,
                             strItem.str, strItem.font, 0);
        } else {
            Text.getSizeForWidth(size, w - (2 * StringItemSkin.PAD_BUTTON_H),
                             strItem.str, strItem.font, 0);
            size[WIDTH] = size[WIDTH] + (2 * StringItemSkin.PAD_BUTTON_H);            
        }
        size[HEIGHT] = strItem.font.getHeight() +
            (2 * StringItemSkin.PAD_BUTTON_V);
    }

    /**
     * Determine if this Item should have a newline before it
     *
     * @return true if it should have a newline before
     */
    boolean equateNLB() {
        String label = strItem.label;
        String str   = strItem.str;

        // If label starts with a\n,
        // put this StringItem on a newline no matter what
        if (label != null && label.length() > 0) {
            if (label.charAt(0) == '\n') {
                return true;
            }
        } else if (str != null && str.length() > 0) {
            // If there is no label and our content starts with a \n,
            // this StringItem starts on a newline
            if (str.charAt(0) == '\n') {
                return true;
            }
        } else {
            // empty StringItem
            return false;
        }
        
        if ((strItem.layout & Item.LAYOUT_2) == Item.LAYOUT_2) {
            return ((strItem.layout & Item.LAYOUT_NEWLINE_BEFORE)
                    == Item.LAYOUT_NEWLINE_BEFORE);
        }
        
        // in MIDP1.0 new any StringItem with a non-null label would
        // go on a new line
        return label != null && label.length() > 0;

        // IMPL NOTE: if there is no label in MIDP1.0
        // StringItem could go on the same line only with 
        // StringItems and ImageItems
    }
    
    /**
     * Determine if this Item should have a newline after it
     *
     * @return true if it should have a newline after
     */
    boolean equateNLA() {
        
        String label = item.label;
        String str = strItem.str;

        // If content ends with a \n,
        // there is a newline after this StringItem no matter what
        if (str != null && str.length() > 0) {
            if (str.charAt(str.length() - 1) == '\n') {
                return true;
            }
        } else if (label != null && label.length() > 0) {
            // If there is no content and our label ends with a \n, 
            // there is a newline after this StringItem
            if (label.charAt(label.length() - 1) == '\n') {
                return true;
            }
        } else {
            // empty StringItem
            return false;
        }
        
        if ((strItem.layout & Item.LAYOUT_2) == Item.LAYOUT_2) {
            return ((item.layout & Item.LAYOUT_NEWLINE_AFTER)
                    == Item.LAYOUT_NEWLINE_AFTER);
        }
        return false;
    }
    
    /**
     * Paint this StringItem
     *
     * @param g the Graphics object to paint to
     * @param width the width of this item
     * @param height the height of this item
     */
    void lCallPaint(Graphics g, int width, int height) {
        // In BUTTON mode internal layout and painting is done through
        // ItemLFImpl
        if (appearanceMode == Item.BUTTON || appearanceMode == Item.HYPERLINK) {
            super.lCallPaint(g, width, height);
            return;
        }

        // **************** Hyperlink and Plain *********************

        lGetLabelSize(labelBounds, width);

        int xOffset = 0;
        int yOffset = 0;

        if (labelBounds[HEIGHT] > 0) {
            Font lFont = ScreenSkin.FONT_LABEL;
            xOffset = Text.paint(g, strItem.label, lFont,
                                 ScreenSkin.COLOR_FG, 0,
                                 width, labelBounds[HEIGHT],
                                 0, Text.NORMAL, null);

            if (xOffset > 0) {
                xOffset += getHorizontalPad();
            }
            yOffset = labelBounds[HEIGHT] - lFont.getHeight();
            g.translate(0, yOffset);
        }
        int mode = Text.NORMAL;
        if ((lGetLockedHeight() != -1) || (lGetLockedWidth() != -1)) {
            mode |= Text.TRUNCATE;	
        } 

        Text.paint(g, strItem.str, strItem.font,
                   getForeground(appearanceMode),
                   getForegroundHilight(appearanceMode),
                   width, height - yOffset, xOffset, mode, null);

        g.translate(0, -yOffset);
           
    }

    /**
     * Paints the content area of this StringItem.
     * Graphics is translated to contents origin.
     * @param g The graphics where StringItem content should be painted
     * @param width The width available for the Item's content
     * @param height The height available for the Item's content
     */
    void lPaintContent(Graphics g, int width, int height) {

        // ********************* BUTTON and HYPERLINK Appearance ******************
        // Graphics is translated to content's top left corner
        switch (appearanceMode) {
            case Item.HYPERLINK: {
                int mode = Text.HYPERLINK;
                if (hasFocus) {
                    mode |= Text.INVERT;
                }

                Text.paint(g, strItem.str, strItem.font,
                        getForeground(appearanceMode),
                        getForegroundHilight(appearanceMode),
                        contentBounds[WIDTH], contentBounds[HEIGHT], 0, mode, null);
            }
            break;
            case Item.BUTTON: {
                if (StringItemSkin.IMAGE_BUTTON == null) {
                    CGraphicsUtil.draw2ColorBorder(g, 0, 0,
                            contentBounds[WIDTH],
                            contentBounds[HEIGHT],
                            hasFocus,
                            StringItemSkin.COLOR_BORDER_DK,
                            StringItemSkin.COLOR_BORDER_LT,
                            StringItemSkin.BUTTON_BORDER_W);
                } else {
                    CGraphicsUtil.draw9pcsBackground(g, 0, 1,
                            contentBounds[WIDTH],
                            contentBounds[HEIGHT],
                            StringItemSkin.IMAGE_BUTTON);
                }

                g.translate(StringItemSkin.PAD_BUTTON_H,
                        StringItemSkin.PAD_BUTTON_V);
                Text.paint(g, strItem.str, strItem.font,
                        getForeground(appearanceMode),
                        getForegroundHilight(appearanceMode),
                        contentBounds[WIDTH] - (2 * StringItemSkin.PAD_BUTTON_H),
                        contentBounds[HEIGHT] - (2 * StringItemSkin.PAD_BUTTON_V),
                        0, Text.TRUNCATE, null);
                g.translate(-StringItemSkin.PAD_BUTTON_H,
                        -StringItemSkin.PAD_BUTTON_V);

            }
        }
    }

    /**
     * called by the system to signal a key press
     *
     * @param keyCode the key code of the key that has been pressed
     * @see #getInteractionModes
     */
    void uCallKeyPressed(int keyCode) {
        ItemCommandListener cl;
        Command defaultCmd;
        
        if (keyCode != Constants.KEYCODE_SELECT) {
            return;
        }
        
        synchronized (Display.LCDUILock) {
            // StringItem takes focus only 
            // if there are one or more Item Commands
            // attached to it
            if (!(strItem.numCommands > 0) || strItem.commandListener == null) {
                return;
            }
        
            cl         = strItem.commandListener;
            defaultCmd = strItem.defaultCommand;
        } // synchronized
        
        // SYNC NOTE: The call to the listener must occur outside
        // of the lock. 'strItem' does not change. So we can use it without
        // LCDUILock.
        synchronized (Display.calloutLock) {
            try {
                if (defaultCmd != null) {
                    cl.commandAction(defaultCmd, strItem);
                } else {
                    // IMPL NOTE: Needs HI decision
                    // either call the first command
                    // from  the command list or
                    // invoke the menu
                }
            } catch (Throwable thr) {
                Display.handleThrowable(thr);
            }
        } // synchronized(calloutLock)
    }
    
    // *****************************************************
    //  Private methods
    // *****************************************************
    
    /**
     * Check that given the label, text, and commands, Form
     * should traverse this StringItem. Updates the internal
     * 'skipTraverse' variable.
     */
    private void checkTraverse() {
        String label = strItem.label;
        String str   = strItem.str;

        if (str == null && label == null) {
            skipTraverse = true;
        } else if (str == null && label.trim().equals("")) {
            skipTraverse = true;
        } else if (label == null && str.trim().equals("")) {
            skipTraverse = true;
        } else {
            skipTraverse = false;
        }
    }

    /** StringItem associated with this view */
    private StringItem strItem;

    /**
     * An internal flag. True if Form should not traverse
     * to this StringItem
     */
    private boolean skipTraverse;


    /** 
     * The actual appearance used for this StringItem.
     * It can be different than the hint set in strItem.
     * Actual appearance is always PLAIN if there are no
     * commands added. And is never PLAIN if there is at least
     * one command added 
     */
    private int appearanceMode;
}
