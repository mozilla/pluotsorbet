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

package com.sun.j2me.pim.formats;

import java.io.IOException;
import java.io.Writer;

/**
 * Implementation of PIMEncoding for VCard/2.1.
 *
 */
public class VCard21Format extends VCardFormat {

    /**
     * Property used to store categories
     */
    private static final String PROPERTY_CATEGORY = "X-J2MEWTK-CATEGORY";
    
    /**
     * Property used to store access class.
     */
    private static final String PROPERTY_CLASS = "X-J2MEWTK-CLASS";
    
        
    /**
     * Writes the attributes for a field.
     * @param w the output writer stream
     * @param attributes the attributes to be processed
     * @throws IOException if an error occurs writing
     */
    protected void writeAttributes(Writer w, int attributes) 
	throws IOException {
        for (int i = 0; i < 32; i++) {
            long mask = 1l << i;
            if ((attributes & mask) != 0) {
                String attributeLabel =
                    VCardSupport.getAttributeLabel((int) mask);
                if (attributeLabel != null) {
                    w.write(';');
                    w.write(attributeLabel);
                }
            }
        }
    }
   
    /**
     * Gets the binary value describing all flags in a vCard line.
     * @param attributes input selection attributes
     * @return  binary attribute codes
     */
    protected int parseAttributes(String[] attributes) {
        int code = 0;
        for (int i = 0; i < attributes.length; i++) {
            code |= VCardSupport.getAttributeCode(attributes[i], 0);
        }
        return code;
    }

    /**
     * Returns name of encoding.
     * @return encoding name
     */    
    protected String getBinaryEncodingName() {
        return "BASE64";
    }

    /**
     * Returns the property category.
     * @return property category
     */    
    protected String getCategoryProperty() {
        return PROPERTY_CATEGORY;
    }

    /**
     * Returns class property.
     * @return class property
     */    
    protected String getClassProperty() {
        return PROPERTY_CLASS;
    }

    /**    
     * Returns VCard version.
     * @return VCard version "2.1"
     */
    protected String getVersion() {
        return "2.1";
    }
    
}
