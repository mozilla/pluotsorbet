// Test for OutputStreamWriter methods

// Written by Daryl Lee (dol@sources.redhat.com)
// Elaboration of except.java  by paul@dawa.demon.co.uk

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
// Boston, MA 02111-1307, USA.

// Tags: JDK1.1

package gnu.testlet.java.io.OutputStreamWriter;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.io.OutputStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class jdk11 implements Testlet
{
  public int getExpectedPass() { return 6; }
  public int getExpectedFail() { return 0; }
  public int getExpectedKnownFail() { return 0; }

  public void test (TestHarness harness)
  {
    try
      {
		String tstr = "ABCDEFGH";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter (baos);  //Default encoding
		harness.check(true, "OutputStreamWriter(writer)");

		osw.write(tstr.charAt(0));					// 'A'
		harness.check(true,"write(int)");
		osw.write("ABCDE", 1, 3);					// 'ABCD'
		harness.check(true,"write(string, off, len)");
		char[] cbuf = new char[8];
		tstr.getChars(4, 8, cbuf, 0);
		osw.write(cbuf, 0, 4);						// 'ABCDEFGH'
		harness.check(true,"write(char[], off, len)");
		osw.flush();
		harness.check(true, "flush()");
		harness.check(baos.toString(), tstr, "Wrote all characters okay");	
		osw.close ();
      }
    catch (IOException e)
      {
		harness.check(false, "IOException unexpected");
      }
  }
}
