package com.sun.amms.directcontrol;

import javax.microedition.media.control.*;
import com.nokia.mid.media.AudioOutputControl;
import com.nokia.mid.media.AudioOutput;

class AudioOutputImpl implements AudioOutput {
    int outputMode;

    AudioOutputImpl(int outputMode) {
        this.outputMode = outputMode;
    }

    public int getActiveOutputMode() {
        System.out.println("AudioOutputImpl::getActiveOutputMode() not implemented");
        return this.outputMode;
    }

    public int[] getOutputDevices() {
        System.out.println("AudioOutputImpl::getOutputDevices() not implemented");
        return null;
    }
}

public class DirectAudioOutputControl extends DirectAMMSControl implements AudioOutputControl {
    int outputMode = AudioOutputControl.DEFAULT;

    public int[] getAvailableOutputModes() {
        System.out.println("DirectAudioOutputControl::getAvailableOutputModes() not implemented");
        return new int[] {
            AudioOutputControl.DEFAULT,
            AudioOutputControl.ALL,
            AudioOutputControl.NONE,
            AudioOutputControl.PRIVATE,
            AudioOutputControl.PUBLIC,
        };
    }

    public int getOutputMode() {
        System.out.println("DirectAudioOutputControl::getOutputMode() not implemented");
        return outputMode;
    }

    public AudioOutput getCurrent() {
        System.out.println("DirectAudioOutputControl::getCurrent() not implemented");
        return new AudioOutputImpl(outputMode);
    }

    public int setOutputMode(int mode) {
        System.out.println("DirectAudioOutputControl::setOutputMode(" + mode + ") not implemented");
        outputMode = mode;
        return outputMode;
    }
}
