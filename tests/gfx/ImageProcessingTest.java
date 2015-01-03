package gfx;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import com.nokia.mid.s40.io.LocalMessageProtocolMessage;
import com.nokia.mid.s40.io.LocalMessageProtocolConnection;
import com.nokia.mid.s40.codec.DataEncoder;
import com.nokia.mid.s40.codec.DataDecoder;
import gnu.testlet.TestUtils;

public class ImageProcessingTest extends MIDlet {
    private Display display;
    private Image image;

    class TestCanvas extends Canvas {
        protected void paint(Graphics g) {
            if (image != null) {
                g.drawImage(image, 0, 0, Graphics.TOP | Graphics.LEFT);
            }
            System.out.println("PAINTED");
        }
    }

    public ImageProcessingTest() throws IOException {
        LocalMessageProtocolConnection client = (LocalMessageProtocolConnection)Connector.open("localmsg://nokia.image-processing");

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
        byte[] clientData = msg.getData();

        FileConnection originalImage = (FileConnection)Connector.open("file:////test.jpg", Connector.READ_WRITE);
        if (!originalImage.exists()) {
            originalImage.create();
        }
        OutputStream os = originalImage.openDataOutputStream();
        InputStream is = getClass().getResourceAsStream("/org/mozilla/io/test.jpg");
        os.write(TestUtils.read(is));
        os.close();

        dataEncoder = new DataEncoder("Conv-BEB");
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
        sendData = dataEncoder.getData();
        client.send(sendData, 0, sendData.length);

        msg = client.newMessage(null);
        client.receive(msg);
        clientData = msg.getData();

        DataDecoder dataDecoder = new DataDecoder("Conv-BEB", clientData, 0, clientData.length);
        dataDecoder.getStart(14);
        dataDecoder.getString(13);
        dataDecoder.getInteger(2);
        dataDecoder.getString(10);
        String path = "file:////" + dataDecoder.getString(11);

        if (!originalImage.exists()) {
            System.out.println("FAIL - Original image has been deleted");
        }
        originalImage.delete();
        originalImage.close();

        client.close();

        FileConnection file = (FileConnection)Connector.open(path);
        if (!file.exists()) {
            System.out.println("FAIL - File doesn't exist");
        }
        is = file.openDataInputStream();

        display = Display.getDisplay(this);

        byte[] imageData = TestUtils.read(is);
        image = Image.createImage(imageData, 0, imageData.length);

        is.close();
        file.delete();
        file.close();
    }

    public void startApp() {
        TestCanvas test = new TestCanvas();
        test.setFullScreenMode(true);
        display.setCurrent(test);
    }

    public void pauseApp() {
        System.out.println("App paused");
    }

    public void destroyApp(boolean unconditional) {
        System.out.println("Goodbye, world");
    }
}

