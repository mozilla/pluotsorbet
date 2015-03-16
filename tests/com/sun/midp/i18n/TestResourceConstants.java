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

package com.sun.midp.i18n;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestResourceConstants implements Testlet {
    public int getExpectedPass() { return 410; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    /**
     * Runs all the tests.
     */
    public void test(TestHarness th) {
        th.check(Resource.getString(ResourceConstants.DONE), "Done");
        th.check(Resource.getString(ResourceConstants.ABSTRACTIONS_PIM_TODO), "PIM to-do list");

        int first = ResourceConstants.DONE;
        int last = ResourceConstants.ABSTRACTIONS_PIM_TODO;

        while (first <= last) {
            th.check(Resource.getString(first).length() > 0);
            first++;
        }
    }

}
