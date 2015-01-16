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

/* import  javax.microedition.lcdui.KeyConverter; */

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;
import com.sun.midp.configurator.Constants;

// **************************************************************************
//  Package Private - These are all methods which delegate calls to
//                    CustomItem application code, locking on the calloutLock
//                    before doing so
// **************************************************************************

/**
 * This is the look and feel implementation for <code>CustomItem</code>.
 */
class CustomItemLFImpl extends ItemLFImpl implements CustomItemLF {

    /**
     * Creates <code>CustomItemLF</code> associated with the passed in 
     * <code>CustomItem</code>.
     *
     * @param ci the <code>CustomItem</code> associated with this 
     *           look &amp; feel.
     */
    CustomItemLFImpl(CustomItem ci) {
        super(ci);

        dirtyRegion = new int[4];

        resetDirtyRegion();

        customItem = ci;
    }

    // **********************************************************
    //  CustItemLF interface implementation
    // ***********************************************************

    /**
     * Notifies L&amp;F that repaint of the entire custom item is needed.
     */
    public void lRepaint() {

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION,
                           LogChannels.LC_HIGHUI_ITEM_REPAINT,
                           ">>> CustomItemLFImpl -- lRepaint()");
        }
        
        // content area is empty no repaint is needed
        if (contentImageData == null) {
            return;
        }

        setDirtyRegionFull();

        try {
            int pad = getItemPad();
            // We prune off the label area when doing a complete repaint
            lRequestPaint(pad,
                          pad + getLabelHeight(bounds[WIDTH]),
                          contentImageData.getWidth(),
                          contentImageData.getHeight());
        } catch (Exception e) {
            Display.handleThrowable(e);
        }
    }

    /**
     * Notifies L&amp;F that repaint of the specified region is needed.
     *
     * @param x the x coordinate of the origin of the dirty region
     * @param y the y coordinate of the origin of the dirty region
     * @param width the width of the dirty region
     * @param height the height of the dirty region
     */
    public void lRepaint(int x, int y, int width, int height) {

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION,
                           LogChannels.LC_HIGHUI_ITEM_REPAINT,
                           ">>> CustomItemLFImpl -- lRepaint(" + x + 
                           "," + y + "," + width + "," + height + ")");
        }

        try {

            // Content area is empty there is no need to do anything
            if (contentImageData == null) {
                return;
            }

            int pad = getItemPad();
            // int lH = getLabelHeight(bounds[WIDTH]);

            // no need to do anything if the repaint region
            // is complete outside the content area
            
            if (x >= bounds[WIDTH] - 2*pad || 
                y >= bounds[HEIGHT] - 2*pad /* - lH */ ||
                x + width <= 0 || y + height <= 0) {
                return;
            }

            // passed in region is expressed in the same coordinate system
            // as the dirtyRegion; join those 2 regions
            if (x <= 0) {
                dirtyRegion[X1] = 0;
            } else {
                // when dirty region is unset the following will be true
                // and dirtyRegion[X1] will be correctly set
                if (dirtyRegion[X1] > x) {
                    dirtyRegion[X1] = x;
                }
            }

            if (y <= 0) {
                dirtyRegion[Y1] = 0;
            } else {
                // when dirty region is unset the following will be true
                // and dirtyRegion[Y1] will be correctly set
                if (dirtyRegion[Y1] > y) {
                    dirtyRegion[Y1] = y;
                }
            }

            if (x + width >= bounds[WIDTH] - pad) {
                dirtyRegion[X2] = bounds[WIDTH] - pad;
            } else {
                // when dirty region is unset the following will be true
                // and dirtyRegion[X2] will be correctly set
                if (x + width > dirtyRegion[X2]) {
                    dirtyRegion[X2] = x + width;
                }
            }

            if (y + height >= bounds[HEIGHT] - pad) {
                dirtyRegion[Y2] = bounds[HEIGHT] - pad;
            } else {
                // when dirty region is unset the following will be true
                // and dirtyRegion[Y2]  will be correctly set
                if (y + height > dirtyRegion[Y2]) {
                    dirtyRegion[Y2] = y + height;
                }
            }

            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION,
                               LogChannels.LC_HIGHUI_ITEM_REPAINT,
                               "after join ..... \t\t dirtyRegion (" +
                                   dirtyRegion[X1] + "," +
                                   dirtyRegion[Y1] + ") - (" +
                                   dirtyRegion[X2] + "," +
                                   dirtyRegion[Y2] + ")");
            }

            // obsolete - can use any number...
            super.lRequestPaint(0, 0, 0, 0);

            /*
            // repaint should be requested in Item's coordinate
            // system (translate by padding and labelHeight)
            super.lRequestPaint(dirtyRegion[X1] + pad,
                                dirtyRegion[Y1] + pad + lH,
                                dirtyRegion[X2] - dirtyRegion[X1] + 1,
                                dirtyRegion[Y2] - dirtyRegion[Y1] + 1);
            */

        } catch (Exception e) {
            Display.handleThrowable(e);
        }
    }

    /**
     * Notifies L&amp;F that <code>CustomItem</code> was invalidated.
     */
    public void lInvalidate() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION,
                           LogChannels.LC_HIGHUI_ITEM_LAYOUT,
                           ">>> CustomItemLFImpl -- lInvalidate()");
        }
        setDirtyRegionFull();
        lRequestInvalidate(true, true);
    }

    /**
     * Get the preferred width of this <code>Item</code>, including 
     * the preferred content width and room for the label. 
     * This is the callback for <code>Item</code>'s public 
     * getPreferredWidth() method.
     *
     * @param h the height to base the width size on
     *
     * @return the preferred width
     */
    private int uCallPreferredWidth(int h) {

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION,
                           LogChannels.LC_HIGHUI_ITEM_LAYOUT,
                           "CustomItem -- uCallPreferredWidth h=" + h);
        }

        // SYNC NOTE: Call into app code. Must not hold LCDUILock.
        int pW = customItem.uGetContentSize(CustomItem.SIZE_PREF_WIDTH, h);

        // preferred width should be at least the minimum allowed,
        // this is checked and fixed at Item level

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION,
                           LogChannels.LC_HIGHUI_ITEM_LAYOUT,
                           "\t -- uCallPreferredWidth item returned " +
                           pW);
        }

        synchronized (Display.LCDUILock) {
            // if width is not locked item.lockedWidth will be -1
            // which means that all available width can be used
            int lw = getLabelWidth(item.lockedWidth);

            // if label is wider than customItem body, we're allowed
            // to make the body wider.
            if (lw > pW) {
                pW = lw;
            }

            if (pW > 0) {
                pW += 2 * getItemPad();
            }
        }

        return pW;
    }

    /**
     * Get the preferred height of this <code>Item</code>, including the 
     * preferred content height and room for the label. 
     * This is the callback for <code>Item</code>'s public 
     * getPreferredHeight() method.
     *
     * @param w the width to base the height size on
     *
     * @return the preferred height
     */
    private int uCallPreferredHeight(int w) {

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION,
                           LogChannels.LC_HIGHUI_ITEM_LAYOUT,
                           "CustomItem -- uCallPreferredHeight w=" + w);
        }
        // SYNC NOTE: Call into app code. Must not hold LCDUILock.
        int pH = customItem.uGetContentSize(CustomItem.SIZE_PREF_HEIGHT, w);

        // preferred height should be at least the minimum allowed,
        // this is checked and fixed at Item level

        synchronized (Display.LCDUILock) {
            pH += getLabelHeight(w);

            if (pH > 0) {
                pH += 2 * getItemPad();
            }
        }

        return pH;
    }

    /**
     * Get the minimum width of this <code>Item</code>, including the 
     * minimum content width and room for the label. 
     * This is the callback for <code>Item</code>'s public 
     * getMinimumWidth() method.
     *
     * @return the minimum width
     */
    private int uCallMinimumWidth() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION,
                           LogChannels.LC_HIGHUI_ITEM_LAYOUT,
                           "CustomItemLFImpl -- uCallMinimumWidth");
        }
        // SYNC NOTE: Call into app code. Must not hold LCDUILock.
        int mW = customItem.uGetContentSize(CustomItem.SIZE_MIN_WIDTH, 0);
        
        synchronized (Display.LCDUILock) {
            int lw = getLabelWidth(-1);
            
            // if label is wider than customItem body, we're allowed
            // to make the body wider.
            if (lw > mW) {
                mW = lw;
            }
            
            if (mW > 0) {
                mW += 2 * getItemPad();
            }
        }
        
        return mW;
    }

    /**
     * Get the minimum height of this <code>Item</code>, including the 
     * minimum content height and room for the label. 
     * This is the callback for <code>Item</code>'s public 
     * getMinimumHeight() method.
     *
     * @return the minimum height
     */
    private int uCallMinimumHeight() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION,
                           LogChannels.LC_HIGHUI_ITEM_LAYOUT,
                           "CustomItemLFImpl -- uCallMinimumHeight");
        }
        // SYNC NOTE: Call into app code. Must not hold LCDUILock.
        int mH = customItem.uGetContentSize(CustomItem.SIZE_MIN_HEIGHT, 0);

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION,
                           LogChannels.LC_HIGHUI_ITEM_LAYOUT,
                           "CustomItem -- uCallMinimumHeight ret: " + mH);
        }
        synchronized (Display.LCDUILock) {
            mH += getLabelHeight(-1);

            if (mH > 0) {
                mH += 2 * getItemPad();
            }
        }

        return mH;
    }

    /**
     * Get minimum and preferred sizes from <code>CustomItem</code> 
     * subclass and cache the result in super class.
     */
    public void uCallSizeRefresh() {

        if (isRequestedSizesValid()) {
            return;
        }

        int mw = uCallMinimumWidth();
        if (mw < 0) mw = 0;

        int mh = uCallMinimumHeight();
        if (mh < 0) mh = 0;
        
        int pw = uCallPreferredWidth(item.lockedHeight);
        if (pw < mw) pw = mw;

        int ph = uCallPreferredHeight(pw);

        synchronized (Display.LCDUILock) {
            // NOTE: When the item should shrink, the minimum size is used,
            // and the minimum size is calculated with the label
            // on the same line
            if (shouldHShrink() &&
                item.label != null &&
                item.label.length() > 0) {
                mh += DEFAULT_LABEL_HEIGHT;
            }
            if (ph < mh) ph = mh;

            // Cache the result in ItemLFImpl
            lSetRequestedSizes(mw, mh, pw, ph);
        }
    }

    /**
     * Overriding <code>ItemLFImpl</code>.
     * Notifies L&amp;F of a label change in the corresponding 
     * <code>Item</code>.
     *
     * @param label the new label string
     */
    public void lSetLabel(String label) {
        super.lSetLabel(label);
    }

    // JAVADOC COMMENT ELIDED
    public int lGetInteractionModes() {

        // removed support for traversal.
        // (MIDlets should use low level key events instead)

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

    // *****************************************************
    //  Package private methods
    // *****************************************************

    /**
     * Calculate minimum and preferred width and height of this item and 
     * store the result in instance variables
     * minimumWidth, minimumHeight, preferredWidth and preferredHeight.
     *
     * Override the version in <code>ItemLFImpl</code> to do nothing.
     */
    void lGetRequestedSizes() {
        // Even if (isRequestedSizesValid() == false), we won't be able to
        // call into app code for the content sizes since we
        // are holding LCDUILock and may be even on event dispatch thread.
        // Do nothing here so the cached requested sizes will be used.
    }

    /**
     * Called by the system to indicate the size available to this 
     * <code>Item</code> has changed.
     *
     * SYNC NOTE: Caller must not hold LCDUILock.
     *
     * @param w the new width of the item's content area
     * @param h the new height of the item's content area
     */
    void uCallSizeChanged(int w, int h) {
        try {
            synchronized (Display.calloutLock) {
                int pad = getItemPad();
                h -= 2*pad + getLabelHeight(w);
                w -= 2*pad;
                customItem.sizeChanged(w, h);
            }
        } catch (Throwable thr) {
            Display.handleThrowable(thr);
        }
    }

    /**
     * Called to paint this <code>CustomItem</code>.
     *
     * @param g the <code>Graphics</code> object to be used for
     *          rendering the item.
     * @param w current width of the item in pixels
     * @param h current height of the item in pixels
     */
    void uCallPaint(Graphics g, int w, int h) {
                
        // ignore passed in graphics and use Custom Item's muttable image
        
        // contentImageData consists only of content so there is
        // no need to do translation by the label height and 
        // around item padding

        int x1, x2, y1, y2;

        boolean visInViewport;

        synchronized (Display.LCDUILock) {

            // spec requires not to call CustomItem's paint
            // if content area width or height is 0 that is why:
            // if content area is empty or dirty region is unset
            // no repaint is needed
            if (contentImageData == null ||
                dirtyRegion[Y2] <= dirtyRegion[Y1] ||
                dirtyRegion[X2] <= dirtyRegion[X1]) {
                return;
            }

            x1 = dirtyRegion[X1];
            x2 = dirtyRegion[X2];
            y1 = dirtyRegion[Y1];
            y2 = dirtyRegion[Y2];
            visInViewport = visibleInViewport;

            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, 
                               LogChannels.LC_HIGHUI_ITEM_REPAINT, 
                               "<<< \t\t Current clip: (" +
                               g.getClipX() + "," +
                               g.getClipY() + ") - (" +
                               (g.getClipWidth()) + "," +
                               (g.getClipHeight()) + ")" +
                               "<<< \t\t clipping to: (" +
                               dirtyRegion[X1] + "," +
                               dirtyRegion[Y1] + ") - (" +
                               (dirtyRegion[X2] - dirtyRegion[X1] + 1) 
                               + "," +
                               (dirtyRegion[Y2] - dirtyRegion[Y1] + 1) +
                               ")\n\n");
            }
            
            contentGraphics.setClip(x1, y1, x2 - x1 + 1, y2 - y1 +1);
            
            contentGraphics.setColor(
                            Theme.getColor(Display.COLOR_BACKGROUND));
            contentGraphics.fillRect(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
            
            contentGraphics.setColor(0);
            contentGraphics.setFont(Font.getDefaultFont());
            contentGraphics.setStrokeStyle(Graphics.SOLID);

            resetDirtyRegion();
            
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                contentGraphics.drawLine(dirtyRegion[X1],
                                         dirtyRegion[Y1],
                                         dirtyRegion[X2] - dirtyRegion[X1] + 1,
                                         dirtyRegion[Y2] - dirtyRegion[Y1] + 1);
                Logging.report(Logging.INFORMATION, 
                               LogChannels.LC_HIGHUI_ITEM_REPAINT,
                               "<<< CustomItemLFImpl -- uCallPaint " +
                               nativeId +" / " + w + "x" + h);
            }
        }
         
        // SYNC NOTE: Call into app code. Must not hold LCDUILock.
        try {   
            synchronized (Display.calloutLock) {
                // call to the MIDlet even if it is not visible
                // but do not refresh if it is not visible in viewport
                
                // SYNC NOTE: the change of contentGraphics and use of
                // contentGraphics happen on the event dispatch thread
                // so there is no problem of doing it outside of 
                // the LCDUILock
                customItem.paint(contentGraphics, 
                                 contentImageData.getWidth(),
                                 contentImageData.getHeight());
            }

            // Show the buffer onto screen
            synchronized (Display.LCDUILock) {
                // Need to check nativeId again since it might have been
                // deleted if the CustomItem is removed from the form
                if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
                    refresh0(nativeId, x1, y1, x2 - x1 + 1, y2 - y1 + 1);
                }
            }
        } catch (Throwable thr) {
            Display.handleThrowable(thr);
        }
    }


    /**
     * Reset the values to invalid coordinates.
     */
    private void resetDirtyRegion() {
        dirtyRegion[X1] = 1000;
        dirtyRegion[Y1] = 1000;
        dirtyRegion[X2] =  0;
        dirtyRegion[Y2] =  0;        
    }

    
    /**
     * Reset the values to invalid coordinates.
     */
    private void setDirtyRegionFull() {
        dirtyRegion[X1] = 0;
        dirtyRegion[Y1] = 0;
        if (contentImageData == null) {
            dirtyRegion[X2] = dirtyRegion[Y2] = 0;
        } else {
            dirtyRegion[X2] = contentImageData.getWidth();
            dirtyRegion[Y2] = contentImageData.getHeight();
        }
    }


    /**
     * Called by the system to notify internal traverse into the item.
     *
     * @param dir the direction of traversal
     * @param viewportWidth the width of the container's viewport
     * @param viewportHeight the height of the container's viewport
     * @param visRect_inout passes the visible rectangle into the method, and
     *                      returns the updated traversal rectangle from the
     *                      method
     *
     * @return <code>true</code> if internal traversal had occurred,
     *         <code>false</code> if traversal should proceed out
     *
     * @see #getInteractionModes
     * @see #traverseOut
     * @see #TRAVERSE_HORIZONTAL
     * @see #TRAVERSE_VERTICAL
     */
    boolean uCallTraverse(int dir, int viewportWidth, int viewportHeight,
                          int[] visRect_inout) {
        // the super implementation has to set the focus to this item

        boolean ret = super.uCallTraverse(dir, viewportWidth,
                                          viewportHeight, visRect_inout);
        try {
            synchronized (Display.calloutLock) {
                if (hasFocus) {
                    int lH = getLabelHeight(bounds[WIDTH]);
                    
                    // We shave off the label height from the overall
                    // item viewport
                    visRect_inout[HEIGHT] -= lH;
                    
                    // NOTE: visRect_inout should reflect native scroll
                    ret |= customItem.traverse(dir, viewportWidth,
                                               viewportHeight - lH,
                                               visRect_inout);
                    // We shift the return value from the item's traverse
                    // by the label height to give the real location
                    visRect_inout[Y] += lH;
                    if (ret) {
                        setDirtyRegionFull();
                    }
                }
            }
        } catch (Throwable thr) {
            Display.handleThrowable(thr);
        }
        return ret;
    }

    /**
     * Called by the system to indicate traversal has left this 
     * <code>Item</code>.
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
                setDirtyRegionFull();
            }
        } catch (Throwable thr) {
            Display.handleThrowable(thr);
        }
    }

    /**
     * Called by the system to signal a key press.
     *
     * @param keyCode the key code of the key that has been pressed.
     *
     * @see #getInteractionModes
     */
    void uCallKeyPressed(int keyCode) {
        ItemCommandListener cl = null;
        Command defaultCmd = null;
        FormLFImpl ownerLFImpl = null;
        boolean internalTraverse = false;
        int vis_Rect[] = new int[4];
        // vpY1 the y coordinate of the top left visible pixel
        int vpY1 = 0;
        // vpY2 the y coordinate of bottom left visible pixel
        int vpY2 = 0;

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                           "CustomItemLFImpl: got uCallKeyPressed: " +
                           keyCode);
        }
      
        synchronized (Display.LCDUILock) {
            cl = customItem.commandListener;
            defaultCmd = customItem.defaultCommand;

            if (item.owner != null) {
                ownerLFImpl = 
                    (FormLFImpl)item.owner.getLF();
            }
        } // synchronized


        // SYNC NOTE: The call to the listener must occur outside the lock

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
            } // end synchronized
        } catch (Throwable thr) {
            Display.handleThrowable(thr);
        }

        synchronized (Display.LCDUILock) {
            if (internalTraverse) {
                scrollforInternalTraversal(
                        ownerLFImpl, vis_Rect);
            }
        } // end synchronized
}

    /**
     * Called by the system to signal a key release.
     *
     * @param keyCode the key code of the key that has been released.
     *
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
     * Called by the system to signal a key repeat.
     *
     * @param keyCode the key code of the key that has been repeated.
     *
     * @see #getInteractionModes
     */
    void uCallKeyRepeated(int keyCode) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                           "CustomItemLFImpl: uCallKeyRepeated!! " + keyCode);
        }

        try {
            synchronized (Display.calloutLock) {
                customItem.keyRepeated(keyCode);
            }
        } catch (Throwable thr) {
            Display.handleThrowable(thr);
        }
    }

    /**
     * Called by the system to signal a pointer press.
     *
     * @param x the x coordinate of the pointer down
     * @param y the y coordinate of the pointer down
     *
     * @see #getInteractionModes
     */
    void uCallPointerPressed(int x, int y) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                           "*-* CustomItem: uCallPointerPressed *-*");
        }
        try {
            synchronized (Display.calloutLock) {
                customItem.pointerPressed(x, y);
            }
        } catch (Throwable thr) {
            Display.handleThrowable(thr);
        }
    }

    /**
     * Called by the system to signal a pointer release.
     *
     * @param x the x coordinate of the pointer up
     * @param y the x coordinate of the pointer up
     *
     * @see #getInteractionModes
     */
    void uCallPointerReleased(int x, int y) {
        try {
            synchronized (Display.calloutLock) {
                customItem.pointerReleased(x, y);
            }
        } catch (Throwable thr) {
            Display.handleThrowable(thr);
        }
    }

    /**
     * Called by the system to signal a pointer drag.
     *
     * @param x the x coordinate of the pointer drag
     * @param y the x coordinate of the pointer drag
     *
     * @see #getInteractionModes
     */
    void uCallPointerDragged(int x, int y) {
        try {
            synchronized (Display.calloutLock) {
                customItem.pointerDragged(x, y);
            }
        } catch (Throwable thr) {
            Display.handleThrowable(thr);
        }
    }

    /**
     * Override <code>ItemLFImpl</code> method to
     * set the dirty region and the content buffer
     * before showing the native resource.
     */
    void lShowNativeResource() {
        if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
            setContentBuffer0(nativeId, contentImageData);
            super.lShowNativeResource();
        }
    }

    /**
     * Override <code>ItemLFImpl</code> method to reset
     * the dirty region before hiding the native resource
     */
    void lHideNativeResource() {
        resetDirtyRegion();
        super.lHideNativeResource();
    }

    /**
     * Overrides the default method in <code>ItemLFImpl</code>.
     * Called by the system to notify this <code>CustomItem</code>
     * that it is being shown. This method will be called only
     * if this <code>CustomItem</code> was made visible.
     * 
     * The default implementation changes the visibleInViewport flag.
     */
    void uCallShowNotify() {
        
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION,
                           LogChannels.LC_HIGHUI_ITEM_REPAINT,
                           "CustomItemLFImpl: uCallShowNotify()");
        }

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
     * Overrides the default method to set dirty region to full size.
     */
    void lCallShowNotify() {
        super.lCallShowNotify();
        setDirtyRegionFull();
    }

    /**
     * Overrides the default method in <code>ItemLFImpl</code>.
     * Called by the system to notify this <code>CustomItem</code>
     * that it is being hidden. This method will be called only
     * if this <code>CustomItem</code> was hidden.
     * 
     * The default implementation changes the visibleInViewport flag.
     */
    void uCallHideNotify() {
        
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION,
                           LogChannels.LC_HIGHUI_ITEM_REPAINT,
                           "CustomItemLFImpl: uCallHideNotify()");
        }

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
     * Sets custom item's size
     * 
     * @param w - the new width of the item
     * @param h - the new height of the item
     */
    void lSetSize(int w, int h) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION,
                           LogChannels.LC_HIGHUI_ITEM_LAYOUT,
                           " CustomItemLFImpl  -  setSize: " + w + "x" + h);
        }

        if (w == bounds[WIDTH] && h == bounds[HEIGHT]) {
            return;
        }
        int pad = getItemPad();
        int contentW = w - 2*pad;
        int contentH = h - 2*pad - getLabelHeight(w);

        if (contentImageData == null ||
            contentImageData.getWidth() != contentW || 
            contentImageData.getHeight() != contentH) {
            if (contentW > 0 && contentH > 0) {
                Image contentImage = Image.createImage(contentW, contentH);
                contentImageData = contentImage.getImageData();
                if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
                    setContentBuffer0(nativeId, contentImageData);
                }
                contentGraphics = contentImage.getGraphics();
                // no need to paint background of the newly created 
                // Mutable image since according to the spec 
                // it will be set to white color
                setDirtyRegionFull();
            }
        }

        super.lSetSize(w, h);
    }

    /**
     * Get label height.
     *
     * @param w width available for label
     *
     * @return label height
     */
    private int getLabelHeight(int w) {

        // check empty label case:
        if (customItem.label == null || customItem.label.equals("") ||
            (w >= 0 && w <= 2*getItemPad())) {
            return 0;
        }
        
        if (w > 0) {
            w -= 2*getItemPad();
        } else if (w != -1) {
            w = -1;
        }

        // query native for real preferred size
        boolean wasNoNative = 
          (nativeId == DisplayableLFImpl.INVALID_NATIVE_ID);

        // Native resource not yet created, do it now
        if (wasNoNative) {
            createTempNativeResource();
        }

        int h = getLabelHeight0(nativeId, w);

        if (wasNoNative) {
            deleteNativeResource();
        }

        return h;
    }

    /**
     * Gets label width used in native. If -1 is passed as a width 
     * parameter the whole available width should be used. Note
     * that padding will be subtracted from the passed in width.
     * 
     * @param w the width to be used to get label width.
     * @return actual width of the label.
     */
    private int getLabelWidth(int w) {

        // check empty label case:
        if (customItem.label == null || customItem.label.equals("") ||
            (w >= 0 && w <= getItemPad())) {
            return 0;
        }

        if (w > 0) {
            w -= 2*getItemPad();
        } else if (w != -1) {
            w = -1;
        }

        // query native for real preferred size
        boolean wasNoNative = 
          (nativeId == DisplayableLFImpl.INVALID_NATIVE_ID);

        // Native resource not yet created, do it now
        if (wasNoNative) {
            createTempNativeResource();
        }

        int lw = getLabelWidth0(nativeId, w);

        if (wasNoNative) {
            deleteNativeResource();
        }

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_ITEM_REPAINT,
                           "CustomItemLFImpl: getLabelWidth(" + w +
                           ")... nativeId==" + nativeId +
                           " \t returning: " + lw);
        }
        return lw;
    }

    
    /**
     * Returns item pad used in native. The value is fetched only once
     * and cached in Java.
     *
     * @return item pad used in native
     */
    private int getItemPad() {

        if (ITEM_PAD == 0) {
            // query native for real preferred size
            boolean wasNoNative = 
                (nativeId == DisplayableLFImpl.INVALID_NATIVE_ID);
            
            // Native resource not yet created, do it now
            if (wasNoNative) {
                createTempNativeResource();
            }
            
            ITEM_PAD = getItemPad0(nativeId);
                        
            if (wasNoNative) {
                deleteNativeResource();
            }
        }

        return ITEM_PAD;
    }


    /** 
     * The <code>CustomItem</code> associated with this view.
     */    
    private CustomItem customItem;


    /**
     * Create native resource for current <code>CustomItem</code>.
     * Override function in <code>ItemLFImpl</code>.
     *
     * @param ownerId Owner screen's native resource id.
     */
    void createNativeResource(int ownerId) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                           "****************************************" +
                           "CustomItem: createNativeResource -- ownerId=" +
                           ownerId + 
                           "bounds are: " + bounds[X] + "," + bounds[Y] +
                           " -- " + bounds[WIDTH] + "x" + bounds[HEIGHT] + 
                           "****************************************");
        }
        nativeId = createNativeResource0(ownerId,
                                         customItem.label,
                                         customItem.layout);
    }

    /**
     * Called by <code>Display</code> to notify an <code>ItemLF</code> 
     * in current <code>FormLF</code> of a change in its peer state.
     * Return false since no notification is needed.
     * @param hint notification sub-type defined as above
     *
     * @return always <code>false</code> since no internal state changes.
     */
    boolean uCallPeerStateChanged(int hint) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                           "-=- CustomItemLFImpl uCallPeerStateChanged " +
                           hint);
        }
        return false;
    }

    // *****************************************************
    //  Private methods
    // *****************************************************

    /**
     * Called by traverse method to 
     * scroll the CustomItem for InternalTraversal.
     * @param ownerLFImpl FormLFImpl
     * @param vis_Rect the updated traversal rectangle from 
     *                      the traverse method
     */
    private void scrollforInternalTraversal(
                      FormLFImpl ownerLFImpl, int[] vis_Rect) {
        int yOffset = 0;

        // check the returned vis_Rect 
        // to check the validity of x,y,w,h
        // x and y values are relative to item's origin

        if (vis_Rect[X] < 0) {
            vis_Rect[X] = 0;
        }
        if (vis_Rect[WIDTH] < 0) {
            vis_Rect[WIDTH] = 0;
        }
        if (vis_Rect[X] > bounds[WIDTH]) {
            vis_Rect[X] = bounds[WIDTH];
        }
        if (vis_Rect[Y] < 0) {
            vis_Rect[Y] = 0;
        }
        if (vis_Rect[HEIGHT] < 0) {
            vis_Rect[HEIGHT] = 0;
        }
        if (vis_Rect[Y] > bounds[HEIGHT]) {
            vis_Rect[Y] = bounds[HEIGHT];
        }

        // shouldn't exceed viewportHeight
        if (vis_Rect[HEIGHT] > ownerLFImpl.height) {
            vis_Rect[HEIGHT] = ownerLFImpl.height;
        }
        // shouldn't exceed viewportwidth
        if (vis_Rect[WIDTH] > ownerLFImpl.width) {
            vis_Rect[WIDTH] = ownerLFImpl.width;
        }

        // current scroll position
        int vpY1 = ownerLFImpl.getScrollPosition0();
        // vpY2 the y coordinate of bottom left visible pixel
        int vpY2 = vpY1 + ownerLFImpl.height;

        // convert vis_Rect Y into form's co-ordinates
        vis_Rect[Y] += bounds[Y];

        int itemHeight = vis_Rect[HEIGHT];
        int vpHeight = ownerLFImpl.height;

        // make sure that the item is visible
        ItemLFImpl itemLFInFocus = ownerLFImpl.getItemInFocus();
        if ((itemLFInFocus != null)
            && (itemLFInFocus.nativeId 
                != ownerLFImpl.INVALID_NATIVE_ID)) {
            // find the y offset depending on 
            // the vis_Rect returned       

            int vpMidpoint = vpY1 + vpHeight/2;
            int itemMidpoint = vis_Rect[Y] + itemHeight/2;

            if (itemHeight <= vpHeight) { // center it
                // HI DECISION: short circuit scrolling all together
                // if the complete vis_Rect area is in the viewport

                if (itemMidpoint > vpMidpoint) { // lower
                    yOffset = itemMidpoint - vpMidpoint;
                } else if (itemMidpoint < vpMidpoint) { // upper
                    yOffset = vpMidpoint - itemMidpoint;
                }
            } else if (itemHeight > vpHeight) { // top it
                if (itemMidpoint > vpMidpoint) { // lower
                    yOffset = vis_Rect[Y] - vpY1;
                } else if (itemMidpoint < vpMidpoint) { // upper
                    yOffset = vpY1 - vis_Rect[Y];
                }                     
            }
                   
            // QT makes this visible with at least
            // 50 pixel margins (if possible, otherwise centered)
            ownerLFImpl.setCurrentItem0(
                        nativeId, itemLFInFocus.nativeId, yOffset);
        }
    }

    /**
     * KNI function that creates native resource for current 
     * <code>CustomItem</code>.
     *
     * @param ownerId Owner screen's native resource id 
     *                (<code>MidpDisplayable *</code>)
     * @param label - label to be used for this <code>Item</code>
     * @param layout - layout directive associated with this <code>Item</code>
     *
     * @return native resource id (<code>MidpItem *</code>) of this 
     *         <code>CustomItem</code>
     */
    private static native int createNativeResource0(int ownerId,
                                                    String label,
                                                    int layout);

    /**
     * Call to blit the paint result to the <code>CustomItem</code>.
     *
     * @param nativeId native resource id for this <code>Item</code>
     * @param x coordinate relative to the widget
     * @param y coordinate relative to the widget
     * @param width invalid width to repaint. If < 0 than paint all.
     * @param height invalid height to repaint. If < 0 than paint all.
     */
    private static native void refresh0(int nativeId,
                                        int x,
                                        int y,
                                        int width,
                                        int height);
  
    /**
     * Returns label height in native widget.
     *
     * @param nativeId native resource id for this <code>Item</code>
     * @param width tentative width used to calculate the height
     *
     * @return label height in native widget
     */
    private static native int getLabelHeight0(int nativeId, int width);

    /**
     * Get the actual width required for the label.
     *
     * @param nativeId native resource id for this <code>Item</code>
     * @param contentWidth hint for the native widget to decide on label layout
     *
     * @return actual label width in native widget
     */
    private static native int getLabelWidth0(int nativeId, int contentWidth);

    /**
     * Returns item pad used in native.
     * 
     * @param nativeId native resource id for this <code>Item</code>
     *
     * @return item pad used in native
     */
    private static native int getItemPad0(int nativeId);

    /**
     * Sets the content buffer. All paints are done to that buffer.
     * When paint is processed snapshot of the buffer is flushed to
     * the native resource content area.
     * @param nativeId native resource is for this CustomItem
     * @param imgData mutable <tt>ImageData</tt>
     *        associated with an <tt>Image</tt> 
     *        that serves as an offscreen buffer
     */
    private static native void setContentBuffer0(int nativeId, 
                                                 ImageData imgData);

    /**
     * Parameter used by dirtyRegion[].
     */
    private final static int X1 = 0;

    /**
     * Parameter used by dirtyRegion[].
     */
    private final static int Y1 = 1;

    /**
     * Parameter used by dirtyRegion[].
     */
    private final static int X2 = 2;

    /**
     * Parameter used by dirtyRegion[].
     */
    private final static int Y2 = 3;

    /**
     * Represents the dirty region since last repaint.
     * Array of 4 integers, representing the two corners: (x1,y1), (x2,y2)
     */
    private int[] dirtyRegion = null; 

    /**
     * Internal spacing between <code>Item</code>'s inner components - 
     * label and body.
     * This value is taken from native, and cached here, to minimize native
     * calls.
     */
    private static int ITEM_PAD = 0;


    /**
     * Mutable image that holds CustomItem repaints
     */
    private ImageData contentImageData; // = NULL;

    /**
     * Graphics associated with contentImage
     */
    private Graphics contentGraphics; // = NULL;

} // CustomItemLF
