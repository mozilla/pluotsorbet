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

package javax.microedition.lcdui;

/**
 * Implemented by the class running the suite so it can be decoupled from
 * the low level LCDUI library.
 */
class SuiteImageCacheFactory {
    
    /**
     * Get an image data cache.
     *
     * @return  true if image was loaded and created, false otherwise
     */
    static SuiteImageCache getCache() {
        Class c;
        SuiteImageCache cache;

        try {
            c = Class.forName(
                    "javax.microedition.lcdui.SuiteImageCacheImpl");
            cache = (SuiteImageCache)c.newInstance();
        } catch (Exception e) {
            cache = new SuiteImageCacheStub();
        }

        return cache;
    }
}

/** Stub class, does nothing. */
class SuiteImageCacheStub implements SuiteImageCache {
    /**
     * Load and create image data from cache. The real work is done in
     * the native function.
     *
     * @param   data The ImageData object
     * @param   resName  Image resource name
     *
     * @return  true if image was loaded and created, false otherwise
     */
    public boolean loadAndCreateImmutableImageData(
                        ImageData data, String resName) {
        return false;
    }
}
