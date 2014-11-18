/*
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

package javax.microedition.amms;

import javax.microedition.media.*;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class is defined by the JSR-234 specification
 * <em>Advanced Multimedia Supplements API
 * for Java&trade; Platform, Micro Edition</em>
 */
// JAVADOC COMMENT ELIDED
public interface MediaProcessor extends Controllable {

    // JAVADOC COMMENT ELIDED
    public static final int UNKNOWN = -1;

    // JAVADOC COMMENT ELIDED
    public static final int UNREALIZED = 100;

    // JAVADOC COMMENT ELIDED
    public static final int REALIZED = 200;

    // JAVADOC COMMENT ELIDED
    public static final int STARTED = 400;

    // JAVADOC COMMENT ELIDED
    public static final int STOPPED = 300;


    // JAVADOC COMMENT ELIDED
    public void setInput( InputStream input, int length ) throws javax.microedition.media.MediaException;

    // JAVADOC COMMENT ELIDED
    public void setInput( Object image ) throws javax.microedition.media.MediaException;

    // JAVADOC COMMENT ELIDED
    public void setOutput( OutputStream output );

    // JAVADOC COMMENT ELIDED
    public void start() throws MediaException;

    // JAVADOC COMMENT ELIDED
    public void stop() throws MediaException;

    // JAVADOC COMMENT ELIDED
    public void complete() throws MediaException;

    // JAVADOC COMMENT ELIDED
    public void abort();

    // JAVADOC COMMENT ELIDED
    public void addMediaProcessorListener( MediaProcessorListener mediaProcessorListener );

    // JAVADOC COMMENT ELIDED
    public void removeMediaProcessorListener( MediaProcessorListener mediaProcessorListener );

    // JAVADOC COMMENT ELIDED
    public int getProgress();

    // JAVADOC COMMENT ELIDED
    public int getState();

}
