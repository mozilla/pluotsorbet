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

package com.sun.jsr082.bluetooth;

/*
 * Class contains Bluetooth helper methods.
 */
public class BluetoothUtils {
    /* Size of byte representation of Bluetooth address. */
    public static final int BTADDR_SIZE = 6;

    /*
     * Converts Bluetooth address from byte array to string representation.
     *
     * @param btaddr 6-bytes byte array containing Bluetooth address in
     *              Bluetooth byte order (little-endian)
     * @return Bluetooth address as string in
     *          user-friendly byte order (big-endian) without comma separator
     * @throws IllegalArgumentException if input address is invalid
     */
    public static String getAddressString(byte[] btaddr)
            throws IllegalArgumentException {
        final int len = btaddr.length;
            if (len != BTADDR_SIZE) {
                throw new IllegalArgumentException("Incorrect address size");
            }

        StringBuffer sb = new StringBuffer(len * 2);
        String s;
        for (int i = (len - 1); i >= 0; i--) {
            // convert decimal to hexadecimal with leading zeroes and uppercase
            s = Integer.toHexString((btaddr[i] >> 4) & 0xF);
            sb.append(s.toUpperCase());
            s = Integer.toHexString(btaddr[i] & 0xF);
            sb.append(s.toUpperCase());
        }

        return sb.toString();
    }

    /*
     * Converts Bluetooth address from string to byte array representation.
     *
     * @param btaddr Bluetooth address as string in
     *          user-friendly byte order (big-endian) without comma separator
     * @return 6-bytes byte array containing Bluetooth address in
     *              Bluetooth byte order (little-endian)
     * @throws IllegalArgumentException if input address is invalid
     */
    public static byte[] getAddressBytes(String btaddr)
            throws IllegalArgumentException {
        final int len = btaddr.length() / 2;
            if (len != BTADDR_SIZE) {
                throw new IllegalArgumentException("Incorrect address size");
            }

        byte[] bytes = new byte[len];
        try {
            for (int i = 0; i < len; i++) {
                String s = btaddr.substring(i * 2, i * 2 + 2);
                bytes[len - 1 - i] = (byte)Integer.parseInt(s, 16);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Incorrect address value");
        }

        return bytes;
    }
}
