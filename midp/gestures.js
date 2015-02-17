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

Override["com/nokia/mid/ui/gestures/GestureEventImpl.<init>.(IIIIIFIIIIIIIIII)V"] =
function(type, dragDistanceX, dragDistanceY, startX, startY, flickDirection, flickSpeed, flickSpeedX, flickSpeedY,
         pinchDistanceStarting, pinchDistanceCurrent, pinchDistanceChange, pinchCenterX, pinchCenterY,
         pinchCenterChangeX, pinchCenterChangeY) {
  this.type = type;
  this.dragDistanceX = dragDistanceX;
  this.dragDistanceY = dragDistanceY;
  this.startX = startX;
  this.startY = startY;
  this.flickDirection = flickDirection;
  this.flickSpeed = flickSpeed;
  this.flickSpeedX = flickSpeedX;
  this.flickSpeedY = flickSpeedY;
  this.pinchDistanceStarting = pinchDistanceStarting;
  this.pinchDistanceCurrent = pinchDistanceCurrent;
  this.pinchDistanceChange = pinchDistanceChange;
  this.pinchCenterX = pinchCenterX;
  this.pinchCenterY = pinchCenterY;
  this.pinchCenterChangeX = pinchCenterChangeX;
  this.pinchCenterChangeY = pinchCenterChangeY;
};

Native["com/nokia/mid/ui/gestures/GestureEventImpl.getType.()I"] = function() {
  return this.type;
};

Native["com/nokia/mid/ui/gestures/GestureEventImpl.getDragDistanceX.()I"] = function() {
  return this.dragDistanceX;
};

Native["com/nokia/mid/ui/gestures/GestureEventImpl.getDragDistanceY.()I"] = function() {
  return this.dragDistanceY;
};

Native["com/nokia/mid/ui/gestures/GestureEventImpl.getStartX.()I"] = function() {
  return this.startX;
};

Native["com/nokia/mid/ui/gestures/GestureEventImpl.getStartY.()I"] = function() {
  return this.startY;
};

Native["com/nokia/mid/ui/gestures/GestureEventImpl.getFlickDirection.()F"] = function() {
  return this.flickDirection;
};

Native["com/nokia/mid/ui/gestures/GestureEventImpl.getFlickSpeed.()I"] = function() {
  return this.flickSpeed;
};

Native["com/nokia/mid/ui/gestures/GestureEventImpl.getFlickSpeedX.()I"] = function() {
  return this.flickSpeedX;
};

Native["com/nokia/mid/ui/gestures/GestureEventImpl.getFlickSpeedY.()I"] = function() {
  return this.flickSpeedY;
};

Native["com/nokia/mid/ui/gestures/GestureEventImpl.getPinchDistanceStarting.()I"] = function() {
  return this.pinchDistanceStarting;
};

Native["com/nokia/mid/ui/gestures/GestureEventImpl.getPinchDistanceCurrent.()I"] = function() {
  return this.pinchDistanceCurrent;
};

Native["com/nokia/mid/ui/gestures/GestureEventImpl.getPinchDistanceChange.()I"] = function() {
  return this.pinchDistanceChange;
};

Native["com/nokia/mid/ui/gestures/GestureEventImpl.getPinchCenterX.()I"] = function() {
  return this.pinchCenterX;
};

Native["com/nokia/mid/ui/gestures/GestureEventImpl.getPinchCenterY.()I"] = function() {
  return this.pinchCenterY;
};

Native["com/nokia/mid/ui/gestures/GestureEventImpl.getPinchCenterChangeX.()I"] = function() {
  return this.pinchCenterChangeX;
};

Native["com/nokia/mid/ui/gestures/GestureEventImpl.getPinchCenterChangeY.()I"] = function() {
  return this.pinchCenterChangeY;
};
