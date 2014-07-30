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
import com.sun.midp.chameleon.skins.ScrollIndSkin;
import com.sun.midp.chameleon.skins.ScreenSkin;
import com.sun.midp.lcdui.EventConstants;
import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

import java.util.Timer;
import java.util.TimerTask;


/**
 * A ScrollBarLayer is a region of the display used for showing the Form's
 * and Alert's scroll bar.
 * 
 */
public class ScrollBarLayer extends ScrollIndLayer {
    /**
     * information of previous pointer event, used
     * for 'drag' event calculations
     */
    private int lastx = -1;
    private int lasty = -1;
    private int gap = 0;

    /** currect scroll type */
    private int scrollType = SCROLL_NONE;

    /** last proportion value */
    protected int proportion;

    /** last position value */
    protected int position;

    /** thumb top in the whole scrollbar's coordinates space */
    private int thumbY; 

    /** thumb height, calculated based on the viewable height */
    private int thumbHeight;
    
    /** The height of arrow area. IMPL_NOTE: has to be moved to skin */ 
    protected static final int ARROW_HEIGHT = 18;

    /** The min height of the thumb */ 
    protected static final int THUMB_HEIGHT_MIN = 12;

    /** The min number of pixels processed while thumb is dragging */ 
    protected static final int DRAG_MIN = 5;

    /** The bar height. Calculated from the scrolling layer height */ 
    int barHeight;

    /** delay for the arrow repaint to make arrow press visible for the user */
    protected static final int ARROW_PRESS_DELAY = 100; // 100 ms
    /** timer for the arrow repaint */
    protected Timer aT; // = null

    /**
     * Construct a new ScrollBarLayer, visible, but transparent
     */
    public ScrollBarLayer(CLayer layer, ScrollListener listener) {
        super(layer, listener);
        setBackground(null, ScrollIndSkin.COLOR_BG);
    }
    
    /**
     * Construct a new ScrollBarLayer, visible, but transparent
     */
    public ScrollBarLayer(CLayer layer) {
        this(layer, null);
    }

    /**
     * Set timer for the arrow repaint 
     */
    private void setTimer() {
        cancelTimer();
        if (scrollType == SCROLL_LINEDOWN ||
            scrollType == SCROLL_LINEUP) {
            if (visible) {
                requestRepaint(0, 0, bounds[W], ARROW_HEIGHT);
                requestRepaint(0, bounds[H] - ARROW_HEIGHT, bounds[W], ARROW_HEIGHT);
                aT = new Timer();
                aT.schedule(new ArrowTimerTask(), ARROW_PRESS_DELAY);
            }
        }
    }
    
    /**
     * Cancel timer for the arrow repaint 
     */
    private void cancelTimer() {
        if (aT != null) {
            aT.cancel();
            aT = null;
            scrollType = SCROLL_NONE;
            if (visible) {
                requestRepaint(0, 0, bounds[W], ARROW_HEIGHT);
                requestRepaint(0, bounds[H] - ARROW_HEIGHT, bounds[W], ARROW_HEIGHT);
            }
        }
    }

    /**
     * Timer task for the arrow repaint 
     */
    protected class ArrowTimerTask extends TimerTask {
        /**
         * Cancel timer as soon as it issues  
         */
        public void run() {
            cancelTimer();
        }
    }
    
    /**
     * Calculate layer bounds depending on the scrollable
     */
    public void setBounds() {
        if (scrollable != null) {
            int[] scrollableBounds = scrollable.getBounds();
            // NOTE : although we define different scrollbar width in RomizedProperties.java,
            // it seems that different with is not needed except for some popups.
            bounds[W] = ScrollIndSkin.WIDTH; 
            bounds[H] = scrollableBounds[H] - 1; // make it look better in all layers
            bounds[Y] = scrollableBounds[Y];
            int shift = 0;
            if ((scrollable instanceof PopupLayer)
                && !(scrollable instanceof MenuLayer))  {
                // the scrollbar in Choicegroup-popup and dateEditor popup should be smaller as limited space.
                bounds[W] -= 3;
                shift =  1; 
            }
            if (ScreenSkin.RL_DIRECTION){
                bounds[X] = scrollableBounds[X] - bounds[W] + shift;
            } else {
                bounds[X] = scrollableBounds[X] + scrollableBounds[W] - shift;
            }

            // the scrollbar move left one pixel as the docking layer draws its bound one pixel less                

                
            // the scrollbar move left one pixel as the alert draws its bound one pixel less
            if (alertMode) {
                bounds[X] -= 1; 
            }
            barHeight = bounds[H] - ARROW_HEIGHT * 2;
        }
    }

    /**
     * Set the current vertical scroll position and proportion.
     *
     * @param scrollPosition vertical scroll position.
     * @param scrollProportion vertical scroll proportion.
     */
    public void setVerticalScroll(int scrollPosition, int scrollProportion) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "ScrollBar.setVertical: pos = " + scrollPosition +
                    " prop = " + scrollProportion);
        }
        setVisible(scrollProportion < 100);
        if (position != scrollPosition || proportion != scrollProportion) {
            proportion = scrollProportion;
            position = scrollPosition;
        }
    }

    /**
     * Paint the scroll bar 
     * @param g the graphics context to paint in
     */
    protected void paintBody(Graphics g) {
        int arrowSignHeight = 4;
        int arrowSignWidth = 7;

        // draw up and down arrows
        g.setColor(scrollType == SCROLL_LINEUP ? ScrollIndSkin.COLOR_UP_ARROW :
                   ScrollIndSkin.COLOR_FG);
        g.fillRect(0, 0, bounds[W], ARROW_HEIGHT);

        g.setColor(scrollType == SCROLL_LINEDOWN ? ScrollIndSkin.COLOR_UP_ARROW :
                   ScrollIndSkin.COLOR_FG);
        g.fillRect(0, bounds[H] - ARROW_HEIGHT, bounds[W], ARROW_HEIGHT);

        g.setColor(ScrollIndSkin.COLOR_FRAME);
       
        g.drawLine(0, ARROW_HEIGHT, bounds[W], ARROW_HEIGHT);
        g.drawLine(0, bounds[H]-ARROW_HEIGHT, bounds[W], bounds[H] - ARROW_HEIGHT);

        int x2 = bounds[W] / 2;
        int y2 = ARROW_HEIGHT / 2 - arrowSignHeight / 2; 
        
        // draw down arrow
        g.setColor(scrollType == SCROLL_LINEUP ? ScrollIndSkin.COLOR_DN_ARROW :
                   ScrollIndSkin.COLOR_UP_ARROW);
        g.fillTriangle(x2 - arrowSignWidth / 2, y2 + arrowSignHeight - 1, 
                       x2, y2, 
                       x2+arrowSignWidth / 2, y2 + arrowSignHeight - 1);

        y2 = bounds[H] - ARROW_HEIGHT+ARROW_HEIGHT / 2+arrowSignHeight / 2; 
        
        // draw up arrow
        g.setColor(scrollType == SCROLL_LINEDOWN ? ScrollIndSkin.COLOR_DN_ARROW :
                   ScrollIndSkin.COLOR_UP_ARROW);

        g.fillTriangle(x2 - arrowSignWidth / 2, y2 - arrowSignHeight + 1, 
                       x2, y2, 
                       x2 + arrowSignWidth / 2, y2 - arrowSignHeight + 1);


        // draw thumb
        g.translate(0, ARROW_HEIGHT);

        thumbHeight = barHeight * proportion / 100;
        if (thumbHeight < THUMB_HEIGHT_MIN) {
            thumbHeight = THUMB_HEIGHT_MIN; 
        }
        thumbY = (barHeight - thumbHeight) * position / 100; //this value is in translated coordinates space
        
        if (thumbY+thumbHeight > barHeight) {
            thumbY = barHeight - thumbHeight;
        }

        g.setColor(ScrollIndSkin.COLOR_FG);
        g.fillRect(1, thumbY, bounds[W], thumbHeight);
        
        //3 horizontal stripes at center of thumb
        g.setColor(ScrollIndSkin.COLOR_FRAME);
        g.drawLine(3, thumbY - 2 + thumbHeight / 2, bounds[W] - 3 ,thumbY - 2 + thumbHeight / 2);
        g.drawLine(3, thumbY + thumbHeight / 2, bounds[W] - 3 , thumbY + thumbHeight / 2);
        g.drawLine(3, thumbY + 2 + thumbHeight / 2, bounds[W] - 3 ,thumbY + 2 + thumbHeight / 2);
        
        
        //draw the frame of the scroll bar
        g.setColor(ScrollIndSkin.COLOR_FRAME);
        g.drawLine(0, thumbY, bounds[W] - 1, thumbY);
        g.drawLine(0, thumbY + thumbHeight, bounds[W] - 1, thumbY+thumbHeight);

        g.translate(0, -ARROW_HEIGHT);

        g.drawLine(0, 0, 0, bounds[H]);
        if (true) { // IMPL_NOTE: add param drawBOrder
           g.drawRect(0, 0, bounds[W], bounds[H]);
        } 

        thumbY += ARROW_HEIGHT; //translate it to the whole scrollbar coordinates space!
    }

    /**
     * Determine he scroll type basing on the pointer coordinates
     * @param x - x coordinate 
     * @param y - y coordinate
     * @return the scroll type
     * The possible types of scrolling are 
     * @see #SCROLL_NONE
     * @see #SCROLL_LINEUP
     * @see #SCROLL_LINEDOWN
     * @see #SCROLL_PAGEUP
     * @see #SCROLL_PAGEDOWN
     * @see #SCROLL_THUMBTRACK
     */ 
    private int getScrollType(int x, int y) {
        int ret;
        if (x < 0 || x > bounds[W] ||
            y < 0 || y > bounds[H]) {
            ret = SCROLL_NONE;
        } else if (y < ARROW_HEIGHT) {
            ret = SCROLL_LINEUP;
        } else if (y > (bounds[H] - ARROW_HEIGHT)) {
            ret = SCROLL_LINEDOWN;
        } else if (y < thumbY) {
            ret = SCROLL_PAGEUP;
        } else if (y > thumbY + thumbHeight) {
            ret = SCROLL_PAGEDOWN;
        } else {
            ret = SCROLL_THUMBTRACK;
        }
        return ret;
    }
   
    /**
     * Handle input from a pen tap. Parameters describe
     * the type of pen event and the x,y location in the
     * layer at which the event occurred. Important : the
     * x,y location of the pen tap will already be translated
     * into the coordinate space of the layer.
     *
     * @param type the type of pen event
     * @param x the x coordinate of the event
     * @param y the y coordinate of the event
     */
    public boolean pointerInput(int type, int x, int y) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "Scroll.pointer type =" + type + ", x =" + x + ", y = " + y + "\n" +
                    "bounds[X] = " + bounds[X] + " bounds[Y] = " + bounds[Y] + " bonds[W] = " +
                    bounds[W] + " bounds[H] = " + bounds[H]);
        }

        switch (type) {
            case EventConstants.PRESSED:
                //        case EventConstants.HOLD:
                // no action for tap-and-hold in scrollbar
                // cancel timer for any press.
                cancelTimer();

                scrollType = getScrollType(x, y);
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                        "Pressed, scrollType=" + scrollType);
                }
                switch (scrollType) {

                    case SCROLL_LINEDOWN:
                    case SCROLL_LINEUP:
                        listener.scrollContent(scrollType, 0);
                        setTimer();
                        break;
                    case SCROLL_PAGEUP:
                    case SCROLL_PAGEDOWN:
                        listener.scrollContent(scrollType, 0);
                        break;
                    case SCROLL_THUMBTRACK:
                        gap = y - thumbY;
                        lastx = x;
                        lasty = y;
                        break;
                    case SCROLL_NONE:
                        break;
                }
                break;
            case EventConstants.RELEASED:
                scrollType = SCROLL_NONE;
                lastx = -1;
                lasty = -1;
                gap = 0;
                break;
            case EventConstants.DRAGGED:
                if (scrollType == SCROLL_THUMBTRACK) {
                    if (y < lasty - DRAG_MIN || y > lasty + DRAG_MIN ||
                        /* accumulate drag events till reaching DRAG_MIN
                   or till reaching drag boundaries */
                        y <= (ARROW_HEIGHT + gap) ||
                        y >= (bounds[H] - ARROW_HEIGHT - thumbHeight + gap)) {

                        lasty = y;
                        y = y - gap - ARROW_HEIGHT;
                        int pos = 100 * y / (barHeight - thumbHeight);
                        pos = (pos < 0) ? 0 : pos;
                        pos = (pos > 100) ? 100 : pos;
                        listener.scrollContent(SCROLL_THUMBTRACK, pos);
                    }
                }
                break;
            default:
                break;
        }

        /* we should process all of the pointer event inside scroll layer
 and don't pass it to underlying layer */
        return true;
    }

    /**
     * Set new scrollable 
     * @param layer new scrollable controlling the scrolling layer
     * @return true if the scrollable is changed, false - otherwise
     */
    public boolean setScrollable(CLayer layer) {
        boolean ret = super.setScrollable(layer);
        if (ret) {
            setBounds();
            position = 0;
            proportion = 0;
            lastx = -1;
            lasty = -1;
            gap = 0;
            scrollType = SCROLL_NONE;
        }
        return ret;
    }
}
