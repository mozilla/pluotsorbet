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

package com.sun.midp.io.j2me.sensor;

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.sensor.*;
import com.sun.cldc.io.ConnectionBaseInterface;
import com.sun.javame.sensor.*;
import com.sun.j2me.security.*;

public class Protocol implements ConnectionBaseInterface {
    
    /** Creates a new instance of Protocol */
    public Protocol() {
    }

    /**
     * Opens a connection.
     *
     * @param name the target of the connection
     * @param mode indicates whether the caller intends to write to the connection. 
     *             Currently, this parameter is ignored.
     * @param timeouts indicates whether the caller wants timeout exceptions. 
     *        Currently, this parameter is ignored.
     * @return this connection
     * @throws IOException if the connection is closed or unavailable
     * @throws SecurityException if access is restricted by ACL
     */
    public Connection openPrim(String name, int mode, boolean timeouts) throws IOException {
        String url = SensorUrl.SCHEME + name;        
        Sensor[] sensors = SensorRegistry.findSensors(url); // throws IllegalArgumentException
        if (sensors == null || sensors.length != 1) {
            // URL is valid but no sensor found
            throw new ConnectionNotFoundException(url);
        }
        
        Sensor s = sensors[0];

        checkPermission("javax.microedition.io.Connector.sensor", url);

        checkPrivateProtectedPermission(s);

        s.open();
        return s;
    }

    /**
     * Checks for private and protected permission.
     *
     * @param sensor the sensor for checking
     * @throws InterruptedIOException when asking permission has been interrupted
     * @throws SecurityException if access is restricted
     */
    static void checkPrivateProtectedPermission(Sensor sensor)
        throws InterruptedIOException, SecurityException {
        String securityGroup = "";
        boolean isSecurity = true;

        try {
            securityGroup = (String)sensor.getProperty("security");
        } catch (IllegalArgumentException ex) {
            isSecurity = false; //no check permission is need
        }

        if (isSecurity && securityGroup != null) {
            String url = sensor.getUrl();
            if (securityGroup.equalsIgnoreCase("private")) {
                checkPermission("javax.microedition.sensor.PrivateSensor", url);
            } else if (securityGroup.equalsIgnoreCase("protected")) {
                checkPermission("javax.microedition.sensor.ProtectedSensor", url);
            }
        }
    }

    /**
     * Checks a permission.
     *
     * @param permission the permission string
     * @param url the sensor URL string
     * @throws InterruptedIOException when asking permission has been interrupted
     * @throws SecurityException if access is restricted
     */
    private static void checkPermission(String permission, String url)
        throws InterruptedIOException, SecurityException {
        try {
            AccessController.checkPermission(permission, url);
        } catch (InterruptedSecurityException ise) {
            throw new InterruptedIOException(
                "Interrupted while trying to ask the user permission");
        }
    }
}
