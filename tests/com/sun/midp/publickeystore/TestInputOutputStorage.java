package com.sun.midp.publickeystore;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import java.io.*;
import com.sun.midp.io.j2me.storage.RandomAccessStream;

public class TestInputOutputStorage implements Testlet {
    public void test(TestHarness th) {
        try {
            FileConnection file = (FileConnection)Connector.open("file:///prova");
            th.check(!file.exists());
            file.create();

            RandomAccessStream ras = new RandomAccessStream();
            ras.connect("prova", Connector.READ_WRITE);

            OutputStream out = ras.openOutputStream();

            OutputStorage outStorage = new OutputStorage(out);
            outStorage.writeValue((byte)1, "Marco");
            outStorage.writeValue((byte)2, new byte[] { 1, 2 });
            outStorage.writeValue((byte)3, 777777777);
            outStorage.writeValue((byte)4, false);
            outStorage.writeValue((byte)5, true);

            out.close();

            InputStream in = ras.openInputStream();

            InputStorage inStorage = new InputStorage(in);
            th.check(inStorage.readValue(new byte[] { 1 }), "Marco");
            th.check(inStorage.readValue(new byte[] { 2 }), new byte[] { 1, 2 });
            th.check(inStorage.readValue(new byte[] { 3 }), new Long(777777777));
            th.check(inStorage.readValue(new byte[] { 4 }), new Boolean(false));
            th.check(inStorage.readValue(new byte[] { 5 }), new Boolean(true));

            in.close();

            ras.disconnect();

            file.delete();
            file.close();
        } catch (Exception e) {
            th.todo(false, "Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
