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

package com.sun.midp.chameleon.input;

import java.util.Hashtable;
    
/**
 * Factory to get the dictionary for predictive text 
 */
public class PTDictionaryFactory {
    /**
     * pool of initialized library for the current session
     */
    static final Hashtable pool = new Hashtable();
    
    /**
     * Get a dictionary for default default.
     * The language string format is platform specific.
     *
     * @return a predictive text dictionary
     */
    public static PTDictionary getDictionary() {
        return getDictionary("default");
    }

    /**
     * Get a dictionary for specified language
     *
     * @param lang language of dictionary
     * @return a predictive text dictionary
     */
    public static PTDictionary getDictionary(String lang) {
        PTDictionary dic = null;
        if (!pool.containsKey(lang)) {
            dic = new PTDictionaryImpl(lang);
            pool.put(lang, dic);
        } else {
            dic = (PTDictionary)pool.get(lang);
        }
        return dic;
    }
}
