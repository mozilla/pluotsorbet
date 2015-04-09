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
    setTimeout(function() {
      ctx.setAsCurrentContext();
      reject($.newNullPointerException("An exception"))
    }, 100);
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
  return J2ME.fromJavaString(str).length;
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
  MIDP.isVKVisible = function() { return false; };
  MIDP.sendVirtualKeyboardEvent();
};

Native["com/nokia/mid/ui/TestVirtualKeyboard.showKeyboard.()V"] = function() {
  MIDP.isVKVisible = function() { return true; };
  MIDP.sendVirtualKeyboardEvent();
};

Native["javax/microedition/lcdui/TestAlert.isTextEditorReallyFocused.()Z"] = function() {
  return (currentlyFocusedTextEditor && currentlyFocusedTextEditor.focused) ? 1 : 0;
};

Native["javax/microedition/lcdui/TestTextEditorFocus.isTextEditorReallyFocused.(Lcom/nokia/mid/ui/TextEditor;)Z"] = function(textEditor) {
  return (currentlyFocusedTextEditor == textEditor.textEditor && currentlyFocusedTextEditor.focused) ? 1 : 0;
};

Native["gnu/testlet/TestHarness.getNumDifferingPixels.(Ljava/lang/String;)I"] = function(pathStr) {
  var path = J2ME.fromJavaString(pathStr);
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

Native["com/sun/midp/i18n/TestResourceConstants.setLanguage.(Ljava/lang/String;)V"] = function(language) {
  MIDP.localizedStrings = null;
  config.language = J2ME.fromJavaString(language);
}

// Many tests create FileConnection objects to files with the "/" root,
// so add it to the list of valid roots.
MIDP.fsRoots.push("/");

Native["org/mozilla/MemorySampler.sampleMemory.(Ljava/lang/String;)V"] = function(label) {
  if (typeof Benchmark !== "undefined") {
    asyncImpl("V", Benchmark.sampleMemory().then(function(memory) {
      var keys = ["totalSize", "domSize", "styleSize", "jsObjectsSize", "jsStringsSize", "jsOtherSize", "otherSize"];
      var rows = [];
      rows.push(keys);
      rows.push(keys.map(function(k) { return memory[k] }));
      var RIGHT = Benchmark.RIGHT;
      var alignment = [RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT];
      console.log((J2ME.fromJavaString(label) || "Memory sample") + ":\n" + Benchmark.prettyTable(rows, alignment));
    }));
  }
};

Native["org/mozilla/Test.callSyncNative.()V"] = function() {
  // A noop sync implementation for comparison with the noop async one.
};

Native["org/mozilla/Test.callAsyncNative.()V"] = function() {
  // A noop async implementation for comparison with the noop sync one.
  asyncImpl("V", new Promise(function (resolve, reject) {
    resolve();
  }));

  // This is even faster, but not very handy, unless your native is really
  // synchronous, and you just want to force the thread to yield.
  // asyncImpl("V", Promise.resolve());
};

var readerOpened = false;
var readerOpenedWaiting = null;

Native["tests/recordstore/ReaderMIDlet.readerOpened.()V"] = function() {
  readerOpened = true;

  if (readerOpenedWaiting) {
    readerOpenedWaiting();
  }
};

Native["tests/recordstore/WriterMIDlet.waitReaderOpened.()V"] = function() {
  asyncImpl("V", new Promise(function(resolve, reject) {
    if (readerOpened) {
      resolve();
    } else {
      readerOpenedWaiting = resolve;
    }
  }));
};

var writerWrote = false;
var writerWroteWaiting = null;

Native["tests/recordstore/WriterMIDlet.writerWrote.()V"] = function() {
  writerWrote = true;

  if (writerWroteWaiting) {
    writerWroteWaiting();
  }
};

Native["tests/recordstore/ReaderMIDlet.waitWriterWrote.()V"] = function() {
  asyncImpl("V", new Promise(function(resolve, reject) {
    if (writerWrote) {
      resolve();
    } else {
      writerWroteWaiting = resolve;
    }
  }));
};
