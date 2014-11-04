/*
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
 
package com.sun.mmedia;

import java.util.Vector;
import com.sun.midp.midlet.*;
import com.sun.midp.main.MIDletSuiteLoader;

/**
 * Tunnel between media and lcdui
 * This is a singleton object per JVM
 */
public final class MediaTunnel {

    /* Playable from background? */
    public final static int PLAYABLE_FROM_BACKGROUND = 1;

    private static MediaTunnel instance;
    private Vector map;
    private boolean hasForeground = true;
    
    private MediaTunnel() {
    }

    /**
     * Is this midlet background playable?
     */
    public boolean isBackPlayable() {
        return false;
    }

    /**
     * Get media tunnel singleton object
     */
    public static MediaTunnel getInstance() {
        if (instance == null) {
            instance =  new MediaTunnel();
        }
        return instance;
    }

    /**
     * Register media event consumer
     * 
     * @retval true     if the current status is in foreground
     * @retval false    if the current status is in background
     */
    public synchronized boolean registerMediaEventConsumer(MediaEventConsumer consumer) {
        if (map == null) {
            map = new Vector(5);
        }
        
        if (false == map.contains(consumer)) {
            map.addElement(consumer);
        }

        return hasForeground;
    }

    /**
     * Register media event consumer
     * 
     * @retval true     if the current status is in foreground
     * @retval false    if the current status is in background
     */
    public synchronized void unregisterMediaEventConsumer(MediaEventConsumer consumer) {
        if (map == null) {
            return;
        }
        
        if (true == map.contains(consumer)) {
            map.removeElement(consumer);
        }
    }

    /**
     * Notify media event consumer about the switch to foreground
     */
    public synchronized void callForegroundEventHandler() {
        if (map == null) {
            return;
        }

        int size = map.size();
        hasForeground = true;
        
        for (int i = 0; i < size; ++i) {
            MediaEventConsumer c = (MediaEventConsumer)map.elementAt(i);
            c.handleMediaForegroundNotify();
        }
    }

    /**
     * Notify media event consumer about the switch to background
     */
    public synchronized void callBackgroundEventHandler() {
        if (map == null) {
          return;
        }

        int size = map.size();
        hasForeground = false;
        
        for (int i = 0; i < size; ++i) {
            MediaEventConsumer c = (MediaEventConsumer)map.elementAt(i);
            c.handleMediaBackgroundNotify();
        }
    }
}
