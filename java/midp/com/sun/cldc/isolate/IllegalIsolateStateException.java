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

import java.io.PrintStream;

/**
 * Signals that a method of <code>Isolate</code> has been invoked at an 
 * illegal or inappropriate time. In other words, the 
 * <code>Isolate</code> is not in an appropriate state for the requested 
 * operation.
 **/
public class IllegalIsolateStateException extends RuntimeException {  
  /**
   * Constructs an <code>IllegalIsolateStateException</code> with no specified detail
   * message.
   */
  public IllegalIsolateStateException() {
  }

  /**
   * Constructs an <code>IllegalIsolateStateException</code> with specified detail
   * message.
   */
  public IllegalIsolateStateException(String detail) {
    super(detail);
  }
}
