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

package com.sun.j2me.pim;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.PIMList;

/**
 * Interface for PIM data encoders and decoders.
 *
 */
public interface PIMFormat {
    
    /**
     * Gets the code name of this encoding (e.g. "VCARD/2.1").
     * @return the encoding name
     */    
    public String getName();
    
    /**
     * Checks to see if a given PIM list type is supported by this encoding.
     * @param pimListType int representing the PIM list type to check
     * @return true if the type can be read and written by this encoding,
     * false otherwise
     */    
    public boolean isTypeSupported(int pimListType);
    
    /**
     * Constructs one or more PIMItems from serialized data.
     * @param in Stream containing serialized data
     * @param encoding Character encoding of the stream
     * @param list PIMList to which items should be added, or null if the items
     * should not be part of a list
     * @return a non-empty array of PIMItems containing the objects described
     * in the serialized data, or null if no items are available
     * @throws UnsupportedPIMFormatException if the serialized data cannot be
     *  interpreted by this encoding.
     * @throws IOException if an error occurs while reading
     */    
    public PIMItem[] decode(InputStream in, String encoding, PIMList list)
        throws IOException;
    
    /**
     * Serializes a PIMItem.
     * @param out Stream to which serialized data is written
     * @param encoding Character encoding to use for serialized data
     * @param pimItem The item to write to the stream
     * @throws IOException if an error occurs while writing
     */    
    public void encode(OutputStream out, String encoding, PIMItem pimItem)
        throws IOException;
    
}
