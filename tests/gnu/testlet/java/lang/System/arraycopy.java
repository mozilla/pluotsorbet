// Tags: JDK1.0

// Copyright (C) 1998, 2003 Red Hat, Inc.
// Copyright (C) 2004 Mark Wielaard (mark@klomp.org)

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

package gnu.testlet.java.lang.System;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class arraycopy implements Testlet
{
  public int getExpectedPass() { return 62; }
  public int getExpectedFail() { return 0; }
  public int getExpectedKnownFail() { return 0; }

  public void fill(int[] a) {
      for (int i = 0; i < a.length; ++i) {
          a[i] = i;
      }
  }

  public void fill(byte[] a) {
      for (int i = 0; i < a.length; ++i) {
          a[i] = (byte)i;
      }
  }

  public void fill(char[] a) {
      for (int i = 0; i < a.length; ++i) {
          a[i] = (char)i;
      }
  }

  public void fill(long[] a) {
      for (int i = 0; i < a.length; ++i) {
          a[i] = i;
      }
  }

  public void fill(double[] a) {
      for (int i = 0; i < a.length; ++i) {
          a[i] = i;
      }
  }

  public void check (TestHarness harness, int[] expect, int[] result) {
      boolean ok = expect.length == result.length;

      for (int i = 0; ok && i < expect.length; ++i) {
          if (expect[i] != result[i]) {
	            ok = false;
          }

          harness.check (ok);
      }
  }

  public void check (TestHarness harness, byte[] expect, byte[] result) {
      boolean ok = expect.length == result.length;

      for (int i = 0; ok && i < expect.length; ++i) {
          if (expect[i] != result[i]) {
              ok = false;
          }

          harness.check (ok);
      }
  }

  public void check (TestHarness harness, char[] expect, char[] result) {
      boolean ok = expect.length == result.length;

      for (int i = 0; ok && i < expect.length; ++i) {
          if (expect[i] != result[i]) {
              ok = false;
          }

          harness.check (ok);
      }
  }

  public void check (TestHarness harness, long[] expect, long[] result) {
      boolean ok = expect.length == result.length;

      for (int i = 0; ok && i < expect.length; ++i) {
          if (expect[i] != result[i]) {
              ok = false;
          }

          harness.check (ok);
      }
  }

  public void check (TestHarness harness, double[] expect, double[] result) {
      boolean ok = expect.length == result.length;

      for (int i = 0; ok && i < expect.length; ++i) {
          if (expect[i] != result[i]) {
              ok = false;
          }

          harness.check (ok);
      }
  }

  public Object copy (Object from, int a, Object to, int b, int c)
    {
      try
	{
	  System.arraycopy (from, a, to, b, c);
	}
      catch (ArrayStoreException xa)
	{
	  return "caught ArrayStoreException";
	}
      catch (IndexOutOfBoundsException xb)
	{
	  return "caught IndexOutOfBoundsException";
	}
      catch (NullPointerException xc)
	{
	  return "caught NullPointerException";
	}
      catch (Throwable xd)
	{
	  return "caught unexpected exception";
	}

      return null;
    }

  public void test (TestHarness harness)
    {
      int[] x, y;

      x = new int[5];
      y = new int[5];
      fill (x);

      harness.checkPoint("Copying integer array");
      harness.check (copy (x, 0, y, 0, x.length), null);
      int[] one = { 0, 1, 2, 3, 4 };
      check (harness, y, one);

      harness.check (copy (x, 1, y, 0, x.length - 1), null);
      harness.check (copy (x, 0, y, x.length - 1, 1), null);
      int[] two = { 1, 2, 3, 4, 0 };
      check (harness, y, two);

      harness.checkPoint("Copying byte array");
      byte[] xBytes = new byte[5];
      byte[] yBytes = new byte[5];
      fill(xBytes);
      harness.check(copy(xBytes, 0, yBytes, 0, xBytes.length), null);
      byte[] oneBytes = { 0, 1, 2, 3, 4 };
      check(harness, yBytes, oneBytes);

      harness.checkPoint("Copying char array");
      char[] xChars = new char[5];
      char[] yChars = new char[5];
      fill(xChars);
      harness.check(copy(xChars, 0, yChars, 0, xChars.length), null);
      char[] oneChars = { 0, 1, 2, 3, 4 };
      check(harness, yChars, oneChars);

      harness.checkPoint("Copying long array");
      long[] xLongs = new long[5];
      long[] yLongs = new long[5];
      fill(xLongs);
      harness.check(copy(xLongs, 0, yLongs, 0, xLongs.length), null);
      long[] oneLongs = { 0, 1, 2, 3, 4 };
      check(harness, yLongs, oneLongs);

      harness.checkPoint("Copying double array");
      double[] xDoubles = new double[5];
      double[] yDoubles = new double[5];
      fill(xDoubles);
      harness.check(copy(xDoubles, 0, yDoubles, 0, xDoubles.length), null);
      double[] oneDoubles = { 0, 1, 2, 3, 4 };
      check(harness, yDoubles, oneDoubles);

      harness.checkPoint("Incompatible arrays");

      Object[] z = new Object[5];
      harness.check (copy (x, 0, z, 0, x.length),
		     "caught ArrayStoreException");

      harness.checkPoint("negative length");

      harness.check (copy (x, 0, y, 0, -23),
		     "caught IndexOutOfBoundsException");

      harness.checkPoint("null arrays");

      harness.check (copy (null, 0, y, 0, -23),
		     "caught NullPointerException");

      harness.check (copy (x, 0, null, 0, -23),
		     "caught NullPointerException");

      harness.checkPoint("Non arrays");

      String q = "metonymy";
      harness.check (copy (q, 0, y, 0, 19),
		     "caught ArrayStoreException");

      harness.check (copy (x, 0, q, 0, 19),
		     "caught ArrayStoreException");

      harness.checkPoint("Incompatible arrays 2");

      double[] v = new double[5];
      harness.check (copy (x, 0, v, 0, 5),
		     "caught ArrayStoreException");

      harness.checkPoint("Bad offset");

      harness.check (copy (x, -1, y, 0, 1),
		     "caught IndexOutOfBoundsException");

      harness.checkPoint("Incompatible arrays 3");

      harness.check (copy (x, 0, z, 0, x.length),
		     "caught ArrayStoreException");

      harness.checkPoint("Bad offset 2");

      harness.check (copy (x, 0, y, -1, 1),
		     "caught IndexOutOfBoundsException");

      harness.check (copy (x, 3, y, 0, 5),
		     "caught IndexOutOfBoundsException");

      harness.check (copy (x, 0, y, 3, 5),
		     "caught IndexOutOfBoundsException");

      // Regression test for missing check in libgcj.
      harness.check (copy (x, 4, y, 4, Integer.MAX_VALUE),
		     "caught IndexOutOfBoundsException");

      harness.checkPoint("Object casting");

      Object[] w = new Object[5];
      String[] ss = new String[5];
      for (int i = 0; i < 5; ++i)
	{
	  w[i] = i + "";
	  ss[i] = (i + 23) + "";
	}
      w[3] = new Integer (23);

      harness.check (copy (w, 0, ss, 0, 5),
		     "caught ArrayStoreException");
      harness.check (ss[0], "0");
      harness.check (ss[1], "1");
      harness.check (ss[2], "2");
      harness.check (ss[3], "26");
      harness.check (ss[4], "27");

      harness.checkPoint("Different dimensions");
      harness.check (copy (new Object[1][1], 0, new Object[1], 0,  1), null);
      harness.check (copy (new int[1][1], 0, new Object[1], 0,  1), null);
      Object[] objs = new Object[1];
      objs[0] = new int[1];
      harness.check (copy (objs, 0, new int[1][1], 0,  1), null);
      harness.check (copy (new String[1][1], 0, new Object[1], 0,  1), null);
      harness.check (copy (new int[1][1], 0, new int[1], 0,  1),
		     "caught ArrayStoreException");
      harness.check (copy (new int[1], 0, new int[1][1], 0,  1),
		     "caught ArrayStoreException");
    }
}
