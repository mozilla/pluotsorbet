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

package com.sun.midp.chameleon;

import com.sun.midp.chameleon.layers.*;
import com.sun.midp.chameleon.skins.*;
import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

import javax.microedition.lcdui.*;

/**
 * The MIDPWindow class is a concrete instance of a CWindow which
 * implements the MIDP specification and its graphical elements,
 * such as a title bar, soft buttons, ticker, etc.
 */
public class MIDPWindow extends CWindow {

    // The order of layers id is impotant during creation and updating

    /** Id of layer containing the alert wash */
    public static final int ALERT_WASH_LAYER = 0;

    /** Id of layer containing the alert displayable */
    public static final int ALERT_LAYER = 1;

    /** Id of layer containing the mail */
    public static final int WASH_LAYER = 2;
    
    /** Id of layer rendering the soft button controls */
    public static final int BTN_LAYER = 3;

    /** Id of layer containing the ticker of the current displayable */
    public static final int TICKER_LAYER = 4;

    /** Id of layer containing the title of the current displayable */
    public static final int TITLE_LAYER = 5;

    /** Id of layer containing the pti contents */
    public static final int PTI_LAYER = 6;

    /** Id of layer containing the virtual keyboard contents */
    public static final int KEYBOARD_LAYER = 7;

    /** Id of layer containing the current displayable's contents */
    public static final int BODY_LAYER = 8;

    /** Number of main layers*/                                           
    public static final int LAST_LAYER = 9;

    /** Used to call back into the Display class from this package */
    ChamDisplayTunnel tunnel;

    /** Cached typed references to the namded layers */
    private WashLayer washLayer;
    private WashLayer alertWashLayer;
    private AlertLayer alertLayer;
    private TitleLayer titleLayer;
    private TickerLayer tickerLayer;
    private SoftButtonLayer buttonLayer;
    private PTILayer ptiLayer;
    private VirtualKeyboardLayer keyboardLayer;
    private BodyLayer bodyLayer;

    // layout modes
    /**
     * Normal screen mode
     */
    private static final int NORMAL_MODE         = 0;

    /**
     * Full screen mode when the current displayable
     * is occupying as much screen as possible
     */
    private static final int FULL_SCR_MODE       = 1;

    /**
     * Current screen mode
     */
    int screenMode;

    /** Cache of screen commands */
    Command[] scrCmdCache;

    /** Number of screen commands in the cache */
    int scrCmdCount;

    /** Listener to notify when a screen command is selected */
    CommandListener scrCmdListener;

    /** Cache of selected item commands */
    Command[] itemCmdCache;

    /** Number of item commands in the cache */
    int itemCmdCount;

    /** Listener to notify when an item command is selected */
    ItemCommandListener itemCmdListener;

    CLayer[] mainLayers = new CLayer[LAST_LAYER];

    /** Determines whether area of the window has been changed */
    boolean sizeChangedOccured = false;

    /** Indicates if body layer was checked for optimized Canvas painting */
    boolean bodyChecked = false;

    /** Indicates wheher body layer is overlapped with a visible layer */
    boolean bodyOverlapped = false;

    /**
     * Construct a new MIDPWindow given the tunnel to the desired
     * MIDP Display instance
     *
     * @param tunnel the "tunnel" to make calls from this java package
     *               back into the Display object in another package
     */
    public MIDPWindow(ChamDisplayTunnel tunnel) {
        super(ScreenSkin.IMAGE_BG, ScreenSkin.COLOR_BG, 
	      tunnel.getDisplayWidth(), tunnel.getDisplayHeight());

        this.tunnel = tunnel;

        for (int i = LAST_LAYER - 1; i >= 0; i-- ) {
            createLayer(i);
        }
    }

    /**
     * Request a repaint. This method does not require any bounds
     * information as it is contained in each of the Chameleon layers.
     * This method simply results in a repaint event being placed in
     * the event queue for a future callback.
     */
    public void requestRepaint() {
        if (tunnel != null) {
            tunnel.scheduleRepaint();
        }
    }

    /**
     * Set the title of this MIDPWindow. This would typically
     * correspond to the title of the current displayable, and
     * may result in the title layer appearing or disappearing.
     *
     * @param title the value of the title. null indicates there
     *              is no title.
     */
    public void setTitle(String title) {
        if (titleLayer.setTitle(title)) {
            resize();
        }
        requestRepaint();
    }

    /**
     * Set the ticker of this MIDPWindow. This would typically
     * correspond to the ticker of the current displayable, and
     * may result in the ticker layer appearing or disappearing.
     *
     * @param ticker the current Ticker object. null indicates there
     *              is no ticker.
     */
    public void setTicker(Ticker ticker) {
        if (tickerLayer.setText((ticker != null) ? ticker.getString() : null)) {
            resize();
        }
        requestRepaint();
    }

    /**
     * Alert this MIDPWindow that the given displayable is now current
     * and should be shown on the screen.
     *
     * This will establish the given displayable on the screen,
     * as well as reflect the displayable's title and ticker (if any).
     * Special circumstances may occur if the displayable is an Alert,
     * such as maintaining the current screen contents and showing the
     * Alert in a popup.
     *
     * @param displayable the newly current displayable to show
     * @param height the preferred height of the new displayable
     */
    public void showDisplayable(Displayable displayable, int height) {
        bodyLayer.opaque =  (displayable instanceof Canvas);

        Ticker t = displayable.getTicker();
        tickerLayer.setText((t != null) ? t.getString() : null);

        if (displayable instanceof Alert) {
            tickerLayer.toggleAlert(true);
            buttonLayer.toggleAlert(true);
            
            // alert does not use title layer. The title is a part of content 
            titleLayer.setTitle(null);

            alertLayer.setAlert(true, (Alert)displayable, height);
            
            paintWash(false);
            addLayer(alertLayer);
        } else {
            titleLayer.setTitle(displayable.getTitle());
	    bodyLayer.setVisible(true);
        }
        addLayer(tickerLayer);

        resize();
        requestRepaint();
    }

    /**
     * Alert this MIDPWindow that the given displayable is no longer
     * current and should be removed from the screen.
     *
     * Special circumstances may occur if the displayable is an Alert,
     * such as removing the popup and re-instating the previous
     * displayable which was visible before the Alert popped up.
     *
     * @param displayable the newly current displayable to show
     */
    public void hideDisplayable(Displayable displayable) {
        if (displayable instanceof Alert) {
            buttonLayer.toggleAlert(false);
            tickerLayer.toggleAlert(false);

            paintWash(false);
            alertLayer.setAlert(false, null, 0);
            removeLayer(alertLayer);
        } else {
            bodyLayer.setVisible(false);
        }
        
        removeLayer(tickerLayer);
        
        buttonLayer.dismissMenu();

        // Make sure that not of the popups are shown
        clearPopups();
    }

    /**
     * Determines if the system menu is currently visible. This can be useful
     * in determining the current isShown() status of the displayable.
     *
     * @return true if the system menu is up
     */
    public boolean systemMenuUp() {
        return buttonLayer.systemMenuUp();
    }

    /**
     * Request a repaint of a region of the current displayable.
     * This method specifically marks a region of the body layer
     * (which renders the displayable's contents) as dirty and
     * results in a repaint request being scheduled. The coordinates
     * are in the space of the displayable itself - that is, 0,0
     * represents the top left corner of the body layer.
     *
     * @param x the x coordinate of the dirty region
     * @param y the y coordinate of the dirty region
     * @param w the width of the dirty region
     * @param h the height of the dirty region
     */
    public void repaintDisplayable(int x, int y, int w, int h) {
        // We mark the body layer as dirty
        if (alertLayer.visible) {
            alertLayer.addDirtyRegion(x, y, w, h);
        } else {
            bodyLayer.addDirtyRegion(x, y, w, h);
        }
        requestRepaint();
    }

    /**
     * Add the given layer to this window. This method is
     * overridden from CWindow in order to special case
     * popup layers. Popup layers can have their own commands
     * which supercede those of the current displayable.
     *
     * @param layer the CLayer to add to this window
     * @return true if new layer was added, false otherwise
     */
    public boolean addLayer(CLayer layer) {
        boolean added = super.addLayer(layer);
	if (added) {
	    if (layer instanceof PopupLayer) {
		PopupLayer popup = (PopupLayer)layer;
		popup.setDirty();
		popup.visible = true;
		
		Command[] cmds = popup.getCommands();
		if (cmds != null) {
		    buttonLayer.updateCommandSet(
						 null, 0, null, cmds, cmds.length,
						 popup.getCommandListener());
		}
	    }
	    
	    if (layer instanceof PTILayer) {
		ptiLayer = (PTILayer)layer;
		mainLayers[PTI_LAYER] = layer;
		resize();
	    } else if (layer instanceof VirtualKeyboardLayer) {
		keyboardLayer = (VirtualKeyboardLayer)layer;
		mainLayers[KEYBOARD_LAYER] = layer;
		resize();
	    } else {
		layer.update(mainLayers);
	    }
	}
        if (added && layer instanceof VirtualKeyboardLayer) {
            keyboardLayer = (VirtualKeyboardLayer)layer;
            mainLayers[KEYBOARD_LAYER] = layer;
            resize();
        }

        return added;
    }

    /**
     * Remove the given layer from this window. This method is
     * overridden from CWindow in order to special case popup
     * layers. Popup layers can have their own commands which
     * supercede those of the current displayable. In this case,
     * the popup is removed and the commands in the soft button
     * bar are restored to either the next top-most popup layer
     * or the current displayable itself.
     *
     * @param layer the CLayer to remove from this window
     * @return true if the layer was able to be removed
     */
    public boolean removeLayer(CLayer layer) {
        if (super.removeLayer(layer)) {
            if (layer instanceof PopupLayer) {
                if (layer == mainLayers[PTI_LAYER]) {
                    ptiLayer = null;
                    mainLayers[PTI_LAYER] = null;
                    resize();
                }
                if (layer == mainLayers[KEYBOARD_LAYER]) {
                    keyboardLayer = null;
                    mainLayers[KEYBOARD_LAYER] = null;
                    resize();
                }

                // Now we update the command set with either the
                // next top most popup or the original cached commands
                PopupLayer p = getTopMostPopup();
                if (p != null && p.getCommands() != null) {
                    Command[] cmds = p.getCommands();
                    buttonLayer.updateCommandSet(
                        null, 0, null, cmds, cmds.length, p.getCommandListener());
                } else {
                    buttonLayer.updateCommandSet(
                        itemCmdCache, itemCmdCount, itemCmdListener,
                        scrCmdCache, scrCmdCount, scrCmdListener);
                }
            } // instanceof
            return true;
        } // removeLayer
        return false;
    }

    /**
     * Return bounds of BodyLayer currently
     * @return array of bounds
     */
    public int[] getBodyLayerBounds() {
        int[] innerBounds = new int[4];
        System.arraycopy(bodyLayer.bounds,0,innerBounds,0,4);
        return innerBounds;

    }

    /**
     * Update this MIDPWindow's current command set to match the
     * current displayable and possibly item selection.
     *
     * @param itemCommands the set of item specific commands
     * @param itemCmdCount the number of item commands
     * @param itemCmdListener the notification listener for item commands
     * @param scrCommands the set of screen specific commands
     * @param scrCmdCount the number of screen commands
     * @param scrCmdListener the notification listener for screen commands
     */
    public void updateCommandSet(Command[] itemCommands,
                                 int itemCmdCount,
                                 ItemCommandListener itemCmdListener,
                                 Command[] scrCommands,
                                 int scrCmdCount,
                                 CommandListener scrCmdListener)
    {
        // We cache commands to easily reset them when a
        // popup takes precedence and then is dismissed
        this.itemCmdCache = itemCommands;
        this.itemCmdCount = itemCmdCount;
        this.itemCmdListener = itemCmdListener;
        this.scrCmdCache = scrCommands;
        this.scrCmdCount = scrCmdCount;
        this.scrCmdListener = scrCmdListener;

        buttonLayer.updateCommandSet(itemCommands, itemCmdCount,
                                  itemCmdListener,
                                  scrCommands, scrCmdCount,
                                  scrCmdListener);
	resize();
    }

    /**
     * Set this MIDPWindow's displayable to "fullscreen" mode. This
     * will expand the region occupied by the current displayable to
     * include the area previously occupied by the title and ticker
     * if present
     *
     * @param onOff true if the displayable should be in fullscreen mode
     */
    public void setFullScreen(boolean onOff) {
        if (onOff) {
            setMode(FULL_SCR_MODE);
        } else {
            setMode(NORMAL_MODE);
        }
    }

    /**
     * Update the current layout
     */
    public void updateLayout() {
        resize();
        requestRepaint();
    }

    /**
     * Changes layout mode.
     *
     * @param mode the mode to be set
     */
    private void setMode(int mode) {
        screenMode = mode;
        updateLayout();
    }

    /**
     * Determines if window is in full screen mode.
     * 
     * @return true if in full screen mode
     */
    public boolean isInFullScreenMode() {
        return screenMode == FULL_SCR_MODE;
    }




    /**
     * Called to paint a wash over the background of this window.
     * Used by SoftButtonLayer when the system menu pops up, and
     * internally when an Alert is shown.
     *
     * @param onOff A flag indicating if the wash should be on or off
     */
    public void paintWash(boolean onOff) {
	if (alertLayer.visible) {
            addLayer(washLayer);
            if (onOff) {
                addLayer(alertWashLayer);
            } else {
                removeLayer(alertWashLayer);

                tickerLayer.addDirtyRegion();
                alertLayer.addDirtyRegion();
            }
        } else {
            removeLayer(alertWashLayer);
            if (onOff) {
                addLayer(washLayer);
            } else {
                removeLayer(washLayer);

                tickerLayer.addDirtyRegion();
                titleLayer.addDirtyRegion();

                if (ptiLayer != null) {
                    ptiLayer.addDirtyRegion();
                }
            }
        }
    }

    /**
     * Returns the left soft button (one).
     *
     * @return the command that's tied to the left soft button
     */
    public Command getSoftOne() {
        return buttonLayer.getSoftOne();
    }

    /**
     * Returns the command array tied to the right soft button (two).
     *
     * @return the command array that's tied to the right soft button
     */
    public Command[] getSoftTwo() {
        return buttonLayer.getSoftTwo();
    }

    /**
     * Called by soft button layer when interactive state of it
     * has been changed
     *
     * @param interactive if soft buttons are currently interactive.
     */
    public void onSoftButtonInteractive(boolean interactive) {
        if (FULL_SCR_MODE == screenMode) {
            // IMPL NOTES: in full screen mode  we hide/show soft button layer 
            // depending on its interactiveness, so we should update layout
            updateLayout();
        }
    }

    /**
     * Returns true if the point lies in the bounds of commnad layer
     * @param x the "x" coordinate of the point
     * @param y the "y" coordinate of the point
     * @return true if the point lies in the bounds of commnad layer
     */
    public boolean belongToCmdLayers(int x, int y) {
        return buttonLayer.belongToCmdLayers(x,y);
    }
    
    /**
     * Set the current vertical scroll position and proportion.
     *
     * @param scrollPosition vertical scroll position.
     * @param scrollProportion vertical scroll proportion.
     * @return true if set vertical scroll occues
     */
    public boolean setVerticalScroll(int scrollPosition, int scrollProportion) {

        BodyLayer layer = null;
        if (alertLayer.isVisible()) {
            layer = alertLayer;
        } else if (bodyLayer.isVisible()) {
            layer = bodyLayer;
        }

        if (layer != null && layer.setVerticalScroll(scrollPosition, scrollProportion)) {
            setDirty();
            sizeChangedOccured = true;
            return true;
        }

        return false;
    }

    /**
     * Get the current x anchor coordinate for the body layer (the body
     * layer renders the contents of the current displayable).
     *
     * @return the x anchor coordinate of the body layer
     */
    public int getBodyAnchorX() {
        return bodyLayer.bounds[X];
    }

    /**
     * Get the current y anchor coordinate for the body layer (the body
     * layer renders the contents of the current displayable).
     *
     * @return the y anchor coordinate of the body layer
     */
    public int getBodyAnchorY() {
        return bodyLayer.bounds[Y];
    }

    /**
     * Get the current width of the body layer (the body
     * layer renders the contents of the current displayable).
     *
     * @return the width of the body layer
     */
    public int getBodyWidth() {
        return bodyLayer.bounds[W];
    }

    /**
     * Get the current height of the body layer (the body
     * layer renders the contents of the current displayable).
     *
     * @return the height of the body layer
     */
    public int getBodyHeight() {
        return bodyLayer.bounds[H];
    }

    /** 
     * Calculate the width of some default Body layer wich is still not rendered on the screen
     * depending on the screen mode and the layers attached to the screen
     * @param width screen width 
     * @param isFullScn true if the full scren is set for the body layer      
     * @param scrollBarIsVisible true if the scroll bar is in use for the body layer 
     * @return width of the paticular body layer
     */
    public static int getDefaultBodyWidth(int width, 
					  boolean isFullScn, 
					  boolean scrollBarIsVisible) {
	int w = width;
	// TODO: scroll arrows (bar? ) indicator has to be hidden?
	if (scrollBarIsVisible) {
	    w -= ScrollIndSkin.WIDTH;
	}
	return w;
    }
    
    /** 
     * Calculate the height of some default Body layer wich is still not rendered on the screen 
     * depending on the screen mode and the layers attached to the screen
     * param height scren height
     * @param isFullScn true if the full scren is set for the body layer      
     * @param titleIsVisible true if the title is attached      
     * @param tickerIsVisible true if the ticker is attached 
     * @param softBtnLayerIsVisible true if command layer is visible
     * @return height of the paticular body layer
     */
    public static int getDefaultBodyHeight(int height,
					   boolean isFullScn, 
					   boolean titleIsVisible, 
					   boolean tickerIsVisible, 
					   boolean softBtnLayerIsVisible) {
	int h = height;
	if (!isFullScn) {
	    if (titleIsVisible) {
		h -= TitleSkin.HEIGHT;
	    }
	    if (tickerIsVisible) {
		h -= TickerSkin.HEIGHT;
	    }
	    if (softBtnLayerIsVisible) {
		h -= SoftButtonSkin.HEIGHT;
	    }
	}
	return h;
    }
    

    /**
     * Get the current width of the alert layer (the body
     * layer renders the contents of the current displayable).
     *
     * @return the width of the alert layer
     */
    public int getAlertWidth() {
        return alertLayer.bounds[W];
    }

    /**
     * Get the current height of the alert layer (the body
     * layer renders the contents of the current displayable).
     *
     * @return the height of the alert layer
     */
    public int getAlertHeight() {
        return alertLayer.bounds[H];
    }

    /**
     * Utility method to determine if the given point lies within
     * the bounds of body layer. The point should be in the coordinate
     * space of this layer's containing CWindow.
     *
     * @param x the "x" coordinate of the point
     * @param y the "y" coordinate of the point
     * @return true if the coordinate lies in the bounds of this layer
     */
    public boolean bodyContainsPoint(int x, int y) {
        return bodyLayer.containsPoint(x, y);
    }

    /**
     * MIDPWindow overrides the parent paint method in order to
     * do special effects such as paint a "wash" over the background
     * when a dialog is up. Also in an effort to call
     * {@link javax.microedition.lcdui.Displayable#sizeChanged }
     * method before painting. This implementation determine whether size
     * has been changed and calls <code>sizeChanged()</code> if it's so.
     * Anyway it invokes the base class's {@link CWindow#paint} method.
     *
     * @param g The graphics object to use to paint this MIDP window.
     * @param refreshQ The chameleon graphics queue.
     */
    public void callPaint(Graphics g, CGraphicsQ refreshQ) {
        if (sizeChangedOccured) {
            if (tunnel != null) {
                int w = getBodyWidth();
                int h = getBodyHeight();
                tunnel.callSizeChanged(w, h);
                sizeChangedOccured = false;
            }
        }
        super.paint(g, refreshQ);
    }

    /**
     * This method is an optimization which allows Display to bypass
     * the Chameleon paint engine logic and directly paint an animating
     * canvas. Display will call this method with the graphics context
     * and this method will either return false, indicating the Chameleon
     * paint engine should not be bypassed, or will return true and will
     * setup the graphics context for the canvas to be painted directly.
     *
     * @param g the graphics context to setup
     * @return true if Chameleon's paint logic can be bypassed and the
     *         canvas can be rendered directly.
     */
    public boolean setGraphicsForCanvas(Graphics g) {
        // IMPL_NOTE: Only Canvas painting specially doesn't change dirty
        //   state of the owner window, however it is not enough to bypass
        //   the Chameleon paint engine. Body layer holding the Canvas
        //   should be not overlapped by a visible layer also.
        if (super.dirty) {
            // Schedule next overlapping check
            bodyChecked = false;
            return false;
        }
        if (!bodyChecked) {
            bodyOverlapped = !bodyLayer.opaque ||
                isOverlapped(bodyLayer);
            bodyChecked = true;
        }
        if (!bodyOverlapped) {
            bodyLayer.setGraphicsForCanvas(g);
            return true;
        }
        return false;
    }

    /**
     * Internal method to resize window and its content layers
     * according to a size changes in the loaded skins.
     * This is important to re-calculate whenever things such as
     * titles, tickers, fullscreen mode, etc. change state.
     */
    public void resize() {
        super.resize(tunnel.getDisplayWidth(), tunnel.getDisplayHeight());

        int oldHeight = bodyLayer.bounds[H];
        int oldWidth = bodyLayer.bounds[W];
        switch (screenMode) {
            case FULL_SCR_MODE:
                // TODO: scroll arrows (bar? ) indicator has to be hidden?
                titleLayer.visible = false;
                tickerLayer.visible = false;
                buttonLayer.visible =
                    buttonLayer.isInteractive();
                break;
            case NORMAL_MODE:
                titleLayer.visible =
                    (titleLayer.getTitle() != null);
                tickerLayer.visible =
                    (tickerLayer.getText() != null);
		buttonLayer.visible = true;
                break;
            default:
                Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                    "MIDPWindow: screenMode=" + screenMode);
                return;
        }

        for (int i = 0; i < LAST_LAYER; i++) {
            CLayer l = mainLayers[i];
            if (l != null && l.visible) {
                l.update(mainLayers);
            }
        }

        if (bodyLayer.bounds[W] != oldWidth ||
                bodyLayer.bounds[H] != oldHeight) {
            setDirty();
            sizeChangedOccured = true;
        }
    }

    /**
     * Internal method to clear all current popups. This occurs if a
     * change of displayable occurs, as all popups are treated as belonging
     * to the current displayable.
     */
    protected void clearPopups() {
        synchronized (super.layers) {
            for (CLayerElement le = super.layers.getTop();
                    le != null; le = le.getLower()) {
                CLayer l = le.getLayer();
                if (l instanceof PopupLayer) {
                    removeLayer(l);
                }
            }
        }
    }

    /**
     * Gets the "top" most Popup layer added to this body layer.
     * If there are no popups, this method returns null.
     *
     * @return the top most popup layer, or null if there are none.
     */
    public PopupLayer getTopMostPopup() {
        synchronized (super.layers) {
            for (CLayerElement le = super.layers.getTop();
                    le != null; le = le.getLower()) {
                CLayer l = le.getLayer();
                if (l instanceof PopupLayer) {
                    return (PopupLayer)l;
                }
            }
        }
        return null;
    }

    /**
     * create new layer by id and launch addLayer()
     * @param id - layer id
     */
    private void createLayer(int id) {
        switch (id) {
            case PTI_LAYER:
                break;
            case TITLE_LAYER:
                titleLayer = new TitleLayer();
                mainLayers[id] = titleLayer;
                addLayer(titleLayer);
                break;
            case TICKER_LAYER:
                tickerLayer = new TickerLayer();
                mainLayers[id] = tickerLayer ;
                break;
            case BTN_LAYER:
                buttonLayer = new SoftButtonLayer(tunnel);
                mainLayers[id] = buttonLayer;
                addLayer(buttonLayer);
                break;
            case ALERT_LAYER:
                alertLayer = new AlertLayer(tunnel);
                mainLayers[id] = alertLayer;
                break;
            case WASH_LAYER:
                washLayer = new WashLayer();
                mainLayers[id] = washLayer;
                break;
            case ALERT_WASH_LAYER:
                alertWashLayer = new WashLayer();
                mainLayers[id] = alertWashLayer;
                break;
            case KEYBOARD_LAYER:
                break;
            case BODY_LAYER:
                bodyLayer = new BodyLayer(tunnel);
                mainLayers[id] = bodyLayer;
                addLayer(bodyLayer);
                break;
        }
    }
}

