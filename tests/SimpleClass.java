interface TestInterface {
    public void asd();
}

class TestClass implements TestInterface {
    public void asd() {
        int c = 5;
        c++;
    }
}

class SimpleClass {
    public static int asd(int a, int b, int c) {
        int k = 10;
        int x = a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        x = x + a + (1 + k);
        return x;
    }

    public static void main(String[] args) {
        System.out.println("I'm hungry");

        TestInterface a = new TestClass();
        SimpleClass b = new SimpleClass();

        for (int i = 0; i < 999999; i++) {
            a.asd();
        }
    }
}
