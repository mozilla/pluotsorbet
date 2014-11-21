/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var FieldInfo = (function() {
    var idgen = 0;
    return function(classInfo, access_flags, name, signature) {
        this.classInfo = classInfo;
        this.access_flags = access_flags;
        this.name = name;
        this.signature = signature;
        this.id = idgen++;
        this.isStatic = ACCESS_FLAGS.isStatic(access_flags);
        this.mangledName = J2ME.C4.Backend.mangleField(this);
    }
})();

FieldInfo.prototype.get = function(obj) {
    var value = obj[this.id];
    if (typeof value === "undefined") {
        value = util.defaultValue(this.signature);
    }
    return value;
}

FieldInfo.prototype.set = function(obj, value) {
    obj[this.id] = value;
}

FieldInfo.prototype.toString = function() {
    return "[field " + this.name + "]";
}

function missingNativeImpl(key, ctx, stack) {
    console.error("Attempted to invoke missing native:", key);
}

/**
 * Required params:
 *   - name
 *   - signature
 *   - classInfo
 *
 * Optional params:
 *   - attributes (defaults to [])
 *   - code (if not provided, pulls from attributes)
 *   - isNative, isPublic, isStatic, isSynchronized
 */
function MethodInfo(opts) {
    this.name = opts.name;
    this.signature = opts.signature;
    this.classInfo = opts.classInfo;
    this.attributes = opts.attributes || [];

    // Use code if provided, otherwise search for the code within attributes.
    if (opts.code) {
        this.code = opts.code;
        this.exception_table = [];
        this.max_locals = undefined; // Unused for now.
    } else {
        for (var i = 0; i < this.attributes.length; i++) {
            var a = this.attributes[i];
            if (a.info.type === ATTRIBUTE_TYPES.Code) {
                this.code = new Uint8Array(a.info.code);
                this.exception_table = a.info.exception_table;
                this.max_locals = a.info.max_locals;
                this.max_stack = a.info.max_stack;
                break;
            }
        }
    }

    this.isNative = opts.isNative;
    this.isPublic = opts.isPublic;
    this.isStatic = opts.isStatic;
    this.isSynchronized = opts.isSynchronized;
    this.key = (this.isStatic ? "S." : "I.") + this.name + "." + this.signature;
    this.implKey = this.classInfo.className + "." + this.name + "." + this.signature;

    if (this.isNative) {
        if (this.implKey in Native) {
            this.alternateImpl = Native[this.implKey];
        } else {
            // Some Native MethodInfos are constructed but never called;
            // that's fine, unless we actually try to call them.
            this.alternateImpl = missingNativeImpl.bind(null, this.implKey);
        }
    } else if (this.implKey in Override) {
        this.alternateImpl = Override[this.implKey];
    } else {
        this.alternateImpl = null;

        if (typeof CC !== "undefined") {
            var compiledMethod = null;
            var classMangledName = J2ME.C4.Backend.mangleClass(this.classInfo);
            var compiledClass = CC[classMangledName];
            if (compiledClass) {
                var methodMangledName = J2ME.C4.Backend.mangleMethod(this);
                compiledMethod = compiledClass.methods[methodMangledName];
                if (this.isStatic) {
                    jsGlobal[methodMangledName] = compiledMethod;
                }
                console.log("HERE: " + compiledMethod + " : ");

            }
            this.fn = compiledMethod;
        }
    }

    this.mangledName = J2ME.C4.Backend.mangleMethod(this);
    this.mangledClassAndMethodName = J2ME.C4.Backend.mangleClassAndMethod(this);

    this.consumes = Signature.getINSlots(this.signature);
    if (!this.isStatic) {
      this.consumes++;
    }
}

var ClassInfo = function(classBytes) {
    var classImage = getClassImage(classBytes, this);
    var cp = classImage.constant_pool;
    this.className = cp[cp[classImage.this_class].name_index].bytes;
    this.superClassName = classImage.super_class ? cp[cp[classImage.super_class].name_index].bytes : null;
    this.access_flags = classImage.access_flags;
    this.constant_pool = cp;
    // Cache for virtual methods and fields
    this.vmc = {};
    this.vfc = {};

    this.mangledName = J2ME.C4.Backend.mangleClass(this);

    if (jsGlobal[this.mangledName]) {
        this.constructor = jsGlobal[this.mangledName];
    } else {
        this.constructor = function () {};
    }
    this.constructor.prototype.class = this;
    this.constructor.prototype.toString = function() {
        return '[instance ' + this.class.className + ']';
    };


    var self = this;

    this.interfaces = [];
    classImage.interfaces.forEach(function(i) {
        var int = CLASSES.loadClass(cp[cp[i].name_index].bytes);
        self.interfaces.push(int);
        self.interfaces = self.interfaces.concat(int.interfaces);
    });

    this.fields = [];
    classImage.fields.forEach(function(f) {
        var field = new FieldInfo(self, f.access_flags, cp[f.name_index].bytes, cp[f.descriptor_index].bytes);
        f.attributes.forEach(function(attribute) {
            if (cp[attribute.attribute_name_index].bytes === "ConstantValue")
                field.constantValue = new DataView(attribute.info).getUint16(0, false);
        });
        self.fields.push(field);
    });

    this.methods = [];
    classImage.methods.forEach(function(m) {
        self.methods.push(new MethodInfo({
            name: cp[m.name_index].bytes,
            signature: cp[m.signature_index].bytes,
            classInfo: self,
            attributes: m.attributes,
            isNative: ACCESS_FLAGS.isNative(m.access_flags),
            isPublic: ACCESS_FLAGS.isPublic(m.access_flags),
            isStatic: ACCESS_FLAGS.isStatic(m.access_flags),
            isSynchronized: ACCESS_FLAGS.isSynchronized(m.access_flags)
        }));
    });

    var classes = this.classes = [];
    classImage.attributes.forEach(function(a) {
        if (a.info.type === ATTRIBUTE_TYPES.InnerClasses) {
            a.info.classes.forEach(function(c) {
                classes.push(cp[cp[c.inner_class_info_index].name_index].bytes);
                if (c.outer_class_info_index)
                    classes.push(cp[cp[c.outer_class_info_index].name_index].bytes);
            });
        }
    });
}

ClassInfo.prototype.implementsInterface = function(iface) {
    var classInfo = this;
    do {
        var interfaces = classInfo.interfaces;
        for (var n = 0; n < interfaces.length; ++n) {
            if (interfaces[n] === iface)
                return true;
        }
        classInfo = classInfo.superClass;
    } while (classInfo);
    return false;
}

ClassInfo.prototype.isAssignableTo = function(toClass) {
    if (this === toClass || toClass === ClassInfo.java_lang_Object)
        return true;
    if (ACCESS_FLAGS.isInterface(toClass.access_flags) && this.implementsInterface(toClass))
        return true;
    if (this.elementClass && toClass.elementClass)
        return this.elementClass.isAssignableTo(toClass.elementClass);
    return this.superClass ? this.superClass.isAssignableTo(toClass) : false;
}

ClassInfo.prototype.getClassObject = function(ctx) {
    var className = this.className;
    var classObjects = ctx.runtime.classObjects;
    var classObject = classObjects[className];
    if (!classObject) {
        classObject = util.newObject(CLASSES.java_lang_Class);
        classObject.vmClass = this;
        classObjects[className] = classObject;
    }
    return classObject;
}

ClassInfo.prototype.getField = function(fieldKey) {
    return CLASSES.getField(this, fieldKey);
}

ClassInfo.prototype.toString = function() {
    return "[class " + this.className + "]";
}

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

function getOnce(obj, key, getter) {
    Object.defineProperty(obj, key, {
      get: function() {
        var value = getter();
        Object.defineProperty(obj, key, {
          value: value,
          configurable: true,
          enumerable: true
        });
        return value;
      },
      configurable: true,
      enumerable: true
    });
}


function trampoline(obj, key, className, methodKey) {
    getOnce(obj, key, function() {
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
                var ctx = arguments[0];
                var args = Array.prototype.slice.call(arguments, 2);
                return alternateImpl.call(null, ctx, args, methodInfo.isStatic, function() {
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
            var ctx = arguments[0];
            ctx.frames.push(frame);
            for (var i = 2; i < arguments.length; i++) {
                frame.setLocal(i - 2, arguments[i]);
            }
            if (methodInfo.isSynchronized) {
                if (!frame.lockObject) {
                  frame.lockObject = methodInfo.isStatic
                    ? methodInfo.classInfo.getClassObject(ctx)
                    : frame.getLocal(0);
                }

                ctx.monitorEnter(frame.lockObject);
            }
            return VM.execute(ctx);
        }
    
        if (methodInfo.alternateImpl) {
          return native;
        }
        return interpreter;
    });
}
