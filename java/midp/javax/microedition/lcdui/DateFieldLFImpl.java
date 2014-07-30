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

import java.util.Calendar;
import java.util.Date;

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;
import com.sun.midp.configurator.Constants;
import com.sun.midp.chameleon.*;
import com.sun.midp.chameleon.skins.DateFieldSkin;
import com.sun.midp.chameleon.skins.resources.DateFieldResources;
import com.sun.midp.chameleon.skins.DateEditorSkin;
import com.sun.midp.chameleon.skins.ScreenSkin;
import com.sun.midp.chameleon.skins.resources.DateEditorResources;
import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * This is the Look &amp; Feel implementation for DateField.
 */
class DateFieldLFImpl extends ItemLFImpl implements DateFieldLF {

    /**
     * Creates DateFieldLF for the passed in DateField object.
     * @param dateField the DateField object associated with this view
     */
    DateFieldLFImpl(DateField dateField) {
        super(dateField);

        DateFieldResources.load();
        DateEditorResources.load();

        df = dateField;
         if (editor == null) {
            editor = new DateEditor(this);
        }
    }
    
    // *****************************************************
    //  Package private methods
    // *****************************************************
    
    /**
     * Notifies Look &amp; Feel of a date change in the corresponding DateField.
     * @param date - the new Date set in the DateField
     */
    public void lSetDate(java.util.Date date) {
        lRequestPaint();
    }


    /**
     * Notifies Look &amp; Feel of a new input mode set in the corresponding
     * DateField.
     * @param mode the new input mode set in the DateField.
     */
    public void lSetInputMode(int mode) {
        lRequestInvalidate(true, true);
    }

    /**
     * Gets the date currently set on the date field widget.
     * This method is called by Date only if Date is initialized.
     * @return the date this widget is currently set to
     */
    public Date lGetDate() {
        return new java.util.Date(df.currentDate.getTime().getTime());
    }


    // *****************************************************
    //  Package private methods
    // *****************************************************
    
    /**
     * Indicate whether or not traversing should occur.
     *
     * @return <code>true</code> if traversing should be skipped;
     *     </code>false</code>, if the field is editable.
     */
    boolean shouldSkipTraverse() {
        return false;        
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
        Font f = DateFieldSkin.FONT;
        size[HEIGHT] = f.getHeight() + (2 * DateFieldSkin.PAD_V);
        int mode = df.mode;
        size[WIDTH] = 
            f.stringWidth(toString(df.currentDate, mode, df.initialized)) +
            (2 * DateFieldSkin.PAD_H);
            
        switch (mode) {
            case DateField.DATE:
                if (DateFieldSkin.IMAGE_ICON_DATE != null) {
                    size[WIDTH] += DateFieldSkin.IMAGE_ICON_DATE.getWidth();
                }
                if (size[WIDTH] < DateEditorSkin.WIDTH_DATE) {
                    size[WIDTH] = DateEditorSkin.WIDTH_DATE;
                }
                break;
            case DateField.TIME:
                if (DateFieldSkin.IMAGE_ICON_TIME != null) {
                    size[WIDTH] += DateFieldSkin.IMAGE_ICON_TIME.getWidth();
                }
                if (size[WIDTH] < DateEditorSkin.WIDTH_TIME) {
                    size[WIDTH] = DateEditorSkin.WIDTH_TIME;
                }
                break;
            case DateField.DATE_TIME:
                if (DateFieldSkin.IMAGE_ICON_DATETIME != null) {
                    size[WIDTH] += 
                        DateFieldSkin.IMAGE_ICON_DATETIME.getWidth();
                }
                if (size[WIDTH] < DateEditorSkin.WIDTH_DATETIME) {
                    size[WIDTH] = DateEditorSkin.WIDTH_DATETIME;
                }
                break;
            default:
                // for safety/completeness.
                Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                    "DateFieldLFImpl: mode=" + mode);
                break;
        }
        
        if (size[WIDTH] > availableWidth) {
            size[WIDTH] = availableWidth;
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

        return ((df.layout & Item.LAYOUT_2) != Item.LAYOUT_2);
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

        return ((df.layout & Item.LAYOUT_2) != Item.LAYOUT_2);
    }

    /**
     * Called from the Date Editor to save the selected Date.
     *
     * @param date The Date object to which current date should be set.
     */
    void saveDate(java.util.Date date) {
        synchronized (Display.LCDUILock) {
            df.setDateImpl(date);
            lSetDate(date);
        }
    }

    /**
     * Paints the content area of this DateField. 
     * Graphics is translated to contents origin.
     * @param g The graphics where DateField content should be painted
     * @param width The width available for the Item's content
     * @param height The height available for the Item's content
     */
    void lPaintContent(Graphics g, int width, int height) {

        currentDate = df.currentDate;
        mode  = df.mode;
        
        // draw background
        if (DateFieldSkin.IMAGE_BG != null) {
            CGraphicsUtil.draw9pcsBackground(g, 0, 0, width, height,
                DateFieldSkin.IMAGE_BG);
        } else {
            // draw widget instead of using images
            CGraphicsUtil.drawDropShadowBox(g, 0, 0, width, height,
                DateFieldSkin.COLOR_BORDER,
                DateFieldSkin.COLOR_BORDER_SHD, 
                DateFieldSkin.COLOR_BG);
        }       
                    
        // draw icon
        int iconWidth = 0;
        int btnOffset = 0;
        switch (mode) {
            case DateField.DATE:
                if (DateFieldSkin.IMAGE_ICON_DATE != null) {
                    iconWidth = DateFieldSkin.IMAGE_ICON_DATE.getWidth();
                    int yOffset = height - 
                        DateFieldSkin.IMAGE_ICON_DATE.getHeight();
                    if (yOffset > 0) {
                        yOffset = (int)(yOffset / 2);
                    } else {
                        yOffset = 0;
                    }
                    if (!ScreenSkin.RL_DIRECTION) {
                        btnOffset = width - iconWidth;
                    }
                    drawButtonBG(g, btnOffset, 0, iconWidth, height);
                    g.drawImage(DateFieldSkin.IMAGE_ICON_DATE,
                                btnOffset, yOffset, 
                                Graphics.LEFT | Graphics.TOP);
                }
                break;
            case DateField.TIME:
                if (DateFieldSkin.IMAGE_ICON_TIME != null) {
                    iconWidth = DateFieldSkin.IMAGE_ICON_TIME.getWidth();
                    int yOffset = height - 
                        DateFieldSkin.IMAGE_ICON_DATE.getHeight();
                    if (yOffset > 0) {
                        yOffset = (int)(yOffset / 2);
                    } else {
                        yOffset = 0;
                    }
                    if (!ScreenSkin.RL_DIRECTION) {
                        btnOffset = width - iconWidth;
                    }
                    drawButtonBG(g, btnOffset, 0, iconWidth, height);
                    g.drawImage(DateFieldSkin.IMAGE_ICON_TIME,
                                btnOffset, yOffset,
                                Graphics.LEFT | Graphics.TOP);
                }
                break;
            case DateField.DATE_TIME:
                if (DateFieldSkin.IMAGE_ICON_DATETIME != null) {
                    iconWidth = DateFieldSkin.IMAGE_ICON_DATETIME.getWidth();
                    int yOffset = height - 
                        DateFieldSkin.IMAGE_ICON_DATE.getHeight();
                    if (yOffset > 0) {
                        yOffset = (int)(yOffset / 2);
                    } else {
                        yOffset = 0;
                    }
                    if (!ScreenSkin.RL_DIRECTION) {
                        btnOffset = width - iconWidth;
                    }
                    drawButtonBG(g, btnOffset, 0, iconWidth, height);
                    g.drawImage(DateFieldSkin.IMAGE_ICON_DATETIME,
                                btnOffset, yOffset, 
                                Graphics.LEFT | Graphics.TOP);
                }
                break;
            default:
                // for safety/completeness.
                Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                    "DateFieldLFImpl: mode=" + mode);
                break;
        }


        int textOffset = DateFieldSkin.PAD_H;
        if (ScreenSkin.RL_DIRECTION) {
            textOffset = width - DateFieldSkin.PAD_H;
        }
        // we clip in case our text is too long
        g.clipRect(ScreenSkin.RL_DIRECTION ? DateFieldSkin.PAD_H + iconWidth : DateFieldSkin.PAD_H,
            DateFieldSkin.PAD_V,
            width - (2 * DateFieldSkin.PAD_H) - iconWidth,
            height - (2 * DateFieldSkin.PAD_V));

        if (!ScreenSkin.RL_DIRECTION) {
            g.translate(DateFieldSkin.PAD_H, DateFieldSkin.PAD_V);
            textOffset = 0;
        }
        
        // paint value
        g.setFont(DateFieldSkin.FONT);
        g.setColor(DateFieldSkin.COLOR_FG);
        g.drawString(toString(currentDate, mode, df.initialized),
                     textOffset, 0, ScreenSkin.TEXT_ORIENT | Graphics.TOP);

        if (!ScreenSkin.RL_DIRECTION) {
            g.translate(-DateFieldSkin.PAD_H, -DateFieldSkin.PAD_V);
        }
        if (editor.isPopupOpen() && editor.isSizeChanged()) {
            setPopupLocation();
        }
    }

    /**
     * Draw background of button 
     * @param g Graphics
     * @param x x coordinate
     * @param y y coordinate
     * @param w width
     * @param h height
     */
    void drawButtonBG(Graphics g, int x, int y, int w, int h) {
        if (DateFieldSkin.IMAGE_BUTTON_BG != null) {
            CGraphicsUtil.draw9pcsBackground(g, x, y, w, h,
                                             DateFieldSkin.IMAGE_BUTTON_BG);
        }
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
        super.lCallTraverse(dir, viewportWidth, viewportHeight, visRect);

        visRect[X] = 0;
        visRect[Y] = 0;
        visRect[HEIGHT] = bounds[HEIGHT];
        visRect[WIDTH] = bounds[WIDTH];

        if (!editor.isPopupOpen()) {
            if (!traversedIn) {
                traversedIn = true;
                return true;
            }
        }
        return false;
    }

    /**
     * Called by the system to indicate traversal has left this Item.
     */
    void lCallTraverseOut() {
        super.lCallTraverseOut();
        traversedIn = false;
        if (editor.isPopupOpen()) {
            editor.hideAllPopups();
            getCurrentDisplay().hidePopup(editor);
        }
    }

    /**
     * Called by the system to signal a key press.
     *
     * @param keyCode the key code of the key that has been pressed
     */
    void uCallKeyPressed(int keyCode) {
        if (keyCode != Constants.KEYCODE_SELECT) {
            return;
        }
        
        synchronized (Display.LCDUILock) {
            if (!editor.isPopupOpen()) {
                setPopupLocation();
                editor.show();
            } else {
                editor.hideAllPopups();
                getCurrentDisplay().hidePopup(editor);                
            }	    
        } // synchronized
        uRequestPaint();
    }

    /**
     * Set location of popup layer
     */
    private void setPopupLocation() {
        ScreenLFImpl sLF = (ScreenLFImpl)df.owner.getLF();
        // decide where to show popup: above, below or in
        // the middle of the screen (if both above/below don't
        // work out)

        int[] avalibleBounds = sLF.lGetCurrentDisplay().getBodyLayerBounds();

        int x = bounds[X] + contentBounds[X] + DateFieldSkin.PAD_V - sLF.viewable[X];
        int y = bounds[Y] + contentBounds[Y] - sLF.viewable[Y];

        if (y - DateEditorSkin.HEIGHT >= 0) {
            // can fit above
            y -= DateEditorSkin.HEIGHT - DateFieldSkin.PAD_V - avalibleBounds[Y];
        } else if (y + contentBounds[HEIGHT] + DateEditorSkin.HEIGHT + 2 * DateFieldSkin.PAD_V < avalibleBounds[HEIGHT]) {
            // can fit below
            y += contentBounds[HEIGHT] + 2 * DateFieldSkin.PAD_V + avalibleBounds[Y];
        } else {
            // fit in the middle of screen
            y = avalibleBounds[Y] + (avalibleBounds[HEIGHT] / 2) -
                (DateEditorSkin.HEIGHT / 2);
        }
        editor.setLocation(x, y);
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
            editor.setSizeChanged();

        }
    }

    /**
     * Get the localized day of the week text given a Calendar.
     *
     * @param calendar The Calendar object to retrieve the date from
     * @return String The day of the week text based on the date in the
     *                  calendar
     */
    static String dayOfWeekString(Calendar calendar) {
        String str;
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:    
                str = Resource.getString(ResourceConstants.LCDUI_DF_SUN);
                break;
            case Calendar.MONDAY:
                str = Resource.getString(ResourceConstants.LCDUI_DF_MON); 
                break;
            case Calendar.TUESDAY:
                str = Resource.getString(ResourceConstants.LCDUI_DF_TUE); 
                break;
            case Calendar.WEDNESDAY: 
                str = Resource.getString(ResourceConstants.LCDUI_DF_WED); 
                break;
            case Calendar.THURSDAY:  
                str = Resource.getString(ResourceConstants.LCDUI_DF_THU); 
                break;
            case Calendar.FRIDAY:    
                str = Resource.getString(ResourceConstants.LCDUI_DF_FRI); 
                break;
            case Calendar.SATURDAY:  
                str = Resource.getString(ResourceConstants.LCDUI_DF_SAT); 
                break;
            default: 
                str = Integer.toString(calendar.get(Calendar.DAY_OF_WEEK));
            break;
        }
        return str;
    }

    /**
     * A utility method to return a numerical digit as two digits
     * if it is less than 10 (used to display time in 2 digits 
     * if required, eg. 6:09pm).
     *
     * @param n The number to convert
     * @return String The String representing the number in two digits
     */
    static String twoDigits(int n) {
        if (n == 0) {
            return "00";
        } else if (n < 10) {
            return "0" + n;
        } else {
            return "" + n;
        }
    }

    /**
     * Get the am/pm text given a Calendar.
     *
     * @param calendar The Calendar object to retrieve the time from
     * @return String The am/pm text based on the time in the calendar
     */
    static String ampmString(Calendar calendar) {
        if (!CLOCK_USES_AM_PM) {
            return null;
        }
        return (calendar.get(Calendar.AM_PM) == Calendar.AM) ?
            Resource.getString(ResourceConstants.LCDUI_DF_AM) :
            Resource.getString(ResourceConstants.LCDUI_DF_PM);
    }

    /**
     * Get the hour given a Calendar, based on a 12-hour or 24-hour clock.
     *
     * @param calendar The Calendar object to retrieve the hour from
     * @return String The hour based on whether it's a 12-hour/24-hour clock
     */
	static String getHourString(Calendar calendar) {
        if (CLOCK_USES_AM_PM) {
            int hour = calendar.get(Calendar.HOUR);
            return (hour == 0) ? "12" : "" + hour;
        } else {
            return Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
        }
	}


    /**
     * Translate the mode of a DateField into a readable string.
     *
     * @param currentDate The current date set in the DateField.
     * @param mode The mode to translate.
     * @param initialized <code>true</code> if the human-readable string has
     *            been initialized; <code>false</code>, otherwise.
     * @return String A human-readable string representing the mode of the
     *              DateField.
     */
    static String toString(Calendar currentDate, int mode, 
			               boolean initialized) 
    {
        String str = null;
        if (!initialized) {
            switch (mode) {
                case DateField.DATE:
                    str = Resource.getString(ResourceConstants.LCDUI_DF_DATE);
                    break;
                case DateField.TIME:
                    str = Resource.getString(ResourceConstants.LCDUI_DF_TIME);
                    break;
                case DateField.DATE_TIME:
                    str = Resource.getString(
                        ResourceConstants.LCDUI_DF_DATETIME);
                    break;
                default:
                    // for safety/completeness.
                    Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                        "DateFieldLFImpl: mode=" + mode);
                    break;
            }
        } else {
            switch (mode) {
                case DateField.DATE:
                    str = Resource.getDateString(
                        dayOfWeekString(currentDate), 
                        "" + currentDate.get(Calendar.DATE), 
                        MONTH_NAMES[currentDate.get(Calendar.MONTH)].
                        substring(0, 3), 
                        Integer.toString(currentDate.get(Calendar.YEAR)));
                    break;
                case DateField.TIME:
                    str = Resource.getTimeString(getHourString(currentDate), 
                        "" + twoDigits(currentDate.get(Calendar.MINUTE)),
                        "" + currentDate.get(Calendar.SECOND), 
                        ampmString(currentDate));
                    break;
                case DateField.DATE_TIME:
                    str = Resource.getDateTimeString(
                        dayOfWeekString(currentDate), 
                        Integer.toString(currentDate.get(Calendar.DATE)), 
                        MONTH_NAMES[currentDate.get(Calendar.MONTH)].
                        substring(0, 3), 
                        Integer.toString(currentDate.get(Calendar.YEAR)), 
                        getHourString(currentDate), 
                        twoDigits(currentDate.get(Calendar.MINUTE)), 
                        Integer.toString(currentDate.get(Calendar.SECOND)), 
                        ampmString(currentDate));
                    break;
                default:
                    // for safety/completeness.
                    Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                        "DateFieldLFImpl: mode=" + mode);
                    break;
            }
        }
        return str;
    }

    /**
     * Static array holding the names of the 12 months.
     */
    static final String[] MONTH_NAMES = {
        Resource.getString(ResourceConstants.LCDUI_DF_JANUARY), 
        Resource.getString(ResourceConstants.LCDUI_DF_FEBRUARY),
        Resource.getString(ResourceConstants.LCDUI_DF_MARCH),
        Resource.getString(ResourceConstants.LCDUI_DF_APRIL),
        Resource.getString(ResourceConstants.LCDUI_DF_MAY),
        Resource.getString(ResourceConstants.LCDUI_DF_JUNE),
        Resource.getString(ResourceConstants.LCDUI_DF_JULY),
        Resource.getString(ResourceConstants.LCDUI_DF_AUGUST),
        Resource.getString(ResourceConstants.LCDUI_DF_SEPTEMBER),
        Resource.getString(ResourceConstants.LCDUI_DF_OCTOBER),
        Resource.getString(ResourceConstants.LCDUI_DF_NOVEMBER),
        Resource.getString(ResourceConstants.LCDUI_DF_DECEMBER)
    };

    /**
     * Flag to signal the clock representation uses AM and PM notation.
     * This is dummy right now, and always set to true since there is
     * not easy to find out from the system if the clock is using a 
     * 12-hour or 24-hour mode.
     */
    static final boolean CLOCK_USES_AM_PM = true;
    
    /**
     * DateField associated with this view.
     */
    DateField df;

    /**
     * Current date being handled by this datefield impl.
     */
    private Calendar currentDate;

    /**
     * Current input mode being handled by this datefield impl.
     */
    int mode;

    /**
     * A flag indicating a prior call to uCallTraverse()
     */
    private boolean traversedIn;
    
    /**
     * The editor for this DateField.
     */
    DateEditor editor = null;

}
