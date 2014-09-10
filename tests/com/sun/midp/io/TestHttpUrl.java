/*
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

public class TestHttpUrl implements Testlet {
    /**
     * Runs all the tests.
     */
    public void test(TestHarness th) {
        testAbsUrl(th);
        testRelUrl(th);
    }

    /**
     * Tests parsing of different pieces of the absolute URL.
     */
    void testAbsUrl(TestHarness th) {
        HttpUrl url;

        url = new HttpUrl("scheme://machine.domain:8080/path?query#fragment");
        th.check("scheme", url.scheme);
        th.check("machine.domain:8080", url.authority);
        th.check("machine.domain", url.host);
        th.check("machine", url.machine);
        th.check("domain", url.domain);
        th.check(8080, url.port);
        th.check("/path", url.path);
        th.check("query", url.query);
        th.check("fragment", url.fragment);

        url = new HttpUrl("scheme://machine.domain:8080/path?query#");
        th.check("scheme", url.scheme);
        th.check("machine.domain:8080", url.authority);
        th.check("machine.domain", url.host);
        th.check("machine", url.machine);
        th.check("domain", url.domain);
        th.check(8080, url.port);
        th.check("/path", url.path);
        th.check("query", url.query);
        th.check(url.fragment == null);

        url = new HttpUrl("scheme://machine.domain:8080/path");
        th.check("scheme", url.scheme);
        th.check("machine.domain:8080", url.authority);
        th.check("machine.domain", url.host);
        th.check("machine", url.machine);
        th.check("domain", url.domain);
        th.check(8080, url.port);
        th.check("/path", url.path);
        th.check(url.query == null);
        th.check(url.fragment == null);

        url = new HttpUrl("scheme://machine.domain:8080/");
        th.check("scheme", url.scheme);
        th.check("machine.domain:8080", url.authority);
        th.check("machine.domain", url.host);
        th.check("machine", url.machine);
        th.check("domain", url.domain);
        th.check(8080, url.port);
        th.check("/", url.path);
        th.check(url.query == null);
        th.check(url.fragment == null);

        url = new HttpUrl("scheme://machine.domain:8080");
        th.check("scheme", url.scheme);
        th.check("machine.domain:8080", url.authority);
        th.check("machine.domain", url.host);
        th.check("machine", url.machine);
        th.check("domain", url.domain);
        th.check(8080, url.port);
        th.check(url.path == null);
        th.check(url.query == null);
        th.check(url.fragment == null);

        url = new HttpUrl("scheme://machine.domain");
        th.check("scheme", url.scheme);
        th.check("machine.domain", url.authority);
        th.check("machine.domain", url.host);
        th.check("machine", url.machine);
        th.check("domain", url.domain);
        th.check(-1, url.port);
        th.check(url.path == null);
        th.check(url.query == null);
        th.check(url.fragment == null);

        url = new HttpUrl("scheme://machine");
        th.check("scheme", url.scheme);
        th.check("machine", url.authority);
        th.check("machine", url.host);
        th.check("machine", url.machine);
        th.check(url.domain == null);
        th.check(-1, url.port);
        th.check(url.path == null);
        th.check(url.query == null);
        th.check(url.fragment == null);

        url = new HttpUrl("scheme://");
        th.check("scheme", url.scheme);
        th.check(url.authority == null);
        th.check(url.host == null);
        th.check(url.machine == null);
        th.check(url.domain == null);
        th.check(-1, url.port);
        th.check(url.path == null);
        th.check(url.query == null);
        th.check(url.fragment == null);

        url = new HttpUrl("scheme://machine.subdomain.domain");
        th.check("machine.subdomain.domain", url.authority);
        th.check("machine.subdomain.domain", url.host);
        th.check("machine", url.machine);
        th.check("subdomain.domain", url.domain);

        try {
            url = new HttpUrl("scheme://123.domain");
            th.check("123.domain", url.authority);
            th.check("123.domain", url.host);
            th.check("123", url.machine);
            th.check("domain", url.domain);
        } catch (IllegalArgumentException e) {
            th.todo(false, "Unexpected exception");
        }

        try {
            url = new HttpUrl("scheme://1234.5678.901.2345");
            th.check("1234.5678.901.2345", url.authority);
            th.check("1234.5678.901.2345", url.host);
            th.check(url.machine == null);
            th.check(url.domain == null);
        } catch (IllegalArgumentException e) {
            th.todo(false, "Unexpected exception");
        }

        try {
            url = new HttpUrl("scheme://1234");
            th.check("1234", url.authority);
            th.check("1234", url.host);
            th.check("1234", url.machine);
            th.check(url.domain == null);
        } catch (IllegalArgumentException e) {
            th.todo(false, "Unexpected exception");
        }

        // IP v4 address
        try {
            url = new HttpUrl("scheme://123.123");
            th.check("123.123", url.authority);
            th.check("123.123", url.host);
            th.check(url.machine == null);
            th.check(url.domain == null);
        } catch (IllegalArgumentException e) {
            th.todo(false, "Unexpected exception");
        }

        // IP v6 address
        try {
            url = new HttpUrl("scheme://[123]");
            th.check("[123]", url.authority);
            th.check("[123]", url.host);
            th.check(url.machine == null);
            th.check(url.domain == null);
        } catch (IllegalArgumentException e) {
            th.todo(false, "Unexpected exception");
        }

        url = new HttpUrl("scheme://authority/");
        th.check("/", url.path);
    }

    /**
     * Tests parsing of different pieces of the relative URL.
     */
    void testRelUrl(TestHarness th) {
        HttpUrl url;

        url = new HttpUrl("//authority/path?query#fragment");
        th.check(url.scheme == null);
        th.check("authority", url.authority);
        th.check("/path", url.path);
        th.check("query", url.query);
        th.check("fragment", url.fragment);

        url = new HttpUrl("//authority/path?query");
        th.check(url.scheme == null);
        th.check("authority", url.authority);
        th.check("/path", url.path);
        th.check("query", url.query);
        th.check(url.fragment == null);

        url = new HttpUrl("//authority/path");
        th.check(url.scheme == null);
        th.check("authority", url.authority);
        th.check("/path", url.path);
        th.check(url.query == null);

        url = new HttpUrl("//authority/");
        th.check(url.scheme == null);
        th.check("authority", url.authority);
        th.check("/", url.path);

        url = new HttpUrl("//authority");
        th.check(url.scheme == null);
        th.check("authority", url.authority);
        th.check(url.path == null);

        url = new HttpUrl("/path");
        th.check(url.scheme == null);
        th.check(url.authority == null);
        th.check("/path", url.path);
        th.check(url.query == null);

        url = new HttpUrl("/");
        th.check(url.scheme == null);
        th.check(url.authority == null);
        th.check("/", url.path);

        url = new HttpUrl("path/subpath");
        th.check(url.scheme == null);
        th.check(url.authority == null);
        th.check("path/subpath", url.path);

        url = new HttpUrl("path");
        th.check(url.scheme == null);
        th.check(url.authority == null);
        th.check("path", url.path);
    }
}
