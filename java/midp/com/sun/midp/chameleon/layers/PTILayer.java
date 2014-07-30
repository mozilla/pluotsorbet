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

package com.sun.midp.chameleon.layers;

import com.sun.midp.chameleon.*;
import javax.microedition.lcdui.*;
import com.sun.midp.chameleon.skins.PTISkin;
import com.sun.midp.lcdui.EventConstants;
import com.sun.midp.configurator.Constants;
import com.sun.midp.chameleon.input.*;

/**
 * A "PTILayer" layer is a special kind of layer which can
 * be visible when the predictive text input mode is active.
 * This layer is added to a MIDPWindow when more than one match
 * exists for the predictive input method. This layer lists the
 * possible words to give user a chance to select word you like.
 * User can traverse the list of words using up/down navigation
 * keys. User may press select bhutton to accept highlighted word.
 */
public class PTILayer extends PopupLayer {
    /** Options have to be listed in the popup dialog */
    private String[] list;

    /** Selected option number */
    private int selId;

    /** Instance of current input mode */
    private TextInputSession iSession;

    /** max text width visible on the screen */
    private int widthMax; 

    /** separator character between words within the list */
    private static final String SEPARATOR = " ";

    /** pointer is clicked outside of any area */
    private static final int OUT_OF_BOUNDS = -1;

    /** pointer is clicked to left arrow */
    private static final int LEFT_ARROW_AREA = 0;

    /** pointer is clicked to right arrow */
    private static final int RIGHT_ARROW_AREA = 1;

    /** pointer is clicked to the word inside of list */
    private static final int LIST_MATCHES_AREA = 2;

    /** Flag indicates that pointer release event should be processed */
    private boolean checkReleased ; //= false;
    /**
     * Create an instance of PTILayer
     * @param inputSession current input session
     */
    public PTILayer(TextInputSession inputSession) {
        super(PTISkin.IMAGE_BG, PTISkin.COLOR_BG);
        iSession = inputSession;
    }

    /**
     * The setVisible() method is overridden in PTILayer
     * so as not to have any effect. PopupLayers are always
     * visible by their very nature. In order to hide a
     * PopupLayer, it should be removed from its containing
     * MIDPWindow.
     * @param visible if true the pti layer has to be shown,
     * if false the layer has to be hidden
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /** PTI layer initialization: init selected id, calculate available size */
    protected void initialize() {
        super.initialize();

        setAnchor();
        selId = 0;
    }

    /**
     * Sets the anchor constants for rendering operation.
     */
    private void setAnchor() {
	if (owner == null) {
	    return;
	}
	bounds[W] = owner.bounds[W];
	bounds[H] = PTISkin.HEIGHT;
	bounds[X] = (owner.bounds[W] - bounds[W]) >> 1;
	bounds[Y] = owner.bounds[H] - bounds[H];
	
        widthMax = bounds[W] - PTISkin.MARGIN;
        if (PTISkin.LEFT_ARROW != null && PTISkin.RIGHT_ARROW != null) {
            widthMax -= 4 * PTISkin.MARGIN +
                PTISkin.LEFT_ARROW.getWidth() +
                PTISkin.RIGHT_ARROW.getWidth();
        }
    }

    /**
     * Set list of matches
     * @param l list of matches
     */
    public synchronized void setList(String[] l) {
        list = new String[l.length];
        System.arraycopy(l, 0, list, 0, l.length);
        visible = (list != null && list.length > 1);
        // IMPL_NOTE: has to be set externally as parameter 
        selId = 0;
        setDirty();
    }

    /**
     * Get list of matches
     * @return list of matches
     */
    public synchronized String[] getList() {
        return list;
    }

    /**
     * Handle key input from a keypad. Parameters describe
     * the type of key event and the platform-specific
     * code for the key. (Codes are translated using the
     * lcdui.Canvas) UP/DOWN/SELECT key press are processed if 
     * is visible. 
     *
     * @param type the type of key event
     * @param keyCode the numeric code assigned to the key
     * @return true if key has been handled by PTI layer, false otherwise
     */
    public boolean keyInput(int type, int keyCode) {
        boolean ret = false;
        String[] l = getList(); 
        if (( (type == EventConstants.PRESSED) ||
                (type == EventConstants.REPEATED) ) && visible) {
            switch (keyCode) {
            case Constants.KEYCODE_UP:
            case Constants.KEYCODE_LEFT:
                selId = (selId - 1 + l.length) % l.length;
                iSession.processKey(Canvas.UP, false);
                ret = true;
                break;
            case Constants.KEYCODE_DOWN:
            case Constants.KEYCODE_RIGHT:
                selId = (selId + 1) % l.length;
                iSession.processKey(Canvas.DOWN, false);
                ret = true;
                break;
            case Constants.KEYCODE_SELECT:
                iSession.processKey(keyCode, false);
                ret = true;
                break;
            default:
                break;
            }
        }
        // process key by input mode 
        requestRepaint();
        return ret;
    }

    /**
     * Get id of the word inside of the list selected by pointer
     * @param x - x coordinate of pointer
     * @param y - y coordinate of pointer
     * @return word index in the range of 0 and list length  - 1.
     * If the pointer does not point to any word  return -1
     */ 
    private int getWordIdAtPointerPosition(int x, int y) {
        String[] l = getList();
        int id = 0;
        int start = PTISkin.MARGIN;
        if (PTISkin.LEFT_ARROW != null) {
            start += PTISkin.LEFT_ARROW.getWidth();
        }
        
        while (id < l.length) {
            int w = PTISkin.FONT.stringWidth(SEPARATOR + l[id]); 
            if (x > start && x <= start + w) {
                break;
            }
            start += w;
            id++;
        }
        
        return id < l.length ? id : -1;
    }

    /**
     * Utility method to determine if this layer wanna handle
     * the given point. PTI layer handles the point if it
     * lies within the bounds of this layer.  The point should be in
     * the coordinate space of this layer's containing CWindow.
     *
     * @param x the "x" coordinate of the point
     * @param y the "y" coordinate of the point
     * @return true if the coordinate lies in the bounds of this layer
     */
    public boolean handlePoint(int x, int y) {
        return containsPoint(x, y);
    }
    
    /**
     * Get the layer area the pointer is clicked in
     * @param x - x coordinate of pointer
     * @param y - y coordinate of pointer
     * @return retuen the area. It can be either OUT_OF_BOUNDS or
     * LEFT_ARROW_AREA or RIGHT_ARROW_AREA or LIST_MATCHES_AREA
     */ 
    private int getAreaAtPointerPosition(int x, int y) {
        int area = OUT_OF_BOUNDS;
        if (x >= PTISkin.MARGIN && x <= bounds[W] - PTISkin.MARGIN) {
            if (PTISkin.LEFT_ARROW != null &&
                x <= PTISkin.MARGIN + PTISkin.LEFT_ARROW.getWidth()) {
                area = LEFT_ARROW_AREA; 
            } else if (PTISkin.RIGHT_ARROW != null &&
                       x >= bounds[W] - PTISkin.MARGIN -
                       PTISkin.RIGHT_ARROW.getWidth()) {
                area = RIGHT_ARROW_AREA; 
            } else {
                area = LIST_MATCHES_AREA; 
            }
        }
        return area;
    }
    
    /**
     * Allow this window to process pointer input. The type of pointer input
     * will be press, release, drag, etc. The x and y coordinates will 
     * identify the point at which the pointer event occurred in the coordinate
     * system of this window. This window will translate the coordinates
     * appropriately for each layer contained in this window. This method will
     * return true if the event was processed by this window or one of its 
     * layers, false otherwise.
     *
     * @param type the type of pointer event (press, release, drag)
     * @param x the x coordinate of the location of the event
     * @param y the y coordinate of the location of the event
     * @return true if this window or one of its layers processed the event
     */
    public boolean pointerInput(int type, int x, int y) {
        if (visible) {
            String[] l = getList();
            
            int area = getAreaAtPointerPosition(x, y);
            
            switch(type) {
            case EventConstants.PRESSED:
                switch (area) {
                case LEFT_ARROW_AREA:
                    selId = (selId - 1 + l.length) % l.length;
                    iSession.processKey(Canvas.UP, false);
                    requestRepaint();
                    break;
                case RIGHT_ARROW_AREA:
                    selId = (selId + 1) % l.length;
                    iSession.processKey(Canvas.DOWN, false);
                    requestRepaint();
                    break;
                case LIST_MATCHES_AREA:
                    // move focus to the selected word
                    int id = getWordIdAtPointerPosition(x, y);
                    if (id >= 0) {
                        checkReleased = true;
                        int i = selId;
                        if (id  > selId) {
                            while (i < id) {
                                iSession.processKey(Canvas.DOWN, false);
                                i++;
                            }
                        } else if (id  < selId) {
                            while (i > id) {
                                iSession.processKey(Canvas.UP, false);
                                i--;
                            }
                        }
                        requestRepaint();
                    }
                    break;
                }
                break;
            case EventConstants.RELEASED:
                if (area == LIST_MATCHES_AREA &&
                    checkReleased
                    // IMPL_NOTE: move the focus in the standart maner,
                    // doon't move the selected item at the head of the list 
                    // && getWordIdAtPointerPosition(x, y) == selId
                    ) {
                    iSession.processKey(Constants.KEYCODE_SELECT, false);
                    requestRepaint();
                }
                checkReleased = false;
                break;
            default:
                break;
            }
        }
        return true;
    }

    /**
     * Paint layer body.
     * @param g - Graphics
     */
    protected void paintBody(Graphics g) {
        String[] l = getList();
        if (l == null || l.length < 1)
            return;

        // draw outer frame
        g.setColor(PTISkin.COLOR_BDR);
        g.drawRect(0, 0, bounds[W] - 1, bounds[H] - 1);

        // draw arrows
        if (PTISkin.LEFT_ARROW != null) {
            g.drawImage(PTISkin.LEFT_ARROW, PTISkin.MARGIN, bounds[H] >> 1,
                        Graphics.VCENTER | Graphics.LEFT);
        }
        
        if (PTISkin.RIGHT_ARROW != null) {
            g.drawImage(PTISkin.RIGHT_ARROW, bounds[W] - PTISkin.MARGIN,
                        bounds[H] >> 1, Graphics.VCENTER | Graphics.RIGHT);
        }

        String text_b = "", text_a = "";

        for (int i = -1; ++i < l.length; ) {
            if (i < selId) {
                text_a += l[i] + SEPARATOR;
            } else if (i > selId) {
                text_b += l[i] + SEPARATOR;
            }
        }

        g.translate((bounds[W] - widthMax) >> 1, 0);
        g.setClip(0, 0, widthMax, bounds[H]);

        int x = 0;
        int y = (bounds[H] - PTISkin.FONT.getHeight()) >> 1;

        // prevent the overlapping of the outline 
        if (y <= 0) y = 1;
        
        // draw before words
        if (text_a.length() > 0) {
            g.setColor(PTISkin.COLOR_FG);
            g.drawString(text_a, x, y, Graphics.LEFT | Graphics.TOP);
            x += PTISkin.FONT.stringWidth(text_a);
        }

        if (l[selId].length() > 0) {
            // draw highlighted word
            // draw highlighted fill rectangle
            g.setColor(PTISkin.COLOR_BG_HL);

            g.fillRect(x - PTISkin.FONT.stringWidth(SEPARATOR) / 2,
                       y < PTISkin.MARGIN ? y : PTISkin.MARGIN,
                       PTISkin.FONT.stringWidth(l[selId] + SEPARATOR),
                       bounds[H] - (y < PTISkin.MARGIN ? y :
                                    PTISkin.MARGIN) * 2);

            g.setColor(PTISkin.COLOR_FG_HL);
            g.drawString(l[selId] + SEPARATOR, x, y,
                         Graphics.LEFT | Graphics.TOP);
            x += PTISkin.FONT.stringWidth(l[selId] + SEPARATOR);
            
        }

        // draw after words

        if (text_b.length() > 0) {
            g.setColor(PTISkin.COLOR_FG);
            g.drawString(text_b, x, y, Graphics.LEFT | Graphics.TOP);
        }

        g.translate(-((bounds[W] - widthMax) >> 1), 0);
        g.setClip(0, 0, bounds[W], bounds[H]);
    }

    /**
     * Update bounds of layer 
     * @param layers - current layer can be dependant on this parameter
     */
    public void update(CLayer[] layers) {
        super.update(layers);
        if (visible) {
            setAnchor();
            bounds[Y] -= (layers[MIDPWindow.BTN_LAYER].isVisible() ?
                    layers[MIDPWindow.BTN_LAYER].bounds[H] : 0) +
                    (layers[MIDPWindow.TICKER_LAYER].isVisible() ?
                            layers[MIDPWindow.TICKER_LAYER].bounds[H] : 0);

        }
    }
}


