/*
 * Copyright © 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */ 
package com.nokia.example.mmapi.mediasampler.viewer;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;

import com.nokia.example.mmapi.mediasampler.MediaSamplerMIDlet;
import com.nokia.example.mmapi.mediasampler.data.MediaFactory;

/**
 * List that allow user to select Video source.
 */
public class VideoSourceSelector extends List implements CommandListener {

    private MediaSamplerMIDlet midlet;
    private List returnList;
    private HTTPUrlForm urlForm;
    private Command backCommand = new Command("Back", Command.BACK, 1);

    public VideoSourceSelector(MediaSamplerMIDlet midlet, MediaList list) {
        super("Select Video Source", List.IMPLICIT);
        this.midlet = midlet;
        this.returnList = list;
        urlForm = new HTTPUrlForm();
        append("From http", null);
        append("From jar", null);
        addCommand(backCommand);
        setCommandListener(this);
    }

    public void commandAction(Command cmd, Displayable d) {
        if (cmd == SELECT_COMMAND) {
            int selection = getSelectedIndex();
            if (selection == 0) { // URL source selected
                Display.getDisplay(midlet).setCurrent(urlForm);
            } else if (selection == 1) { // JAR source selected
                // File name returned by the MediaFactory refers to the
                // "Video-Clip" application property...
                String videoFile = MediaFactory.getDefaultVideo().getFile();
                commitSelection(videoFile);
            }
        } else if (cmd == backCommand) {
            Display.getDisplay(midlet).setCurrent(returnList);
        }
    }

    /**
     * Initializes and set visible the video canvas with the selected video
     * source.
     * 
     * @param input
     *            String as video http url or file path.
     */
    private void commitSelection(String url) {
        try {
            VideoCanvas canvas = new VideoCanvas(midlet, returnList, url);
            canvas.prepareToPlay();
            Display.getDisplay(midlet).setCurrent(canvas);
        } catch (Exception e) {
            midlet.alertError("Cannot open connection: " + e.getMessage());
        }
    }

    /**
     * Form for the video URL input.
     */
    public class HTTPUrlForm extends Form implements CommandListener {

        TextField tf = new TextField("URL", "http://", 100, TextField.URL);
        Command cmdOK = new Command("OK", Command.OK, 1);
        Command cmdBack = new Command("Back", Command.BACK, 1);

        public HTTPUrlForm() {
            super("HTTP Address");
            String defaultURL = VideoSourceSelector.this.midlet.getAppProperty("Video-URL");
            if (defaultURL != null && defaultURL.length() > 0) {
                tf.setString(defaultURL);
            }
            append(tf);
            addCommand(cmdOK);
            addCommand(cmdBack);
            setCommandListener(this);
        }

        public void commandAction(Command cmd, Displayable d) {
            if (cmd == cmdOK) {
                String url = tf.getString();
                commitSelection(url);
            } else {
                Display.getDisplay(midlet).setCurrent(VideoSourceSelector.this);
            }
        }
    }
}
