/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var AccelerometerSensor = {};

AccelerometerSensor.IS_MOBILE = navigator.userAgent.search("Mobile") !== -1;

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

// Simulate acceleration data by moving mouse.
AccelerometerSensor.simulator = {
    _intervalId: -1,

    start: function() {
        var currentMouseX = -1;
        var currentMouseY = -1;
        var c = document.getElementById("canvas");
        c.onmousemove = function(ev) {
            currentMouseX =ev.layerX;
            currentMouseY =ev.layerY;
        };

        var time = 0;
        var mouseX = -1;
        var mouseY = -1;
        var velocityX = -1;
        var velocityY = -1;
        this._intervalId = setInterval(function() {
            var previousTime = time;
            var previousMouseX = mouseX;
            var previousMouseY = mouseY;
            var previousVelocityX = velocityX;
            var previousVelocityY = velocityY;

            time = Date.now();
            var dt = (time - previousTime) / 1000;
            mouseX = currentMouseX * c.width / c.offsetWidth / 5000;
            mouseY = currentMouseY * c.height / c.offsetHeight / 5000;
            velocityX = (mouseX - previousMouseX) / dt;
            velocityY = (mouseY - previousMouseY) / dt;
            var ax = (velocityX - previousVelocityX) / dt;
            var ay = ax;
            var az = (velocityY - previousVelocityY) / dt;

            AccelerometerSensor.handleEvent({
                accelerationIncludingGravity: { x: ax, y: ay, z: az }
            });
        }, 50);
    },

    stop: function() {
        document.getElementById("canvas").onmousemove = null;
        clearInterval(this._interalId);
    }
};

AccelerometerSensor.open = function() {
    window.addEventListener('devicemotion', this);
    if (!this.IS_MOBILE) {
        this.simulator.start();
    }
};

AccelerometerSensor.close = function() {
    window.removeEventListener('devicemotion', this);
    if (!this.IS_MOBILE) {
        this.simulator.stop();
    }
};

AccelerometerSensor.readBuffer = (function() {
    var offset = 0;

    var write_int32 = function(out, value) {
      var a = new Int8Array(4);
      new Int32Array(a.buffer)[0] = value;
      Array.prototype.reverse.apply(a);
      out.set(a, offset);
      offset += 4;
    };

    var write_boolean = function(out, value) {
      out[offset++] = value;
    };

    var write_float32 = function(out, value) {
      var a = new Int8Array(4);
      new Float32Array(a.buffer)[0] = value;
      Array.prototype.reverse.apply(a);
      out.set(a, offset);
      offset += 4;
    };

    var write_double64 = function(out, value) {
      var a = new Int8Array(8);
      new Float64Array(a.buffer)[0] = value;
      Array.prototype.reverse.apply(a);
      out.set(a, offset);
      offset += 8;
    };

    var DATA_LENGTH = 1;
    var result = new Int8Array(5 + DATA_LENGTH * 13);

    return function(channelNumber) {
        offset = 0;
        result[offset++] = this.channels[channelNumber].dataType;
        // Set data length
        write_int32(result, DATA_LENGTH);
        // Set validity
        write_boolean(result, true);
        // Set uncertainty
        write_float32(result, 0);
        // Set sensor data.
        write_double64(result, this.acceleration[channelNumber]);
        return result;
    };
})();

AccelerometerSensor.acceleration = [0, 0, 0];

// Event handler to handle devicemotion event.
AccelerometerSensor.handleEvent = function(evt) {
    var a = evt.accelerationIncludingGravity;
    this.acceleration[0] = a.x;
    this.acceleration[1] = a.y;
    this.acceleration[2] = a.z;
};

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
    AccelerometerSensor.open();
    return true;
});

Native.create("com/sun/javame/sensor/NativeSensor.doFinishSensor.(I)Z", function(number) {
    if (number !== 0) {
        return false;
    }
    AccelerometerSensor.close();
    return true;
});

Native.create("com/sun/javame/sensor/NativeChannel.doMeasureData.(II)[B", function(sensorNumber, channelNumber) {
    if (sensorNumber !== 0 || channelNumber < 0 || channelNumber >= 3) {
        if (sensorNumber !== 0) {
            console.error("Invalid sensor number: " + sensorsNumber);
        } else {
            console.error("Invalid channel number: " + channelNumber);
        }
        return util.newPrimitiveArray("B", 0);
    }

    return AccelerometerSensor.readBuffer(channelNumber);
});
