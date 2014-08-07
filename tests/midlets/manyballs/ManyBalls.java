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

package com.sun.midp.demos.manyballs;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class ManyBalls extends MIDlet implements CommandListener {

    Display display;
    ManyCanvas canvas;		// The main screen
    private Command exitCommand = new Command("Exit", Command.EXIT, 99);
    private Command toggleCommand = new Command("Stop/Go", Command.SCREEN, 1);
    private Command helpCommand = new Command("Help", Command.HELP, 2);
    private Form helpScreen;
    private String helpText = "^ = faster\n v = slower\n < = fewer\n> = more";


    // the GUI buttons
    //	Button exitButton, clearButton, moreButton, lessButton;

    /*
     * Create the canvas
     */
    public ManyBalls() {
	display = Display.getDisplay(this);

	canvas = new ManyCanvas(display, 40);
	canvas.addCommand(exitCommand);
	canvas.addCommand(toggleCommand);
	canvas.addCommand(helpCommand);
	canvas.setCommandListener(this);
    }

    public void startApp() throws MIDletStateChangeException {
	canvas.start();
    }
    
    public void pauseApp() {
	canvas.pause();
    }

    public void destroyApp(boolean unconditional) 
	throws MIDletStateChangeException {
	canvas.destroy();
    }

    /*
     * Respond to a command issued on the Canvas.
     */
    public void commandAction(Command c, Displayable s) {
	if (c == toggleCommand) {
	    if (canvas.isPaused())
		canvas.start();
	    else
		canvas.pause();
	} else if (c == helpCommand) {
	    canvas.pause();
	    showHelp();
	} else if (c == exitCommand) {
	    try {
		destroyApp(false);
		notifyDestroyed();
	    } catch (MIDletStateChangeException ex) {
	    }
	}
    }

    /*
     * Put up the help screen. Create it if necessary.
     * Add only the Resume command.
     */
    void showHelp() {
	if (helpScreen == null) {
	    helpScreen = new Form("Many Balls Help");
	    helpScreen.append("^ = faster\n");
	    helpScreen.append("v = slower\n");
	    helpScreen.append("< = fewer\n");
	    helpScreen.append("> = more\n");
	}
	helpScreen.addCommand(toggleCommand);
	helpScreen.setCommandListener(this);
	display.setCurrent(helpScreen);
    }



}
