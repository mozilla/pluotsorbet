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
import com.sun.midp.chameleon.skins.ScreenSkin;


/**
 * Look and feel class for Form.
 * See DisplayableLF.java for naming convention.
 */
class FormLFImpl extends ScreenLFImpl implements FormLF {

    /**
     * Creates FormLF associated with passed in form.
     * FormLFImpl maintains an array of views associated with its items.
     * 
     * @param form the Form object associated with this FormLF
     * @param items the array of Items using which the passed in Form
     *        was created
     * @param numOfItems current number of elements
     */
    FormLFImpl(Form form, Item items[], int numOfItems) {
        
        super(form);
        
        // Initialize the in-out rect for Item traversal
        visRect = new int[4];
        
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
     * Creates FormLF for the passed in screen.
     * Passed in ItemLF is added as the only itemLF present.
     * This constructor is used by List and TextBox.
     * 
     * @param screen the Screen object associated with this FormLFImpl
     * @param item the Item to be added to this screen
     */
    FormLFImpl(Screen screen, Item item) {
        super(screen);

        itemLFs = new ItemLFImpl[1];
        itemLFs[0] = (ItemLFImpl)item.getLF();
        numOfLFs = 1;

        visRect = new int[4];

        if (screen instanceof TextBox && item instanceof TextField) {
            TextField textField = (TextField)item;
            ((TextFieldLFImpl)textField.textFieldLF).setBorder(false);
        }
    }

    // ************************************************************
    //  public methods - FormLF interface implementation
    // ************************************************************

    /**
     * Set the current traversal location to the given Item.
     * This call has no effect if the given Item is the
     * current traversal item, or if the given Item is not
     * part of this Form. Note that null can be passed in
     * clear the previously set current item.
     *
     *
     * @param i the Item to make the current traversal item
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
        uScrollToItem(i);
    }

    /**
     * Scrolls to the passed in Item. 
     * @param item The Item that should be shown on the screen.
     */
    private void uScrollToItem(Item item) {
        if (item == null || item.owner != owner) {
            return;
        }

        ItemLFImpl[] itemsCopy = null;
        int itemsCopyCount = 0;
        int traverseIndexCopy = -1;
        ItemLFImpl itemLF = null;
        
        synchronized (Display.LCDUILock) {
            itemsCopy = new ItemLFImpl[numOfLFs];
            itemsCopyCount = numOfLFs;
            System.arraycopy(itemLFs, 0, itemsCopy, 0, numOfLFs);
            traverseIndexCopy = traverseIndex;
        }

        
        int index = -1;
        
        if (traverseIndexCopy != -1 && (itemsCopy[traverseIndexCopy].item == item)) {
            index = traverseIndexCopy;
        } else {
            for (int i = 0; i < itemsCopyCount; i++) {
                if (itemsCopy[i].item == item) {
                    index = i;
                    break;
                }
            }
        }

        // item is found
        if (index > -1) {
            itemLF = itemsCopy[index];

            if (index != traverseIndexCopy) {
                
                // We record the present traverseItem because if it
                // is valid, we will have to call traverseOut() on that
                // item when we process the invalidate call.
                if (traverseIndexCopy != -1) {
                    try {
                        itemsCopy[traverseIndexCopy].uCallTraverseOut();
                    } catch (Throwable t) {
                        if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                            Logging.report(Logging.WARNING, LogChannels.LC_HIGHUI,
                                           "Throwable while traversing out");
                        }
                    }
                }
                
                // If the item is not interactive, we just leave it
                // visible on the screen, but set traverseIndex to -1
                // so that any interactive item which is visible will
                // be traversed to when the invalidate occurs
                traverseIndexCopy = itemLF.shouldSkipTraverse() ? -1 : index;
                
                if (traverseIndexCopy > -1) {
                    itemTraverse = uCallItemTraverse(itemsCopy[traverseIndexCopy], CustomItem.NONE);

                }
                
                synchronized (Display.LCDUILock) {
                    traverseIndex = traverseIndexCopy;
                }                        
                updateCommandSet();
            }

            // Ensure the item is visible at least partially
            if (!itemPartiallyVisible(itemLF)) {
                viewable[Y] = itemLF.bounds[Y];
                if (viewable[Y] + viewport[HEIGHT] > viewable[HEIGHT]) {
                    viewable[Y] = viewable[HEIGHT] - viewport[HEIGHT];
                }
                uHideShowItems(itemsCopy);
                setupScroll(); 
            }

            // If complex item need extra scrolling we should make it
            if (!itemCompletelyVisible(itemLF)) {
                if (itemsCopy[traverseIndexCopy].lScrollToItem(viewport, visRect)) {
                    if (alignForBounds(visRect)) {
                        uHideShowItems(itemsCopy);
                        setupScroll();
                    }
                }
            }
            uRequestPaint();
        } // index > -1
    }


    /**
     * Notifies look&feel object of an item deleted in the corresponding
     * Form.
     * 
     * @param itemNum - the index of the item set
     * @param item - the item set in the corresponding Form
     *
     */
    public void lSet(int itemNum, Item item) {
        itemLFs[itemNum] = (ItemLFImpl)item.itemLF;
        itemsModified = true;

        // current optimization: the new item is marked invalid, so when
        // the callInvalidate arrives, we'll know to update it and
        // the minimum set of neighbor Items.

        lRequestInvalidate();
    }

    /**
     * Notifies look&feel object of an item inserted in the corresponding
     * Form.
     * 
     * @param itemNum - the index of the inserted item
     * @param item - the item inserted in the corresponding Form
     *
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
        
        itemLFs[itemNum] = (ItemLFImpl)((Form)owner).items[itemNum].getLF();
        
        numOfLFs++;
        itemsModified = true;

        if (traverseIndex >= itemNum) {
            traverseIndex++;
        } else if (traverseIndex == -1) {
            traverseIndex = itemNum;
        }

        lRequestInvalidate();
    }

    /**
     * Notifies look&feel object of an item deleted in the corresponding
     * Form.
     * 
     * @param itemNum - the index of the deleted item
     * @param deleteditem - the item deleted in the corresponding form
     *
     */
    public void lDelete(int itemNum, Item deleteditem) {

        // start optimization...

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
                                   "[F]setting actualBoundsInvalid[Y] #" + 
                                   (itemNum + 1));
                    if (itemNum > 0) {
                        Logging.report(Logging.INFORMATION, 
                                       LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                       "[F]  | itemLFs[itemNum-1] = " +
                                       itemLFs[itemNum-1]);
                    }
                    Logging.report(Logging.INFORMATION, 
                                   LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                   "[F]  | itemLFs[itemNum] = " +
                                   itemLFs[itemNum]);
                    if (itemNum < numOfLFs-1) {
                        Logging.report(Logging.INFORMATION, 
                                       LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                       "[F]  | itemLFs[itemNum+1] = "+
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
     * Notifies look&feel object of an item deleted in the corresponding
     * Form.
     * 
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
     * (1) Re-validate the contents of this Form, possibly due to an
     * individual item
     * (2) setup the viewable/scroll position
     * (3) repaint the currently visible Items
     */
    public void uCallInvalidate() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "[F] *-* FormLFImpl: dsInvalidate ");
        }


        synchronized (Display.LCDUILock) {
            pendingInvalidate = false;
        }
        
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

        // IMPL NOTES: Remove this line after UDPATE_LAYOUT is fixed
        firstShown = true;
        // Update contents
        uShowContents(false);

        // Request a repaint
        uRequestPaint();
    }

    /**
     * Paint the contents of this Form
     *
     * @param g the Graphics object to paint on
     * @param target the target Object of this repaint
     */
    public void uCallPaint(Graphics g, Object target) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION,
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "[F] *-* FormLFImpl: dsPaint " +
                           target);
        }

        super.uCallPaint(g, target);

        // IMPL_NOTE: Thread safety
        // Cannot call paintItem inside this block
        synchronized (Display.LCDUILock) {
            // SYNC NOTE: We cannot hold any lock around a call into
            // the application's paint() routine. Rather than copy
            // the dataset and expend heap space, we simply protect
            // this operation with try/catch. The only error condition
            // would be if an insert/append/delete occurred in the middle
            // of painting. This error condition would be quickly remedied
            // by the pending validation of that change which causes a
            // repaint automatically

            try {
                if (numOfLFs == 0) {
                    return;
                }

                int clip[] = new int[4];
                clip[X] = g.getClipX();
                clip[Y] = g.getClipY();
                clip[WIDTH] = g.getClipWidth();
                clip[HEIGHT] = g.getClipHeight();

                // If the clip is an area above our viewport, just return
                if (clip[Y] + clip[HEIGHT] <= 0) {
                    return;
                }

                if (!scrollInitialized) {
                    setVerticalScroll();
                    scrollInitialized = true;
                }

                if (target instanceof Item) {

                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION,
                                       LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                       "[F] FormLFImpl: dsPaint ONE Item " +
                                       target);
                    }

                    if (((Item)target).owner == this.owner) {
                        ((ItemLFImpl)((Item)target).getLF())
                            .paintItem(g, clip,
                                       0 - viewable[X],
                                       0 - viewable[Y]);
                    }
                } else {

                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION, 
                                       LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                       "[F] FormLFImpl: dsPt. ALL Items ");
                    }
                    for (int i = 0; i < numOfLFs; i++) {
                    	
                        itemLFs[i].paintItem(g, clip,
                                             0 - viewable[X],
                                             0 - viewable[Y]);
                    }
                }
            } catch (Throwable t) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_HIGHUI,
                                  "Throwable while paint item");
                }
            }
        } // synchronized

        if (Constants.FINGER_TOUCH && pointerIndicator) {
            paintPointerIndicator(g,pointerX,pointerY);
        }

    }

    /**
     * Called by Display to notify current FormLF of a change in its peer 
     * state. For Java widgets, there is no peer for either Form or Items.
     * This implementation does nothing.
     *
     * @param modelVersion the version of the peer's data model
     * @param subtype the sub type of peer event
     * @param itemPeerId the id of the ItemLF's peer whose state has changed
     * @param hint some value that is interpreted only between the peers
     */
    public void uCallPeerStateChanged(int modelVersion, int subType,
                                      int itemPeerId, int hint) {
        // No peer. Not expected to be called
    }

    /**
     * notify this Form it is being shown on the given Display
     */
    public void uCallShow() {
        super.uCallShow();
        uShowContents(true);

        synchronized (Display.LCDUILock) {
            scrollInitialized = false;
            pendingCurrentItem = null;
        }
    }
    
    /**
     * notify this Form it is being hidden on the given Display
     */
    public void uCallHide() {
        int oldState;

        synchronized (Display.LCDUILock) {
            oldState = state;
            pendingCurrentItem = null;
        } 

        super.uCallHide();

        if (oldState == SHOWN) {
            uCallItemHide();
        }
    }

    
    /**
     * notify this Form it is being frozen on the given Display
     */
    public void uCallFreeze() {
        int oldState = state;

        super.uCallFreeze();

        if (oldState == SHOWN) {
            uCallItemHide();
        }
    }  
    
    /**
     * Updates command set if this Displayable is visible
     *
     * SYNC NOTE: Caller should hold LCDUILock.
     */
    public void updateCommandSet() {
        if (state == SHOWN) {
            currentDisplay.updateCommandSet();
        }
    }

    /**
     * Returns the width of the area available to the application.
     * @return width of the area available to the application
     */
    public int lGetWidth() {
        int w = super.lGetWidth();
        return (w > 2 * ScreenSkin.PAD_FORM_ITEMS ? 
                w - 2 * ScreenSkin.PAD_FORM_ITEMS : 0);
    }
    
    /**
     * Returns the height of the area available to the application.
     * @return height of the area available to the application
     */
    public int lGetHeight() {
        int h = super.lGetHeight();
        return (h > 2 * ScreenSkin.PAD_FORM_ITEMS ? 
                h - 2 * ScreenSkin.PAD_FORM_ITEMS : 0);
    }

    // *****************************************************
    //  Package private methods
    // *****************************************************
    
    /**
     * Handle a key press
     *
     * @param keyCode the key code of the key which was pressed
     */
    void uCallKeyPressed(int keyCode) {
    	if (keyCode == Constants.KEYCODE_UP
            || keyCode == Constants.KEYCODE_DOWN
            || keyCode == Constants.KEYCODE_LEFT
            || keyCode == Constants.KEYCODE_RIGHT) 
        {
            int dir = KeyConverter.getGameAction(keyCode);
            
            // If 2D traversal is not enabled, we "direction-bend"
            // and traverse in a typewriter-like fashion, line-by-line
            // from left to right
            if (!Constants.TRAVERSAL2D) {
                if (dir == Canvas.DOWN) {
                    dir = Canvas.RIGHT;
                } else if (dir == Canvas.UP) {
                    dir = Canvas.LEFT;
                }
            }
            uTraverse(dir);
            return;
        } else {
            ItemLFImpl v = null;
            synchronized (Display.LCDUILock) {
                if (numOfLFs == 0 || traverseIndex < 0) {
                    return;
                }
                
                v = itemLFs[traverseIndex];
                
            } // synchronized
    
            // SYNC NOTE: uCallKeyPressed may result in a call to the
            // application, so we make sure we do this outside of the
            // LCDUILock
            if (v != null) {
                // pass the keypress onto the current item
                v.uCallKeyPressed(keyCode);
            }
        }
    }

    /**
     * Handle a key release event
     *
     * @param keyCode the key which was released
     */
    void uCallKeyReleased(int keyCode) {
    	if (keyCode == Constants.KEYCODE_UP
            || keyCode == Constants.KEYCODE_DOWN
            || keyCode == Constants.KEYCODE_LEFT
            || keyCode == Constants.KEYCODE_RIGHT) 
        {
            return;
        } else {
            ItemLFImpl v = null;
            synchronized (Display.LCDUILock) {
                if (numOfLFs == 0 || traverseIndex < 0) {
                    return;
                }
                
                v = itemLFs[traverseIndex];
                
            } // synchronized
    
            // SYNC NOTE: uCallKeyPressed may result in a call to the
            // application, so we make sure we do this outside of the
            // LCDUILock
            if (v != null) {
                // pass the keypress onto the current item
                v.uCallKeyReleased(keyCode);
            }
        }
    }

    /**
     * Handle a key repeat
     *
     * @param keyCode the key code of the key which was repeated
     */
    void uCallKeyRepeated(int keyCode) {
        if (keyCode == Constants.KEYCODE_UP
            || keyCode == Constants.KEYCODE_DOWN
            || keyCode == Constants.KEYCODE_LEFT
            || keyCode == Constants.KEYCODE_RIGHT) 
        {
            int dir = KeyConverter.getGameAction(keyCode);
            
            // If 2D traversal is not enabled, we "direction-bend"
            // and traverse in a typewriter-like fashion, line-by-line
            // from left to right
            if (!Constants.TRAVERSAL2D) {
                if (dir == Canvas.DOWN) {
                    dir = Canvas.RIGHT;
                } else if (dir == Canvas.UP) {
                    dir = Canvas.LEFT;
                }
            }
            uTraverse(dir);
            return;
        } else {
            ItemLFImpl v = null;
            synchronized (Display.LCDUILock) {
                if (numOfLFs == 0 || traverseIndex < 0) {
                    return;
                }
                
                v = itemLFs[traverseIndex];
                
            } // synchronized
    
            // SYNC NOTE: uCallKeyPressed may result in a call to the
            // application, so we make sure we do this outside of the
            // LCDUILock
            if (v != null) {
                // pass the keypress onto the current item
                v.uCallKeyRepeated(keyCode);
            }
        }
    }

    
    /**
     * Return the item containing the pointer {x, y}
     * @param x - x demension
     * @param y - y demension
     * @return the item containing pointer,
     * if such item is not found returns null
     */
    private ItemLFImpl findItemByPointer(int x, int y) {
        ItemLFImpl item = null;
        for (int i = 0; i < numOfLFs; i++) {
            if (!itemLFs[i].shouldSkipTraverse()) {
                if (Constants.FINGER_TOUCH) {
                    int res1 = (itemLFs[i].itemAcceptPointer(x + viewable[X], y + viewable[Y]));

                    if (res1 == 0 || (res1 > 0 && i == numOfLFs - 1)) {
                        item = itemLFs[i];
                        break;
                    } else if (res1 > 0) {
                        int res2 = (itemLFs[i + 1].itemAcceptPointer(x + viewable[X], y + viewable[Y]));
                        if (res1 < res2 || res2 == -1) {
                            item = itemLFs[i];
                        } else if (res1 > res2) {
                            item = itemLFs[i + 1];
                        } else {
                             item = itemLFs[i].findNearestItem(itemLFs[i + 1],x);
                        }
                        break;
                    }
                } else if (itemLFs[i].itemContainsPointer(x + viewable[X], y + viewable[Y])) {
                    item = itemLFs[i];
                    break;
                }
            }
        }
        return item;
    }

    
    /**
     * Handle a pointer pressed event
     *
     * @param x The x coordinate of the press
     * @param y The y coordinate of the press
     */
    void uCallPointerPressed(int x, int y) {
        ItemLFImpl v = null;

        pointerIndicator = true;
        pointerX = x;
        pointerY = y;               

        synchronized (Display.LCDUILock) {
            if (numOfLFs == 0) {
                return;
            }
            
            v = findItemByPointer(x, y);
            pointerPressed = true;
        }
        
        // SYNC NOTE: this call may result in a call to the
        // application, so we make sure we do this outside of the
        // LCDUILock
        if (v != null) {
            x = (x + viewable[X]) - v.getInnerBounds(X);
            y = (y + viewable[Y]) - v.getInnerBounds(Y);
            v.uCallPointerPressed(x, y);

            uScrollToItem(v.item);
        } else {
            uRequestPaint();
        }
        
    }

    /**
     * Handle a pointer released event
     *
     * @param x The x coordinate of the release
     * @param y The y coordinate of the release
     */
    void uCallPointerReleased(int x, int y) {
        ItemLFImpl v = null;

        pointerIndicator = false;        

        synchronized (Display.LCDUILock) {
            if (numOfLFs == 0 || 
                traverseIndex < 0 || !pointerPressed) {
                return;
            }

            v = findItemByPointer(x, y);
            pointerPressed = false;

        }

        // SYNC NOTE: this call may result in a call to the
        // application, so we make sure we do this outside of the
        // LCDUILock
        if (v != null) {
            x = (x + viewable[X]) - v.getInnerBounds(X);
            y = (y + viewable[Y]) - v.getInnerBounds(Y);
            v.uCallPointerReleased(x, y);
        }

        uRequestPaint();        
    }

    void paintPointerIndicator(Graphics g, int x, int y) {
         // NTS: This may need to special case StringItem?
         g.setColor(ScreenSkin.COLOR_TRAVERSE_IND);
         g.drawArc(x - ScreenSkin.TOUCH_RADIUS, y - ScreenSkin.TOUCH_RADIUS, 2 * ScreenSkin.TOUCH_RADIUS, 2 * ScreenSkin.TOUCH_RADIUS, 0, 360);
    }


    /**
     * Handle a pointer dragged event
     *
     * @param x The x coordinate of the drag
     * @param y The y coordinate of the drag
     */
    void uCallPointerDragged(int x, int y) {
        ItemLFImpl v = null;

        synchronized (Display.LCDUILock) {
            if (numOfLFs == 0 || 
                traverseIndex < 0 || !pointerPressed) {
                return;
            }

            v = itemLFs[traverseIndex];

            x = (x + viewable[X]) - v.getInnerBounds(X);
            y = (y + viewable[Y]) - v.getInnerBounds(Y);

        }

        // SYNC NOTE: this call may result in a call to the
        // application, so we make sure we do this outside of the
        // LCDUILock
        v.uCallPointerDragged(x, y);
    }

    /**
     * Called to commit any pending user interaction for the current item.
     * Override the no-op in Displayable.
     */
    public void lCommitPendingInteraction() {
        if (traverseIndex >= 0) {
            itemLFs[traverseIndex].lCommitPendingInteraction();
        }
    }

    
    /**
     * Gets item currently in focus. This is will be only applicable to
     * Form. The rest of the subclasses will return null.
     * @return the item currently in focus in this Displayable;
     *          if there are no items in focus, null is returned
     */
    public Item lGetCurrentItem() {
        // SYNC NOTE: getCurrentItem is always called from within
        // a hold on LCDUILock
        return traverseIndex < 0 ? null : itemLFs[traverseIndex].item;
    }


    /**
     * Ensure CustomItems have their requested size cached.
     */
    void uEnsureRequestedSizes() {
        int i, count = 0;

        synchronized (Display.LCDUILock) {

            // Make a temporary copy of ItemLFs we need to collect sizes from
            ensureDispatchItemArray(numOfLFs);

            // Make sure each Item has native resource
            // and remember all the CustomItemLFImpls
            for (i = 0; i < numOfLFs; i++) {
                if (itemLFs[i] instanceof CustomItemLFImpl) {
                    // Remember this in temporary array
                    dispatchItemLFs[count++] = itemLFs[i];
                }
            }
        } // synchronized

        // Collect min and preferred sizes from CustomItems
        // SYNC NOTE: This may call into app code like
        // CustomItem.getPrefContentWidth(). So do it outside LCDUILock
        for (i = 0; i < count; i++) {
            ((CustomItemLFImpl)dispatchItemLFs[i]).uCallSizeRefresh();
        }

        // Dereference ItemLFImpl objects in dispatchItemLFs
        resetDispatchItemArray(true);
    }

    /**
     * Show all items and give focus to current item.
     * SYNC NOTE: caller must NOT hold LCDUILock since this function may
     * call into app code like getPrefContentWidth(), sizeChanged or paint()
     * of CustomItem. 
     * @param initialTraverse the flag to indicate this is the initial 
     *               traversal focus setup when this Form is being shown
     */
    void uShowContents(boolean initialTraverse) {

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "\nFormLFImpl: uShowContents()");
        }        

        synchronized (Display.LCDUILock) {
            if (firstShown) {
                for (int i = 0; i < numOfLFs; i++) {
                    itemLFs[i].cachedWidth = ItemLFImpl.INVALID_SIZE;
                }
            }
        }

        // Collect CustomItem sizes outside LCDUILock
        uEnsureRequestedSizes();

        ItemLFImpl[] itemsCopy = null;
        int itemsCopyCount = 0;
        int traverseIndexCopy = -1;
        
        synchronized (Display.LCDUILock) {
            keepFocusOnTheScreen = (traverseIndex != -1);
            
            if (firstShown) {
                super.layout(); // moved from LayoutManager
                LayoutManager.instance().lLayout(LayoutManager.FULL_LAYOUT,
                                                 itemLFs, numOfLFs,
                                                 viewport[WIDTH],
                                                 viewport[HEIGHT],
                                                 viewable);
                firstShown = false;
            } else {            
                LayoutManager.instance().lLayout(LayoutManager.UPDATE_LAYOUT,
                                                 itemLFs, numOfLFs,
                                                 viewport[WIDTH],
                                                 viewport[HEIGHT],
                                                 viewable);
            }
            if (resetToTop) {
                traverseIndex = -1;
                keepFocusOnTheScreen = false;
                viewable[Y] = 0;
                viewable[X] = 0;
            } else {
                // correct scroll position if any
                if (viewable[HEIGHT] <= viewport[HEIGHT] ||
                    viewable[Y] < 0) {
                    // if viewable height is less than viewport
                    // height just reset viewable y
                    // if viewable y is less than 0 set it to 0 
                    viewable[Y] = 0;
                } else if (viewable[Y] > (viewable[HEIGHT] - viewport[HEIGHT])) {
                    // if viewable y exceeds the max value set it to the max
                    // height just reset viewable y
                    viewable[Y] = viewable[HEIGHT] - viewport[HEIGHT];
                }
            }
            
            
            itemsCopy = new ItemLFImpl[numOfLFs];
            itemsCopyCount = numOfLFs;
            System.arraycopy(itemLFs, 0, itemsCopy, 0, numOfLFs);
            traverseIndexCopy = traverseIndex;
            itemsModified = false;

            if (pendingCurrentItem != null) {
                for (int i = 0; i < itemsCopyCount; i++) {
                    if (itemsCopy[i].item == pendingCurrentItem) {
                        traverseIndexCopy = i;
                        keepFocusOnTheScreen = true;
                        break;
                    }
                }
            }

        } // synchronized
        
        // We issue a default traverse to setup focus
        //
        // SYNC NOTE: Since this may call into CustomItem.traverse/traverseOut,
        // call it outside LCDUILock

                
        uInitItemsInViewport(CustomItem.NONE, itemsCopy, traverseIndexCopy);
        if (initialTraverse) {
            updateCommandSet();
        }

        for (int index = 0; index < itemsCopyCount; index++) {
            if (itemsCopy[index].sizeChanged) {
                itemsCopy[index].uCallSizeChanged(itemsCopy[index].getInnerBounds(WIDTH),
                        itemsCopy[index].getInnerBounds(HEIGHT));
                itemsCopy[index].sizeChanged = false;
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
            setupScroll();
            return;
        }
        
        uHideShowItems(itemsCopy); 

        // The result of an invalidate() call
        if (traverseIndexCopy != -1 && dir == CustomItem.NONE) {
            itemTraverse = 
                    uCallItemTraverse(itemsCopy[traverseIndexCopy], dir);
            
            uRequestPaint(); // request to paint contents area
            
            if (keepFocusOnTheScreen) {
                uScrollToItem(itemsCopy[traverseIndexCopy].item);
                synchronized (Display.LCDUILock) {
                    keepFocusOnTheScreen = false;
                }
            }
            setupScroll();
            return;
        }
        
        if (traverseIndexCopy > -1) {
            // If there is a traversable item, we go ahead and traverse
            // to it. We do *not* scroll at all under these circumstances
            // because we have just performed a fresh page view (or scroll)
            itemTraverse = uCallItemTraverse(itemsCopy[traverseIndexCopy], dir);
        }           
        if (!itemTraverse) {
            // If paging "down", we find the interactive item by moving
            // left to right - this ensures we move line by line searching
            // for an interactive item. When paging "up", we search from
            // right to left.

            int nextIndex = traverseIndexCopy, curIndex = traverseIndexCopy;
            int maxRate = traverseIndexCopy > -1 ?
                howMuchItemVisible(itemsCopy[traverseIndexCopy]) : 0;
            
            // Special case: It could be that no item in the current view
            // is interactive
            if (curIndex == -1 && (dir == Canvas.UP || dir == Canvas.LEFT)) { 
                // If traverseIndexCopy equals to -1 and  we are scrolling upwards, we artificially
                // set it to be the last item on the form (+1) so that it will be subsequently
                // decreased by 1 and start searching for an interactive item from the bottom
                // of the form upwards.
                curIndex = itemsCopy.length;
            } else if (curIndex == itemsCopy.length &&
                       (dir == Canvas.DOWN || dir == Canvas.RIGHT)) {
                // If the traverseIndexCopy equals to items.length. If we are scrolling downwards,
                // we artificially set it to be the first item on the form (-1) so that it will be
                // subsequently increased by 1 and start searching for an interactive item from the
                // top of the form downwards.
                curIndex = -1;
            }
            
            while (maxRate < 100) {
                curIndex = getNextInteractiveItem(itemsCopy,
                                                  dir == CustomItem.NONE ? Canvas.RIGHT : dir,
                                                  curIndex);
                if (curIndex > -1) {
                    int rate  = howMuchItemVisible(itemsCopy[curIndex]);
                    if (rate > maxRate) {
                        maxRate = rate;
                        nextIndex = curIndex;
                    }
                } else {
                    // no more interractive items on the screen
                    if (nextIndex > -1) {
                        int rate  = howMuchItemVisible(itemsCopy[nextIndex]);
                        if (rate == 0) {
                            nextIndex = curIndex;
                        } 
                    }
                    break;
                }
            }
            
            if (nextIndex != traverseIndexCopy && traverseIndexCopy > -1) {
                // It could be we need to traverse out of a current
                // item before paging
                itemsCopy[traverseIndexCopy].uCallTraverseOut();
                synchronized (Display.LCDUILock) {
                    traverseIndex = traverseIndexCopy = -1;  // reset real index
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
        }
         
        uRequestPaint(); // request to paint contents area                
        setupScroll();
    }
    
    /**
     * Notify the form that the content has been scrolled 
     * Form should notify all items. 
     */
    void scrollChanged() {
        ItemLFImpl[] itemsCopy = null;
        int itemsCopyCount = 0;
        int newX, newY;
        synchronized (Display.LCDUILock) {
            itemsCopy = new ItemLFImpl[numOfLFs];
            itemsCopyCount = numOfLFs;
            System.arraycopy(itemLFs, 0, itemsCopy, 0, numOfLFs);
            newX = viewable[X];
            newY = viewable[Y];
        } // synchronized
        
        for (int index = 0; index < itemsCopyCount; index++) {
            itemsCopy[index].uCallScrollChanged(newX, newY);
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
            !itemLFs[nextIndexInLFs].shouldSkipTraverse())
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
             * Need check that last condition in the above  "IF" is needed,
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
     * Loop through the set of items, making sure they have been
     * properly notified of their visibility via their show/hideNotify
     * methods.
     *
     * @param items the set of items to notify
     */
    void uHideShowItems(ItemLFImpl[] items) {
        // If an item is at least partially visible, it must have
        // its showNotify() called (if not already done). If an
        // item is completely offscreen, it must have its hideNotify()
        // called (if not already done).
        for (int i = 0; i < items.length; i++) {
            if (items[i].visible != itemPartiallyVisible(items[i])) {
                try {
                    if (items[i].visible) {
                        items[i].uCallHideNotify();
                    } else {
                        items[i].uCallShowNotify();
                    }
                } catch (Throwable t) {
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_HIGHUI,
                                   "Throwable while hideNotify or showNotify");
                    }
                }
            }
        }
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
        return !(item.bounds[Y] >= viewable[Y] + viewport[HEIGHT] ||
                 item.bounds[Y] + item.bounds[HEIGHT] <= viewable[Y]);
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
            (visibleArea[HEIGHT] * 100 / item.bounds[HEIGHT])
            : 0;
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
        return (item.bounds[Y] >= viewable[Y] && 
                item.bounds[Y] + item.bounds[HEIGHT] <= (viewable[Y] + viewport[HEIGHT]));
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
            
            while (true) {
                switch (dir) {
                case Canvas.UP:
                case Canvas.LEFT:
                    index--;
                    break;
                case Canvas.DOWN:
                case Canvas.RIGHT:
                    index++;
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
                if (items[index].shouldSkipTraverse()) {
                    continue;
                }
                
                if (itemPartiallyVisible(items[index])) {
                    break;
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
     * Find the nearest neighbor to the given Item index
     * moving upward
     *
     * @param items the set of items to search
     * @param index the index of the "anchor" to begin the search
     * @return the index of the nearest neighbor up
     */
    int findNearestNeighborUp(ItemLFImpl[] items, int index) {
        // SYNC NOTE: see traverse()
        if (index == 0) {
            return -1;
        }

        int a1 = items[index].bounds[X];
        int b1 = items[index].bounds[Y] - 1;
        int a2 = a1 + items[index].bounds[WIDTH];
        int b2 = b1 + items[index].bounds[HEIGHT] - 1;

        int x1, y1, x2, y2;
        int leastY = viewable[HEIGHT];
        
        while (true) {
            for (int i = index - 1; i >= 0; i--) {
                x1 = items[i].bounds[X];
                y1 = items[i].bounds[Y];
                x2 = x1 + items[i].bounds[WIDTH];
                y2 = y1 + items[i].bounds[HEIGHT];

                if (y1 < leastY) {
                    leastY = y1;
                }
                
                x1 = (a1 > x1) ? a1: x1;
                y1 = (b1 > y1) ? b1: y1;
                x2 = (a2 < x2) ? a2: x2;
                y2 = (b2 < y2) ? b2: y2;

                if (x2 >= x1 & y2 >= y1) {
                    return i;
                }
            }
            
            if (b1 < leastY) {
                break;
            }
        }

        return -1;
    }

    /**
     * Find the nearest neighbor to the given Item index
     * moving downward
     *
     * @param items the set of items to search
     * @param index the index of the "anchor" to begin the search
     * @return the index of the nearest neighbor down
     */
    int findNearestNeighborDown(ItemLFImpl[] items, int index) {
        // SYNC NOTE: see traverse()
        
        if (index == -1) {
            return 0;
        }

        int a1 = items[index].bounds[X];
        int b1 = items[index].bounds[Y];
        int a2 = a1 + items[index].bounds[WIDTH];
        int b2 = b1 + items[index].bounds[HEIGHT] + 1;

        int x1, y1, x2, y2;
        int greatestY = -1;

        while (true) {
            for (int i = index + 1; i < items.length; i++) {
                x1 = items[i].bounds[X];
                y1 = items[i].bounds[Y];
                x2 = x1 + items[i].bounds[WIDTH];
                y2 = y1 + items[i].bounds[HEIGHT];

                if (y2 > greatestY) {
                    greatestY = y2;
                }

                x1 = (a1 > x1) ? a1: x1;
                y1 = (b1 > y1) ? b1: y1;
                x2 = (a2 < x2) ? a2: x2;
                y2 = (b2 < y2) ? b2: y2;

                if (x2 >= x1 & y2 >= y1) {
                    return i;
                }
            }

            if (b2 > greatestY) {
                break;
            }
        }

        return -1;
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
            visRect[WIDTH] = viewport[WIDTH];
 
            // take the coordinates from the overall
            // coordinate space 
 
            int itemY1 = item.bounds[Y];
            int itemY2 = item.bounds[Y] + item.bounds[HEIGHT];
 
            // vpY1 the y coordinate of the top left visible pixel
            // current scroll position
            int vpY1 = viewable[Y];
            // vpY2 the y coordinate of bottom left pixel
            int vpY2 = vpY1 + viewport[HEIGHT];
                         
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
        
        // Whether the item performs an internal traversal or not,
        // it has the current input focus
        item.hasFocus = true;
        setVisRect(item, visRect);
        // Call traverse() outside LCDUILock
        if (item.uCallTraverse(dir,
                               viewport[WIDTH], viewport[HEIGHT], visRect)) {

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
            ItemLFImpl item = itemsCopy[traverseIndexCopy];
            item.setInternalCycle(this.numOfLFs == 1);
            itemTraverse = 
                    uCallItemTraverse(itemsCopy[traverseIndexCopy], dir);
                
            if (itemTraverse) {
                // We may have to scroll to accommodate the new
                // traversal location 
                if (scrollForBounds(dir, visRect)) {
                    uHideShowItems(itemsCopy);
                    uRequestPaint(); // request to paint contents area
                    setupScroll();
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
            uTraverseOutItem(traverseIndexCopy, itemsCopy);
            
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
                    uHideShowItems(itemsCopy);
                    uRequestPaint(); // request to paint contents area
                } else {
                    itemsCopy[traverseIndexCopy].uRequestPaint();
                }
            }
            // There is a special case when traversing to the very last
            // item on a Form
            if (traverseIndexCopy == (itemsCopy.length - 1)) {
                uSpecialCaseTraverseLastItem(traverseIndexCopy, itemsCopy); 
            } else if (traverseIndexCopy == 0) {
            // Likewise, there is a special case when traversing up to
            // the very first item on a Form            
                uSpecialCaseTraverseFirstItem(traverseIndexCopy, itemsCopy); 
            }
            
            setupScroll();
            updateCommandSet();
        } else {                      
            // There is no more interactive items wholly visible on
            // the current page. We may need to scroll to the next page,
            // if we do, then traverse out of the current item and 
            // scroll the page            
            if ((dir == Canvas.LEFT || dir == Canvas.UP) && viewable[Y] >= 0) {
                // Special case. We're at the top-most interactive item, but
                // its internal traversal doesn't allow the very top to be
                // seen, we just scroll the view to show it
                if (traverseIndexCopy != -1 && 
                    (viewable[Y] > itemsCopy[traverseIndexCopy].bounds[Y])) 
                {
                    viewable[Y] -= (viewport[HEIGHT] - PIXELS_LEFT_ON_PAGE);
                    if (viewable[Y] < 0) {
                        viewable[Y] = 0;
                    }
                    uHideShowItems(itemsCopy);
                    setupScroll();
                    uRequestPaint();
                } else {
                    //cycling up
                    if (!cyclingPageUp(traverseIndexCopy,itemsCopy)) {         		
                        // page up
                        pageScroll(Canvas.UP, traverseIndexCopy, itemsCopy);
                    }
                    return;
                }
            } else if ((dir == Canvas.RIGHT || dir == Canvas.DOWN) ) {
                // Special case. We're at the bottom-most interactive item,
                // but its internal traversal doesn't allow the very bottom
                // to be seen, we just scroll the view to show it
                boolean  isBottomShown = (viewable[Y] + viewport[HEIGHT] < viewable[HEIGHT]);
                if (traverseIndexCopy != -1 && isBottomShown &&
                    ((itemsCopy[traverseIndexCopy].bounds[Y] + 
                        itemsCopy[traverseIndexCopy].bounds[HEIGHT]) >
                    (viewable[Y] + viewport[HEIGHT]))) 
                {
                    viewable[Y] += (viewport[HEIGHT] - PIXELS_LEFT_ON_PAGE);
                    if (viewable[Y] > (viewable[HEIGHT] - viewport[HEIGHT])) 
                    {
                        viewable[Y] = viewable[HEIGHT] - viewport[HEIGHT];
                    }
                    uHideShowItems(itemsCopy);
                    setupScroll();
                    uRequestPaint();                    
                } else {
                    //cyclic down
                    if (!cyclingPageDown(traverseIndexCopy, itemsCopy) && isBottomShown) {
                        // page down
                        pageScroll(Canvas.DOWN, traverseIndexCopy, itemsCopy);
                    }
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
     * This method scroll page in <code>dir<code> direction
     * @param dir - the towards for scroll
     * @param traverseIndexCopy the index of the traverse item
     * @param itemsCopy the array of items
     */
    private void pageScroll(int dir, int traverseIndexCopy, 
        ItemLFImpl[] itemsCopy) {
        uScrollViewport(dir, itemsCopy);
        uInitItemsInViewport(
            dir, itemsCopy, traverseIndexCopy);
        updateCommandSet();
        return;
    }
    
    /**
     * This method move from the first item to the last item.
     * @param traverseIndexCopy the index of the traverse item
     * @param itemsCopy the array of items
     */
    private boolean cyclingPageUp(int traverseIndexCopy,
        ItemLFImpl[] itemsCopy) {
        boolean isCycle = false;
        if (viewable[Y] == 0 && itemsCopy.length > 0) {
            uTraverseOutItem(traverseIndexCopy, itemsCopy);
            
            int nextIndex = itemsCopy.length - 1;
            if (itemsCopy[nextIndex].shouldSkipTraverse()) {
                nextIndex = -1;
            }
            synchronized (Display.LCDUILock) {
                traverseIndex = nextIndex;
                traverseIndexCopy = nextIndex;
            }
            viewable[Y] = (viewable[HEIGHT] > viewport[HEIGHT]) ? 
                (viewable[HEIGHT] - viewport[HEIGHT]) : 0;  
            uInitItemsInViewport(Canvas.UP, itemsCopy, nextIndex);
            updateCommandSet();
             
            if (nextIndex != -1 &&viewable[HEIGHT] > viewport[HEIGHT]) {
     	        uSpecialCaseTraverseLastItem(traverseIndexCopy, itemsCopy);
     	    }
            uHideShowItems(itemsCopy);
            uRequestPaint(); // request to paint contents area
            setupScroll();
            isCycle = true;
        }
        return isCycle;
    }
    
    /**
     * This method move from the last item to the first item.
     * @param traverseIndexCopy the index of the traverse item
     * @param itemsCopy the array of items
     */
    private boolean cyclingPageDown(int traverseIndexCopy,
        ItemLFImpl[] itemsCopy) {
        boolean isCycle = false;
        if (itemsCopy.length > 0 && (viewable[Y] + viewport[HEIGHT] == viewable[HEIGHT] || 
            viewport[HEIGHT] >= viewable[HEIGHT])) {
            isCycle = true;
            uTraverseOutItem(traverseIndexCopy, itemsCopy);
            
            viewable[Y] = 0;
            //index of next interactive item 
            int nextIndex = 0;
            if (itemsCopy[0].shouldSkipTraverse()) {
                nextIndex = -1;
            }
            synchronized (Display.LCDUILock) {
                traverseIndex = nextIndex;
                traverseIndexCopy = nextIndex;
            } 
            //set up next interactive item or -1
            uInitItemsInViewport(Canvas.DOWN, itemsCopy, nextIndex);
            updateCommandSet();
            if (nextIndex == 0) {
                uSpecialCaseTraverseFirstItem(traverseIndexCopy, itemsCopy);
     	    }
            uHideShowItems(itemsCopy);
            uRequestPaint(); // request to paint contents area
            setupScroll();       
        }
        return isCycle;
    }
    
    /** This method traverse item in a special case when traversing to the very last
     *  item on a Form
     *  @param traverseIndexCopy the index of the traverse item
     *  @param itemsCopy the array of items
     */
    private void uSpecialCaseTraverseLastItem(int traverseIndexCopy,
        ItemLFImpl[] itemsCopy) {
        // There is a special case when traversing to the very last
        // item on a Form
        if (!itemCompletelyVisible(itemsCopy[traverseIndexCopy])) 
        {
            // Since its the last item, we may need to
            // perform a partial scroll to fit it.                
            if (viewable[Y] + viewport[HEIGHT] !=
                itemsCopy[traverseIndexCopy].bounds[Y] + 
                itemsCopy[traverseIndexCopy].bounds[HEIGHT])
            {
                viewable[Y] = viewable[HEIGHT] - 
                    viewport[HEIGHT];
                    
                // We make sure we don't go past the top of the
                // item, as we must have been going down to reach
                // the last item
                if (viewable[Y] > itemsCopy[traverseIndexCopy].bounds[Y]) {
                    viewable[Y] = itemsCopy[traverseIndexCopy].bounds[Y];
                }
                uHideShowItems(itemsCopy);
                uRequestPaint();
            }
        }
    }
    
    /** This method traverse item in a special case when traversing up to the very first
     *  item on a Form
     *  @param traverseIndexCopy the index of the traverse item
     *  @param itemsCopy the array of items
     */
    private void uSpecialCaseTraverseFirstItem(int traverseIndexCopy,
        ItemLFImpl[] itemsCopy) {
        // Since its the first item, we may need to
        // perform a partial scroll to fit it.
        if (viewable[Y] != itemsCopy[traverseIndexCopy].bounds[Y]) {
            viewable[Y] = itemsCopy[traverseIndexCopy].bounds[Y];
            
            // We make sure we don't go past the bottom of the
            // item, as we must have been going up to get to
            // the first item
            if (itemsCopy[traverseIndexCopy].bounds[HEIGHT] > 
                    viewport[HEIGHT])
            {
                viewable[Y] = 
                    itemsCopy[traverseIndexCopy].bounds[HEIGHT] -
                    viewport[HEIGHT];
            }
            uHideShowItems(itemsCopy);
            uRequestPaint();
        }		
    }
    
    /**
     * Traverse out of the  item
     * @param traverseIndexCopy the index of the traverse item
     * @param itemsCopy the array of items 
     */
    private void uTraverseOutItem(int traverseIndexCopy,
        ItemLFImpl[] itemsCopy) {
        if (traverseIndexCopy != -1) {
            itemsCopy[traverseIndexCopy].uCallTraverseOut();
            itemsCopy[traverseIndexCopy].uRequestPaint();
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
                
        if (dir == Canvas.UP) {
            int newY = viewable[Y] - (viewport[HEIGHT] - PIXELS_LEFT_ON_PAGE);
            if (newY < 0) {
                newY = 0;
            }
            
            // We loop upwards until we find the first item which is
            // currently at least partially visible
            int firstVis = items.length;
            for (int i = items.length - 1; i >= 0; i--) {
                if (items[i].visible) {
                    firstVis = i;
                }
            }

            if (firstVis == items.length) {
                viewable[Y] = newY;
                return;
            }
            
            // case 1. We're at the top of the item so just
            // traverse normally
            if (items[firstVis].bounds[Y] >= viewable[Y]) {
                viewable[Y] = newY;
                return;
            }
            
            // case 2. We try to fit as much of the partially visible
            // item onscreen as possible.
            int fitY = 
                (items[firstVis].bounds[Y] + items[firstVis].bounds[HEIGHT]) -
                viewport[HEIGHT];
                
            if (fitY > newY && viewable[Y] > fitY) {
                newY = fitY;
            } 
           
            viewable[Y] = newY;
            return;
            
        } else if (dir == Canvas.DOWN) {            
            int newY = viewable[Y] + (viewport[HEIGHT] - PIXELS_LEFT_ON_PAGE);
            if (newY > viewable[HEIGHT] - viewport[HEIGHT]) {
                newY = viewable[HEIGHT] - viewport[HEIGHT];
            }
            
            // We loop downwards until we find the last item which is
            // at least partially visible
            int lastVis = -1;
            for (int i = 0; i < items.length; i++) {
                if (items[i].visible) {
                    lastVis = i;
                }
            }
            
            // case 1. We're at the bottom of the item so just
            // traverse normally
            if (items[lastVis].bounds[Y] + items[lastVis].bounds[HEIGHT] <=
                viewable[Y] + viewport[HEIGHT])
            {
                viewable[Y] = newY;
                return;
            }

            // case 2. We try to fit as much of the partially visible
            // item onscreen as possible unless we're already at the top
            // of the item from a previous scroll
            if (newY > items[lastVis].bounds[Y] && 
                viewable[Y] < items[lastVis].bounds[Y]) 
            {
                newY = items[lastVis].bounds[Y];
            }
            
            viewable[Y] = newY;
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

        // There is a special case whereby the CustomItem
        // spec mandates the upper left corner of the internal
        // traversal rect be visible if the rect is larger than
        // the available viewport
        if (bounds[HEIGHT] >= viewport[HEIGHT] &&
            viewable[Y] != bounds[Y])
        {
            int maxViewableY = viewable[HEIGHT] - viewport[HEIGHT];
            viewable[Y] = (maxViewableY <= bounds[Y]) ? maxViewableY : bounds[Y];
            return true;
        }

        switch (dir) {
            case Canvas.LEFT:
            case Canvas.UP:
                if (bounds[Y] >= viewable[Y]) {
                    //cycling
                    if (viewable[Y] == 0 && bounds[Y] > viewable[HEIGHT] - viewport[HEIGHT] ) {
                        viewable[Y] = viewable[HEIGHT] - viewport[HEIGHT];
                        viewable[Y] = (viewable[Y] >=0) ? viewable[Y] : 0;
                        return true;
                    }
                    return false;
                }

                viewable[Y] -= (viewport[HEIGHT] - PIXELS_LEFT_ON_PAGE);
                if (viewable[Y] < 0) {
                    viewable[Y] = 0;
                }
                return true;
            case Canvas.RIGHT:
            case Canvas.DOWN:
                if (bounds[Y] + bounds[HEIGHT] <=
                    viewable[Y] + viewport[HEIGHT]) 
                {
                    //cycling 
                    if ((viewable[Y] == viewable[HEIGHT] - viewport[HEIGHT]) &&
                        (bounds[Y] <= bounds[HEIGHT])) {
                        viewable[Y] = 0;
                        return true;		
                    }
                    return false;
                }

                viewable[Y] += (viewport[HEIGHT] - PIXELS_LEFT_ON_PAGE);
                if (viewable[Y] > bounds[Y]) {
                    viewable[Y] = bounds[Y];
                }
                if (viewable[Y] + viewport[HEIGHT] > viewable[HEIGHT]) {
                    viewable[Y] = viewable[HEIGHT] - viewport[HEIGHT];
                    if (viewable[Y] < 0) {
                         viewable[Y] = 0;
                    }
                }
                return true;
            default:
                // for safety/completeness, don't scroll.
                Logging.report(Logging.WARNING,
                    LogChannels.LC_HIGHUI_FORM_LAYOUT,
                    "FormLFImpl: bounds, dir=" + dir);
                break;
        }
        return false;
    }

    /**
     * Determine if internal scrolling is needed for a given bounding box,
     * and perform such scrolling if necessary.

     * @param bounds
     * @return
     */
    boolean alignForBounds(int bounds[]) {

        boolean res = false;

        if (bounds == null || bounds[0] == -1) {
            return false;
        }

        if (bounds[Y] < viewable[Y]) {
            viewable[Y] = bounds[Y];
            res = true;
        } else if (viewable[Y] + viewport[HEIGHT] < bounds[Y] + bounds[HEIGHT]) {
            viewable[Y] = bounds[Y] - viewport[HEIGHT] + bounds[HEIGHT];
            res = true;
        }

        if (viewable[Y] + viewport[HEIGHT] > viewable[HEIGHT]) {
            viewable[Y] = viewable[HEIGHT] - viewport[HEIGHT];
        }

        if (viewable[Y] < 0) {
            viewable[Y] = 0;
        }

        return res;
    }

    /**
     * Calls uCallItemTraverseOut() and uCallHideNotify() when Form is
     * frozen or hidden
     */
    private void uCallItemHide() {
        
        // SYNC NOTE: Rather than make a complete copy of the set
        // of items on this form, we'll simply catch any exception
        // that occurs and move on. The only problem that could occur
        // would be items being deleted from the Form, which would
        // mean the application was removing items from the Form
        // while it was technically still visible.
        if (traverseIndex != -1) {
            try {
                itemLFs[traverseIndex].uCallTraverseOut();
            } catch (Throwable t) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_HIGHUI,
                                  "Throwable while traversing out");
                }
            }
        }
        
        // We need to loop through our Items and call hideNotify
        // on those that were visible
        for (int x = 0; x < numOfLFs; x++) {
            try {
                if (itemLFs[x].visible) {
                    itemLFs[x].uCallHideNotify();
                }
            } catch (Throwable t) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_HIGHUI,
                                  "Throwable while hideNotify");
                }
            }
        }
    }

    /**
     * Ensure that dispatchItemLFs array has enough space for use.
     * SYNC NOTE: This function must only be used in event dispatch thread.
     *
     * @param size maximum number of itemLFs needed
     */
    static void ensureDispatchItemArray(int size) {
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
    static void resetDispatchItemArray(boolean alsoShrink) {

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
     * Scroll content inside of the form.
     * @param scrollType scrollType. Scroll type can be one of the following
     * @see ScrollBarLayer.SCROLL_NONE 
     * @see ScrollBarLayer.SCROLL_PAGEUP
     * @see ScrollBarLayer.SCROLL_PAGEDOWN
     * @see ScrollBarLayer.SCROLL_LINEUP
     * @see ScrollBarLayer.SCROLL_LINEDOWN or
     * @see ScrollBarLayer.SCROLL_THUMBTRACK
     * @param thumbPosition
     */
    public void uCallScrollContent(int scrollType, int thumbPosition) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI,
                           "FormLF.uCallScrollContent scrollType=" + scrollType + 
                           " thumbPosition=" + thumbPosition); 
        }
        if (owner instanceof TextBox && itemLFs[0] instanceof TextBoxLFImpl) {
            ((TextBoxLFImpl)itemLFs[0]).uCallScrollContent(scrollType, thumbPosition);
        } else {
            super.uCallScrollContent(scrollType, thumbPosition);
        }
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
     * Set the vertical scroll indicators for this Screen
     */
    void setVerticalScroll() {
        if (owner instanceof TextBox && itemLFs[0] instanceof TextBoxLFImpl) {
            ((TextBoxLFImpl)itemLFs[0]).setVerticalScroll();
        } else {
            super.setVerticalScroll();
        }
        scrollChanged();
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
     */
    protected void uScrollViewport(int dir) {
        ItemLFImpl[] items = null;
        synchronized (Display.LCDUILock) {
            items = new ItemLFImpl[numOfLFs];
            System.arraycopy(itemLFs, 0, items, 0, numOfLFs);
        }
        int oldY = viewable[Y];
        uScrollViewport(dir, items);
        if (oldY != viewable[Y]) {
            uInitItemsInViewport(dir, items, traverseIndex);
            updateCommandSet();
        }
    }

    /**
     * Perform a line scrolling in the given direction. This method will
     * attempt to scroll the view to show next/previous line.
     *
     * @param dir the direction of the flip, either DOWN or UP
     */
    protected void uScrollByLine(int dir) {
        ItemLFImpl[] items = null;
        synchronized (Display.LCDUILock) {
            items = new ItemLFImpl[numOfLFs];
            System.arraycopy(itemLFs, 0, items, 0, numOfLFs);
        }
        int oldY = viewable[Y];
        super.uScrollByLine(dir);
        if (oldY != viewable[Y]) {
            uInitItemsInViewport(dir, items, traverseIndex);
            updateCommandSet();
        }
    }

    /**
     * Perform a scrolling at the given position. 
     * @param context position  
     */
    protected void uScrollAt(int position) {
        ItemLFImpl[] items = null;
        synchronized (Display.LCDUILock) {
            items = new ItemLFImpl[numOfLFs];
            System.arraycopy(itemLFs, 0, items, 0, numOfLFs);
        }
        int oldY = viewable[Y];
        super.uScrollAt(position);
        if (oldY != viewable[Y]) {
            uInitItemsInViewport(viewable[Y] > oldY ? Canvas.DOWN : Canvas.UP,
                                 items, traverseIndex);
            updateCommandSet();
        }
    }

    /**
     * A boolean declaring whether the contents of the viewport
     * can be traversed using the horizontal traversal keys,
     * ie, left and right
     */
    final static boolean TRAVERSE_HORIZONTAL = true;

    /**
     * A boolean declaring whether the contents of the viewport
     * can be traversed using the vertical traversal keys,
     * ie, up and down
     */
    final static boolean TRAVERSE_VERTICAL = true;
    
    /**
     * This is the rate at which the internal array of Items grows if
     * it gets filled up
     */
    static final int GROW_SIZE = 4;

    /**
     * This is the number of pixels left from the previous "page"
     * when a page up or down occurs
     */
    static final int PIXELS_LEFT_ON_PAGE = 15;
    
    /** The item index which has the traversal focus */
    int traverseIndex = -1;

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
     * true if a callPointerPressed event has occurred without
     * a corresponding callPointerReleased. false otherwise
     */
    boolean pointerPressed;

    /**
     * A flag indicating if scroll has been initialized. When the form
     * is first shown, it can only set the scroll after it has become
     * current, which is after show() and before paint(), but there's
     * hard to set the scroll then until the paint() routine - which
     * is not the best place to do it.
     */
    boolean scrollInitialized;

    /**
     * When a Form calls an Item's traverse() method, it passes in
     * an in-out int[] that represents the Item's traversal
     * bounds. This gets cached in the visRect variable
     */
    int[] visRect;

    /**
     * Array of ItemLFs that correspond to the array of items in Form
     */
    ItemLFImpl[] itemLFs;

    /**
     * Block size of the temporary array of ItemLFs used in dispatch.
     */
    final static int DISPATCH_ITEM_ARRAY_BLOCK = 10;

    /**
     * Temporary array of ItemLFs that is ONLY used in dispatch thread 
     * during show, hide and re-layout this Form.
     *
     * ensureDispatchItemArray() should be called before use and
     * resetDispatchItemArray() should be called when it is no longer needed,
     * to allow ItemLFImpl objects been GC'ed.
     */
    static ItemLFImpl[] dispatchItemLFs =
                new ItemLFImpl[DISPATCH_ITEM_ARRAY_BLOCK];

    /**
     * The number of views present in this FormLF.
     */
    int numOfLFs;
    
    /**
     * optimization flag
     */
    boolean firstShown = true;

    /**
     * flag indicates if the focused item is required to be bisible
     * in the current viewport 
     */
    boolean keepFocusOnTheScreen = false;

    /**
     * Item that was made visible using display.setCurrentItem() call
     * while FormLF was in HIDDEN or FROZEN state.
     */
    Item pendingCurrentItem; // = null

    boolean pointerIndicator;

    int pointerX;
    int pointerY;


} // class FormLFImpl
