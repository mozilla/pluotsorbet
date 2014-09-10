package com.nokia.example.gafa;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;

import com.nokia.mid.ui.frameanimator.FrameAnimator;
import com.nokia.mid.ui.frameanimator.FrameAnimatorListener;
import com.nokia.mid.ui.gestures.GestureEvent;
import com.nokia.mid.ui.gestures.GestureInteractiveZone;
import com.nokia.mid.ui.gestures.GestureListener;
import com.nokia.mid.ui.gestures.GestureRegistrationManager;

public class GAFAView extends Canvas implements GestureListener,
		FrameAnimatorListener {
	/** Commands */
	private Command exitCommand;
	/** For Gestures */
	private GestureInteractiveZone gizCanvas;
	private GestureInteractiveZone gizRectangle;
	/** For the frame animator */
	private FrameAnimator rectAnimator;
	private short dragCounterX;
	private short dragCounterY;
	private short maxFps;
	private short maxPps;
	/** For display */
	private short myRectWidth;
	private short myRectHeight;
	private int myRectPosX;
	private int myRectPosY;
	private int myGestureZoneWidth;
	private int myGestureZoneHeight;
	private int myGestureZonePosX;
	private int myGestureZonePosY;
	private int clipX;
	private int clipY;
	/** Squares data. */
	private Vector mySquares;

	// Inner classes
	/**
	 * Data structure class for tap gesture representation on screen
	 **/
	private class Square {
		public int posX;
		public int posY;

		public Square(int x, int y) {
			posX = x;
			posY = y;
		}
	}

	public GAFAView(CommandListener commandListener, Display display) {
		setTitle("GAFA APIs");
		myRectPosX = myGestureZonePosX = 5;
		myRectPosY = myGestureZonePosY = 5;
		myRectWidth = (short) (myGestureZoneWidth = 32);
		myRectHeight = (short) (myGestureZoneHeight = 32);
		mySquares = new Vector(10, 4);

		// Add commands
		exitCommand = new Command("Exit", Command.EXIT, 1);
		addCommand(exitCommand);
		setCommandListener(commandListener);

		// Create the first GestureInteractiveZone. The GestureInteractiveZone
		// class is used to define an
		// area of the screen that reacts to a set of specified gestures.
		// The parameter GESTURE_ALL means that we want events for all gestures.
		gizCanvas = new GestureInteractiveZone(
				GestureInteractiveZone.GESTURE_ALL);
		// Create the second interactive zone handling the DRAG gestures on the
		// rectangle area
		gizRectangle = new GestureInteractiveZone(
				GestureInteractiveZone.GESTURE_DRAG);
		gizRectangle.setRectangle(myRectPosX, myRectPosY, myRectWidth,
				myRectHeight);

		// Register the GestureInteractiveZones for myCanvas.
		if (GestureRegistrationManager.register(this, gizCanvas))
			System.out.println("Gestures for canvas added");
		if (GestureRegistrationManager.register(this, gizRectangle))
			System.out.println("Gestures for rect added");

		// Set this listener to a canvas or custom item
		GestureRegistrationManager.setListener(this, this);

		/*
		 * // Get system properties for fps and pps defaultFps = (short)
		 * Integer.parseInt(System
		 * .getProperty("com.nokia.mid.ui.frameanimator.fps")); defaultPps =
		 * (short) Integer.parseInt(System
		 * .getProperty("com.nokia.mid.ui.frameanimator.pps"));
		 */
		// Use default values
		maxFps = 0;
		maxPps = 0;

		// Create the frame animator for the rectangle
		rectAnimator = new FrameAnimator();
		// Initialize the frame animator
		rectAnimator.register(myRectPosX, myRectPosY, maxFps, maxPps,
				(FrameAnimatorListener) this);

		// Initialize the drag & drop counter used to store missing frame
		// increments
		dragCounterX = 0;
		dragCounterY = 0;
	}

	protected void paint(Graphics g) {
		// Get the canvas dimensions
		clipX = g.getClipHeight();
		clipY = g.getClipWidth();
		// Clear screen with white color
		g.setColor(255, 255, 255);
		g.fillRect(0, 0, clipX, clipY);

		// Draw the squares in red
		g.setColor(255, 0, 0);
		Enumeration en = mySquares.elements();
		while (en.hasMoreElements()) {
			Square sq = (Square) en.nextElement();
			g.fillRect(sq.posX, sq.posY, 4, 4);
		}
		// Draw the rectangle in black
		g.setColor(0, 0, 0);
		g.fillRect(myRectPosX, myRectPosY, myRectWidth, myRectHeight);

		// Draw the edges of the Rectangle Gesture Interactive Zone in green
		g.setColor(0, 255, 0);
		g.drawRect(myGestureZonePosX, myGestureZonePosY, myGestureZoneWidth,
				myGestureZoneHeight);
	}

	public void gestureAction(Object container,
			GestureInteractiveZone gestureZone, GestureEvent gestureEvent) {
		if (container.equals(this)) {
			if (gestureZone.equals(gizCanvas))
				handleGestureCanvas(container, gestureZone, gestureEvent);
			else if (gestureZone.equals(gizRectangle))
				handleGestureRect(container, gestureZone, gestureEvent);
		}
	}

	public void handleGestureCanvas(Object container,
			GestureInteractiveZone gestureZone, GestureEvent gestureEvent) {
		// Which gesture
		switch (gestureEvent.getType()) {
		case GestureInteractiveZone.GESTURE_TAP: {
			createSquare(gestureEvent.getStartX(), gestureEvent.getStartY());
		}
			;
			break;
		case GestureInteractiveZone.GESTURE_FLICK: {
			rectAnimator.kineticScroll(gestureEvent.getFlickSpeed(),
					FrameAnimator.FRAME_ANIMATOR_FREE_ANGLE,
					FrameAnimator.FRAME_ANIMATOR_FRICTION_MEDIUM,
					gestureEvent.getFlickDirection());
		}
			;
			break;
		default:
		}
	}

	public void handleGestureRect(Object container,
			GestureInteractiveZone gestureZone, GestureEvent gestureEvent) {
		// Which gesture
		switch (gestureEvent.getType()) {
		case GestureInteractiveZone.GESTURE_DRAG: {
			// Adds up the drag distances since last call to animate()
			dragCounterX += gestureEvent.getDragDistanceX();
			dragCounterY += gestureEvent.getDragDistanceY();

			int newX = (dragCounterX + myRectPosX);
			int newY = (dragCounterY + myRectPosY);
			rectAnimator.drag(newX, newY);
		}
			;
			break;
		default:
		}
	}

	public void animate(FrameAnimator animator, int x, int y, short delta,
			short deltaX, short deltaY, boolean lastFrame) {
		// Update paint with the new coordinates for the rectangle
		setMyRectPosX(x);
		setMyRectPosY(y);
		// Update the Gesture Interactive Zone for the rectangle
		gizRectangle.setRectangle(myRectPosX, myRectPosY, myRectWidth,
				myRectHeight);
		// Update paint with the new coordinates for the interactive zone
		myGestureZonePosX = myRectPosX;
		myGestureZonePosY = myRectPosY;
		// Refresh screen
		repaint();

		// If last frame of the animation
		if (lastFrame) {
			// Reset animation settings
			rectAnimator.unregister();
			rectAnimator.register(myRectPosX, myRectPosY, maxFps, maxPps, this);
			resetDragCounters();
		}
	}

	/* ------------------------------------------------------------------ */
	/* ::::::::::::::::::::::::::::: Utils :::::::::::::::::::::::::::::: */
	/* ------------------------------------------------------------------ */
	public void createSquare(int x, int y) {
		Square sq = new Square(x, y);
		mySquares.addElement(sq);
		repaint();
	}

	// set the x position of the rectangle so that it never goes out of the
	// canvas
	public int setMyRectPosX(int x) {
		if (x + myRectWidth > clipX) {
			myRectPosX = clipX - myRectWidth;
		} else {
			if (x < 0) {
				myRectPosX = 0;
			} else {
				myRectPosX = x;
			}
		}
		return myRectPosX;
	}

	// set the y position of the rectangle so that it never goes out of the
	// canvas
	public int setMyRectPosY(int y) {
		if (y + myRectHeight > clipY) {
			myRectPosY = clipY - myRectHeight;
		} else {
			if (y < 0) {
				myRectPosY = 0;
			} else {
				myRectPosY = y;
			}
		}
		return myRectPosY;
	}

	public Command getExitCommand() {
		return exitCommand;
	}

	public void resetDragCounters() {
		dragCounterX = dragCounterY = 0;
	}
}
