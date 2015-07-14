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

declare function load(path: string);
declare var JARStore;
declare function snarf(path: string, mode: string): any;
declare var scriptArgs: string [];
declare function quit(code: number);

var config = {
  logConsole: "native",
  args: []
};

if (scriptArgs.length !== 1) {
  print("error: One main class name must be specified.");
  print("usage: jsshell <main class name>");
  quit(1);
}

module J2ME.Shell {
  var root = "./";
  load(root + "libs/relooper.js");
  load(root + "bld/j2me.js");
  load(root + "bld/classes.jar.js");
  load(root + "bld/main-all.js");

  JARStore.addBuiltIn("java/classes.jar", snarf("java/classes.jar", "binary").buffer);
  JARStore.addBuiltIn("tests/tests.jar", snarf("tests/tests.jar", "binary").buffer);
  JARStore.addBuiltIn("bench/benchmark.jar", snarf("bench/benchmark.jar", "binary").buffer);
  JARStore.addBuiltIn("program.jar", snarf("program.jar", "binary").buffer);

  CLASSES.initializeBuiltinClasses();

  var start = dateNow();
  var jvm = new JVM();

  J2ME.writers = J2ME.WriterFlags.All;
  start = dateNow();
  var runtime = jvm.startIsolate0(scriptArgs[0], config.args);

  //while (callbacks.length) {
  //  (callbacks.shift())();
  //}

  microTaskQueue.run(10000, 10000, true, function () {
    print("X");
    return true;
  });

  print("Time: " + (dateNow() - start).toFixed(4) + " ms");

}
