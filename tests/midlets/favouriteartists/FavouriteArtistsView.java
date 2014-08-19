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
import com.nokia.mid.ui.frameanimator.*;
import com.nokia.mid.ui.gestures.*;

/**
 * Favourite artists view. Shows current favourite artists in a grid.
 * <p>
 * This class illustrates the use of Gestures API and Frame Animator API.
 * Points of interests are: the constructor {@link #FavouriteArtistsView(CommandHandler, Display, ImageProvider)},
 * Gestures API callback {@link #gestureAction(Object, GestureInteractiveZone, GestureEvent)} and 
 * Frame Animator API callback {@link #animate(FrameAnimator, int, int, short, short, short, boolean)}
 * <p>
 * Gestures used in this example: 
 * {@link GestureInteractiveZone#GESTURE_TAP}
 * {@link GestureInteractiveZone#GESTURE_LONG_PRESS}
 * {@link GestureInteractiveZone#GESTURE_DRAG}
 * {@link GestureInteractiveZone#GESTURE_DROP}
 * {@link GestureInteractiveZone#GESTURE_FLICK}
 * <p>
 * From Frame Animator API, {@link FrameAnimator#kineticScroll(int, int, int, float)} is used
 * with {@link FrameAnimator#FRAME_ANIMATOR_FREE_ANGLE} to achieve two-dimensional kinetic scrolling
 * of the grid (used for flick gesture).
 * <p>
 * The grid can be scrolled with drag and flick gestures. A single item occupies
 * most of the screen area. After a scroll movement the view focuses on the nearest
 * item, see {@link FavouriteArtistsView.FocusMoveThread}. Also, a single tap while scrolling will focus the item.
 * When item is focused, it can be selected with either single tap or long press, see
 * {link {@link #handleSelectEvent(GestureEvent)}.
 * Item selection will open up rating view {@link RatingView}.
 */
public class FavouriteArtistsView extends Canvas implements GestureListener, FrameAnimatorListener{

	// Constants
	/** Horizontal margin between item top/bottom and screen top/bottom */
	private static final int ITEM_H_MARGIN = 20;
	/** Vertical margin between item top/bottom and screen top/bottom */
	private static final int ITEM_V_MARGIN = 20;
	
	// Member data
	/** Display. */
	private Display display;
	/** Command handler. */
	private CommandHandler commandHandler;
	/** For retrieving images. */
	ImageProvider imageProvider;
    /** Short tap action. */
    private short tapActionId;
    /** Long press action */
    private short longPressActionId;
    /** Visible area's top left x-coordinate (i.e. where in the grid are we) */    
    private int xOffset;
    /** Visible area's top left y-coordinate (i.e. where in the grid are we) */
    private int yOffset;
    /** Width of the grid, i.e. the scrollable area. */
    private int gridWidth;
    /** Height of the grid, i.e. the scrollable area. */
    private int gridHeight;
    /** Number of columns in the grid, calculated from item count. */
    private int columns;
    /** Number of rows in the grid, calculated from item count. */
    private int rows;
    /** Width of a single item, calculated from Canvas width. */
    private int itemWidth;
    /** Height of a single item, calculated from Canvas width. */
    private int itemHeight;
    /** The selected item, only valid during select action handling. */
    private GridItem selectedItem;
    /** Grid items. */
    private Vector items;
    /** The gesture interactive zone registered to this Canvas. */ 
    private GestureInteractiveZone zone;
    /** FrameAnimator instance for animating list scrolling. */
    private FrameAnimator animator;
    /** Flag for defining whether scrolling is active (gestures are handled differently while scrolling). */
    private boolean scrollingActive;
    /** Pending select gesture event that is stored for the duration of the select delay. */
    private int pendingGestureEvent;
    /** Select delay. */
    private SelectDelay selectDelay;
    /** Thread used for the select delay. Reference kept to be able to cancel. */
    private Thread delayThread;
    /** Thread used for focus move animation. Reference kept to be able to cancel.*/
    Thread focusMoveThread;
    /** Runnable used for focus move. */
    FocusMoveThread focusMoveRunnable;
    /** This is used to delay starting of focus move while dragging. */
    Thread dragDelayThread;
    
    
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
     * Drag delay thread. This is used to give the user a moment to drag some more before
     * starting the automatic focus move.
     */
    private class DragDelayThread implements Runnable{
    	
    	public void run() {
			if (Log.TEST) Log.note("[DragDelayThread#run]-->");
			try {
	            Thread.sleep(500);
	            centerOnClosestItem(true);
	        } catch (InterruptedException e) {
	        	if (Log.TEST) Log.note("[DragDelayThread#run] interrupted");
	        	return;
	        }
    	}
    }
    
    /**
     * Used to animate focus centering to an item after flick/drag.
     */
    private class FocusMoveThread implements Runnable{

    	// Constants
    	/** Amount of pixels to move per one animation step */
    	private static final int PIXELS_PER_STEP = 4;
    	
    	// Member data
    	/** X-coordinate target when focusing to item */
        private int xTarget;
        /** Y-coordinate target when focusing to item */
        private int yTarget;
        /** How many pixels to move the x-coordinate per one animation frame */
        private int xStep;
        /** How many pixels to move the y-coordinate per one animation frame */
        private int yStep;
        /** Number of steps to animate in x-direction */
        private int stepCountX;
        /** Number of steps to animate in y-direction */
        private int stepCountY;
        /** X-coordinate distance to target. */
        private int xDistance;
        /** Y-coordinate distance to target. */
        private int yDistance;
        /** Step counter. */
        private int step = 1;
        /** Defines whether to continue the animation loop. */
        private boolean keepRunning;
        
        // Methods
		/**
		 * Constructor.
		 * 
		 * @param xTarget The target x-coordinate.
		 * @param yTarget The target y-coordinate.
		 * @param xStep The x-coordinate step amount in pixels.
		 * @param yStep The y-coordinate step amount in pixels.
		 */
		public FocusMoveThread(int xTarget, int yTarget/*, int xStep, int yStep*/){
			if (Log.TEST) Log.note("[FocusMoveThread#run]-->");
			this.xTarget = xTarget;
			this.yTarget = yTarget;
			xDistance = Math.abs(xTarget - xOffset);
			yDistance = Math.abs(yTarget - yOffset);
			// Calculate step counts
			stepCountX = xDistance / PIXELS_PER_STEP;
			stepCountY = yDistance / PIXELS_PER_STEP;
			if(stepCountX < 1){
				stepCountX = 1;
			}
			if(stepCountY < 1){
				stepCountY = 1;
			}
			// Calculate the size of x/y steps
			xStep = xTarget < xOffset ? -PIXELS_PER_STEP : PIXELS_PER_STEP;
			yStep = yTarget < yOffset ? -PIXELS_PER_STEP : PIXELS_PER_STEP;
			if (Log.TEST) Log.note("[FocusMoveThread#run]"
					+ " xTarget: "
					+ xTarget
					+ " yTarget: "
					+ yTarget
					+ " xDistance: "
					+ xDistance
					+ " yDistance: "
					+ yDistance
					+ " stepCountX: "
					+ stepCountX
					+ " stepCountY: "
					+ stepCountY
					+ " xStep: "
					+ xStep
					+ " yStep: "
					+ yStep);
		}
		
		/**
		 * Stops the animation.
		 */
		public void quit(){
			keepRunning = false;
		}
		
		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			keepRunning = true;
			while(keepRunning){
				if (Log.TEST) Log.note("[FocusMoveThread#run]"
						+ " step: "
						+ step);
				scrollingActive = true;
				try {
		            Thread.sleep(10);
		        } catch (InterruptedException e) {
		        	if (Log.TEST) Log.note("[FocusMoveThread#run] interrupted");
		        	scrollingActive = false;
		        	return;
		        }
				if( stepCountX > stepCountY ){
					// update x coord
					xOffset += xStep;
					if(step % (stepCountX / stepCountY) == 0 && yOffset != yTarget){
						// update y coord as well
						yOffset += yStep;
					}
				} else if (stepCountY > stepCountX){
					// update y coord
					yOffset += yStep;
					if(step % (stepCountY / stepCountX) == 0 && xOffset != xTarget){
						// update x coord as well
						xOffset += xStep;
					}	
				} else{
					xOffset += xStep;
					yOffset += yStep;
				}
				// Check that x-coordinate does not go past target
				if(xStep > 0){
					if(xOffset > xTarget){
						xOffset = xTarget;
					}
				} else {
					if(xOffset < xTarget){
						xOffset = xTarget;
					}
				}
				// Check that y-coordinate does not go past target
				if(yStep > 0){
					if(yOffset > yTarget){
						yOffset = yTarget;
					}
				} else {
					if(yOffset < yTarget){
						yOffset = yTarget;
					}
				}
				// Make sure that we're on target if this is the last step
				if(step >= (stepCountX > stepCountY ? stepCountX : stepCountY)){
					xOffset = xTarget;
					yOffset = yTarget;
				}
				// Stop the loop when were on target
				if( xOffset == xTarget && yOffset == yTarget){
					keepRunning = false;
				}
				repaint();
				step++;
			}
			scrollingActive = false;
		}
    }
    
	// Methods
    /**
     * Constructor.
     * 
     * @param commandHandler For command handling.
     * @param display For color retrieval.
     * @param imageProvider For image retrieval.
     * @throws FavouriteArtistsException
     */
    public FavouriteArtistsView(CommandHandler commandHandler, Display display, ImageProvider imageProvider)
    	throws FavouriteArtistsException {
        
    	if (Log.TEST) Log.note("[FavouriteArtistsView#FavouriteArtistsView]-->");
    	this.commandHandler = commandHandler;
    	this.display = display;
    	this.imageProvider = imageProvider;
    	selectDelay = new SelectDelay();
    	
    	// First create a GestureInteractiveZone. The GestureInteractiveZone class is used to define an
        // area of the screen that reacts to a set of specified gestures.
        // The parameter GESTURE_ALL means that we want events for all gestures.
        zone = new GestureInteractiveZone(GestureInteractiveZone.GESTURE_DRAG | 
                GestureInteractiveZone.GESTURE_DROP |
                GestureInteractiveZone.GESTURE_FLICK |
                GestureInteractiveZone.GESTURE_LONG_PRESS |
                GestureInteractiveZone.GESTURE_LONG_PRESS_REPEATED |
                GestureInteractiveZone.GESTURE_TAP
        );
        
        // Next the GestureInteractiveZone is registered to registration manager. Note that multiple zones
        // can be registered for a single container, reference to the affected zone is passed in the event callback.
        if(GestureRegistrationManager.register(this, zone) != true){
        	// Throw an exception if register fails
        	throw new FavouriteArtistsException("GestureRegistrationManager.register() failed!");
        }
        // Add a listener for gesture events for this container. A container can be either a Canvas or a CustomItem.
        // In this example, only one gesture zone that covers the whole canvas is used.
        GestureRegistrationManager.setListener(this, this);

        // Create a FrameAnimator instance. In this class, the frame animator is used to animate grid scrolling
        // for flick gestures.
        animator = new FrameAnimator();

        // Use default values for maxFps an maxPps (zero parameter means that default is used).
        final short maxFps = 0;
        final short maxPps = 0;
        // Register the FrameAnimator. Animation uses the initial x & y coordinates as a starting point
        // i.e. the animate() callback will give coordinates in relation to this point.
        // In this example only the delta values are used from the callback so the initial values don't make much difference. 
        if(animator.register(0, 0, maxFps, maxPps, this) != true){
        	// Throw an exception if register fails
        	throw new FavouriteArtistsException("FrameAnimator.register() failed!");
        }
        
        // Define actions that are initiated for tap and long press gestures
        tapActionId = Actions.SHOW_RATING;
        longPressActionId = Actions.SHOW_RATING;
        
        // Add commands
        addCommand(new ActionCommand(Actions.EXIT_MIDLET, "Exit", Command.EXIT, 0));
        addCommand(new ActionCommand(Actions.SHOW_ADD_FAVOURITE, "Add", Command.SCREEN, 1));
        addCommand(new ActionCommand(Actions.ARRANGE_FAVOURITES, "Arrange", Command.SCREEN, 1));
    	
        // Delegate command handling to separate class.
        setCommandListener(commandHandler);
        items = new Vector();
        updateItems();
	}
    
    /**
     * Calculates grid size and updates grid item coordinates.
     */
    private void updateItems(){
    	if (Log.TEST) Log.note("[FavouriteArtistsView#updateItems]-->");
    	// Reset offsets because previous ones might not be valid anymore (e.g. out of grid area)
    	xOffset = 0;
    	yOffset = 0;
    	
    	// First determine a suitable number of rows and columns based on item count
    	columns = 1;
    	while(rows * columns < items.size()){
    		if(rows < columns){
    			rows++;
    		} else {
    			columns++;
    		}
    	}
    	
    	// Calculate item size based on Canvas size, one item is intended to be shown on screen at a time
    	itemWidth = getWidth() - 2 * ITEM_H_MARGIN;
    	itemHeight = getHeight() - 2 * ITEM_V_MARGIN;
    	
    	// Calculate grid size, this sets the limits to the area that can be scrolled
    	gridWidth = columns * (itemWidth + ITEM_H_MARGIN * 2);
        gridHeight = rows * (itemHeight + ITEM_V_MARGIN * 2);
    	
    	// Set coordinates for each item.
        // These coordinates are in relation to the grid, actual drawing coordinates are
        // derived from these and current x/y offset of visible area
        int itemX = ITEM_H_MARGIN;
    	int itemY = ITEM_V_MARGIN;
    	for (int i = 0; i < items.size(); i++) {
            GridItem item = (GridItem) items.elementAt(i);
            item.setRect(itemX, itemY, itemWidth, itemHeight);
            if(i > 0 && (i + 1) % columns == 0){
            	itemY += itemHeight + ITEM_V_MARGIN * 2;
            	itemX = ITEM_H_MARGIN;
            } else {
            	itemX += itemWidth + ITEM_H_MARGIN * 2;
            }
        }
    }
    
    /**
     * Updates the view with new favourite data.
     * @param favouriteDatas New data array.
     * @param repaint If true, then a repaint will be requested.
     */
    public void updateView(FavouriteData[] favouriteDatas, boolean repaint){
    	if (Log.TEST) Log.note("[FavouriteArtistsView#updateView]-->");
    	
    	if(favouriteDatas == null){
    		return;
    	}
    	
    	// Clear the old items first
    	items.removeAllElements();
    	
    	// Create grid items from data and add them
    	for(int i = 0; i < favouriteDatas.length; i++){
    		FavouriteData favData = favouriteDatas[i];
    		GridItem favItem = new GridItem(display, imageProvider);
    		favItem.setIcon(imageProvider.getImage(favData.getImageFilename()));
    		favItem.setFavData(favData);
    		items.addElement(favItem);
    	}
    	updateItems();
    	if(repaint){
    		repaint();
    	}
    }

	
	protected void paint(Graphics g) {
     	if (Log.TEST) Log.note("[FavouriteArtistsView#paint]-->");
        // Draw background
        g.setColor(display.getColor(Display.COLOR_BACKGROUND));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        boolean xOverBounds = false;
        boolean yOverBounds = false;
        if(xOffset < 0){
        	xOffset = 0;
        	xOverBounds = true;
        }else if(xOffset + getWidth() > gridWidth){
        	xOffset = gridWidth - getWidth();
        	xOverBounds = true;
        }
        if(yOffset < 0){
        	yOffset = 0;
        	yOverBounds = true;
        } else if(yOffset + getHeight() > gridHeight){
        	yOffset = gridHeight - getHeight();
        	yOverBounds = true;
        }
        // Stop scrolling if both x and y are over bounds
        // NOTE: if x or y alone would merit a scrolling stop, then scrolling 
        // near grid borders would have bad UX (e.g. scroll would stop because of tiny
        // y direction change when user wants to scroll in horizontally).
        // This affects flicking only.
        if (scrollingActive && xOverBounds && yOverBounds) {
            stopScrolling(true);
        }
        
        // Check which items are visible and draw only those.
        for (int i = 0; i < items.size(); i++) {
            GridItem item = (GridItem) items.elementAt(i);
            if(item.isVisible(xOffset, yOffset, getWidth(), getHeight())){
            	item.paint(g, xOffset, yOffset);
            }
        }
	}
	
	
	/**
	 * Center on the item that's closest to the given coordinates.
	 * 
	 * @param animate Determines whether the focus movement will be animated.
	 */
	private void centerOnClosestItem(int x, int y, boolean animate){
		if (Log.TEST) Log.note("[FavouriteArtistsView#centerOnClosestItem]--> x: " + x + " y: " + y);
		// Cancel any existing focus move thread
		if(focusMoveThread != null){
			focusMoveRunnable.quit();
			focusMoveThread = null;
		}
		// Find the closest item
		GridItem closestItem = null;
		int closestItemDistance = Integer.MAX_VALUE;
		for (int i = 0; i < items.size(); i++) {
            GridItem item = (GridItem) items.elementAt(i);
            int itemCenterX = item.getCenterX();
            int itemCenterY = item.getCenterY();
            int itemDistance = Math.abs(itemCenterX - x) +
            	Math.abs(itemCenterY - y);
            if (Log.TEST) Log.note("[FavouriteArtistsView#centerOnClosestItem] item center x: " + itemCenterX
            		+ " item center y: " + itemCenterY + " item distance: " + itemDistance);
            if(itemDistance < closestItemDistance){
            	closestItemDistance = itemDistance;
            	closestItem = item;
            }
        }
		centerOnItem(closestItem, animate);
	}
	
	/**
	 * Centers on the given item.
	 * 
	 * @param item The item to center on.
	 * @param animate Determines whether the focus movement will be animated.
	 */
	private void centerOnItem(GridItem item, boolean animate){
		if (Log.TEST) Log.note("[FavouriteArtistsView#centerOnItem]-->");
		if(item == null){
			return;
		}
		int xTarget = item.getX() - ITEM_H_MARGIN;
		int yTarget = item.getY() - ITEM_V_MARGIN;
		if(animate){
			// Animate the movement
			scrollingActive = true;
			focusMoveRunnable = new FocusMoveThread(xTarget, yTarget);
			focusMoveThread = new Thread(focusMoveRunnable);
			focusMoveThread.start();
		} else {
			// Just set the new coordinates 
			xOffset = xTarget;
			yOffset = yTarget;
		}
	}
	
	/**
	 * Center on the item that's closest to the center of the screen.
	 * 
	 * @param animate Determines whether focus movement will be animated.
	 */
	private void centerOnClosestItem(boolean animate){
		if (Log.TEST) Log.note("[FavouriteArtistsView#centerOnClosestItem]");
		int screenCenterX = xOffset + getWidth()/2;
		int screenCenterY = yOffset + getHeight()/2;
		centerOnClosestItem(screenCenterX, screenCenterY, animate);
	}

	
	/**
     * @see com.nokia.mid.ui.frameanimator.FrameAnimatorListener#animate(com.nokia.mid.ui.frameanimator.FrameAnimator, int, int, short, short, short, boolean)
     */
    public void animate(FrameAnimator animator, int x, int y, short delta, short deltaX, short deltaY, boolean lastFrame) {
    	if (Log.TEST) Log.note("[FavouriteArtistsView#animate]"
    			+ " x: "
    			+ x
    			+ " y: "
    			+ y
    			+ " delta: "
    			+ delta
    			+ "deltaX: "
    			+ deltaX
    			+ " deltaY: "
    			+ deltaY);
    	//NOTE: this affects only flicks, drag is not animated with FrameAnimator
    	// We want to drag the grid, so movement goes to opposite direction
    	xOffset -= deltaX;
        yOffset -= deltaY;
        // Scrolling is no longer active if this is the last frame
        scrollingActive = !lastFrame;
        repaint();
        if(lastFrame == true){
        	// Start focus move, 
        	centerOnClosestItem(true);
        }
    }

    /**
     * Stops scrolling.
     * 
     * @param center Whether to start centering.
     */
    private void stopScrolling(boolean center) {
        scrollingActive = false;
        animator.stop();
        if(center == true){
        	centerOnClosestItem(true);
        }
    }

    /**
     * @see com.nokia.mid.ui.gestures.GestureListener#gestureAction(java.lang.Object, com.nokia.mid.ui.gestures.GestureInteractiveZone, com.nokia.mid.ui.gestures.GestureEvent)
     */
    public void gestureAction(Object container, GestureInteractiveZone zone, GestureEvent event) {
        if (Log.TEST) Log.note("[FavouriteArtistsView#gestureAction]-->");
    	
    	// Block gesture handling if already handling one,
    	// this may happen due to delayed handling of events (delay is needed for showing focus on selected item)
    	if(pendingGestureEvent > 0){
    		if (Log.TEST) Log.note("[FavouriteArtistsView#gestureAction] pending gesture -> return");
    		return;
    	}
    	
    	// Stop any drag delay or focus move threads before handling the gesture 
    	if(dragDelayThread != null){
    		if (Log.TEST) Log.note("[FavouriteArtistsView#gestureAction] interrupting drag delay thread");
        	dragDelayThread.interrupt();
            dragDelayThread = null;
        }
    	if(focusMoveThread != null){
    		if (Log.TEST) Log.note("[FavouriteArtistsView#gestureAction] quitting focus move thread");
    		focusMoveRunnable.quit();
    		focusMoveRunnable = null;
    		focusMoveThread = null;
        }

        switch (event.getType()) {
            case GestureInteractiveZone.GESTURE_DRAG:{
            	if (Log.TEST) Log.note("[FavouriteArtistsView#gestureAction] drag x: "
            			+ event.getDragDistanceX()
            			+ " y: "
            			+ event.getDragDistanceY());
            	// In this example the drag gesture is directly used for altering the
            	// visible area coordinates. Note that only the delta values are used. The reason for this 
            	// is that gesture event coordinates are screen coordinates and xOffset/yOffset are grid coordinates. 
            	// NOTE: Drag gestures are received in very rapid succession, the "whole"
            	// drag (e.g. what the user would perceive as a complete drag gesture) usually ends when a
            	// GESTURE_DROP event is received.
                stopScrolling(false);
                // We want to drag the grid, so movement goes to opposite direction
                xOffset -= event.getDragDistanceX();
                yOffset -= event.getDragDistanceY();
                repaint();
                break;
            }
            case GestureInteractiveZone.GESTURE_TAP:{
            	if (Log.TEST) Log.note("[FavouriteArtistsView#gestureAction] tap");
            	// Tap gesture is used for item selection.
            	if (scrollingActive) {
                    stopScrolling(false);
                    // First center on the item if scrolling was active.
                    // Note that here we derive the grid coordinates from the event
                    // coordinates and current offset.
                    centerOnClosestItem(xOffset + event.getStartX(),
                    		yOffset + event.getStartY(), true);
                } else if (tapActionId != Actions.INVALID_ACTION_ID) {
                	// Handle gesture only if there's an action set for it
                	handleSelectEvent(event);
                }
                break;
            }
            case GestureInteractiveZone.GESTURE_DROP: {
            	if (Log.TEST) Log.note("[FavouriteArtistsView#gestureAction] drop");
            	// Drop indicates the end of a "whole" drag (see above).
        	    stopScrolling(false);
        	    // Start the drag delay thread, this delay allows the user some time to continue
        	    // dragging before initiating automatic focus movement.
                dragDelayThread = new Thread(new DragDelayThread());
                dragDelayThread.start();
                break;
            }            
            case GestureInteractiveZone.GESTURE_FLICK:{
            	if (Log.TEST) Log.note("[FavouriteArtistsView#gestureAction] flick");
            	// This gives the angle in radians.
                float angle = event.getFlickDirection();
            	if (Log.TEST) Log.note("[FavouriteArtistsView#gestureAction] flick angle: " + angle);
            	// Because we're using free angle we want the whole flick speed instead of just x or y.
            	int startSpeed = event.getFlickSpeed();
                int direction = FrameAnimator.FRAME_ANIMATOR_FREE_ANGLE;
                // This affects the deceleration of the scroll.
                int friction = FrameAnimator.FRAME_ANIMATOR_FRICTION_LOW;
                scrollingActive = true;
                // Start the scroll, animate() callbacks will follow.
                animator.kineticScroll(startSpeed, direction, friction, angle);
                break;
            }
            case GestureInteractiveZone.GESTURE_LONG_PRESS:{
            	if (Log.TEST) Log.note("[FavouriteArtistsView#gestureAction] long press"); 
            	// Long press handling has mostly the same implementation as tap.
            	if (scrollingActive) {
                    stopScrolling(false);
                    centerOnClosestItem(xOffset + event.getStartX(),
                    		yOffset + event.getStartY(), true);
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
     * Getter for selected item. Since the UI is touch based there is no selected item
     * all the time. In this example the selected item is only valid for the duration of action handling
     * 
     * @return The selected item.
     */
    public FavouriteData getSelectedItem() {
    	if (Log.TEST) Log.note("[FavouriteArtistsView#getSelectedItem]-->");
    	return selectedItem.getFavData();
    }
    
    /**
     * Common handler function for selection events.
     * 
     * @param event Gesture event.
     */
    private void handleSelectEvent(GestureEvent event){
    	if (Log.TEST) Log.note("[FavouriteArtistsView#handleSelectEvent]-->");
    	// Find the selected item if any.
    	GridItem selected = getItemAt(event.getStartX(), event.getStartY());
        if (selected != null) {
        	if (Log.TEST) Log.note("[FavouriteArtistsView#handleSelectEvent] got selected item");
        	// Draw highlight for the selected item
        	selectedItem = selected;
        	selected.setSelected(true);
        	centerOnItem(selectedItem, false);
        	repaint();
        	// Delay the event handling, so that user gets a chance to see the highlighted item.
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
    	if (Log.TEST) Log.note("[FavouriteArtistsView#handleTap]-->");
    	// Delegate handling to command handler
    	commandHandler.handleAction(tapActionId, null, this);
    }
    
    /**
     * Handler for long press gesture.
     */
    private void handleLongPress() {
     	if (Log.TEST) Log.note("[FavouriteArtistsView#handleLongPress]-->");
     	//Delegate handling to command handler
     	commandHandler.handleAction(longPressActionId, null, this);
    }

    /**
     * Finds the item with the given coordinates.
     * 
     * @param x X-coordinate.
     * @param y Y-coordinate.
     * @return Item at the given coordinates or null if no item found.
     */
    private GridItem getItemAt(int x, int y) {
        GridItem item = null;
        int translatedX = xOffset + x;
    	int translatedY = yOffset + y;
        for (int i = 0; i < items.size(); i++) {
            item = (GridItem)items.elementAt(i);
            if(item.isInItem(translatedX, translatedY)){
            	return item;
            }
        }
        return null;
    }	
}

