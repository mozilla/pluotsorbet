/*
 * Copyright © 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */ 
package com.nokia.example.mmapi.mediasampler.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 * Datatype for the Medias.
 */
public class Media {

    private String file;
    private String type;
    private int location;
    public static final int LOCATION_JAR = 0;
    public static final int LOCATION_HTTP = 1;
    public static final int LOCATION_FILE = 3;

    public Media(String file, String type, int location) {
        this.file = file;
        this.type = type;
        this.location = location;
    }

    /**
     * Return location type of this media.
     * 
     * @return int as type of this media location. Type value is one of the
     *         constants LOCATION_JAR, LOCATION_HTTP or LOCATION_FILE.
     */
    public int getLocation() {
        return location;
    }

    /**
     * Return file location of this Media.
     * 
     * @return String as url or path to media.
     */
    public String getFile() {
        return file;
    }

    /**
     * Returns mime type of this media.
     * 
     * @return String as mime type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns InputStream to the media data.
     * 
     * @return InputStream
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException {
        if (location == LOCATION_JAR) {
            return getClass().getResourceAsStream(file);
        } else if (location == LOCATION_HTTP) {
            return urlToStream(file);
        }
        throw new IOException("Not supported location type!");
    }

    /**
     * Reads the content from the specified HTTP URL and returns InputStream
     * where the contents are read.
     * 
     * @return InputStream
     * @throws IOException
     */
    private InputStream urlToStream(String url) throws IOException {
        // Open connection to the http url...
        HttpConnection connection = (HttpConnection) Connector.open(url);
        DataInputStream dataIn = connection.openDataInputStream();
        byte[] buffer = new byte[1000];
        int read = -1;
        // Read the content from url.
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        while ((read = dataIn.read(buffer)) >= 0) {
            byteout.write(buffer, 0, read);
        }
        dataIn.close();
        // Fill InputStream to return with content read from the URL.
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteout.toByteArray());
        return byteIn;
    }
}
