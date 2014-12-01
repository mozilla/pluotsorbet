
module J2ME {
  export var CLASSES = new ClassRegistry();
  declare var util;

  import Isolate = com.sun.cldc.isolate.Isolate;
  export class JVM {
    constructor() {
      // ...
    }

    startIsolate0(className: string, args: string []) {
      var runtime = new Runtime(this);
      var ctx = new Context(runtime);
      ctx.setCurrent();

      var isolateClassInfo = CLASSES.getClass("com/sun/cldc/isolate/Isolate");
      var isolate: Isolate = <Isolate>newObject(isolateClassInfo.klass);

      isolate.id = util.id();

      var array = newStringArray(args.length);
      for (var n = 0; n < args.length; ++n)
        array[n] = args[n] ? util.newString(args[n]) : null;

      ctx.frames.push(new Frame(CLASSES.getMethod(isolateClassInfo, "I.<init>.(Ljava/lang/String;[Ljava/lang/String;)V"),
        [ isolate, util.newString(className.replace(/\./g, "/")), array ], 0));
      ctx.execute();

      ctx.frames.push(new Frame(CLASSES.getMethod(isolateClassInfo, "I.start.()V"), [ isolate ], 0));
      ctx.start();
    }

    startIsolate(isolate: Isolate) {
      var mainClass = fromJavaString(isolate.$_mainClass).replace(/\./g, "/");
      var mainArgs = isolate.$_mainArgs.map(fromJavaString);
      var runtime = new J2ME.Runtime(this);
      var ctx = new Context(runtime);
      ctx.setCurrent();

      isolate.runtime = runtime;
      runtime.isolate = isolate;

      runtime.updateStatus(RuntimeStatus.Started);

      var classInfo = CLASSES.getClass(mainClass);
      if (!classInfo)
        throw new Error("Could not find or load main class " + mainClass);

      var entryPoint = CLASSES.getEntryPoint(classInfo);
      if (!entryPoint)
        throw new Error("Could not find main method in class " + mainClass);

      ctx.thread = runtime.mainThread = util.newObject(CLASSES.java_lang_Thread);
      ctx.thread.pid = util.id();
      ctx.thread.alive = true;

      ctx.frames.push(new Frame(CLASSES.getMethod(CLASSES.java_lang_Thread, "I.<init>.(Ljava/lang/String;)V"),
        [ runtime.mainThread, util.newString("main") ], 0));
      ctx.execute();

      var args = J2ME.newStringArray(mainArgs.length);
      for (var n = 0; n < mainArgs.length; ++n)
        args[n] = mainArgs[n] ? util.newString(mainArgs[n]) : null;

      ctx.frames.push(new Frame(entryPoint, [ args ], 0));
      ctx.start();
    }

  }
}

Object.defineProperty(jsGlobal, "CLASSES", {
  get: function () {
    return J2ME.CLASSES;
  }
});

var JVM = J2ME.JVM;