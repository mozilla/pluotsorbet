/*
 * Copyright � 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.favouriteartists;

import java.util.Vector;

import javax.microedition.lcdui.*;

import com.nokia.example.favouriteartists.tool.Log;
import com.nokia.mid.ui.gestures.*;
import com.nokia.mid.ui.frameanimator.*;
 

/**
 * Simple list control that uses the Gestures- and Frame Animator APIs for scrolling
 * and handling selecting list items. Each list item has an image and a short
 * string.
 * <p>
 *  This class illustrates the use of Gestures API and Frame Animator API.
 * Points of interests are: the constructor {@link #CustomList(CustomListOwner, short, short)},
 * Gestures API callback {@link #gestureAction(Object, GestureInteractiveZone, GestureEvent)} and 
 * Frame Animator API callback {@link #animate(FrameAnimator, int, int, short, short, short, boolean)}
 * <p>
 * Gestures used in this example: 
 * {@link GestureInteractiveZone#GESTURE_TAP}
 * {@link GestureInteractiveZone#GESTURE_LONG_PRESS}
 * {@link GestureInteractiveZone#GESTURE_DRAG}
 * {@link GestureInteractiveZone#GESTURE_FLICK}
 * <p>
 * From Frame Animator API, {@link FrameAnimator#kineticScroll(int, int, int, float)} is used
 * with {@link FrameAnimator#FRAME_ANIMATOR_VERTICAL} to achieve kinetic scrolling of the list
 * (used for flick gesture).
 * <p>
 * Items can be selected by either short tap or long press. An action can be mapped for both gestures.
 * When an item is selected the selection is highlighted briefly to give the user some visual feedback
 * of the selection (See {@link #handleSelectEvent} and {@link CustomList.SelectDelay}). 
 * <p>
 * The {@link ListItem} objects represent individual items and are responsible for drawing themselves.
 * <p>
 * Scrolling is done by modifying the {@link #translateY} value based on the
 * values received from the Gestures- and Frame Animator API callbacks. The list
 * paint always goes through all list items from first to last, but skips
 * drawing of items that aren't partially or fully visible. In theory the list
 * items can be of any height (in theory because this hasn't been tested).
 * <p>
 * This list control does not inherit any {@link Displayable} directly. Instead, the owner
 * instance is a {@link Displayable} that delegates actual painting etc. to this class.
 * The reason for this is to make this class re-usable ({@link Canvas} and {@link CustomItem}) 
 */
public class CustomList implements GestureListener, FrameAnimatorListener{
	
	// Member data
	/** Owner of this list control. */
    private CustomListOwner owner;
    /** Short tap action. */
    private short tapActionId;
    /** Long press action */
    private short longPressActionId;
    /** Height */
    private int height;
    /** The selected item, only valid during select action handling. */
    private ListItem selectedItem;
    /** List items. */
    private Vector items;
    /** Filtered items */
    private Vector originalItems;
    /** Filter string */
    private String filterString;
    /** Y-coordinate of the top of visible area. */
    private int translateY = 0;
    /** FrameAnimator instance for animating list scrolling. */
    private FrameAnimator animator;
    /** Flag for defining whether scrolling is active (gestures are handled differently while scrolling). */
    private boolean scrollingActive;
    /** Pending select gesture event that is stored for the duration of the select delay. */
    private int pendingGestureEvent;
    /** Select delay */
    private SelectDelay selectDelay;
    /** Thread used for the select delay. Reference kept to be able to cancel during delay. */
    private Thread delayThread;
    /** Last known list height */
    private int lastKnownListHeight = -1;
    /** The MIDlet's main display */
    private Display display;
    
    // Inner classes
    /**
     * Delay for showing selection focus to user before initiating action.
     */
    private class SelectDelay implements Runnable {
	 
    	/**
	     * @see java.lang.Runnable#run()
	     */
	    public void run() {
	    	if (Log.TEST) Log.note("[SelectDelay#run]-->");
	        try {
	            Thread.sleep(100);
	        } catch (InterruptedException e) {
	        	if (Log.TEST) Log.note("[SelectDelay#run] interrupted");
	        	return;
	        }
	        if(pendingGestureEvent > 0){
	        	if (Log.TEST) Log.note("[SelectDelay#run] handling pending gesture");
	        	switch (pendingGestureEvent) {
				case GestureInteractiveZone.GESTURE_TAP:{
					handleTap();
					break;
				}
				case GestureInteractiveZone.GESTURE_LONG_PRESS:{
					handleLongPress();
					break;
				}

				default:
					if (Log.TEST) Log.note("[SelectDelay#run] wrong type!");
					break;
				}
	        	selectedItem.setSelected(false);
	            selectedItem = null;
	        	pendingGestureEvent = 0;
	        	delayThread = null;
	        }        
    	}
    }

    
    /**
     * Constructor.
     * 
     * @param owner Parent object (e.g. Canvas or CustomItem).
     * @param tapActionId Action for short tap gesture.
     * @param longPressActionId Action for long press gesture.
     */
    public CustomList(CustomListOwner owner, short tapActionId, short longPressActionId, Display display)
    throws FavouriteArtistsException {
        
    	if (Log.TEST) Log.note("[CustomList#CustomList]-->");
    	
        this.display = display;
    	this.owner = owner;
    	this.tapActionId = tapActionId;
    	this.longPressActionId = longPressActionId;
    	this.items = new Vector();
    	selectDelay = new SelectDelay();

        // First create a GestureInteractiveZone. The GestureInteractiveZone class is used to define an
        // area of the screen that reacts to a set of specified gestures.
        // The parameter GESTURE_ALL means that we want events for all gestures.
        GestureInteractiveZone zone = new GestureInteractiveZone(GestureInteractiveZone.GESTURE_DRAG | 
                GestureInteractiveZone.GESTURE_DROP |
                GestureInteractiveZone.GESTURE_FLICK |
                GestureInteractiveZone.GESTURE_LONG_PRESS |
                GestureInteractiveZone.GESTURE_LONG_PRESS_REPEATED |
                GestureInteractiveZone.GESTURE_TAP
);
        // Next the GestureInteractiveZone is registered to registration manager. Note that multiple zones
        // can be registered for a single container, reference to the affected zone is passed in the event callback.
        if(GestureRegistrationManager.register(owner, zone) != true){
        	//throw new FavouriteArtistsException("GestureRegistrationManager.register() failed!");
        }
        // Add a listener for gesture events.
        GestureRegistrationManager.setListener(owner, this);

        // Create a FrameAnimator instance.
        animator = new FrameAnimator();

        // Use default values for maxFps an maxPps (zero param means that default is used).
        final short maxFps = 0;
        final short maxPps = 0;
        // Register the FrameAnimator. Animation uses the initial x & y coordinates as a starting point
        // i.e. the animate() callback will give coordinates in relation to this point.
        if(animator.register((short)0, (short)0, maxFps, maxPps, this) != true){
        	throw new FavouriteArtistsException("GestureRegistrationManager.register() failed!");
        }
    }
    
    /**
     * Height of this list.
     */
    public void setHeight(int height){
    	if (Log.TEST) Log.note("[CustomList#setHeight]-->");
    	this.height = height;
    }
    
    /**
     * Appends an item to the list.
     * 
     * @param item Item to append.
     */
    public void appendItem(ListItem item){
    	if (Log.TEST) Log.note("[CustomList#appendItem]-->");
    	items.addElement(item);
    }
    
    /**
     * Removes an item from the list.
     * @param item The item to remove
     */
    public void removeItem(ListItem item){
    	if (Log.TEST) Log.note("[CustomList#appendItem]-->");
    	items.removeElement(item);
    	translateY = 0;
    }
    
    /**
     * Clears the list.
     */
    public void clearList(){
    	if (Log.TEST) Log.note("[CustomList#clearList]-->");
    	translateY = 0;
    	selectedItem = null;
    	items.removeAllElements();
    	originalItems = null;
    	filterString = null;
    }
    
    /**
     * Filters the visible list to items which' text1 contains the given search string.
     * 
     * @param filterString Filter string.
     */
    public void filterList(String filterString){
    	if (Log.TEST) Log.note("[CustomList#filterList]-->");
    	if(filterString == null || filterString.length() == 0){
    		return;
    	}
    	if (Log.TEST) Log.note("[CustomList#filterList] filterString: " + filterString );
    	Vector sourceItems = null;
    	if(this.filterString != null && this.filterString.startsWith(filterString) == true){
    		if (Log.TEST) Log.note("[CustomList#filterList] using old filter");
    		// Filter the existing set further
    		this.filterString = filterString;
    		sourceItems = items;
    	} else {
    		if (Log.TEST) Log.note("[CustomList#filterList] creating new filter");
    		// Remove any existing filter
    		if(this.filterString != null){
    			this.filterString = null;
        		items = originalItems;
    		}    		
    		// New filter
    		this.filterString = filterString;
    		// Store the original items
    		originalItems = items;
    		sourceItems = originalItems;
    		// Create new vector for the filtered items.
    		items = new Vector();
    	}
    	// Filter matching items
    	for(int i = 0; i < sourceItems.size(); i++){
    		ListItem item = (ListItem)sourceItems.elementAt(i);
    		if(item.getText1().indexOf(filterString) >= 0){
    			items.addElement(item);
    		}
    	}
    	translateY = 0;
    }
    
    /**
     * Removes possible filter.
     */
    public void removeFilter(){
    	if(originalItems != null){
    		items = originalItems;
    	}
    	originalItems = null;
    	filterString = null;
    }
    
    /**
     * Gets the selected item. Only valid during command handling.
     * 
     * @return The selected item or null if nothing selected.
     */
    public ListItem getSelectedItem(){
    	if (Log.TEST) Log.note("[CustomList#getSelectedItem]-->");
    	return selectedItem;
    }

    /**
     * @see com.nokia.mid.ui.frameanimator.FrameAnimatorListener#animate(com.nokia.mid.ui.frameanimator.FrameAnimator, int, int, short, short, short, boolean)
     */
    public void animate(FrameAnimator animator, int x, int y, short delta, short deltaX, short deltaY, boolean lastFrame) {
        // In this example the y coordinate is used directly.
    	translateY = y;
    	// Scrolling is no longer active if this is the last frame
        scrollingActive = !lastFrame;
        // Request repaint from parent Displayable
        owner.requestRepaint();
    }

    /**
     * Stops scrolling.
     */
    private void stopScrolling() {
        scrollingActive = false;
        animator.stop();         
    }

    /**
     * @see com.nokia.mid.ui.gestures.GestureListener#gestureAction(java.lang.Object, com.nokia.mid.ui.gestures.GestureInteractiveZone, com.nokia.mid.ui.gestures.GestureEvent)
     */
    public void gestureAction(Object container, GestureInteractiveZone zone, GestureEvent event) {
        
    	// Block gesture handling if already handling one,
    	// this may happen due to delayed handling of events (delay is needed for showing focus on selected item)
    	if(pendingGestureEvent > 0){
    		return;
    	}

        switch (event.getType()) {
            case GestureInteractiveZone.GESTURE_DRAG:{
            	if (Log.TEST) Log.note("[CustomList#gestureAction] drag");
            	// In this example the drag gesture is directly used for altering the
            	// visible area y-coordinate. Note that only the delta value is used. The reason for this 
            	// is that gesture event coordinates are screen coordinates and translateY is a list coordinate. 
            	// NOTE: Drag gestures are received in very rapid succession, the "whole"
            	// drag (e.g. what the user would perceive as a complete drag gesture) usually ends when a
            	// GESTURE_DROP event is received. In this example we don't need to know when user stops the drag,
            	// so GESTURE_DROP is omitted.
                if (scrollingActive) {
                    stopScrolling();
                } else {
                    translateY += event.getDragDistanceY();
                    owner.requestRepaint();
                }
                break;
            }
            case GestureInteractiveZone.GESTURE_TAP:{
            	if (Log.TEST) Log.note("[CustomList#gestureAction] tap");
            	if (scrollingActive) {
                	// Stop scrolling first
                    stopScrolling();
                } else if (tapActionId != Actions.INVALID_ACTION_ID) {
                	// Select is initiated only when list is not scrolling.
                	// Handle gesture only if there's an action set for it
                	handleSelectEvent(event);
                }
                break;
            }      
            case GestureInteractiveZone.GESTURE_FLICK:{
            	if (Log.TEST) Log.note("[CustomList#gestureAction] flick"); 
                // Start vertical flick only if the gesture is more vertical than horizontal.
            	float angle = Math.abs(event.getFlickDirection());
            	if (Log.TEST) Log.note("[CustomList#gestureAction] flick angle: " + angle);
                if((angle > (Math.PI / 4) && angle < (3 * Math.PI / 4))){
                	if (Log.TEST) Log.note("[CustomList#gestureAction] flick accepted");
                	// Because we're using only vertical scrolling we use just the y speed.
                	int startSpeed = event.getFlickSpeedY();
                	int direction = FrameAnimator.FRAME_ANIMATOR_VERTICAL;
                    // This affects the deceleration of the scroll.
                    int friction = FrameAnimator.FRAME_ANIMATOR_FRICTION_LOW;
                    scrollingActive = true;
                    // Start the scroll, animate() callbacks will follow.
                    animator.kineticScroll(startSpeed, direction, friction, 0);
                }
                break;
            }
            case GestureInteractiveZone.GESTURE_LONG_PRESS:{
            	if (Log.TEST) Log.note("[CustomList#gestureAction] long press"); 
            	//Long press handling has mostly the same implementation as tap.
            	if (scrollingActive) {
                    stopScrolling();
                } else if (longPressActionId != Actions.INVALID_ACTION_ID){
                	// Handle gesture only if there's an action set for it
                	handleSelectEvent(event);
                }
                break;
            }
            default:
                break;
        }
    }
    
    /**
     * Common handler function for selection events.
     * 
     * @param event Gesture event.
     */
    private void handleSelectEvent(GestureEvent event){
    	if (Log.TEST) Log.note("[CustomList#handleSelectEvent]-->");
    	ListItem selected = getItemAt(event.getStartX(), event.getStartY());
        if (selected != null) {
        	if (Log.TEST) Log.note("[CustomList#handleSelectEvent] got selected item");
        	// Draw focus for the selected item
        	selectedItem = selected;
        	selected.setSelected(true);
        	owner.requestRepaint();
        	// Delay the event handling, so that user gets a chance to see the focus
        	// Gesture is stored so that we can handle it later on.
        	pendingGestureEvent = event.getType();
        	delayThread = new Thread(selectDelay);
        	delayThread.start();
        }
    }
    
    /**
     * Handler for tap gesture.
     */
    private void handleTap() {
    	if (Log.TEST) { 
            Log.note("[CustomList#handleTap]-->");
            animator.unregister();
        }
    	// Delegate handling to owner
    	owner.handleAction(tapActionId);
    }
    
    /**
     * Handler for long press gesture.
     */
    private void handleLongPress() {
     	if (Log.TEST) Log.note("[CustomList#handleLongPress]-->");
     	// Delegate handling to owner
        owner.handleAction(longPressActionId);
    }

    /**
     * Finds the item with the given coordinates.
     * 
     * @param x X-coordinate.
     * @param y Y-coordinate.
     * @return Item at the given coordinates or null if no item found.
     */
    private ListItem getItemAt(int x, int y) {
        y -= translateY;
        ListItem item = null;
        int heightSoFar = 0;
        int heightNext = 0;

        // Go through all items (pretend that list item heights can vary).
        for (int i = 0; i < items.size(); i++) {
            item = (ListItem) items.elementAt(i);
            heightNext += item.getHeight();

            if (y >= heightSoFar && y <= heightNext) {
                return item;
            }

            heightSoFar += item.getHeight();
        }

        return null;
    }

    /**
     * @see javax.microedition.lcdui.Canvas#paint(javax.microedition.lcdui.Graphics)
     */
    protected void paint(Graphics g) {
     	if (Log.TEST) Log.note("[CustomList#paint] ");
        
        if (translateY > 0) {
            // Trying to scroll beyond list start.
            translateY = 0;

            if (scrollingActive) {
                stopScrolling();
            }

        } else if (lastKnownListHeight != -1 && (translateY + lastKnownListHeight) < height) {
            // Trying to scroll beyond list end.
            translateY = -lastKnownListHeight + height;

            if (scrollingActive) {
                stopScrolling();
            }
        }
        if (Log.TEST) Log.note("[CustomList#paint] translateY: " + translateY);
        g.translate(0, translateY);

        ListItem item = null;
        int yOffset = 0;
        int yOffsetNext = 0;
        int y0 = -translateY;
        int y1 = y0 + height;
        int i = 0;
        
        g.setColor(display.getColor(Display.COLOR_BACKGROUND));
        g.fillRect(0, 0, g.getClipWidth(), g.getClipHeight()*items.size());
        g.setColor(display.getColor(Display.COLOR_FOREGROUND));
        
        for (; i < items.size(); i++) {
            item = (ListItem) items.elementAt(i);
            yOffsetNext += item.getHeight();
            
            if (yOffsetNext < y0) {
                // Item is not visible -> skip drawing it.
                yOffset = yOffsetNext;
                continue;

            } else if (yOffset > y1) {
                // Item would be drawn "under" the visible area -> stop drawing.
                break;
            }
            if (Log.TEST) Log.note("[CustomList#paint] yOffset: " + yOffset);
            item.paint(g, yOffset);
            yOffset = yOffsetNext;
        }

        if (i == items.size()) {
            lastKnownListHeight = yOffset;
        }
    }
}
