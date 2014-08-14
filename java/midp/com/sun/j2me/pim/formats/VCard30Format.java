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
import com.sun.j2me.jsr75.StringUtil;

/**
 * Implementation of PIMEncoding for VCard/3.0.
 *
 */
public class VCard30Format extends VCardFormat {

    /**
     * Writes the attributes for a field.
     * @param w output stream target
     * @param attributes fields to be written
     * @throws IOException if an error occurs while writing
     */
    protected void writeAttributes(Writer w, int attributes)
            throws IOException {
        boolean writtenData = false;
        for (int i = 0; i < 32; i++) {
            long mask = 1l << i;
            if ((attributes & mask) != 0) {
                String attributeLabel =
                    VCardSupport.getAttributeLabel((int) mask);
                if (attributeLabel != null) {
                    if (writtenData) {
                        w.write(",");
                    } else {
                        w.write(";TYPE=");
                        writtenData = true;
                    }
                    w.write(attributeLabel);
                }
            }
        }
    }

    /**
     * Get the binary value describing all flags in a vCard line.
     * @param attributes fields to parse
     * @return binary coded flags
     */
    protected int parseAttributes(String[] attributes) {
        int code = 0;
        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i].startsWith("TYPE=")) {
                String[] s = StringUtil.split(attributes[i], ',', 5);
                for (int j = 0; j < s.length; j++) {
                    code |= VCardSupport.getAttributeCode(s[j], 0);
                }
            } else {
                code |= VCardSupport.getAttributeCode(attributes[i], 0);
            }
        }
        return code;
    }

    /**
     * Gets the binary encoding name.
     * @return the binary encoding name "B"
     */
    protected String getBinaryEncodingName() {
        return "B";
    }

    /**
     * Gets the category property name.
     * @return the category property name "CATEGORY"
     */
    protected String getCategoryProperty() {
        return "CATEGORY";
    }

    /**
     * Gets the class property name.
     * @return the class property name "CLASS"
     */
    protected String getClassProperty() {
        return "CLASS";
    }

    /**
     * Gets the VCard version number.
     * @return the VCard version number "3.0"
     */
    protected String getVersion() {
        return "3.0";
    }

}
