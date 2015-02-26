package benchmark;

interface IFace {
    public int method();
}

class IFaceImpl implements IFace {
    public int method() {
        return 6;
    }
}

class InvokeInterface {
    public static void main(String[] args) {
        IFace foo = new IFaceImpl();
        for (int i = 0; i < 100000; i++) {
            foo.method();
        }
    }
}
