package benchmark;
import com.sun.cldchi.jvm.JVM;

class All {
    public static void summary(String message) {
        System.out.println(message);
        Thread.yield();
    }
    public static void main(String[] args) {

        long bigBang = JVM.monotonicTimeMillis();
        long start = JVM.monotonicTimeMillis();

        com.sun.midp.crypto.ARC4_Bench.main(args);
        summary(">> com.sun.midp.crypto.ARC4_Bench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        com.sun.midp.crypto.Cipher_Bench.main(args);
        summary(">> com.sun.midp.crypto.Cipher_Bench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        Arithmetic.main(args);
        summary(">> Arithmetic: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        Compiler.main(args);
        summary(">> Compiler: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        BouncyCastleSHA256.main(args);
        summary(">> BouncyCastleSHA256: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        BubbleSort.main(args);
        summary(">> BubbleSort: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        // ByteArrayInputOutputStreamBench.main(args); // Memory problem, too slow.
        // CallNativeBench.main(args);
        ClassLoading.main(args);
        summary(">> ClassLoading: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        DataInputOutputStreamBench.main(args);
        summary(">> DataInputOutputStreamBench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        // DataInputOutputStreamFileBench.main(args);
        DefaultCaseConverterBench.main(args);
        summary(">> DefaultCaseConverterBench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        Fields.main(args);
        summary(">> Fields: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        // FileConnectionBench.main(args);
        FileStressBench.main(args);
        summary(">> FileStressBench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        GestureInteractiveZoneBench.main(args);
        summary(">> GestureInteractiveZoneBench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        // ImageProcessingBench.main(args);
        Invoke.main(args);
        summary(">> Invoke: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        InvokeInterface.main(args);
        summary(">> InvokeInterface: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        InvokeStatic.main(args);
        summary(">> InvokeStatic: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        InvokeVirtual.main(args);
        summary(">> InvokeVirtual: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        // JITBenchmark.main(args);
        // JZlibBench.main(args); Memory
        MathBench.main(args);
        summary(">> MathBench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        // Regex.main(args); Memory
        com.sun.cldc.io.ResourceInputStreamBench.main(args);
        summary(">> com.sun.cldc.io.ResourceInputStreamBench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        com.sun.midp.crypto.SHA1_Bench.main(args);
        summary(">> com.sun.midp.crypto.SHA1_Bench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        Sha256Bench.main(args);
        summary(">> Sha256Bench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        // SocketBench.main(args);
        // SocketStressBench.main(args);
        // SSLSocketBench.main(args);
        DoubleBench.main(args);
        summary(">> DoubleBench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        // SystemOutBench.main(args);
        // TestFileSystemPerf.main(args);
        Time.main(args);
        summary(">> Time: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        // UTF8Bench.main(args); // Bug
        YieldBench.main(args);
        summary(">> YieldBench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        summary("== All ==: " + (JVM.monotonicTimeMillis() - bigBang));
    }
}
