/* Sha256.java --
   Copyright (C) 2003, 2006 Free Software Foundation, Inc.

This file is a part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or (at
your option) any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
USA

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.  */

package com.sun.midp.crypto;

import gnu.java.security.hash.Sha256;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class Sha256Test implements Testlet {
    public int getExpectedPass() { return 7; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    private static final String[] messages = {
        "",
        "a",
        "abc",
        "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq",
        "message digest",
        "secure hash algorithm"
    };

    private static final String[] digests = {
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
        "ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb",
        "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
        "248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1",
        "f7846f55cf23e14eebeab5b4e1550cad5b509e3348fbc4efa3a1413d393cb650",
        "f30ceb2bb2829e79e4ca9753d35a8ecc00262d164cc077080295381cbd643f0d"
    };

    private static final String MILLION_A_DIGEST = "cdc76e5c9914fb9281a1c7e284d73e67f1809a48a497200e046d39ccc7112cd0";

    public void test(TestHarness th) {
        Sha256 md = new Sha256();

        for (int i = 0; i < messages.length; i++) {
            byte[] bytes = messages[i].getBytes();
            md.update(bytes, 0, bytes.length);
            th.check(Util.hexEncode(md.digest()).toLowerCase(), digests[i]);
        }

        for (int i = 0; i < 1000000; i++) {
            md.update((byte)'a');
        }
        th.check(Util.hexEncode(md.digest()).toLowerCase(), MILLION_A_DIGEST);
    }
}
