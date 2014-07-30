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

package com.sun.midp.rms;

import com.sun.midp.configurator.Constants;
import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.Permissions;
import com.sun.midp.events.EventQueue;
import com.sun.midp.events.NativeEvent;
import com.sun.midp.events.EventTypes;
import com.sun.midp.main.MIDletSuiteUtils;

/**
 * A class represents registry of record stores withing current execution context.
 * Execution context can be understood e.g. as VM task in multitasking environment.
 * In the case record store is accessed concurrently from a few execution contexts,
 * listeners registered to be called on record store changing must be notified on
 * changes done to the record store in any of these execution contexts.
 * RecordStoreRegistry is responsible for system wide notifications about
 * changes of record stores done in a different execution contexts.
 */
public class RecordStoreRegistry {

    /**
     * Registers listener and consumer of record store change events
     * @param token security token to restrict usage of the method
     * @param consumer record store events consumer
     */
    public static void registerRecordStoreEventConsumer(
            SecurityToken token, RecordStoreEventConsumer consumer) {

        token.checkIfPermissionAllowed(Permissions.MIDP);
        EventQueue eventQueue = EventQueue.getEventQueue(token);
        RecordStoreEventListener recordEventListener =
            new RecordStoreEventListener(token, consumer);
        eventQueue.registerEventListener(
            EventTypes.RECORD_STORE_CHANGE_EVENT, recordEventListener);
    }

    /**
     * Starts listening of asynchronous changes of record store
     *
     * @param token security token to restrict usage of the method
     * @param suiteId suite ID of record store to start listen for
     * @param storeName name of record store to start listen for
     */
    public static void startRecordStoreListening(
            SecurityToken token, int suiteId, String storeName) {
        token.checkIfPermissionAllowed(Permissions.MIDP);
        startRecordStoreListening(suiteId, storeName);
    }

    /**
     * Stops listening of asynchronous changes of record store
     *
     * @param token security token to restrict usage of the method
     * @param suiteId suite ID of record store to stop listen for
     * @param storeName name of record store to stop listen for
     */
    public static void stopRecordStoreListening(
            SecurityToken token, int suiteId, String storeName) {
        token.checkIfPermissionAllowed(Permissions.MIDP);
        stopRecordStoreListening(suiteId, storeName);
    }

    /**
     * Sends asynchronous notification about change of record store done
     * in the current execution context of method caller
     *
     * @param token security token to restrict usage of the method
     * @param suiteId suite ID of changed record store
     * @param storeName name of changed record store
     * @param changeType type of record change: ADDED, DELETED or CHANGED
     * @param recordId ID of changed record
     * @see #notifyRecordStoreChangeImpl
     */
    public static void notifyRecordStoreChange(
            SecurityToken token, int suiteId, String storeName,
            int changeType, int recordId) {
        token.checkIfPermissionAllowed(Permissions.MIDP);
        notifyRecordStoreChangeImpl(token, suiteId, storeName, changeType, recordId);
    }

    /**
     * Acknowledges delivery of record store notifications
     * @param token security token to restrict usage of the method
     */
    public static void acknowledgeRecordStoreNotifications(SecurityToken token) {
        token.checkIfPermissionAllowed(Permissions.MIDP);
        int taskId = MIDletSuiteUtils.getIsolateId();
        acknowledgeNotificationsDelivery(taskId);
    }

    /**
     * Native implementation of #startRecordStoreListening
     * @param suiteId suite ID of record store to start listen for
     * @param storeName name of record store to start listen for
     */
    private static native void startRecordStoreListening(
        int suiteId, String storeName);

    /**
     * Native implementation of #stopRecordStoreListening
     * @param suiteId suite ID of record store to stop listen for
     * @param storeName name of record store to stop listen for
     */
    private static native void stopRecordStoreListening(
        int suiteId, String storeName);

    /**
     * Native implementation of #notifyRecordStoreChange
     * @param suiteId suite ID of changed record store
     * @param storeName name of changed record store
     * @param changeType type of record change: ADDED, DELETED or CHANGED
     * @param recordId ID of changed record
     */
    private static native void sendRecordStoreChangeEvent(
        int suiteId, String storeName, int changeType, int recordId);

    /**
     * Gets list of pairs <task ID, counter> for each VM task listening for changes
     * of given record store. The pair consists of ID of VM task, and of number of
     * record store notifications been sent to this VM task and not acknowledged by
     * it yet. The number of not acknowledged record store notifications must be
     * regarded by notification sender to not overflow event queue of the reciever
     * VM task.
     *
     * @param suiteId suite ID of record store
     * @param storeName name of record store
     * @returns list of <task ID, notification counter> pairs,
     *   or null in the case there are no VM tasks listening for this record store 
     */
    private static native int[] getRecordStoreListeners(
        int suiteId, String storeName);

    /**
     * Acknowledges delivery of record store notifications sent to VM task earlier.
     * The acknowledgment is required by notifications sender for each series of
     * notification events to be sure a reciever can process the notifications and
     * its queue won't be overflowen.
     *
     * @param taskId ID of VM task
     */
    private static native void acknowledgeNotificationsDelivery(int taskId);


    /**
     * Resets record store notification counter for given VM task.
     * The notification counter is used to request notifications reciever to
     * acknowledge delivery of a series of notifications. Resetting of the
     * counter can be done either after the acknowledgment, or to not wait
     * for an acknowledgment in abnormal situations.
     * 
     * @param taskId VM task ID
     */
    private static native void resetNotificationCounter(int taskId);

    /**
     * Detects ID of the listener VM task that didn't acknowledge delivery of
     * record store notifications and therefore will cause blocking of sender
     * for predefined timeout period
     *
     * @param listeners list of pairs <ID, counter> for each VM task with
     *   registered record store listeneres inside
     * @return ID of the VM task that didn't acknowledge notifications delivery,
     *   or -1 if there is no such VM task in the list
     */
    private static int checkRecordStoreListeners(int[] listeners) {
        for (int i = 0; i < listeners.length; i+= 2) {
            int id = listeners[i];
            int count = listeners[i+1];
            if (count > Constants.RECORD_STORE_NOTIFICATION_QUEUE_SIZE) {
                return id;
            }
        }
        return -1;
    }

    /**
     * Sends asynchronous notification about change of record store done in the
     * current execution context of method caller.
     *
     * It is possible that not all recievers of the notification can accept it
     * immediately, e.g. because of no place in event queue, or due to other reasons.
     * To protect sender from being blocked and to not discard notification messages
     * the implementation requires from recievers to send acknoledgement message
     * on each series of N notifications. Whether acknowledgment is not recieved
     * by sender it tries to wait for it. If not acknowled reports the problem to
     * AMS then.
     *
     * @param token security token to restrict usage of the method
     * @param suiteId suite ID of changed record store
     * @param storeName name of changed record store
     * @param changeType type of record change, can be ADDED, DELETED or CHANGED
     * @param recordId ID of the changed record
     */
    private static void notifyRecordStoreChangeImpl(
            SecurityToken token,
            int suiteId, String storeName,
            int changeType, int recordId) {

        int attempt = 0;
        int prevBlockerId = -1;
        boolean readyToSend = false;
        EventQueue eventQueue = EventQueue.getEventQueue(token);

        while (!readyToSend) {
            int[] listeners = getRecordStoreListeners(suiteId, storeName);
            if (listeners == null) {
                return;
            }

            int blockerId = checkRecordStoreListeners(listeners);
            readyToSend = (blockerId == -1);
            if (!readyToSend) {

                // There is reciever that cannot accept notification
                // Limit number of retry attempts
                if (attempt > Constants.RECORD_STORE_NOTIFICATION_ATTEMPTS) {
                    int amsId = MIDletSuiteUtils.getAmsIsolateId();
                    int senderId = MIDletSuiteUtils.getIsolateId();
                    NativeEvent evt = new NativeEvent(
                        EventTypes.RECORD_STORE_FAILED_NOTIFICATION_EVENT);
                    evt.intParam1 = senderId;
                    evt.intParam2 = -1;
                    eventQueue.sendNativeEventToIsolate(evt, amsId);

                    // Don't discard the notification, send it even if not all
                    // recievers can accept it properly. It's up to AMS to deal
                    // with it on failure report.  
                    readyToSend = true;

                } else {
                    if (blockerId == prevBlockerId) {
                        // Repeated attempt to send notification after timeout
                        // failed due to the same blocker reciever, report the
                        // failure to AMS 
                        int amsId = MIDletSuiteUtils.getAmsIsolateId();
                        int senderId = MIDletSuiteUtils.getIsolateId();
                        NativeEvent evt = new NativeEvent(
                            EventTypes.RECORD_STORE_FAILED_NOTIFICATION_EVENT);
                        evt.intParam1 = senderId;
                        evt.intParam2 = blockerId;
                        eventQueue.sendNativeEventToIsolate(evt, amsId);

                        // Unblock reciever to retry notification sending ignoring
                        // reciever problems. It's up to AMS to deal with it on
                        // failure report.
                        resetNotificationCounter(blockerId);
                        
                    } else {

                        // Reciever didn't acknowledge delivery of the previous
                        // notifications, sleep predefined timeout waiting for
                        // the acknowledgment and retry sending
                        try {
                            prevBlockerId = blockerId;
                            Thread.sleep(
                                Constants.RECORD_STORE_NOTIFICATION_TIMEOUT);
                        } catch(InterruptedException ie) {}

                    }
                    // Retry to send notification
                    attempt++;
                }
            }
        }

        // Send notification to all recievers in other VM tasks
        sendRecordStoreChangeEvent(
            suiteId, storeName, changeType, recordId);
    }

    /**
     * Shutdowns record store registry for this VM task
     * @param token security token to restrict usage of the method 
     */
    public static void shutdown(SecurityToken token) {
        token.checkIfPermissionAllowed(Permissions.MIDP);
        int taskId = MIDletSuiteUtils.getIsolateId();
        stopAllRecordStoreListeners(taskId);
    }

    /**
     * Stops listening for any record store changes in VM task
     * @param taskId ID of VM task
     */
    private static native void stopAllRecordStoreListeners(int taskId);

}
