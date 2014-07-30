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

import javax.microedition.lcdui.game.GameCanvas;
import com.sun.midp.lcdui.EventConstants;
import com.sun.midp.lcdui.GameMap;
import com.sun.midp.lcdui.GameCanvasLFImpl;
import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;
import com.sun.midp.chameleon.skins.*;

/**
* This is the look &amp; feel implementation for Displayable.
*/
class DisplayableLFImpl implements DisplayableLF {


    /**
     * Creates DisplayableLF for the passed in Displayable.
     * @param d the Displayable object associated with this
     * look &amps; feel.
     */
    DisplayableLFImpl(Displayable d) {
        owner = d;
        resetViewport();
    }
    
    // ************************************************************
    //  public methods - DisplayableLF interface implementation
    // ************************************************************
    
    /**
     * Returns the width of the area available to the application.
     * @return width of the area available to the application
     */
    public int lGetWidth() {

        // NOTE: If we update the viewport size in all cases we change
        // the displayable's size then we needn't to update it inside
        // the method.  So we need to investigate removing
        // 'resetViewport()' below.

        resetViewport();
        return viewport[WIDTH];
    }
    
    /**
     * Returns the height of the area available to the application.
     * @return height of the area available to the application
     */
    public int lGetHeight() {

        // NOTE: If we update the viewport size in all cases we change
        // the displayable's size then we needn't to update it inside
        // the method.  So we need to investigate removing
        // 'resetViewport()' below.

        resetViewport();
        return viewport[HEIGHT];
    }
    
    /**
     * Notifies Displayable's look &amp; feel object of a title change.
     *
     * SYNC NOTE: The caller of this method handles synchronization.
     *
     * @param oldTitle the old title, or <code>null</code> for no title
     * @param newTitle the new title, or <code>null</code> for no title
     */
    public void lSetTitle(String oldTitle, String newTitle) {
        Display d = lGetCurrentDisplay();
        if (d != null) {
            d.lSetTitle(this, newTitle);
        }
        // Displayable area size may be affected by the presence or
        // absence of the title so we need to update the viewport
        resetViewport();
    }


    /**
     * Notifies Displayable's look &amp; feel object of a ticker change.
     *
     * SYNC NOTE: The caller of this method handles synchronization.
     *
     * @param oldTicker the old ticker, or <code>null</code> for no ticker
     * @param newTicker the new ticker, or <code>null</code> for no ticker
     */
    public void lSetTicker(Ticker oldTicker, Ticker newTicker) {
        Display d = lGetCurrentDisplay();
        if (d != null) {
            d.lSetTicker(this, newTicker);
        }
        if (newTicker != null) {
            newTicker.tickerLF.lSetOwner(this);            
        }
        // Displayable area size may be affected by the presence or
        // absence of the ticker so we need to update the viewport
        resetViewport();
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
        updateCommandSet();
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
        updateCommandSet();
    }

    /**
     * Updates command set if this Displayable is visible.
     *
     * SYNC NOTE: Caller should hold LCDUILock.
     */
    public void updateCommandSet() {
        if (state == SHOWN && currentDisplay != null) {
            currentDisplay.updateCommandSet();
        }
    }

    /**
     * Return the Display instance in which the LF is currently shown.
     * @return the current Display.
     */
    public Display lGetCurrentDisplay() {
        return currentDisplay;
    }

    /**
     * Implement public API isShown().
     * @return true if current DisplayableLF is interactive with user.
     */
    public boolean lIsShown() {
        return !(currentDisplay == null) && currentDisplay.isShown(this);
    }

    /**
     * Notifies look and feel object of a full screen mode change.
     * If true, this DisplayableLF will take up as much screen real estate
     * as possible.
     *
     * @param mode - if true displayable should be displayed 
     *               without title, ticker, etc.; if false - otherwise 
     */
    public void uSetFullScreenMode(boolean mode) {
        boolean requestRepaint = false;
        
        synchronized (Display.LCDUILock) {
            if (lIsShown()) {
                // IMPL_NOTE: Notify MainWindow of screen mode change
                
                // currentDisplay is not null when lIsShown is true
                currentDisplay.lSetFullScreen(mode);
                
                layout();
                updateCommandSet();
                requestRepaint = true;
                
            } else {
                // Layout needs to happen even if the canvas is not visible
                // so that correct width and height could be returned 
                // in getWidth() and getHeight()
                layout();
            }  
        } 

        // app's sizeChanged has to be called before repaint
        synchronized (Display.LCDUILock) {
            if (requestRepaint) {
                lRequestPaint();
            }
        }
    }

    /**
     * \Need revisit Move this to CanvasLFImpl.
     * Called to get key mask of all the keys that were pressed.
     * @return keyMask  The key mask of all the keys that were pressed.
     */
    public int uGetKeyMask() {
        synchronized (Display.LCDUILock) {
            // don't release currently pressed keys
            int savedMaskCopy = stickyKeyMask | currentKeyMask;
            stickyKeyMask = 0;
            return savedMaskCopy;
        }
    }

    /**
     * Return the associated Displayable object.
     * @return the Displayable.
     */
    public Displayable lGetDisplayable() {
        return owner;
    }

    /**
     * Set the display instance the Displayable is associated with.
     * Caller should hold LCDUILock around this call.
     *
     * @param d Display instance in which this DisplayableLF is visible.
     *                null if this DisplayableLF is no longer visible.
     */
    public void lSetDisplay(Display d) {
        // ASSERT(d == null || currentDisplay == null)
        currentDisplay = d;
    }

    /**
     * Prepare to show this LF on physical screen.
     * This function simply calls lCallShow() after obtaining LCDUILock.
     */
    public void uCallShow() {

        boolean copyDefferedSizeChange;

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION,
                    LogChannels.LC_HIGHUI_FORM_LAYOUT,
                    "# in DisplayableLFImpl: uCallShow");
        }

        synchronized (Display.LCDUILock) {

            // Assure correct screen mode
            currentDisplay.lSetFullScreen(owner.isInFullScreenMode);
            copyDefferedSizeChange = defferedSizeChange;
            defferedSizeChange = false;
        }

        if (copyDefferedSizeChange) {
            synchronized (Display.calloutLock) {
                try {
                    owner.sizeChanged(viewport[WIDTH], viewport[HEIGHT]);
                } catch (Throwable t) {
                    Display.handleThrowable(t);
                }
            }
        }
        synchronized (Display.LCDUILock) {
            // Do the internal show preparation
            lCallShow();
            if (pendingInvalidate || copyDefferedSizeChange) {
                lRequestInvalidate();
            }
        }
    }

    /**
     * Prepare to show this LF on physical screen. This is the
     * internal version of showNotify() function as defined in MIDP spec.
     * It is called immediately prior to this LF being made visible
     * on the display. The LF should load any resource that is
     * needed, layout. App's paint() should NOT be called in this function.
     * Instead, it should be in the uCallPaint() that will be called on this
     * LF shortly after.
     *
     * This function sets this DisplayableLF to SHOWN state.
     */
    void lCallShow() {

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "# in DisplayableLFImpl: lCallShow");        
        }

        // This will suppress drags, repeats and ups until a
        // corresponding down is seen.
        sawPointerPress = sawKeyPress = false;

        // IMPL_NOTE: Move this to CanvasLFImpl
        // set mapping between GameCanvas and DisplayAccess
        // set Game key event flag based on value passed in
        // GameCanvas constructor.
        if (owner instanceof GameCanvas) {
            GameMap.registerDisplayAccess(owner, currentDisplay.accessor);
            stickyKeyMask = currentKeyMask = 0;
        } else {
            // set the keymask to -1 when
            // the displayable is not a GameCanvas.
            stickyKeyMask = currentKeyMask = -1;
        }

       // IMPL_NOTES: should be remove after interface will be fixed
         currentDisplay.setVerticalScroll(getVerticalScrollPosition(), 
                                          getVerticalScrollProportion());

        state = SHOWN;

    } // lCallShow()


    /**
     * Get the current vertical scroll position
     *
     * @return int The vertical scroll position on a scale of 0-100
     */
    public int getVerticalScrollPosition() {
        return 0;
    }

    /**
     * Get the current vertical scroll proportion
     *
     * @return ing The vertical scroll proportion on a scale of 0-100
     */
    public int getVerticalScrollProportion() {
        // SYNC NOTE: return of atomic value
        return 100;
    }

    /**
     * Remove this displayable from physical screen.
     * This function simply calls lCallHide() after obtaining LCDUILock.
     */
    public void uCallHide() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "# in DisplayableLFImpl: uCallHide");        
        }

        synchronized (Display.LCDUILock) {
            lCallHide();
        }
    }

    /**
     * Remove this displayable from physical screen.
     * The displayable should unload any resource that was allocated. It's not
     * required to clean the physical screen before this function returns.
     * This function could be called while a LF is in "freeze" mode.<p>
     * 
     * This function simply sets this DisplayableLF to HIDDEN state.
     */
    void lCallHide() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "# in DisplayableLFImpl: lCallHide");        
        }

        state = HIDDEN;
    }

    /**
     * Stop any further updates to physical screen because some
     * "system modal dialog" takes over physical screen buffer
     * and user input now or foreground is lost.
     * This function simply calls lCallFreeze after obtaining LCDUILock.
     */
    public void uCallFreeze() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "# in DisplayableLFImpl: uCallFreeze");        
        }

        synchronized (Display.LCDUILock) {
            lCallFreeze();
        }
    }

    /**
     * While UI resources of this LF are created and visible already, stop any
     * further updates to physical screen because some "system modal dialog"
     * takes over physical screen buffer and user input now or
     * foreground is lost.
     * Repaint and invalidate requests from this DisplayableLF will be really 
     * scheduled into event queue. Instead, only dirty flag is set.
     * After a LF enters "freeze" mode, it can be resumed of visibility or 
     * directly replaced by a new Displayable.
     *
     * This function simply sets this DisplayableLF to HIDDEN state.
     */
    void lCallFreeze() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "# in DisplayableLFImpl: lCallFreeze");        
        }
        state = FROZEN;
    }

    /**
     * Called by the event handler to perform an
     * invalidation of this Displayable
     */
    public void uCallInvalidate() {
        
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "# in DisplayableLFImpl: uCallInvalidate");        
        }

        // SYNC NOTE: It is safe to obtain a lock here because
        // the only subclass that can call into the app code in layout()
        // is FormLFImpl and it overrides uCallInvalidate()

        synchronized (Display.LCDUILock) {
            pendingInvalidate = false;

            layout();
        }
    }

    /**
     * This method is used int repaint, int order to determine the translation
     * of the draw coordinates.
     * @return true if the scroll responsibility is of the native platform.
     * false - if the scroll is done in the Java level.     
     */
    public boolean uIsScrollNative() {
        // only native form overrides this and returns true
        return false;
    }

    // ************************************************************
    //  package private methods
    // ************************************************************

    /**
     * Package private equivalent of sizeChanged()
     *
     * @param w the new width
     * @param h the new height
     *
     */
    public void uCallSizeChanged(int w, int h) {        
        
        boolean copyDefferedSizeChange;

        synchronized (Display.LCDUILock) {
            if (owner instanceof GameCanvas) {
                GameCanvasLFImpl gameCanvasLF =
                    GameMap.getGameCanvasImpl((GameCanvas)owner);
                if (gameCanvasLF != null) {
                    gameCanvasLF.lCallSizeChanged(w, h);
                }
            }
  
            // If there is no Display, or if this Displayable is not
            // currently visible, we simply record the fact that the
            // size has changed
            defferedSizeChange = (state != SHOWN);
            copyDefferedSizeChange = defferedSizeChange;

            /*
            * sizeChangeOccurred is a boolean which (when true) indicates
            * that sizeChanged() will be called at a later time. So, if it
            * is false after calling super(), we go ahead and notify the
            * Canvas now, rather than later
            */

            resetViewport();

            if (!defferedSizeChange) {
                lRequestInvalidate();
            }

        }
        if (!copyDefferedSizeChange) {
            synchronized (Display.calloutLock) {
                try {
                    owner.sizeChanged(w, h);
                } catch (Throwable t) {
                    Display.handleThrowable(t);
                }
            }
        }

    }

    /**
     * This method notify displayable to scroll its content 
     *
     * @param scrollType scrollType
     * @param thumbPosition
     */
    public void uCallScrollContent(int scrollType, int thumbPosition) {
        // by default nothing to do 
    }
    

    /**
     * Display calls this method on it's current Displayable.
     * This function simply calls lCallPaint() after obtaining LCDUILock.
     *
     * @param g the graphics context to paint into.
     * @param target the target Object of this repaint
     */
    public void uCallPaint(Graphics g, Object target) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "# in DisplayableLFImpl: uCallPaint");        
        }

        synchronized (Display.LCDUILock) {
            lCallPaint(g, target);
        }
    }

    /**
     * Display calls this method on it's current Displayable.
     * Displayable uses this opportunity to do necessary stuff
     * on the Graphics context, this includes,
     * paint Ticker, paint Title, translate as necessary.
     *
     * <p>The target Object of this repaint may be some Object
     * initially set by this Displayable when the repaint was
     * requested - allowing this Displayable to know exactly
     * which Object it needs to call to service this repaint,
     * rather than potentially querying all of its Objects to
     * determine the one(s) which need painting.
     *
     * SYNC NOTE: The caller of this method handles synchronization.
     *
     * @param g the graphics context to paint into.
     * @param target the target Object of this repaint
     */
    void lCallPaint(Graphics g, Object target) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "# in DisplayableLFImpl: lCallPaint");        
        }
    }

    /**
     * Handle a raw key event from Display.
     * @param type - key event type.
     * @param keyCode - the key involved in this key event.
     */
    public void uCallKeyEvent(int type, int keyCode) {
        int eventType = -1;

        synchronized (Display.LCDUILock) {

            switch (type) {
                case EventConstants.PRESSED:
                    sawKeyPress = true;
                    eventType = 0;
                    break;
                case EventConstants.RELEASED:
                    if (sawKeyPress) {
                        eventType = 1;
                    }
                    break;
                case EventConstants.REPEATED:
                    if (sawKeyPress) {
                        eventType = 2;
                    }
                    break;
                default:
                    // wrong key type will be handled below
                    break;
            }
            // used later by getKeyMask()
            if (currentKeyMask > -1 && eventType != -1) {
                if (eventType == 1) {
                    releaseKeyMask(keyCode);
                } else {
                    // set the mask on key press, repeat or type.
                    // don't set the mask when a key was released.
                    setKeyMask(keyCode);
                }
            }
        } // synchronized

        // SYNC NOTE: Since we may call into application code,
        // we do so outside of LCDUILock
        switch (eventType) {
        case 0:
            uCallKeyPressed(keyCode);
            break;
        case 1:
            uCallKeyReleased(keyCode);
            break;
        case 2:
            uCallKeyRepeated(keyCode);
            break;
        default:
            /*
             * TBD:
             *
             * Originally severity level was "ERROR". 
             * But it was reduced to INFO because 
             * a). it do not harm to the system
             * b). some cases, 
             *     Displayable processes KEY_PRESS events
             *     (when in system menu) & cleans all related status flag, 
             *     while following KEY_REPEAT & KEY_RELEASE event pairs
             *     are not processed in the same way and therefore
             *     this eror messae was printed or them.
             *
             * As a temporary solution it was decided to disable messages 
             * insead of additional event filtering.
             */
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                               "DisplayableLFImpl: uCallKeyEvent," +
                               "type=" +type+ " keyCode=" +keyCode);
            }
            break;
        }
    } // end of dsKeyEvent()

    /**
     * Handle a key press
     *
     * @param keyCode The key that was pressed
     */
    void uCallKeyPressed(int keyCode) { }
    /**
     * Handle a repeated key press
     *
     * @param keyCode The key that was pressed
     */
    void uCallKeyRepeated(int keyCode) { }
    /**
     * Handle a key release
     *
     * @param keyCode The key that was released
     */
    void uCallKeyReleased(int keyCode) { }

    /**
     * Called from the event delivery loop when a pointer event is seen.
     * @param type kind of pointer event
     * @param x x-coordinate of pointer event
     * @param y y-coordinate of pointer event
     */
    public void uCallPointerEvent(int type, int x, int y) {
        int eventType = -1;

        synchronized (Display.LCDUILock) {
            switch (type) {
                case EventConstants.PRESSED:
                    sawPointerPress = true;
                    eventType = 0;
                    break;
                case EventConstants.RELEASED:
                    if (sawPointerPress) {
                        eventType = 1;
                    }
                    break;
                case EventConstants.DRAGGED:
                    if (sawPointerPress) {
                        eventType = 2;
                    }
                    break;
                default:
                    // will be handled below
                    break;
            }
        } // synchronized

        // SYNC NOTE: Since we may call into application code,
        // we do so outside of LCDUILock
        switch (eventType) {
        case 0:
            uCallPointerPressed(x, y);
            break;
        case 1:
            uCallPointerReleased(x, y);
            break;
        case 2:
            uCallPointerDragged(x, y);
            break;
        default:
            if (sawPointerPress) {
                Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                               "DisplayableLFImpl: uCallPointerEvent," +
                               " type=" +type+ " x=" +x+ " y=" +y);
            }
            break;
        }
    } // uCallPointerEvent()

    /**
     * Handle a pointer press event
     *
     * @param x The x coordinate of the press
     * @param y The y coordinate of the press
     */
    void uCallPointerPressed(int x, int y) { }
    /**
     * Handle a pointer drag event
     *
     * @param x The x coordinate of the drag
     * @param y The y coordinate of the drag
     */
    void uCallPointerDragged(int x, int y) { }
    /**
     * Handle a pointer release event
     *
     * @param x The x coordinate of the release
     * @param y The y coordinate of the release
     */
    void uCallPointerReleased(int x, int y) { }
        

    /**
     * Called to commit any pending user interaction for the current item.
     */
    public void lCommitPendingInteraction() { }


    /**
     * Set status of screen rotation
     * @param newStatus
     * @return
     */
    public boolean uSetRotatedStatus(boolean newStatus) {
        synchronized (Display.LCDUILock) {
            if (newStatus == owner.isRotated) {
                return false;
            } else {
                owner.isRotated = newStatus;
                return true;
            }
        }
    }


    /**
     * Perform any necessary layout, and update the viewport
     * as necessary
     */
    void layout() {
        resetViewport();
    }
    
    /**
     * Request to paint this Displayable (without holding a lock).
     *
     * @param x The x coordinate of the region to repaint
     * @param y The y coordinate of the region to repaint
     * @param width The width of the region to repaint
     * @param height The height of the region to repaint
     */
    void uRequestPaint(int x, int y, int width, int height) {
        synchronized (Display.LCDUILock) {
            lRequestPaint(x, y, width, height);
        }
    }
    
    /**
     * Request to paint this Displayable.
     *
     * @param x The x coordinate of the region to repaint
     * @param y The y coordinate of the region to repaint
     * @param width The width of the region to repaint
     * @param height The height of the region to repaint
     */
    void lRequestPaint(int x, int y, int width, int height) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "# in DisplayableLFImpl: lRequestPaint");        
        }

        // Note: Display will not let anyone but the current
        // Displayable schedule repaints
        if (lIsShown()) {
            currentDisplay.repaintImpl(this, 
                                       x, y, width, height,
                                       null);
        }
    }

    /**
     * Request to paint all of this Displayable (without holding a lock).
     */    
    void uRequestPaint() {
        synchronized (Display.LCDUILock) {
            lRequestPaint();
        }
    }

    /**
     * Request to paint all of this Displayable.
     * This is the same as calling 
     * lRequestPaint(0, 0, 
     *     viewport[WIDTH],
     *     viewport[HEIGHT], null)
     */
    void lRequestPaint() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "# in DisplayableLFImpl: lRequestPaint");        
        }

        lRequestPaint(0, 0, viewport[WIDTH], viewport[HEIGHT]);
    }


    /**
     * IMPL_NOTE: Invalidate requests are served with a limited rate to
     *   not exceed 25 fps, i.e. 40 ms delay is enabled between sequential
     *   requests. More frequent requests are ignored, the invalidate
     *   request timer guarantees they will be processed in a predefined
     *   time frame.
     */

    /** Last time the invalidate request was accepted */
    private long lastTimeInvalidate = 0;

    /** Timer to schedule delayed invalidate task */
    private LFTimer invalidateTimer = new LFTimer(Display.LCDUILock) {
        protected void perform() {
            lastTimeInvalidate = System.currentTimeMillis();
            lRequestInvalidateImpl();
        }
    };

    /** The time in milliseconds between sequential invalidate requests */
    private final int INVALIDATE_REQUESTS_PERIOD = 40; // 40 ms is 25 fps

    /** Grace period to process last unserved invalidate request */
    private final int INVALIDATE_REQUESTS_GRACE = 80; // ms

    /**
     * Called to schedule an "invalidate" for this Displayable.
     * The method recalls internal implementation of invalidate
     * request limiting the rate of requests to be not bigger than
     * a predefined constant.
     *
     * SYNC NOTE: Caller should hold LCDUILock.
     */
    void lRequestInvalidate() {
        long timePassed = System.currentTimeMillis() - lastTimeInvalidate;
        if (timePassed >= INVALIDATE_REQUESTS_PERIOD) {
            invalidateTimer.cancel();
            lRequestInvalidateImpl();
            lastTimeInvalidate += timePassed;

        } else {
            // Postpone too frequent invalidate requests.
            invalidateTimer.schedule(
                INVALIDATE_REQUESTS_GRACE - timePassed);
        }
    }

    /**
     * Called to schedule an "invalidate" for this Displayable. Invalidation
     * is caused by things like size changes, content changes, or spontaneous
     * traversal within the Displayable.
     *
     * SYNC NOTE: Caller should hold LCDUILock.
     */
    void lRequestInvalidateImpl() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "# in DisplayableLFImpl: invalidate");
        }

        pendingInvalidate = true;

        if (state == SHOWN && currentDisplay != null) {
            currentDisplay.invalidate();
            invalidScroll = true;
        }
    }

    // ************************************************************
    //  private methods
    // ************************************************************

    
    /**
     * By default, the viewport array is configured to be
     * at origin 0,0 with width and height set depending
     * on the full screen mode setting.
     */
    private void resetViewport() {
        // setup the default viewport, the size of the Display
        if (viewport == null) {
            viewport = new int[4];
        }

        viewport[WIDTH] = getDisplayableWidth();
        viewport[HEIGHT] = getDisplayableHeight();
    }

    /**
     * Calculate the height a displayable would occupy if it was to
     * be displayed.
     *
     * @return the height a displayable would occupy 
     */
    public int getDisplayableHeight() {
        return (currentDisplay != null ?
		currentDisplay.getDisplayableHeight() :
		Display.getDefaultDisplayableHeight(owner.isInFullScreenMode, 
						    owner.getTitle() != null, 
						    owner.getTicker() != null, 
						    owner.numCommands > 0));              
    }

    /**
     * Calculate the width a displayable would occupy if it was to
     * be displayed
     *
     * @return the width a displayable would occupy 
     */
    public int getDisplayableWidth() {
	return (currentDisplay != null ?
		currentDisplay.getDisplayableWidth() :
		Display.getDefaultDisplayableWidth(owner.isInFullScreenMode, 
						   getVerticalScrollProportion() != 100)); 
    }
    

    
    /**
     * IMPL_NOTE: Move this to CanvasLFImpl.
     * Called to set key mask of all the keys that were pressed.
     * @param keyCode The key code to set the key mask.
     */
    private void setKeyMask(int keyCode) {

        // set the mask of keys pressed 
        switch (KeyConverter.getGameAction(keyCode)) {
        case Canvas.UP:
            stickyKeyMask = stickyKeyMask | GameCanvas.UP_PRESSED;
            currentKeyMask = currentKeyMask | GameCanvas.UP_PRESSED;
            break;
        case Canvas.DOWN:
            stickyKeyMask = stickyKeyMask | GameCanvas.DOWN_PRESSED;
            currentKeyMask = currentKeyMask | GameCanvas.DOWN_PRESSED;
            break;
        case Canvas.LEFT:
            stickyKeyMask = stickyKeyMask | GameCanvas.LEFT_PRESSED;
            currentKeyMask = currentKeyMask | GameCanvas.LEFT_PRESSED;
            break;
        case Canvas.RIGHT:
            stickyKeyMask = stickyKeyMask | GameCanvas.RIGHT_PRESSED;
            currentKeyMask = currentKeyMask | GameCanvas.RIGHT_PRESSED;
            break;
        case Canvas.FIRE:
            stickyKeyMask = stickyKeyMask | GameCanvas.FIRE_PRESSED;
            currentKeyMask = currentKeyMask | GameCanvas.FIRE_PRESSED;
            break;
        case Canvas.GAME_A:
            stickyKeyMask = stickyKeyMask | GameCanvas.GAME_A_PRESSED;
            currentKeyMask = currentKeyMask | GameCanvas.GAME_A_PRESSED;
            break;
        case Canvas.GAME_B:
            stickyKeyMask = stickyKeyMask | GameCanvas.GAME_B_PRESSED;
            currentKeyMask = currentKeyMask | GameCanvas.GAME_B_PRESSED;
            break;
        case Canvas.GAME_C:
            stickyKeyMask = stickyKeyMask | GameCanvas.GAME_C_PRESSED;
            currentKeyMask = currentKeyMask | GameCanvas.GAME_C_PRESSED;
            break;
        case Canvas.GAME_D:
            stickyKeyMask = stickyKeyMask | GameCanvas.GAME_D_PRESSED;
            currentKeyMask = currentKeyMask | GameCanvas.GAME_D_PRESSED;
            break;
        default:
            // no mask should be set
            break;
        }
    }

    /**
     * IMPL_NOTE: Move this to CanvasLFImpl.
     * Called to release key mask of all the keys that were release.
     * @param keyCode The key code to release the key mask.
     */
    private void releaseKeyMask(int keyCode) {

        // set the mask of keys pressed 
        switch (KeyConverter.getGameAction(keyCode)) {
        case Canvas.UP:
            currentKeyMask = currentKeyMask & ~ GameCanvas.UP_PRESSED;
            break;
        case Canvas.DOWN:
            currentKeyMask = currentKeyMask & ~ GameCanvas.DOWN_PRESSED;
            break;
        case Canvas.LEFT:
            currentKeyMask = currentKeyMask & ~ GameCanvas.LEFT_PRESSED;
            break;
        case Canvas.RIGHT:
            currentKeyMask = currentKeyMask & ~ GameCanvas.RIGHT_PRESSED;
            break;
        case Canvas.FIRE:
            currentKeyMask = currentKeyMask & ~ GameCanvas.FIRE_PRESSED;
            break;
        case Canvas.GAME_A:
            currentKeyMask = currentKeyMask & ~ GameCanvas.GAME_A_PRESSED;
            break;
        case Canvas.GAME_B:
            currentKeyMask = currentKeyMask & ~ GameCanvas.GAME_B_PRESSED;
            break;
        case Canvas.GAME_C:
            currentKeyMask = currentKeyMask & ~ GameCanvas.GAME_C_PRESSED;
            break;
        case Canvas.GAME_D:
            currentKeyMask = currentKeyMask & ~ GameCanvas.GAME_D_PRESSED;
            break;
        default:
            // no key mask should be set
            break;
        }
    }

    // ************************************************************
    //  public member variables - NOT ALLOWED in this class
    // ************************************************************
    
    // ************************************************************
    //  protected member variables - NOT ALLOWED in this class
    // ************************************************************
    
    // ************************************************************
    //  package private member variables
    // ************************************************************
    
    /** The current Display object */
    Display currentDisplay;

    /**
     * The viewport coordinates.
     * Index 0: x origin coordinate (in the Display's coordinate space) 
     * Index 1: y origin coordinate (in the DIsplay's coordinate space) 
     * Index 2: width 
     * Index 3: height 
     */
    int[] viewport;

    /**
     * Signals that scroll has changed, and needs resetting.
     * It is used by Form (or Alert) to reset the scroll.
     * It should be false before paint can be performed.
     * It is set to true if one of these conditions occurs:
     * - viewport height has changed
     * - viewport y has changed
     * - traverse ot of the screen occurred
     * - layout() occurred
     *
     * It is set back to false by the Form (or Alert)
     */
    boolean invalidScroll = true;
    
    /**
     * True, indicates that before being painted, this Displayable should
     * be notified that its size has changed via uCallSizeChanged()
     */
    boolean defferedSizeChange = true;
    
    /**
     * The owner of this view.
     */
    Displayable owner;

    /** current state of DisplayableLF (HIDDEN, SHOWN, or FROZEN) */
    int state; // = HIDDEN (0)
    
    // ************************************************************
    //  private member variables
    // ************************************************************
    
    // No events will be delivered while these are false
    // This is our attempt at avoiding spurious up events
    /** true, if a pointer press is in progress. */
    boolean sawPointerPress;

    /** true, if a key press is in progress. */
    boolean sawKeyPress;

    /** stores key code of the current key pressed at least once */
    // caters to the GameCanvas.getKeyStats()
    // latching behavior. This latched state is cleared
    // when the getKeyStats() is called.
    private int stickyKeyMask;
    
    /** stores key code of the current key is currently down */
    // sets the key to 1 when the key
    // is currently down
    private int currentKeyMask;

    /**
     * Used to indicate the invalidate is needed
     */
    boolean pendingInvalidate;
   
    // ************************************************************
    //  Static initializer, constructor
    // ************************************************************
    /** Used as an index into the viewport[], for the x */
    final static int X      = 0;

    /** Used as an index into the viewport[], for the y */
    final static int Y      = 1;

    /** Used as an index into the viewport[], for the width */
    final static int WIDTH  = 2;
    
    /** Used as an index into the viewport[], for the height */
    final static int HEIGHT = 3;



    /** hidden state of DisplayableLF */
    final static int HIDDEN = 0;

    /** shown state of DisplayableLF */
    final static int SHOWN  = 1;

    /** frozen state of DisplayableLF */
    final static int FROZEN = 2;

} // DisplayableLFImpl
