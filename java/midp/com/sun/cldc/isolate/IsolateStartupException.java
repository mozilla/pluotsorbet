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
 * An exception thrown when the implementation is unable to start
 * execution of a new isolate due to an error or exceptional condition
 * in the bootstrap code for the new isolate (i.e., before the
 * application starts).  Generally, this exception implies the new
 * isolate was misconfigured, and the misconfiguration was detected
 * when the isolate was initializing (either during the {@linkplain
 * Isolate#Isolate constructor call} or {@link Isolate#start
 * Isolate.start invocation}).  If the underlying misconfiguration resulted in an
 * exception in the new isolate, the accessors for the remote exception
 * information will provide the details.  If not, those methods will
 * all return <code>null</code>.
 *
 * <p> Any errors or exceptions thrown by application code in the new
 * isolate will be handled by the default {@link
 * ThreadGroup#uncaughtException ThreadGroup.uncaughtException} and
 * will not manifest as an <code>IsolateStartupException</code> in the creating
 * isolate.
 *
 * <p>Because any nested exception occurred in a remote
 * isolate, the exception cannot be directly {@linkplain
 * java.lang.Throwable#initCause chained} (for example, the exception
 * type may be available in the creator).  However, the information
 * about the remote exception is available from the {@link
 * #getRemoteName}, {@link #getRemoteMessage} methods.
 *
 * <p>The detail message in an <code>IsolateStartupException</code> will
 * be the <code>toString</code> string of the original exception in the
 * remote isolate, if available.  The backtrace associated with an
 * <code>IsolateStartupException</code> will be from the current thread's
 * stack.
 *
 * @see Isolate#Isolate
 * @see Isolate#start
 **/
public class IsolateStartupException extends Exception {  
  /**
   * Constructs an <code>IsolateStartupException</code> with no specified detail
   * message and <code>null</code> remote exception information.
   */
  public IsolateStartupException() {
  }

  /**
   * Constructs an <code>IsolateStartupException</code> with specified detail
   * message and <code>null</code> remote exception information.
   */
  public IsolateStartupException(String detail) {
    super(detail);
  }

  /**
   * Returns either the name of the exception type of the remote exception
   * or <code>null</code> if there was no reportable remote Java exception.
   *
   * @return the name of the exception type of the remote exception
   */
  public String getRemoteName() {
    return null;
  }

  /**
   * Returns the detail message string for the remote exception (which
   * may be <code>null</code> even if a remote exception occured).
   *
   * @see #getRemoteName
   * @return the detail message string for the remote exception.
   */
  public String getRemoteMessage() {
    return null;
  }

  /*
   * Prints the remote exception name, message, and stack trace, like
   * {@link java.lang.Throwable#printStackTrace()} does for the local
   * exception.  If no exception information is available, nothing
   * will be printed.
   */
  public void printRemoteStackTrace() {
  }
  
  /**
   * Prints the remote exception like {@link #printRemoteStackTrace()}
   * to the given <code>PrintStream</code>.
   * If no exception information is available, nothing
   * will be printed.
   */
  public void printRemoteStackTrace(PrintStream ps) {
  }
}
