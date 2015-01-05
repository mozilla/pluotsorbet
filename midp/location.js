/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

// We only support one single location provider via the geolocation API.
Location.PROVIDER_NAME = "browser";

Location.Providers = {};

var LocationProvider = function() {
    this.watchId = -1;
    this.state = LocationProvider.OUT_OF_SERVICE;
    this.position = null;
};

LocationProvider.AVAILABLE = 1;
LocationProvider.TEMPORARILY_UNAVAILABLE = 1;
LocationProvider.OUT_OF_SERVICE = 1;

LocationProvider.prototype.start = function() {
    return new Promise(function(resolve, reject) {
        this.state = LocationProvider.TEMPORARILY_UNAVAILABLE;
        this.watchId = navigator.geolocation.watchPosition(function(pos) {
            this.state = LocationProvider.AVAILABLE;
            this.position = pos;
            console.info("latitude: " + pos.coords.latitude + " longitude: " + pos.coords.longitude);
            resolve(this.watchId);
        }.bind(this), function(err) {
            console.error(err);
            this.position = null;
            this.stop();
            reject();
        }.bind(this), {
            enableHighAccuracy: true,
            timeout: 5000,
            maximumAge: 0
        });
        return this.watchId;
    }.bind(this));
};

LocationProvider.prototype.stop = function() {
    if (this.watchId === -1) {
        return;
    }
    navigator.geolocation.clearWatch(this.watchId);
    this.watchId = -1;
    this.state = LocationProvider.OUT_OF_SERVICE;
};

Native.create("com/sun/j2me/location/PlatformLocationProvider.getListOfLocationProviders.()Ljava/lang/String;", function() {
    // If there are more than one providers, separate them by comma.
    return Location.PROVIDER_NAME;
});

Native.create("com/sun/j2me/location/CriteriaImpl.initNativeClass.()V", function() {
    console.warn("com/sun/j2me/location/CriteriaImpl.initNativeClass.()V not implemented");
});

Native.create("com/sun/j2me/location/PlatformLocationProvider.getBestProviderByCriteriaImpl.(Lcom/sun/j2me/location/CriteriaImpl;)Z", function(criteria) {
    criteria.class.getField("I.providerName.Ljava/lang/String;")
                  .set(criteria, util.newString(Location.PROVIDER_NAME));
    return true;
});

Native.create("com/sun/j2me/location/LocationProviderInfo.initNativeClass.()V", function() {
    console.warn("com/sun/j2me/location/LocationProviderInfo.initNativeClass.()V not implemented");
});

Native.create("com/sun/j2me/location/LocationInfo.initNativeClass.()V", function() {
    console.warn("com/sun/j2me/location/LocationInfo.initNativeClass.()V not implemented");
});

Native.create("com/sun/j2me/location/PlatformLocationProvider.open.(Ljava/lang/String;)I", function(name) {
    return new Promise(function(resolve, reject) {
        var provider = new LocationProvider();
        provider.start().then(function(watchId){
            var id = watchId + 1;
            Location.Providers[id] = provider;
            resolve(id);
        }, function() {
            resolve(0);
        });
    });
}, true);

Native.create("com/sun/j2me/location/PlatformLocationProvider.resetImpl.(I)V", function(providerId) {
    var provider = Location.Providers[providerId];
    provider.stop();
    Location.Providers[providerId] = null;
});

Native.create("com/sun/j2me/location/PlatformLocationProvider.getCriteria.(Ljava/lang/String;Lcom/sun/j2me/location/LocationProviderInfo;)Z", function(name, criteria) {
    criteria.class.getField("I.canReportAltitude.Z")
                  .set(criteria, true);
    criteria.class.getField("I.canReportSpeedCource.Z")
                  .set(criteria, true);
    return true;
});

Native.create("com/sun/j2me/location/PlatformLocationProvider.setUpdateIntervalImpl.(II)V", function(providerId, interval) {
    console.warn("com/sun/j2me/location/PlatformLocationProvider.setUpdateIntervalImpl.(II)V not implemented");
});

Native.create("com/sun/j2me/location/PlatformLocationProvider.getLastLocationImpl.(ILcom/sun/j2me/location/LocationInfo;)Z", function(providerId, locationInfo) {
    var provider = Location.Providers[providerId];
    var pos = provider.position;
    if (!pos) {
        return true;
        return false;
    }
    locationInfo.class.getField("I.isValid.Z")
                      .set(locationInfo, true);
    locationInfo.class.getField("I.timestamp.J")
                      .set(locationInfo, Long.fromNumber(pos.timestamp));

    var c = pos.coords;
    locationInfo.class.getField("I.latitude.D")
                      .set(locationInfo, c.latitude);
    locationInfo.class.getField("I.longitude.D")
                      .set(locationInfo, c.longitude);
    locationInfo.class.getField("I.altitude.F")
                      .set(locationInfo, c.altitude !== null ? c.altitude : NaN);
    locationInfo.class.getField("I.horizontalAccuracy.F")
                      .set(locationInfo, c.accuracy !== null ? c.accuracy : NaN);
    locationInfo.class.getField("I.verticalAccuracy.F")
                      .set(locationInfo, c.altitudeAccuracy !== null ? c.altitudeAccuracy : NaN);
    locationInfo.class.getField("I.speed.F")
                      .set(locationInfo, c.speed !== null ? c.speed : NaN);
    locationInfo.class.getField("I.course.F")
                      .set(locationInfo, c.heading !== null ? c.heading : NaN);

    locationInfo.class.getField("I.method.I")
                      .set(locationInfo, 0);
    return true;
});

Native.create("com/sun/j2me/location/PlatformLocationProvider.getStateImpl.(I)I", function(providerId) {
    var provider = Location.Providers[providerId];
    return provider.state;
});

Native.create("com/sun/j2me/location/PlatformLocationProvider.waitForNewLocation.(IJ)Z", function(providerId, timeout) {
    console.warn("com/sun/j2me/location/PlatformLocationProvider.waitForNewLocation.(IJ)Z not implemented");
    return true;
});

Native.create("com/sun/j2me/location/PlatformLocationProvider.receiveNewLocationImpl.(IJ)Z", function(providerId, timestamp) {
    console.warn("com/sun/j2me/location/PlatformLocationProvider.receiveNewLocationImpl.(IJ)Z not implemented");
    return true;
});
