package benchmark;

class InvokeStatic {
    public static int staticus() {
        return 5;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10000000; i++) {
            staticus();
        }
    }
}
