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
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;

/**
 * Hardware display resource representation.  
 */
public class DisplayDevice {
    
    /*
     * ************* public member variables
     */

    /** Display device state indicating the hardware is enabled and can be used */
    public static final int DISPLAY_DEVICE_ENABLED = 0;

    /** Display device state indicating the hardware is desabled and not visible for the user */
    public static final int DISPLAY_DEVICE_DISABLED = 1;

    /** Display device state indicating the hardware is no more available */

    public static final int DISPLAY_DEVICE_ABSENT = 2;

    /** Bit flag indicating the display supports input events */
    public static final int DISPLAY_DEVICE_SUPPORTS_INPUT_EVENTS = 1;

    /** Bit flag indicating the display supports commands */
    public static final int DISPLAY_DEVICE_SUPPORTS_COMMANDS = 2;

    /** Bit flag indicating the display supports forms */
    public static final int DISPLAY_DEVICE_SUPPORTS_FORMS = 4;

    /** Bit flag indicating the display supports ticker */
    public static final int DISPLAY_DEVICE_SUPPORTS_TICKER = 8;

    /** Bit flag indicating the display supports title */
    public static final int DISPLAY_DEVICE_SUPPORTS_TITLE = 16;

    /** Bit flag indicating the display supports alert */
    public static final int DISPLAY_DEVICE_SUPPORTS_ALERTS = 32;

    /** Bit flag indicating the display supports lists */
    public static final int DISPLAY_DEVICE_SUPPORTS_LISTS = 64;

    /** Bit flag indicating the display supports textboxes */
    public static final int DISPLAY_DEVICE_SUPPORTS_TEXTBOXES = 128;

    /** Bit flag indicating the display supports tabbedpanes */
    public static final int DISPLAY_DEVICE_SUPPORTS_TABBEDPANES = 256;

    /** Bit flag indicating the display supports ileselectors */
    public static final int DISPLAY_DEVICE_SUPPORTS_FILESELECTORS = 512;
    

    
    /*
     * ************* private member variables
     */
    
    private int hardwareId;
    private String displayName;
    private boolean isPrimary = true; 
    private boolean buildInDisp;
    private int capabilities;
    private boolean isPenSupported;
    private boolean isPenMotionSupported;


    private int state = DISPLAY_DEVICE_DISABLED; // display is  disabled by default


    /*
     * ************* public methods
     */
    
    public DisplayDevice(int id) {
	hardwareId = id;
	displayName = getDisplayName0(hardwareId);
	isPrimary = isDisplayPrimary0(hardwareId); 
	buildInDisp = isbuildInDisplay0(hardwareId);
	capabilities = getDisplayCapabilities0(hardwareId);
	isPenSupported = isDisplayPenSupported0(hardwareId);
	isPenMotionSupported = isDisplayPenMotionSupported0(hardwareId);
    }

    /**
     * Get the hardware state 
     * @return state of he display device. Possible values are
     * <code>DISPLAY_DEVICE_ENABLED</code>, <code>DISPLAY_DEVICE_DISABLED</code>,
     * <code>DISPLAY_DEVICE_ABSENT</code>
     */
    public int getState() {
	return state;
    }
    
    /**
     * Set the hardware state 
     * @param1 state of he display device. Possible values are
     * <code>DISPLAY_DEVICE_ENABLED</code>, <code>DISPLAY_DEVICE_DISABLED</code>,
     * <code>DISPLAY_DEVICE_ABSENT</code>
     */
    public void setState(int newState) {
        if (state != newState) {
	        state = newState;
            displayStateChanged0(hardwareId, state);
        }
    }

    /**
     * Get the capabilities of the display device. 
     * @return the display capabilities. The returned integer value may be 
     * any combination of
     * <code> DISPLAY_DEVICE_SUPPORTS_INPUT_EVENTS </code>
     * <code> DISPLAY_DEVICE_SUPPORTS_COMMANDS </code>
     * <code> DISPLAY_DEVICE_SUPPORTS_FORMS </code>
     * <code> DISPLAY_DEVICE_SUPPORTS_TICKER </code>
     * <code> DISPLAY_DEVICE_SUPPORTS_TITLE </code>
     * <code> DISPLAY_DEVICE_SUPPORTS_ALERTS </code>
     * <code> DISPLAY_DEVICE_SUPPORTS_LISTS </code>
     * <code> DISPLAY_DEVICE_SUPPORTS_TEXTBOXES </code>
     * <code> DISPLAY_DEVICE_SUPPORTS_TABBEDPANES </code>
     * <code> DISPLAY_DEVICE_SUPPORTS_FILESELECTORS </code>
     * If 0 is returned just Canvas screen is supported.
     */
    public int getCapabilities() {
	return capabilities;
    }

    /**
     * Device has to support at least one primary Display. Moreover some 
     * secondary displays can be supported. This method returns true if this 
     * display device is primary otherwise false is returned
     * @return true if the display is primary , false - otherwise 
     */
    public boolean isPrimaryDisplay() {
	return isPrimary;
    }
    

    /** 
     * Check if the display is build-in. 
     * @return true if the display is build-in, otherwise - false.
     */
    public boolean isBuildIn() {
	return buildInDisp;
    }
    
    /** 
     * Check if the display supports pointer events.
     * @return true if the display supports pointer events, otherwise - false.
     */
    public boolean hasPointerEvents() {
	return isPenSupported;
    }

    /** 
     * Check if the display supports pointer motion events.
     * @return true if the display supports pointer motion events, otherwise - false.
     */
    public boolean hasPointerMotionEvents() {
	return isPenMotionSupported;
    }
    
    /** 
     * Get display device name 
     * @return Display device name
     */
    public String getDisplayDeviceName() {
	return displayName;
    }

    /** 
     * Get unique display hardware id 
     * @return Display device id
     */
    public int getHardwareId() {
	return hardwareId;
    }

    /** 
     * Get curent display device width 
     * @return Display device width
     */
    public int getWidth() {
	return getScreenWidth0(hardwareId);
    }

    /** 
     * Get curent display device height 
     * @return Display device height
     */
    public int getHeight() {
	return getScreenHeight0(hardwareId);
    }


    /** 
     * Get the current reverse orientation flag 
     * @return true or false depending on the screen orientation
     */
    public boolean getReverseOrientation() {
	return getReverseOrientation0(hardwareId);
    }

    /** 
     * Invert screen orientation
     * @return true or false depending on the screen orientation 
     */
    public boolean reverseOrientation() {
	return reverseOrientation0(hardwareId);
    }

      /** 
     * Handle clamshell event.
     */
    public void clamshellHandling() {
	clamshellHandling0();
    }

    /**
     * Redraw a portion of the display.
     *
     * @param displayId The display ID associated with this Display
     * @param x1 upper left corner x-coordinate
     * @param y1 upper left corner y-coordinate
     * @param x2 lower right corner x-coordinate
     * @param y2 lower right corner y-coordinat
     */
    public void refresh(int displayId,
				int x1, int y1, int x2, int y2) {
	refresh0(hardwareId, displayId, x1, y1, x2, y2); 
    }

    /**
     * Sets full screen on the device.
     * @param mode The new screen size mode to be set. True if all
     *             available space should be given to content; false -
     *             some areas can be reserved for ticker, title, commands.
     */
    public void setFullScreen(int displayId, boolean mode) {
	setFullScreen0(hardwareId, displayId, mode);
    }

    /**
     * Flushes the offscreen buffer directly to the device screen.
     * The size of the buffer flushed is defined by offscreen buffer width
     * and passed in height. 
     * Offscreen_buffer must be aligned to the top-left of the screen and
     * its width must be the same as the device screen width.
     * @param graphics The Graphics instance associated with the screen.
     * @param offscreen_buffer The offscreen buffer to be flushed
     * @param height The height to be flushed
     * @return true if direct_flush was successful, false - otherwise
     */
    public boolean directFlush(Graphics graphics, 
				Image offscreen_buffer, int height) {
	return directFlush0(hardwareId, graphics, offscreen_buffer, height);
    }
 
    /**
     * Resets native resources on a foreground change
     *
     * @param displayId The display ID associated with this Display
     */
    public void gainedForeground(int displayId) {
	gainedForeground0(hardwareId, displayId);
    }

    public String toString() {
	return "hardwareId: " + hardwareId +
	    " displayName: " + displayName +
	    " isPrimary: " +  isPrimary +
	    " buildInDisp:" +buildInDisp +
            " capabilities:"+capabilities +
	    " isPenSupported:"+ isPenSupported+
            " isPenMotionSupported:"+isPenMotionSupported+
	    " state:" +state;

    }

    private native int getScreenWidth0(int hardwareId);
    private native int getScreenHeight0(int hardwareId);
    private native boolean getReverseOrientation0(int hardwareId);
    private native boolean reverseOrientation0(int hardwareId);
    private native void refresh0(int hardwareId, int displayId,
                                 int x1, int y1, int x2, int y2);
    private native void setFullScreen0(int hardwareId, int displayId, boolean mode);
    private native boolean directFlush0(int hardwareId, Graphics graphics, 
					Image offscreen_buffer, int height);
    private native void gainedForeground0(int hardwareId, int displayId);


    private native String getDisplayName0(int hardwareId);
    private native boolean isDisplayPrimary0(int hardwareId); 
    private native boolean isbuildInDisplay0(int hardwareId);
    private native int getDisplayCapabilities0(int hardwareId);
    private native boolean isDisplayPenSupported0(int hardwareId);
    private native boolean isDisplayPenMotionSupported0(int hardwareId);
    private native void displayStateChanged0(int hardwareId, int state);
    private native void clamshellHandling0();

}

