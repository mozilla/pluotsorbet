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

package javax.microedition.lcdui;

/**
 * Postponed task timer class is designed to postpone too frequent
 * requests.
 *
 * IMPL_NOTE: The methods cancel() and schedule() are to be as fast
 *   as possible, so method run() has simplified synchronization
 *   that enables cancelled task to be done on thread wake up.
 */
abstract class LFTimer implements Runnable {

    /** Invalidate timer states */
    final static int DEAD = -1;      // Timer thread is not started
    final static int ACTIVATED = -2; // Timer is activated to perform the task on wakeup
    final static int IDLE = -3;      // The task is not scheduled since
                                     //   timer is either done, or cancelled
    /**
     * State of the timer.
     * A positive value is one more timer state meaning
     * the time to wait to process inavlidate request.
     */
    private long state = DEAD;

    private final Object monitor;


    public LFTimer(Object monitor) {
        this.monitor = monitor;
    }

    /**
     * Cancel postponed task.
     * If task timer thread is started, it will be stopped on wake up.
     */
    void cancel() {
        synchronized(monitor) {
            if (state != DEAD) {
                state = IDLE;
            }
        }
    }

    /** Wait until postponed task can be done or cancelled */
    public void run() {
        while (true) {
            long sleepTime;
            synchronized(monitor) {
                if (state > 0) {
                    sleepTime = state;
                    state = ACTIVATED;
                } else {
                    // Terminate timer thread
                    state = DEAD;
                    return;
                }
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ie) {
                // Consider interruption as wakeup
            }
            if (state == ACTIVATED){
                doTask();
            }
        }
    }

    /**
     * Schedule postponed task to be done later,
     * start timer thread if it has not been started yet.
     * @param time time to postpone the task for
     */
    void schedule(long time) {
        synchronized (monitor) {
            if (state == IDLE) {
                state = time;
            } else if (state == DEAD) {
                state = time;
                new Thread(this).start();
            }
        }
    }

    /** Process scheduled task. */
    private void doTask() {
        synchronized (monitor) {
            // While monitor was awaited, the state could be changed
            if (state == ACTIVATED) {
                perform();
                state = IDLE;
            }
        }
    }

    abstract protected void perform();
}
