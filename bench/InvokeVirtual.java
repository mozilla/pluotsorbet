package benchmark;

class BaseClass {
    public int method() {
        return 5;
    }
}

class NextClass extends BaseClass {
    public int method(int x) {
        return x + 1;
    }
}

class InvokeVirtual {
    public static void main(String[] args) {
        NextClass foo = new NextClass();
        int x = 0;
        for (int i = 0; i < 20000000; i++) {
            x += foo.method(x);
            x += foo.method(x);
            x += foo.method(x);
            x += foo.method(x);
            x += foo.method(x);
            x = i;
        }
    }
}
