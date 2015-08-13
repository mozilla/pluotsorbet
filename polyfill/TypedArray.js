'use strict';

(function () {
  // Per https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/TypedArray,
  // "all typed array prototypes (TypedArray.prototype) have %TypedArray%.prototype as their [[Prototype]]."
  // So it doesn't matter which TypedArray constructor we use to dereference
  // the prototype.
  var proto = Object.getPrototypeOf(Int8Array.prototype);

  if (!("fill" in proto)) {
    proto.fill = function(value, start, end) {
      if ((typeof start) === "undefined") {
        start = 0;
      }

      if ((typeof end) === "undefined") {
        end = this.length;
      }

      for (var i = start; i < end; i++) {
        this[i] = value;
      }

      return this;
    };
  }

})();
