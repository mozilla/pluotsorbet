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

package javax.microedition.sensor;

import com.sun.javame.sensor.SensorRegistry;

/**
 * The <code>SensorManager</code> class is used to find sensors and monitor
 * their availability.
 *
 * <h3>Finding sensors</h3>
 * <p>
 * <code>SensorManager</code> provides two static methods to find sensors.
 * They both return an array of {@link SensorInfo} objects listing the found
 * sensors:
 * <ol>
 * <li>{@link SensorManager#findSensors(String, String)}</li>
 * <li>{@link SensorManager#findSensors(String)}</li>
 * </ol>
 * </p>
 * <p>
 * If there are several sensors measuring the same quantity, the application
 * developer may want select a specific sensor based on criteria such as
 * accuracy, or a sampling rate. This is done by examining and comparing the
 * information provided by the <code>SensorInfo</code> instances.
 * </p>
 * <p>
 * Note: some sensors are intended for restricted use only, to be used in the
 * manufacturer, operator, or trusted party domain applications only, or if the
 * user permits. When the application doesn't have the required permissions, all
 * the found sensors are still returned but they cannot necessary be opened. The
 * <code>Connector.open()</code> and
 * <code>PushRegistry.registerConnection()</code> methods throw
 * SecurityException if the application does not have the required permission to
 * use the sensor.
 * </p>
 * <h3>Monitoring sensors</h3>
 * <p>
 * <code>SensorManager</code> is also responsible for registering and
 * unregistering <code>SensorListener</code> objects. A
 * <code>SensorListener</code> will get
 * {@link SensorListener#sensorAvailable(SensorInfo)} /
 * {@link SensorListener#sensorUnavailable(SensorInfo)} notifications. Only one
 * notification for each matching <code>SensorListener</code> is sent per
 * change in availability.
 * </p>
 */
public final class SensorManager {

    /** Prevents instantiation. */
    private SensorManager() {
    }

    public static SensorInfo[] findSensors(String quantity,
            String contextType) {
        return SensorRegistry.findSensors(quantity, contextType);
    }

    public static SensorInfo[] findSensors(String url) {
        return SensorRegistry.findSensors(url);
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
        SensorRegistry.addSensorListener(listener, info);
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
        SensorRegistry.addSensorListener(listener, quantity);
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
        SensorRegistry.removeSensorListener(listener);
    }
}
