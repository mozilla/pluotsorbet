package com.nokia.example.gafa;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

/**
 * This class controls the operation of the MIDlet, including command handling
 * and view creation etc.
 */
public class GAFAControl implements CommandListener {

	/** Reference of the midlet class. */
	private GAFAMidlet mainApp;
	/** A <code>Display</code> object. */
	private Display display;
	/** Main view of the app. */
	private GAFAView myCanvas;

	/**
	 * Constructor.
	 * 
	 * @param app
	 *            reference to the main midlet.
	 */
	public GAFAControl(GAFAMidlet app) {
		mainApp = app;
	}

	/** Separate initialization function for convenience. */
	public void initialize() {
		display = Display.getDisplay(mainApp);
		myCanvas = new GAFAView(this, display);
		display.setCurrent(myCanvas);
	}

	/**
	 * Set a view to the current display.
	 * 
	 * @param disp
	 *            a view to set.
	 */
	public void setCurrent(Displayable disp) {
		if (display == null)
			display = Display.getDisplay(mainApp);
		display.setCurrent(disp);
	}

	/**
	 * @see CommandListener#commandAction(Command, Displayable)
	 */
	public void commandAction(Command command, Displayable displayable) {
		if (displayable == myCanvas) {
			if (command == myCanvas.getExitCommand()) {
				exitMidlet();
			}
		}
	}

	/**
	 * Called when the user has chosen to exit the MIDlet or when the MIDlet
	 * receives a <code>destroyApp()</code> call.
	 */
	public final void exitMidlet() {
		mainApp.notifyDestroyed();
	}
}
