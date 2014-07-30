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

import java.util.TimerTask;
import java.util.Timer;

import javax.microedition.lcdui.game.Sprite;
import com.sun.midp.chameleon.skins.ScreenSkin;
import com.sun.midp.chameleon.skins.GaugeSkin;
import com.sun.midp.chameleon.skins.ProgressBarSkin;
import com.sun.midp.chameleon.skins.BusyCursorSkin;
import com.sun.midp.chameleon.skins.UpdateBarSkin;
import com.sun.midp.chameleon.skins.resources.GaugeResources;
import com.sun.midp.chameleon.skins.resources.ProgressBarResources;
import com.sun.midp.chameleon.skins.resources.UpdateBarResources;
import com.sun.midp.chameleon.skins.resources.BusyCursorResources;
import com.sun.midp.chameleon.*;
import com.sun.midp.lcdui.Text;
import com.sun.midp.configurator.Constants;
import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
* This is the look & feel implementation for Gauge.
*/
class GaugeLFImpl extends ItemLFImpl implements GaugeLF {

    /**
     * Creates GaugeLF for the passed in Gauge.
     * @param gauge the Gauge object associated with this look&feel.
     */
    GaugeLFImpl(Gauge gauge) {
        
        super(gauge);

        this.gauge = gauge;

        lSetMaxValue(0, gauge.maxValue);
        lSetValue(0, gauge.value);

        // IMPL NOTE: Make this smarter so that only the
        // resources we need load. Also, replicate this
        // code in the set* methods so that a gauge which
        // changes on the fly has the proper resources
        if (gauge.interactive) {
            GaugeResources.load();
        } else {
            ProgressBarResources.load();
            UpdateBarResources.load();
            BusyCursorResources.load();
        }
        
        percentLoc = new int[2];
        drawsTraversalIndicator = false;
    }


    // *****************************************************
    //  Public methods
    // *****************************************************


    /**
     * Notifies L&F of a value change in the corresponding Gauge.
     * @param oldValue - the old value set in the Gauge
     * @param newValue - the new value set in the Gauge
     */
    public void lSetValue(int oldValue, int newValue) {
        synchronized (Display.LCDUILock) {
            if (gauge.maxValue == Gauge.INDEFINITE) {
                /**
                 *  -- if there are more than one Gauge, we shouldn't
                 *  stop other continuous gauges until we change Displayable.
                 *  IMPL NOTE: see if this is the only Gauge on the form   -au
                 */
                
                if (newValue == Gauge.CONTINUOUS_RUNNING) {
                    // If this gauge is already visible and its new value
                    // is changed to CONTINUOUS_RUNNING, then start update
                    // task here. 
                    // Otherwise, delay update task creation until it becomes 
                    // visible (I.e. lCallShow is called).
                    if (visible) {
                        startGaugeUpdateTask();
                    }
                } else if (oldValue == Gauge.CONTINUOUS_RUNNING) {
                    cancelGaugeUpdateTask();
                }
                if (oldValue != newValue) 
                    lRequestInvalidate(true, true);
                else
                    lRequestPaint();
            } else if (oldValue != newValue) {
                lRequestPaint();
            }
        } // end sync
    }

    /**
     * Gets the current value.
     * @return the current value
     */
    public int lGetValue() {
        return gauge.value;
    }

    /**
     * Notifies L&F of a maximum value change in the corresponding Gauge
     * @param oldMaxValue - the old maximum value set in the Gauge
     * @param newMaxValue - the new maximum value set in the Gauge
     */
    public void lSetMaxValue(int oldMaxValue, int newMaxValue) {        
        // changing the max value will change the scale of the gauge
        if (oldMaxValue != newMaxValue) {        
            lRequestInvalidate(true, true);
        }
    }


    // *****************************************************
    //  Package private methods
    // *****************************************************
    
    /**
     * Determine if this Gauge should not be traversed to
     *
     * @return true if this Gauge should not be traversed to
     */
    boolean shouldSkipTraverse() {
        // Only traverse to gauges which are interactive, or have
        // item-specific commands added to them
        return (gauge.interactive) ? false : super.shouldSkipTraverse(); 
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
        if (gauge.interactive) {
            size[WIDTH] = GaugeSkin.WIDTH;
            size[HEIGHT] = GaugeSkin.HEIGHT;
        } else if (gauge.maxValue != Gauge.INDEFINITE) {
            size[WIDTH] = ProgressBarSkin.WIDTH;
            size[HEIGHT] = ProgressBarSkin.HEIGHT;
        } else if ((gauge.value == Gauge.CONTINUOUS_RUNNING) ||
                   (gauge.value == Gauge.CONTINUOUS_IDLE)) {
           size[WIDTH] = BusyCursorSkin.WIDTH;
           size[HEIGHT] = BusyCursorSkin.HEIGHT;
        } else {
            size[WIDTH] = UpdateBarSkin.WIDTH;
            size[HEIGHT] = UpdateBarSkin.HEIGHT;
        }
    }

    /**
     * Determine if this Item should have a newline after it
     *
     * @return true if it should have a newline after
     */
    boolean equateNLA() {
        if (super.equateNLA()) {
            return true;
        }

        return ((gauge.layout & Item.LAYOUT_2) != Item.LAYOUT_2);
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

        return ((gauge.layout & Item.LAYOUT_2) != Item.LAYOUT_2);
    }


    /**
     * Handle traversal within this Gauge
     *
     * @param dir the direction of traversal
     * @param viewportWidth the width of the viewport
     * @param viewportHeight the height of the viewport
     * @param visRect the in/out rectangle for the internal traversal location
     * @return True if traversal occurred within this Gauge
     */
    boolean uCallTraverse(int dir, int viewportWidth, int viewportHeight,
                         int[] visRect) 
    {
        
        super.uCallTraverse(dir, viewportWidth, viewportHeight, visRect);
       
        // If its a non-interactive gauge, there is no internal traversal
        // No need to lock either, we just access the boolean once.
        // (In fact, traverse should never be called on a non-interactive
        // gauge because shouldSkipTraverse() returns true in that case)
        if (!gauge.interactive) {
            return false;
        }
        
        // If it was an invalidate or something, just keep the focus
        // button where it is and reflect there is internal traversal
        if (dir == CustomItem.NONE) {
            intTraverse = true;
        } else {
            // The standard horizontal gauge's orientation is RIGHT, all
            // others accommodate vertical gauges and possible gauges for
            // right-to-left languages (which would be a LEFT orientation)
            
            switch (GaugeSkin.ORIENTATION) {
                // Gauge increases left to right, horizontally
                // (this is the default in chameleon)
            case Graphics.RIGHT:
                switch (dir) {
                case Canvas.LEFT:
                    if (initialTraverse) {
                        intTraverse = true;
                        focusBtn = I_INC_BTN;
                    } else {
                        intTraverse = (focusBtn != I_DEC_BTN);
                        focusBtn = I_DEC_BTN;
                    }
                    break;                        
                case Canvas.RIGHT:
                    if (initialTraverse) {
                        intTraverse = true;
                        focusBtn = I_DEC_BTN;
                    } else {
                        intTraverse = (focusBtn != I_INC_BTN);
                        focusBtn = I_INC_BTN;
                    }
                    break;                        
                case Canvas.UP:
                    intTraverse = initialTraverse;
                    if (initialTraverse) {
                        focusBtn = I_INC_BTN;
                    }
                    break;
                case Canvas.DOWN:
                    intTraverse = initialTraverse;
                    if (initialTraverse) {
                        focusBtn = I_DEC_BTN;
                    }
                    break;
                default:
                    Logging.report(Logging.ERROR, 
                                   LogChannels.LC_HIGHUI,
                                   "GaugeLFImpl: uCallTraverse, dir=" +dir);
                    break;
                }
                break;
                // Gauge increases right to left, horizontally
                // (think of right-to-left languages)
            case Graphics.LEFT:
                switch (dir) {
                case Canvas.LEFT:
                    if (initialTraverse) {
                        intTraverse = true;
                        focusBtn = I_DEC_BTN;
                    } else {
                        intTraverse = (focusBtn != I_INC_BTN);
                        focusBtn = I_INC_BTN;
                    }
                    break;                        
                case Canvas.RIGHT:
                    if (initialTraverse) {
                        intTraverse = true;
                        focusBtn = I_INC_BTN;
                    } else {
                        intTraverse = (focusBtn != I_DEC_BTN);
                        focusBtn = I_DEC_BTN;
                    }
                    break;                        
                case Canvas.UP:
                    intTraverse = initialTraverse;
                    if (initialTraverse) {
                        focusBtn = I_DEC_BTN;
                    }
                    break;
                case Canvas.DOWN:
                    intTraverse = initialTraverse;
                    if (initialTraverse) {
                        focusBtn = I_INC_BTN;
                    }
                    break;
                default:
                    Logging.report(Logging.ERROR, 
                                   LogChannels.LC_HIGHUI,
                                   "GaugeLFImpl: uCallTraverse, dir=" +dir);
                    break;
                }
                break;
                // Gauge increases bottom to top, vertically
            case Graphics.TOP:
                switch (dir) {
                case Canvas.LEFT:
                    intTraverse = initialTraverse;
                    if (initialTraverse) {
                        focusBtn = I_INC_BTN;
                    }
                    break;
                case Canvas.RIGHT:
                    intTraverse = initialTraverse;
                    if (initialTraverse) {
                        focusBtn = I_DEC_BTN;
                    }
                    break;
                case Canvas.UP:
                    if (initialTraverse) {
                        intTraverse = true;
                        focusBtn = I_DEC_BTN;
                    } else {
                        intTraverse = (focusBtn != I_INC_BTN);
                        focusBtn = I_INC_BTN;
                    }
                    break;                        
                case Canvas.DOWN:
                    if (initialTraverse) {
                        intTraverse = true;
                        focusBtn = I_INC_BTN;
                    } else {
                        intTraverse = (focusBtn != I_DEC_BTN);
                        focusBtn = I_DEC_BTN;
                    }
                    break;
                default:
                    Logging.report(Logging.ERROR, 
                                   LogChannels.LC_HIGHUI,
                                   "GaugeLFImpl: uCallTraverse, dir=" +dir);
                    break;                        
                }
                break;
                // Gauge increases top to bottom, vertically
            case Graphics.BOTTOM:
                switch (dir) {
                case Canvas.LEFT:
                    intTraverse = initialTraverse;
                    if (initialTraverse) {
                        focusBtn = I_DEC_BTN;
                    }
                    break;
                case Canvas.RIGHT:
                    intTraverse = initialTraverse;
                    if (initialTraverse) {
                        focusBtn = I_INC_BTN;
                    }
                    break;
                case Canvas.UP:
                    if (initialTraverse) {
                        intTraverse = true;
                        focusBtn = I_INC_BTN;
                    } else {
                        intTraverse = (focusBtn != I_DEC_BTN);
                        focusBtn = I_DEC_BTN;
                    }
                    break;                        
                case Canvas.DOWN:
                    if (initialTraverse) {
                        intTraverse = true;
                        focusBtn = I_DEC_BTN;
                    } else {
                        intTraverse = (focusBtn != I_INC_BTN);
                        focusBtn = I_INC_BTN;
                    }
                    break;
                default:
                    Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                                   "GaugeLFImpl: uCallTraverse, dir=" +dir);
                    break;
                }
                break;
            default:
                Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                               "GaugeLFImpl: uCallTraverse," +
                               "GaugeSkin.ORIENTATION=" +
                               GaugeSkin.ORIENTATION);
                break;
            }
        }
        
        
        switch (focusBtn) {
        case I_INC_BTN:
            visRect[X] = GaugeSkin.INC_BTN_X;
            visRect[Y] = GaugeSkin.INC_BTN_Y;
            if (GaugeSkin.IMAGE_INC_BTN != null) {
                visRect[WIDTH] = GaugeSkin.IMAGE_INC_BTN.getWidth();
                visRect[HEIGHT] = GaugeSkin.IMAGE_INC_BTN.getHeight();
            } else {
                visRect[WIDTH] = 15;
                visRect[HEIGHT] = 15;
            }
            break;
        case I_DEC_BTN:
            visRect[X] = GaugeSkin.DEC_BTN_X;
            visRect[Y] = GaugeSkin.DEC_BTN_Y;
            if (GaugeSkin.IMAGE_DEC_BTN != null) {
                visRect[WIDTH] = GaugeSkin.IMAGE_DEC_BTN.getWidth();
                visRect[HEIGHT] = GaugeSkin.IMAGE_DEC_BTN.getHeight();
            } else {
                visRect[WIDTH] = 15;
                visRect[HEIGHT] = 15;
            }
            break;
        default:
            Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                           "GaugeLFImpl: uCallTraverse, focusBtn=" +focusBtn);
            break;
        }
        
        // Gauge should always return true on at least the initial
        // traverse, or any internal traverse between the two buttons
        if (initialTraverse || intTraverse) {
            initialTraverse = false;
            uRequestPaint();
            return true;
        } 

        return false;
    }

    /**
     * Called by the system to indicate traversal has left this Item.
     */
    void uCallTraverseOut() {
        super.uCallTraverseOut();
        initialTraverse = true;
    }

    /**
     * Called by the system to signal a key repeat
     *
     * @param keyCode the key code of the key that has been pressed
     */
    void uCallKeyRepeated(int keyCode) {
        uCallKeyPressed(keyCode);
    }
    
    /**
     * Called by the system to signal a key press.
     *
     * @param keyCode the key code of the key that has been pressed
     */
    void uCallKeyPressed(int keyCode) {
        if (keyCode != Constants.KEYCODE_SELECT ||
            !gauge.interactive) {
            return;
        }
        
        Form form = null;

        synchronized (Display.LCDUILock) {
            int maxValue = gauge.maxValue;
            int oldValue = gauge.value;
            int value = oldValue;

            switch (focusBtn) {
            case I_INC_BTN:
                value++;
                break;
            case I_DEC_BTN:
                value--;
                break;
            default:
                Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                               "GaugeLFImpl: uCallKeyPressed, focusBtn=" +
                               focusBtn);
                break;
            }
            
            gauge.setValueImpl(value);

            // IMPL NOTE: paint optimization
            lRequestPaint();

            if (value != oldValue) {
                // notify the ItemStateChangedListener
                form = (Form)gauge.owner;
            }
            
        } // end synchronized

        // SYNC NOTE: We make sure we notify the ItemStateChangedListener
        // outside of LCDUILock
        if (form != null) {
            form.uCallItemStateChanged(gauge);
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
        if (gauge.interactive) {
            pointerArea = getPointerArea(x, y);

            switch(pointerArea) {
            case DEC_BTN_AREA:
                focusBtn = I_DEC_BTN;
                uRequestPaint();
                break;
            case INC_BTN_AREA:
                focusBtn = I_INC_BTN;
                uRequestPaint();
                break;
            default:
                break;
            }
        }
    }
   
    /**
     * Get the area of gauge the pointer cliked in.
     * @param x x coordinate of pointer 
     * @param y y coordinate of pointer 
     * @return the gauge area is returned. The possible values are
     *
     * @see #INVALID_AREA if pointer is out of gauge's bounds 
     * @see #INC_BTN_AREA if pointer is on the increment button
     * @see #DEC_BTN_AREA if pointer is on the decrement button
     * @see #METER_AREA if pointer is on the meter
     */
    private int getPointerArea(int x, int y) {
        int area = INVALID_AREA;

        x -= contentBounds[X];
        y -= contentBounds[Y];

        if (area == INVALID_AREA &&
            GaugeSkin.IMAGE_DEC_BTN != null &&
            // check coordinates
            x >= GaugeSkin.DEC_BTN_X &&
            y > GaugeSkin.DEC_BTN_Y &&
            x <= (GaugeSkin.DEC_BTN_X +
                  GaugeSkin.IMAGE_DEC_BTN.getWidth()) &&
            y <= (GaugeSkin.DEC_BTN_Y +
                  GaugeSkin.IMAGE_DEC_BTN.getHeight())) {
            
            area = DEC_BTN_AREA;
        }
        if (area == INVALID_AREA &&
            GaugeSkin.IMAGE_INC_BTN != null &&
            // check coordinates
            x >= GaugeSkin.INC_BTN_X &&
            y > GaugeSkin.INC_BTN_Y &&
            x <= (GaugeSkin.INC_BTN_X +
                  GaugeSkin.IMAGE_INC_BTN.getWidth()) &&
            y <= (GaugeSkin.INC_BTN_Y +
                  GaugeSkin.IMAGE_INC_BTN.getHeight())) {
            
            area = INC_BTN_AREA;
        }
        if (area == INVALID_AREA &&
            // check coordinates
            x >= GaugeSkin.METER_X && y > GaugeSkin.METER_Y &&
            x <= (GaugeSkin.METER_X +
                  GaugeSkin.IMAGE_METER_FULL.getWidth()) && 
            y <= (GaugeSkin.METER_Y +
                  GaugeSkin.IMAGE_METER_FULL.getHeight())) {
            
            area = METER_AREA;
        }
        
        return area;
    }
    
    /**
     * Called by the system to signal a pointer release
     *
     * @param x the x coordinate of the pointer up
     * @param y the x coordinate of the pointer up
     */
    void uCallPointerReleased(int x, int y) {
        if (gauge.interactive) {
            int newArea = getPointerArea(x, y);
        
            if (pointerArea == newArea) {
                switch (pointerArea) {
                case DEC_BTN_AREA:
                case INC_BTN_AREA:
                    uCallKeyPressed(Constants.KEYCODE_SELECT);
                    break;
                case METER_AREA:
                    {
                        Form form = null;
                        synchronized (Display.LCDUILock) {
                            int oldValue = gauge.value;
                            int locationOnMeter = x - contentBounds[X] - GaugeSkin.METER_X;
                            float percent = locationOnMeter * 100 / GaugeSkin.IMAGE_METER_FULL.getWidth(); 
                            float value = percent / 100 * gauge.maxValue;
                            /* round the value */
                            int intValue = (int)value;
                            float remainder = value - intValue;
                            if (remainder > 0.5) {
                                intValue++;
                            }
                            
                            gauge.setValueImpl(intValue);
                            lRequestPaint();
                            if (intValue != oldValue) {
                                // notify the ItemStateChangedListener
                                form = (Form)gauge.owner;
                            }
                        }
                        // SYNC NOTE: We make sure we notify the ItemStateChangedListener
                        // outside of LCDUILock
                        if (form != null) {
                            form.uCallItemStateChanged(gauge);
                        }
                    }
                    break;
                default:
                    break;
                } // end of switch 
            } // pointerArea == newArea
        } // interactive gauge
    }

    
    /**
     * Called by the system to notify this Item it is being shown
     *
     * <p>The default implementation of this method updates
     * the 'visible' state
     */
    void lCallShowNotify() {
        
        super.lCallShowNotify();

        // Start update task for CONTINUOUS_RUNNING gauge upon visible
        if (gauge.maxValue == Gauge.INDEFINITE && 
            gauge.value == Gauge.CONTINUOUS_RUNNING) {
            startGaugeUpdateTask();
        }
    }
    
    /**
     * Called by the system to notify this Item it is being hidden
     *
     * <p>The default implementation of this method updates
     * the 'visible' state
     */
    void lCallHideNotify() {
        
        super.lCallHideNotify();

        cancelGaugeUpdateTask();
    }

    /**
     * Paints the content area of this Gauge. 
     * Graphics is translated to contents origin.
     * @param g The graphics where Gauge content should be painted
     * @param w The width available for the Item's content
     * @param h The height available for the Item's content
     */
    void lPaintContent(Graphics g, int w, int h) {     
        if (gauge.interactive) {
            lPaintInteractiveGauge(g, w, h, gauge.maxValue, gauge.value);
        } else if (gauge.maxValue == Gauge.INDEFINITE) {
            lPaintIndefinite(g, w, h, gauge.maxValue, gauge.value);
        } else {
            lPaintProgressBar(g, w, h, gauge.maxValue, gauge.value);
        }
    }
    
    void lPaintInteractiveGauge(Graphics g, int w, int h, 
                                int maxValue, int value)
    {
        // case: interactive gauge
        // draw background, if present
        if (GaugeSkin.IMAGE_BG != null) {
            g.drawImage(GaugeSkin.IMAGE_BG, 0, 0,
                        Graphics.LEFT | Graphics.TOP);
        } else {
            g.setColor(0xCCCCCC);
            g.fillRect(0, 0, GaugeSkin.WIDTH, GaugeSkin.HEIGHT);
            g.setColor(0);
            return; 
            // we return early on interactive gauge if there are
            // no images because its rather complicated to draw
            // otherwise, at least right now            
        }

        // computation to decide how much of filled & unfilled
        // portions to be displayed
        int body_full;
        int body_empty;
        if (GaugeSkin.ORIENTATION == Graphics.LEFT ||
            GaugeSkin.ORIENTATION == Graphics.RIGHT)
        {
            body_empty = GaugeSkin.IMAGE_METER_FULL.getWidth();
            body_full = (((value * 100) / maxValue) * body_empty) / 100;
            body_empty -= body_full;
            
            if (GaugeSkin.ORIENTATION == Graphics.RIGHT) {
                g.drawRegion(GaugeSkin.IMAGE_METER_FULL, 0, 0, body_full, 
                             GaugeSkin.IMAGE_METER_FULL.getHeight(), 
                             Sprite.TRANS_NONE, GaugeSkin.METER_X, 
                             GaugeSkin.METER_Y, 
                             Graphics.LEFT | Graphics.TOP);
                             
                g.drawRegion(GaugeSkin.IMAGE_METER_EMPTY, body_full, 0, 
                             body_empty, 
                             GaugeSkin.IMAGE_METER_EMPTY.getHeight(), 
                             Sprite.TRANS_NONE, GaugeSkin.METER_X + 
                             body_full, GaugeSkin.METER_Y, 
                             Graphics.LEFT | Graphics.TOP);
            } else {
                g.drawRegion(GaugeSkin.IMAGE_METER_EMPTY, 0, 0, body_empty, 
                             GaugeSkin.IMAGE_METER_EMPTY.getHeight(), 
                             Sprite.TRANS_NONE, GaugeSkin.METER_X, 
                             GaugeSkin.METER_Y, 
                             Graphics.LEFT | Graphics.TOP);
                             
                g.drawRegion(GaugeSkin.IMAGE_METER_FULL, body_empty, 0, 
                             body_full, 
                             GaugeSkin.IMAGE_METER_FULL.getHeight(), 
                             Sprite.TRANS_NONE, GaugeSkin.METER_X + 
                             body_empty, GaugeSkin.METER_Y, 
                             Graphics.LEFT | Graphics.TOP);
            }                               
                         
        } else {
            body_empty = GaugeSkin.IMAGE_METER_FULL.getHeight();
            body_full = (((value * 100) / maxValue) * body_empty) / 100;
            body_empty -= body_full;
            
            if (GaugeSkin.ORIENTATION == Graphics.TOP) {
                g.drawRegion(GaugeSkin.IMAGE_METER_FULL, 0, body_empty,
                             GaugeSkin.IMAGE_METER_FULL.getWidth(), 
                             body_full, 
                             Sprite.TRANS_NONE, GaugeSkin.METER_X, 
                             GaugeSkin.METER_Y + body_empty, 
                             Graphics.LEFT | Graphics.TOP);
                             
                g.drawRegion(GaugeSkin.IMAGE_METER_EMPTY, 0, 0,
                             GaugeSkin.IMAGE_METER_EMPTY.getWidth(), 
                             body_empty,
                             Sprite.TRANS_NONE, GaugeSkin.METER_X, 
                             GaugeSkin.METER_Y, 
                             Graphics.LEFT | Graphics.TOP);
            } else {
                g.drawRegion(GaugeSkin.IMAGE_METER_EMPTY, 0, body_full,
                             GaugeSkin.IMAGE_METER_EMPTY.getWidth(), 
                             body_empty, 
                             Sprite.TRANS_NONE, GaugeSkin.METER_X, 
                             GaugeSkin.METER_Y + body_full, 
                             Graphics.LEFT | Graphics.TOP);
                             
                g.drawRegion(GaugeSkin.IMAGE_METER_FULL, 0, 0,
                             GaugeSkin.IMAGE_METER_FULL.getWidth(), 
                             body_full,
                             Sprite.TRANS_NONE, GaugeSkin.METER_X, 
                             GaugeSkin.METER_Y, 
                             Graphics.LEFT | Graphics.TOP);
            }                               
            
        }

        // display current value of gauge
        drawNumber(g, GaugeSkin.IMAGE_VALUES, 11, value, 
                   GaugeSkin.VALUE_X, GaugeSkin.VALUE_Y,
                   (value * 100) / maxValue);

        if (GaugeSkin.IMAGE_DEC_BTN != null) {
            // decrease button            
            g.drawImage(GaugeSkin.IMAGE_DEC_BTN,
                        GaugeSkin.DEC_BTN_X,
                        GaugeSkin.DEC_BTN_Y,
                        Graphics.LEFT | Graphics.TOP);
            if (hasFocus && focusBtn == I_DEC_BTN) {
                g.setColor(ScreenSkin.COLOR_TRAVERSE_IND);
                g.drawRect(GaugeSkin.DEC_BTN_X, 
                           GaugeSkin.DEC_BTN_Y, 
                           GaugeSkin.IMAGE_DEC_BTN.getWidth(), 
                           GaugeSkin.IMAGE_DEC_BTN.getHeight());
                g.setColor(0);
            }
        } else {
            g.drawString("-", GaugeSkin.DEC_BTN_X, GaugeSkin.DEC_BTN_Y,
                         Graphics.LEFT | Graphics.TOP);
            if (hasFocus && focusBtn == I_DEC_BTN) {
                g.setColor(ScreenSkin.COLOR_TRAVERSE_IND);
                g.drawRect(GaugeSkin.DEC_BTN_X,
                           GaugeSkin.DEC_BTN_Y,
                           g.getFont().charWidth('-'),
                           g.getFont().charWidth('-'));
                g.setColor(0);
            }
        }
        
        if (GaugeSkin.IMAGE_INC_BTN != null) {
            // increase button
            g.drawImage(GaugeSkin.IMAGE_INC_BTN,
                        GaugeSkin.INC_BTN_X,
                        GaugeSkin.INC_BTN_Y,
                        Graphics.LEFT | Graphics.TOP);
            if (hasFocus && focusBtn == I_INC_BTN) {
                g.setColor(ScreenSkin.COLOR_TRAVERSE_IND);
                g.drawRect(GaugeSkin.INC_BTN_X,
                           GaugeSkin.INC_BTN_Y, 
                           GaugeSkin.IMAGE_INC_BTN.getWidth(), 
                           GaugeSkin.IMAGE_INC_BTN.getHeight());
                g.setColor(0);
            }
        } else {
            g.drawString("+", GaugeSkin.INC_BTN_X, GaugeSkin.INC_BTN_Y,
                         Graphics.LEFT | Graphics.TOP);
            
            if (hasFocus && focusBtn == I_INC_BTN) {
                g.setColor(ScreenSkin.COLOR_TRAVERSE_IND);
                g.drawRect(GaugeSkin.INC_BTN_X,
                           GaugeSkin.INC_BTN_Y,
                           g.getFont().charWidth('+'),
                           g.getFont().charWidth('+'));
                g.setColor(0);
            }
        }
    } // lPaintInteractiveGauge
            
    void lPaintIndefinite(Graphics g, int w, int h, 
                          int maxValue, int value)
    {            
        switch (value) {
        case Gauge.CONTINUOUS_RUNNING:
            drawBusyCursor(g, nextFrame);
            
            // increment the frame counter of the incremental gauge
            // and remember to reset it when it hits the total number
            // of frames, so it can start all over again
            if (BusyCursorSkin.FRAME_SEQUENCE != null) {
                nextFrame = ++nextFrame % BusyCursorSkin.FRAME_SEQUENCE.length;
            }
            break;
        case Gauge.CONTINUOUS_IDLE:
            drawBusyCursor(g, 0);
            break;
        case Gauge.INCREMENTAL_UPDATING:
            drawUpdateBar(g, nextFrame);
            
            // increment the frame counter of the incremental gauge
            // and remember to reset it when it hits the total number
            // of frames, so it can start all over again
            if (UpdateBarSkin.FRAME_SEQUENCE != null) {
                nextFrame = ++nextFrame % UpdateBarSkin.FRAME_SEQUENCE.length;
            }
            break;
        case Gauge.INCREMENTAL_IDLE:
            drawUpdateBar(g, 0);
            break;
        default:
            Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                           "GaugeLFImpl: lPaintIndefinite, value=" +value);
            break;
        }
    }
            
    void lPaintProgressBar(Graphics g, int w, int h, 
                           int maxValue, int value)
    {
            
        // case: non-interactive definite range gauge/progressbar
        // draw background, if present
        if (ProgressBarSkin.IMAGE_BG != null) {
            g.drawImage(ProgressBarSkin.IMAGE_BG, 0, 0,
                        Graphics.LEFT | Graphics.TOP);
        } else {
            g.setColor(0xCCCCCC);
            g.fillRect(0, 0, ProgressBarSkin.WIDTH, ProgressBarSkin.HEIGHT);
            g.setColor(0);
        }

        // first compute current value of gauge as a percentage
        int n = (value * 100) / maxValue;

        // decide how much of filled & unfilled portions to be shown
        int body_full = 0;
        if (ProgressBarSkin.IMAGE_METER_FULL != null) {
            body_full = 
                (n * ProgressBarSkin.IMAGE_METER_FULL.getWidth()) / 100;
            g.drawRegion(ProgressBarSkin.IMAGE_METER_FULL, 0, 0, 
                         body_full, 
                         ProgressBarSkin.IMAGE_METER_FULL.getHeight(), 
                         Sprite.TRANS_NONE, 
                         ProgressBarSkin.METER_X, 
                         ProgressBarSkin.METER_Y, 
                         Graphics.LEFT | Graphics.TOP);
        } else {
            body_full =
                (n * ProgressBarSkin.WIDTH - ProgressBarSkin.VALUE_WIDTH - 
                    10) / 100;
            g.fillRect(ProgressBarSkin.METER_X, ProgressBarSkin.METER_Y,
                       body_full, ProgressBarSkin.HEIGHT - 
                            (2 * ProgressBarSkin.METER_Y));            
        }
        
        int body_empty = 0;
        if (ProgressBarSkin.IMAGE_METER_FULL != null) {
            body_empty = 
                ProgressBarSkin.IMAGE_METER_FULL.getWidth() - body_full;
                g.drawRegion(ProgressBarSkin.IMAGE_METER_EMPTY, body_full,
                             0, body_empty, 
                             ProgressBarSkin.IMAGE_METER_EMPTY.getHeight(), 
                             Sprite.TRANS_NONE, 
                             ProgressBarSkin.METER_X + body_full, 
                             ProgressBarSkin.METER_Y, 
                             Graphics.LEFT | Graphics.TOP);
        } else {
            body_empty = 
                (ProgressBarSkin.WIDTH - ProgressBarSkin.VALUE_WIDTH -
                    10) - body_full;
            g.setColor(0xFFFFFF);
            g.fillRect(ProgressBarSkin.METER_X + body_full,
                       ProgressBarSkin.METER_Y,
                       body_empty,
                       ProgressBarSkin.HEIGHT -
                        (2 * ProgressBarSkin.METER_Y));
        }

        // current value of gauge as a percentage 
        drawNumber(g, ProgressBarSkin.IMAGE_VALUES, 11, n,
                   ProgressBarSkin.VALUE_X, ProgressBarSkin.VALUE_Y,
                   (value * 100) / maxValue);

        // the percentage symbol
        drawPercentage(g, ProgressBarSkin.IMAGE_PERCENTS, value);           
    }


    /**
     * Called by the system to traverse this DateField.
     *
     * @param dir the direction of traversal
     * @param viewportWidth the width of the container's viewport
     * @param viewportHeight the height of the container's viewport
     * @param visRect passes the visible rectangle into the method, and
     * returns the updated traversal rectangle from the method
     * @return true if internal traversal had occurred, false if traversal
     * should proceed out
     */
    boolean lCallTraverse(int dir, int viewportWidth, int viewportHeight,
                          int[] visRect)
    {
        boolean res = super.lCallTraverse(dir, viewportWidth, viewportHeight, visRect);

        visRect[X] = 0;
        visRect[Y] = 0;
        visRect[HEIGHT] = bounds[HEIGHT];
        visRect[WIDTH] = bounds[WIDTH];
        return res;

    }
               
    
    /**
     * Paints the given frame of the incremental updating/idle gauge.
     *
     * @param g the graphics to paint to
     * @param frameToDraw the frame index of the frame to be drawn
     *                    from a frame-sequence
     */
    void drawUpdateBar(Graphics g, int frameToDraw) {
        if (UpdateBarSkin.FRAME_SEQUENCE == null ||
            frameToDraw > UpdateBarSkin.FRAME_SEQUENCE.length) 
        {
            return;
        }
        
        frameToDraw = UpdateBarSkin.FRAME_SEQUENCE[frameToDraw];
        
        // draw background, if present
        if (UpdateBarSkin.IMAGE_BG != null) {
            g.drawImage(UpdateBarSkin.IMAGE_BG, 0, 0,
                        Graphics.LEFT | Graphics.TOP);
        } else {
            g.setColor(0xCCCCCC);
            g.fillRect(0, 0, UpdateBarSkin.WIDTH, UpdateBarSkin.HEIGHT);
            g.setColor(0);
        }
        
        if (UpdateBarSkin.IMAGE_FRAME != null 
            && frameToDraw < UpdateBarSkin.IMAGE_FRAME.length) 
        {
            // draw the frame
            g.drawImage(UpdateBarSkin.IMAGE_FRAME[frameToDraw],
                        UpdateBarSkin.FRAME_X,
                        UpdateBarSkin.FRAME_Y,
                        Graphics.LEFT | Graphics.TOP);
        } else {
            g.fillRect(2, 2, 
                UpdateBarSkin.WIDTH - 4, UpdateBarSkin.HEIGHT - 4);
            int width = 
                (UpdateBarSkin.WIDTH / UpdateBarSkin.FRAME_SEQUENCE.length) *
                frameToDraw;
            if (width < 0) {
                width = 0;
            }
            g.setColor(0xFFFFFF);
            g.fillRect(2, 2, width - 4, UpdateBarSkin.HEIGHT - 4);
            g.setColor(0);
        }
    }

    /**
     * Paints the given frame of the continuous running/idle gauge.
     *
     * @param g the graphics to paint to
     * @param frameToDraw the frame index of the frame to be drawn
     *                    from a frame-sequence
     */
    void drawBusyCursor(Graphics g, int frameToDraw) {
        if (BusyCursorSkin.FRAME_SEQUENCE == null ||
            frameToDraw > BusyCursorSkin.FRAME_SEQUENCE.length) 
        {
            return;
        }
        
        frameToDraw = BusyCursorSkin.FRAME_SEQUENCE[frameToDraw];
        
        // draw background, if present
        if (BusyCursorSkin.IMAGE_BG != null) {
            g.drawImage(BusyCursorSkin.IMAGE_BG, 0, 0,
                        Graphics.LEFT | Graphics.TOP);
        } else {
            g.setColor(0xCCCCCC);
            g.fillRect(0, 0, BusyCursorSkin.WIDTH, BusyCursorSkin.HEIGHT);
            g.setColor(0);
        }
        
        if (BusyCursorSkin.IMAGE_FRAME != null &&
            frameToDraw < BusyCursorSkin.IMAGE_FRAME.length) 
        {
            // draw the frame
            g.drawImage(BusyCursorSkin.IMAGE_FRAME[frameToDraw],
                        BusyCursorSkin.FRAME_X,
                        BusyCursorSkin.FRAME_Y,
                        Graphics.LEFT | Graphics.TOP);
        } else {
            switch (frameToDraw % 3) {
                case 0:
                    g.drawString("-", 
                                 (int)(BusyCursorSkin.WIDTH / 2), 
                                 (int)(BusyCursorSkin.HEIGHT / 2),
                                 Graphics.HCENTER | Graphics.BOTTOM);
                    break;
                case 1:
                    g.drawString("\\", 
                                 (int)(BusyCursorSkin.WIDTH / 2), 
                                 (int)(BusyCursorSkin.HEIGHT / 2),
                                 Graphics.HCENTER | Graphics.BOTTOM);
                    break;
                case 2:
                    g.drawString("|", 
                                 (int)(BusyCursorSkin.WIDTH / 2), 
                                 (int)(BusyCursorSkin.HEIGHT / 2),
                                 Graphics.HCENTER | Graphics.BOTTOM);
                    break;
                default:
                    g.drawString("/", 
                                 (int)(BusyCursorSkin.WIDTH / 2), 
                                 (int)(BusyCursorSkin.HEIGHT / 2),
                                 Graphics.HCENTER | Graphics.BOTTOM);
                    break;
            }
        }
    }

    /**
     * Accepts a numeric value and draws the number graphically.
     *
     * @param g the graphics to paint to
     * @param valuesImg the image to clip from
     * @param numDigits the number of digits in the image     
     * @param index the index of the digit that needs to be drawn graphically
     * @param locationX the x co-ordinate of the location to start drawing 
     *                  the digits
     * @param locationY the y co-ordinate of the location to start drawing 
     *                  the digits
     * @param n the value as a percentage, equal to (value * 1000) / maxValue
     */
    void drawNumber(Graphics g, Image valuesImg, int numDigits, int index,
                    int locationX, int locationY, int n) 
    {
        /*
         * Find the first leftmost, non-zero digit.
         *
         * Keep using a smaller divisor to get to a non-zero
         * digit in n. If the divisor itself becomes zero,
         * this is the case where n = 0. We break out of the
         * loop, in that case.
         *
         * Note that the test for divisor == 0 is done before
         * extracting the next digit in the "while" part, to
         * avoid a divide-by-zero ArithmeticException.
         */
        
        int divisor = 1000;   // assume the max displayed is 4 digit num
        int digit = 0;
        int digitCount = 4;        
        
        int digitWidth = g.getFont().charWidth('9');
        if (valuesImg != null) {
            digitWidth = (int)(valuesImg.getWidth() / numDigits);
        }
                
        // keep whittling down the number until it's in range.
        while (n > 9999) n /= 10;

        // figure out how many digits... adjust divisor accordingly.
        while (divisor != 0 && (digit = n / divisor) == 0) {
            divisor /= 10;
            digitCount--;
        }

        // special case for 0
        if (digitCount == 0) digitCount = 1;

        // position where to start displaying the gauge value
        int start_x = locationX;

        // offset by a digit's width in case of shorter values
        // to make it more presentable
        start_x += (digitCount < 3) ? digitWidth : 0;
        
        // now show the digits
        switch (divisor) {
        case 1000:
            digit = n / 1000;
            n %= divisor;
            divisor /= 10;

            paintDigit(g, valuesImg, numDigits, digit, start_x, locationY);
            start_x += digitWidth;
            // fall through to handle next lower digit.

        case 100:
            digit = n / 100;
            n %= divisor;
            divisor /= 10;

            paintDigit(g, valuesImg, numDigits, digit, start_x, locationY);
            start_x += digitWidth;
            // fall through to handle next lower digit.

        case 10:
            digit = n / 10;
            n %= divisor;
            divisor /= 10;

            paintDigit(g, valuesImg, numDigits, digit, start_x, locationY);
            start_x += digitWidth;
            // fall through to handle next lower digit.

        case 1:
            digit = n;
            paintDigit(g, valuesImg, numDigits, digit, start_x, locationY);
            break;
            
        case 0:  // special case when n = 0
            // paint the non-highlight 0
            paintDigit(g, valuesImg, numDigits, numDigits - 1, 
                       start_x, locationY);
            break;
        default:
            Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                           "GaugeLFImpl: drawNumber," +
                           " divisor=" +divisor);
            break;
        }

        // ASSERT: Make sure this array gets filled with the x,y 
        // co-ordinates where the digits end, so as to facilitate 
        // drawing, say a percentage sign after the value, in some 
        // cases like the progressbar. Removing this notion will
        // cause *malfunction* of the drawPercentage() method, in
        // not being able to locate where to draw the percentage 
        // sign correctly.

        percentLoc[0] = start_x + digitWidth;
        percentLoc[1] = locationY;
    }
    
    /**
     * Calculates the position of the digit to be clipped from the values
     * image (based on the "digitLocation" parameter passed in) and 
     * paints it.
     *
     * @param g the graphics to paint t
     * @param valuesImg the image to clip from
     * @param numDigits the number of digits in the image
     * @param digitIndex the pixel location of this digit inside the 
     *                      values image, used to extract the digit images
     *                      from
     * @param x the x-coordinate where to draw this digit
     * @param y the y-coordinate where to draw this digit
     */
    void paintDigit(Graphics g, Image valuesImg, int numDigits,
                    int digitIndex, int x, int y) 
    {
        if (valuesImg != null) {
            int digitWidth = (int)(valuesImg.getWidth() / numDigits);
            
            g.drawRegion(valuesImg, 
                         digitIndex * digitWidth, 0, 
                         digitWidth,
                         valuesImg.getHeight(), 
                         Sprite.TRANS_NONE, x, y, 
                         Graphics.LEFT | Graphics.TOP);
        } else {
            if (digitIndex == numDigits - 1) {
                digitIndex = 0;
            }
            g.drawString("" + digitIndex, x, y, Graphics.LEFT | Graphics.TOP);
        }
    }

    /**
     * Draws a percentage sign.
     *
     * @param g the graphics to paint to
     * @param percentImg the image to clip from
     * @param value the value of the gauge
     */
    void drawPercentage(Graphics g, Image percentImg, int value) {

        if (percentImg != null && percentWidth == -1) {
            percentWidth = (int)(percentImg.getWidth() / 2);
            percentHeight = percentImg.getHeight();
        }
        
        if (percentImg != null) {
            // Note: The percentLoc[] array holds the x,y location
            // where the percentage sign should be painted. This is filled
            // by the drawNumber() method after it finishes drawing the
            // numeric value graphically.
    
            g.drawRegion(percentImg, 
                         (value == 0) ? percentWidth : 0, 0, 
                         percentWidth, percentHeight, 
                         Sprite.TRANS_NONE, 
                         percentLoc[0], percentLoc[1], 
                         Graphics.LEFT | Graphics.TOP);
        } else {
            g.drawString("%", percentLoc[0], percentLoc[1], 
                         Graphics.LEFT | Graphics.TOP);
        }
    }

    /**
     * A helper class to update a continuous running gauge.
     */
    class GaugeUpdateTask extends TimerTask {
        /**
         * the gauge to repaint
         */
        GaugeLFImpl myGaugeLF;

        /**
         * Construct a new GaugeUpdateTask.
         *
         * @param gaugeLF the gauge to repaint
         */
        GaugeUpdateTask(GaugeLF gaugeLF) {
            super();
            myGaugeLF = (GaugeLFImpl)gaugeLF;
        }

        /**
         * required method in TimerTask derivatives
         */
        public final void run() {
            // increment the frame counter of the continuous gauge
            // and remember to reset it when it hits the total number 
            // of frames, so it can start all over again
            myGaugeLF.lRequestPaint();
        }
    }

    /**
     * Start the GaugeUpdateTask running
     */
    void startGaugeUpdateTask() {
        if (updateHelper == null) {
            updateHelper = new GaugeUpdateTask(this);
            gaugeUpdateTimer.schedule(updateHelper, 100, 100);
        }
    }
    
    /**
     * Stop the GaugeUpdateTask from running.
     */
    void cancelGaugeUpdateTask() {
        if (updateHelper != null) {
            updateHelper.cancel();
            updateHelper = null;
        }
    }

    /** Gauge instance associated with this view */
    Gauge gauge;
        
    /** 
     * A Timer which will handle scheduling repaints of an 
     * indefinite range gauge when value == Gauge.CONTINUOUS_RUNNING
     */
    static Timer gaugeUpdateTimer; 

    /**
     * Constant representing the increment button. It is an arbitrary 
     * integer. The focusBtn attribute is set to this value if, during 
     * traversal, the focus is gained by the increment button and later
     * on used to check for focus highlight painting. 
     */
    static final int I_INC_BTN = 66;

    /**
     * Constant representing the decrement button. It is an arbitrary 
     * integer. The focusBtn attribute is set to this value if, during 
     * traversal, the focus is gained by the decrement button and later
     * on used to check for focus highlight painting. 
     */
    static final int I_DEC_BTN = 33;
    
    static {
        /*
         * The Timer to schedule update/repaint events with
         * in Gauge.CONTINUOUS_RUNNING mode.
         */
        gaugeUpdateTimer = new Timer();
        
    } // static 

    /** 
     * A TimerTask which will schedule repaints of an indefinite 
     * range gauge when value == Gauge.CONTINUOUS_RUNNING
     */
    GaugeUpdateTask updateHelper;
   
    /**
     * The cached value for the width of the graphical percent sign
     */
    int percentWidth = -1;
    
    /**
     * The cached value for the height of the graphical percent sign
     */
    int percentHeight = -1;
    
    /**
     * Used to remember internally the current button in focus 
     * (increment/decrement) when traversal happens and focus is gained
     * by an interactive gauge from another widget, or during internal
     * traversal, between increment and decrement buttons. The constant
     * values it can assume could be:
     *    I_INC_BTN: if focus is on increment button
     *    I_DEC_BTN: if focus is on decrement button (default initial value)
     * This is later used to check for focus highlight painting. 
     */
    int focusBtn = I_DEC_BTN;
    
    /**
     * An array storing the x,y co-ordinates of the location where the 
     * percentage sign should be displayed for a progressbar. 
     * This *MUST BE FILLED* inside the drawNumber() method,
     * so that the drawPercentage() method knows where to draw the
     * percentage sign correctly.
     */
    int[] percentLoc;

    /**
     * An index that keeps track of the next image that should be drawn
     * to animate a continuous running gauge, from an array of frames.
     */
    int nextFrame = 0;

    /**
     * A flag to mark when the call to a gauge's traverse method is the
     * initial traverse or not. True by default, 
     * (reset to true in traverseOut).
     */
    boolean initialTraverse = true;

    /**
     * A flag to indicate that internal traversal has occurred within
     * the gauge itself.
     */
    boolean intTraverse;
    
    /**
     * the area accepting the pointer event
     */ 
    private int pointerArea = INVALID_AREA;

    /** 
     * the different areas of an interactive gauge, used for pointer events
     */ 
    private static final int INVALID_AREA = -1;
    private static final int DEC_BTN_AREA = 0;
    private static final int INC_BTN_AREA = 1;
    private static final int METER_AREA = 2;
    
    
} // GaugeView
