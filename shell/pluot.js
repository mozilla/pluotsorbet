/**
 * Copyright 2014 Mozilla Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Bootstrap to start shell in spider monkey or node.

var inSpiderMonkey = typeof pc2line !== "undefined";
var startScript = "shell/shell.js";

if (inSpiderMonkey) {
  try {
    load(startScript);
  } catch (e) {
    print(e);
    // Spider monkey doesn't print stack traces by default.
    if (e.stack) {
      print(e.stack);
    }
    quit(1);
  }
} else {
  var vm = require("vm"),
      fs = require("fs");

  function evalScript() {
  for (var i = 0; i < arguments.length; i++) {
      var path = arguments[i];
      var code = fs.readFileSync(path).toString();
      vm.runInThisContext(code, {filename: path});
    }
  }
  global.evalScript = evalScript;
  global.dateNow = Date.now;
  global.scriptArgs = Array.prototype.slice.call(process.argv, 2);
  global.pc2line = function () {};
  global.snarf = function (path, type) {
    var buffer = fs.readFileSync(path);
    return type !== 'binary' ? buffer.toString() : new Uint8Array(buffer);
  };
  global.printErr = function (msg) {
    console.log(msg);
  };
  global.putstr = function (s) {
    process.stdout.write(s);
  };
  global.print = function (msg) {
    console.log(msg);
  };
  global.help = function () {
    // simulating SpiderMonkey interface
  };
  global.quit = function (code) {
    process.exit(code);
  };

  evalScript(startScript);
}