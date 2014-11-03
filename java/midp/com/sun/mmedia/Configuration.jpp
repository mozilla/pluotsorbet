/*
 *  Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License version
 *  2 only, as published by the Free Software Foundation.
 *  
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License version 2 for more details (a copy is
 *  included at /legal/license.txt).
 *  
 *  You should have received a copy of the GNU General Public License
 *  version 2 along with this work; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA
 *  
 *  Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 *  Clara, CA 95054 or visit www.sun.com if you need additional
 *  information or have any questions.
 */
package com.sun.mmedia;

import java.util.Hashtable;
import javax.microedition.media.Control;

/**
 *  The configuration module for MMAPI.
 *
 */
public abstract class Configuration {

    /**
     * Tone sequence mime type.
     */
    public final static String MIME_AUDIO_TONE = "audio/x-tone-seq";

    /**
     * MIME_AUDI_AMR NB mime type.
     */
    public final static String MIME_AUDIO_WAV = "audio/x-wav";
    public final static String MIME_AUDIO_WAV_2 = "audio/wav";

    /**
     * MIME_AUDI_AMR mime type
     */
    public final static String MIME_AUDIO_AMR = "audio/amr";

    /**
     * MIME_AUDI_AMR WB mime type.
     */
    public final static String MIME_AUDIO_AMR_WB = "audio/amr-wb";

    /**
     * MIME_AUDIO_MIDI mime type.
     */
    public final static String MIME_AUDIO_MIDI = "audio/midi";
    public final static String MIME_AUDIO_MIDI_2 = "audio/mid";

    /**
     * SP-MIME_AUDIO_MIDI mime type.
     */
    public static final String MIME_AUDIO_SP_MIDI = "audio/sp-midi";

    /**
     * MIME_AUDIO_MP3 mime type
     */
    public final static String MIME_AUDIO_MP3 = "audio/mpeg";
    public final static String MIME_AUDIO_MP3_2 = "audio/mp3";

    /**
     * MP4 audio type
     */
    public final static String MIME_AUDIO_MP4 = "audio/mp4";
    public final static String MIME_AUDIO_MP4_2 = "audio/mp4a-latm";

    /**
     * MIME_AUDIO_AAC audio type
     */
    public final static String MIME_AUDIO_AAC = "audio/aac";

    /**
     * MIME_AUDIO_QCELP audio type
     */
    public final static String MIME_AUDIO_QCELP = "audio/qcelp";
    public final static String MIME_AUDIO_QCELP_2 = "audio/vnd.qcelp";

    /**
     * GIF mime type.
     */
    public final static String MIME_IMAGE_GIF = "image/gif";

    /**
     * PNG mime type.
     */
    public final static String MIME_IMAGE_PNG = "image/png";

    /**
     * JPEG mime type.
     */
    public final static String MIME_IMAGE_JPEG = "image/jpeg";

    /**
     * Raw image mime type.
     */
    public final static String MIME_IMAGE_RAW = "image/raw";


    /**
     * 3GPP video mime type
     */
    public final static String MIME_VIDEO_3GPP = "video/3gpp";
    public final static String MIME_VIDEO_3GPP_2 = "video/3gpp2";

    /**
     * MPEG video mime type
     */
    public final static String MIME_VIDEO_MPEG = "video/mpeg";

    /**
     * MPEG4 video mime type
     */
    public final static String MIME_VIDEO_MPEG4 = "video/mp4";

    /**
     * WMV video mime type
     */
    public final static String MIME_VIDEO_WMV = "video/x-ms-wmv";
    /**
     * AVI video mime type
     */
    public final static String MIME_VIDEO_AVI = "video/avi";

    public final static String TONE_DEVICE_LOCATOR = javax.microedition.media.Manager.TONE_DEVICE_LOCATOR; //"device://tone";
    public final static String MIDI_DEVICE_LOCATOR = javax.microedition.media.Manager.MIDI_DEVICE_LOCATOR; //"device://midi";
    public final static String CAPTURE_LOCATOR       = "capture://";
    public final static String RADIO_CAPTURE_LOCATOR = "capture://radio";
    public final static String AUDIO_CAPTURE_LOCATOR = "capture://audio";
    public final static String VIDEO_CAPTURE_LOCATOR = "capture://video";

    /**
     * A hash table of the protocol handlers.
     */
    protected Hashtable protocolHandlers;

    /**
     * A table of mime types.
     */
    protected Hashtable mimeTypes;

    /**
     * A table of media formats supported by Jave.
     */
    protected Hashtable mFormats;

    /**
     *  The current configuration object.
     */
    private static Configuration config;

    /**
     *  True if players loop in native code,
     *  otherwise false
     */
    protected static boolean nativeLooping = false;
    
    protected Hashtable properties;
    
    /**
     * defines whether to process jsr234-specific operations or not 
     * (jsr135 only).
     */
    protected boolean needAMMS;

    /**
     *Constructor for the Configuration object
     */
    public Configuration() {
        protocolHandlers = new Hashtable();
        mimeTypes = new Hashtable();
        mFormats = new Hashtable();
        properties = new Hashtable();
        try {
            String value = System.getProperty("microedition.amms.version");
            needAMMS = (value != null);
        } catch (Exception e) {
            needAMMS = false;
        }
    }


    /**
     *  Gets supported content types for given protocol
     *
     * @param  protocol  protocol
     * @return           array of supported content types
     */
    public abstract String[] getSupportedContentTypes(String protocol);

    /**
     *  Gets supported protocols for given content type
     *
     * @param  ctype  content type
     * @return        array of supported protocols
     */
    public abstract String[] getSupportedProtocols(String ctype);

    public abstract String getProperty(String key);

    public abstract void setProperty(String key, String value);

    /**
     *  Gets Accessor to platform specific Image classes
     *  To be defined in derived classes.
     *
     * @return instance of ImageAccess class
     */
    public abstract ImageAccess getImageAccessor();

    /**
     * Gets the video renderer.
     *
     * @return The video renderer
     */
    public abstract VideoRenderer getVideoRenderer(BasicPlayer player);

    /**
     *  Gets the tonePlayer attribute of the Configuration object
     *
     * @return    The tonePlayer value
     */
    public abstract TonePlayer getTonePlayer();

    /**
     * Convert from the name of a file to its corresponding mediaFormat
     * format based on the extension for Java Players.
     *
     * @param  name  file's pathname
     * @return       media Format for this name or null, if couldn't be determined
     */
    public String ext2Format(String name) {
        int idx = name.lastIndexOf('.');
        String ext;
        if (idx != -1) {
            ext = name.substring(idx + 1).toLowerCase();
        } else {
            ext = name.toLowerCase();
        }
        return (String) mFormats.get(ext);
    }

    /**
     * Convert from the content type to its corresponding mediaFormat
     * format based on the MIME types for Java Players.
     *
     * @param  type  MIME type
     * @return       media Format for this name or null, if couldn't be determined
     */
    public String mime2Format(String type) {
        String ext = null;
        for (java.util.Enumeration e = mimeTypes.keys(); e.hasMoreElements();) {
            String k = (String)e.nextElement();
            if (mimeTypes.get(k).equals(type)) {
                ext = k;
                break;
            }
        }
        if (ext != null) {
            return (String)mFormats.get(ext);
        }
        return null;
    }

    /**
     *  Gets the handler attribute of the Configuration object
     *
     * @param  type  The content type
     * @return       The handler value
     */
    public String getProtocolHandler(String type) {
        return (String) protocolHandlers.get(type);
    }

    /**
     *  Gets the configuration attribute of the Configuration class
     *
     * @return    The configuration value
     */
    public static Configuration getConfiguration() {
        if (config != null) return config;
    
        String className = System.getProperty("mmapi-configuration");
        
        if (className != null) {        
            try {
                // ... try and instantiate the configuration class ...
                Class handlerClass = Class.forName(className);
                config = (Configuration) handlerClass.newInstance();
            } catch (Exception e) {
                // do nothing
            }
        } else {            
            config = new DefaultConfiguration();
        }

        return config;
    }


    /**
     *  Sets the configuration attribute of the Configuration class
     *
     * @param  cnf  The new configuration value
     */
     /*
    public static void setConfiguration(Configuration cnf) {
        config = cnf;
    }
    */

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public static boolean nativeLoopMode() {
        return nativeLooping;
    }

    public boolean isRadioSupported()
    {
        return false;
    }
    
}

