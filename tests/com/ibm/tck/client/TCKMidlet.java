package com.ibm.tck.client;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * MIDlet class that executes tests.  Invokes a TestRunner with the MIDlet
 * as a parameter.  After the tests are executed, notifyDestroyed() is called.
 */
public class TCKMidlet extends MIDlet {

	/**
	 * Performs two functions.
	 * <pre>
	 * 	new TestRunner(this);
	 * 	notifyDestroyed();
	 * </pre>
	 * @see MIDlet#startApp().
	 */
	protected void startApp() throws MIDletStateChangeException {
		new TestRunner(this);
		notifyDestroyed();
	}

	/**
	 * @see MIDlet#pauseApp()
	 */
	protected void pauseApp() {
	}

	/**
	 * @see MIDlet#destroyApp(boolean)
	 */
	protected void destroyApp(boolean flag) throws MIDletStateChangeException {
	}
}
