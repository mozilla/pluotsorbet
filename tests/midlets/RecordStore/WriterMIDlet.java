package tests.recordstore;

import javax.microedition.midlet.*;
import javax.microedition.io.PushRegistry;
import java.util.Date;
import javax.microedition.rms.*;

public class WriterMIDlet extends MIDlet {
    native void waitReaderOpened();
    native void writerWrote();

    public void go() throws RecordStoreException {
        byte[] data = new byte[5];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)i;
        }

        try {
            RecordStore.deleteRecordStore("test");
        } catch (RecordStoreNotFoundException e) {
        }

        RecordStore store = RecordStore.openRecordStore("test", true);

        waitReaderOpened();

        int recordId1 = store.addRecord(data, 0, data.length);

        writerWrote();
    }

    public void startApp() {
        try {
            PushRegistry.registerAlarm("tests.recordstore.ReaderMIDlet", new Date().getTime());
        } catch (Exception e) {
            System.out.println("Unexpected exception");
        }

        try {
            go();
        } catch (RecordStoreException e) {
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
