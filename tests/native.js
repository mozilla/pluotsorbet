/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

Native["gnu/testlet/vm/NativeTest.getInt.()I"] = function() {
  return ~~(0xFFFFFFFF);
};

Native["gnu/testlet/vm/NativeTest.getLongReturnLong.(J)J"] = function(val) {
  return Long.fromNumber(40 + val.toNumber());
};

Native["gnu/testlet/vm/NativeTest.getLongReturnInt.(J)I"] = function(val) {
  return ~~(40 + val.toNumber());
};

Native["gnu/testlet/vm/NativeTest.getIntReturnLong.(I)J"] = function(val) {
  return Long.fromNumber(40 + val);
};

Native["gnu/testlet/vm/NativeTest.throwException.()V"] = function() {
  throw $.newNullPointerException("An exception");
};

Native["gnu/testlet/vm/NativeTest.throwExceptionAfterPause.()V"] = function() {
  var ctx = $.ctx;
  asyncImpl("V", new Promise(function(resolve, reject) {
    ctx.setAsCurrentContext();
    setTimeout(reject.bind(null, $.newNullPointerException("An exception")), 100);
  }));
};

Native["gnu/testlet/vm/NativeTest.returnAfterPause.()I"] = function() {
  asyncImpl("I", new Promise(function(resolve, reject) {
    setTimeout(resolve.bind(null, 42), 100);
  }));
};

Native["gnu/testlet/vm/NativeTest.nonStatic.(I)I"] = function(val) {
  return val + 40;
};

Native["gnu/testlet/vm/NativeTest.fromJavaString.(Ljava/lang/String;)I"] = function(str) {
  return util.fromJavaString(str).length;
};

Native["gnu/testlet/vm/NativeTest.decodeUtf8.([B)I"] = function(str) {
  return util.decodeUtf8(str).length;
};

Native["gnu/testlet/vm/NativeTest.newFunction.()Z"] = function() {
  try {
    var fn = new Function("return true;");
    return fn() ? 1 : 0;
  } catch(ex) {
    console.error(ex);
    return 0;
  }
};

Native["gnu/testlet/vm/NativeTest.dumbPipe.()Z"] = function() {
  asyncImpl("Z", new Promise(function(resolve, reject) {
    // Ensure we can echo a large amount of data.
    var array = [];
    for (var i = 0; i < 128 * 1024; i++) {
      array[i] = i;
    }
    DumbPipe.open("echo", array, function(message) {
      resolve(JSON.stringify(array) === JSON.stringify(message) ? 1 : 0);
    });
  }));
};

Native["com/nokia/mid/ui/TestVirtualKeyboard.hideKeyboard.()V"] = function() {
  DumbPipe.open("hideKeyboard", null, function(message) {});
};

Native["com/nokia/mid/ui/TestVirtualKeyboard.showKeyboard.()V"] = function() {
  DumbPipe.open("showKeyboard", null, function(message) {});
};

Native["javax/microedition/lcdui/TestAlert.isTextEditorReallyFocused.()Z"] = function() {
  return currentlyFocusedTextEditor.textEditor.focused ? 1 : 0;
};

Native["gnu/testlet/TestHarness.getNumDifferingPixels.(Ljava/lang/String;)I"] = function(pathStr) {
  var path = util.fromJavaString(pathStr);
  asyncImpl("I", new Promise(function(resolve, reject) {
    var gotCanvas = document.getElementById("canvas");
    var gotPixels = new Uint32Array(gotCanvas.getContext("2d").getImageData(0, 0, gotCanvas.width, gotCanvas.height).data.buffer);

    var img = new Image();
    img.src = "tests/" + path;

    img.onerror = function() {
      console.error("Error while loading image: " + img.src);
      reject($.newException("Error while loading image: " + img.src));
    }
    img.onload = function() {
      var expectedCanvas = document.createElement('canvas');
      expectedCanvas.width = img.width;
      expectedCanvas.height = img.height;
      expectedCanvas.getContext("2d").drawImage(img, 0, 0);

      var expectedPixels = new Uint32Array(expectedCanvas.getContext("2d").getImageData(0, 0, img.width, img.height).data.buffer);

      if (expectedCanvas.width !== gotCanvas.width || expectedCanvas.height !== gotCanvas.height) {
        var message = "Width (got: " + gotCanvas.width + ", expected: " + expectedCanvas.width + "), " +
                      "height (got: " + gotCanvas.height + ", expected: " + expectedCanvas.width + ")";
        console.error(message);
        reject($.newException(message));
        return;
      }

      var different = 0;
      var i = 0;
      for (var x = 0; x < gotCanvas.width; x++) {
        for (var y = 0; y < gotCanvas.height; y++) {
          if (expectedPixels[i] !== gotPixels[i]) {
            different++;
          }

          i++;
        }
      }

      resolve(different);
    };
  }));
};

Native["com/nokia/mid/impl/jms/core/TestLauncher.checkImageModalDialog.()Z"] = function() {
  return document.getElementById("image-launcher") != null ? 1 : 0;
};

Native["org/mozilla/io/TestNokiaPhoneStatusServer.sendFakeOnlineEvent.()V"] = function() {
  window.dispatchEvent(new CustomEvent("online"));
};

Native["org/mozilla/io/TestNokiaPhoneStatusServer.sendFakeOfflineEvent.()V"] = function() {
  window.dispatchEvent(new CustomEvent("offline"));
};

Native["javax/microedition/media/TestAudioRecorder.convert3gpToAmr.([B)[B"] = function(data) {
  var converted = Media.convert3gpToAmr(new Uint8Array(data));
  var result = J2ME.newByteArray(converted.length);
  result.set(converted);
  return result;
};
