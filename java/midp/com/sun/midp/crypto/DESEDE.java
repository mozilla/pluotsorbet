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

package com.sun.midp.crypto;

/**
 * DES EDE (Triple DES)cipher implementation.
 */
public class DESEDE extends DES {
    /**
     * Called by the factory method to set the mode and padding parameters.
     * Need because Class.newInstance does not take args.
     *
     * @param mode the mode parsed from the transformation parameter of
     *             getInstance
     * @param padding the paddinge parsed from the transformation parameter of
     *                getInstance
     *
     * @exception NoSuchPaddingException if <code>transformation</code>
     * contains a padding scheme that is not available.
     */
    protected void setChainingModeAndPadding(String mode, String padding)
            throws NoSuchPaddingException {
        if (mode.equals("ECB") || mode.equals("")) {
            cipher = new DES_ECB(true);
        } else if (mode.equals("CBC")) {
            cipher = new DES_CBC(true);
        } else {
            throw new IllegalArgumentException();
        }

        cipher.setChainingModeAndPadding(mode, padding);
    }
}
