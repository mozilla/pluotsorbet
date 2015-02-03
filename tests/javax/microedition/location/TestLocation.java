package javax.microedition.location;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import gnu.testlet.TestUtils;
import com.nokia.mid.location.LocationUtil;

public class TestLocation implements Testlet, LocationListener {
    TestHarness th;

    public void locationUpdated(LocationProvider provider, final Location location) {
        try {
            QualifiedCoordinates c = location.getQualifiedCoordinates();
            if (c != null) {
                System.out.println("new Location Latitude: " + c.getLatitude() +
                                   " Logitude: " + c.getLongitude());

            }
        } catch (Exception e) {
            e.printStackTrace();
            th.fail("Unexpected exception: " + e);
        }
        provider.setLocationListener(null, -1, -1, -1);
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

