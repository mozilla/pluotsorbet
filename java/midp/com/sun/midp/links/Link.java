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
import java.io.IOException;
import java.io.InterruptedIOException;

public class Link {

    private Link emptyLinkCache; // = null

    private int nativePointer; // set and get only by native code

    public static Link newLink(Isolate sender, Isolate receiver) {
        int rid = receiver.id();  // throws NullPointerException
        int sid = sender.id();    // throws NullPointerException

        if (rid == -1 || sid == -1
                || receiver.isTerminated() || sender.isTerminated() ) {
            throw new IllegalStateException();
        }

        /*
         * IMPL_NOTE - this has race conditions. One of the isolates can
         * change state between the test and the call to init0().
         */

        Link link = new Link();
        link.init0(sender.id(), receiver.id());
        return link;
    }

    public native void close();

    public boolean equals(Object obj) {
        return
            obj instanceof Link
            && ((Link)obj).nativePointer == this.nativePointer;
    }

    public int hashCode() {
        return nativePointer;
    }

    public native boolean isOpen();

    /** 
     * Throws IllegalArgumentException if the calling thread is not in the 
     * receiving isolate for this link.
     *
     * (Note: this differs from the JSR-121 specification, which states that
     * UnsupportedOperationException should be thrown in this case. That
     * exception doesn't appear in CLDC, so IllegalArgumentException is thrown
     * instead.)
     */
    public LinkMessage receive()
            throws ClosedLinkException,
                   InterruptedIOException,
                   IOException {
        Link emptyLink;
        LinkMessage msg = new LinkMessage();

        synchronized (this) {
            if (emptyLinkCache == null) {
                emptyLink = new Link();
            } else {
                emptyLink = emptyLinkCache;
                emptyLinkCache = null;
            }
        }

        receive0(msg, emptyLink);

        if (!msg.containsLink()) {
            synchronized (this) {
                if (emptyLinkCache == null) {
                    emptyLinkCache = emptyLink;
                }
            }
        }

        return msg;
    }

    /** 
     * Throws IllegalArgumentException if the calling thread is not in the 
     * sending isolate for this link.
     *
     * (Note: this differs from the JSR-121 specification, which states that
     * UnsupportedOperationException should be thrown in this case. That
     * exception doesn't appear in CLDC, so IllegalArgumentException is thrown
     * instead.)
     */
    public void send(LinkMessage lm)
            throws ClosedLinkException,
                   InterruptedIOException,
                   IOException {
        if (lm == null) {
            throw new NullPointerException();
        }
        send0(lm);
    }

    /**
     * Creates a new, empty link. This link must be filled in by native code 
     * before it can be used.
     */
    Link() {
    }

    private native void finalize();

    private native void init0(int sender, int receiver);

    private native void receive0(LinkMessage msg, Link link)
            throws ClosedLinkException,
                   InterruptedIOException,
                   IOException;

    private native void send0(LinkMessage msg)
            throws ClosedLinkException,
                   InterruptedIOException,
                   IOException;
}
