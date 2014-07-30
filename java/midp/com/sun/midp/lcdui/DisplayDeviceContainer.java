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

import java.util.Vector;

/**
 * Stores array of hardware displays 
 */
public class DisplayDeviceContainer {

    /** vector of display devices */
    private Vector displayList = new Vector(2, 2);;
    

    public DisplayDeviceContainer() {
	int[] ids = getDisplayDevicesIds0();	
	for (int i = 0; i < ids.length; i++) {
	    addDisplayDevice(new DisplayDevice(ids[i]));
	}
    }

    /** 
     * Get the array of the display devices
     * @return list of the display devices
     */
    public synchronized DisplayDevice[] getDisplayDevices() {
	DisplayDevice[] a = new DisplayDevice[displayList.size()];
	displayList.copyInto(a);
	return a;
    } 

    /** 
     * Get the display device by hardwareId
     * @return list of the display devices
     */
    public synchronized DisplayDevice getDisplayDeviceById(int hardwareId) {
	DisplayDevice display = null;
	for (int i = 0; i < displayList.size(); i++) {
	    DisplayDevice current = (DisplayDevice)displayList.elementAt(i);
	    if (current.getHardwareId() == hardwareId) {
		display = current;
		break;
	    }
	}
	return display;
    } 

    /**
     * Get the primary display device
     * @return primary display device
     */
    public synchronized DisplayDevice getPrimaryDisplayDevice() {
	DisplayDevice primaryDisplay = null;
	for (int i = 0; i < displayList.size(); i++) {
	    DisplayDevice current = (DisplayDevice)displayList.elementAt(i);
	    if (current.isPrimaryDisplay()) {
		primaryDisplay = current;
		break;                          
	    }
	}
	return primaryDisplay;
    } 


    /** 
     * Add new display device into the list 
     * @param dd Display device
     */
    public synchronized void addDisplayDevice(DisplayDevice dd) {
	if (dd.isPrimaryDisplay()) {
	    // add the primary display at the head of the list
	    displayList.insertElementAt(dd, 0);
	} else {
	    // add the primary display at the tail of the list
	    displayList.addElement(dd);
	}
    }

    /** 
     * Remove display devices from the list by id
     * @param hardwareId id of  display device
     * @return true if at least one display device has been removed from the list, otherwise - false
     */
     public synchronized boolean removeDisplayDevice(int hardwareId) {
	 boolean ret = false; 
	 
	 for (int i = displayList.size(); --i >= 0;) {
	     int id = ((DisplayDevice)displayList.elementAt(i)).getHardwareId();
	     if (id == hardwareId) { 
		 displayList.removeElementAt(i);
		 ret = true;
		 break;
	     }
	 }
	 return ret;
     }

    private native int[] getDisplayDevicesIds0();
}

