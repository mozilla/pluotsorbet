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

/**
 * StatesEvents contains constants for different states and events
 * of sensors and channels.
 */
class StatesEvents {

    /**
     * Sensor's states.
     *
     * Sensor can be in one of states: SENSOR_IDLE, WAIT_DATA, WAIT_CLOSE_DATA and
     * WAIT_CLOSE_CONTINUE. Please see states descriptions below.
     */

    /** 
     * SENSOR_IDLE state.
     * 
     * Sensor is in SENSOR_IDLE state when it doesn't wait any data from its channels.
     */
    static final int SENSOR_IDLE = 0;

    /** 
     * WAIT_DATA state.
     * 
     * Sensor is in WAIT_DATA state when it requested data from channels and wait
     * responses from them. 
     */
    static final int WAIT_DATA = 1;

    /** 
     * WAIT_CLOSE_DATA state.
     * 
     * Sensor is in WAIT_CLOSE_DATA state when it requested to stop data collection
     * for channels and wait confirmation from channels about it.
     */
    static final int WAIT_CLOSE_DATA = 2;

    /** 
     * WAIT_CLOSE_CONTINUE state.
     * 
     * Sensor is in WAIT_CLOSE_CONTINUE state when it requested to stop data collection
     * for channels and plans to start new data collection.
     */
    static final int WAIT_CLOSE_CONTINUE = 3;

    /**
     * Sensor's events.
     *
     * Sensor can receive events from user: GET_DATA, STOP_GET_DATA.
     * Events received from channels are: IND_DATA and STOP_GET_DATA_CONF.
     * Event from sensor to user is USER_IND_DATA.
     * Events from sensor to channel is START_GET_DATA, STOP_GET_DATA.
     * Events description are placed below.
     */

    /** 
     * GET_DATA event.
     * 
     * Sensor receives GET_DATA event from user when user requests data from sensor.
     */
    static final int GET_DATA = 0;

    /** 
     * STOP_GET_DATA event.
     * 
     * Sensor receives STOP_GET_DATA event from user when user wants to interrupt
     * data collecting from sensor.
     */
    static final int STOP_GET_DATA = 1;

    /** 
     * IND_DATA event.
     * 
     * Sensor receives IND_DATA event from channel when channel finished
     * data collecting.
     */
    static final int IND_DATA = 2;

    /** 
     * STOP_GET_DATA_CONF event.
     * 
     * Sensor receives STOP_GET_DATA_CONF event from channel when channel
     * finished interrupting data collecting.
     */
    static final int STOP_GET_DATA_CONF = 3;

    /** 
     * USER_IND_DATA event.
     * 
     * Sensor sends USER_IND_DATA event to user when data collection has finished.
     */
    static final int USER_IND_DATA = 4;

    /** 
     * START_GET_DATA event.
     * 
     * Sensor sends START_GET_DATA event to channel when sensor requests data collection.
     */
    static final int START_GET_DATA = 5;

    /** 
     * IND_ERROR event.
     * 
     * Sensor receives IND_ERROR event from channel on error
     * data collecting.
     */
    static final int IND_ERROR = 6;

    /** 
     * State-event table for sensor.
     * 
     * -----------------------------------------------------------------------------------------------
     * event\state        SENSOR_IDLE output event   to      WAIT_DATA           output event   to     
     * -----------------------------------------------------------------------------------------------
     * GET_DATA           WAIT_DATA   START_GET_DATA channel WAIT_CLOSE_CONTINUE STOP_GET_DATA  channel
     * STOP_GET_DATA      SENSOR_IDLE                        WAIT_CLOSE_DATA     STOP_GET_DATA  channel
     * IND_DATA           SENSOR_IDLE                        SENSOR_IDLE         USER_IND_DATA  user
     * STOP_GET_DATA_CONF SENSOR_IDLE                        WAIT_DATA     
     *
     * -----------------------------------------------------------------------------------------------
     * event\state        WAIT_CLOSE_DATA output event to WAIT_CLOSE_CONTINUE output event   to     
     * -----------------------------------------------------------------------------------------------
     * GET_DATA           WAIT_CLOSE_DATA                 WAIT_CLOSE_CONTINUE 
     *                    (save new data                  (save new data 
     *                     collection parameters)          collection parameters)
     * STOP_GET_DATA      WAIT_CLOSE_DATA                 WAIT_CLOSE_CONTINUE
     * IND_DATA           WAIT_CLOSE_DATA                 WAIT_CLOSE_CONTINUE
     * STOP_GET_DATA_CONF SENSOR_IDLE                     WAIT_DATA           START_GET_DATA channel
     */

    /**
     * Channel's states.
     *
     * Channel can be in one of states: CHANNEL_IDLE, CHANNEL_WAIT_DATA, WAIT_STOP_DATA.
     * Please see states descriptions below.
     */

    /** 
     * CHANNEL_IDLE state.
     * 
     * Sensor is in CHANNEL_IDLE state when it doesn't collect any data.
     */
    static final int CHANNEL_IDLE = 0;

    /** 
     * CHANNEL_WAIT_DATA state.
     * 
     * Sensor is in CHANNEL_WAIT_DATA state when it waiting data from device.
     */
    static final int CHANNEL_WAIT_DATA = 1;

    /** 
     * WAIT_STOP_DATA state.
     * 
     * Sensor is in WAIT_STOP_DATA state when it waiting data from device for ignore it.
     */
    static final int WAIT_STOP_DATA = 2;

    /**
     * Channel's events.
     *
     * Channel receives events from sensor: START_GET_DATA, STOP_GET_DATA.
     * Channel sends events to sensor: IND_DATA and STOP_GET_DATA_CONF.
     * Channel receives event from device: RESPONSE_DATA and RESPONSE_ERROR.
     * Channel sends event to device: REQUEST_DATA.
     */

    /** 
     * RESPONSE_DATA event.
     * 
     * Device sends RESPONSE_DATA event to channel when data
     * is ready.
     */
    static final int RESPONSE_DATA = 6;

    /** 
     * RESPONSE_ERROR event.
     * 
     * Device sends RESPONSE_ERROR event to channel on any 
     * device's problem.
     */
    static final int RESPONSE_ERROR = 7;

    /** 
     * State-event table for channel.
     * 
     * -----------------------------------------------------------------------------------------------
     * event\state    CHANNEL_IDLE output event       to     CHANNEL_WAIT_DATA output event to     
     * -----------------------------------------------------------------------------------------------
     * START_GET_DATA CHANNEL_WAIT_DATA REQUEST_DATA  device CHANNEL_WAIT_DATA 
     * STOP_GET_DATA  CHANNEL_IDLE STOP_GET_DATA_CONF sensor WAIT_STOP_DATA 
     * RESPONSE_DATA  CHANNEL_IDLE                           CHANNEL_WAIT_DATA REQUEST_DATA device
     *                                                       (when need more data)
     * RESPONSE_DATA  CHANNEL_IDLE                           CHANNEL_IDLE      IND_DATA     sensor
     *                                                       (when data collecting is finished)
     *
     * -----------------------------------------------------------------------------------------------
     * event\state    WAIT_STOP_DATA output event       to
     * -----------------------------------------------------------------------------------------------
     * START_GET_DATA WAIT_STOP_DATA
     * STOP_GET_DATA  WAIT_STOP_DATA
     * RESPONSE_DATA  CHANNEL_IDLE   STOP_GET_DATA_CONF sensor
     */

}
