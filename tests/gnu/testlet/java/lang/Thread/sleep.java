// Tags: JDK1.0

// Copyright (C) 2002 Free Software Foundation, Inc.
// Written by Mark Wielaard (mark@klomp.org)

// This file is part of Mauve.

// Mauve is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2, or (at your option)
// any later version.

// Mauve is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Mauve; see the file COPYING.  If not, write to
// the Free Software Foundation, 59 Temple Place - Suite 330,
// Boston, MA 02111-1307, USA.  */


package gnu.testlet.java.lang.Thread;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import com.sun.cldchi.jvm.JVM;


public class sleep extends Thread implements Testlet
{
  public int getExpectedPass() { return 1; }
  public int getExpectedFail() { return 0; }
  public int getExpectedKnownFail() { return 0; }

  public void test (TestHarness harness)
  {
    long start = JVM.monotonicTimeMillis();

    try {
      Thread.sleep(100);
      Thread.sleep(100);
      Thread.sleep(100);
      Thread.sleep(100);
      Thread.sleep(100);
      harness.check((JVM.monotonicTimeMillis() - start) > 200, "Not sleeping: " + (JVM.monotonicTimeMillis() - start));
    } catch (InterruptedException e) {

    }
  }
}

