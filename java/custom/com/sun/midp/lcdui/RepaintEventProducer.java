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

package com.sun.midp.lcdui;

import com.sun.midp.events.EventTypes;
import com.sun.midp.events.Event;
import com.sun.midp.events.EventListener;
import com.sun.midp.events.EventQueue;

import com.sun.midp.security.Permissions;
import com.sun.midp.security.SecurityToken;

import javax.microedition.lcdui.Display;

/**
 * This class performs all of the repaint event handling.
 */
public class RepaintEventProducer implements EventListener {

    /** Cached reference to the MIDP event queue. */
    private EventQueue eventQueue;

    /** Repaint event free for reuse. */
    private RepaintEvent pooledEvent1;
    /** Repaint event free for reuse. */
    private RepaintEvent pooledEvent2;
    /** Repaint event free for reuse. */
    private RepaintEvent pooledEvent3;

    /**
     * Repaint event in the queue or waiting to be moved to the processed
     * slot. Events are rotated to different slots in
     * <code>preprocessRepaints</code>.
     */
    private RepaintEvent queuedEvent;

    /**
     * Repaint event being processed (queue has event also) or
     * has been processed (no event in the queue).
     */
    private RepaintEvent eventInProcess;

    /**
     * The constructor LCDUI repaint events handler.
     *
     * @param theEventQueue the event queue
     */
    public RepaintEventProducer(EventQueue theEventQueue) {
        
        eventQueue = theEventQueue;

        pooledEvent1 = RepaintEvent.createRepaintEvent(null, 0, 0, 0, 0, null);
        pooledEvent2 = RepaintEvent.createRepaintEvent(null, 0, 0, 0, 0, null);
        pooledEvent3 = RepaintEvent.createRepaintEvent(null, 0, 0, 0, 0, null);
        
        eventQueue.registerEventListener(EventTypes.REPAINT_EVENT, this);
    }

    /**
     * Called to schedule a repaint of the current Displayable
     * as soon as possible.
     *
     * @param d     The Display
     * @param x     The x coordinate of the origin of the repaint rectangle
     * @param y     The y coordinate of the origin of the repaint rectangle
     * @param w     The width of the repaint rectangle
     * @param h     The height of the repaint rectangle
     * @param target An optional target Object, which may have been the
     *               original requestor for the repaint
     */
    public void scheduleRepaint(DisplayEventConsumer d,
                                int x, int y, int w, int h, Object target) {
        RepaintEvent freeEvent;

        synchronized (this) {
            freeEvent = pooledEvent1;

            freeEvent.setRepaintFields(d, x, y, w, h, target);

            if (queuedEvent == null) {
                /*
                 * Since the queue does not lock posting during event
                 * processing, we need to rotate 3 repaint events for reuse.
                 *
                 * 1 for the queued event, 1 for the event begin processed
                 * and 1 for temporary use when merging a new event with
                 * a queued event.
                 *
                 * Event pooling is done because game can generate upto 15
                 * repaints a second and cause a lot garbage collection if
                 * we created new repaint every time.
                 */
                pooledEvent1 = pooledEvent2;
                pooledEvent2 = pooledEvent3;
                pooledEvent3 = freeEvent;

                queuedEvent = freeEvent;

                eventQueue.post(queuedEvent);
            } else if (queuedEvent.display != d) {
                /*
                 * A new display has come to the foreground so don't
                 * bother paint the display in the queued event.
                 *
                 * Overwrite this fields in queue event with new values.
                 */
                 queuedEvent.setRepaintFields(d, x, y, w, h, target);
            } else {
                /*
                 * When there is a pending repaint
                 * union the dirty regions into one event
                 */
                if (queuedEvent.paintX1 > freeEvent.paintX1) {
                    queuedEvent.paintX1 = freeEvent.paintX1;
                }

                if (queuedEvent.paintY1 > freeEvent.paintY1) {
                    queuedEvent.paintY1 = freeEvent.paintY1;
                }

                if (queuedEvent.paintX2 < freeEvent.paintX2) {
                    queuedEvent.paintX2 = freeEvent.paintX2;
                }
                
                if (queuedEvent.paintY2 < freeEvent.paintY2) {
                    queuedEvent.paintY2 = freeEvent.paintY2;
                }

                queuedEvent.paintTarget = null;
            }
        }
    }

    /**
     * Preprocess an event that is being posted to the event queue.
     * 
     * @param event event being posted
     *
     * @param waitingEvent previous event of this type waiting in the
     *     queue to be processed
     * 
     * @return true to allow the post to continue, false to not post the
     *     event to the queue
     */
    public boolean preprocess(Event event, Event waitingEvent) {
        /*
         * Because of the needs of serviceRepaints the preprocessing
         * is done in scheduleRepaint.
         */
        return true;
    }

    /**
     * Process an event.
     *
     * @param genericEvent event to process
     */
    public void process(Event genericEvent) {
        RepaintEvent event = (RepaintEvent)genericEvent;

        synchronized (this) {
            queuedEvent = null;
            eventInProcess = event;
        }

        /*
         * Target DisplayEventConsumer is obtained directly from event field.
         * Assumed that target consumer is not null.
         */
        event.display.handleRepaintEvent(
                event.paintX1, event.paintY1, 
                event.paintX2, event.paintY2, 
                event.paintTarget);

        synchronized (this) {
            /* Change the ID here to signal waitForRepaint. */
            eventInProcess.perUseID++;
            
            eventInProcess = null;

            // ServiceRepaints may be blocking.
            notifyAll();
        }
    }

    /**
     * Called to block the calling MIDlet until the the pending repaint
     * operation is performed.
     * <p>
     * Does not return until the pending repaint (if any)
     * has been processed.</p>
     *
     */
    public void serviceRepaints() {
        if (EventQueue.isDispatchThread()) {
            Event event;

            if (eventInProcess != null) {
                /*
                 * We are in the midst of a calling the application's
                 * paint method, avoid recursion, simply return
                 */
                return;
            }

            /*
             * Since we are in the dispatch thread, at this point
             * there can only one repaint in the queue so remove it
             * from the queue and process it.
             */
            event = eventQueue.remove(EventTypes.REPAINT_EVENT);

            /* Do not hold a lock when calling out to the application */
            if (event != null) {
                process(event);
            }
        } else {
            /*
             * We only want paint events to be process in the dispatch
             * to avoid deadlocks. So wait for the repaint event to occur.
             */
            waitForCurrentRepaintEvents();
        }
    }

    /**
     * Wait for the event queue to process all of the events currently
     * in the queue but not any new ones after the start of this
     * method.
     */
    private void waitForCurrentRepaintEvents() {
        RepaintEvent eventToWaitFor = null;
        int currentEventUseID;

        synchronized (this) {
            if (queuedEvent != null) {
                eventToWaitFor = queuedEvent;
            } else if (eventInProcess != null) {
                eventToWaitFor = eventInProcess;
            } else {
                /* Nothing to wait for, done. */
                return;
            }

            currentEventUseID = eventToWaitFor.perUseID;
            while (eventToWaitFor.perUseID == currentEventUseID) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                    /* The application wants this thread to unblock */
                    break;
                }
            }
                
        }
    }    
}
