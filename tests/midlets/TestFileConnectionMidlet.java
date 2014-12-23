/* vim: set filetype=java shiftwidth=4 tabstop=4 autoindent cindent expandtab : */

package midlets;

import java.io.IOException;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.*;

public class TestFileConnectionMidlet extends MIDlet {
    public void startApp() {
        final String dirPath = System.getProperty("fileconn.dir.private");
        final TextBox textBox = new TextBox("Test File Connection Midlet", "", 1000, TextField.ANY);
        Display.getDisplay(this).setCurrent(textBox);

        try {
            // First ensure the file exists.
            FileConnection file = (FileConnection)Connector.open(dirPath + "tmp.txt");
            if (!file.exists()) {
                file.create();
            }
            file.close();

            // Then open the file and write some data to it while displaying
            // the modification time at various points in the process.
            file = (FileConnection)Connector.open(dirPath + "tmp.txt");
            textBox.insert("On open file connection: " + file.lastModified() + "\n", textBox.size());

            try { Thread.sleep(1); } catch (Exception e) {}
            OutputStream out = file.openOutputStream();
            textBox.insert("On open output stream: " + file.lastModified() + "\n", textBox.size());

            try { Thread.sleep(1); } catch (Exception e) {}
            out.write(new byte[]{ 4, 3, 2, 1 });
            textBox.insert("On write to output stream: " + file.lastModified() + "\n", textBox.size());

            try { Thread.sleep(1); } catch (Exception e) {}
            out.close();
            textBox.insert("On close output stream: " + file.lastModified() + "\n", textBox.size());

            // Finally clean up.
            file.delete();
            file.close();
        } catch (IOException ex) {
            System.out.println("Unexpected exception: " + ex);
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
};
