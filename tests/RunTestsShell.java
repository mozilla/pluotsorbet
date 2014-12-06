import gnu.testlet.*;

import java.util.Vector;

public class RunTestsShell {
static String[] list = {
"gnu/testlet/java/io/ByteArrayInputStreamTest",
"gnu/testlet/java/io/ByteArrayOutputStreamTest",
"gnu/testlet/java/io/DataInputStreamTest",
"gnu/testlet/java/io/DataOutputStreamTest",
"gnu/testlet/java/io/PrintStreamTest",
"gnu/testlet/java/lang/Boolean/BooleanTest",
"gnu/testlet/java/lang/Boolean/BooleanTest2",
"gnu/testlet/java/lang/Boolean/equals_Boolean",
"gnu/testlet/java/lang/Boolean/hashcode_Boolean",
"gnu/testlet/java/lang/Byte/byteDivide",
"gnu/testlet/java/lang/Byte/ByteTest",
"gnu/testlet/java/lang/Byte/new_Byte",
"gnu/testlet/java/lang/Byte/parseByte",
"gnu/testlet/java/lang/Byte/parseByteRadix",
"gnu/testlet/java/lang/Character/CharacterTest",
"gnu/testlet/java/lang/Character/hash",
"gnu/testlet/java/lang/Character/to",
"gnu/testlet/java/lang/Date/DateTest",
"gnu/testlet/java/lang/Double/DoubleTest",
"gnu/testlet/java/lang/Integer/IntegerTest",
"gnu/testlet/java/lang/Math/max",
"gnu/testlet/java/lang/Math/min",
"gnu/testlet/java/lang/Object/constructor",
"gnu/testlet/java/lang/Object/ObjectTest",
"gnu/testlet/java/lang/Object/wait",
"gnu/testlet/java/lang/System/arraycopy",
"gnu/testlet/java/lang/System/getProperty",
// "gnu/testlet/java/lang/Thread/isAlive",
"gnu/testlet/java/lang/Thread/name",
// "gnu/testlet/java/lang/Thread/wait",
"gnu/testlet/vm/ArrayTest",
"gnu/testlet/vm/BytecodesTest",
"gnu/testlet/vm/ClassTest",
"gnu/testlet/vm/ClassTest2",
"gnu/testlet/vm/ConditionsTest",
"gnu/testlet/vm/DupTest",
"gnu/testlet/vm/ExceptionTest",
"gnu/testlet/vm/FieldNotFoundException",
"gnu/testlet/vm/GetBytesTest",
"gnu/testlet/vm/InterfaceTest",
"gnu/testlet/vm/LongTest",
"gnu/testlet/vm/MathTest",
"gnu/testlet/vm/MethodNotFoundException",
"gnu/testlet/vm/NestedExceptionTest",
"gnu/testlet/vm/ObjectsTest",
"gnu/testlet/vm/OpsTest",
"gnu/testlet/vm/OverrideTest",
"gnu/testlet/vm/RuntimeTest",
"gnu/testlet/vm/StringBufferTest",
"gnu/testlet/vm/StringTest",
"gnu/testlet/vm/SystemTest",
"gnu/testlet/vm/TestArithmetic",
// "gnu/testlet/vm/TestIsolate",
"gnu/testlet/vm/ThrowableTest",
"java/lang/TestArrayPrameter",
"java/lang/TestString",
"java/lang/TestStringIntern",
// "java/lang/TestSystem",
// "java/lang/TestThread",
"org/json/me/TestJSON",
"test/org/jikesrvm/basic/bugs/R1644449",
"test/org/jikesrvm/basic/bugs/R1644460",
"test/org/jikesrvm/basic/bugs/R1644460_B",
"test/org/jikesrvm/basic/bugs/R1657236",
"test/org/jikesrvm/basic/core/bytecode/TestArrayAccess",
"test/org/jikesrvm/basic/core/bytecode/TestCompare",
"test/org/jikesrvm/basic/core/bytecode/TestConstants",
"test/org/jikesrvm/basic/core/bytecode/TestFieldAccess",
"test/org/jikesrvm/basic/core/bytecode/TestFinally",
"test/org/jikesrvm/basic/core/bytecode/TestFloatingRem",
"test/org/jikesrvm/basic/core/bytecode/TestResolveOnCheckcast",
"test/org/jikesrvm/basic/core/bytecode/TestResolveOnInstanceof",
"test/org/jikesrvm/basic/core/bytecode/TestResolveOnInvokeInterface",
"test/org/jikesrvm/basic/core/bytecode/TestReturn",
"test/org/jikesrvm/basic/core/bytecode/TestSwitch",
"test/org/jikesrvm/basic/core/bytecode/TestThrownException",
"test/org/jikesrvm/basic/core/classloading/TestClassLoading",
"test/org/jikesrvm/basic/core/classloading/TestUTF8",
"test/org/jikesrvm/basic/java/lang/TestMath",

// -------The Following will only work in browser-------
// "gnu/testlet/vm/NativeTest",

// -------The Following will probably only work with the midlets-------
// "org/mozilla/io/TestLocalMsgProtocol",
// "org/mozilla/io/TestNokiaContactsServer",
// "org/mozilla/io/TestNokiaImageProcessingServer",
// "org/mozilla/io/TestNokiaMessagingServer",
// "org/mozilla/io/TestNokiaPhoneStatusServer",
// "javax/crypto/TestRC4",
// "javax/microedition/io/file/TestFileSystemRegistry",
// "javax/microedition/io/TestHttpsConnection",
// "javax/microedition/lcdui/game/TestSprite",
// "javax/microedition/lcdui/TestGraphicsClipping",
// "javax/microedition/lcdui/TestKeyConverter",
// "javax/microedition/lcdui/TestOne",
// "javax/microedition/lcdui/TestStringItemNoLabelSizing",
// "javax/microedition/lcdui/TestStringItemSizing",
// "javax/microedition/lcdui/TestTwo",
// "javax/microedition/pim/TestPIM",
// "javax/microedition/rms/TestRecordStore",
// "com/ibm/oti/connection/file/TestFileConnection",
// "com/nokia/mid/s40/codec/TestDataEncodeDecode",
// "com/nokia/mid/ui/frameanimator/TestFrameAnimator",
// "com/nokia/mid/ui/TestDirectGraphics",
// "com/nokia/mid/ui/TestTextEditor",
// "com/sun/cldc/i18n/TestUtfReaders",
// "com/sun/cldc/io/TestResourceInputStream",
// "com/sun/midp/crypto/TestAES",
// "com/sun/midp/crypto/TestARC4",
// "com/sun/midp/crypto/TestDES",
// "com/sun/midp/crypto/TestMD5",
// "com/sun/midp/crypto/TestRSA",
// "com/sun/midp/crypto/TestSHA",
// "com/sun/midp/events/TestEventQueue",
// "com/sun/midp/events/TestNativeEventPool",
// "com/sun/midp/io/j2me/http/TestHttpConnection",
// "com/sun/midp/io/j2me/http/TestHttpHeaders",
// "com/sun/midp/io/j2me/socket/StressTestSocket",
// "com/sun/midp/io/j2me/socket/TestSocket",
// "com/sun/midp/io/j2me/storage/TestRandomAccessStream",
// "com/sun/midp/io/TestHttpUrl",
// "com/sun/midp/io/TestUrl",
// "com/sun/midp/publickeystore/TestInputOutputStorage",
// "com/sun/midp/publickeystore/TestWebPublicKeyStore",
// "com/sun/midp/rms/TestRecordStoreFileNatives",
// "com/sun/midp/ssl/TestSSLStreamConnection",
// "com/sun/midp/util/isolate/TestInterIsolateMutex",
  null};

    private static class Harness extends TestHarness {
        private String testName;
        private int testNumber = 0;
        private String testNote = null;
        private int pass = 0;
        private int fail = 0;
        private int knownFail = 0;
        private int unknownPass = 0;

        public Harness(String note) {
            this.testName = note;
        }

        public void setNote(String note) {
            testNote = note;
        }

        public void debug(String msg) {
            System.out.println(testName + "-" + testNumber + ": " + msg + ((testNote != null) ? (" [" + testNote + "]") : ""));
        }

        public void check(boolean ok) {
            if (ok) {
                ++pass;
            }
            else {
                ++fail;
                debug("fail");
            }
            ++testNumber;
            setNote(null);
        }

        public void todo(boolean ok) {
            if (ok)
                ++unknownPass;
            else
                ++knownFail;
            ++testNumber;
            setNote(null);
        }

        public void report() {
            System.out.println(testName + ": " + pass + " pass, " + fail + " fail");
        }

        public int passed() {
            return pass;
        }

        public int failed() {
            return fail;
        }

        public int knownFailed() {
            return knownFail;
        }

        public int unknownPassed() {
            return unknownPass;
        }
    };

    int pass = 0, fail = 0, knownFail = 0, unknownPass = 0;

    void runTest(String name) {
        name = name.replace('/', '.');
        Harness harness = new Harness(name);
        Class c = null;
        try {
            c = Class.forName(name);
        } catch (Exception e) {
            System.err.println(e);
            harness.fail("Can't load test");
        }
        Object obj = null;
        try {
            obj = c.newInstance();
        } catch (Exception e) {
            System.err.println(e);
            harness.fail("Can't instantiate test");
        }
        Testlet t = (Testlet) obj;
        t.test(harness);
        if (harness.failed() > 0)
            harness.report();
        pass += harness.passed();
        fail += harness.failed();
        knownFail += harness.knownFailed();
        unknownPass += harness.unknownPassed();
    }

    public void runTests(String args[]) {
       // if (args != null && args.length > 0) {
       //      Vector v = new Vector();
       //      for (int n = 0; n < Testlets.list.length; ++n) {
       //          v.addElement(Testlets.list[n]);
       //      }

       //      for (int i = 0; i < args.length; i++) {
       //          String arg = args[i];
       //          if (v.contains(arg)) {
       //              runTest(arg);
       //          } else {
       //              System.err.println("can't find test " + arg);
       //          }
       //      }
       //  } else {
            for (int n = 0; n < list.length; ++n) {
                String name = list[n];
                if (name == null)
                    break;
                runTest(name);
            }
        // }
        System.out.println("DONE: " + pass + " pass, " + fail + " fail, " + knownFail + " known fail, " + unknownPass + " unknown pass"); 
    }

    public static void main(String args[]) {
        RunTestsShell rts = new RunTestsShell();
        rts.runTests(args);
    }
};
