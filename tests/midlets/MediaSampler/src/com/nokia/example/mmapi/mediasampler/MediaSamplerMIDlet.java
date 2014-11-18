/*
 * Copyright © 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */ 
package com.nokia.example.mmapi.mediasampler;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

import com.nokia.example.mmapi.mediasampler.data.MediaFactory;
import com.nokia.example.mmapi.mediasampler.viewer.MediaList;

/**
 * MMAPI example MIDlet class.
 */
public class MediaSamplerMIDlet extends MIDlet {

    private final MediaList list;
    public int globalVolume = 0;
    public int actualVolume = 0;
    public int midletVolume = 100;
    public String eventString = "";

    public MediaSamplerMIDlet() {
        MediaFactory.setMidlet(this);
        list = new MediaList(this);
    }

    /**
     * Overriden MIDlet method
     */
    public void startApp() {
        Displayable current = Display.getDisplay(this).getCurrent();
        if (current == null) {
            // first call
            Display.getDisplay(this).setCurrent(list);
        } else {
            Display.getDisplay(this).setCurrent(current);
        }
    }

    /**
     * Overriden MIDlet method
     */
    public void pauseApp() {
    }

    /**
     * Overriden MIDlet method.
     */
    public void destroyApp(boolean unconditional) {
        list.releaseResources();
    }

    /**
     * Displays an error message in Alert.
     * 
     * @param message
     *            String as message to display.
     */
    public void alertError(String message) {
        Alert alert = new Alert("Error", message, null, AlertType.ERROR);
        Display display = Display.getDisplay(this);
        Displayable current = display.getCurrent();
        if (!(current instanceof Alert)) {
            // This next call can't be done when current is an Alert
            display.setCurrent(alert, current);
        }
    }

    public void updateVolume() {
        list.updateVolume();
    }
}
