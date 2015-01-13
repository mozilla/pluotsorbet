// Test of Long.method parseLong(String, radix).

// Copyright 2012 Red Hat, Inc.
// Written by Pavel Tisnovsky <ptisnovs@redhat.com>

// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published 
// by the Free Software Foundation, either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software Foundation
// Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA

// Tags: JDK1.4
// Tags: CompileOptions: -source 1.4

package gnu.testlet.java.lang.Long;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

/**
 * Test the method Long.parseLong(String, radix);
 */
public class parseLongRadix implements Testlet
{
  public void test (TestHarness harness)
  {
    long l;

    l = Long.parseLong("0", 2);
    harness.check(l, 0);
    l = Long.parseLong("0", 10);
    harness.check(l, 0);
    l = Long.parseLong("0", 16);
    harness.check(l, 0);
    l = Long.parseLong("0", 36);
    harness.check(l, 0);

    l = Long.parseLong("10", 8);
    harness.check(l, 8);
    l = Long.parseLong("10", 10);
    harness.check(l, 10);
    l = Long.parseLong("10", 16);
    harness.check(l, 16);

    l = Long.parseLong("z", 36);
    harness.check(l, 35);

    l = Long.parseLong("-80", 16);
    harness.check(l, -128);

    l = Long.parseLong("7f", 16);
    harness.check(l, 127);

    try
      {
	l = Long.parseLong("10", Character.MIN_RADIX - 1);
	harness.fail("too small radix");
      }
    catch (NumberFormatException nfe)
      {
	harness.check(true);
      }

    try
      {
	l = Long.parseLong("10", Character.MAX_RADIX + 1);
	harness.fail("too small radix");
      }
    catch (NumberFormatException nfe)
      {
	harness.check(true);
      }

    try
      {
	l = Long.parseLong("-9223372036854775809");
	harness.fail("-9223372036854775809 is to small for a Long");
      }
    catch (NumberFormatException nfe)
      {
	harness.check(true);
      }

    try
      {
	l = Long.parseLong("9223372036854775808");
	harness.fail("9223372036854775808 is to big for a Long");
      }
    catch (NumberFormatException nfe)
      {
	harness.check(true);
      }

    try
      {
        l = Long.parseLong("abc");
	harness.fail("Illegal input (abc) must throw NumberFormatException");
      }
    catch (NumberFormatException nfe)
      {
	harness.check(true);
      }

    try
      {
        l = Long.parseLong("-");
	harness.fail("Single '-' must throw NumberFormatException");
      }
    catch (NumberFormatException nfe)
      {
	harness.check(true);
      }

    try
    {
      l = Long.parseLong("+");
	harness.fail("Single '+' must throw NumberFormatException");
    }
  catch (NumberFormatException nfe)
    {
	harness.check(true);
    }

      try
        {
          l = Long.parseLong("+10", 10);
          harness.fail("'+10' must throw NumberFormatException");
        }
      catch (NumberFormatException nfe)
        {
          harness.check(true);
        }

    try
      {
        l = Long.parseLong(null, 10);
	harness.fail("null input must throw NumberFormatException");
      }
    catch (NullPointerException npe)
      {
	harness.fail("null input must throw NumberFormatException, not NullPointerException");
      }
    catch (NumberFormatException nfe)
      {
	harness.check(true);
      }
    
    try
      {
        l = Long.parseLong("", 10);
	harness.fail("empty input must throw NumberFormatException");
      }
    catch (IndexOutOfBoundsException ioobe)
      {
	harness.fail("empty input must throw NumberFormatException, not IndexOutOfBoundsException");
      }
    catch (NumberFormatException nfe)
      {
	harness.check(true);
      }
    }
}
