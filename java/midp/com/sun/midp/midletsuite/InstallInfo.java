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

import java.io.IOException;

/**
 * Information about a MIDlet that is to be installed.
 */
public class InstallInfo {
    /** True if this is not a midlet suite but a suite's component. */
    public boolean isSuiteComponent;

    /**
     * If isSuiteComponent is true, holds ID of the component. In this case
     * field "id" holds ID of the suite that owns this component.
     */
    public int componentId;

    /** What ID the installed suite is stored by. */
    public int id;

    /** URL of the JAD. */
    public String jadUrl;

    /** URL of the JAR. */
    public String jarUrl;
        
    /** Name of the downloaded MIDlet suite jar file. */
    public String jarFilename;

    /** How big the JAD says the JAR is. */
    public int expectedJarSize;

    /** Name of the suite. */
    public String suiteName;

    /** User-friendly name of the suite or component being installed. */
    public String displayName;

    /** Vendor of the suite. */
    public String suiteVendor;

    /** Version of the suite. */
    public String suiteVersion;

    /** Description of the suite. */
    public String description;

    /**
     * Authorization path, staring with the most trusted CA authorizing
     * the suite, for secure installing.
     */
    public String[] authPath;

    /** Security domain of the suite, for secure installing. */
    public String domain;

    /** Flag for trusted suites. If true the system trust icon is displayed. */
    public boolean trusted;

    /** 
     * Flag for temporary suites. All temporary suites can be uninstalled at 
     * once.
     */
    public boolean temporary;
    
    /** Hash value of the suite with preverified classes */
    public byte[] verifyHash;

    /**
     * Constructor for InstallInfo to be called when storing a new suite.
     *
     * @param theId ID of the installed suite
     */
    public InstallInfo(int theId) {
        id = theId;
    }

    /**
     * Gets the JAD URL of the suite. This is only for the installer.
     *
     * @return URL of the JAD can be null
     */
    public String getJadUrl() {
        return jadUrl;
    }

    /**
     * Gets the JAR URL of the suite. This is only for the installer.
     *
     * @return URL of the JAR, never null, even in development environments
     */
    public String getJarUrl() {
        return jarUrl;
    }

    /**
     * Gets the name of CA that authorized this suite.
     *
     * @return name of a CA or null if the suite was not signed
     */
    public String getCA() {
        if (authPath == null || authPath.length == 0) {
            return null;
        }

        return authPath[0];
    }

    /**
     * Gets the authoriztion path of this suite. The path starts with
     * the most trusted CA that authorized this suite.
     *
     * @return array of CA names or null if the suite was not signed
     */
    public String[] getAuthPath() {
        if (authPath == null) {
            return authPath;
        }

        String[] result = new String[authPath.length];

        System.arraycopy(authPath, 0, result, 0, authPath.length);

        return result;
    }

    /**
     * Gets the security domain of this suite.
     *
     * @return name of a security domain
     */
    public String getSecurityDomain() {
        return domain;
    }

    /**
     * Indicates if this suite is trusted.
     * (not to be confused with a domain named "trusted",
     * this is used to determine if a trusted symbol should be displayed
     * to the user and not used for permissions)
     *
     * @return true if the suite is trusted false if not
     */
    public boolean isTrusted() {
        return trusted;
    }

    /**
     * Indicates whether this suite is temporary. All temporary suites can 
     * be uninstalled at once.
     *
     * @return true if the suite is temporary, false if not
     */
    public boolean isTemporary() {
        return temporary;
    }
    
    /**
     * Gets hash value for the suite with all classes successfully
     * verified during the suite installation, otherwise null value
     * will be returned
     *
     * @return suite hash value
     */
    public final byte[] getVerifyHash() {
        return verifyHash;
    }

    /**
     * Gets the URL that the suite was downloaded from.
     *
     * @return URL of the JAD, or JAR for a JAR only suite, never null,
     * even in development environments
     */
    public String getDownloadUrl() {
        String url = getJadUrl();

        if (url != null) {
            return url;
        }

        return getJarUrl();
    }

    /**
     * Gets the unique ID of the suite.
     *
     * @return suite ID
     */
    public int getID() {
        return id;
    }

    /**
     * Populates this InstallInfo instance from persistent store.
     *
     * @throws IOException if the information cannot be read
     */
    native void load() throws IOException;

    /**
     * Saves the Suite Install Info from persistent store
     */
    // native void save();
}
