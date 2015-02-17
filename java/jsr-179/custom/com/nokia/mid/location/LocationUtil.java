package com.nokia.mid.location;

import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;

public final class LocationUtil {
  public static LocationProvider getLocationProvider(int[] preferredMethods,
    java.lang.String parameters) throws java.lang.SecurityException,
                                        LocationException {
    return LocationProvider.getInstance(null);
  }
}
