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

package com.sun.javame.sensor;

import java.util.*;

public class DefaultSensorProperties implements SensorProperties {
    private static final int DEFAULT_PROPS_MAX_NUMBER = 6;
    
    private Hashtable props = new Hashtable(DEFAULT_PROPS_MAX_NUMBER);
    
    /** Creates a new instance of DefaultSensorProperties */
    public DefaultSensorProperties() {
    }
    
    public void setProperty(String key, Object value) {
        props.put(key, value);
    }
    
    public Object getProperty(String key) {
        return props.get(key);
    }
    
    public String[] getPropertyNames() {
        String[] keys = new String[props.size()];
        
        int index = 0;
        for (Enumeration e = props.keys(); e.hasMoreElements(); ) {
            keys[index++] = (String)e.nextElement();
        }
        return keys;        
    }

    public boolean containsName(String name) {
        return props.containsKey(name);
    }
    
}
