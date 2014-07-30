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

package com.sun.midp.appmanager;

import javax.microedition.lcdui.*;
import com.sun.midp.installer.*;

import java.util.Timer;
import java.util.TimerTask;


/** Implements the splash screen */
class SplashScreen extends Canvas {

    /** Splash screen image */
    private Image splashScreen;

    /** The displayable to be displayed after SplashScreen is dismissed. */
    private Displayable nextScreen;

    /** The display associated with Manager midlet. */
    private Display display;

    /**
     * A TimerTask which will be set to switch to the App Manager Screen
     * after its timeout period has elapsed.
     */
    TimerTask timerTask;

    /**
     * A Timer which serves this Splash Screen object to schedule
     * its timeout task
     */
    Timer timeoutTimer;

    /**
     * Creates a Splash screen.
     * @param display - the display associated wit the Manager midlet
     * @param nextScreen - the screen to be displayed after timeout
     */
    SplashScreen(Display display, Displayable nextScreen) {
        this.nextScreen = nextScreen;
        this.display = display;

        setFullScreenMode(true);
    }

    /**
     * Paint splash screen.
     *
     * @param g Graphics instance to paint on
     */
    public void paint(Graphics g) {

        // White background
        g.setColor(255, 255, 255);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (splashScreen != null) {
            g.drawImage(splashScreen, 0, 0, Graphics.LEFT | Graphics.TOP);
        }
    }

    /**
     * Override showNotify to set a timer task so that Splash screen
     * automatically switches to the App Manager Screen.
     */
    public void showNotify() {
        if (timerTask != null) {
            timerTask.cancel();
        }

        if (timeoutTimer  == null) {
            timeoutTimer = new Timer();
        }

        timerTask = new TimeoutTask();
        timeoutTimer.schedule(timerTask, 2000);
    }

    /**
     * This method is called when available area of 
     * the Displayable has been changed.
     */
    protected  void  sizeChanged(int w, int h) {
        splashScreen = 
            GraphicalInstaller.getImageFromInternalStorage("splash_screen_" 
            + getWidth() + "x" + getHeight());
        
    }

    /**
     * Override hideNotify  to cancel timer task.
     */
    public void hideNotify() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        timerTask = null;

        timeoutTimer = null;
    }

// *****************************************************
//  Internal Class
// *****************************************************

    /**
     * A TimerTask subclass which will switch to the App Manager after
     * a time out time set.
     */
    private class TimeoutTask extends TimerTask {

        /**
         * Create a new timeout task
         */
        TimeoutTask() { }

        /**
         * Switch to the the App Manager Screen.
         */
        public void run() {
	    display.setCurrent(nextScreen);
        }
    } // TimeoutTask
}
