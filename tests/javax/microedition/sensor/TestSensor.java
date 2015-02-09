package javax.microedition.sensor;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

import javax.microedition.io.Connector;

public class TestSensor implements Testlet, DataListener {
    static final int BUFFER_SIZE = 3;
    TestHarness th;

    public int getExpectedPass() { return 5; }

    public int getExpectedFail() { return 0; }

    public int getExpectedKnownFail() { return 0; }

    public void test(TestHarness th) {
        this.th = th;
        try {
            SensorInfo[] infos = SensorManager.findSensors("acceleration", null);
            th.check(infos[0].getChannelInfos()[0].getDataType() == ChannelInfo.TYPE_DOUBLE);
            SensorConnection conn = (SensorConnection)Connector.open(infos[0].getUrl());
            conn.setDataListener(this, BUFFER_SIZE);
            synchronized(this) {
                wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            th.fail("Unexpected exception: " + e);
        }
    }

    public void dataReceived(SensorConnection con, Data[] aData, boolean aMissed) {
        try {
            th.check(aData.length == 3);
            for (int i = 0; i < aData.length; i++) {
                th.check(aData[i].getDoubleValues() != null);
            }
            synchronized(this) {
                notify();
            }
        } catch (Exception e) {
            e.printStackTrace();
            th.fail("Unexpected exception: " + e);
        }
    }
}

