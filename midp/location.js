/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Location = {};

// We only support one single location provider via the geolocation API.
Location.PROVIDER_NAME = "browser";

Location.Providers = {};

// Provider ID should be greater or equanl to 1.
Location.Providers.nextId = 1;

var LocationProvider = function() {
    this.state = LocationProvider.OUT_OF_SERVICE;
    this.position = {
        timestamp: 0,
        latitude: 0,
        longitude: 0,
        altitude: NaN,
        horizontalAccuracy: NaN,
        verticalAccuracy: NaN,
        speed: NaN,
        heading: NaN
    };

    // DumbPipe sender
    this.sender = null;

    // Called when location data is received.
    this.ondata = null;
};

LocationProvider.OUT_OF_SERVICE = 1;

// DumbPipe recipient
LocationProvider.prototype.recipient = function(message) {
    if (message.type === "data") {
        this.state = message.state;
        this.position = message.position;
        if (this.ondata) {
            this.ondata();
        }
    }
};

LocationProvider.prototype.start = function() {
    this.sender = DumbPipe.open("locationprovider", {},
                                this.recipient.bind(this));
};

LocationProvider.prototype.stop = function() {
    this.sender({ type: "close" });
    DumbPipe.close(this.sender);
};

LocationProvider.prototype.requestData = function() {
    return new Promise(function(resolve, reject) {
        this.sender({ type: "requestData" });
        this.ondata = resolve;
    }.bind(this));
};

Native["com/sun/j2me/location/PlatformLocationProvider.getListOfLocationProviders.()Ljava/lang/String;"] = function() {
    // If there are more than one providers, separate them by comma.
    return J2ME.newString(Location.PROVIDER_NAME);
};

addUnimplementedNative("com/sun/j2me/location/CriteriaImpl.initNativeClass.()V");

Native["com/sun/j2me/location/PlatformLocationProvider.getBestProviderByCriteriaImpl.(Lcom/sun/j2me/location/CriteriaImpl;)Z"] = function(criteria) {
    criteria.klass.classInfo.getField("I.providerName.Ljava/lang/String;")
                  .set(criteria, J2ME.newString(Location.PROVIDER_NAME));
    return 1;
};

addUnimplementedNative("com/sun/j2me/location/LocationProviderInfo.initNativeClass.()V");
addUnimplementedNative("com/sun/j2me/location/LocationInfo.initNativeClass.()V");

Native["com/sun/j2me/location/PlatformLocationProvider.open.(Ljava/lang/String;)I"] = function(name) {
    var provider = new LocationProvider();
    provider.start();
    var id = Location.Providers.nextId;
    Location.Providers.nextId = Location.Providers.nextId % 0xff + 1;
    Location.Providers[id] = provider;
    return id;
};

Native["com/sun/j2me/location/PlatformLocationProvider.resetImpl.(I)V"] = function(providerId) {
    var provider = Location.Providers[providerId];
    provider.stop();
    Location.Providers[providerId] = null;
};

Native["com/sun/j2me/location/PlatformLocationProvider.getCriteria.(Ljava/lang/String;Lcom/sun/j2me/location/LocationProviderInfo;)Z"] = function(name, criteria) {
    criteria.klass.classInfo.getField("I.canReportAltitude.Z")
                  .set(criteria, true);
    criteria.klass.classInfo.getField("I.canReportSpeedCource.Z")
                  .set(criteria, true);
    criteria.klass.classInfo.getField("I.averageResponseTime.I")
                  .set(criteria, 10000);
    return 1;
};

Native["com/sun/j2me/location/PlatformLocationProvider.setUpdateIntervalImpl.(II)V"] = function(providerId, interval) {
    console.warn("com/sun/j2me/location/PlatformLocationProvider.setUpdateIntervalImpl.(II)V not implemented");
};

Native["com/sun/j2me/location/PlatformLocationProvider.getLastLocationImpl.(ILcom/sun/j2me/location/LocationInfo;)Z"] = function(providerId, locationInfo) {
    var provider = Location.Providers[providerId];
    var pos = provider.position;
    locationInfo.klass.classInfo.getField("I.isValid.Z")
                      .set(locationInfo, true);
    locationInfo.klass.classInfo.getField("I.timestamp.J")
                      .set(locationInfo, Long.fromNumber(pos.timestamp));
    locationInfo.klass.classInfo.getField("I.latitude.D")
                      .set(locationInfo, pos.latitude);
    locationInfo.klass.classInfo.getField("I.longitude.D")
                      .set(locationInfo, pos.longitude);
    locationInfo.klass.classInfo.getField("I.altitude.F")
                      .set(locationInfo, pos.altitude);
    locationInfo.klass.classInfo.getField("I.horizontalAccuracy.F")
                      .set(locationInfo, pos.horizontalAccuracy);
    locationInfo.klass.classInfo.getField("I.verticalAccuracy.F")
                      .set(locationInfo, pos.verticalAccuracy);
    locationInfo.klass.classInfo.getField("I.speed.F")
                      .set(locationInfo, pos.speed);
    locationInfo.klass.classInfo.getField("I.course.F")
                      .set(locationInfo, pos.heading);

    locationInfo.klass.classInfo.getField("I.method.I")
                      .set(locationInfo, 0);
    return 1;
};

Native["com/sun/j2me/location/PlatformLocationProvider.getStateImpl.(I)I"] = function(providerId) {
    var provider = Location.Providers[providerId];
    return provider.state;
};

Native["com/sun/j2me/location/PlatformLocationProvider.waitForNewLocation.(IJ)Z"] = function(providerId, timeout) {
    asyncImpl("Z", new Promise(function(resolve, reject) {
        var provider = Location.Providers[providerId];
        provider.requestData().then(resolve.bind(null, 1));
        setTimeout(resolve.bind(null, 0), timeout);
    }));
};

Native["com/sun/j2me/location/PlatformLocationProvider.receiveNewLocationImpl.(IJ)Z"] = function(providerId, timestamp) {
    var provider = Location.Providers[providerId];
    var result = Math.abs(timestamp.toNumber() - provider.position.timestamp) < 10000;
    return result ? 1 : 0;
};
