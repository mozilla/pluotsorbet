package javax.microedition.io.file;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import java.io.IOException;
import java.util.Enumeration;
import javax.microedition.io.Connector;

public class TestFileSystemRegistry implements Testlet {

    public void test(TestHarness th) {
        try {
            testListRoots(th);
        } catch (Throwable e) {
            th.todo(false, "Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    public void testListRoots(TestHarness th) throws IOException {
        Enumeration rootEnum = FileSystemRegistry.listRoots();
        th.check(rootEnum != null);
        while (rootEnum.hasMoreElements()) {
            String root = (String)rootEnum.nextElement();
            FileConnection fc = (FileConnection)Connector.open("file:///" + root);
            th.check(fc != null);
        }
    }
}
