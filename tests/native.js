/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

Native.create("gnu/testlet/vm/NativeTest.getInt.()I", function() {
  return 0xFFFFFFFF;
});

Native.create("gnu/testlet/vm/NativeTest.getLongReturnLong.(J)J", function(val, _) {
  return Long.fromNumber(40 + val.toNumber());
});

Native.create("gnu/testlet/vm/NativeTest.getLongReturnInt.(J)I", function(val, _) {
  return 40 + val.toNumber();
});

Native.create("gnu/testlet/vm/NativeTest.getIntReturnLong.(I)J", function(val) {
  return Long.fromNumber(40 + val);
});

Native.create("gnu/testlet/vm/NativeTest.throwException.()V", function() {
  throw new JavaException("java/lang/NullPointerException", "An exception");
});

Native.create("gnu/testlet/vm/NativeTest.throwExceptionAfterPause.()V", function() {
  return new Promise(function(resolve, reject) {
    setTimeout(reject.bind(null, new JavaException("java/lang/NullPointerException", "An exception")), 100);
  });
}, true);

Native.create("gnu/testlet/vm/NativeTest.returnAfterPause.()I", function() {
  return new Promise(function(resolve, reject) {
    setTimeout(resolve.bind(null, 42), 100);
  });
}, true);

Native.create("gnu/testlet/vm/NativeTest.nonStatic.(I)I", function(val) {
  return val + 40;
});

Native.create("gnu/testlet/vm/NativeTest.fromJavaString.(Ljava/lang/String;)I", function(str) {
  return util.fromJavaString(str).length;
});

Native.create("gnu/testlet/vm/NativeTest.decodeUtf8.([B)I", function(str) {
  return util.decodeUtf8(str).length;
});

Native.create("gnu/testlet/vm/NativeTest.newFunction.()Z", function() {
  try {
    var fn = new Function("return true;");
    return fn();
  } catch(ex) {
    console.error(ex);
    return false;
  }
});

Native.create("gnu/testlet/vm/NativeTest.dumbPipe.()Z", function() {
  return new Promise(function(resolve, reject) {
    // Ensure we can echo a large amount of data.
    var array = [];
    for (var i = 0; i < 128 * 1024; i++) {
      array[i] = i;
    }
    DumbPipe.open("echo", array, function(message) {
      resolve(JSON.stringify(array) === JSON.stringify(message));
    });
  });
}, true);

Native.create("com/nokia/mid/ui/TestVirtualKeyboard.hideKeyboard.()V", function() {
  DumbPipe.open("hideKeyboard", null, function(message) {});
});

Native.create("com/nokia/mid/ui/TestVirtualKeyboard.showKeyboard.()V", function() {
  DumbPipe.open("showKeyboard", null, function(message) {});
});

Native.create("javax/microedition/lcdui/TestAlert.isTextEditorReallyFocused.()Z", function() {
  return currentlyFocusedTextEditor.textEditor.focused;
});

Native.create("gnu/testlet/TestHarness.getNumDifferingPixels.(Ljava/lang/String;)I", function(pathStr) {
  var path = util.fromJavaString(pathStr);
  return new Promise(function(resolve, reject) {
    var gotCanvas = document.getElementById("canvas");
    var gotPixels = new Uint32Array(gotCanvas.getContext("2d").getImageData(0, 0, gotCanvas.width, gotCanvas.height).data.buffer);

    var img = new Image();
    img.src = "tests/" + path;

    img.onerror = function() {
      console.error("Error while loading image: " + img.src);
      reject(new JavaException("java/lang/Exception", "Error while loading image: " + img.src));
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
        reject(new JavaException("java/lang/Exception", message));
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
  });
}, true);

Native.create("com/nokia/mid/impl/jms/core/TestLauncher.checkImageModalDialog.()Z", function() {
  return document.getElementById("image-launcher") != null;
});

Native.create("org/mozilla/io/TestNokiaPhoneStatusServer.sendFakeOnlineEvent.()V", function() {
  window.dispatchEvent(new CustomEvent("online"));
});

Native.create("org/mozilla/io/TestNokiaPhoneStatusServer.sendFakeOfflineEvent.()V", function() {
  window.dispatchEvent(new CustomEvent("offline"));
});
