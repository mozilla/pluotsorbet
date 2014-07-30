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

/**
 * Look and Feel interface used by Displayable.
 * <p>
 * See <a href="doc-files/naming.html">Naming Conventions</a>
 * for information about method naming conventions.
 */
interface DisplayableLF {

/*
 * Interface to Displayable
 */
    /**
     * Implement the public API isShown().
     * @return true if the DisplayableLF is interactive with user.
     */
    boolean lIsShown();

    /**
     * Returns the width of the area available to the application.
     *
     * @return width of the area available to the application
     */
    int lGetWidth();
    
    /**
     * Returns the height of the area available to the application.
     *
     * @return height of the area available to the application
     */
    int lGetHeight();

    /**
     * Notifies Displayable's look & feel object of a title change.
     *
     * @param oldTitle the old title, or <code>null</code> for no title
     * @param newTitle the new title, or <code>null</code> for no title
     */
    void lSetTitle(String oldTitle, String newTitle);

    /**
     * Notifies Displayable's look & feel object of a ticker change.
     *
     * @param oldTicker the old ticker, or <code>null</code> for no ticker
     * @param newTicker the new ticker, or <code>null</code> for no ticker
     */
    void lSetTicker(Ticker oldTicker, Ticker newTicker);

    /**
     * Notifies look&feel object of a command addition 
     * to the <code>Displayable</code>.
     * 
     * @param cmd the command that was added
     * @param i the index of the added command in Displayable.commands[] 
     *        array
     */
    void lAddCommand(Command cmd, int i);

    /**
     * Notifies look&feel object of a command removal 
     * from the <code>Displayable</code>.
     * 
     * @param cmd the command that was removed
     * @param i the index of the removed command in Displayable.commands[] 
     *        array
     */
    void lRemoveCommand(Command cmd, int i);

    /**
     * Notifies look&feel object of commandset being updated.
     */
    void updateCommandSet();

    /**
     * Return in which Display instance the LF is visible.
     *
     * @return the current display.
     */
    Display lGetCurrentDisplay();

    /**
     * Notify the full screen mode of this LF.
     *
     * If true, this DisplayableLF will take up as much screen
     * real estate as possible. Any subclass of Displayable 
     * should be prepared to do their job to accommodate the
     * change.
     * Note that it can call into the app code.
     *
     * @param fullScreenMode true if full screen mode should be turned on
     */
    void uSetFullScreenMode(boolean fullScreenMode);

/*
 * Interface to Display
 */

    /**
     * Return the associated Displayable object.
     *
     * @return the Displayable object.
     */
    Displayable lGetDisplayable();

    /**
     * Prepare to show this LF on physical screen. This is the
     * internal version of showNotify() function as defined in MIDP spec.
     * It is called immediately prior to this LF being made visible
     * on the display. The LF should load any resource that is
     * needed, layout. App's paint() should NOT be called in this function.
     * Instead, it should be in the dsPaint() that will be called on this
     * LF shortly after.
     */
    void uCallShow();

    /**
     * Remove this displayable from physical screen.
     * The displayable should unload any resource that was allocated. It's not
     * required to clean the physical screen before this function returns.
     * This function could be called while a LF is in "freeze" mode.
     */
    void uCallHide();

    /**
     * Set the display instance the Displayable is associated with.
     * Caller should hold LCDUILock around this call.
     *
     * @param d Display instance in which this DisplayableLF is visible.
     *		null if this DisplayableLF is no longer visible.
     */
    void lSetDisplay(Display d);

    /**
     * While UI resources of this LF are created and visible already, stop any
     * further updates to physical screen because some "system modal dialog"
     * takes over physical screen buffer and user input now.
     * Repaint and invalidate requests from this DisplayableLF will be really 
     * scheduled into event queue. Instead, only dirty flag is set.
     * After a LF enters "freeze" mode, it can be resumed of visibility or 
     * directly replaced by a new Displayable.
     */
    void uCallFreeze();

    /**
     * Handle key events.
     *
     * @param type defined in EventConstants.
     * @param keyCode the key involved in this event.
     */
    void uCallKeyEvent(int type, int keyCode);

    /**
     * Handle pointer events.
     * @param type kind of event, defined in EventConstants.
     * @param x    x-coordinate of pointer event
     * @param y    y-coordinate of pointer event
     */
    void uCallPointerEvent(int type, int x, int y);

    /**
     * Relayout and repaint now. Called by the event handler to perform an
     * invalidation of this Displayable.
     */
    void uCallInvalidate();

    /**
     * Repaint now. Display calls this method on it's current Displayable.
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
    void uCallPaint(Graphics g, Object target);

    /**
     * Called to commit any pending user interaction for the current
     * item before an abstract command is fired.
     * Caller should hold LCDUILock around this call.
     */
    void lCommitPendingInteraction();

    /**
     * This method is used int repaint, int order to determine the
     * translation of the draw coordinates.
     * @return true if the scroll responsibility is of the native platform.
     * false - if the scroll is done in the Java level.     
     */
    boolean uIsScrollNative();

    /**
     * This method calls Displayable.sizeChanged method.
     * 
     * @param w the new width
     * @param h the new height
     */    
    void uCallSizeChanged(int w, int h);

    /**
     * This method notify displayable to scroll its content 
     *
     * @param scrollType scrollType
     * @param thumbPosition
     */
    void uCallScrollContent(int scrollType, int thumbPosition);

    boolean uSetRotatedStatus (boolean newStatus);

    /**
     * Get the current vertical scroll position
     *
     * @return int The vertical scroll position on a scale of 0-100
     */
    int getVerticalScrollPosition();

    /**
     * Get the current vertical scroll proportion
     *
     * @return ing The vertical scroll proportion on a scale of 0-100
     */
    int getVerticalScrollProportion();

}
