/*
 * Copyright © 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */ 
package com.nokia.example.mmapi.mediasampler.viewer;

import javax.microedition.lcdui.*;

import com.nokia.example.mmapi.mediasampler.MediaSamplerMIDlet;

/**
 * Media list is the main view in the MIDlet. From this view the user may swich
 * to other views.
 */
public class MediaList extends List implements CommandListener {

    private final MediaSamplerMIDlet midlet;
    private final VideoSourceSelector sourceSelector;
    protected AudioCanvas audioCanvas;
    private SupportForm form;
    private final Command exitCommand;

    public MediaList(MediaSamplerMIDlet midlet) {
        super("Media Sampler", IMPLICIT);
        this.midlet = midlet;
        sourceSelector = new VideoSourceSelector(midlet, this);
        append("Play audio", null);
        append("Play video", null);
        append("Check MM API support", null);
        exitCommand = new Command("Exit", Command.EXIT, 1);
        addCommand(exitCommand);
        setCommandListener(this);
    }

    /**
     * Release all resources loaded by sub components.
     */
    public void releaseResources() {
        if (audioCanvas != null) {
            audioCanvas.releaseResources();
        }
    }

    /**
     * Implemented CommandListener method.
     * 
     * Sets the selected Example visible
     */
    public void commandAction(Command cmd, Displayable disp) {
        if (cmd.getCommandType() == Command.SCREEN) {
            int index = getSelectedIndex();
            if (index != -1) { // -1 means nothing selected
                String selected = getString(index);
                if (selected.equals("Play video")) {
                    Display.getDisplay(midlet).setCurrent(sourceSelector);
                } else if (selected.equals("Play audio")) {
                    if (audioCanvas == null) {
                        audioCanvas = new AudioCanvas(midlet, this, 0);
                    }else{
                        audioCanvas.initSounds();
                    }
                    Display.getDisplay(midlet).setCurrent(audioCanvas);
                } else if (selected.equals("Check MM API support")) {
                    if (form == null) {
                        form = new SupportForm(midlet, this);
                    }
                    Display.getDisplay(midlet).setCurrent(form);
                }
            }
        } else if (cmd == exitCommand) {
            midlet.notifyDestroyed();
        }
    }

    public void updateVolume() {
        audioCanvas.repaint();
        audioCanvas.serviceRepaints();
    }
}
