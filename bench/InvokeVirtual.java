package benchmark;

class BaseClass {
    public int method() {
        return 5;
    }
}

class NextClass extends BaseClass {
    public int method() {
        return 6;
    }
}

class InvokeVirtual {
    public static void main(String[] args) {
        NextClass foo = new NextClass();
        for (int i = 0; i < 100000; i++) {
            foo.method();
        }
    }
}
