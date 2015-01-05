/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

// We only support one single location provider via the geolocation API.
Location.PROVIDER_NAME = "browser";

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
