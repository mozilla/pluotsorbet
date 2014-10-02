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
    public void asd() {
        int c = 5;
        c++;
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
