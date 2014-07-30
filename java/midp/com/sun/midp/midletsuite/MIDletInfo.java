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

package com.sun.midp.midletsuite;

import java.util.Vector;

import com.sun.midp.io.Util;

/**
 * Simple attribute storage for MIDlets in the descriptor/manifest.
 */
public class MIDletInfo {
    /** The name of the MIDlet. */
    public String name;
    /** The icon of the MIDlet. */
    public String icon;
    /** The main class for the MIDlet. */
    public String classname;

    /**
    * Parses out the name, icon and classname. 
    * @param attr contains the name, icon and classname line to be 
    * parsed
    */
    public MIDletInfo(String attr) {
        Vector args;

        if (attr == null) {
            return;
        }

        args = Util.getCommaSeparatedValues(attr);
        if (args.size() > 0) {
            name = (String)args.elementAt(0);
            if (args.size() > 1) {
                icon = (String)args.elementAt(1);
                if (icon.length() == 0) {
                    icon = null;
                }

                if (args.size() > 2) {
                    classname = (String)args.elementAt(2);
                    if (classname.length() == 0) {
                        classname = null;
                    }
                }
            }
        }
    }

    /**
    * Container class to hold information about the current MIDlet.
    * @param name the name of the MIDlet from descriptor file or
    * manifest
    * @param icon the icon to display when the user selects the MIDlet 
    * from a list
    * @param classname the main class for this MIDlet
    */
    public MIDletInfo(String name, String icon, String classname) {
        this.name = name;
        this.icon = icon;
        this.classname = classname;
    }
}
