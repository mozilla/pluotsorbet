/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var FrameAnimator = function() {};

FrameAnimator.numRegistered = 0;

FrameAnimator.prototype._isRegistered = false;

FrameAnimator.prototype.register = function(x, y, maxFps, maxPps, listener) {
  this.x = x;
  this.y = y;
  this.maxFps = maxFps;
  this.maxPps = maxPps;
  this.listener = listener;
  this._isRegistered = true;
  ++FrameAnimator.numRegistered;
};

FrameAnimator.prototype.unregister = function() {
  this.x = null;
  this.y = null;
  this.maxFps = null;
  this.maxPps = null;
  this.listener = null;
  this._isRegistered = false;
  --FrameAnimator.numRegistered;
};

FrameAnimator.prototype.isRegistered = function() {
  return this._isRegistered;
};

Native.create("com/nokia/mid/ui/frameanimator/FrameAnimator.init.()V", function(ctx) {
  this.nativeObject = new FrameAnimator();
});

Native.create("com/nokia/mid/ui/frameanimator/FrameAnimator.register.(IISSLcom/nokia/mid/ui/frameanimator/FrameAnimatorListener;)Z", function(ctx, x, y, maxFps, maxPps, listener) {
  if (this.nativeObject.isRegistered()) {
    throw new JavaException("java/lang/IllegalStateException", "FrameAnimator already registered");
  }

  if (!listener) {
    throw new JavaException("java/lang/NullPointerException", "listener is null");
  }

  if (x < -65535 || x > 65535 || y < -65535 || y > 65535) {
    throw new JavaException("java/lang/IllegalArgumentException", "coordinate out of bounds");
  }

  // XXX return false if FrameAnimator.numRegistered >= FRAME_ANIMATOR_MAX_CONCURRENT

  this.nativeObject.register(x, y, maxFps, maxPps, listener);
  return true;
});

Native.create("com/nokia/mid/ui/frameanimator/FrameAnimator.unregister.()V", function(ctx) {
  if (!this.nativeObject.isRegistered()) {
    throw new JavaException("java/lang/IllegalStateException", "FrameAnimator not registered");
  }

  this.nativeObject.unregister();
});

Native.create("com/nokia/mid/ui/frameanimator/FrameAnimator.drag.(II)V", function(ctx, x, y) {
  console.warn("FrameAnimator.drag(II)V not implemented (" + x + ", " + y + ")");
});

Native.create("com/nokia/mid/ui/frameanimator/FrameAnimator.kineticScroll.(IIIF)V", function(ctx, speed, direction, friction, angle) {
  console.warn("FrameAnimator.kineticScroll(IIIF)V not implemented (" +
               speed + ", " + direction + ", " + friction + ", " + angle + ")");
});

Native.create("com/nokia/mid/ui/frameanimator/FrameAnimator.limitedKineticScroll.(IIIFII)V", function(ctx, speed, direction, friction, angle, limitUp, limitDown) {
  console.warn("FrameAnimator.limitedKineticScroll(IIIFII)V not implemented (" +
               speed + ", " + direction + ", " + friction + ", " + angle + ", " + limitUp + ", " + limitDown + ")");
});

Native.create("com/nokia/mid/ui/frameanimator/FrameAnimator.stop.()V", function(ctx) {
  console.warn("FrameAnimator.stop()V not implemented");
});

Native.create("com/nokia/mid/ui/frameanimator/FrameAnimator.isRegistered.()Z", function(ctx) {
  return this.nativeObject.isRegistered();
});

Native.create("com/nokia/mid/ui/frameanimator/FrameAnimator.getNumRegisteredFrameAnimators.()I", function(ctx) {
  return FrameAnimator.numRegistered;
});
