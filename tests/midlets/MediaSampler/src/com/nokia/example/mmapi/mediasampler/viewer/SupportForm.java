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
import javax.microedition.media.Manager;

import com.nokia.example.mmapi.mediasampler.MediaSamplerMIDlet;

/**
 * View that outputs MM API's System properties to a Form.
 */
public class SupportForm extends Form implements CommandListener {

    private MediaSamplerMIDlet midlet;
    private Displayable medialist;
    private Command backCmd = new Command("Back", Command.BACK, 1);

    public SupportForm(MediaSamplerMIDlet midlet, Displayable medialist) {
        super("MM API support check");
        this.midlet = midlet;
        this.medialist = medialist;
        addCommand(backCmd);
        setCommandListener(this);
        init();
    }

    private void init() {
        String apiVersion = System.getProperty("microedition.media.version");
        append("MM API version:" + apiVersion + "\n");
        append("Mixing supported: " + System.getProperty("supports.mixing") + "\n");
        append("Audio capture supported: " + System.getProperty("supports.audio.capture") + "\n");
        append("Video capture supported: " + System.getProperty("supports.video.capture") + "\n");
        append("Recording supported: " + System.getProperty("supports.recording") + "\n");
        append("Supported audio encodings: " + System.getProperty("audio.encodings") + "\n");
        append("Supported video encodings: " + System.getProperty("video.encodings") + "\n");
        append("Supported video snaphot encodings: " + System.getProperty("video.snapshot.encodings") + "\n");
        append("\n");
        String streamable = System.getProperty("streamable.contents");
        if (streamable == null) {
            append("Streaming: not supported.\n");
        } else {
            append("Streamable contents: " + streamable);
            String[] rtp = Manager.getSupportedContentTypes("rtp");
            if (rtp != null && rtp.length > 0) {
                append("RTP protocol supported.");
            }
            String rtsp[] = Manager.getSupportedContentTypes("rtsp");
            if (rtsp != null && rtsp.length > 0) {
                append("RTSP protocol supported.");
            }
        }
        String[] contentTypes = Manager.getSupportedContentTypes(null);
        if (contentTypes != null) {
            append("\n\nAll supported content types:\n");
            for (int i = 0; i < contentTypes.length; i++) {
                append(contentTypes[i] + "\n");
            }
        }
    }

    public void commandAction(Command cmd, Displayable d) {
        Display.getDisplay(midlet).setCurrent(medialist);
    }
}
