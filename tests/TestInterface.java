public class TestInterface implements Runnable {
    public void run() {
	System.out.println("yes!");
    }

    public static void main(String[] args) {
	Runnable r = new TestInterface();
	r.run();
    }
}