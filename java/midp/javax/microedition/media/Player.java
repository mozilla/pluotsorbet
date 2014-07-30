/*
 *   
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

package javax.microedition.media;


import java.io.IOException;



/**
 * This class is defined by the JSR-118 specification
 * <em>MIDP 2,
 * Version 2.0.</em>
 */
// JAVADOC COMMENT ELIDED


public interface Player extends Controllable {

    // JAVADOC COMMENT ELIDED
    static final int UNREALIZED = 100;

    // JAVADOC COMMENT ELIDED
    static final int REALIZED = 200;

    // JAVADOC COMMENT ELIDED
    static final int PREFETCHED = 300;

    // JAVADOC COMMENT ELIDED
    static final int STARTED = 400;

    // JAVADOC COMMENT ELIDED
    static final int CLOSED = 0;

    // JAVADOC COMMENT ELIDED
    static final long TIME_UNKNOWN = -1;
    
    // JAVADOC COMMENT ELIDED
    void realize() throws MediaException;

    // JAVADOC COMMENT ELIDED
    void prefetch() throws MediaException;

    // JAVADOC COMMENT ELIDED
    void start() throws MediaException;

    // JAVADOC COMMENT ELIDED
    void stop() throws MediaException;

    // JAVADOC COMMENT ELIDED
    void deallocate();

    // JAVADOC COMMENT ELIDED
    void close();
    

    // JAVADOC COMMENT ELIDED
    long setMediaTime(long now) throws MediaException;

    // JAVADOC COMMENT ELIDED
    long getMediaTime();

    // JAVADOC COMMENT ELIDED
    int getState();

    // JAVADOC COMMENT ELIDED
    long getDuration();

    // JAVADOC COMMENT ELIDED
    String getContentType();


    // JAVADOC COMMENT ELIDED

    void setLoopCount(int count);

    // JAVADOC COMMENT ELIDED
    void addPlayerListener(PlayerListener playerListener);

    // JAVADOC COMMENT ELIDED
    void removePlayerListener(PlayerListener playerListener);
}
