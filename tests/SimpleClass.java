import java.lang.Integer;

interface TestInterface {
    public void asd();
}

class Point {
  int x;
  int y;
  static int z;
  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }
}

class SimpleClass {
    public static int run() {
      int s = 0;
      int c = 1024 * 1024;
      Point a = new Point(1, 2);
      for (int i = 0; i < c; i++) {
        a.x += a.y;
        s += a.x + a.y;
        s += Point.z;
      }
      return s;
    }

    public static void main(String[] args) {
//      checkFalse(((Object) new SimpleClass[10]) instanceof SimpleClass);
//      checkTrue(((Object) new SimpleClass[10]) instanceof SimpleClass[]);
//
//      checkTrue(((Object) new int[10]) instanceof Object);
//      checkFalse(((Object) new int[10]) instanceof Integer);
//      checkTrue(((Object)new int[10]) instanceof int []);
//      checkFalse(((Object) new int[10]) instanceof short[]);
//      checkFalse(((Object) new SimpleClass[10][10]) instanceof SimpleClass[]);
//      checkTrue(((Object) new SimpleClass[10][10]) instanceof SimpleClass[][]);
//      checkTrue(((Object) new SimpleClass[10]) instanceof Object[]);

      run();
    }
}
