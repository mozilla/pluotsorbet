package javax.microedition.location;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import gnu.testlet.TestUtils;
import com.nokia.mid.location.LocationUtil;

public class TestLocation implements Testlet, LocationListener {
    public int getExpectedPass() { return 8; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    TestHarness th;

    public void locationUpdated(LocationProvider provider, final Location location) {
        try {
            QualifiedCoordinates c = location.getQualifiedCoordinates();
            th.check(c.getLatitude(), 45);
            th.check(c.getLongitude(), -122);
            th.check(c.getAltitude(), 500);
            th.check(c.getHorizontalAccuracy(), 200);
            th.check(c.getVerticalAccuracy(), 10);
            th.check(location.getSpeed(), 90);
            th.check(location.getCourse(), 2);
        } catch (Exception e) {
            e.printStackTrace();
            th.fail("Unexpected exception: " + e);
        }
        // TODO There is an unknown bug that causes the following line blocking
        // the VM.
        // provider.setLocationListener(null, -1, -1, -1);
        synchronized(this) {
            this.notify();
        }
    }

    public void providerStateChanged(LocationProvider provider, final int newState) {
    }

    public void test(TestHarness th) {
        this.th = th;
        try {
            int[] methods = {(Location.MTA_ASSISTED | Location.MTE_SATELLITE | Location.MTY_TERMINALBASED)};
            LocationProvider provider = LocationUtil.getLocationProvider(methods, null);
            provider.setLocationListener(this, -1, -1, -1);
            synchronized(this) {
                this.wait();
            }
            th.pass();
        } catch (Exception e) {
            e.printStackTrace();
            th.fail("Unexpected exception: " + e);
        }
    }
}

