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

package com.sun.midp.events;

/**
 * A listener interface for preprocessing and receiving MIDP events.
 * <p>
 * Each system that wants to receive events implements an EventListener and
 * registers the listener with the EventQueue for each event type to be
 * received. Registration will require a system issued security token to
 * prevent unauthorized registrations, to get this token the system will
 * implement a static method to be called at VM startup. The EventListener
 * interface has methods for pre-processing
 * an event and receiving an event. The pre-processing method gets
 * called in the Java thread that is inserting an event when the event is the
 * type that it was registered for with a reference to the event to inserted
 * and the previously inserted event (if there is one). If the pre-processing
 * method merges the events and does not want does not want the new event
 * inserted into the queue, then it will return false. The receiving method
 * gets called in the event delivery thread with the first event of the type
 * of it registered for in the queue.</p>
 */
public interface EventListener {
    /**
     * Preprocess an event that is being posted to the event queue.
     * This method will get called in the thread that posted the event.
     * 
     * @param event event being posted
     *
     * @param waitingEvent previous event of this type waiting in the
     *     queue to be processed
     * 
     * @return true to allow the post to continue, false to not post the
     *     event to the queue
     */
    public boolean preprocess(Event event, Event waitingEvent);

    /**
     * Process an event.
     * This method will get called in the event queue processing thread.
     *
     * @param event event to process
     */
    public void process(Event event);
}
