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

package javax.microedition.media.control;


/**
 * <code>RateControl</code> controls the playback rate of a 
 * <code>Player</code>.<p>
 * 
 * The rate defines the relationship between the 
 * <code>Player's</code>&nbsp;<i>media time</i> and its 
 * <code>TimeBase</code>.  Rates are specified in &quot;milli-
 * percentage&quot;.<p>
 * 
 * For example, a rate of 200'000 indicates that <i>media 
 * time</i> will pass twice as fast as the 
 * <code>TimeBase</code> time once the <code>Player</code> 
 * starts.  Similarly, a negative rate indicates that the 
 * <code>Player</code> runs in the opposite direction of its 
 * <code>TimeBase</code>, i.e. playing in reverse.<p>
 * 
 * All <code>Player</code> must support the default rate 
 * 100'000. <code>Player</code>s that support only the default 
 * rate must not implement this interface. 
 * <code>Player</code>s that support other rates besides 
 * 100'000, should implement this interface and specify the 
 * appropriate minimum and maximum playback rates.<p>
 * 
 * For audio, specific implementations may change the playback 
 * pitch when changing the playback rate. This may be viewed as an 
 * undesirable side-effect. See <code>PitchControl</code> for 
 * changing pitch without changing playback rate.
 * 
 * @see javax.microedition.media.Player
 * @see javax.microedition.media.control.TempoControl
 * @see javax.microedition.media.control.PitchControl
 */
public interface RateControl extends javax.microedition.media.Control {

    /**
     * Sets the playback rate.  
     * 
     * The specified rate is 1000 times the percentage of the 
     * actual rate. For example, to play back at twice the speed, specify
     * a rate of 200'000.<p>
     *
     * The <code>setRate</code> method returns the actual rate set by the
     * <code>Player</code>.  <code>Player</code> should set their rate 
     * as close to the requested
     * value as possible, but are not required to set the rate to the exact
     * value of any argument other than 100'000. A <code>Player</code> 
     * is only guaranteed to set
     * its rate exactly to 100'000.
     * If the given rate is less than <code>getMinRate</code>
     * or greater than <code>getMaxRate</code>,
     * the rate will be adjusted to the minimum or maximum
     * supported rate respectively.
     * <p>
     * If the <code>Player</code> is already
     * started, <code>setRate</code> will immediately take effect.
     * 
     * @param millirate The playback rate to set. The rate is given in 
     *        a &quot;milli-percentage&quot; value.
     * @return The actual rate set in &quot;milli-percentage&quot;.
     * @see #getRate
     */
    int setRate(int millirate);

    /**
     * Gets the current playback rate.
     *
     * @return the current playback rate in &quot;milli-percentage&quot;.
     * @see #setRate
     */
    int getRate();

    /**
     * Gets the maximum playback rate supported by the <code>Player</code>.
     *
     * @return the maximum rate in &quot;milli-percentage&quot;.
     */
    int getMaxRate();
 
    /**
     * Gets the minimum playback rate supported by the <code>Player</code>.
     *
     * @return the minimum rate in &quot;milli-percentage&quot;.
     */
    int getMinRate();
}

