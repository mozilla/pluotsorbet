/*
 node-jvm
 Copyright (c) 2013 Yaroslav Gaponov <yaroslav.gaponov@gmail.com>
*/

module J2ME {
  export var CLASSES = new ClassRegistry();
  declare var util;

  import Isolate = com.sun.cldc.isolate.Isolate;
  export class JVM {
    constructor() {
      // ...
    }
    
    private createIsolateCtx(): Context {
      var runtime = new Runtime(this);
      var ctx = new Context(runtime);
      ctx.thread = runtime.mainThread = <java.lang.Thread>newObject(CLASSES.java_lang_Thread.klass);
      ctx.thread.pid = util.id();
      ctx.thread.alive = true;
      // The constructor will set the real priority, however one is needed for the scheduler.
      ctx.thread.priority = NORMAL_PRIORITY;
      runtime.preInitializeClasses(ctx);
      return ctx;
    }

    startIsolate0(className: string, args: string []) {
      var ctx = this.createIsolateCtx();

      var isolateClassInfo = CLASSES.getClass("com/sun/cldc/isolate/Isolate");
      var isolate: Isolate = <Isolate>newObject(isolateClassInfo.klass);
      isolate.id = util.id();

      var array = newStringArray(args.length);
      for (var n = 0; n < args.length; ++n)
        array[n] = args[n] ? J2ME.newString(args[n]) : null;

      // The <init> frames go at the end of the array so they are executed first to initialize the thread and isolate.
      ctx.start([
        Frame.create(isolateClassInfo.getMethodByNameString("start", "()V"), [ isolate ]),
        Frame.create(isolateClassInfo.getMethodByNameString("<init>", "(Ljava/lang/String;[Ljava/lang/String;)V"),
                                       [ isolate, J2ME.newString(className.replace(/\./g, "/")), array ])
      ]);
      release || Debug.assert(!U, "Unexpected unwind during isolate initialization.");
    }

    startIsolate(isolate: Isolate) {
      var ctx = this.createIsolateCtx();
      var runtime = ctx.runtime;
      isolate.runtime = runtime;
      runtime.isolate = isolate;

      runtime.updateStatus(RuntimeStatus.Started);
      runtime.priority = isolate._priority;

      var mainClass = J2ME.fromJavaString(isolate._mainClass).replace(/\./g, "/");
      var mainArgs = isolate._mainArgs;
      var classInfo = CLASSES.getClass(mainClass);
      linkKlass(classInfo);
      if (!classInfo)
        throw new Error("Could not find or load main class " + mainClass);

      var entryPoint = CLASSES.getEntryPoint(classInfo);
      if (!entryPoint)
        throw new Error("Could not find main method in class " + mainClass);

      var args = J2ME.newStringArray(mainArgs.length);
      for (var n = 0; n < mainArgs.length; ++n) {
        args[n] = mainArgs[n];
      }

      var frames = [Frame.create(entryPoint, [ args ])];

      var clinit = classInfo.staticInitializer;
      if (clinit) {
        frames.push(Frame.create(clinit, []));
      }

      frames.push(Frame.create(CLASSES.java_lang_Thread.getMethodByNameString("<init>", "(Ljava/lang/String;)V"),
                               [ runtime.mainThread, J2ME.newString("main") ]));

      ctx.start(frames);
      release || Debug.assert(!U, "Unexpected unwind during isolate initialization.");
    }

  }
}

Object.defineProperty(jsGlobal, "CLASSES", {
  get: function () {
    return J2ME.CLASSES;
  }
});

var JVM = J2ME.JVM;
