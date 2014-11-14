/*
 * Copyright © 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */ 
package com.nokia.example.mmapi.mediasampler.data;

import javax.microedition.media.control.ToneControl;
import javax.microedition.midlet.MIDlet;

/**
 * MediaFactory is a provides class for medias.
 */
public class MediaFactory {

    private static boolean soundInitDone = false;
    private static boolean videoInitDone = false;
    private static Media[] soundMedias;
    private static Media[] videoMedias;
    private static MIDlet mlet;

    public static Media getDefaultARM() {
        return getSoundMedias()[1];
    }

    public static Media getDefaultMidi() {
        return getSoundMedias()[0];
    }

    public static Media getDefaultXWav() {
        return getSoundMedias()[3];
    }

    public static Media getDefaultWav() {
        return getSoundMedias()[2];
    }

    public static Media getDefaultVideo() {
        return getVideoMedias()[0];
    }

    public static void setMidlet(MIDlet midlet) {
        mlet = midlet;
    }

    public static Media[] getAllMedias() {
        return null;
    }

    public static Media[] getSoundMedias() {
        if (!soundInitDone) {
            String armFile = mlet.getAppProperty("AMR-Audio-Clip");
            String armFileLong = mlet.getAppProperty("AMR-Audio-Clip-Long");
            String midiFile = mlet.getAppProperty("MIDI-Audio-Clip");
            String wavFile = mlet.getAppProperty("WAV-Audio-Clip");

            Media m1 = new Media(midiFile, "audio/midi", Media.LOCATION_JAR);
            Media m2 = new Media(armFile, "audio/amr", Media.LOCATION_JAR);
            Media m2_long = new Media(armFileLong, "audio/amr", Media.LOCATION_JAR);
            Media m3 = new Media(wavFile, "audio/wav", Media.LOCATION_JAR);
            Media m4 = new Media(wavFile, "audio/x-wav", Media.LOCATION_JAR);

            soundMedias = new Media[]{m1, m2_long, m2, m3, m4};
            soundInitDone = true;
        }
        return soundMedias;
    }

    public static Media[] getVideoMedias() {
        if (!videoInitDone) {
            String videoFile = mlet.getAppProperty("Video-Clip");
            Media m = new Media(videoFile, "video/3gpp", Media.LOCATION_JAR);
            videoMedias = new Media[]{m};
            videoInitDone = true;
        }
        return videoMedias;
    }

    public static byte[] getToneSequence() {
        byte[] sequence = {ToneControl.VERSION, 1, ToneControl.TEMPO, 30, // times 4 = 120 beats-per-minute
            ToneControl.C4, 16, ToneControl.C4 + 2, 16, // D4
            ToneControl.C4 + 4, 16, // E4
            ToneControl.C4 + 5, 16, // F4 (note E# does not exist)
            ToneControl.C4 + 7, 16, // G4
            ToneControl.C4 + 9, 16, // A4
            ToneControl.C4 + 11, 16, // B4
            ToneControl.C4 + 9, 8, // A4
            ToneControl.C4 + 7, 8, // G4
            ToneControl.C4 + 5, 8, // F4 (note E# does not exist)
            ToneControl.C4 + 4, 8, // E4
            ToneControl.C4 + 2, 8, // D4
            ToneControl.C4, 8,};
        return sequence;
    }
}
