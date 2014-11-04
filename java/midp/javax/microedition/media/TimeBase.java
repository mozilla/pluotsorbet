/*
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

package javax.microedition.media;

/**
 * A <code>TimeBase</code> is a constantly ticking source of time.
 * It measures the progress of time and
 * provides the basic means for synchronizing media playback for
 * <code>Player</code>s.
 * <p>
 * A <code>TimeBase</code> measures time in microseconds in
 * order to provide the necessary resolution for synchronization.
 * It is acknowledged that some implementations may not be able to
 * support time resolution in the microseconds range.  For such 
 * implementations, the internal representation of time can be done 
 * within their limits.
 * But the time reported via the API must be scaled to the microseconds
 * range.
 * <p>
 * <code>Manager.getSystemTimeBase</code> provides the default 
 * <code>TimeBase</code> used by the system.
 *
 * @see Player
 */
public interface TimeBase {

    /**
     * Get the current time of this <code>TimeBase</code>.  The values
     * returned must be non-negative and non-decreasing over time.
     *
     * @return the current <code>TimeBase</code> time in microseconds.
     */
    long getTime();
}
