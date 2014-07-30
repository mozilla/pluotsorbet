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

import com.sun.midp.util.Properties;
import java.io.IOException;

/**
 * The properties for the suite.
 */
public class SuiteProperties {

    /** Suite properties from the application descriptor and manifest. */
    private Properties properties;

    /** The ID of this suite. */
    private int suiteId;

    /**
     * Package private constructor for SuiteProperties.
     *
     * @param id of the suite for these settings
     */
    SuiteProperties(int id) {
        suiteId = id;
    }

    /**
     * Gets a property of the suite. A property is an attribute from
     * either the application descriptor or JAR Manifest.
     *
     * @param key the name of the property
     * @return A string with the value of the property.
     *    <code>null</code> is returned if no value is available for
     *          the key.
     */
    public String getProperty(String key) {
        if (properties == null) {
            loadProperties();
        }

        return properties.getProperty(key);
    }

    /**
     * Replace or add a property to the suite for this run only.
     *
     * @param key the name of the property
     * @param value the value of the property
     *
     * @exception SecurityException if the calling suite does not have
     *            internal API permission
     */
    public void setTempProperty(String key, String value) {
        if (properties == null) {
            loadProperties();
        }

        properties.setProperty(key, value);
    }

    /**
     * Gets the unique ID of the suite.
     *
     * @return suite ID
     */
    public int getSuiteId() {
        return suiteId;
    }

    /**
     * Loads suite properties from persistent storage into a properties
     * object. If an IOException occurs, simply leaves the properties object
     * empty.
     */
    void loadProperties() {
        String[] propertyList;

        properties = new Properties();

        try {
            propertyList = load();
        } catch (IOException ioe) {
            return;
        }

        /*
         * Convert the string pairs into properties.
         * JAD properties are stored before Manifest properties, but according
         * to the MIDP spec, for untrusted applications, if an attribute in
         * the descriptor has the same name as an attribute in the manifest
         * the value from the descriptor must be used and the value from the
         * manifest must be ignored. So bellow we loop through the properties
         * backward to override the properties existing in the manifest.
         */
        for (int i = propertyList.length - 2; i >= 0; i -= 2) {
            properties.setProperty(propertyList[i], propertyList[i+1]);
        }
    }

    /**
     * Gets the suite properties from persistent store. Returns the
     * properties as an array of strings: key0, value0, key1, value1, etc.
     *
     * @return an array of property key-value pairs
     *
     * @throws IOException if an IO error occurs
     */
    native String[] load() throws IOException;

    /**
     * Saves the Suite Properties to persistent store
     */
    // native void save(String[] propertyList);
}
