/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

function missingNativeImpl(key, ctx, stack) {
    console.error("Attempted to invoke missing native:", key);
}

var FieldInfo = J2ME.FieldInfo;
var MethodInfo = J2ME.MethodInfo;
var ClassInfo = J2ME.ClassInfo;

var ArrayClass = function(className, elementClass) {
    this.className = className;
    this.superClassName = "java/lang/Object";
    this.access_flags = 0;
    this.elementClass = elementClass;
    this.vmc = {};
    this.vfc = {};
}

ArrayClass.prototype.methods = [];

ArrayClass.prototype.isArrayClass = true;

ArrayClass.prototype.implementsInterface = function(iface) {
    return false;
}

ArrayClass.prototype.isAssignableTo = ClassInfo.prototype.isAssignableTo;

ArrayClass.prototype.getClassObject = ClassInfo.prototype.getClassObject;

function getOnce(objectKeyPairs, getter) {
  objectKeyPairs.forEach(function(pair) {
      var obj = pair[0];
      var key = pair[1];
      Object.defineProperty(obj, key, {
        get: function() {
          var value = getter();
          objectKeyPairs.forEach(function(pair) {
            Object.defineProperty(pair[0], pair[1], {
              value: value,
              configurable: true,
              enumerable: true
            });
          });
          return value;
        },
        configurable: true,
        enumerable: true
      });
    });
}


function trampoline(mangledClassName, mangledMethodName, mangledClassAndMethodName, className, methodKey, isStatic) {
    var objectKeyPairs = [];
    objectKeyPairs.push([jsGlobal, mangledClassAndMethodName]);
    if (!isStatic) {
      objectKeyPairs.push([jsGlobal[mangledClassName].prototype, mangledMethodName]);
    }
    getOnce(objectKeyPairs, function() {
        var classInfo = CLASSES.getClass(className);
        var methodInfo = CLASSES.getMethod(classInfo, methodKey);
        function native() {
            var alternateImpl = methodInfo.alternateImpl;
            try {
                // Async alternate functions push data back on the stack, but in the case of a compiled
                // function, the stack doesn't exist. To handle async, a bailout is triggered so the
                // compiled frame is replaced with an interpreted frame. The async function then can get the new
                // frame's stack from this callback.
                // TODO Refactor override so we don't have to slice here.
                var ctx = $.ctx;
                return alternateImpl.call(null, ctx, Array.prototype.slice.call(arguments), methodInfo.isStatic, function() {
                  assert(ctx.frameSets.length === 0, "There are still compiled frames.");
                  return ctx.current().stack;
                });
            } catch (e) {
                if (e === VM.Pause || e === VM.Yield) {
                    throw e;
                }
                throw new Error("TODO handle exceptions/yields in alternate impls. " + e + e.stack);
            }
        }

        function interpreter() {
            var frame = new Frame(methodInfo, [], 0);
            var ctx = $.ctx;
            var args = Array.prototype.slice.call(arguments);

            if (!methodInfo.isStatic) {
                args.unshift(this);
            }
            for (var i = 0; i < args.length; i++) {
                frame.setLocal(i, args[i]);
            }
            if (methodInfo.isSynchronized) {
                if (!frame.lockObject) {
                  frame.lockObject = methodInfo.isStatic
                    ? methodInfo.classInfo.getClassObject(ctx)
                    : frame.getLocal(0);
                }

                ctx.monitorEnter(frame.lockObject);
            }

            return ctx.executeNewFrameSet([frame]);
        }

        if (methodInfo.alternateImpl) {
          return native;
        }
        return interpreter;
    });
}
