package tests.recordstore;

import javax.microedition.midlet.*;
import javax.microedition.rms.*;

public class ReaderMIDlet extends MIDlet {
    native void readerOpened();
    native void waitWriterWrote();

    public void go() throws RecordStoreException {
      int successes = 0;

      RecordStore store = RecordStore.openRecordStore("test", false);

      readerOpened();
      waitWriterWrote();

      if (store.getNumRecords() != 1) {
        System.out.println("FAIL - Wrong number of records: " + store.getNumRecords());
      } else {
        successes++;
      }

      RecordEnumeration recordEnumeration = store.enumerateRecords(null, null, true);

      if (!recordEnumeration.hasNextElement()) {
        System.out.println("FAIL - No next element");
      } else {
        successes++;
      }

      int recordID = recordEnumeration.nextRecordId();
      byte record[] = store.getRecord(recordID);

      if (record.length != 5) {
        System.out.println("FAIL - Record length isn't 5");
      } else {
        successes++;
      }

      for (int i = 0; i < record.length; i++) {
        if (record[i] != i) {
          System.out.println("FAIL - Wrong values in record");
        } else {
          successes++;
        }
      }

      System.out.println("SUCCESS " + successes + "/8");
      System.out.println("DONE");
    }

    public void startApp() {
        try {
          go();
        } catch (RecordStoreException e) {
          System.out.println("FAIL - " + e);
          e.printStackTrace();
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
