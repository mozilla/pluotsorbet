package benchmark;

class InvokeStatic {
    public static int staticus(int x) {
        return x + 5;
    }

    public static void main(String[] args) {
        int x = 0;
        for (int i = 0; i < 20000000; i++) {
            x += staticus(x);
            x += staticus(x);
            x += staticus(x);
            x += staticus(x);
            x += staticus(x);
            x = i;
        }
    }
}
