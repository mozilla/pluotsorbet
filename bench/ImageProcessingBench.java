package benchmark;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import com.nokia.mid.s40.io.LocalMessageProtocolMessage;
import com.nokia.mid.s40.io.LocalMessageProtocolConnection;
import com.nokia.mid.s40.codec.DataEncoder;
import com.nokia.mid.s40.codec.DataDecoder;
import gnu.testlet.TestUtils;
import org.mozilla.MemorySampler;
import com.sun.cldchi.jvm.JVM;

public class ImageProcessingBench {
    LocalMessageProtocolConnection client;

    String scaleImage() throws IOException {
        DataEncoder dataEncoder = new DataEncoder("Conv-BEB");
        dataEncoder.putStart(14, "event");
        dataEncoder.put(13, "name", "Scale");
        dataEncoder.put(2, "trans_id", 42);
        dataEncoder.put(11, "filename", "test.jpg");
        dataEncoder.putStart(15, "limits");
        dataEncoder.put(5, "max_hres", 100);
        dataEncoder.put(5, "max_vres", 100);
        dataEncoder.putEnd(15, "limits");
        dataEncoder.put(10, "aspect", "FullImage");
        dataEncoder.put(2, "quality", 80);
        dataEncoder.putEnd(14, "event");
        byte[] sendData = dataEncoder.getData();
        client.send(sendData, 0, sendData.length);

        LocalMessageProtocolMessage msg = client.newMessage(null);
        client.receive(msg);
        byte[] clientData = msg.getData();

        DataDecoder dataDecoder = new DataDecoder("Conv-BEB", clientData, 0, clientData.length);
        dataDecoder.getStart(14);
        dataDecoder.getString(13);
        dataDecoder.getInteger(2);
        dataDecoder.getString(10);
        return "file:////" + dataDecoder.getString(11);
    }

    void runBenchmark() {
      try {
          long start, time = 0;

          client = (LocalMessageProtocolConnection)Connector.open("localmsg://nokia.image-processing");

          DataEncoder dataEncoder = new DataEncoder("Conv-BEB");
          dataEncoder.putStart(14, "event");
          dataEncoder.put(13, "name", "Common");
          dataEncoder.putStart(14, "message");
          dataEncoder.put(13, "name", "ProtocolVersion");
          dataEncoder.put(10, "version", "1.[0-10]");
          dataEncoder.putEnd(14, "message");
          dataEncoder.putEnd(14, "event");
          byte[] sendData = dataEncoder.getData();
          client.send(sendData, 0, sendData.length);

          LocalMessageProtocolMessage msg = client.newMessage(null);
          client.receive(msg);

          FileConnection originalImage = (FileConnection)Connector.open("file:////test.jpg", Connector.READ_WRITE);
          if (!originalImage.exists()) {
              originalImage.create();
          }
          OutputStream os = originalImage.openDataOutputStream();
          InputStream is = getClass().getResourceAsStream("/org/mozilla/io/test.jpg");
          os.write(TestUtils.read(is));
          os.close();

          MemorySampler.sampleMemory("Memory before nokia.image-processing benchmark");
          for (int i = 0; i < 1000; i++) {
            start = JVM.monotonicTimeMillis();
            String path = scaleImage();
            time += JVM.monotonicTimeMillis() - start;

            FileConnection file = (FileConnection)Connector.open(path);
            file.delete();
            file.close();
          }
          System.out.println("scaleImage: " + time);
          MemorySampler.sampleMemory("Memory after nokia.image-processing benchmark");

          originalImage.delete();
          originalImage.close();

          client.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public static void main(String args[]) {
      ImageProcessingBench bench = new ImageProcessingBench();
      bench.runBenchmark();
    }
}
