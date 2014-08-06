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

package com.sun.midp.links;

import com.sun.cldc.isolate.Isolate;


/**
 * This class provides foreign methods of the Isolate class. (For discussion 
 * of foreign methods, see Fowler, _Refactoring_, "Introduce Foreign Method", 
 * p. 162.)
 *
 * The technique for passing links between isolates is implemented as a
 * separate class in order to avoid short term dependencies on the
 * implementation of the Isolate class in CLDC. These functions will 
 * eventually be moved to the Isolate class of CLDC.
 *
 * IMPL_NOTE
 * 
 * The current arrangement is highly problematic and therefore should be 
 * considered only as a temporary solution. The portal keeps track of links by 
 * isolate ID. However, an isolate ID is only assigned after the isolate has 
 * been started. Therefore, getLinks() must block until the parent isolate has 
 * had a chance to call setLinks(). The sender and receiver must agree to pass 
 * this initial set of links. This is pretty fragile, but it's only necessary 
 * for passing the initial set of links. Any additional links can be passed 
 * via the initial set of links.
 *
 * If an isolate dies before it gets its links, they'll be stranded in the 
 * portal. The parent isolate can overwrite them, though, and they should be 
 * collected. Worse, if an isolate dies and another isolate is created and 
 * gets the same IDs, it can get the links that were destined for the first. 
 * This requires the parent isolate to be responsible for cleaning up any 
 * unused links passed to its children, which could be difficult in general.
 */
public class LinkPortal {


    /**
     * Gets the links for this isolate that had been set by its creator.  If
     * links are available, this returns immediately. If no links are
     * available, this method will block until the creator sets them. This
     * requires close cooperation between the creator isolate and the newly
     * created isolate. The typical usage is that the creator creates the new
     * isolate and passes one set of links to it, and the first thing the new
     * new isolate does is to get this initial set of links. Any subsequent 
     * links should be passed through the initial set of links.
     */
    public synchronized static Link[] getLinks() {
        int count = getLinkCount0();

        Link[] la = new Link[count];

        for (int i = 0; i < count; i++) {
            la[i] = new Link();
        }

        /*
         * IMPL_NOTE - race condition
         * The creator could change the number of links between the call to 
         * getCount0() and the call to getLinks0().
         */

        getLinks0(la);
        return la;
    }


    /**
     * Sets the array of links for the given isolate. Once set, the array of 
     * links can be retrieved by the designated isolate using the getLinks() 
     * method.
     *
     * Setting of links should be done after the isolate is started and before 
     * it terminates, so that a valid id is available. However, this 
     * introduces a race condition between the starting of the isolate, the 
     * call to setLinks(), and the new isolate calling getLinks(). This means 
     * that the new isolate code will need to call getLinks() until it gets 
     * the array of links it expects.
     *
     * The linkarray parameter may be null, in which case it removes any links
     * that had been previously set for isolate.  If getLinks() is called at
     * this point, it will block until a non-null array is set.  Passing a
     * null linkarray to setLinks() differs from passing a zero-length array,
     * which will satisfy a call to getLinks() and cause it to return a
     * zero-length array.
     *
     * If linkarray is an array whose length is greater than zero, every entry 
     * must be a valid (non-null) Link object.
     *
     * @throws NullPointerException if isolate is null
     * @throws NullPointerException if any entry in linkarray is null
     * @throws IllegalArgumentException if any link in linkarray is closed
     * @throws IllegalStateException if isolate hasn't been started or has 
     *         been terminated
     */
    public static void setLinks(Isolate isolate, Link[] linkarray) {
        if (isolate == null) {
            throw new NullPointerException();
        }

        if (linkarray != null) {
            for (int i = 0; i < linkarray.length; i++) {
                if (linkarray[i] == null) {
                    throw new NullPointerException();
                }
            }
        }

        int id = isolate.id();
        if (id == -1 || isolate.isTerminated()) {
            throw new IllegalStateException();
        }

        /*
         * IMPL_NOTE - race conditions
         * The array could change between these checks and the call to
         * setLinks0(). The state of the isolate could also change.
         */

        setLinks0(id, linkarray);
    }


    /**
     * Prevents construction of any instances.
     */
    private LinkPortal() {
    }


    private static native int getLinkCount0();

    private static native void getLinks0(Link[] linkarray);

    private static native void setLinks0(int isolateid, Link[] linkarray);
}
