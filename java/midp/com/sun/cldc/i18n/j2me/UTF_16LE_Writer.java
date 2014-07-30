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

package com.sun.cldc.i18n.j2me;

/**
 * Writer for Little Endian UTF-16 encoded output streams.
 * We assume that character strings
 * are correctly converted to UFT-16, so no additional checking is performed.
 */
public class UTF_16LE_Writer extends UTF_16BE_Writer {

    /**
     * Convert a 16-bit character into two bytes.
     * (This class uses the Little Endian byte order);
     * @param inputChar character to convert
     * @param outputBytes the array receiving the two bytes
     */
    protected void charToBytes(int inputChar, byte[] outputBytes) {
        outputBytes[0] = (byte) (0xFF&inputChar);
        outputBytes[1] = (byte) (0xFF&(inputChar>>8));
    }

}



