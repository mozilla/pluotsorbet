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

package com.sun.midp.installer;

/**
 * Holds the state of an installation, so it can restarted after it has
 * been stopped.
 */
public interface InstallState {
    /**
     * Gets the last recoverable exception that stopped the install.
     * Non-recoverable exceptions are thrown and not saved in the state.
     *
     * @return last exception that stopped the install
     */
    public InvalidJadException getLastException();

    /**
     * Gets the unique name that the installed suite was stored with.
     *
     * @return storage name that can be used to load the suite
     */
    public int getID();

    /**
     * Sets the username to be used for HTTP authentication.
     *
     * @param theUsername 8 bit username, cannot contain a ":"
     */
    public void setUsername(String theUsername);

    /**
     * Sets the password to be used for HTTP authentication.
     *
     * @param thePassword 8 bit password
     */
    public void setPassword(String thePassword);

    /**
     * Sets the username to be used for HTTP proxy authentication.
     *
     * @param theUsername 8 bit username, cannot contain a ":"
     */
    public void setProxyUsername(String theUsername);

    /**
     * Sets the password to be used for HTTP proxy authentication.
     *
     * @param thePassword 8 bit password
     */
    public void setProxyPassword(String thePassword);

    /**
     * Gets a property of the application to be installed.
     * First from the JAD, then if not found, the JAR manifest.
     *
     * @param key key of the property
     *
     * @return value of the property or null if not found
     */
    public String getAppProperty(String key);

    /**
     * Gets the URL of the JAR.
     *
     * @return URL of the JAR
     */
    public String getJarUrl();

    /**
     * Gets the label for the downloaded JAR.
     *
     * @return suite name
     */
    public String getSuiteName();

    /**
     * Gets the expected size of the JAR.
     *
     * @return size of the JAR in K bytes
     */
    public int getJarSize();

    /**
     * Gets the authorization path of this suite. The path starts with
     * the most trusted CA that authorized this suite.
     *
     * @return array of CA names or null if the suite was not signed
     */
    public String[] getAuthPath();
}
