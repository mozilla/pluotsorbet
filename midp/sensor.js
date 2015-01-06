/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var AccelerometerSensor = {};

AccelerometerSensor.model = {
    description: "Acceleration sensor measures acceleration in SI units for x, y and z - axis.",
    model: "FirefoxOS",
    quantity: "acceleration",
    contextType: "user",
    connectionType: 1, // ChannelType.TYPE_DOUBLE = 1
    maxBufferSize: 256,
    availabilityPush: false,
    conditionPush: false,
    channelCount: 3,
    properties: [
        "vendor", "FirefoxOS",
        "version", "1.0",
        "maxSamplingRate", "20.0",
        "location", "NoLoc",
        "security", "private"
    ]
};

var doubleToLongBits = (function() {
    var da = new Float64Array(1);
    var ia = new Int32Array(da.buffer);
    return function(val) {
        da[0] = val;
        return Long.fromBits(ia[0], ia[1]);
    }
})();

AccelerometerSensor.channels = [ {
        scale: 0,
        name: "axis_x",
        unit: "m/s^2",
        dataType: 1, // 1 == Double type
        accuracy: 1,
        mrangeArray: [
            doubleToLongBits(-19.6), // smallest value
            doubleToLongBits(19.6),  // largest value
            doubleToLongBits(0.153)  // resolution
        ]
    }, {
        scale: 0,
        name: "axis_y",
        unit: "m/s^2",
        dataType: 1, // 1 == Double type
        accuracy: 1,
        mrangeArray: [
            doubleToLongBits(-19.6), // smallest value
            doubleToLongBits(19.6),  // largest value
            doubleToLongBits(0.153)  // resolution
        ]
    }, {
        scale: 0,
        name: "axis_z",
        unit: "m/s^2",
        dataType: 1, // 1 == Double type
        accuracy: 1,
        mrangeArray: [
            doubleToLongBits(-19.6), // smallest value
            doubleToLongBits(19.6),  // largest value
            doubleToLongBits(0.153)  // resolution
        ]
    }
];

Native.create("com/sun/javame/sensor/SensorRegistry.doGetNumberOfSensors.()I", function() {
    // Only support the acceleration sensor.
    return 1;
});

Native.create("com/sun/javame/sensor/Sensor.doGetSensorModel.(ILcom/sun/javame/sensor/SensorModel;)V", function(number, model) {
    if (number !== 0) {
        console.error("Invalid sensor number: " + number);
        return;
    }
    var m = AccelerometerSensor.model;
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

    var n = m.properties.length;
    var p = util.newArray("[Ljava/lang/String;", n);
    for (var i = 0; i < n; i++) {
        p[i] = util.newString(m.properties[i]);
    }
    model.class.getField("I.properties.[Ljava/lang/String;").set(model, p);
});

Native.create("com/sun/javame/sensor/ChannelImpl.doGetChannelModel.(IILcom/sun/javame/sensor/ChannelModel;)V", function(sensorsNumber, number, model) {
    if (sensorsNumber !== 0) {
        console.error("Invalid sensor number: " + sensorsNumber);
        return;
    }
    if (number < 0 || number >= AccelerometerSensor.channels.length) {
        console.error("Invalid channel number: " + number);
        return;
    }
    var c = AccelerometerSensor.channels[number];
    model.class.getField("I.scale.I")
               .set(model, c.scale);
    model.class.getField("I.name.Ljava/lang/String;")
               .set(model, util.newString(c.name));
    model.class.getField("I.unit.Ljava/lang/String;")
               .set(model, util.newString(c.unit));
    model.class.getField("I.dataType.I")
               .set(model, c.dataType);
    model.class.getField("I.accuracy.I")
               .set(model, c.accuracy);
    model.class.getField("I.mrangeCount.I")
               .set(model, c.mrangeArray.length);

    var n = c.mrangeArray.length;
    var array = util.newArray("[J", n);
    for (var i = 0; i < n; i++) {
        array[i] = c.mrangeArray[i];
    }
    model.class.getField("I.mrangeArray.[J").set(model, array);
});

Native.create("com/sun/javame/sensor/NativeSensor.doIsAvailable.(I)Z", function(number) {
    // Only support the acceleration sensor with number = 0.
    return number === 0;
});

Native.create("com/sun/javame/sensor/NativeSensor.doInitSensor.(I)Z", function(number) {
    if (number !== 0) {
        return false;
    }
    console.warn("NativeSensor.doInitSensor not implemented");
    return true;
});

Native.create("com/sun/javame/sensor/NativeSensor.doFinishSensor.(I)Z", function(number) {
    if (number !== 0) {
        return false;
    }
    console.warn("NativeSensor.doInitSensor not implemented");
    return true;
});
