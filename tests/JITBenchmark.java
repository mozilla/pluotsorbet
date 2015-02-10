import java.lang.Object;
import java.lang.System;
import java.util.Hashtable;
import java.util.Vector;
import java.io.ByteArrayOutputStream;
import org.mozilla.internal.Sys;

class JITBenchmark {

  static class A {}
  static class B extends A {}

  public static int size = 0;
  public static long start = 0;

  public static void createObjectArrays() {
    Object array = null;
    int count = size / 8;
    for (int i = 0; i < count; i++) {
      array = new Object[64];
      array = new Object[128];
      array = new Object[256];
      array = new Object[512];
    }
  }

  public static void createPrimitiveArrays() {
    Object array = null;
    int count = size / 8;
    for (int i = 0; i < count; i++) {
      array = new int[64];
      array = new int[128];
      array = new int[256];
      array = new int[512];
    }
  }


  public static void writeByteArrayOutputStream() {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    int count = size * 16;
    for (int i = 0; i < count; i++) {
      stream.write(i);
      stream.write(i);
      stream.write(i);
      stream.write(i);
    }
  }

  public static void concatStrings() {
    String s = "";
    int count = size / 16;
    for (int i = 0; i < count; i++) {
      s += "X";
    }
  }

  public static void getBytes() {
    String s = "getBytesgetBytesgetBytesgetBytesgetBytesgetBytesgetBytesgetBytesgetBytesgetBytesgetBytesgetBytesgetBytesgetBytesgetBytesgetBytes";
    int count = size / 4;
    byte [] bytes = null;
    for (int i = 0; i < count; i++) {
      bytes = s.getBytes();
    }
  }

  public static void synch() {
    int count = size * 32;
    byte [] bytes = null;
    Object o = new Object();
    int sum = 0;
    for (int i = 0; i < count; i++) {
      synchronized (o) {
        sum++;
      }
      synchronized (o) {
        sum++;
      }
      synchronized (o) {
        sum++;
      }
      synchronized (o) {
        sum++;
      }
      synchronized (o) {
        sum++;
      }
      synchronized (o) {
        sum++;
      }
    }
  }

  public static void hashtable() {
    int count = size * 2;
    Hashtable hash = new Hashtable();
    Object o = new Object();
    String [] names = {
      "hello", "world", "hello1", "world2", "hello3", "world4", "hello5", "world6"
    };
    for (int i = 0; i < count; i++) {
      String name = names[i % names.length];
      hash.put(name, o);
      hash.get(name);
      hash.put(name, o);
      hash.get(name);
      hash.put(name, o);
      hash.get(name);
      hash.put(name, o);
      hash.get(name);
    }
  }

  public static void arrayTypeCheck() {
    A [] array = new A [1024];
    A a = new A();
    B b = new B();
    int count = size * 3;
    for (int i = 0; i < count - 1; i++) {
      array[i % 1024] = a;
      array[i % 1024] = b;
    }
  }

  public static void begin() {
    System.gc();
    start = System.currentTimeMillis();
  }

  public static void finish(String name) {
    System.out.println(name + ": " + (System.currentTimeMillis() - start));
  }

  public static void main(String[] args) {

    // Sys.eval("J2ME.emitCheckArrayStore = false;");
    // Sys.eval("J2ME.emitCheckArrayBounds = false;");

    size = 1024;

    begin();
    start = System.currentTimeMillis();
    createObjectArrays();
    createPrimitiveArrays();
    writeByteArrayOutputStream();
    concatStrings();
    getBytes();
    synch();
    hashtable();
    arrayTypeCheck();
    finish("startup");

    size = 1024 * 256;

    long start = System.currentTimeMillis();
    begin();
    createObjectArrays();
    finish("createObjectArrays");

    begin();
    createPrimitiveArrays();
    finish("createPrimitiveArrays");

    begin();
    writeByteArrayOutputStream();
    finish("writeByteArrayOutputStream");

    begin();
    concatStrings();
    finish("concatStrings");

    begin();
    getBytes();
    finish("getBytes");

    begin();
    synch();
    finish("synchronize");

    begin();
    hashtable();
    finish("hashtable");

    begin();
    arrayTypeCheck();
    finish("arrayTypeCheck");

    System.out.println();
    System.out.println("Total: " + (System.currentTimeMillis() - start));
  }
}
