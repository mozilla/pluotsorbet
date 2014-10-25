/*
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

package com.sun.mmedia.protocol;

import java.io.*;
import javax.microedition.media.*;
import javax.microedition.media.protocol.*;
import java.util.Hashtable;

/**
 * A DataSource base class
 */
public abstract class BasicDS extends DataSource {

    protected String locator = null;
    protected String contentType = null;

    public BasicDS() {
        super(null);
    }

    public void setLocator(String ml) throws MediaException {
        locator = ml;
    }

    public String getLocator() {
        return locator;
    }

 
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String type) {
        contentType = type;
    }

    public Control[] getControls() {
        return new Control[0];
    }

    public Control getControl(String controlType) {
        return null;
    }
 
    /**
     * Parse the protocol part of the locator string.
     */
    static public String getProtocol(String loc) {
        String proto = "";
        int idx = loc.indexOf(':');

        if( idx != -1) {
            proto = loc.substring(0,idx);
        }

        return proto;
    }
}
