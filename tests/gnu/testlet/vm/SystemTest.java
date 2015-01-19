/*
This license header comes from http://wiki.open-mika.org/wiki/MikaLicence.

Copyright  (c) 2001 by Acunia N.V. All rights reserved.
Parts copyright (c) 1999, 2000, 2001, 2002 by Punch Telematix. All rights reserved.
Parts copyright (c) 2003, 2004, 2005, 2006, 2007 by Chris Gray, /k/ Embedded Java Solutions.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions
   nor the names of other contributors may be used to endorse or promote
   products derived from this software without specific prior written
   permission.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL PUNCH
TELEMATIX, /K/ EMBEDDED SOLUTIONS OR OTHER CONTRIBUTORS BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package gnu.testlet.vm;

import gnu.testlet.*;

public class SystemTest implements Testlet {
  final static int ASX = 10;
  final static int NPX = 20;
  final static int ISX = 30;
  final static int AOK = 40;
  final static int ERR = 50;

  static public void fill(int[] a) {
    for (int i = 0; i < a.length; ++i)
      a[i] = i;
  }

  static public boolean check(int[] expect, int[] result) {

      boolean ok = expect.length == result.length;

      for (int i = 0; ok && i < expect.length; ++i) {
	if (expect[i] != result[i]) {
	  ok = false;
        }
      }
    
      return ok;

  }

  static public int copy (Object from, int a, Object to, int b, int c) {

    try {
      System.arraycopy (from, a, to, b, c);
      return AOK;
    }
    catch (ArrayStoreException xa) {
      return ASX;
    }
    catch (IndexOutOfBoundsException xb) {
      return ISX;
    }
    catch (NullPointerException xc) {
      return NPX;
    }
    catch (Throwable xd) {
      return ERR;
    }
  
  }

  public int test() {

    /*
    ** Arraycopy tests...
    */
     

    int[] x, y;

    x = new int[5];
    y = new int[5];
    fill(x);

    if (copy (x, 0, y, 0, x.length) != AOK) {
      return 160;
    }
      
    int[] one = { 0, 1, 2, 3, 4 };
    if (! check(y, one)) {
      return 170;
    }

    if (copy (x, 1, y, 0, x.length - 1) != AOK) {
      return 180;
    }

    if (copy (x, 0, y, x.length - 1, 1) != AOK) {
      return 190;
    }

    int[] two = { 1, 2, 3, 4, 0 };
    if (! check (y, two)) {
      return 200;
    }

    Object[] z = new Object[5];
    if (copy (x, 0, z, 0, x.length) != ASX) {
      return 210;
    }

    if (copy (x, 0, y, 0, -23) != ISX) {
      return 220;
    }

    if (copy (null, 0, y, 0, -23) != NPX) {
      return 230;
    }

    if (copy (x, 0, null, 0, -23) != NPX) {
      return 240;
    }

    String q = "metonymy";
    if (copy (q, 0, y, 0, 19) != ASX) {
      return 250;
    }

    if (copy (x, 0, q, 0, 19) != ASX) {
      return 260;
    }

    double[] v = new double[5];
    if (copy (x, 0, v, 0, 5) != ASX) {
      return 270;
    }

    if (copy (x, -1, y, 0, 1) != ISX) {
      return 280;
    }

    if (copy (x, 0, z, 0, x.length) != ASX) {
      return 290;
    }

    if (copy (x, 0, y, -1, 1) != ISX) {
      return 300;
    }

    if (copy (x, 3, y, 0, 5) != ISX) {
      return 310;
    }

    if (copy (x, 0, y, 3, 5) != ISX) {
      return 320;
    }

    Object[] w = new Object[5];
    String[] ss = new String[5];
    for (int i = 0; i < 5; i++) {
      w[i] = i + "";
      ss[i] = (i + 23) + "";
    }
    w[3] = new Integer (23);

    if (copy (w, 0, ss, 0, 5) != ASX) {
      return 330;
    }

    if (! ss[0].equals("0")) {
      return 340;
    }
    if (! ss[1].equals("1")) {
      return 350;
    }
    if (! ss[2].equals("2")) {
      return 360;
    }
    if (! ss[3].equals("26")) {
      return 370;
    }
    if (! ss[4].equals("27")) {
      return 380;
    }

    return 0;
    
  }

    public static void main(String[] args) {
	System.out.println(new SystemTest().test());
    }

    public void test(TestHarness harness) {
	harness.check(test(), 0);
    }
}
