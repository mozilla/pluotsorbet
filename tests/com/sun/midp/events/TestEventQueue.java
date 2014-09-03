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

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

/**
 * Unit tests for the EventQueue class.
 */
public class TestEventQueue implements Testlet {
    TestHarness th;

    /**
     * Test simple creation of an event queue.
     */
    void testCreate() {
        EventQueue eq = new EventQueue();

        th.check(!eq.alive);
        th.check(eq.dispatchTable != null);
        th.check(eq.dispatchTable.length > 0);
        th.check(eq.pool != null);
        th.check(-1, eq.nativeEventQueueHandle);
        th.check(eq.eventQueueThread != null);
        th.check(eq.eventMonitorThread != null);
        th.check(!eq.eventQueueThread.isAlive());
        th.check(!eq.eventMonitorThread.isAlive());
    }

    /**
     * Tests the ability to register an event.
     */
    void testRegister() {
        final int EVENT_TYPE = 7;
        EventQueue eq = new EventQueue();
        InstrumentedEventListener iel = new InstrumentedEventListener();
        eq.registerEventListener(EVENT_TYPE, iel);

        DispatchData dd = eq.dispatchTable[EVENT_TYPE-1];
        th.check(dd != null);
        th.check(iel, dd.listener);
    }

    /**
     * Tests whether the dispatch table is grown properly.
     */
    void testGrowDispatchTable() {
        final int EVENT_TYPE_A = 4;
        final int EVENT_TYPE_B = 97;
            // must be larger than the dispatch table default size

        EventQueue eq = new EventQueue();

        th.check(EVENT_TYPE_B > eq.dispatchTable.length);

        InstrumentedEventListener iela = new InstrumentedEventListener();
        InstrumentedEventListener ielb = new InstrumentedEventListener();

        eq.registerEventListener(EVENT_TYPE_A, iela);
        eq.registerEventListener(EVENT_TYPE_B, ielb);

        DispatchData dda = eq.dispatchTable[EVENT_TYPE_A-1];
        th.check(dda != null);
        th.check(iela, dda.listener);

        DispatchData ddb = eq.dispatchTable[EVENT_TYPE_B-1];
        th.check(ddb != null);
        th.check(ielb, ddb.listener);
    }

    /**
     * Tests posting of an event.
     */
    void testPost1() {
        final int EVENT_TYPE = 14;
        EventQueue eq = new EventQueue();

        InstrumentedEventListener iel = new InstrumentedEventListener();
        eq.registerEventListener(EVENT_TYPE, iel);

        Event ev = new Event(EVENT_TYPE);
        eq.post(ev);

        // assertions on the event queue

        th.check(ev == eq.nextEvent);
        th.check(ev == eq.lastEvent);

        // assertions from the event listener

        Event[] arr;

        arr = iel.getProcessedEvents();
        th.check(0, arr.length);

        arr = iel.getPreprocessedEvents();
        th.check(1, arr.length);
        th.check(ev == arr[0]);

        arr = iel.getWaitingEvents();
        th.check(1, arr.length);
        th.check(arr[0] == null);
    }

    /**
     * Tests posting of three events.
     */
    void testPost3() {
        final int EVENT_TYPE_A = 5;
        final int EVENT_TYPE_B = 7;

        EventQueue eq = new EventQueue();

        InstrumentedEventListener iel = new InstrumentedEventListener();
        eq.registerEventListener(EVENT_TYPE_A, iel);
        eq.registerEventListener(EVENT_TYPE_B, iel);

        Event ev0 = new Event(EVENT_TYPE_A);
        Event ev1 = new Event(EVENT_TYPE_B);
        Event ev2 = new Event(EVENT_TYPE_A);
        eq.post(ev0);
        eq.post(ev1);
        eq.post(ev2);

        // assertions on the event queue

        th.check(ev0 == eq.nextEvent);
        th.check(ev2 == eq.lastEvent);
        th.check(ev1 == ev0.next);
        th.check(ev2 == ev1.next);
        th.check(ev2.next == null);

        // assertions from the event listener

        Event[] arr;

        arr = iel.getProcessedEvents();
        th.check(0, arr.length);

        arr = iel.getPreprocessedEvents();
        th.check(3, arr.length);
        th.check(ev0 == arr[0]);
        th.check(ev1 == arr[1]);
        th.check(ev2 == arr[2]);

        arr = iel.getWaitingEvents();
        th.check(3, arr.length);
        th.check(arr[0] == null);
        th.check(arr[1] == null);
        th.check(ev0, arr[2]);
    }

    /**
     * Tests preprocessing of events.
     */
    void testPreprocess() {
        EventQueue eq = new EventQueue();
        InstrumentedEventListener iel = new InstrumentedEventListener(true);
        final int EVENT_TYPE = 10;

        eq.registerEventListener(EVENT_TYPE, iel);

        Event ev0 = new Event(EVENT_TYPE);
        Event ev1 = new Event(EVENT_TYPE);
        eq.post(ev0);
        iel.setPreprocess(false);
        eq.post(ev1);

        // assertions on the event queue

        th.check(ev0 == eq.nextEvent);
        th.check(ev0 == eq.lastEvent);
        th.check(ev0.next == null);

        // assertions from the event listener

        Event[] arr;

        arr = iel.getProcessedEvents();
        th.check(0, arr.length);

        arr = iel.getPreprocessedEvents();
        th.check(2, arr.length);
        th.check(ev0 == arr[0]);
        th.check(ev1 == arr[1]);

        arr = iel.getWaitingEvents();
        th.check(2, arr.length);
        th.check(arr[0] == null);
        th.check(ev0, arr[1]);
    }

    /**
     * Runs all tests.
     */
    public void test(TestHarness th) {
        this.th = th;

        testCreate();
        testRegister();
        testGrowDispatchTable();
        testPost1();
        testPost3();
        testPreprocess();
    }

}
