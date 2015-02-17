/*
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

package javax.microedition.sensor;

public interface Channel {

    /**
     * Sets a Condition object to be monitored.
     *
     * @param listener - the ConditionListener to which the
     * ConditionListener.conditionMet() notifications are sent
     * @param condition - the Condition object defining the
     * condition to be monitored.
     * @throws java.lang.NullPointerException - if the listener is null
     * or in the case of the application is not automatically launched
     * by the push mechanism and the condition is null
     * @throws java.lang.IllegalArgumentException - if the data type
     * of the channel is TYPE_INT or TYPE_DOUBLE and an ObjectCondition
     * is passed in; or if the data type is TYPE_OBJECT and a LimitCondition
     * or a RangeCondition is passed in
     * @throws java.lang.IllegalStateException - if the SensorConnection
     * is in the STATE_CLOSED state
     */
    public void addCondition(ConditionListener listener, Condition condition);

    /**
     * Returns the ChannelInfo object associated with the Channel.
     *
     * The ChannelInfo contains the properties of the channel data.
     *
     * @return a ChannelInfo object
     */
    public ChannelInfo getChannelInfo();

    /**
     * Returns the Condition objects set for the given listener.
     *
     * @param listener - the ConditionListener whose Condition
     * objects are requested
     * @return the Condition objects set for the listener object.
     * A zero-length Condition array is returned if the given
     * listener has no Condition objects.
     * @throws java.lang.NullPointerException - if the listener is null
     */
    public Condition[] getConditions(ConditionListener listener);

    /**
     * This method returns a string identifying the channel
     * and listing all its unique conditions.
     *
     * @return an URL of channel
     */
    public java.lang.String getChannelUrl();

    /**
     * Removes all Condition and ConditionListener objects
     * registered in this Channel.
     *
     * @throws java.lang.IllegalStateException - if the SensorConnection
     * is in the STATE_CLOSED state
     */
    public void removeAllConditions();

    /**
     * Removes a given Condition and ConditionListener object pair
     * from this Channel.
     *
     * @param listener - the ConditionListener whose Condition
     * will be removed
     * @param condition - the Condition to be removed
     * @throws java.lang.NullPointerException - if either of the
     * parameters is null
     * @throws java.lang.IllegalStateException - if the SensorConnection
     * is in the STATE_CLOSED state
     */
    public void removeCondition(ConditionListener listener, Condition condition);

    /**
     * Removes a given ConditionListener and all Condition objects
     * associated with it.
     *
     * @param listener - the ConditionListener to be removed
     * @throws java.lang.NullPointerException - if the listener is null
     * @throws java.lang.IllegalStateException - if the SensorConnection
     * is in the STATE_CLOSED state
     */
    public void removeConditionListener(ConditionListener listener);
}
