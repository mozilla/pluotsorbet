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
package com.sun.midp.lcdui;

import javax.microedition.lcdui.Display;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class that allows Display to access 
 * Device specific calls in the native code
 * like flashBacklight.
 */

public class DisplayDeviceAccess {

    /** 
     * The Timer to service TimerTasks. 
     */
    private static Timer timerService = new Timer();

    /** 
     * A TimerTask for backlight. 
     */
    private TimerTask task = null;

    /** 
     * A TimerTask for vibrate.
     */
    private TimerTask vibrateTask = null;

    /**
     * The interval, in microseconds between backlight 
     * toggles (in microseconds)
     */
    private static int BLINK_RATE = 250;

    /**
     * Number of repetitions left in flash duration
     */
    private int flashCount = 0;

    /**
     * We always want this to be false at the end of 
     * a flashBacklight call.
     */
    private boolean isLit = false;


    /**
     * Requests a flashing effect for the device's backlight.
     *
     * @param displayId The display ID associated with this Display
     * @param duration the number of milliseconds the backlight should be 
     * flashed, or zero if the flashing should be stopped
     *
     * @return true if the backlight can be controlled
     */
    public synchronized boolean flashBacklight(int displayId, int duration) {

        // Test for negative of duration is in public class

        if (duration == 0) {
            cancelTimer();
            if (isLit) {
                isLit = !isLit;
                return toggleBacklight0(displayId);
            } else {
                return isBacklightSupported0(displayId);
            }
        } else {
            setTimer(displayId, duration);
            isLit = !isLit;
            return toggleBacklight0(displayId);
        }
    }
    
    /**
     * Set a new timer.  Determine <code>flashCount</code> based on
     * <code>duration</code> divided by <code>BLINK_RATE</code>
     *
     * @param displayId The display ID associated with this Display
     * @param duration the number of milliseconds the timer should be run
     */
    private void setTimer(int displayId, int duration) {
        cancelTimer();
        try {
            task = new TimerClient(displayId);
            /* flash every <tt>BLINK_RATE</tt> miliseconds */
            flashCount = duration / BLINK_RATE;
            timerService.schedule(task, BLINK_RATE, BLINK_RATE);
        } catch (IllegalStateException e) {
            cancelTimer();
        }
    }

    /**
     * Cancel any running Timer.
     */
    private void cancelTimer() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        flashCount = 0;
    }

    /**
     * Inner class TimerTask
     *
     * Used to toggle the backlight and turn it off when
     * the duration of the timer is up
     */
    class TimerClient extends TimerTask {
 
        /**
         * Creates TimerClient to show backlight for 
         * Display with passed displayId.
         *
         * @param displayId The display ID associated with the caller Display
         */
        TimerClient(int displayId) {
            this.displayId = displayId;
        }

        /**
         * If there are flashes left to perform, 
         * simply toggle the backlight.
         */
        public final void run() {
            if (flashCount > 0) {
                flashCount--;
                isLit = !isLit;
                toggleBacklight0(displayId);
            } else {
                if (isLit) {
                    isLit = !isLit;
                    toggleBacklight0(displayId);
                }
                this.cancel();
            }
        }
        /** The display ID associated with the caller Display */
        private int displayId;
    }

    /**
     * Toggles backlight.
     *  
     * @param displayId The display ID associated with the caller Display
     * @return true if backlight control is supported, false otherwise        
     */
    private native boolean toggleBacklight0(int displayId);

    /**
     * Tests if backlight is supported.
     *  
     * @param displayId The display ID associated with the caller Display
     * @return true if backlight control is supported, false otherwise        
     */
    private native boolean isBacklightSupported0(int displayId);

     /**
      * Requests for the device's vibrating.
      *
      * @param displayId The display ID associated with this Display
      * @param duration the number of milliseconds the vibration should be  
      * on, or zero if the flashing should be stopped
      *
      * @return true if the vibration can be controlled
      */
     public synchronized boolean vibrate(int displayId, int duration) {
 
         // Test for negative of duration is in public class
 
         if (duration == 0) {
             cancelVibrateTimer();
             return vibrate0(displayId, false);
         } else {
             setVibrateTimer(displayId, duration);
             return vibrate0(displayId, true);
         }
     }
 
     /**
      * Set a new vibration timer.  
      * The timerTask will be executed after <code>duration</code> milliseconds.
      * 
      * @param displayId The display ID associated with this Display
      * @param duration the number of milliseconds the timer should be run
      */
     private void setVibrateTimer(int displayId, int duration) {
         cancelVibrateTimer();
         try {
             vibrateTask = new VibrateTimerClient(displayId);
             timerService.schedule(vibrateTask, duration);
         } catch (IllegalStateException e) {
             cancelVibrateTimer();
         }
     }
 
     /**
      * Cancel any running vibration Timer.
      */ 
     private void cancelVibrateTimer() {
         if (vibrateTask != null) {
             vibrateTask.cancel();
             vibrateTask = null;
         }
     }
 
     /**
      * Inner class TimerTask
      *
      * Used to stop the device's vibration when
      * the duration of the timer is up
      */
     class VibrateTimerClient extends TimerTask {
  
         /**
          * Creates VibrateTimerClient to stop vibration for 
          * Display with passed displayId.
          *
          * @param displayId The display ID associated with the caller Display
          */
         VibrateTimerClient(int displayId) {
             this.displayId = displayId;
         }
 
         /**
          * simply stop the vibration.
          *
          */
         public final void run() {
             vibrate0(displayId, false);
             this.cancel();
         }
         /** The display ID associated with the caller Display */
         private int displayId;
     }
 
     /**
      * Show vibration.  Turn it on or  turn it off.
      *  
      * @param displayId The display ID associated with the caller Display
      * @param turnVibrateOn true to turn on the vibration, 
      *             or false to turn off it.
      * @return true if vibration control is supported, false otherwise
      */ 
     private native boolean vibrate0(int displayId, boolean turnVibrateOn);
}
