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

import java.io.*;
import java.util.*;
import javax.microedition.sensor.*;


public class Sensor implements SensorInfo, SensorConnection,
        ChannelDataListener, AvailabilityNotifier {

    private class SensorEventQueue implements Runnable {

        /** Sensor message queue. */
        private Vector messages = new Vector();

        /** Stop flag. */
        private boolean isStop = true;

        /** Flag of notification. */
        private boolean isNotify;

        /** Sensor state */
        private int state = SensorConnection.STATE_CLOSED;

        /** Sensor state for data collecting */
        private int stateData = StatesEvents.SENSOR_IDLE;

        /** Thread of event queue. */
        private Thread eventQueueThread;

        /**
         * Put message to queue.
         *
         * @param msg message code
         */
        private synchronized void putMessage(int msg) {
            messages.addElement(new Integer(msg));
            isNotify = true;
            notify();
        }

        /**
         * Gets the current state.
         */
        private int getState() {
            return state;
        }

        /**
         * Sets the current state.
         */
        private synchronized void setState(int state) {
            this.state = state;
        }

        /**
         * Gets the current data state.
         */
        private int getStateData() {
            return stateData;
        }

        /**
         * Sets the current data state.
         */
        private synchronized void setStateData(int state) {
            stateData = state;
        }

        /**
         * Start process the queue.
         */
        private synchronized void start() {
            isStop = false;
            eventQueueThread = new Thread(this);
            eventQueueThread.start();
        }

        /**
         * Stop process the queue.
         */
        private void stop() {
            if (!isStop) {
                synchronized (this) {
                    isStop = true;
                    isNotify = true;
                    notify();
                }
                try {
                    eventQueueThread.join();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        /**
         * Process the queue.
         */
        public void run() {
            while (!isStop) {
                synchronized (this) {
                    if (isStop) {
                        break;
                    }
                    if (messages.size() == 0) {
                        isNotify = false;
                        while (!isNotify) {
                            try {
                                wait();
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }
                // Process messages
                while (!isStop && messages.size() > 0) {
                    int msg = ((Integer)messages.firstElement()).intValue();
                    messages.removeElementAt(0);
                    switch (msg) {
                        case StatesEvents.IND_DATA: // data from channel was received
                            if (stateData == StatesEvents.WAIT_DATA) {
                                switch (state) {
                                    case SensorConnection.STATE_OPENED: // getData(...)
                                        setNotify();
                                        stateData = StatesEvents.SENSOR_IDLE;
                                        break;
                                    case SensorConnection.STATE_LISTENING: // call listener.dataReceived(...)
                                        callDataListener();
                                        break;
                                }
                            }
                            break;
                        case StatesEvents.IND_ERROR: // error from channel
                            callDataErrorListener();
                            break;
                        case StatesEvents.STOP_GET_DATA: // data collecting is need to stop
                            if (stateData == StatesEvents.WAIT_DATA) {
                                stopGetData();
                                stateData = StatesEvents.WAIT_CLOSE_DATA;
                            }
                            break;
                        case StatesEvents.STOP_GET_DATA_CONF: // data collecting stop confirmation
                            if (stateData == StatesEvents.WAIT_CLOSE_DATA) {
                                if (isAllChannelsAnswered()) {
                                    stateData = StatesEvents.SENSOR_IDLE;
                                    if (state == SensorConnection.STATE_LISTENING) {
                                        setNotify();
                                    }
                                }
                            }
                            break;
                    }
                }
            }
        }
    } // end of the private class declaration

    /* Sensor information */
    private int number;
    private String description;
    private String contextType;
    private String model;
    private int maxBufferSize;
    private int connType;
    private String quantity;

    /** device-specific code relating the whole sensor */
    private SensorDevice sensorDevice = null;

    /** Channel count. */
    private int channelCount;

    /** Data array which read from sensor. */
    private DataImpl[] retData;

    /** Listener for notification about data receiving. */
    private DataListener listener;

    /** Last channel status. */
    private int channelStatus;

    /** Channel number. */
    private int channelNumber;

    /** Error timestamp. */
    private long errorTimestamp;

    /** Sensor properties */
    SensorProperties props;

    /** Channels */
    private ChannelImpl[] channels;

    /** Availability push supporting flag. */
    private boolean isSensorAvailabilityPushSupported;

    /** Condition push supporting flag. */
    private boolean isSensorConditionPushSupported;

    /** Flag of notification. */
    private boolean isNotify;

    /** Error codes table */
    private Hashtable errorCodes;

    /** Sensor event queue. */
    private SensorEventQueue eventQueue;

    /** Remove datallistener counter. */
    private volatile int remDataListCount = 0;

    /** 
     * Creates a new instance of Sensor.
     *
     * @param number number of the sensor
     */
    Sensor(int number) {
        this.number = number;
        initFields();
        eventQueue = new SensorEventQueue();
    }

    /**
     * Gets the sensor's ChannelInfo array
     * representing channels of the sensor.
     *
     * @return ChannelInfo array
     */
    public ChannelInfo[] getChannelInfos() {
        return channels;
    }

    /**
     * Gets the connection type.
     *
     * @return one of values: CONN_EMBEDDED, CONN_REMOTE,
     * CONN_SHORT_RANGE_WIRELESS or CONN_WIRED
     */
    public int getConnectionType() {
        return connType;
    }

    /**
     * Gets the context type.
     *
     * @return one of values: CONTEXT_TYPE_USER,
     * CONTEXT_TYPE_DEVICE or CONTEXT_TYPE_AMBIENT
     */
    public String getContextType() {
        return contextType;
    }

    /**
     * Gets the description of sensor.
     *
     * @return the readable description of sensor
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the maximal data buffer length of sensor.
     *
     * @return maximal data buffer length of sensor
     */
    public int getMaxBufferSize() {
        return maxBufferSize;
    }

    /**
     * Gets the model name of sensor.
     *
     * @return the model name of sensor
     */
    public String getModel() {
        return model;
    }

    /**
     * Gets the quantity of sensor.
     *
     * @return the quantity of sensor
     */
    public String getQuantity() {
        return quantity;
    }


    /**
     * Gets URL needed to open SensorConnection.
     *
     * @return the URL needed to open SensorConnection
     */
    public String getUrl() {
        return SensorUrl.createUrl(this);
    }

    /**
     * Checks is sensor supports availability push.
     *
     * @return true when sensor supports availability push
     */
    public boolean isAvailabilityPushSupported() {
        return isSensorAvailabilityPushSupported;
    }

    /**
     * Checks is sensor available.
     *
     * @return true when sensor is available else false
     */
    public boolean isAvailable() {
        return sensorDevice.isAvailable();
    }

    /**
     * Checks is sensor supports condition push.
     *
     * @return true when sensor supports condition push
     */
    public boolean isConditionPushSupported() {
        return isSensorConditionPushSupported;
    }

    /**
     * Gets the property of sensor by name.
     *
     * @param name the name of property
     * @return the quantity of sensor
     */
    public Object getProperty(String name) {        
        if (name == null) {
            throw new NullPointerException();
        }

        if (!props.containsName(name)) {
            throw new IllegalArgumentException();
        }

        return props.getProperty(name);
    }

    /**
     * Gets the array of property names of sensor.
     *
     * @return an array of property keys for the sensor
     */
    public String[] getPropertyNames() {
        return props.getPropertyNames();
    }

    /**
     * Checks is sensor contains given quantity and context type.
     *
     * @return true when sensor contains given quantity and context type
     */
    public boolean matches(String quantity, String contextType) {
        return ((quantity == null) || quantity.equals(this.quantity)) &&
               ((contextType == null) || contextType.equals(this.contextType));
    }

    /**
     * Checks is sensor matches to given URL.
     *
     * @return true when sensor matches to given URL
     */
    public boolean matches(SensorUrl url) {
        String location = (String)props.getProperty(PROP_LOCATION);

        return quantity.equals(url.getQuantity()) &&
               ((url.getContextType() == null) || url.getContextType().equals(contextType)) &&
               ((url.getLocation() == null) || url.getLocation().equals(location)) &&
               ((url.getModel() == null) || url.getModel().equals(model));
    }

    /**
     * Opens sensor.
     *
     * @throws IOException if the sensor has wrong state
     */
    public void open() throws IOException {

        if (eventQueue.getState() != STATE_CLOSED)
        {
            // Decide later if we allow multiple connections to the same sensor
            // JSR 256 spec leaves this to implementation
            throw new IOException("Sensor is already opened");
        }

        boolean isInitOk = true;
        isInitOk &= sensorDevice.initSensor();
        for (int i = 0; isInitOk && i < channels.length; i++) {
            isInitOk &= channels[i].initChannel();
            isInitOk &= channels[i].getChannelDevice().initChannel();
        }

        if (!isInitOk) {
            throw new IOException("Sensor start fails");
        }
        eventQueue.start();
        eventQueue.setState(STATE_OPENED);
    }
    
    /*
     * SensorConnection methods
     */
    
    public int getState() {
        return eventQueue.getState();
    }
    
    public Channel getChannel(ChannelInfo channelInfo) {
        if (channelInfo == null) {
            throw new NullPointerException();
        }

        /* In this implementation ChannelInfo is the same as Channel. So, we just
         * have to check that this specific sensor owns the channel.
         */
        for (int i = 0; i < channels.length; i++) {
            if (channelInfo == channels[i]) {
                return channels[i];
            }
        }
        
        /* This is not a channel from this sensor */
        throw new IllegalArgumentException("This channel is not from this sensor");
    }


    /**
     * Fetches data in the synchronous mode.
     *
     * @param bufferSize the size of the data buffer ( &gt; 0)
     * @return the collected data of all the channels
     * of this sensor
     * @throws IllegalArgumentException - when bufferSize &lt; 1
     * or if bufferSize &gt; the maximum size of the buffer
     * @throws java.io.IOException - if the state is STATE_CLOSED
     * or if any input/output problems are occured
     * @throws java.lang.IllegalStateException - in case of  the
     * state is STATE_LISTENING
     */
    public Data[] getData(int bufferSize) throws java.io.IOException {
        return getData(bufferSize, 0L, false, false, false);
    }


    /**
     * Retrieves data in the synchronous mode.
     *
     * @param bufferSize - the size of the data buffer 
     * @param bufferingPeriod - the time to buffer values
     * @param isTimestampIncluded - if true timestamps should be 
     *  included in returned Data objects
     * @param isUncertaintyIncluded - if true uncertainties should be
     *  included in returned Data objects
     * @param isValidityIncluded - if true validities should be
     *  included in returned Data objects
     * @return collected data of all the channels of this sensor.
     * @throws java.lang.IllegalArgumentException - if the both, bufferSize
     *  and bufferingPeriod, have values less than 1, or if bufferSize
     *  exceeds the maximum size of the buffer
     * @throws java.lang.IllegalStateException - if the state is STATE_LISTENING 
     * @throws java.io.IOException - if the state is STATE_CLOSED
     *  or if any input/output problems are occured
     */
   public synchronized Data[] getData(int bufferSize,
       long bufferingPeriod,
       boolean isTimestampIncluded,
       boolean isUncertaintyIncluded,
       boolean isValidityIncluded)
       throws java.io.IOException {
       if ((bufferSize < 1 && bufferingPeriod < 1) ||
           bufferSize > maxBufferSize) {
           throw new IllegalArgumentException(
                   "Wrong buffer size or/and period values");
       }

       int state = eventQueue.getState(); 
       if (state == STATE_LISTENING) {
            throw new IllegalStateException("Wrong state");
        }

        if (state == STATE_CLOSED) {
            throw new IOException("Wrong state");
        }

        if (bufferSize < 1)
        {
            bufferSize = maxBufferSize;
        }

        /* Sending signals to each channel to start getting data */
        long startTime = System.currentTimeMillis();
        this.listener = null;
        channelCount = 0;
        channelStatus = ValueListener.DATA_READ_OK;
        retData = new DataImpl[channels.length];
        eventQueue.setStateData(StatesEvents.WAIT_DATA);
        for (int i = 0; i < channels.length; i++) {
            channels[i].startGetData(this, bufferSize, bufferingPeriod,
                isTimestampIncluded, isUncertaintyIncluded,
                isValidityIncluded, false, startTime);
        }
        isNotify = false;
        while (!isNotify) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        if (channelStatus != ValueListener.DATA_READ_OK)
        {
            throw new IOException("Read data error with code " + channelStatus +
            " on channel " + channelNumber);
        }
        return retData;
    }

    private native void doGetSensorModel(int n, SensorModel m);

    private void initFields() {
        SensorModel sensorModel = new SensorModel();
        doGetSensorModel(number, sensorModel); 
        description = sensorModel.description;
        quantity = sensorModel.quantity;
        contextType = sensorModel.contextType;
        model = sensorModel.model;
        maxBufferSize = sensorModel.maxBufferSize;
        connType = sensorModel.connectionType;
        isSensorAvailabilityPushSupported = sensorModel.availabilityPush;
        isSensorConditionPushSupported = sensorModel.conditionPush;
        sensorDevice = DeviceFactory.generateSensor(number);
        props = sensorModel.getProperties();
        errorCodes = sensorModel.getErrorCodes();
        channels = new ChannelImpl[sensorModel.channelCount];
        for (int i=0; i<sensorModel.channelCount; ++i){
            channels[i] = new ChannelImpl(number,i);
            channels[i].setSensor(this);
        }
    }
    /**
     * Notification about data from channel.
     *
     * @param number channel number
     * @param data data instance from channel
     */
    public void channelDataReceived(int number, DataImpl data) {
        retData[number] = data;
        if (isAllChannelsAnswered())
        {
            eventQueue.putMessage(StatesEvents.IND_DATA);
            channelCount = 0;
        }
    }

    /**
     * Notification about channel error.
     *
     * @param number channel number
     * @param errorCode code of channel error
     * @param timeStamp timestamp of error
     */
    public void channelErrorReceived(int number, int errorCode,
        long timestamp) {
            if (eventQueue.getState() == STATE_LISTENING &&
                eventQueue.getStateData() == StatesEvents.WAIT_DATA &&
                listener instanceof DataAndErrorListener) {
            channelStatus = errorCode;
            channelNumber = number;
            errorTimestamp = timestamp;
            eventQueue.putMessage(StatesEvents.IND_ERROR);
        }
    }

    /**
     * Notification stop collecting data from channel.
     *
     * @param number channel number
     */
    void confirmStopData(int number) {
        eventQueue.putMessage(StatesEvents.STOP_GET_DATA_CONF);
    }

    public SensorInfo getSensorInfo() {
        return this;
    }


    /**
     * Removes the DataListener registered to this SensorConnection.
     *
     * @throws java.lang.IllegalStateException - if this SensorConnection
     * is already closed
     */
    public void removeDataListener() {
        int state = eventQueue.getState();
        if (state == STATE_CLOSED) {
            throw new IllegalStateException("Connection is already closed");
        }
        if (state == STATE_LISTENING)
        {
            if (remDataListCount > 0)
            {
                return;
            }
            remDataListCount++;
            synchronized (this)
            {
                eventQueue.putMessage(StatesEvents.STOP_GET_DATA);
                isNotify = false;
                while (!isNotify)
                {
                    try
                    {
                        wait();
                    }
                    catch (InterruptedException ex)
                    {
                        Thread.currentThread().interrupt();
                    }
                }
                listener = null;
                eventQueue.setState(STATE_OPENED);
            }
            remDataListCount--;
        }
    }

    /**
     * Registers a DataListener to receive collected data asynchronously.
     *
     * @param listener - DataListener to be registered
     * @param bufferSize - size of the buffer, value must be &gt; 0
     * @throws java.lang.NullPointerException - if the listener is null
     * @throws java.lang.IllegalArgumentException - if the bufferSize &lt; 1,
     *  or if bufferSize exceeds the maximum size of the buffer
     * @throws java.lang.IllegalStateException - if this SensorConnection
     * is already closed
     */
    public void setDataListener(DataListener listener, int bufferSize) {
        setDataListener(listener, bufferSize, 0L, false, false, false);
    }

    /**
     * Registers a DataListener to receive collected data asynchronously.
     *
     * @param listener - the listener to be registered
     * @param bufferSize - the size of the buffer of the data values, bufferSize &lt; 1
     * means the size is left undefined
     * @param bufferingPeriod - the time in milliseconds to buffer values inside
     * one Data object. bufferingPeriod &lt; 1 means the period is left undefined.
     * @param isTimestampIncluded - if true timestamps should be included in
     * returned Data objects
     * @param isUncertaintyIncluded - if true uncertainties should be included
     * in returned Data objects
     * @param isValidityIncluded - if true validities should be included in
     * returned Data objects
     * @throws java.lang.NullPointerException - if the listener is null
     * @throws java.lang.IllegalArgumentException - if the bufferSize
     * and the bufferingPeriod both are &lt; 1 or if bufferSize exceeds
     * the maximum size of the buffer
     * @throws java.lang.IllegalStateException - if this SensorConnection is already closed
     */
    public void setDataListener(DataListener listener,
                            int bufferSize,
                            long bufferingPeriod,
                            boolean isTimestampIncluded,
                            boolean isUncertaintyIncluded,
                            boolean isValidityIncluded) {

        if ((bufferSize < 1 && bufferingPeriod < 1) ||
            bufferSize > maxBufferSize) {
            throw new IllegalArgumentException(
                    "Wrong buffer size or/and period values");
        }

        int state = eventQueue.getState();
        if (state == STATE_CLOSED) {
            throw new IllegalStateException("Connection is closed");
        }

        if (listener == null) {
            throw new NullPointerException("Listener is null");
        }

        if (state == STATE_LISTENING) {
            removeDataListener();
        }

        eventQueue.setState(STATE_LISTENING);

        if (bufferSize < 1) {
            bufferSize = maxBufferSize;
        }

        /* Sending signals to each channel to start getting data */
        long startTime = System.currentTimeMillis();
        this.listener = listener;
        channelCount = 0;
        channelStatus = ValueListener.DATA_READ_OK;
        eventQueue.setStateData(StatesEvents.WAIT_DATA);
        retData = new DataImpl[channels.length];
        for (int i = 0; i < channels.length; i++) {
            channels[i].startGetData(this, bufferSize, bufferingPeriod,
                isTimestampIncluded, isUncertaintyIncluded,
                isValidityIncluded, true, startTime);
        }
    }

    /**
     * Calls data listener.
     *
     */
    void callDataListener() {
        new Thread(new CallDataListener(this, listener, retData)).start();
    }

    /**
     * Calls data error listener.
     *
     */
    void callDataErrorListener()
    {
        new Thread(new CallDataListener(this, errorTimestamp,
            listener, channelStatus)).start();
    }
    
    public void close() throws IOException {
        sensorDevice.finishSensor(); // ignore the success flag
        if (eventQueue.getState() == STATE_LISTENING)
        {
            removeDataListener();
        }
        for (int i = 0; i < channels.length; i++)
        {
            channels[i].stopChannel();
        }
        eventQueue.stop();
        eventQueue.setState(STATE_CLOSED);
    }

    /**
     * Gets ChannelDevice instance (i3tests only).
     *
     * @param number channel number
     * @return ChannelDevice instance
     */
    ChannelDevice getChannelDevice(int number) {
        ChannelDevice device = null;
        if (0 <= number && number < channels.length) {
            device = channels[number].getChannelDevice();
        }
        return device;
    }

    /** Smart conversion to String. It's a debugging means.
     *
      * @return human-readable representation
     */
    public String toString() { // IMPL_NOTE: this is needed only for debugging.N
        return super.toString()+"{ quantity="+quantity+" contextType="+contextType+" model="+model
                +" prop:location="+props.getProperty(PROP_LOCATION)+"}";
    }

    /**
     * Inform SensorDevice to start sending availability informations.
     *
     * @param listener which will receive the notifications
     */
    public void startMonitoringAvailability(AvailabilityListener listener) {
        sensorDevice.startMonitoringAvailability(listener);
    }

    /**
     * Inform SensorDevice to stop sending availability informations.
     *
     * @param listener which will stop receiving the notifications
     */
    public void stopMonitoringAvailability(AvailabilityListener listener) {
        sensorDevice.stopMonitoringAvailability();
    }

    /**
     * Gets the sensor error codes.
     *
     * @return array of error codes specified for the given sensor 
     */
    public int[] getErrorCodes() {
        int[] retV = new int[errorCodes.size()];
        if (retV.length > 0) {
            Enumeration enumErrCodes = errorCodes.keys();
            for (int i = 0; enumErrCodes.hasMoreElements(); i++) {
                retV[i] = ((Integer)(enumErrCodes.nextElement())).intValue();
            }
        }
        return retV;
    }

    /**
     * Gets the error description.
     *
     * @param errorCode code of the error
     * @return description of error
     */
    public String getErrorText(int errorCode) {
        Integer errCodeObject = new Integer(errorCode);
        if (!errorCodes.containsKey(errCodeObject)) {
            throw new IllegalArgumentException("Wrong error code");
        }
        return (String)errorCodes.get(errCodeObject);
    }

    /**
     * Sets the notify flag.
     */
    synchronized void setNotify() {
        isNotify = true;
        notify();
    }

    /**
     * Gets the sensor number.
     */
    int getNumber() {
        return number;
    }

    /**
     * Stops getting data from channels.
     */
    void stopGetData() {
        channelCount = 0;
        for (int i = 0; i < channels.length; i++) {
            channels[i].stopGetData();
        }
    }

    /**
     * Checks if all channels sent data or confirmation.
     * 
     * @retun true when all channels sent data else false
     */
    boolean isAllChannelsAnswered() {
        return (++channelCount == channels.length);
    }
}

class CallDataListener implements Runnable {
    /** Error flag. */
    private boolean isError = false;

    /** Sensor link. */
    private Sensor sensor;

    /** Data array which read from sensor. */
    private DataImpl[] retData;

    /** Error timestamp. */
    private long errorTimestamp;

    /** Listener for notification about data receiving. */
    private DataListener listener;

    /** Last channel status. */
    private int channelStatus;

    /**
     * Initialization for data listening.
     */
    CallDataListener(Sensor sensor, DataListener listener, DataImpl[] retData) {
        this.sensor = sensor;
        this.listener = listener;
        this.retData = retData;
    }

    /**
     * Initialization for error listening.
     */
    CallDataListener(Sensor sensor, long errorTimestamp, DataListener listener,
        int channelStatus) {
        this.sensor = sensor;
        this.errorTimestamp = errorTimestamp;
        this.listener = listener;
        this.channelStatus = channelStatus;
        isError = true;
    }

    /**
     * Run the listener.
     */
    public void run() {
        if (listener == null) {
            return;
        }

        try {
            if (isError) {
                ((DataAndErrorListener)listener).errorReceived((SensorConnection)sensor,
                    channelStatus, errorTimestamp);
            } else {
                listener.dataReceived(sensor, retData, false);
            }
        } catch (Exception ex) { // user exception - ignore
        }
    }
}
