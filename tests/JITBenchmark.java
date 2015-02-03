import java.lang.Object;
import java.lang.System;
import java.util.Hashtable;
import java.util.Vector;
import java.io.ByteArrayOutputStream;

class JITBenchmark {

  static class A {}
  static class B extends A {}

  public static int size = 1024 * 256;

  public static void createObjectArrays() {
    Object array = null;
    for (int i = 0; i < size; i++) {
      array = new Object[64];
      array = new Object[128];
      array = new Object[256];
      array = new Object[512];
    }
  }

  public static void createPrimitiveArrays() {
    Object array = null;
    for (int i = 0; i < size; i++) {
      array = new int[64];
      array = new int[128];
      array = new int[256];
      array = new int[512];
    }
  }


  public static void writeByteArrayOutputStream() {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    for (int i = 0; i < size * 8; i++) {
      stream.write(i);
      stream.write(i);
      stream.write(i);
      stream.write(i);
    }

    System.out.println("Length: " + stream.size());
  }

  public static void concatStrings() {
    String s = "";
    int count = size / 16;
    for (int i = 0; i < count; i++) {
      s += "X";
    }
    System.out.println("Length: " + s.length());
  }

  public static void getBytes() {
    String s = "getBytesgetBytesgetBytesgetBytesgetBytesgetBytesgetBytesgetBytesgetBytesgetBytesgetBytesgetBytesgetBytesgetBytesgetBytesgetBytes";
    int count = size / 4;
    byte [] bytes = null;
    for (int i = 0; i < count; i++) {
      bytes = s.getBytes();
    }
    System.out.println("Length: " + bytes.length);
  }

  public static void synch() {
    int count = size * 10;
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
    System.out.println("Sum: " + sum);
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
    int count = size * 10;
    for (int i = 0; i < count - 1; i++) {
      array[i % 1024] = a;
      array[i % 1024] = b;
    }
  }

  public static void main(String[] args) {
    long start = 0;

    start = System.currentTimeMillis();
    System.out.println("createObjectArrays: ...");
    createObjectArrays();
    System.out.println("createObjectArrays: " + (System.currentTimeMillis() - start));

    start = System.currentTimeMillis();
    System.out.println("createPrimitiveArrays: ...");
    createPrimitiveArrays();
    System.out.println("createPrimitiveArrays: " + (System.currentTimeMillis() - start));

    start = System.currentTimeMillis();
    System.out.println("writeByteArrayOutputStream: ...");
    writeByteArrayOutputStream();
    System.out.println("writeByteArrayOutputStream: " + (System.currentTimeMillis() - start));

    start = System.currentTimeMillis();
    System.out.println("concatStrings: ...");
    concatStrings();
    System.out.println("concatStrings: " + (System.currentTimeMillis() - start));

    start = System.currentTimeMillis();
    System.out.println("getBytes: ...");
    getBytes();
    System.out.println("getBytes: " + (System.currentTimeMillis() - start));

    start = System.currentTimeMillis();
    System.out.println("synchronize: ...");
    synch();
    System.out.println("synchronize: " + (System.currentTimeMillis() - start));

    start = System.currentTimeMillis();
    System.out.println("hashtable: ...");
    hashtable();
    System.out.println("hashtable: " + (System.currentTimeMillis() - start));

    start = System.currentTimeMillis();
    System.out.println("arrayTypeCheck: ...");
    arrayTypeCheck();
    System.out.println("arrayTypeCheck: " + (System.currentTimeMillis() - start));
  }
}
