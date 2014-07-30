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

import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.midlet.MIDletStateHandler;

/** Class to load and create image data from a suite's cache. */
class SuiteImageCacheImpl implements SuiteImageCache {
    /**
     * Load and create image data from cache. The real work is done in
     * the native function.
     *
     * @param   data The ImageData object
     * @param   resName  Image resource name
     * @return  true if image was loaded and created, false otherwise
     */
    public boolean loadAndCreateImmutableImageData(
                    ImageData data, String resName) {
        MIDletSuite midletSuite =
            MIDletStateHandler.getMidletStateHandler().getMIDletSuite();
        int suiteId = midletSuite.getID();

        return loadAndCreateImmutableImageDataFromCache0(data, 
                                                        suiteId, 
                                                        resName);
    }

    /**
     * Native function to load native image data from cache and create
     * an immutable image.
     *
     * @param data      The ImageData object
     * @param suiteId   The suite id
     * @param resName   The image resource name
     * @return          true if image was loaded and created, false otherwise
     */
    private native boolean loadAndCreateImmutableImageDataFromCache0(
                           ImageData data, int suiteId, String resName);
}
