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

package com.sun.midp.io;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestUrl implements Testlet {
    public int getExpectedPass() { return 28; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    TestHarness th;

    private void testUrl(String url, boolean isCorrect) {
        try {
            HttpUrl httpUrl = new HttpUrl(url);
            if (isCorrect) {
                th.check(true);
            } else {
                th.fail("No IllegalArgumentException for url " + url);
            }
        } catch (IllegalArgumentException ex) {
            if (isCorrect) {
                th.fail("Wrong exception " + ex + " for url " + url);
            } else {
                th.check(true);
            }
        } catch (Exception ex) {
            th.fail("Wrong exception " + ex + " for url " + url);
        }
    }

    /**
     * Runs all the tests.
     */
    public void test(TestHarness th) {
        this.th = th;

        // IPv6 - true
        String url = "http://[FEDC:BA98:7654:3210:FEDC:BA98:7654:3210]";
        testUrl(url, true);
        url = "http://[1080:0:0:0:8:800:200C:417A]";
        testUrl(url, true);
        url = "http://[0:0:0:0:0:0:0:0]";
        testUrl(url, true);
        url = "http://[1080::8:800:200C:417A]";
        testUrl(url, true);
        url = "http://[FF01::101]";
        testUrl(url, true);
        url = "http://[::1]";
        testUrl(url, true);
        url = "http://[::]";
        testUrl(url, true);
        url = "http://[0:0:0:0:0:0:13.1.68.3]";
        testUrl(url, true);
        url = "http://[0:0:0:0:0:FFFF:129.144.52.38]";
        testUrl(url, true);
        url = "http://[::13.1.68.3]";
        testUrl(url, true);
        url = "http://[::FFFF:129.144.52.38]";
        testUrl(url, true);
        url = "http://[12AB:0000:0000:CD30:0000:0000:0000:0000/60]";
        testUrl(url, true);
        url = "http://[12AB::CD30:0:0:0:0/60]";
        testUrl(url, true);
        url = "http://[12AB:0:0:CD30::/60]";
        testUrl(url, true);
        // IPv6 - false
        url = "http://[PEDC:BA98:7654:3210:FEDC:BA98:7654:3210]";
        testUrl(url, false);
        url = "http://[FEDC:BA98:7654:3210:FEDC:BA98:7654]";
        testUrl(url, false);
        url = "http://[FEDC:BA98:7654:3210:FEDC:BA98:7654:]";
        testUrl(url, false);
        url = "http://[1080::8:800::200C:417A]";
        testUrl(url, false);
        url = "http://[:::]";
        testUrl(url, false);
        url = "http://[0:0:0:0:0:0:A3.1.68.3]";
        testUrl(url, false);
        url = "http://[0:0:0:0:0:0:3.1.3]";
        testUrl(url, false);
        url = "http://[0:0:0:0:0:0:3.1.3.5.7]";
        testUrl(url, false);
        url = "http://[0:0:0:0:0:0:3.1.3.5.]";
        testUrl(url, false);
        // IPv4 - true
        url = "http://3.1.3.5:2040";
        testUrl(url, true);
        url = "http://3.1.3.5";
        testUrl(url, true);
        // IPv4 - false
        url = "http://3.1.3";
        testUrl(url, false);
        url = "http://3.1.3A.5";
        testUrl(url, false);
        url = "http://3.1.256.5";
        testUrl(url, false);
    }
}
