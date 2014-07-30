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

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;
import com.sun.midp.lcdui.Text;

import java.util.Timer;
import java.util.TimerTask;
import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;
import com.sun.midp.chameleon.skins.AlertSkin;
import com.sun.midp.chameleon.skins.ScreenSkin;
import com.sun.midp.chameleon.skins.resources.AlertResources;

/**
 * This is the look &amp; feel implementation for Alert.
 * See DisplayableLF for naming convention.
 */
class AlertLFImpl extends ScreenLFImpl implements AlertLF {

    /**
     * Creates an AlertLF for the passed in Alert instance.
     * @param a The Alert associated with this look &amp; feel
     */
    AlertLFImpl(Alert a) {
        super(a);

        alert = a;
        
        AlertResources.load();
        
        // SYNC NOTE: Hold the lock to prevent changes to indicator
        // internal state
        synchronized (Display.LCDUILock) {
            layout();
        }

        // The viewport is equal to the height of the alert
        viewport[HEIGHT] = AlertSkin.HEIGHT - AlertSkin.PAD_VERT;        
    }

    /**
     * Returns the actual needed height for the Alert instead of
     * the maximum possible height.
     * @return height of the area available to the application
     */
    public int lGetHeight() {        
        // This should return the height available for content
        // within the Alert dialog. It can be used by applications
        // to choose appropriately sized content.
        return AlertSkin.HEIGHT - AlertSkin.TITLE_HEIGHT;
    }

    // ************************************************************
    //  public methods - FormLF interface implementation
    // ************************************************************

    /**
     * Determines if alert associated with this view is modal.
     *
     * @return true if this AlertLF should be displayed as modal
     */    
    public boolean lIsModal() {
        if (alert.numCommands > 1) {
            return true;
        }

        if (!isLayoutValid) {
            layout();
        }
        return (maxScroll > 0);
    }

    /**
     * Gets default timeout for the alert associated with this view
     * @return the default timeout
     */
    public int lGetDefaultTimeout() {
        return AlertSkin.TIMEOUT;
    }

    /**
     * Get command that Alert.DISMISS_COMMAND is mapped to.
     * 
     * @return command that Alert.DISMISS_COMMAND is mapped to
     */
    public Command lGetDismissCommand() {
        return OK;
    }

    /**
     * Notifies look&feel object of a timeout change.
     * 
     * @param timeout - the new timeout set in the corresponding Alert.
     */
    public void lSetTimeout(int timeout) {
        try {
            if (timerTask != null) {
                timerTask.cancel();
            } 
            
            if (timeout == Alert.FOREVER) {
                timerTask = null;
            } else {
                timerTask = new TimeoutTask();
                if (timeoutTimer == null) {
                    timeoutTimer = new Timer();
                }
                timeoutTimer.schedule(timerTask, timeout);
            }                       
        } catch (Throwable t) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_HIGHUI,
                              "Throwable while lSetTimeout");
            }
        } 
    }

    /**
     * Notifies look&feel object of a Alert type change.
     * 
     * @param type - the new AlertType set in the corresponding Alert.
     */
    public void lSetType(AlertType type) {
        lRequestInvalidate();
    }


    /**
     * Notifies look&feel object of a string change.
     * 
     * @param oldString - the old string set in the corresponding Alert.
     * @param newString - the new string set in the corresponding Alert.
     */
    public void lSetString(String oldString, String newString) {
        lRequestInvalidate();
    }

    /**
     * Notifies look&feel object of an image change.
     * 
     * @param oldImg - the old image set in the corresponding Alert.
     * @param newImg - the new image set in the corresponding Alert.
     */
    public void lSetImage(Image oldImg, Image newImg) {
        lRequestInvalidate();
    }

    /**
     * Notifies look&feel object of an indicator change.
     * 
     * @param oldIndicator - the old indicator set in the corresponding Alert
     * @param newIndicator - the new indicator set in the corresponding Alert
     */
    public void lSetIndicator(Gauge oldIndicator, Gauge newIndicator) {
        lRequestInvalidate();
    }

    /**
     * This method is responsible for:
     * (1) Re-layout the contents
     * (2) setup the viewable/scroll position
     * (3) repaint contents
     */
    public void uCallInvalidate() {
        boolean wasModal = maxScroll > 0 || alert.numCommands > 1;

        super.uCallInvalidate();
        
        synchronized (Display.LCDUILock) {
            if (wasModal != lIsModal()) {
                lSetTimeout(alert.getTimeout());
            }
            lRequestPaint();
        }
        
        setVerticalScroll();
    }

    /**
     * Notify return screen about screen size change
     */
    public void uCallSizeChanged(int w, int h) {
        super.uCallSizeChanged(w,h);
        Displayable returnScreen = alert.getReturnScreen();
        if (returnScreen != null) {
            (returnScreen.displayableLF).uCallSizeChanged(w,h);
        }
    }

    /**
     * Notifies look &amp; feel object of a command addition 
     * to the <code>Displayable</code>.
     * 
     * SYNC NOTE: The caller of this method handles synchronization.
     *
     * @param cmd the command that was added
     * @param i the index of the added command in Displayable.commands[] 
     *        array
     */
    public void lAddCommand(Command cmd, int i) {
        super.lAddCommand(cmd, i);
        // make alert Modal
        if (alert.numCommands == 2) {
            lSetTimeout(alert.getTimeout());
        }
    }

    /**
     * Notifies look &amp; feel object of a command removal 
     * from the <code>Displayable</code>.
     *
     * SYNC NOTE: The caller of this method handles synchronization.
     * 
     * @param cmd the command that was removed
     * @param i the index of the removed command in Displayable.commands[] 
     *        array
     */
    public void lRemoveCommand(Command cmd, int i) {
        super.lRemoveCommand(cmd, i);
        // remove modality if it was forced by command presence
        if (alert.numCommands == 1) {
            lSetTimeout(alert.getTimeout());
        }
    }

    /**
     * Paint the contents of this Alert given the graphics context.
     *
     * @param g The Graphics object to paint this Alert to
     * @param target the target Object of this repaint
     */
    public void uCallPaint(Graphics g, Object target) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           " # in AlertLFImpl: uCallPaint "+
                           viewable[X]+","+
                           viewable[Y]+","+
                           viewable[WIDTH]+","+
                           viewable[HEIGHT]);
        }
        clipx = g.getClipX();
        clipy = g.getClipY();
        clipw = g.getClipWidth();
        cliph = g.getClipHeight();
        
        synchronized (Display.LCDUILock) {
            // titleHeight will be AlertSkin.TITLE_HEIGHT
            lPaintTitleBar(g);   
            // Restore the clip            
            g.setClip(clipx, AlertSkin.TITLE_HEIGHT, 
                      clipw, cliph - AlertSkin.TITLE_HEIGHT);            
            // translate the viewport to offset for the titlebar
            g.translate(0, AlertSkin.TITLE_HEIGHT);           
            // translate to accommodate any scrolling we've done
            g.translate(0, -viewable[Y]);            
            // paint the indicator            
            int indHeight = lPaintIndicator(g);
            // translate to offset for the indicator            
            g.translate(0, indHeight);
            // paint the image
            int imgHeight = lPaintImage(g);         
            // translate to offset for the image
            g.translate(0, imgHeight);          
            // paint the body text
            lPaintContent(g);      
            // restore the translate
            g.translate(-g.getTranslateX(), -g.getTranslateY());
        } // synchronized
        setVerticalScroll();
    }
    
    // *****************************************************
    //  Package private methods
    // *****************************************************

    /**
     * Paint the title bar area for this alert.
     *
     * @param g the Graphics to draw too
     */
    void lPaintTitleBar(Graphics g) {
        if (alert.type == null && alert.title == null) {
            return;
        }
        
        icon = (alert.type == null) ? null : getIcon(alert.type);
        
        title = getTitle(alert.type);
        
        titlew = AlertSkin.FONT_TITLE.stringWidth(title);        
        if (icon != null) {
            iconw = icon.getWidth();
            titlew += (AlertSkin.PAD_HORIZ + iconw);
            iconh = icon.getHeight();
            // We vertically center the icon
            icony = 0;
            if (iconh < AlertSkin.TITLE_HEIGHT) {
                icony = (AlertSkin.TITLE_HEIGHT - iconh) / 2;
            }
        } else {
            iconw = 0;
        }
        
        if (titlew > AlertSkin.WIDTH - (2 * AlertSkin.TITLE_MARGIN)) {
            titlew = AlertSkin.WIDTH - (2 * AlertSkin.TITLE_MARGIN);
        }
        
        // We vertically center the title text
        titley = 
            (AlertSkin.TITLE_HEIGHT - AlertSkin.FONT_TITLE.getHeight()) / 2;

        switch (AlertSkin.TITLE_ALIGN) {
            case Graphics.RIGHT:
                titlex = AlertSkin.WIDTH - AlertSkin.TITLE_MARGIN;
                break;
            case Graphics.HCENTER:
                titlex = (AlertSkin.WIDTH - titlew) / 2;
                break;
            case Graphics.LEFT:
            default:
                titlex = AlertSkin.TITLE_MARGIN;
                break;
        }
        
        // We'll clip down the "box" for the title just in case
        // its a really long string
        // g.clipRect(titlex, 0, titlew, AlertSkin.TITLE_HEIGHT);
        
        if (icon != null) {
            g.drawImage(icon, titlex, icony,
                        ScreenSkin.TEXT_ORIENT | Graphics.TOP);
            titlex += (AlertSkin.PAD_HORIZ + iconw);
            titlew -= (AlertSkin.PAD_HORIZ + iconw);
        }

        g.translate(titlex, titley);
        Text.drawTruncString(g, title,
                AlertSkin.FONT_TITLE, AlertSkin.COLOR_TITLE, titlew);
        g.translate(-titlex, -titley);
    }
    
    /**
     * Paint the gauge indicator for this alert, if there is one.
     *
     * @param g the Graphics to draw to
     * @return the height occupied by the indicator
     */
    int lPaintIndicator(Graphics g) {
        if (alert.indicator == null) {
            return 0;
        }
        
        GaugeLFImpl indicatorLF = (GaugeLFImpl)alert.indicator.gaugeLF;

        // We center the gauge
        int offsetx = (int)
            ((AlertSkin.WIDTH - indicatorLF.bounds[WIDTH]) / 2);
            
        g.translate(offsetx, 0);                
        // SYNC NOTE: paint in gauge does not involve app code.
        // So it's OK to call it from LCDUILock block.
        indicatorLF.lCallPaint(g, indicatorLF.bounds[WIDTH], 
                               indicatorLF.bounds[HEIGHT]);

        g.translate(-offsetx, 0);
        return indicatorLF.bounds[HEIGHT];
    }
    
    /**
     * Paint the application-supplied image for this alert, if there is one.
     *
     * @param g the Graphics to draw to
     * @return the height occupied by the image
     */
    int lPaintImage(Graphics g) {
        if (alert.image == null) {
            return 0;
        }
        
        // We center the image
        int offsetx = (int)
            ((AlertSkin.WIDTH - alert.image.getWidth()) / 2);
            
        g.drawImage(alert.image, offsetx, 0, Graphics.TOP | Graphics.LEFT);
        return alert.image.getHeight();
    }
    
    /**
     * Paint the text content of this alert, if there is any
     *
     * @param g the Graphics to draw to
     */
    void lPaintContent(Graphics g) {        
        if (alert.text != null) {
            g.translate(AlertSkin.MARGIN_H, 0);
            Text.paint(g, alert.text, AlertSkin.FONT_TEXT,
                       AlertSkin.COLOR_FG, 0,
                       viewable[WIDTH], viewable[HEIGHT],
                       0, Text.NORMAL, null);
            g.translate(-AlertSkin.MARGIN_H, 0);
        }
    }
   
    /**
     * Handle a key press
     *
     * @param keyCode the key which was pressed
     */
    void uCallKeyPressed(int keyCode) {
        int gameAction = KeyConverter.getGameAction(keyCode);

        synchronized (Display.LCDUILock) {
            switch (gameAction) {
            case Canvas.UP:
                if (viewable[Y] > 0) {
                    viewable[Y] -= AlertSkin.SCROLL_AMOUNT;
                    if (viewable[Y] < 0) {
                        viewable[Y] = 0;
                    }
                    lRequestPaint();
                }
                break;

            case Canvas.DOWN:
                if (viewable[Y] < maxScroll) {
                    viewable[Y] += AlertSkin.SCROLL_AMOUNT;
                    if (viewable[Y] > maxScroll) {
                        viewable[Y] = maxScroll;
                    }
                    lRequestPaint();
                }
                break;
            }
        }
        setVerticalScroll();
    }
    
    /**
     * Handle a key repeat
     *
     * @param keyCode the key which was repeated
     */
    void uCallKeyRepeated(int keyCode) {
        uCallKeyPressed(keyCode);
    }
    
    /**
     * Notify this Alert that is being displayed on the
     * given Display and whether it needs to initialize its
     * highlight
     */
    void lCallShow() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "# in AlertLFImpl: lCallShow");        
        }

        super.lCallShow();

        if (alert.type != null) {
            currentDisplay.playAlertSound(alert.type);
        }

        if (alert.indicator != null) {
            ((GaugeLFImpl)alert.indicator.gaugeLF).lCallShowNotify();
        }
        
        if (!isLayoutValid) {
            layout();
        }

        lSetTimeout(alert.getTimeout());
        
        // We reset any scrolling done in a previous showing
        viewable[Y] = 0;
        
        setVerticalScroll();
    }

    /**
     * Notify this Alert that it will no longer be displayed
     * on the given Display
     */
    void lCallHide() {
        
        super.lCallHide();
        
        if (alert.indicator != null) {
            ((GaugeLFImpl)alert.indicator.gaugeLF).lCallHideNotify();
        }

        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    /**
     * Cancel the timer whenever the alert is frozen. Alert is frozen
     * when the display does not have foreground
     */
    void lCallFreeze() {
        
        super.lCallFreeze();

        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    /** 
     * Called upon content change to schedule a request for relayout and 
     * repaint. 
     */
    void lRequestInvalidate() {
        super.lRequestInvalidate();
        isLayoutValid = false;
    }
    
    /**
     * Set the vertical scroll indicators for this Screen.
     * We override this from our superclass because the viewport[] is
     * set to the entire Alert dialog, but scrolling is confined to the
     * "inner" viewport we've constructed beneath the title to scroll
     * the text content. This scrolling behavior is maintained by
     * 'maxScroll' - which represents the maximum number of pixels
     * needed to scroll in order to reach the bottom of the scrollable
     * content. If maxScroll is 0, no scrolling is necessary.
     */
    void setVerticalScroll() {
        
        if (maxScroll == 0) {
            setVerticalScroll(0, 100);
        } else {
            setVerticalScroll((viewable[Y] * 100 / (maxScroll)),
                              ((AlertSkin.HEIGHT - AlertSkin.TITLE_HEIGHT) 
                                    * 100 / viewable[HEIGHT]));
        }
    }

    /**
     * Layout the content of this Alert given the width and
     * height parameters
     */
    void layout() {
        super.layout();

        // layout() is called from DisplayableLFImpl constructor
        // and at that time alert is not initialized
        if (alert == null) {
            maxScroll = 0;
            return;
        }

        // The width of the viewable area is equal to the width of
        // the alert minus a left and right margin

        viewable[WIDTH] = getDisplayableWidth() - (2 * AlertSkin.MARGIN_H);

        // height of activity indicator, if any
        int indHeight = 0;                
        if (alert.indicator != null) {
            GaugeLFImpl indicatorLF = (GaugeLFImpl)alert.indicator.gaugeLF;

            if (indicatorLF.bounds == null) {
                indicatorLF.bounds = new int[4];
            }
            
            int pW = indicatorLF.lGetPreferredWidth(-1);
            if (pW > viewable[WIDTH] - (2 * AlertSkin.PAD_HORIZ)) {
                pW = viewable[WIDTH] - (2 * AlertSkin.PAD_HORIZ);
            }
            indHeight = indicatorLF.lGetPreferredHeight(pW);

            // We assign the item a bounds which is its pixel location,
            // width, and height in coordinates which represent offsets
            // of the viewport origin (that is, are in the viewport
            // coordinate space)
            indicatorLF.bounds[X] = 0;
            indicatorLF.bounds[Y] = 0;
            indicatorLF.bounds[WIDTH]  = pW;
            indicatorLF.bounds[HEIGHT] = indHeight;
        }
        
        // height of the alert's image, if any
        int imageHeight = (alert.image == null) ? 0 : alert.image.getHeight();
        
        // height of the alert's text content, if any
        int textHeight = (alert.text == null) ? 0 : 
            Text.getHeightForWidth(alert.text, AlertSkin.FONT_TEXT,
                                   viewable[WIDTH], 0);
        
        // This gives us the height of the scrollable area
        viewable[HEIGHT] = AlertSkin.PAD_VERT;
        if (indHeight > 0) {            
            viewable[HEIGHT] += (indHeight + AlertSkin.PAD_VERT);
        }
        if (imageHeight > 0) {
            viewable[HEIGHT] += (imageHeight + AlertSkin.PAD_VERT);
        }
        if (textHeight > 0) {
            viewable[HEIGHT] += (textHeight + AlertSkin.PAD_VERT);
        }
        
        maxScroll = viewable[HEIGHT] - 
            (AlertSkin.HEIGHT - AlertSkin.TITLE_HEIGHT);
        if (maxScroll < 0) {
            maxScroll = 0;
        }
        
        isLayoutValid = true;
    }
   
    /**
     * Returns the system image to draw in title area.
     * If AlertType is not set, no image is drawn.
     * @param alertType The type of the Alert
     * @return the image to draw in title area
     */
    static Image getIcon(AlertType alertType) {
        if (alertType == null) {
            return null;
        }
        if (alertType.equals(AlertType.INFO)) {
            return AlertSkin.IMAGE_ICON_INFO;
        } else if (alertType.equals(AlertType.WARNING)) {
            return AlertSkin.IMAGE_ICON_WARN;
        } else if (alertType.equals(AlertType.ERROR)) {
            return AlertSkin.IMAGE_ICON_ERRR;
        } else if (alertType.equals(AlertType.ALARM)) {
            return AlertSkin.IMAGE_ICON_ALRM;
        } else { 
            return AlertSkin.IMAGE_ICON_CNFM;
        }
    }
    
    /**
     * Returns the system image to draw in title area.
     * If AlertType is not set, no image is drawn.
     * @param alertType The type of the Alert
     * @return the image to draw in title area
     */
    String getTitle(AlertType alertType) {
        if (alert.title != null) {
            return alert.title;
        }
        if (alertType.equals(AlertType.INFO)) {
            return AlertSkin.TEXT_TITLE_INFO;
        } else if (alertType.equals(AlertType.WARNING)) {
            return AlertSkin.TEXT_TITLE_WARN;
        } else if (alertType.equals(AlertType.ERROR)) {
            return AlertSkin.TEXT_TITLE_ERRR;
        } else if (alertType.equals(AlertType.ALARM)) {
            return AlertSkin.TEXT_TITLE_ALRM;
        } else {
            return AlertSkin.TEXT_TITLE_CNFM;
        }
    }
    
    /**
     * Calculate the height a displayable would occupy if it was to
     * be displayed.
     *
     * @return the height a displayable would occupy 
     */
    public int getDisplayableHeight() {
        return currentDisplay != null ?
            currentDisplay.getDisplayableHeight() :
            AlertSkin.HEIGHT;
    }

    /**
     * Calculate the width a displayable would occupy if it was to
     * be displayed
     *
     * @return the width a displayable would occupy 
     */
    public int getDisplayableWidth() {
        return currentDisplay != null ?
            currentDisplay.getDisplayableWidth() :
            AlertSkin.WIDTH;
    }

    /**
     * The maximum amount of scroll needed to see all the contents
     * @return get the maximum scroll amount
     */
    protected int getMaxScroll() {
        return maxScroll;
    }
    
    /**
     * This is the number of pixels left from the previous "page"
     * when a page up or down occurs. The same value is used for line by
     * line scrolling 
     * @return the number of pixels. 
     */
    protected int getScrollAmount() {
        return AlertSkin.SCROLL_AMOUNT;
    }

    
    /**
     * Static default Command for "OK"
     */
    static final Command OK =
        new Command(Resource.getString(ResourceConstants.DONE), 
                    Command.OK, 0);
    
    /**
     * A Timer which serves all Alert objects to schedule
     * their timeout tasks
     */
    static Timer timeoutTimer;
    
    /**
     * Variables to hold the clip coordinates
     */
    int clipx, clipy, clipw, cliph;

    /**
     * The maximum amount of scroll needed to see all the contents
     * of the Alert
     */
    int maxScroll;
    
    /**
     * The total maximum height of this Alert. 
     * This will be <= AlertSkin.MAX_HEIGHT.
     */
    int totalHeight;
    
    /**
     * Alert associated with this view
     */
    Alert alert;
    
    /**
     * The icon for the Alert
     */
    Image icon;

    /**
     * A TimerTask which will be set to expire this Alert after
     * its timeout period has elapsed.
     */
    TimerTask timerTask;

    /**
     * A flag indicates that whether the layout of the alert
     * is known.
     */
    boolean isLayoutValid; // Default is false
    
    /** local variable for the paint method (title x location) */
    int titlex;

    /** local variable for the paint method (title y location) */
    int titley;

    /** local variable for the paint method (title width) */
    int titlew;

    /** local variable for the paint method (title height) */
    int titleh;

    /** local variable for the paint method (icon y location) */
    int icony;

    /** local variable for the paint method (icon width) */
    int iconw;

    /** local variable for the paint method (icon height) */
    int iconh;

    /** Alert's title */
    String title;
    

// *****************************************************
//  Internal Class
// *****************************************************

    /**
     * A TimerTask subclass which will notify the Display to
     * make the 'returnScreen' of this Alert the new current screen.
     */
    private class TimeoutTask extends TimerTask {
        
        /**
         * Create a new timeout task
         */
        TimeoutTask() { }
        
        /**
         * Simply set the Display's current screen to be this
         * Alert's return screen
         */
        public void run() {
            // It could be this timeout task got scheduled and in
            // the meantime the alert's contents were updated. There is
            // a timing condition whereby an old timeout task could dismiss
            // an alert which is now updated, so we stop and check to make
            // sure the alert is not modal before we go ahead and dismiss it
            synchronized (Display.LCDUILock) {
                if (lIsModal() || alert.getTimeout() == Alert.FOREVER) {
                    return;
                }
            }
            alert.uNotifyTimeout();
        }
    } // TimeoutTask
}
