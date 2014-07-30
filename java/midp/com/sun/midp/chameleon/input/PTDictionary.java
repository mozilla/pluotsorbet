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
 * Interface for predictive text processing
 *
 * This class is responsible for initializing the Dictionary
 * Two APIs are required,
 *  addWord : An interface to add a word to current dictionary (if supported)
 *  iterator: Get a iterator object with the following methods
 *          iterator.next()        : get next possible completion string
 *          iterator.hasNext()     : check if another possible completion exists
 *          iterator.nextLevel(int key) : add a char to the current completion
 *          iterator.provLevel()   : backspace last char from current completion
 *          iterator.reset()       : clear current completion
 *          iteratr.resetNext()    : revert to first possible completion string
 *
 * Example of using the predictive text API:
 *
 *      PTDictionary dictionary;
 *      PTIterator iter=dictionary.iterator();
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
 */
public interface PTDictionary {
    /**
     * get a iterator to the predictive text library
     *
     * @return a predictive text Iterator
     */
    
    public PTIterator iterator();
    /**
     * add a new word to the dictionary
     *
     * @param word new word to add to dictionary
     * @return true if new word was added, false otherwise
     */
    
    public boolean addWord(String word);
}
