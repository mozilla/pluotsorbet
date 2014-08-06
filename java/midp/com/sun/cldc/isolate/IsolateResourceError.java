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

package com.sun.cldc.isolate;

/**
 * IsolateResourceError is thrown when the resource requirements required for an action
 * cannot be fulfilled. 
 *
 * <p>The conditions leading to this exception may be transient or permanent 
 * depending on the action and the resource involved. Refer to the
 * specific documentation for the constructor or method in question.
 *
 * <p>Note that resource in the context of this exception is
 * defined as something required for program operations, for which the lack
 * thereof causes a failure.
 *
 **/

public class IsolateResourceError extends Error {  
  /**
   * Constructs a <code>IsolateResourceError</code> with no specified detail
   * message.
   */
  public IsolateResourceError() {
  }

  /**
   * Constructs a <code>IsolateResourceError</code> with specified detail
   * message.
   *
   * @param detail Detailed information about the exception
   */
  public IsolateResourceError(String detail) {
  }

  /**
   * Constructs a <code>IsolateResourceError</code> that wraps the specified 
   * throwable.
   * @param cause the cause (which is saved for later retrieval by the 
   * {@link #getCause getCause()} method). (A <code>null</code> value is 
   * permitted and indicates that the cause is nonexistent or unknown.)
   */
  public IsolateResourceError(Throwable cause) {
    super((cause == null)?null:cause.toString());
  }
  /**
   * Constructs a <code>IsolateResourceError</code> that wraps the specified
   * throwable and provides the given detail message.
   * @param detail Detailed information about the exception
   * @param cause the cause (which is saved for later retrieval by the 
   * {@link #getCause getCause()} method). (A <code>null</code> value is 
   * permitted and indicates that the cause is nonexistent or unknown.)
   */
  public IsolateResourceError(String detail, Throwable cause) {
  }
}
