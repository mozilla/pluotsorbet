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
package com.sun.jsr082.obex;

// J2ME security classes
import com.sun.j2me.crypto.MessageDigest;
import com.sun.j2me.crypto.NoSuchAlgorithmException;
import com.sun.j2me.crypto.DigestException;

import java.io.IOException;

/*
 * The platform dependent class which provides a wrapper for
 * security API in J2ME or J2SE.
 */
final class SSLWrapper {
    private MessageDigest md5;

    SSLWrapper() throws IOException {
        try {
            md5 = new MessageDigest("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e.getMessage());
        }
    }

    void update(byte[] input, int offset, int length) {
        md5.update(input, offset, length);
    }

    void doFinal(byte[] srcData, int srcOff, int srcLen, byte[] dstData,
            int dstOff) {

        if (srcLen != 0) {
            md5.update(srcData, srcOff, srcLen);
        }

        try {
            md5.digest(dstData, dstOff, dstData.length - dstOff);
        } catch (DigestException e) {
            // Buffer too short
            throw new RuntimeException("output buffer too short");
        }
    }
} // end of class 'SSLWrapper' definition
