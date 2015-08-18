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
    availabilityPush: 0,
    conditionPush: 0,
    channelCount: 3,
    properties: [
        "vendor", "FirefoxOS",
        "version", "1.0",
        "maxSamplingRate", "20.0",
        "location", "NoLoc",
        "security", "private"
    ]
};

var createLongArrayFromDoubles = (function() {
    var da = new Float64Array(1);
    var ia = new Int32Array(da.buffer);
    return function(doubles) {
        var ret = [];
        for (var i = 0; i < doubles.length; i++) {
            var val = doubles[i];
            da[0] = val;
            ret.push(ia[0], ia[1]);
        }
        return ret;
    };
})();

AccelerometerSensor.channels = [ {
        scale: 0,
        name: "axis_x",
        unit: "m/s^2",
        dataType: 1, // 1 == Double type
        accuracy: 1,
        mrangeArray: createLongArrayFromDoubles([
            -19.6, // smallest value
            19.6,  // largest value
            0.153  // resolution
        ])
    }, {
        scale: 0,
        name: "axis_y",
        unit: "m/s^2",
        dataType: 1, // 1 == Double type
        accuracy: 1,
        mrangeArray: createLongArrayFromDoubles([
            -19.6, // smallest value
            19.6,  // largest value
            0.153  // resolution
        ])
    }, {
        scale: 0,
        name: "axis_z",
        unit: "m/s^2",
        dataType: 1, // 1 == Double type
        accuracy: 1,
        mrangeArray: createLongArrayFromDoubles([
            -19.6, // smallest value
            19.6,  // largest value
            0.153  // resolution
        ])
    }
];

// Simulate acceleration data by moving mouse.
AccelerometerSensor.simulator = {
    _intervalId: -1,

    start: function() {
        var currentMouseX = -1;
        var currentMouseY = -1;
        var c = MIDP.deviceContext.canvas;
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
        MIDP.deviceContext.canvas.onmousemove = null;
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

    return function(channelNumber) {
        var resultAddr = J2ME.newByteArray(5 + DATA_LENGTH * 13);
        var result = J2ME.getArrayFromAddr(resultAddr);

        offset = 0;
        result[offset++] = this.channels[channelNumber].dataType;
        // Set data length
        write_int32(result, DATA_LENGTH);
        // Set validity
        write_boolean(result, 1);
        // Set uncertainty
        write_float32(result, 0);
        // Set sensor data.
        write_double64(result, this.acceleration[channelNumber]);

        return resultAddr;
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

Native["com/sun/javame/sensor/SensorRegistry.doGetNumberOfSensors.()I"] = function(addr) {
    // Only support the acceleration sensor.
    return 1;
};

Native["com/sun/javame/sensor/Sensor.doGetSensorModel.(ILcom/sun/javame/sensor/SensorModel;)V"] =
function(addr, number, modelAddr) {
    if (number !== 0) {
        console.error("Invalid sensor number: " + number);
        return;
    }
    var model = getHandle(modelAddr);
    var m = AccelerometerSensor.model;
    model.description = J2ME.newString(m.description);
    model.model = J2ME.newString(m.model);
    model.quantity = J2ME.newString(m.quantity);
    model.contextType = J2ME.newString(m.contextType);
    model.connectionType = m.connectionType;
    model.maxBufferSize = m.maxBufferSize;
    model.availabilityPush = m.availabilityPush;
    model.conditionPush = m.conditionPush;
    model.channelCount = m.channelCount;
    model.errorCodes = J2ME.newIntArray(0);
    model.errorMsgs = J2ME.newStringArray(0);

    var n = m.properties.length;
    var pAddr = J2ME.newStringArray(n);
    model.properties = pAddr;
    var p = J2ME.getArrayFromAddr(pAddr);
    for (var i = 0; i < n; i++) {
        p[i] = J2ME.newString(m.properties[i]);
    }
};

Native["com/sun/javame/sensor/ChannelImpl.doGetChannelModel.(IILcom/sun/javame/sensor/ChannelModel;)V"] =
function(addr, sensorsNumber, number, modelAddr) {
    if (sensorsNumber !== 0) {
        console.error("Invalid sensor number: " + sensorsNumber);
        return;
    }
    if (number < 0 || number >= AccelerometerSensor.channels.length) {
        console.error("Invalid channel number: " + number);
        return;
    }
    var model = getHandle(modelAddr);
    var c = AccelerometerSensor.channels[number];
    model.scale = c.scale;
    model.name = J2ME.newString(c.name);
    model.unit = J2ME.newString(c.unit);
    model.dataType = c.dataType;
    model.accuracy = c.accuracy;
    var n = c.mrangeArray.length / 2;
    model.mrangeCount = n;

    var n = c.mrangeArray.length;
    var arrayAddr = J2ME.newArray(J2ME.PrimitiveClassInfo.J, n);
    var array = J2ME.getArrayFromAddr(arrayAddr);
    var i32array = new Int32Array(array.buffer, array.byteOffset, array.length * 2);
    for (var i = 0; i < n; i++) {
        i32array[i * 2] = c.mrangeArray[i].low_;
        i32array[i * 2 + 1] = c.mrangeArray[i].high_;
    }
    model.mrageArray = arrayAddr;
};

Native["com/sun/javame/sensor/NativeSensor.doIsAvailable.(I)Z"] = function(addr, number) {
    // Only support the acceleration sensor with number = 0.
    return number === 0 ? 1 : 0;
};

Native["com/sun/javame/sensor/NativeSensor.doInitSensor.(I)Z"] = function(addr, number) {
    if (number !== 0) {
        return 0;
    }
    AccelerometerSensor.open();
    return 1;
};

Native["com/sun/javame/sensor/NativeSensor.doFinishSensor.(I)Z"] = function(addr, number) {
    if (number !== 0) {
        return 0;
    }
    AccelerometerSensor.close();
    return 1;
};

Native["com/sun/javame/sensor/NativeChannel.doMeasureData.(II)[B"] = function(addr, sensorNumber, channelNumber) {
    if (sensorNumber !== 0 || channelNumber < 0 || channelNumber >= 3) {
        if (sensorNumber !== 0) {
            console.error("Invalid sensor number: " + sensorsNumber);
        } else {
            console.error("Invalid channel number: " + channelNumber);
        }
        return J2ME.newByteArray(0);
    }

    var resultHolder = J2ME.gcMallocUncollectable(4);

    asyncImpl("[B", new Promise(function(resolve, reject) {
        var resultAddr = AccelerometerSensor.readBuffer(channelNumber);
        i32[resultHolder >> 2] = resultAddr;
        setTimeout(resolve.bind(null, resultAddr), 50);
    }), function() {
        ASM._gcFree(resultHolder);
    });
};
