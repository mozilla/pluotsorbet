import java.lang.Object;
import java.lang.System;
import java.util.Vector;
import java.io.ByteArrayOutputStream;

class JITBenchmark {

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

  public static void main(String[] args) {
    long start = 0;

    start = System.currentTimeMillis();
    System.out.println("createObjectArrays: ...");
    // createObjectArrays();
    System.out.println("createObjectArrays: " + (System.currentTimeMillis() - start));

    start = System.currentTimeMillis();
    System.out.println("createPrimitiveArrays: ...");
    // createPrimitiveArrays();
    System.out.println("createPrimitiveArrays: " + (System.currentTimeMillis() - start));

    start = System.currentTimeMillis();
    System.out.println("writeByteArrayOutputStream: ...");
    writeByteArrayOutputStream();
    System.out.println("writeByteArrayOutputStream: " + (System.currentTimeMillis() - start));
  }
}
