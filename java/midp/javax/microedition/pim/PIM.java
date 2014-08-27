/*
 *   
 *
 * Portions Copyright  2000-2009 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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
 
/*
 * Copyright (C) 2002-2003 PalmSource, Inc.  All Rights Reserved.
 */

package javax.microedition.pim;

import com.sun.j2me.main.Configuration;

import java.io.*;

/**
 * This class is defined by the JSR-75 specification
 * <em>PDA Optional Packages for the J2ME&trade; Platform</em>
 */
// JAVADOC COMMENT ELIDED
public abstract class PIM {
    // JAVADOC COMMENT ELIDED
    public static final int CONTACT_LIST = 1;
    // JAVADOC COMMENT ELIDED
    public static final int EVENT_LIST = 2;
    // JAVADOC COMMENT ELIDED
    public static final int TODO_LIST = 3;
    // JAVADOC COMMENT ELIDED
    public static final int READ_ONLY = 1;
    // JAVADOC COMMENT ELIDED
    public static final int WRITE_ONLY = 2;
    // JAVADOC COMMENT ELIDED
    public static final int READ_WRITE = 3;
    /** Current PIM instance handle. */
    private static PIM instance;

    // JAVADOC COMMENT ELIDED
    public static PIM getInstance() {
        synchronized (PIM.class) {
            if (instance == null) {
                String className =
                    Configuration.getProperty("javax.microedition.pim.impl");
                if (className == null) {
                    className = "com.sun.j2me.pim.PIMImpl";
                }
                boolean excThrowed = false;
                try {
                    instance = (PIM) Class.forName(className).newInstance();
                } catch (ClassNotFoundException e) {
                    excThrowed = true;
                } catch (Error e) {
                    excThrowed = true;
                } catch (IllegalAccessException e) {
                    excThrowed = true;
                } catch (InstantiationException e) {
                    excThrowed = true;
                }
                if (excThrowed) {
                    throw new Error("PIM implementation '"
                        + className + "' could not be initialized.");
                }
            }
            return instance;
        }
    }

    // JAVADOC COMMENT ELIDED
    protected PIM() {
    }

    // JAVADOC COMMENT ELIDED
    public abstract PIMList openPIMList(int pimListType, int mode)
        throws PIMException;

    // JAVADOC COMMENT ELIDED
    public abstract PIMList openPIMList(int pimListType, int mode, String name)
        throws PIMException;

    // JAVADOC COMMENT ELIDED
    public abstract String[] listPIMLists(int pimListType);

    // JAVADOC COMMENT ELIDED
    public abstract PIMItem[] fromSerialFormat(java.io.InputStream is,
                                String enc)
        throws PIMException, UnsupportedEncodingException;

    // JAVADOC COMMENT ELIDED
    public abstract void toSerialFormat(PIMItem item,
                                java.io.OutputStream os,
                                String enc,
                                String dataFormat)
        throws PIMException, UnsupportedEncodingException;

    // JAVADOC COMMENT ELIDED
    public abstract String[] supportedSerialFormats(int pimListType);

}
