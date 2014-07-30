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

import java.io.Writer;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

/**
 * Writer for UTF-16 encoded output streams (default byte order).
 * We assume that character strings
 * are correctly converted to UFT-16, so no additional checking is performed.
 */
 /* IMPL_NOTE: we need this class name because it is a part of
 * an internal interface: encoding name is converted into a class name,
 * and then Class.forName() is performed.
 */
public class UTF_16_Writer extends UTF_16BE_Writer {
    // Just extending the right class for the default byte order
    // To change the default encoding you need to change the base class

    /**
     * Gets set to true after writing byte order mark (BOM), BOM is 0xfeff.
     */
    protected boolean addedBOM;

    static final char[] bom = new char[] {0xfeff};

    /**
     * Write a portion of an array of characters.
     *
     * @param  cbuf  Array of characters
     * @param  off   Offset from which to start writing characters
     * @param  len   Number of characters to write
     *
     * @exception  java.io.IOException  If an I/O error occurs
     */
    public void write(char cbuf[], int off, int len) throws IOException {
        if (!addedBOM) {
            super.write(bom,0,1);
            addedBOM=true;
        }
        super.write(cbuf,off,len);
    }

    /**
     * Get the size in bytes of an array of chars.
     *
     * @param      array  Source buffer
     * @param      offset Offset at which to start counting character sizes
     * @param      length number of bytes to use for counting
     *
     * @return     number of bytes that the characters would be converted to
     */
    /* The class Helper will ask the writer for the size the output will be.
     * Since chars already are in utf16, calculation is simple. */
    public int sizeOf(char[] array, int offset, int length) {
        // add the size of BOM
        return super.sizeOf(array,offset,length) + 2;
    }
    /**
     * Open the writer
     */
    public Writer open(OutputStream out, String enc)
        throws UnsupportedEncodingException {
        super.open(out,enc);
        addedBOM = false;
        return this;
    }
}



