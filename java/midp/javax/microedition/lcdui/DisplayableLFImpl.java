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

import javax.microedition.lcdui.game.GameCanvas;
import com.sun.midp.lcdui.EventConstants;
import com.sun.midp.lcdui.GameMap;
import com.sun.midp.lcdui.GameCanvasLFImpl;
import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;
import com.sun.midp.configurator.Constants;

/**
 * The look and feel implementation of <code>Displayable</code> based 
 * on platform widget.
 */
abstract class DisplayableLFImpl implements DisplayableLF {

    /** Static initializer. */
    static {
        initialize0();
    }

    /**
     * Native class initializer.
     */
    private static native void initialize0();

    /**
     * Creates <code>DisplayableLF</code> for the passed in 
     * <code>Displayable</code>.
     *
     * @param d the <code>Displayable</code> object associated with this 
     *          look &amp; feel.
     */
    DisplayableLFImpl(Displayable d) {
        owner = d;
        width  = Display.WIDTH;
        height = Display.HEIGHT;
    }
    
    /**
     * Native finalizer to delete native resources.
     */
    private native void finalize();

    // ************************************************************
    //  public methods - DisplayableLF interface implementation
    // ************************************************************
    
    /**
     * Implement public API isShown().
     *
     * @return true if current <code>DisplayableLF</code> is interactive 
     * with user.
     */
    public boolean lIsShown() {
        return (currentDisplay != null) && currentDisplay.isShown(this);
    }

    /**
     * Get the width in pixels this <code>Displayable</code> is using.
     *
     * @return width of the area available to the application
     */
    public int lGetWidth() {
        return width;
    }
    
    /**
     * Get the height in pixels this <code>Displayable</code> is using.
     *
     * @return height of the area available to the application
     */
    public int lGetHeight() {
        return height;
    }
    
    /**
     * Notifies <code>Displayable</code>'s look &amp; feel object of 
     * a title change.
     *
     * SYNC NOTE: The caller of this method handles synchronization.
     *
     * @param oldTitle the old title, or <code>null</code> for no title
     * @param newTitle the new title, or <code>null</code> for no title
     */
    public void lSetTitle(String oldTitle, String newTitle) {
        // No updates are necessary if we are in a full screen mode
        if (owner.isInFullScreenMode) {
            return;
        }
        // No update needed if title string is the same object
        if (oldTitle == newTitle) {
            return;
        }
        // No update needed if title strings have same content
        if (oldTitle != null && 
            newTitle != null && 
            oldTitle.equals(newTitle)) {
            return;
        }
        // Update only if we have native resource created
        if (nativeId != INVALID_NATIVE_ID) {
            setTitle0(nativeId, newTitle);
        }
    }

    /**
     * Notifies <code>Displayable</code>'s look &amp; feel object of 
     * a ticker change.
     *
     * SYNC NOTE: The caller of this method handles synchronization.
     *
     * @param oldTicker the old ticker, or <code>null</code> for no ticker
     * @param newTicker the new ticker, or <code>null</code> for no ticker
     */
    public void lSetTicker(Ticker oldTicker, Ticker newTicker) {

        // This method will not be called if oldTicker and 
        // newTicker are the same (that includes both being null)

        if (owner.ticker != null) {
            owner.ticker.tickerLF.lSetOwner(this);
        }

        updateNativeTicker(oldTicker, newTicker);
    }

    /**
     * Notifies look &amp; feel object of a command addition
     * to the <code>Displayable</code>.
     * 
     * SYNC NOTE: The caller of this method handles synchronization.
     *
     * @param cmd the command that was added
     * @param i the index of the added command in 
     *          <code>Displayable.commands[]</code> array
     */
    public void lAddCommand(Command cmd, int i) {
        updateCommandSet();
    }

    /**
     * Notifies look &amps; feel object of a command removal 
     * from the <code>Displayable</code>.
     *
     * SYNC NOTE: The caller of this method handles synchronization.
     * 
     * @param cmd the command that was removed
     * @param i the index of the removed command in 
     *          <code>Displayable.commands[]</code> array
     */
    public void lRemoveCommand(Command cmd, int i) {
        updateCommandSet();
    }

    /**
     * Updates command set if this <code>Displayable</code> is visible.
     *
     * SYNC NOTE: Caller must hold LCDUILock around this call.
     */
    public void updateCommandSet() {
        if (state == SHOWN && currentDisplay != null) {
            currentDisplay.updateCommandSet();
        }
    }

    /**
     * Return the <code>Display</code> instance in which the LF is 
     * currently shown.
     *
     * @return the <code>Display</code> instance in which the LF is shown. 
     *         <code>Null</code> if not shown.
     */
    public Display lGetCurrentDisplay() {
        return currentDisplay;
    }

    /**
     * Called to get the key mask of all the keys that were pressed.
     * Implement an interface function for <code>CanvasLF</code> only.
     *
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
     * Set the display instance the <code>Displayable</code> is associated 
     * with.
     * Caller should hold LCDUILock around this call.
     *
     * @param d <code>Display</code> instance in which this 
     *          <code>DisplayableLF</code> is visible.
     *                <code>null</code> if this <code>DisplayableLF</code> is 
     *           no longer visible.
     */
    public void lSetDisplay(Display d) {
        // ASSERT(d == null || currentDisplay == null)
        currentDisplay = d;
    }

    /**
     * Return the associated Displayable object.
     *
     * SYNC NOTE: Since the <code>Displayable</code> and 
     * <code>DisplayableLFImpl</code> has 1-to-1 mapping, this function
     * can be called from in or outside of LCDUILock.
     *
     * @return the public model object this LF is associated with.
     */
    public Displayable lGetDisplayable() {
        return owner;
    }

    /**
     * Notifies look &amp; feel object of a full screen mode change.
     *
     * @param mode <code>true</code>, if canvas should be displayed 
     *             without title, ticker, etc.; <code>false</code> otherwise 
     */
    public void uSetFullScreenMode(boolean mode) {

        boolean requestRepaint = false;

        synchronized (Display.LCDUILock) {

            if (lIsShown()) {
                // currentDisplay is not null when lIsShown is true
                currentDisplay.lSetFullScreen(mode);
                if (mode) {
                    setTicker(null);
                } else if (owner.ticker != null) {
                    setTicker(owner.ticker);
                }
                updateCommandSet();
                requestRepaint = true;
            }
        }
        if (currentDisplay != null) {
            // This may call into app code, so do it outside LCDUILock
            uCallSizeChanged(currentDisplay.width, currentDisplay.height);
        } else {
            uCallSizeChanged(Display.WIDTH, Display.HEIGHT);
        }
        // app's sizeChanged has to be called before repaint
        synchronized (Display.LCDUILock) {
            if (requestRepaint) {
                lRequestPaint();
            }
        }
    }


    /**
     * Prepare to show this LF on physical screen.
     * This function will set correct screen mode screen mode 
     * then call lCallShow.
     */
    public void uCallShow() {

        boolean copyDefferedSizeChange;

        synchronized (Display.LCDUILock) {
            // Assure correct screen mode
            currentDisplay.lSetFullScreen(owner.isInFullScreenMode);
            // display dimentions may change as the resulr of lSetFullScreen
            width = currentDisplay.width;
            height = currentDisplay.height;
            if (owner.isInFullScreenMode) {
                setTicker(null);
            } else if (owner.ticker != null) {
                setTicker(owner.ticker);
            }
            copyDefferedSizeChange = defferedSizeChange;
            defferedSizeChange = false;
        }

        if (copyDefferedSizeChange) {
            synchronized (Display.calloutLock) { 
                try { 
                    owner.sizeChanged(width, height); 
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

        // This will suppress drags, repeats and ups until a
        // corresponding down is seen.
        sawPointerPress = sawKeyPress = false;
        
        if (state != SHOWN) {
            // Create native resource first
            // since the title and ticker may depend on it
            createNativeResource();
        }

        // Start to paint the ticker
        updateNativeTicker(null, owner.ticker);

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

        state = SHOWN;
    } // lCallShow()

    /**
     * Get the current vertical scroll position.
     *
     * @return int The vertical scroll position on a scale of 0-100
     */
    public int getVerticalScrollPosition() {
        // SYNC NOTE: return of atomic value
        return 0;
    }

    /**
     * Get the current vertical scroll proportion.
     *
     * @return ing The vertical scroll proportion on a scale of 0-100
     */
    public int getVerticalScrollProportion() {
        // SYNC NOTE: return of atomic value
        return 100;
    }

    /**
     * Remove this <code>Displayable</code> from physical screen.
     * This function calls lCallHide after holding LCDUILock
     * and sets this DisplayableLF to HIDDEN state.
     */
    public void uCallHide() {
        synchronized (Display.LCDUILock) {
            // Delete native resources and update ticker
            lCallHide();
            // set state
            state = HIDDEN;
        }
    }

    /**
     * Some "system modal dialog" takes over physical screen 
     * buffer and user input now or foreground is lost.
     * This function calls lCallHide after holding LCDUILock
     * and sets this DisplayableLF to FROZEN state.
     */
    public void uCallFreeze() {
        synchronized (Display.LCDUILock) {
            // Delete native resources and update ticker
            lCallHide();
            // set state
            state = FROZEN;
        }
    }

    /**
     * Remove this <code>Displayable</code> from physical screen.
     * The <code>Displayable</code> should unload any resource that 
     * was allocated. It is not required to clean the physical screen 
     * before this function returns.
     */
    void lCallHide() {
        if (state == SHOWN) {
            updateNativeTicker(owner.ticker, null);
        }

        // Delete native resources
        deleteNativeResource();
        
    }

    /**
     * Called by the event handler to perform an invalidation of this 
     * <code>Displayable</code>.
     * Subclass should override to perform re-layout.
     * Default implementation does nothing.
     */
    public void uCallInvalidate() {
        synchronized (Display.LCDUILock) {
            pendingInvalidate = false;
        }
    }

    /**
     * This method is used in repaint, in order to determine the translation
     * of the draw coordinates.
     *
     * @return <code>true</code>, if the scroll responsibility is on 
     *          the native platform.
     *         <code>false</code>, if the scroll is done at Java level.
     */
    public boolean uIsScrollNative() {
        // only native form overrides this and returns true
        return false;
    }
    
    // ************************************************************
    //  package private methods
    // ************************************************************

    /**
     * Create native resource.
     * Instance variable {@link #nativeId nativeId} must be set
     * to the id of the new resource.
     */
    abstract void createNativeResource();

    /**
     * Delete native resource.
     * Instance variable {@link #nativeId nativeId} is reset
     * to {@link #INVALID_NATIVE_ID INVALID_NATIVE_ID}.
     */
    void deleteNativeResource() {
        if (nativeId != INVALID_NATIVE_ID) {
            deleteNativeResource0(nativeId);
            nativeId = INVALID_NATIVE_ID;
        }
    }

    /**
     * Package private equivalent of sizeChanged().
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

            width = w;
            height = h;
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
     * <code>Display</code> calls this method on it's current 
     * <code>Displayable</code>.
     * <code>Displayable</code> uses this opportunity to do necessary stuff
     * on the graphics context, this includes, paint Ticker, paint Title 
     * and translate as necessary.
     *
     * <p>The target Object of this repaint may be some Object
     * initially set by this <code>Displayable</code> when the repaint was
     * requested - allowing this <code>Displayable</code> to know exactly
     * which Object it needs to call to service this repaint,
     * rather than potentially querying all of its Objects to
     * determine the one(s) which need painting.
     *
     * SYNC NOTE: The caller of this method handles synchronization.
     *
     * @param g the graphics context to paint into.
     * @param target the target Object of this repaint
     */
    public void uCallPaint(Graphics g, Object target) {
        // Made obsolete by dsShow, where native title is shown already
    }
    
    /**
     * Handle a raw key event from <code>Display</code>.
     *
     * @param type type of event, defined in <code>EventConstants</code>
     * @param keyCode code of the key event
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
        case -1:
            return;
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
     * Handle a key press.
     *
     * @param keyCode The key that was pressed
     */
    void uCallKeyPressed(int keyCode) { }

    /**
     * Handle a repeated key press.
     *
     * @param keyCode The key that was pressed
     */
    void uCallKeyRepeated(int keyCode) { }

    /**
     * Handle a key release.
     *
     * @param keyCode The key that was released
     */
    void uCallKeyReleased(int keyCode) { }

    /**
     * Called from the event delivery loop when a pointer event is seen.
     *
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
            }
        } // synchronized

        // SYNC NOTE: Since we may call into application code,
        // we do so outside of LCDUILock
        switch (eventType) {
        case -1:
            return;
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
            // this is an error
            break;
        }
    } // uCallPointerEvent()

    /**
     * Handle a pointer press event.
     *
     * @param x The x coordinate of the press
     * @param y The y coordinate of the press
     */
    void uCallPointerPressed(int x, int y) { }

    /**
     * Handle a pointer drag event.
     *
     * @param x The x coordinate of the drag
     * @param y The y coordinate of the drag
     */
    void uCallPointerDragged(int x, int y) { }

    /**
     * Handle a pointer release event.
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
     * Repaint this <code>Displayable</code>.
     *
     * @param x The x coordinate of the region to repaint
     * @param y The y coordinate of the region to repaint
     * @param width The width of the region to repaint
     * @param height The height of the region to repaint
     * @param target an optional paint target to receive the paint request
     *               when it returns via uCallPaint()
     */
    void lRequestPaint(int x, int y, int width, int height, Object target) {
        if (lIsShown()) {
            // Note: Display will not let anyone but the current
            // Displayable schedule repaints
            currentDisplay.repaintImpl(this, 
                                       x, y, width, height,
                                       target);
        }
    }
    
    /**
     * Repaints this <code>Displayable</code>. 
     * This is the same as calling 
     * repaint(0, 0, width, height, null)
     */
    void lRequestPaint() {
        lRequestPaint(0, 0, width, height, null);
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
     * Repaint the whole <code>Displayable</code>.
     */
    void lRequestPaintContents() {
        lRequestPaint(0, 0, width, height, null);
    }
    
    /**
     * Called to schedule an "invalidate" for this <code>Displayable</code>. 
     * Invalidation is caused by things like size changes, content changes, 
     * or spontaneous traversal within the <code>Item</code>.
     *
     * SYNC NOTE: Caller must hold LCDUILock around this call.
     */
    void lRequestInvalidate() {
        pendingInvalidate = true;
        if (state == SHOWN && currentDisplay != null) {
            currentDisplay.invalidate();
        }
    }

    // ************************************************************
    //  private methods
    // ************************************************************

    /**
     * Updates the ticker.
     *
     * @param oldTicker the old ticker, or <code>null</code> for no ticker
     * @param newTicker the new ticker, or <code>null</code> for no ticker
     */
    private void updateNativeTicker(Ticker oldTicker, Ticker newTicker) {

        // CASES:
        // 1. Had an invisible non-null ticker, setting a null ticker
        //    - We need to set the new ticker. There's no need to re-layout
        //      or start the new ticker
        // 2. Had an invisible non-null ticker, setting a non-null ticker
        //    - We need to set the new ticker. There's no need to re-layout
        //      or start the new ticker
        // 3. Had a visible non-null ticker, setting a null ticker
        //    - We need to set the new ticker and re-layout. There's no
        //      need to start the new ticker.
        // 4. Had a null ticker, setting a visible non-null ticker
        //    - We need to set the new ticker, re-layout, and
        //      start up the new ticker
        // 5. Had a visible non-null ticker, setting a non-null ticker
        //    - We need to set the new ticker. There's no need to re-layout

        if ((owner.isInFullScreenMode) || 
            ((oldTicker == null) && (newTicker == null))) {
            return;
        } else {
            setTicker(newTicker);
        }
    }

    /**
     * Set the ticker.
     *
     * @param t the new ticker to be set
     */
    private void setTicker(Ticker t) {

        if (nativeId != INVALID_NATIVE_ID) {
            setTicker0(nativeId, (t == null) ? null : t.displayedMessage);
        }
    }

    /**
     * Notification that the ticker has changed.
     * This method is called from <code>TickerLFImpl</code>.
     *
     * @param t the ticker associated with the TickerLFImpl
     */
    void tickerTextChanged(Ticker t) {
        if (owner.ticker != t) {
            return;
        }
        setTicker(t);
    }

    /**
     * Called to set key mask of all the keys that were pressed.
     *
     * @param keyCode The key code to set the key mask.
     */
    private void setKeyMask(int keyCode) {
        /*
        // Shouldn't run into this case.
        if (paintSuspended || !hasForeground) {
            return;
        }
        */

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
        }
    }

    /**
     * Called to release key mask of all the keys that were release.
     *
     * @param keyCode The key code to release the key mask.
     */
    private void releaseKeyMask(int keyCode) {
        /*
        // Leave this case to dsHide and dsFreeze()
        if (paintSuspended || !hasForeground) {
            currentKeyMask = 0;
            return;
        }
        */

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
    
    /** 
     * The current <code>Display</code> object.
     */
    Display currentDisplay;

    /**
     * Width and height available to the <code>Displayable</code>.
     */
    int width, height;

    
    /**
     * <code>True</code>, indicates that before being painted, this 
     * <code>Displayable</code> should be notified that its size has 
     * changed via uCallSizeChanged().
     */
    boolean defferedSizeChange = true;
    
    /**
     * The owner of this view.
     */
    Displayable owner;
 
    /**
     * The <code>MidpDisplayable*</code> of this <code>Displayable</code> 
     * while visible.
     * <code>INVALID_NATIVE_ID</code> if no native resource has been created.
     */
    int nativeId = INVALID_NATIVE_ID;

    /**
     * Version number of this LF's data model.
     * Should be updated when public data model has changed and
     * be passed to native peer if visible. Native peer tags
     * all its native events with this version to prevent user
     * actions on obsolete copy of the data in native.
     */
    int modelVersion; // = 0

    // ************************************************************
    //  private member variables
    // ************************************************************
        
    // No events will be delivered while these are false.
    // This is our attempt at avoiding spurious up events.

    /** 
     * <code>True</code>, if a pointer press is in progress. 
     */
    boolean sawPointerPress;

    /** 
     * <code>True</code>, if a key press is in progress. 
     */
    boolean sawKeyPress;

    /** current state of DisplayableLF (HIDDEN, SHOWN, or FROZEN) */
    int state; // = HIDDEN (0)

    /** 
     * Stores key code of the current key pressed at least once.
     */
    // caters to the GameCanvas.getKeyStats() latching behavior. 
    // This latched state is cleared when getKeyStats() is called.
    private int stickyKeyMask;
    
    /** 
     * Stores key code of the current key is currently down.
     */
    // sets the key to 1 when the key is currently down
    private int currentKeyMask;

    /**
     * Used to indicate the invalidate is needed 
     */
    boolean pendingInvalidate;


    // ************************************************************
    //  Static initializer, constructor
    // ************************************************************

    /** 
     * Used as an index into the viewport[], for the x origin.
     */
    final static int X      = 0;
        
    /** 
     * Used as an index into the viewport[], for the y origin.
     */
    final static int Y      = 1;
    
    /** 
     * Used as an index into the viewport[], for the width.
     */
    final static int WIDTH  = 2;
    
    /** 
     * Used as an index into the viewport[], for the height.
     */
    final static int HEIGHT = 3;

    /** 
     * Uninitialized native resource id value.
     */
    final static int INVALID_NATIVE_ID = 0;


    /** hidden state of DisplayableLF */
    final static int HIDDEN = 0;

    /** shown state of DisplayableLF */
    final static int SHOWN  = 1;

    /** frozen state of DisplayableLF */
    final static int FROZEN = 2;
    // ************************************************************
    //  Native methods
    // ***********************************************************

    /**
     * Free the native resource of this <code>Displayable</code> and hide 
     * it from display.
     * 
     * @param nativeId native resource id
     *
     * @exception OutOfMemoryException - if out of native resource
     */
    private native void deleteNativeResource0(int nativeId);

    /**
     * Change the title of native resource.
     *
     * @param nativeId native resource id (<code>MidpDisplayable *</code>)
     * @param title New title string. Can be <code>null</code>.
     */
    private static native void setTitle0(int nativeId, String title);

    /**
     * Set text of the native ticker.
     *
     * @param nativeId native resource id.
     * @param text text used. Null if the ticker should stop.
     */
    private static native void setTicker0(int nativeId, String text);

} // DisplayableLFImpl
