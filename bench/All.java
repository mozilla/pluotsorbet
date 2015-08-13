package benchmark;
import com.sun.cldchi.jvm.JVM;

class All {
    public static void main(String[] args) {

        long start = JVM.monotonicTimeMillis();

        com.sun.midp.crypto.ARC4_Bench.main(args);
        System.out.println(">> com.sun.midp.crypto.ARC4_Bench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        com.sun.midp.crypto.Cipher_Bench.main(args);
        System.out.println(">> com.sun.midp.crypto.Cipher_Bench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        Arithmetic.main(args);
        System.out.println(">> Arithmetic: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        BouncyCastleSHA256.main(args);
        System.out.println(">> BouncyCastleSHA256: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        BubbleSort.main(args);
        System.out.println(">> BubbleSort: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        ArrayCopyBench.main(args);
        System.out.println(">> ArrayCopyBench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        // ByteArrayInputOutputStreamBench.main(args); // Memory problem, too slow.
        // CallNativeBench.main(args);
        ClassLoading.main(args);
        System.out.println(">> ClassLoading: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        DataInputOutputStreamBench.main(args);
        System.out.println(">> DataInputOutputStreamBench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        // DataInputOutputStreamFileBench.main(args);
        DefaultCaseConverterBench.main(args);
        System.out.println(">> DefaultCaseConverterBench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        Fields.main(args);
        System.out.println(">> Fields: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        // FileConnectionBench.main(args);
        FileStressBench.main(args);
        System.out.println(">> FileStressBench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        GestureInteractiveZoneBench.main(args);
        System.out.println(">> GestureInteractiveZoneBench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        // ImageProcessingBench.main(args);
        Invoke.main(args);
        System.out.println(">> Invoke: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        InvokeInterface.main(args);
        System.out.println(">> InvokeInterface: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        InvokeStatic.main(args);
        System.out.println(">> InvokeStatic: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        InvokeVirtual.main(args);
        System.out.println(">> InvokeVirtual: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        // JITBenchmark.main(args);
        // JZlibBench.main(args); Memory
        MathBench.main(args);
        System.out.println(">> MathBench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        // Regex.main(args); Memory
        com.sun.cldc.io.ResourceInputStreamBench.main(args);
        System.out.println(">> com.sun.cldc.io.ResourceInputStreamBench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        com.sun.midp.crypto.SHA1_Bench.main(args);
        System.out.println(">> com.sun.midp.crypto.SHA1_Bench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        Sha256Bench.main(args);
        System.out.println(">> Sha256Bench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        // SocketBench.main(args);
        // SocketStressBench.main(args);
        // SSLSocketBench.main(args);
        Stress.main(args);
        System.out.println(">> Stress: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        // SystemOutBench.main(args);
        // TestFileSystemPerf.main(args);
        Time.main(args);
        System.out.println(">> Time: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        // UTF8Bench.main(args); // Bug
        YieldBench.main(args);
        System.out.println(">> YieldBench: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

    }
}
