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

package com.sun.midp.main;

/** access the implementation configuration file parameters. */
public class Configuration {
    /** Don't let anyone instantiate this class */
    private Configuration() {
    }
    /**
     * Gets the implementation property indicated by the specified key.
     *
     * @param      key   the name of the implementation property.
     * @return     the string value of the implementation property,
     *             or <code>null</code> if there is no property with that key.
     *
     * @exception  NullPointerException if <code>key</code> is
     *             <code>null</code>.
     * @exception  IllegalArgumentException if <code>key</code> is empty.
     */
    public static String getProperty(String key) {
	// If key is null, then a NullPointerException is thrown.
	// If key is blank, then throw a specific IllegalArgumentException
        if (key.length() ==  0) {
            throw new IllegalArgumentException("key can't be empty");
        }
        return getProperty0(key);
    }

    /**
     * Gets the implementation property indicated by the specified key or
     * returns the specified default value.
     *
     * @param      key   the name of the implementation property.
     * @param      def   the default value for the property if not
     *                  specified in the configuration files or command 
     *                  line over rides.
     * @return     the string value of the implementation property,
     *             or <code>def</code> if there is no property with that key.
     *
     * @exception  NullPointerException if <code>key</code> is
     *             <code>null</code>.
     * @exception  IllegalArgumentException if <code>key</code> is empty.
     */
    public static String getPropertyDefault(String key, String def) {
	String result = getProperty(key);

	return (result != null ? result : def);
    }

    /**
     * Gets the implementation property indicated by the specified key or
     * returns the specified default value as an positive int.
     *
     * @param      key   the name of the implementation property.
     * @param      def   the default value for the property if not
     *                  specified in the configuration files or command 
     *                  line over rides.
     *
     * @return     the int value of the implementation property,
     *             or <code>def</code> if there is no property with that key or
     *             the config value is not a positive int (zero is not
     *             positive).
     *
     * @exception  NullPointerException if <code>key</code> is
     *             <code>null</code>.
     * @exception  IllegalArgumentException if <code>key</code> is empty.
     */
    public static int getPositiveIntProperty(String key, int def) {
        int temp = getIntProperty(key, def);

        if (temp > 0) {
            return temp;
        }

        return def;
    }

    /**
     * Gets the implementation property indicated by the specified key or
     * returns the specified default value as an non-zero int.
     *
     * @param      key   the name of the implementation property.
     * @param      def   the default value for the property if not
     *                  specified in the configuration files or command 
     *                  line over rides.
     * @return     the int value of the implementation property,
     *             or <code>def</code> if there is no property with that key or
     *             the config value is not an int.
     *
     * @exception  NullPointerException if <code>key</code> is
     *             <code>null</code>.
     * @exception  IllegalArgumentException if <code>key</code> is empty.
     */
    public static int getNonNegativeIntProperty(String key, int def) {
        int temp = getIntProperty(key, def);

        if (temp >= 0) {
            return temp;
        }

        return def;
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
        /*
         * Get the  maximum number of persistent connections
         * from the configuration file.
         */
        String prop = getProperty(key);
        if (prop == null) {
            return def;
        }

        try {
            int temp = Integer.parseInt(prop);
            return temp;
        } catch (NumberFormatException nfe) {
            // keep the default
        }

        return def;
    }
    /**
     * native interface to the configuration parameter storage.
     *
     * @param      key   the name of the implementation property.
     * @return     the string value of the implementation property,
     *             or <code>null</code> if there is no property with that key.
     */
    private native static String getProperty0(String key);
}
