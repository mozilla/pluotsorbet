package benchmark;
import com.sun.cldchi.jvm.JVM;

class All {
    public static void main(String[] args) {
        com.sun.midp.crypto.ARC4_Bench.main(args);
        Arithmetic.main(args);
        BouncyCastleSHA256.main(args);
        BubbleSort.main(args);
        // ByteArrayInputOutputStreamBench.main(args); // Memory problem, too slow.
        // CallNativeBench.main(args);
        ClassLoading.main(args);
        DataInputOutputStreamBench.main(args);
        // DataInputOutputStreamFileBench.main(args);
        DefaultCaseConverterBench.main(args);
        Fields.main(args);
        // FileConnectionBench.main(args);
        FileStressBench.main(args);
        GestureInteractiveZoneBench.main(args);
        // ImageProcessingBench.main(args);
        Invoke.main(args);
        InvokeInterface.main(args);
        InvokeStatic.main(args);
        InvokeVirtual.main(args);
        // JITBenchmark.main(args);
        // JZlibBench.main(args); Memory
        MathBench.main(args);
        // Regex.main(args); Memory
        com.sun.cldc.io.ResourceInputStreamBench.main(args);
        com.sun.midp.crypto.SHA1_Bench.main(args);
        Sha256Bench.main(args);
        // SocketBench.main(args);
        // SocketStressBench.main(args);
        // SSLSocketBench.main(args);
        Stress.main(args);
        SystemOutBench.main(args);
        // TestFileSystemPerf.main(args);
        Time.main(args);
        // UTF8Bench.main(args); // Bug
        YieldBench.main(args);
    }
}
