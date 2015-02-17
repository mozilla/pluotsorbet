/*
 *   
 *
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
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

package java.util;

import com.sun.cldchi.jvm.JVM;

/**
 * The <code>Vector</code> class implements a growable array of
 * objects. Like an array, it contains components that can be
 * accessed using an integer index. However, the size of a
 * <code>Vector</code> can grow or shrink as needed to accommodate
 * adding and removing items after the <code>Vector</code> has been created.
 * <p>
 * Each vector tries to optimize storage management by maintaining a
 * <code>capacity</code> and a <code>capacityIncrement</code>. The
 * <code>capacity</code> is always at least as large as the vector
 * size; it is usually larger because as components are added to the
 * vector, the vector's storage increases in chunks the size of
 * <code>capacityIncrement</code>. An application can increase the
 * capacity of a vector before inserting a large number of
 * components; this reduces the amount of incremental reallocation.
 *
 * @version 12/17/01 (CLDC 1.1)
 * @since   JDK1.0, CLDC 1.0
 */
public
class Vector {

    /**
     * The array buffer into which the components of the vector are
     * stored. The capacity of the vector is the length of this array buffer.
     *
     * @since   JDK1.0
     */
    protected Object elementData[];

    /**
     * The number of valid components in the vector.
     *
     * @since   JDK1.0
     */
    protected int elementCount;

    /**
     * The amount by which the capacity of the vector is automatically
     * incremented when its size becomes greater than its capacity. If
     * the capacity increment is <code>0</code>, the capacity of the
     * vector is doubled each time it needs to grow.
     *
     * @since   JDK1.0
     */
    protected int capacityIncrement;

    /**
     * Constructs an empty vector with the specified initial capacity and
     * capacity increment.
     *
     * @param   initialCapacity     the initial capacity of the vector.
     * @param   capacityIncrement   the amount by which the capacity is
     *                              increased when the vector overflows.
     * @exception IllegalArgumentException if the specified initial capacity
     *            is negative
     */
    public Vector(int initialCapacity, int capacityIncrement) {
        super();
        if (initialCapacity < 0) {
            throw new IllegalArgumentException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "Illegal Capacity: "+ initialCapacity
/* #endif */
            );
        }
        this.elementData = new Object[initialCapacity];
        this.capacityIncrement = capacityIncrement;
    }

    /**
     * Constructs an empty vector with the specified initial capacity.
     *
     * @param   initialCapacity   the initial capacity of the vector.
     * @since   JDK1.0
     */
    public Vector(int initialCapacity) {
        this(initialCapacity, 0);
    }

    /**
     * Constructs an empty vector.
     *
     * @since   JDK1.0
     */
    public Vector() {
        this(10);
    }

    /**
     * Copies the components of this vector into the specified array.
     * The array must be big enough to hold all the objects in this  vector.
     *
     * @param   anArray   the array into which the components get copied.
     * @since   JDK1.0
     */
    public void copyInto(Object anArray[]) {
        int i = elementCount;
        while (i-- > 0) {
            anArray[i] = elementData[i];
        }
    }

    /**
     * Trims the capacity of this vector to be the vector's current
     * size. An application can use this operation to minimize the
     * storage of a vector.
     *
     * @since   JDK1.0
     */
    public void trimToSize() {
        int oldCapacity = elementData.length;
        if (elementCount < oldCapacity) {
            Object oldData[] = elementData;
            elementData = new Object[elementCount];
            System.arraycopy(oldData, 0, elementData, 0, elementCount);
        }
    }

    /**
     * Increases the capacity of this vector, if necessary, to ensure
     * that it can hold at least the number of components specified by
     * the minimum capacity argument.
     *
     * @param   minCapacity   the desired minimum capacity.
     * @since   JDK1.0
     */
    public void ensureCapacity(int minCapacity) {
        if (minCapacity > elementData.length) {
            ensureCapacityHelper(minCapacity);
        }
    }

    /**
     * This implements the unsynchronized semantics of ensureCapacity.
     * Synchronized methods in this class can internally call this
     * method for ensuring capacity without incurring the cost of an
     * extra synchronization.
     *
     * @see java.util.Vector#ensureCapacity(int)
     */
    private void ensureCapacityHelper(int minCapacity) {
        int oldCapacity = elementData.length;
        Object oldData[] = elementData;
        int newCapacity = (capacityIncrement > 0) ?
            (oldCapacity + capacityIncrement) : (oldCapacity * 2);
        if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
        }
        elementData = new Object[newCapacity];
        System.arraycopy(oldData, 0, elementData, 0, elementCount);
    }

    /**
     * Sets the size of this vector. If the new size is greater than the
     * current size, new <code>null</code> items are added to the end of
     * the vector. If the new size is less than the current size, all
     * components at index <code>newSize</code> and greater are discarded.
     *
     * @param   newSize   the new size of this vector.
     * @throws  ArrayIndexOutOfBoundsException if new size is negative.
     * @since   JDK1.0
     */
    public void setSize(int newSize) {
        if ((newSize > elementCount) && (newSize > elementData.length)) {
            ensureCapacityHelper(newSize);
        } else {
            for (int i = newSize ; i < elementCount ; i++) {
                elementData[i] = null;
            }
        }
        elementCount = newSize;
    }

    /**
     * Returns the current capacity of this vector.
     *
     * @return  the current capacity of this vector.
     * @since   JDK1.0
     */
    public int capacity() {
        return elementData.length;
    }

    /**
     * Returns the number of components in this vector.
     *
     * @return  the number of components in this vector.
     * @since   JDK1.0
     */
    public int size() {
        return elementCount;
    }

    /**
     * Tests if this vector has no components.
     *
     * @return  <code>true</code> if this vector has no components;
     *          <code>false</code> otherwise.
     * @since   JDK1.0
     */
    public boolean isEmpty() {
        return elementCount == 0;
    }

    /**
     * Returns an enumeration of the components of this vector.
     *
     * @return  an enumeration of the components of this vector.
     * @see     java.util.Enumeration
     * @since   JDK1.0
     */
    public Enumeration elements() {
        return new VectorEnumerator(this);
    }

    /**
     * Tests if the specified object is a component in this vector.
     *
     * @param   elem   an object.
     * @return  <code>true</code> if the specified object is a component in
     *          this vector; <code>false</code> otherwise.
     * @since   JDK1.0
     */
    public boolean contains(Object elem) {
        return indexOf(elem, 0) >= 0;
    }

    /**
     * Searches for the first occurrence of the given argument, testing
     * for equality using the <code>equals</code> method.
     *
     * @param   elem   an object.
     * @return  the index of the first occurrence of the argument in this
     *          vector; returns <code>-1</code> if the object is not found.
     * @see     java.lang.Object#equals(java.lang.Object)
     * @since   JDK1.0
     */
    public int indexOf(Object elem) {
        return indexOf(elem, 0);
    }

    /**
     * Searches for the first occurrence of the given argument, beginning
     * the search at <code>index</code>, and testing for equality using
     * the <code>equals</code> method.
     *
     * @param   elem    an object.
     * @param   index   the index to start searching from.
     * @return  the index of the first occurrence of the object argument in
     *          this vector at position <code>index</code> or later in the
     *          vector; returns <code>-1</code> if the object is not found.
     * @see     java.lang.Object#equals(java.lang.Object)
     * @since   JDK1.0
     */
    public int indexOf(Object elem, int index) {
        if (elem == null) {
            for (int i = index ; i < elementCount ; i++)
                if (elementData[i]==null)
                    return i;
        } else {
            for (int i = index ; i < elementCount ; i++)
                if (elem.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified object in
     * this vector.
     *
     * @param   elem   the desired component.
     * @return  the index of the last occurrence of the specified object in
     *          this vector; returns <code>-1</code> if the object is not found.
     * @since   JDK1.0
     */
    public int lastIndexOf(Object elem) {
        return lastIndexOf(elem, elementCount-1);
    }

    /**
     * Searches backwards for the specified object, starting from the
     * specified index, and returns an index to it.
     *
     * @param   elem    the desired component.
     * @param   index   the index to start searching from.
     * @return  the index of the last occurrence of the specified object in this
     *          vector at position less than <code>index</code> in the vector;
     *          <code>-1</code> if the object is not found.
     * @exception  IndexOutOfBoundsException  if <tt>index</tt> is greater
     *             than or equal to the current size of this vector.
     * @since   JDK1.0
     */
    public int lastIndexOf(Object elem, int index) {
        if (index >= elementCount) {
            throw new IndexOutOfBoundsException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       index + " >= " + elementCount
/* #endif */
            );
        }

        if (elem == null) {
            for (int i = index; i >= 0; i--)
                if (elementData[i]==null)
                    return i;
        } else {
            for (int i = index; i >= 0; i--)
                if (elem.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

    /**
     * Returns the component at the specified index.
     *
     * @param      index   an index into this vector.
     * @return     the component at the specified index.
     * @exception  ArrayIndexOutOfBoundsException  if an invalid index was
     *             given.
     * @since      JDK1.0
     */
    public Object elementAt(int index) {
        if (index >= elementCount) {
            throw new ArrayIndexOutOfBoundsException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       index + " >= " + elementCount
/* #endif */
            );
        }
        return elementData[index];
    }

    /**
     * Returns the first component of this vector.
     *
     * @return     the first component of this vector.
     * @exception  NoSuchElementException  if this vector has no components.
     * @since      JDK1.0
     */
    public Object firstElement() {
        if (elementCount == 0) {
            throw new NoSuchElementException();
        }
        return elementData[0];
    }

    /**
     * Returns the last component of the vector.
     *
     * @return  the last component of the vector, i.e., the component at index
     *          <code>size()&nbsp;-&nbsp;1</code>.
     * @exception  NoSuchElementException  if this vector is empty.
     * @since   JDK1.0
     */
    public Object lastElement() {
        if (elementCount == 0) {
            throw new NoSuchElementException();
        }
        return elementData[elementCount - 1];
    }

    /**
     * Sets the component at the specified <code>index</code> of this
     * vector to be the specified object. The previous component at that
     * position is discarded.
     * <p>
     * The index must be a value greater than or equal to <code>0</code>
     * and less than the current size of the vector.
     *
     * @param      obj     what the component is to be set to.
     * @param      index   the specified index.
     * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
     * @see        java.util.Vector#size()
     * @since      JDK1.0
     */
    public void setElementAt(Object obj, int index) {
        if (index >= elementCount) {
            throw new ArrayIndexOutOfBoundsException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       index + " >= " +
/// skipped                       elementCount
/* #endif */
            );
        }
        elementData[index] = obj;
    }

    /**
     * Deletes the component at the specified index. Each component in
     * this vector with an index greater or equal to the specified
     * <code>index</code> is shifted downward to have an index one
     * smaller than the value it had previously.
     * <p>
     * The index must be a value greater than or equal to <code>0</code>
     * and less than the current size of the vector.
     *
     * @param      index   the index of the object to remove.
     * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
     * @see        java.util.Vector#size()
     * @since      JDK1.0
     */
    public void removeElementAt(int index) {
        if (index >= elementCount) {
            throw new ArrayIndexOutOfBoundsException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       index + " >= " +
/// skipped                       elementCount
/* #endif */
            );
        }
        else if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       index
/* #endif */
            );
        }
        int j = elementCount - index - 1;
        if (j > 0) {
            System.arraycopy(elementData, index + 1, elementData, index, j);
        }
        elementCount--;
        elementData[elementCount] = null; /* to let gc do its work */
    }

    /**
     * Inserts the specified object as a component in this vector at the
     * specified <code>index</code>. Each component in this vector with
     * an index greater or equal to the specified <code>index</code> is
     * shifted upward to have an index one greater than the value it had
     * previously.
     * <p>
     * The index must be a value greater than or equal to <code>0</code>
     * and less than or equal to the current size of the vector.
     *
     * @param      obj     the component to insert.
     * @param      index   where to insert the new component.
     * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
     * @see        java.util.Vector#size()
     * @since      JDK1.0
     */
    public void insertElementAt(Object obj, int index) {
        int newcount = elementCount + 1;
        if (index < 0 || index >= newcount) {
            throw new ArrayIndexOutOfBoundsException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       index + " > " + elementCount
/* #endif */
            );
        }
        if (newcount > elementData.length) {
            ensureCapacityHelper(newcount);
        }
        System.arraycopy(elementData, index, elementData, index + 1, 
                         elementCount - index);
        elementData[index] = obj;
        elementCount++;
    }

    /**
     * Adds the specified component to the end of this vector,
     * increasing its size by one. The capacity of this vector is
     * increased if its size becomes greater than its capacity.
     *
     * @param   obj   the component to be added.
     * @since   JDK1.0
     */
    public void addElement(Object obj) {
        int newcount = elementCount + 1;
        if (newcount > elementData.length) {
            ensureCapacityHelper(newcount);
        }
        elementData[elementCount++] = obj;
    }

    /**
     * Removes the first occurrence of the argument from this vector. If
     * the object is found in this vector, each component in the vector
     * with an index greater or equal to the object's index is shifted
     * downward to have an index one smaller than the value it had previously.
     *
     * @param   obj   the component to be removed.
     * @return  <code>true</code> if the argument was a component of this
     *          vector; <code>false</code> otherwise.
     * @since   JDK1.0
     */
    public boolean removeElement(Object obj) {
        int i = indexOf(obj);
        if (i >= 0) {
            removeElementAt(i);
            return true;
        }
        return false;
    }

    /**
     * Removes all components from this vector and sets its size to zero.
     *
     * @since   JDK1.0
     */
    public void removeAllElements() {
        for (int i = 0; i < elementCount; i++) {
            elementData[i] = null;
        }
        elementCount = 0;
    }

    /**
     * Returns a string representation of this vector.
     *
     * @return  a string representation of this vector.
     * @since   JDK1.0
     */
    public String toString() {
        int max = size() - 1;
        StringBuffer buf = new StringBuffer();
        Enumeration e = elements();
        buf.append("[");

        for (int i = 0 ; i <= max ; i++) {
            buf.append(e.nextElement());
            if (i < max) {
                buf.append(", ");
            }
        }
        buf.append("]");
        return buf.toString();
    }
}

final
class VectorEnumerator implements Enumeration {
    Vector vector;
    int count;

    VectorEnumerator(Vector v) {
        vector = v;
        count = 0;
    }

    public boolean hasMoreElements() {
        return count < vector.elementCount;
    }

    public Object nextElement() {
            if (count < vector.elementCount) {
                return vector.elementData[count++];
            }
        throw new NoSuchElementException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                   "VectorEnumerator"
/* #endif */
        );
    }
}
