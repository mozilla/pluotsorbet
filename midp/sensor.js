/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var SensorModel = {
    acceleration: {
        description: "Acceleration sensor measures acceleration in SI units for x, y and z - axis.",
        model: "FirefoxOS",
        quantity: "acceleration",
        contextType: "user",
        connectionType: 1, // ChannelType.TYPE_DOUBLE = 1
        maxBufferSize: 256,
        availabilityPush: false,
        conditionPush: false,
        channelCount: 3
    }
};

Native.create("com/sun/javame/sensor/SensorRegistry.doGetNumberOfSensors.()I", function() {
    // Only support the acceleration sensor.
    return 1;
});

Native.create("com/sun/javame/sensor/Sensor.doGetSensorModel.(ILcom/sun/javame/sensor/SensorModel;)V", function(number, model) {
    if (number != 0) {
        return;
    }
    var m = SensorModel.acceleration;
    model.class.getField("I.description.Ljava/lang/String;")
               .set(model, util.newString(m.description));
    model.class.getField("I.model.Ljava/lang/String;")
               .set(model, util.newString(m.model));
    model.class.getField("I.quantity.Ljava/lang/String;")
               .set(model, util.newString(m.quantity));
    model.class.getField("I.contextType.Ljava/lang/String;")
               .set(model, util.newString(m.contextType));
    model.class.getField("I.connectionType.I")
               .set(model, m.connectionType);
    model.class.getField("I.maxBufferSize.I")
               .set(model, m.maxBufferSize);
    model.class.getField("I.availabilityPush.Z")
               .set(model, m.availabilityPush);
    model.class.getField("I.conditionPush.Z")
               .set(model, m.conditionPush);
    model.class.getField("I.channelCount.I")
               .set(model, m.channelCount);
    model.class.getField("I.errorCodes.[I").set(model, util.newArray("[I", 0));
    model.class.getField("I.errorMsgs.[Ljava/lang/String;").set(model, util.newArray("[Ljava/lang/String;", 0));
    model.class.getField("I.properties.[Ljava/lang/String;").set(model, util.newArray("[Ljava/lang/String;", 0));
});
