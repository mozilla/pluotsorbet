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

import java.util.Date;
import java.util.Calendar;

import com.sun.midp.lcdui.*;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;
import com.sun.midp.configurator.Constants;
import com.sun.midp.chameleon.skins.DateEditorSkin;
import com.sun.midp.chameleon.skins.ScreenSkin;
import com.sun.midp.chameleon.skins.resources.DateEditorResources;
import com.sun.midp.chameleon.layers.PopupLayer;

import javax.microedition.lcdui.game.Sprite;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * A utility class for editing date/time components for a DateField.
 */
class MiniDateEditor extends DateEditor {     

    /**
     * Create a new DateEditor layer.
     *
     * @param lf The DateFieldLFImpl that triggered this date editor
     */
    public MiniDateEditor(DateFieldLFImpl lf) {
        super(lf);
        timeComponentsOffset = 0;
    }      
    
    void show() {
        super.show();

        switch (mode) {
            case DateField.DATE:
            case DateField.DATE_TIME:
                focusOn = DAY_POPUP;                
                break;
        }
    }

    /**
     * Hide all sub-popups triggered by this date editor.
     * Overridden from parent class to close additional
     * 'day of the month' popup.
     */
    void hideAllPopups() {
        synchronized (Display.LCDUILock) {
            ScreenLFImpl sLF = (ScreenLFImpl)lf.df.owner.getLF();
            Display d = sLF.lGetCurrentDisplay();
            if (d != null) {
                d.hidePopup(dayPopup);
                d.hidePopup(monthPopup);
                d.hidePopup(yearPopup);
                d.hidePopup(hoursPopup);
                d.hidePopup(minutesPopup);
            }
        } // synchronized
    }

    /**
     * Populate the date components.
     */
    protected void populateDateComponents() {
        super.populateDateComponents();
        
        // populate DAYS[]
        int daysInMonth = daysInMonth(
            editDate.get(Calendar.MONTH), editDate.get(Calendar.YEAR));
        DAYS = new String[daysInMonth];
        for (int i = 1; i <= daysInMonth; i++) {
            DAYS[i - 1] = Integer.toString(i);
        }
        dayPopup = new DEPopupLayer(this, DAYS, 
            editDate.get(Calendar.DAY_OF_MONTH) - 1, true);        
    }

    /**
     * Draw the date components.
     *
     * @param g The Graphics object to paint to
     */
    protected void drawDateComponents(Graphics g) {
        
        nextX = 4;
        nextY = 0;
        
        int w = 0;
        int h = 0;
        

        g.setFont(DateEditorSkin.FONT_POPUPS);
        g.setColor(0);
        g.drawString(Resource.getString(ResourceConstants.LCDUI_DF_DATE_MINI),
                     nextX, nextY, Graphics.LEFT | Graphics.TOP);
        
        nextY = DateEditorSkin.FONT_POPUPS.getHeight() + 2;
        g.translate(nextX, nextY);
        
        if (DateEditorSkin.IMAGE_MONTH_BG != null) {
            g.drawImage(DateEditorSkin.IMAGE_TIME_BG, 0, 0,
                        Graphics.LEFT | Graphics.TOP);
            w = DateEditorSkin.IMAGE_TIME_BG.getWidth();
            h = DateEditorSkin.IMAGE_TIME_BG.getHeight();
            if (focusOn == DAY_POPUP) {
                g.setColor(DateEditorSkin.COLOR_TRAVERSE_IND);
                g.drawRect(-2, -2, w + 3, h + 3);
            }
            dayPopup.setElementSize(
                w - 4, DateEditorSkin.FONT_POPUPS.getHeight());
            dayPopup.setBounds(g.getTranslateX(),
                                 g.getTranslateY() + h,
                                 w, DateEditorSkin.HEIGHT_POPUPS);
        }
        g.setFont(DateEditorSkin.FONT_POPUPS);
        g.setColor(0);
        g.drawString(DAYS[editDate.get(Calendar.DAY_OF_MONTH) - 1],
                     3, 0, Graphics.LEFT | Graphics.TOP);               
        
        g.translate(w + 2, 0);
        
        if (DateEditorSkin.IMAGE_MONTH_BG != null) {
            g.drawImage(DateEditorSkin.IMAGE_MONTH_BG, 0, 0,
                        Graphics.LEFT | Graphics.TOP);
            w = DateEditorSkin.IMAGE_MONTH_BG.getWidth();
            h = DateEditorSkin.IMAGE_MONTH_BG.getHeight();
            if (focusOn == MONTH_POPUP) {
                g.setColor(DateEditorSkin.COLOR_TRAVERSE_IND);
                g.drawRect(-2, -2, w + 3, h + 3);
            }
            monthPopup.setElementSize(
                w - 4, DateEditorSkin.FONT_POPUPS.getHeight());
            monthPopup.setBounds(g.getTranslateX(),
                                 g.getTranslateY() + h,
                                 w, DateEditorSkin.HEIGHT_POPUPS);
        }
        g.setFont(DateEditorSkin.FONT_POPUPS);
        g.setColor(0);
        g.drawString(MONTHS[editDate.get(Calendar.MONTH)],
                     4, 0, Graphics.LEFT | Graphics.TOP);

        g.translate(w + 2, 0);
        if (DateEditorSkin.IMAGE_YEAR_BG != null) {
            g.drawImage(DateEditorSkin.IMAGE_YEAR_BG, 0, 0,
                        Graphics.LEFT | Graphics.TOP);
            w = DateEditorSkin.IMAGE_YEAR_BG.getWidth();
            h = DateEditorSkin.IMAGE_YEAR_BG.getHeight();
            if (focusOn == YEAR_POPUP) {
                g.setColor(DateEditorSkin.COLOR_TRAVERSE_IND);
                g.drawRect(-2, -2, w + 3, h + 3);
            }
            yearPopup.setElementSize(
                w - 4, DateEditorSkin.FONT_POPUPS.getHeight());
            yearPopup.setBounds(g.getTranslateX(),
                                 g.getTranslateY() + h,
                                 w, DateEditorSkin.HEIGHT_POPUPS);
        }

        g.setFont(DateEditorSkin.FONT_POPUPS);
        g.setColor(0);
        g.drawString(Integer.toString(editDate.get(Calendar.YEAR)),
                     4, 0, Graphics.LEFT | Graphics.TOP);
                     
        g.translate(-g.getTranslateX() + 4, -nextY);
    }
    
    /**
     * Draw the time components.
     *
     * @param g The Graphics object to paint to
     */
    protected void drawTimeComponents(Graphics g) {
        nextX = 4;
        nextY = (mode == DateField.DATE_TIME) ? 33 : 0;
        
        g.setFont(DateEditorSkin.FONT_POPUPS);
        g.setColor(0);
        g.drawString(Resource.getString(ResourceConstants.LCDUI_DF_TIME_MINI),
                     nextX, nextY, Graphics.LEFT | Graphics.TOP);

        nextY += DateEditorSkin.FONT_POPUPS.getHeight();
        
        int w = 0;
        int h = 0;
                
        g.translate(nextX, nextY);
        
        if (DateEditorSkin.IMAGE_TIME_BG != null) {
            g.drawImage(DateEditorSkin.IMAGE_TIME_BG, 0, 0,
                        Graphics.LEFT | Graphics.TOP);
            w = DateEditorSkin.IMAGE_TIME_BG.getWidth();
            h = DateEditorSkin.IMAGE_TIME_BG.getHeight();
            if (focusOn == HOURS_POPUP) {
                g.setColor(DateEditorSkin.COLOR_TRAVERSE_IND);
                g.drawRect(-2, -2, w + 3, h + 3);
            }
            hoursPopup.setElementSize(
                w - 4, DateEditorSkin.FONT_POPUPS.getHeight());
            hoursPopup.setBounds(g.getTranslateX(),
                                 g.getTranslateY() + h,
                                 w, DateEditorSkin.HEIGHT_POPUPS);
        }

        g.setFont(DateEditorSkin.FONT_POPUPS);
        g.setColor(0);
        g.drawString(Integer.toString(HOURS[hoursPopup.getSelectedIndex()]), 
                     3, 0, Graphics.LEFT | Graphics.TOP);

        g.translate(w + 2, 0);        
        if (DateEditorSkin.IMAGE_TIME_BG != null) {
            g.drawImage(DateEditorSkin.IMAGE_TIME_BG, 0, 0,
                        Graphics.LEFT | Graphics.TOP);
            w = DateEditorSkin.IMAGE_TIME_BG.getWidth();
            h = DateEditorSkin.IMAGE_TIME_BG.getHeight();
            if (focusOn == MINUTES_POPUP) {
                g.setColor(DateEditorSkin.COLOR_TRAVERSE_IND);
                g.drawRect(-2, -2, w + 3, h + 3);
            }
            minutesPopup.setElementSize(
                w - 4, DateEditorSkin.FONT_POPUPS.getHeight());
            minutesPopup.setBounds(g.getTranslateX(),
                                   g.getTranslateY() + h,
                                   w, DateEditorSkin.HEIGHT_POPUPS);
        }

        g.setFont(DateEditorSkin.FONT_POPUPS);
        g.setColor(0);
        g.drawString(DateFieldLFImpl.twoDigits(editDate.get(Calendar.MINUTE)),
                     3, 0, Graphics.LEFT | Graphics.TOP);
                     
        g.translate(w + 2, 0);
        
        nextX = (mode == DateField.TIME) ? 15 : 0;
        nextY = 45;

        g.translate(-g.getTranslateX(), 20);
        g.translate(10, 0);
        paintAmPm(g);
    }
    
    /**
     * Paint the am/pm indicators.
     * 
     * @param g The graphics context to paint to
     */
    protected void paintAmPm(Graphics g) {
        int clockStartX, clockStartY;

        if (!lf.CLOCK_USES_AM_PM) {
            clockStartY = 9;
        } else {
            // paint AM
            if (DateEditorSkin.IMAGE_RADIO != null) {
                g.drawImage((amSelected) ? 
                            DateEditorSkin.IMAGE_RADIO[1] : 
                            DateEditorSkin.IMAGE_RADIO[0],
                            0, 0, Graphics.LEFT | Graphics.TOP);
                
                if ((focusOn == AM_PM) && (amHilighted)) {
                    g.setColor(DateEditorSkin.COLOR_TRAVERSE_IND);
                    g.drawRect(0, 0, 
                               DateEditorSkin.IMAGE_RADIO[0].getWidth(), 
                               DateEditorSkin.IMAGE_RADIO[0].getHeight());
                    g.setColor(0);
                }
                
                if (DateEditorSkin.IMAGE_AMPM != null) {
                    int w = DateEditorSkin.IMAGE_AMPM.getWidth() / 2;
                    g.drawRegion(DateEditorSkin.IMAGE_AMPM,
                                 0, 0, 
                                 w, DateEditorSkin.IMAGE_AMPM.getHeight(),
                                 Sprite.TRANS_NONE,
                                 DateEditorSkin.IMAGE_RADIO[0].getWidth(), 
                                 (DateEditorSkin.IMAGE_RADIO[0].getHeight()/2),
                                 Graphics.VCENTER | Graphics.LEFT);
                }
            }
            
            g.translate(35, 0);
            // paint PM
            if (DateEditorSkin.IMAGE_RADIO != null) {
                g.drawImage((amSelected) ?
                            DateEditorSkin.IMAGE_RADIO[0] : 
                            DateEditorSkin.IMAGE_RADIO[1],
                            0, 0, Graphics.LEFT | Graphics.TOP);
                
                if ((focusOn == AM_PM) && (!amHilighted)) {
                    g.setColor(DateEditorSkin.COLOR_TRAVERSE_IND);
                    g.drawRect(0, 0, 
                               DateEditorSkin.IMAGE_RADIO[0].getWidth(), 
                               DateEditorSkin.IMAGE_RADIO[0].getHeight());
                    g.setColor(0);
                }
                
                if (DateEditorSkin.IMAGE_AMPM != null) {
                    int w = DateEditorSkin.IMAGE_AMPM.getWidth() / 2;
                    g.drawRegion(DateEditorSkin.IMAGE_AMPM,
                                 (DateEditorSkin.IMAGE_AMPM.getWidth() / 2), 0, 
                                 w, DateEditorSkin.IMAGE_AMPM.getHeight(),
                                 Sprite.TRANS_NONE,
                                 DateEditorSkin.IMAGE_RADIO[0].getWidth(), 
                                 (DateEditorSkin.IMAGE_RADIO[0].getHeight()/2),
                                 Graphics.VCENTER | Graphics.LEFT);
                }
            }
            g.translate(-35, 0);
            clockStartY = 22;
        }
    }
    
    /**
     * Called when select key is fired, to take further action on it,
     * based on where the focus is on the date editor.
     *
     * @return true if key was handled, false otherwise
     */
    protected boolean selectFired() {
        boolean done = false;
        ScreenLFImpl sLF = (ScreenLFImpl)lf.df.owner.getLF();
        switch (focusOn) {
            case DAY_POPUP:
                if (!dayPopup.open) {
                    sLF.lGetCurrentDisplay().showPopup(dayPopup);
                    dayPopup.open = !dayPopup.open;
                    done = true;
                } else {
                    dayPopup.open = !dayPopup.open;
                    int day = dayPopup.getSelectedIndex();                    
                    editDate.set(Calendar.DAY_OF_MONTH, day + 1);
                    sLF.lGetCurrentDisplay().hidePopup(dayPopup);
                }
                break;
            default:
                return super.selectFired();
        }
        return done;
    }

    /**
     * Handles internal traversal within the date editor.
     *
     * @param code the code of this key event
     * @return true always, since popup layers swallow all events
     */
    protected boolean traverseEditor(int code) {
        // handle internal traversal
        switch (focusOn) {
        case DAY_POPUP: 
            switch (code) {
            case Constants.KEYCODE_RIGHT:
                focusOn = MONTH_POPUP;
                 break;
            case Constants.KEYCODE_DOWN:
                if (mode == DateField.DATE_TIME) {
                    focusOn = HOURS_POPUP;
                }
                break;
            default:
                break;
            }
            break;
        case MONTH_POPUP:
            switch (code) {
            case Constants.KEYCODE_RIGHT:
                focusOn = YEAR_POPUP;
                break;
            case Constants.KEYCODE_LEFT:
                focusOn = DAY_POPUP;
                break;
            case Constants.KEYCODE_DOWN:
                if (mode == DateField.DATE_TIME) {
                    focusOn = MINUTES_POPUP;
                }
            default:
                // no-op
                break;
            }
            break;
        case YEAR_POPUP:
           switch (code) {
            case Constants.KEYCODE_LEFT:
                focusOn = MONTH_POPUP;
                break;
            case Constants.KEYCODE_RIGHT:
                if (mode == DateField.DATE_TIME) {
                    focusOn = HOURS_POPUP;
                }
                break;
            default:
                // no-op
                break;
            }
            break;
        case HOURS_POPUP:
            switch (code) {
            case Constants.KEYCODE_UP:
                if (mode == DateField.DATE_TIME) {
                    focusOn = DAY_POPUP;
                }
                break;
            case Constants.KEYCODE_DOWN:
                focusOn = AM_PM;
                break;
            case Constants.KEYCODE_LEFT:
                if (mode == DateField.DATE_TIME) {
                    focusOn = YEAR_POPUP;
                }
                break;
            case Constants.KEYCODE_RIGHT:
                focusOn = MINUTES_POPUP;
                break;
            default:
                // no-op
                break;
            }
            break;
        case MINUTES_POPUP:
            switch (code) {
            case Constants.KEYCODE_UP:
                if (mode == DateField.DATE_TIME) {
                    focusOn = MONTH_POPUP;
                }
                break;
            case Constants.KEYCODE_DOWN:
                focusOn = AM_PM;
                break;            
            case Constants.KEYCODE_LEFT:
                focusOn = HOURS_POPUP;
                break;
            default:
                // no-op
                break;
            }
            break;
        case AM_PM:
            traverseAmPm(code);
            break;
        default:
            Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                           "DateEditor.traverseEditor(), focusOn=" +focusOn);
            break;
        }
        return true;
    }

    /**
     * Handle internal traversal between the am/pm indicators.
     *
     * @param code the code of this key event
     * @return true if internal traversal occurred, false otherwise
     */
    protected boolean traverseAmPm(int code) {
        boolean traverse = false;
        switch (code) {
        case Constants.KEYCODE_UP:
            focusOn = HOURS_POPUP;
            traverse = true;
            break;
        case Constants.KEYCODE_LEFT:
            if (amHilighted) {
                if (mode == DateField.DATE_TIME) {
                    focusOn = YEAR_POPUP;
                    traverse = true;
                }
            } else {
                amHilighted = true;
                traverse = true;
            }
            break;
        case Constants.KEYCODE_RIGHT:
            if (amHilighted) {
                amHilighted = false;
                traverse = true;
            }
            break;
        default:
            // no-op
            break;
        }
        return traverse;
    }

    /**
     * Constant indicating the day of the month popup, used in the process
     * of current focus tracking inside the date editor.
     */
    protected static final int DAY_POPUP = 7;
    
    /**
     * Static array holding the day of the month values.
     */
    protected static String[] DAYS;

    /** The sub-popup layer used to select day of the month value */
    protected DEPopupLayer dayPopup;

}
