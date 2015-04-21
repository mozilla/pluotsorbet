class MainStaticInitializer {
    static {
        System.out.println("1) static init");
    }
    public static void main(String args[]) {
        System.out.println("2) main");
        System.out.println("DONE");
    }
}

