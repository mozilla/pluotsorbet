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

import javax.microedition.media.*;
import javax.microedition.media.control.*;

/**
 * Implement all of MIDI related controls like :
 *     MIDIControl, RateControl, PitchControl, TempoControl
 */
class DirectMIDIControl implements DirectControls {

    // MIDIControl
    private native void nSetChannelVolume(int handle, int channel, int volume);
    private native int nGetChannelVolume(int handle, int channel);
    private native void nSetProgram(int handle, int channel, int bank, int program);
    private native void nShortMidiEvent(int handle, int type, int data1, int data2);
    private native int nLongMidiEvent(int handle, byte[] data, int offset, int length);

    // RateControl
    private native int nGetMaxRate(int handle);
    private native int nGetMinRate(int handle);
    private native int nSetRate(int handle, int rate);
    private native int nGetRate(int handle);

    // PitchControl
    private native int nGetMaxPitch(int handle);
    private native int nGetMinPitch(int handle);
    private native int nSetPitch(int handle, int pitch);
    private native int nGetPitch(int handle);

    // TempoControl
    private native int nGetTempo(int handle);
    private native int nSetTempo(int handle, int tempo);

    // Bank Query
    private native boolean nIsBankQuerySupported(int handle);
    private native int nGetBankList(int handle, boolean custom, int[] list);
    private native int nGetKeyName(int handle, int bank, int prog, int key, byte[] keyname);
    private native int nGetProgramName(int handle, int bank, int prog, byte[] progname);
    private native int nGetProgramList(int handle, int bank, int[] proglist);
    private native int nGetProgram(int handle, int channel, int[] program);

    DirectMIDIControl(DirectPlayer p) {
        _player = p;
    }

    // Private //////////////////////////////////////////////////////////

    private void checkState() {
        if (_player.state < Player.PREFETCHED) {
            throw new IllegalStateException("Not prefetched");
        }
    }

    private void checkChannel(int channel) {
        if (channel < 0 || channel > 15) {
            throw new IllegalArgumentException("Channel is out of range");
        }
    }

    private void checkVolume(int volume) {
        if (volume < 0 || volume > 127) {
            throw new IllegalArgumentException("Volume is out of range");
        }
    }

    private void checkType(int type) {
        if (type < 0x80 || type > 0xFF || type == 0xF0 || type == 0xF7) {
            throw new IllegalArgumentException("Type is out of range");
        }
    }

    private void checkData(int data) {
        if (data < 0 || data > 127) {
            throw new IllegalArgumentException("Data is out of range");
        }
    }

    private void checkLongMidiEvent(byte[] data, int offset, int length) {
        if (data == null) {
            throw new IllegalArgumentException("Data is null for long MIDI event ");
        } else if ((offset >= data.length) || (offset < 0)) {
            if (data.length != 0) {
                throw new IllegalArgumentException("Invalid offset for long MIDI event");
            }
        } else if ((length > data.length) || (length < 0)) {
            throw new IllegalArgumentException("Length is out of range for long MIDI event");
        }
    }

    private void checkProgram(int program) {
        if (program < 0 || program > 127) {
            throw new IllegalArgumentException("Program is out of range");
        }
    }
    private void checkBank(int bank) {
        if (bank < 0 || bank > 16383) {
            throw new IllegalArgumentException("Bank is out of range");
        }
    }
    // DirectControls ////////////////////////////////////////////////////

    public synchronized void playerStarted() {
        if (_cachedRate != -1) {
            // NOTE - MMAPI SPEC is not clear about this
            // set cached rate at enter playing state
            nSetRate(_player.hNative, _cachedRate);
            _cachedRate = -1;
        }
    }

    public synchronized void playerStopped() {
    }

    public synchronized void playerClosed() {
        _player = null;
    }

    // MIDIControl ///////////////////////////////////////////////////////

    private MIDIControlImpl midiCtl = null;

    public synchronized MIDIControl getMIDIControl() {
        if (null == midiCtl) {
            midiCtl = new MIDIControlImpl();
        }
        return midiCtl;
    }

    class MIDIControlImpl implements MIDIControl {
        public synchronized boolean isBankQuerySupported() {
            return nIsBankQuerySupported(_player.hNative);
        }

        public synchronized int[] getProgram(int channel) throws MediaException {

            checkState();
            checkChannel(channel);

            int[] p = new int[2];

            int len = nGetProgram(_player.hNative, channel, p);
            if(len < 0) throw new MediaException("GetProgram failure");

            return p;
        }

        public synchronized int[] getBankList(boolean custom) throws MediaException {

            checkState();

            int[] bl1 = new int[20];

            int len = nGetBankList(_player.hNative, custom, bl1);
            if(len < 0) throw new MediaException("BankList failure");

            int[] bl2 = new int[len];

            for(int i=0; i<len; i++)
                bl2[i] = bl1[i];

            return bl2;
        }

        public synchronized String getKeyName(int bank, int prog, int key) throws MediaException {

            checkState();
            checkBank(bank);
            checkProgram(prog);

            if((key < 0) || (key > 127))
                throw new IllegalArgumentException("key out of range");

            byte[] str = new byte[64];

            int len = nGetKeyName(_player.hNative, bank, prog, key, str);
            if(len < 0) throw new MediaException("KeyName failure");

            return new String(str, 0, len);
        }

        public synchronized String getProgramName(int bank, int prog) throws MediaException {

            checkState();
            checkBank(bank);
            checkProgram(prog);

            byte[] str = new byte[64];

            int len = nGetProgramName(_player.hNative, bank, prog, str);
            if(len < 0) throw new MediaException("ProgramName failure");

            return new String(str, 0, len);
        }

        public synchronized int[] getProgramList(int bank) throws MediaException {

            checkState();
            checkBank(bank);

            int[] pl1 = new int[200];

            int len = nGetProgramList(_player.hNative, bank, pl1);
            if(len < 0) throw new MediaException("ProgramList failure");

            int[] pl2 = new int[len];

            for(int i=0; i<len; i++)
                pl2[i] = pl1[i];

            return pl2;
        }

        public synchronized void setChannelVolume(int channel, int volume) {
            checkState();
                    checkChannel(channel);
                    checkVolume(volume);

            if (_player != null && _player.hNative != 0) {
                nSetChannelVolume(_player.hNative, channel, volume);
            }
        }

        public synchronized int getChannelVolume(int channel) {
            checkState();
            checkChannel(channel);

            if (_player != null && _player.hNative != 0) {
                return nGetChannelVolume(_player.hNative, channel);
            } else {
                return -1;
            }
        }

        public synchronized void setProgram(int channel, int bank, int program) {
            checkState();
            checkChannel(channel);

            if(bank != -1) checkBank(bank);
            checkProgram(program);

            if (_player != null && _player.hNative != 0) {
                nSetProgram(_player.hNative, channel, bank, program);
            }
        }

        public synchronized void shortMidiEvent(int type, int data1, int data2) {
            checkState();
            checkType(type);
            checkData(data1);
            checkData(data2);

            if (_player != null && _player.hNative != 0) {
                nShortMidiEvent(_player.hNative, type, data1, data2);
            }
        }

        public synchronized int longMidiEvent(byte[] data, int offset, int length) {
            checkState();
            checkLongMidiEvent(data, offset, length);

            if (_player != null && _player.hNative != 0) {
                return nLongMidiEvent(_player.hNative, data, offset, length);
            } else {
                return -1;
            }
        }
    }

    // RateControl ///////////////////////////////////////////////////////

    private RateControlImpl rateCtl = null;

    public synchronized RateControl getRateControl() {
        if (null == rateCtl) {
            rateCtl = new RateControlImpl();
        }
        return rateCtl;
    }

    class RateControlImpl implements RateControl {
        //
        // SPEC:
        //     If the Player is already started, setRate will immediately take effect.
        //    Q - How about the other case?
        //
        public synchronized int setRate(int millirate) {
            if (_player == null)
                return 0;

            int max = getMaxRate();
            int min = getMinRate();

            if (millirate > max) {
                    millirate = max;
            } else if (millirate < min) {
                    millirate = min;
            }

            recalculateStopTime();
            if (_player.state >= Player.STARTED) {
                    _cachedRate = -1;
                    return nSetRate(_player.hNative, millirate);
            } else {
                    _cachedRate = millirate;
                    return millirate;
            }
        }

        public synchronized int getRate() {
            if (_player != null && _player.hNative != 0) {
                return _cachedRate == -1 ? nGetRate(_player.hNative) : _cachedRate;
            } else {
                return 0;
            }
        }

        public synchronized int getMaxRate() {
            if (_player != null && _player.hNative != 0) {
                return nGetMaxRate(_player.hNative);
            } else {
                return 0;
            }
        }

        public synchronized int getMinRate() {
            if (_player != null && _player.hNative != 0) {
                return nGetMinRate(_player.hNative);
            } else {
                return 0;
            }
        }
    }

    // PitchControl //////////////////////////////////////////////////////

    private PitchControlImpl pitchCtl = null;

    public synchronized PitchControl getPitchControl() {
        if (null == pitchCtl) {
            pitchCtl = new PitchControlImpl();
        }
        return pitchCtl;
    }

    class PitchControlImpl implements PitchControl {
        public synchronized int setPitch(int millisemitones) {
            int max = getMaxPitch();
            int min = getMinPitch();

            if (millisemitones > max) {
                millisemitones = max;
            } else if (millisemitones < min) {
                millisemitones = min;
            }

            if (_player != null && _player.hNative != 0) {
                return nSetPitch(_player.hNative, millisemitones);
            } else {
                return 0;
            }
        }

        public synchronized int getPitch() {
            if (_player != null && _player.hNative != 0) {
                return nGetPitch(_player.hNative);
            } else {
                return 0;
            }
        }

        public synchronized int getMaxPitch() {
            if (_player != null && _player.hNative != 0) {
                return nGetMaxPitch(_player.hNative);
            } else {
                return 0;
            }
        }

        public synchronized int getMinPitch() {
            if (_player != null && _player.hNative != 0) {
                return nGetMinPitch(_player.hNative);
            } else {
                return 0;
            }
        }
    }

    // TempoControl //////////////////////////////////////////////////////

    private TempoControlImpl tempoCtl = null;

    public synchronized TempoControl getTempoControl() {
        if (null == tempoCtl) {
            tempoCtl = new TempoControlImpl();
        }
        return tempoCtl;
    }

    class TempoControlImpl extends RateControlImpl implements TempoControl {
        public synchronized int setTempo(int millitempo) {
            if (millitempo < 0) {
                millitempo = 0;
            }

            recalculateStopTime();
            if (_player != null && _player.hNative != 0) {
                return nSetTempo(_player.hNative, millitempo);
            } else {
                return 0;
            }
        }

        public synchronized int getTempo() {
            if (_player != null &&  _player.hNative != 0) {
                return nGetTempo(_player.hNative);
            } else {
                return 0;
            }
        }
    }

    //////////////////////////////////////////////////////////////////////
    private void recalculateStopTime() {
        if (_player != null) {
            long stopTime = _player.getStopTime();
            if (stopTime != StopTimeControl.RESET) {
                _player.setStopTime(StopTimeControl.RESET);
                _player.setStopTime(stopTime);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////

    private DirectPlayer _player;
    private int _cachedRate = -1;
}
