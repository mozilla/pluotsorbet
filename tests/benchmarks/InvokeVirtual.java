package benchmark;

class BaseClass {
    public int method(int a, int b) {
        return a + b;
    }
}

class NextClass extends BaseClass {
    public int method(int a, int b) {
        return a * b;
    }
}

class InvokeVirtual {
    public static void main(String[] args) {
        NextClass foo = new NextClass();
        for (int i = 0; i < 100000; i++) {
            foo.method(i, i + 40);
        }
    }
}
