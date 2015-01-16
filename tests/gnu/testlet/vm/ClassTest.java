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

public class ClassTest implements Testlet {

  protected TestHarness th;
  public void test(TestHarness harness) {

    th = harness;
    th.checkPoint("basic class testing ...");
    String s1 = getClass().toString();
    String s2 = (getClass().isInterface() ? "interface " : "class ") + getClass().getName();
    th.check( s1.equals(s2),
          "S1 >>>" + s1 + "<<< S2 >>>" + s2 + "<<<");
    th.check( (new Object()).getClass().toString().equals("class java.lang.Object"));
    th.check( (new java.util.Vector()).getClass().getName().equals("java.util.Vector"));
    th.check( (new Object[3]).getClass().getName().equals("[Ljava.lang.Object;"));
    th.check( (new int[6][7][8]).getClass().getName().equals("[[[I"));
    th.check(!(new Object()).getClass().isInterface());
    th.check(! getClass().isInterface());

    Class xclss = getClass();
    Object obj;

    try {
      obj = xclss.newInstance();
      obj = xclss.newInstance();
      obj = xclss.newInstance();
      obj = xclss.newInstance();
    }
    catch ( Exception e ) {
      th.fail("should not throw an Exception -- 8");
    }
    catch ( Error e ) {
      th.fail("should not throw an Error -- 1");
    }

    try {
      obj = Class.forName("java.lang.Object");
      th.check(obj != null , "getting object");
    }
    catch ( Exception e ) {
      th.fail("should not throw an Exception -- 9");
    }

    try {
      Object obj1 = Class.forName("ab.cd.ef");
      th.fail("should throw a ClassNotFoundException");
    }
    catch ( ClassNotFoundException e ) {
    th.check(true);
    }

    try {
      Class obj1 = Class.forName("java.lang.String");
      th.check( obj1.isInstance("babu")) ;
      Class obj2 = Class.forName("java.lang.Integer");
      th.check( obj2.isInstance(new Integer(10)));
      int arr[]= new int[3];
      Class arrclass = Class.forName("[I");
      th.check( arrclass.isInstance(arr));
      Class cls1 = Class.forName("java.lang.String");
      Class supercls = Class.forName("java.lang.Object"); 
      th.check( supercls.isAssignableFrom(cls1));
      th.check(!cls1.isAssignableFrom(supercls));
      Class cls2 = Class.forName("java.lang.String");
      th.check( cls2.isAssignableFrom( cls1 ));
      arrclass = Class.forName("[I");
      Class arrclass1 = Class.forName("[[[I");
      Class arrclass2 = Class.forName("[[D");
      th.check(arrclass.isArray() && arrclass1.isArray() && arrclass2.isArray());
    }
    catch (Exception e) {
      th.fail("should not throw an Exception -- 11");
    }

    try {
      Class obj1 = Class.forName(null);
      th.fail();
    } catch (ClassNotFoundException e) {
      th.check(true);
    }
  }

}

