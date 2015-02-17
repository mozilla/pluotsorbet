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

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;
import com.sun.midp.configurator.Constants;

/**
 * Look and feel implementation for <code>Item</code> based on 
 * platform widgets.
 *
 * SYNC NOTE: Only functions with prefix 'uCall' handle locking internally.
 * Its the caller's responsibility to hold LCDUILock around calls to the
 * rest of functions.
 */
abstract class ItemLFImpl implements ItemLF {

    /**
     * Creates look and feel for the passed in <code>Item</code>.
     *
     * @param item the <code>Item</code> for which the look &amp; feel 
     *             should be created
     */
    ItemLFImpl(Item item) {
        this.item = item;
        actualBoundsInvalid = new boolean[] {true, true, true, true};
        bounds = new int[] {0, 0, 0, 0};
    }


    // *****************************************************
    //  Public methods - ItemLF interface implementation
    // *****************************************************

    
    /**
     * Returns the locked width of the Item, or -1 if it's not locked
     * @return locked width plus adornment width, or -1 if it's not locked
     */
    int lGetLockedWidth() {
	
	if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
	    Logging.report(Logging.INFORMATION,
			   LogChannels.LC_HIGHUI_ITEM_LAYOUT,
			   " ItemLFImpl  -  lGetLockedWidth: " + 
			   item.lockedWidth);
	}
	return item.lockedWidth;
    }

    /**
     * Returns the locked height of the Item, or -1 if it's not locked
     * @return locked height plus adornment height, or -1 if it's not locked
     */
    protected int lGetLockedHeight() {

	if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
	    Logging.report(Logging.INFORMATION,
			   LogChannels.LC_HIGHUI_ITEM_LAYOUT,
			   " ItemLFImpl  -  lGetLockedHeight: " + 
			   item.lockedHeight);
	}
	return item.lockedHeight;
    }

    /**
     * PLACE HOLDER
     * Used by the Form Layout to set the size of this Item
     *
     * @param height the tentative content height in pixels
     * @return the preferred width 
     */
    int lGetAdornedPreferredWidth(int height) {
	return lGetPreferredWidth(height);
    }

    /**
     * PLACE HOLDER
     * Used by the Form Layout to set the size of this Item
     *
     * @param width the tentative content width in pixels
     * @return the preferred height
     */
    int lGetAdornedPreferredHeight(int width) {
	return lGetPreferredHeight(width);
    }

    /**
     * PLACE HOLDER
     * Used by the Form Layout to set the size of this Item
     * @return the minimum width that includes cell spacing
     */
    int lGetAdornedMinimumWidth() {
	return lGetMinimumWidth();
    }

    /**
     * PLACE HOLDER
     * Used by the Form Layout to set the size of this Item
     * @return the minimum height that includes cell spacing
     */
    int lGetAdornedMinimumHeight() {
	return lGetMinimumHeight();
    }
    
    /**
     * Get the preferred width of this <code>Item</code>.
     *
     * @param h tentative locked height.
     * 		Ignored here and item.lockedHeight is used always.
     *
     * @return the preferred width
     */
    public int lGetPreferredWidth(int h) {
	
	// note: h is not used!

	lGetRequestedSizes();
	return preferredWidth;
    }

    /**
     * Get the preferred height of this <code>Item</code>.
     *
     * @param w tentative locked width.
     * 		Ignored here and preferred width is used always.
     *
     * @return the preferred height
     */
    public int lGetPreferredHeight(int w) {

	// note: w is not used

	lGetRequestedSizes();
	return preferredHeight;
    }

    /**
     * Get the minimum width of this <code>Item</code>.
     *
     * @return the minimum width
     */
    public int lGetMinimumWidth() {

	lGetRequestedSizes(); // Make sure cached sizes are up to date
	return minimumWidth;
    }

    /**
     * Get the minimum height of this <code>Item</code>.
     *
     * @return the minimum height
     */
    public int lGetMinimumHeight() {
	lGetRequestedSizes();
	return minimumHeight;
    } 

    /**
     * Notifies L&amp;F of a label change in the corresponding 
     * <code>Item</code>.
     *
     * @param label the new label string
     */
    public void lSetLabel(String label) {
	if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
	    setLabel0(nativeId, label);
	}
        lRequestInvalidate(true, true);
    }

    /**
     * Notifies L&amp;F of a layout change in the corresponding 
     * <code>Item</code>.
     *
     * @param layout the new layout descriptor
     */
    public void lSetLayout(int layout) {
        lRequestInvalidate(true, true);
    }

    /**
     * Notifies L&amp;F of a command addition in the corresponding 
     * <code>Item</code>.
     *
     * @param cmd the newly added command
     * @param i the index of the added command in the <code>Item</code>'s
     *          commands[] array
     */
    public void lAddCommand(Command cmd, int i) {
        if (item.owner != null) {
            ((DisplayableLFImpl)item.owner.getLF()).updateCommandSet();
        }
    }

    /**
     * Notifies L&amp;F of a command removal in the corresponding
     * <code>Item</code>.
     *
     * @param cmd the newly removed command
     * @param i the index of the removed command in the <code>Item</code>'s
     *          commands[] array
     */
    public void lRemoveCommand(Command cmd, int i) {
        if (item.owner != null) {
            ((DisplayableLFImpl)item.owner.getLF()).updateCommandSet();
        }
    }

    /**
     * Notifies L&amp;F of a preferred size change in the corresponding
     * <code>Item</code>.
     *
     * @param width the value to which the width is locked, or
     *              <code>-1</code> if it is unlocked
     * @param height the value to which the height is locked, or
     *               <code>-1</code> if it is unlocked
     */
    public void lSetPreferredSize(int width, int height) {
        if (width == bounds[WIDTH] && height == bounds[HEIGHT]) {
            // no need to invalidate
            return;
        }
        lRequestInvalidate(width != bounds[WIDTH],
			   height != bounds[HEIGHT]);
    }

    /**
     * Notifies L&amp;F of the default command change in the corresponding
     * <code>Item</code>.
     *
     * @param cmd the newly set default command
     * @param i index of this new command in the <code>ChoiceGroup</code>'s
     *          commands array
     */
    public void lSetDefaultCommand(Command cmd, int i) {}

    /**
     * Notify this itemLF that its owner screen has changed.
     * Clear internal state if its new owner is <code>null</code>.
     *
     * @param oldOwner old owner screen before this change. New owner
     *                 can be found in <code>Item</code> model.
     */
    public void lSetOwner(Screen oldOwner) {

	// Currently Item has no owner
	if (item.owner == null) {

	    // Hide native resource
	    lHideNativeResource();

	    // Free any native resource
	    deleteNativeResource();

	    // do app notification
	    if (visibleInViewport) {
		uCallHideNotify();
	    }

	    // the deleted item may be added later, so we
	    // have to invalidate it.
	    actualBoundsInvalid[X] = true;
	    actualBoundsInvalid[Y] = true;
	    actualBoundsInvalid[WIDTH] = true;
	    actualBoundsInvalid[HEIGHT] = true;
	    cachedWidth = INVALID_SIZE;
	    hasFocus = false;
	}
    }

    /**
     * Return whether the cached requested sizes are valid.
     *
     * @return <code>true</code> if the cached requested sizes are up to date.
     *	       <code>false</code> if they have been invalidated.
     */
    public final boolean isRequestedSizesValid() {
	    return (cachedWidth != INVALID_SIZE);
    }

    // *****************************************************
    //  Package private methods
    // *****************************************************

    /**
     * Create native resource of this <code>Item</code>.
     * After this call, <code>Item.nativeId</code> should have had the
     * created native resource id.
     *
     * @param ownerId native <code>MidpDisplayable*</code> of the owner screen
     */
    abstract void createNativeResource(int ownerId);

    /**
     * Delete native resource of this <code>Item</code>.
     * After this call, <code>Item.nativeId</code> should have been reset to
     * <code>DisplayableLFImpl.INVALID_NATIVE_ID</code>.
     */
    void deleteNativeResource() {
	if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
	    delete0(nativeId);
	    nativeId = DisplayableLFImpl.INVALID_NATIVE_ID;
	}
    }

    /**
     * Create a temporary native resource of this <code>Item</code> whose
     * id must be deleted before caller function returns.
     */
    protected void createTempNativeResource() {

        int ownerId = (item.owner == null)
			    ? DisplayableLFImpl.INVALID_NATIVE_ID
			    : ((DisplayableLFImpl)item.owner.getLF()).nativeId;

	createNativeResource(ownerId);
    }

    /**
     * Paint the content of this <code>Item</code>.
     * This function simply calls lCallPaint() after obtaining LCDUILock.
     *
     * @param g the <code>Graphics</code> object to be used for rendering
     *          the item
     * @param w current width of the item in pixels
     * @param h current height of the item in pixels
     */
    void uCallPaint(Graphics g, int w, int h) {
	synchronized (Display.LCDUILock) {
	    lCallPaint(g, w, h);
	}
    }

    /**
     * Paint the content of this <code>Item</code>.
     * The default implementation does nothing.
     *
     * @param g the <code>Graphics</code> object to be used for rendering
     *          the item
     * @param w current width of the item in pixels
     * @param h current height of the item in pixels
     */
    void lCallPaint(Graphics g, int w, int h) {}

    /**
     * Determine if this <code>Item</code> should horizontally shrink.
     *
     * @return <code>true</code> if it should horizontally shrink
     */
    boolean shouldHShrink() {
        return ((item.layout & Item.LAYOUT_SHRINK) == Item.LAYOUT_SHRINK);
    }

    /**
     * Determine if this <code>Item</code> should horizontally expand.
     *
     * @return <code>true</code> if it should horizontally expand
     */
    boolean shouldHExpand() {
        return ((item.layout & Item.LAYOUT_EXPAND) == Item.LAYOUT_EXPAND);
    }

    /**
     * Determine if this <code>Item</code> should vertically shrink.
     *
     * @return <code>true</code> if it should vertically shrink
     */
    boolean shouldVShrink() {
        return ((item.layout & Item.LAYOUT_VSHRINK) == Item.LAYOUT_VSHRINK);
    }

    /**
     * Determine if this <code>Item</code> should vertically expand.
     *
     * @return <code>true</code> if it should vertically expand
     */
    boolean shouldVExpand() {
        return ((item.layout & Item.LAYOUT_VEXPAND) == Item.LAYOUT_VEXPAND);
    }

    /**
     * Determine if this <code>Item</code> should have a newline after it.
     *
     * @return <code>true</code> if it should have a newline after
     */
    boolean equateNLA() {
        return ((item.layout & Item.LAYOUT_NEWLINE_AFTER) ==
                Item.LAYOUT_NEWLINE_AFTER);
    }

    /**
     * Determine if this <code>Item</code> should have a newline before it.
     *
     * @return <code>true</code> if it should have a newline before
     */
    boolean equateNLB() {
        return ((item.layout & Item.LAYOUT_NEWLINE_BEFORE) ==
                Item.LAYOUT_NEWLINE_BEFORE);
    }

    /**
     * Get the effective layout type of this <code>Item</code>.
     *
     * @return layout The translated layout type.
     */
    int getLayout() {
        int l = item.layout;
        if (l == Item.LAYOUT_DEFAULT) {
	    // the spec requires the default vertical layout to be bottom
            return Item.LAYOUT_BOTTOM;
        } else {
            return l;
        }
    }

    /**
     * Called by the system to notify internal traverse into the item.
     * By default, it always returns false and should be overriden for
     * items with enabled internal traversal
     *
     * @param dir the direction of traversal
     * @param viewportWidth the width of the container's viewport
     * @param viewportHeight the height of the container's viewport
     * @param visRect_inout passes the visible rectangle into the method, and
     *                      returns the updated traversal rectangle from the
     *                      method
     *
     * @return <code>true</code> if focus ius accepted by the item
     *         <code>false</code> if traversal should proceed out
     *
     * @see #getInteractionModes
     * @see #traverseOut
     * @see #TRAVERSE_HORIZONTAL
     * @see #TRAVERSE_VERTICAL
     */
    boolean uCallTraverse(int dir, int viewportWidth, int viewportHeight,
                          int[] visRect_inout) {
        boolean ret = false;
        synchronized (Display.LCDUILock) {
            if (!hasFocus || dir == CustomItem.NONE) {
                if (ret = item.acceptFocus()) {
                    hasFocus = true;
                }
            } 
        }
        return ret;
    }

        
    /**
     * Called by the system to indicate traversal has left this 
     * <code>Item</code>.
     * This function simply calls lCallTraverseOut after obtaining LCDUILock.
     *
     * @see #getInteractionModes
     * @see #traverse
     * @see #TRAVERSE_HORIZONTAL
     * @see #TRAVERSE_VERTICAL
     */
    void uCallTraverseOut() {
        synchronized (Display.LCDUILock) {
            lCallTraverseOut();
        }
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
    void lCallTraverseOut() {
        hasFocus = false;
    }

    /**
     * Initialize native resource - size and location.
     */
    void initNativeResource() {
    if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
	    if (!actualBoundsInvalid[WIDTH] && !actualBoundsInvalid[HEIGHT]) {
		setSize0(nativeId, bounds[WIDTH], bounds[HEIGHT]);
	    }
	    if (!actualBoundsInvalid[X] && !actualBoundsInvalid[Y]) {
		setLocation0(nativeId, bounds[X], bounds[Y]); 
	    }
	}
    }

    /**
     * Called by the system to show this <code>Item</code>'s
     * native resource.
     *
     * <p>The default implementation of this method shows
     * the native resource corresponding to this <code>Item</code>
     * 
     * <p>SYNC NOTE: 
     */
    void lShowNativeResource() {
	if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
	    show0(nativeId);
	}
    }

    /**
     * Called by the system to hide this <code>Item</code>'s
     * native resource.
     *
     * <p>The default implementation of this method 
     * hides native resources corresponding to this <code>Item</code>
     */
    void lHideNativeResource() {
	if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
	    hide0(nativeId);
	}
    }

    /**
     * Sets the visibleInViewport flag of this Item to be true. 
     * Note that this method has no effect on the native resource.
     * This function simply calls lCallShowNotify() after obtaining LCDUILock.
     */
    void uCallShowNotify() {
	synchronized (Display.LCDUILock) {
	    lCallShowNotify();
	}
    }

    /**
     * Sets the visibleInViewport flag of this Item to be true. 
     * Note that this method has no effect on the native resource.
     */
    void lCallShowNotify() {
	this.visibleInViewport = true;
    }

    /**
     * Sets the visibleInViewport flag to be false. 
     * Note the native resource is not hidden, nor deleted.
     * This function simply calls lCallShowNotify() after obtaining LCDUILock.
     */
    void uCallHideNotify() {
	synchronized (Display.LCDUILock) {
	    lCallHideNotify();
	}
    }
    /**
     * Sets the visibleInViewport flag to be false. 
     * Note the native resource is not hidden, nor deleted.
     */
    void lCallHideNotify() {
	this.visibleInViewport = false;
    }

    /**
     * Called by event delivery to notify an <code>ItemLF</code> in current 
     * <code>FormLF</code> of a change in its peer state.
     * Subclass should implement this function to sync its state with
     * the peer and choose to return proper value to let <code>Form</code> know
     * whether the itemStateListener should be notified.
     *
     * @param hint some value that is interpreted only between the peers
     *
     * @return <code>true</code> if internal state has changed and 
     *         <code>ItemStateListener</code> should be notified after 
     *         this function returns.
     */
    abstract boolean uCallPeerStateChanged(int hint);

    /**
     * Called by the system to signal a key press.
     *
     * @param keyCode the key code of the key that has been pressed
     *
     * @see #getInteractionModes
     */
    void uCallKeyPressed(int keyCode) { }
    
    /**
     * Called by the system to signal a key release.
     *
     * @param keyCode the key code of the key that has been released
     *
     * @see #getInteractionModes
     */
    void uCallKeyReleased(int keyCode) { }
    
    /**
     * Called by the system to signal a key repeat.
     *
     * @param keyCode the key code of the key that has been repeated
     *
     * @see #getInteractionModes
     */
    void uCallKeyRepeated(int keyCode) { }
    
    /**
     * Called by the system to signal a pointer press.
     *
     * @param x the x coordinate of the pointer down
     * @param y the y coordinate of the pointer down
     *
     * @see #getInteractionModes
     */
    void uCallPointerPressed(int x, int y) { }
    
    /**
     * Called by the system to signal a pointer release.
     *
     * @param x the x coordinate of the pointer up
     * @param y the x coordinate of the pointer up
     *
     * @see #getInteractionModes
     */
    void uCallPointerReleased(int x, int y) {}
    
    /**
     * Called by the system to signal a pointer drag.
     *
     * @param x the x coordinate of the pointer drag
     * @param y the x coordinate of the pointer drag
     *
     * @see #getInteractionModes
     */
    void uCallPointerDragged(int x, int y) { }
    
    /**
     * Called by the system to indicate the size available to this 
     * <code>Item</code> has changed.
     *
     * @param w the new width of the item's content area
     * @param h the new height of the item's content area
     */
    void uCallSizeChanged(int w, int h) {
        synchronized (Display.LCDUILock) {
            item.lUpdateLockedSize();
        }
 }
    
    /**
     * Called to commit any pending user interaction for the item.
     */
    public void lCommitPendingInteraction() { }

    /**
     * Sets item's location.
     *
     * @param x the new x location in form's content coordinate system
     * @param y the new y location in form's content coordinate system
     */
    void lSetLocation(int x, int y) {

	bounds[X] = x;
	bounds[Y] = y;

	if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
	    setLocation0(nativeId, x, y);
	}
    }

    /**
     * Sets item's size.
     * 
     * @param w the new width of the item
     * @param h the new height of the item
     */
    void lSetSize(int w, int h) {

	if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
	    Logging.report(Logging.INFORMATION,
			   LogChannels.LC_HIGHUI_ITEM_LAYOUT,
			   " ItemLFImpl  -  lSetSize: " + w + "x" + h);
	}

	if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
	    if (w < minimumWidth) {
		Logging.report(Logging.INFORMATION,
			       LogChannels.LC_HIGHUI_ITEM_LAYOUT,
			       " ******* ItemLFImpl  -  lSetSize: " + w +
			       " < " + minimumWidth);
	    }
	}
        bounds[WIDTH] = w;
        bounds[HEIGHT] = h;

	if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
	    setSize0(nativeId, w, h);
	}
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
        return null;
    }

    /**
     * Moves item's location by delta. Both deltaX and deltaY can be
     * positive and negative.
     *
     * @param deltaX the amount of pixels by which x item's location
     *               has to be moved
     * @param deltaY the amount of pixels by which y item's location
     *               has to be moved
     */
    void lMove(int deltaX, int deltaY) {

        bounds[X] += deltaX;
        bounds[Y] += deltaY;

	if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
	    setLocation0(nativeId, bounds[X], bounds[Y]);
	}
    }

    /**
     * Request the event scheduler to schedule an invalidate event, that
     * eventually will call uCallInvalidate(this item) of this item's 
     * <code>DisplayableLFImpl</code>.
     * If caller also needs to call lSetRequestedSizes, then it must be
     * called AFTER this function, to have effect.
     *
     * @param width <code>true</code> if it was changed
     * @param height <code>true</code> if it was changed
     */
    void lRequestInvalidate(boolean width, boolean height) {

        actualBoundsInvalid[WIDTH] = actualBoundsInvalid[WIDTH] || width;
        actualBoundsInvalid[HEIGHT] = actualBoundsInvalid[HEIGHT] || height;

	if (width || height) {
	    cachedWidth = INVALID_SIZE;
	}
	
	// if native resource is not visible we still need 
	// process invalidate to proper set the sizes of this ItemLF
	// and of form container as well
        if (item.owner != null) {
	    ((DisplayableLFImpl)item.owner.getLF()).lRequestInvalidate();
        }
    }

    /**
     * Set minimum and preferred width and height of this item.
     * This function should only be called on a <code>CustomItemLFImpl</code>
     * or <code>SpacerLFImpl</code>. 
     * They will collect its own sizing, then call this function to cache them.
     *
     * @param mw minimum width
     * @param mh minimum height
     * @param pw preferred width
     * @param ph preferred height
     */
    final void lSetRequestedSizes(int mw, int mh, int pw, int ph) {
	// ASSERT (this instanceof CustomItemLFImpl or SpacerLFImpl)
	minimumWidth = mw;
	minimumHeight = mh;
	preferredWidth = pw;
	preferredHeight = ph;
	cachedWidth= pw;

	if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
	    Logging.report(Logging.INFORMATION,
			   LogChannels.LC_HIGHUI_ITEM_LAYOUT,
			   " lSetRequestedSizes: " +
			   " mw=" + minimumWidth + " mh=" + minimumHeight +
			   " pw=" + preferredWidth + " ph=" + preferredHeight);
	}

    }

    /**
     * Calculate minimum and preferred width and height of this item and 
     * store the result in instance variables:
     * minimumWidth, minimumHeight, preferredWidth and preferredHeight.
     * This function will query native resources for the sizes. For 
     * <code>Item</code>s like <code>CustomItem</code> and 
     * <code>Spacer</code>, whose sizes are only calculated in Java, this
     * function should be overriden to perform their own calculation.
     */
    void lGetRequestedSizes() {

	if (cachedWidth != INVALID_SIZE) {
	    return;
	}

	// query native for real preferred sizes
	boolean wasNoNative = (nativeId == 
			       DisplayableLFImpl.INVALID_NATIVE_ID);

	// Native resource not yet created, create a temporary one
	if (wasNoNative) {
	    createTempNativeResource();
	}

	minimumWidth = getMinimumWidth0(nativeId);
	// ASSERT (ownerLFImpl.width 
	//         >= minimumWidth >= 0)
	
	minimumHeight = getMinimumHeight0(nativeId);
	// ASSERT (minimumHeight >= 0)
	
	// NOTE: When the item should shrink, the minimum size is used,
	// and the minimum size is calculated with the label
	// on the same line
	if (shouldHShrink() && item.label != null && item.label.length() > 0) {
	    minimumHeight += DEFAULT_LABEL_HEIGHT;
	}
	
	preferredWidth = getPreferredWidth0(nativeId, item.lockedHeight);
	if (preferredWidth < minimumWidth) {
	    preferredWidth = minimumWidth;
	}

	// Since we don't support horizontal scrolling, 
	// we limit the width before using it to query for preferredHeight
        if (item.owner != null) {

	    DisplayableLFImpl ownerLFImpl = 
		(DisplayableLFImpl)item.owner.getLF();

	    // Right now the space available for items is
	    // always limited by the scrollbar space (see first 
	    // FormLFImpl constructor)
	    
	    // The exception of that rule is List and TextBox
	    // implementations that use FormLFImpl 
	    // (notice that in the second constructor the width
	    // is not limited by scrollbar)
	    // But when a native peer cannot support vertical
	    // scrolling and has to be longer than the viewport
	    // FormLFImpl needs to provide its native scrollbar
	    // To make sure that happens we do the following check.
	    // That check must be removed if FormLFImpl supports
	    // dynamic change of the width available for layout.
	    if (ownerLFImpl.width == ownerLFImpl.currentDisplay.width &&
		minimumHeight > ownerLFImpl.currentDisplay.height) {
	    
		ownerLFImpl.width -= Constants.VERT_SCROLLBAR_WIDTH;
	    }

	    if (preferredWidth > ownerLFImpl.width) {
		preferredWidth = ownerLFImpl.width;
	    }

	} else {
	    if (preferredWidth > 
		Display.WIDTH - Constants.VERT_SCROLLBAR_WIDTH) {
		preferredWidth = 
		    Display.WIDTH - Constants.VERT_SCROLLBAR_WIDTH;
	    }
	}

	preferredHeight = getPreferredHeight0(nativeId,
					      (item.lockedWidth == -1)
					      ? preferredWidth
					      : item.lockedWidth);

	if (preferredHeight < minimumHeight) {
	    preferredHeight = minimumHeight;
	}

	// Delete temporary native resource
	if (wasNoNative) {
	    deleteNativeResource();
	}

	cachedWidth = preferredWidth;
	
	if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
	    Logging.report(Logging.INFORMATION,
			   LogChannels.LC_HIGHUI_ITEM_LAYOUT,
			   " lGetRequestedSizes: " +
			   " mw=" + minimumWidth + " mh=" + minimumHeight +
			   " pw=" + preferredWidth + " ph=" + preferredHeight);
	}
    }

    /**
     * Called by subclasses to repaint this entire <code>Item</code>'s bounds.
     */
    void lRequestPaint() {
        if (bounds != null) {
            lRequestPaint(0, 0, bounds[WIDTH], bounds[HEIGHT]);
        }
    }
    
    /**
     * Called by subclasses to repaint a portion of this <code>Item</code>'s 
     * bounds.
     *
     * @param x the x coordinate of the origin of the dirty region
     * @param y the y coordinate of the origin of the dirty region
     * @param w the width of the dirty region
     * @param h the height of the dirty region
     */
    void lRequestPaint(int x, int y, int w, int h) {

	if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
	    Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
			   "repaint "+this+" \t owner ="+item.owner);
	}

        if (item.owner != null) {
            if (w < 0) {
                return;
            } else if (w > bounds[WIDTH]) {
		w = bounds[WIDTH];
            }
            
            if (h < 0) {
                return;
            } else if (h > bounds[HEIGHT]) {
		h = bounds[HEIGHT];
            }
            
            if (x < 0) {
                x = 0;
            } else if (x > bounds[WIDTH]) {
		return;
            }
            
            if (y < 0) {
                y = 0;
            } else if (y > bounds[HEIGHT]) {
                return;
            }
            
            if (item.owner.getLF() instanceof FormLFImpl) {
                ((FormLFImpl)item.owner.getLF()).lRequestPaintItem(item,
								   x, y, w, h);
            } else if (item.owner.getLF() instanceof AlertLFImpl) {
                // ((AlertLFImpl)item.owner.getLF()).repaintItem(item, 
                //                                               x, y, w, h); 
                ((AlertLFImpl)item.owner.getLF()).lRequestPaintContents();
            }
            
        }
    }
    
    /**
     * Return the current <code>Display</code> instance that this 
     * <code>Item</code> is displayed in.
     *
     * @return the <code>Display</code> instance this <code>Item</code> 
     *         is displayed in
     */
    Display getCurrentDisplay() {
        return (item.owner == null) ? 
	    null : item.owner.getLF().lGetCurrentDisplay();
    }

    /**
     * Makes native widget that corresponds to this <code>Item</code> 
     * visible on the screen at the location set earlier.
     *
     * @param nativeId native resource id of this <code>Item</code>
     */
    private native void show0(int nativeId);
 
    /**
     * Makes native widget that corresponds to this <code>Item</code> 
     * invisible and free native resource. After this call, the nativeId 
     * is no longer valid.
     *
     * @param nativeId native resource id of this <code>Item</code>
     */
    private native void hide0(int nativeId);

    /**
     * Notify native <code>Item</code> to delete its resource.
     *
     * @param nativeId native resource id (<code>MidpItem *</code>) of this 
     *                 <code>Item</code>
     */
    private native void delete0(int nativeId);

    /**
     * Notifies native widget that <code>Form</code> set location of this 
     * <code>Item</code> to be at x, y.
     *
     * @param nativeId native resource id of this <code>Item</code>
     * @param x the new x location of the <code>Item</code>
     * @param y the new y location of the <code>Item</code>
     */
    private native void setLocation0(int nativeId, int x, int y);

    /**
     * Notifies native widget that <code>Form</code> set size of this 
     * <code>Item</code> to be at w and h.
     *
     * @param nativeId native resource id of this <code>Item</code>
     * @param w the new width of the <code>Item</code>
     * @param h the new height of the <code>Item</code>
     */
    private native void setSize0(int nativeId, int w, int h);

    /**
     * Notifies native width that new label was set on this <code>Item</code>.
     *
     * @param nativeId native resource id of this <code>Item</code>
     * @param label the new label set on this <code>Item</code>
     */
    native void setLabel0(int nativeId, String label);

    /**
     * Gets preferred width from the native resource corresponding to 
     * this <code>Item</code>.
     *
     * @param nativeId pointer to the native resource
     * @param h the tentative content height in pixels, 
     *          or <code>-1</code> if a tentative height has not been computed
     *
     * @return preferred width that corresponds to the passed in height
     */
    native int getPreferredWidth0(int nativeId, int h);

    /**
     * Gets preferred height from the native resource corresponding to 
     * this <code>Item</code>.
     *
     * @param nativeId pointer to the native resource
     * @param w the tentative content width in pixels, 
     *          or <code>-1</code> if a tentative width has not been computed
     *
     * @return preferred height that corresponds to the passed in width
     */
    native int getPreferredHeight0(int nativeId, int w);

    /**
     * Gets minimum width from the native resource corresponding to 
     * this <code>Item</code>.
     *
     * @param nativeId pointer to the native resource
     *
     * @return minimum width 
     */
    native int getMinimumWidth0(int nativeId);

    /**
     * Gets minimum height from the native resource corresponding to 
     * this <code>Item</code>.
     *
     * @param nativeId pointer to the native resource
     *
     * @return minimum height 
     */
    native int getMinimumHeight0(int nativeId);

    /** 
     * Bounds[] array index to x coordinate.
     */
    final static int X      = FormLFImpl.X;
    
    /** 
     * Bounds[] array index to y coordinate.
     */
    final static int Y      = FormLFImpl.Y;
    
    /** 
     * Bounds[] array index to width.
     */
    final static int WIDTH  = FormLFImpl.WIDTH;
    
    /** 
     * Bounds[] array index to height.
     */
    final static int HEIGHT = FormLFImpl.HEIGHT;

    /**
     * The default label height for an item.
     */
    static final int DEFAULT_LABEL_HEIGHT = 14;
    
    /**
     * The owner of this view. Set in the constructor.
     */
    Item item; // = null
    
    /**
     * The native <code>MidpItem*</code> of this view.
     * Set by owner screen based on the return value of 
     * createNativeResource().
     */
    int nativeId;
    
    /**
     * This is the size <i>granted</i> to this item by the form layout.
     * An array of 4 elements, describing the x, y, width, height
     * of this <code>Item</code>'s bounds in the viewport coordinate space. If
     * it is <code>null</code>, it means the <code>Item</code> is currently 
     * not in the viewport.
     * This array is set by this.lSetLocation() and this.lSetSize(),
     * and read by <code>FormLFImpl</code> during layout.
     */
    int[] bounds;

    /**
     * A flag indicating this <code>Item</code> has the input focus. This is
     * maintained by the <code>Item</code> superclass for easy use by subclass
     * code.
     */
    boolean hasFocus; // = false

    /**
     * A flag indicating the size of this <code>Item</code> has changed in a
     * subsequent layout operation.
     * This variable is set and read by <code>FormLFImpl</code> during layout.
     */
    boolean sizeChanged; // = false

    /**
     * A flag indicating the native resource of this 
     * <code>Item</code> is currently (at least 
     * partially) visible on the <code>Form</code> it is on. 
     * This is maintained by the <code>Item</code> superclass for easy 
     * use by subclass code.
     */
    boolean visibleInViewport; // = false

    /**
     * Flags that are set to true when calling invalidate() on this item.
     * If it is known that one of the dimensions remains the same, then
     * the responding invalid flag will stay <code>false</code>.
     * It will turn to <code>false</code> after it gets laid out.
     * This is set by <code>Item</code> class and read by 
     * <code>FormLFImpl</code> during layout.
     */
    boolean[] actualBoundsInvalid;
    
    /**
     * Cached preferred height when validRequestedSizes is <code>false</code>.
     */
    private int preferredHeight;

    /**
     * Cached preferred width when validRequestedSizes is <code>false</code>.
     */
    private int preferredWidth;

    /**
     * Cached minimum height when validRequestedSizes is <code>false</code>.
     */
    private int minimumHeight;

    /**
     * Cached minimum width when validRequestedSizes is <code>false</code>.
     */
    private int minimumWidth;

    /**
     * If set to true rely on native scrolling mechanism of this item
     * and give this item all width and height available.
     * (Used for TextBox and List implementation).
     */
    boolean supportsNativeScrolling; // = false
    /**
     * This flag marks if the last layout performed placed
     * this <code>Item</code> at the beginning of a line.
     * This variable is set and read by <code>FormLFImpl</code> during
     * layout.
     */
    boolean isNewLine = false;

    /**
     * Marks the height of the row this <code>Item</code> is placed on.
     * Note that the <code>Item</code> may be shorter than the height of 
     * the row.
     * This variable is set and read by <code>FormLFImpl</code> during layout.
     */
    int rowHeight = 0;

    /**
     * A constant used to indicate that Item sizes have to be recalculated.
     */
    static final int INVALID_SIZE = -1;

    /**
     * Width used for last calculations. If that indicates if the preferredHeight, preferredWidth,
     * minimumHeight, minimumWidth are valid, or need a new query.
     */
    int cachedWidth = INVALID_SIZE;
}
