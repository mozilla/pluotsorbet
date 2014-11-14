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

package javax.microedition.amms.control.camera;

import javax.microedition.media.MediaException;
import javax.microedition.media.Control;

/**
 * This class is defined by the JSR-234 specification
 * <em>Advanced Multimedia Supplements API
 * for Java&trade; Platform, Micro Edition</em>
 */
// JAVADOC COMMENT ELIDED
public interface SnapshotControl extends Control {

    // JAVADOC COMMENT ELIDED
    String SHOOTING_STOPPED = "SHOOTING_STOPPED";

    // JAVADOC COMMENT ELIDED
    String STORAGE_ERROR = "STORAGE_ERROR";

    // JAVADOC COMMENT ELIDED
    String WAITING_UNFREEZE = "WAITING_UNFREEZE";

    // JAVADOC COMMENT ELIDED
    public final static int FREEZE = -2;

    // JAVADOC COMMENT ELIDED
    public final static int FREEZE_AND_CONFIRM = -1;


    // JAVADOC COMMENT ELIDED
    void setDirectory(String directory);

    // JAVADOC COMMENT ELIDED
    String getDirectory();

    // JAVADOC COMMENT ELIDED
    void setFilePrefix(String prefix);

    // JAVADOC COMMENT ELIDED
    String getFilePrefix();

    // JAVADOC COMMENT ELIDED
    void setFileSuffix(String suffix);

    // JAVADOC COMMENT ELIDED
    String getFileSuffix();

    // JAVADOC COMMENT ELIDED
    void start(int maxShots) throws SecurityException;

    // JAVADOC COMMENT ELIDED
    void stop();

    // JAVADOC COMMENT ELIDED
    void unfreeze(boolean save);

}
