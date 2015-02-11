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

import java.util.Vector;

import javax.microedition.sensor.SensorInfo;
import javax.microedition.sensor.SensorListener;

/**
 * Class implementing the {@link javax.microedition.sensor.SensorManager}
 * methods.
 */
public final class SensorRegistry {

    private final static Sensor[] sensorsCache = createSensors();

    private final static AvailabilityPoller poller = new AvailabilityPoller(
            SensorConf.getAvailabilityPollerSleep());

    /** Prevents instantiation. */
    private SensorRegistry() {
    }

    /**
     * Construct sensor instances.
     *
     * @return Sensor array with initialized sensors or empty array on error
     */
    private static Sensor[] createSensors() {
        int n = doGetNumberOfSensors();
        Sensor[] sensors = new Sensor[n];
        for (int i = 0; i < n; i++) {
            sensors[i] = new Sensor(i);
        }
        return sensors;
    }
    
    private static native int doGetNumberOfSensors();

    public static SensorInfo[] findSensors(String quantity,
            String contextType) {
        if (contextType != null
                && !SensorInfo.CONTEXT_TYPE_AMBIENT.equals(contextType)
                && !SensorInfo.CONTEXT_TYPE_DEVICE.equals(contextType)
                && !SensorInfo.CONTEXT_TYPE_USER.equals(contextType)) {
            throw new IllegalArgumentException("Illegal contextType");
        }

        if (quantity == null && contextType == null) {
            return getAllSensors();
        }

        Vector mv = new Vector(sensorsCache.length);
        for (int i = 0; i < sensorsCache.length; i++) {
            Sensor s = sensorsCache[i];
            if (s.matches(quantity, contextType)) {
                mv.addElement(s);
            }
        }
        SensorInfo[] matches = new SensorInfo[mv.size()];
        mv.copyInto(matches);
        return matches;
    }

    public static Sensor[] findSensors(String url) {
        if (url == null) {
            throw new NullPointerException("url is null");
        }
        SensorUrl su = SensorUrl.parseUrl(url);
        return findSensors(su);
    }

    public static Sensor[] findSensors(SensorUrl su) {
        Vector mv = new Vector(sensorsCache.length);
        for (int i = 0; i < sensorsCache.length; i++) {
            Sensor s = sensorsCache[i];
            if (s.matches(su)) {
                mv.addElement(s);
            }
        }
        Sensor[] matches = new Sensor[mv.size()];
        mv.copyInto(matches);
        return matches;
    }

    static Sensor[] getAllSensors() {
        // Safe copy
        Sensor[] ret = new Sensor[sensorsCache.length];
        for (int i = 0; i < sensorsCache.length; i++) {
            ret[i] = sensorsCache[i];
        }
        return ret;
    }

    static Sensor getSensor(int sensorNumber) {
        Sensor retValue = null;
        if (0 <= sensorNumber && sensorNumber < sensorsCache.length) {
            retValue = sensorsCache[sensorNumber];
        } 
        return retValue;
    }

    /**
     * Registers a <code>SensorListener</code> to monitor changes in the
     * availability of any sensor that is measuring the defined quantity.
     * Attempts to register the same combination of listener and quantity as has
     * been previously registered is ignored.
     *
     * @param listener <code>SensorListener</code> to be registered
     * @param quantity a quantity in which the application is interested
     * @throws NullPointerException if the listener, or the quantity is null
     */
    public static void addSensorListener(SensorListener listener,
            String quantity) {
        if (listener == null) {
            throw new NullPointerException("Listener is null");
        }
        if (quantity == null) {
            throw new NullPointerException("Quantity is null");
        }
        // This is valid only because sensors can not be added dynamically
        for (int i = 0; i < sensorsCache.length; i++) {
            if (sensorsCache[i].getQuantity().equals(quantity)) {
                addSensorListener(listener, sensorsCache[i]);
            }
        }
    }

    /**
     * Registers {@link SensorListener} to monitor the availability of the given
     * sensor. Attempts to register the same combination of listener and
     * SensorInfo that is already registered is ignored.
     *
     * @param listener <code>SensorListener</code> to be registered
     * @param info <code>SensorInfo</code> defining the sensor, the
     *        availability of which is monitored. The parameter is compared with
     *        instance equality with the <code>SensorInfo</code> objects
     *        defining the sensors. Therefore, the instance must be an object
     *        that has previously been returned from the
     *        <code>findSensors()</code> method or from
     *        <code>SensorConnection</code> with the method
     *        <code>getSensorInfo()</code>.
     * @throws NullPointerException if either of the parameters is null
     * @throws IllegalArgumentException if info does not match to any of the
     *         provided sensors
     */
    public static void addSensorListener(SensorListener listener,
            SensorInfo info) {
        if (listener == null) {
            throw new NullPointerException("Listener is null");
        }
        if (info == null) {
            throw new NullPointerException("Info is null");
        }
        if (!containsSensorInfo(info)) {
            throw new IllegalArgumentException("Invalid SensorInfo");
        }
        poller.addListener(listener, info);
    }

    /**
     * Removes the <code>SensorListener</code> from the list of listeners
     * monitoring the availability of defined sensor(s). Returns silently if the
     * listener has not been previously registered.
     *
     * @param listener the <code>SensorListener</code> to be removed
     * @throws NullPointerException if the listener is null
     */
    public static void removeSensorListener(SensorListener listener) {
        if (listener == null) {
            throw new NullPointerException("Listener is null");
        }
        poller.removeListener(listener);
    }

    static int getSensorCount() {
        return sensorsCache.length;
    }

    private static boolean containsSensorInfo(final SensorInfo info) {
        for (int i = 0; i < sensorsCache.length; i++) {
            if (sensorsCache[i] == info) {
                return true;
            }
        }
        return false;
    }
}
