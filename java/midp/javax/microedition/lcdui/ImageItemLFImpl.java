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
import com.sun.midp.configurator.Constants;
import com.sun.midp.chameleon.CGraphicsUtil;
import com.sun.midp.chameleon.skins.ImageItemSkin;
import com.sun.midp.chameleon.skins.resources.ImageItemResources;

/**
* This is the Look &amps; Feel implementation for ImageItem.
*/
class ImageItemLFImpl extends ItemLFImpl implements ImageItemLF {

    /**
     * Creates look&amps;feel for an ImageItem
     * @param imageItem the ImageItem associated with this look&amps;feel
     */
    ImageItemLFImpl(ImageItem imageItem) {
        super(imageItem);
        
        this.imgItem = imageItem;

        ImageItemResources.load();
        
        // when no commands are added actual appearance
        // is PLAIN; actual appearance will be the same
        // as appearance set in ImageItem if a command is added
        // this ImageItem
        appearanceMode = Item.PLAIN;
    }

    // *****************************************************
    //  Public methods (ImageItemLF impl)
    // *****************************************************

    /**
     * Notifies L&F of an image change in the corresponding ImageItem.
     * @param img - the new image set in the ImageItem
     */
    public void lSetImage(Image img) {
        lRequestInvalidate(true, true);
    }

    /**
     * Notifies L&F of an alternative text change 
     * in the corresponding ImageItem.
     * @param altText - the new alternative text set in the ImageItem
     */
    public void lSetAltText(String altText) {
    }

    /**
     * Notifies L&F of a command addition in the corresponding ImageItem.
     * @param cmd the newly added command
     * @param i the index of the added command in the ImageItem's
     *        commands[] array
     */
    public void lAddCommand(Command cmd, int i) {
        super.lAddCommand(cmd, i);

        // restore the value of the original appearanceMode
        if ((imgItem.numCommands >= 1) && (appearanceMode == Item.PLAIN)) {
            appearanceMode = imgItem.appearanceMode == Item.BUTTON ?
                             Item.BUTTON : Item.HYPERLINK;
            lRequestInvalidate(true, true);
        }
    }

    /**
     * Notifies L&F of a command removal in the corresponding ImageItem.
     * @param cmd the newly removed command
     * @param i the index of the removed command in the ImageItem's
     *        commands[] array
     */
    public void lRemoveCommand(Command cmd, int i) {
        super.lRemoveCommand(cmd, i);

        // default to Plain if there are not commands
        if (imgItem.numCommands < 1) {
            appearanceMode = Item.PLAIN;
            lRequestInvalidate(true, true);            
        }
    }

    // *****************************************************
    //  Package private methods
    // *****************************************************

    /**
     * Returns vertical padding per ImageItem's appearance mode.
     * That is the distance between the top edge and 
     * the top part of the text. 
     * @param appearance The appearance mode of StringItem
     * @return the vertical padding used in ImageItem per its appearance mode
     */
    static int getVerticalPad(int appearance) {
        switch (appearance) {
        case Item.PLAIN:
            return 0;
        case Item.HYPERLINK:
            return ImageItemSkin.PAD_LINK_V;
        default:  // Item.BUTTON
            return ImageItemSkin.PAD_BUTTON_V;
        }
    }

    /**
     * Returns horizontal padding per ImageItem's appearance mode.
     * That is the distance between the left edge and the left part 
     * of the text. 
     * @param appearance The appearance mode of StringItem
     * @return the horizontal padding used in ImageItem per its appearance mode
     */
    static int getHorizontalPad(int appearance) {
        switch (appearance) {
        case Item.PLAIN:
            return 0;
        case Item.HYPERLINK:
            return ImageItemSkin.PAD_LINK_H;
        default:  // Item.BUTTON
            return ImageItemSkin.PAD_BUTTON_H;
        }
    }

    /**
     * Determine if this Item should have a newline before it
     *
     * @return true if it should have a newline before
     */
    boolean equateNLB() {
        // LAYOUT_NEWLINE_BEFORE is set in the layout
        // ImageItem had layout directives in MIDP1.0
        // so if LAYOUT_NEWLINE_BEFORE is set we do not need
        // to check LAYOUT_2 (as in StringItem)
        if (super.equateNLB()) {
            return true;
        }

        // LAYOUT_NEWLINE_BEFORE is not set but LAYOUT_2 is set
        // which means that items could be positioned side by side
        // unless LAYOUT_NEWLINE_BEFORE is set
        if ((imgItem.layout & Item.LAYOUT_2) == Item.LAYOUT_2) {
            return false;
        }
        
        // in MIDP1.0  any ImageItem with a non-null label would
        // go on a new line
        return imgItem.label != null && imgItem.label.length() > 0;

        // IMPL NOTE: if there is no label in MIDP1.0
        // ImageItem could go on the same line only with 
        // StringItems and ImageItems
    }


    /**
     * Called by the system to signal a key press
     *
     * @param keyCode the key code of the key that has been pressed
     * @see #getInteractionModes
     */
    void uCallKeyPressed(int keyCode) {
        if (keyCode != Constants.KEYCODE_SELECT) {
            return;
        }

        ItemCommandListener cl;
        Command defaultCmd;

        synchronized (Display.LCDUILock) {
            if (imgItem.numCommands == 0 || 
                imgItem.commandListener == null) {
                return;
            }

            cl = imgItem.commandListener;
            defaultCmd = imgItem.defaultCommand;
        } // synchronized

        // SYNC NOTE: The call to the listener must occur outside
        // of the lock
        if (cl != null) {
            try {
                // SYNC NOTE: We lock on uCalloutLock around any calls
                // into application code
                synchronized (Display.calloutLock) {
                    if (defaultCmd != null) {
                        cl.commandAction(defaultCmd, imgItem);
                    } else {
                        // IMPL NOTE: Needs HI decision
                        // either call the first command
                        // from  the command list or
                        // invoke the menu
                    }
                }
            } catch (Throwable thr) {
                Display.handleThrowable(thr);
            }
        }
    }

    /**
     * Sets the content size in the passed in array.
     * Content is calculated based on the availableWidth.
     * size[WIDTH] and size[HEIGHT] should be set by this method.
     * @param size The array that holds Item content size and location 
     *             in Item internal bounds coordinate system.
     * @param availableWidth The width available for this Item
     */
    void lGetContentSize(int size[], int availableWidth) {
        Image img = imgItem.immutableImg;
        if (img == null) {
            size[WIDTH] = size[HEIGHT] = 0;
        } else {
            size[WIDTH]  = img.getWidth() + 
                           2 * getHorizontalPad(appearanceMode);
            size[HEIGHT] = img.getHeight() +
                           2 * getVerticalPad(appearanceMode);
        }
    }


    /**
     * Paint this ImageItem
     *
     * @param g the Graphics context to paint to
     * @param width the width of the content area
     * @param height the height of the content area
     */
    void lPaintContent(Graphics g, int width, int height) {

        if (appearanceMode == Item.HYPERLINK) {

            // we re-draw the HYPERLINK border image 
            // offset to the right (bottom) as many times as necessary
            // to fill the width (height) of the Image in the ImageItem.
            // with the H_HL_BORDER_PAD from the edge 
            CGraphicsUtil.drawTop_BottomBorder(g, 
                ImageItemSkin.IMAGE_LINK_H,
                ImageItemSkin.IMAGE_LINK_H,
                ImageItemSkin.PAD_LINK_H, 
                contentBounds[WIDTH] - ImageItemSkin.PAD_LINK_H,
                ImageItemSkin.PAD_LINK_H, 
                height - ImageItemSkin.PAD_LINK_H - 
                ImageItemSkin.IMAGE_LINK_H.getHeight());
                
            CGraphicsUtil.drawLeft_RightBorder(g, 
                ImageItemSkin.IMAGE_LINK_V,
                ImageItemSkin.IMAGE_LINK_V,
                ImageItemSkin.PAD_LINK_H + 
                    ImageItemSkin.IMAGE_LINK_V.getHeight(),
                contentBounds[HEIGHT] - ImageItemSkin.PAD_LINK_H -
                    ImageItemSkin.IMAGE_LINK_H.getHeight(),
                ImageItemSkin.PAD_LINK_H, 
                width - ImageItemSkin.PAD_LINK_H -
                    ImageItemSkin.IMAGE_LINK_V.getWidth());
                    
        } else if (appearanceMode == Item.BUTTON) {

            if (ImageItemSkin.IMAGE_BUTTON == null) {
                CGraphicsUtil.draw2ColorBorder(g, 0, 0, width, height, 
                                               hasFocus, 
                                               ImageItemSkin.COLOR_BORDER_DK, 
                                               ImageItemSkin.COLOR_BORDER_LT,
                                               ImageItemSkin.BUTTON_BORDER_W);
            } else {
                CGraphicsUtil.draw9pcsBackground(g, 0, 0,
                                         width, height,
                                         ImageItemSkin.IMAGE_BUTTON);
            }
        }

        Image img = imgItem.immutableImg;
        // Determine whether 'img' is not 'null' otherwise
        // 'drawImage' method throws 'NullPointerException'
        if (img != null) {
            g.drawImage(img,
                        getHorizontalPad(appearanceMode), 
                        getVerticalPad(appearanceMode), 
                        Graphics.TOP | Graphics.LEFT);
        }
    }

    // *****************************************************
    //  Private methods
    // *****************************************************

    /** ImageItem associated with this view */
    private ImageItem imgItem;

    /**
     * The appearance hint
     */
    private int appearanceMode;
    
} // ImageItemView
