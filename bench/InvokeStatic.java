package benchmark;

class InvokeStatic {
    public static int staticus() {
        return 5;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100000; i++) {
            staticus();
        }
    }
}
