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

import java.util.Vector;

/**
 * An implementation of an EventListener that records the events passed to its 
 * process() and preprocess() methods.
 */
class InstrumentedEventListener implements EventListener {

    /** The value preprocess() should return. */
    boolean preprocess;

    /** Accumulates events passed to process(). */
    Vector processedEvents;

    /** Accumulates events passed to preprocess(). */
    Vector preprocessedEvents;

    /** Accumulates the waitingEvents passed to preprocess(). */
    Vector preprocessedWaitingEvents;

    /**
     * Creates a new InstrumentedEventListener, setting
     * the return value of its preprocess() method to be
     * newPreprocess.
     */
    public InstrumentedEventListener(boolean newPreprocess) {
        processedEvents = new Vector();
        preprocessedEvents = new Vector();
        preprocessedWaitingEvents = new Vector();
        preprocess = newPreprocess;
    }

    /**
     * Creates a new InstrumentedEventListener whose preprocess()
     * method returns true.
     */
    public InstrumentedEventListener() {
        this(true);
    }

    /**
     * The preprocess() method of EventListener.  Records the event and 
     * the waitingEvent in the corresponding vectors.  Returns the current 
     * value of preprocess.
     */
    public boolean preprocess(Event event, Event waitingEvent) {
        preprocessedEvents.addElement(event);
        preprocessedWaitingEvents.addElement(waitingEvent);
        return preprocess;
    }

    /**
     * Sets the return value of the preprocess() method.
     */
    public void setPreprocess(boolean p) {
        preprocess = p;
    }

    /**
     * The process() method of EventListener.  Simply records the given event 
     * in the processedEvents vector.
     */
    public void process(Event event) {
        processedEvents.addElement(event);
    }

    /**
     * Returns an array of events recorded by the process() method.
     */
    public Event[] getProcessedEvents() {
        return getArray(processedEvents);
    }

    /**
     * Returns an array of events recorded by the preprocess() method.
     */
    public Event[] getPreprocessedEvents() {
        return getArray(preprocessedEvents);
    }
    
    /**
     * Returns an array of waiting events recorded by the preprocess() method.
     * Note that there will likely be null elements within this array.
     */
    public Event[] getWaitingEvents() {
        return getArray(preprocessedWaitingEvents);
    }
    
    /**
     * Returns an array of events, given a vector of events.
     */
    Event[] getArray(Vector v) {
        Event eva[] = new Event[v.size()];
        v.copyInto(eva);
        return eva;
    }

}
