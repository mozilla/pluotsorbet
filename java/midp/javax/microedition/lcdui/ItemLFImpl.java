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

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;
import com.sun.midp.configurator.Constants;
import com.sun.midp.chameleon.skins.ScreenSkin;

/**
* This is the look &amps; feel implementation for Item.
*/
abstract class ItemLFImpl implements ItemLF {

    /**
     * Creates look and feel for the passed in item
     * @param item the Item for which the Look and feel should be created
     */
    ItemLFImpl(Item item) {
        this.item = item;
        actualBoundsInvalid = new boolean[] {true, true, true, true};
        bounds = new int[] {0, 0, 0, 0};
        target = new int[4];

        labelBounds = new int[] {0, 0, 0, 0};
        contentBounds = new int[] {0, 0, 0, 0};
    }


    // *****************************************************
    //  Public methods - ItemLF interface implementation
    // *****************************************************
        
    /**
     * Get the minimum width of this Item
     *
     * @return the minimum width
     */
    public int lGetMinimumWidth() {
        // IMPL_NOTE minimum width should be less than preferred
        return lGetPreferredWidth(-1);
    }
    
    /**
     * Get the preferred width of this Item
     *
     * @param h the tentative content height in pixels, or -1 if a
     * tentative height has not been computed
     * @return the preferred width
     */
    public int lGetPreferredWidth(int h) {

        int availableWidth = lGetAvailableWidth();
        if (cachedWidth == INVALID_SIZE || cachedWidth != availableWidth) {
            lGetLabelSize(labelBounds, availableWidth);
            lGetContentSize(contentBounds, availableWidth);
            cachedWidth = availableWidth;
        }

        // no content
        if (contentBounds[HEIGHT] == 0) {
            return labelBounds[WIDTH];
        }
        
        // no label
        if (labelBounds[HEIGHT] == 0) {
            return contentBounds[WIDTH];
        }

        if (labelAndContentOnSameLine(labelBounds[HEIGHT]) &&
            (labelBounds[WIDTH] + getHorizontalPad() + 
             contentBounds[WIDTH] <= availableWidth)) {
            return labelBounds[WIDTH] + getHorizontalPad() + 
                   contentBounds[WIDTH];
        }

        return (labelBounds[WIDTH] > contentBounds[WIDTH] ? 
                labelBounds[WIDTH] : contentBounds[WIDTH]);
    }

    /**
     * Get the minimum height of this Item
     *
     * @return the minimum height
     */
    public int lGetMinimumHeight() {
        // minimum height will be reached if we give item the most width
        return lGetPreferredHeight(-1);
    }

    /**
     * Get the preferred height of this Item
     *
     * @param w the tentative content width in pixels, or -1 if a
     * tentative width has not been computed
     * @return the preferred height
     */
    public int lGetPreferredHeight(int w) {

        if (w == -1) {
            w = lGetAvailableWidth();
        }

        if (cachedWidth == INVALID_SIZE || cachedWidth != w) {
            lGetLabelSize(labelBounds, w);
            lGetContentSize(contentBounds, w);
            cachedWidth = w;
        }

        // no content
        if (contentBounds[HEIGHT] == 0) {
            return labelBounds[HEIGHT];
        }
        
        // no label
        if (labelBounds[HEIGHT] == 0) {
            return contentBounds[HEIGHT];
        }

        if (labelAndContentOnSameLine(labelBounds[HEIGHT]) &&
            (labelBounds[WIDTH] + getHorizontalPad() + 
             contentBounds[WIDTH] <= w)) {
            return labelBounds[HEIGHT] < contentBounds[HEIGHT] ?
                   contentBounds[HEIGHT] : labelBounds[HEIGHT];
        }

        return labelBounds[HEIGHT] + getVerticalPad() +
               contentBounds[HEIGHT];
    }

    /**
     * Returns if the pointer location (x, y, w.r.t. the Form origin)
     * is within the bounds of the 'clickable' area of this
     * ItemLFImpl. We exclude non-interactive areas such as the
     * label. <p>
     * 
     * Most items can use this method. The only case that needs
     * overriding is the ChoiceGroupPopupLFImpl.  
     */
    boolean itemContainsPointer(int x, int y) {
        int contentX = bounds[X] + contentBounds[X] + ScreenSkin.PAD_FORM_ITEMS - 2;
        int contentY = bounds[Y] + contentBounds[Y] + ScreenSkin.PAD_FORM_ITEMS - 2;

        int myX = x - contentX;
        int myY = y - contentY;

        return (myX >= 0 && myX <= contentBounds[WIDTH] + ScreenSkin.PAD_FORM_ITEMS - 2 &&
                myY >= 0 && myY <= contentBounds[HEIGHT] + ScreenSkin.PAD_FORM_ITEMS - 2);
    }


    /**
     * Returns if the pointer location (x, y, w.r.t. the Form origin)
     * is within the bounds of the 'clickable' area of this
     * ItemLFImpl. We exclude non-interactive areas such as the
     * label. <p>
     *
     * Most items can use this method. The only case that needs
     * overriding is the ChoiceGroupPopupLFImpl.
     */
    int itemAcceptPointer(int x, int y) {

        int contentX = bounds[X] + contentBounds[X] + ScreenSkin.PAD_FORM_ITEMS - 2;
        int contentY = bounds[Y] + contentBounds[Y] + ScreenSkin.PAD_FORM_ITEMS - 2;

        int myX = x - contentX;
        int myY = y - contentY;

        if ((myX >= -ScreenSkin.TOUCH_RADIUS) &&
            (myX <= contentBounds[WIDTH] + ScreenSkin.PAD_FORM_ITEMS - 2 + ScreenSkin.TOUCH_RADIUS)) {
            if ((myY < -ScreenSkin.TOUCH_RADIUS
                    || myY > contentBounds[HEIGHT] + ScreenSkin.PAD_FORM_ITEMS - 2 + ScreenSkin.TOUCH_RADIUS)) {
                return -1;
            } else {
                if (myY >= 0
                    && myY <= contentBounds[HEIGHT] + ScreenSkin.PAD_FORM_ITEMS - 2 ) {
                    return 0;
                } else {
                    return Math.max(Math.abs(myY),Math.abs(myY - contentBounds[HEIGHT] + ScreenSkin.PAD_FORM_ITEMS - 2));
                }
            }
        }

        return -1;
    }

    ItemLFImpl findNearestItem(ItemLFImpl secondItem, int x) {
        int x1 = bounds[X] + contentBounds[WIDTH];
        int x2 = secondItem.bounds[X];
        if ((x - x1) <= (x2 - x)) {
            return this;
        } else {
            return secondItem ;
        }

    }

    
    /**
     * Notifies L&F of a label change in the corresponding Item.
     * @param label the new label string
     */
    public void lSetLabel(String label) {
        int[] oldBounds = new int[]{labelBounds[X],labelBounds[Y],labelBounds[WIDTH],labelBounds[HEIGHT]};
        lGetLabelSize(labelBounds, lGetAvailableWidth());
        if (labelBounds[X] == oldBounds[X] && labelBounds[Y] == oldBounds[Y] &&
                labelBounds[WIDTH] == oldBounds[WIDTH] && labelBounds[HEIGHT] == oldBounds[HEIGHT]) {
            lRequestPaint(labelBounds[X],labelBounds[Y],labelBounds[WIDTH],labelBounds[HEIGHT]);
        } else {
            lRequestInvalidate(true,true);
        }
    }

    /**
     * Notifies L&F of a layout change in the corresponding Item.
     * @param layout the new layout descriptor
     */
    public void lSetLayout(int layout) {
        lRequestInvalidate(true, true);
    }

    /**
     * Notifies L&F of a command addition in the corresponding Item.
     * @param cmd the newly added command
     * @param i the index of the added command in the Item's
     *        commands[] array
     */
    public void lAddCommand(Command cmd, int i) {
        if (item.owner != null) {
            ((DisplayableLFImpl)item.owner.getLF()).updateCommandSet();
        }
    }

    /**
     * Notifies L&F of a command removal in the corresponding Item.
     * @param cmd the newly removed command
     * @param i the index of the removed command in the Item's
     *        commands[] array
     */
    public void lRemoveCommand(Command cmd, int i) {
        if (item.owner != null) {
            ((DisplayableLFImpl)item.owner.getLF()).updateCommandSet();
        }
    }

    /**
     * Notifies L&F of a preferred size change in the corresponding Item.
     * @param width the value to which the width is locked, or
     * <code>-1</code> if it is unlocked
     * @param height the value to which the height is locked, or 
     * <code>-1</code> if it is unlocked
     */
    public void lSetPreferredSize(int width, int height) {

        // the "preferred size" is in "inner bounds" (contents) terms
        if (width == getInnerBounds(WIDTH) &&
            height == getInnerBounds(HEIGHT)) { 
            /* no need to invalidate */
            return;
        }
        lRequestInvalidate(width != getInnerBounds(WIDTH),
                           height != getInnerBounds(HEIGHT));   
    }

    /**
     * Notifies L&F of the default command change in the corresponding Item.
     * @param cmd the newly set default command
     * @param i index of this new command in the ChoiceGroup's commands array
     */
    public void lSetDefaultCommand(Command cmd, int i) {}

    /**
     * Notify this itemLF that its owner screen has changed.
     * Clear internal state if its new owner is null.
     * 
     * @param oldOwner old owner screen before this change. New owner 
     *                 can be found in Item model.
     */

    public void lSetOwner(Screen oldOwner) {
        if (item.owner == null) {
            // Hide it
            if (visible) {
                // IMPL_NOTE: We are holding LCDUILock and this 
                //         will call into app code on CustomItem.
                //         Need to schedule an event to do that.
                uCallHideNotify();
            }

            // the deleted item may be added later, so we
            // have to invalidate it.
            actualBoundsInvalid[X] = true;
            actualBoundsInvalid[Y] = true;
            actualBoundsInvalid[WIDTH] = true;
            actualBoundsInvalid[HEIGHT] = true;
            hasFocus = false;
        }
    }

    /**
     * Called by the system to indicate the content has been scrolled
     * inside of the form
     *
     * @param w the new width of the viewport of the screen
     * @param h the new height of the viewport of the screen
     */
    public void uCallScrollChanged(int newViewportX, int newViewportY) {
        // do nothing by default. 
    }

    /**
     * Return whether the cached requested sizes are valid.
     *
     * @return <code>true</code> if the cached requested sizes are up to date.
     *         <code>false</code> if they have been invalidated.
     */
    public final boolean isRequestedSizesValid() {
        return (cachedWidth != INVALID_SIZE);
    }
 
    // *****************************************************
    //  Package private methods
    // *****************************************************
    /**
     * Used by the Form Layout to set the size of this Item
     *
     * @param height the tentative content height in pixels
     * @return the preferred width 
     */
    int lGetAdornedPreferredWidth(int height) {
        if (height > 2 * ScreenSkin.PAD_FORM_ITEMS) {
            height -= 2 * ScreenSkin.PAD_FORM_ITEMS;
        } else {
            height = -1;
        }

        return lGetPreferredWidth(height) + 2 * ScreenSkin.PAD_FORM_ITEMS;

    }
    
    /**
     * Used by the Form Layout to set the size of this Item
     *
     * @param width the tentative content width in pixels
     * @return the preferred height
     */
    int lGetAdornedPreferredHeight(int width) {
        if (width > 2 * ScreenSkin.PAD_FORM_ITEMS) {
            width -= 2 * ScreenSkin.PAD_FORM_ITEMS;
        } else {
            width = -1;
        }

        return lGetPreferredHeight(width) + 2 * ScreenSkin.PAD_FORM_ITEMS;
    }

    /**
     * Used by the Form Layout to set the size of this Item
     * @return the minimum width that includes cell spacing
     */
    int lGetAdornedMinimumWidth() {

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "  [I] lGetAdornedMinimumWidth() " + this);
        }
        return lGetMinimumWidth() + 2 * ScreenSkin.PAD_FORM_ITEMS;
    }

    /**
     * Used by the Form Layout to set the size of this Item
     * @return the minimum height that includes cell spacing
     */
    int lGetAdornedMinimumHeight() {

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "  [I] lGetAdornedMinimumHeight() " + this);
        }

        return lGetMinimumHeight() + 2 * ScreenSkin.PAD_FORM_ITEMS;
    }

    /**
     * Returns the width available for layout by default.
     * @return The width available for the item by default
     */
    int lGetAvailableWidth() {
        int w = (item.owner != null) ?
            ((DisplayableLFImpl)item.owner.getLF()).lGetWidth() :
            Display.WIDTH - 2 * ScreenSkin.PAD_FORM_ITEMS;
        return w;
    }

    /**
     * Sets item's size
     * 
     * @param w - the new width of the item
     * @param h - the new height of the item
     */
    void lSetSize(int w, int h) {
        bounds[WIDTH] = w;
        bounds[HEIGHT] = h;
    }

    /**
     * Sets item's location.
     *
     * @param x - the new x location in form's content coordinate system
     * @param y - the new y location in form's content coordinate system
     */
    void lSetLocation(int x, int y) {
        bounds[X] = x;
        bounds[Y] = y;
    }

    /**
     * Moves item's location by delta. Both deltaX and deltaY can be
     * positive and negative.
     *
     * @param deltaX - the amount of pixels by which x item's location
     *                 has to be moved
     * @param deltaY - the amount of pixels by which y item's location
     *                 has to be moved
     */
    void lMove(int deltaX, int deltaY) {
        bounds[X] += deltaX;
        bounds[Y] += deltaY;
    }

    /**
     * Paint the content of this Item while LCDUILock is unlocked.
     * This function simply obtains LCDUILock and calls lCallPaint.
     *
     * @param g the Graphics object to be used for rendering the item
     * @param w current width of the item in pixels
     * @param h current height of the item in pixels
     */
    void uCallPaint(Graphics g, int w, int h) {
        synchronized (Display.LCDUILock) {
            lCallPaint(g, w, h);
        }
    }

    /**
     * Paint the content of this Item while LCDUILock is locked.
     *
     * @param g the Graphics object to be used for rendering the item
     * @param w current width of the item in pixels
     * @param h current height of the item in pixels
     */
    void lCallPaint(Graphics g, int w, int h) {

        lDoInternalLayout(labelBounds, contentBounds, w, h);

        g.translate(labelBounds[X], labelBounds[Y]);

        paintLabel(g, labelBounds[WIDTH]);

        g.translate(-labelBounds[X] + contentBounds[X],
                    -labelBounds[Y] + contentBounds[Y]);

        lPaintContent(g, contentBounds[WIDTH], contentBounds[HEIGHT]);

        g.translate(-contentBounds[X], -contentBounds[Y]);
    }

    /**
     * Paints the content area of the Item. This method should be
     * overridden by the subclasses. Graphics will be translated to
     * contents origin.
     * @param g The graphics where Item content should be painted
     * @param w The width available for the Item's content
     * @param h The height available for the Item's content
     */
    void lPaintContent(Graphics g, int w, int h) {
    }

    /**
     * Does internal Item layout which includes setting of location
     * and size of label and content. 
     * @param labelBounds The array that will hold label location and size
     * @param contentBounds The array that will hold content location and size
     * @param w The width given to the Item
     * @param h The height given to the Item
     */        
    void lDoInternalLayout(int labelBounds[], int contentBounds[], 
                           int w, int h) {
        if (cachedWidth == INVALID_SIZE || cachedWidth != w) {
            if (actualBoundsInvalid[WIDTH] || actualBoundsInvalid[HEIGHT]) {
                // Note: It is possible that lDoInternalLayout is called while
                // invalidate is being processed. In  that case we have to make
                // sure that internal layout happens once paint is called after
                // invalidate with correct new bounds.
                cachedWidth = INVALID_SIZE;
                layoutDone = false;
                return;
            }

            lGetLabelSize(labelBounds, w);
            lGetContentSize(contentBounds, w);
            cachedWidth = w;
        }

        if (layoutDone) {
            return;
        }

        labelBounds[X] = labelBounds[Y] = 0;
        contentBounds[X] = contentBounds[Y] = 0;

        int itemWidth, itemHeight;

        // empty label
        if (labelBounds[HEIGHT] == 0) {

            // both label and content are empty
            if (contentBounds[HEIGHT] == 0) {
                layoutDone = true;
                return;
            }

            itemWidth = contentBounds[WIDTH];
            itemHeight = contentBounds[HEIGHT];

            // empty content
        } else if (contentBounds[HEIGHT] == 0) {
            itemWidth = labelBounds[WIDTH];
            itemHeight = labelBounds[HEIGHT];

        } else { // both labelBounds[HEIGHT] && contentBounds[HEIGHT] != 0

            itemWidth = contentBounds[WIDTH];
            itemHeight = contentBounds[HEIGHT];

            // label and content fit in the width available
            if (labelAndContentOnSameLine(labelBounds[HEIGHT]) &&
                (labelBounds[WIDTH] + getHorizontalPad() + 
                 contentBounds[WIDTH]) <= w) {
                if (contentBounds[HEIGHT] < labelBounds[HEIGHT]) {
                    contentBounds[Y] = 
                        (labelBounds[HEIGHT] - contentBounds[HEIGHT]) / 2;
                    itemHeight = labelBounds[HEIGHT];
                } else {
                    labelBounds[Y] = 
                        (contentBounds[HEIGHT] - labelBounds[HEIGHT]) / 2;
                    itemHeight = contentBounds[HEIGHT];
                }
                    if (ScreenSkin.TEXT_ORIENT == Graphics.RIGHT) {
                        labelBounds[X] =
                            contentBounds[WIDTH] + getHorizontalPad();
                        itemWidth = labelBounds[X] + labelBounds[WIDTH];
                    } else {
                        contentBounds[X] =
                        labelBounds[WIDTH] + getHorizontalPad();
                        itemWidth = contentBounds[X] + contentBounds[WIDTH];
                    }

            } else {
                // label and content do NOT fit in width available
                contentBounds[Y] =
                    labelBounds[HEIGHT] + getVerticalPad();
                itemHeight = contentBounds[Y] + contentBounds[HEIGHT];

                if (contentBounds[WIDTH] < labelBounds[WIDTH]) {
                    switch (item.layout & ImageItem.LAYOUT_CENTER) {
                    case Item.LAYOUT_CENTER:
                        contentBounds[X] = 
                            (labelBounds[WIDTH] - contentBounds[WIDTH]) / 2;
                        break;
                    case Item.LAYOUT_RIGHT:
                        contentBounds[X] = 
                            labelBounds[WIDTH] - contentBounds[WIDTH];
                        break;
                    case Item.LAYOUT_LEFT:
                        break;
                    default: // Item.LAYOUT_LEFT
                        if (ScreenSkin.TEXT_ORIENT == Graphics.RIGHT) {
                           contentBounds[X] =
                            labelBounds[WIDTH] - contentBounds[WIDTH];
                        }
                        break;
                    }
                    itemWidth = labelBounds[WIDTH];
                } else {
                    // IMPL_NOTE check with ue what should happen if content is
                    // wider than label
                    switch (item.layout & ImageItem.LAYOUT_CENTER) {
                    case Item.LAYOUT_CENTER:
                        labelBounds[X] = 
                            (contentBounds[WIDTH] - labelBounds[WIDTH]) / 2;
                        break;
                    case Item.LAYOUT_RIGHT:
                        // we do not right justify the label
                        if (ScreenSkin.TEXT_ORIENT == Graphics.RIGHT) {
                             labelBounds[X] =
                                 contentBounds[WIDTH] - labelBounds[WIDTH];
                        }
                        break;
                    case Item.LAYOUT_LEFT:
                        break;
                    default: // Item.LAYOUT_LEFT
                        if (ScreenSkin.TEXT_ORIENT == Graphics.RIGHT) {
                             labelBounds[X] =
                                 contentBounds[WIDTH] - labelBounds[WIDTH];
                        }
                        break;
                    }
                    itemWidth = contentBounds[WIDTH];
                }
            }
        }

        // find overall ImageItem location inside space provided
        int x, y;
        switch (item.layout & ImageItem.LAYOUT_CENTER) {
        case Item.LAYOUT_CENTER:
            x = (w - itemWidth) / 2;
            break;
        case Item.LAYOUT_RIGHT:
            x = w - itemWidth;
            break;
        case Item.LAYOUT_LEFT:
            x = 0;
            break;
        default: // Item.LAYOUT_LEFT and Default
            if (ScreenSkin.TEXT_ORIENT == Graphics.RIGHT) {
                x = w - itemWidth;
            } else {
                x = 0;
            }
        }

        switch (item.layout & ImageItem.LAYOUT_VCENTER) {
        case Item.LAYOUT_VCENTER:
            y = (h - itemHeight) / 2;
            break;
        case Item.LAYOUT_BOTTOM:
            y = h - itemHeight;
            break;
        default: // Item.LAYOUT_TOP and Default
            y = 0;
        }

        if (labelBounds[HEIGHT] > 0) {
            labelBounds[X] += x;
            labelBounds[Y] += y;
        }

        if (contentBounds[HEIGHT] > 0) {
            contentBounds[X] += x;
            contentBounds[Y] += y;
        }

        layoutDone = true;
    }

    /**
     * Determine if this Item should horizontally shrink
     *
     * @return true if it should horizontally shrink
     */
    boolean shouldHShrink() {
        return ((item.layout & Item.LAYOUT_SHRINK) == Item.LAYOUT_SHRINK);
    }
    
    /**
     * Determine if this Item should horizontally expand
     *
     * @return true if it should horizontally expand
     */
    boolean shouldHExpand() {
        return ((item.layout & Item.LAYOUT_EXPAND) == Item.LAYOUT_EXPAND);
    }
    
    /**
     * Determine if this Item should vertically shrink
     *
     * @return true if it should vertically shrink
     */
    boolean shouldVShrink() {
        return ((item.layout & Item.LAYOUT_VSHRINK) == Item.LAYOUT_VSHRINK);
    }
    
    /**
     * Determine if this Item should vertically expand
     *
     * @return true if it should vertically expand
     */
    boolean shouldVExpand() {
        return ((item.layout & Item.LAYOUT_VEXPAND) == Item.LAYOUT_VEXPAND);
    }
    
    /**
     * Determine if this Item should have a newline after it
     *
     * @return true if it should have a newline after
     */
    boolean equateNLA() {
        return ((item.layout & Item.LAYOUT_NEWLINE_AFTER) == 
                Item.LAYOUT_NEWLINE_AFTER);
    }
    
    /**
     * Determine if this Item should have a newline before it
     *
     * @return true if it should have a newline before
     */
    boolean equateNLB() {
        return ((item.layout & Item.LAYOUT_NEWLINE_BEFORE) == 
                Item.LAYOUT_NEWLINE_BEFORE);
    }
    
    /**
     * Get the effective layout type of this Item
     *
     * @return layout The translated layout type.
     */
    int getLayout() {
        int l = item.layout;
        if (l == Item.LAYOUT_DEFAULT) {
            return Item.LAYOUT_BOTTOM;
        } else {
            return l;
        }
    }
    
    /**
     * Determine if this Item should not be traversed to. By default,
     * this method will return true only if the owner item has a
     * default command or a number of item commands associated with it.
     *
     * @return true if this Item should not be traversed to
     */
    boolean shouldSkipTraverse() {
        return (item.defaultCommand == null && item.numCommands == 0);        
    }

    /**
     * Called by the system
     * This function simply calls lCallTraverse() after obtaining LCDUILock.
     *
     * @param dir the direction of traversal
     * @param viewportWidth the width of the container's viewport
     * @param viewportHeight the height of the container's viewport
     * @param visRect_inout passes the visible rectangle into the method, and
     * returns the updated traversal rectangle from the method
     * @return true if internal traversal had occurred, false if traversal
     * should proceed out
     */
    boolean uCallTraverse(int dir, int viewportWidth, int viewportHeight,
                         int[] visRect_inout) {
        synchronized (Display.LCDUILock) {
            return lCallTraverse(dir, 
                                 viewportWidth, 
                                 viewportHeight,
                                 visRect_inout);
        }
    }

    /**
     * Called by the system
     *
     * <p>The default implementation of the traverse() method always returns
     * false.</p>
     *
     * @param dir the direction of traversal
     * @param viewportWidth the width of the container's viewport
     * @param viewportHeight the height of the container's viewport
     * @param visRect_inout passes the visible rectangle into the method, and
     * returns the updated traversal rectangle from the method
     * @return true if internal traversal had occurred, false if traversal
     * should proceed out
     *
     * @see #getInteractionModes
     * @see #traverseOut
     * @see #TRAVERSE_HORIZONTAL
     * @see #TRAVERSE_VERTICAL
     */
    boolean lCallTraverse(int dir, int viewportWidth, int viewportHeight,
                         int[] visRect_inout) {
        
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "  [I] lCallTraverse()");
        }

        hasFocus = true;
        return false;
    }

    /**
     *  If hilighted element of item is not completely visible should make it visible
     * @param viewport the viewport coordinates
     * @param visRect the in/out rectangle for the internal traversal location
     * @return true if visRect was changed
     */
    boolean lScrollToItem(int[] viewport, int[] visRect) {
        visRect[Y] = bounds[Y];
        visRect[HEIGHT] = bounds[HEIGHT];
        return true;
    }
        
    /**
     * Called by the system to indicate traversal has left this Item
     * This function simply calls lCallTraverseOut() after obtaining LCDUILock.
     */
    void uCallTraverseOut() {
        synchronized (Display.LCDUILock) {
            lCallTraverseOut();
        }
    }

    /**
     * Called by the system to indicate traversal has left this Item
     *
     * @see #getInteractionModes
     * @see #traverse
     * @see #TRAVERSE_HORIZONTAL
     * @see #TRAVERSE_VERTICAL
     */
    void lCallTraverseOut() {

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "  [I] uCallTraverseOut()");
        }

        hasFocus = false;
    }
    
    /**
     * Called by the system to notify this Item it is being shown
     * This function simply calls lCallShowNotify() after obtaining LCDUILock.
     */
    void uCallShowNotify() {
        synchronized (Display.LCDUILock) {
            lCallShowNotify();
        }
    }

    /**
     * Called by the system to notify this Item it is being shown
     *
     * <p>The default implementation of this method updates
     * the 'visible' state
     */
    void lCallShowNotify() {

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "  [I] uCallShowNotify()");
        }

        this.visible  = true;
    }
    
    /**
     * Called by the system to notify this Item it is being hidden.
     * This function simply calls lCallHideNotify() after obtaining LCDUILock.
     */
    void uCallHideNotify() {
        synchronized (Display.LCDUILock) {
            lCallHideNotify();
        }
    }

    /**
     * Called by the system to notify this Item it is being hidden.
     *
     * <p>The default implementation of this method updates
     * the 'visible' state
     */
    void lCallHideNotify() {
        this.visible = false;
    }

    
    /**
     * Called by the system to signal a key press
     *
     * @param keyCode the key code of the key that has been pressed
     * @see #getInteractionModes
     */
    void uCallKeyPressed(int keyCode) { }
    
    /**
     * Called by the system to signal a key release
     *
     * @param keyCode the key code of the key that has been released
     * @see #getInteractionModes
     */
    void uCallKeyReleased(int keyCode) { }
    
    /**
     * Called by the system to signal a key repeat
     *
     * @param keyCode the key code of the key that has been repeated
     * @see #getInteractionModes
     */
    void uCallKeyRepeated(int keyCode) { }
    
    /**
     * Called by the system to signal a pointer press
     *
     * @param x the x coordinate of the pointer down
     * @param y the y coordinate of the pointer down
     *
     * @see #getInteractionModes
     */
    void uCallPointerPressed(int x, int y) {
        itemWasPressed = true;
    }
    
    /**
     * Called by the system to signal a pointer release
     *
     * @param x the x coordinate of the pointer up
     * @param y the x coordinate of the pointer up
     *
     * @see #getInteractionModes
     */
    void uCallPointerReleased(int x, int y) {
        x -= contentBounds[X];
        y -= contentBounds[Y];
        if ( (x >= 0 && x <= contentBounds[WIDTH] && y >= 0 &&
              y <= contentBounds[HEIGHT]) &&
             (itemWasPressed && (hasFocus || item.owner.numCommands <= 1))) {
            //should check the x,y is in item's content area
            uCallKeyPressed(Constants.KEYCODE_SELECT);
        }
        itemWasPressed = false;
    }
    
    /**
     * Called by the system to signal a pointer drag
     *
     * @param x the x coordinate of the pointer drag
     * @param y the x coordinate of the pointer drag
     *
     * @see #getInteractionModes
     */
    void uCallPointerDragged(int x, int y) { }
    
    /**
     * Called by the system to indicate the size available to this Item
     * has changed
     *
     * @param w the new width of the item's content area
     * @param h the new height of the item's content area
     */
    void uCallSizeChanged(int w, int h) {
        synchronized (Display.LCDUILock) {
            layoutDone = false;
            item.lUpdateLockedSize();
        }
    }
    
    /**
     * Called to commit any pending user interaction for the item
     */
    public void lCommitPendingInteraction() { }

    /**
     * Optimized invalidate: the layout manager will do a more efficient
     * re-layout if only the height was changed.
     *
     * request the event scheduler to schedule an invalidate event, that
     * eventually will call uCallInvalidate(this item) of this item's 
     * DisplayableLFImpl
     * @param width true if it was changed
     * @param height true if it was changed
     */
    void lRequestInvalidate(boolean width, boolean height) {

        // note: we should also not call invalidate if size has not changed, 
        // and traversal has not changed.

        actualBoundsInvalid[WIDTH] = actualBoundsInvalid[WIDTH] || width;
        actualBoundsInvalid[HEIGHT] = actualBoundsInvalid[HEIGHT] || height;

        cachedWidth = INVALID_SIZE;
        layoutDone = false;
        
        if (item.owner != null) {
            ((DisplayableLFImpl)item.owner.getLF()).lRequestInvalidate();
        }
    }

    /**
     * Called by subclasses to repaint this entire Item's bounds
     */
    void uRequestPaint() {
        synchronized (Display.LCDUILock) {
            lRequestPaint();
        }
    }
    
    /**
     * Called by subclasses to repaint this entire Item's bounds
     */
    void lRequestPaint() {
        if (bounds != null) {
            // "outer" bounds repaint
            lRequestPaint(0, 0, bounds[WIDTH], bounds[HEIGHT]);

        }
    }
    
    /**
     * Called by subclasses to repaint a portion of this Item's bounds
     *
     * @param x the x coordinate of the origin of the dirty region
     * @param y the y coordinate of the origin of the dirty region
     * @param w the width of the dirty region
     * @param h the height of the dirty region
     */
    void lRequestPaint(int x, int y, int w, int h) {

        // target[] is recalculated completely, so ne need
        // to initialize it beforehand.
        if (item.owner != null) {
            
            if (x >= bounds[WIDTH] || y >= bounds[HEIGHT] ||
                x + w <= 0 || y + h <= 0 || w <= 0 || h <= 0) {
                return;
            }

            if (x < 0) {
                w += x;
                target[X] = 0;
            } else {
                target[X] = x;
            }

            if (y < 0) {
                h += y;
                target[Y] = 0;
            } else {
                target[Y] = y;
            }

            target[WIDTH] = bounds[WIDTH] - target[X];
            if (w < target[WIDTH]) {
                target[WIDTH] = w;
            }
            
            target[HEIGHT] = bounds[HEIGHT] - target[Y];
            if (h < target[HEIGHT]) {
                target[HEIGHT] = h;
            }

            if (item.owner.getLF() instanceof FormLFImpl) {
                ((FormLFImpl)item.owner.getLF()).lRequestPaintItem(item, 
                                                             target[X],
                                                             target[Y],
                                                             target[WIDTH],
                                                             target[HEIGHT]);
            } else if (item.owner.getLF() instanceof AlertLFImpl) {
                // ((AlertLFImpl)item.owner.getLF()).lRequestPaintItem(item, 
                //                                               x, y, w, h); 
                // Causes a paint error in Alert 
                // only a partial painting of the gauge...
                ((AlertLFImpl)item.owner.getLF()).lRequestPaint();
            }
        }
    }

    /**
     * Paint an item - called by Form. 
     * IMPL_NOTE: This function must be called with LCDUILock unlocked.
     *              To be renamed to uPaintItem.
     *
     * @param g the Graphics object to paint to
     * @param clip the original graphics clip to restore
     * @param trX x co-ordinate used for computing location
     * @param trY y co-ordinate used for computing location
     */
    void paintItem(Graphics g, int[] clip, int trX, int trY) {
        // SYNC NOTE: see uCallPaint()
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_ITEM_PAINT,
                           "  [I] paintItem " + this + 
                           "\t visible=" + visible);
            // note: paintItem is called more times than needed.
        }
        
        // NOTE: Its possible, that an Item is in an invalid state
        // during a requested repaint. Its ok to simply return,
        // because it means there is a validation event coming on
        // the event thread. When the form re-validates, the Item
        // will be given a proper bounds and will be repainted
        if (actualBoundsInvalid[X] || actualBoundsInvalid[Y] || 
            actualBoundsInvalid[WIDTH] || actualBoundsInvalid[HEIGHT]) {
            // I assume the invalid flag is turned to true before
            // calling paint.
            
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION,
                               LogChannels.LC_HIGHUI_ITEM_PAINT,
                               "  [I] ItemLFImpl: paintItem(..) " +
                               " BUG: invalid=true");
            }
            // should have returned here
        }
        
        // it cannot be null, since it's initialized in the CTOR, and
        // it's never get nullified afterwards:
        // if (bounds == null) {
        //    return;
        // }

        // "inner" bounds location in screen coordinates
        int tX = getInnerBounds(X) + trX;
        int tY = getInnerBounds(Y) + trY;

        // If we're already beyond the clip, quit looping, as long
        // as we're not validating the visibility of Items after a
        // scroll (calling show/hideNotify())
        if (((tY + getInnerBounds(HEIGHT) < clip[Y]) || 
             (tY > (clip[Y] + clip[HEIGHT])))) {

            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, 
                               LogChannels.LC_HIGHUI_ITEM_PAINT,
                               "  [I] ItemLFImpl: paintItem(..) " +
                               "- cutting the loop");
            }
            return;
        }

        // Clip the dirty region to only include the item
        // g.clipRect(tX, tY, bounds[WIDTH], bounds[HEIGHT]);

        // "inner" bounds
        g.clipRect(tX, tY, 
                   getInnerBounds(WIDTH),
                   getInnerBounds(HEIGHT));

        // If the Item is inside the clip, go ahead and paint it
        if (g.getClipWidth() > 0 && g.getClipHeight() > 0) {

            // Translate into the Item's coordinate space
            g.translate(tX, tY);

            // need revisit: call showNotify() on the Item first

            // We translate the Graphics into the Item's
            // coordinate space
            uCallPaint(g, getInnerBounds(WIDTH),
                       getInnerBounds(HEIGHT));

            // Its critical to undo any translates
            // IMPL_NOTE: If CustomItems are poorly written, they
            // will potentially ruin the translate. Harden this code,
            // but DO NOT use g.reset() as the translate needs to remain
            // for the surrounding layer mechanics.
            g.translate(-tX, -tY);
        } 

        // Restore the clip to its original context so
        // future clipRect() calls will have the correct intersection
        g.setClip(clip[X], clip[Y], clip[WIDTH], clip[HEIGHT]);

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_ITEM_PAINT,
                           "  [I] draw border? hasFocus="+hasFocus);
        }

        if (drawsTraversalIndicator && hasFocus && (contentBounds[HEIGHT] > 0)) {
            g.clipRect(bounds[X] + trX,
                       bounds[Y] + trY,
                       bounds[WIDTH],
                       bounds[HEIGHT]);
            paintTraversalIndicator(g, bounds[X] + trX, bounds[Y] + trY);

            g.setClip(clip[X], clip[Y], clip[WIDTH], clip[HEIGHT]);
        }        
    }

    /**
     * Paint the traversal indicator. The width/height are obtained from
     * the current traversal item's bounds.
     *
     * @param g the Graphics to paint on
     * @param x the x origin coordinate to paint the traversal indicator
     * @param y the y origin coordinate to paint the traversal indicator
     */
    

    // should move to ScreenLFImpl.paintTraversalIndicator(..) - 
    // could be used in Alert as well
    
    void paintTraversalIndicator(Graphics g, int x, int y) {
        // SYNC NOTE: see uCallPaint()
        
        // ItemLFImpl itemLF = itemLFs[traverseIndex];
        
        // NTS: This may need to special case StringItem?
        g.setColor(ScreenSkin.COLOR_TRAVERSE_IND);
         
        g.drawRect(x + 1, y + 1, bounds[WIDTH] - 2, bounds[HEIGHT]- 2);
    }
    

    /**
     * Sets the content size in the passed in array.
     * Content is calculated based on the availableWidth.
     * size[WIDTH] and size[HEIGHT] should be set by this method.
     * Subclasses need to override this method for correct layout.
     * @param size The array that holds Item content size and location 
     *             in Item internal bounds coordinate system.
     * @param availableWidth The width available for this Item
     */
    void lGetContentSize(int size[], int availableWidth) {
    }


    /**
     * Return the content size
     * @return  array of content size
     */
    int[] lGetContentBounds() {
        return contentBounds;
    }

    /**
     * Sets the label size in the passed in array.
     * Content is calculated based on the availableWidth.
     * size[WIDTH] and size[HEIGHT] should be set by this method.
     * Subclasses need to override this method for correct layout.
     * @param size The size array that holds Item label size and location 
     *             in Item internal bounds coordinate system.
     * @param availableWidth The width available for this Item
     */
    void lGetLabelSize(int size[], int availableWidth) {

        if (item.label == null || item.label.length() == 0) {
            size[WIDTH] = size[HEIGHT] = 0;
            return;
        } 
        
        if (availableWidth == -1) {
            availableWidth = lGetAvailableWidth();
        }

        Text.getSizeForWidth(size, availableWidth, item.label,
                                     ScreenSkin.FONT_LABEL, 0);
    }

    /**
     * Called by subclasses to paint this Item's label
     *
     * @param g the graphics to draw to
     * @param width the allowable width for the label
     */
    void  paintLabel(Graphics g, int width) {
        Text.paint(g, item.label, ScreenSkin.FONT_LABEL,
                   ScreenSkin.COLOR_FG, 0,
                   width, labelBounds[HEIGHT], 0, Text.NORMAL, null);
    }
    
    /**
     * Returns true if label and content can be placed on the same line.
     * If this function returns always false then content will be
     * always put on a new line in relation to label.
     * 
     * @param labelHeight The height available for the label
     * @return true If label and content can be placed on the same line; 
     *              otherwise - false.
     */
    boolean labelAndContentOnSameLine(int labelHeight) {
        return labelHeight <= ScreenSkin.FONT_LABEL.getHeight();
    }

    /**
     * Returns the vertical padding used between label and content
     * when those are laid out vertically. Subclasses should override it
     * if the padding should be other than ScreenSkin.AFTER_LABEL_VERT_PAD
     * @return the vertical padding between label and content
     */
    int getVerticalPad() {
        return ScreenSkin.PAD_LABEL_VERT;
    }


    /**
     * Returns the horizontal padding used between label and content
     * when those are laid out horizontally. Subclasses should override it
     * if the padding should be other than ScreenSkin.AFTER_LABEL_HORZ_PAD.
     * @return the horizontal padding between label and content
     */
    int getHorizontalPad() {
        return ScreenSkin.PAD_LABEL_HORIZ;
    }

    /**
     * Return the current Display instance that this Item is displayed in.
     * @return the current Display
     */
    Display getCurrentDisplay() {
        return (item.owner == null) ? 
            null :
            item.owner.getLF().lGetCurrentDisplay();
    }

    /**
     * Used by child classes to get their location and size.
     * the bounds represent the "outer" bounds of the Item, 
     * i.e. location and size that include the space needed for 
     * painting the border.
     * The "inner" bounds is the space for the Item to paint into.
     * (alternatively, it's possible to do setOuterBounds(..) instead
     *  and store in this class both "bounds" for inner bounds, and
     *  "outerBounds" for use from form. It could be more efficient
     *  for painting the item).
     * @param dimension <placeholder>
     * @return the inner bounds.
     */
    protected int getInnerBounds(int dimension) {

        // if there should be space left for highlight:
        if (dimension == X || dimension == Y) {
            return bounds[dimension] + ScreenSkin.PAD_FORM_ITEMS;
        } else {
            return bounds[dimension] - 
                ScreenSkin.PAD_FORM_ITEMS -
                ScreenSkin.PAD_FORM_ITEMS;                
        }
    }


    /**
     * Returns the locked width of the Item, or -1 if it's not locked
     * @return locked width plus adornment width, or -1 if it's not locked
     */
    protected int lGetLockedWidth() {

        if (item.lockedWidth == -1) {
            return -1;
        }

        return item.lockedWidth + ScreenSkin.PAD_FORM_ITEMS + 
            ScreenSkin.PAD_FORM_ITEMS;
    }
    
    /**
     * Returns the locked height of the Item, or -1 if it's not locked
     * @return locked height plus adornment height, or -1 if it's not locked
     */
    protected int lGetLockedHeight() {
        
        if (item.lockedHeight == -1) {
            return -1;
        }

        return item.lockedHeight + ScreenSkin.PAD_FORM_ITEMS + 
            ScreenSkin.PAD_FORM_ITEMS;
    }
    
    /**
     * This method set up internal cycle.
     *   
     * @param cycle - show if internal cycle need in
     * this item. 
     */
    void setInternalCycle(boolean cycle) {
        this.isInternalCycle = cycle;   
    }

    /** bounds[] array index to x coordinate */
    final static int X      = DisplayableLFImpl.X;
    
    /** bounds[] array index to y coordinate */
    final static int Y      = DisplayableLFImpl.Y;
    
    /** bounds[] array index to width */
    final static int WIDTH  = DisplayableLFImpl.WIDTH;
    
    /** bounds[] array index to height */
    final static int HEIGHT = DisplayableLFImpl.HEIGHT;
    
    /**
     * The owner of this view. Set in the constructor.
     */
    Item item; // = null
    
    /**
     * An array of 4 elements, describing the x, y, width, height
     * of this Item's bounds in the viewport coordinate space.
     * The bounds include the space needed for highlight and cell spacing.
     * If its null, it means the Item is currently not in the viewport
     */
    int[] bounds;
    

    /**
     * A flag indicating this Item has the input focus. This is
     * maintained by the Item superclass for easy use by subclass
     * code.
     */
    boolean hasFocus; // = false

    /**
     * A flag indicating the size of this Item has changed in a
     * subsequent layout operation
     */
    boolean sizeChanged; // = false

    /**
     * A flag indicating this Item is currently (at least partially)
     * visible on the Form it is on. This is maintained by the Item
     * superclass for easy use by subclass code.
     */
    boolean visible; // = false


    /**
     * Flags that are set to true when handling invalidate on this item.
     * If it is known that one of the dimensions remains the same, than
     * the responding invalid flag will stay false.
     * It will turn to false after it get laid out
     */
    boolean[] actualBoundsInvalid;
    
    /**
     * This flag marks if the last layout performed placed
     * this Item at the beginning of a line.
     */
    boolean isNewLine = false;

    /**
     * Marks the height of the Row this Item is placed on.
     * Note that the Item may be shorter than the height of the row
     */
    int rowHeight = 0;

    /**
     * helper array used by lRequestPaint(x,y,w,h)
     */
    int target[];

    /**
     * Is true only if this item can indicate that it has focus by 
     * changing appearance, requiring no external traversal indicator
     */

    boolean drawsTraversalIndicator = true;

    /**
     * A constant used to indicate that Item sizes have to be recalculated.
     */
    static final int INVALID_SIZE = -1;

    /**
     * Width used for last calculations. If the width passed into 
     * lGetPreferredWidth/Height and lCallPaint is the same as cachedWidth
     * then values from labelBounds and contentBounds could be used
     * without recalculation.
     */
    int cachedWidth = INVALID_SIZE;

    /**
     * Current label location and size. This array is newed in the constructor.
     * It holds location and size of the label in the internal bounds
     * coordinate system. It can be referenced using X, Y, WIDTH, HEIGHT
     * defined in this class.
     */
    int labelBounds[]; // = null
     
    /**
     * Current content location and size. This array is newed in the 
     * constructor. It holds location and size of the label 
     * in the internal bounds coordinate system. 
     * It can be referenced using X, Y, WIDTH, HEIGHT defined in this class.
     */
    int contentBounds[]; // = null

    /**
     * If true label and content locations are accurate in the
     * labelBounds and contentBounds arrays otherwise they have to be
     * recalculated.
     */
    boolean layoutDone; // = false

    /** true is the item has been focused before pointer down */
    boolean itemWasPressed; // = false
    
    /** True if internal cycle need in this item.*/
    boolean isInternalCycle;
}
