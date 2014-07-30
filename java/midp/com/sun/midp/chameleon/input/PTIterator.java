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

package com.sun.midp.chameleon.input;

/**
 *  Aniterator that provides predictive text browsing capabilities
 *  Requires only single word predictive text librray support.
 *  Traversal is done by using nextLevel(keyCode) to enter keys and
 *  next() to get possible completions.
 *
 *  Exmaple of using predictive text iterators:
 *
 *      PTDictionary dictionary;
 *      Iterator iter=dictionary.iterator();
 *      iter.nextLevel('2');
 *      iter.nextLevel('2');
 *      iter.nextLevel('2');
 *      while(iter.hasNext()) {
 *          String completion=iter.next();
 *          System.out.println(completion);
 *          //will print "aca" (short of "academy") "cab" (short of "cabin"),
 *          // "acc" (short of "accelerate") etc.
 *      }
 *
 * PTIterator operations:
 *          next()              : get next possible completion string
 *          hasNext()           : check if another possible completion exists
 *          nextLevel(int key)  : add a char to the current completion
 *          prevLevel()         : backspace last char from current completion
 *          reset()             : clear current completion
 *          resetNext()         : revert to first possible completion string
 */
public interface PTIterator {
    
    /**
     * add a key to current completion string
     * @param keyCode char in the range '0'-'9', '#', or '*'
     */
    public void nextLevel(int keyCode);
    /**
     * backspace on key in current completion string
     */
    public void prevLevel();
    /**
     * Returns true if the iteration has more elements. (In other words,
     * returns <code>true</code> if <code>next</code> would return an
     * element rather than throwing an exception.)
     *
     * @return true if the iterator has more elements.
     */
    public boolean hasNext();
    /**
     * Revert to first possible completion.
     * If next() has been called uptil hasNext() returns false, then after
     * calling resetNext(), calling next() will return the 1st completion
     */
    public void resetNext();
    /**
     * Returns the next element in the iteration.
     * @return next element in the iteration.
     * @exception NoSuchElementException iteration has no more elements.
     */
    public String next();
    /**
     * create a new handle and clear completion state by calling
     * ptNewIterator0()
     */
    public void reset();
}
