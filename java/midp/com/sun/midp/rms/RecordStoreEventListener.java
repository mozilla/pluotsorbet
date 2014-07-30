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

package com.sun.midp.rms;

import com.sun.midp.events.EventListener;
import com.sun.midp.events.Event;
import com.sun.midp.events.NativeEvent;
import com.sun.midp.events.EventTypes;
import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;
import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.Permissions;
import com.sun.midp.main.MIDletSuiteUtils;

/**
 * Listener for asynchronous record store change events like adding, deleting or
 * changing of record store record. Asynchronous record store events are used to
 * notify MIDP record store listeners registered in a different (possibly isolated)
 * execution contexts. 
 */
public class RecordStoreEventListener implements EventListener {

    /** Security token to access restricted APIs */
    private SecurityToken securityToken; 
    /** Event consumer for record store change events */
    private RecordStoreEventConsumer recordStoreEventConsumer;

    /** Constructs record store listener instance */
    public RecordStoreEventListener(
            SecurityToken token, RecordStoreEventConsumer consumer) {
        securityToken = token;
        recordStoreEventConsumer = consumer;
    }

    /**
     * Preprocess asynchronous record store change event. Not used now but
     * can be useful to glue together multiple events about the same change
     * of record store
     * @param event record store change event
     * @param waitingEvent previous record store change event waiting in the
     *     queue to be processed
     * @return true to allow the post to continue, false to not post the
     *     event to the queue
     */
    public boolean preprocess(Event event, Event waitingEvent) {
        return true;
    }

    /**
     * Processes asynchronous record store change event calling
     * registered event consumer with event paramters
     * @param event async record store change event
     */
    public void process(Event event) {
        if (recordStoreEventConsumer != null) {
            NativeEvent nativeEvent = (NativeEvent) event;
            if (event.getType() == EventTypes.RECORD_STORE_CHANGE_EVENT) {
                int suiteId = nativeEvent.intParam1;
                int changeType = nativeEvent.intParam2;
                int recordId = nativeEvent.intParam3;
                int requiresAcknowledgment = nativeEvent.intParam4;
                String recordStoreName = nativeEvent.stringParam1;
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_RMS,
                        "RecordStoreEventListener.process(): " +
                            "suiteId = " + suiteId + ", "  +
                            "storeName = " + recordStoreName + ", " +
                            "changeType = " + changeType + ", " +
                            "recordId = " + recordId + ", " +
                            "acknowledge = " + requiresAcknowledgment);
                }

                recordStoreEventConsumer.handleRecordStoreChange(
                    suiteId, recordStoreName, changeType, recordId);

                if (requiresAcknowledgment == 1) {
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION, LogChannels.LC_RMS,
                            "Acknowledge record store notifications delivery");
                    }
                    RecordStoreRegistry.acknowledgeRecordStoreNotifications(
                        securityToken);
                }
            }
        }
    }
}
