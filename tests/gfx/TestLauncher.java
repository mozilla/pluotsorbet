package gfx;

import java.io.*;
import javax.microedition.io.file.*;
import javax.microedition.io.*;
import javax.microedition.midlet.*;
import com.nokia.mid.impl.jms.core.Launcher;
import gnu.testlet.TestUtils;

public class TestLauncher extends MIDlet {
    public void startApp() {
        try {
            FileConnection file = (FileConnection)Connector.open("file:////test.png", Connector.READ_WRITE);
            if (!file.exists()) {
                file.create();
            }
            OutputStream os = file.openDataOutputStream();
            InputStream is = getClass().getResourceAsStream("/gfx/images/red.png");
            os.write(TestUtils.read(is));
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
