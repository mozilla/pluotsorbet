package com.sun.midp.publickeystore;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestInputOutputStorage implements Testlet {
    public void test(TestHarness th) {
        try {
            FileConnection file = (FileConnection)Connector.open("file:///prova");
            th.check(!file.exists());
            file.create();

            OutputStream out = file.openOutputStream();

            OutputStorage outStorage = new OutputStorage(out);
            outStorage.writeValue(1, "Marco");
            outStorage.writeValue(2, new byte[] { 1, 2 });
            outStorage.writeValue(3, 9999999999999);
            outStorage.writeValue(4, false);
            outStorage.writeValue(5, true);

            out.close();

            InputStream in = file.openInputStream();

            InputStorage inStorage = new InputStorage(in);
            th.check(inStorage.readValue(1), "Marco");
            th.check(inStorage.readValue(2), new byte[] { 1, 2 });
            th.check(inStorage.readValue(3), 9999999999999);
            th.check(inStorage.readValue(4), false);
            th.check(inStorage.readValue(5));

            in.close();

            file.delete();
            file.close();
        } catch (Exception e) {
            th.todo(false, "Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
