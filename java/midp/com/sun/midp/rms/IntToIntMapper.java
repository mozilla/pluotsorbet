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
package com.sun.midp.rms;

/**
 * This class implements a mapping int --> int.
 * (Analogs: vector, hash table, etc.) The implementation uses two arrays:
 * one for keys and one for values.
 * The methods are analogous to those of the java.util.Vector class.
 */
class IntToIntMapper {
    /**
     * The array buffer into which the values to be returned are
     * stored. The capacity of the mapper is the length of this array buffer.
     */
    protected int elementData[];

    /**
     * The array buffer into which the values used as keys are
     * stored. The capacity of the mapper is the length of this array buffer.
     */
    protected int elementKey[];

    /**
     * The number of valid components in the mapper.
     */
    protected int elementCount;

    /**
     * The amount by which the capacity is automatically
     * incremented when its size becomes greater than its capacity. If
     * the capacity increment is <code>0</code>, the capacity
     * is doubled each time it needs to grow.
     */
    protected int capacityIncrement;

    /**
     * The value returned if no value is associated with the key
     * searched for.
     */
    public int defaultValue;

    /**
     * Constructs an empty mapper with the specified initial capacity and
     * capacity increment.
     *
     * @param   initialCapacity     the initial capacity of the mapper.
     * @param   defaultElement      the value that gets returned for
     *                              keys that are not there
     * @param   capacityIncrement   the amount by which the capacity is
     *                              increased when the mapper overflows.
     *                              (0 means "to be doubled")
     * @exception IllegalArgumentException if the specified initial capacity
     *            is negative
     */
    public IntToIntMapper(int initialCapacity,
                          int defaultElement,
                          int capacityIncrement) {
        super();
        if (initialCapacity < 0) {
            throw new IllegalArgumentException();
        }
        this.elementData = new int[initialCapacity];
        this.elementKey = new int[initialCapacity];
        this.capacityIncrement = capacityIncrement;
        this.defaultValue = defaultElement;
    }
    /*
    / * * this code used to be used in Vector.... not needed here for now
    *
    * Increases the capacity of this mapper, if necessary, to ensure
    * that it can hold at least the number of components specified by
    * the minimum capacity argument.
    *
    * @param   minCapacity   the desired minimum capacity.
    */
    /*
    public synchronized void ensureCapacity(int minCapacity) {
        if (minCapacity > elementData.length) {
            ensureCapacityHelper(minCapacity);
        }
    }
    */
    /**
     * This implements the unsynchronized semantics of ensureCapacity.
     * Synchronized methods in this class can internally call this
     * method for ensuring capacity without incurring the cost of an
     * extra synchronization.
     *
     * This function increases the size of the mapper according to the
     * value of capacityIncrement, and makes sure that the new size
     * is not less than minCapacity.
     *
     * @param minCapacity     the desired minimum capacity.
     */
    private void ensureCapacityHelper(int minCapacity) {
        int oldCapacity = elementData.length;
        int oldData[] = elementData;
        int oldKey[] = elementKey;
        int newCapacity = (capacityIncrement > 0) ?
            (oldCapacity + capacityIncrement) : (oldCapacity * 2);
        if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
        }

        elementData = new int[newCapacity];
        elementKey = new int[newCapacity];
        System.arraycopy(oldData, 0, elementData, 0, elementCount);
        System.arraycopy(oldKey, 0, elementKey, 0, elementCount);
    }
    /**
     * Returns the number of components in this mapper.
     *
     * @return  the number of components in this mapper.
     */
    public int size() {
        return elementCount;
    }
    /**
     * Tests if this mapper has no components.
     *
     * @return  <code>true</code> if this mapper has no components;
     *          <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return elementCount == 0;
    }
    /**
     * Returns the component at the specified key.
     *
     * @param      key   a key identifying an object in the mapper
     * @return     the component at the specified key, or the default value
     *             if an invalid key was given.
     */
    public synchronized int elementAt(int key) {
        for (int i = 0; i < elementCount; i++) {
            if (key == elementKey[i]) {
                return elementData[i];
            }
        }
        return defaultValue;
    }
    /**
     * Adds the specified component to the end of this mapper,
     * increasing its size by one. The capacity of this mapper is
     * increased if its size becomes greater than its capacity.
     *
     * @param   obj   the component to be added.
     * @param   key   the key for that component.
     */
    private void addElement(int obj, int key) {
        int newcount = elementCount + 1;
        if (newcount > elementData.length) {
            ensureCapacityHelper(newcount);
        }
        elementKey[elementCount] = key;
        elementData[elementCount++] = obj;
    }

    /**
     * Sets the component at the specified <code>key</code> of this
     * mapper to be the specified value. The previous component at that
     * position is discarded.
     *
     * @param      obj     what the component is to be set to.
     * @param      key     the key for that component.
     */
    public synchronized void setElementAt(int obj, int key) {
        for (int i = 0; i < elementCount; i++)
            if (key == elementKey[i]) {
                elementData[i] = obj;
                return;
        }
        addElement(obj, key);
    }

    /**
     * Deletes the component at the specified key. Nothing happens if
     * no component has been associated with the key.
     *
     * @param      key   the key of the object to remove.
     */
    public synchronized void removeElementAt(int key) {
        final int nowhere = -1;
        int where = nowhere;
        for (int i = 0; i < elementCount; i++)
        {
            if (key == elementKey[i]) {
                where = i;
                break;
            }
        }
        if (where == nowhere) {
            return;
        }

        // breaking the order :(
        if (where < elementCount--)
        {
            elementKey[where] = elementKey[elementCount];
            elementData[where] = elementData[elementCount];
        }
    }
}

