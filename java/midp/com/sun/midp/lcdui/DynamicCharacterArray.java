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
package com.sun.midp.lcdui;

/**
 * Similar to StringBuffer but does NOT resize when needed, only when
 * requested
 */
public class DynamicCharacterArray {

    /**
     * buffer to store data. The capacity of this DynamicCharacterArray
     * is equal to the length of this buffer
     */
    protected char buffer[];

    /** 
     * the length of the array currently in use
     */
    protected int length;

    /**
     * Initializes the DCA with a capacity and an empty array
     *
     * @param capacity the maximum storage capacity
     * @throws IllegalArgumentException if capacity <= 0 
     * @throws IllegalArgumentException if data.length > capacity 
     */
    public DynamicCharacterArray(int capacity) {
        this(null, capacity);
    }

    /**
     * Initializes the array with str and a capacity of str.length()
     *
     * @param str initial array data, may NOT be null
     * @throws NullPointerException if str is null
     */
    public DynamicCharacterArray(String str) {
        this(str.toCharArray());
    }

    /**
     * Initializes the array with data and a capacity of data.length
     *
     * @param data initial array data, may NOT be null
     * @throws NullPointerException if data is null
     */
    public DynamicCharacterArray(char[] data) {
        this(data, data.length);
    }

    /**
     * Initializes the array with data and a capacity of capacity
     *
     * @param data initial array data, may be null
     * @param capacity initial maximum capacity
     * @throws IllegalArgumentException if capacity <= 0 
     * @throws IllegalArgumentException if data.length > capacity 
     */
    public DynamicCharacterArray(char[] data, int capacity) {
        int len;

        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }

        if (data != null) {
            if (data.length > capacity) {
                throw new IllegalArgumentException();
            }

            this.length = data.length;

            buffer = new char[capacity];
            System.arraycopy(data, 0, buffer, 0, this.length);

        } else {
            buffer = new char[capacity];
        }
    }

    /**
     * Inserts an subset of an array into the buffer.
     * The offset parameter must be within the range [0..(size())], inclusive.
     * The length parameter must be a non-negative integer such that 
     * (offset + length) <= size().
     *
     * @param data array to insert
     * @param offset offset into data
     * @param ins_length length of subset
     * @param position index into the internal buffer to insert the subset
     * @return the actual position the data was inserted
     * @throws ArrayIndexOutOfBoundsException if offset and length
     *         specify an invalid range
     * @throws IllegalArgumentException if the resulting array would exceed
     *         the capacity
     * @throws NullPointerException if data is null
     */
    public int insert(char[] data, int offset, int ins_length, int position) {

        if (position < 0) {
            position = 0;
        } else if (position > length) {
            position = length;
        }

        if (offset < 0 
            || offset > data.length 
            || ins_length < 0
            || ins_length > data.length
            || (offset + ins_length) < 0
            || (offset + ins_length) > data.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        if (length + ins_length > buffer.length) {
            throw new IllegalArgumentException();
        }

        if (data.length != 0) {

            System.arraycopy(buffer, position, 
                             buffer, position + ins_length, 
                             length - position);
            System.arraycopy(data, offset, buffer, position, ins_length);

            length += ins_length;
        }

        return position;
    }

    /**
     * Inserts an character into the buffer.
     *
     * @param ch character to insert
     * @param position index into the internal buffer to insert the subset
     * @return the actual position the data was inserted
     * @throws IllegalArgumentException if the resulting array would exceed
     *         the capacity
     */
    public int insert(int position, char ch) {
        char arr[] = { ch };
        return insert(arr, 0, 1, position);
    }
   
    /**
     * Inserts an String into the buffer.
     *
     * @param str String to insert
     * @param position index into the internal buffer to insert the subset
     * @return the actual position the data was inserted
     * @throws ArrayIndexOutOfBoundsException if offset and length
     *         specify an invalid range
     * @throws IllegalArgumentException if the resulting array would exceed
     *         the capacity
     * @throws NullPointerException if data is null
     */
    public int insert(int position, String str) {
        return insert(str.toCharArray(), 0, str.length(), position);
    }

    /**
     * Appends a character onto the end of the buffer.
     *
     * @param c character to append
     * @throws IllegalArgumentException if the resulting array would exceed
     *         the capacity
     */
    public void append(char c) {
        insert(length, c);
    }

    /**
     * Sets the internal buffer to the values of the subset specified
     * The offset parameter must be within the range [0..(size())], inclusive.
     * The length parameter must be a non-negative integer such that 
     * (offset + length) <= size(). If data is null the buffer is emptied.
     *
     * @param data the data to use to set the buffer
     * @param offset offset into data
     * @param set_length length of the subset
     * @throws ArrayIndexOutOfBoundsException if offset and length
     *         specify an invalid range
     * @throws IllegalArgumentException if length exceeds the capacity
     */
    public void set(char[] data, int offset, int set_length) {

        if (data == null) {
            length = 0;
            return;
        }

        if (offset < 0 
            || offset > data.length 
            || set_length < 0
            || set_length > data.length
            || (offset + set_length) < 0
            || (offset + set_length) > data.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        if (set_length > buffer.length) { 
            throw new IllegalArgumentException();
        }

        System.arraycopy(data, offset, buffer, 0, set_length);
        length = set_length;
    }

    /**
     * Returns the internal buffer in the array provided. This must
     * be at least length() long.
     *
     * @param data array to fill with the character of the internal buffer
     * @throws IndexOutOfBoundsException if data cannot hold the contents
     * @throws NullPointerException if data is null
     */
    public void get(char[] data) {
        getChars(0, buffer.length, data, 0);
    }

    /**
     * Returns the internal buffer in the array provided. 
     *
     * @param position index into the internal buffer to start copying
     * @param get_length length of region to copy
     * @param data array to fill with the character of the internal buffer
     * @param offset offset into data to copy to
     * @throws IndexOutOfBoundsException if data cannot hold the contents
     * @throws NullPointerException if data is null
     */
    public void getChars(int position, int get_length, 
    			 char[] data, int offset) {
        System.arraycopy(buffer, position, data, offset, get_length);
    }

    /**
     * Returns a copy of the active portion of the internal buffer. 
     *
     * @return character array
     */
    public char[] toCharArray() {
        char[] buf = new char[length];
        System.arraycopy(buffer, 0, buf, 0, buf.length); 
        return buf;
    }

    /**
     * Deletes the specified range from the internal buffer
     *
     * @param offset offset to begin deleting
     * @param del_length length of portion to delete
     * @throws StringIndexOutOfBoundsException if offset and length do
     *         not specific a valid range in the internal buffer
     */
    public void delete(int offset, int del_length) {
        if (offset < 0 
            || del_length < 0 
            || (offset + del_length) < 0
            || (offset + del_length) > length) {
            throw new StringIndexOutOfBoundsException();
        }

        if ((offset + del_length) < length) {
            System.arraycopy(buffer, offset + del_length, 
                             buffer, offset, 
                             length - (offset + del_length));
        }

        length -= del_length;
    }

    /**
     * Sets the maximum capacity to the specified value. the buffer may
     * be truncated if the new capacity is less than the current capacity.
     *
     * @param capacity new maximum capacity
     * @throws IllegalArgumentException is zero or less
     */

    public void setCapacity(int capacity) {

        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }

        if (buffer.length == capacity) {
            return;
        }

        if (length > capacity) {
            length = capacity;
        }

        char[] newBuffer = new char[capacity];
        System.arraycopy(buffer, 0, newBuffer, 0, length);
 
        buffer = newBuffer;
    }

    /**
     * Returns the current capacity
     *
     * @return the maximum capacity 
     */
    public int capacity() {
        return buffer.length;
    }

    /**  
     * Returns the length of current data
     *
     * @return current length
     */ 
    public int length() {
        return length;
    }

    /**
     * Returns the character at the specified index of the internal buffer
     *
     * @param index index into the buffer
     * @return the character at the specified index
     * @throws IndexOutOfBoundsException if the 0 < index or index > length()
     */
    public char charAt(int index) {
        if (index < 0 || index > length) { 
            throw new IndexOutOfBoundsException();
        }

        return buffer[index];
    }

    /**
     * Sets the character at index to ch
     *
     * @param index index into the buffer
     * @param ch character to set to
     * @throws IndexOutOfBoundsException if the 0 < index or index > length()
     */
    public void setCharAt(int index, char ch) {
        if (index < 0 || index > length) { 
            throw new IndexOutOfBoundsException();
        }

        buffer[index] = ch;
    }

    /**
     * Return a String representation of the internal buffer
     *
     * @return a String object
     */
    public String toString() {
        return String.valueOf(buffer, 0, length);
    }
}

