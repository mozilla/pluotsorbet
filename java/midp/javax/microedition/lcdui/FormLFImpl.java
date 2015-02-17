/*
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
 * Look and feel class for <code>Form</code>.
 */
class FormLFImpl extends DisplayableLFImpl implements FormLF {
    /**
     * Creates <code>FormLF</code> associated with passed in form.
     * <code>FormLFImpl</code> maintains an array of views associated
     * with its items.
     *
     * @param form the <code>Form</code> object associated with this
     *             <code>FormLF</code>
     * @param items the array of Items using which the passed in
     *              <code>Form</code> was created
     * @param numOfItems current number of elements
     */
    FormLFImpl(Form form, Item items[], int numOfItems) {
        super(form);

        // Initialize the in-out rect for Item traversal
        visRect = new int[4];

        width -= Constants.VERT_SCROLLBAR_WIDTH;

        if (items == null) {
            itemLFs = new ItemLFImpl[GROW_SIZE];
            // numOfLFs was initialized to 0
            // so there is no need to update it
        } else {
            this.itemLFs = new ItemLFImpl[items.length > GROW_SIZE ?
                                         items.length : GROW_SIZE];

            for (int i = 0; i < numOfItems; i++) {
                itemLFs[i] = (ItemLFImpl)items[i].getLF();
            }

            // right now we have the same number of views as items
            numOfLFs = numOfItems;
        }
    }

    /**
     * Creates <code>FormLF</code> for the passed in screen.
     * Passed in <code>ItemLF</code> is added as the only itemLF present.
     * This constructor is used by <code>List</code> and <code>TextBox</code>.
     *
     * @param screen the <code>Screen</code> object associated with this
     *               <code>FormLFImpl</code>
     * @param item the <code>Item</code> to be added to this screen
     */
    FormLFImpl(Screen screen, Item item) {
        super(screen);

        itemLFs = new ItemLFImpl[1];
        itemLFs[0] = (ItemLFImpl)item.getLF();
        numOfLFs = 1;

        // Initialize the in-out rect for Item traversal
        visRect = new int[4];
    }

    // ************************************************************
    //  public methods - FormLF interface implementation
    // ************************************************************

    /**
     * Returns the width in pixels of the displayable area available for
     * items.
     * The value may depend on how the device uses the screen and may be
     * affected by the presence or absence of the ticker, title,
     * or commands.
     * The <code>Item</code>s of the <code>Form</code> are
     * laid out to fit within this width.
     *
     * @return the width of the <code>Form</code> in pixels
     */
    public int lGetWidth() {
	return width;
    }

    /**
     * Returns the height in pixels of the displayable area available
     * for items.
     * This value is the height of the form that can be displayed without
     * scrolling.
     * The value may depend on how the device uses the screen and may be
     * affected by the presence or absence of the ticker, title,
     * or commands.
     *
     * @return the height of the displayable area of the
     *         <code>Form</code> in pixels
     */
    public int lGetHeight() {
	return height;
    }

    /**
     * Set the current traversal location to the given <code>Item</code>.
     * This call has no effect if the given <code>Item</code> is the
     * current traversal item, or if the given <code>Item</code> is not
     * part of this <code>Form</code>. Note that null can be passed in
     * clear the previously set current item.
     *
     * @param item the <code>Item</code> to make the current traversal item
     */
    public void uItemMakeVisible(Item i) {

        synchronized (Display.LCDUILock) {

            if (i == null) {
                pendingCurrentItem = null;
            }

            /**
             * Display could be made visible using display.setCurrentItem()
             * call. In those cases foregroung will be granted after there
             * there is a screen change event and after uItemMakeVisible()
             * is called. In such cases we need to set pendingCurrentItem and
             * call uItemMakeVisible() again when the FormLF changes its
             * state to SHOWN.
             */
            if (state != SHOWN) {
                pendingCurrentItem = i;
                return;
            }
        }
    }

    /**
     * Notifies look&amp;feel object of an item set in the corresponding
     * <code>Form</code>.
     *
     * @param itemNum the index of the item set
     * @param item the item set in the corresponding <code>Form</code>
     */
    public void lSet(int itemNum, Item item) {

        itemLFs[itemNum] = (ItemLFImpl)item.getLF();
        itemsModified = true;

        // Focus index remains at the same location

        lRequestInvalidate();
    }

    /**
     * Notifies look&amp;feel object of an item inserted in the corresponding
     * <code>Form</code>.
     *
     * @param itemNum the index of the inserted item
     * @param item the item inserted in the corresponding <code>Form</code>
     */
    public void lInsert(int itemNum, Item item) {
        if (itemLFs.length == numOfLFs) {
            ItemLFImpl newItemLFs[] =
                new ItemLFImpl[numOfLFs + GROW_SIZE];
            System.arraycopy(itemLFs, 0, newItemLFs, 0, itemNum);
            System.arraycopy(itemLFs, itemNum, newItemLFs, itemNum + 1,
                             numOfLFs - itemNum);
            itemLFs = newItemLFs;
        } else {
            // if we're not appending
            if (itemNum != numOfLFs) {
                System.arraycopy(itemLFs, itemNum, itemLFs, itemNum + 1,
                                 numOfLFs - itemNum);
            }
        }

        itemLFs[itemNum]  = (ItemLFImpl)item.getLF();

        numOfLFs++;
        itemsModified = true;
        // Focus remains on the same item
        if (traverseIndex >= itemNum) {
            traverseIndex++;
        } else if (traverseIndex == -1) {
            traverseIndex = itemNum;
        }
        
        lRequestInvalidate();
    }

    /**
     * Notifies look&amp;feel object of an item deleted in the corresponding
     * <code>Form</code>.
     *
     * @param itemNum the index of the deleted item
     * @param deleteditem the item deleted in the corresponding form
     */
    public void lDelete(int itemNum, Item deleteditem) {

        // if the previous item has new line after, or the next item has
        // new line before, and it's not the last item,
        // than we could just mark the next item as actualBoundsInvalid[Y]
        if (itemNum < (numOfLFs-1)) {
            if (((itemNum > 0) && (itemLFs[itemNum-1].equateNLA()) ||
		 itemLFs[itemNum+1].equateNLB()) &&
                itemLFs[itemNum+1].isNewLine) {

		if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
		    Logging.report(Logging.INFORMATION,
				   LogChannels.LC_HIGHUI_FORM_LAYOUT,
				   " setting actualBoundsInvalid[Y] #" +
				   (itemNum + 1));
		    if (itemNum > 0) {
			Logging.report(Logging.INFORMATION,
				       LogChannels.LC_HIGHUI_FORM_LAYOUT,
				       " | itemLFs[itemNum-1] = "+
				       itemLFs[itemNum - 1]);
		    }
		    Logging.report(Logging.INFORMATION,
				   LogChannels.LC_HIGHUI_FORM_LAYOUT,
				   " | itemLFs[itemNum] = " +
				   itemLFs[itemNum]);
                    if (itemNum < numOfLFs - 1) {
			Logging.report(Logging.INFORMATION,
				       LogChannels.LC_HIGHUI_FORM_LAYOUT,
				       " | itemLFs[itemNum+1] = " +
				       itemLFs[itemNum+1]);
		    }
		}
                itemLFs[itemNum+1].actualBoundsInvalid[Y] = true;
            } else {
                itemLFs[itemNum+1].actualBoundsInvalid[X] = true;
            }
        }

        if (traverseIndex == itemNum) {
            lastTraverseItem = itemLFs[traverseIndex];
        }

        numOfLFs--;
        itemsModified = true;

        if (traverseIndex > 0 && traverseIndex >= itemNum) {
             traverseIndex--;
         } else if (0 == numOfLFs) {
             traverseIndex = -1;
         }

        if (itemNum < numOfLFs) {
            System.arraycopy(itemLFs, itemNum + 1, itemLFs, itemNum,
                             numOfLFs - itemNum);
        }

        // Delete reference to the last item view
        // that was left after array copy
        itemLFs[numOfLFs] = null;

        if (pendingCurrentItem == deleteditem) {
            pendingCurrentItem = null;
        }

        lRequestInvalidate();
    }

    /**
     * Notifies look&amp;feel object that all items are deleted in
     * the corresponding <code>Form</code>.
     */
    public void lDeleteAll() {
        if (traverseIndex != -1) {
            lastTraverseItem = itemLFs[traverseIndex];
        }
        // Dereference all ItemLFImpls so they can be GC'ed
        while (numOfLFs > 0) {
            itemLFs[--numOfLFs] = null;
        }
        traverseIndex = -1;
        itemsModified = true;
        pendingCurrentItem = null;
        lRequestInvalidate();
    }

    /**
     * This method is responsible for:
     * (1) Re-validate the contents of this <code>Form</code>, possibly due
     * to an individual item
     * (2) setup the viewable/scroll position
     * (3) repaint the currently visible <code>Item</code>s
     */
    public void uCallInvalidate() {

        super.uCallInvalidate();

        int new_width = currentDisplay.width; 
        int new_height = currentDisplay.height; 

        // It could be that setCurrentItem() was called and we
        // have done an 'artificial' traversal. In this case, we
        // manually call traverseOut() on the last traversed item
        // if there is one.

        if (lastTraverseItem != null) {
            try {
                lastTraverseItem.uCallTraverseOut();                
            } catch (Throwable t) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_HIGHUI,
                                  "Throwable while traversing out");
                }
            }
            lastTraverseItem = null;
            updateCommandSet();
        }

        synchronized (Display.LCDUILock) {
            // Do nothing if paint is suspended in current Display
            if (!lIsShown()) {
                return;
            }
            // Do not reset the Form from the top since this is an update
            resetToTop = false;
        }

        // Setup items and show form native resource
        // SYNC NOTE: 
        // called without LCDUILock, since it may end up calling into a MIDlet
        if (new_width != width || new_height != height) {
            width = new_width;
            height = new_height;
            firstShown = true;
        }
        // IMPL NOTES: Remove this line after UDPATE_LAYOUT is fixed
        firstShown = true;
        // Update contents
        uShowContents(false);

        // SYNC NOTE:
        // 1. We are on event dispatch thread, currentDisplay won't change.
        // 2. We are on event dispatch thread, call paint synchronously.
        // 3. Since we could call into app's functions, like traverse(),
        //    showNotify() and paint(), do this outside LCDUILock block.
        currentDisplay.callPaint(0, 0, width, height, null);        
    }

    /**
     * Paint the contents of this <code>Form</code>.
     *
     * @param g the <code>Graphics</code> object to paint on
     * @param target the target Object of this repaint
     */
    public void uCallPaint(Graphics g, Object target) {
        int count;
        synchronized (Display.LCDUILock) {
            // super.lCallPaint(g, target); -- obsolete

            if (numOfLFs == 0) {
                return;
            }

            // SYNC NOTE: since we may call into CustomItem.paint(),
            // we have to do it outside LCDUILock. So make a copy of the
            // itemLFs array.
            if (target instanceof Item) {
                if (((Item)target).owner == this.owner) {
                    ensureDispatchItemArray(1);
                    dispatchItemLFs[0] = (ItemLFImpl)((Item)target).itemLF;
                    count = 1;
                } else {
                    count = 0;
                }
            } else {
                ensureDispatchItemArray(numOfLFs);
                System.arraycopy(itemLFs, 0, dispatchItemLFs, 0, numOfLFs);
                count = numOfLFs;
            }
        }

        // Call paint on the copied itemLFs array
        for (int i = 0; i < count; i++) {
            uPaintItem(dispatchItemLFs[i], g);
        }

        // Dereference ItemLFImpl objects in dispatchItemLFs
        // But leave the shrinking to uCallHide
        resetDispatchItemArray(false);
    }

    /**
     * Notify this <code>Form</code> that it is being shown.
     */
    public void uCallShow() {
        // Create native resources with title and ticker
        super.uCallShow();
        // Setup items and show form native resource
        // SYNC NOTE: May call into app code to collect sizes.
        // Call it outside LCDUILock
        uShowContents(true);

        synchronized (Display.LCDUILock) {
           
           if (pendingCurrentItem != null) {
              lScrollToItem(pendingCurrentItem);
              pendingCurrentItem = null;
           }
        }
    }

    /**
     * Notify this <code>Form</code> that it is being hidden.
     */

    public void uCallHide() {

        synchronized (Display.LCDUILock) {
            pendingCurrentItem = null;
        } 

        uCallItemHide();
	    // Delete Form's native resource including title and ticker
	    super.uCallHide();
    }

    /**
     * Notify this <code>Form</code> that it is being frozen.
     */

    public void uCallFreeze() {

        if (state == SHOWN) {
            resetToTop = false;
        }
        uCallItemHide();
        // Delete Form's native resource including title and ticker
        super.uCallFreeze();
    }


    /**
     * Hide items when Form is frozen or hidden
     */
   void uCallItemHide() {
        // No more than one custom item can be in focus at a time
        ItemLFImpl customItemToTraverseOut = null;
        ItemLFImpl[] itemsCopy = null;
        int count = 0;

        synchronized (Display.LCDUILock) {

            // We need to loop through our Items to identify those
            // that traverseOut and hideNotify need to be called.
            //
            // SYNC NOTE:
            // We cannot call into app code while holding LCDUILock.
            // For CustomItems, we postpone calls to outside this
            // sync. block.

            itemsCopy = new ItemLFImpl[numOfLFs];

            for (int x = 0; x < numOfLFs; x++) {
                try {
                   // callTraverseOut needs to happen on the item in focus
                    if (itemLFs[x].hasFocus) {
                        if (itemLFs[x] instanceof CustomItemLFImpl) {
                            customItemToTraverseOut = itemLFs[x];
                        } else {
                            // SYNC NOTE: Items other than CustomItem do not
                            // call into app code in their traverseOut.
                            // We can call it while holding the LCDUILock.
                            itemLFs[x].uCallTraverseOut();
                        }
                    }

                    itemLFs[x].lHideNativeResource();
                    // Free native resource of each ItemLF
                    itemLFs[x].deleteNativeResource();


                    // Items that are visible in the viewport
                    // should set their visibleInViewport flag to false and
                    // CustomItems should call app's hideNotify() as well
                    if (itemLFs[x].visibleInViewport) {
                        if (itemLFs[x] instanceof CustomItemLFImpl) {
                            // Remember it in temporary array
                            itemsCopy[count++] = itemLFs[x];
                        } else {
                            itemLFs[x].lCallHideNotify();
                        }
                    }

                } catch (Throwable t) {
                    // do nothing... move on
                }
            }

        } // synchronized

        // Call CustomItem traverseOut outside LCDUILock
        if (customItemToTraverseOut != null) {
            customItemToTraverseOut.uCallTraverseOut();
        }

        // Call CustomItem hideNotify outside LCDUILock
        for (count--; count >= 0; count--) {
            itemsCopy[count].uCallHideNotify();
        }
    }

    /**
     * Called by <code>Display</code> to notify an <code>ItemLF</code>
     * in current <code>FormLF</code> of a change in its peer state.
     * If the the peerId matches the nativeId of this <code>FormLF</code>,
     * uViewportChanged() will be called to process the scroll
     * notification.
     * Otherwise, if there is an <code>ItemLF</code> that matches the peerId,
     * the <code>ItemLF</code> will be called to process this notification.
     * Otherwise, this is treated as a special notification to this
     * <code>FormLF</code> upon focus changed between items, and
     * parameter 'hint' will contain the index of the new current
     * <code>ItemLF</code>.
     *
     * @param modelVersion the version of the peer's data model
     * @param subtype the type of event
     * @param peerId one of the following:
     *  <ul> <li> the id of this <code>FormLF</code> if viewport
     *            has changed in the corresponding native resource
     *            of this <code>FormLF</code> 
     *            (current scroll position is passed as hint)
     *            or traverse has been requested by peer
     *       <li> the id of the <code>ItemLF</code> whose peer state
     *            has changed or new focus item in case of focus 
     *            change notification.
     * @param hint some value that is interpreted only between the peers
     */
    public void uCallPeerStateChanged(int modelVersion,
				      int subType, int peerId, int hint) {

	if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
	    Logging.report(Logging.INFORMATION,
			   LogChannels.LC_HIGHUI_FORM_LAYOUT,
			   "-=-=- FormLF: dsPeerStateChanged " +
			   peerId + "/" + hint);
	}

	ItemLFImpl itemLFToNotify = null;

	synchronized (Display.LCDUILock) {
        if (modelVersion != super.modelVersion) {
            return; // model version out of sync, ignore the event
        }

        switch (subType) {
            case PEER_FOCUS_CHANGED:
            case PEER_ITEM_CHANGED:
                itemLFToNotify = id2Item(peerId);
                break;
            case PEER_VIEWPORT_CHANGED:
            case PEER_TRAVERSE_REQUEST:
                if (peerId != nativeId) {
                    return; // invalid peer id
                }
                break;
        }
	}

	// SYNC NOTE: Following calls may end in app code.
	// 	      So do it outside LCDUILock
	switch (subType) {

	case PEER_FOCUS_CHANGED: // Focus notification
        uFocusChanged(itemLFToNotify);
	    break;
            
	case PEER_VIEWPORT_CHANGED: // Scrolling notification
	    // 'hint' is the new viewport position
	    uViewportChanged(hint, hint + viewportHeight);

	    // Spec requires CustomItem's paint() to be called after
	    // its showNotify() is called and before hideNotify()
	    // it is safe to pass null as both parameters
	    // because only CustomItems will be repainted and they
	    // use their own Graphics
	    uCallPaint(null, null);
	    break;

	case PEER_ITEM_CHANGED: // Item peer notification
        if (itemLFToNotify != null &&
        itemLFToNotify.uCallPeerStateChanged(hint)) {
            // Notify the itemStateListener
            owner.uCallItemStateChanged(itemLFToNotify.item);
        }
	    break;

    case PEER_TRAVERSE_REQUEST: // traverse requested
        uTraverse((hint == 1) ? Canvas.RIGHT : Canvas.LEFT);
        break;

	default:
	    // for safety/completeness.
            Logging.report(Logging.WARNING, LogChannels.LC_HIGHUI_FORM_LAYOUT,
                "FormLFImpl: notifyType=" + subType);
	    break;
	}
    }

    /**
     * Update current item index and notify related item of the change.
     * Item specific abstract commands will also be shown.
     *
     * @param newFocus the new item in focus.
     */
    private void uFocusChanged(ItemLFImpl newFocus) {
        ItemLFImpl oldFocus = null;
        synchronized (Display.LCDUILock) {
            pendingCurrentItem = null;
            int focusIndex = item2Index(newFocus); // Could be -1
            if (focusIndex == traverseIndex) {
                oldFocus = newFocus;
            } else {
                oldFocus = traverseIndex >= 0 ? itemLFs[traverseIndex] : null;
                traverseIndex = focusIndex;
            }
        }

        if (oldFocus != newFocus) {
            if (oldFocus != null) {
                oldFocus.uCallTraverseOut();
            }
            if (newFocus != null) {
                itemTraverse = 
                    uCallItemTraverse(newFocus, CustomItem.NONE);
            }
            updateCommandSet();
            // call paint for custom items
            uRequestPaint();
        }
    }

    /**
     * This method is used in repaint, in order to determine the translation
     * of the draw coordinates.
     *
     * @return <code>true</code>, if the scroll responsibility is on the
     *         native platform.
     *         <code>false</code>, if the scroll is done at Java level.
     */
    public boolean uIsScrollNative() {
	// only native form overrides this and returns true
	return true;
    }

    // *****************************************************
    //  Package private methods
    // *****************************************************

    /**
     * Check the key and return true if it's navigation key
     * @param key key code
     * @return true if the key is navigation key false otherwise 
     */
    private boolean isNavigationKey(int key) {
        return key == Canvas.UP ||
            key == Canvas.LEFT ||
            key == Canvas.DOWN ||
            key == Canvas.RIGHT;
    }

    /**
     * Set status of screen rotation
     * @param newStatus
     * @return
     */
    public boolean uSetRotatedStatus (boolean newStatus) {
         boolean status = super.uSetRotatedStatus(newStatus);
         if (status) {
             firstShown = true;
         }
         return status;
     }
    
    /**
     * Handle a key press.
     *
     * @param keyCode the key code of the key which was pressed
     */
    void uCallKeyPressed(int keyCode) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION,
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "got callKeyPressed: " + keyCode);
        }
        int dir = KeyConverter.getGameAction(keyCode);
        if (isNavigationKey(dir)) {
            uTraverse(dir);
        } else {
            ItemLFImpl v = null;
            synchronized (Display.LCDUILock) {
                v = getItemInFocus();
            }
            
            // pass the keypress onto the current item
            if (v != null && v instanceof CustomItemLFImpl) {

                // NOTE: customItem.getInteractionModes() determines
                // the supported events. The Zaurus platform implementation
                // does not support traversal in any direction.
                // if it is desired to support horizontal and/or vertical
                // traversal, than the proper flags must be set accordingly.
            
                // pass all key events to the CustomItem, including arrows
                v.uCallKeyPressed(keyCode);
            }
        }
    }

    /**
     * Handle a key release event.
     *
     * @param keyCode the key which was released
     */
    void uCallKeyReleased(int keyCode) {
	if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
	    Logging.report(Logging.INFORMATION,
			   LogChannels.LC_HIGHUI_FORM_LAYOUT,
			   "got callKeyReleased: " + keyCode);
	}
        
        if (!isNavigationKey(KeyConverter.getGameAction(keyCode))) {
            ItemLFImpl v = null;
            synchronized (Display.LCDUILock) {
                v = getItemInFocus();
            } // synchronized
            
            // SYNC NOTE: formMode can only change as a result of a
            // traversal, which can only occur serially on the event
            // thread, so its safe to use it outside of the lock
            
            if (v != null && v instanceof CustomItemLFImpl) {
                v.uCallKeyReleased(keyCode);
            }
        }
    }

    /**
     * Handle a key repeat.
     *
     * @param keyCode the key code of the key which was repeated
     */
    void uCallKeyRepeated(int keyCode) {
	if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
	    Logging.report(Logging.INFORMATION,
			   LogChannels.LC_HIGHUI_FORM_LAYOUT,
			   "got callKeyRepeated: " + keyCode);
	}
        if (isNavigationKey(KeyConverter.getGameAction(keyCode))) {
            uCallKeyPressed(keyCode);
        } else {
            ItemLFImpl v = null;
            synchronized (Display.LCDUILock) {
                v = getItemInFocus();
            } // synchronized

            // SYNC NOTE: formMode can only change as a result of a
            // traversal, which can only occur serially on the event
            // thread, so its safe to use it outside of the lock

            if (v != null && v instanceof CustomItemLFImpl) {
                v.uCallKeyRepeated(keyCode);
            }
        }
    }

    /**
     * Handle a pointer pressed event.
     *
     * @param x The x coordinate of the press
     * @param y The y coordinate of the press
     */
    void uCallPointerPressed(int x, int y) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION,
                LogChannels.LC_HIGHUI_FORM_LAYOUT,
                "got callPointerPressed: " + x + "," + y);
        }

        ItemLFImpl v = null;

        synchronized (Display.LCDUILock) {

            v = getItemInFocus();

	        // stop here if no current item to handle the key
            if (v == null) {
                return;
            }

        } // synchronized

        // SYNC NOTE: formMode can only change as a result of a
        // traversal, which can only occur serially on the event
        // thread, so its safe to use it outside of the lock

        if (v instanceof CustomItemLFImpl) {
            v.uCallPointerPressed(x, y);
        }
    }

    /**
     * Handle a pointer released event.
     *
     * @param x The x coordinate of the release
     * @param y The y coordinate of the release
     */
    void uCallPointerReleased(int x, int y) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION,
                LogChannels.LC_HIGHUI_FORM_LAYOUT,
                "got callPointerReleased: " + x + "," + y);
        }

        ItemLFImpl v = null;

        synchronized (Display.LCDUILock) {

            v = getItemInFocus();

            // stop here if no current item to handle the key
            if (v == null) {
                return;
            }

        } // synchronized

        // SYNC NOTE: formMode can only change as a result of a
        // traversal, which can only occur serially on the event
        // thread, so its safe to use it outside of the lock

        if (v instanceof CustomItemLFImpl) {
            v.uCallPointerReleased(x, y);
        }
    }

    /**
     * Handle a pointer dragged event.
     *
     * @param x The x coordinate of the drag
     * @param y The y coordinate of the drag
     */
    void uCallPointerDragged(int x, int y) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION,
                LogChannels.LC_HIGHUI_FORM_LAYOUT,
                "got callPointerDragged: " + x + "," + y);
        }

        ItemLFImpl v = null;

        synchronized (Display.LCDUILock) {

            v = getItemInFocus();

            // stop here if no current item to handle the key
            if (v == null) {
                return;
            }

        } // synchronized

        // SYNC NOTE: formMode can only change as a result of a
        // traversal, which can only occur serially on the event
        // thread, so its safe to use it outside of the lock

        if (v instanceof CustomItemLFImpl) {
            v.uCallPointerDragged(x, y);
        }
    }

    /**
     * Gets item currently in focus.
     *
     * @return the item currently in focus in this form;
     *         if there are no items in focus, <code>null</code> is returned
     */
    public Item lGetCurrentItem() {
        ItemLFImpl v = getItemInFocus();

        if (v == null) {
            return null;
        }

        return v.item;
    }

    /**
     * Paint an item.
     *
     * @param itemLF the <code>ItemLFImpl</code> to paint
     * @param g the <code>Graphics</code> object to paint to
     */
    void uPaintItem(ItemLFImpl itemLF, Graphics g) {
        synchronized (Display.LCDUILock) {
            // NOTE: Its possible, that an Item is in an invalid state
            // during a requested repaint. Its ok to simply return,
            // because it means there is a validation event coming on
            // the event thread. When the form re-validates, the Item
            // will be given a proper bounds and will be repainted
            if (itemLF.actualBoundsInvalid[X]
                || itemLF.actualBoundsInvalid[Y]
                || itemLF.actualBoundsInvalid[WIDTH]
                || itemLF.actualBoundsInvalid[HEIGHT]
                || itemLF.nativeId == INVALID_NATIVE_ID) {
                return;
            }
        }
        
        // repaint only visible in viewport items
        if (itemLF.visibleInViewport) {
            // CustomItem uses its own off screen graphics for painting
            // and the rest of the items do not need to repaint
            itemLF.uCallPaint(null,
                itemLF.bounds[WIDTH], itemLF.bounds[HEIGHT]);
        }
    }


    /**
     * Paint an <code>Item</code> contained in this <code>Screen</code>.
     * The <code>Item</code> requests a paint in its own coordinate space.
     * <code>Screen</code> translates those coordinates into the overall
     * coordinate space and schedules the repaint
     *
     * @param item the <code>Item</code> requesting the repaint
     * @param x the x-coordinate of the origin of the dirty region
     * @param y the y-coordinate of the origin of the dirty region
     * @param w the width of the dirty region
     * @param h the height of the dirty region
     */
    void lRequestPaintItem(Item item, int x, int y, int w, int h) {

        ItemLFImpl iLF = (ItemLFImpl)item.getLF();

        lRequestPaint(iLF.bounds[X] + x, iLF.bounds[Y] + y, w, h, item);
    }

    /**
     * Create native resource for this <code>Form</code>.
     * <code>Item</code>s' resources are not created here.
     */
    void createNativeResource() {
        setScrollPosition0(0);    
        nativeId = createNativeResource0(owner.title,
					 owner.ticker == null ?  null
					    : owner.ticker.getString());
    }

    /**
     * Service method - returns the <code>ItemLFImpl</code> that has focus.
     *
     * @return the current <code>ItemLFImpl</code>, or <code>null</code>
     *         if there is no current.
     */
    ItemLFImpl getItemInFocus() {
        if (traverseIndex < 0) {
            return null;
        } else {
            return itemLFs[traverseIndex];
        }
    }

    // ***************************************************************

    /**
     * Scroll to show an <code>Item</code> and give focus to it if possible.
     *
     * @param nativeId native resource id of the <code>Form</code>
     * @param itemId native resource id for the focused <code>Item</code>
     * @param yOffset offset for the y co-ordinate of the
     *                focused <code>Item</code>
     */
    native void setCurrentItem0(int nativeId, int itemId, int yOffset);

    /**
     * Current Y position in a scrollable form.
     *
     * @return current scroll Y position
     */
    native int getScrollPosition0();

    /**
     * Set Y position in a scrollable form.
     *
     */
    native void setScrollPosition0(int pos);

    /**
     * Create the native resource of this <code>Form</code>.
     *
     * @param title the title text of the <code>Form</code>
     * @param tickerText the text of the <code>Ticker</code>,
     *                   <code>Null</code> if no ticker.
     *
     * @return native resource id
     *
     * @exception OutOfMemoryException - if out of native resource
     */
    private native int createNativeResource0(String title, String tickerText);

    /**
     * Populate the native <code>Form</code> with visible <code>ItemLF</code>s
     * and then show.
     *
     * @param nativeId native resource id
     * @param modelVersion initial model version number for this visible period
     * @param w width of the virtual Form without scrolling
     * @param h height of the virtual Form without scrolling
     *
     * @exception OutOfMemoryException - if out of native resource
     */
    private native void showNativeResource0(int nativeId,
					    int modelVersion,
					    int w, int h);

    /**
     * Current viewport height in the native resource
     *
     * @return current viewport height
     */
    private native int getViewportHeight0();

    /**
     * Make sure all items have native resource and
     * all <code>CustomItem</code>s have their minimum and preferred sizes
     * cached.
     */
    private void uEnsureResourceAndRequestedSizes() {
        int i, count = 0;
        ItemLFImpl[] itemsCopy = null;

        synchronized (Display.LCDUILock) {
            if (nativeId == INVALID_NATIVE_ID) {
                return;
            }
            // Make a temporary copy of ItemLFs we need to collect sizes from
            itemsCopy = new ItemLFImpl[numOfLFs];


            // Make sure each Item has native resource
            // and remember all the CustomItemLFImpls
            for (i = 0; i < numOfLFs; i++) {
                if (itemLFs[i].nativeId == INVALID_NATIVE_ID) {
                    itemLFs[i].createNativeResource(super.nativeId);
                    // layout(UPDATE_LAYOUT) later will not call
                    // setSize/setLocation on an ItemLF that has valid bounds
                    // already. But the native resource is recreated
                    // above, we make up these two calls here.
                    itemLFs[i].initNativeResource();
                    // Every native resource is default to be visible in
                    // viewport. It's up to the native container to maintain
                    // viewport.
                    itemLFs[i].lShowNativeResource();
                }
    
                if (itemLFs[i] instanceof CustomItemLFImpl) {
                    // Remember this in temporary array
                    itemsCopy[count++] = itemLFs[i];
                }
            }
        } // synchronized

        // Collect min and preferred sizes from CustomItems
        // SYNC NOTE: This may call into app code like
        // CustomItem.getPrefContentWidth(). So do it outside LCDUILock
        for (i = 0; i < count; i++) {
            ((CustomItemLFImpl)itemsCopy[i]).uCallSizeRefresh();
        }

    }


    /**
     * Show all items and give focus to current item.
     * SYNC NOTE: caller must NOT hold LCDUILock since this function may
     * call into app code like getPrefContentWidth(), sizeChanged or paint()
     * of CustomItem. 
     * @param initialTraverse the flag to indicate this is the initial 
     *               traversal focus setup when this Form is being shown
     */    
    private void uShowContents(boolean initialTraverse) {

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION,
                LogChannels.LC_HIGHUI_FORM_LAYOUT,
                "\nFormLFImpl: showContents()");
        }

        synchronized (Display.LCDUILock) {
            if (firstShown) {
                for (int i = 0; i < numOfLFs; i++) {
                    itemLFs[i].cachedWidth = ItemLFImpl.INVALID_SIZE;
                }
            }
        }

        // Ensure resources for all items and requested sizes for CustomItems
        uEnsureResourceAndRequestedSizes();

        ItemLFImpl[] itemsCopy = null;
        int itemsCopyCount = 0;
        int traverseIndexCopy = -1;

        // Layout
        synchronized (Display.LCDUILock) {
            if (nativeId == INVALID_NATIVE_ID) {
                return;
            }

            if (firstShown) {
                LayoutManager.instance().lLayout(LayoutManager.FULL_LAYOUT,
                    itemLFs,
                    numOfLFs,
                    width,
                    height,
                    viewable);
                firstShown = false;

            } else {
                LayoutManager.instance().lLayout(LayoutManager.UPDATE_LAYOUT,
                    itemLFs,
                    numOfLFs,
                    width,
                    height,
                    viewable);
            }

            if (resetToTop) {
                traverseIndex = -1;
                setScrollPosition0(0);
            } 

            itemsCopy = new ItemLFImpl[numOfLFs];
            itemsCopyCount = numOfLFs;
            System.arraycopy(itemLFs, 0, itemsCopy, 0, numOfLFs);
            traverseIndexCopy = traverseIndex;
            itemsModified = false;

            // Set native Form's window viewable size (logical Form size)
            // and make it shown if not yet
            showNativeResource0(nativeId, modelVersion, width,
                viewable[HEIGHT]);

            // update viewport height
            viewportHeight = getViewportHeight0();

            // correct scroll position if any
            if (viewable[HEIGHT] <= viewportHeight) {
                // if viewable height is less than viewport
                // height just reset viewable y
                setScrollPosition0(0);
            } else if (getScrollPosition0() > (viewable[HEIGHT] - viewportHeight)) {
                // if viewable y exceeds the max value set it to the max
                // height just reset viewable y
                setScrollPosition0(viewable[HEIGHT] - viewportHeight);
            }
            
        } // synchronized

        uInitItemsInViewport(CustomItem.NONE, itemsCopy, traverseIndexCopy);

        if (initialTraverse) {
            updateCommandSet();
        }

        for (int index = 0; index < itemsCopyCount; index++) {
            if (itemsCopy[index].sizeChanged) {
                itemsCopy[index].uCallSizeChanged(itemsCopy[index].bounds[WIDTH],
                        itemsCopy[index].bounds[HEIGHT]);
                itemsCopy[index].sizeChanged = false;
            }
        }
    }

    /**
     * Perform a traversal. This method handles traversal within a
     * "page" after the initial page has been shown via the 
     * uInitItemsInViewport() routine. At the point this method is 
     * called, the following conditions must be true:
     *
     * 1.) There are no interactive items at all on the current page
     * or
     * 2.) There is at least one interactive item on the current page
     *     and the traverseIndex is currently set to that item. In this
     *     case, itemTraverse represents the return value of that item's
     *     initial traverse() call.
     *
     * Based on these conditions, this method will either:
     *
     * 1.) Continue the internal traversal on the current item (scrolling
     *     as necessary to display the item's internal traversal location)
     * or
     * 2.) Perform a traversal to the next interactive item on the page
     * or
     * 3.) Perform a page flip (uScrollViewport()) and call the 
     *     uInitItemsInViewport() routine to select an appropriate 
     *     traversal item
     *
     * SYNC NOTE: Maybe call into CustomItem.traverse().
     * So caller must not hold LCDUILock.
     *
     * @param dir the direction of traversal
     */
    void uTraverse(int dir) {

        ItemLFImpl[] itemsCopy;
        int traverseIndexCopy;

        synchronized (Display.LCDUILock) {
            itemsCopy = new ItemLFImpl[numOfLFs];
            traverseIndexCopy = traverseIndex;
            System.arraycopy(itemLFs, 0, itemsCopy, 0, numOfLFs);
            itemsModified = false;
        }
        
        // itemTraverse indicates the return value of the
        // last call to the current item's traverse method.
        // 'true' indicates it is doing internal traversal,
        // 'false' indicates we may traverse out of that item
        // if we have something else to traverse to or scrolling
        // that needs to be done
        if (itemTraverse) {
            
            if (traverseIndexCopy == -1) {
                itemTraverse = false;
                return;
            }
                        
            itemTraverse = 
                    uCallItemTraverse(itemsCopy[traverseIndexCopy], dir);
                
            if (itemTraverse) {
                // We may have to scroll to accommodate the new
                // traversal location 
                if (scrollForBounds(dir, visRect)) {
                    uRequestPaint();
                }
                return;
            }
        } 
                     
        // We are done with the traversal of the current item, so
        // we look to see if another interactive item is available on
        // current page
        int nextIndex = 
                getNextInteractiveItem(itemsCopy, dir, traverseIndexCopy);

        if (nextIndex != -1) {
            // NOTE: In traverse(), if there is a "next" interactive
            // item, there must have been a "first" interactive item
            // (which was set initially in uInitItemsInViewport())
            // so traverseIndex should always be valid
            
            // We need to traverse out of the previous item, now that
            // we've found a new item to traverse to

            // NOTE WELL: traverseIndex (and thus traverseIndexCopy) may well 
            // be invalid if there is no currently focused item, the app adds 
            // a focusable item, and then the user traverses before the 
            // resulting invalidation can be processed. Thus, this value must 
            // be guarded anyway. See CR#6254765.

            if (traverseIndexCopy != -1) {
                itemsCopy[traverseIndexCopy].uCallTraverseOut();
                synchronized (Display.LCDUILock) {
                    itemsCopy[traverseIndexCopy].lRequestPaint();
                }
            }
            
            /*
             * NOTE: Although we update traverseIndex in a synchronized block
             * and call "lRefreshItems()" to update itemsCopy[] & 
             * traverseIndexCopy, 
             * original itemLFs[] & traverseIndex can change after sync block - 
             * so we still have a risk of referring to a non-existent item...
             */
            synchronized (Display.LCDUILock) {
                if (itemsModified) {
                    // SYNCHRONIZE itemLFs & itemsCopy ...
                    itemsCopy = lRefreshItems(
                            itemsCopy, traverseIndexCopy, nextIndex);
                } else {
                    // Update our traverse index to the new item
                    traverseIndex = nextIndex;
                }
                traverseIndexCopy = traverseIndex;
            }
            
            if (traverseIndexCopy != -1) {
                // We then need to traverse to the next item
                itemTraverse = 
                    uCallItemTraverse(itemsCopy[traverseIndexCopy], dir);
                
                if (scrollForBounds(dir, visRect)) {
                    uRequestPaint(); // request to paint contents area
                } else {                   
                    synchronized (Display.LCDUILock) {
                        itemsCopy[traverseIndexCopy].lRequestPaint();
                    }
                }
            }
            
            int scrollPos = getScrollPosition0();
            // There is a special case when traversing to the very last
            // item on a Form
            if (traverseIndexCopy == (itemsCopy.length - 1) && 
                !itemCompletelyVisible(itemsCopy[traverseIndexCopy])) 
            {
                // Since its the last item, we may need to
                // perform a partial scroll to fit it.                
                if (scrollPos + viewportHeight !=
                    itemsCopy[traverseIndexCopy].bounds[Y] + 
                    itemsCopy[traverseIndexCopy].bounds[HEIGHT])
                {
                    scrollPos = viewable[HEIGHT] - viewportHeight;
                        
                    // We make sure we don't go past the top of the
                    // item, as we must have been going down to reach
                    // the last item
                    if (scrollPos > itemsCopy[traverseIndexCopy].bounds[Y]) {
                        scrollPos = itemsCopy[traverseIndexCopy].bounds[Y];
                    }
                    uRequestPaint();
                }
            }
            
            // Likewise, there is a special case when traversing up to
            // the very first item on a Form
            if (traverseIndexCopy == 0) {
                // Since its the first item, we may need to
                // perform a partial scroll to fit it.
                if (scrollPos != itemsCopy[traverseIndexCopy].bounds[Y]) {
                    scrollPos = itemsCopy[traverseIndexCopy].bounds[Y];
                    
                    // We make sure we don't go past the bottom of the
                    // item, as we must have been going up to get to
                    // the first item
                    if (itemsCopy[traverseIndexCopy].bounds[HEIGHT] > 
                            viewportHeight)
                    {
                        scrollPos = 
                            itemsCopy[traverseIndexCopy].bounds[HEIGHT] -
                            viewportHeight;
                    }
                    uRequestPaint();
                }
            }
            setScrollPosition0(scrollPos);
            updateCommandSet();
        } else {                      
            
            // There is no more interactive items wholly visible on
            // the current page. We may need to scroll to the next page,
            // if we do, then traverse out of the current item and 
            // scroll the page
            
            int scrollPos = getScrollPosition0();
            if ((dir == Canvas.LEFT || dir == Canvas.UP) && scrollPos > 0) {
                // Special case. We're at the top-most interactive item, but
                // its internal traversal doesn't allow the very top to be
                // seen, we just scroll the view to show it
                if (traverseIndexCopy != -1 && 
                    (scrollPos > itemsCopy[traverseIndexCopy].bounds[Y])) 
                {
                    scrollPos -= (viewportHeight - PIXELS_LEFT_ON_PAGE);
                    if (scrollPos < 0) {
                        scrollPos = 0;
                    }
                    setScrollPosition0(scrollPos);
                    uRequestPaint();
                } else {
                    // page up
                    uScrollViewport(Canvas.UP, itemsCopy);
                    uInitItemsInViewport(
                            Canvas.UP, itemsCopy, traverseIndexCopy);
                    updateCommandSet();
                    return;
                }
            } else if ((dir == Canvas.RIGHT || dir == Canvas.DOWN) &&
                (scrollPos + viewportHeight < viewable[HEIGHT])) 
            {
                // Special case. We're at the bottom-most interactive item,
                // but its internal traversal doesn't allow the very bottom
                // to be seen, we just scroll the view to show it
                if (traverseIndexCopy != -1 &&
                    ((itemsCopy[traverseIndexCopy].bounds[Y] + 
                        itemsCopy[traverseIndex].bounds[HEIGHT]) >
                    (scrollPos + viewportHeight))) 
                {
                    scrollPos += (viewportHeight - PIXELS_LEFT_ON_PAGE);
                    if (scrollPos > (viewable[HEIGHT] - viewportHeight)) 
                    {
                        scrollPos = viewable[HEIGHT] - viewportHeight;
                    }
                    setScrollPosition0(scrollPos);
                    uRequestPaint();
                } else {            
                    // page down
                    uScrollViewport(Canvas.DOWN, itemsCopy);
                    uInitItemsInViewport(
                            Canvas.DOWN, itemsCopy, traverseIndexCopy);
                    updateCommandSet();
                    return;
                }
            }
            
            // If we don't need to scroll the page and there is nothing
            // to traverse to, we reset the itemTraverse result as if
            // the Item wishes to proceed with internal traversal (as long
            // as there was some initial traverse in the first place, ie,
            // traverseIndex != -1)
            if (traverseIndexCopy != -1) {
                itemTraverse = true;
            }
            updateCommandSet();
        }        
    }


    /**
     * Perform a page flip in the given direction. This method will
     * attempt to scroll the view to show as much of the next page
     * as possible. It uses the locations and bounds of the items on
     * the page to best determine a new location - taking into account
     * items which may lie on page boundaries as well as items which
     * may span several pages.
     *
     * @param dir the direction of the flip, either DOWN or UP
     * @param items the set of items on the Form, used to determine
     *        the best suited scroll locations
     */
    void uScrollViewport(int dir, ItemLFImpl[] items) {
        int scrollPos = getScrollPosition0();

        if (dir == Canvas.UP) {
            int newY = scrollPos - (viewportHeight - PIXELS_LEFT_ON_PAGE);
            if (newY < 0) {
                newY = 0;
            }

            // We loop upwards until we find the first item which is
            // currently at least partially visible
            int firstVis = items.length;
            for (int i = items.length - 1; i >= 0; i--) {
                if (items[i].visibleInViewport) {
                    firstVis = i;
                }
            }

            if (firstVis == items.length) {
                scrollPos = newY;
                setScrollPosition0(scrollPos);
                return;
            }
            
            // case 1. We're at the top of the item so just
            // traverse normally
            if (items[firstVis].bounds[Y] >= scrollPos) {
                scrollPos = newY;
                setScrollPosition0(scrollPos);
                return;
            }
            
            // case 2. We try to fit as much of the partially visible
            // item onscreen as possible.
            int fitY = 
                (items[firstVis].bounds[Y] + items[firstVis].bounds[HEIGHT]) -
                viewportHeight;
                
            if (fitY > newY && scrollPos > fitY) {
                newY = fitY;
            } 
           
            scrollPos = newY;
            setScrollPosition0(scrollPos);
            return;
            
        } else if (dir == Canvas.DOWN) {            
            int newY = scrollPos + (viewportHeight - PIXELS_LEFT_ON_PAGE);
            if (newY > viewable[HEIGHT] - viewportHeight) {
                newY = viewable[HEIGHT] - viewportHeight;
            }
            
            // We loop downwards until we find the last item which is
            // at least partially visible
            int lastVis = -1;
            for (int i = 0; i < items.length; i++) {
                if (items[i].visibleInViewport) {
                    lastVis = i;
                }
            }

            
            // case 1. We're at the bottom of the item so just
            // traverse normally
            if (items[lastVis].bounds[Y] + items[lastVis].bounds[HEIGHT] <=
                scrollPos + viewportHeight)
            {
                scrollPos = newY;
                setScrollPosition0(scrollPos);
                return;
            }

            // case 2. We try to fit as much of the partially visible
            // item onscreen as possible unless we're already at the top
            // of the item from a previous scroll
            if (newY > items[lastVis].bounds[Y] && 
                scrollPos < items[lastVis].bounds[Y]) 
            {
                newY = items[lastVis].bounds[Y];
            }
            
            scrollPos = newY;
            setScrollPosition0(scrollPos);
            return;
        }
    }

    /**
     * Determine if scrolling is needed for a given bounding box,
     * and perform such scrolling if necessary.
     *
     * @param dir the direction of travel
     * @param bounds the bounding box of the traversal location
     * @return <code>true</code> if it was necessary to scroll the view 
     *         in order to best accommodate the bounding box
     */
    boolean scrollForBounds(int dir, int bounds[]) {
        if (bounds == null || bounds[0] == -1) {
            return false;
        }

        int scrollPos = getScrollPosition0();
        
        // There is a special case whereby the CustomItem
        // spec mandates the upper left corner of the internal
        // traversal rect be visible if the rect is larger than
        // the available viewport
        if (bounds[HEIGHT] >= viewportHeight &&
            scrollPos != bounds[Y])
        {
            setScrollPosition0(bounds[Y]);
            return true;
        }
        
        switch (dir) {
            case Canvas.LEFT:
            case Canvas.UP:
                if (bounds[Y] >= scrollPos) {
                    return false;
                }

                scrollPos -= (viewportHeight - PIXELS_LEFT_ON_PAGE);
                if (scrollPos < 0) {
                    scrollPos = 0;
                }
                setScrollPosition0(scrollPos);
                return true;
            case Canvas.RIGHT:
            case Canvas.DOWN:
                if (bounds[Y] + bounds[HEIGHT] <=
                    scrollPos + viewportHeight) 
                {
                    return false;
                }

                scrollPos += (viewportHeight - PIXELS_LEFT_ON_PAGE);
                if (scrollPos > bounds[Y]) {
                    scrollPos = bounds[Y];
                }
                if (scrollPos + viewportHeight > viewable[HEIGHT]) {
                    scrollPos = viewable[HEIGHT] - viewportHeight;
                }
                setScrollPosition0(scrollPos);
                return true;
            default:
                // for safety/completeness, don't scroll.
                Logging.report(Logging.ERROR, 
                    LogChannels.LC_HIGHUI_FORM_LAYOUT,
                    "FormLFImpl: bounds, dir=" + dir);
                break;
        }
        return false;
    }


    /**
     * This method will return the index of the next interactive
     * item which is wholly visible on the screen given the traversal
     * direction, or -1 if no visible items in that traversal direction
     * are interactive (or completely visible).
     *
     * @param items the set of items to search
     * @param dir the direction of traversal
     * @param index the "anchor" of the index to start from
     * @return the index of the next interactive item, or -1 if one is
     *         not completely visible or available in the given direction
     */
    int getNextInteractiveItem(ItemLFImpl[] items, int dir, int index) {
              
        try {
            int scrollPos = getScrollPosition0();
            
            while (true) {
                switch (dir) {
                    case Canvas.UP:
                    case Canvas.LEFT:
                        index -= 1;
                        break;
                    case Canvas.DOWN:
                    case Canvas.RIGHT:
                        index += 1;
                        break;
                    case CustomItem.NONE:
                        // no - op
                        break;
                    default:
                        // for safety/completeness.
                        Logging.report(Logging.ERROR, 
                                       LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                       "FormLFImpl: dir=" + dir);
                        return index;
                }
                // If we've exhausted the set, stop looking                
                if (index < 0 || index >= items.length) {
                    break;
                }
                
                // If we've found a non-interactive item, continue

                if (!items[index].item.acceptFocus()) {
                    continue;
                }


                // If we've found a completely visible, interactive
                // item, stop and traverse to it
                if (itemCompletelyVisible(items[index])) {
                    break;
                }

                // If we've found a partially visible, interactive
                // item, there is some special casing involved with
                // how to scroll appropriately
                if (itemPartiallyVisible(items[index])) {
                    if (dir == Canvas.RIGHT || dir == Canvas.DOWN) {
                        
                        // If we're paging down and the item's top
                        // is at the top of the viewport, stop and
                        // traverse to that item (its bigger than the
                        // viewport
                        if (items[index].bounds[Y] == scrollPos) {
                            break;
                        }
                            
                        // If we're paging down and the item's bottom
                        // is the very last thing in the view, stop and
                        // keep traversal on that item (item is bigger
                        // than the viewport and we can go no further)
                        if (items[index].bounds[Y] +
                            items[index].bounds[HEIGHT] == 
                                viewable[HEIGHT]) 
                        {
                            break;
                        }
                    } else if (dir == Canvas.LEFT || dir == Canvas.UP) {
                        
                        // If we're paging up and the item's bottom is the
                        // very bottom of the viewport, stop and keep
                        // traversal on that item (item is bigger than the
                        // viewport and we start at the bottom)
                        if (items[index].bounds[Y] +
                            items[index].bounds[HEIGHT] == 
                                viewable[HEIGHT]) 
                        {
                            break;
                        }
                        
                        // If we're paging up and the item's top is at
                        // the top of the viewport, stop and traverse
                        // to that item (its bigger than the viewport
                        // and we should show the top of it before leaving)
                        if (items[index].bounds[Y] == scrollPos &&
                            scrollPos == 0) 
                        {
                            break;
                        }
                    }
                }                
            } // while                
        } catch (Throwable t) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_HIGHUI,
                           "Throwable while finding next item for traversal");
            }
            return -1;
        }
        
        // This means there was no interactive item in the currently
        // visible viewport
        if (index < 0 || index >= items.length) {
            return -1;
        }       
        
        return index;
    }

    /**
     * Determine if the given item is at least partially visible
     * in the current viewport.
     *
     * @param item the item to determine visibility
     * @return true if at least part of the item is visible
     */
    boolean itemPartiallyVisible(ItemLFImpl item) {
        // If the Form is hidden, all the items are
        // hidden and we just return false
        if (super.state == HIDDEN) {
            return false;
        }
        int scrollPos = getScrollPosition0();        
        // If the Item's top is within the viewport, return true
        return !(item.bounds[Y] >= scrollPos + viewportHeight ||
                 item.bounds[Y] + item.bounds[HEIGHT] <= scrollPos);
    }

    /**
     * Determine how much the given item is visible in the current viewport.
     *
     * @param item the item to determine visibility
     * @return the percentage of visible item area rated from 0 to 100
     * 0 - item is not visible,
     * 100 - item is completely visible
     * 1 - 99 - item is partially visible 
     */
    int howMuchItemVisible(ItemLFImpl item) {
        int[] visibleArea = new int[4];
        setVisRect(item, visibleArea);
        return item.bounds[HEIGHT] > 0 ?
            (visibleArea[HEIGHT] * 100 / item.bounds[HEIGHT]) : 0;
    }
    
    /**
     * Determine if the given item is at completely visible
     * in the current viewport.
     *
     * @param item the item to determine visibility
     * @return true if at the item is entirely visible
     */
    boolean itemCompletelyVisible(ItemLFImpl item) {
        // If the Form is being hidden, all the items are
        // hidden and we just return false
        if (super.state == HIDDEN) {
            return false;
        }

        // If the Item's top and bottom are within the viewport,
        // return true
        int scrollPos = getScrollPosition0();
        return (item.bounds[Y] >= scrollPos) && 
            (item.bounds[Y] + item.bounds[HEIGHT] <= scrollPos + viewportHeight);
    }


    /**
     * Calculate the rectangle representing the region of the item that is
     * currently visible. This region might have zero area if no part of the
     * item is visible, for example, if it is scrolled offscreen.
     * @param item item
     * @param visRect  It must be an int[4] array. The information in this array is
     * a rectangle of the form [x,y,w,h]  where (x,y) is the location of the
     * upper-left corner of the rectangle relative to the item's origin, and
     * (w,h) are the width and height of the rectangle.
     */
    private void setVisRect(ItemLFImpl item, int[] visRect) {
        synchronized (Display.LCDUILock) {
            // Initialize the in-out rect for traversal
            visRect[X] = 0;
            visRect[WIDTH] = width;
 
            // take the coordinates from the overall
            // coordinate space 
 
            int itemY1 = item.bounds[Y];
            int itemY2 = item.bounds[Y] + item.bounds[HEIGHT];
 
            // vpY1 the y coordinate of the top left visible pixel
            // current scroll position
            int vpY1 = getScrollPosition0();;
            // vpY2 the y coordinate of bottom left pixel
            int vpY2 = vpY1 + height;
                         
            // return only the visible region of item
 
            // item completely visible in viewport
            visRect[Y] = 0;
            visRect[HEIGHT] = item.bounds[HEIGHT];
                         
            if ((itemY1 >= vpY2) || (itemY2 <= vpY1)) { 
                // no part of the item is visible
                // so this region has zero area
                visRect[WIDTH] = 0;
                visRect[HEIGHT] = 0;
            } else {
                if (itemY1 < vpY1) {
                    // upper overlap
                    visRect[Y] =  vpY1 - itemY1;
                    visRect[HEIGHT] -= (vpY1 - itemY1);
                }
                if (itemY2 > vpY2) {
                    // lower overlap
                    visRect[HEIGHT] -= (itemY2 - vpY2);
                }
            } 
        }
    }

    /**
     * Perform an internal traversal on the given item in
     * the given direction. The only assertion here is that
     * the item provided must be interactive (or otherwise
     * be a CustomItem). When this method returns, visRect[]
     * will hold the bounding box of the item's internal
     * traversal (in the Form's coordinate space).
     *
     * @param item the item to traverse within
     * @param dir the direction of traversal
     * @return true if this item performed an internal traversal
     *         in the given direction.
     */
    boolean uCallItemTraverse(ItemLFImpl item, int dir) {

        boolean ret = false;

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "[F] uCallItemTraverse: dir=" + dir +
                           " traverseIndex=" + traverseIndex);
        }

        // The visRect is supposed to show the bounds of the Item
        // currently visible on the screen
        setVisRect(item, visRect);

        // Whether the item performs an internal traversal or not,
        // it has the current input focus
        //item.hasFocus = true;
        
        // Call traverse() outside LCDUILock
        if (item.uCallTraverse(dir,
                               width, viewportHeight, visRect)) 
        {
            synchronized (Display.LCDUILock) {
                // It's possible that this newFocus item has
                // been just removed from this Form since we
                // are outside LCDUILock. Check again.
                if (item.nativeId != INVALID_NATIVE_ID) {
                    setCurrentItem0(nativeId, item.nativeId, visRect[Y]);
                }
            }

            ret = true;
        }

        // Since visRect is sent to the Item in its own coordinate
        // space, we translate it back into the overall Form's
        // coordinate space
        visRect[X] += item.bounds[X];
        visRect[Y] += item.bounds[Y];

        return ret;
    }

    /**
     * Scrolls to the passed in Item.
     * @param item The Item that should be shown on the screen.
     */
    private void lScrollToItem(Item item) {
        
        if (item == null || item.owner != owner) {
            return;
        }

        int index = -1;
        
        ItemLFImpl itemLF = null;
        if (traverseIndex != -1 && (itemLFs[traverseIndex].item == item)) {
            index = traverseIndex;
        } else {
            for (int i = 0; i < numOfLFs; i++) {
                if (itemLFs[i].item == item) {
                    index = i;
                    break;
                }
            }
        }

        // item not found
        if (index==-1) {
            return;
        }

        itemLF = itemLFs[index];
        
        if (index != traverseIndex) {
            // Ensure the item is visible
            if (!itemCompletelyVisible(itemLF)) {
                int scrollPos = itemLF.bounds[Y];
                if (scrollPos + viewportHeight > viewable[HEIGHT]) {
                    scrollPos = viewable[HEIGHT] - viewportHeight;
                }
                setScrollPosition0(scrollPos);
            }

            // We record the present traverseItem because if it
            // is valid, we will have to call traverseOut() on that
            // item when we process the invalidate call.
            if (traverseIndex != -1) {
                lastTraverseItem = itemLFs[traverseIndex];
            }
            
            // If the item is not interactive, we just leave it
            // visible on the screen, but set traverseIndex to -1
            // so that any interactive item which is visible will
            // be traversed to when the invalidate occurs
            traverseIndex = itemLF.item.acceptFocus() ? index : -1;
            lRequestInvalidate();
        } else {
            // Ensure the item is visible
            if (!itemPartiallyVisible(itemLF)) {
                int scrollPos = itemLF.bounds[Y];
                if (scrollPos + viewportHeight > viewable[HEIGHT]) {
                    scrollPos = viewable[HEIGHT] - viewportHeight;
                }
                setScrollPosition0(scrollPos);
            }
        }
    }
    
    /**
     * Initialize the current page of items, perform a traverse if possible.
     * This method is always called when a page is initially "shown".
     * This occurs when the form gains visibility for the very first
     * time as well as after every page up/page down occurs.
     *
     * This method searches for the most appropriate item on the form
     * to receive the interaction focus.
     *
     * @param dir the direction of travel. Can be NONE when a page is
     *        first shown or as the result of an invalidate.
     * @param itemsCopy a copy of the set of ItemLFImpls in this form.
     * @param traverseIndexCopy a copy of taverseIndex to work with itesCopy[]
     */
    void uInitItemsInViewport(
        int dir, ItemLFImpl[] itemsCopy, int traverseIndexCopy) {
        // Create a copy of the current index for comparisons, below.
        if (itemsCopy.length == 0) {
            return;
        }
        
        // Hide & Show Items 
        int pos = getScrollPosition0();
        uViewportChanged(pos, pos + viewportHeight);
        
        // The result of an invalidate() call
        if (traverseIndexCopy != -1 && dir == CustomItem.NONE) {
            itemTraverse = 
                    uCallItemTraverse(itemsCopy[traverseIndexCopy], dir);

            uRequestPaint(); // request to paint contents area
            return;
        }
        
        // Special case: It could be that no item in the current view
        // is interactive, and thus the traverseIndexCopy is -1. If we
        // are scrolling upwards, we artificially set it to be the last
        // item on the form (+1) so that the getNextInteractiveItem()
        // routine will subsequently reduce it by 1 and start searching
        // for an interactive item from the bottom of the form upwards.
        if (dir == Canvas.UP && traverseIndexCopy == -1) {
            traverseIndexCopy = itemsCopy.length;
        }
        
        // If paging "down", we find the interactive item by moving
        // left to right - this ensures we move line by line searching
        // for an interactive item. When paging "up", we search from
        // right to left.


        int nextIndex = traverseIndexCopy, curIndex = traverseIndexCopy;
        int maxRate = traverseIndexCopy > -1 ?
            howMuchItemVisible(itemsCopy[traverseIndexCopy]) : 0;
        
        while (maxRate < 100) {
            curIndex = getNextInteractiveItem(itemsCopy,
                                              (dir == Canvas.DOWN || dir == CustomItem.NONE) ?
                                              Canvas.RIGHT : Canvas.LEFT,
                                              curIndex);
            if (curIndex != -1) {
                int rate  = howMuchItemVisible(itemsCopy[curIndex]);
                if (rate > maxRate) {
                    maxRate = rate;
                    nextIndex = curIndex;
                }
            } else {
                // no more interractive items on the screen 
                break;
            }
        }
        
        if (traverseIndexCopy > -1 && traverseIndexCopy < itemsCopy.length) {
            if (nextIndex != -1 || 
                !itemCompletelyVisible(itemsCopy[traverseIndexCopy])) 
            {
                // It could be we need to traverse out of a current
                // item before paging
                itemsCopy[traverseIndexCopy].uCallTraverseOut();
                synchronized (Display.LCDUILock) {
                    traverseIndex = -1;  // reset real index
                    traverseIndexCopy = traverseIndex;
                }
            }
        } 
        /*
         * NOTE: between these two sync sections itemLFs[] & traverseIndex
         * can change again ...
         */
        synchronized (Display.LCDUILock) {
            if (itemsModified) { 
                // SYNCHRONIZE itemLFs & itemsCopy, update traverseIndex ...
                itemsCopy = lRefreshItems(
                        itemsCopy, traverseIndexCopy, nextIndex);
            } else if ((nextIndex > -1) && (nextIndex < numOfLFs)) {
                traverseIndex = nextIndex;
            }
            traverseIndexCopy = traverseIndex;
        }

        if (traverseIndexCopy == -1 || traverseIndexCopy == itemsCopy.length) {
            // If there is no traversable item on the current page,
            // we simply return, and on the next 'traverse' we will
            // perform a page scroll and repeat this method
        } else {
            // If there is a traversable item, we go ahead and traverse
            // to it. We do *not* scroll at all under these circumstances
            // because we have just performed a fresh page view (or scroll)
            itemTraverse = 
                    uCallItemTraverse(itemsCopy[traverseIndexCopy], dir);
        }
         
        uRequestPaint(); // request to paint contents area                
    }


    /**
     * Called by the system to notify that viewport scroll location
     * or height has been changed.
     *
     * @param vpY1 the y coordinate of the top left visible pixel
     * @param vpY2 the y coordinate of bottom left pixel 
     *             immediately below the viewport
     */
    private void uViewportChanged(int vpY1, int vpY2) {
	
    int i, showCount, hideCount, size;
    ItemLFImpl[] itemsCopy = null;
	
    synchronized (Display.LCDUILock) {

        itemsCopy = new ItemLFImpl[numOfLFs];
        size = numOfLFs;
	       
        showCount = 0;
        hideCount = numOfLFs;
	       
        for (i = 0; i < numOfLFs; i++) {
            if (itemLFs[i].bounds[Y] + 
                itemLFs[i].bounds[HEIGHT]-1 > vpY1 &&
                itemLFs[i].bounds[Y] < vpY2) { 
                // should become visible
                if (itemLFs[i].visibleInViewport == false) {
                    itemsCopy[showCount++] = itemLFs[i];
                }
            } else { 
                // should not be visible
                if (itemLFs[i].visibleInViewport) {
                    itemsCopy[--hideCount] = itemLFs[i];
                }
            }
        }
	} // synchronized (LCDUILock)
	   
    for (i = 0; i < showCount; i++) {
        itemsCopy[i].uCallShowNotify();
    }
	   
    for (i = hideCount; i < size; i++) {
        itemsCopy[i].uCallHideNotify();
    }
    }

    /**
     * Service method - find the <code>ItemLFImpl</code> from a given 
     * native id.
     *
     * @param nativeId native id to search
     *
     * @return the <code>ItemLFImpl</code>, or <code>null</code> not found
     */
    private ItemLFImpl id2Item(int nativeId) {

        ItemLFImpl focus = getItemInFocus();

        if (focus != null && focus.nativeId == nativeId) {
            return focus;
        } else {
            for (int i = 0; i < numOfLFs; i++) {
                if (itemLFs[i].nativeId == nativeId) {
                    return itemLFs[i];
                }
            }
            // there is no matching ItemLFImpl
            return null;
        }
    }

    /**
     * Service method - find the <code>ItemLFImpl</code> index.
     *
     * @param itemLF itemLF to map
     *
     * @return index of the item. -1 if not found.
     */
    private int item2Index(ItemLFImpl itemLF) {

	for (int i = 0; i < numOfLFs; i++) {
	    if (itemLFs[i] == itemLF) {
		return i;
	    }
	}

	return -1;
    }


    /**
     * Ensure that dispatchItemLFs array has enough space for use.
     * SYNC NOTE: This function must only be used in event dispatch thread.
     *
     * @param size maximum number of itemLFs needed
     */
    private static void ensureDispatchItemArray(int size) {
	if (size > dispatchItemLFs.length) {
	    dispatchItemLFs = new ItemLFImpl[size];
	}
    }

    /**
     * Clear contents of dispatchItemLFs array after use.
     * SYNC NOTE: This function must only be used in event dispatch thread.
     *
     * @param alsoShrink true if the array size should be minimized
     */
    private static void resetDispatchItemArray(boolean alsoShrink) {

	if (alsoShrink && dispatchItemLFs.length > DISPATCH_ITEM_ARRAY_BLOCK) {
	    dispatchItemLFs = new ItemLFImpl[DISPATCH_ITEM_ARRAY_BLOCK];
	} else {
	    // Only clean up existing array contents
	    for (int i = 0; i < dispatchItemLFs.length; i++) {
		dispatchItemLFs[i] = null;
	    }
	}
    }

    /**
     * Synchronizes itemLFs[] array with itemsCopy[] array, 
     * as well as traverseIndex with traverseIndexCopy & nextIndex. 
     *
     * Since most of work with copies occurs outside of LCDUILock
     * (this is, BTW, the reason, why copies are used instead of original
     * fields), there is a risk, that
     * itemLFs[] content can be changed (ex. insert/delete/replace Item): 
     * traverseIndexCopy can point to a different object 
     * (including non-interactive), 
     * or outside of changed itemLFs array (throws exception), 
     * or we can refer to a deleted item (deleted in itemLFs, 
     * but still exists in a copy).
     *
     * This method tries to find item, referred by nextIndex in itemsCopy[], 
     * in itelLFs[], and if found, sets traverseIndex to foundItem, 
     * else sets traverse index to -1.
     *
     * This method indended to be called in LCDUILock-protected code, 
     * from uInitItemsInViewport(...) & uTraverse(...).
     *
     * @param itemsCopy a copy of the set of ItemLFImpls in this form.
     * @param traverseIndexCopy a copy of traverseIndex to work with itemsCopy
     * @param nextIndex suggested new value of traverseIndex, 
     *        the item from itemsCopy[] to be found in changed itemLFs[]
     *
     * @return updated itemsCopy[] array, synchronized with itemLFs[]
     */
    private ItemLFImpl[] lRefreshItems(
            ItemLFImpl[] itemsCopy, 
            int traverseIndexCopy, 
            int nextIndex) {
        
        final int nextIndexInLFs = nextIndex + 
                (traverseIndex - traverseIndexCopy);
        traverseIndex = (
            (traverseIndex > -1) && 
            /* (traverseIndex < numOfLFs) && */
            (traverseIndexCopy > -1) && 
            /* (traverseIndexCopy < itemsCopy.length) && */
            (nextIndex > -1) && 
            /* (nextIndex < itemsCopy.length) && */
            (nextIndexInLFs > -1) && 
            (nextIndexInLFs < numOfLFs) &&
            !itemLFs[nextIndexInLFs].item.acceptFocus())
            /* (itemsCopy[nextIndex] == itemLFs[nextIndexInLFs])) */
            /*
             * Assume that:
             * 1). traverseIndex has always valid value: 
             * i.e. -1 or within [0..numOfLFs[ range of itemLFs[] array.
             * 2). traverseIndexCopy & nextIndex have always valid values: 
             * i.e. -1 or within [0..itemsCopy.length[ range  
             * of itemsCopy[] array.
             * As the result we need to check them only for "-1" value.
             * Computed "nextIndexInLFs" needs to be checked for 
             * being in bounds of itemLFs[].
             *
             * Need revisit : if last condition in the above  "IF" is needed,
             * however it ensures, that the  next current item 
             * will be exactly the same item that has been found 
             * by "getNext...". 
             * Without thast statement we have a risk to point to 
             * a completely different item: still valid & 
             * in the range, but probably NON-interactive :-( 
             * To avoid this, "shouldSkipTraverse()" could be used insead ...
             */
            ? nextIndexInLFs
            : -1;

        // refresh itemsCopy array ...
        itemsCopy = new ItemLFImpl[numOfLFs];
        System.arraycopy(itemLFs, 0, itemsCopy, 0, numOfLFs);
        itemsModified = false;
        return itemsCopy;
    }


    /**
     * Sub types of peer notification events
     */
    private static final int PEER_FOCUS_CHANGED = 0;
    private static final int PEER_VIEWPORT_CHANGED = 1;
    private static final int PEER_ITEM_CHANGED = 2;
    private static final int PEER_TRAVERSE_REQUEST = 3;

    /** 
     * A bit mask to capture the horizontal layout directive of an item.
     */
    final static int LAYOUT_HMASK = 0x03;

    /** 
     * A bit mask to capture the vertical layout directive of an item.
     */
    final static int LAYOUT_VMASK = 0x30;

    /** 
     * Do a full layout.
     */
    final static int FULL_LAYOUT = -1;

    /** 
     * Only update layout.
     */
    final static int UPDATE_LAYOUT = -2;

    /**
     * This is the rate at which the internal array of Items grows if
     * it gets filled up.
     */
    private static final int GROW_SIZE = 4;

    /**
     * This is the number of pixels left from the previous "page"
     * when a page up or down occurs
     */
    static final int PIXELS_LEFT_ON_PAGE = 15;


    /** The item index which has the traversal focus */
    int traverseIndex = -1;

    /**
     * Item that was made visible using display.setCurrentItem() call
     * while FormLF was in HIDDEN or FROZEN state.
     */
    Item pendingCurrentItem = null;

    /** 
     * This is a special case variable which tracks the last
     * traversed item when a new item is traversed to via the
     * setCurrentItem() call.
     */
    ItemLFImpl lastTraverseItem;

    /** 
     * A flag indicating the return value of the currently
     * selected Item from its traverse() method
     */
    boolean itemTraverse;

    /** 
     * A flag that shows that itemLFs[] storage has been modified
     * (items inserted/deleted) and earlier made copies (extends itemsCopy[])
     * are outdated. 
     * flag is set by item insert/delete operations, 
     * cleared when copy operation is performed. 
     */
    boolean itemsModified; 

    /**
     * When a Form calls an Item's traverse() method, it passes in
     * an in-out int[] that represents the Item's traversal
     * bounds. This gets cached in the visRect variable
     */
    int[] visRect;

    /**
     * Array of <code>ItemLF</code>s that correspond to the array of items 
     * in <code>Form</code>.
     */
    private ItemLFImpl[] itemLFs;

    /**
     * Block size of the temporary array of <code>ItemLF</code>s used 
     * in dispatch.
     */
    private final static int DISPATCH_ITEM_ARRAY_BLOCK = 10;

    /**
     * Temporary array of <code>ItemLF</code>s that is ONLY used in 
     * dispatch thread during show, hide and re-layout this <code>Form</code>.
     *
     * ensureDispatchItemArray() should be called before use and
     * resetDispatchItemArray() should be called when it is no longer needed,
     * to allow <code>ItemLFImpl</code> objects been GC'ed.
     */
    private static ItemLFImpl[] dispatchItemLFs =
		new ItemLFImpl[DISPATCH_ITEM_ARRAY_BLOCK];

    /**
     * The number of views present in this <code>FormLF</code>.
     */
    private int numOfLFs;
    
    /**
     * This helps an optimization.
     */
    private boolean firstShown = true;

    /**
     * Screens should automatically reset to the top of the when
     * they are shown, except in cases where it is interrupted by
     * a system menu or an off-screen editor - in which case it
     * should be reshown exactly as it was.
     */
    boolean resetToTop = true;

    /**
     * Viewport height in the native resource
     */
    private int viewportHeight; // = 0;

    /** 
     * Overall dimensions of the view. It is an array so it could be passed 
     * as a reference to <code>LayoutManager</code>.
     */
    int viewable[] = new int[4];

    /**
     * Left to right layout is default.
     * Used by isImplicitLineBreak.
     */
    final static boolean ltr = true; 

} // class FormLFImpl
