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

import java.util.Hashtable;

import javax.microedition.sensor.SensorInfo;
import javax.microedition.sensor.DataListener;

import com.sun.midp.events.Event;
import com.sun.midp.events.EventListener;
import com.sun.midp.events.EventQueue;
import com.sun.midp.events.EventTypes;
import com.sun.midp.events.NativeEvent;
import com.sun.midp.security.*;

/**
 * This class is a bridge between native event callbacks and
 * {@link AvailabilityListener} callbacks. The current implementation of
 * SensorDevice does not know anything about SensorInfo to which it belongs to,
 * so we must register them in the {@link Configurator} factory.
 */
final class NativeSensorRegistry {

    /** Maps native sensor id to the SensorInfo. */
    private static final Hashtable ID_INFO_MAP = new Hashtable();

    /** Maps native sensor id to the AvailabilityListener. */
    private static final Hashtable ID_LISTENER_MAP = new Hashtable();

    /** Event queue instance. */
    private static final EventQueue eventQueue;

    /** Code operation table: smallest code. */
    static final int EVENT_SMALLEST_CODE = 1;

    /** Code operation table: aviability listener code. */
    static final int EVENT_AV_LISTENER_CODE = EVENT_SMALLEST_CODE;

    /** Code operation table: data collect code. */
    static final int EVENT_DATA_COLLECT_CODE = EVENT_SMALLEST_CODE + 1;

    /** Code operation table: highest code. */
    static final int EVENT_HIGHEST_CODE = EVENT_DATA_COLLECT_CODE;

    /** Native event listener. */
    private static final EventListener EVENT_LISTENER = new EventListener() {
        public boolean preprocess(Event evtNew, Event evtOld) {
            boolean retV = (evtNew instanceof NativeEvent);
            if (retV) {
                int codeOp = ((NativeEvent)evtNew).intParam1;
                retV = (EVENT_SMALLEST_CODE <= codeOp && codeOp <= EVENT_HIGHEST_CODE);
            }
            return retV;
        }

        public void process(Event evt) {
            if (evt instanceof NativeEvent) {
                NativeEvent nativeEvt = (NativeEvent) evt;
                int codeOp = nativeEvt.intParam1;
                switch(codeOp) {

                    case EVENT_AV_LISTENER_CODE: // aviability listener
                        processAvListEvent(nativeEvt);
                        break;

                    case EVENT_DATA_COLLECT_CODE: // start collecting data
                        processDataCollectEvent(nativeEvt);
                        break;

                }
            }
        }
        
        private void processAvListEvent(NativeEvent nativeEvt) {

                Integer sensorType = new Integer(nativeEvt.intParam2);
                boolean available = (nativeEvt.intParam3 == 1);

                AvailabilityListener listener;
                synchronized (ID_LISTENER_MAP) {
                    listener = (AvailabilityListener) ID_LISTENER_MAP
                            .get(sensorType);
                }
                if (listener != null) {
                    SensorInfo info;
                    synchronized (ID_INFO_MAP) {
                        info = (SensorInfo) ID_INFO_MAP.get(sensorType);
                    }
                    listener.notifyAvailability(info, available);
                }
        }
        
        private void processDataCollectEvent(NativeEvent nativeEvt) {
            Sensor sensor = SensorRegistry.getSensor(nativeEvt.intParam2);
            if (sensor != null) {
                ChannelDevice device = sensor.getChannelDevice(nativeEvt.intParam3);
                if (device != null) {
                    ValueListener listener;
                    if ((listener = device.getListener()) != null) {
                        new Thread(new RunGetData(device, listener, nativeEvt.intParam3)).start();
                    }
                }
            }
        }
        
    };

    /**
     * Inner class to request security token from SecurityInitializer.
     * SecurityInitializer should be able to check this inner class name.
     */
    static private class SecurityTrusted
        implements ImplicitlyTrustedClass {};

    static {
        /** Security token to allow access to implementation APIs */
        SecurityToken classSecurityToken =
            SecurityInitializer.requestToken(new SecurityTrusted()); 
        eventQueue = EventQueue.getEventQueue(classSecurityToken);
        // Register EVENT_LISTENER with SENSOR_EVENT type
        eventQueue.registerEventListener(
                EventTypes.SENSOR_EVENT, EVENT_LISTENER);
    }

    /** Prevents instantiation. */
    private NativeSensorRegistry() {
    }

    /**
     * Register the {@link SensorDevice} with {@link SensorInfo}.
     *
     * @param device SensorDevice instance
     * @param info SensorInfo instance
     */
    static void register(SensorDevice device, SensorInfo info) {
        synchronized (ID_INFO_MAP) {
            ID_INFO_MAP.put(new Integer(device.numberSensor), info);
        }
    }

    /**
     * Posts an event to queue.
     *
     * @param codeOp code operation
     * @param param1 parameter 1
     * @param param2 parameter 2
     * @param param3 parameter 3
     */
    static void postSensorEvent(int codeOp, int param1, int param2, int param3) {
        NativeEvent event = new NativeEvent(EventTypes.SENSOR_EVENT);
        event.intParam1 = codeOp;
        event.intParam2 = param1;
        event.intParam3 = param2;
        event.intParam4 = param3;
        eventQueue.post(event);
    }

    /**
     * Start monitoring the activity change callbacks from native layer.
     *
     * @param sensorType id of the native sensor
     * @param listener AvailabilityListener callback listener
     */
    static void startMonitoringAvailability(int sensorType,
            AvailabilityListener listener) {
        synchronized (ID_LISTENER_MAP) {
            ID_LISTENER_MAP.put(new Integer(sensorType), listener);
        }
        doStartMonitoringAvailability(sensorType);
    }

    /**
     * Stop monitoring the activity change callbacks from native layer.
     *
     * @param sensorType the native sensor id
     */
    static void stopMonitoringAvailability(int sensorType) {
        doStopMonitoringAvailability(sensorType);
        synchronized (ID_LISTENER_MAP) {
            ID_LISTENER_MAP.remove(new Integer(sensorType));
        }
    }

    /**
     * Start monitoring the activity change events in native layer.
     * <p>
     * <i>calls javacall_sensor_start_monitor_availability(sensor)</i>
     *
     * @param sensorType the native sensor id
     * @return true on success false otherwise
     */
    private static native boolean doStartMonitoringAvailability(int sensorType);

    /**
     * Stop monitoring the activity change events in native layer.
     * <p>
     * <i>calls javacall_sensor_stop_monitor_availability(sensor)</i>
     *
     * @param sensorType the native sensor id
     * @return true on success false otherwise
     */
    private static native boolean doStopMonitoringAvailability(int sensorType);
}

class RunGetData implements Runnable {
    ChannelDevice device;
    ValueListener listener;
    int numChannel;

    RunGetData(ChannelDevice device, ValueListener listener, int numChannel) {
        this.device = device;
        this.listener = listener;
        this.numChannel = numChannel;
    }

    public void run() {
        int errorCode = device.measureData();
        if (errorCode == ValueListener.DATA_READ_OK) {
            //Data listener
            listener.valueReceived(numChannel,
                device.getData(), device.getUncertainty(),
                device.getValidity());
        } else {
            listener.dataReadError(numChannel, errorCode);
        }
    }
}