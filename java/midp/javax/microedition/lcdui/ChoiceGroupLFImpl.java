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
import javax.microedition.lcdui.ChoiceGroup.CGElement;
import com.sun.midp.chameleon.skins.ChoiceGroupSkin;
import com.sun.midp.chameleon.skins.resources.ChoiceGroupResources;
import com.sun.midp.chameleon.skins.ScreenSkin;
import com.sun.midp.chameleon.layers.ScrollIndLayer;

/**
 * This is the look &amps; feel implementation for ChoiceGroup.
 */
class ChoiceGroupLFImpl extends ItemLFImpl implements ChoiceGroupLF {

    /**
     * Creates ChoiceLF for the passed in ChoiceGroup.
     * @param choiceGroup - the ChoiceGroup object associated with this view
     */
    ChoiceGroupLFImpl(ChoiceGroup choiceGroup) {
        super(choiceGroup);
        cg = choiceGroup;

        ChoiceGroupResources.load();
        
        if (cg.numOfEls > 0) {
            if (cg.choiceType != Choice.MULTIPLE) {
                selectedIndex = 0;
                cg.cgElements[selectedIndex].setSelected(true);        
            }
            hilightedIndex = -1;
        }
        contentX = getContentX(cg.choiceType);
        elHeights = new int[cg.numOfEls + ChoiceGroup.GROW_FACTOR];
    }

    // *******************************************************
    // ChoiceGroupLF implementation
    // ********************************************************

    /**
     * Sets the content size in the passed in array.
     * Content is calculated based on the availableWidth.
     * size[WIDTH] and size[HEIGHT] should be set by this method.
     * @param size The array that holds Item content size and location 
     *             in Item internal bounds coordinate system.
     * @param width The width available for this Item
     */
    public void lGetContentSize(int size[], int width) {
        // Allow lists to be as big at least as the view port
        if ((cg.owner != null) && (cg.owner instanceof List)) {
            size[WIDTH] = cg.owner.getLF().lGetWidth();
            int eHeight = calculateHeight(
                       getAvailableContentWidth(cg.choiceType, 
                                                size[WIDTH]));
            size[HEIGHT] = (cg.owner.getLF().lGetHeight() > eHeight ? 
                    cg.owner.getLF().lGetHeight() : eHeight);
        } else {

            if (cg.numOfEls == 0) {
                size[WIDTH] = size[HEIGHT] = 0;
            }

            int availableWidth = getAvailableContentWidth(cg.choiceType, 
                                                          width);

            int eHeight = calculateHeight(availableWidth);
            int maxContentWidth = getMaxElementWidth(availableWidth);

            if (maxContentWidth <= availableWidth) {
            // note that width - availableWidth equals
            // choice image area all horizontal padding;
            // thus width - availableWidth + maxContentWidth is
            // the width of the widest element in ChoiceGroup with padding
                size[WIDTH] = width - availableWidth + maxContentWidth;
            } else {
                size[WIDTH] = width;
            }
            size[HEIGHT] = eHeight;
        }
    }

    /**
     * Notifies L&F that an element was inserted into the 
     * <code>ChoiceGroup</code> at the elementNum specified.
     *
     * @param elementNum the index of the element where insertion occurred
     * @param stringPart the string part of the element to be inserted
     * @param imagePart the image part of the element to be inserted,
     * or <code>null</code> if there is no image part
     */
    public void lInsert(int elementNum, String stringPart, Image imagePart) {
        // Implicit, popup and exclusive 
        // (in those case there is always a selection)
        if (cg.choiceType != Choice.MULTIPLE) {
            if (selectedIndex == -1) {
                selectedIndex = 0;
                cg.cgElements[selectedIndex].setSelected(true);
            } else if (elementNum <= selectedIndex) {
                selectedIndex++;
            }
        }

        // set hilighted index (it always exists)
        if (hasFocus && (elementNum <= hilightedIndex || hilightedIndex == -1)) {
            hilightedIndex++;
        }
        
        // Note that cg.numOfEls is already + 1

        // elHeights is created in the constructor and cannot be null
        if (cg.numOfEls - 1 == elHeights.length) { // full capacity reached
            int[] newArray = 
                new int[cg.numOfEls + ChoiceGroup.GROW_FACTOR];
            System.arraycopy(elHeights, 0, newArray, 0, elementNum);
            System.arraycopy(elHeights, elementNum, newArray, elementNum + 1, 
                             cg.numOfEls - elementNum - 1);
            elHeights = newArray; // swap them

        } else if (elementNum != cg.numOfEls - 1) {
            // if we're not appending
            System.arraycopy(elHeights, elementNum, elHeights, elementNum + 1,
                             cg.numOfEls - elementNum - 1);
        }
                
        lRequestInvalidate(true, true);
    }

    /**
     * Notifies L&F that an element referenced by <code>elementNum</code>
     * was deleted in the corresponding ChoiceGroup.
     *
     * @param elementNum the index of the deleted element
     */
    public void lDelete(int elementNum) {
        if (cg.numOfEls == 0) {
            selectedIndex = -1;
            hilightedIndex = -1;
        } else {
                // adjust hilighted index
            if (elementNum < hilightedIndex) {
                hilightedIndex--;
            } else if (elementNum == hilightedIndex &&
                       hilightedIndex == cg.numOfEls) {
                hilightedIndex = cg.numOfEls - 1;
            }

            if (cg.choiceType != ChoiceGroup.MULTIPLE) {
                if (elementNum < selectedIndex) {
                    selectedIndex--;
                } else if (elementNum == selectedIndex &&
                           selectedIndex == cg.numOfEls) {
                    // last element is selected and deleted - 
                    // new last should be selected
                    selectedIndex = cg.numOfEls - 1;
                }
                cg.cgElements[selectedIndex].setSelected(true);
            }
        }

        // setup new elements array (note that numOfEls is already -1)
        if (elementNum != cg.numOfEls) {
            System.arraycopy(elHeights, elementNum + 1, elHeights,
                             elementNum, cg.numOfEls - elementNum);
        }
        
        // free some memory... (efficient for very large arrays) 
        if (elHeights.length > (ChoiceGroup.GROW_FACTOR * 10) &&
            elHeights.length / cg.numOfEls >= 2) {
            int[] newArray = new int[cg.numOfEls + ChoiceGroup.GROW_FACTOR];
            System.arraycopy(elHeights, 0, newArray, 0, cg.numOfEls);
            elHeights = newArray;
            newArray = null;
        }

        lRequestInvalidate(true, true);
    }

    /**
     * Notifies L&F that all elements 
     * were deleted in the corresponding ChoiceGroup.
     */
    public void lDeleteAll() {
        selectedIndex = hilightedIndex = -1;
        elHeights = new int[ChoiceGroup.GROW_FACTOR]; // initial size
        lRequestInvalidate(true, true);
        
    }

    /**
     * Notifies L&F that the <code>String</code> and 
     * <code>Image</code> parts of the
     * element referenced by <code>elementNum</code> were set in
     * the corresponding ChoiceGroup,
     * replacing the previous contents of the element.
     *
     * @param elementNum the index of the element set
     * @param stringPart the string part of the new element
     * @param imagePart the image part of the element, or <code>null</code>
     * if there is no image part
     */
    public void lSet(int elementNum, String stringPart, Image imagePart) {
        lRequestInvalidate(true, true);
    }


    /**
     * Notify this itemLF that its owner screen has changed.
     * Clear internal state if its new owner is null.
     *
     * @param oldOwner old owner screen before this change. New owner
     *                 can be found in Item model.
     */
    public void lSetOwner(Screen oldOwner) {
        super.lSetOwner(oldOwner);
        if (item.owner != null && item.owner instanceof List) {
            drawsTraversalIndicator = false;
        }
    }

    /**
     * Notifies L&F that an element was selected/deselected in the 
     * corresponding ChoiceGroup.
     *
     * @param elementNum the number of the element. Indexing of the
     * elements is zero-based
     * @param selected the new state of the element <code>true=selected</code>,
     * <code>false=not</code> selected
     */
    public void lSetSelectedIndex(int elementNum, boolean selected) {
        setSelectedIndex(elementNum, selected);
        lRequestPaint();
    }

    /**
     * Notifies L&F that selected state was changed on several elements 
     * in the corresponding MULTIPLE ChoiceGroup.
     * @param selectedArray an array in which the method collect the
     * selection status
     */
    public void lSetSelectedFlags(boolean[] selectedArray) {
        lRequestPaint();
    }

    /**
     * Notifies L&F that a new text fit policy was set in the corresponding
     * ChoiceGroup.
     * @param fitPolicy preferred content fit policy for choice elements
     */
    public void lSetFitPolicy(int fitPolicy) {
        lRequestInvalidate(true, true);
    }

    /**
     * Notifies L&F that a new font was set for an element with the 
     * specified elementNum in the corresponding ChoiceGroup.
     * @param elementNum the index of the element, starting from zero
     * @param font the preferred font to use to render the element
     */
    public void lSetFont(int elementNum, Font font) {
        lRequestInvalidate(true, true);
    }

    /**
     * Gets default font to render ChoiceGroup element if it was not
     * set by the application
     * @return - the font to render ChoiceGroup element if it was not 
     *           set by the app
     */
    public Font getDefaultFont() {
        return getTextFont(cg.choiceType, false);
    }

    /**
     * Gets currently selected index 
     * @return currently selected index
     */
    public int lGetSelectedIndex() {
        return selectedIndex;
    }


    /**
     * Gets selected flags (only elements corresponding to the 
     * elements are expected to be filled). ChoiceGroup sets the rest to
     * false
     * @param selectedArray_return to contain the results
     * @return the number of selected elements
     */
    public int lGetSelectedFlags(boolean[] selectedArray_return) {
        int countSelected = 0;
        for (int i = 0; i < cg.numOfEls; i++) {
            selectedArray_return[i] = cg.cgElements[i].selected;
            if (selectedArray_return[i]) {
                countSelected++;
            }
        }
        return countSelected;
    }


    /**
     * Determines if an element with a passed in index
     * is selected or not.
     * @param elementNum the index of an element in question
     * @return true if the element is selected, false - otherwise
     */
    public boolean lIsSelected(int elementNum) {
        return cg.cgElements[elementNum].selected;
    }

    // *****************************************************
    //  Package private methods
    // *****************************************************


    /**
     * Determine if this Item should have a newline after it
     *
     * @return true if it should have a newline after
     */
    boolean equateNLA() {
        if (super.equateNLA()) {
            return true;
        }
        return ((cg.layout & Item.LAYOUT_2) != Item.LAYOUT_2);
    }


    /**
     * Determine if this Item should have a newline before it
     *
     * @return true if it should have a newline before
     */
    boolean equateNLB() {
        if (super.equateNLB()) {
            return true;
        }

        return ((cg.layout & Item.LAYOUT_2) != Item.LAYOUT_2);
    }

    /**
     * Handle traversal within this ChoiceGroup
     *
     * @param dir the direction of traversal
     * @param viewportWidth the width of the viewport
     * @param viewportHeight the height of the viewport
     * @param visRect the in/out rectangle for the internal traversal location
     * @return True if traversal occurred within this ChoiceGroup
     */
    boolean lCallTraverse(int dir, int viewportWidth, int viewportHeight,
                          int[] visRect) 
    {
        boolean ret = super.lCallTraverse(dir, viewportWidth, viewportHeight, visRect);
        // all choice items are out of viewport.
        // Probably justr the label (if it's present) is visible on the screen
        int contentY = contentBounds[Y];
        int contentH = contentBounds[HEIGHT];


        if (contentY > visRect[Y] + visRect[HEIGHT] ||
            contentY + contentH < visRect[Y]) {
            return ret;
        }

        // If we have no elements - just return false
        
        if (cg.numOfEls > 0) {
            int newHeight = visRect[HEIGHT];
            int newHilightedIndex = hilightedIndex;
            if (isInternalCycle) {
                if (hilightedIndex == (cg.numOfEls-1) && Canvas.DOWN == dir) {
                    newHilightedIndex = 0;
                    lScrollToItem(visRect, newHilightedIndex); 
                } else if (newHilightedIndex == 0 && Canvas.UP == dir) {
                    newHilightedIndex = cg.numOfEls-1;
                    lScrollToItem(visRect, newHilightedIndex); 
                } 
            }
            int newY = contentY;
            boolean resetVisRect = false;

            if (traversedIn) {
                for (int i = 0; i < newHilightedIndex; i++) {
                    newY += elHeights[i];
                }
                newHeight = elHeights[newHilightedIndex];

                
                // highlighted index is out of visible rect
                // move highlight to the best place
                if (newY + newHeight > visRect[Y] + visRect[HEIGHT]) {
                    newHilightedIndex =
                        getIndexByPointer(visRect[X],
                                          visRect[Y] + visRect[HEIGHT] - 1);
                } else if (newY < visRect[Y]) {
                    newHilightedIndex = getIndexByPointer(visRect[X], visRect[Y] + 1);
                }

                resetVisRect = ret = (newHilightedIndex != hilightedIndex);
                
                // if the visRect does not contain highlighted item
                // don't adjust the highlighted index once again
                if (!ret) {
                    switch (dir) {
                    case Canvas.UP:
                        if (hilightedIndex > 0) {
                            if (newY >= visRect[Y]) {
                                newHeight = elHeights[--newHilightedIndex];
                                newY -=newHeight;
                            }
                            ret = true;
                        }
                        break;
                    case Canvas.DOWN:
                        if (hilightedIndex < (cg.numOfEls - 1)) {
                            if (newY + newHeight <= visRect[Y] + visRect[HEIGHT]) {
                                newY +=elHeights[newHilightedIndex];
                                newHeight = elHeights[++newHilightedIndex];
                            }
                            ret = true;
                        }
                        break;
                    case CustomItem.NONE:
                        // don't move the highlight 
                        ret = true;
                        break;
                    }
                }
            } else {

                if (cg.choiceType == Choice.IMPLICIT &&
                    pendingIndex == -1) {
                    pendingIndex = selectedIndex;
                }

                if (pendingIndex != -1) {
                    newHilightedIndex = pendingIndex;
                    pendingIndex = -1;
                } else if (newHilightedIndex == -1) {
                    newHilightedIndex = getIndexByPointer(contentBounds[X], dir == Canvas.UP ?
                                      contentY + contentH - 1 :
                                      contentY);
                }
                
                if (newHilightedIndex != -1) {
                    traversedIn = true;
                    ret = cg.numOfEls > 1;
                    resetVisRect = true;
                }
            }
            if (hilightedIndex != newHilightedIndex &&
                newHilightedIndex != -1) {

                if (resetVisRect) {
                    newY = contentY;
                    for (int i = 0; i < newHilightedIndex; i++) {
                        newY += elHeights[i];
                    }
                    newHeight = elHeights[newHilightedIndex];
                }

                if (cg.choiceType == Choice.IMPLICIT) {
                    setSelectedIndex(newHilightedIndex, true);
                }
                hilightedIndex = newHilightedIndex;
                lRequestPaint();
            }
            visRect[Y] = newY;
            visRect[HEIGHT] = newHeight;
        }
        return ret;
    }

    /**
     *  If hilighted element of item is not completely visible should make it visible
     * @param viewport
     * @param visRect the in/out rectangle for the internal traversal location
     * @return
     */
    boolean lScrollToItem(int[] viewport, int[] visRect) {
        return lScrollToItem(visRect, hilightedIndex ); 
    }

    /**
     *  Set visRect as need to pHilightedIndex
     * @param pHighlightedIndex the index of the highlighted element
     * @param visRect the in/out rectangle for the internal traversal location
     * @return true if 
     */
    boolean lScrollToItem(int[] visRect, int pHilightedIndex ) {
        int contentY = contentBounds[Y];

        if (cg.numOfEls > 0) {
            int newY = contentY + ChoiceGroupSkin.PAD_H;
            if (traversedIn) {
                for (int i = 0; i < pHilightedIndex; i++) {
                    newY += elHeights[i];
                }
             
                if (newY + elHeights[pHilightedIndex] > visRect[Y] + visRect[HEIGHT] || newY < visRect[Y]) {
                    visRect[Y] = bounds[Y] + newY;
                    visRect[HEIGHT] = elHeights[pHilightedIndex];
                    return true;
                }
            }
        }
        return false; 
    }

    /**
     * Traverse out of this ChoiceGroup
     */
    void lCallTraverseOut() {
        super.lCallTraverseOut();
        traversedIn = false;
        hilightedIndex = -1;
    }

    /**
     * Determine if Form should not traverse to this ChoiceGroup
     *
     * @return true if Form should not traverse to this ChoiceGroup
     */
    boolean shouldSkipTraverse() {
        if ((cg.label == null || cg.label.equals("")) &&
            (cg.numOfEls == 0)) {
            return true;
        }
        return false;
    }

    /**
     * Get the index of choice item contains the pointer 
     * @param x the x coordinate of the pointer
     * @param y the y coordinate of the pointer
     * @return the index of choice item
     */      
    int getIndexByPointer(int x, int y) {

        int id = -1;
        if (cg.numOfEls > 0) {
            //if pointer was dragged outside the item.
            if (contentBounds[X] <= x &&
                x <= contentBounds[X] + contentBounds[WIDTH] &&
                contentBounds[Y] <= y &&
                y <= contentBounds[Y] + contentBounds[HEIGHT]) { 
                int visY = contentBounds[Y];
                for (int i = 0; i < cg.numOfEls; i++) {
                    visY += elHeights[i];
                    if (visY >= y) {
                        id = i;
                        break;
                    }
                }
            }
        }
        return id;
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
        itemWasPressed = true;
        int i = getIndexByPointer(x, y);
        if (i >= 0) {
            hilightedIndex = pendingIndex = i;
            hasFocusWhenPressed = cg.cgElements[hilightedIndex].selected; 
            if (cg.choiceType == Choice.IMPLICIT) {               
                setSelectedIndex(hilightedIndex, true);
            }
            uRequestPaint();
            //            getCurrentDisplay().serviceRepaints(cg.owner.getLF()); //make the change shown immediately for better user experience
        }

    }

    /**
     * Called by the system to signal a pointer release
     *
     * @param x the x coordinate of the pointer up
     * @param y the x coordinate of the pointer up
     */
    void uCallPointerReleased(int x, int y) {
        if (!itemWasPressed)
            return;
        int i = getIndexByPointer(x, y);
        if (i == hilightedIndex) { // execute command only if no drag event occured
            if (cg.choiceType == Choice.IMPLICIT) {
                if (hasFocusWhenPressed || item.owner.numCommands <= 1) {
                    uCallKeyPressed(Constants.KEYCODE_SELECT);
                }
            } else {
                uCallKeyPressed(Constants.KEYCODE_SELECT);
            }
            uRequestPaint();
        }
        itemWasPressed = false;
    }
    
    /**
     * Handle a key press event
     *
     * @param keyCode the key which was pressed
     */
    void uCallKeyPressed(int keyCode) {
        Form form = null;
        List list = null;
        Command cmd = null;
        CommandListener cl = null;

        synchronized (Display.LCDUILock) {

            if (keyCode != Constants.KEYCODE_SELECT || (cg.numOfEls == 0)) {
                return;
            }

            switch (cg.choiceType) {
                case Choice.EXCLUSIVE:
                    if (hilightedIndex == selectedIndex || hilightedIndex < 0) {
                        return;
                    }
                    setSelectedIndex(hilightedIndex, true);
                    if (cg.owner instanceof Form) {
                        form = (Form)cg.owner; // notify itemStateListener
                    }
                    break;

                case Choice.MULTIPLE:
                    if (hilightedIndex < 0) {
                        return;
                    }
                    setSelectedIndex(hilightedIndex,
                        !cg.cgElements[hilightedIndex].selected);
                    if (cg.owner instanceof Form) {
                        form = (Form)cg.owner; // notify itemStateListener
                    }
                    break;

                case Choice.IMPLICIT:
                    list = (List)cg.owner;
                    if (list.listener != null && list.selectCommand != null) {
                        cl =  list.listener;
                        cmd = list.selectCommand;
                    }
                    break;
            }

            lRequestPaint();

        } // synchronized (LCDUILock)
         
        // For IMPLICIT List, notify command listener
        if (cl != null) {
            try {
                // SYNC NOTE: We lock on calloutLock around any calls
                // into application code.
                synchronized (Display.calloutLock) {
                    cl.commandAction(cmd, list);
                }
            } catch (Throwable thr) {
                Display.handleThrowable(thr);
            }
        } else if (form != null) {
            // For EXCLUSIVE and MULTIPLE CG, notify item state listener
            form.uCallItemStateChanged(cg);
        }
    }

    /**
     * Get the total element height of this CGroup
     *
     * @param width the desired width for this CG
     * @return the total element height
     */
    int calculateHeight(int width) {
        int eHeight = 0;
        for (int x = 0; x < cg.numOfEls; x++) {
            eHeight += calculateElementHeight(cg.cgElements[x], x, width);
        }
        return eHeight;

    }

    /**
     * Get the width of the widest element in choice group
     *
     * @param availableWidth The width available for rendering
     *        content of the element
     * @return the width of the widest element in the choice group
     */
    int getMaxElementWidth(int availableWidth) {
        int width = 0;
        int maxWidth = 0;
        
        for (int i = 0; i < cg.numOfEls; i++) {    
            width = contentX;
            if (cg.cgElements[i].imageEl != null) {
                width += ChoiceGroupSkin.WIDTH_IMAGE +
                    ChoiceGroupSkin.PAD_H;
            }
            
            if ((cg.cgElements[i].stringEl != null) && 
                (cg.cgElements[i].stringEl.length() > 0)) {
                width += (2 * ChoiceGroupSkin.PAD_H) +
                    Text.getWidestLineWidth(
                                            cg.cgElements[i].stringEl,
                                            width,
                                            availableWidth, 
                                            cg.cgElements[i].getFont());
            }
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        return maxWidth;
    }

    /**
     * Sets or unsets selection of an element with elementNum index.
     * @param elementNum index of the element which selection has to 
     *        to changed
     * @param selected - true if the element is to be selected,
     *                   false - otherwise
     */
    void setSelectedIndex(int elementNum, boolean selected) {
        if (cg.choiceType == Choice.MULTIPLE) {
            cg.cgElements[elementNum].setSelected(selected);
        } else {
            // selected item cannot be deselected in 
            // EXCLUSIVE, IMPLICIT, POPUP ChoiceGroup
            if (!selected || 
                (cg.choiceType != Choice.IMPLICIT && 
                 selectedIndex == elementNum)) {
                return;
            }

            if (hilightedIndex != elementNum &&
                elementNum >= 0 && cg.choiceType == Choice.IMPLICIT) {
                hilightedIndex = elementNum;
            }

            cg.cgElements[selectedIndex].setSelected(false);
            selectedIndex = elementNum;
            cg.cgElements[selectedIndex].setSelected(true);
        }
    }
    
    /**
     * Paints the content area of this ChoiceGroup. 
     * Graphics is translated to contents origin.
     * @param g The graphics where Item content should be painted
     * @param w The width available for the Item's content
     * @param h The height available for the Item's content
     */
    void lPaintContent(Graphics g, int w, int h) {
        lPaintElements(g, w, h);
    }

    /**
     * Paints the all the elements onto the graphics that translated to
     * the elements origin.
     * @param g The graphics where elements should be painted
     * @param w The width available for the elements content
     * @param h The height available for the elements content
     */
    void lPaintElements(Graphics g, int w, int h) {

        Image choiceImg;
        int textOffset;
        boolean hilighted;

        int cType = cg.choiceType;
        
        int contentW = getAvailableContentWidth(cType, w);
        int translatedY = 0;

        // IMPL_NOTE: Right now we have no vertical padding per element,
        // nor per line in the element
        // ChoiceImage and content images are drawn at y = 0

        // IMPL_NOTE: Content image area is always of PREFERRED_IMG_W and
        // PREFERRED_IMG_H (even if the image is smaller or there is space)
        // and it is always painted at y = 0 of the element

        // Note that lPaintElements is always called after 
        // ItemLFImpl.lDoInternalLayout() which will make sure that
        // calculateElementHeight() was called

        int mode = (cg.fitPolicy == Choice.TEXT_WRAP_OFF) ?
                    (Text.NORMAL | Text.TRUNCATE) : Text.NORMAL;

        int offSetX = ChoiceGroupSkin.PAD_H;

        // start for
        for (int iX, iY, iW, iH, i = 0; i < cg.numOfEls; i++) {

            // note that background was cleared
            // we will need to repaint background only for
            // hilighted portion

            choiceImg = getChoiceImage(cType,
                                       cType == Choice.MULTIPLE ?
                                           cg.cgElements[i].selected :
                                           i == selectedIndex);
            
            if (choiceImg != null) {
                if (ScreenSkin.RL_DIRECTION) {
                    g.drawImage(choiceImg, bounds[WIDTH]
                            - 2 * ChoiceGroupSkin.PAD_H - choiceImg.getWidth(),
                            0, Graphics.LEFT | Graphics.TOP);
                    offSetX = ChoiceGroupSkin.PAD_H;
                } else {
                    g.drawImage(choiceImg, 0, 0,
                            Graphics.LEFT | Graphics.TOP);
                    offSetX = ChoiceGroupSkin.PAD_H + choiceImg.getWidth();
                }
            } else {
                g.setColor(ChoiceGroupSkin.COLOR_FG);
                switch (cType) {
                    case Choice.MULTIPLE:
                        offSetX = ChoiceGroupSkin.PAD_H +
                            ChoiceGroupSkin.WIDTH_IMAGE;
                        g.drawRect(1, 1,
                                   ChoiceGroupSkin.WIDTH_IMAGE - 3,
                                   ChoiceGroupSkin.HEIGHT_IMAGE - 3);
                        if (cg.cgElements[i].selected) {
                            g.fillRect(3, 3,
                                ChoiceGroupSkin.WIDTH_IMAGE - 6,
                                ChoiceGroupSkin.HEIGHT_IMAGE - 6);
                        }
                        break;
                    case Choice.EXCLUSIVE:
                        offSetX = ChoiceGroupSkin.PAD_H +
                            ChoiceGroupSkin.WIDTH_IMAGE;
                        g.drawArc(1, 1,
                            ChoiceGroupSkin.WIDTH_IMAGE - 2,
                            ChoiceGroupSkin.HEIGHT_IMAGE - 2, 0, 360);
                        if (i == selectedIndex) {
                            g.fillArc(3, 3,
                                ChoiceGroupSkin.WIDTH_IMAGE - 5,
                                ChoiceGroupSkin.HEIGHT_IMAGE - 5, 0, 360);
                        }
                        break;
                }
            }
                g.translate(offSetX, 0);

            hilighted = (i == hilightedIndex && hasFocus);

            if (hilighted) {
                g.setColor(ScreenSkin.COLOR_BG_HL);
                g.fillRect(-ChoiceGroupSkin.PAD_H, 0,
                           ChoiceGroupSkin.PAD_H + contentW +
                           ChoiceGroupSkin.PAD_H,
                           elHeights[i]);
            }

            textOffset = 0;
            if (cg.cgElements[i].imageEl != null) {


                iX = g.getClipX();
                iY = g.getClipY();
                iW = g.getClipWidth();
                iH = g.getClipHeight();

                if (ScreenSkin.RL_DIRECTION) {
                    if (choiceImg != null) {
                        textOffset = w - ChoiceGroupSkin.WIDTH_IMAGE - choiceImg.getWidth() - 2 * ChoiceGroupSkin.PAD_H;
                    } else {
                        textOffset = w - ChoiceGroupSkin.WIDTH_IMAGE - ChoiceGroupSkin.PAD_H;                        
                    }
                }
                g.clipRect(textOffset, 0,
                           ChoiceGroupSkin.WIDTH_IMAGE,
                           ChoiceGroupSkin.HEIGHT_IMAGE);
                g.drawImage(cg.cgElements[i].imageEl,
                            textOffset , 0,
                            Graphics.LEFT | Graphics.TOP);
                g.setClip(iX, iY, iW, iH);
                textOffset = ChoiceGroupSkin.WIDTH_IMAGE +
                        ChoiceGroupSkin.PAD_H;
            }

            g.translate(0, -1);
            Text.paint(g, cg.cgElements[i].stringEl,
                       cg.cgElements[i].getFont(),
                       ChoiceGroupSkin.COLOR_FG,
                       ScreenSkin.COLOR_FG_HL,
                       contentW, elHeights[i], textOffset,
                       (hilighted) ? mode | Text.INVERT : mode, null);
            g.translate(-offSetX, elHeights[i] + 1);
            translatedY += elHeights[i];

        } // end for

        g.translate(0, -translatedY);
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
        return cg.choiceType == Choice.POPUP ? 
               super.labelAndContentOnSameLine(labelHeight) :
               false;
    }
    /**
     * Returns the font to use when rendering a choice element
     * based on the boolean hilighted flag being passed in.
     *
     * @param type choicegroup type used to decide what font to return
     * @param hilighted used to decide on hilighted font or normal font
     * @return the font to use to render text
    */
    public static Font getTextFont(int type, boolean hilighted) {
         return (hilighted) ? ChoiceGroupSkin.FONT_FOCUS : 
            ChoiceGroupSkin.FONT;
    }

    /**
     * Returns the available content width to render a choice element
     * for the passed in choicegroup type and given total width.
     *
     * @param type choicegroup type
     * @param w given width, used to calculate available width for content
     * @return the available content width to render a choice element
     */
    static int getAvailableContentWidth(int type, int w) {
        
        w -= (2 * ChoiceGroupSkin.PAD_H); // Implicit
        
        switch (type) {
        case Choice.EXCLUSIVE:
        case Choice.MULTIPLE:
            w -= (ChoiceGroupSkin.WIDTH_IMAGE + ChoiceGroupSkin.PAD_H);
            break;
            
        case Choice.POPUP:
            if (ChoiceGroupSkin.IMAGE_BUTTON_ICON != null) {
                w -= ChoiceGroupSkin.IMAGE_BUTTON_ICON.getWidth();
            } else {
                w -= 11;
            }
            break;
        }
        
        return w;
    }

    /**
     * Returns the x-location where the content should be rendered within
     * a choice element given the choicegroup type.
     *
     * @param type choicegroup type
     * @return the x-location where to start rendering choice element
     *         content
     */
    static int getContentX(int type) {
        switch (type) {
            case Choice.EXCLUSIVE:
            case Choice.MULTIPLE:
                return ((2 * ChoiceGroupSkin.PAD_H) +
                    ChoiceGroupSkin.WIDTH_IMAGE);            
        }
        return ChoiceGroupSkin.PAD_H;
    }

    /**
     * Returns the choice image (checkbox/radio button) based on the
     * passed in choicegroup type.
     *
     * @param type choicegroup type
     * @param on boolean indicating whether to return the 
     *           "CHOICE_ON" image or the "CHOICE_OFF" image
     * @return the CHOICE_ON or CHOICE_OFF image as requested
     */
    static Image getChoiceImage(int type, boolean on) {
        switch (type) {
        case Choice.EXCLUSIVE:
            if (ChoiceGroupSkin.IMAGE_RADIO == null) {
                return null;
            }
            return (on ? ChoiceGroupSkin.IMAGE_RADIO[1] : 
                ChoiceGroupSkin.IMAGE_RADIO[0]);
            
        case Choice.MULTIPLE:
            if (ChoiceGroupSkin.IMAGE_CHKBOX == null) {
                return null;
            }
            return (on ? ChoiceGroupSkin.IMAGE_CHKBOX[1] :  
                ChoiceGroupSkin.IMAGE_CHKBOX[0]);

        default: // IMPLICIT or POPUP
            return null;
        }
    }

    // *****************************************************
    //  Private methods
    // *****************************************************

    /**
     * Calculate height of an choice group element.
     *
     * @param cgEl the element to calculate
     * @param i index of the element
     * @param availableWidth the tentative width
     * @return the height under the given width
     */
    private int calculateElementHeight(CGElement cgEl, int i, 
                                       int availableWidth) 
    {

        // IMPL_NOTE there is an assumption here that text height is always
        // taller then the choice image and taller then the content image

        elHeights[i] = 0;

        int textOffset = (cgEl.imageEl == null) ? 0 : 
            ChoiceGroupSkin.WIDTH_IMAGE + 
            ChoiceGroupSkin.PAD_H;
        
        Font fnt = cgEl.getFont();
        
        if (cg.fitPolicy == ChoiceGroup.TEXT_WRAP_OFF) {
            elHeights[i] += fnt.getHeight();
        } else {
            elHeights[i] += Text.getHeightForWidth(cgEl.stringEl, fnt,
                                                    availableWidth, textOffset);
        }
 
        return elHeights[i];
    }

    /** ChoiceGroup associated with this ChoiceGroupLF */
    ChoiceGroup cg;

    /**
     * The currently selected index of this ChoiceGroup (-1 by default)
     */
    int selectedIndex = -1;

    /**
     * The currently highlighted index of this ChoiceGroup (-1 by default)
     */
    int hilightedIndex = -1;

    int pendingIndex = -1;
    /**
     * Stores the x-location of where the choice element content 
     * would begin.
     */
    private int contentX = 0;

    /**
     * The array containing the individual heights of each element,
     * based on the preferred layout width.
     */
    int[] elHeights;

    /**
     * A flag indicating if traversal has occurred into this
     * CG on a prior lCallTraverse. Its reset to false again
     * in lCallTraverseOut().
     */
    boolean traversedIn;

    boolean hasFocusWhenPressed; // = false 
}
