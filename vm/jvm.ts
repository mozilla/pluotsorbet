
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
      ctx.setAsCurrentContext();

      var isolateClassInfo = CLASSES.getClass("com/sun/cldc/isolate/Isolate");

      linkKlass(isolateClassInfo);
      var isolate: Isolate = <Isolate>newObject(isolateClassInfo.klass);

      isolate.id = util.id();

      var array = newStringArray(args.length);
      for (var n = 0; n < args.length; ++n)
        array[n] = args[n] ? J2ME.newString(args[n]) : null;

      ctx.executeFrames([
        Frame.create(CLASSES.getMethod(isolateClassInfo, "I.<init>.(Ljava/lang/String;[Ljava/lang/String;)V"),
                  [ isolate, J2ME.newString(className.replace(/\./g, "/")), array ], 0)
      ]);

      ctx.start(Frame.create(CLASSES.getMethod(isolateClassInfo, "I.start.()V"), [ isolate ], 0));
    }

    startIsolate(isolate: Isolate) {
      var mainClass = util.fromJavaString(isolate.klass.classInfo.getField("I._mainClass.Ljava/lang/String;").get(isolate)).replace(/\./g, "/");
      var mainArgs = isolate.klass.classInfo.getField("I._mainArgs.[Ljava/lang/String;").get(isolate);
      var runtime = new J2ME.Runtime(this);
      var ctx = new Context(runtime);

      isolate.runtime = runtime;
      runtime.isolate = isolate;

      runtime.updateStatus(RuntimeStatus.Started);

      var classInfo = CLASSES.getClass(mainClass);
      linkKlass(classInfo);
      if (!classInfo)
        throw new Error("Could not find or load main class " + mainClass);

      var entryPoint = CLASSES.getEntryPoint(classInfo);
      if (!entryPoint)
        throw new Error("Could not find main method in class " + mainClass);

      ctx.thread = runtime.mainThread = <java.lang.Thread>newObject(CLASSES.java_lang_Thread.klass);
      ctx.thread.pid = util.id();
      ctx.thread.alive = true;

      var oldCtx = $.ctx;
      ctx.setAsCurrentContext();
      ctx.executeFrames([Frame.create(CLASSES.getMethod(CLASSES.java_lang_Thread, "I.<init>.(Ljava/lang/String;)V"),
                              [ runtime.mainThread, J2ME.newString("main") ], 0)])
      oldCtx.setAsCurrentContext();

      var args = J2ME.newStringArray(mainArgs.length);
      for (var n = 0; n < mainArgs.length; ++n) {
        args[n] = mainArgs[n];
      }

      ctx.start(Frame.create(entryPoint, [ args ], 0));
    }

  }
}

Object.defineProperty(jsGlobal, "CLASSES", {
  get: function () {
    return J2ME.CLASSES;
  }
});

var JVM = J2ME.JVM;
