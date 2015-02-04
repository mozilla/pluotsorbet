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

package com.sun.cldc.i18n;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

import java.io.*;

public class TestUtfReaders implements Testlet {
    TestHarness th;

    public String teststr1 = "你好世界";
    public String teststr2 = "привет,мир";
    public String teststr3 = "hello, world!";

    public void test2way(int strId, String s, String e) {
        byte[] b = s.getBytes(e);

        String t = new String(b,e);

        int diff = s.compareTo(t);
        th.check(diff, 0);
    }

    public void testMark(int strId, String s, String e) {
        byte[] b = s.getBytes(e);
        final StreamReader r = //new ReaderUTF16(new ByteArrayInputStream(b));
                (StreamReader)Class.forName("com.sun.cldc.i18n.j2me."+e+"_Reader").newInstance();
        final ByteArrayInputStream bais = new ByteArrayInputStream(b);
        r.open(bais, "UTF_16");

        th.check(r.markSupported(), bais.markSupported());
        th.check(r.markSupported());
        r.mark(2);

        int c1 = r.read();
        int c2 = r.read();
        r.reset();

        int c3 = r.read();
        int c4 = r.read();
        th.check(c1, c3);
        th.check(c2, c4);
        r.reset();

        int c;
        String s2 = "";
        while(-1 != (c=r.read())) {
            s2+=(char)c;
        }

        th.check(s, s2);
    }

    /**
     * Runs all the tests.
     */
    public void test(TestHarness th) {
        this.th = th;

        String[] enc = { "UTF_16", "UTF_16LE", "UTF_16BE","UTF_8" };
        String[] str = { teststr1, teststr2, teststr3 };
        for (int i=0; i < enc.length; i++) {
            for (int j=0; j < str.length; j++) {
                test2way(j,str[j],enc[i]);
                testMark(j,str[j],enc[i]);
            }
        }
    }

}
