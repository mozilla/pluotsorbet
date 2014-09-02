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

/*
 * NOTE:
 * Because of various external restrictions (i.e. US export
 * regulations, etc.), the actual source code can not be provided
 * at this time. This file represents the skeleton of the source
 * file, so that javadocs of the API can be created.
 */

package javax.crypto;
import java.security.*;

/** 
 * This exception is thrown when the length of data provided to a block
 * cipher is incorrect, i.e., does not match the block size of the cipher.
 *
 */
public class IllegalBlockSizeException  extends GeneralSecurityException 
    
{

    /** 
     * Constructs an IllegalBlockSizeException with no detail message.
     * (A detail message is a String that describes this particular
     * exception.)  
     */
    public IllegalBlockSizeException() { 
	super();
    }

    /** 
     * Constructs an IllegalBlockSizeException with the specified
     * detail message. (A detail message is a String that describes
     * this particular exception.)  
     *
     * @param msg the detail message. 
     */
    public IllegalBlockSizeException(String msg) { 
	super(msg);
    }
}
