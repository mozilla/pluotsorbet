/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

Override["com/nokia/mid/ui/gestures/GestureInteractiveZone.<init>.(I)V"] = function(gestures) {
  this.gestures = gestures;
};

Native["com/nokia/mid/ui/gestures/GestureInteractiveZone.setRectangle.(IIII)V"] = function(x, y, width, height) {
  this.rect = {
    x: x,
    y: y,
    width: width,
    height: height,
  };
};

Native["com/nokia/mid/ui/gestures/GestureInteractiveZone.contains.(II)Z"] = function(x, y) {
  if (!this.rect ||
      x >= this.rect.x && x <= (this.rect.x + this.rect.width) &&
      y >= this.rect.y && y <= (this.rect.y + this.rect.height)) {
    return 1;
  }

  return 0;
};

Native["com/nokia/mid/ui/gestures/GestureInteractiveZone.supports.(I)Z"] = function(type) {
  return ((type & this.gestures) == type) ? 1 : 0;
};