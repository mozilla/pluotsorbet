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

import com.sun.midp.lcdui.EventConstants;
import com.sun.midp.lcdui.Text;
import com.sun.midp.configurator.Constants;
import com.sun.midp.chameleon.skins.ChoiceGroupSkin;
import com.sun.midp.chameleon.skins.resources.ChoiceGroupResources;
import com.sun.midp.chameleon.skins.ScrollIndSkin;
import com.sun.midp.chameleon.skins.ScreenSkin;
import com.sun.midp.chameleon.layers.ScrollablePopupLayer;
import com.sun.midp.chameleon.layers.ScrollIndLayer;
import com.sun.midp.chameleon.CGraphicsUtil;
import com.sun.midp.chameleon.skins.resources.ScrollIndResourcesConstants;


/**
 * This is the Look &amps; Feel implementation for ChoiceGroupPopup.
 */
class ChoiceGroupPopupLFImpl extends ChoiceGroupLFImpl {

    /**
     * Creates ChoiceGroupPopupLF for the passed in choiceGroup of
     * Choice.POPUP type.
     * @param choiceGroup the ChoiceGroup object associated with this view
     */
    ChoiceGroupPopupLFImpl(ChoiceGroup choiceGroup) {
        super(choiceGroup);
        
        ChoiceGroupResources.load();
        
        viewable = new int[4];
        popupLayer = new CGPopupLayer(this);
    }

    /**
     * Sets the content size in the passed in array.
     * Content is calculated based on the availableWidth.
     * size[WIDTH] and size[HEIGHT] should be set by this method.
     * @param size The array that holds Item content size and location 
     *             in Item internal bounds coordinate system.
     * @param width The width available for this Item
     */
    public void lGetContentSize(int size[], int width) {

        // no label and empty popup => nothing is drawn
        // no elements => only label is drawn
        if (cg.numOfEls == 0) {
            size[WIDTH] = size[HEIGHT] = 0;
            return;
        }

        int w = getAvailableContentWidth(Choice.POPUP, width);
        int maxContentWidth = getMaxElementWidth(w);

        viewable[HEIGHT] = calculateHeight(w);

        int s = (selectedIndex < 0) ? 0 : selectedIndex;
        size[HEIGHT] = cg.cgElements[s].getFont().getHeight() + 
            (2 * ChoiceGroupSkin.PAD_V);

        if (maxContentWidth < w) {
            size[WIDTH] = width - w + maxContentWidth;
        } else {
            size[WIDTH] = width;
        }
        viewable[WIDTH] = size[WIDTH];
    }

    // *****************************************************
    //  Package private methods
    // *****************************************************

    /**
     * Paints the content area of the ChoiceGroup POPUP. 
     * Graphics is translated to contents origin.
     * @param g The graphics where Item content should be painted
     * @param width The width available for the Item's content
     * @param height The height available for the Item's content
     */
    void lPaintContent(Graphics g, int width, int height) {
        // paint closed state of the popup

        int textOffset = 0;

        // if there are no elements, we are done
        if (cg.numOfEls == 0) {
            return;
        }
        // draw background
        if (ChoiceGroupSkin.IMAGE_BG != null) {
            CGraphicsUtil.draw9pcsBackground(g, 0, 0, width, height,
                ChoiceGroupSkin.IMAGE_BG);
        } else {
            // draw widget instead of using images
            CGraphicsUtil.drawDropShadowBox(g, 0, 0, width, height,
                ChoiceGroupSkin.COLOR_BORDER,
                ChoiceGroupSkin.COLOR_BORDER_SHD,
                ChoiceGroupSkin.COLOR_BG);
        }

        // draw icon
        if (ChoiceGroupSkin.IMAGE_BUTTON_ICON != null) {
            int w = ChoiceGroupSkin.IMAGE_BUTTON_ICON.getWidth();
            int yOffset = height -
                ChoiceGroupSkin.IMAGE_BUTTON_ICON.getHeight();
            if (yOffset > 0) {
                yOffset = yOffset / 2;
            } else {
                yOffset = 0;
            }
            width -= (w + 1);

           if (ScreenSkin.RL_DIRECTION) {
              textOffset = 0;
            } else {
              textOffset = width;
            }
            if (ChoiceGroupSkin.IMAGE_BUTTON_BG != null) {
                CGraphicsUtil.draw9pcsBackground(
                    g, textOffset, 1, w, height - 2,
                    ChoiceGroupSkin.IMAGE_BUTTON_BG);
            }
            g.drawImage(ChoiceGroupSkin.IMAGE_BUTTON_ICON,
                        textOffset, yOffset + 1,
                        Graphics.LEFT | Graphics.TOP);
            width -= ChoiceGroupSkin.PAD_H;
        }

        g.translate(ChoiceGroupSkin.PAD_H, ChoiceGroupSkin.PAD_V);

        int s = selectedIndex < 0 ? 0 : selectedIndex;

        // paint value



        if (cg.cgElements[s].imageEl != null) {
            int iX = g.getClipX();
            int iY = g.getClipY();
            int iW = g.getClipWidth();
            int iH = g.getClipHeight();

            if (ScreenSkin.RL_DIRECTION) {
                textOffset = width;
            } else {
                textOffset = 0;
            }

            g.clipRect(textOffset, 0,
                       ChoiceGroupSkin.WIDTH_IMAGE,
                       ChoiceGroupSkin.HEIGHT_IMAGE);
            g.drawImage(cg.cgElements[s].imageEl,
                        textOffset, 0,
                        Graphics.LEFT | Graphics.TOP);
            g.setClip(iX, iY, iW, iH);

            if (ScreenSkin.RL_DIRECTION) {
                textOffset = 0;
            } else {
                textOffset = ChoiceGroupSkin.WIDTH_IMAGE +
                        ChoiceGroupSkin.PAD_H;
            }
        } else {
            textOffset = 0;
        }


        g.translate(textOffset, 0);
        Text.drawTruncString(g,
                        cg.cgElements[s].stringEl,
                        cg.cgElements[s].getFont(),
                        (hasFocus) ? ScreenSkin.COLOR_FG_HL :
                            ChoiceGroupSkin.COLOR_FG,
                        width);
        g.translate(-textOffset, 0);

        g.translate(-ChoiceGroupSkin.PAD_H, -ChoiceGroupSkin.PAD_V);

        if (popupLayer.isSizeChanged() && cachedWidth != INVALID_SIZE) {
            popupLayer.refresh();
            popupLayer.setSizeChanged(false);
        }
    }

    /**
     * Called by the system to indicate traversal has left this Item
     * This function simply calls lCallTraverseOut() after obtaining LCDUILock.
     */
    void uCallTraverseOut() {
        super.uCallTraverseOut();
        
        synchronized (Display.LCDUILock) {
            if (popupLayer.isPopupOpen()) {
                hilightedIndex = 0;
                popupLayer.hide();
            }
        }
    }

    /**
     * Handle traversal within this ChoiceGroup
     *
     * @param dir the direction of traversal
     * @param viewportWidth the width of the viewport
     * @param viewportHeight the height of the viewport
     * @param visRect the in/out rectangle for the internal traversal location
     * @return true if traversal occurred within this ChoiceGroup
     */
    boolean lCallTraverse(int dir, int viewportWidth, int viewportHeight,
                         int[] visRect) {

        boolean ret = false;
        // If we have no elements, or if the user pressed left/right,
        // don't bother with the visRect and just return false
        if (cg.numOfEls > 0) {

            // If we are a closed popup, don't bother with the visRect
            // and return true on the initial traverse, false on subsequent
            // traverses
            if (popupLayer.isPopupOpen()) {
                traversedIn = true;
                ret = super.lCallTraverse(dir, viewportWidth, viewportHeight, visRect);
            } else {
                 visRect[X] = 0;
                 visRect[Y] = 0;
                 visRect[HEIGHT] = bounds[HEIGHT];
                 visRect[WIDTH] = bounds[WIDTH];
            }
        }
        
        return ret;
    }

    /**
     * Handle traversal in the open popup
     * @param dir - the direction of traversal (Canvas.UP, Canvas.DOWN)
     * @param viewportWidth - the width of the viewport
     * @param viewportHeight - the height of the viewport
     * @return true if traverse event was handled, false - otherwise
     */
    boolean traverseInPopup(int dir, int viewportWidth, int viewportHeight) {
        boolean ret = false;
        if (popupLayer.isPopupOpen()) {
            if (cg.numOfEls > 1) {
                int prevIndex = hilightedIndex;
                int hilightY = 0;
                switch (dir) {
                case Canvas.UP:
                    if (hilightedIndex > 0) {
                        hilightedIndex--;
                    } else {
                        hilightedIndex = cg.numOfEls - 1;
                    }
                    break;
                case Canvas.DOWN:
                    if (hilightedIndex < (cg.numOfEls - 1)) {
                        hilightedIndex++;
                    } else {
                        hilightedIndex = 0;
                    }
                    break;
                default:
                    break;
                }
                
                if (ret = prevIndex != hilightedIndex) {
                    for (int i = 0; i < hilightedIndex; i++) {
                        hilightY += elHeights[i];
                    }
                    int y2= hilightY + elHeights[hilightedIndex];
                    
                    if (hilightY < viewable[Y]) {
                        viewable[Y] = hilightY;
                    } else if (y2 > viewable[Y] + viewportHeight) {
                        viewable[Y] = y2 - viewportHeight;
                    }
                    lRequestPaint();
                }
            } 
        } // popus is opened
        return ret;
    }

    /**
     * Handle a key press event
     *
     * @param keyCode the key which was pressed
     */
    void uCallKeyPressed(int keyCode) {

        Form form = null;

        synchronized (Display.LCDUILock) {

            if (cg.numOfEls == 0) {
                return;
            }
            
            if (!popupLayer.isPopupOpen()) {
                if (keyCode != Constants.KEYCODE_SELECT) {
                    return;
                }
                // show popup

                ScreenLFImpl sLF = (ScreenLFImpl)cg.owner.getLF();
                int top = getInnerBounds(Y) - sLF.viewable[Y] + contentBounds[Y];
                int bottom = sLF.viewport[HEIGHT] - contentBounds[HEIGHT] - top;

                int x = getInnerBounds(X) - sLF.viewable[X] + contentBounds[X] +
                    getCurrentDisplay().getWindow().getBodyAnchorX();
                int y = top + getCurrentDisplay().getWindow().getBodyAnchorY();
                hilightedIndex = selectedIndex > 0 ? selectedIndex : 0;
                    
                popupLayer.show(x, y,
                                contentBounds[WIDTH], contentBounds[HEIGHT],
                                viewable[WIDTH], viewable[HEIGHT],
                                top,
                                bottom);
            } else {

                // popup is closed when SELECT, LEFT or RIGHT is pressed;
                // popup selection is changed only when SELECT is pressed
                if (keyCode != Constants.KEYCODE_SELECT &&
                    keyCode != Constants.KEYCODE_LEFT && 
                    keyCode != Constants.KEYCODE_RIGHT) {
                    return;
                }

                // IMPL_NOTE Check if we need notification if selected element 
                // did not change
                if (keyCode == Constants.KEYCODE_SELECT) {
                    if (selectedIndex >= 0) {
                        lSetSelectedIndex(hilightedIndex, true);
                        form = (Form)cg.owner; // To be called outside the lock
                    }
                }
                hilightedIndex = 0;
                popupLayer.hide();
            }
            lRequestPaint();
        } // synchronized
        
        // Notify itemStateListener if necessary
        if (form != null) {
            form.uCallItemStateChanged(cg);
        }
    }

    /**
     * Check if the pointer is clicked to the item
     * @param x x coordinate of pointer 
     * @param y y coordinate of pointer
     * @return true if the item contains the pointer, otherwise - false
     */
    boolean itemContainsPointer(int x, int y) {
        if (!popupLayer.isPopupOpen()) {
            return super.itemContainsPointer(x, y);
        } else {
            // We grab the whole screen, so consider all clicks contained
            // by this item.
            return true;
        }
    }

    /**
     * Find the choice index inside of the list containing the pointer 
     * @param x x coordinate of pointer 
     * @param y y coordinate of pointer
     * @return choice index, -1 - if index is not found
     */
    int getIndexByPointer(int x, int y) {
        int popupLayer_bounds[]= popupLayer.getBounds();
        int id = -1;
        if (cg.numOfEls > 0) {
            ScreenLFImpl sLF = (ScreenLFImpl)cg.owner.getLF();
            x = x +(bounds[X] - sLF.viewable[X]) - popupLayer_bounds[X];
            y = y +(bounds[Y] - sLF.viewable[Y]) - popupLayer_bounds[Y];
            
            if (x >= 0 && x <= popupLayer_bounds[WIDTH] &&
                y >= 0 && y <= popupLayer_bounds[HEIGHT]) {
                int visY =  0;
                int i = 0;
                
                // calculate the scroll position and update the y coordinate accordingly.
                y += viewable[Y];
                
                for (i = 0; i < cg.numOfEls; i++) {
                    int h = elHeights[i];
                    if (y > visY && y < visY + h) {
                        break;
                    }
                    visY += h;
                }
                if (i < cg.numOfEls) {
                    id = i;
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
     */
    void uCallPointerPressed(int x, int y) {
        itemWasPressed = true;
        itemSelectedWhenPressed = false;
        if (popupLayer.isPopupOpen()) {
            // popupLayer.
            int i = getIndexByPointer(x, y);
            if (i >= 0) {
                itemSelectedWhenPressed = true;
                if (hilightedIndex != i) {
                    hilightedIndex = i;
                    uRequestPaint();//request paint as the highlighted changed
                    popupLayer.requestRepaint();//of course, we should repaint the popupLayer
                    getCurrentDisplay().serviceRepaints(cg.owner.getLF());
                }
            } else {
                hilightedIndex = 0;
                popupLayer.hide();
                uRequestPaint();
            }
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
        
        if (popupLayer.isPopupOpen()) {
            // do not dismiss the popup until a new selection is made.
            int i = getIndexByPointer(x, y);
            if ( (i >= 0 && hilightedIndex == i && itemSelectedWhenPressed) ||
                 (!itemSelectedWhenPressed)) {
                uCallKeyPressed(itemSelectedWhenPressed ?
                                // close the popup with highlighted item selected
                                Constants.KEYCODE_SELECT :
                                // close the popup as cancel
                                Constants.KEYCODE_RIGHT);
            }
        } else if (super.itemContainsPointer(x + bounds[X], y + bounds[Y])) {
            uCallKeyPressed(Constants.KEYCODE_SELECT);
        }
        itemSelectedWhenPressed = false;
        itemWasPressed = false;

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
        synchronized (Display.LCDUILock) {
            popupLayer.setSizeChanged(true);
        }
    }

    // *****************************************************
    //  Private methods
    // *****************************************************        

    /** pressed on a valid item in popup layer **/
    private boolean itemSelectedWhenPressed = false;

    /** The PopupLayer that represents open state of this ChoiceGroup POPUP. */
    CGPopupLayer popupLayer;

    /** 
     * Content of the popupLayer is drawn in ChoiceGroupPopupLFImpl.
     * That content can be taller then the layer itself.
     * viewable holds information of the current scroll position
     * and the size of the content (X, Y, WIDTH, HEIGHT)
     */
    int viewable[];

    // *****************************************************
    //  Inner class
    // *****************************************************
    /**
     * The following is the implementation of the ChoiceGroup POPUP
     * open state. The popup is shown and hidden on a KEYCODE_SELECT.
     * It is placed above or below the ChoiceGroup POPUP button (closed
     * state) depending on the space available. If there are too
     * many elements to display in the popup 
     * scrollbar will be added on the right.
     * If possible popup should be displayed below the button.
     * If possible the entire content of the popup should be seen.
     */
    class CGPopupLayer extends ScrollablePopupLayer {
        
        /**
         * CGPopupLayer constructor. Sets ChoiceGroupPopupLFImpl that
         * is associated with this CGPopupLayer.
         * @param lf - The ChoiceGroupPopupLFImpl associated with this 
         *             CGPopupLayer
         */
        CGPopupLayer(ChoiceGroupPopupLFImpl lf) {
            super(ScrollIndSkin.MODE == 
                    ScrollIndResourcesConstants.MODE_ARROWS ?
                  ChoiceGroupSkin.IMAGE_POPUP_BG : null, 
                  ChoiceGroupSkin.COLOR_BG);
            this.lf = lf;
        }
                
        /**
         * Initializes internal structures of CGPopupLayer.
         */
        protected void initialize() {
            super.initialize();
            viewport =  new int[4];
        }
        
        /**
         * Handles key event in the open popup
         * @param type - The type of this key event (pressed, released)
         * @param code - The code of this key event
         * @return true if the key event was handled and false - otherwise
         */
        public boolean keyInput(int type, int code) {
            if (type == EventConstants.PRESSED && lf != null) {

                if (code == Constants.KEYCODE_UP
                    || code == Constants.KEYCODE_DOWN) 
                {
                    if (lf.traverseInPopup(KeyConverter.getGameAction(code),
                                           viewport[WIDTH], 
                                           viewport[HEIGHT])) {
                        // the viewable[Y] is correct after traverseInPopup() calls,
                        // but we should update scroll position
                        updateScrollIndicator();
                        requestRepaint();
                        return true;
                    }
                }

                lf.uCallKeyPressed(code);
            }
            // PopupLayers always swallow all key events
            return true;
        }

        /**
         * Paints popup background (including borders) and scrollbar
         * if it is present
         * @param g - The graphics object to paint background on
         */
        protected void paintBackground(Graphics g) {
            super.paintBackground(g);
            // draw border if there is no background image
            if (bgImage == null) {
                g.setColor(ChoiceGroupSkin.COLOR_BORDER);
                g.drawRect(0, 0, bounds[W] - 1, bounds[HEIGHT] - 1);
              
                g.setColor(ChoiceGroupSkin.COLOR_BORDER_SHD);
                g.drawLine(1, 1, 1, bounds[HEIGHT] - 2); 
            }

            if (sbVisible && ScrollIndSkin.MODE ==
                    ScrollIndResourcesConstants.MODE_ARROWS) {
                int sbX;
                if (ScreenSkin.RL_DIRECTION) {
                    sbX = (ChoiceGroupSkin.WIDTH_SCROLL / 2) + 1;  
                } else {
                    sbX = bounds[WIDTH] -(ChoiceGroupSkin.WIDTH_SCROLL / 2) - 1;
                }
                int sbY = ChoiceGroupSkin.PAD_V;
                int sbH = bounds[HEIGHT] - (2 * ChoiceGroupSkin.PAD_V);
                int thumbY = sbY + 4 + 
                    ((lf.viewable[Y] * 
                      (sbH - 8 - ChoiceGroupSkin.HEIGHT_THUMB)) /
                     (lf.viewable[HEIGHT] - viewport[HEIGHT]));
                
                if (bgImage == null) {                    
                    // draw scrollbar with arrrows
                    g.setColor(ChoiceGroupSkin.COLOR_SCROLL);

                    int sbY2 = sbY + sbH - 1;
                    g.drawLine(sbX, sbY, sbX, sbY2);

                    g.drawLine(sbX - 2, sbY + 2, sbX - 1, sbY + 1);
                    g.drawLine(sbX + 1, sbY + 1, sbX + 2, sbY + 2);
                    g.drawLine(sbX - 2, sbY2 - 2, sbX - 1, sbY2 - 1);
                    g.drawLine(sbX + 1, sbY2 - 1, sbX + 2, sbY2 - 2);
                }

                // draw scrollbar thumb
                g.setColor(ChoiceGroupSkin.COLOR_THUMB);
                g.fillRect(sbX - (ChoiceGroupSkin.WIDTH_THUMB / 2), 
                           thumbY,
                           ChoiceGroupSkin.WIDTH_THUMB,
                           ChoiceGroupSkin.HEIGHT_THUMB);
            }
        }

        /**
         * Paints the content area of ChoiceGroup popup
         * @param g - The Graphics object to paint content on
         */
        protected void paintBody(Graphics g) {
            g.clipRect(viewport[X], viewport[Y],
                       viewport[WIDTH], viewport[HEIGHT]);

            g.translate(viewport[X] - lf.viewable[X],
                        viewport[Y] - lf.viewable[Y]);


            lf.lPaintElements(g, bounds[WIDTH], viewable[HEIGHT]);

            g.translate(-viewport[X] + lf.viewable[X],
                        -viewport[Y] + lf.viewable[Y]);
        }

        /**
         * Shows popup for the ChoiceGroup POPUP button that
         * is drawn at the passed in location. 
         * It will determine if popup will be drawn above or
         * below the ChoiceGroup POPUP button depending on the
         * passed in info.
         * @param buttonX - the x location of ChoiceGroup POPUP button
         *                  in BodyLayer's coordinate system.
         * @param buttonY - the y location of ChoiceGroup POPUP button
         *                  in BodyLayer's coordinate system.
         * @param buttonW - the width of ChoiceGroup POPUP button
         * @param buttonH - the height of ChoiceGroup POPUP button
         * @param elementsWidth - the width of the widest element in 
         *                        the popup
         * @param elementsHeight - the height of all elements if they are
         *                         drawn vertically one after another
         * @param top - the amount of space available for popup above the
         *              ChoiceGroup POPUP button
         * @param bottom - the amount of space available for popup below the
         *                 ChoiceGroup POPUP button
         */
        void show(int buttonX, int buttonY,
                  int buttonW, int buttonH,
                  int elementsWidth, int elementsHeight,
                  int top, int bottom) {
            // popup with all elements displayed fits under the popup button
            
            if (elementsHeight + 1 <= bottom - ChoiceGroupSkin.PAD_V) {
                setBounds(buttonX,
                          buttonY + buttonH - 1, // hide top border
                          buttonW,
                          elementsHeight + 2); // border width
                popupDrawnDown = true;
                sbVisible = false;

            // popup with all elements displayed fits above the popup button
            } else if (elementsHeight + 1 <= top - ChoiceGroupSkin.PAD_V) {
                setBounds(buttonX,
                          buttonY - elementsHeight - 1, // show top border
                          buttonW,
                          elementsHeight + 2); // border width
                popupDrawnDown = false;
                sbVisible = false;

            } else if (bottom > top) { // there is more space at the bottom
                setBounds(buttonX,
                          buttonY + buttonH - 1, // hide top border width
                          buttonW,
                          bottom - ChoiceGroupSkin.PAD_V);
                popupDrawnDown = true;
                sbVisible = true;

            } else { // there is more space at the top
                setBounds(buttonX,
                          buttonY - top + 1, // show top border
                          buttonW,
                          top - ChoiceGroupSkin.PAD_V + 1);
                popupDrawnDown = false;
                sbVisible = true;
            }
 
            // set viewport in popup's coordinate system
            viewport[X] = 2; // border width
            viewport[Y] = 1; // border width
            viewport[WIDTH]  = viewable[WIDTH];
            viewport[HEIGHT] = bounds[HEIGHT] - 2; // border width

            // ASSERT: since we are receiving key events,
            //         currentDisplay cannot be null.

            lf.getCurrentDisplay().showPopup(popupLayer);
            popUpOpen = true;

            if (ScrollIndSkin.MODE == ScrollIndResourcesConstants.MODE_BAR) {
                setScrollInd(ScrollIndLayer.getInstance(ScrollIndSkin.MODE));
                
//                setBackground(sbVisible ? null : ChoiceGroupSkin.IMAGE_POPUP_BG,
//                              ChoiceGroupSkin.COLOR_BG);
            }
            int newY = viewable[Y];
            if (newY > viewable[HEIGHT] - viewport[HEIGHT]) {
                newY = viewable[HEIGHT] - viewport[HEIGHT];
            }
            updatePopupLayer(newY);
        }

        /**
         * Hide popup for choice group 
         */
        
        void hide() {
            if (scrollInd != null) {
                scrollInd.setVisible(false);
                sbVisible = false;
                updateScrollIndicator();
                setScrollInd(null);
            }
            
            lf.getCurrentDisplay().hidePopup(popupLayer);
            popUpOpen = false;
        }

        
        /**
         * Scroll content inside of the CouiceGroup.
         * @param scrollType scrollType. Scroll type can be one of the following
         * @see ScrollIndLayer.SCROLL_NONE
         * @see ScrollIndLayer.SCROLL_PAGEUP
         * @see ScrollIndLayer.SCROLL_PAGEDOWN
         * @see ScrollIndLayer.SCROLL_LINEUP
         * @see ScrollIndLayer.SCROLL_LINEDOWN or
         * @see ScrollIndLayer.SCROLL_THUMBTRACK
         * @param thumbPosition
         */
        public void scrollContent(int scrollType, int thumbPosition) {
            switch (scrollType) {
                case ScrollIndLayer.SCROLL_PAGEUP:
                    uScrollViewport(Canvas.UP);
                    break;
                case ScrollIndLayer.SCROLL_PAGEDOWN:
                    uScrollViewport(Canvas.DOWN);
                    break;
                case ScrollIndLayer.SCROLL_LINEUP:
                    uScrollByLine(Canvas.UP);
                    break;
                case ScrollIndLayer.SCROLL_LINEDOWN:
                    uScrollByLine(Canvas.DOWN);
                    break;
                case ScrollIndLayer.SCROLL_THUMBTRACK:
                    uScrollAt(thumbPosition);
                    break;
                default:
                    break;
            }
        }

        /**
         * Perform a line scrolling in the given direction. This method will
         * attempt to scroll the view to show next/previous line.
         *
         * @param dir the direction of the flip, either DOWN or UP
         */
        private void uScrollByLine(int dir) {
            int newY = viewable[Y];
            switch (dir) {
            case Canvas.UP:
                newY -= PIXELS_LEFT_ON_PAGE;
                if (newY < 0) {
                    newY = 0;
                }
                break;
            case Canvas.DOWN:
                newY += PIXELS_LEFT_ON_PAGE;
                if (newY > viewable[HEIGHT] - viewport[HEIGHT]) {
                    newY = viewable[HEIGHT] - viewport[HEIGHT];
                }
                break;
            }
            updatePopupLayer(newY);
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
        private void uScrollViewport(int dir) {
            int newY = viewable[Y];
            switch (dir) {
            case Canvas.UP:
                newY -= viewport[HEIGHT] - PIXELS_LEFT_ON_PAGE;
                if (newY < 0) {
                    newY = 0;
                }
                break;
            case Canvas.DOWN:
                newY += viewport[HEIGHT] - PIXELS_LEFT_ON_PAGE;
                if (newY > viewable[HEIGHT] - viewport[HEIGHT]) {
                    newY = viewable[HEIGHT] - viewport[HEIGHT];
                }
                break;
            }
            updatePopupLayer(newY);
        }

        /**
         * Perform a scrolling at the given position.
         * @param position
         */
        void uScrollAt(int position) {
            int newY = (viewable[HEIGHT] - viewport[HEIGHT]) * position / 100;
            if (newY < 0) {
                newY = 0;
            } else if (newY > viewable[HEIGHT] - viewport[HEIGHT]) {
                newY = viewable[HEIGHT] - viewport[HEIGHT];
            }
            updatePopupLayer(newY);
        }

        /**
         * This method initiate repaint of the popup layer
         *
         * @param newY
         */
        private void updatePopupLayer(int newY) {
            viewable[Y] = newY;
            
            // correct hilighted index depending on new viewport. The hilighted item
            // always has to be visible
            if (hilightedIndex >= 0) {
                // calculate y coordinates of hilighted item

                int vy1 = viewable[Y];
                int vy2 = viewable[Y] + viewport[HEIGHT];
                for (int i = 0, y = 0;
                     i <= cg.numOfEls - 1;
                     i++, y += elHeights[i]) {
                    
                    if (y >= vy1) {
                        if (y + elHeights[i] <= vy2) {
                            if (hilightedIndex <= i) {
                                hilightedIndex = i;
                                break;
                            }
                        } else {
                            if (hilightedIndex >= i) {
                                hilightedIndex = i;
                                if (i > 0 && y > vy1) {
                                    hilightedIndex--;
                                } 
                                break;
                            }                            
                        }
                    } else if (y + elHeights[i] >= vy2) {
                        hilightedIndex = i;
                        break;
                    }
                }
            } 
            
            requestRepaint();
            updateScrollIndicator();
        }

        /**
         * Updates the scroll indicator.
         */
        public void updateScrollIndicator() {
            if (scrollInd != null) {
                if (sbVisible) {
                    scrollInd.setVerticalScroll(
                          (viewable[Y] * 100 / (viewable[HEIGHT] - viewport[HEIGHT])),
                          (viewport[HEIGHT] * 100 / viewable[HEIGHT]));
                } else {
                    scrollInd.setVerticalScroll(0, 100);
                }
                super.updateScrollIndicator();
            } 
        }


        /**
         * Handle pointer events 
         * @param type pointer event type 
         * @param x x coordinate of pointer 
         * @param y y coordinate of pointer
         * @return true if the event is processed and should not be passed
         * to other layers, false - otherwise 
         */
        public boolean pointerInput(int type, int x, int y) {
            ScreenLFImpl sLF = (ScreenLFImpl)lf.item.owner.getLF();
            int transX = x + this.bounds[X] + sLF.viewable[X] - lf.bounds[X];
            int transY = y + this.bounds[Y] + sLF.viewable[Y] - lf.bounds[Y];

            boolean consume = true;
            
            if (!containsPoint(x + bounds[X], y + bounds[Y])) {
                consume = false;
            }
            
            switch (type) {
            case EventConstants.PRESSED:
                lf.uCallPointerPressed(transX, transY);
                break;
            case EventConstants.RELEASED:
                lf.uCallPointerReleased(transX, transY);
                break;
            }
            return consume;
        }

        /**
         * Update bounds of popup anf show
         */
        public void refresh() {
            // show popup
            if (popUpOpen) {
                ScreenLFImpl sLF = (ScreenLFImpl) cg.owner.getLF();
                int top = getInnerBounds(Y) - sLF.viewable[Y] + contentBounds[Y];
                int bottom = sLF.viewport[HEIGHT] - contentBounds[HEIGHT] - top;
                int x = getInnerBounds(X) - sLF.viewable[X] + contentBounds[X] +
                    getCurrentDisplay().getWindow().getBodyAnchorX();
                int y = top + getCurrentDisplay().getWindow().getBodyAnchorY();

                popupLayer.show(x, y,
                                contentBounds[WIDTH], contentBounds[HEIGHT],
                                viewable[WIDTH], viewable[HEIGHT],
                                top,
                                bottom);
            }
        }

        /**
         *  Return sizeChanged flag
         * @return true if size change iccurs
         */
        public boolean isSizeChanged() {
            return sizeChanged;
        }

        /**
         *  Set sizeChanged flag
         * @param sizeChanged true if size change occurs
         */
        public void setSizeChanged(boolean sizeChanged) {
            this.sizeChanged = sizeChanged;
        }

        /**
         *  Return Popup layer flag
         * @return true if popup Layer is shown
         */
        public boolean isPopupOpen() {
            return popUpOpen;
        }

        /**
         *  Set popup Layer flag
         */
        public void setPopupOpen() {
            this.popUpOpen = true;
        }

        /** The ChoiceGroupPopupLFImpl associated with this popup */
        ChoiceGroupPopupLFImpl lf; // = null;

        /**
         * The viewport setting inside this popup (X, Y, WIDTH, HEIGHT).
         * It is set in layer's coordinate system.
         */
        private int viewport[]; // = null;

        /** True if popup is drawn below Popup button, false - otherwise */
        boolean popupDrawnDown; // = false;

        /** True if sb is present in the Popup layer, false - otherwise */
        private boolean sbVisible; // = false;

        // True if size of screen was changed
        private boolean sizeChanged;

        /** The state of the popup ChoiceGroup (false by default) */
        private boolean popUpOpen; // = false;

        /**
         * This is the number of pixels left from the previous "page"
         * when a page up or down occurs
         */
        static final int PIXELS_LEFT_ON_PAGE = 15;

    }

}

