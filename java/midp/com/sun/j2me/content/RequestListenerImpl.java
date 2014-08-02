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

package com.sun.j2me.content;

import javax.microedition.content.RequestListener;

/**
 * Thread to monitor pending invocations and notify a listener
 * when a matching one is present.
 */
class RequestListenerImpl implements Runnable, Counter {

    /** ContenHandlerImpl for which this is listening. */
    private final ContentHandlerImpl handler;

    /** The active thread processing the run method. */
    private Thread thread;
    
    int stopFlag = 0;

    /**
     * Create a new listener for pending invocations.
     *
     * @param handler the ContentHandlerImpl to listen for
     * @param listener the listener to notify when present
     */
    RequestListenerImpl(ContentHandlerImpl handler, RequestListener listener) {
		this.handler = handler;
		setListener(listener);
    }

    /**
     * Set the listener to be notified and start/stop the monitoring
     * thread as necessary.
     * If the listener is non-null make sure there is a thread active
     * to monitor it.
     * If there is no listener, then stop the monitor thread.
     * Unblock any blocked threads so they can get the updated listener.
     * @param listener the listener to update
     */
    void setListener(RequestListener listener) {

		if (listener != null) {
		    // Ensure a thread is running to watch for it
		    if (thread == null || !thread.isAlive()) {
				thread = new Thread(this);
				thread.start();
		    }
		} else {
		    // Forget the thread doing the listening; it will exit
		    thread = null;
		}
	
		/*
		 * Reset notified flags on pending requests.
		 * Unblock any threads waiting to notify current listeners
		 */
		InvocationStore.setListenNotify(handler.applicationID, true);
		// stop thread
		stopFlag++;
		InvocationStore.cancel();
    }

    /**
     * The run method checks for pending invocations for a
     * desired ContentHandler or application.
     * When an invocation is available the listener is
     * notified.
     */
    public void run() {
		Thread mythread = Thread.currentThread();
		while (mythread == thread) {
		    // Wait for a matching invocation
		    boolean pending = InvocationStore.listen(handler.applicationID, true, true, this);
		    if (pending) {
		    	handler.requestNotify();
		    }
		}
    }

	public int getCounter() {
		return stopFlag;
	}
}
