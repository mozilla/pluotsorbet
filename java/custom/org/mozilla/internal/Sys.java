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

package org.mozilla.internal;

import com.sun.cldc.isolate.Isolate;

/**
 * The <code>Sys</code> class contains several useful privileged functions.
 */
public final class Sys {
  private Sys() { }

  public static void copyArray(byte [] src, int srcOffset,
                               byte [] dst, int dstOffset,
                               int length) {
    if (src == dst) {
      System.arraycopy(src, srcOffset, dst, dstOffset, length);
      return;
    }

    if (srcOffset < 0 || (srcOffset + length) > src.length || dstOffset < 0 || (dstOffset + length) > dst.length || length < 0) {
      throw new ArrayIndexOutOfBoundsException("Invalid index.");
    }

    for (int n = 0; n < length; ++n) {
      dst[dstOffset++] = src[srcOffset++];
    }
  }

  public static void copyArray(char [] src, int srcOffset,
                               char [] dst, int dstOffset,
                               int length) {
    if (src == dst) {
      System.arraycopy(src, srcOffset, dst, dstOffset, length);
      return;
    }

    if (srcOffset < 0 || (srcOffset + length) > src.length || dstOffset < 0 || (dstOffset + length) > dst.length || length < 0) {
      throw new ArrayIndexOutOfBoundsException("Invalid index.");
    }

    for (int n = 0; n < length; ++n) {
      dst[dstOffset++] = src[srcOffset++];
    }
  }

  /**
   * Evals code in the JS shell, only available in non-release builds as an
   * escape hatch for the purpose of testing and profiling.
   */
  public native static void eval(String src);

  /**
   * Returns the total number of times the VM has unwound threads.
   */
  public native static int getUnwindCount();

  public static void throwException(Exception e) throws Exception {
    throw e;
  }

  public static void runThread(Thread t) {
    t.run();
    synchronized (t) {
      t.notifyAll();
    }
  }

  public static void isolate0Entry(String name, String args[]) throws com.sun.cldc.isolate.IsolateStartupException {
    Isolate isolate = new com.sun.cldc.isolate.Isolate(name, args);
    isolate.start();
  }

  private native static void constructCurrentThread();
  private native static String getIsolateMain();
  private native static void executeMain(Class main);

  public static void isolateEntryPoint(Isolate isolate) throws ClassNotFoundException {
    // Run thread initializer.
    constructCurrentThread();
    // Get the main class.
    Class main = Class.forName(getIsolateMain());
    // Execute main.
    executeMain(main);
  }

  public native static void startProfile();
  public native static void stopProfile();

  public static void unwind() {}
  public static long unwind(long v) { return v; }
  public static double unwind(double v) { return v; }
  public static float unwind(float v) { return v; }
  public static int unwind(int v) { return v; }
  public static Object unwind(Object v) { return v; }

  public static void unwindFromInvoke() { unwind(); }
}
