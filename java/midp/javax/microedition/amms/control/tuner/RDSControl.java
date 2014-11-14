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

package javax.microedition.amms.control.tuner;

import javax.microedition.media.*;

import java.util.Date;

/**
 * This class is defined by the JSR-234 specification
 * <em>Advanced Multimedia Supplements API
 * for Java&trade; Platform, Micro Edition</em>
 */
// JAVADOC COMMENT ELIDED
public interface RDSControl extends javax.microedition.media.Control {    

    // JAVADOC COMMENT ELIDED
    String RDS_NEW_DATA = "RDS_NEW_DATA";

    // JAVADOC COMMENT ELIDED
    String RDS_NEW_ALARM = "RDS_ALARM";

    // JAVADOC COMMENT ELIDED
    String RADIO_CHANGED = "radio_changed";


    // JAVADOC COMMENT ELIDED
    boolean isRDSSignal();

    // JAVADOC COMMENT ELIDED
    String getPS();

    // JAVADOC COMMENT ELIDED
    String getRT();

    // JAVADOC COMMENT ELIDED
    short getPTY();

    // JAVADOC COMMENT ELIDED
    String getPTYString(boolean longer);

    // JAVADOC COMMENT ELIDED
    short getPI();

    // JAVADOC COMMENT ELIDED
    int[] getFreqsByPTY(short PTY);

    // JAVADOC COMMENT ELIDED
    int[][] getFreqsByTA(boolean TA);

    // JAVADOC COMMENT ELIDED
    String[] getPSByPTY(short PTY);

    // JAVADOC COMMENT ELIDED
    String[] getPSByTA(boolean TA);

    // JAVADOC COMMENT ELIDED
    Date getCT();

    // JAVADOC COMMENT ELIDED
    boolean getTA();

    // JAVADOC COMMENT ELIDED
    boolean getTP();

    /**
     * Gets the current Traffic Message Channel's (TMC) message.
     *
     * @return TBD based on CEN standard ENV 12313-1
     */
    //Object getTMC();
     
    // JAVADOC COMMENT ELIDED
    void setAutomaticSwitching(boolean automatic)
	throws MediaException;

    // JAVADOC COMMENT ELIDED
    boolean getAutomaticSwitching();

    // JAVADOC COMMENT ELIDED
    void setAutomaticTA(boolean automatic) throws MediaException;

    // JAVADOC COMMENT ELIDED
    boolean getAutomaticTA();

}
