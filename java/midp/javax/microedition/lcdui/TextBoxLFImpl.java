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

import com.sun.midp.lcdui.*;
import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

import com.sun.midp.chameleon.CGraphicsUtil;
import com.sun.midp.chameleon.input.*;
import com.sun.midp.chameleon.skins.*;
import com.sun.midp.chameleon.layers.ScrollBarLayer;

/**
 * This is the look &amps; feel implementation for TextBox.
 */
class TextBoxLFImpl extends TextFieldLFImpl implements TextFieldLF {

    /**
     * Contains line-break information for a blob of text
     */
    protected TextInfo myInfo; 
    
    /**
     * A flag indicating the scroll indicator has been initialized
     * for this textbox. This happens only once when the textbox
     * first paints its contents.
     */
    protected boolean scrollInitialized;

    /**
     * Creates TextFieldLF for the passed in TextField.
     * @param tf The TextField associated with this TextFieldLF
     */
    TextBoxLFImpl(TextField tf) {
        super(tf);

        if (myInfo == null) {
            myInfo = new TextInfo(4); // IMPL NOTE: add initial size to skin
        }
        drawsTraversalIndicator = false;
    }

    // *****************************************************
    // Public methods defined in interfaces
    // *****************************************************

    /**
     * Notifies L&F of a content change in the corresponding TextBox.
     */
    public void lSetChars() {
        cursor.index = tf.buffer.length(); // cursor at the end
        cursor.option = Text.PAINT_USE_CURSOR_INDEX;

        myInfo.scrollY = myInfo.isModified = true;
        updateTextInfo();
    }


    /**
     * Update text info if required
     *
     */
    private void updateTextInfo() {
        int w = contentBounds[WIDTH];
        int h = contentBounds[HEIGHT];
        // bounds are already initialized
        if (w > 0 && h > 0) {
            w -= 2 * TextFieldSkin.BOX_MARGIN + 2 * TextFieldSkin.PAD_H;
            h -= ((2 * TextFieldSkin.BOX_MARGIN) +     
                  (inputModeIndicator.getDisplayMode() != null ?
                   Font.getDefaultFont().getHeight() : 0));
            Text.updateTextInfo(tf.buffer.toString(),
                                ScreenSkin.FONT_INPUT_TEXT,
                                w, h, 0, Text.NORMAL,
                                cursor, myInfo);
            if (setVerticalScroll()) {
                lRequestInvalidate(true,true);
            } else {
                lRequestPaint();
            }
        }
    }

    /**
     * Set new cursor position. Update text info if cursor position is changed
     * @param pos new position                                                                        
     */
    protected void setCaretPosition(int pos) {
        int oldPos = cursor.index;
        super.setCaretPosition(pos);
        cursor.option = Text.PAINT_USE_CURSOR_INDEX;
        myInfo.isModified = myInfo.scrollY |= (oldPos != cursor.index);
        updateTextInfo();
    }


    /**
     * Commit the given input to this TextInputComponent's buffer.
     * This call constitutes a change to the value of this TextInputComponent
     * and should result in any listeners being notified.
     * @param input text to commit 
     */
    public void commit(String input) {
        // keep the first visible line 
        int oldTopVis = myInfo.topVis;
        super.commit(input);
        // try to restore the visible region
        myInfo.topVis = oldTopVis;
        myInfo.isModified = myInfo.scrollY = true;
        updateTextInfo();
    }

    
    /**
     * Notifies L&amps;F of a character insertion in the corresponding
     * TextBox.
     * @param data the source of the character data
     * @param offset the beginning of the region of characters copied
     * @param length the number of characters copied
     * @param position the position at which insertion occurred
     */
    public void lInsert(char data[], int offset, int length, int position) {
        if (data != null) {
            if (editable) {
                if (position <= cursor.index) {
                    cursor.index += length;
                    cursor.option = Text.PAINT_USE_CURSOR_INDEX;
                }
            }
            myInfo.isModified = myInfo.scrollY = true;
            updateTextInfo();
        }
    }

    /**
     * Notifies L&amsp;F of character deletion in the corresponding
     * TextField.
     * @param offset the beginning of the deleted region
     * @param length the number of characters deleted
     *
     * @exception IllegalArgumentException if the resulting contents
     * would be illegal for the current
     * @exception StringIndexOutOfBoundsException if <code>offset</code>
     * and <code>length</code> do not
     * specify a valid range within the contents of the <code>TextField</code>
     */
    public void lDelete(int offset, int length) {
        if (editable) {
            if (cursor.index >= offset) {
                int diff = cursor.index - offset;
                cursor.index -= (diff < length) ? diff : length;
                cursor.option = Text.PAINT_USE_CURSOR_INDEX;
            }
        }
        myInfo.isModified = myInfo.scrollY = true;
        updateTextInfo();
    }

    /**
     * Notifies L&amps;F of a maximum size change in the corresponding
     * TextBox.
     * @param maxSize - the new maximum size
     */
    public void lSetMaxSize(int maxSize) {
        if (editable) {
            if (cursor.index >= maxSize) {
                cursor.index = maxSize;
                cursor.option = Text.PAINT_USE_CURSOR_INDEX;
            }
        }
        myInfo.isModified = myInfo.scrollY = true;
        updateTextInfo();
    }

    /**
     * Notifies L&amps;F that constraints have to be changed.
     */
    public void lSetConstraints() {
        boolean wasEditable = editable;
    	setConstraintsCommon(false);
        
        setVerticalScroll();

        if (myInfo != null) {
            // reset cursor position if needed
            if (editable) {
                int pos = cursor.y / ScreenSkin.FONT_INPUT_TEXT.getHeight();
                int newPos = pos;
                // if text box has been uneditable before to reset cursor
                // position to the top of the screen
                if (!wasEditable) {
                    newPos = myInfo.topVis + 1;
                } else if (pos <= myInfo.topVis) {
                    newPos = myInfo.topVis + 1;
                } else if (pos > myInfo.topVis + myInfo.visLines) {
                    newPos = myInfo.topVis + myInfo.visLines;
                }
                if (newPos != pos) {
                    cursor.y = newPos  * ScreenSkin.FONT_INPUT_TEXT.getHeight();
                    cursor.option = Text.PAINT_GET_CURSOR_INDEX;
                    myInfo.isModified = myInfo.scrollY = true;
                    updateTextInfo();
                }
            } else {
                myInfo.isModified = myInfo.scrollY = true;
                updateTextInfo();
            }
        }
        lRequestPaint();
    }

    /**
     * Paint the text, linewrapping when necessary
     *
     * @param g the Graphics to use to paint with. If g is null then
     *        only the first four arguments are used and nothing is
     *        painted. Use this to return just the displayed string
     * @param dca the text to paint
     * @param opChar if opChar > 0 then an optional character to paint. 
     * @param constraints text constraints
     * @param font the font to use to paint the text
     * @param fgColor foreground color
     * @param w the available width for the text
     * @param h the available height for the text
     * @param offset the first line pixel offset
     * @param options any of Text.[NORMAL | INVERT | HYPERLINK | TRUNCATE]
     * @param cursor text cursor object to use to draw vertical bar
     * @param info TextInfo structure to use for paint
     */
    public void paint(Graphics g,
                      DynamicCharacterArray dca,
                      char opChar,
                      int constraints,
                      Font font,
                      int fgColor,
                      int w,
                      int h,
                      int offset,
                      int options,
                      TextCursor cursor,
                      TextInfo info) 
    {
        if (opChar != 0) {
            cursor = new TextCursor(cursor);
            info.isModified = true;            
        }
        
        String str = getDisplayString(dca, opChar, constraints,
                                      cursor, true);
        info.isModified |= !bufferedTheSameAsDisplayed(tf.constraints);
        
        Text.updateTextInfo(str, font, w, h, offset, options, cursor, info);

        Text.paintText(info, g, str, font, fgColor, 0xffffff - fgColor,
                       w, h, offset, options, cursor);
        
        // just correct cursor index if the charracter has
        // been already committed 
        if (str != null && str.length() > 0) {
            getBufferString(new DynamicCharacterArray(str),
                            constraints, cursor, true);
        }
        
        // has to be moved to correct place. It's incorrect to change 
        // the layer's dirty bounds in paint context 
        showPTPopup((int)0, cursor, w, h);
        showKeyboardLayer();
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
        int oldWidth = size[WIDTH];
        int oldHeight = size[HEIGHT];
        try {
            // We size to the maximum allowed, minus the padding
            // defined in the skin.
            size[WIDTH] = ((DisplayableLFImpl)tf.owner.getLF()).
                getDisplayableWidth() - 2 * TextFieldSkin.BOX_MARGIN;
                       
            // Note: tf.owner is the original TextBox for this LFImpl
            size[HEIGHT] = ((DisplayableLFImpl)tf.owner.getLF()).
                getDisplayableHeight() - 2 * TextFieldSkin.BOX_MARGIN;
        } catch (Throwable t) {
            // NOTE: the above call to getCurrent() will size the textbox
            // appropriately if there is a title, ticker, etc. Calling
            // this method depends on the textbox being current however.
            size[WIDTH] = 100;
            size[HEIGHT] = 100;
            // IMPL NOTE: Log this as an error
        }
        if (oldHeight != size[HEIGHT] || oldWidth != size[WIDTH]) {
            myInfo.scrollY = myInfo.isModified = true;
            updateTextInfo();
        }
    }

    /**
     * Paints the content area of this TextField.
     * Graphics is translated to contents origin.
     * @param g The graphics where Item content should be painted
     * @param width The width available for the Item's content
     * @param height The height available for the Item's content
     */
    void lPaintContent(Graphics g, int width, int height) {
        g.translate(TextFieldSkin.BOX_MARGIN, TextFieldSkin.BOX_MARGIN);
        width -= (2 * TextFieldSkin.BOX_MARGIN);
        height -= ((2 * TextFieldSkin.BOX_MARGIN) +     
                   (inputModeIndicator.getDisplayMode() != null ?
                    Font.getDefaultFont().getHeight() : 0));

        if (editable) {
            if (TextFieldSkin.IMAGE_BG != null) {
                CGraphicsUtil.draw9pcsBackground(g, 0, 0, width, height,
                    TextFieldSkin.IMAGE_BG);
            } else {
                CGraphicsUtil.drawDropShadowBox(g, 0, 0, width, height,
                    TextFieldSkin.COLOR_BORDER,
                    TextFieldSkin.COLOR_BORDER_SHD, 
                    TextFieldSkin.COLOR_BG);
            }
        } else {
            if (TextFieldSkin.IMAGE_BG_UE != null) {
                CGraphicsUtil.draw9pcsBackground(g, 0, 0, width, height,
                    TextFieldSkin.IMAGE_BG_UE);
            } else {
                CGraphicsUtil.drawDropShadowBox(g, 0, 0, width, height,
                    TextFieldSkin.COLOR_BORDER_UE,
                    TextFieldSkin.COLOR_BORDER_SHD_UE, 
                    TextFieldSkin.COLOR_BG_UE);
            }
        }

        // We need to translate by 1 more pixel horizontally 
        // to reserve space for cursor in the empty textfield
        g.translate(TextFieldSkin.PAD_H + 1, TextFieldSkin.PAD_V);

        // Input session should be retrieved from the current Display
        TextInputSession is = getInputSession();

        paint(g, tf.buffer,
              is.getPendingChar(),
              tf.constraints, 
              ScreenSkin.FONT_INPUT_TEXT, 
              (editable ? TextFieldSkin.COLOR_FG : TextFieldSkin.COLOR_FG_UE), 
              width - (2 * (TextFieldSkin.PAD_H)), height, 0,  
              Text.NORMAL, cursor, myInfo); 

        if (!scrollInitialized) {
            setVerticalScroll();
            scrollInitialized = true;
        }
        
        g.translate(-(TextFieldSkin.PAD_H + 1), -(TextFieldSkin.PAD_V));

        if (usePreferredX) {
            cursor.preferredX = cursor.x +
                ((myInfo.lineStart[myInfo.cursorLine] == cursor.index &&
                    cursor.index < tf.buffer.length()) ?
                 ScreenSkin.FONT_INPUT_TEXT.charWidth(
                                                      tf.buffer.charAt(cursor.index)) :
                 0);
        }
        

        g.translate(-TextFieldSkin.BOX_MARGIN, -TextFieldSkin.BOX_MARGIN);
    }

    /**
     * Get character index at the pointer position
     *
     * @param x pointer x coordinate
     * @param y pointer y coordinate
     * @return the character index
     */
    protected int getIndexAt(int x, int y) {
        x -= contentBounds[X] +
            TextFieldSkin.BOX_MARGIN +
            TextFieldSkin.PAD_H;
        y -= contentBounds[Y] +
            TextFieldSkin.BOX_MARGIN +
            TextFieldSkin.PAD_V;
        int id = -1;
        // the pointer is inside of the content 
        if (x >= 0 && y >= 0) {
            
            int numLines = myInfo.topVis + y / ScreenSkin.FONT_INPUT_TEXT.getHeight();
            id = tf.buffer.length();
            
            // the cursor has to be moved to the symbol the pointer is clicked at
            // if pointer is out of text just move the cursor at the last text position
            // if pointer is out of line just move the cursor at the last line position
            if (numLines < myInfo.numLines) {
                char[] data = tf.buffer.toCharArray();
                int i = 1;
                int startId = myInfo.lineStart[numLines];
                for (; i <= myInfo.lineEnd[numLines] - startId; i++) {
                    if (x <= ScreenSkin.FONT_INPUT_TEXT.charsWidth(data, startId, i)) {
                        break;
                    }
                }
                id = startId + i - 1;
            }
        }
        return id;
    }


    /**
     * Used internally to set the vertical scroll position
     */
    boolean setVerticalScroll() {
        ScreenLFImpl lf = null;
        if (tf != null &&
            tf.owner != null &&
            (lf = (ScreenLFImpl)tf.owner.getLF()) != null &&
            myInfo != null) {
            return lf.setVerticalScroll(myInfo.getScrollPosition(),
                                 myInfo.getScrollProportion());          
        }
        return false;
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
    void uCallScrollContent(int scrollType, int thumbPosition) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI,
                           "TextBoxLFImpl.uCallScrollContent scrollType=" + scrollType + 
                           " thumbPosition=" + thumbPosition); 
        }
        
        switch (scrollType) {
            case ScrollBarLayer.SCROLL_PAGEUP:
                uScrollViewport(Canvas.UP);
                break;
            case ScrollBarLayer.SCROLL_PAGEDOWN:
                uScrollViewport(Canvas.DOWN);
                break;
            case ScrollBarLayer.SCROLL_LINEUP:
                uScrollByLine(Canvas.UP);
                break;
            case ScrollBarLayer.SCROLL_LINEDOWN:
                uScrollByLine(Canvas.DOWN);
                break;
            case ScrollBarLayer.SCROLL_THUMBTRACK:
                uScrollAt(thumbPosition);
                break;
            default:
                break;
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
     */
    protected void uScrollViewport(int dir) {
        int lines = myInfo.scrollByPage(dir == Canvas.UP ?
                                        TextInfo.BACK : TextInfo.FORWARD);
        if (lines != 0) {
            if (editable) {
                // accept the word if the PTI is currently enabled
                acceptPTI();
                cursor.y += ScreenSkin.FONT_INPUT_TEXT.getHeight() * lines;
                cursor.option = Text.PAINT_GET_CURSOR_INDEX;
            }
            updateTextInfo();
        }
    }

    /**
     * Perform a line scrolling in the given direction. This method will
     * attempt to scroll the view to show next/previous line.
     *
     * @param dir the direction of the flip, either DOWN or UP
     */
    protected void uScrollByLine(int dir) {
        int oldTopVis = myInfo.topVis;
        if (myInfo.scroll(dir == Canvas.UP ? TextInfo.BACK : TextInfo.FORWARD)) {
            if (editable) {
                // accept the word if the PTI is currently enabled
                acceptPTI();

                cursor.y += (myInfo.topVis - oldTopVis) * ScreenSkin.FONT_INPUT_TEXT.getHeight();
                cursor.option = Text.PAINT_GET_CURSOR_INDEX;
            }
            updateTextInfo();
        }
    }

    /**
     * Perform a scrolling at the given position. 
     * @param context position  
     */
    protected void uScrollAt(int position) {
        int oldTopVis = myInfo.topVis;
        myInfo.topVis  = ((myInfo.height - myInfo.visLines * ScreenSkin.FONT_INPUT_TEXT.getHeight()) *
                          position / 100) / ScreenSkin.FONT_INPUT_TEXT.getHeight();
        
        if (myInfo.topVis < 0) {
            myInfo.topVis = 0;
        } else if (myInfo.topVis + myInfo.visLines > myInfo.numLines) {
            myInfo.topVis = myInfo.numLines - myInfo.visLines;
        }
        
        if (myInfo.topVis != oldTopVis) {
            if (editable) {
                // accept the word if the PTI is currently enabled
                acceptPTI();

                cursor.y += (myInfo.topVis - oldTopVis) * ScreenSkin.FONT_INPUT_TEXT.getHeight();
                cursor.option = Text.PAINT_GET_CURSOR_INDEX;
            }
            myInfo.isModified = myInfo.scrollY = true;
            updateTextInfo();
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

    void uCallTraverseOut() {
    }

    /**
     * Called by the system to notify this Item it is being hidden.
     * This function simply calls lCallHideNotify() after obtaining LCDUILock.
     */
    void uCallHideNotify() {
        super.uCallHideNotify();
        if (editable) {
            // TextBox can be hidden on activation of input method with
            // own Displayable, for example symbol table input method
            TextInputSession is = getInputSession();
            InputMode im = (is != null) ? is.getCurrentInputMode() : null;
            if (im == null || !im.hasDisplayable()) {
                disableInput();
                // IMPL_NOTE: problem with synchronization on layers and LCDUILock
                showIMPopup = false;
                disableLayers();
            }
        }
    }


    /**
     * Move the text cursor in the given direction
     *
     * @param dir direction to move
     * @return true if the cursor was moved, false otherwise
     */
    boolean moveCursor(int dir) {

        boolean keyUsed = false;

        switch (dir) {

	case Canvas.LEFT:
	    if (editable) {
            keyClicked(dir);
            if (ScreenSkin.RL_DIRECTION) {
                if (cursor.index < tf.buffer.length()) {
                    cursor.index++;
                }
            } else {
                if (cursor.index > 0) {
                    cursor.index--;
                }
            }
            cursor.option = Text.PAINT_USE_CURSOR_INDEX;
            myInfo.isModified = myInfo.scrollX = keyUsed = true;
        } else {
            if (ScreenSkin.RL_DIRECTION) {
                keyUsed = myInfo.scroll(TextInfo.FORWARD);
            } else {
                keyUsed = myInfo.scroll(TextInfo.BACK);
            }
        }
	    break;

	case Canvas.RIGHT:
	    if (editable) {
            keyClicked(dir);
            if (ScreenSkin.RL_DIRECTION) {
                if (cursor.index > 0) {
                    cursor.index--;
                }
            } else {
                if (cursor.index < tf.buffer.length()) {
                    cursor.index++;
                }
            }
            cursor.option = Text.PAINT_USE_CURSOR_INDEX;
            myInfo.isModified = myInfo.scrollX = keyUsed = true;
        } else {
            if (ScreenSkin.RL_DIRECTION) {
                 keyUsed = myInfo.scroll(TextInfo.BACK);
            } else {
                keyUsed = myInfo.scroll(TextInfo.FORWARD);
            }
        }
	    break;
	    
	case Canvas.UP:
	    if (editable) {
            keyClicked(dir);
            cursor.y -= ScreenSkin.FONT_INPUT_TEXT.getHeight();
            if (cursor.y > 0) {
                cursor.option = Text.PAINT_GET_CURSOR_INDEX;
                myInfo.isModified = myInfo.scrollY = keyUsed = true;
            } else { 
                cursor.y += ScreenSkin.FONT_INPUT_TEXT.getHeight();
            }
	    } else {
            keyUsed = myInfo.scroll(TextInfo.BACK);
	    }
        break;
        
        case Canvas.DOWN:
            if (editable) {
                keyClicked(dir);
                cursor.y += ScreenSkin.FONT_INPUT_TEXT.getHeight();
                if (cursor.y <= myInfo.height) {
                    cursor.option = Text.PAINT_GET_CURSOR_INDEX;
                    myInfo.isModified = myInfo.scrollY = keyUsed = true;
                } else {
                    cursor.y -= ScreenSkin.FONT_INPUT_TEXT.getHeight();
                }
            } else {
                keyUsed = myInfo.scroll(TextInfo.FORWARD);
            }
            break;
        default:
            // no-op
            break;
        }
        
        updateTextInfo();
        
        return keyUsed;
    }
     
    /**
     * Called by the system to notify this Item it is being shown
     *
     * <p>The default implementation of this method updates
     * the 'visible' state
     */
    void lCallShowNotify() {
        super.lCallShowNotify();
        this.scrollInitialized = false;
    }

    void uCallShowNotify() {
        super.uCallShowNotify();
        if (editable) {
            // TextBox can be shown after deactivation of input method with
            // own Displayable, for example symbol table input method
            TextInputSession is = getInputSession();
            InputMode im = (is != null) ? is.getCurrentInputMode() : null;
            if (im == null || !im.hasDisplayable()) {
                enableInput();
                // IMPL_NOTE: problem with synchronization on layers and LCDUILock
                showIMPopup = true;
                enableLayers();
            }
        }
    }
    
    /**
     * This is a utility function to calculate the anchor point
     * for the InputModeIndicator layer. Override TextFieldLFImpl
     * version for effeciency.
     * @return anchor (x, y, w, h)
     */
    protected int[] getInputModeAnchor() {
        ScreenLFImpl sLF = (ScreenLFImpl)tf.owner.getLF();
        
        int space = TextFieldSkin.BOX_MARGIN
                    + Font.getDefaultFont().getHeight();
        
        return new int[] {
            sLF.viewport[WIDTH] - TextFieldSkin.BOX_MARGIN - 4
                + getCurrentDisplay().getWindow().getBodyAnchorX(),
            getCurrentDisplay().getWindow().getBodyAnchorY(),
            sLF.viewport[HEIGHT] - space - 4,                    
            space};
    }

    /**
     * Returns true if the keyCode is used as 'enter' (user types in \n)
     * ('select' plays the role of 'enter' in some input modes).
     *
     * @param keyCode key code
     * @return true if key code is the one for newline, false otherwise
     */
    public boolean isNewlineKey(int keyCode) {
        return EventConstants.SYSTEM_KEY_SELECT ==
            KeyConverter.getSystemKey(keyCode);
    }



}
