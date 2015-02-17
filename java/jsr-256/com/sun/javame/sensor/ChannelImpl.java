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

import java.util.*;
import javax.microedition.sensor.*;

/**
 * Pair contains condition and its listener.
 *
 * The vector of pairs contaons conditions which awaiting the met
 * and listener for execute when condition is met.
 */
class ConditionListenerPair {
    private ConditionListener listener;
    private Condition condition;
    private Data data;
    
    public ConditionListenerPair(ConditionListener listener, Condition condition) {
        this.listener = listener;
        this.condition = condition;
    }
    
    public Condition getCondition() {
        return condition;
    }
    
    public ConditionListener getListener() {
        return listener;
    }
    
    public void setData(Data data) {
        this.data = data;
    }
    
    public Data getData() {
        return data;
    }
    
    public boolean equals(ConditionListenerPair pair) {
        return ((this.listener == pair.listener) && (this.condition == pair.condition));
    }
    
    public boolean matches(ConditionListener listener) {
        return (this.listener == listener);
    }
    
    public boolean matches(ConditionListener listener, Condition condition) {
        return ((this.listener == listener) && (this.condition == condition));        
    }
}

public class ChannelImpl implements Channel, ChannelInfo{

    private class ChannelEventQueue implements Runnable {

        /** Channel message queue. */
        private Vector messages = new Vector();

        /** Stop flag. */
        private boolean isStop = true;

        /** Flag of notification. */
        private boolean isNotify;

        /** Thread of event queue. */
        private Thread eventQueueThread;

        /** Channel state for data collecting */
        private int stateData = StatesEvents.CHANNEL_IDLE;

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
                        case StatesEvents.START_GET_DATA: // start data collecting from sensor
                            if (stateData == StatesEvents.CHANNEL_IDLE) {
                                stateData = StatesEvents.CHANNEL_WAIT_DATA;
                                startDataCollection();
                            }
                            break;
                        case StatesEvents.RESPONSE_DATA: // data arrived from device
                             switch (stateData) {
                                case StatesEvents.CHANNEL_WAIT_DATA: // add data
                                    if (addData()) { // need more data
                                        channelDevice.startGetData(channelControl);
                                    } else { // return data to sensor
                                        stateData = StatesEvents.CHANNEL_IDLE;
                                        retDataToSensor();
                                        if (isRepeat()) {
                                            stateData = StatesEvents.CHANNEL_WAIT_DATA;
                                            startDataCollection();
                                        }
                                    }
                                    break;
                                case StatesEvents.WAIT_STOP_DATA: // send confirmation
                                    confirmStopData();
                                    stateData = StatesEvents.CHANNEL_IDLE;
                                    break;
                            }
                            break;
                        case StatesEvents.RESPONSE_ERROR: // error arrived from device
                            switch (stateData) {
                                case StatesEvents.CHANNEL_WAIT_DATA: // send error to sensor
                                    reportErrorData();
                                    break;
                            }
                            break;
                        case StatesEvents.STOP_GET_DATA: // stop collecting data
                            switch (stateData) {
                                case StatesEvents.CHANNEL_IDLE: // send confirmation
                                    confirmStopData();
                                    break;
                                case StatesEvents.CHANNEL_WAIT_DATA:
                                    stateData = StatesEvents.WAIT_STOP_DATA;
                                    break;
                            }
                            break;
                    }
                }
            }
        }
    } //end of private class declaration


    /** Measurement ranges array. */
    private MeasurementRange[] ranges;

    /** Channel's name. */
    private String name;

    /** Sensor's number. */
    private int sensorsNumber;

    /** Channel's number. */
    private int number;

    /** Channel's data type. */
    private int dataType;

    /** Channel's scale. */
    private int scale;

    /** Channel's unit. */
    private Unit unit;

    /** Channel's accuracy. */
    private float accuracy;

    /** Channel's condition array. */
    private Vector conditions = new Vector();

    /** Channel's met condition array. */
    private Vector conditionsMet = new Vector();

    /** Channel device instance. */
    private ChannelDevice channelDevice;

    /** Sensor instance. */
    private Sensor sensor;

    /** Return data. */
    private DataImpl retData;

    /** End time of data collecting. */
    private long endTime;

    /** Current value of read data items. */
    private int readItems;

    /** Channel control. */
    private ChannelControl channelControl;

    /** Listener dor data notification. */
    private ChannelDataListener listener;

    /** Buffer size for data items. */
    private int buffersize;

    /** Maximal time for data measuring. */
    private long bufferingPeriod;

    /** Flag of timestamping. */
    private boolean isTimestampIncluded;

    /** Flag of uncertainty including. */
    private boolean isUncertaintyIncluded;

    /** Flag of validity including. */
    private boolean isValidityIncluded;

    /** Repeating flag. */
    private boolean isRepeat;

    /** Sensor event queue. */
    private ChannelEventQueue eventQueue;
    
    /**
     * Creates a new instance of ChannelImpl.
     *
     * @param num number of sensor based 0
     * @param number number of channel based 0
     */
    public ChannelImpl(int num, int number) {
        this.sensorsNumber = num;
        this.number = number;
        initFields();
        eventQueue = new ChannelEventQueue();
    }

    /**
     * Initializes the processing of message queue.
     */
    boolean initChannel() {
        eventQueue.start();
        return true;
    }

    /**
     * Stops the processing of message queue.
     */
    void stopChannel() {
        eventQueue.stop();
    }

    /**
     * Gets the channel device instance.
     *
     * @return the channel device instance
     */
    ChannelDevice getChannelDevice() {
        return channelDevice;
    }

    /**
     * Gets the array of channel's conditions.
     *
     * @return the array of channel's conditions
     */
    synchronized Condition[] getAllConditions() {
        Condition[] conds = null;
        if (conditions.size() > 0) {
            conds = new Condition[conditions.size()];
            for (int i = 0; i < conds.length; i++) {
                conds[i] = ((ConditionListenerPair)conditions.elementAt(i)).getCondition();
            }
       }
        return conds;
    }

    /* 
     * ChannelInfo methods 
     */


    /**
     * Returns the accuracy of this channel.
     *
     * @return the accuracy of the channel of the sensor
     */
    public float getAccuracy() {
        return accuracy;
    }

    /**
     * Returns the data type of the channel.
     *
     * @return the data type of the channel
     */
    public int getDataType() {
        return dataType;
    }

    /**
     * This method returns all the measurement ranges
     * of this channel of the sensor.
     *
     * @return all measurement ranges of the channel
     */
    public MeasurementRange[] getMeasurementRanges() {
        MeasurementRange[] retValue = new MeasurementRange[0];
        if (dataType != TYPE_OBJECT && ranges != null
            && ranges.length > 0) {
            MeasurementRange curRange;
            retValue = new MeasurementRange[ranges.length];
            for (int i = 0; i < ranges.length; i++) {
                curRange = ranges[i];
                retValue[i] = new MeasurementRange(curRange.getSmallestValue(),
                        curRange.getLargestValue(), curRange.getResolution());
            }
        }
        return retValue;
    }

    /**
     * Returns the name of the channel.
     *
     * @return the name of the channel
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the scale used for the measurement values of this channel.
     *
     * @return scale
     */
    public int getScale() {
        return scale;
    }

    /**
     * Returns the unit, in which data values are presented.
     *
     * @return the unit, in which data values of the channel are presented
     */
    public Unit getUnit() {
        return unit;
    }

    /*
     * Channel methods 
     */

    /**
     * Sets a Condition object to be monitored.
     *
     * @param listener - the ConditionListener to which the
     * ConditionListener.conditionMet() notifications are sent
     * @param condition - the Condition object defining the
     * condition to be monitored.
     * @throws java.lang.NullPointerException - if the listener is null
     * or in the case of the application is not automatically launched
     * by the push mechanism and the condition is null
     * @throws java.lang.IllegalArgumentException - if the data type
     * of the channel is TYPE_INT or TYPE_DOUBLE and an ObjectCondition
     * is passed in; or if the data type is TYPE_OBJECT and a LimitCondition
     * or a RangeCondition is passed in
     * @throws java.lang.IllegalStateException - if the SensorConnection
     * is in the STATE_CLOSED state
     */
    public synchronized void addCondition(ConditionListener listener, Condition condition) {
        if (listener == null) {
            throw new NullPointerException();
        }

        if (sensor.getState() == SensorConnection.STATE_CLOSED) {
            throw new IllegalStateException();
        }

        if (condition == null) { //IMPL_NOTE: determine here is application is 
                                 //automatically launched by push mechanism
            throw new NullPointerException();
        }

        /* MUST BE IMPLEMENTED: handle push */

        boolean isObjCond = condition instanceof ObjectCondition;
        if ((isObjCond && (dataType != TYPE_OBJECT)) || (!isObjCond && (dataType == TYPE_OBJECT))) {
            throw new IllegalArgumentException();
        }
                int count = conditions.size();
        ConditionListenerPair pair = new ConditionListenerPair(listener, condition);
        for (int i = 0; i < count; i++) {
            if (((ConditionListenerPair)conditions.elementAt(i)).equals(pair)) {
                return;
            }
        }
        conditions.addElement(pair);
                channelDevice.startGetData(channelControl);
        }

    /*
     * ValueListener methods
     */

    /**
     * Object value from channel has been received.
     *
     * @param number the channel number
     * @param value the object value
     * @param uncertainty the uncertainty of data
     * @param validity the validity of data
     */
    public void condValueReceived(int number, Object[] value, float[] uncertainty,
                              boolean[] validity) {
                if (conditions.size() == 0) {
            return;
        }
        boolean isNumeric = value instanceof Double[] || value instanceof Integer[];
        double[] doubleValue = null;
        int dataIndex = 0;
        if (isNumeric) {
            doubleValue = new double[value.length];
            for ( dataIndex = 0; dataIndex < value.length; dataIndex ++ ) {
                if (value instanceof Double[]) {
                    doubleValue[dataIndex] = ((Double)value[dataIndex]).doubleValue();
                } else if (value instanceof Integer[]) {
                    doubleValue[dataIndex] = ((Integer)value[dataIndex]).doubleValue();
                }
            }
        }
        ConditionListenerPair currPair;
        Condition currCond;
        int dataType = ChannelInfo.TYPE_OBJECT;
        if (value instanceof Double[]) {
            dataType = ChannelInfo.TYPE_DOUBLE;
        } else if (value instanceof Integer[]) {
            dataType = ChannelInfo.TYPE_INT;
        }
        boolean wasMet = false;
        Enumeration en = conditions.elements();
        while (en.hasMoreElements()) {
            boolean conMet = false;
            currPair = (ConditionListenerPair)en.nextElement();
            currCond = currPair.getCondition();
            DataImpl data = new DataImpl(this, 1, dataType, true, true, true);
            for ( dataIndex = 0; dataIndex < value.length; dataIndex ++ ){
                if ( isNumeric && currCond.isMet(doubleValue[dataIndex])){
                    conMet = true;
                    break;
                }
            }
            if ( conMet ) {
                wasMet = true;
                data.setData(0, value[dataIndex]);
                data.setTimestamp(0, System.currentTimeMillis());
                data.setUncertainty(0, uncertainty[dataIndex]);
                data.setValidity(0, validity[dataIndex]);
                currPair.setData(data);
                conditionsMet.addElement(currPair);
                conditions.removeElement(currPair);
            }
        }
        if (wasMet) {
                        new Thread(new RunConditionMet(sensor, this)).start();
        }
        if (conditions.size() > 0) { // some conditions were not met
                        channelDevice.startGetData(channelControl); 
        }
    }

    /**
     * Gets the condition listener pair.
     *
     * @return condition listener pair instance
     */
    synchronized ConditionListenerPair getCondPair() {
        ConditionListenerPair returnValue = null;
        if (conditionsMet.size() > 0) {
            returnValue = (ConditionListenerPair)conditionsMet.firstElement();
            conditionsMet.removeElementAt(0);
        }
        return returnValue;
    }

    /**
     * Wrong data reading.
     *
     * @param number the channel number
     * @param errorCode the code error of data reading
     */
    public void dataReadError(int number, int errorCode) { // ignore
    }

    /**
     * Returns the ChannelInfo object associated with the Channel.
     *
     * The ChannelInfo contains the properties of the channel data.
     *
     * @return a ChannelInfo object
     */
    public ChannelInfo getChannelInfo() {
        return this;
    }

    /**
     * Returns the Condition objects set for the given listener.
     *
     * @param listener - the ConditionListener whose Condition
     * objects are requested
     * @return the Condition objects set for the listener object.
     * A zero-length Condition array is returned if the given
     * listener has no Condition objects.
     * @throws java.lang.NullPointerException - if the listener is null
     */
    public synchronized Condition[] getConditions(ConditionListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        
        Vector mv = new Vector();
        ConditionListenerPair currPair;
        Enumeration en = conditions.elements();
        while (en.hasMoreElements()) {
            currPair = (ConditionListenerPair)en.nextElement();
            if (currPair.matches(listener)) {
                mv.addElement(currPair.getCondition());
            }
        }
        
        Condition[] conds = new Condition[mv.size()];
        for (int i = 0; i < conds.length; i++) {
            conds[i] = (Condition)mv.elementAt(i);
        }
        
        return conds;
    }

    /**
     * This method returns a string identifying the channel
     * and listing all its unique conditions.
     *
     * @return an URL of channel
     */
    public java.lang.String getChannelUrl() {
        return ChannelUrl.createUrl(this);
    }

    /**
     * Removes all Condition and ConditionListener objects
     * registered in this Channel.
     *
     * @throws java.lang.IllegalStateException - if the SensorConnection
     * is in the STATE_CLOSED state
     */
    public synchronized void removeAllConditions() {
        if (sensor.getState() == SensorConnection.STATE_CLOSED) {
            throw new IllegalStateException();
        }
        conditions.removeAllElements();
        conditionsMet.removeAllElements();
    }

    /**
     * Removes a given Condition and ConditionListener object pair
     * from this Channel.
     *
     * @param listener - the ConditionListener whose Condition
     * will be removed
     * @param condition - the Condition to be removed
     * @throws java.lang.NullPointerException - if either of the
     * parameters is null
     * @throws java.lang.IllegalStateException - if the SensorConnection
     * is in the STATE_CLOSED state
     */
    public synchronized void removeCondition(ConditionListener listener, Condition condition) {
        if ((listener == null) || (condition == null)) {
            throw new NullPointerException();
        }

        if (sensor.getState() == SensorConnection.STATE_CLOSED) {
            throw new IllegalStateException();
        }

        boolean isMatch = false;
        ConditionListenerPair currPair;
        Enumeration en = conditions.elements();
        while (en.hasMoreElements()) {
            currPair = (ConditionListenerPair)en.nextElement();
            if (currPair.matches(listener, condition)) {
                conditions.removeElement(currPair);                
                isMatch = true;
                break;     // Only one pair can exist in the Vector according to the spec
            }
        }
        if (!isMatch) {
            en = conditionsMet.elements();
            while (en.hasMoreElements()) {
                currPair = (ConditionListenerPair)en.nextElement();
                if (currPair.matches(listener, condition)) {
                    conditionsMet.removeElement(currPair);                
                    break;     // Only one pair can exist in the Vector according to the spec
                }
            }
        }
    }

    /**
     * Removes a given ConditionListener and all Condition objects
     * associated with it.
     *
     * @param listener - the ConditionListener to be removed
     * @throws java.lang.NullPointerException - if the listener is null
     * @throws java.lang.IllegalStateException - if the SensorConnection
     * is in the STATE_CLOSED state
     */
    public synchronized void removeConditionListener(ConditionListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        if (sensor.getState() == SensorConnection.STATE_CLOSED) {
            throw new IllegalStateException();
        }

        for (int i = 0; i < conditions.size(); i++) {
            if (((ConditionListenerPair)conditions.elementAt(i)).matches(listener)) {
                conditions.removeElementAt(i);
                i--;        // Elements are shifted so we have to check the same index again
            }
        }
    }

    /*
     * Internal methods
     */

    /**
     * Sets the parent sensor instance.
     *
     * @param sensor - the parent sensor instance
     */
    void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * Starts the collecting data from channel.
     *
     * @param listener - the listener for data modification
     * @param buffersize - the maximal buffer size for data
     * @param bufferingPeriod - the maximal time for data
     * collecting or unlimited in case of &lt; 1
     * @param isTimestampIncluded - add timestamp on true
     * @param isUncertaintyIncluded - add uncertaintyIncluded on true
     * @param isValidityIncluded - add validity on true
     * @param isRepeat - repeat reading data on true
     * @param startTime - time of starting measurement
     */
    void startGetData(ChannelDataListener listener, int buffersize,
                      long bufferingPeriod, boolean isTimestampIncluded,
                      boolean isUncertaintyIncluded,
                      boolean isValidityIncluded,
                      boolean isRepeat, long startTime) {
        
        this.listener = listener;
        this.buffersize = buffersize;
        this.bufferingPeriod = bufferingPeriod;
        this.isTimestampIncluded = isTimestampIncluded;
        this.isUncertaintyIncluded = isUncertaintyIncluded;
        this.isValidityIncluded = isValidityIncluded;
        this.isRepeat = isRepeat;
        if (bufferingPeriod > 0) {
            endTime = startTime + bufferingPeriod;
        }
                eventQueue.putMessage(StatesEvents.START_GET_DATA);
    }

    private void initFields() {
        ChannelModel channelModel = new ChannelModel(); 
        doGetChannelModel(sensorsNumber, number, channelModel); 
        name = channelModel.name;
        dataType = channelModel.dataType;
        accuracy = channelModel.accuracy;
        scale = channelModel.scale;
        unit = Unit.getUnit(channelModel.unit);
        ranges = channelModel.getMeasurementRanges(); 
        channelDevice = DeviceFactory.generateChannel(sensorsNumber, number);
    }
    
    /**
     * Prepare to collecting data from channel.
     *
     */
    void startDataCollection() {
        retData = new DataImpl(this, buffersize,
                    getDataType(), isTimestampIncluded,
                    isUncertaintyIncluded, isValidityIncluded);
        readItems = 0;
        channelControl = new ChannelControl(this,
            channelDevice, isTimestampIncluded,
            isUncertaintyIncluded, isValidityIncluded);
        channelDevice.startGetData(channelControl);
    }
    
    /**
     * Add data from device to buffer.
     *
     * @return true when more data is need,
     * else false
     */
    boolean addData() {
        long currTimeStamp = 0L;
        if (isTimestampIncluded) {
            currTimeStamp = channelControl.getTimestamp();
        }
        if (bufferingPeriod > 0) { // check timeout
            if (isTimestampIncluded) {
                if (endTime < currTimeStamp) {
                    return false;
                }
            } else {
                // checking timeout when !isTimestampIncluded
                if (endTime < System.currentTimeMillis()) {
                    return false;
                }
            }
        }
        Object[] data = channelControl.getReadData();
        if (data == null) {
            return false;
        }
        int i = readItems;
        int copyLen = data.length;
        if (copyLen < 1) {
            return false;
        }
        if (i + copyLen > buffersize) {
            readItems = buffersize;
            copyLen = buffersize - i;
        } else {
            readItems += copyLen;
        }
        for ( int dataNum = 0; dataNum < copyLen; dataNum ++ ){
            // checking timeout
            retData.setData( i + dataNum, data[dataNum] );
            if (isTimestampIncluded) {
                retData.setTimestamp(i + dataNum, currTimeStamp);
            }
            if (isUncertaintyIncluded) {
                retData.setUncertainty(i + dataNum,
                    channelControl.getReadUncertainty()[dataNum]);
            }
            if (isValidityIncluded) {
                retData.setValidity(i + dataNum,
                    channelControl.getReadValidity()[dataNum]);
            }
        } 
        if (readItems >= buffersize) { // buffer is full
            return false;
        }
        return true;
    }

    /**
     * Stops the collecting data from channel.
     *
     */
    void stopGetData() {
        eventQueue.putMessage(StatesEvents.STOP_GET_DATA);
    }

    private native void doGetChannelModel(int sensorNumber, int channelNumber,
            ChannelModel m);

    /**
     * Puts the message into event queue.
     *
     */
    void putMessage(int msg) {
        eventQueue.putMessage(msg);
    }

    /**
     * Returns data to sensor.
     *
     */
    void retDataToSensor() {
        listener.channelDataReceived(number, retData);
    }

    /**
     * Confirms the stopping of data processing.
     *
     */
    void confirmStopData() {
        sensor.confirmStopData(number);
    }

    /**
     * Reports about error data.
     *
     */
    void reportErrorData() {
        listener.channelErrorReceived(number, channelControl.getErrorCode(),
            channelControl.getTimestamp());
    }

    /**
     * Checks is need to repeat reading data.
     *
     */
    boolean isRepeat() {
        return isRepeat;
    }
}
class ChannelControl implements ValueListener {

    /** Channel instance. */
    private ChannelImpl channel;

    /** Channel device instance. */
    private ChannelDevice channelDevice;

    /** Dava value. */
    private Object[] dataValue;

    /** Timerstamp. */
    private long timeStamp;

    /** Uncertainty. */
    private float[] uncertainty;

    /** Validity. */
    private boolean[] validity;


    /** Timestamp including flag. */
    private boolean isTimestampIncluded;

    /** Uncertainty including flag. */
    private boolean isUncertaintyIncluded;

    /** Validity including flag. */
    private boolean isValidityIncluded;

    /** Error code. */
    private int errorCode;

   /**
     * Initialization.
     *
     * @param channel - ChannelImpl instance
     * @param channelDevice - ChannelDevice instance
     * @param isTimestampIncluded - if true timestamps should be
     *  included in returned Data objects
     * @param isUncertaintyIncluded - if true uncertainties should be
     *  included in returned Data objects
     * @param isValidityIncluded - if true validities should be
     *  included in returned Data objects
     */
    ChannelControl(ChannelImpl channel, ChannelDevice channelDevice,
        boolean isTimestampIncluded,
        boolean isUncertaintyIncluded, boolean isValidityIncluded) {
        this.channel = channel;
        this.channelDevice = channelDevice;
        this.isTimestampIncluded = isTimestampIncluded;
        this.isUncertaintyIncluded = isUncertaintyIncluded;
        this.isValidityIncluded = isValidityIncluded;
        errorCode = ValueListener.DATA_READ_OK;
    }

   /**
     * Gets data that has been read.
     *
     * @return data object from channel
     */
    synchronized Object[] getReadData() {
        return dataValue;
    }

    /**
      * Gets last error code.
      *
      * @return data object from channel
      */
     synchronized int getErrorCode() {
         return errorCode;
     }

    /**
      * Gets time stamp that has been read.
      *
      * @return time stamp from channel
      */
     synchronized long getTimestamp() {
         return timeStamp;
     }

    /**
      * Gets uncertainty that has been read.
      *
      * @return uncertainty from channel
      */
     synchronized float[] getReadUncertainty() {
         return uncertainty;
     }

    /**
      * Gets validity that has been read.
      *
      * @return validity from channel
      */
     synchronized boolean[] getReadValidity() {
         return validity;
     }

    /**
     * Object value from channel has been received.
     *
     * @param number the channel number
     * @param value the object value
     * @param uncertainty the uncertainty of data
     * @param validity the validity of data
     */
    public synchronized void valueReceived(int number, Object[] value, float[] uncertainty,
                              boolean[] validity) {
                // Check for conditions
                channel.condValueReceived(number, value, uncertainty, validity);
                dataValue = value;
        if (isTimestampIncluded) {
              timeStamp = System.currentTimeMillis();
        }
        if (isUncertaintyIncluded) {
            this.uncertainty = uncertainty;
        }
        if (isValidityIncluded) {
            this.validity = validity;
        }
        channel.putMessage(StatesEvents.RESPONSE_DATA);
    }

    /**
     * Wrong data reading.
     *
     * @param number the channel number
     * @param errorCode the code error of data reading
     */
    public synchronized void dataReadError(int number, int errorCode) {
                this.errorCode = errorCode;
        this.validity = new boolean[dataValue.length]; // all is false
        channel.putMessage(StatesEvents.RESPONSE_ERROR);
    }
} //end of main class declaration

class RunConditionMet implements Runnable {
    private Sensor sensor;
    private ChannelImpl channel;

    RunConditionMet(Sensor sensor, ChannelImpl channel) {
        this.sensor = sensor;
        this.channel = channel;
    }

    public void run() {
        ConditionListenerPair pair;
        while ((pair = channel.getCondPair()) != null) {
            try {
                pair.getListener().conditionMet(sensor, pair.getData(), pair.getCondition());
            } catch (Exception exc) { // user exception - ignore
            }
        }
    }
}
