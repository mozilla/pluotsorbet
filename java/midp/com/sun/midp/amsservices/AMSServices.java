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

package com.sun.midp.amsservices;

import java.io.IOException;

/**
 * Interface providing access to some AMS services.
 */
public interface AMSServices {
    /**
     * Installs the component pointed by the given URL.
     *
     * @param url HTTP URL pointing to the application descriptor
     *            or to the jar file of the component that must
     *            be installed
     * @param name user-friendly name of the component
     *
     * @return unique component identifier
     *
     * @throws IOException if the installation failed
     * @throws SecurityException if the caller does not have permission
     *         to install components
     */
    public int installComponent(String url, String name)
            throws IOException, SecurityException;

    /**
     * Removes the specified component belonging to the calling midlet.
     *
     * @param componentId ID of the component to remove
     *
     * @throws IllegalArgumentException if the component with the given ID
     *                                  does not exist
     * @throws IOException if the component is used now and can't be removed, or
     *                     other I/O error occured when removing the component
     * @throws SecurityException if the component with the given ID doesn't
     *                           belong to the calling midlet suite
     */
    public void removeComponent(int componentId)
            throws IllegalArgumentException, IOException, SecurityException;

    /**
     * Removes all installed components belonging to the calling midlet.
     *
     *
     * @throws IllegalArgumentException if there is no suite with
     *                                  the specified ID
     * @throws IOException is thrown, if any component is locked
     * @throws SecurityException if the calling midlet suite has no rights
     *                           to access this API
     */
    public void removeAllComponents()
        throws IllegalArgumentException, IOException, SecurityException;

    /**
     * Returns description of the components belonging to the calling midlet.
     *
     * @return an array of classes describing the components belonging to
     *         the calling midlet, or an empty array if there are no such
     *         components
     *
     * @throws SecurityException if the calling midlet suite has no rights
     *                           to access this API
     * @throws IOException if an the information cannot be read
     */
    public ComponentInfo[] getAllComponentsInfo()
            throws SecurityException, IOException;

    /**
     * Retrieves information about the component having the given ID belonging
     * to the calling suite.
     *
     * @param componentId ID of the component
     *
     * @return a class describing the component with the given ID
     *
     * @throws IllegalArgumentException if the component with the given ID
     *                                  does not exist
     * @throws SecurityException if the component with the given ID doesn't
     *                           belong to the calling midlet suite
     * @throws IOException if an the information cannot be read
     */
    public ComponentInfo getComponentInfo(int componentId)
            throws IllegalArgumentException, SecurityException, IOException;
}
