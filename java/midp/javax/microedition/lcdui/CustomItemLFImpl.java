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
import com.sun.midp.chameleon.skins.ScreenSkin;

// **************************************************************************
//  Package Private - These are all methods which delegate calls to
//                    CustomItem application code, locking on the calloutLock
//                    before doing so
// **************************************************************************

/**
* This is the Look &amps; Feel implementation for CustomItem.
*/
class CustomItemLFImpl extends ItemLFImpl implements CustomItemLF {

    /**
     * Creates CustomItemLF associated with the passed in CustomItem.
     * @param ci the CustomItem associated with this look&amps;feel.
     */
    CustomItemLFImpl(CustomItem ci) {
        super(ci);
        customItem = ci;
    }

    // **********************************************************
    //  CustItemLF interface implementation
    // ***********************************************************

    /**
     * Notifies L&F that repaint of the entire custom item is needed
     */
    public void lRepaint() {
        lRepaint(0, 0, contentBounds[WIDTH], contentBounds[HEIGHT]);
    }

    /**
     * Notifies L&F that repaint of the specified region is needed.
     *
     * @param x the x coordinate of the origin of the dirty region
     * @param y the y coordinate of the origin of the dirty region
     * @param w the width of the dirty region
     * @param h the height of the dirty region
     */
    public void lRepaint(int x, int y, int w, int h) {
        try {

            if (x > contentBounds[WIDTH] || y > contentBounds[HEIGHT] ||
                x + w <= 0 || y + w <= 0 || w <= 0 || h <= 0) {
                return;
            }

            if (x < 0) {
                w += x;
                x = 0;

            }

            if (x + w > contentBounds[WIDTH]) {
                w = contentBounds[WIDTH] - x;
            }


            if (y < 0) {
                h += y;
                y = 0;
            }

            if (y + h > contentBounds[HEIGHT]) {
                h = contentBounds[HEIGHT] - y;
            }

            lRequestPaint(x + contentBounds[X] + ScreenSkin.PAD_FORM_ITEMS,
                          y + contentBounds[Y] + ScreenSkin.PAD_FORM_ITEMS,
                          w, h);

        } catch (Exception e) {
            Display.handleThrowable(e);
        }
    }

    /**
     * Notifies L&F that Custom Item was invalidated.
     */
    public void lInvalidate() {
        lRequestInvalidate(true, true);
    }

    // JAVADOC COMMENT ELIDED
    public int lGetInteractionModes() {
        int result = customItem.TRAVERSE_HORIZONTAL |
                customItem.TRAVERSE_VERTICAL |
                customItem.KEY_PRESS |
                customItem.KEY_RELEASE;
        if (Constants.REPEAT_SUPPORTED) {
            result = result | customItem.KEY_REPEAT;
        }
        if (Constants.POINTER_SUPPORTED) {
            result = result | customItem.POINTER_PRESS |
                customItem.POINTER_RELEASE;
        }
        if (Constants.MOTION_SUPPORTED) {
            result = result | customItem.POINTER_DRAG;
        }
        return result;

    }

    /**
     * Refresh the cached preferred and minimum sizes of this CustomItem.
     */
    public void uCallSizeRefresh() {

        if (isRequestedSizesValid()) {
            return;
        }

        int mw = uCallMinimumWidth();
        if (mw < 0) mw = 0;

        int mh = uCallMinimumHeight();
        if (mh < 0) mh = 0;

        int pw = item.lockedWidth == -1 ?
            uCallPreferredWidth(item.lockedHeight) : item.lockedWidth;
        if (pw < mw) pw = mw;

        int ph = uCallPreferredHeight(pw);
        if (ph < mh) ph = mh;

        // Cache the result in ItemLFImpl
        synchronized (Display.LCDUILock) {
            minimumWidth = mw;
            minimumHeight = mh;
            preferredWidth = pw;
            preferredHeight = ph;
            cachedWidth = super.lGetAvailableWidth();
        }
    }

    /**
     * Get the preferred width of this Item, including the preferred
     * content width and room for the label. This is the callback
     * for Item's public getPreferredWidth() method.
     *
     * @param h the height to base the width size on
     * @return the preferred width
     */
    private int uCallPreferredWidth(int h) {
        // SYNC NOTE: call into app code. Must not hold LCDUILock.
        int pw = customItem.uGetContentSize(CustomItem.SIZE_PREF_WIDTH, h);
        int ph = h == -1 ?
            customItem.uGetContentSize(CustomItem.SIZE_PREF_HEIGHT, pw) : h;

        synchronized (Display.LCDUILock) {
            if (cachedWidth != INVALID_SIZE ||
                contentBounds[WIDTH] != pw || contentBounds[HEIGHT] != ph) {
                contentBounds[WIDTH]  = pw;
                contentBounds[HEIGHT] = ph;
                cachedWidth = INVALID_SIZE;
            }

            // Note that content size had to be calculated outside of
            // the LCDUILock that is why it is set here and
            // lGetContentSize is left empty
            return super.lGetPreferredWidth(h);
        }
    }

    /**
     * Get the preferred height of this Item, including the preferred
     * content height and room for the label. This is the callback
     * for Item's public getPreferredHeight() method.
     *
     * @param w the width to base the height size on
     * @return the preferred height
     */
    private int uCallPreferredHeight(int w) {
        // SYNC NOTE: call into app code. Must not hold LCDUILock.
        int prefH = customItem.uGetContentSize(CustomItem.SIZE_PREF_HEIGHT, w);
        int prefW = customItem.uGetContentSize(CustomItem.SIZE_PREF_WIDTH,
                                               prefH);
        if (prefW > w) prefW = w;

        synchronized (Display.LCDUILock) {
            if (cachedWidth != INVALID_SIZE ||
                contentBounds[WIDTH] != prefW ||
                contentBounds[HEIGHT] != prefH) {
                contentBounds[WIDTH] = prefW;
                contentBounds[HEIGHT] = prefH;
                cachedWidth = INVALID_SIZE;
            }
            // Note that content size had to be calculated outside of
            // the LCDUILock that is why it is set here and
            // lGetContentSize is left empty
            return super.lGetPreferredHeight(w);
        }
    }

    /**
     * Get the minimum width of this Item, including the minimum
     * content width and room for the label. This is the callback
     * for Item's public getMinimumWidth() method.
     *
     * @return the minimum width
     */
    private int uCallMinimumWidth() {
        // SYNC NOTE: call into app code. Must not hold LCDUILock.
        int mw = customItem.uGetContentSize(CustomItem.SIZE_MIN_WIDTH, 0);
        int ph = customItem.uGetContentSize(CustomItem.SIZE_PREF_HEIGHT, mw);

        synchronized (Display.LCDUILock) {
            if (cachedWidth != INVALID_SIZE ||
                contentBounds[WIDTH] != mw || contentBounds[HEIGHT] != ph) {
                contentBounds[WIDTH] = mw;
                contentBounds[HEIGHT] = ph;
                cachedWidth = INVALID_SIZE;
            }
            // Note that content size had to be calculated outside of
            // the LCDUILock that is why it is set here and
            // lGetContentSize is left empty
            return super.lGetPreferredWidth(-1);
        }
    }

    /**
     * Get the minimum height of this Item, including the minimum
     * content height and room for the label. This is the callback
     * for Item's public getMinimumHeight() method.
     *
     * @return the minimum height
     */
    private int uCallMinimumHeight() {
        // SYNC NOTE: call into app code. Must not hold LCDUILock.
        int minH  = customItem.uGetContentSize(CustomItem.SIZE_MIN_HEIGHT, 0);
        int prefW = customItem.uGetContentSize(CustomItem.SIZE_PREF_WIDTH,
                                               minH);
        synchronized (Display.LCDUILock) {
            if (cachedWidth != INVALID_SIZE ||
                contentBounds[WIDTH] != prefW ||
                contentBounds[HEIGHT] != minH) {
                contentBounds[WIDTH]  = prefW;
                contentBounds[HEIGHT] = minH;
                cachedWidth = INVALID_SIZE;
            }

            // Note that content size had to be calculated outside of
            // the LCDUILock that is why it is set here and
            // lGetContentSize is left empty
            return super.lGetPreferredHeight(-1);
        }
    }

    // *****************************************************
    //  Package private methods
    // *****************************************************

    /**
      * Get the preferred width of this time.
      * @param h tentative height
      * @return cached size
      */
     public int lGetPreferredWidth(int h) {
         // argument h is ignored since we are only using the cached value
         return preferredWidth;
     }

     /**
      * Get the preferred height of this time.
      * @param w tentative width
      * @return cached size
      */
     public int lGetPreferredHeight(int w) {
         // argument w is ignored since we are only using the cached value
         return preferredHeight;
     }
    /**
     * Get the minimum width of this time.
     * @return cached size
     */
    public int lGetMinimumWidth() {
        return minimumWidth;
    }

    /**
     * Get the minimum height of this time.
     * @return cached size
     */
    public int lGetMinimumHeight() {
        return minimumHeight;
    }


    /**
     * Determine if this Item should not be traversed to
     *
     * @return true if this Item should not be traversed to
     */
    boolean shouldSkipTraverse() {
        return false;
    }

    /**
     * Called by the system to indicate the size available to this Item
     * has changed
     *
     * @param w the new width of the item's content area
     * @param h the new height of the item's content area
     */
    void uCallSizeChanged(int w, int h) {
        super.uCallSizeChanged(w,h);
        int prefH = customItem.uGetContentSize(CustomItem.SIZE_PREF_HEIGHT, w);
        int prefW = customItem.uGetContentSize(CustomItem.SIZE_PREF_WIDTH, h);

        if (prefW > w) {
            prefW = w;
        }

        if (prefH > h) {
            prefH = h;
        }

        synchronized (Display.LCDUILock) {
            contentBounds[WIDTH] = prefW;
            contentBounds[HEIGHT] = prefH;
            lDoInternalLayout(labelBounds, contentBounds, w, h);
        }

        synchronized (Display.calloutLock) {
            try {
                customItem.sizeChanged(prefW, prefH);
            } catch (Throwable thr) {
                Display.handleThrowable(thr);
            }
        }
    }

    /**
     * Called to paint this CustomItem
     *
     * @param g the <code>Graphics</code> object to be used for
     * rendering the item
     * @param w current width of the item in pixels
     * @param h current height of the item in pixels
     */
    void uCallPaint(Graphics g, int w, int h) {

        int clipX, clipY, clipH, clipW;

        synchronized (Display.LCDUILock) {
            // do internal layout, paint label
            lDoInternalLayout(labelBounds, contentBounds, w, h);

            g.translate(labelBounds[X], labelBounds[Y]);

            paintLabel(g, labelBounds[WIDTH]);

            g.translate(-labelBounds[X] + contentBounds[X],
                    -labelBounds[Y] + contentBounds[Y]);

            clipX = g.getClipX();
            clipY = g.getClipY();
            clipH = g.getClipHeight();
            clipW = g.getClipWidth();

            w = contentBounds[WIDTH];
            h = contentBounds[HEIGHT];
        }

        if (clipY + clipH >= 0 && clipY < contentBounds[HEIGHT] &&
            clipX + clipW >= 0 && clipX < contentBounds[WIDTH]) {

            // We prevent the CustomItem from drawing outside the bounds.
            g.preserveMIDPRuntimeGC(0, 0, contentBounds[WIDTH], contentBounds[HEIGHT]);
            // Reset the graphics context
            g.resetGC();

            synchronized (Display.calloutLock) {
                try {
                    customItem.paint(g, w, h);
                } catch (Throwable thr) {
                    Display.handleThrowable(thr);
                }
            }
            g.restoreMIDPRuntimeGC();
        }
        g.translate(-contentBounds[X], -contentBounds[Y]);

    }

    /**
     * Traverse this CustomItem
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

        super.uCallTraverse(dir, viewportWidth, viewportHeight, visRect_inout);

        try {
            synchronized (Display.calloutLock) {
                // SYNC NOTE: Make a copy of current label height
                int contW = contentBounds[WIDTH];
                int contH = contentBounds[HEIGHT];
                int contX = contentBounds[X];
                int contY = contentBounds[Y];

                boolean t = customItem.traverse(dir, viewportWidth, 
                                                viewportHeight,
                                                visRect_inout);

                // We shift the return value from the item's traverse
                // by the label height to give the real location
                visRect_inout[X] += contX;
                visRect_inout[Y] += contY;
                return t;
            }
        } catch (Throwable thr) {
            Display.handleThrowable(thr);
        }
        return false;
    }

    /**
     * Called by the system to indicate traversal has left this Item
     *
     * @see #getInteractionModes
     * @see #traverse
     * @see #TRAVERSE_HORIZONTAL
     * @see #TRAVERSE_VERTICAL
     */
    void uCallTraverseOut() {
        super.uCallTraverseOut();

        try {
            synchronized (Display.calloutLock) {
                customItem.traverseOut();
            }
        } catch (Throwable thr) {
            Display.handleThrowable(thr);
        }
    }

    /**
     * Called by the system to signal a key press
     *
     * @param keyCode the key code of the key that has been pressed
     * @see #getInteractionModes
     */
    void uCallKeyPressed(int keyCode) {

        ItemCommandListener cl = null;
        Command defaultCmd = null;

        synchronized (Display.LCDUILock) {
            cl = customItem.commandListener;
            defaultCmd = customItem.defaultCommand;
        } // synchronized


        // SYNC NOTE: The call to the listener must occur outside
        // of the lock

        try {
            // SYNC NOTE: We lock on calloutLock around any calls
            // into application code
            synchronized (Display.calloutLock) {

                if ((cl != null)
                    && (defaultCmd != null)
                    && (keyCode == Constants.KEYCODE_SELECT)) {
                    cl.commandAction(defaultCmd, customItem);
                } else {
                    customItem.keyPressed(keyCode);
                }
            }
        } catch (Throwable thr) {
            Display.handleThrowable(thr);
        }

    }

    /**
     * Called by the system to signal a key release
     *
     * @param keyCode the key code of the key that has been released
     * @see #getInteractionModes
     */
    void uCallKeyReleased(int keyCode) {
        try {
            synchronized (Display.calloutLock) {
                customItem.keyReleased(keyCode);
            }
        } catch (Throwable thr) {
            Display.handleThrowable(thr);
        }
    }

    /**
     * Called by the system to signal a key repeat
     *
     * @param keyCode the key code of the key that has been repeated
     * @see #getInteractionModes
     */
    void uCallKeyRepeated(int keyCode) {
        try {
            synchronized (Display.calloutLock) {
                customItem.keyRepeated(keyCode);
            }
        } catch (Throwable thr) {
            Display.handleThrowable(thr);
        }
    }

    /**
     * Called by the system to signal a pointer press
     *
     * @param x the x coordinate of the pointer down
     * @param y the y coordinate of the pointer down
     *
     * @see #getInteractionModes
     */
    void uCallPointerPressed(int x, int y) {
        synchronized (Display.LCDUILock) {
            if (hasFocus) {
                itemWasPressed = true;
            }
        } // synchronized
        
        try {
            synchronized (Display.calloutLock) {
                customItem.pointerPressed(x - contentBounds[X] -
                                          ScreenSkin.PAD_FORM_ITEMS, 
                                          y - contentBounds[Y] -
                                          ScreenSkin.PAD_FORM_ITEMS);
            }
        } catch (Throwable thr) {
            Display.handleThrowable(thr);
        }
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
        boolean handled = false;
        synchronized (Display.LCDUILock) {
            ItemCommandListener cl = customItem.commandListener;
            Command defaultCmd = customItem.defaultCommand;
            if ((cl != null) && (defaultCmd != null) && itemWasPressed) {
                cl.commandAction(defaultCmd, customItem);
                handled = true; 
            }
            itemWasPressed = false;
        } // synchronized
        
        try {
            synchronized (Display.calloutLock) {
                if (!handled) {
                    customItem.pointerReleased(x - contentBounds[X] - 
                                               ScreenSkin.PAD_FORM_ITEMS, 
                                               y - contentBounds[Y] -
                                               ScreenSkin.PAD_FORM_ITEMS);
                }
            }
        } catch (Throwable thr) {
            Display.handleThrowable(thr);
        }
    }

    /**
     * Called by the system to signal a pointer drag
     *
     * @param x the x coordinate of the pointer drag
     * @param y the x coordinate of the pointer drag
     *
     * @see #getInteractionModes
     */
    void uCallPointerDragged(int x, int y) {
        try {
            synchronized (Display.calloutLock) {
                customItem.pointerDragged(x - contentBounds[X] -
                                          ScreenSkin.PAD_FORM_ITEMS, 
                                          y - contentBounds[Y] - 
                                          ScreenSkin.PAD_FORM_ITEMS);
            }
        } catch (Throwable thr) {
            Display.handleThrowable(thr);
        }
    }

    /**
     * Called by the system to notify this Item it is being shown
     *
     * <p>The default implementation of this method does nothing.</p>
     */
    void uCallShowNotify() {
        super.uCallShowNotify();

        try {
            synchronized (Display.calloutLock) {
                customItem.showNotify();
            }
        } catch (Throwable thr) {
            Display.handleThrowable(thr);
        }
    }

    /**
     * Called by the system to notify this Item it is being hidden
     *
     * <p>The default implementation of this method does nothing.</p>
     */
    void uCallHideNotify() {
        super.uCallHideNotify();

        try {
            synchronized (Display.calloutLock) {
                customItem.hideNotify();
            }
        } catch (Throwable thr) {
            Display.handleThrowable(thr);
        }
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
        return false;
    }

    /** The CustomItem associated with this view */    
    private CustomItem customItem;

    /**
     * Cached preferred height when validRequestedSizes is true.
     */
    private int preferredHeight; // default 0

    /**
     * Cached preferred width when validRequestedSizes is true.
     */
    private int preferredWidth; // default 0

    /**
     * Cached minimum height when validRequestedSizes is true.
     */
    private int minimumHeight; // default 0

    /**
     * Cached minimum width when validRequestedSizes is true.
     */
    private int minimumWidth; // default 0
} // CustomItemLF
