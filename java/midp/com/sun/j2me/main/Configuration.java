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

package com.sun.j2me.main;

/**
 * Intermediate class for getting system properties
 */
public class Configuration {

    /** Don't let anyone instantiate this class */
    private Configuration() {
    }

    /**
     * Returns internal property value by key
     * @param key property key
     * @return property value
     */
    public static String getProperty(String key) {
        return com.sun.midp.main.Configuration.getProperty(key);
    }

    /**
     * Gets the implementation property indicated by the specified key or
     * returns the specified default value as an int.
     *
     * @param      key   the name of the implementation property.
     * @param      def   the default value for the property if not
     *                  specified in the configuration files or command
     *                  line over rides.
     *
     * @return     the int value of the implementation property,
     *             or <code>def</code> if there is no property with that key or
     *             the config value is not an int.
     *
     * @exception  NullPointerException if <code>key</code> is
     *             <code>null</code>.
     * @exception  IllegalArgumentException if <code>key</code> is empty.
     */
    public static int getIntProperty(String key, int def) {
        return com.sun.midp.main.Configuration.getIntProperty(key, def);        
    }

    /**
     * Returns system property value by the given key using a privileged call.
     *
     * @param key property key
     * @return property value
     */
    public static String getSystemProperty(String key) {
        return System.getProperty(key);
    }
}
