package com.nokia.example.gafa;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

public class GAFAMidlet extends MIDlet {

	/** A Display instance. */
	private Display display;
	/** Controls the operation of the midlet. */
	private GAFAControl control;

	/** Creates the midlet and initiates the display. */
	public GAFAMidlet() {
		display = Display.getDisplay(this);
	}

	/** Called when the midlet starts. */
	public void startApp() {
		control = new GAFAControl(this);
		control.initialize();
	}

	/** Called when the midlet is paused. */
	public void pauseApp() {
	}

	/** Destroys the midlet and exit. */
	public void destroyApp(boolean unconditional) {
		control.exitMidlet();
	}

	/**
	 * Sets a view to the display.
	 * 
	 * @param disp
	 *            A Displayable object to display.
	 */
	public void setCurrentDisplay(Displayable disp) {
		display.setCurrent(disp);
	}

}