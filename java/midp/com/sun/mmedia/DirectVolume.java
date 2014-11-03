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

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;
import javax.microedition.media.*;
import javax.microedition.media.control.*;

/**
 * Native volume control implementation
 */
public class DirectVolume implements VolumeControl {

    // VolumeControl functions
    protected native int nGetVolume(int hNative);
    protected native int nSetVolume(int hNative, int level);
    protected native boolean nIsMuted(int hNative);
    protected native boolean nSetMute(int hNative, boolean mute);

    private int _level = -1;
    private int _mute = -1;
    private int _hNative;
    private BasicPlayer _player;

    DirectVolume(BasicPlayer player, int hNative) {
        _player = player;
        _hNative = hNative;
    }
    
    void setToThisPlayerLevel() {
        if (_level == -1)
            return;
        if (_hNative != 0)
            nSetVolume(_hNative, _level);
    }

    void setToPlayerMute() {
        if (_mute == -1)
            return;
        if (_hNative != 0)
            nSetMute(_hNative, _mute == 1);
    }

    void playerClosed() {
    	_hNative = 0;
    }

    public int getLevel() {
        if (_hNative != 0 && _level == -1) {
            _level = nGetVolume(_hNative);
        }
        return _level;
    }

    public int setLevel(int level) {
        if (level < 0) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_MMAPI, 
                    "volume level " + level + " is negative, change to 0");
            }
            level = 0;
        } else if (level > 100) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_MMAPI, 
                    "volume level " + level + " is too big, change to 100");
            }
            level = 100;
        }

        // Volume value is the same or player is closed. Just return.
        if (_level == level || _hNative == 0) {
            return _level;
        }

	// Try to set the native player volume 
	if (-1 == nSetVolume(_hNative, level)) {
	    if (Logging.REPORT_LEVEL <= Logging.ERROR) {
		Logging.report(Logging.ERROR, LogChannels.LC_MMAPI, 
		    "set volume failed volume=" + _level);
	    }
	}

        _level = level;
        _player.sendEvent(PlayerListener.VOLUME_CHANGED, this);
        
        return _level;
    }

    public boolean isMuted() {
        if (_hNative == 0 || _mute != -1)
            return (_mute == 1);
        if (nIsMuted(_hNative)) {
            _mute = 1;
            return true;
        } else {
            _mute = 0;
            return false;
        }
    }

    public void setMute(boolean mute) {
        if (_hNative != 0) {
            nSetMute(_hNative, mute);
        }
        _mute = mute ? 1 : 0;
    }
}
