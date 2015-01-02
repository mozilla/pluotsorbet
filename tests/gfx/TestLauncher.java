package gfx;

import java.io.*;
import javax.microedition.io.file.*;
import javax.microedition.io.*;
import javax.microedition.midlet.*;
import com.nokia.mid.impl.jms.core.*;

public class TestLauncher extends MIDlet {
    byte[] read(InputStream is) throws IOException {
        int l = is.available();
        byte[] buffer = new byte[l+1];
        int length = 0;

        while ((l = is.read(buffer, length, buffer.length - length)) != -1) {
            length += l;
            if (length == buffer.length) {
                byte[] b = new byte[buffer.length + 4096];
                System.arraycopy(buffer, 0, b, 0, length);
                buffer = b;
            }
        }

        return buffer;
    }

    public void startApp() {
        try {
            FileConnection file = (FileConnection)Connector.open("file:////test.png", Connector.READ_WRITE);
            if (!file.exists()) {
                file.create();
            }
            OutputStream os = file.openDataOutputStream();
            InputStream is = getClass().getResourceAsStream("/gfx/images/FirefoxLogo.png");
            os.write(read(is));
            os.close();

            Launcher.handleContent("test.png");

            file.delete();
            file.close();

            System.out.println("PAINTED");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("FAIL");
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
