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

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestOne implements Testlet {
    public int getExpectedPass() { return 480; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    TestHarness th;

    // utilities

    Item[] createManyItems(int n) {
        Item ar[] = new Item[n];
        for (int i = 0; i < n; i++) {
            ar[i] = new StringItem(null, null);
        }
        return ar;
    }

    // assertions

    void checkItems(Form f, int expectedNumOfItems) {
        th.check(f.items != null);
        th.check(expectedNumOfItems, f.numOfItems);
        th.check(f.items.length >= f.numOfItems);

        // check ownership of all items
        for (int i = 0; i < f.numOfItems; i++) {
            th.check(f, f.items[i].owner);
        }

        // check that the tail of the array is all nulls
        for (int i = f.numOfItems; i < f.items.length; i++) {
            th.check(f.items[i] == null);
        }
    }

    // the tests

    public void testAppend() {
        Form f = new Form("testAppend");
        f.append(new StringItem("one", null));
        checkItems(f, 1);
    }

    public void testAppendIndex() {
        final int COUNT = 7;
        Item ia[] = createManyItems(COUNT);
        Item newItem = new StringItem("new", null);
        Form f = new Form("testAppendIndex", ia);
        int ix = f.append(newItem);
        th.check(COUNT, ix);
        checkItems(f, COUNT+1);
    }

    public void testConstruct0() {
        Form f = new Form("testConstruct0");
        checkItems(f, 0);
    }

    public void testConstructN() {
        final int COUNT = 35;
        Item ia[] = createManyItems(COUNT);
        Form f = new Form("testConstructN", ia);
        checkItems(f, COUNT);
    }

    public void testDelete0() {
        final int COUNT = 18;

        Item ia[] = createManyItems(COUNT);
        Form f = new Form("testDelete0", ia);
        checkItems(f, COUNT);

        f.delete(0);
        checkItems(f, COUNT-1);
        th.check(ia[0].owner == null);

        // check that items after the deleted one are moved properly
        for (int i = 0; i < COUNT-1; i++) {
            th.check(ia[i+1], f.items[i]);
        }
    }

    public void testDeleteK() {
        final int COUNT = 20;
        final int DELITEM = 10;

        Item ia[] = createManyItems(COUNT);
        Form f = new Form("testDeleteK", ia);
        checkItems(f, COUNT);

        f.delete(DELITEM);
        checkItems(f, COUNT-1);
        th.check(ia[DELITEM].owner == null);

        // check that items before the deleted one are undisturbed
        for (int i = 0; i < DELITEM; i++) {
            th.check(ia[i], f.items[i]);
        }

        // check that items after the deleted one are moved properly
        for (int i = DELITEM; i < COUNT-1; i++) {
            th.check(ia[i+1], f.items[i]);
        }
    }

    public void testDeleteN() {
        final int COUNT = 15;

        Item ia[] = createManyItems(COUNT);
        Form f = new Form("testDeleteN", ia);
        checkItems(f, COUNT);

        f.delete(COUNT-1);
        checkItems(f, COUNT-1);
        th.check(ia[COUNT-1].owner == null);

        // check that items before the deleted one are undisturbed
        for (int i = 0; i < COUNT-1; i++) {
            th.check(ia[i], f.items[i]);
        }
    }

    public void testDeleteAll() {
        final int COUNT = 17;

        Item ia[] = createManyItems(COUNT);
        Form f = new Form("testDeleteAll", ia);
        checkItems(f, COUNT);

        f.deleteAll();
        checkItems(f, 0);

        for (int i = 0; i < COUNT; i++) {
            th.check(ia[i].owner == null);
        }
    }

    public void testInsert0() {
        final int COUNT = 13;

        Item ia[] = createManyItems(COUNT);
        Form f = new Form("testInsert0", ia);
        checkItems(f, COUNT);
        Item newItem = new StringItem("new", null);
        f.insert(0, newItem);

        checkItems(f, COUNT+1);
        th.check(newItem, f.items[0]);

        // check items after insertion are moved properly
        for (int i = 0; i < COUNT; i++) {
            th.check(ia[i], f.items[i+1]);
        }
    }

    public void testInsertK() {
        final int COUNT = 17;
        final int INSIDX = 4;

        Item ia[] = createManyItems(COUNT);
        Form f = new Form("testInsertK", ia);
        checkItems(f, COUNT);
        Item newItem = new StringItem("new", null);
        f.insert(INSIDX, newItem);

        checkItems(f, COUNT+1);
        th.check(newItem, f.items[INSIDX]);

        // check items before insertion are undisturbed
        for (int i = 0; i < INSIDX; i++) {
            th.check(ia[i], f.items[i]);
        }

        // check items after insertion are moved properly
        for (int i = INSIDX; i < COUNT; i++) {
            th.check(ia[i], f.items[i+1]);
        }
    }

    public void testInsertN() {
        final int COUNT = 23;

        Item ia[] = createManyItems(COUNT);
        Form f = new Form("testInsertN", ia);
        checkItems(f, COUNT);
        Item newItem = new StringItem("new", null);
        f.insert(COUNT, newItem);

        checkItems(f, COUNT+1);
        th.check(newItem, f.items[COUNT]);

        // check items before insertion are undisturbed
        for (int i = 0; i < COUNT; i++) {
            th.check(ia[i], f.items[i]);
        }
    }

    // main test driver

    public void test(TestHarness th) {
        this.th = th;

        testAppend();
        testAppendIndex();
        testConstruct0();
        testConstructN();
        testDelete0();
        testDeleteK();
        testDeleteN();
        testDeleteAll();
        testInsert0();
        testInsertK();
        testInsertN();
    }
}
